package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreferDoWhileCheckTest {
	private static final String DIR = "preferdowhile/";

	private static final String MESSAGE = "Replace pre-loop statement and 'while' with 'do-while'.";

	@Nullable
	private static DetailAST findFirst(@Nonnull DetailAST node, int tokenType) {
		if (node.getType() == tokenType)
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findFirst(child, tokenType);
			if (found != null)
				return found;
		}
		return null;
	}

	@Nonnull
	private static DetailAST parseSource(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("test", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferDoWhileCheck.class, DIR + "InputPreferDoWhileClean.java").isEmpty());
	}

	@Test
	public void testSemiSiblingBetweenExprAndWhile() throws Exception {
		final var ast = parseSource("class T { void f(int i) { ++i; while (i < 10) ++i; } }");
		final var literalWhile = findFirst(ast, TokenTypes.LITERAL_WHILE);
		assertNotNull(literalWhile);
		final var prev = literalWhile.getPreviousSibling();
		assertNotNull(prev);
		assertEquals(TokenTypes.SEMI, prev.getType());
	}

	@Test
	public void testSlistChildrenWithRcurlyTerminator() throws Exception {
		final var ast = parseSource("class T { void f(int i) { while (i < 10) { ++i; } } }");
		final var slist = findFirst(findFirst(ast, TokenTypes.LITERAL_WHILE), TokenTypes.SLIST);
		assertNotNull(slist);
		var sawExpr = false;
		var sawRcurly = false;
		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				sawExpr = true;
			if (child.getType() == TokenTypes.RCURLY)
				sawRcurly = true;
		}
		assertTrue(sawExpr);
		assertTrue(sawRcurly);
	}

	@Test
	public void testSlistEmptyBodyHasOnlyRcurly() throws Exception {
		final var ast = parseSource("class T { void f(int i) { while (i < 10) {} } }");
		final var slist = findFirst(findFirst(ast, TokenTypes.LITERAL_WHILE), TokenTypes.SLIST);
		assertNotNull(slist);
		assertEquals(TokenTypes.RCURLY, slist.getFirstChild().getType());
		assertNull(slist.getFirstChild().getNextSibling());
	}

	@Test
	public void testUnbracedBodyIsExpr() throws Exception {
		final var ast = parseSource("class T { void f(int i) { while (i < 10) ++i; } }");
		final var literalWhile = findFirst(ast, TokenTypes.LITERAL_WHILE);
		assertNotNull(literalWhile);
		final var rparen = literalWhile.findFirstToken(TokenTypes.RPAREN);
		assertNotNull(rparen);
		assertEquals(TokenTypes.EXPR, rparen.getNextSibling().getType());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferDoWhileCheck.class, DIR + "InputPreferDoWhileViolation.java");
		assertEquals(11, violations.size());
		final int[] expectedLines = {14, 20, 26, 32, 40, 46, 59, 67, 74, 80, 86};
		for (var i = 0; i < expectedLines.length; ++i) {
			assertEquals(expectedLines[i], violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
			assertEquals(MESSAGE, violations.get(i).getMessage());
		}
	}
}