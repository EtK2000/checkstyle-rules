package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreferDoWhileCheckTest {
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
		final var tempFile = File.createTempFile("test", ".java");
		try {
			Files.writeString(tempFile.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tempFile, StandardCharsets.UTF_8.name())));
		}
		finally {
			tempFile.delete();
		}
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
}