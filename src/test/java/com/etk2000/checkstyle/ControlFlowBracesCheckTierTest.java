package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;

/**
 * Tests for {@link ControlFlowBracesCheck} tier determination and
 * expression classification using direct AST inspection.
 */
public class ControlFlowBracesCheckTierTest {
	@Nonnull
	private static DetailAST findDoBody(@Nonnull String doWhileCode) throws Exception {
		final var source = "class T { void f(int x, int y) { " + doWhileCode + " } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		if (doAst == null)
			throw new AssertionError("No LITERAL_DO found in: " + doWhileCode);
		return doAst.getFirstChild();
	}

	@Nonnull
	private static DetailAST findDoNode(@Nonnull String doWhileCode) throws Exception {
		final var source = "class T { void f(int x, int y) { " + doWhileCode + " } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
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

	@Test
	public void testIsSimpleAssign() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x = 0; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignBand() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x &= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignBor() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x |= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignBsr() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x >>>= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignBxor() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x ^= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignDiv() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x /= 2; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignMinus() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x -= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignMod() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x %= 3; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignPlus() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x += 5; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignSl() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x <<= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignSr() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x >>= 1; while (x > 0);")));
	}

	@Test
	public void testIsSimpleCompoundAssignStar() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x *= 2; while (x > 0);")));
	}

	@Test
	public void testIsSimpleDec() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do --x; while (x > 0);")));
	}

	@Test
	public void testIsSimpleInc() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do ++x; while (x > 0);")));
	}

	@Test
	public void testIsSimpleMethodCallBare() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do f(x, y); while (x > 0);")));
	}

	@Test
	public void testIsSimpleMethodCallChainedReturnsFalse() throws Exception {
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x.a().b(); while (x > 0);")));
	}

	@Test
	public void testIsSimpleMethodCallDotted() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do System.out.println(x); while (x > 0);")));
	}

	@Test
	public void testIsSimpleNewReturnsFalse() throws Exception {
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do new Object(); while (x > 0);")));
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

	@Test
	public void testIsSimplePostDec() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x--; while (x > 0);")));
	}

	@Test
	public void testIsSimplePostInc() throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody("do x++; while (x > 0);")));
	}

	@Test
	public void testTierComplexRhsAssignIsTier3() throws Exception {
		final var doAst = findDoNode("do x = x + y; while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_3, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierComplexRhsCompoundIsTier3() throws Exception {
		final var doAst = findDoNode("do x += 5 * y; while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_3, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierCompoundWhileAndIsTier2() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0 && x < 100);");
		assertEquals(ControlFlowBracesCheck.TIER_2, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierCompoundWhileOrIsTier2() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0 || x < 100);");
		assertEquals(ControlFlowBracesCheck.TIER_2, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierDottedBodyIsTier2() throws Exception {
		final var doAst = findDoNode("do System.out.println(x); while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_2, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierDottedRhsIsTier2() throws Exception {
		final var doAst = findDoNode("do x = System.out.hashCode(); while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_2, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierNewObjectIsTier3() throws Exception {
		final var doAst = findDoNode("do new Object(); while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_3, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierPostDecIsTier1() throws Exception {
		final var doAst = findDoNode("do x--; while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_1, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierPostIncIsTier1() throws Exception {
		final var doAst = findDoNode("do x++; while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_1, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}

	@Test
	public void testTierSimpleDecrementIsTier1() throws Exception {
		final var doAst = findDoNode("do --x; while (x > 0);");
		assertEquals(ControlFlowBracesCheck.TIER_1, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}
}