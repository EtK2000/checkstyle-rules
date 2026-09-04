package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that suggests using a literal suffix instead of a
 * widening cast when the other operand of a binary operator or ternary
 * branch is a numeric literal. For example,
 * {@code (long) x * 100} should be {@code x * 100L}, and
 * {@code flag ? (long) x : 0} should be {@code flag ? x : 0L}.
 */
public class PreferLiteralSuffixCheck extends AbstractResolvingCheck {
	/**
	 * The widening cast found at a located position, and the nodes a rewrite has to consume around it.
	 *
	 * <p>{@code literal} is the {@code NUM_INT} itself rather than a {@code UNARY_MINUS} wrapping it,
	 * while {@code literalText} is the operand as written, leading sign included, which is what the
	 * message names. {@code subject} is the cast's operand expression, which for a parenthesized
	 * operand is the {@code LPAREN}.
	 */
	public record LiteralSuffixTarget(
			@Nonnull DetailAST typecast,
			@Nonnull DetailAST subject,
			@Nonnull DetailAST literal,
			@Nonnull String literalText,
			@Nonnull String suffix
	) {}

	/** How far {@link #subjectKey} will walk down an operator expression before giving up. */
	private static final int MAX_SUBJECT_DEPTH = 64;

	/** The primitive types each cast target may stand in front of without changing the value. */
	private static final Map<String, Set<String>> WIDENS_TO = Map.of(
			"double", Set.of("byte", "char", "short", "int", "long", "float", "double"),
			"float", Set.of("byte", "char", "short", "int", "long", "float"),
			"long", Set.of("byte", "char", "short", "int", "long")
	);

	/** Binary numeric promotion's result, widest operand wins (JLS 5.6.2). */
	private static final List<String> PROMOTION_ORDER = List.of("int", "long", "float", "double");

	private static final String MSG_KEY = "prefer.literal.suffix";

	/**
	 * Binary numeric promotion's result type for two operand keys, or null when either is not a
	 * numeric primitive this file proved.
	 */
	@CheckReturnValue
	@Nullable
	private static String binaryPromoted(@Nullable String left, @Nullable String right) {
		final var a = unaryPromoted(left);
		final var b = unaryPromoted(right);
		if (a == null || b == null)
			return null;
		final var rank = Math.max(PROMOTION_ORDER.indexOf(a), PROMOTION_ORDER.indexOf(b));
		return rank < 0 ? null : PROMOTION_ORDER.get(rank);
	}

	/**
	 * Finds the other operand/branch relative to the given cast node.
	 * For arithmetic/bitwise operators, returns the sibling operand.
	 * For ternary, returns the other branch.
	 *
	 * <p>Steps over comment nodes because {@code targetAt} is public and its caller chooses the parse:
	 * under {@code WITH_COMMENTS} a comment ahead of the first operand becomes the operator's first
	 * child, which otherwise reads as "the other operand is not a literal" and silently classifies
	 * nothing. Measured: with a block comment written between {@code return} and the literal of
	 * {@code return 100 + (long) y;}, the {@code PLUS} node's first child is the comment.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findOtherOperand(@Nonnull DetailAST typecast) {
		final var parent = typecast.getParent();
		if (parent == null)
			return null;

		if (isBinaryNumericPromotion(parent.getType())) {
			final var first = AstText.firstRealChild(parent);
			return first == typecast ? AstText.nextRealSibling(typecast) : first;
		}

		// ternary: QUESTION has children [condition, trueBranch, COLON, falseBranch]
		if (parent.getType() == TokenTypes.QUESTION) {
			final var next = AstText.nextRealSibling(typecast);
			if (next != null && next.getType() == TokenTypes.COLON)
				return AstText.nextRealSibling(next);
			final var prev = AstText.previousRealSibling(typecast);
			if (prev != null && prev.getType() == TokenTypes.COLON)
				return AstText.previousRealSibling(prev);
		}

		return null;
	}

	@CheckReturnValue
	static boolean hasNegativeIntValueWhenWidened(@Nonnull String literalText) {
		// Only hex/binary NUM_INT literals can be negative in int and non-negative as long
		// (their textual value is unsigned but their int value sign-extends on widening).
		// Decimal NUM_INT is always non-negative; a leading sign is UNARY_MINUS/PLUS.
		final var normalised = literalText.replace("_", "");
		final var isHex = normalised.startsWith("0x") || normalised.startsWith("0X");
		final var isBinary = normalised.startsWith("0b") || normalised.startsWith("0B");
		if (!isHex && !isBinary)
			return false;
		final var digits = normalised.substring(2);
		try {
			return Integer.parseUnsignedInt(digits, isHex ? 16 : 2) < 0;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	@CheckReturnValue
	private static boolean isBinaryNumericPromotion(int tokenType) {
		// SL, SR, BSR intentionally excluded: shift result type/masking depends on
		// the LHS operand only (JLS 15.19), so swapping a cast for a suffix on the
		// RHS literal changes the semantic, not just the syntax.
		return switch (tokenType) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BXOR,
			     TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE,
			     TokenTypes.GT, TokenTypes.LE, TokenTypes.LT,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL,
			     TokenTypes.PLUS, TokenTypes.STAR -> true;
			default -> false;
		};
	}

	/**
	 * Whether {@code literalText} reads as a decimal {@code NUM_INT} a {@code d}/{@code f} suffix may
	 * be appended to. Both letters are hex digits, so a hex literal absorbs the suffix into its own
	 * digits ({@code 0xFF} is 255, {@code 0xFFd} is 4093), a binary literal stops compiling, and a
	 * leading zero turns an octal value into a decimal one ({@code 010} is 8, {@code 010d} is 10.0).
	 */
	@CheckReturnValue
	static boolean isPlainDecimal(@Nonnull String literalText) {
		final var normalised = literalText.replace("_", "");
		if (normalised.isEmpty() || (normalised.length() > 1 && normalised.charAt(0) == '0'))
			return false;
		for (var i = 0; i < normalised.length(); ++i) {
			if (normalised.charAt(i) < '0' || normalised.charAt(i) > '9')
				return false;
		}
		return true;
	}

	/**
	 * The primitive type {@code subject}'s value has, or null when this file does not prove one.
	 *
	 * <p>{@link ProvenTypes#keyOf} has no case for an operator node, so it answers null for every
	 * {@code -x}, {@code ~x}, {@code ++i} and {@code x + y}. Reading that null as "safe" would let a
	 * single {@code -} past a guard that refuses the bare subject, so the operator forms are typed
	 * here instead: {@code ++}/{@code --} keep the operand's own type (JLS 15.15.1) and a shift takes
	 * its promoted left operand (JLS 15.19).
	 */
	@CheckReturnValue
	@Nullable
	private static String subjectKey(@Nonnull DetailAST subject, @Nonnull ProvenTypes types, int depth) {
		final var node = AstQuery.unwrapParensAndExpr(subject);
		if (node == null || depth > MAX_SUBJECT_DEPTH)
			return null;

		final var left = AstText.firstRealChild(node);
		return switch (node.getType()) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BXOR, TokenTypes.DIV,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.PLUS, TokenTypes.STAR -> {
				final var right = AstQuery.unwrapParensAndExprFromEnd(node.getLastChild());
				yield left == null || right == null
						? null
						: binaryPromoted(subjectKey(left, types, depth + 1), subjectKey(right, types, depth + 1));
			}
			case TokenTypes.BNOT, TokenTypes.UNARY_MINUS, TokenTypes.UNARY_PLUS ->
					left == null ? null : unaryPromoted(subjectKey(left, types, depth + 1));
			case TokenTypes.BSR, TokenTypes.SL, TokenTypes.SR ->
					left == null ? null : unaryPromoted(subjectKey(left, types, depth + 1));
			case TokenTypes.DEC, TokenTypes.INC, TokenTypes.POST_DEC, TokenTypes.POST_INC ->
					left == null ? null : subjectKey(left, types, depth + 1);
			default -> types.keyOf(node);
		};
	}

	/** The expression the cast applies to: the first node after the cast's own closing parenthesis. */
	@CheckReturnValue
	@Nullable
	private static DetailAST subjectOf(@Nonnull DetailAST typecast) {
		final var rparen = typecast.findFirstToken(TokenTypes.RPAREN);
		return rparen == null ? null : AstText.nextRealSibling(rparen);
	}

	@CheckReturnValue
	@Nullable
	private static String suffixFor(@Nonnull String castType) {
		return switch (castType) {
			case "double" -> "d";
			case "float" -> "f";
			case "long" -> "L";
			default -> null;
		};
	}

	/**
	 * Returns the cast to rewrite at {@code line} (0-based) and {@code column} (a 0-based code-point
	 * column), or null when no reportable cast starts exactly there.
	 */
	@CheckReturnValue
	@Nullable
	public static LiteralSuffixTarget targetAt(@Nonnull DetailAST root, int line, int column) {
		final var typecast = AstQuery.findNodeAt(root, line, column, ast -> ast.getType() == TokenTypes.TYPECAST);
		if (typecast == null)
			return null;

		var unit = typecast;
		while (unit.getParent() != null)
			unit = unit.getParent();
		return targetFor(typecast, ProvenTypes.forRoot(unit));
	}

	@CheckReturnValue
	@Nullable
	private static LiteralSuffixTarget targetFor(@Nonnull DetailAST typecast, @Nonnull ProvenTypes types) {
		final var typeNode = typecast.findFirstToken(TokenTypes.TYPE);
		if (typeNode == null)
			return null;

		final var castType = typeName(typeNode);
		final var suffix = castType != null ? suffixFor(castType) : null;
		if (suffix == null)
			return null;

		final var other = findOtherOperand(typecast);
		if (other == null)
			return null;
		final var unwrapped = (other.getType() == TokenTypes.UNARY_MINUS || other.getType() == TokenTypes.UNARY_PLUS)
				? AstText.firstRealChild(other) : other;
		if (unwrapped == null || unwrapped.getType() != TokenTypes.NUM_INT)
			return null;

		// Hex/binary NUM_INT with bit 31 set: 0xFFFFFFFF as int is -1 (sign-extends to -1L
		// on widening), but 0xFFFFFFFFL as a long literal is +4294967295L. Removing the
		// cast would change the value.
		if (hasNegativeIntValueWhenWidened(unwrapped.getText()))
			return null;

		if (!"L".equals(suffix) && !isPlainDecimal(unwrapped.getText()))
			return null;

		// `-0` is the int zero, which the cast widens to a positive zero, while `-0d`/`-0f` is a
		// negative zero: `x * 0.0` and `x * -0.0` differ in the sign of the product
		if (!"L".equals(suffix) && other.getType() == TokenTypes.UNARY_MINUS
				&& unwrapped.getText().replace("_", "").chars().allMatch(c -> c == '0'))
			return null;

		final var subject = subjectOf(typecast);
		if (subject == null)
			return null;

		// the check's premise is that the cast only widens, which the cast type alone does not say:
		// `(long) aDouble * 100` narrows, and `aDouble * 100L` is a double expression with a
		// different value that does not even assign to a long. An unproven subject is refused too,
		// since "this file could not type it" is no evidence the cast widens
		// an immutable Set throws rather than answering false for a null member, so the unproven case
		// is tested before the lookup rather than through it
		final var subjectKey = subjectKey(subject, types, 0);
		if (subjectKey == null || !WIDENS_TO.getOrDefault(castType, Set.of()).contains(subjectKey))
			return null;

		return new LiteralSuffixTarget(
				typecast,
				subject,
				unwrapped,
				other == unwrapped ? unwrapped.getText() : other.getText() + unwrapped.getText(),
				suffix
		);
	}

	@CheckReturnValue
	@Nullable
	private static String typeName(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild == null)
			return null;

		return switch (firstChild.getType()) {
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			default -> null;
		};
	}

	/** Unary numeric promotion (JLS 5.6.1): the narrow integral types become {@code int}. */
	@CheckReturnValue
	@Nullable
	private static String unaryPromoted(@Nullable String key) {
		return switch (key == null ? "" : key) {
			case "byte", "char", "int", "short" -> "int";
			case "double", "float", "long" -> key;
			default -> null;
		};
	}

	@Nonnull
	private ProvenTypes types = new ProvenTypes(this::resolve, null);

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		types = new ProvenTypes(this::resolve, packageName());
	}

	/**
	 * Checkstyle reuses one check instance across files, so {@code types} left populated would pin
	 * the finished file's whole AST through the declarations it has keyed.
	 */
	@Override
	protected void finishFile(@Nullable DetailAST rootAST) {
		types = new ProvenTypes(this::resolve, null);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.TYPECAST};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast, types);
		if (target != null)
			log(ast, MSG_KEY, target.suffix(), target.literalText());
	}
}