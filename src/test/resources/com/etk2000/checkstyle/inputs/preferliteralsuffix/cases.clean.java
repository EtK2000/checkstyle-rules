package com.etk2000.checkstyle.inputs.preferliteralsuffix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

class InputLiteralSuffixClean {
	@Target(ElementType.TYPE_USE)
	@interface Ann {}

	interface IntCalc {
		int value();
	}

	// type-use annotation occupies TYPE's first child, so the cast type never resolves to a name
	void annotatedCastType() {
		final int x = 5;
		final long y = (@Ann long) x * 100;
	}

	// array cast type: the TYPE child is an ARRAY_DECLARATOR, not a primitive keyword
	void arrayCastType() {
		final Object raw = new long[1];
		final Object o = (long[]) raw;
	}

	// binary literal with bit 31 set (would sign-extend to -1L if cast removed)
	void binaryLiteralHighBitSet() {
		final int x = 5;
		final long y = (long) x | 0b10000000_00000000_00000000_00000000;
	}

	// `d` is not a binary digit, so `0b1010d` would not compile
	void binaryLiteralWithDoubleCast() {
		final int x = 5;
		final double y = (double) x * 0b1010;
	}

	// both operands are variables, cast on left
	void bothVariablesCastOnLeft() {
		final int x = 5;
		final int y = 10;
		final long z = (long) x * y;
	}

	// both operands are variables, cast on right
	void bothVariablesCastOnRight() {
		final int x = 5;
		final int y = 10;
		final long z = y * (long) x;
	}

	// a boxed subject resolves to a reference type, which is in none of the widening sets. The
	// rewrite would be value-identical, so this is a false negative the guard accepts deliberately
	void boxedIntegerSubject(Integer boxed) {
		final long y = (long) boxed * 100;
	}

	// grouping parens make the LPAREN the operator's first child, so the literal is never the sibling
	void castInsideEnclosingParens() {
		final int x = 5;
		final long y = ((long) x) * 100;
	}

	// cast on whole expression, not an operand
	void castOnWholeExpression() {
		final int x = 5;
		final long y = (long) (x * 100);
	}

	// cast to byte (no suffix exists)
	void castToByte() {
		final int x = 5;
		final byte y = (byte) x;
	}

	// cast to byte, cast on the left of a binary operator
	void castToByteLeftOperand() {
		final int x = 5;
		final int y = (byte) x & 255;
	}

	// cast to byte, cast on the right of a binary operator
	void castToByteRightOperand() {
		final int x = 5;
		final int y = 255 & (byte) x;
	}

	// cast to byte in a ternary's false branch
	void castToByteTernaryFalse(boolean flag) {
		final int x = 5;
		final int y = flag ? 0 : (byte) x;
	}

	// cast to byte in a ternary's true branch
	void castToByteTernaryTrue(boolean flag) {
		final int x = 5;
		final int y = flag ? (byte) x : 0;
	}

	// cast to char (no suffix exists)
	void castToCharLeftOperand() {
		final int x = 5;
		final int y = (char) x * 100;
	}

	// cast to char, cast on the right of a binary operator
	void castToCharRightOperand() {
		final int x = 5;
		final int y = 100 * (char) x;
	}

	// cast to char in a ternary's false branch
	void castToCharTernaryFalse(boolean flag) {
		final int x = 5;
		final int y = flag ? 0 : (char) x;
	}

	// cast to char in a ternary's true branch
	void castToCharTernaryTrue(boolean flag) {
		final int x = 5;
		final int y = flag ? (char) x : 0;
	}

	// cast to int (no suffix exists)
	void castToInt() {
		final byte x = 5;
		final int y = (int) x * 100;
	}

	// cast to int, cast on the right of a binary operator
	void castToIntRightOperand() {
		final byte x = 5;
		final int y = 100 * (int) x;
	}

	// cast to int in a ternary's false branch
	void castToIntTernaryFalse(boolean flag) {
		final byte x = 5;
		final int y = flag ? 0 : (int) x;
	}

	// cast to int in a ternary's true branch
	void castToIntTernaryTrue(boolean flag) {
		final byte x = 5;
		final int y = flag ? (int) x : 0;
	}

	// cast to short (no suffix exists)
	void castToShort() {
		final byte x = 5;
		final short y = (short) x;
	}

	// cast to short, cast on the left of a binary operator
	void castToShortLeftOperand() {
		final short x = 5;
		final int y = (short) x + 100;
	}

	// cast to short, cast on the right of a binary operator
	void castToShortRightOperand() {
		final short x = 5;
		final int y = 100 + (short) x;
	}

	// cast to short in a ternary's false branch
	void castToShortTernaryFalse(boolean flag) {
		final short x = 5;
		final int y = flag ? 0 : (short) x;
	}

	// cast to short in a ternary's true branch
	void castToShortTernaryTrue(boolean flag) {
		final short x = 5;
		final int y = flag ? (short) x : 0;
	}

	// the receiver's type is not on checkstyle's classpath, so the subject's type is never proven
	void chainedCallOnUnresolvedTypeSubject(StringBuilder sb) {
		final long y = (long) sb.append("a").length() * 100;
	}

	// a tool-directive lookalike in a comment is never parsed as a cast
	void commentWithCastText() {
		final int x = 5;
		// (long) x * 100
		final long y = x;
	}

	// compound assignment is not a binary-numeric-promotion operator
	void compoundAssignmentCast(long total) {
		final int x = 5;
		total *= (long) x;
	}

	// compound shift assignment, the boundary partner of the plain shifts
	void compoundShiftAssignmentCast(long total) {
		final int x = 5;
		total <<= (long) x;
	}

	// two unary minus nodes: the inner one is not a NUM_INT, so nothing pairs with the cast
	void doubleSignedLiteral() {
		final int x = 5;
		final long y = (long) x * - -100;
	}

	// a generic cast is a reference cast, so the cast type never resolves to a primitive
	void genericReferenceCast(Object raw) {
		final int y = ((List<Integer>) raw).size() + 100;
	}

	// hex literal exactly at bit 31 (would change value if suffix added)
	void hexLiteralBit31() {
		final int x = 5;
		final long y = (long) x | 0x80000000;
	}

	// hex literal with bit 31 set, with underscore separators (still suppresses)
	void hexLiteralBit31WithUnderscores() {
		final int x = 5;
		final long y = (long) x | 0x80_00_00_00;
	}

	// hex literal with all 32 bits set (would change value if suffix added)
	void hexLiteralFullMask() {
		final int x = 5;
		final long y = (long) x | 0xFFFFFFFF;
	}

	// `d` is a hex digit, so `0xFFd` is the int 4093 rather than the double 255
	void hexLiteralWithDoubleCast() {
		final int x = 5;
		final double y = (double) x * 0xFF;
	}

	// `f` is a hex digit too, so `0xFFf` is the int 4095
	void hexLiteralWithFloatCast() {
		final int x = 5;
		final float y = (float) x * 0xFF;
	}

	// underscores do not change which digits the suffix would join
	void hexLiteralWithUnderscoresAndDoubleCast() {
		final int x = 5;
		final double y = (double) x * 0xFF_FF;
	}

	// `++d` keeps the operand's own type (JLS 15.15.1), so the cast narrows
	void incrementSubjectDouble(double d) {
		final long y = (long) ++d * 100;
	}

	// a poly argument stops the call's return type resolving, so the subject is unproven
	void lambdaArgumentCallSubject() {
		final long y = (long) run(() -> 1) * 100;
	}

	// literal already has the right suffix (no cast needed)
	void literalAlreadyHasSuffix() {
		final int x = 5;
		final long y = x * 100L;
	}

	// grouping parens around the literal make the LPAREN the cast's sibling, not the NUM_INT
	void literalInsideParens() {
		final int x = 5;
		final long y = (long) x * (100);
	}

	// literal is a double (not NUM_INT, no cast needed)
	void literalIsDouble() {
		final int x = 5;
		final double d = x * 1.5;
	}

	// literal is a float (not NUM_INT, no cast needed)
	void literalIsFloat() {
		final int x = 5;
		final float f = x * 1.5f;
	}

	// `d + x` promotes to double, so the cast narrows and `(d + x) * 100L` would not assign to a long
	void narrowingBinarySubjectDoublePlusInt(double d, int x) {
		final long y = (long) (d + x) * 100;
	}

	// the widest operand decides, whichever side it is on: `x + d` is double, so the cast narrows
	void narrowingBinarySubjectIntPlusDouble(int x, double d) {
		final long y = (long) (x + d) * 100;
	}

	// a parenthesized left operand once made the right one read as a second copy of itself, so this
	// typed as int and the fix emitted a double expression that would not compile
	void narrowingBinarySubjectParenthesizedLeft(int hi, int lo) {
		final long y = (long) ((hi + lo) / 2.0) * 1000;
	}

	// a boxed operand has no promoted type, so the binary subject's own type is never proven
	void narrowingBinarySubjectUnprovenOperand(Integer boxed, int x) {
		final long y = (long) (x + boxed) * 100;
	}

	// the cast narrows a double literal, so dropping it would leave double arithmetic
	void narrowingCastOnDoubleLiteral() {
		final long y = (long) 1.9 * 100;
	}

	// `(long) d` narrows: `d * 100L` is a double expression, which does not even assign to a long
	void narrowingCastOnDoubleParameter(double d) {
		final long y = (long) d * 100;
	}

	// same for a float subject
	void narrowingCastOnFloatParameter(float f) {
		final long y = (long) f * 100;
	}

	// the nested cast makes the subject's type provable without resolving a declaration
	void narrowingCastOnNestedFloatCast(int x) {
		final long y = (long) (float) x * 100;
	}

	// grouping parens do not hide the subject's type: keyOf unwraps them before resolving
	void narrowingCastOnParenthesizedDoubleParameter(double d) {
		final long y = (long) (d) * 100;
	}

	// double does not widen to float either
	void narrowingDoubleToFloatCast(double d) {
		final float y = (float) d * 100;
	}

	// a shift's type is its promoted left operand's, and a boxed one proves none
	void narrowingShiftSubjectUnprovenOperand(Integer boxed) {
		final long y = (long) (boxed << 1) * 100;
	}

	// `-d` stays double under unary promotion, so the cast narrows
	void narrowingUnarySubjectDouble(double d) {
		final long y = (long) -d * 100;
	}

	// `-0` is the int zero, which the cast widens to `+0.0`, while `-0d` is a negative zero
	void negativeZeroWithDoubleCast() {
		final int x = 5;
		final double y = (double) x * -0;
	}

	// the constructed type is not on checkstyle's classpath, so the chain's result is never proven
	void newExpressionSubject() {
		final long y = (long) new StringBuilder("ab").length() * 100;
	}

	// not in arithmetic context (method arguments)
	void notArithmeticArgs() {
		final int x = 5;
		takesLong((long) x);
	}

	// a leading zero is octal only for integers: `010` is 8 but `010d` is 10.0
	void octalLiteralWithDoubleCast() {
		final int x = 5;
		final double y = (double) x * 010;
	}

	// a reference cast has an IDENT under TYPE, so no suffix exists for it
	void referenceCast(Object boxed) {
		final int y = (Integer) boxed + 100;
	}

	int run(IntCalc c) {
		return c.value();
	}

	// shift-left: result type depends on LHS only, suffix would change semantics
	void shiftLeftCast() {
		final int x = 5;
		final long y = (long) x << 32;
	}

	// shift-right: ditto
	void shiftRightCast() {
		final int x = 5;
		final long y = (long) x >> 1;
	}

	// unsigned shift-right: ditto
	void shiftUnsignedRightCast() {
		final int x = 5;
		final long y = (long) x >>> 1;
	}

	// shift with cast on RHS: shift type still decided by LHS literal (int)
	void shiftWithCastOnRight() {
		final int x = 5;
		final long y = 1 << (long) x;
	}

	// sibling is a complex expression, cast on left
	void siblingIsExpressionCastOnLeft() {
		final int x = 5;
		final int y = 10;
		final long z = (long) x * (y + 100);
	}

	// sibling is a complex expression, cast on right
	void siblingIsExpressionCastOnRight() {
		final int x = 5;
		final int y = 10;
		final long z = (y + 100) * (long) x;
	}

	// sibling is NUM_LONG with different cast type
	void siblingIsLongLiteralDifferentCast() {
		final int x = 5;
		final double d = (double) x * 100L;
	}

	// the same unproven receiver, with the chain split across lines
	void splitChainedCallSubject(StringBuilder sb) {
		final long y = (long) sb
				.length() * 100;
	}

	// a cast lookalike inside a string literal is text, never a TYPECAST
	void stringWithCastText() {
		final String s = "(long) x * 100";
		final int y = s.length();
	}

	// checkstyle's lexer reads the supplementary character as part of the identifier, but javac
	// rejects it, so no declaration matches and the subject's type is never proven
	void supplementaryIdentifierSubject(int x) {
		final long y = (long) x😀 + 100;
	}

	void takesLong(long x) {}

	// the nested ternary's QUESTION is the other branch, not a NUM_INT
	void ternaryNestedOtherBranch(boolean flag, boolean flag2) {
		final int x = 5;
		final long z = flag ? (long) x : flag2 ? 0 : 1;
	}

	// ternary: other branch is not a literal
	void ternaryOtherBranchNotLiteral(boolean flag) {
		final int x = 5;
		final int y = 10;
		final long z = flag ? (long) x : y;
	}

	// grouping parens around the other branch put an LPAREN where the NUM_INT would be
	void ternaryParenthesizedOtherBranch(boolean flag) {
		final int x = 5;
		final long z = flag ? (long) x : (0);
	}

	// a cast lookalike inside a text block is text, never a TYPECAST
	void textBlockWithCastText() {
		final String s = """
				(long) x * 100""";
		final int y = s.length();
	}

	// the unary node wraps a float literal, so the unwrapped node is not a NUM_INT
	void unaryWrappedFloatSibling() {
		final int x = 5;
		final double z = (long) x * -1.5;
	}
}