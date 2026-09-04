package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.assertSameWithAndWithoutComments;
import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.findMethod;
import static com.etk2000.checkstyle.ast.AstTestSupport.parse;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseExprFirstChild;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSource;
import static com.etk2000.checkstyle.ast.AstTestSupport.root;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

public class AstTestSupportTest {
	@Test
	public void testAssertSameWithAndWithoutCommentsFailsWhenTheParsesDisagree() {
		// isEmptyBody is the one measured disagreement deliberately left standing (a comment-only
		// block is empty WITHOUT_COMMENTS and non-empty WITH_COMMENTS), so it doubles as this
		// helper's negative case without inventing a fake one
		final var thrown = assertThrows(
				AssertionError.class,
				() -> assertSameWithAndWithoutComments(
						"class T { void m(int x) { if (x > 0) { /*only*/ } } }",
						true,
						root -> AstQuery.isEmptyBody(findFirst(findFirst(root, TokenTypes.LITERAL_IF), TokenTypes.SLIST))
				)
		);
		assertTrue(thrown.getMessage().contains("WITH_COMMENTS"), "the failure must name the half that disagreed: " + thrown.getMessage());
	}

	@Test
	public void testAssertSameWithAndWithoutCommentsPassesWhenTheParsesAgree() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object a) { return a./*c*/b; } }",
				"ab",
				root -> AstText.exprText(findFirst(findFirst(root, TokenTypes.LITERAL_RETURN), TokenTypes.EXPR))
		);
	}

	@Test
	public void testFindFirstReturnsNullWhenNoDescendantMatches() throws Exception {
		assertNull(findFirst(parseSource("class T { void f() {} }"), TokenTypes.LITERAL_TRY));
	}

	@Test
	public void testFindFirstReturnsTheArgumentWhenItAlreadyMatches() throws Exception {
		final var method = findFirst(parseSource("class T { void f() {} }"), TokenTypes.METHOD_DEF);
		assertNotNull(method);
		assertSame(method, findFirst(method, TokenTypes.METHOD_DEF));
	}

	@Test
	public void testFindMethodReturnsNullWhenNoMethodMatches() throws Exception {
		assertNull(findMethod(parseSource("class T { void f() {} }"), "g"));
	}

	@Test
	public void testParseExprFirstChildKeepsBareOperandOfAssignmentStatement() throws Exception {
		final var value = parseExprFirstChild("class T { void f(int x) { x = 5; } }");
		assertEquals(TokenTypes.IDENT, value.getType());
		assertEquals("x", value.getText());
	}

	@Test
	public void testParseExprFirstChildRejectsSourceWithoutAnAssignment() {
		final var thrown = assertThrows(NullPointerException.class, () -> parseExprFirstChild("class T { void f() {} }"));
		assertEquals("No ASSIGN found", thrown.getMessage());
	}

	@Test
	public void testParseExprFirstChildUnwrapsExprInitializer() throws Exception {
		final var value = parseExprFirstChild("class T { void f() { int x = 5; } }");
		assertEquals(TokenTypes.NUM_INT, value.getType());
		assertEquals("5", value.getText());
	}

	@Test
	public void testParseNamesTheMissingResourceInTheFailure() {
		final var thrown = assertThrows(NullPointerException.class, () -> parse("astutil/NoSuchFixture.java"));
		assertTrue(
				thrown.getMessage().contains("astutil/NoSuchFixture.java"),
				"a missing fixture must name itself, not just fail: " + thrown.getMessage()
		);
	}

	@Test
	public void testRootIsParsedOnceAndSharedAcrossCalls() {
		assertSame(root(), root());
	}
}