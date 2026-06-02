package com.etk2000.checkstyle.inputs.constructorassignmentorder;

// === case: alphabetical_simple ===
class InputConstructorAssignViolation {
	int alpha, beta;

	InputConstructorAssignViolation(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}
// === end ===

// === case: alphabetical_three_fields ===
class InputConstructorAssignAlphabeticalThreeFieldsSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignAlphabeticalThreeFieldsSliceViolation(int a, int b, int c) {
		this.alpha = a;
		this.beta = b;
		this.gamma = c;
	}
}
// === end ===

// === case: assignment_shares_line_with_statement ===
// skip-reason: cannot reorder: multiple statements share a line
class InputConstructorAssignSharesLineWithStatementSliceViolation {
	int alpha, beta;

	InputConstructorAssignSharesLineWithStatementSliceViolation() {
		this.beta = 2;
		bar(); this.alpha = 1;
	}
}
// === end ===

// === case: bare_dependency_violation ===
class InputConstructorAssignBareDependencyViolation {
	int alpha, beta;

	InputConstructorAssignBareDependencyViolation() {
		this.alpha = 3;
		this.beta = alpha;
	}
}
// === end ===

// === case: bare_in_string_not_dependency ===
class InputConstructorAssignBareInStringNotDependency {
	String alpha, beta;

	InputConstructorAssignBareInStringNotDependency(String x) {
		this.alpha = "beta";
		this.beta = x;
	}
}
// === end ===

// === case: bare_lambda_dependency_violation ===
class InputConstructorAssignBareLambdaDependencyViolation {
	int value;
	Runnable alpha;

	InputConstructorAssignBareLambdaDependencyViolation(int seed) {
		this.value = seed;
		this.alpha = () -> System.out.println(value);
	}
}
// === end ===

// === case: bare_substring_not_matched ===
class InputConstructorAssignBareSubstringNotMatched {
	int alpha, beta;

	InputConstructorAssignBareSubstringNotMatched(int betaValue) {
		this.alpha = betaValue;
		this.beta = betaValue;
	}
}
// === end ===

// === case: bare_var_dependency_violation ===
class InputConstructorAssignBareVarDependencyViolation {
	int alpha, beta;

	InputConstructorAssignBareVarDependencyViolation(int x) {
		final var c = x;
		this.beta = c;
		this.alpha = c + beta;
	}
}
// === end ===

// === case: blank_between_assignments ===
class InputConstructorAssignBlankBetweenAssignments {
	int alpha, beta;

	InputConstructorAssignBlankBetweenAssignments(int a, int b) {
		this.alpha = a;
		this.beta = b;
	}
}
// === end ===

// === case: block_comment_between_assignments ===
// skip-reason: cannot reorder: an interleaved comment would be lost
class InputConstructorAssignBlockCommentBetweenAssignmentsSliceViolation {
	int alpha, beta;

	InputConstructorAssignBlockCommentBetweenAssignmentsSliceViolation(int a, int b) {
		this.beta = b;
		/* keep beta
		   before alpha */
		this.alpha = a;
	}
}
// === end ===

// === case: comment_between_assignments ===
// skip-reason: cannot reorder: an interleaved comment would be lost
class InputConstructorAssignCommentBetweenAssignmentsSliceViolation {
	int alpha, beta;

	InputConstructorAssignCommentBetweenAssignmentsSliceViolation(int a, int b) {
		this.beta = b;
		// keep beta initialized before alpha
		this.alpha = a;
	}
}
// === end ===

// === case: compact_ctor_reorder ===
record InputConstructorAssignCompactCtorReorderSliceViolation(int alpha, int beta) {
	InputConstructorAssignCompactCtorReorderSliceViolation {
		final var temp = alpha + beta;
		this.alpha = temp;
		this.beta = temp + 1;
	}
}
// === end ===

// === case: dependency_cycle ===
// skip-reason: cannot reorder: a field dependency cycle has no valid order
class InputConstructorAssignDependencyCycleSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignDependencyCycleSliceViolation() {
		this.alpha = this.beta + 1;
		this.beta = this.gamma + 1;
		this.gamma = this.alpha + 1;
	}
}
// === end ===

// === case: dependency_violation ===
class InputConstructorAssignDependencyViolation {
	int alpha, beta;

	InputConstructorAssignDependencyViolation(int alpha) {
		this.alpha = alpha;
		this.beta = this.alpha + 1;
	}
}
// === end ===

// === case: duplicate_field_assignment ===
// skip-reason: cannot reorder: a field is assigned more than once
class InputConstructorAssignDuplicateFieldAssignmentSliceViolation {
	int x, y;

	InputConstructorAssignDuplicateFieldAssignmentSliceViolation() {
		this.x = 1;
		this.y = x;
		this.x = 2;
	}
}
// === end ===

// === case: generic_param_shadow ===
// imports: java.util.Map
class InputConstructorAssignGenericParamShadow {
	int tracks, value;

	InputConstructorAssignGenericParamShadow(Map<String, Integer> value) {
		this.tracks = value.size() + 1;
		this.value = value.size();
	}
}
// === end ===

// === case: instance_init_bare_dependency ===
class InputConstructorAssignInstanceInitBareDependency {
	int alpha, value;

	{
		this.value = 5;
		this.alpha = value + 1;
	}
}
// === end ===

// === case: leading_statement_before_var_group ===
class InputConstructorAssignLeadingStatementBeforeVarGroupSliceViolation {
	int alpha, beta;

	InputConstructorAssignLeadingStatementBeforeVarGroupSliceViolation(int x) {
		System.out.println(x);
		final var v = x + 1;
		this.alpha = v;
		this.beta = v;
	}
}
// === end ===

// === case: leading_var_blank_separator ===
class InputConstructorAssignLeadingVarBlankSeparatorSliceViolation {
	int alpha, beta;

	InputConstructorAssignLeadingVarBlankSeparatorSliceViolation(int x) {
		final var v = x + 1;
		this.alpha = v;
		this.beta = v;
	}
}
// === end ===

// === case: masked_var_in_char_stays_simple ===
class InputConstructorAssignMaskedVarInChar {
	int alpha, beta;

	InputConstructorAssignMaskedVarInChar(int x) {
		this.alpha = 'c';

		final var c = x + 1;
		this.beta = c;
	}
}
// === end ===

// === case: masked_var_in_multiline_string_stays_multi ===
class InputConstructorAssignMaskedVarInMultilineString {
	int alpha;
	Object beta;

	InputConstructorAssignMaskedVarInMultilineString(int x) {
		this.alpha = new Object() {
			@Override
			public String toString() {
				return "factory";
			}
		};

		final var factory = x + 1;
		this.beta = factory;
	}
}
// === end ===

// === case: masked_var_in_string_stays_simple ===
class InputConstructorAssignMaskedVarInString {
	int alpha, beta;

	InputConstructorAssignMaskedVarInString(int x) {
		this.alpha = "size";

		final var size = x + 1;
		this.beta = size;
	}
}
// === end ===

// === case: method_name_not_dependency ===
class InputConstructorAssignMethodNameNotDependency {
	static int value() {
		return 7;
	}

	int beta, value;

	InputConstructorAssignMethodNameNotDependency() {
		this.beta = value();
		this.value = value();
	}
}
// === end ===

// === case: multi_alphabetical ===
class InputConstructorAssignMultiAlphabetical {
	Object alpha, beta;

	InputConstructorAssignMultiAlphabetical(Object a, Object b) {
		this.alpha = new Object() {
			@Override
			public String toString() {
				return a.toString();
			}
		};
		this.beta = new Object() {
			@Override
			public String toString() {
				return b.toString();
			}
		};
	}
}
// === end ===

// === case: multi_before_simple ===
class InputConstructorAssignMultiBeforeSimple {
	int alpha;
	Object beta;

	InputConstructorAssignMultiBeforeSimple(int alpha, Object beta) {
		this.alpha = alpha;

		this.beta = new Object() {
			@Override
			public String toString() {
				return beta.toString();
			}
		};
	}
}
// === end ===

// === case: multiline_local_var_decl_untracked ===
// skip-reason: cannot reorder: relocating an unused local would move its initializer's side effects
class InputConstructorAssignMultilineLocalVarDeclUntrackedSliceViolation {
	int alpha, beta;

	InputConstructorAssignMultilineLocalVarDeclUntrackedSliceViolation(int a) {
		this.beta = a;
		final var x =
				compute(a);
		this.alpha = a;
	}
}
// === end ===

// === case: no_shadow_dependency_reorder ===
class InputConstructorAssignNoShadowDependencyReorderSliceViolation {
	int alpha, beta;

	InputConstructorAssignNoShadowDependencyReorderSliceViolation() {
		this.alpha = 5;
		this.beta = this.alpha + 1;
	}
}
// === end ===

// === case: param_shadow_alphabetical ===
class InputConstructorAssignParamShadowAlphabetical {
	int tracks, value;

	InputConstructorAssignParamShadowAlphabetical(int value) {
		this.tracks = value + 1;
		this.value = value;
	}
}
// === end ===

// === case: partial_var_name_not_matched ===
class InputConstructorAssignPartialVarNameNotMatchedSliceViolation {
	int alpha, beta;

	InputConstructorAssignPartialVarNameNotMatchedSliceViolation(int x) {
		this.alpha = x;
		this.beta = x;
	}
}
// === end ===

// === case: primitive_array_local_var ===
class InputConstructorAssignPrimitiveArrayLocalVarSliceViolation {
	int alpha;
	int[] beta;

	InputConstructorAssignPrimitiveArrayLocalVarSliceViolation(int[] src) {
		this.alpha = 1;

		final var arr = src;
		this.beta = arr;
	}
}
// === end ===

// === case: primitive_local_var ===
class InputConstructorAssignPrimitiveLocalVarSliceViolation {
	int alpha, beta;

	InputConstructorAssignPrimitiveLocalVarSliceViolation(int x) {
		this.alpha = x;

		final var size = x + 1;
		this.beta = size;
	}
}
// === end ===

// === case: qualified_chain_dependency ===
class InputConstructorAssignQualifiedChainDependency {
	int alpha;
	String value;

	InputConstructorAssignQualifiedChainDependency(String value) {
		this.value = value;
		this.alpha = this.value.length();
	}
}
// === end ===

// === case: shared_line_statements ===
// skip-reason: cannot reorder: multiple statements share a line
class InputConstructorAssignSharedLineStatementsSliceViolation {
	int alpha, beta;

	InputConstructorAssignSharedLineStatementsSliceViolation() {
		this.beta = 2; this.alpha = 1;
	}
}
// === end ===

// === case: statement_after_second_assignment ===
// skip-reason: cannot reorder: an interleaved statement would change execution order
class InputConstructorAssignStatementAfterSecondAssignmentSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignStatementAfterSecondAssignmentSliceViolation(int a, int b, int c) {
		this.gamma = c;
		this.alpha = a;
		foo();
		this.beta = b;
	}
}
// === end ===

// === case: statement_between_assignments ===
// skip-reason: cannot reorder: an interleaved statement would change execution order
class InputConstructorAssignStatementBetweenAssignmentsSliceViolation {
	int alpha, beta;

	InputConstructorAssignStatementBetweenAssignmentsSliceViolation(int a, int b) {
		this.beta = b;
		System.out.println(a);
		this.alpha = a;
	}
}
// === end ===

// === case: string_with_braces ===
class InputConstructorAssignStringWithBracesSliceViolation {
	String alpha, beta;

	InputConstructorAssignStringWithBracesSliceViolation(String a) {
		this.alpha = a;
		this.beta = "{}";
	}
}
// === end ===

// === case: text_block_escaped_quote ===
class InputConstructorAssignTextBlockEscapedQuoteSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockEscapedQuoteSliceViolation(String a) {
		this.alpha = a;

		this.beta = """
				he said \"hi\"
				""";
	}
}
// === end ===

// === case: text_block_multiple_braces ===
class InputConstructorAssignTextBlockMultipleBracesSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockMultipleBracesSliceViolation(String a) {
		this.alpha = a;

		this.beta = """
				{ } { }
				""";
	}
}
// === end ===

// === case: text_block_unbalanced_open_brace ===
class InputConstructorAssignTextBlockUnbalancedOpenBraceSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockUnbalancedOpenBraceSliceViolation(String a) {
		this.alpha = a;

		this.beta = """
				if (a != null) {
				""";
	}
}
// === end ===

// === case: text_block_with_brace ===
class InputConstructorAssignTextBlockWithBraceSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockWithBraceSliceViolation(String a) {
		this.alpha = a;

		this.beta = """
				}
				""";
	}
}
// === end ===

// === case: three_var_subgroups ===
class InputConstructorAssignThreeVarSubgroupsSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignThreeVarSubgroupsSliceViolation(int x) {
		final var first = x + 1;
		this.alpha = first;

		final var second = x + 2;
		this.beta = second;

		final var third = x + 3;
		this.gamma = third;
	}
}
// === end ===

// === case: unused_local_var_tail ===
class InputConstructorAssignUnusedLocalVarTailSliceViolation {
	int alpha, beta;

	InputConstructorAssignUnusedLocalVarTailSliceViolation(int x) {
		this.alpha = x;
		this.beta = x;
		final var unused = x + 1;
	}
}
// === end ===

// === case: var_alphabetical ===
class InputConstructorAssignVarAlphabetical {
	int alpha, beta;

	InputConstructorAssignVarAlphabetical(int x) {
		final var computed = x * 2;
		this.alpha = computed + 1;
		this.beta = computed;
	}
}
// === end ===

// === case: var_before_multi ===
class InputConstructorAssignVarBeforeMulti {
	int beta;
	Object alpha;

	InputConstructorAssignVarBeforeMulti(int x) {
		this.alpha = new Object() {
			@Override
			public String toString() {
				return "test";
			}
		};

		final var computed = x * 2;
		this.beta = computed;
	}
}
// === end ===

// === case: var_before_simple ===
class InputConstructorAssignVarBeforeSimple {
	int alpha, beta;

	InputConstructorAssignVarBeforeSimple(int x) {
		this.beta = x;

		final var computed = x * 2;
		this.alpha = computed;
	}
}
// === end ===

// === case: var_chain_three_deep ===
class InputConstructorAssignVarChainThreeDeepSliceViolation {
	int data;
	String name;

	InputConstructorAssignVarChainThreeDeepSliceViolation(int x) {
		this.name = "x";

		final var seed = x;
		final var mid = seed + 1;
		final var derived = mid + 1;
		this.data = derived;
	}
}
// === end ===

// === case: var_dependency_violation ===
class InputConstructorAssignVarDependencyViolation {
	int alpha, beta;

	InputConstructorAssignVarDependencyViolation(int beta, int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = computed + this.beta;
	}
}
// === end ===

// === case: var_group_order ===
class InputConstructorAssignVarGroupOrder {
	int alpha, beta;

	InputConstructorAssignVarGroupOrder(int x) {
		final var first = x + 1;
		this.alpha = first;

		final var second = x + 2;
		this.beta = second;
	}
}
// === end ===

// === case: var_used_by_var ===
class InputConstructorAssignVarUsedByVarSliceViolation {
	int data;
	String name;

	InputConstructorAssignVarUsedByVarSliceViolation(int x) {
		this.name = "x";

		final var seed = x;
		final var derived = seed + 1;
		this.data = derived;
	}
}
// === end ===