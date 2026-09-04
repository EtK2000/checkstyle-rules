package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags redundant type casts: same-type casts,
 * null casts in a context that already fixes the target type, and
 * widening primitive casts that Java performs implicitly.
 */
public class RedundantCastCheck extends AbstractAstCheck {
	/**
	 * A redundant cast found at a located position, and the nodes a rewrite has to consume around it.
	 *
	 * <p>{@code subject} is the expression the cast applies to. {@code wrapOpen} and {@code wrapClose}
	 * are the grouping parentheses that removing the cast makes redundant, both null when there are
	 * none to remove.
	 *
	 * <p>{@code castType} and {@code subjectType} are the names the message reports, the second being
	 * {@code "null"} for a null cast.
	 */
	public record RedundantCastTarget(
			@Nonnull DetailAST typecast,
			@Nonnull DetailAST subject,
			@Nullable DetailAST wrapOpen,
			@Nullable DetailAST wrapClose,
			@Nonnull String castType,
			@Nonnull String subjectType
	) {}

	private static final String MSG_KEY = "redundant.cast";
	// resolving one operand scans the whole member it sits in, and a member holds many casts, so
	// without this a file costs O(casts x member size): measured at 3.9x per doubling of method
	// length. Entries are per member; CACHED_UNIT is the generation that drops them all when a new
	// compilation unit arrives, so a finished file's tree is not held
	private static final ThreadLocal<Map<DetailAST, Map<String, String>>> CACHED_TYPES = ThreadLocal.withInitial(HashMap::new);
	private static final ThreadLocal<DetailAST> CACHED_UNIT = new ThreadLocal<>();

	/**
	 * Appends {@code node} and its descendants as they were written, in source order. Imaginary
	 * grouping nodes carry their own token name as text rather than source, so they contribute
	 * nothing; a real token whose text would fuse with what precedes it gets a space, which keeps
	 * {@code <? extends Number>} distinct from {@code <? super Number>} (both carry the bound on a
	 * node that also has a child, so a leaf-only walk would render them alike).
	 */
	private static void appendSourceTokens(@Nonnull StringBuilder sb, @Nonnull DetailAST node) {
		// iterative to match AstText.exprText and AstText.tokens, whose nesting is bounded only by
		// the source. Measured, the depth that breaks a cast of nested generics breaks the same
		// file with no cast in it at all, so what gives way is checkstyle's own walk rather than
		// this one; the form is a convention here, not a fix for a reachable overflow
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(node);
		while (!pending.isEmpty()) {
			final var current = pending.pop();
			if (!AstText.isImaginaryToken(current)) {
				final var text = current.getText();
				if (!text.isEmpty()) {
					if (!sb.isEmpty()
							&& LineText.isIdentifierPart(sb.charAt(sb.length() - 1))
							&& LineText.isIdentifierPart(text.charAt(0)))
						sb.append(' ');
					sb.append(text);
				}
			}

			final var children = new ArrayDeque<DetailAST>();
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				children.push(child);
			for (var child : children)
				pending.push(child);
		}
	}

	/**
	 * The cast's type name, or {@code null} when the cast has no single type name. An intersection
	 * cast ({@code (Runnable & Serializable) r}) has one {@code TYPE} child per conjunct, so reading
	 * the first one names only part of the cast: removing such a cast narrows the expression to that
	 * conjunct, which does not compile wherever another conjunct is what the context needs.
	 */
	@CheckReturnValue
	@Nullable
	private static String castTypeName(@Nonnull DetailAST typecast) {
		if (typecast.findFirstToken(TokenTypes.TYPE_EXTENSION_AND) != null)
			return null;
		final var type = typecast.findFirstToken(TokenTypes.TYPE);
		return type != null ? typeName(type) : null;
	}

	/**
	 * Drops this thread's resolution cache. {@link #finishTree} brackets the check's own run, but the
	 * {@link #targetAt} seam has no such bracket, so the fix pass clears this the way it clears the
	 * parse it handed in rather than leaving a pooled thread holding a finished file's tree.
	 */
	public static void clearCache() {
		// remove rather than get().clear(): get() installs a map through withInitial, so clearing
		// would leave an entry, sized by the largest file seen, on a thread that never used the check
		CACHED_TYPES.remove();
		CACHED_UNIT.remove();
	}

	/**
	 * Adds every declaration of {@code name} in {@code root}'s subtree to {@code out}. A lambda's
	 * sole parameter written without parentheses is a bare {@code IDENT} child of the lambda rather
	 * than a declarator node, so it is matched separately.
	 */
	private static void collectDeclarations(
			@Nonnull DetailAST root,
			@Nonnull String name,
			@Nonnull List<DetailAST> out
	) {
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(root);
		while (!pending.isEmpty()) {
			final var current = pending.pop();
			if (declaresName(current, name))
				out.add(current);
			else if (current.getType() == TokenTypes.LAMBDA) {
				for (var child = current.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getType() == TokenTypes.IDENT && name.equals(child.getText()))
						out.add(child);
				}
			}

			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
	}

	@CheckReturnValue
	@Nullable
	private static String contextTargetType(@Nonnull DetailAST typecast, boolean walkTernary) {
		var parent = typecast.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR)
			parent = parent.getParent();
		if (parent == null)
			return null;

		if (walkTernary && parent.getType() == TokenTypes.QUESTION) {
			var outer = parent.getParent();
			while (outer != null && outer.getType() == TokenTypes.EXPR)
				outer = outer.getParent();
			if (outer != null)
				return resolveAssignOrReturn(outer);
			return null;
		}

		return resolveAssignOrReturn(parent);
	}

	@CheckReturnValue
	private static boolean declaresName(@Nonnull DetailAST node, @Nonnull String name) {
		final var declares = switch (node.getType()) {
			case TokenTypes.PARAMETER_DEF, TokenTypes.PATTERN_VARIABLE_DEF,
			     TokenTypes.RECORD_COMPONENT_DEF, TokenTypes.RESOURCE,
			     TokenTypes.VARIABLE_DEF -> true;
			default -> false;
		};
		if (!declares)
			return false;

		final var declared = node.findFirstToken(TokenTypes.IDENT);
		return declared != null && name.equals(declared.getText());
	}

	/** The nearest named type enclosing {@code ast}, or {@code null} when there is none. */
	@CheckReturnValue
	@Nullable
	private static String enclosingClassName(@Nonnull DetailAST ast) {
		var node = ast.getParent();
		while (node != null) {
			if (node.getType() == TokenTypes.CLASS_DEF
					|| node.getType() == TokenTypes.ENUM_DEF
					|| node.getType() == TokenTypes.INTERFACE_DEF
					|| node.getType() == TokenTypes.RECORD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				return ident != null ? ident.getText() : null;
			}
			node = node.getParent();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String expressionType(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.CHAR_LITERAL -> "char";
			case TokenTypes.EXPR -> {
				final var child = expr.getFirstChild();
				yield child != null ? expressionType(child) : null;
			}
			case TokenTypes.IDENT -> lookupVariableType(expr);
			case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
			case TokenTypes.LITERAL_NEW -> AstText.findNewClassName(expr);
			case TokenTypes.LITERAL_THIS -> enclosingClassName(expr);
			case TokenTypes.NUM_DOUBLE -> "double";
			case TokenTypes.NUM_FLOAT -> {
				// NUM_FLOAT is any floating-point literal, not only Java float (f/F)
				final var text = expr.getText();
				yield text.endsWith("f") || text.endsWith("F") ? "float" : "double";
			}
			case TokenTypes.NUM_INT -> "int";
			case TokenTypes.NUM_LONG -> "long";
			case TokenTypes.STRING_LITERAL -> "String";
			case TokenTypes.TYPECAST -> castTypeName(expr);
			default -> null;
		};
	}

	/**
	 * Checks if a widening cast is inside a compound assignment
	 * ({@code +=}, {@code -=}, etc.) where it is redundant. The compound
	 * operator's own implicit cast makes the explicit one redundant only
	 * when the cast reaches the same value by the same route. Three ways
	 * it does not: a non-primitive target, where {@code +=} selects string
	 * concatenation rather than a numeric conversion and {@code s += (int) c}
	 * appends {@code "120"} where {@code s += c} appends {@code "x"}; a cast
	 * wider than the target, where the arithmetic happens in the cast's type
	 * and is narrowed back, so {@code f += (double) i} and {@code f += i}
	 * differ above 2^24; and a cast that rounds on the way
	 * ({@link #roundsThroughTheCast}), where {@code d += (float) i} rounds
	 * {@code i} to a {@code float} before the addition and
	 * {@code d += i} converts it exactly.
	 */
	@CheckReturnValue
	private static boolean isCompoundAssignment(@Nonnull DetailAST typecast, @Nonnull String castType) {
		var parent = typecast.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR)
			parent = parent.getParent();
		if (parent == null)
			return false;

		final var compound = switch (parent.getType()) {
			case TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			     TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN,
			     TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN,
			     TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN,
			     TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN,
			     TokenTypes.STAR_ASSIGN -> true;
			default -> false;
		};
		if (!compound)
			return false;

		final var target = parent.getFirstChild();
		final var targetType = target != null && target.getType() == TokenTypes.IDENT
				? lookupVariableType(target)
				: null;
		return targetType != null
				&& isPrimitive(targetType)
				&& !isWideningPrimitive(targetType, castType)
				&& !roundsThroughTheCast(castType, targetType);
	}

	@CheckReturnValue
	private static boolean isOrDescendant(@Nonnull DetailAST node, @Nonnull DetailAST ancestor) {
		for (var p = node; p != null; p = p.getParent()) {
			if (p == ancestor)
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isPrimitive(@Nonnull String type) {
		return switch (type) {
			case "boolean", "byte", "char", "double", "float",
			     "int", "long", "short" -> true;
			default -> false;
		};
	}

	/**
	 * Checks if a widening cast is redundant because the sibling operand
	 * in a binary operator already has the cast type or wider, making
	 * the binary numeric promotion handle the widening automatically.
	 * For example, {@code (long) x * 100L} is redundant because
	 * {@code 100L} already promotes the multiplication to {@code long}.
	 * Excludes shift operators where only the left operand determines
	 * the result type.
	 */
	@CheckReturnValue
	private static boolean isSiblingAlreadyWiderOrEqual(@Nonnull DetailAST typecast, @Nonnull String castType) {
		final var parent = typecast.getParent();
		if (parent == null)
			return false;

		return switch (parent.getType()) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BXOR,
			     TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE,
			     TokenTypes.GT, TokenTypes.LE, TokenTypes.LT,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL,
			     TokenTypes.PLUS, TokenTypes.STAR -> {
				final var sibling = parent.getFirstChild() == typecast
						? typecast.getNextSibling()
						: parent.getFirstChild();
				if (sibling == null)
					yield false;
				final var siblingType = expressionType(sibling);
				if (roundsThroughTheCast(castType, siblingType))
					yield false;
				yield castType.equals(siblingType)
						|| (siblingType != null && isWideningPrimitive(castType, siblingType));
			}
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isTernarySiblingAlreadyWiderOrEqual(@Nonnull DetailAST typecast, @Nonnull String castType) {
		var parent = typecast.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR)
			parent = parent.getParent();
		if (parent == null || parent.getType() != TokenTypes.QUESTION)
			return false;

		// QUESTION children: condition, true-branch, COLON, false-branch
		final var condition = parent.getFirstChild();
		if (condition == null)
			return false;
		final var trueBranch = condition.getNextSibling();
		if (trueBranch == null)
			return false;
		var falseBranch = trueBranch.getNextSibling();
		if (falseBranch != null && falseBranch.getType() == TokenTypes.COLON)
			falseBranch = falseBranch.getNextSibling();
		if (falseBranch == null)
			return false;

		final var other = isOrDescendant(typecast, trueBranch) ? falseBranch : trueBranch;
		final var otherType = expressionType(other);
		return !roundsThroughTheCast(castType, otherType)
				&& (castType.equals(otherType)
				|| (otherType != null && isWideningPrimitive(castType, otherType)));
	}

	@CheckReturnValue
	private static boolean isWideningPrimitive(@Nonnull String fromType, @Nonnull String toType) {
		return switch (fromType) {
			case "byte" -> switch (toType) {
				case "double", "float", "int", "long", "short" -> true;
				default -> false;
			};
			case "char" -> switch (toType) {
				case "double", "float", "int", "long" -> true;
				default -> false;
			};
			case "float" -> "double".equals(toType);
			case "int" -> switch (toType) {
				case "double", "float", "long" -> true;
				default -> false;
			};
			case "long" -> "double".equals(toType) || "float".equals(toType);
			case "short" -> switch (toType) {
				case "double", "float", "int", "long" -> true;
				default -> false;
			};
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isWideningRedundantInContext(@Nonnull DetailAST typecast, @Nonnull String castType) {
		final var targetType = contextTargetType(typecast, true);
		return targetType != null
				&& isPrimitive(targetType)
				&& !roundsThroughTheCast(castType, targetType);
	}

	/**
	 * The declared type of what {@code ident} names, or {@code null} when that cannot be decided.
	 *
	 * <p>A name is resolved only when exactly one declaration of it is visible: the member the use
	 * sits in, plus the enclosing type's fields and record components. More than one and the answer
	 * depends on scoping this does not model, since a pattern variable's scope follows the flow of
	 * its condition, a resource's covers only the try block, and a local's starts at its own
	 * declarator. Guessing there reads an operand as a type it does not have, and the fix that
	 * follows deletes a cast the code needs. An input that compiles cannot mis-resolve a name with
	 * a single declaration, so the one-declaration rule costs only shadowed names.
	 */
	@CheckReturnValue
	@Nullable
	private static String lookupVariableType(@Nonnull DetailAST ident) {
		final var name = ident.getText();

		var member = ident;
		var objBlock = ident.getParent();
		while (objBlock != null && objBlock.getType() != TokenTypes.OBJBLOCK) {
			member = objBlock;
			objBlock = objBlock.getParent();
		}

		var unit = objBlock == null ? member : objBlock;
		while (unit.getParent() != null)
			unit = unit.getParent();
		if (CACHED_UNIT.get() != unit) {
			CACHED_TYPES.get().clear();
			CACHED_UNIT.set(unit);
		}

		final var perMember = CACHED_TYPES.get().computeIfAbsent(member, k -> new HashMap<>());
		if (perMember.containsKey(name))
			return perMember.get(name);

		final var resolved = resolveVariableType(member, objBlock, name);
		perMember.put(name, resolved);
		return resolved;
	}

	@CheckReturnValue
	@Nullable
	private static String methodReturnType(@Nonnull DetailAST returnNode) {
		var parent = returnNode.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.METHOD_DEF) {
				final var type = parent.findFirstToken(TokenTypes.TYPE);
				return type != null ? typeName(type) : null;
			}
			if (parent.getType() == TokenTypes.CLASS_DEF
					|| parent.getType() == TokenTypes.LAMBDA
					|| parent.getType() == TokenTypes.RECORD_DEF)
				return null;
			parent = parent.getParent();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String resolveAssignOrReturn(@Nonnull DetailAST parent) {
		if (parent.getType() == TokenTypes.ASSIGN) {
			final var grandparent = parent.getParent();
			if (grandparent != null && grandparent.getType() == TokenTypes.VARIABLE_DEF)
				return variableDefType(grandparent);
			if (grandparent != null && grandparent.getType() == TokenTypes.EXPR) {
				final var target = parent.getFirstChild();
				if (target != null && target.getType() == TokenTypes.IDENT)
					return lookupVariableType(target);
			}
		}
		if (parent.getType() == TokenTypes.LITERAL_RETURN)
			return methodReturnType(parent);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String resolveVariableType(
			@Nonnull DetailAST member,
			@Nullable DetailAST objBlock,
			@Nonnull String name
	) {
		final var declarations = new ArrayList<DetailAST>();
		collectDeclarations(member, name, declarations);

		if (objBlock != null) {
			for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (declaresName(child, name))
					declarations.add(child);
			}

			final var typeDef = objBlock.getParent();
			final var components = typeDef == null
					? null
					: typeDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
			if (components != null) {
				for (var child = components.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (declaresName(child, name))
						declarations.add(child);
				}
			}
		}

		return declarations.size() == 1 ? variableDefType(declarations.getFirst()) : null;
	}

	/**
	 * Whether binary numeric promotion to {@code otherType} would reach a different value with the
	 * cast than without it. Promotion to the other operand's type happens either way, but the cast
	 * rounds first: {@code (float) i * 2.0} rounds {@code i} to 24 bits of mantissa before widening
	 * to {@code double}, while {@code i * 2.0} converts it exactly. At {@code i == 16777217} the two
	 * differ. Only a {@code float} cast under a {@code double} sibling loses anything, since every
	 * other promotion a wider sibling can force is exact for the types reaching it.
	 */
	@CheckReturnValue
	private static boolean roundsThroughTheCast(@Nonnull String castType, @Nullable String otherType) {
		return "float".equals(castType) && "double".equals(otherType);
	}

	/**
	 * Returns the redundant cast at {@code line} (0-based) and {@code column} (a 0-based code-point
	 * column), or null when no reportable cast starts exactly there.
	 */
	@CheckReturnValue
	@Nullable
	public static RedundantCastTarget targetAt(@Nonnull DetailAST root, int line, int column) {
		final var typecast = AstQuery.findNodeAt(root, line, column, ast -> ast.getType() == TokenTypes.TYPECAST);
		return typecast == null ? null : targetFor(typecast);
	}

	@CheckReturnValue
	@Nullable
	private static RedundantCastTarget targetFor(@Nonnull DetailAST typecast) {
		final var castType = castTypeName(typecast);
		if (castType == null)
			return null;

		final var rparen = typecast.findFirstToken(TokenTypes.RPAREN);
		final var subject = rparen != null ? rparen.getNextSibling() : null;
		if (subject == null)
			return null;

		// null is assignable to any reference type; (Type) null is only
		// meaningful in method/constructor args (overload resolution) or
		// var declarations (type inference), so flag it everywhere else
		if (subject.getType() == TokenTypes.LITERAL_NULL) {
			final var targetType = contextTargetType(typecast, false);
			return targetType != null && !"var".equals(targetType)
					? targeting(typecast, subject, castType, "null")
					: null;
		}

		final var subjectType = expressionType(subject);
		if (subjectType == null)
			return null;

		if (castType.equals(subjectType))
			return targeting(typecast, subject, castType, subjectType);

		return isWideningPrimitive(subjectType, castType)
				&& (isCompoundAssignment(typecast, castType) || isWideningRedundantInContext(typecast, castType) || isSiblingAlreadyWiderOrEqual(typecast, castType) || isTernarySiblingAlreadyWiderOrEqual(typecast, castType))
				? targeting(typecast, subject, castType, subjectType)
				: null;
	}

	/**
	 * The target for a cast already decided to be redundant, with the grouping parentheses its
	 * removal makes redundant attached. Those are the cast's own siblings: {@code ((String) o).length()}
	 * parses as {@code DOT{LPAREN, TYPECAST, RPAREN, IDENT}}, while {@code use((String) o)} and
	 * {@code switch ((char) c)} leave the cast alone under an {@code EXPR}, so a parenthesis belonging
	 * to the call or the selector is never read as a grouping one. Nested pairs are followed out to
	 * the outermost, each being redundant once the cast goes.
	 */
	@CheckReturnValue
	@Nonnull
	private static RedundantCastTarget targeting(
			@Nonnull DetailAST typecast,
			@Nonnull DetailAST subject,
			@Nonnull String castType,
			@Nonnull String subjectType
	) {
		DetailAST wrapOpen = null;
		DetailAST wrapClose = null;
		// a cast binds looser than the `.` a wrap can stand to the left of, so a subject that is
		// itself a cast keeps its parentheses: `((String) (String) s).length()` would otherwise
		// become `(String) s.length()`, which casts the length rather than the string
		if (subject.getType() != TokenTypes.TYPECAST) {
			var open = typecast.getPreviousSibling();
			var close = typecast.getNextSibling();
			while (open != null && close != null
					&& open.getType() == TokenTypes.LPAREN
					&& close.getType() == TokenTypes.RPAREN) {
				wrapOpen = open;
				wrapClose = close;
				open = open.getPreviousSibling();
				close = close.getNextSibling();
			}
		}
		return new RedundantCastTarget(typecast, subject, wrapOpen, wrapClose, castType, subjectType);
	}

	/**
	 * The type as written, carrying its type arguments and array brackets, or {@code null} when it
	 * has no such spelling. The arguments are part of the name because the comparison this feeds is
	 * by name: dropping them makes {@code (List<String>) raw} over a raw {@code List} read as a
	 * same-type cast, and removing that cast leaves an expression of a different type, so a consumer
	 * that needs the argument ({@code for (String s : ...)}) stops compiling. Two identical
	 * spellings in one scope denote one type, so comparing them is sound; two spellings of one type
	 * ({@code List} against {@code java.util.List}) compare unequal and the check stays quiet, which
	 * is the safe direction.
	 */
	@CheckReturnValue
	@Nullable
	private static String typeName(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild == null)
			return null;

		final var baseName = switch (firstChild.getType()) {
			case TokenTypes.IDENT -> firstChild.getText();
			case TokenTypes.LITERAL_BOOLEAN -> "boolean";
			case TokenTypes.LITERAL_BYTE -> "byte";
			case TokenTypes.LITERAL_CHAR -> "char";
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			case TokenTypes.LITERAL_SHORT -> "short";
			default -> null;
		};
		if (baseName == null)
			return null;

		// TYPE's children are in source order, so arguments and brackets append where they belong
		final var sb = new StringBuilder(baseName);
		for (var child = firstChild.getNextSibling(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				sb.append("[]");
			else if (child.getType() == TokenTypes.TYPE_ARGUMENTS)
				appendSourceTokens(sb, child);
			// a sibling with no rendering is part of a type this cannot name, and naming the stump
			// is what makes a multi-catch's `A | B` read as a bare `A`. ANNOTATIONS is the one
			// sibling that changes no type: `(String @Ann []) o` is still a `String[]`
			else if (child.getType() != TokenTypes.ANNOTATIONS)
				return null;
		}
		return sb.toString();
	}

	@CheckReturnValue
	@Nullable
	private static String variableDefType(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		return type != null ? typeName(type) : null;
	}

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		clearCache();
	}

	@Override
	public void finishTree(@Nonnull DetailAST rootAST) {
		clearCache();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.TYPECAST};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast);
		if (target != null)
			log(ast, MSG_KEY, target.castType(), target.subjectType());
	}
}