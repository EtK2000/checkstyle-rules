package com.etk2000.checkstyle.inputs.jitinefficiency;

// === case: allocation_append_concat_this_receiver_self_reference_bails ===
// skip-reason: cannot rewrite the string concatenation inside 'append(...)': this shape is not supported
class InputJitInefficiencyAllocationAppendConcatThisReceiverSelfReferenceBailsSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	void m(String x) {
		sb.append(x + sb.length());
	}
}
// === end ===

// === case: allocation_append_concat_this_receiver_sibling_field ===
class InputJitInefficiencyAllocationAppendConcatThisReceiverSiblingFieldSliceViolation {
	private final String other = "o";
	private final StringBuilder sb = new StringBuilder();

	void m(String x) {
		// both sides resolve to a field name, so a different field reads as a different name
		sb.append(x).append(other);
	}
}
// === end ===

// === case: allocation_append_concat_this_receiver_unrelated_operand ===
class InputJitInefficiencyAllocationAppendConcatThisReceiverUnrelatedOperandSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	void m(String x, String y) {
		sb.append(x).append(y);
	}
}
// === end ===

// === case: allocation_string_buffer_explicit_type_bails ===
// skip-reason: cannot replace the local 'StringBuffer': this shape is not supported
class InputJitInefficiencyAllocationStringBufferExplicitTypeBailsSliceViolation {
	void m() {
		// only the class name inside the `new` is replaced, so an explicit declaration would be
		// left saying StringBuffer over a StringBuilder initializer
		final var sb = new StringBuilder();
		sb.append("x");
		System.out.println(sb);
	}
}
// === end ===

// === case: allocation_to_array_sized_qualified_type ===
// skip-reason: cannot rewrite the presized 'toArray(...)': this shape is not supported
// imports: java.util.Date
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedQualifiedTypeSliceViolation {
	void m(List<Date> list) {
		final var a = list.toArray(new Date[5]);
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

// === case: allocation_to_array_sized_this_qualified_size ===
// skip-reason: cannot rewrite the presized 'toArray(...)': this shape is not supported
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedThisQualifiedSizeSliceViolation {
	int n;

	void m(List<String> list) {
		final var a = list.toArray(new String[n]);
		System.out.println(a.length);
	}
}
// === end ===

// === case: array_lhs_body_line_packs_this_chain_prefix_mutated_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: empty_string_concat_anon_inherited_string_method ===
class InputJitInefficiencyEmptyStringConcatAnonInheritedStringMethodSliceViolationBase {
	String pick() {
		return "hi";
	}
}

class InputJitInefficiencyEmptyStringConcatAnonInheritedStringMethodSliceViolation {
	Object m() {
		// `this` names the anonymous body, so it reads the base's String pick() rather than the
		// char[] one on the class around it
		return new InputJitInefficiencyEmptyStringConcatAnonInheritedStringMethodSliceViolationBase() {
			String dump() {
				return String.valueOf(pick());
			}
		};
	}

	char[] pick() {
		return new char[] { 'h', 'i' };
	}
}
// === end ===

// === case: empty_string_concat_char_array_field_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatCharArrayFieldBailsSliceViolation {
	private char[] buf;

	String dump() {
		// the same divergence reached through a field access rather than a bare name
		return "" + buf;
	}
}
// === end ===

// === case: empty_string_concat_diamond_operand ===
// imports: java.util.ArrayList
class InputJitInefficiencyEmptyStringConcatDiamondOperandSliceViolation {
	void m() {
		final var s = String.valueOf(new ArrayList<>());
		System.out.println(s);
	}
}
// === end ===

// === case: empty_string_concat_enum_constant_this_field_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
enum InputJitInefficiencyEmptyStringConcatEnumConstantThisFieldBailsSliceViolation {
	A {
		@Override
		String render() {
			// an enum constant's body subclasses the enum, so `this.chars` names the enum's own
			// field through inheritance; stopping the member scan at the constant's own body
			// leaves the type unresolved and the char[] gate fails open
			return "" + chars;
		}
	};

	char[] chars = { 'h', 'i' };

	abstract String render();
}
// === end ===

// === case: empty_string_concat_inherited_char_array_this_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatInheritedCharArrayThisBailsSliceViolationBase {
	char[] chars = { 'h', 'i' };
}

class InputJitInefficiencyEmptyStringConcatInheritedCharArrayThisBailsSliceViolation
		extends InputJitInefficiencyEmptyStringConcatInheritedCharArrayThisBailsSliceViolationBase {
	String dump() {
		// the qualified spelling must reach the same field the bare one does
		return "" + chars;
	}
}
// === end ===

// === case: empty_string_concat_outer_vs_inherited_char_array_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatOuterVsInheritedCharArrayBailsSliceViolationBase {
	char[] chars = { 'h', 'i' };
}

class InputJitInefficiencyEmptyStringConcatOuterVsInheritedCharArrayBailsSliceViolation {
	class Inner
			extends InputJitInefficiencyEmptyStringConcatOuterVsInheritedCharArrayBailsSliceViolationBase {
		String dump() {
			// an inherited field hides an enclosing one, so this is the base's char[], not the
			// outer class's String
			return "" + chars;
		}
	}

	String chars = "outer";
}
// === end ===

// === case: empty_string_concat_qualified_owner_char_array_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatQualifiedOwnerCharArrayBailsSliceViolation {
	static class Outer {
		static class Inner {
			char[] v = { 'h', 'i' };
		}
	}

	String dump() {
		// the local's declared type is written qualified, which only a dotted lookup resolves
		final var box = new Outer.Inner();
		return "" + box.v;
	}
}
// === end ===

// === case: empty_string_concat_record_accessor_this_char_array_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
record InputJitInefficiencyEmptyStringConcatRecordAccessorThisCharArrayBailsSliceViolation(char[] data) {
	String dump() {
		// the qualified spelling must reach the same component the bare one does
		return "" + data();
	}
}
// === end ===

// === case: empty_string_concat_record_component_varargs_array_this_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
record InputJitInefficiencyEmptyStringConcatRecordComponentVarargsArrayThisBailsSliceViolation(char[]... rows) {
	String dump() {
		return "" + rows[0];
	}
}
// === end ===

// === case: empty_string_concat_record_component_varargs_this_char_array_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
record InputJitInefficiencyEmptyStringConcatRecordComponentVarargsThisCharArrayBailsSliceViolation(char... cs) {
	String dump() {
		// the qualified spelling reads the component's type directly, with no dimension to re-add
		return "" + cs;
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

// === case: empty_string_concat_switch_expression_operand_plus ===
class InputJitInefficiencyEmptyStringConcatSwitchExpressionOperandPlusSliceViolation {
	String m(int kind, String a, String b) {
		final var label = String.valueOf(switch (kind) { case 1 -> a + b; default -> "c"; });
		return label;
	}
}
// === end ===

// === case: empty_string_concat_ternary_char_array_paren_condition_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatTernaryCharArrayParenConditionBailsSliceViolation {
	char[] a = { 'h', 'i' }, b = { 'y', 'o' };

	String m(int n) {
		// a grouping paren is a sibling, so it takes the slot the condition would sit in
		return "" + ((n > 0) ? a : b);
	}
}
// === end ===

// === case: empty_string_concat_ternary_char_array_paren_false_branch_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatTernaryCharArrayParenFalseBranchBailsSliceViolation {
	char[] a = { 'h', 'i' }, b = { 'y', 'o' };

	String m(boolean flag) {
		// the last child here is the RPAREN, not the branch
		return "" + (flag ? a : (b));
	}
}
// === end ===

// === case: empty_string_concat_this_qualified_char_array_call_bails ===
// skip-reason: cannot simplify the empty-string concatenation: this shape is not supported
class InputJitInefficiencyEmptyStringConcatThisQualifiedCharArrayCallBailsSliceViolation {
	char[] pick() {
		return new char[] { 'h', 'i' };
	}

	String m() {
		// a this. qualifier names a method of this very type, exactly as the bare spelling does
		return "" + pick();
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: foreign_category_enum_values_field_lhs_does_not_borrow_concat_rewrite ===
// skip-reason: cannot hoist the 'Enum.values()' call out of the loop: this shape is not supported
class InputJitInefficiencyForeignCategoryEnumValuesFieldLhsDoesNotBorrowConcatRewriteSliceViolation {
	enum Color {
		BLUE
	}

	int total;

	int m() {
		// the loop rewriter's field path synthesizes a String declaration without resolving the
		// field's type, so reaching it from another category emitted `this.total = sb.toString();`
		for (var i = 0; i < 3; ++i)
			this.total = total + Color.values().length;
		return total;
	}
}
// === end ===

// === case: foreign_category_reusable_object_does_not_borrow_concat_rewrite ===
// skip-reason: cannot hoist the reusable object creation: this shape is not supported
// imports: java.util.regex.Pattern
class InputJitInefficiencyForeignCategoryReusableObjectDoesNotBorrowConcatRewriteSliceViolation {
	int total;

	int m() {
		for (var i = 0; i < 3; ++i)
			this.total = total + Pattern.compile("a").pattern().length();
		return total;
	}
}
// === end ===

// === case: loop_boxed_accumulator_byte ===
// skip-reason: cannot rewrite the boxed accumulator: this shape is not supported
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
// skip-reason: cannot rewrite the boxed accumulator: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatArrayLhsMultiLineForEachContinuationLineBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[3];
		for (var i : indices)
			arr[i] = arr[i] + "x";
	}
}
// === end ===

// === case: string_concat_array_lhs_post_decrement_index_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_bare_indexed_read_of_this_qualified_array_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatBareIndexedReadOfThisQualifiedArrayAccumulatorBailsSliceViolation {
	String[] arr;

	void m(int k, List<String> list) {
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x;
			// the same slot under its other spelling, carried with its index
			System.out.println(arr[k]);
		}
	}
}
// === end ===

// === case: string_concat_bare_operand_reads_accumulator_alias_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatBareOperandReadsAccumulatorAliasBailsSliceViolation {
	private String f = "";

	void m(List<String> list) {
		for (var x : list) {
			// the operand reads the accumulator under its other spelling, on the assignment's
			// own line, where only the spelling the rewrite redirects would have been caught
			this.f = f + f.length();
		}
		System.out.println(x(list));
	}

	private String x(List<String> list) {
		return list.toString();
	}
}
// === end ===

// === case: string_concat_bare_read_of_nested_this_qualified_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatBareReadOfNestedThisQualifiedAccumulatorBailsSliceViolation {
	static class Holder {
		String value = "";
	}

	private final Holder holder = new Holder();

	void m(List<String> list) {
		for (var x : list) {
			holder.value = holder.value + x;
			// the same storage one segment deeper, which a whole-identifier scan cannot see
			log(holder.value);
		}
	}

	private void log(String v) {
		System.out.println(v);
	}
}
// === end ===

// === case: string_concat_bare_read_of_this_qualified_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatBareReadOfThisQualifiedAccumulatorBailsSliceViolation {
	private String s = "";

	void m(List<String> list) {
		for (var v : list) {
			this.s = s + v;
			// the same field under its other spelling, which the write-back only reaches after
			// the loop, so rewriting would feed this read the pre-loop value every iteration
			log(s);
		}
	}

	private void log(String v) {
		System.out.println(v);
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_gap_cross_scope_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatGapCrossScopeBailsSliceViolation {
	void g() {
		final var s = "in g";
		System.out.println(s);
	}

	// the accumulator is a parameter here, so the backward scan for its declaration runs past the
	// method boundary and lands on g()'s local
	String h(List<String> list, String s) {
		for (var x : list)
			s = s + x;
		return s;
	}
}
// === end ===

// === case: string_concat_gap_nested_scope_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatGapNestedScopeBailsSliceViolation {
	String m(boolean flag, List<String> list) {
		var s = "";
		if (flag) {
			for (var x : list)
				s = s + x;
		}
		return s;
	}
}
// === end ===

// === case: string_concat_gap_text_block_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatGapTextBlockBailsSliceViolation {
	String m(List<String> list) {
		var s = "";
		final var note = """
				note""";
		for (var x : list)
			s = s + x;
		return s + note;
	}
}
// === end ===

// === case: string_concat_generic_type_args_init_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatMiddleAppendReadsAccumulatorBailsSliceViolation {
	String f(String x) {
		var s = "";
		for (var i = 0; i < 3; ++i)
			s = s + "-" + s.length() + x;
		return s;
	}
}
// === end ===

// === case: string_concat_nested_assignment_lhs_mismatch_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatNestedAssignmentLhsMismatchBailsSliceViolation {
	String f(String x) {
		var a = "";
		var s = "";
		// the check reports the inner assignment to `s`, while the line scanner stops at the first
		// `=` and reads `a`, so the two disagree about which variable the rewrite would replace
		for (var i = 0; i < 3; ++i)
			a = s = s + x;
		return a + s;
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_plus_assign_char_array_operand_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatPlusAssignCharArrayOperandBailsSliceViolation {
	void m(int n, char[] buf) {
		// the operand gets its own append, where append(char[]) writes the characters
		var s = "";
		for (var i = 0; i < n; ++i)
			s += buf;
		System.out.println(s);
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

// === case: string_concat_plus_assign_null_operand_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatPlusAssignNullOperandBailsSliceViolation {
	void m(int n) {
		// append(null) picks no overload, so the rewrite would not compile
		var s = "";
		for (var i = 0; i < n; ++i)
			s += null;
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_prepend_char_array_operand_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatPrependCharArrayOperandBailsSliceViolation {
	void m(int n, char[] buf) {
		// a lone prepend becomes insert(0, buf), which binds insert(int, char[])
		var s = "";
		for (var i = 0; i < n; ++i)
			s = buf + s;
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_prepend_then_append_reads_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_this_qualified_read_of_bare_qualified_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
// imports: java.util.List
class InputJitInefficiencyStringConcatThisQualifiedReadOfBareQualifiedAccumulatorBailsSliceViolation {
	static class Holder {
		String value = "";
	}

	private final Holder holder = new Holder();

	void m(List<String> list) {
		for (var x : list) {
			holder.value = holder.value + x;
			// the same storage with `this.` put back: the other direction of the alias rule
			System.out.println(holder.value);
		}
	}
}
// === end ===

// === case: string_concat_tier2_do_while_array_lhs_mutated_index_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatTier2DoWhileArrayLhsMutatedIndexBailsSliceViolation {
	void m(String[] arr, int k) {
		do
			arr[k] = arr[k] + (++k);
		while (arr[k] != null);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_bare_read_of_this_qualified_accumulator_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatTier2DoWhileBareReadOfThisQualifiedAccumulatorBailsSliceViolation {
	private String f = "";

	void m() {
		// the rewrite never updates `f`, so a condition reading it would never stop
		do
			this.f = f + "y";
		while (f.length() < 5);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_block_comment_in_do_line_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_tier2_do_while_char_array_operand_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatTier2DoWhileCharArrayOperandBailsSliceViolation {
	void m(char[] buf) {
		// the tier-2 rewrite is its own function, so it needs the operand gate of its own
		var s = "";
		do
			s = s + buf;
		while (s.length() < 5);
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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

// === case: string_concat_tier2_do_while_nested_scope_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
class InputJitInefficiencyStringConcatTier2DoWhileNestedScopeBailsSliceViolation {
	String m(boolean flag) {
		var s = "";
		if (flag) {
			do
				s = s + "y";
			while (s.length() < 5);
		}
		return s;
	}
}
// === end ===

// === case: string_concat_tier2_do_while_reassigned_after_loop_bails ===
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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
// skip-reason: cannot rewrite the string concatenation in a loop: this shape is not supported
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