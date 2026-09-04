package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: annotated_instanceof_type_skipped ===
// skip-reason: cannot rewrite the assertion: the instanceof type is annotated
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AnnotatedInstanceofTypeSkippedSlice {
	@interface Tagged {
	}

	void m(Object o) {
		assertTrue(o instanceof @Tagged String);
	}
}
// === end ===

// === case: assert_false_equal ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseEqualSlice {
	void m() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: assert_false_ge ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseGeSlice {
	void m() {
		assertFalse(1 >= 0);
	}
}
// === end ===

// === case: assert_false_gt ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseGtSlice {
	void m() {
		assertFalse(1 > 0);
	}
}
// === end ===

// === case: assert_false_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalseInstanceOfSlice {
	void m(Object o) {
		assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: assert_false_instance_of_fully_qualified ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalseInstanceOfFullyQualifiedSlice {
	void m(Object o) {
		assertNotInstanceOf(java.lang.Integer.class, o);
	}
}
// === end ===

// === case: assert_false_instance_of_negated ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
class AssertFalseInstanceOfNegatedSlice {
	void m(Object o) {
		assertInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: assert_false_instance_of_negated_twice ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalseInstanceOfNegatedTwiceSlice {
	void m(Object o) {
		assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: assert_false_instance_of_nested ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalseInstanceOfNestedSlice {
	void m(Object o) {
		assertNotInstanceOf(java.util.Map.Entry.class, o);
	}
}
// === end ===

// === case: assert_false_le ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseLeSlice {
	void m() {
		assertFalse(0 <= 1);
	}
}
// === end ===

// === case: assert_false_lt ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseLtSlice {
	void m() {
		assertFalse(0 < 1);
	}
}
// === end ===

// === case: assert_false_negated_identifier ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertFalseNegatedIdentifierSlice {
	void m(boolean flag) {
		assertTrue(flag);
	}
}
// === end ===

// === case: assert_false_negated_method_call_junit4 ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class AssertFalseNegatedMethodCallJunit4Slice {
	void m() {
		assertTrue("hello".isEmpty());
	}
}
// === end ===

// === case: assert_false_negated_pattern_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertFalseNegatedPatternInstanceOfSlice {
	void m(Object o) {
		assertTrue(o instanceof String s);
	}
}
// === end ===

// === case: assert_false_negated_qualified_assert_instance_of ===
class AssertFalseNegatedQualifiedAssertInstanceOfSlice {
	void m(Object o) {
		org.junit.Assert.assertTrue(o instanceof String);
	}
}
// === end ===

// === case: assert_false_not_equal ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class AssertFalseNotEqualSlice {
	void m() {
		assertFalse(1 != 1);
	}
}
// === end ===

// === case: assert_false_pattern_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalsePatternInstanceOfSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_false_pattern_instance_of_negated_twice ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalsePatternInstanceOfNegatedTwiceSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_true_complex_lhs_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueComplexLhsInstanceOfSlice {
	void m(Throwable ex) {
		assertInstanceOf(RuntimeException.class, ex.getCause());
	}
}
// === end ===

// === case: assert_true_equal ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueEqualSlice {
	void m() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_true_ge ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueGeSlice {
	void m() {
		assertTrue(2 >= 1);
	}
}
// === end ===

// === case: assert_true_gt ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueGtSlice {
	void m() {
		assertTrue(1 > 0);
	}
}
// === end ===

// === case: assert_true_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueInstanceOfSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_true_instance_of_fully_qualified ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueInstanceOfFullyQualifiedSlice {
	void m(Object o) {
		assertInstanceOf(java.lang.String.class, o);
	}
}
// === end ===

// === case: assert_true_instance_of_negated ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueInstanceOfNegatedSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_true_instance_of_negated_twice ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueInstanceOfNegatedTwiceSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_true_instance_of_nested ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueInstanceOfNestedSlice {
	void m(Object o) {
		assertInstanceOf(java.util.Map.Entry.class, o);
	}
}
// === end ===

// === case: assert_true_le ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueLeSlice {
	void m() {
		assertTrue(0 <= 1);
	}
}
// === end ===

// === case: assert_true_lt ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueLtSlice {
	void m() {
		assertTrue(0 < 1);
	}
}
// === end ===

// === case: assert_true_negated_assert_wildcard_junit4 ===
// imports: static org.junit.Assert.*
class AssertTrueNegatedAssertWildcardJunit4Slice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_assertfalse_already_imported ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedAssertfalseAlreadyImportedSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_assertions_wildcard ===
// imports: static org.junit.jupiter.api.Assertions.*
class AssertTrueNegatedAssertionsWildcardSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_identifier ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedIdentifierSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_logical_and ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedLogicalAndSlice {
	void m(boolean a, boolean b) {
		assertFalse(a && b);
	}
}
// === end ===

// === case: assert_true_negated_message_first_junit4 ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class AssertTrueNegatedMessageFirstJunit4Slice {
	void m(boolean flag) {
		assertFalse("msg", flag);
	}
}
// === end ===

// === case: assert_true_negated_method_call ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedMethodCallSlice {
	void m(java.util.List<String> list) {
		assertFalse(list.isEmpty());
	}
}
// === end ===

// === case: assert_true_negated_method_call_junit4 ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class AssertTrueNegatedMethodCallJunit4Slice {
	void m(Object o) {
		assertFalse(o.toString().isEmpty());
	}
}
// === end ===

// === case: assert_true_negated_outer_parens ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedOuterParensSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_pattern_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedPatternInstanceOfSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assert_true_negated_qualified_assert_instance_of ===
class AssertTrueNegatedQualifiedAssertInstanceOfSlice {
	void m(Object o) {
		org.junit.Assert.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: assert_true_negated_qualified_junit4 ===
class AssertTrueNegatedQualifiedJunit4Slice {
	void m(boolean flag) {
		org.junit.Assert.assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_qualified_junit5 ===
class AssertTrueNegatedQualifiedJunit5Slice {
	void m(boolean flag) {
		org.junit.jupiter.api.Assertions.assertFalse(flag);
	}
}
// === end ===

// === case: assert_true_negated_with_message_last ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNegatedWithMessageLastSlice {
	void m(boolean flag) {
		assertFalse(flag, "msg");
	}
}
// === end ===

// === case: assert_true_not_equal ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertTrueNotEqualSlice {
	void m() {
		assertTrue(1 != 2);
	}
}
// === end ===

// === case: assertj_static_import_not_mistaken_for_junit ===
// imports: static org.assertj.core.api.Assertions.assertThat
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertjStaticImportNotMistakenForJunitSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: assertj_static_import_not_mistaken_for_junit_negation ===
// imports: static org.assertj.core.api.Assertions.assertThat
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class AssertjStaticImportNotMistakenForJunitNegationSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: block_comment_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class BlockCommentInArgsFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, /* note */ o);
	}
}
// === end ===

// === case: block_comment_unbalanced_bracket_before_instanceof_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class BlockCommentUnbalancedBracketBeforeInstanceofFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o /* [ */);
	}
}
// === end ===

// === case: block_comment_with_comma_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class BlockCommentWithCommaInArgsFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, /* a, b */ o);
	}
}
// === end ===

// === case: char_with_slash_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class CharWithSlashInArgsFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, '/');
	}
}
// === end ===

// === case: comparison_with_negated_message_skipped ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ComparisonWithNegatedMessageSkippedSlice {
	void m(int count, boolean quiet) {
		// the regression case: the old text fixer took the first argument whose stripped text started
		// with `!`, which here is the MESSAGE, and flipped assertTrue to assertFalse around it
		assertTrue(count > 0, !quiet ? "count was " + count : "");
	}
}
// === end ===

// === case: complex_lhs_preserved ===
// imports: java.io.IOException
// imports: static org.junit.jupiter.api.Assertions.*
class ComplexLhsPreservedSlice {
	void m(Throwable ex) {
		assertInstanceOf(IOException.class, ex.getCause());
	}
}
// === end ===

// === case: dollar_in_import_fqn_parsed ===
// imports: com.foo.Outer$Inner
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class DollarInImportFqnParsedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: explicit_type_arg_in_args_fixed ===
// imports: java.util.Map
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ExplicitTypeArgInArgsFixedSlice {
	void m() {
		assertInstanceOf(Map.class, Map.<String, Integer>of());
	}
}
// === end ===

// === case: find_method_call_open_skips_custom_then_matches_real ===
// imports: static org.junit.jupiter.api.Assertions.*
class FindMethodCallOpenSkipsCustomThenMatchesRealSlice {
	void customAssertTrue(Object x) {
	}

	void m(Object o, Object x) {
		customAssertTrue(x);
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: fully_qualified_type_preserved ===
// imports: static org.junit.jupiter.api.Assertions.*
class FullyQualifiedTypePreservedSlice {
	void m(Throwable ex) {
		assertInstanceOf(java.io.IOException.class, ex.getCause());
	}
}
// === end ===

// === case: generic_type_skipped ===
// skip-reason: cannot rewrite the assertion: the instanceof type is generic
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class GenericTypeSkippedSlice {
	void m(Object o) {
		assertTrue(o instanceof java.util.List<?>);
	}
}
// === end ===

// === case: import_line_commented_out_junit4_import_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ImportLineCommentedOutJunit4ImportIgnoredSlice {
	// import static org.junit.Assert.assertFalse;
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_inside_block_comment_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
/* temporarily disabled:
import static org.junit.Assert.assertFalse;
*/
class ImportLineInsideBlockCommentIgnoredSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_inside_block_comment_not_used_for_class_inference ===
/* replaced:
import static com.example.Assertions.assertTrue;
*/
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ImportLineInsideBlockCommentNotUsedForClassInferenceSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: import_line_inside_text_block_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ImportLineInsideTextBlockIgnoredSlice {
	void m(boolean flag) {
		assertFalse(flag, """
			import static org.junit.Assert.assertFalse;
			""");
	}
}
// === end ===

// === case: import_member_not_a_known_assertion_skips ===
// skip-reason: cannot rewrite the assertion: no static import says which class the replacement comes from
// imports: static org.junit.jupiter.api.Assertions.assertThrowsExactly
class ImportMemberNotAKnownAssertionSkipsSlice {
	void m(Object o) {
		assertTrue(o instanceof String);
	}
}
// === end ===

// === case: import_member_not_a_known_assertion_skips_negation ===
// skip-reason: cannot rewrite the assertion: no static import says which class the replacement comes from
// imports: static org.junit.jupiter.api.Assertions.assertThrowsExactly
class ImportMemberNotAKnownAssertionSkipsNegationSlice {
	void m(boolean flag) {
		assertTrue(!flag);
	}
}
// === end ===

// === case: instanceof_comment_abutting_keyword_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class InstanceofCommentAbuttingKeywordFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o/* c */);
	}
}
// === end ===

// === case: instanceof_comment_abutting_type_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class InstanceofCommentAbuttingTypeFixedSlice {
	void m(Object o) {
		assertInstanceOf(/* c */String.class, o);
	}
}
// === end ===

// === case: instanceof_comment_abutting_type_no_spaces_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class InstanceofCommentAbuttingTypeNoSpacesFixedSlice {
	void m(Object o) {
		assertInstanceOf(/*c*/String.class, o);
	}
}
// === end ===

// === case: instanceof_inside_parens_in_other_arg_ignored ===
// imports: static org.junit.jupiter.api.Assertions.*
class InstanceofInsideParensInOtherArgIgnoredSlice {
	static class Y {
	}

	void m(Object x) {
		assertInstanceOf(Y.class, x, "got: " + (x instanceof Y));
	}
}
// === end ===

// === case: instanceof_text_block_abutting_keyword_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class InstanceofTextBlockAbuttingKeywordFixedSlice {
	void m() {
		assertInstanceOf(String.class, """some text""");
	}
}
// === end ===

// === case: junit4_and_5_mixed_imports_rewrite_qualified ===
// imports: org.junit.jupiter.api.Assertions
// imports: static org.junit.Assert.assertTrue
class Junit4And5MixedImportsRewriteQualifiedSlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: junit4_message_first_shape ===
// imports: static org.junit.jupiter.api.Assertions.*
class Junit4MessageFirstShapeSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "should be a string");
	}
}
// === end ===

// === case: junit4_non_static_and_junit5_static_mixed_rewrites_unqualified ===
// imports: org.junit.Assert
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class Junit4NonStaticAndJunit5StaticMixedRewritesUnqualifiedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: junit4_package_wildcard_with_junit5_rewrites_unqualified ===
// imports: org.junit.*
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class Junit4PackageWildcardWithJunit5RewritesUnqualifiedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: junit5_message_last_shape ===
// imports: static org.junit.jupiter.api.Assertions.*
class Junit5MessageLastShapeSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "should be a string");
	}
}
// === end ===

// === case: junit5_wildcard_followed_by_static_assert_import_skips_add ===
// imports: static org.junit.jupiter.api.Assertions.*
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class Junit5WildcardFollowedByStaticAssertImportSkipsAddSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: lambda_body_assertion_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LambdaBodyAssertionFixedSlice {
	void m(Object o) {
		runLater(() -> assertInstanceOf(String.class, o));
	}

	private void runLater(Runnable task) {
	}
}
// === end ===

// === case: lhs_with_char_comma_preserved ===
// imports: static org.junit.jupiter.api.Assertions.*
class LhsWithCharCommaPreservedSlice {
	static class Foo {
	}

	void m() {
		assertInstanceOf(Foo.class, processChar(','));
	}

	Object processChar(char c) {
		return null;
	}
}
// === end ===

// === case: lhs_with_nested_call_preserved ===
// imports: static org.junit.jupiter.api.Assertions.*
class LhsWithNestedCallPreservedSlice {
	static class Foo {
	}

	void m(Object a, Object b, Object c) {
		assertInstanceOf(Foo.class, transform(a, b, c));
	}

	Object transform(Object a, Object b, Object c) {
		return null;
	}
}
// === end ===

// === case: message_with_comma_preserved ===
// imports: static org.junit.jupiter.api.Assertions.*
class MessageWithCommaPreservedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "foo, bar");
	}
}
// === end ===

// === case: multi_line_call_block_comment_containing_name_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallBlockCommentContainingNameIgnoredSlice {
	void m(Object o) {
		/* assertTrue(stale) */
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_junit5_message_last_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallJunit5MessageLastFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "msg");
	}
}
// === end ===

// === case: multi_line_call_negated_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallNegatedFixedSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_open_paren_on_own_line_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallOpenParenOnOwnLineFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_semi_on_own_line_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallSemiOnOwnLineFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_space_before_open_paren_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallSpaceBeforeOpenParenFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_string_literal_containing_name_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallStringLiteralContainingNameIgnoredSlice {
	void m(Object o) {
		final var s = "earlier: assertTrue";
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_call_with_block_comment_before_open_paren_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallWithBlockCommentBeforeOpenParenFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o); // note
	}
}
// === end ===

// === case: multi_line_call_with_comment_between_close_and_semi_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallWithCommentBetweenCloseAndSemiFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o); // note
	}
}
// === end ===

// === case: multi_line_call_with_line_comment_before_open_paren_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallWithLineCommentBeforeOpenParenFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o); // note
	}
}
// === end ===

// === case: multi_line_call_with_line_comment_between_close_and_semi_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineCallWithLineCommentBetweenCloseAndSemiFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o); // note
	}
}
// === end ===

// === case: multi_line_negated_instance_of_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineNegatedInstanceOfFixedSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_outer_paren_arg_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineOuterParenArgFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_qualified_assert_negated_instance_of ===
class MultiLineQualifiedAssertNegatedInstanceOfSlice {
	void m(Object o) {
		org.junit.Assert
				.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: multi_line_qualified_assertions_instance_of ===
class MultiLineQualifiedAssertionsInstanceOfSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions
				.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: multi_line_qualified_chain_falls_back_to_negation ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineQualifiedChainFallsBackToNegationSlice {
	void m(Object o) {
		Assert
				.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: multi_line_qualified_chain_falls_back_to_negation_assert_false ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class MultiLineQualifiedChainFallsBackToNegationAssertFalseSlice {
	void m(Object o) {
		Assert
				.assertTrue(o instanceof String);
	}
}
// === end ===

// === case: multi_target_position_skipped ===
// skip-reason: cannot rewrite the assertion: more than one assertion violation at this position
class MultiTargetPositionSkippedSlice {
	private static class Assertions {
		static void assertTrue(boolean condition, boolean other) {
		}
	}

	void m(boolean flag, int a, int b) {
		Assertions.assertTrue(!flag, a == b);
	}
}
// === end ===

// === case: nan_comparison_skipped ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class NanComparisonSkippedSlice {
	void m(double x) {
		// a tripwire: assertEquals(Double.NaN, x) PASSES where this assertion fails, so rewriting
		// a comparison without the operand types inverts the test
		assertTrue(Double.NaN == x);
	}
}
// === end ===

// === case: negated_operand_comparison_skipped ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class NegatedOperandComparisonSkippedSlice {
	void m(boolean a, boolean b) {
		// reads like a negation but parses as `(!a) == b`, so the comparison rule owns it
		assertTrue(!a == b);
	}
}
// === end ===

// === case: negation_instance_of_junit4_fallback ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class NegationInstanceOfJunit4FallbackSlice {
	void m(Object o) {
		assertFalse(o instanceof String);
	}
}
// === end ===

// === case: negative_zero_comparison_skipped ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class NegativeZeroComparisonSkippedSlice {
	void m() {
		// the mirror tripwire: assertEquals(0.0, -0.0) FAILS where this assertion passes
		assertTrue(0.0 == -0.0);
	}
}
// === end ===

// === case: nested_type_name_preserved ===
// imports: static org.junit.jupiter.api.Assertions.*
class NestedTypeNamePreservedSlice {
	void m(Object o) {
		assertInstanceOf(java.util.Map.Entry.class, o);
	}
}
// === end ===

// === case: no_static_import_leaves_imports_empty ===
// imports: static org.junit.jupiter.api.Assertions.*
class NoStaticImportLeavesImportsEmptySlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: non_assert_static_import_leaves_imports_empty ===
// imports: static java.util.Objects.requireNonNull
class NonAssertStaticImportLeavesImportsEmptySlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: parens_around_negated_instance_of_fixed ===
// imports: static org.junit.jupiter.api.Assertions.*
class ParensAroundNegatedInstanceOfFixedSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: parenthesized_comparison_fires ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ParenthesizedComparisonFiresSlice {
	void m() {
		assertTrue((1 > 0));
	}
}
// === end ===

// === case: parenthesized_instance_of_arg_double_paren_fixed ===
// imports: static org.junit.jupiter.api.Assertions.*
class ParenthesizedInstanceOfArgDoubleParenFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: parenthesized_instance_of_arg_fixed ===
// imports: static org.junit.jupiter.api.Assertions.*
class ParenthesizedInstanceOfArgFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: parenthesized_instance_of_arg_with_junit4_message_first ===
// imports: static org.junit.jupiter.api.Assertions.*
class ParenthesizedInstanceOfArgWithJunit4MessageFirstSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "msg");
	}
}
// === end ===

// === case: parenthesized_instance_of_arg_with_junit5_message_last ===
// imports: static org.junit.jupiter.api.Assertions.*
class ParenthesizedInstanceOfArgWithJunit5MessageLastSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "msg");
	}
}
// === end ===

// === case: parenthesized_instance_of_fires ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
class ParenthesizedInstanceOfFiresSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: qualified_assert_false_call ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class QualifiedAssertFalseCallSlice {
	void m() {
		org.junit.Assert.assertFalse(1 > 0);
	}
}
// === end ===

// === case: qualified_assert_negated_instance_of_falls_back_to_negation ===
class QualifiedAssertNegatedInstanceOfFallsBackToNegationSlice {
	void m(Object o) {
		org.junit.Assert.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: qualified_assert_true_call ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class QualifiedAssertTrueCallSlice {
	void m() {
		org.junit.Assert.assertTrue(1 > 0);
	}
}
// === end ===

// === case: qualified_assert_with_space_between_dot_and_method ===
class QualifiedAssertWithSpaceBetweenDotAndMethodSlice {
	void m(Object o) {
		org.junit.Assert.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: qualified_junit5_assert_false ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class QualifiedJunit5AssertFalseSlice {
	void m() {
		org.junit.jupiter.api.Assertions.assertFalse(1 > 0);
	}
}
// === end ===

// === case: qualified_junit5_assert_false_ne ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class QualifiedJunit5AssertFalseNeSlice {
	void m() {
		org.junit.jupiter.api.Assertions.assertFalse(1 != 2);
	}
}
// === end ===

// === case: qualified_junit5_assert_false_negated_instance_of ===
class QualifiedJunit5AssertFalseNegatedInstanceOfSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: qualified_junit5_assert_true ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class QualifiedJunit5AssertTrueSlice {
	void m() {
		org.junit.jupiter.api.Assertions.assertTrue(1 > 0);
	}
}
// === end ===

// === case: qualified_junit5_assert_true_eq ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class QualifiedJunit5AssertTrueEqSlice {
	void m() {
		org.junit.jupiter.api.Assertions.assertTrue(1 == 1);
	}
}
// === end ===

// === case: qualified_junit5_assert_true_instance_of ===
class QualifiedJunit5AssertTrueInstanceOfSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: qualified_junit5_assert_true_negated_instance_of ===
class QualifiedJunit5AssertTrueNegatedInstanceOfSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: qualified_junit5_call_preserves_prefix ===
// imports: static org.junit.jupiter.api.Assertions.*
class QualifiedJunit5CallPreservesPrefixSlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: record_pattern_skipped ===
// skip-reason: cannot rewrite the assertion: record pattern with bindings
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class RecordPatternSkippedSlice {
	record Point(int x, int y) {}

	void m(Object o) {
		assertTrue(o instanceof Point(int x, int y));
	}
}
// === end ===

// === case: same_line_earlier_ident_with_matching_name_ignored ===
// imports: static org.junit.jupiter.api.Assertions.*
class SameLineEarlierIdentWithMatchingNameIgnoredSlice {
	void m(Object o) {
		final var msg = assertTrue + " x";
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: split_leading_stmt_before_dot_space_qualifier ===
class SplitLeadingStmtBeforeDotSpaceQualifierSlice {
	void m(Object o) {
		System.out.println(o);
		org.junit.Assert.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: split_leading_stmt_before_multi_segment_qualifier ===
class SplitLeadingStmtBeforeMultiSegmentQualifierSlice {
	void m(Object o) {
		System.out.println(o);
		org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: string_literal_containing_assert_true_ignored ===
// imports: static org.junit.jupiter.api.Assertions.*
class StringLiteralContainingAssertTrueIgnoredSlice {
	void m(Object o) {
		final var s = "call assertTrue(here)";
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: string_with_dot_lt_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class StringWithDotLtInArgsFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "x.<y>z");
	}
}
// === end ===

// === case: string_with_slash_slash_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class StringWithSlashSlashInArgsFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "url://x");
	}
}
// === end ===

// === case: structural_line_comment_in_args_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class StructuralLineCommentInArgsFixedSlice {
	void m(boolean flag) {
		assertFalse(flag); // dangerous
	}
}
// === end ===

// === case: text_block_comma_in_message_not_split ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TextBlockCommaInMessageNotSplitSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, """
			a, b""");
	}
}
// === end ===

// === case: text_block_instanceof_in_message_ignored ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TextBlockInstanceofInMessageIgnoredSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, """
			x instanceof Y""");
	}
}
// === end ===

// === case: text_block_message_multi_line_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TextBlockMessageMultiLineFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, """
			should be a string""");
	}
}
// === end ===

// === case: text_block_single_line_arg_fixed ===
// imports: static org.junit.jupiter.api.Assertions.*
class TextBlockSingleLineArgFixedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, """some text""");
	}
}
// === end ===

// === case: three_args_skipped ===
// skip-reason: cannot rewrite the assertion: it has more arguments than a condition and a message
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ThreeArgsSkippedSlice {
	void m(Object o) {
		assertTrue(o instanceof String, "msg", "extra");
	}
}
// === end ===

// === case: trailing_stmt_after_assert_split ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TrailingStmtAfterAssertSplitSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
		System.out.println(o);
	}
}
// === end ===

// === case: trailing_text_block_after_assert_keeps_comment ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TrailingTextBlockAfterAssertKeepsCommentSlice {
	void m(boolean flag) {
		// the trailing statement moves to its own line, so nothing follows the relocated comment and
		// appending it is safe even though the suffix opens a text block
		assertFalse(flag); // why
		log("""
				text
				""");
	}

	private void log(String message) {
	}
}
// === end ===

// === case: triple_negation_flips_like_single ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TripleNegationFlipsLikeSingleSlice {
	void m(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: two_arg_junit5_assert_false ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class TwoArgJunit5AssertFalseSlice {
	void m() {
		assertFalse(1 >= 0, "should be negative");
	}
}
// === end ===

// === case: two_arg_junit5_assert_true ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TwoArgJunit5AssertTrueSlice {
	void m() {
		assertTrue(1 > 0, "should be positive");
	}
}
// === end ===

// === case: two_arg_junit5_assert_true_instance_of ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
class TwoArgJunit5AssertTrueInstanceOfSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o, "should be a string");
	}
}
// === end ===

// === case: unrelated_wildcard_does_not_block_junit5_scan ===
// imports: static java.util.Arrays.*
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class UnrelatedWildcardDoesNotBlockJunit5ScanSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: unrelated_wildcard_does_not_block_negation_scan ===
// imports: static java.util.Arrays.*
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class UnrelatedWildcardDoesNotBlockNegationScanSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: wildcard_static_import_skips_adding_import ===
// imports: static org.junit.jupiter.api.Assertions.*
class WildcardStaticImportSkipsAddingImportSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===