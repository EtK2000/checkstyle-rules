package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Which fields of a class body may trade places. Two fields keep their relative order when one
 * reads the other ({@link FieldSortingCheck#fieldDependencies}), or when they are
 * {@linkplain #pinned pinned}: one initializer may observe the order, and the other does
 * something its position could change. What an initializer reads or runs, and what that code
 * touches, cannot be decided from the source, so only an initializer built from literals alone is
 * trusted ({@link #isLiteral}); every read, call, lambda and {@code new} counts as observing the
 * order.
 */
public final class FieldInitializers {
	/**
	 * One class body's enum constants and fields in source order, with what decides whether two
	 * of them may trade places: whether each is static, its {@link Kind}, the fields it reads, the
	 * later fields its initializer reads while it runs ({@link FieldSortingCheck#initTimeForwardReads}).
	 * An enum constant's reads of the other constants ({@link #constantReads}) are among the last
	 * two. {@code path} names the body by what owns it ({@link #ownerPath}), which moving fields and
	 * enum constants leaves as it is.
	 */
	public record BodyOrder(
			@Nonnull String path,
			@Nonnull List<String> names,
			@Nonnull List<Boolean> statics,
			@Nonnull List<Kind> kinds,
			@Nonnull Map<String, Set<String>> reads,
			@Nonnull Map<String, Set<String>> forwardReads
	) {}

	/** What a field's initializer does when it runs. */
	public enum Kind {
		/**
		 * No initializer, or a constant variable, whose value javac inlines: the field reads the
		 * same wherever it is declared, and its declaration runs nothing.
		 */
		INERT,
		/**
		 * Built from literals alone ({@link #isLiteral}): it computes the same value wherever it is
		 * declared, and observes no other field.
		 */
		LITERAL,
		/**
		 * Anything that is not {@link #LITERAL}: a read, a call, a lambda, a {@code new}, a write, any
		 * other syntax, and every enum constant, whose order code anywhere may depend on.
		 */
		RUNS_CODE
	}

	private static final int MAX_LITERAL_DIMENSION = 1 << 16;
	// the declarations whose first identifier child is the name they declare, never a read
	private static final Set<Integer> DECLARATIONS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.ANNOTATION_FIELD_DEF,
			TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
			TokenTypes.CLASS_DEF,
			TokenTypes.CTOR_DEF,
			TokenTypes.ENUM_CONSTANT_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.LABELED_STAT,
			TokenTypes.METHOD_DEF,
			TokenTypes.PARAMETER_DEF,
			TokenTypes.PATTERN_VARIABLE_DEF,
			TokenTypes.RECORD_COMPONENT_DEF,
			TokenTypes.RECORD_DEF,
			TokenTypes.VARIABLE_DEF
	);
	// what names a method calls, a statement jumps to, or a type, never a read of a field
	private static final Set<Integer> NAME_ONLY = Set.of(
			TokenTypes.ANNOTATION,
			TokenTypes.ANNOTATIONS,
			TokenTypes.LITERAL_BREAK,
			TokenTypes.LITERAL_CONTINUE,
			TokenTypes.TYPE,
			TokenTypes.TYPE_ARGUMENTS
	);
	private static final Set<Integer> LITERAL_TOKENS = Set.of(
			TokenTypes.ARRAY_DECLARATOR,
			TokenTypes.ARRAY_INIT,
			TokenTypes.BAND,
			TokenTypes.BNOT,
			TokenTypes.BOR,
			TokenTypes.BSR,
			TokenTypes.BXOR,
			TokenTypes.CHAR_LITERAL,
			TokenTypes.COLON,
			TokenTypes.COMMA,
			TokenTypes.DIV,
			TokenTypes.EQUAL,
			TokenTypes.EXPR,
			TokenTypes.GE,
			TokenTypes.GT,
			TokenTypes.LAND,
			TokenTypes.LE,
			TokenTypes.LITERAL_FALSE,
			TokenTypes.LITERAL_NULL,
			TokenTypes.LITERAL_TRUE,
			TokenTypes.LNOT,
			TokenTypes.LOR,
			TokenTypes.LPAREN,
			TokenTypes.LT,
			TokenTypes.MINUS,
			TokenTypes.MOD,
			TokenTypes.NOT_EQUAL,
			TokenTypes.NUM_DOUBLE,
			TokenTypes.NUM_FLOAT,
			TokenTypes.NUM_INT,
			TokenTypes.NUM_LONG,
			TokenTypes.PLUS,
			TokenTypes.QUESTION,
			TokenTypes.RBRACK,
			TokenTypes.RCURLY,
			TokenTypes.RPAREN,
			TokenTypes.SL,
			TokenTypes.SR,
			TokenTypes.STAR,
			TokenTypes.STRING_LITERAL,
			TokenTypes.TEXT_BLOCK_CONTENT,
			TokenTypes.TEXT_BLOCK_LITERAL_BEGIN,
			TokenTypes.TEXT_BLOCK_LITERAL_END,
			TokenTypes.UNARY_MINUS,
			TokenTypes.UNARY_PLUS
	);
	private static final Set<Integer> TYPE_DEFINITIONS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.RECORD_DEF
	);
	private static final Set<Integer> OWNERS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.CTOR_DEF,
			TokenTypes.ENUM_CONSTANT_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.METHOD_DEF,
			TokenTypes.RECORD_DEF,
			TokenTypes.VARIABLE_DEF
	);

	private static void addIdents(@Nonnull DetailAST node, @Nonnull Set<DetailAST> idents) {
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(node);
		while (!pending.isEmpty()) {
			final var current = pending.pop();
			if (current.getType() == TokenTypes.IDENT)
				idents.add(current);
			pushChildren(current, pending);
		}
	}

	/**
	 * For each of {@code constants} (one enum's constants in source order) that reads a later one
	 * by bare name among its own arguments ({@link #constantReads}), an illegal forward reference
	 * that does not compile, the first such constant.
	 */
	@CheckReturnValue
	@Nonnull
	public static Map<String, String> bareForwardReads(@Nonnull List<DetailAST> constants) {
		final var positions = positionsOf(constants);
		final var forward = new HashMap<String, String>();
		for (var entry : constantReads(constants).entrySet()) {
			final var reader = positions.get(entry.getKey());
			for (var read : entry.getValue().entrySet()) {
				final var at = positions.get(read.getKey());
				final var first = forward.get(entry.getKey());
				if (read.getValue() && at > reader && (first == null || at < positions.get(first)))
					forward.put(entry.getKey(), read.getKey());
			}
		}
		return forward;
	}

	/** {@code scope} with {@code names} added, as a new set when there are any. */
	@CheckReturnValue
	@Nonnull
	private static Set<String> bindingAlso(@Nonnull Set<String> scope, @Nonnull List<String> names) {
		if (names.isEmpty())
			return scope;
		final var bound = new HashSet<>(scope);
		bound.addAll(names);
		return bound;
	}

	@CheckReturnValue
	@Nonnull
	private static BodyOrder bodyOrder(@Nonnull DetailAST objBlock, @Nonnull String path) {
		final var members = new ArrayList<DetailAST>();
		final var constants = new ArrayList<DetailAST>();
		final var fields = new ArrayList<DetailAST>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
				members.add(child);
				constants.add(child);
			}
			else if (child.getType() == TokenTypes.VARIABLE_DEF) {
				members.add(child);
				fields.add(child);
			}
		}
		final var names = new ArrayList<String>(members.size());
		final var statics = new ArrayList<Boolean>(members.size());
		for (var member : members) {
			names.add(FieldSortingCheck.fieldName(member));
			statics.add(isStaticMember(member));
		}
		final var reads = new HashMap<>(FieldSortingCheck.fieldDependencies(fields));
		final var forwardReads = new HashMap<>(FieldSortingCheck.initTimeForwardReads(fields));
		final var positions = positionsOf(constants);
		for (var entry : constantReads(constants).entrySet()) {
			for (var read : entry.getValue().keySet()) {
				reads.computeIfAbsent(entry.getKey(), name -> new HashSet<>()).add(read);
				if (positions.get(read) > positions.get(entry.getKey()))
					forwardReads.computeIfAbsent(entry.getKey(), name -> new HashSet<>()).add(read);
			}
		}
		return new BodyOrder(path, names, statics, kindsOf(members), reads, forwardReads);
	}

	/**
	 * Every class body of the tree under {@code root}, in tree order, as a {@link BodyOrder}. A
	 * body's {@link BodyOrder#path} is its {@link #ownerPath}, numbered from the second body of
	 * the same path on in tree order ({@code T/a/<new>#2}), which a rewrite that only moves fields
	 * and enum constants keeps, whatever it does to the order of the bodies those carry.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<BodyOrder> bodyOrders(@Nonnull DetailAST root) {
		final var orders = new ArrayList<BodyOrder>();
		final var seen = new HashMap<String, Integer>();
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(root);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (node.getType() == TokenTypes.OBJBLOCK) {
				final var path = ownerPath(node);
				final var count = seen.merge(path, 1, Integer::sum);
				orders.add(bodyOrder(node, count == 1 ? path : path + "#" + count));
			}
			final var children = new ArrayList<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				children.add(child);
			for (var i = children.size() - 1; i >= 0; --i)
				pending.push(children.get(i));
		}
		return orders;
	}

	/**
	 * The names of {@code names}, the constants of the enum whose body is {@code enumBody}, that
	 * the tree under {@code node} reads, each mapped to whether a read of it is bare. A name that
	 * names no field ({@link #nonReads}) is no read, and neither is a {@code case} label
	 * ({@link #isCaseLabel}): one naming an enum constant names the selector's and reads nothing.
	 */
	@CheckReturnValue
	@Nonnull
	private static Map<String, Boolean> constantNamesRead(@Nonnull DetailAST node, @Nonnull DetailAST enumBody, @Nonnull Set<String> names) {
		final var reads = new HashMap<String, Boolean>();
		final var nonReads = nonReads(node);
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(node);
		while (!pending.isEmpty()) {
			final var current = pending.pop();
			pushChildren(current, pending);
			if (current.getType() != TokenTypes.IDENT || !names.contains(current.getText()) || nonReads.contains(current) || isCaseLabel(current))
				continue;
			final var parent = current.getParent();
			final var selected = parent.getType() == TokenTypes.DOT && parent.getLastChild() == current;
			if (!selected)
				reads.put(current.getText(), true);
			else if (isOwnName(parent.getFirstChild(), enumBody))
				reads.putIfAbsent(current.getText(), false);
		}
		return reads;
	}

	/**
	 * For each of {@code constants} (one enum's constants in source order), the other constants
	 * creating it reads, each mapped to whether a read of it is a bare name among the constant's
	 * own arguments, which ahead of its declaration is an illegal forward reference
	 * ({@link #bareForwardReads}). A read is a bare name, or one qualified by the enum's name, in
	 * the constant's arguments or body, or in the enum's constructors, instance initializers and
	 * instance field initializers, which run for every constant.
	 */
	@CheckReturnValue
	@Nonnull
	public static Map<String, Map<String, Boolean>> constantReads(@Nonnull List<DetailAST> constants) {
		final var reads = new HashMap<String, Map<String, Boolean>>();
		if (constants.isEmpty())
			return reads;
		final var enumBody = constants.getFirst().getParent();
		final var names = positionsOf(constants).keySet();
		final var shared = new HashSet<String>();
		for (var member = enumBody.getFirstChild(); member != null; member = member.getNextSibling()) {
			final var instanceCode = member.getType() == TokenTypes.CTOR_DEF
					|| member.getType() == TokenTypes.INSTANCE_INIT
					|| member.getType() == TokenTypes.VARIABLE_DEF && !isStaticMember(member);
			if (instanceCode)
				shared.addAll(constantNamesRead(member, enumBody, names).keySet());
		}
		for (var constant : constants) {
			final var name = FieldSortingCheck.fieldName(constant);
			final var own = new HashMap<String, Boolean>();
			final var arguments = constant.findFirstToken(TokenTypes.ELIST);
			if (arguments != null)
				own.putAll(constantNamesRead(arguments, enumBody, names));
			final var body = constant.findFirstToken(TokenTypes.OBJBLOCK);
			if (body != null) {
				for (var read : constantNamesRead(body, enumBody, names).keySet())
					own.putIfAbsent(read, false);
			}
			for (var read : shared)
				own.putIfAbsent(read, false);
			own.remove(name);
			if (!own.isEmpty())
				reads.put(name, own);
		}
		return reads;
	}

	/** The names the declarations among the children of {@code node} of type {@code type} declare. */
	@CheckReturnValue
	@Nonnull
	private static List<String> declaredBy(@Nullable DetailAST node, int type) {
		final var names = new ArrayList<String>();
		for (var child = node == null ? null : node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == type && declaresName(child))
				names.add(FieldSortingCheck.fieldName(child));
		}
		return names;
	}

	/**
	 * True when {@code declaration}, a variable, parameter or resource, declares a name: every one
	 * does but a resource naming an existing variable ({@code try (rate)}), which reads it.
	 */
	@CheckReturnValue
	private static boolean declaresName(@Nonnull DetailAST declaration) {
		return declaration.getType() != TokenTypes.RESOURCE || declaration.findFirstToken(TokenTypes.TYPE) != null;
	}

	/**
	 * True when every dimension the {@code new} {@code creation} gives is a non-negative int literal,
	 * and the dimensions multiplied up to the first 0, past which nothing more is allocated, come to
	 * at most {@link #MAX_LITERAL_DIMENSION}: any other may throw ({@code new int[-1]},
	 * {@code new int[0][0xFFFFFFFF]}, an {@code OutOfMemoryError} for a huge one) instead of
	 * computing a value.
	 */
	@CheckReturnValue
	private static boolean hasSafeDimensions(@Nonnull DetailAST creation) {
		var cells = 1L;
		for (var declarator = creation.getFirstChild(); declarator != null; declarator = declarator.getNextSibling()) {
			final var dimension = declarator.getType() == TokenTypes.ARRAY_DECLARATOR ? declarator.findFirstToken(TokenTypes.EXPR) : null;
			if (dimension == null)
				continue;
			final var literal = dimension.getFirstChild();
			if (literal.getType() != TokenTypes.NUM_INT || literal.getNextSibling() != null)
				return false;
			final var digits = literal.getText().replace("_", "").toLowerCase(Locale.ROOT);
			final long value;
			try {
				value = digits.startsWith("0b") ? Long.parseLong(digits.substring(2), 2) : Long.decode(digits);
			}
			catch (NumberFormatException e) {
				return false;
			}
			// a hex, octal or binary int literal past Integer.MAX_VALUE is negative (0xFFFFFFFF is -1),
			// and a negative dimension throws even past a 0 one
			if (value > Integer.MAX_VALUE || cells > 0 && value > MAX_LITERAL_DIMENSION)
				return false;
			cells *= value;
			if (cells > MAX_LITERAL_DIMENSION)
				return false;
		}
		return true;
	}

	/**
	 * True when the identifier {@code ident} is a whole {@code case} label ({@code case RED ->}),
	 * which names either a constant variable, read like any other name, or a constant of the
	 * selector's enum, which no field of the class is, whatever its name: javac resolves it
	 * against that enum alone (JLS 14.11.1).
	 */
	@CheckReturnValue
	public static boolean isCaseLabel(@Nonnull DetailAST ident) {
		final var label = ident.getParent();
		return label.getType() == TokenTypes.EXPR
				&& label.getFirstChild() == ident
				&& ident.getNextSibling() == null
				&& label.getParent().getType() == TokenTypes.LITERAL_CASE;
	}

	/**
	 * True when {@code expression}, an initializer or part of one, is built from literals alone:
	 * literals and text blocks; unary, binary, conditional and comparison operators; parentheses;
	 * casts; and array creation ({@linkplain #hasSafeDimensions dimensions that cannot throw}) and
	 * initializers. A name of any kind, a call, a {@code new} of anything but an array, a lambda, a method reference, a switch, an {@code instanceof}, an
	 * assignment, an increment or decrement, and a division or remainder that may be by zero
	 * ({@link #mayDivideByZero}) all make it something else.
	 */
	@CheckReturnValue
	static boolean isLiteral(@Nonnull DetailAST expression) {
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(expression);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (mayDivideByZero(node))
				return false;
			if (LITERAL_TOKENS.contains(node.getType())) {
				pushChildren(node, pending);
				continue;
			}
			switch (node.getType()) {
				case TokenTypes.LITERAL_NEW -> {
					if (node.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null && node.findFirstToken(TokenTypes.ARRAY_INIT) == null
							|| !hasSafeDimensions(node))
						return false;
					for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
						if (child.getType() == TokenTypes.ARRAY_DECLARATOR || child.getType() == TokenTypes.ARRAY_INIT)
							pending.push(child);
					}
				}
				case TokenTypes.TYPECAST -> {
					final var type = node.findFirstToken(TokenTypes.TYPE);
					if (type == null)
						return false;
					for (var child = type.getNextSibling(); child != null; child = child.getNextSibling())
						pending.push(child);
				}
				default -> {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * True when {@code qualifier} spells the name of the type whose body is {@code body}: simply,
	 * through its enclosing types, or through its package as well. An anonymous class body, an
	 * enum constant's among them, has no name to spell.
	 */
	@CheckReturnValue
	static boolean isOwnName(@Nonnull DetailAST qualifier, @Nonnull DetailAST body) {
		if (qualifier.getType() != TokenTypes.IDENT && qualifier.getType() != TokenTypes.DOT || !TYPE_DEFINITIONS.contains(body.getParent().getType()))
			return false;
		final var spelled = FullIdent.createFullIdent(qualifier).getText();
		final var names = new ArrayList<String>();
		for (var node = body.getParent(); node != null; node = node.getParent()) {
			if (TYPE_DEFINITIONS.contains(node.getType()))
				names.addFirst(node.findFirstToken(TokenTypes.IDENT).getText());
		}
		for (var i = names.size() - 1; i >= 0; --i) {
			if (spelled.equals(String.join(".", names.subList(i, names.size()))))
				return true;
		}
		var root = body;
		while (root.getParent() != null)
			root = root.getParent();
		// a check sees the file's top-level nodes as siblings, a parse of it as children of one unit
		var packageDef = root.findFirstToken(TokenTypes.PACKAGE_DEF);
		for (var node = root; packageDef == null && node != null; node = node.getPreviousSibling()) {
			if (node.getType() == TokenTypes.PACKAGE_DEF)
				packageDef = node;
		}
		return packageDef != null
				&& spelled.equals(FullIdent.createFullIdent(packageDef.getLastChild().getPreviousSibling()).getText() + "." + String.join(".", names));
	}

	/** True when {@code member}, a field or an enum constant, is initialized with the class rather than with an instance. */
	@CheckReturnValue
	private static boolean isStaticMember(@Nonnull DetailAST member) {
		return member.getType() == TokenTypes.ENUM_CONSTANT_DEF || FieldSortingCheck.classifyField(member).isStatic();
	}

	/**
	 * True when {@code after}, a rewrite of the class body {@code before} describes, keeps every
	 * pair of same-group members (fields, and enum constants, which share the static group) that
	 * may not trade places in its order: a {@linkplain #pinned pinned} pair, whatever else holds
	 * between the two; a member and an earlier one it reads, which it stays below; and a member and
	 * a later one it reads while it is initialized (a constant reading a later one included), which
	 * stays below it. A reader above a field it reads by bare name, which does not compile, may
	 * move below it when the two are not pinned. False as well when the two hold different members.
	 *
	 * <p>The reads are checked one by one, the pins in one pass per group: a member running code
	 * must stay below every earlier non-{@link Kind#INERT} one, and any other non-inert member
	 * below every earlier one running code.
	 */
	@CheckReturnValue
	public static boolean keepsOrder(@Nonnull BodyOrder before, @Nonnull BodyOrder after) {
		if (!new HashSet<>(before.names()).equals(new HashSet<>(after.names())) || before.names().size() != after.names().size())
			return false;
		final var position = new HashMap<String, Integer>();
		for (var i = 0; i < after.names().size(); ++i)
			position.put(after.names().get(i), i);
		final var count = before.names().size();
		final var at = new int[count];
		final var index = new HashMap<String, Integer>();
		for (var i = 0; i < count; ++i) {
			at[i] = position.get(before.names().get(i));
			index.putIfAbsent(before.names().get(i), i);
		}
		final var lowestLive = new HashMap<Boolean, Integer>();
		final var lowestRunning = new HashMap<Boolean, Integer>();
		for (var i = 0; i < count; ++i) {
			final var kind = before.kinds().get(i);
			if (kind == Kind.INERT)
				continue;
			final var group = before.statics().get(i);
			final var above = (kind == Kind.RUNS_CODE ? lowestLive : lowestRunning).getOrDefault(group, -1);
			if (at[i] <= above)
				return false;
			lowestLive.merge(group, at[i], Math::max);
			if (kind == Kind.RUNS_CODE)
				lowestRunning.merge(group, at[i], Math::max);
		}
		return keepsReads(before, at, index, before.reads(), false) && keepsReads(before, at, index, before.forwardReads(), true);
	}

	/**
	 * True when no read of {@code reads}, of {@code before}'s members, has moved to the wrong side
	 * of its reader in the rewrite placing each member, by its index in {@code before}, at
	 * {@code at} ({@code index} giving each name's first index): an earlier member read stays above
	 * its reader, and when {@code forward}, a later one stays below it.
	 */
	@CheckReturnValue
	private static boolean keepsReads(
			@Nonnull BodyOrder before,
			@Nonnull int[] at,
			@Nonnull Map<String, Integer> index,
			@Nonnull Map<String, Set<String>> reads,
			boolean forward
	) {
		for (var entry : reads.entrySet()) {
			final var reader = index.get(entry.getKey());
			for (var read : entry.getValue()) {
				final var target = index.get(read);
				if (reader == null || target == null || target.equals(reader) || target > reader != forward || !before.statics().get(target).equals(before.statics().get(reader)))
					continue;
				if (forward ? at[target] <= at[reader] : at[reader] <= at[target])
					return false;
			}
		}
		return true;
	}

	/**
	 * What initializing {@code member} does when it runs: a field declarator by its initializer;
	 * an enum constant always runs code, since code anywhere may depend on the constants' order.
	 */
	@CheckReturnValue
	@Nonnull
	public static Kind kindOf(@Nonnull DetailAST member) {
		if (member.getType() == TokenTypes.ENUM_CONSTANT_DEF)
			return Kind.RUNS_CODE;
		if (!FieldSortingCheck.isInitializedAtRuntime(member))
			return Kind.INERT;
		return isLiteral(member.findFirstToken(TokenTypes.ASSIGN).getFirstChild()) ? Kind.LITERAL : Kind.RUNS_CODE;
	}

	/** The {@link #kindOf kinds} of {@code members}, in their order. */
	@CheckReturnValue
	@Nonnull
	public static List<Kind> kindsOf(@Nonnull List<DetailAST> members) {
		final var kinds = new ArrayList<Kind>(members.size());
		for (var member : members)
			kinds.add(kindOf(member));
		return kinds;
	}

	/**
	 * True when {@code node} is a division or remainder that may be integral and by zero, so may
	 * throw instead of computing a value: one whose divisor, past its parentheses and signs, is
	 * neither a nonzero integer or long literal ({@code 1 / 2}, {@code 7 % (-3L)}) nor a floating
	 * one ({@code 1.0 / 0.0}, which is infinite). Such a division is neither a constant expression
	 * nor a literal whose place does not matter: {@code 1 / 0}, {@code 1 / (1 - 1)}, {@code 1 / '\0'}.
	 */
	@CheckReturnValue
	static boolean mayDivideByZero(@Nonnull DetailAST node) {
		if (node.getType() != TokenTypes.DIV && node.getType() != TokenTypes.MOD)
			return false;
		var divisor = node.getLastChild();
		while (divisor != null && (divisor.getType() == TokenTypes.RPAREN || divisor.getType() == TokenTypes.LPAREN))
			divisor = divisor.getPreviousSibling();
		while (divisor != null && (divisor.getType() == TokenTypes.UNARY_MINUS || divisor.getType() == TokenTypes.UNARY_PLUS))
			divisor = unparenthesized(divisor.getFirstChild());
		if (divisor != null && (divisor.getType() == TokenTypes.NUM_DOUBLE || divisor.getType() == TokenTypes.NUM_FLOAT))
			return false;
		if (divisor == null || divisor.getType() != TokenTypes.NUM_INT && divisor.getType() != TokenTypes.NUM_LONG)
			return true;
		var digits = divisor.getText().replace("_", "").toLowerCase(Locale.ROOT);
		if (digits.endsWith("l"))
			digits = digits.substring(0, digits.length() - 1);
		if (digits.startsWith("0x") || digits.startsWith("0b"))
			digits = digits.substring(2);
		return digits.chars().allMatch(digit -> digit == '0');
	}

	/** True when the identifier {@code ident} names a method being called ({@code m()}, {@code a.m()}) or referred to ({@code A::m}). */
	@CheckReturnValue
	private static boolean namesMethod(@Nonnull DetailAST ident) {
		final var parent = ident.getParent();
		return switch (parent.getType()) {
			case TokenTypes.DOT -> parent.getLastChild() == ident
					&& parent.getParent().getType() == TokenTypes.METHOD_CALL
					&& parent.getParent().getFirstChild() == parent;
			case TokenTypes.METHOD_CALL -> parent.getFirstChild() == ident;
			case TokenTypes.METHOD_REF -> parent.getLastChild() == ident;
			default -> false;
		};
	}

	/**
	 * True when the identifier {@code ident} names no variable it reads, by where it sits: the
	 * name a declaration, a label or an annotation element declares, a lambda's lone parameter, a
	 * resource it declares, or a method being called or referred to.
	 */
	@CheckReturnValue
	private static boolean namesNoRead(@Nonnull DetailAST ident) {
		final var parent = ident.getParent();
		return switch (parent.getType()) {
			case TokenTypes.DOT, TokenTypes.METHOD_CALL, TokenTypes.METHOD_REF -> namesMethod(ident);
			case TokenTypes.LAMBDA -> ident.getNextSibling() != null;
			case TokenTypes.RESOURCE -> declaresName(parent) && parent.findFirstToken(TokenTypes.IDENT) == ident;
			default -> DECLARATIONS.contains(parent.getType()) && parent.findFirstToken(TokenTypes.IDENT) == ident;
		};
	}

	/**
	 * The identifiers under {@code root}, an initializer or an initializer block, that read no
	 * field: the names a declaration, a label, an annotation, a type, a method call or a jump
	 * spells ({@link #namesNoRead}), and the bare names {@code root} binds itself where javac's
	 * scope for them is unconditional: a lambda's or a method's parameters, a local declared earlier
	 * in an enclosing block, and a for, catch or resource variable inside its own statement. A
	 * pattern variable's reads are not among them, since where it is in scope depends on how its
	 * test came out, nor is a member a dot selects, which never names a local.
	 */
	@CheckReturnValue
	@Nonnull
	public static Set<DetailAST> nonReads(@Nonnull DetailAST root) {
		final var idents = Collections.newSetFromMap(new IdentityHashMap<DetailAST, Boolean>());
		final var nodes = new ArrayDeque<DetailAST>();
		final var scopes = new ArrayDeque<Set<String>>();
		nodes.push(root);
		scopes.push(Set.of());
		while (!nodes.isEmpty()) {
			final var node = nodes.pop();
			final var scope = scopes.pop();
			if (node.getType() == TokenTypes.IDENT) {
				final var selected = node.getParent().getType() == TokenTypes.DOT && node.getParent().getLastChild() == node;
				if (namesNoRead(node) || !selected && scope.contains(node.getText()))
					idents.add(node);
				continue;
			}
			if (NAME_ONLY.contains(node.getType()) || node.getType() == TokenTypes.DOT && node.getLastChild().getType() == TokenTypes.LITERAL_CLASS) {
				addIdents(node, idents);
				continue;
			}
			// the names each child sees, given what the node binds and where
			final List<String> bound;
			DetailAST from = null;
			switch (node.getType()) {
				case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
					bound = declaredBy(node.findFirstToken(TokenTypes.PARAMETERS), TokenTypes.PARAMETER_DEF);
					from = node.findFirstToken(TokenTypes.SLIST);
				}
				case TokenTypes.LAMBDA -> {
					final var first = node.getFirstChild();
					bound = first != null && first.getType() == TokenTypes.IDENT && first.getNextSibling() != null
							? List.of(first.getText())
							: declaredBy(node.findFirstToken(TokenTypes.PARAMETERS), TokenTypes.PARAMETER_DEF);
					from = node.getLastChild();
				}
				case TokenTypes.LITERAL_CATCH -> {
					bound = declaredBy(node, TokenTypes.PARAMETER_DEF);
					from = node.findFirstToken(TokenTypes.SLIST);
				}
				case TokenTypes.LITERAL_FOR -> {
					final var init = node.findFirstToken(TokenTypes.FOR_INIT);
					final var clause = init != null ? init : node.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
					bound = declaredBy(clause, TokenTypes.VARIABLE_DEF);
					from = clause == null ? null : clause.getNextSibling();
				}
				case TokenTypes.LITERAL_TRY -> {
					final var specification = node.findFirstToken(TokenTypes.RESOURCE_SPECIFICATION);
					bound = specification == null ? List.of() : declaredBy(specification.findFirstToken(TokenTypes.RESOURCES), TokenTypes.RESOURCE);
					from = node.findFirstToken(TokenTypes.SLIST);
				}
				default -> bound = List.of();
			}
			final var sequential = node.getType() == TokenTypes.SLIST || node.getType() == TokenTypes.FOR_INIT || node.getType() == TokenTypes.RESOURCES;
			var current = scope;
			var reached = false;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				reached |= child == from;
				// the type a `new` creates, its arguments being inside its ELIST
				if (node.getType() == TokenTypes.LITERAL_NEW && (child.getType() == TokenTypes.IDENT || child.getType() == TokenTypes.DOT)) {
					addIdents(child, idents);
					continue;
				}
				final var catchOrTryBody = node.getType() == TokenTypes.LITERAL_CATCH || node.getType() == TokenTypes.LITERAL_TRY;
				nodes.push(child);
				scopes.push(reached && (!catchOrTryBody || child == from) ? bindingAlso(current, bound) : current);
				if (sequential && (child.getType() == TokenTypes.VARIABLE_DEF || child.getType() == TokenTypes.RESOURCE) && declaresName(child))
					current = bindingAlso(current, List.of(FieldSortingCheck.fieldName(child)));
			}
		}
		return idents;
	}

	/**
	 * The names that own the class body {@code objBlock}, outermost first and {@code /} apart: each
	 * type, enum constant, field or local, and method or constructor around it, and {@code <new>}
	 * for each anonymous class body among them ({@code Shade/ZETA}, {@code T/handler/<new>}).
	 */
	@CheckReturnValue
	@Nonnull
	private static String ownerPath(@Nonnull DetailAST objBlock) {
		final var segments = new ArrayList<String>();
		for (var node = objBlock.getParent(); node != null; node = node.getParent()) {
			if (node.getType() == TokenTypes.LITERAL_NEW && node.findFirstToken(TokenTypes.OBJBLOCK) != null)
				segments.addFirst("<new>");
			else if (OWNERS.contains(node.getType()))
				segments.addFirst(FieldSortingCheck.fieldName(node));
		}
		return String.join("/", segments);
	}

	/**
	 * True when two fields of one group with initializers of kinds {@code a} and {@code b} may
	 * not trade places: one runs code, which may read or write the other's field or share state
	 * with its initializer, and the other is not {@link Kind#INERT}.
	 */
	@CheckReturnValue
	public static boolean pinned(@Nonnull Kind a, @Nonnull Kind b) {
		return a == Kind.RUNS_CODE && b != Kind.INERT || b == Kind.RUNS_CODE && a != Kind.INERT;
	}

	/**
	 * For each of {@code members} (fields of one class body, in source order), the members of its
	 * static or instance group it must stay after: the earlier ones it is
	 * {@linkplain #pinned pinned} to. The pins are cut down to the ones that imply the rest: a
	 * member running code follows the last earlier one running code and every
	 * non-{@link Kind#INERT} one since, and any other non-inert member follows the last earlier one
	 * running code.
	 */
	@CheckReturnValue
	@Nonnull
	public static Map<String, Set<String>> pinnedAfter(@Nonnull List<DetailAST> members) {
		final var pins = new HashMap<String, Set<String>>();
		final var lastRunning = new HashMap<Boolean, String>();
		final var literalSince = new HashMap<Boolean, List<String>>();
		final var kinds = kindsOf(members);
		for (var i = 0; i < members.size(); ++i) {
			final var member = members.get(i);
			final var group = isStaticMember(member);
			final var name = FieldSortingCheck.fieldName(member);
			final var kind = kinds.get(i);
			if (kind == Kind.INERT)
				continue;
			final var after = new HashSet<String>();
			if (lastRunning.containsKey(group))
				after.add(lastRunning.get(group));
			if (kind == Kind.RUNS_CODE) {
				after.addAll(literalSince.getOrDefault(group, List.of()));
				lastRunning.put(group, name);
				literalSince.put(group, new ArrayList<>());
			}
			else
				literalSince.computeIfAbsent(group, key -> new ArrayList<>()).add(name);
			if (!after.isEmpty())
				pins.computeIfAbsent(name, key -> new HashSet<>()).addAll(after);
		}
		return pins;
	}

	/** The index of each of {@code members} by name, the first one of a name winning. */
	@CheckReturnValue
	@Nonnull
	private static Map<String, Integer> positionsOf(@Nonnull List<DetailAST> members) {
		final var positions = new HashMap<String, Integer>();
		for (var i = 0; i < members.size(); ++i)
			positions.putIfAbsent(FieldSortingCheck.fieldName(members.get(i)), i);
		return positions;
	}

	private static void pushChildren(@Nonnull DetailAST node, @Nonnull Deque<DetailAST> pending) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
			pending.push(child);
	}

	/**
	 * The one node among {@code first} and its later siblings that is not a parenthesis, which is
	 * how checkstyle lays out a parenthesized operand, or null when there is none or more than one.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST unparenthesized(@Nullable DetailAST first) {
		DetailAST operand = null;
		for (var node = first; node != null; node = node.getNextSibling()) {
			if (node.getType() == TokenTypes.LPAREN || node.getType() == TokenTypes.RPAREN)
				continue;
			if (operand != null)
				return null;
			operand = node;
		}
		return operand;
	}

	private FieldInitializers() {
	}
}