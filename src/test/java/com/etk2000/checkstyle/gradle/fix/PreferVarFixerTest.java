package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferVarFixerTest {
	private static final String TOPIC = "prefervar";

	private final CheckstyleFixer fixer = new PreferVarFixer();

	@Test
	public void testArrayInitBeforeReportedDeclaration() throws Exception {
		assertSimpleFix(fixer, TOPIC, "array_init_before_reported_declaration");
	}

	@Test
	public void testAtPrefixedOpenParenDoesNotJoin() throws Exception {
		// can't migrate: unparseable Java fragment (unclosed `@Foo(` annotation on prev line)
		assertSimpleFix(fixer, TOPIC, "at_prefixed_open_paren_does_not_join");
	}

	@Test
	public void testChainReceiverDiamondWithoutAnAst() throws Exception {
		assertSkipResult(fixer, TOPIC, "chain_receiver_diamond_without_an_ast", "declared type arguments belong to a diamond this fixer cannot reach");
	}

	@Test
	public void testColumnAtExactEnd() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_at_exact_end", "reported column does not resolve on this line");
	}

	@Test
	public void testColumnAtNonIdentifier() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_at_non_identifier", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testColumnMidIdentifier() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_mid_identifier", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testDeclarationTypeInsideStringLiteral() throws Exception {
		assertSkipResult(fixer, TOPIC, "declaration_type_inside_string_literal", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testDiamondNoNewKeyword() throws Exception {
		assertSkipResult(fixer, TOPIC, "diamond_no_new_keyword", "declaration already uses 'var'");
	}

	@Test
	public void testDiamondUnbalancedAngleBrackets() throws Exception {
		assertSkipResult(fixer, TOPIC, "diamond_unbalanced_angle_brackets", "declaration already uses 'var'");
	}

	@Test
	public void testDottedExpressionIsNotADeclaration() throws Exception {
		assertSkipResult(fixer, TOPIC, "dotted_expression_is_not_a_declaration", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testDoubleEqualsAfterName() throws Exception {
		assertSkipResult(fixer, TOPIC, "double_equals_after_name", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testEqualsAtLineEnd() throws Exception {
		// can't migrate: unparseable Java fragment (incomplete `int[] a =`)
		assertSimpleFix(fixer, TOPIC, "equals_at_line_end");
	}

	@Test
	public void testEqualsOnlyDoubleEquals() throws Exception {
		assertSkipResult(fixer, TOPIC, "equals_only_double_equals", "declaration already uses 'var'");
	}

	@Test
	public void testExplicitArrayInitNoBrace() throws Exception {
		// can't migrate: unparseable Java fragment (incomplete `int[] a = new int[]`)
		assertSimpleFix(fixer, TOPIC, "explicit_array_init_no_brace");
	}

	@Test
	public void testExplicitArrayInitNoBraceVar() throws Exception {
		assertSkipResult(fixer, TOPIC, "explicit_array_init_no_brace_var", "declaration already uses 'var'");
	}

	@Test
	public void testExplicitArrayInitUnbalancedAngleBrackets() throws Exception {
		assertSkipResult(fixer, TOPIC, "explicit_array_init_unbalanced_angle_brackets", "declaration already uses 'var'");
	}

	@Test
	public void testExplicitArrayInitUnbalancedParen() throws Exception {
		assertSkipResult(fixer, TOPIC, "explicit_array_init_unbalanced_paren", "declaration already uses 'var'");
	}

	@Test
	public void testMultiVarTextFallback() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_var_text_fallback", "reported position is not a declaration this fixer recognises");
	}

	@Test
	public void testUnterminatedGenericDeclaration() throws Exception {
		assertSkipResult(
				fixer,
				TOPIC,
				"unterminated_generic_declaration",
				"declared type arguments belong to a diamond this fixer cannot reach"
		);
	}

	@Test
	public void testVarDeclarationAfterReportedDeclaration() throws Exception {
		assertSimpleFix(fixer, TOPIC, "var_declaration_after_reported_declaration");
	}
}