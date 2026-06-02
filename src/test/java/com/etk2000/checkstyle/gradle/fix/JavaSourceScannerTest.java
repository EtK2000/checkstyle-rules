package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

public class JavaSourceScannerTest {
	@Test
	public void testContainsIdentifierAfterDot() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("java.util.Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierAfterTextBlock() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("String s = \"\"\"", "    nope", "    \"\"\";", "Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierAnnotationPosition() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("@Nonnull String s;"), "Nonnull"));
	}

	@Test
	public void testContainsIdentifierBlockCommentWithQuoteInside() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("/* contains \" quote */ Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierDigitIsIdentifierPart() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("Arrays2 a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierEmptyIdentifier() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("Arrays.asList(x)"), ""));
	}

	@Test
	public void testContainsIdentifierEmptyLines() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of(), "Arrays"));
	}

	@Test
	public void testContainsIdentifierEscapedBackslashClosesString() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("String s = \"a\\\\\"; Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierEscapedQuoteInString() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("String s = \"\\\"Arrays\\\"\";"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierFirstLineMatchReturnsImmediately() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("Arrays a;", "int y;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierFoundAsIdentifier() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("Arrays.asList(x)"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierFoundAtLineEnd() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("var a = Arrays"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierFoundAtLineStart() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierGenericPosition() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("List<String> a;"), "List"));
	}

	@Test
	public void testContainsIdentifierMatchAcrossMultipleLines() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("int x;", "Arrays a;", "int y;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierMatchAfterMultiLineBlockComment() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("/* nope */", "Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierMatchInsideBlockComment() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("int x; /* Arrays */ int y;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierMatchInsideCharLiteralIsRejected() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("char c = 'A';"), "A"));
	}

	@Test
	public void testContainsIdentifierMatchInsideLineComment() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("int x; // Arrays"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierMatchInsideMultiLineBlockComment() {
		assertFalse(JavaSourceScanner.containsIdentifier(
				List.of("/*", " * Arrays mentioned in javadoc", " */", "int x;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierMatchInsideStringLiteral() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("String s = \"Arrays\";"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierMethodRefPosition() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("Objects::requireNonNull"), "Objects"));
	}

	@Test
	public void testContainsIdentifierMultiLineBlockCommentWithQuoteInside() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("/* multi", "line with \" inside */", "Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierNotFoundSubstringBothSides() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("FooArraysBar"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierNotFoundSubstringLeft() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("MyArrays.foo()"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierNotFoundSubstringRight() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("ArraysHelper.foo()"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierSingleLineTextBlockIgnored() {
		assertFalse(JavaSourceScanner.containsIdentifier(
				List.of("String s = \"\"\"Arrays inside\"\"\";"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierStringContainingBlockCommentMarker() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("String s = \"/*\"; Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierStringContainingLineCommentMarker() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("String s = \"// nope\"; Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierStringFollowedByCodeMatch() {
		assertTrue(JavaSourceScanner.containsIdentifier(List.of("String s = \"x\"; Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierTextBlockContentIgnored() {
		assertFalse(JavaSourceScanner.containsIdentifier(
				List.of("String s = \"\"\"", "    Arrays in a text block", "    \"\"\";"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierTripleQuoteFollowedByCode() {
		assertTrue(JavaSourceScanner.containsIdentifier(
				List.of("String s = \"\"\"x\"\"\"; Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierUnderscoreIsIdentifierPart() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("Foo_Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsIdentifierUnterminatedBlockCommentSpansAllRemainingLines() {
		assertFalse(JavaSourceScanner.containsIdentifier(
				List.of("/* never closes", "Arrays a;", "int y;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsIdentifierWithBracketInIdentifierReturnsFalse() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("arr[0] = 1;"), "arr[0]"));
	}

	@Test
	public void testContainsIdentifierWithDotInIdentifierReturnsFalse() {
		assertFalse(JavaSourceScanner.containsIdentifier(List.of("a.b c;"), "a.b"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierAcceptsAtLineStart() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("Arrays a;"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierAcceptsMethodReferenceReceiver() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("Objects::requireNonNull"), "Objects"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierEmptyLines() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of(), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierMatchAcrossMultipleLines() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("int x;", "Arrays a;", "int y;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierMatchAfterMultiLineBlockComment() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("/*", " * nope", " */", "Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierMatchAfterTextBlock() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("String s = \"\"\"", "    nope", "    \"\"\";", "Arrays a;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierMatchInsideMultiLineBlockComment() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("/*", " * Arrays mentioned in javadoc", " */", "int x;"),
				"Arrays"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierMixedLineFirstQualifiedSecondUnqualified() {
		assertTrue(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("Assertions.assertTrue(f); assertTrue(g);"),
				"assertTrue"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierRejectsAfterDot() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("a.Arrays b;"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierRejectsAllOccurrencesQualified() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(
				List.of("a.assertTrue(); b.assertTrue();"),
				"assertTrue"
		));
	}

	@Test
	public void testContainsUnqualifiedIdentifierRejectsIdentifierPrefix() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("MyArrays.x"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierRejectsIdentifierSuffix() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("ArraysHelper.x"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierSkipsBlockComment() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("/* Arrays */"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierSkipsLineComment() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("// Arrays"), "Arrays"));
	}

	@Test
	public void testContainsUnqualifiedIdentifierSkipsStringLiteral() {
		assertFalse(JavaSourceScanner.containsUnqualifiedIdentifier(List.of("String s = \"Arrays\";"), "Arrays"));
	}
}