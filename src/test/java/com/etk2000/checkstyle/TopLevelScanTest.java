package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TopLevelScan.Brackets;
import com.etk2000.checkstyle.TopLevelScan.Target;
import com.etk2000.checkstyle.TopLevelScan.Unbalanced;
import com.etk2000.checkstyle.TopLevelScan.Underflow;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TopLevelScanTest {
	private static final Target ANY_EQUALS = (masked, index) -> masked.charAt(index) == '=';

	private static final Target INSTANCEOF = (masked, index) -> masked.startsWith(" instanceof ", index);

	/**
	 * An {@code =} that is not half of {@code ==}. Exists to exercise the primitive's
	 * reject-and-continue contract and the bound-checking a neighbour-reading target has to
	 * do; the production veto sets live with their own call sites, not here.
	 */
	private static final Target LONE_EQUALS = (masked, index) -> masked.charAt(index) == '='
			&& (index == 0 || masked.charAt(index - 1) != '=')
			&& (index + 1 >= masked.length() || masked.charAt(index + 1) != '=');

	private static final Target LOWERCASE_A = (masked, index) -> masked.charAt(index) == 'a';
	private static final Target PAREN = (masked, index) -> masked.charAt(index) == '(' || masked.charAt(index) == ')';
	private static final TopLevelScan ALL_CLAMP = new TopLevelScan(Brackets.ALL, Underflow.CLAMP);
	private static final TopLevelScan ALL_SIGNED = new TopLevelScan(Brackets.ALL, Underflow.SIGNED);
	private static final TopLevelScan NO_BRACES_CLAMP = new TopLevelScan(Brackets.NO_BRACES, Underflow.CLAMP);
	private static final TopLevelScan NO_BRACES_SIGNED = new TopLevelScan(Brackets.NO_BRACES, Underflow.SIGNED);

	@Test
	public void testContainsBraceGroupClosesSoLaterTargetFound() {
		assertTrue(ALL_CLAMP.contains("{a} , b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsBraceGroupHidesTarget() {
		assertFalse(ALL_CLAMP.contains("x -> { a + b }", '+', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsBracketInsideStringLiteralDoesNotOpenGroup() {
		assertTrue(ALL_CLAMP.contains("\"(\" , x", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsClampExcessCloseEndsBalancedSoUnbalancedNotReported() {
		assertFalse(ALL_CLAMP.contains("a)b", ',', Unbalanced.FOUND));
	}

	@Test
	public void testContainsClampExcessCloseLeavesTargetTopLevel() {
		assertTrue(ALL_CLAMP.contains("a) , b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsEmptyTextIsFalse() {
		assertFalse(ALL_CLAMP.contains("", ',', Unbalanced.NOT_FOUND));
		assertFalse(ALL_CLAMP.contains("", ',', Unbalanced.FOUND));
	}

	@Test
	public void testContainsInterleavedBracketKindsHideTarget() {
		assertFalse(ALL_CLAMP.contains("a([{ , }])", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsNoBracesBraceGroupDoesNotHideTarget() {
		assertTrue(NO_BRACES_CLAMP.contains("x -> { a + b }", '+', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsNoBracesSignedExcessCloseHidesTarget() {
		assertFalse(NO_BRACES_SIGNED.contains("a) , b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsParenGroupHidesTarget() {
		assertFalse(ALL_CLAMP.contains("f(a, b)", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsSignedExcessCloseEndsNegativeSoUnbalancedReported() {
		assertTrue(ALL_SIGNED.contains("a)b", ',', Unbalanced.FOUND));
	}

	@Test
	public void testContainsSignedExcessCloseHidesTarget() {
		assertFalse(ALL_SIGNED.contains("a) , b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsSquareGroupClosesSoLaterTargetFound() {
		assertTrue(ALL_CLAMP.contains("m[a] , b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsSquareGroupHidesTarget() {
		assertFalse(ALL_CLAMP.contains("m[a, b]", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetAfterClosedGroupFound() {
		assertTrue(ALL_CLAMP.contains("f(a), b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetAfterEscapedQuoteStillInsideStringIgnored() {
		assertFalse(ALL_CLAMP.contains("s = \"a\\\",b\"", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideBlockCommentIgnored() {
		assertFalse(ALL_CLAMP.contains("x /* a, b */ y", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideCharLiteralIgnored() {
		assertFalse(ALL_CLAMP.contains("c = ','", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideLineCommentIgnored() {
		assertFalse(ALL_CLAMP.contains("x // a, b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideStringLiteralIgnored() {
		assertFalse(ALL_CLAMP.contains("x = \"a,b\"", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideTextBlockIgnored() {
		assertFalse(ALL_CLAMP.contains("s = \"\"\"a,b\"\"\"", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetInsideUnterminatedBlockCommentIgnored() {
		assertFalse(ALL_CLAMP.contains("x /* a, b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsTargetThatIsABracketNeverFound() {
		assertFalse(ALL_CLAMP.contains("(", '(', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsUnbalancedFoundBalancedWithoutTargetIsFalse() {
		assertFalse(ALL_CLAMP.contains("f(a)", ',', Unbalanced.FOUND));
	}

	@Test
	public void testContainsUnbalancedFoundReportsUnclosedGroup() {
		assertTrue(ALL_CLAMP.contains("f(a", ',', Unbalanced.FOUND));
	}

	@Test
	public void testContainsUnbalancedNotFoundBalancedWithoutTargetIsFalse() {
		assertFalse(ALL_CLAMP.contains("f(a)", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsUnbalancedNotFoundIgnoresUnclosedGroup() {
		assertFalse(ALL_CLAMP.contains("f(a", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testContainsUnbalancedNotFoundStillReportsRealHit() {
		assertTrue(ALL_CLAMP.contains("a, f(b", ',', Unbalanced.NOT_FOUND));
	}

	@Test
	public void testIndexOfBraceGroupHidesTarget() {
		assertEquals(-1, ALL_SIGNED.indexOf("{ a = b }", ANY_EQUALS));
	}

	@Test
	public void testIndexOfClampExcessCloseLeavesTargetTopLevel() {
		assertEquals(3, ALL_CLAMP.indexOf("a) = b", ANY_EQUALS));
	}

	@Test
	public void testIndexOfEmptyTextReturnsMinusOne() {
		assertEquals(-1, ALL_SIGNED.indexOf("", ANY_EQUALS));
	}

	@Test
	public void testIndexOfLineCommentMaskedTargetNotFound() {
		assertEquals(-1, ALL_SIGNED.indexOf("a // x = y", ANY_EQUALS));
	}

	@Test
	public void testIndexOfMultiCharTargetMatched() {
		assertEquals(1, ALL_SIGNED.indexOf("a instanceof B", INSTANCEOF));
	}

	@Test
	public void testIndexOfMultiCharTargetTruncatedAtEndNotMatched() {
		assertEquals(-1, ALL_SIGNED.indexOf("a instanceo", INSTANCEOF));
	}

	@Test
	public void testIndexOfNoBracesBraceGroupDoesNotHideTarget() {
		assertEquals(4, NO_BRACES_SIGNED.indexOf("{ a = b }", ANY_EQUALS));
	}

	@Test
	public void testIndexOfNoBracesClampExcessCloseLeavesTargetTopLevel() {
		assertEquals(3, NO_BRACES_CLAMP.indexOf("a) = b", ANY_EQUALS));
	}

	@Test
	public void testIndexOfParenGroupHidesTarget() {
		assertEquals(-1, ALL_SIGNED.indexOf("f(a = b)", ANY_EQUALS));
	}

	@Test
	public void testIndexOfReturnsIndexIntoOriginalText() {
		final var text = "\"a,b\" = c";
		final var index = ALL_SIGNED.indexOf(text, ANY_EQUALS);
		assertEquals(6, index);
		assertEquals('=', text.charAt(index));
	}

	@Test
	public void testIndexOfSignedExcessCloseHidesTarget() {
		assertEquals(-1, ALL_SIGNED.indexOf("a) = b", ANY_EQUALS));
	}

	@Test
	public void testIndexOfSignedReopenAfterExcessCloseRestoresTopLevel() {
		assertEquals(5, ALL_SIGNED.indexOf("a) ( = b", ANY_EQUALS));
	}

	@Test
	public void testIndexOfSquareGroupHidesTarget() {
		assertEquals(-1, ALL_SIGNED.indexOf("m[a = b]", ANY_EQUALS));
	}

	@Test
	public void testIndexOfTargetMatchingAtFirstIndex() {
		assertEquals(0, ALL_SIGNED.indexOf("= a", ANY_EQUALS));
	}

	@Test
	public void testIndexOfTargetMatchingAtLastIndex() {
		assertEquals(2, ALL_SIGNED.indexOf("a =", ANY_EQUALS));
	}

	@Test
	public void testIndexOfTargetNeverMatchingReturnsMinusOne() {
		assertEquals(-1, ALL_SIGNED.indexOf("abc", ANY_EQUALS));
	}

	@Test
	public void testIndexOfTargetNotOfferedBracketPositions() {
		assertEquals(-1, ALL_SIGNED.indexOf("()", PAREN));
	}

	@Test
	public void testIndexOfTargetReadingNeighbourAtFirstIndex() {
		assertEquals(0, ALL_SIGNED.indexOf("= a", LONE_EQUALS));
	}

	@Test
	public void testIndexOfTargetReadingNeighbourAtLastIndex() {
		assertEquals(2, ALL_SIGNED.indexOf("a =", LONE_EQUALS));
	}

	@Test
	public void testIndexOfTargetRejectingCandidateContinuesScan() {
		assertEquals(7, ALL_SIGNED.indexOf("a == b = c", LONE_EQUALS));
	}

	@Test
	public void testIndexOfTargetSeesMaskedTextNotOriginal() {
		assertEquals(-1, ALL_SIGNED.indexOf("\"a\"", LOWERCASE_A));
	}

	@Test
	public void testSplitBlockCommentSeparatorNotSplitButPreserved() {
		assertEquals(List.of("a /*, */ b"), ALL_SIGNED.split("a /*, */ b", ','));
	}

	@Test
	public void testSplitBraceGroupHidesSeparator() {
		assertEquals(List.of("a", " {b, c}"), ALL_SIGNED.split("a, {b, c}", ','));
	}

	@Test
	public void testSplitClampExcessCloseSplitsAfterIt() {
		assertEquals(List.of("a)", " b"), ALL_CLAMP.split("a), b", ','));
	}

	@Test
	public void testSplitConsecutiveSeparatorsYieldEmptyMiddlePart() {
		assertEquals(List.of("a", "", "b"), ALL_SIGNED.split("a,,b", ','));
	}

	@Test
	public void testSplitDoesNotTrimParts() {
		assertEquals(List.of("a ", " b"), ALL_SIGNED.split("a , b", ','));
	}

	@Test
	public void testSplitEmptyTextReturnsSingleEmptyPart() {
		assertEquals(List.of(""), ALL_SIGNED.split("", ','));
	}

	@Test
	public void testSplitLeadingSeparatorYieldsEmptyFirstPart() {
		assertEquals(List.of("", "a"), ALL_SIGNED.split(",a", ','));
	}

	@Test
	public void testSplitNestedGroupsHideSeparators() {
		assertEquals(List.of("f(a, g(b, c))", " d"), ALL_SIGNED.split("f(a, g(b, c)), d", ','));
	}

	@Test
	public void testSplitNoBracesBraceAsSeparatorSplits() {
		assertEquals(List.of("a", "b"), NO_BRACES_SIGNED.split("a}b", '}'));
	}

	@Test
	public void testSplitNoBracesBraceGroupDoesNotHideSeparator() {
		assertEquals(List.of("a", " {b", " c}"), NO_BRACES_SIGNED.split("a, {b, c}", ','));
	}

	@Test
	public void testSplitNoBracesClampExcessCloseSplitsAfterIt() {
		assertEquals(List.of("a)", " b"), NO_BRACES_CLAMP.split("a), b", ','));
	}

	@Test
	public void testSplitReturnsMutableList() {
		final var parts = ALL_SIGNED.split("a,b", ',');
		parts.add("c");
		assertEquals(List.of("a", "b", "c"), parts);
	}

	@Test
	public void testSplitSeparatorThatIsABracketNeverSplits() {
		assertEquals(List.of("a(b"), ALL_SIGNED.split("a(b", '('));
	}

	@Test
	public void testSplitSignedExcessCloseSuppressesSplit() {
		assertEquals(List.of("a), b"), ALL_SIGNED.split("a), b", ','));
	}

	@Test
	public void testSplitSlicesFromOriginalPreservingLiteralContent() {
		assertEquals(List.of("a", " \"x,y\"", " b"), ALL_SIGNED.split("a, \"x,y\", b", ','));
	}

	@Test
	public void testSplitSquareGroupHidesSeparator() {
		assertEquals(List.of("a[b, c]"), ALL_SIGNED.split("a[b, c]", ','));
	}

	@Test
	public void testSplitTrailingSeparatorYieldsEmptyLastPart() {
		assertEquals(List.of("a", ""), ALL_SIGNED.split("a,", ','));
	}

	@Test
	public void testSplitUnclosedGroupStillSplitsEarlierTopLevel() {
		assertEquals(List.of("a", " f(b"), ALL_SIGNED.split("a, f(b", ','));
	}

	@Test
	public void testSplitWithoutSeparatorReturnsWholeText() {
		assertEquals(List.of("abc"), ALL_SIGNED.split("abc", ','));
	}
}