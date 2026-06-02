package com.etk2000.checkstyle.inputs.jitinefficiency;

// === case: allocation_new_string_fqn_bails ===
// skip-reason: redundant new String(...) wrapper
class InputJitInefficiencyAllocationNewStringFqnBailsSliceViolation {
	void m() {
		final var a = "hello";
		System.out.println(a);
	}
}
// === end ===

// === case: allocation_string_buffer_fqn_constructor ===
// skip-reason: local StringBuffer
class InputJitInefficiencyAllocationStringBufferFqnConstructorSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		System.out.println(sb);
	}
}
// === end ===

// === case: allocation_to_array_sized_bare_ident_size ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedBareIdentSizeSliceViolation {
	void m(List<String> list, int n) {
		final var a = list.toArray(String[]::new);
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_length_suffix_size ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedLengthSuffixSizeSliceViolation {
	void m(List<String> list, String key) {
		final var a = list.toArray(String[]::new);
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_literal ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedLiteralSliceViolation {
	void m(List<String> list) {
		final var a = list.toArray(String[]::new);
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_qualified_type ===
// imports: java.util.Date
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedQualifiedTypeSliceViolation {
	void m(List<Date> list) {
		final var a = list.toArray(Date[]::new);
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_size_expr ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedSizeExprSliceViolation {
	void m(List<String> list) {
		final var b = list.toArray(String[]::new);
		System.out.println(b.length);
	}
}
// === end ===

// === case: array_lhs_body_line_packs_this_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksThisChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		matrix.cells[i][j] = "";
		for (var x : list)
			matrix.cells[i][j] = matrix.cells[i][j] + x; this.matrix = pickNew();
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: array_lhs_for_header_chain_prefix_mentioned_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderChainPrefixMentionedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int k, int j) {
		for (var entry : matrix.entrySet())
			matrix.cells[k][j] = matrix.cells[k][j] + entry;
	}
}
// === end ===

// === case: array_lhs_for_header_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int a, int b, int n) {
		int j;
		for (this.matrix = pickNew(), j = 0; j < n; ++j)
			matrix.cells[a][b] = matrix.cells[a][b] + j;
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: array_lhs_this_chain_in_block_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInBlockCommentPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		matrix.cells[i][j] = "";
		final var sb = new StringBuilder();
		sb.append(matrix.cells[i][j]);
		for (var x : list) {
			/* this.matrix is fine */
			sb.append(x);
		}
		matrix.cells[i][j] = sb.toString();
	}
}
// === end ===

// === case: array_lhs_this_chain_in_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInCommentPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		matrix.cells[i][j] = "";
		final var sb = new StringBuilder();
		sb.append(matrix.cells[i][j]);
		for (var x : list) {
			// this.matrix is fine
			sb.append(x);
		}
		matrix.cells[i][j] = sb.toString();
	}
}
// === end ===

// === case: array_lhs_this_chain_in_string_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInStringLiteralPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		matrix.cells[i][j] = "";
		final var sb = new StringBuilder();
		sb.append(matrix.cells[i][j]);
		for (var x : list) {
			log("this.matrix is fine");
			sb.append(x);
		}
		matrix.cells[i][j] = sb.toString();
	}

	private void log(String s) {
	}
}
// === end ===

// === case: array_lhs_this_chain_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainMutatedBailsSliceViolation {
	static class Inner {
		String[] b;
	}

	final Inner a = new Inner();

	void m(List<String> list) {
		a.b[0] = "";
		for (var x : list) {
			a.b = newArr();
			a.b[0] = a.b[0] + x;
		}
	}

	String[] newArr() {
		return new String[10];
	}
}
// === end ===

// === case: array_lhs_this_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		matrix.cells[i][j] = "";
		for (var x : list) {
			this.matrix = pickNew();
			matrix.cells[i][j] = matrix.cells[i][j] + x;
		}
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: empty_string_concat_diamond_operand_bails ===
// skip-reason: empty-string concatenation the fixer cannot simplify
// imports: java.util.ArrayList
class InputJitInefficiencyEmptyStringConcatDiamondOperandBailsSliceViolation {
	void m() {
		final var s = "" + new ArrayList<>();
		System.out.println(s);
	}
}
// === end ===

// === case: empty_string_concat_switch_expression ===
class InputJitInefficiencyEmptyStringConcatSwitchExpressionSliceViolation {
	String m(int kind) {
		final var label = String.valueOf(switch (kind) { case 1 -> "a"; default -> "b"; });
		return label;
	}
}
// === end ===

// === case: empty_string_concat_switch_expression_operand_plus_bails ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyEmptyStringConcatSwitchExpressionOperandPlusBailsSliceViolation {
	String m(int kind, String a, String b) {
		final var label = "" + switch (kind) { case 1 -> a + b; default -> "c"; };
		return label;
	}
}
// === end ===

// === case: explicit_form_array_lhs_this_array ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsThisArraySliceViolation {
	String[] arr;

	void m(int k, List<String> list) {
		arr[k] = "";
		final var sb = new StringBuilder();
		sb.append(arr[k]);
		for (var x : list)
			sb.append(x);
		arr[k] = sb.toString();
	}
}
// === end ===

// === case: explicit_form_array_lhs_this_chained_index ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsThisChainedIndexSliceViolation {
	String[][] grid;

	void m(int k, int j, List<String> list) {
		grid[k][j] = "";
		final var sb = new StringBuilder();
		sb.append(grid[k][j]);
		for (var x : list)
			sb.append(x);
		grid[k][j] = sb.toString();
	}
}
// === end ===

// === case: explicit_form_deep_nested_field ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormDeepNestedFieldSliceViolation {
	static class Holder {
		Holder next;
		String value;
	}

	final Holder holder = new Holder();

	void m(List<String> list) {
		holder.next.value = "";
		final var sb = new StringBuilder();
		sb.append(holder.next.value);
		for (var x : list)
			sb.append(x);
		holder.next.value = sb.toString();
	}
}
// === end ===

// === case: explicit_form_do_while_tier3 ===
class InputJitInefficiencyExplicitFormDoWhileTier3SliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("x");
		while (sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: explicit_form_fqn_array_lhs ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormFqnArrayLhsSliceViolation {
	void m(int k, List<String> list) {
		final var local = new String[10];
		local[k] = "";
		final var sb = new StringBuilder();
		sb.append(local[k]);
		for (var x : list)
			sb.append(x);
		local[k] = sb.toString();
	}
}
// === end ===

// === case: explicit_form_this_dot_nested ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormThisDotNestedSliceViolation {
	static class Holder {
		Holder next;
		String value;
	}

	final Holder holder = new Holder();

	void m(List<String> list) {
		holder.value = "";
		final var sb = new StringBuilder();
		sb.append(holder.value);
		for (var x : list)
			sb.append(x);
		holder.value = sb.toString();
	}
}
// === end ===

// === case: field_lhs_tier2_do_while_after_statement_label_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyFieldLhsTier2DoWhileAfterStatementLabelBailsSliceViolation {
	private String out = "";

	void m(String out, String x, boolean c) {
		this.out = out;
		outer:
		do
			this.out = this.out + x;
		while (c);
	}
}
// === end ===

// === case: loop_boxed_accumulator_byte ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorByteSliceViolation {
	void m(int n) {
		var total = Byte.valueOf((byte) 0);
		for (var i = 0; i < n; ++i)
			total += 1;
		System.out.println(total);
	}
}
// === end ===

// === case: loop_boxed_accumulator_short ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorShortSliceViolation {
	void m(int n) {
		var total = Short.valueOf((short) 0);
		for (var i = 0; i < n; ++i)
			total += 1;
		System.out.println(total);
	}
}
// === end ===

// === case: string_concat_array_lhs_multi_line_for_each_continuation_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatArrayLhsMultiLineForEachContinuationLineBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[3];
		for (var i : indices)
			arr[i] = arr[i] + "x";
	}
}
// === end ===

// === case: string_concat_array_lhs_post_decrement_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsPostDecrementIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x;
			--k;
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_post_increment_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsPostIncrementIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x;
			++k;
		}
	}
}
// === end ===

// === case: string_concat_braced_do_while ===
class InputJitInefficiencyStringConcatBracedDoWhileSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("x");
		while (sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_condition_wraps_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatBracedDoWhileConditionWrapsBailsSliceViolation {
	void m(boolean flag) {
		var s = "";
		do
			s = s + "x";
		while (s.length() < 5
				&& flag);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_cuddled_terminator ===
class InputJitInefficiencyStringConcatBracedDoWhileCuddledTerminatorSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("x");
		while (sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_unsafe_method_in_while_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatBracedDoWhileUnsafeMethodInWhileBailsSliceViolation {
	void m(String target) {
		var s = "";
		do
			s = s + "y";
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_cuddled_else_below_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatCuddledElseBelowBodyBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x != null)
				s = s + x;
			else
				log("skip");
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_cuddled_else_no_space_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatCuddledElseNoSpaceBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x != null)
				s = s + x;
			else
				log("skip");
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_do_while_cuddled_terminator_decoy_while_below ===
class InputJitInefficiencyStringConcatDoWhileCuddledTerminatorDecoyWhileBelowSliceViolation {
	private String f = "";

	void m(boolean c, int n, String f) {
		this.f = f;
		final var sb = new StringBuilder();
		sb.append(this.f);
		do {
			if (c)
				sb.append("y");
		} while (n < 3);
		this.f = sb.toString();
		System.out.println(this.f);
		do n = next(n);
		while (n < 9);
	}

	private int next(int x) {
		return x + 1;
	}
}
// === end ===

// === case: string_concat_do_while_tier3_while_no_space_fixed ===
class InputJitInefficiencyStringConcatDoWhileTier3WhileNoSpaceFixedSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("y");
		while(sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_else_brace_no_space_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatElseBraceNoSpaceBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x != null)
				s = s + x;
			else
				log("skip");
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_else_branch_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatElseBranchBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x != null)
				s = s + x;
			else
				log("skip");
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_explicit_braced_body ===
// imports: java.util.List
class InputJitInefficiencyStringConcatExplicitBracedBodySliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list)
			sb.append(x);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_field_lhs_sb_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatFieldLhsSbTakenSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	String f;

	void m(List<String> list) {
		this.f = "";
		for (var x : list)
			this.f = f + x;
		sb.append(f);
	}
}
// === end ===

// === case: string_concat_generic_type_args_init_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.HashMap
// imports: java.util.List
class InputJitInefficiencyStringConcatGenericTypeArgsInitBailsSliceViolation {
	void m(List<String> values) {
		var s = new HashMap<String, Integer>().toString();
		for (var v : values)
			s = s + v;
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_if_else_above_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatIfElseAboveBodyBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x == null)
				log("skip");
			else
				s = s + x;
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_later_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatLaterAppendReadsAccumulatorBailsSliceViolation {
	String f() {
		var s = "";
		for (var i = 0; i < 3; ++i)
			s = s + "-" + s.length();
		return s;
	}
}
// === end ===

// === case: string_concat_middle_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatMiddleAppendReadsAccumulatorBailsSliceViolation {
	String f(String x) {
		var s = "";
		for (var i = 0; i < 3; ++i)
			s = s + "-" + s.length() + x;
		return s;
	}
}
// === end ===

// === case: string_concat_nested_if_body ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNestedIfBodySliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list) {
			if (x != null) {
				if (!x.isEmpty())
					sb.append(x);
			}
		}
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_packed_second_statement_in_decl_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatPackedSecondStatementInDeclBailsSliceViolation {
	void m(List<String> xs) {
		var s = ""; final var first = true;
		for (var x : xs)
			s += x;
		use(s, first);
	}

	void use(String a, boolean b) {
	}
}
// === end ===

// === case: string_concat_plus_assign_do_while_tier3 ===
class InputJitInefficiencyStringConcatPlusAssignDoWhileTier3SliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("x");
		while (sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_prepend_then_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatPrependThenAppendReadsAccumulatorBailsSliceViolation {
	String f(String p) {
		var s = "";
		for (var i = 0; i < 3; ++i)
			s = p + s + s.length();
		return s;
	}
}
// === end ===

// === case: string_concat_reassigned_after_loop_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatReassignedAfterLoopBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s = s + x;
		s = s.trim();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_same_line_else_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatSameLineElseBodyBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			if (x != null)
				s = s + x;
			else
				log("skip");
		}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_single_append_reads_accumulator ===
class InputJitInefficiencyStringConcatSingleAppendReadsAccumulatorSliceViolation {
	String f() {
		final var sb = new StringBuilder();
		for (var i = 0; i < 3; ++i)
			sb.append(sb.length());
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: string_concat_text_block_below_loop_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockBelowLoopBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x;
			System.out.println(x);
		}
		final var tb = """
				note
				""";
		System.out.println(s + tb);
	}
}
// === end ===

// === case: string_concat_text_block_close_line_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockCloseLineReadsAccumulatorBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			final var note = """
					note""" + s;
			s = s + x;
			System.out.println(note);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_array_lhs_mutated_index_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileArrayLhsMutatedIndexBailsSliceViolation {
	void m(String[] arr, int k) {
		do
			arr[k] = arr[k] + (++k);
		while (arr[k] != null);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_block_comment_in_do_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileBlockCommentInDoLineBailsSliceViolation {
	void m() {
		var s = "";
		do
			/* note */ s = s + "y";
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_block_comment_in_while_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileBlockCommentInWhileLineBailsSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("y");
		while (/* note */ sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_field_lhs_sb_taken ===
class InputJitInefficiencyStringConcatTier2DoWhileFieldLhsSbTakenSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	String f;

	void m() {
		this.f = "";
		do
			this.f = f + "y";
		while (f.length() < 5);
		sb.append(f);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_gap_mentions_var_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileGapMentionsVarBailsSliceViolation {
	void m() {
		var s = "";
		log(s);
		do
			s = s + "y";
		while (s.length() < 5);
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_tier2_do_while_mismatched_while_indent_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileMismatchedWhileIndentBailsSliceViolation {
	void m() {
		final var sb = new StringBuilder();
		do sb.append("y");
		while (sb.length() < 5);
		final var s = sb.toString();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_reassigned_after_loop_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileReassignedAfterLoopBailsSliceViolation {
	void m(int n) {
		var result = "";
		var x = n;
		do
			result = result + "a";
		while (--x > 0);
		result = result.trim();
		System.out.println(result);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_unsafe_method_in_while_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileUnsafeMethodInWhileBailsSliceViolation {
	void m(String target) {
		var s = "";
		do
			s = s + "y";
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_unary_increment_operand_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatUnaryIncrementOperandBailsSliceViolation {
	int count;

	String f(int n) {
		var s = "";
		for (var i = 0; i < n; ++i)
			s = s + ++count;
		return s;
	}
}
// === end ===

// === case: string_concat_unary_plus_operand_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatUnaryPlusOperandBailsSliceViolation {
	String f(int n, int b) {
		var s = "";
		for (var i = 0; i < n; ++i)
			s = s + +b;
		return s;
	}
}
// === end ===

// === case: string_concat_while_header_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderUnsafeMethodBailsSliceViolation {
	void m(String target) {
		var s = "";
		while (!s.equals(target))
			s = s + "x";
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_wraps_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderWrapsUnsafeMethodBailsSliceViolation {
	void m(int n, String target) {
		var s = "";
		while (n > 0
				&& !s.equals(target)) {
			s = s + "x";
			--n;
		}
		System.out.println(s);
	}
}
// === end ===

// === case: tier2_do_while_field_this ===
class InputJitInefficiencyTier2DoWhileFieldThisSliceViolation {
	String f;

	void m() {
		do
			this.f = f + "y";
		while (f.length() < 5);
	}
}
// === end ===