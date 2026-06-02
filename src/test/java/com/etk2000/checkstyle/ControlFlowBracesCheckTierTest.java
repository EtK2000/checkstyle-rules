package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class ControlFlowBracesCheckTierTest {
	@Nonnull
	private static DetailAST findDoBody(@Nonnull String doWhileCode) throws Exception {
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), wrap(doWhileCode));
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		if (doAst == null)
			throw new AssertionError("No LITERAL_DO found in: " + doWhileCode);
		return doAst.getFirstChild();
	}

	@Nonnull
	private static DetailAST findDoNode(@Nonnull String doWhileCode) throws Exception {
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), wrap(doWhileCode));
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		if (doAst == null)
			throw new AssertionError("No LITERAL_DO found in: " + doWhileCode);
		return doAst;
	}

	private static DetailAST findFirst(@Nonnull DetailAST root, int tokenType) {
		for (var node = root; node != null; node = node.getNextSibling()) {
			if (node.getType() == tokenType)
				return node;
			final var child = findFirst(node.getFirstChild(), tokenType);
			if (child != null)
				return child;
		}
		return null;
	}

	@Nonnull
	private static DetailAST findKeyword(@Nonnull String code, int tokenType) throws Exception {
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), wrap(code));
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var keyword = findFirst(ast, tokenType);
		if (keyword == null)
			throw new AssertionError("No token " + tokenType + " found in: " + code);
		return keyword;
	}

	static Stream<Arguments> tierProvider() {
		return Stream.of(
				Arguments.of("do --x; while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x--; while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x++; while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do --x; while (x > 0 && x < 100);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do --x; while (x > 0 || x < 100);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do System.out.println(x); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = System.out.hashCode(); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = a.b.c; while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do a.b.c(); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = a.b(); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = f(g(y)); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = -y; while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = a.b().c(); while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = -y - 1; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x + y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x - y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x * y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x / y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x % y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x & y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x | y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x ^ y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x << y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x >> y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = x >>> y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				// bare comparison RHS: the ternary rows below cannot discriminate these arms,
				// because QUESTION is in the same subtree and matches on its own
				Arguments.of("do x = a == b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a != b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a < b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a > b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a <= b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a >= b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a && b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a || b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a == b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a < b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a > b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a <= b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a >= b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a != b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a && b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x = a || b ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				// bare condition: every other ternary row carries a comparison, which matches instead
				Arguments.of("do x = a ? 1 : 2; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x += 5 * y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x -= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x *= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x /= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x %= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x &= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x |= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x ^= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x <<= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x >>= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x >>>= a + b; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do new Object(); while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do a.b().c.d(); while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do this.helper().chain(); while (x > 0);", ControlFlowBracesCheck.TIER_3)
		);
	}

	@Nonnull
	private static String wrap(@Nonnull String code) {
		return "class T { void f(int x, int y) { " + code + " } }";
	}

	@Test
	public void testAssignNewExprRhsIsTier3() throws Exception {
		final var source = "class T { void f(int x, Object o) { do o = new Object(); while (x > 0); } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		assertEquals(ControlFlowBracesCheck.TIER_3, ControlFlowBracesCheck.determineTier(doAst.getFirstChild()));
	}

	@Test
	public void testBodyAtBlockBodyReportsBracePosition() throws Exception {
		final var body = "if (x > 0) { --x; }";
		final var ifAst = findKeyword(body, TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertTrue(found.block());
		assertEquals(ifAst.getLineNo() - 1, found.line());
		assertEquals(wrap(body).indexOf('{', wrap(body).indexOf("if (")), found.column());
		assertEquals(ifAst.getLineNo() - 1, found.headerLine());
	}

	@Test
	public void testBodyAtBracedInnerBodyReportsTrailingStatement() throws Exception {
		final var ifAst = findKeyword("if (x > 0)\n\t\twhile (y > 0) { --y; } ++x;", TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(ifAst.getLineNo(), found.endLine());
		assertEquals("\t\twhile (y > 0) { --y; } ".length(), found.endColumn());
		assertEquals(ifAst.getLineNo(), found.lastLine());
	}

	@Test
	public void testBodyAtBracelessBodyReportsStatementEnd() throws Exception {
		final var ifAst = findKeyword("if (x > 0)\n\t\t--x;", TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(ifAst.getLineNo(), found.endLine());
		assertEquals("\t\t--x;".indexOf(';') + 1, found.endColumn());
		assertEquals(ifAst.getLineNo(), found.lastLine());
	}

	@Test
	public void testBodyAtBracelessBodyReportsStatementStart() throws Exception {
		final var ifAst = findKeyword("if (x > 0)\n\t\t--x;", TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertFalse(found.block());
		assertEquals(ifAst.getLineNo(), found.line());
		assertEquals(2, found.column());
		assertEquals(ifAst.getLineNo() - 1, found.headerLine());
	}

	@Test
	public void testBodyAtColumnMissesReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0) { --x; }", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo() + 99));
	}

	@Test
	public void testBodyAtDoKeywordReturnsNull() throws Exception {
		final var doAst = findKeyword("do --x; while (x > 0);", TokenTypes.LITERAL_DO);
		assertNull(ControlFlowBracesCheck.bodyAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo()));
	}

	@Test
	public void testBodyAtElseReportsKeywordLineAsHeader() throws Exception {
		final var elseAst = findKeyword("if (x > 0)\n\t\t--x;\n\telse { ++x; }", TokenTypes.LITERAL_ELSE);
		final var found = ControlFlowBracesCheck.bodyAt(elseAst, elseAst.getLineNo() - 1, elseAst.getColumnNo());
		assertNotNull(found);
		assertTrue(found.block());
		assertEquals(elseAst.getLineNo() - 1, found.line());
		assertEquals(elseAst.getLineNo() - 1, found.headerLine());
	}

	@Test
	public void testBodyAtEmptyStatementReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0) ;", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo()));
	}

	@Test
	public void testBodyAtMultiLineHeaderReportsCloseParenLine() throws Exception {
		final var ifAst = findKeyword("if (x > 0\n\t\t\t\t&& y > 0) { --x; }", TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(ifAst.getLineNo(), found.headerLine());
	}

	@Test
	public void testBodyAtNestedControlBodyReportsNoEnd() throws Exception {
		final var ifAst = findKeyword("if (x > 0)\n\t\tif (y > 0)\n\t\t\t--x;", TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.bodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(-1, found.endLine());
		assertEquals(-1, found.endColumn());
		assertEquals(ifAst.getLineNo() + 1, found.lastLine());
	}

	@ParameterizedTest
	@ValueSource(strings = {"do x.a().b(); while (x > 0);", "do new Object(); while (x > 0);"})
	void testIsSimpleFalse(String doWhileCode) throws Exception {
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(findDoBody(doWhileCode)));
	}

	@Test
	public void testIsSimpleNonExprReturnsFalse() throws Exception {
		final var source = "class T { void f(int x) { do { --x; } while (x > 0); } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(doAst.getFirstChild()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"do x = 0; while (x > 0);", "do x &= 1; while (x > 0);",
			"do x |= 1; while (x > 0);", "do x >>>= 1; while (x > 0);",
			"do x ^= 1; while (x > 0);", "do x /= 2; while (x > 0);",
			"do x -= 1; while (x > 0);", "do x %= 3; while (x > 0);",
			"do x += 5; while (x > 0);", "do x <<= 1; while (x > 0);",
			"do x >>= 1; while (x > 0);", "do x *= 2; while (x > 0);",
			"do --x; while (x > 0);", "do ++x; while (x > 0);",
			"do f(x, y); while (x > 0);", "do System.out.println(x); while (x > 0);",
			"do x--; while (x > 0);", "do x++; while (x > 0);"})
	void testIsSimpleTrue(String doWhileCode) throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody(doWhileCode)));
	}

	@Test
	public void testOneLinerBodyAtBodyOnNextLineReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0)\n\t\t--x;", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo()));
	}

	@Test
	public void testOneLinerBodyAtBracedBodyReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0) { --x; }", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo()));
	}

	@Test
	public void testOneLinerBodyAtColumnMissesReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0) --x;", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo() + 99));
	}

	@Test
	public void testOneLinerBodyAtDoKeywordReturnsNull() throws Exception {
		final var doAst = findKeyword("do --x; while (x > 0);", TokenTypes.LITERAL_DO);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo()));
	}

	@Test
	public void testOneLinerBodyAtElseIfChainReturnsNull() throws Exception {
		final var elseAst = findKeyword("if (x > 0) --x; else if (y > 0) --y;", TokenTypes.LITERAL_ELSE);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(elseAst, elseAst.getLineNo() - 1, elseAst.getColumnNo()));
	}

	@Test
	public void testOneLinerBodyAtElseOnNextLineReportsNoElseColumn() throws Exception {
		final var body = "if (x > 0) --x;\n\t\telse\n\t\t\t++x;";
		final var ifAst = findKeyword(body, TokenTypes.LITERAL_IF);
		final var source = wrap(body);
		final var found = ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(source.indexOf("--x"), found.column());
		assertEquals(ifAst.getLineNo() - 1, found.endLine());
		assertEquals(source.indexOf(';') + 1, found.endColumn());
		assertEquals(-1, found.elseColumn());
		assertEquals(ifAst.getLineNo() - 1, found.lastLine());
	}

	@Test
	public void testOneLinerBodyAtElseWithStatementBodyReportsBody() throws Exception {
		final var body = "if (x > 0) --x; else ++x;";
		final var elseAst = findKeyword(body, TokenTypes.LITERAL_ELSE);
		final var source = wrap(body);
		final var found = ControlFlowBracesCheck.oneLinerBodyAt(elseAst, elseAst.getLineNo() - 1, elseAst.getColumnNo());
		assertNotNull(found);
		assertEquals(source.indexOf("++x"), found.column());
		assertEquals(source.lastIndexOf(';') + 1, found.endColumn());
		assertEquals(-1, found.elseColumn());
		assertEquals(elseAst.getLineNo() - 1, found.lastLine());
	}

	@Test
	public void testOneLinerBodyAtEmptyStatementReturnsNull() throws Exception {
		final var ifAst = findKeyword("if (x > 0) ;", TokenTypes.LITERAL_IF);
		assertNull(ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo()));
	}

	@Test
	public void testOneLinerBodyAtNestedControlBodyReportsNoEndColumn() throws Exception {
		final var body = "if (x > 0) if (y > 0) --x;";
		final var ifAst = findKeyword(body, TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(wrap(body).indexOf("if (y"), found.column());
		assertEquals(-1, found.endLine());
		assertEquals(-1, found.endColumn());
		assertEquals(-1, found.elseColumn());
		assertEquals(ifAst.getLineNo() - 1, found.lastLine());
	}

	@Test
	public void testOneLinerBodyAtReportsBodyEndAndElsePositions() throws Exception {
		final var body = "if (x > 0) --x; else ++x;";
		final var ifAst = findKeyword(body, TokenTypes.LITERAL_IF);
		final var source = wrap(body);
		final var found = ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(source.indexOf("--x"), found.column());
		assertEquals(ifAst.getLineNo() - 1, found.endLine());
		assertEquals(source.indexOf(';') + 1, found.endColumn());
		assertEquals(source.indexOf("else"), found.elseColumn());
		assertEquals(ifAst.getLineNo() - 1, found.lastLine());
	}

	@Test
	public void testOneLinerBodyAtSemicolonOnNextLineExtendsLastLine() throws Exception {
		final var body = "if (x > 0) f(x)\n\t\t\t\t;";
		final var ifAst = findKeyword(body, TokenTypes.LITERAL_IF);
		final var found = ControlFlowBracesCheck.oneLinerBodyAt(ifAst, ifAst.getLineNo() - 1, ifAst.getColumnNo());
		assertNotNull(found);
		assertEquals(wrap(body).indexOf("f(x)"), found.column());
		assertEquals(ifAst.getLineNo(), found.endLine());
		assertEquals("\t\t\t\t;".indexOf(';') + 1, found.endColumn());
		assertEquals(-1, found.elseColumn());
		assertEquals(ifAst.getLineNo(), found.lastLine());
	}

	@Test
	public void testShapeAtAdvancesAcrossTopLevelSiblings() throws Exception {
		final var source = "class A { }\nclass B { void f(int x) { do --x; while (x > 0); } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var root = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(root, TokenTypes.LITERAL_DO);
		final var shape = ControlFlowBracesCheck.shapeAt(root, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(ControlFlowBracesCheck.TIER_2, shape.tier());
	}

	@Test
	public void testShapeAtLocatesBracedBody() throws Exception {
		final var doAst = findDoNode("do { x = a.b().c(); } while (x > 0);");
		final var shape = ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(ControlFlowBracesCheck.TIER_3, shape.tier());
	}

	@Test
	public void testShapeAtLocatesExprBody() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0);");
		final var shape = ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(ControlFlowBracesCheck.TIER_2, shape.tier());
	}

	/**
	 * The nested statement's own {@code while} comes first, so a terminator rule
	 * that took the nearest one would close the outer statement at it.
	 */
	@Test
	public void testShapeAtNestedDoWhileReportsOuterTerminator() throws Exception {
		final var body = "do do --x; while (y > 0); while (x > 0);";
		final var doAst = findDoNode(body);
		final var shape = ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(doAst.getLineNo() - 1, shape.whileLine());
		assertEquals(wrap(body).lastIndexOf("while"), shape.whileColumn());
		assertTrue(shape.whileOnBodyLine());
	}

	@Test
	public void testShapeAtReportsTerminatorCuddledOntoBody() throws Exception {
		final var body = "do\n\t\t\t--x; while (x > 0);";
		final var doAst = findDoNode(body);
		final var shape = ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(doAst.getLineNo(), shape.whileLine());
		assertEquals("\t\t\t--x; ".length(), shape.whileColumn());
		assertTrue(shape.whileOnBodyLine());
	}

	@Test
	public void testShapeAtReportsTerminatorOnOwnLine() throws Exception {
		final var body = "do\n\t\t\t--x;\n\t\twhile (x > 0);";
		final var doAst = findDoNode(body);
		final var shape = ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo());
		assertNotNull(shape);
		assertEquals(doAst.getLineNo() + 1, shape.whileLine());
		assertEquals(2, shape.whileColumn());
		assertFalse(shape.whileOnBodyLine());
	}

	@Test
	public void testShapeAtReturnsNullWhenColumnMisses() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0);");
		assertNull(ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() - 1, doAst.getColumnNo() + 99));
	}

	@Test
	public void testShapeAtReturnsNullWhenLineMisses() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0);");
		assertNull(ControlFlowBracesCheck.shapeAt(doAst, doAst.getLineNo() + 5, doAst.getColumnNo()));
	}

	@MethodSource("tierProvider")
	@ParameterizedTest
	void testTierClassification(String doWhileCode, int expectedTier) throws Exception {
		final var doAst = findDoNode(doWhileCode);
		assertEquals(expectedTier, ControlFlowBracesCheck.determineTier(doAst.getFirstChild()));
	}
}