package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags redundant explicit array creation when calling varargs methods
 * or constructors. For example, {@code String.join(",", new CharSequence[]{"a", "b"})}
 * should be {@code String.join(",", "a", "b")}.
 *
 * <p>Uses reflection to confirm the target method/constructor is actually
 * varargs before flagging. Primitive arrays passed to reference-type varargs
 * are skipped because removing the array wrapper would change autoboxing
 * behavior.
 *
 * <p>Reflection loads the target through this check's own class loader, which carries the JDK and
 * the rule jar's dependencies and not the sources being analysed. A target declared in the
 * compilation unit under check therefore never resolves, and nothing is reported against it.
 */
public class RedundantArrayCreationCheck extends AbstractResolvingCheck {
	/**
	 * A varargs argument list's redundant array creation: the {@code new}, the {@code {...}} it
	 * wraps the arguments in, and the method or class name the violation names.
	 */
	public record ArrayArgTarget(
			@Nonnull DetailAST literalNew,
			@Nonnull DetailAST arrayInit,
			@Nonnull String targetName
	) {}

	private static final String MSG = "redundant.array.creation";

	/**
	 * {@code elist}'s last argument, or null when it is not an {@code EXPR}. Only the last argument
	 * can be the varargs one, and the parser leaves a lambda argument unwrapped, so scanning for
	 * the last {@code EXPR} instead would read a creation written <em>before</em> a lambda as the
	 * last argument.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findLastExpr(@Nonnull DetailAST elist) {
		DetailAST last = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				last = child;
		}
		return last != null && last.getType() == TokenTypes.EXPR ? last : null;
	}

	/**
	 * The {@code {...}} of the array creation {@code elist} ends with, or null when its last
	 * argument is not one.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findRedundantArrayInit(@Nonnull DetailAST elist) {
		final var lastExpr = findLastExpr(elist);
		if (lastExpr == null)
			return null;

		final var inner = lastExpr.getFirstChild();
		if (inner == null || inner.getType() != TokenTypes.LITERAL_NEW)
			return null;

		return inner.findFirstToken(TokenTypes.ARRAY_INIT);
	}

	/**
	 * Whether {@code arrayInit} holds exactly one element whose type cannot be proven to be
	 * neither an array nor {@code null}.
	 *
	 * <p>The wrapper is load-bearing for a lone element. {@code f(new T[]{e})} always passes an
	 * array of one, but {@code f(e)} is resolved by strict invocation first, so when {@code e} is
	 * assignable to {@code T[]} it becomes the whole varargs array instead of its first element.
	 * Measured: {@code Arrays.asList(new Object[]{row}).size()} is 1 and
	 * {@code Arrays.asList(row).size()} is 2, and {@code Arrays.asList(new Object[]{null})} is a
	 * one-element list while {@code Arrays.asList(null)} throws. Two or more elements cannot bind
	 * the fixed-arity form and zero elements pass an empty array either way, so only this cell is
	 * unsafe.
	 *
	 * <p>Answered from the shape of the element alone, by listing the forms that cannot produce an
	 * array rather than by resolving its type: an unresolved type would have to be read as safe to
	 * keep the common cases firing, and a wrong answer here rewrites a call into one that means
	 * something else. Anything outside the list keeps its wrapper, which is a deliberate false
	 * negative.
	 */
	@CheckReturnValue
	private static boolean hasUnprovenLoneElement(@Nonnull DetailAST arrayInit) {
		final var element = arrayInit.getFirstChild();
		if (element == null || element.getType() != TokenTypes.EXPR)
			return false;

		var next = element.getNextSibling();
		if (next != null && next.getType() == TokenTypes.COMMA)
			next = next.getNextSibling();
		if (next == null || next.getType() != TokenTypes.RCURLY)
			return false;

		final var value = element.getFirstChild();
		return value == null || !isProvablyNotAnArray(value);
	}

	@CheckReturnValue
	private static boolean isPrimitiveArray(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
				     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
				     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
				     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT ->
					{ return true; }
			}
		}
		return false;
	}

	/**
	 * Whether {@code value} is a form the language cannot give an array type, so that unwrapping it
	 * out of a lone-element initializer cannot re-bind it as the varargs array.
	 */
	@CheckReturnValue
	private static boolean isProvablyNotAnArray(@Nonnull DetailAST value) {
		return switch (value.getType()) {
			// arithmetic and shifts yield a number, comparisons and the logical operators a boolean,
			// a method reference a functional interface, and no literal is an array
			case TokenTypes.BAND, TokenTypes.BNOT, TokenTypes.BOR, TokenTypes.BSR, TokenTypes.BXOR,
			     TokenTypes.CHAR_LITERAL, TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE,
			     TokenTypes.GT, TokenTypes.LE, TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_INSTANCEOF,
			     TokenTypes.LITERAL_THIS, TokenTypes.LITERAL_TRUE, TokenTypes.LNOT, TokenTypes.LT,
			     TokenTypes.METHOD_REF, TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL,
			     TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG,
			     TokenTypes.PLUS, TokenTypes.SL, TokenTypes.SR, TokenTypes.STAR,
			     TokenTypes.STRING_LITERAL, TokenTypes.TEXT_BLOCK_LITERAL_BEGIN,
			     TokenTypes.UNARY_MINUS, TokenTypes.UNARY_PLUS -> true;
			case TokenTypes.LITERAL_NEW -> value.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null
					&& value.findFirstToken(TokenTypes.ARRAY_INIT) == null;
			case TokenTypes.TYPECAST -> {
				final var type = value.findFirstToken(TokenTypes.TYPE);
				yield type != null && type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null;
			}
			default -> false;
		};
	}

	/** The fully qualified name of {@code methodCall}'s receiver type, resolved through {@code scope}. */
	@CheckReturnValue
	@Nullable
	private static String receiverClassName(@Nonnull DetailAST methodCall, @Nonnull ResolutionScope scope) {
		final var receiverTypeName = AstResolve.getReceiverTypeName(methodCall, scope.packageName(), scope.imports());
		return receiverTypeName == null
				? null
				: ReflectionUtil.resolveClassName(receiverTypeName, scope.packageName(), scope.imports());
	}

	/**
	 * The redundant array creation whose {@code new} stands at {@code (line, column)}, or null when
	 * nothing there is one the check reports. {@code line} is 0-based and {@code column} is a
	 * 0-based code-point column.
	 *
	 * <p>The position is the array creation's, because that is the node {@link #visitScopedToken}
	 * logs against.
	 */
	@CheckReturnValue
	@Nullable
	public static ArrayArgTarget redundantArrayAt(@Nonnull DetailAST root, int line, int column) {
		final var arrayNew = AstQuery.findNodeAt(
				root,
				line,
				column,
				ast -> ast.getType() == TokenTypes.LITERAL_NEW && ast.findFirstToken(TokenTypes.ARRAY_INIT) != null
		);
		if (arrayNew == null)
			return null;

		final var expr = arrayNew.getParent();
		final var elist = expr == null ? null : expr.getParent();
		if (elist == null || elist.getType() != TokenTypes.ELIST)
			return null;

		final var owner = elist.getParent();
		final var target = owner == null ? null : targetFor(owner, ResolutionScope.of(root));
		return target != null && target.literalNew() == arrayNew ? target : null;
	}

	/** The redundant array creation {@code owner}'s argument list ends with, or null when there is none. */
	@CheckReturnValue
	@Nullable
	private static ArrayArgTarget targetFor(@Nonnull DetailAST owner, @Nonnull ResolutionScope scope) {
		final var elist = owner.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;

		final var arrayInit = findRedundantArrayInit(elist);
		if (arrayInit == null || hasUnprovenLoneElement(arrayInit))
			return null;

		final var constructor = owner.getType() == TokenTypes.LITERAL_NEW;
		final var name = constructor ? AstText.findNewClassName(owner) : AstQuery.getMethodName(owner);
		if (name == null)
			return null;

		final var fqcn = constructor
				? ReflectionUtil.resolveClassName(name, scope.packageName(), scope.imports())
				: receiverClassName(owner, scope);
		if (fqcn == null)
			return null;

		final var componentType = ReflectionUtil.getVarArgsComponentType(
				fqcn, constructor ? "new" : name, AstQuery.countArguments(elist)
		);
		final var literalNew = arrayInit.getParent();
		return componentType == null || isPrimitiveArray(literalNew) && !componentType.isPrimitive()
				? null
				: new ArrayArgTarget(literalNew, arrayInit, name);
	}

	private ResolutionScope scope = ResolutionScope.EMPTY;

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		scope = ResolutionScope.of(rootAST);
	}

	@Override
	protected void finishFile(@Nullable DetailAST rootAST) {
		scope = ResolutionScope.EMPTY;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast, scope);
		if (target != null)
			log(target.literalNew(), MSG, target.targetName());
	}
}