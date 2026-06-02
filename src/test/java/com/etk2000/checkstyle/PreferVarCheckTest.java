package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct AST tests for the positional predicates {@code PreferVarFixer} consults. They need a
 * buffer that parses, which no fragment in the corpus is, so their refusal and unresolved arms
 * are only reachable from here.
 */
public class PreferVarCheckTest {
	private static final String CLEAN_FIXTURE = "prefervar/cases.clean.java";

	@CheckReturnValue
	@Nullable
	private static DetailAST find(@Nonnull DetailAST node, @Nonnull Predicate<DetailAST> predicate) {
		for (var child = node; child != null; child = child.getNextSibling()) {
			if (predicate.test(child))
				return child;
			final var found = find(child.getFirstChild(), predicate);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static int[] leafPosition(@Nonnull DetailAST node) {
		var leaf = node;
		while (leaf.getFirstChild() != null)
			leaf = leaf.getFirstChild();
		return new int[]{leaf.getLineNo() - 1, leaf.getColumnNo()};
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST locate(@Nonnull DetailAST root, @Nonnull Predicate<DetailAST> predicate) {
		return requireNonNull(find(root, predicate), "no matching node in " + CLEAN_FIXTURE);
	}

	@CheckReturnValue
	private static boolean parentIs(@Nonnull DetailAST node, int tokenType) {
		return node.getParent() != null && node.getParent().getType() == tokenType;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var url = PreferVarCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + CLEAN_FIXTURE);
		requireNonNull(url, "Test input file not found: " + CLEAN_FIXTURE);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	@CheckReturnValue
	@Nonnull
	private static int[] typePosition(@Nonnull DetailAST varDef) {
		return leafPosition(requireNonNull(varDef.findFirstToken(TokenTypes.TYPE), "declaration has no TYPE"));
	}

	@CheckReturnValue
	private static boolean varDefNamed(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() != TokenTypes.VARIABLE_DEF)
			return false;
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && name.equals(ident.getText());
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtConditionalWithAllNewArms() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "everyArmTernary")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtConditionalWithNonNewArm() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "ternary")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtDeclarationWithoutInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "uninitialized")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtLocalNewInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtMethodCallInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "targetTyped")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtSwitchWithAllNewArms() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "everyArmSwitch")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, 0, 0));
	}

	@Test
	public void testExplicitArrayInitAtDeclarationWithoutInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "uninitialized")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtImplicitArrayInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "numbers")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtPlainDeclaration() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, 0, 0));
	}

	@Test
	public void testIsConvertibleDeclarationAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertEquals(Boolean.FALSE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtField() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isField =
				node -> varDefNamed(node, "x") && parentIs(node, TokenTypes.OBJBLOCK);
		final var at = typePosition(locate(root, isField));
		assertEquals(Boolean.FALSE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtForEachVariable() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isForEachVar = node -> node.getType() == TokenTypes.VARIABLE_DEF
				&& parentIs(node, TokenTypes.FOR_EACH_CLAUSE);
		final var at = typePosition(locate(root, isForEachVar));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtForInit() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isForInitVar = node -> node.getType() == TokenTypes.VARIABLE_DEF
				&& parentIs(node, TokenTypes.FOR_INIT);
		final var at = typePosition(locate(root, isForInitVar));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtLocal() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertNull(PreferVarCheck.isConvertibleDeclarationAt(root, 0, 0));
	}

	@Test
	public void testIsMultiVarDeclarationAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertNull(PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtMultiVar() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "y")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtSingleVar() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertEquals(Boolean.FALSE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.FALSE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertNull(PreferVarCheck.isMultiVarDeclarationAt(root, 0, 0));
	}
}