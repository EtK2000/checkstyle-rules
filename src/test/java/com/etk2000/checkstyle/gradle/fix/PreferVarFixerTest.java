package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.PreferVarCheck;
import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class PreferVarFixerTest {
	private static final String TOPIC = "prefervar";

	/**
	 * One production pass of the fixer over the slice {@code caseName}, which must yield its {@code .out}
	 * with {@code skips} refused. For a slice holding a fix and a skip together, which the slice
	 * harness cannot check, since its multi-violation sweep requires every error to be fixed.
	 */
	private static void assertOnePass(@Nonnull String caseName, @Nonnull List<String> skips) throws Exception {
		final var slice = TestResources.loadCaseSlice(TOPIC, caseName);
		final var input = FullPipelineRunner.stripViolationComments(String.join("\n", slice.inputLines()));
		final var events = BaseCheckTest.runCheckInline(PreferVarCheck.class, input);
		final var lines = new ArrayList<>(List.of(input.split("\n", -1)));
		final var result = CheckstyleFixAction.applyFixes(lines, new ArrayList<>(events), CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(slice.fixedLines(), lines);
		assertEquals(Map.of("PreferVarCheck", skips), result.skippedReasons());
	}

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
		assertSkipResult(fixer, TOPIC, "chain_receiver_diamond_without_an_ast", "cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach");
	}

	@Test
	public void testColumnAtExactEnd() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_at_exact_end", "cannot rewrite the declaration: the reported column does not resolve on this line");
	}

	@Test
	public void testColumnAtNonIdentifier() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_at_non_identifier", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testColumnMidIdentifier() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_mid_identifier", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testDeclarationTypeInsideStringLiteral() throws Exception {
		assertSkipResult(fixer, TOPIC, "declaration_type_inside_string_literal", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testDiamondNoNewKeyword() throws Exception {
		assertSkipResult(fixer, TOPIC, "diamond_no_new_keyword", "cannot use 'var': the declaration already uses it");
	}

	@Test
	public void testDiamondUnbalancedAngleBrackets() throws Exception {
		assertSkipResult(fixer, TOPIC, "diamond_unbalanced_angle_brackets", "cannot use 'var': the declaration already uses it");
	}

	@Test
	public void testDottedExpressionIsNotADeclaration() throws Exception {
		assertSkipResult(fixer, TOPIC, "dotted_expression_is_not_a_declaration", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testDoubleEqualsAfterName() throws Exception {
		assertSkipResult(fixer, TOPIC, "double_equals_after_name", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testEqualsAtLineEnd() throws Exception {
		// can't migrate: unparseable Java fragment (incomplete `int[] a =`)
		assertSimpleFix(fixer, TOPIC, "equals_at_line_end");
	}

	@Test
	public void testEqualsOnlyDoubleEquals() throws Exception {
		assertSkipResult(fixer, TOPIC, "equals_only_double_equals", "cannot use 'var': the declaration already uses it");
	}

	@Test
	public void testMultiVarTextFallback() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_var_text_fallback", "cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes");
	}

	@Test
	public void testTryResourceInsideAWrappedExplicitArrayInitializerConverts() throws Exception {
		assertOnePass(
				"try_resource_inside_a_wrapped_explicit_array_initializer_converts",
				List.of(SkipMessages.PREFER_VAR_SKIP_SPLIT_ARRAY_INITIALIZER)
		);
	}

	@Test
	public void testUnterminatedGenericDeclaration() throws Exception {
		assertSkipResult(
				fixer,
				TOPIC,
				"unterminated_generic_declaration",
				"cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach"
		);
	}

	@Test
	public void testVarDeclarationAfterReportedDeclaration() throws Exception {
		assertSimpleFix(fixer, TOPIC, "var_declaration_after_reported_declaration");
	}
}