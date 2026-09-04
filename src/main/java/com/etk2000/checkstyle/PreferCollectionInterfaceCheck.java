package com.etk2000.checkstyle;

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
 * Checkstyle check that flags concrete collection types in method and
 * constructor signatures (return types and parameter types). Suggests
 * the corresponding interface type instead.
 */
public class PreferCollectionInterfaceCheck extends AbstractResolvingCheck {
	/** The source span of a spelled type name. Lines are 0-based, columns are char indices. */
	public record TypeNameSpan(int startLine, int startColumn, int endLine, int endColumn, @Nonnull String spelling) {}

	/**
	 * A record component's declared type name and, when the record writes one out, its accessor's
	 * return type name. JLS 8.10.3 forces the two to stay identical, so an edit that moves one has
	 * to move the other.
	 *
	 * @param accessorPresent whether the record declares an explicit accessor at all. When this is
	 *                        true and {@code accessor} is null its name could not be located, and
	 *                        the pair has to be refused rather than half-rewritten
	 */
	public record RecordTypePair(
			@Nonnull TypeNameSpan component,
			@Nullable TypeNameSpan accessor,
			boolean atComponent,
			boolean accessorPresent
	) {}

	/** What the types enclosing a member allow another compilation unit to do with it. */
	private enum OwnerReach {
		/** Nameable and subclassable there, so an unseen override may exist. */
		EXTENDABLE,

		/** Nameable there, but nothing can subclass it. */
		UNEXTENDABLE,

		/** Named nowhere else, so no other file can call it or extend it. */
		UNNAMEABLE
	}

	/**
	 * What a body use of a parameter says about widening the type it is declared with.
	 */
	private enum UseVerdict {
		/** Survives widening whatever else happens. */
		ACCEPTED,

		/** Survives only if every {@code coupledReturns} return type widens in this same pass. */
		COUPLED_TO_A_RETURN,

		/** Demands the concrete type, so the declaration must not move at all. */
		REFUSED
	}

	/**
	 * A {@link UseVerdict} with the methods it depends on, which are the ones enclosing the
	 * {@code return} rather than the one being checked: a captured parameter may be returned from a
	 * method of a nested anonymous or local class, whose return type this pass decides separately.
	 */
	private record UseOutcome(@Nonnull UseVerdict verdict, @Nonnull List<DetailAST> coupledReturns) {
		private static final UseOutcome ACCEPTED = new UseOutcome(UseVerdict.ACCEPTED, List.of());
		private static final UseOutcome REFUSED = new UseOutcome(UseVerdict.REFUSED, List.of());

		@CheckReturnValue
		@Nonnull
		static UseOutcome coupledTo(@Nullable DetailAST method) {
			return method == null
					? REFUSED
					: new UseOutcome(UseVerdict.COUPLED_TO_A_RETURN, List.of(method));
		}

		@CheckReturnValue
		@Nonnull
		static UseOutcome of(boolean accepted) {
			return accepted ? ACCEPTED : REFUSED;
		}
	}

	public static final String COLLECTION_PACKAGE = "java.util.";

	private static final String MSG = "prefer.replacement";

	@CheckReturnValue
	@Nullable
	private static DetailAST componentTypeNamed(@Nonnull DetailAST recordDef, @Nonnull String name) {
		final var components = recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		for (var component = components == null ? null : components.getFirstChild(); component != null; component = component.getNextSibling()) {
			final var ident = component.getType() == TokenTypes.RECORD_COMPONENT_DEF
					? component.findFirstToken(TokenTypes.IDENT)
					: null;
			if (ident != null && name.equals(ident.getText()))
				return component.findFirstToken(TokenTypes.TYPE);
		}
		return null;
	}

	/**
	 * The record's written-out accessor for {@code name}, or null when the implicit one stands.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST explicitAccessor(@Nullable DetailAST objBlock, @Nonnull String name) {
		for (var member = objBlock == null ? null : objBlock.getFirstChild(); member != null; member = member.getNextSibling()) {
			if (member.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var params = member.findFirstToken(TokenTypes.PARAMETERS);
			final var ident = member.findFirstToken(TokenTypes.IDENT);
			if (ident != null && name.equals(ident.getText())
					&& (params == null || params.findFirstToken(TokenTypes.PARAMETER_DEF) == null))
				return member;
		}
		return null;
	}

	/**
	 * Whether {@code defNode} is a record's accessor for one of its components: no parameters, and a
	 * name matching a component. The component list is the single place that type is decided, and
	 * {@link #checkRecordComponents} drives it, so judging the accessor's return type separately
	 * lets the two halves disagree and rewrites only one of them.
	 */
	@CheckReturnValue
	private static boolean isRecordAccessor(@Nonnull DetailAST defNode) {
		final var objBlock = defNode.getParent();
		final var owner = objBlock == null ? null : objBlock.getParent();
		if (owner == null || owner.getType() != TokenTypes.RECORD_DEF)
			return false;

		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		final var name = defNode.findFirstToken(TokenTypes.IDENT);
		if (name == null || (params != null && params.findFirstToken(TokenTypes.PARAMETER_DEF) != null))
			return false;

		final var components = owner.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		for (var component = components == null ? null : components.getFirstChild(); component != null; component = component.getNextSibling()) {
			final var ident = component.getType() == TokenTypes.RECORD_COMPONENT_DEF
					? component.findFirstToken(TokenTypes.IDENT)
					: null;
			if (ident != null && name.getText().equals(ident.getText()))
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code (line, column)} sits on a record component's or record accessor's own type,
	 * whether or not {@link #recordTypePairAt} could build spans for it.
	 */
	@CheckReturnValue
	public static boolean isRecordPairPosition(@Nonnull DetailAST root, int line, int column) {
		final var found = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.IDENT);
		if (found == null)
			return false;

		// checkstyle puts ARRAY_DECLARATOR beside the name rather than around it, so no input drives
		// that half today; AstUtil.getTypeName hedges both shapes the same way
		var type = found.getParent();
		while (type != null && (type.getType() == TokenTypes.DOT || type.getType() == TokenTypes.ARRAY_DECLARATOR))
			type = type.getParent();
		if (type == null || type.getType() != TokenTypes.TYPE)
			return false;

		final var member = type.getParent();
		if (member == null)
			return false;

		return member.getType() == TokenTypes.RECORD_COMPONENT_DEF
				|| (member.getType() == TokenTypes.METHOD_DEF && isRecordAccessor(member));
	}

	/**
	 * Whether {@code node} is one of the nodes a type's own structure interposes between a name it
	 * spells and the {@code TYPE} that declares it.
	 */
	@CheckReturnValue
	private static boolean isTypeStructure(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.ARRAY_DECLARATOR, TokenTypes.DOT, TokenTypes.TYPE_ARGUMENT,
			     TokenTypes.TYPE_ARGUMENTS, TokenTypes.TYPE_LOWER_BOUNDS,
			     TokenTypes.TYPE_UPPER_BOUNDS -> true;

			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isVarArgs(@Nonnull DetailAST defNode) {
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		for (var param = params == null ? null : params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.findFirstToken(TokenTypes.ELLIPSIS) != null)
				return true;
		}
		return false;
	}

	@CheckReturnValue
	public static boolean namesATypeIdentifierAt(@Nonnull DetailAST root, int line, int column) {
		final var found = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.IDENT);
		if (found == null)
			return false;

		var type = found.getParent();
		while (type != null && isTypeStructure(type))
			type = type.getParent();
		if (type == null || type.getType() != TokenTypes.TYPE)
			return false;

		final var member = type.getParent();
		return member != null && (member.getType() == TokenTypes.METHOD_DEF
				|| member.getType() == TokenTypes.PARAMETER_DEF
				|| member.getType() == TokenTypes.RECORD_COMPONENT_DEF);
	}

	@CheckReturnValue
	@Nonnull
	private static String nestingPath(@Nonnull DetailAST typeDef) {
		final var segments = new ArrayList<String>();
		for (var node = typeDef; node != null; node = node.getParent()) {
			final var type = node.getType();
			if (type != TokenTypes.ANNOTATION_DEF && type != TokenTypes.CLASS_DEF
					&& type != TokenTypes.ENUM_DEF && type != TokenTypes.INTERFACE_DEF
					&& type != TokenTypes.RECORD_DEF)
				continue;

			final var ident = node.findFirstToken(TokenTypes.IDENT);
			if (ident == null)
				continue;

			// two local classes of the same name in two methods are different types, and nothing in
			// the enclosing chain distinguishes them, so the declaration site does
			final var scope = node.getParent();
			final var segment = scope != null && scope.getType() == TokenTypes.SLIST
					? ident.getText() + "#" + node.getLineNo()
					: ident.getText();
			segments.add(0, segment);
		}
		return String.join(".", segments);
	}

	/**
	 * What the chain of types enclosing {@code owner} says about reaching a member declared there
	 * from another compilation unit. The walk starts at the declaring type and moves outward,
	 * because an enclosing type's finality says nothing about a type nested inside it.
	 */
	@CheckReturnValue
	@Nonnull
	private static OwnerReach ownerReach(@Nonnull DetailAST owner) {
		for (var frame = owner; frame != null; frame = frame.getParent()) {
			switch (frame.getType()) {
				case TokenTypes.ANNOTATION_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF -> {
					if (frame == owner)
						return OwnerReach.UNEXTENDABLE;
				}

				case TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF -> {
					if (frame == owner && AstUtil.hasModifier(frame, TokenTypes.FINAL))
						return OwnerReach.UNEXTENDABLE;
				}

				case TokenTypes.ENUM_CONSTANT_DEF, TokenTypes.LITERAL_NEW, TokenTypes.SLIST -> {
					return OwnerReach.UNNAMEABLE;
				}

				default -> { }
			}
		}
		return OwnerReach.EXTENDABLE;
	}

	/**
	 * Whether nothing outside this compilation unit can name {@code recordDef}, and so cannot call
	 * the accessors its components generate.
	 */
	@CheckReturnValue
	static boolean recordIsUnnameableOutsideThisFile(@Nonnull DetailAST recordDef) {
		if (AstUtil.hasModifier(recordDef, TokenTypes.LITERAL_PRIVATE))
			return true;

		final var enclosing = recordDef.getParent();
		return enclosing != null && ownerReach(enclosing) == OwnerReach.UNNAMEABLE;
	}

	/**
	 * The component and accessor type names the violation at 0-based {@code (line, column)} belongs
	 * to, or null when that position is neither.
	 */
	@CheckReturnValue
	@Nullable
	public static RecordTypePair recordTypePairAt(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var found = AstUtil.findNodeAt(
				root,
				line,
				column,
				node -> node.getType() == TokenTypes.IDENT
		);
		if (found == null)
			return null;

		// the logged token is the type's own name: its last segment when qualified, and never a
		// type argument or an array element
		final var owner = found.getParent();
		final var type = owner == null ? null : (owner.getType() == TokenTypes.DOT ? owner.getParent() : owner);
		if (type == null || type.getType() != TokenTypes.TYPE || typeNameNode(type) != found)
			return null;

		final var member = type.getParent();
		if (member == null)
			return null;

		final var atComponent = member.getType() == TokenTypes.RECORD_COMPONENT_DEF;
		if (!atComponent && (member.getType() != TokenTypes.METHOD_DEF || !isRecordAccessor(member)))
			return null;

		final var name = member.findFirstToken(TokenTypes.IDENT);
		// a component's grandparent is its RECORD_COMPONENTS' record; an accessor's is its OBJBLOCK's
		final var recordDef = member.getParent() == null ? null : member.getParent().getParent();
		if (name == null || recordDef == null || recordDef.getType() != TokenTypes.RECORD_DEF)
			return null;

		final var componentType = atComponent ? type : componentTypeNamed(recordDef, name.getText());
		final var accessor = explicitAccessor(recordDef.findFirstToken(TokenTypes.OBJBLOCK), name.getText());
		final var accessorType = accessor == null ? null : accessor.findFirstToken(TokenTypes.TYPE);
		final var componentName = typeNameNode(componentType);
		final var accessorName = typeNameNode(accessorType);
		if (componentName == null)
			return null;

		return new RecordTypePair(
				spanOf(componentName, lines),
				accessorName == null ? null : spanOf(accessorName, lines),
				atComponent,
				accessor != null
		);
	}

	/**
	 * Whether nothing outside this compilation unit can override {@code defNode}. An override there
	 * is invisible here, so widening a parameter it pins breaks that file without ever reporting a
	 * violation against it, and without {@code @Override} the two silently stop being a pair.
	 *
	 * <p>The owner's own visibility seals nothing: a public subtype in this package re-exports the
	 * member, and any other package may then extend that subtype.
	 *
	 * <p>{@code static} and {@code final} are not enough on their own. They stop an override, but a
	 * subclass in a file this run cannot see still declares a member with the same name: against a
	 * {@code final} method the widened signature turns a legal overload there into an illegal
	 * override, and against a {@code static} one it starts hiding rather than overloading. Both need
	 * an owner nothing outside can extend.
	 */
	@CheckReturnValue
	static boolean sealedAgainstOutsideOverride(@Nonnull DetailAST defNode) {
		if (defNode.getType() != TokenTypes.METHOD_DEF
				|| AstUtil.hasModifier(defNode, TokenTypes.LITERAL_PRIVATE))
			return true;

		final var objBlock = defNode.getParent();
		final var owner = objBlock == null ? null : objBlock.getParent();
		return owner != null && ownerReach(owner) != OwnerReach.EXTENDABLE;
	}

	/**
	 * Whether nothing outside this compilation unit can name {@code defNode} to call it. Widening a
	 * return type keeps every unseen override legal, so overridability is the wrong question there;
	 * what breaks is a caller assigning the result to the concrete type, or one whose overload set
	 * silently rebinds to a wider candidate.
	 */
	@CheckReturnValue
	static boolean sealedInsideThisFile(@Nonnull DetailAST defNode) {
		if (AstUtil.hasModifier(defNode, TokenTypes.LITERAL_PRIVATE))
			return true;

		final var objBlock = defNode.getParent();
		final var owner = objBlock == null ? null : objBlock.getParent();
		return owner != null && ownerReach(owner) == OwnerReach.UNNAMEABLE;
	}

	/**
	 * The simple name {@code node} selects, or null when it selects none. A {@code METHOD_CALL}'s
	 * name is its first child, or the last segment of that child when the receiver is qualified;
	 * {@code METHOD_CALL.getLastChild()} is the closing paren rather than the name.
	 */
	@CheckReturnValue
	@Nullable
	private static String selectedName(@Nonnull DetailAST node) {
		if (node.getType() == TokenTypes.METHOD_REF) {
			final var referenced = node.getLastChild();
			return referenced != null && referenced.getType() == TokenTypes.IDENT ? referenced.getText() : null;
		}
		if (node.getType() != TokenTypes.METHOD_CALL)
			return null;

		final var target = node.getFirstChild();
		if (target == null)
			return null;
		if (target.getType() == TokenTypes.IDENT)
			return target.getText();

		final var selected = target.getType() == TokenTypes.DOT ? target.getLastChild() : null;
		return selected != null && selected.getType() == TokenTypes.IDENT ? selected.getText() : null;
	}

	/**
	 * The span of the type name {@code nameNode} spells, walking a qualified chain back to its
	 * leftmost segment so a name broken across lines is still one span.
	 */
	@CheckReturnValue
	@Nonnull
	private static TypeNameSpan spanOf(@Nonnull DetailAST nameNode, @Nonnull List<String> lines) {
		final var parent = nameNode.getParent();
		final var qualified = parent != null && parent.getType() == TokenTypes.DOT;
		var leftmost = nameNode;
		if (qualified) {
			for (var node = parent; node != null && node.getType() == TokenTypes.DOT; node = node.getFirstChild())
				leftmost = node.getFirstChild() == null ? leftmost : node.getFirstChild();
		}
		// getColumnNo counts code points while every splice indexes chars, so a supplementary
		// character earlier on the line would put both ends of the span in the wrong place
		final var startLine = leftmost.getLineNo() - 1;
		final var endLine = nameNode.getLineNo() - 1;
		return new TypeNameSpan(
				startLine,
				LineText.charIndexOfColumn(lines.get(startLine), leftmost.getColumnNo()),
				endLine,
				LineText.charIndexOfColumn(lines.get(endLine), nameNode.getColumnNo()) + nameNode.getText().length(),
				qualified ? AstUtil.dottedName(parent) : nameNode.getText()
		);
	}

	/**
	 * Whether rewriting the type named at {@code node} would substitute a type argument rather than
	 * widen the declared type. Generics are invariant, so {@code Map<String, ArrayList<X>>} and
	 * {@code Map<String, List<X>>} are unrelated types and every call site breaks, even one in this
	 * same file that no fix touches.
	 *
	 * <p>The sole safe position is a {@code ? extends} bound on a parameter, where raising the bound
	 * accepts strictly more arguments. The same bound on a return type narrows what the caller may
	 * assign, a record component is both at once, and a {@code ? super} bound narrows everywhere.
	 */
	@CheckReturnValue
	private static boolean substitutesATypeArgument(@Nonnull DetailAST node, @Nonnull DetailAST typeRoot) {
		var crossedTypeArguments = false;
		var boundSinceLastLevel = false;
		for (var frame = node; frame != null && frame != typeRoot; frame = frame.getParent()) {
			if (frame.getType() == TokenTypes.TYPE_UPPER_BOUNDS)
				boundSinceLastLevel = true;
			else if (frame.getType() == TokenTypes.TYPE_ARGUMENTS) {
				// a level not entered through a bound is invariant, so raising anything under it
				// substitutes rather than widens no matter what the levels further out are
				if (!boundSinceLastLevel)
					return true;

				boundSinceLastLevel = false;
				crossedTypeArguments = true;
			}
		}
		if (!crossedTypeArguments)
			return false;

		final var context = typeRoot.getParent();
		return context == null || context.getType() != TokenTypes.PARAMETER_DEF;
	}

	/**
	 * The type name a {@code TYPE} spells, or null when it spells none, as a primitive or {@code void}
	 * does. An array is not one of those: {@code ARRAY_DECLARATOR} is a sibling of the name rather
	 * than a wrapper around it, so an array type spells its element's name like any other.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST typeNameNode(@Nullable DetailAST type) {
		final var declared = type == null ? null : type.getFirstChild();
		if (declared == null)
			return null;
		if (declared.getType() == TokenTypes.IDENT)
			return declared;

		if (declared.getType() != TokenTypes.DOT)
			return null;

		// a qualified generic type hangs its TYPE_ARGUMENTS off the DOT, so the name is the last
		// IDENT child rather than the last child
		DetailAST last = null;
		for (var child = declared.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT)
				last = child;
		}
		return last;
	}

	/**
	 * The name {@code written} erases to when it is a type parameter in scope: its first bound, or
	 * {@code Object} when unbounded. Null when it is not a type parameter.
	 */
	@CheckReturnValue
	@Nullable
	private static String typeParameterBound(@Nonnull DetailAST scope, @Nonnull String written) {
		for (var node = scope; node != null; node = node.getParent()) {
			final var params = node.findFirstToken(TokenTypes.TYPE_PARAMETERS);
			if (params == null)
				continue;

			for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
				final var ident = param.getType() == TokenTypes.TYPE_PARAMETER
						? param.findFirstToken(TokenTypes.IDENT)
						: null;
				if (ident == null || !written.equals(ident.getText()))
					continue;

				// the leftmost bound is the erasure; the rest are siblings past a TYPE_EXTENSION_AND,
				// and canonicalType would run them together into a name no type ever has
				final var bounds = param.findFirstToken(TokenTypes.TYPE_UPPER_BOUNDS);
				for (var bound = bounds == null ? null : bounds.getFirstChild(); bound != null; bound = bound.getNextSibling()) {
					final var boundName = AstUtil.typeName(bound);
					if (boundName != null)
						return boundName;
				}
				return Object.class.getName();
			}
		}
		return null;
	}

	private Map<String, List<DetailAST>> useSites;

	private TypeGraph typeGraph;

	/**
	 * Whether a target declared as {@code typeNode} accepts a value of {@code valueFqcn}. A type this
	 * file declares, or one the classpath cannot resolve, answers no, so the rewrite is refused
	 * rather than guessed at.
	 */
	@CheckReturnValue
	private boolean acceptsType(@Nullable DetailAST typeNode, @Nonnull String valueFqcn) {
		final var written = typeNode == null ? null : AstUtil.typeName(typeNode.getFirstChild());
		if (written == null || AstUtil.sameFileTypeBody(typeNode, written) != null)
			return false;

		final var fqcn = resolve(written);
		return fqcn != null && ReflectionUtil.acceptsValueOfType(fqcn, valueFqcn);
	}

	/**
	 * Whether a supertype pins the accessor a component implies, so the component's type cannot move
	 * on its own. A same-file supertype is left to the ordinary walk, which flags and rewrites both
	 * halves together; a supertype this run cannot resolve is refused, since nothing here can show
	 * it does not declare the accessor and no violation would ever be reported against it.
	 */
	@CheckReturnValue
	private boolean accessorIsPinnedBySupertype(@Nonnull DetailAST recordDef, @Nonnull String name) {
		for (var clause = recordDef.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var nameNode = clause.getFirstChild(); nameNode != null; nameNode = nameNode.getNextSibling()) {
				final var written = AstUtil.typeName(nameNode);
				if (written == null || AstUtil.sameFileTypeBody(recordDef, written) != null)
					continue;

				// resolveClassName echoes a qualified or explicitly imported name back without
				// loading it, so a non-null answer alone does not mean the supertype can be read,
				// and a type that loads may still have members whose descriptors cannot resolve
				final var fqcn = resolve(written);
				if (fqcn == null || !ReflectionUtil.isResolvableClass(fqcn)
						|| ReflectionUtil.hasUnreadableMembers(fqcn))
					return true;
			}
		}

		final var objBlock = recordDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return false;

		// a same-file supertype's own accessor is normally flagged and rewritten alongside the
		// component, so the pair stays consistent. That holds only while the supertype's declaration
		// is itself rewritable: one the ordinary walk refuses would keep the concrete type while the
		// component moved, and the transitive closure is searched because the declaration may sit on
		// an interface the record only reaches through another
		if (typeGraph == null)
			typeGraph = new TypeGraph(objBlock);

		for (var ancestor : typeGraph.related(objBlock).ancestors()) {
			final var declared = explicitAccessor(ancestor, name);
			if (declared != null && !returnTypeIsAutoFixable(declared))
				return true;
		}
		return classpathSupertypeCollides(objBlock, name, List.of());
	}

	/**
	 * Whether an overload of the same name and arity is reachable from {@code defNode}'s type.
	 * Widening a parameter makes the signature applicable to calls that used to bind to that
	 * overload, and resolution then picks whichever is more specific, so a call this fix never
	 * touches can change target while still compiling.
	 */
	@CheckReturnValue
	private boolean anOverloadCouldRebind(@Nonnull DetailAST defNode) {
		final var name = defNode.findFirstToken(TokenTypes.IDENT);
		final var objBlock = defNode.getParent();
		if (name == null || objBlock == null)
			return false;

		final var arity = rewrittenParameterTypes(defNode, true).size();
		if (defNode.getType() != TokenTypes.METHOD_DEF) {
			// a record's canonical constructor is implied by the component list rather than declared,
			// so it is not among objBlock's children for the sibling walk to find
			final var recordDef = objBlock.getParent();
			if (recordDef != null && recordDef.getType() == TokenTypes.RECORD_DEF
					&& rewrittenComponentTypes(recordDef).size() == arity)
				return true;

			return bodyHasAnOverload(objBlock, defNode, name.getText(), arity, false);
		}

		if (typeGraph == null)
			typeGraph = new TypeGraph(objBlock);

		final var related = typeGraph.related(objBlock);
		for (var body : related.descendants()) {
			if (bodyHasAnOverload(body, defNode, name.getText(), arity, false))
				return true;
		}
		for (var body : related.ancestors()) {
			if (bodyHasAnOverload(body, defNode, name.getText(), arity, true))
				return true;
		}
		return classpathSupertypeDeclaresAnOverload(objBlock, name.getText(), arity);
	}

	/**
	 * Whether the call consuming {@code argument} still binds once that argument's type widens to
	 * {@code iface}, decided against the callee's own parameter in the same position.
	 */
	@CheckReturnValue
	private boolean argumentAcceptsTheInterface(
			@Nonnull DetailAST elist,
			@Nonnull DetailAST argument,
			@Nonnull String iface,
			@Nonnull String concrete
	) {
		var index = 0;
		for (var child = elist.getFirstChild(); child != null && child != argument; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++index;
		}

		final var call = elist.getParent();
		if (call == null)
			return false;

		final var count = AstUtil.countArguments(elist);
		if (call.getType() == TokenTypes.SUPER_CTOR_CALL)
			return superParameterAcceptsTheInterface(call, count, index, iface, concrete);

		final String written;
		final String called;
		if (call.getType() == TokenTypes.LITERAL_NEW) {
			written = AstUtil.typeName(call.getFirstChild());
			called = "new";
		}
		else if (call.getType() == TokenTypes.METHOD_CALL) {
			final var receiver = receiverTypeName(call);
			written = receiver != null ? receiver : staticFieldReceiverType(call);
			final var dot = call.getFirstChild();
			final var name = dot != null && dot.getType() == TokenTypes.DOT ? dot.getLastChild() : null;
			called = name == null ? null : name.getText();
		}
		else
			return false;

		// a receiver or constructed type this file declares shadows the classpath homonym, so
		// reflection would weigh the wrong overload set
		if (written != null && AstUtil.sameFileTypeBody(call, written) != null)
			return false;

		final var fqcn = written == null ? null : resolve(written);
		return fqcn != null && called != null && ReflectionUtil.parameterAcceptsType(
				fqcn, called, count, index, concrete, COLLECTION_PACKAGE + iface
		);
	}

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		// built on demand instead of here: checkstyle hands a null root for a file with no
		// compilation unit, and a file with no signature to weigh never needs either index at all
		useSites = null;
		typeGraph = null;
	}

	/**
	 * Whether {@code body} declares a member that {@code defNode}'s rewritten signature would
	 * become identical to. {@code inherited} marks a body whose members reach {@code defNode}'s own
	 * body by inheritance, where a private member is invisible and so cannot collide. The reverse
	 * direction must not skip private members: a subtype declaring a private method whose erasure
	 * matches an inherited one is a weaker-access compile error, not an overload.
	 */
	@CheckReturnValue
	private boolean bodyCollides(
			@Nonnull DetailAST body,
			@Nonnull DetailAST defNode,
			@Nonnull String name,
			@Nonnull List<String> rewritten,
			boolean inherited
	) {
		final var isMethod = defNode.getType() == TokenTypes.METHOD_DEF;
		for (var sibling = body.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling == defNode || sibling.getType() != defNode.getType())
				continue;

			final var siblingName = sibling.findFirstToken(TokenTypes.IDENT);
			if (isMethod && (siblingName == null || !name.equals(siblingName.getText())))
				continue;

			final var modifiers = sibling.findFirstToken(TokenTypes.MODIFIERS);
			if (inherited && modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null)
				continue;

			// a static interface method is not inherited by an implementor, unlike a static class
			// method, which is hidden rather than ignored
			if (inherited && modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null
					&& body.getParent() != null && body.getParent().getType() == TokenTypes.INTERFACE_DEF)
				continue;

			if (!rewritten.equals(rewrittenParameterTypes(sibling, true)))
				continue;

			// two members whose erasures are ALREADY identical are an override or hide pair, not a
			// collision the rewrite would create: rewriting both preserves the relation, so the
			// check must still flag them rather than fall silent on the pair
			if (body == defNode.getParent()
					|| !rewrittenParameterTypes(defNode, false).equals(rewrittenParameterTypes(sibling, false)))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private boolean bodyHasAnOverload(
			@Nonnull DetailAST body,
			@Nonnull DetailAST defNode,
			@Nonnull String name,
			int arity,
			boolean inherited
	) {
		final var isMethod = defNode.getType() == TokenTypes.METHOD_DEF;
		for (var sibling = body.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling == defNode || sibling.getType() != defNode.getType())
				continue;

			final var siblingName = sibling.findFirstToken(TokenTypes.IDENT);
			if (isMethod && (siblingName == null || !name.equals(siblingName.getText())))
				continue;

			final var modifiers = sibling.findFirstToken(TokenTypes.MODIFIERS);
			if (inherited && modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null)
				continue;

			// a static interface method is not inherited by an implementor, so it is not in the
			// overload set any call through this type resolves against
			if (inherited && modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null
					&& body.getParent() != null && body.getParent().getType() == TokenTypes.INTERFACE_DEF)
				continue;

			// a variable-arity method competes with every call of at least its fixed count, so the
			// declared counts need not match for one to take a call from the other
			final var siblingArity = rewrittenParameterTypes(sibling, true).size();
			if (siblingArity == arity
					|| (isVarArgs(sibling) && arity >= siblingArity - 1)
					|| (isVarArgs(defNode) && siblingArity >= arity - 1))
				return true;
		}
		return false;
	}

	/**
	 * Whether the body uses {@code parameterName} in a position that demands its declared type.
	 *
	 * <p>Deliberately a whitelist: a use kind nobody enumerated refuses the rewrite rather than
	 * corrupting the file. A nested scope that reuses the name is read as the parameter too, which
	 * at worst refuses a rewrite that would have been safe.
	 */
	@CheckReturnValue
	@Nonnull
	private UseOutcome bodyNeedsTheConcreteType(
			@Nullable DetailAST body,
			@Nonnull String parameterName,
			@Nonnull String iface,
			@Nonnull String concrete,
			boolean array
	) {
		if (body == null)
			return UseOutcome.ACCEPTED;

		// every coupled use is kept: two of them can name different methods, and the widening holds
		// only if all of those return types move
		final var coupled = new ArrayList<DetailAST>();
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(body);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (node.getType() == TokenTypes.IDENT && parameterName.equals(node.getText())) {
				final var use = isWidenedUse(node, iface, concrete, array);
				if (use.verdict() == UseVerdict.REFUSED)
					return use;

				coupled.addAll(use.coupledReturns());
			}

			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
		return coupled.isEmpty()
				? UseOutcome.ACCEPTED
				: new UseOutcome(UseVerdict.COUPLED_TO_A_RETURN, coupled);
	}

	/**
	 * Whether every site in this file that reads {@code defNode}'s result still compiles and still
	 * binds after the return type widens. A result reaching a position this walk does not model
	 * refuses, which costs the auto-fix tier rather than correctness.
	 *
	 * <p>A site surviving only because ANOTHER return type widens is refused rather than chased:
	 * chasing it would make this question depend on its own answer through {@link #returnWidensToo},
	 * and two methods returning each other's result would not terminate.
	 */
	@CheckReturnValue
	private boolean callSitesAcceptTheInterface(
			@Nonnull DetailAST defNode,
			@Nonnull String name,
			@Nonnull String iface,
			@Nonnull String concrete
	) {
		// a RECORD_DEF stands in for the accessors its components imply, so the body that declares
		// them is the record's own rather than the one the record is nested in
		final var ownerBody = defNode.getType() == TokenTypes.RECORD_DEF
				? defNode.findFirstToken(TokenTypes.OBJBLOCK)
				: defNode.getParent();
		if (ownerBody == null)
			return false;

		final var arity = rewrittenParameterTypes(defNode, true).size();
		final var varargs = isVarArgs(defNode);

		for (var use : useSitesNamed(defNode, name)) {
			if (use.getType() == TokenTypes.METHOD_REF) {
				// a method reference names no arguments, so what consumes its result comes from a
				// target functional interface this walk cannot read
				if (!receiverIsAnotherType(use, ownerBody))
					return false;

				continue;
			}

			final var arguments = use.findFirstToken(TokenTypes.ELIST);
			if (!varargs && (arguments == null ? 0 : AstUtil.countArguments(arguments)) != arity)
				continue;
			if (receiverIsAnotherType(use, ownerBody))
				continue;
			if (isWidenedUse(use, iface, concrete, false).verdict() != UseVerdict.ACCEPTED)
				return false;
		}
		return true;
	}

	private void checkParameters(@Nonnull DetailAST ast) {
		final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null || collapsesOntoAnOverload(ast))
			return;

		final var sealed = sealedAgainstOutsideOverride(ast) && !anOverloadCouldRebind(ast);

		final var body = ast.findFirstToken(TokenTypes.SLIST);
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			final var paramType = param.findFirstToken(TokenTypes.TYPE);
			final var name = param.findFirstToken(TokenTypes.IDENT);
			if (paramType == null)
				continue;

			final var declared = paramType.getFirstChild();
			final var iface = declared == null ? null : collectionInterfaceOf(declared);
			final var concrete = resolvedTypeName(declared);
			// an array or varargs parameter widens its ELEMENT type, so the uses that must survive
			// are the ones on an element rather than on the parameter itself
			final var array = paramType.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null
					|| param.findFirstToken(TokenTypes.ELLIPSIS) != null;
			final var use = iface != null && name != null && concrete != null
					? bodyNeedsTheConcreteType(body, name.getText(), iface, concrete, array)
					: UseOutcome.ACCEPTED;
			if (use.verdict() == UseVerdict.REFUSED)
				continue;

			checkTypeTree(paramType, sealed && returnWidensToo(use));
		}
	}

	/**
	 * Checks a record's components, which are at once the canonical constructor's parameter types
	 * and the accessors' return types. The whole list is left alone when rewriting it would give the
	 * canonical constructor the same erasure as an explicit one, which would not compile.
	 */
	private void checkRecordComponents(@Nonnull DetailAST recordDef) {
		final var components = recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		if (components == null || componentsCollapseOntoAConstructor(recordDef, components))
			return;

		final var body = recordDef.findFirstToken(TokenTypes.OBJBLOCK);
		for (var component = components.getFirstChild(); component != null; component = component.getNextSibling()) {
			if (component.getType() != TokenTypes.RECORD_COMPONENT_DEF)
				continue;

			final var type = component.findFirstToken(TokenTypes.TYPE);
			final var name = component.findFirstToken(TokenTypes.IDENT);
			if (type == null)
				continue;

			final var declared = type.getFirstChild();
			final var iface = declared == null ? null : collectionInterfaceOf(declared);
			final var concrete = resolvedTypeName(declared);
			final var array = type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null
					|| component.findFirstToken(TokenTypes.ELLIPSIS) != null;
			final var use = iface != null && name != null && concrete != null
					? recordBodyNeedsTheConcreteType(body, name.getText(), iface, concrete, array)
					: UseOutcome.ACCEPTED;
			if (use.verdict() == UseVerdict.REFUSED)
				continue;

			if (name != null && accessorIsPinnedBySupertype(recordDef, name.getText()))
				continue;

			// an accessor's return type has to stay identical to its component (JLS 8.10.3), and
			// visitScopedToken skips a record accessor, so the explicit one is rewritten here instead
			final var accessor = name == null ? null : explicitAccessor(body, name.getText());

			// the accessor calls read the component through the same type the constructor writes it
			// with, so one answer decides both halves of the pair
			final var autoFixable = recordIsUnnameableOutsideThisFile(recordDef)
					&& iface != null && concrete != null && name != null
					&& coupledRecordReturnsMoveToo(use, accessor)
					&& callSitesAcceptTheInterface(recordDef, name.getText(), iface, concrete);
			checkTypeTree(type, autoFixable);

			final var accessorType = accessor == null ? null : accessor.findFirstToken(TokenTypes.TYPE);
			if (accessorType != null)
				checkTypeTree(accessorType, autoFixable);
		}
	}

	/**
	 * Iterative for the same reason as the walks in {@code AstUtil}: a pathological generic nesting
	 * depth must not overflow the stack inside a check, where the error aborts the whole run rather
	 * than failing one file. Children are queued so they pop in document order, which keeps the
	 * violations in source order.
	 *
	 * @param autoFixable whether the rewrite is provably contained in this compilation unit. When
	 *                    it is not, the violation is still worth reporting but must not be applied
	 *                    unseen, so it is logged as a warning and the fix pipeline skips it.
	 */
	private void checkTypeTree(@Nonnull DetailAST ast, boolean autoFixable) {
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(ast);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			final var children = new ArrayDeque<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				// an annotation's own name and its member names are not types in the signature, so
				// resolving them reads `@Stack` or `@Ann(Vector = "x")` as a collection to rewrite.
				// A type-use annotation is a sibling of the type it annotates, never its parent, so
				// skipping the subtree cannot hide an annotated type argument
				if (child.getType() == TokenTypes.ANNOTATION || child.getType() == TokenTypes.ANNOTATIONS)
					continue;

				if (child.getType() == TokenTypes.DOT) {
					final var iface = collectionInterfaceOf(child);
					if (iface != null) {
						final var simpleName = AstUtil.simpleName(AstUtil.dottedName(child));
						// log at the last IDENT child so the fixer column targets the simple name
						DetailAST lastIdent = null;
						for (var c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
							if (c.getType() == TokenTypes.IDENT)
								lastIdent = c;
						}
						if (lastIdent != null && !substitutesATypeArgument(lastIdent, ast)) {
							if (autoFixable)
								log(lastIdent, MSG, iface, simpleName);
							else
								logWarning(lastIdent, MSG, iface, simpleName);
						}
					}
					// a qualified name's segments are not types in their own right: resolving them
					// one by one reads `com.example.ArrayList` as the imported `java.util.ArrayList`.
					// Only the type arguments hanging off the qualifier chain are separate types
					for (var segment = child; segment != null && segment.getType() == TokenTypes.DOT; segment = segment.getFirstChild()) {
						// a DOT spanning two generic segments (`Outer<String>.Inner<T>`) carries one
						// TYPE_ARGUMENTS per segment, so findFirstToken would drop all but the first
						for (var args = segment.getFirstChild(); args != null; args = args.getNextSibling()) {
							if (args.getType() == TokenTypes.TYPE_ARGUMENTS)
								children.push(args);
						}
					}
					continue;
				}
				else if (child.getType() == TokenTypes.IDENT) {
					final var iface = collectionInterfaceOf(child);
					if (iface != null && !substitutesATypeArgument(child, ast)) {
						if (autoFixable)
							log(child, MSG, iface, child.getText());
						else
							logWarning(child, MSG, iface, child.getText());
					}
				}
				children.push(child);
			}
			for (var child : children)
				pending.push(child);
		}
	}

	/**
	 * Whether a supertype that has no AST in this file declares a method the rewritten signature
	 * would collide with.
	 */
	@CheckReturnValue
	private boolean classpathSupertypeCollides(
			@Nonnull DetailAST objBlock,
			@Nonnull String name,
			@Nonnull List<String> rewritten
	) {
		final var typeDef = objBlock.getParent();
		if (typeDef == null)
			return false;

		// an anonymous body names its supertype on the `new` and an enum constant body inherits its
		// own enum, so neither carries the clauses the walk below reads
		if (typeDef.getType() == TokenTypes.LITERAL_NEW) {
			final var written = AstUtil.findNewClassName(typeDef);
			if (written == null || AstUtil.sameFileTypeBody(typeDef, written) != null)
				return false;

			final var fqcn = resolve(written);
			return fqcn != null && ReflectionUtil.declaresMethodErasure(fqcn, name, rewritten);
		}
		if (typeDef.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
			final var enumBody = typeDef.getParent();
			return enumBody != null && classpathSupertypeCollides(enumBody, name, rewritten);
		}

		for (var clause = typeDef.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var nameNode = clause.getFirstChild(); nameNode != null; nameNode = nameNode.getNextSibling()) {
				final var written = AstUtil.typeName(nameNode);
				if (written == null || AstUtil.sameFileTypeBody(typeDef, written) != null)
					continue;

				final var fqcn = resolve(written);
				if (fqcn != null && ReflectionUtil.declaresMethodErasure(fqcn, name, rewritten))
					return true;
			}
		}
		return false;
	}

	/**
	 * Whether a supertype outside this file declares an overload of {@code name} that a call of
	 * {@code arity} arguments could select.
	 */
	@CheckReturnValue
	private boolean classpathSupertypeDeclaresAnOverload(
			@Nonnull DetailAST objBlock,
			@Nonnull String name,
			int arity
	) {
		final var typeDef = objBlock.getParent();
		if (typeDef == null)
			return false;

		// an anonymous body names its supertype on the `new` and an enum constant body inherits its
		// own enum, so neither carries the clauses the walk below reads
		if (typeDef.getType() == TokenTypes.LITERAL_NEW) {
			final var written = AstUtil.findNewClassName(typeDef);
			final var fqcn = written == null || AstUtil.sameFileTypeBody(typeDef, written) != null
					? null
					: resolve(written);

			// an anonymous body inherits Object like any other, and a supertype that is an interface
			// ends the reflective superclass chain before Object is ever reached
			return (fqcn != null && ReflectionUtil.declaresOverloadAt(fqcn, name, arity))
					|| ReflectionUtil.declaresOverloadAt(Object.class.getName(), name, arity);
		}
		if (typeDef.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
			final var enumBody = typeDef.getParent();
			return enumBody != null && classpathSupertypeDeclaresAnOverload(enumBody, name, arity);
		}

		for (var clause = typeDef.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var named = clause.getFirstChild(); named != null; named = named.getNextSibling()) {
				final var written = AstUtil.typeName(named);
				if (written == null || AstUtil.sameFileTypeBody(named, written) != null)
					continue;

				final var fqcn = resolve(written);
				if (fqcn != null && ReflectionUtil.declaresOverloadAt(fqcn, name, arity))
					return true;
			}
		}

		// every type implicitly inherits Object, an enum its own Enum and a record its Record, and
		// none of those appear in a clause for the walk above to read
		final var implicit = switch (typeDef.getType()) {
			case TokenTypes.ENUM_DEF -> Enum.class.getName();
			case TokenTypes.RECORD_DEF -> Record.class.getName();
			default -> Object.class.getName();
		};
		return ReflectionUtil.declaresOverloadAt(implicit, name, arity);
	}

	@CheckReturnValue
	private boolean collapsesOntoAnOverload(@Nonnull DetailAST defNode) {
		final var name = defNode.findFirstToken(TokenTypes.IDENT);
		final var objBlock = defNode.getParent();
		if (name == null || objBlock == null)
			return false;

		final var rewritten = rewrittenParameterTypes(defNode, true);
		// constructors are not inherited, so only this very body can hold a colliding one
		if (defNode.getType() != TokenTypes.METHOD_DEF) {
			// a record's canonical constructor is implied by the component list rather than
			// declared, so it is not among objBlock's children for bodyCollides to find
			final var recordDef = objBlock.getParent();
			if (recordDef != null && recordDef.getType() == TokenTypes.RECORD_DEF
					&& rewritten.equals(rewrittenComponentTypes(recordDef)))
				return true;

			return bodyCollides(objBlock, defNode, name.getText(), rewritten, false);
		}

		if (typeGraph == null)
			typeGraph = new TypeGraph(objBlock);

		final var related = typeGraph.related(objBlock);
		for (var body : related.descendants()) {
			if (bodyCollides(body, defNode, name.getText(), rewritten, false))
				return true;
		}
		for (var body : related.ancestors()) {
			if (bodyCollides(body, defNode, name.getText(), rewritten, true))
				return true;
		}
		return overridesAClasspathMethod(defNode)
				|| classpathSupertypeCollides(objBlock, name.getText(), rewritten);
	}

	/**
	 * The collection interface the type named at {@code node} should be rewritten to, or null when
	 * it names anything else. A type this file declares shadows the import the same spelling would
	 * otherwise resolve to, so the user's own {@code Stack} is never read as {@code java.util.Stack}.
	 */
	@CheckReturnValue
	@Nullable
	private String collectionInterfaceOf(@Nonnull DetailAST node) {
		final String iface;
		if (node.getType() == TokenTypes.DOT)
			iface = ReflectionUtil.findCollectionInterface(AstUtil.dottedName(node));
		else if (node.getType() != TokenTypes.IDENT)
			return null;
		else {
			final var fqcn = resolve(node.getText());
			final var resolved = fqcn == null ? null : ReflectionUtil.findCollectionInterface(fqcn);
			// the shadowing walk is the only uncached one of the three, so it is asked last: a name
			// the classpath does not call a collection never needs it answered at all
			iface = resolved != null && AstUtil.sameFileTypeBody(node, node.getText()) == null ? resolved : null;
		}
		if (iface == null || AstUtil.sameFileTypeBody(node, iface) != null)
			return null;

		// the fix writes the bare simple name and adds a java.util import, so a file that already
		// binds that name would have every other use of it silently rebound, or stop compiling
		final var bound = resolve(iface);
		return bound == null || bound.equals(COLLECTION_PACKAGE + iface) ? iface : null;
	}

	@CheckReturnValue
	private boolean componentsCollapseOntoAConstructor(@Nonnull DetailAST recordDef, @Nonnull DetailAST components) {
		final var objBlock = recordDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return false;

		final var rewritten = rewrittenComponentTypes(recordDef);
		for (var sibling = objBlock.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.CTOR_DEF && rewritten.equals(rewrittenParameterTypes(sibling, true)))
				return true;
		}
		return false;
	}

	/**
	 * Whether every return type a record body couples the component to is moving in this same pass.
	 * The component's own explicit accessor is excluded: it is rewritten in lockstep a few lines
	 * below, while {@code returnTypeIsAutoFixable} refuses every record accessor by design, so
	 * asking about it would demote every record that writes one out.
	 */
	@CheckReturnValue
	private boolean coupledRecordReturnsMoveToo(@Nonnull UseOutcome use, @Nullable DetailAST accessor) {
		for (var method : use.coupledReturns()) {
			if (method != accessor && !returnTypeIsAutoFixable(method))
				return false;
		}
		return true;
	}

	/**
	 * Releases the per-file indexes. Checkstyle reuses one check instance across files, so an index
	 * left populated pins the finished file's whole AST through the nodes it holds.
	 */
	@Override
	public void finishTree(@Nonnull DetailAST rootAST) {
		useSites = null;
		typeGraph = null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.CTOR_DEF,
				TokenTypes.IMPORT,
				TokenTypes.METHOD_DEF,
				TokenTypes.PACKAGE_DEF,
				TokenTypes.RECORD_DEF
		};
	}

	/**
	 * What this use of {@code ident} says about widening its declared type to {@code iface}.
	 *
	 * <p>When the parameter is an array it is the ELEMENT type that widens, so the surviving uses
	 * are a call on an indexed element and {@code length}, which every array has whatever it holds.
	 */
	@CheckReturnValue
	@Nonnull
	private UseOutcome isWidenedUse(
			@Nonnull DetailAST ident,
			@Nonnull String iface,
			@Nonnull String concrete,
			boolean array
	) {
		var subject = ident;
		if (array) {
			final var index = ident.getParent();
			if (index == null || index.getType() != TokenTypes.INDEX_OP || index.getFirstChild() != ident) {
				final var access = ident.getParent();
				final var member = ident.getNextSibling();
				final var isLength = access != null && access.getType() == TokenTypes.DOT
						&& access.getFirstChild() == ident
						&& member != null && "length".equals(member.getText());
				return UseOutcome.of(isLength);
			}
			subject = index;
		}

		final var parent = subject.getParent();
		if (parent == null)
			return UseOutcome.REFUSED;

		if (parent.getType() == TokenTypes.DOT && parent.getFirstChild() == subject) {
			final var call = parent.getParent();
			final var called = subject.getNextSibling();
			if (call == null || call.getType() != TokenTypes.METHOD_CALL || call.getFirstChild() != parent
					|| called == null || called.getType() != TokenTypes.IDENT)
				return UseOutcome.REFUSED;

			// the arity says the call still binds; the return type says what it binds to still fits
			// whatever consumes it, which a covariant override on the concrete type would change
			final var arguments = call.findFirstToken(TokenTypes.ELIST);
			final var stillBinds = arguments != null && ReflectionUtil.returnsTheSameType(
					concrete, COLLECTION_PACKAGE + iface, called.getText(), AstUtil.countArguments(arguments)
			);
			return UseOutcome.of(stillBinds);
		}
		// a method reference names no arguments, so its arity comes from a target functional
		// interface this walk cannot read
		if (parent.getType() == TokenTypes.METHOD_REF && parent.getFirstChild() == subject)
			return UseOutcome.REFUSED;
		return targetAcceptsTheInterface(subject, iface, concrete);
	}

	/**
	 * Whether this method overrides one declared by a supertype with no AST in this file. Asked with
	 * the method's CURRENT erasure rather than its rewritten one: the question is not "would the
	 * rewrite collide" but "is this signature already pinned by a supertype". If it is, the
	 * parameters cannot change without breaking the override and the return type cannot widen
	 * without breaking covariance, and the supertype is not visible to be rewritten alongside it.
	 */
	@CheckReturnValue
	private boolean overridesAClasspathMethod(@Nonnull DetailAST defNode) {
		final var name = defNode.findFirstToken(TokenTypes.IDENT);
		final var objBlock = defNode.getParent();
		return name != null && objBlock != null
				&& classpathSupertypeCollides(objBlock, name.getText(), rewrittenParameterTypes(defNode, false));
	}

	/**
	 * Whether {@code use} provably selects a member of some type other than {@code ownerBody}'s.
	 * Proving the negative is the point: a receiver this walk cannot read is checked rather than
	 * skipped, since including an unrelated site only ever adds a constraint.
	 */
	@CheckReturnValue
	private boolean receiverIsAnotherType(@Nonnull DetailAST use, @Nonnull DetailAST ownerBody) {
		final var written = use.getType() == TokenTypes.METHOD_CALL
				? receiverTypeName(use)
				: AstUtil.typeName(use.getFirstChild());
		if (written == null)
			return false;

		final var body = AstUtil.sameFileTypeBody(use, written);
		if (body != null) {
			if (typeGraph == null)
				typeGraph = new TypeGraph(ownerBody);

			final var related = typeGraph.related(ownerBody);
			return body != ownerBody
					&& !related.descendants().contains(body) && !related.ancestors().contains(body);
		}
		// a receiver the classpath LOADS is typed as something no other file can name, so it cannot
		// dispatch to a member only this file declares. resolveClassName echoes a qualified name
		// back unloaded, so a non-null answer alone proves nothing
		final var fqcn = resolve(written);
		return fqcn != null && ReflectionUtil.isResolvableClass(fqcn);
	}

	/**
	 * A record component is the canonical constructor's parameter and the accessor's return type at
	 * once, so any body in the record can demand its concrete type: a compact constructor, an
	 * explicit canonical one, or an accessor written out instead of being implied.
	 */
	@CheckReturnValue
	private UseOutcome recordBodyNeedsTheConcreteType(
			@Nullable DetailAST objBlock,
			@Nonnull String name,
			@Nonnull String iface,
			@Nonnull String concrete,
			boolean array
	) {
		if (objBlock == null)
			return UseOutcome.ACCEPTED;

		final var coupled = new ArrayList<DetailAST>();
		for (var member = objBlock.getFirstChild(); member != null; member = member.getNextSibling()) {
			final var type = member.getType();
			if (type != TokenTypes.COMPACT_CTOR_DEF && type != TokenTypes.CTOR_DEF
					&& type != TokenTypes.METHOD_DEF)
				continue;

			final var use = bodyNeedsTheConcreteType(member.findFirstToken(TokenTypes.SLIST), name, iface, concrete, array);
			if (use.verdict() == UseVerdict.REFUSED)
				return use;

			coupled.addAll(use.coupledReturns());
		}
		return coupled.isEmpty()
				? UseOutcome.ACCEPTED
				: new UseOutcome(UseVerdict.COUPLED_TO_A_RETURN, coupled);
	}

	@CheckReturnValue
	@Nullable
	private String resolvedTypeName(@Nullable DetailAST node) {
		final var written = node == null ? null : AstUtil.typeName(node);
		return written == null ? null : resolve(written);
	}

	/**
	 * Whether widening {@code methodDef}'s return type is provably contained in this file. Asked by
	 * the return tier itself, by the parameter tier through {@link #returnWidensToo}, and by the
	 * record-accessor pin, so the three cannot decide it differently.
	 *
	 * <p>A return type only ever logs its outermost name, because {@code substitutesATypeArgument}
	 * refuses every nested one outside a parameter, so one interface and one concrete name describe
	 * the whole rewrite. An array spelling is no exception: {@code ARRAY_DECLARATOR} is a sibling
	 * of the type name rather than a wrapper, so the name weighed here is the element's.
	 */
	@CheckReturnValue
	private boolean returnTypeIsAutoFixable(@Nonnull DetailAST methodDef) {
		if (!sealedInsideThisFile(methodDef) || overridesAClasspathMethod(methodDef)
				|| isRecordAccessor(methodDef))
			return false;

		final var name = methodDef.findFirstToken(TokenTypes.IDENT);
		final var returnType = methodDef.findFirstToken(TokenTypes.TYPE);
		final var declared = returnType == null ? null : returnType.getFirstChild();
		final var iface = declared == null ? null : collectionInterfaceOf(declared);
		final var concrete = resolvedTypeName(declared);
		return name != null && iface != null && concrete != null
				&& callSitesAcceptTheInterface(methodDef, name.getText(), iface, concrete);
	}

	@CheckReturnValue
	private boolean returnWidensToo(@Nonnull UseOutcome use) {
		for (var method : use.coupledReturns()) {
			if (!returnTypeIsAutoFixable(method))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nonnull
	private List<String> rewrittenComponentTypes(@Nonnull DetailAST recordDef) {
		final var rewritten = new ArrayList<String>();
		final var components = recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		for (var component = components == null ? null : components.getFirstChild(); component != null; component = component.getNextSibling()) {
			if (component.getType() != TokenTypes.RECORD_COMPONENT_DEF)
				continue;

			final var type = component.findFirstToken(TokenTypes.TYPE);
			final var suffix = component.findFirstToken(TokenTypes.ELLIPSIS) == null ? "" : "[]";
			rewritten.add(type == null ? "" : rewrittenTypeName(component, AstUtil.canonicalType(type), true) + suffix);
		}
		return rewritten;
	}

	@CheckReturnValue
	@Nonnull
	private List<String> rewrittenParameterTypes(@Nonnull DetailAST defNode, boolean substitute) {
		final var types = new ArrayList<String>();
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return types;

		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			final var type = param.findFirstToken(TokenTypes.TYPE);
			if (type == null) {
				types.add("");
				continue;
			}

			// varargs erase to an array, making `f(T...)` a distinct overload from `f(T)`; the
			// ellipsis hangs off the parameter rather than the TYPE that canonicalType reads
			final var suffix = param.findFirstToken(TokenTypes.ELLIPSIS) == null ? "" : "[]";
			types.add(rewrittenTypeName(param, AstUtil.canonicalType(type), substitute) + suffix);
		}
		return types;
	}

	@CheckReturnValue
	@Nonnull
	private String rewrittenTypeName(@Nonnull DetailAST scope, @Nonnull String canonical, boolean substitute) {
		final var brackets = canonical.indexOf('[');
		final var written = brackets < 0 ? canonical : canonical.substring(0, brackets);
		final var suffix = brackets < 0 ? "" : canonical.substring(brackets);
		final var bound = typeParameterBound(scope, written);
		final var base = bound == null ? written : bound;

		// a same-file type is identified by where it is declared, not by how it was spelled, so
		// `Box` and `Outer.Box` cannot look like two different parameter types
		final var sameFile = AstUtil.sameFileClassDef(scope, base);
		if (sameFile != null)
			return nestingPath(sameFile) + suffix;

		// compared fully qualified: `List` and `java.util.List` name one type but differ as strings,
		// so comparing the written spellings would miss the collapse they produce together
		final var fqcn = resolve(base);
		if (fqcn == null)
			return base + suffix;

		final var iface = substitute ? ReflectionUtil.collectionInterfaceFqcn(fqcn) : null;
		if (iface != null)
			return iface + suffix;

		// a nested type has several spellings that all name it, and resolveClassName echoes back
		// whichever one was written, so two overloads spelling it differently would not compare equal
		final var binary = ReflectionUtil.binaryName(fqcn);
		return (binary == null ? fqcn : binary) + suffix;
	}

	/**
	 * The type of a {@code Type.field} receiver such as {@code System.out}, which
	 * {@link AstUtil#getReceiverTypeName} leaves unresolved.
	 */
	@CheckReturnValue
	@Nullable
	private String staticFieldReceiverType(@Nonnull DetailAST call) {
		final var dot = call.getFirstChild();
		final var receiver = dot == null || dot.getType() != TokenTypes.DOT ? null : dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.DOT)
			return null;

		final var owner = AstUtil.typeName(receiver.getFirstChild());
		if (owner != null && AstUtil.sameFileTypeBody(call, owner) != null)
			return null;

		final var field = receiver.getLastChild();
		final var fqcn = owner == null ? null : resolve(owner);
		return fqcn == null || field == null ? null : ReflectionUtil.fieldTypeName(fqcn, field.getText());
	}

	/**
	 * Whether the same-file superclass constructor a {@code super(...)} call selects declares a
	 * parameter at {@code index} that accepts {@code iface}. A superclass with no AST here answers
	 * no, since its constructors cannot be read.
	 */
	@CheckReturnValue
	private boolean superParameterAcceptsTheInterface(
			@Nonnull DetailAST call,
			int count,
			int index,
			@Nonnull String iface,
			@Nonnull String concrete
	) {
		DetailAST typeDef = null;
		for (var frame = call.getParent(); frame != null && typeDef == null; frame = frame.getParent()) {
			if (frame.getType() == TokenTypes.OBJBLOCK)
				typeDef = frame.getParent();
		}

		final var clause = typeDef == null ? null : typeDef.findFirstToken(TokenTypes.EXTENDS_CLAUSE);
		final var written = clause == null ? null : AstUtil.typeName(clause.getFirstChild());
		final var body = written == null ? null : AstUtil.sameFileTypeBody(typeDef, written);
		if (body == null)
			return false;

		var candidates = 0;
		for (var member = body.getFirstChild(); member != null; member = member.getNextSibling()) {
			if (member.getType() != TokenTypes.CTOR_DEF)
				continue;

			final var params = member.findFirstToken(TokenTypes.PARAMETERS);
			if (params == null)
				continue;

			final var declared = new ArrayList<DetailAST>();
			for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
				if (param.getType() == TokenTypes.PARAMETER_DEF)
					declared.add(param);
			}
			if (declared.size() != count || index >= count)
				continue;

			final var declaredType = declared.get(index).findFirstToken(TokenTypes.TYPE);
			if (!acceptsType(declaredType, concrete))
				continue;
			if (!acceptsType(declaredType, COLLECTION_PACKAGE + iface))
				return false;

			++candidates;
		}
		return candidates > 0;
	}

	/**
	 * What the construct consuming this use says about a value of {@code iface}. Positions that read
	 * the value through {@code Object} alone accept it; an assignment, a return, or an argument is
	 * decided against the declared type on the other side. Anything else refuses.
	 */
	@CheckReturnValue
	@Nonnull
	private UseOutcome targetAcceptsTheInterface(
			@Nonnull DetailAST use,
			@Nonnull String iface,
			@Nonnull String concrete
	) {
		var node = use;
		var parent = node.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR) {
			node = parent;
			parent = parent.getParent();
		}
		if (parent == null)
			return UseOutcome.REFUSED;

		final var consumer = node;
		return switch (parent.getType()) {
			case TokenTypes.ASSIGN -> {
				// an initializer's ASSIGN holds only the value, so the target has to be read off
				// the declaration rather than from the left of the assignment
				final var declaration = parent.getParent();
				if (declaration != null && declaration.getType() == TokenTypes.VARIABLE_DEF)
					yield UseOutcome.of(acceptsType(declaration.findFirstToken(TokenTypes.TYPE), COLLECTION_PACKAGE + iface));

				// writing through the name rather than reading it: the target is the very
				// declaration being widened, so whatever the value is, it still fits
				yield UseOutcome.of(parent.getFirstChild() == consumer);
			}

			case TokenTypes.ELIST -> UseOutcome.of(argumentAcceptsTheInterface(parent, consumer, iface, concrete));

			case TokenTypes.EQUAL, TokenTypes.FOR_EACH_CLAUSE, TokenTypes.LITERAL_INSTANCEOF,
			     TokenTypes.NOT_EQUAL, TokenTypes.PLUS -> UseOutcome.ACCEPTED;

			case TokenTypes.LITERAL_RETURN -> {
				DetailAST method = null;
				for (var frame = parent; frame != null && method == null; frame = frame.getParent()) {
					// a lambda's return answers to its target functional interface, which this walk
					// cannot read, rather than to the enclosing method's return type
					if (frame.getType() == TokenTypes.LAMBDA)
						yield UseOutcome.REFUSED;

					if (frame.getType() == TokenTypes.METHOD_DEF)
						method = frame;
				}
				final var returnType = method == null ? null : method.findFirstToken(TokenTypes.TYPE);
				final var declared = returnType == null ? null : returnType.getFirstChild();
				if (acceptsType(returnType, COLLECTION_PACKAGE + iface))
					yield UseOutcome.ACCEPTED;

				yield declared != null && iface.equals(collectionInterfaceOf(declared))
						? UseOutcome.coupledTo(method)
						: UseOutcome.REFUSED;
			}

			// an expression statement discards the value, so nothing reads it at any type
			case TokenTypes.SLIST -> UseOutcome.ACCEPTED;

			default -> UseOutcome.REFUSED;
		};
	}

	/**
	 * Every call and method reference in the file that selects {@code name}, indexed on first use.
	 */
	@CheckReturnValue
	@Nonnull
	private List<DetailAST> useSitesNamed(@Nonnull DetailAST anyNode, @Nonnull String name) {
		if (useSites == null) {
			useSites = new HashMap<>();
			var root = anyNode;
			while (root.getParent() != null)
				root = root.getParent();

			final var pending = new ArrayDeque<DetailAST>();
			pending.push(root);
			while (!pending.isEmpty()) {
				final var node = pending.pop();
				final var selected = selectedName(node);
				if (selected != null)
					useSites.computeIfAbsent(selected, key -> new ArrayList<>()).add(node);

				for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
					pending.push(child);
			}
		}
		return useSites.getOrDefault(name, List.of());
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.CTOR_DEF -> checkParameters(ast);
			case TokenTypes.METHOD_DEF -> {
				final var returnType = ast.findFirstToken(TokenTypes.TYPE);
				if (returnType != null && !overridesAClasspathMethod(ast) && !isRecordAccessor(ast))
					checkTypeTree(returnType, returnTypeIsAutoFixable(ast));
				checkParameters(ast);
			}
			case TokenTypes.RECORD_DEF -> checkRecordComponents(ast);
		}
	}
}