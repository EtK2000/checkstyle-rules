package com.etk2000.checkstyle.inputs.constructorassignmentorder;

// === case: alphabetical_simple ===
class InputConstructorAssignViolation {
	int alpha, beta;

	InputConstructorAssignViolation(int alpha, int beta) {
		this.beta = beta;
		this.alpha = alpha; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: alphabetical_three_fields ===
class InputConstructorAssignAlphabeticalThreeFieldsSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignAlphabeticalThreeFieldsSliceViolation(int a, int b, int c) {
		this.gamma = c;
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.gamma' (alphabetical order).
		this.beta = b;
	}
}
// === end ===

// === case: assignment_shares_line_with_statement ===
// skip-reason: cannot reorder: multiple statements share a line
class InputConstructorAssignSharesLineWithStatementSliceViolation {
	int alpha, beta;

	InputConstructorAssignSharesLineWithStatementSliceViolation() {
		this.beta = 2;
		bar(); this.alpha = 1; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: bare_dependency_violation ===
class InputConstructorAssignBareDependencyViolation {
	int alpha, beta;

	InputConstructorAssignBareDependencyViolation() {
		this.beta = alpha; // violation: Assignment 'this.beta' references 'this.alpha' which should be assigned before it.
		this.alpha = 3;
	}
}
// === end ===

// === case: bare_in_string_not_dependency ===
class InputConstructorAssignBareInStringNotDependency {
	String alpha, beta;

	InputConstructorAssignBareInStringNotDependency(String x) {
		this.beta = x;
		this.alpha = "beta"; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: bare_lambda_dependency_violation ===
class InputConstructorAssignBareLambdaDependencyViolation {
	int value;
	Runnable alpha;

	InputConstructorAssignBareLambdaDependencyViolation(int seed) {
		this.alpha = () -> System.out.println(value); // violation: Assignment 'this.alpha' references 'this.value' which should be assigned before it.
		this.value = seed;
	}
}
// === end ===

// === case: bare_substring_not_matched ===
class InputConstructorAssignBareSubstringNotMatched {
	int alpha, beta;

	InputConstructorAssignBareSubstringNotMatched(int betaValue) {
		this.beta = betaValue;
		this.alpha = betaValue; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: bare_var_dependency_violation ===
class InputConstructorAssignBareVarDependencyViolation {
	int alpha, beta;

	InputConstructorAssignBareVarDependencyViolation(int x) {
		final var c = x;
		this.alpha = c + beta; // violation: Assignment 'this.alpha' references 'this.beta' which should be assigned before it.
		this.beta = c;
	}
}
// === end ===

// === case: blank_between_assignments ===
class InputConstructorAssignBlankBetweenAssignments {
	int alpha, beta;

	InputConstructorAssignBlankBetweenAssignments(int a, int b) {
		this.beta = b;

		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
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
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
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
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: compact_ctor_reorder ===
record InputConstructorAssignCompactCtorReorderSliceViolation(int alpha, int beta) {
	InputConstructorAssignCompactCtorReorderSliceViolation {
		final var temp = alpha + beta;
		this.beta = temp + 1;
		this.alpha = temp; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: dependency_cycle ===
// multi-fix-expected
// skip-reason: cannot reorder: a field dependency cycle has no valid order
class InputConstructorAssignDependencyCycleSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignDependencyCycleSliceViolation() {
		this.alpha = this.beta + 1; // violation: Assignment 'this.alpha' references 'this.beta' which should be assigned before it.
		this.beta = this.gamma + 1; // violation: Assignment 'this.beta' references 'this.gamma' which should be assigned before it.
		this.gamma = this.alpha + 1;
	}
}
// === end ===

// === case: dependency_violation ===
class InputConstructorAssignDependencyViolation {
	int alpha, beta;

	InputConstructorAssignDependencyViolation(int alpha) {
		this.beta = this.alpha + 1; // violation: Assignment 'this.beta' references 'this.alpha' which should be assigned before it.
		this.alpha = alpha;
	}
}
// === end ===

// === case: duplicate_field_assignment ===
// skip-reason: cannot reorder: a field is assigned more than once
class InputConstructorAssignDuplicateFieldAssignmentSliceViolation {
	int x, y;

	InputConstructorAssignDuplicateFieldAssignmentSliceViolation() {
		this.x = 1;
		this.y = x; // violation: Assignment 'this.y' references 'this.x' which should be assigned before it.
		this.x = 2;
	}
}
// === end ===

// === case: generic_param_shadow ===
// imports: java.util.Map
class InputConstructorAssignGenericParamShadow {
	int tracks, value;

	InputConstructorAssignGenericParamShadow(Map<String, Integer> value) {
		this.value = value.size();
		this.tracks = value.size() + 1; // violation: Assignment 'this.tracks' must appear before 'this.value' (alphabetical order).
	}
}
// === end ===

// === case: instance_init_bare_dependency ===
class InputConstructorAssignInstanceInitBareDependency {
	int alpha, value;

	{
		this.alpha = value + 1; // violation: Assignment 'this.alpha' references 'this.value' which should be assigned before it.
		this.value = 5;
	}
}
// === end ===

// === case: leading_statement_before_var_group ===
class InputConstructorAssignLeadingStatementBeforeVarGroupSliceViolation {
	int alpha, beta;

	InputConstructorAssignLeadingStatementBeforeVarGroupSliceViolation(int x) {
		System.out.println(x);
		final var v = x + 1;
		this.beta = v;
		this.alpha = v; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: leading_var_blank_separator ===
class InputConstructorAssignLeadingVarBlankSeparatorSliceViolation {
	int alpha, beta;

	InputConstructorAssignLeadingVarBlankSeparatorSliceViolation(int x) {
		final var v = x + 1;

		this.beta = v;
		this.alpha = v; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: masked_var_in_char_stays_simple ===
class InputConstructorAssignMaskedVarInChar {
	int alpha, beta;

	InputConstructorAssignMaskedVarInChar(int x) {
		final var c = x + 1;
		this.beta = c;
		this.alpha = 'c'; // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
	}
}
// === end ===

// === case: masked_var_in_multiline_string_stays_multi ===
class InputConstructorAssignMaskedVarInMultilineString {
	int alpha;
	Object beta;

	InputConstructorAssignMaskedVarInMultilineString(int x) {
		final var factory = x + 1;
		this.beta = factory;
		this.alpha = new Object() { // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
			@Override
			public String toString() {
				return "factory";
			}
		};
	}
}
// === end ===

// === case: masked_var_in_string_stays_simple ===
class InputConstructorAssignMaskedVarInString {
	int alpha, beta;

	InputConstructorAssignMaskedVarInString(int x) {
		final var size = x + 1;
		this.beta = size;
		this.alpha = "size"; // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
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
		this.value = value();
		this.beta = value(); // violation: Assignment 'this.beta' must appear before 'this.value' (alphabetical order).
	}
}
// === end ===

// === case: multi_alphabetical ===
class InputConstructorAssignMultiAlphabetical {
	Object alpha, beta;

	InputConstructorAssignMultiAlphabetical(Object a, Object b) {
		this.beta = new Object() {
			@Override
			public String toString() {
				return b.toString();
			}
		};
		this.alpha = new Object() { // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
			@Override
			public String toString() {
				return a.toString();
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
		this.beta = new Object() {
			@Override
			public String toString() {
				return beta.toString();
			}
		};
		this.alpha = alpha; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
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
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: no_shadow_dependency_reorder ===
class InputConstructorAssignNoShadowDependencyReorderSliceViolation {
	int alpha, beta;

	InputConstructorAssignNoShadowDependencyReorderSliceViolation() {
		this.beta = this.alpha + 1; // violation: Assignment 'this.beta' references 'this.alpha' which should be assigned before it.
		this.alpha = 5;
	}
}
// === end ===

// === case: param_shadow_alphabetical ===
class InputConstructorAssignParamShadowAlphabetical {
	int tracks, value;

	InputConstructorAssignParamShadowAlphabetical(int value) {
		this.value = value;
		this.tracks = value + 1; // violation: Assignment 'this.tracks' must appear before 'this.value' (alphabetical order).
	}
}
// === end ===

// === case: partial_var_name_not_matched ===
class InputConstructorAssignPartialVarNameNotMatchedSliceViolation {
	int alpha, beta;

	InputConstructorAssignPartialVarNameNotMatchedSliceViolation(int x) {
		this.beta = x;
		this.alpha = x; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: primitive_array_local_var ===
class InputConstructorAssignPrimitiveArrayLocalVarSliceViolation {
	int alpha;
	int[] beta;

	InputConstructorAssignPrimitiveArrayLocalVarSliceViolation(int[] src) {
		final var arr = src;
		this.beta = arr;
		this.alpha = 1; // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
	}
}
// === end ===

// === case: primitive_local_var ===
class InputConstructorAssignPrimitiveLocalVarSliceViolation {
	int alpha, beta;

	InputConstructorAssignPrimitiveLocalVarSliceViolation(int x) {
		final var size = x + 1;
		this.beta = size;
		this.alpha = x; // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
	}
}
// === end ===

// === case: qualified_chain_dependency ===
class InputConstructorAssignQualifiedChainDependency {
	int alpha;
	String value;

	InputConstructorAssignQualifiedChainDependency(String value) {
		this.alpha = this.value.length(); // violation: Assignment 'this.alpha' references 'this.value' which should be assigned before it.
		this.value = value;
	}
}
// === end ===

// === case: shared_line_statements ===
// skip-reason: cannot reorder: multiple statements share a line
class InputConstructorAssignSharedLineStatementsSliceViolation {
	int alpha, beta;

	InputConstructorAssignSharedLineStatementsSliceViolation() {
		this.beta = 2; this.alpha = 1; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: statement_after_second_assignment ===
// skip-reason: cannot reorder: an interleaved statement would change execution order
class InputConstructorAssignStatementAfterSecondAssignmentSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignStatementAfterSecondAssignmentSliceViolation(int a, int b, int c) {
		this.gamma = c;
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.gamma' (alphabetical order).
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
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: string_with_braces ===
class InputConstructorAssignStringWithBracesSliceViolation {
	String alpha, beta;

	InputConstructorAssignStringWithBracesSliceViolation(String a) {
		this.beta = "{}";
		this.alpha = a; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: text_block_escaped_quote ===
class InputConstructorAssignTextBlockEscapedQuoteSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockEscapedQuoteSliceViolation(String a) {
		this.beta = """
				he said \"hi\"
				""";
		this.alpha = a; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}
// === end ===

// === case: text_block_multiple_braces ===
class InputConstructorAssignTextBlockMultipleBracesSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockMultipleBracesSliceViolation(String a) {
		this.beta = """
				{ } { }
				""";
		this.alpha = a; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}
// === end ===

// === case: text_block_unbalanced_open_brace ===
class InputConstructorAssignTextBlockUnbalancedOpenBraceSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockUnbalancedOpenBraceSliceViolation(String a) {
		this.beta = """
				if (a != null) {
				""";
		this.alpha = a; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}
// === end ===

// === case: text_block_with_brace ===
class InputConstructorAssignTextBlockWithBraceSliceViolation {
	String alpha, beta;

	InputConstructorAssignTextBlockWithBraceSliceViolation(String a) {
		this.beta = """
				}
				""";
		this.alpha = a; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}
// === end ===

// === case: three_var_subgroups ===
class InputConstructorAssignThreeVarSubgroupsSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignThreeVarSubgroupsSliceViolation(int x) {
		final var first = x + 1;
		final var second = x + 2;
		final var third = x + 3;
		this.alpha = first;
		this.gamma = third;
		this.beta = second; // violation: Assignment 'this.beta' must appear before 'this.gamma' (variable declaration order).
	}
}
// === end ===

// === case: unused_local_var_tail ===
class InputConstructorAssignUnusedLocalVarTailSliceViolation {
	int alpha, beta;

	InputConstructorAssignUnusedLocalVarTailSliceViolation(int x) {
		final var unused = x + 1;
		this.beta = x;
		this.alpha = x; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: var_alphabetical ===
class InputConstructorAssignVarAlphabetical {
	int alpha, beta;

	InputConstructorAssignVarAlphabetical(int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = computed + 1; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}
// === end ===

// === case: var_before_multi ===
class InputConstructorAssignVarBeforeMulti {
	int beta;
	Object alpha;

	InputConstructorAssignVarBeforeMulti(int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = new Object() { // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
			@Override
			public String toString() {
				return "test";
			}
		};
	}
}
// === end ===

// === case: var_before_simple ===
class InputConstructorAssignVarBeforeSimple {
	int alpha, beta;

	InputConstructorAssignVarBeforeSimple(int x) {
		final var computed = x * 2;
		this.alpha = computed;
		this.beta = x; // violation: Assignment 'this.beta' must appear before variable-dependent assignments.
	}
}
// === end ===

// === case: var_chain_three_deep ===
class InputConstructorAssignVarChainThreeDeepSliceViolation {
	int data;
	String name;

	InputConstructorAssignVarChainThreeDeepSliceViolation(int x) {
		final var seed = x;
		final var mid = seed + 1;
		final var derived = mid + 1;
		this.data = derived;
		this.name = "x"; // violation: Assignment 'this.name' must appear before variable-dependent assignments.
	}
}
// === end ===

// === case: var_dependency_violation ===
class InputConstructorAssignVarDependencyViolation {
	int alpha, beta;

	InputConstructorAssignVarDependencyViolation(int beta, int x) {
		final var computed = x * 2;
		this.alpha = computed + this.beta; // violation: Assignment 'this.alpha' references 'this.beta' which should be assigned before it.
		this.beta = computed;
	}
}
// === end ===

// === case: var_group_order ===
class InputConstructorAssignVarGroupOrder {
	int alpha, beta;

	InputConstructorAssignVarGroupOrder(int x) {
		final var first = x + 1;
		final var second = x + 2;
		this.beta = second;
		this.alpha = first; // violation: Assignment 'this.alpha' must appear before 'this.beta' (variable declaration order).
	}
}
// === end ===

// === case: var_used_by_var ===
class InputConstructorAssignVarUsedByVarSliceViolation {
	int data;
	String name;

	InputConstructorAssignVarUsedByVarSliceViolation(int x) {
		final var seed = x;
		final var derived = seed + 1;
		this.data = derived;
		this.name = "x"; // violation: Assignment 'this.name' must appear before variable-dependent assignments.
	}
}
// === end ===