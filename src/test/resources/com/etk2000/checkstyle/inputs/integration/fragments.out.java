// === case: all_fixed_no_skip_reasons ===
class T {
	long x = 3000000000L;
	long y = 4000000000L;
}
// === end ===

// === case: annotation_own_line_blank ===
class T {
	@Deprecated
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_after_block_comment ===
class T {
	@Deprecated
	/* block */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_after_javadoc ===
class T {
	@Deprecated
	/** Javadoc. */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_after_line_comment ===
class T {
	@Deprecated
	// comment
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_after_multi_line_block_comment ===
class T {
	@Deprecated
	/*
	 * comment
	 */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_after_multi_line_block_comment_with_internal_blank ===
class T {
	@Deprecated
	/*
	 * comment
	 *
	 * more
	 */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_before_block_comment ===
class T {
	@Deprecated
	/* block */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_before_javadoc ===
class T {
	@Deprecated
	/** Javadoc. */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_before_line_comment ===
class T {
	@Deprecated
	// comment
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_before_multi_line_block_comment ===
class T {
	@Deprecated
	/*
	 * comment
	 */
	void method() {}
}
// === end ===

// === case: annotation_own_line_blank_multi_line ===
class T {
	@SuppressWarnings({
		"unchecked",
		"rawtypes"
	})
	void method() {}
}
// === end ===

// === case: annotation_own_line_embedded_after_final ===
class T {
	void f() {
		@Deprecated
		final int x;
	}
}
// === end ===

// === case: annotation_own_line_embedded_after_multiple_modifiers ===
class T {
	@Deprecated
	private final String field;
}
// === end ===

// === case: annotation_own_line_embedded_after_static_final ===
class T {
	@Deprecated
	static final int CONST = 1;
}
// === end ===

// === case: annotation_own_line_embedded_leading_and_embedded ===
class T {
	void f() {
		@Deprecated
		@Override
		final int x;
	}
}
// === end ===

// === case: annotation_own_line_reorder ===
class T {
	@Deprecated
	@Override
	void method() {}
}
// === end ===

// === case: annotation_own_line_split ===
class T {
	@Deprecated
	@Override
	void method() {}
}
// === end ===

// === case: annotation_same_line_inline_reorder ===
class T {
	void method(@Deprecated @Override String param) {}
}
// === end ===

// === case: annotation_same_line_join ===
class T {
	void method(
			@Deprecated String param
	) {}
}
// === end ===

// === case: annotation_syntax_empty_parens ===
class T {
	@Deprecated
	void method() {}
}
// === end ===

// === case: annotation_syntax_explicit_value ===
class T {
	@SuppressWarnings("unchecked")
	void method() {}
}
// === end ===

// === case: annotation_syntax_explicit_value_string_decoy ===
class T {
	@SuppressWarnings("value = x")
	void method() {}
}
// === end ===

// === case: array_trailing_comma ===
class T {
	int[] a = {1, 2};
}
// === end ===

// === case: array_type_style_c_style_field ===
class T {
	int[] x;
}
// === end ===

// === case: array_type_style_compound_local ===
class T {
	void m() {
		final int[][] x = {{1}};
		x[0][0] = 1;
	}
}
// === end ===

// === case: array_type_style_method_multi_param ===
class T {
	void m(int[] x, int y) {
		x[0] = y;
	}
}
// === end ===

// === case: array_type_style_method_parameter ===
class T {
	void m(int[] x) {
		x[0] = 1;
	}
}
// === end ===

// === case: array_type_style_method_return_type ===
class T {
	int[] m() {
		return null;
	}
}
// === end ===

// === case: array_type_style_mixed_java_and_c ===
class T {
	int[][] x;
}
// === end ===

// === case: array_type_style_record_component ===
class T {
	record R(int[] x) {}
}
// === end ===

// === case: blank_line_after_break ===
class T {
	void f(int x) {
		switch (x) {
			case 1:
				doSomething();
				break;

			case 2:
				break;

			default:
				break;
		}
	}
	void doSomething() {}
}
// === end ===

// === case: blank_line_after_break_fall_through ===
class T {
	void f(int x) {
		switch (x) {
			case 1:
			case 2:
				doSomething();
				break;

			case 3:
				break;
		}
	}
	void doSomething() {}
}
// === end ===

// === case: blank_line_after_class_brace ===
class T {
	int x;
}
// === end ===

// === case: blank_line_after_class_brace_combined_with_before_close ===
class T {
	int x;
}
// === end ===

// === case: blank_line_after_class_brace_multi_line ===
class T
		extends Base {
	int x;
}
// === end ===

// === case: blank_line_before_closing_brace ===
class T {
	int x;
}
// === end ===

// === case: blank_line_before_closing_brace_double ===
class T {
	int x;
}
// === end ===

// === case: blank_line_before_closing_brace_triple ===
class T {
	int x;
}
// === end ===

// === case: blank_line_between_single_cases ===
class T {
	void f(int x) {
		switch (x) {
			case 1:
				return;
			case 2:
				return;
		}
	}
}
// === end ===

// === case: blank_line_between_single_cases_multiple_blank_lines ===
class T {
	void f(int x) {
		switch (x) {
			case 1:
				return;
			case 2:
				return;
		}
	}
}
// === end ===

// === case: constructor_assign_alphabetical ===
class T {
	int alpha, beta;

	T(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}
// === end ===

// === case: constructor_assign_dependency_swap ===
class T {
	int alpha, beta;

	T(int alpha) {
		this.alpha = alpha;
		this.beta = this.alpha + 1;
	}
}
// === end ===

// === case: constructor_assign_multi_line_before_simple ===
class T {
	int alpha;
	Object beta;

	T(int alpha, Object beta) {
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

// === case: constructor_assign_var_before_simple ===
class T {
	int alpha, beta;

	T(int x) {
		this.beta = x;

		final var computed = x * 2;
		this.alpha = computed;
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_for_each_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_if ===
class T {
	void f(int x) {
		if (x > 0)
			--x;
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_while ===
class T {
	void f(int x) {
		while (x > 0)
			--x;
	}
}
// === end ===

// === case: control_flow_missing_braces_else ===
class T {
	void f(int x) {
		if (x > 0)
			--x;
		else {
			for (var i = 0; i < x; ++i)
				++x;
		}
	}
}
// === end ===

// === case: control_flow_missing_braces_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i) {
			if (i > 0)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: control_flow_missing_braces_for_each ===
import java.util.List;
class T {
	void f() {
		for (var item : List.of("a")) {
			if (item != null)
				System.out.println(item);
		}
	}
}
// === end ===

// === case: control_flow_missing_braces_if ===
class T {
	void f(int x) {
		if (x > 0) {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: control_flow_missing_braces_while ===
class T {
	void f(int x) {
		while (x > 0) {
			if (x > 5)
				--x;
		}
	}
}
// === end ===

// === case: control_flow_one_liner_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: control_flow_one_liner_for_array_fill ===
import java.util.Arrays;
class T {
	void f(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: control_flow_one_liner_for_each_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: control_flow_one_liner_if_else ===
class T {
	void f(int x) {
		if (x > 0)
			--x;
		else
			++x;
	}
}
// === end ===

// === case: control_flow_one_liner_while ===
class T {
	void f(int x) {
		while (x > 0)
			--x;
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_comment_on_brace_nested ===
class T {
	void f(int x, int y) {
		if (x > 0) { // guard
			if (y > 0)
				--x;
		}
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_else ===
class T {
	void f(int x) {
		if (x > 0)
			++x;
		else
			--x;
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_for_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_if ===
class T {
	void f(int x) {
		if (x > 0)
			--x;
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_while ===
class T {
	void f(int x) {
		while (x > 0)
			--x;
	}
}
// === end ===

// === case: do_while_assign_chained_rhs ===
class T {
	void f(java.util.List<String> list, int x) {
		do
			x = list.subList(0, 1).size();
		while (x > 0);
	}
}
// === end ===

// === case: do_while_assign_new_rhs ===
class T {
	void f(int x, Object o) {
		do
			o = new Object();
		while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_simple_body ===
class T {
	void f(int x) {
		do --x;
		while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_simple_body_compound_while ===
class T {
	void f(int x) {
		do --x;
		while (x > 0 && x < 100);
	}
}
// === end ===

// === case: do_while_braced_tier2 ===
class T {
	void f(int x) {
		do System.out.println(x);
		while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_tier3 ===
class T {
	void f(java.util.List<String> list) {
		do
			list.subList(0, 1).clear();
		while (!list.isEmpty());
	}
}
// === end ===

// === case: do_while_braced_tier3_complex_rhs ===
class T {
	void f(int x, int y) {
		do
			x += 5 * y;
		while (x < 100);
	}
}
// === end ===

// === case: do_while_braced_tier3_this_chained_call ===
class T {
	T helper() { return this; }
	T chain() { return this; }
	void f(int x) {
		do
			helper().chain();
		while (x > 0);
	}
}
// === end ===

// === case: do_while_missing_braces ===
class T {
	void f(int x) {
		do {
			if (x > 0)
				--x;
		} while (x > 0);
	}
}
// === end ===

// === case: do_while_own_line_simple_body ===
class T {
	void f(int x) {
		do --x;
		while (x > 0);
	}
}
// === end ===

// === case: do_while_own_line_tier2 ===
class T {
	void f(int x) {
		do System.out.println(x);
		while (x > 0);
	}
}
// === end ===

// === case: do_while_simple_body_one_liner_splits ===
class T {
	void f(int x) {
		do --x;
		while (x > 0);
	}
}
// === end ===

// === case: do_while_tier2_while_on_same_line ===
class T {
	void f(int x) {
		do System.out.println(x);
		while (x > 0);
	}
}
// === end ===

// === case: do_while_tier3_as_tier2 ===
class T {
	void f(java.util.List<String> list) {
		do
			list.subList(0, 1).clear();
		while (!list.isEmpty());
	}
}
// === end ===

// === case: do_while_tier3_one_liner ===
class T {
	void f(java.util.List<String> list) {
		do
			list.subList(0, 1).clear();
		while (!list.isEmpty());
	}
}
// === end ===

// === case: double_blank_lines ===
class T {
	int x;

	int y;
}
// === end ===

// === case: double_blank_lines_triple ===
class T {
	int x;

	int y;
}
// === end ===

// === case: enum_nested_two_tab_indent ===
class Outer {
	static class Inner {
		enum Nested {
			ALPHA,
			ZETA
		}
	}
}
// === end ===

// === case: enum_trailing_comma ===
enum Color {
	GREEN,
	RED
}
// === end ===

// === case: enum_trailing_semicolon ===
enum Semi {
	A,
	B
}
// === end ===

// === case: enum_trailing_semicolon_constant_body ===
enum SemiBody {
	X {
		@Override
		public String toString() {
			return "x";
		}
	}
}
// === end ===

// === case: enum_trailing_semicolon_deep_tab ===
class T {
	enum E {
		X
	}
}
// === end ===

// === case: enum_trailing_semicolon_delete_line ===
enum SemiEmpty {
}
// === end ===

// === case: enum_trailing_semicolon_delete_own_line ===
enum SemiOwn {
	X
}
// === end ===

// === case: enum_trailing_semicolon_inline ===
enum SemiInline { X }
// === end ===

// === case: enum_trailing_semicolon_with_comment ===
enum SemiComment {
	X // remark
}
// === end ===

// === case: explicit_initialization ===
class T {
	boolean b;
	int x;
	Object o;
}
// === end ===

// === case: explicit_initialization_comma_in_comment ===
class T {
	int x /* , */, y;
}
// === end ===

// === case: explicit_initialization_comment_preserving ===
class T {
	int x /* c */;
}
// === end ===

// === case: explicit_initialization_multi_declaration ===
class T {
	int a, b;
}
// === end ===

// === case: explicit_initialization_multi_declaration_both ===
class T {
	int a, b;
}
// === end ===

// === case: explicit_initialization_tab_comment ===
class T {
	int x; // note
}
// === end ===

// === case: field_consolidation_and_annotation_reorder_with_comment_between ===
class T {
	@Deprecated
	int alpha, beta;
	// trailing comment

	@A
	@B
	void f() {}
}
// === end ===

// === case: field_consolidation_annotated ===
class T {
	@Deprecated
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_annotated_multiple_own_line ===
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
class T {
	@CheckReturnValue
	@Nonnull
	String alpha, beta;
}
// === end ===

// === case: field_consolidation_both_c_style_array ===
class T {
	int[] alpha, beta;
}
// === end ===

// === case: field_consolidation_both_c_style_array_wrapping ===
class T {
	int[] aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: field_consolidation_c_style_curr_java_prev ===
class T {
	int[] alpha, beta;
}
// === end ===

// === case: field_consolidation_c_style_prev_java_curr ===
class T {
	int[] alpha, beta;
}
// === end ===

// === case: field_consolidation_comma_merge ===
class T {
	int alpha, beta, gamma;
}
// === end ===

// === case: field_consolidation_final ===
class T {
	final int alpha, beta;
	T(int a, int b) { alpha = a; beta = b; }
}
// === end ===

// === case: field_consolidation_generic_type ===
import java.util.List;
class T {
	List<String> names, words;
}
// === end ===

// === case: field_consolidation_multi_var_prev ===
class T {
	int a, b, c;
}
// === end ===

// === case: field_consolidation_protected ===
class T {
	protected int alpha, beta;
}
// === end ===

// === case: field_consolidation_simple ===
class T {
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_static ===
class T {
	static int global, shared;
}
// === end ===

// === case: field_consolidation_three_fields ===
class T {
	int a, b, c;
}
// === end ===

// === case: field_consolidation_wrapping_four_long_fields ===
class T {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			ccccccccccccccccccccccccccccccccccc, ddddddddddddddddddddddddddddddddddd;
}
// === end ===

// === case: field_consolidation_wrapping_three_fields ===
class T {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			ccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: field_consolidation_wrapping_two_long_fields ===
class T {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: field_sorting_annotation_consolidation ===
class T {
	@NonNull
	final String currencyCode, equityNumber, source, subAccount, subAccountName;
	@Nullable
	final String engName, engSymbol, exchange, hebName, hebSymbol, itemType, stockType;

	T() {
		this.currencyCode = null;
		this.engName = null;
		this.engSymbol = null;
		this.equityNumber = null;
		this.exchange = null;
		this.hebName = null;
		this.hebSymbol = null;
		this.itemType = null;
		this.source = null;
		this.stockType = null;
		this.subAccount = null;
		this.subAccountName = null;
	}
}
// === end ===

// === case: field_sorting_annotation_different_annotations ===
class T {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_sorting_annotation_order ===
class T {
	String plain;
	@Deprecated
	String annotated;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order ===
@interface TA {}
class T {
	List<String> plain;
	List<@TA String> annotated;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_fqn ===
@interface TA {}
@SuppressWarnings("PreferImport")
class T {
	java.util.Set<String> plain;
	java.util.Set<@TA String> annotated;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard ===
@interface TA {}
class T {
	List<? extends Number> plain;
	List<@TA ? extends Number> annotated;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard_bound ===
@interface TA {}
class T {
	List<? extends Number> plain;
	List<? extends @TA Number> annotated;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard_lower_bound ===
@interface TA {}
class T {
	List<? super Number> plain;
	List<? super @TA Number> annotated;
}
// === end ===

// === case: field_sorting_chunk_order ===
class T {
	final int finalWithValue = 1;

	int nonFinal;

	T() {
		this.nonFinal = 0;
	}
}
// === end ===

// === case: field_sorting_dependency_order ===
class T {
	static final int A = 0;
	static final int B = A + 1;
}
// === end ===

// === case: field_sorting_enum_inner_class ===
class T {
	enum E {
		ALPHA,
		BETA
	}
}
// === end ===

// === case: field_sorting_enum_reorder ===
enum T {
	ALPHA,
	BETA
}
// === end ===

// === case: field_sorting_enum_same_line ===
enum T {
	ALPHA,
	BETA
}
// === end ===

// === case: field_sorting_enum_same_line_and_reorder ===
enum T {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: field_sorting_enum_semicolon ===
enum T {
	ALPHA,
	BETA;
	int x;
}
// === end ===

// === case: field_sorting_enum_with_annotations ===
enum T {
	ALPHA,
	@Deprecated
	BETA
}
// === end ===

// === case: field_sorting_enum_with_args ===
enum T {
	APPLE("g"),
	CHERRY("r")
}
// === end ===

// === case: field_sorting_enum_with_bodies ===
enum T {
	ADD {
		int v() {
			return 0;
		}
	},
	SUB {
		int v() {
			return 1;
		}
	};
	abstract int v();
}
// === end ===

// === case: field_sorting_enum_with_trailing_comments ===
enum T {
	ALPHA, // a
	BETA // b
}
// === end ===

// === case: field_sorting_name_order ===
class T {
	final int a = 0;
	final int z = 1;
}
// === end ===

// === case: field_sorting_type_order ===
class T {
	final int count = 0;
	final String name = "x";
}
// === end ===

// === case: final_local_variable ===
class T {
	void f() {
		final var x = 5;
		final var y = "hello";
	}
}
// === end ===

// === case: final_local_variable_tab_indented ===
class T {
	void f() {
		if (true) {
			final var x = 5;
		}
	}
}
// === end ===

// === case: fix_lambda_param_remove_parens ===
import java.util.List;
class T {
	void f(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_remove_type ===
import java.util.List;
class T {
	void f(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_replace_type_with_var ===
import java.util.List;
@interface A {}
class T {
	void f(List<String> list) {
		list.forEach((@A var x) -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_replace_type_with_var_multi_param ===
import java.util.List;
@interface A {}
class T {
	void f(List<String> list) {
		list.sort((@A var x, var y) -> x.compareTo(y));
	}
}
// === end ===

// === case: fix_order_bottom_to_top ===
class T {
	int[] a = {1};
	int[] b = {2};
	int[] c = {3};
}
// === end ===

// === case: fixer_returns_null_for_duplicate_on_same_line ===
class T {
	void f() {
		final int x, y;
	}
}
// === end ===

// === case: jit_inefficiency_append_concat ===
class T {
	void f(StringBuilder sb, String v) {
		sb.append("key=").append(v);
	}
}
// === end ===

// === case: jit_inefficiency_boxed_constructor ===
class T {
	Integer value() {
		return Integer.valueOf(42);
	}
}
// === end ===

// === case: jit_inefficiency_empty_string_concat ===
class T {
	String f(int x) {
		return String.valueOf(x);
	}
}
// === end ===

// === case: jit_inefficiency_new_string ===
class T {
	String f() {
		return "hello";
	}
}
// === end ===

// === case: jit_inefficiency_string_buffer ===
class T {
	String f() {
		final var sb = new StringBuilder("hi");
		return sb.toString();
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_array_lhs ===
import java.util.List;
class T {
	String[] arr = new String[1];
	T(List<String> list) {
		arr[0] = "";
		final var sb = new StringBuilder();
		sb.append(arr[0]);
		for (var x : list)
			sb.append(x);
		arr[0] = sb.toString();
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_buried_in_if ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		sb.append(list.get(0));
		log("start");
		for (var i = 1; i < list.size(); ++i) {
			final var x = list.get(i);
			if (x != null && !x.isEmpty())
				sb.append(", ").append(x);
		}
		final var names = sb.toString();
		return names;
	}

	void log(String s) {}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_decl_with_gap ===
import java.util.List;
class T {
	String f(List<String> list, int seed) {
		final var sb = new StringBuilder();
		final var n = seed * 2;
		for (var x : list)
			sb.append(x);
		final var s = sb.toString();
		return s + n;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_explicit_chained ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list)
			sb.append(", ").append(x);
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_field_this ===
import java.util.List;
class T {
	String f;
	T(List<String> list, String f) {
		this.f = f;
		final var sb = new StringBuilder();
		sb.append(this.f);
		for (var x : list)
			sb.append(x);
		this.f = sb.toString();
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_for_loop ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list)
			sb.append(x);
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_mid_loop_read ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list) {
			if (sb.length() < 100)
				sb.append(x);
		}
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_non_empty_init ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		sb.append("prefix:");
		for (var x : list)
			sb.append(x);
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_reverse_form ===
import java.util.List;
class T {
	String f(List<String> list) {
		final var sb = new StringBuilder();
		for (var x : list)
			sb.insert(0, x);
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_tier2_do_while ===
class T {
	String f() {
		final var sb = new StringBuilder();
		do sb.append("y");
		while (sb.length() < 5);
		final var s = sb.toString();
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_to_array_sized ===
import java.util.List;
class T {
	String[] f(List<String> list) {
		return list.toArray(new String[0]);
	}
}
// === end ===

// === case: mixed_fix_and_skip_from_same_check ===
class T {
	enum E {
		A,
		B
	}
	int a, b;
}
// === end ===

// === case: multiline_close_move_general ===
class T {
	void m() {
		foo(1, 2);
	}
}
// === end ===

// === case: multiline_close_move_method_def ===
class T {
	void m(int a, int b) {
	}
}
// === end ===

// === case: multiline_close_move_tab_indented ===
class T {
	class Inner {
		void m() {
			foo(1, 2);
		}
	}
}
// === end ===

// === case: multiline_close_pullup_getstring_context_local ===
class T {
	void m() {
		final var ctx = requireContext();
		foo(ctx.getString(1));
	}
}
// === end ===

// === case: multiline_close_pullup_lambda ===
class T {
	void m() {
		foo(x -> {
			bar(x);
		});
	}
}
// === end ===

// === case: multiline_close_pullup_ternary ===
class T {
	void m() {
		foo(true ? "a" : "b");
	}
}
// === end ===

// === case: multiline_open_move_general ===
class T {
	void m() {
		foo(1, 2);
	}
}
// === end ===

// === case: multiline_open_move_tab_indented ===
class T {
	class Inner {
		void m() {
			foo(1, 2);
		}
	}
}
// === end ===

// === case: multiline_put_collapsible_bare ===
class T {
	void m() {
		new JSONObject().put("k", 1);
	}
}
// === end ===

// === case: multiline_put_collapsible_nested_converges ===
class T {
	Map<String, Object> cache;

	void m() {
		cache.put("View", new JSONObject().put("Account", new JSONObject().put("id", 1)));
	}
}
// === end ===

// === case: multiline_put_collapsible_prefixed ===
class T {
	Map<String, Object> cache;

	void m() {
		cache.put("k", new JSONObject().put("a", 1));
	}
}
// === end ===

// === case: multiline_put_collapsible_tab_indented ===
class T {
	class Inner {
		void m() {
			new JSONObject().put("k", 1);
		}
	}
}
// === end ===

// === case: multiline_put_collapsible_trailing_comment_last_line ===
class T {
	void m() {
		new JSONObject().put("k", 1); // note
	}
}
// === end ===

// === case: multiple_checks_skip_reasons ===
class T {
	int x;
	void f(boolean a, boolean b) {
		if (a
				&& b) {
			System.out.println(a);
		}
	}
}
// === end ===

// === case: multiple_violations_same_file ===
class T {
	int[] a = {1};
	long x = 100;
}
// === end ===

// === case: no_final_parameters_catch ===
class T {
	void f() {
		try {
			System.out.println();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: no_final_parameters_constructor ===
class T {
	T(int x) {}
}
// === end ===

// === case: no_final_parameters_for_each ===
import java.util.List;
class T {
	void f(List<String> list) {
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: no_final_parameters_method ===
class T {
	void f(int x, String y) {}
}
// === end ===

// === case: no_final_parameters_second_param ===
class T {
	void f(int x, String y) {}
}
// === end ===

// === case: postfix_decrement ===
class T {
	void run() {
		var i = 5;
		--i;
	}
}
// === end ===

// === case: postfix_increment ===
class T {
	void run() {
		var i = 0;
		++i;
	}
}
// === end ===

// === case: postfix_increment_for_loop ===
class T {
	void run() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: prefer_bulk_operation_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill ===
import java.util.Arrays;
class T {
	void f(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_braced ===
import java.util.Arrays;
class T {
	void f(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_source_name_starts_with_length ===
import java.util.Arrays;
class T {
	void f(int[] lengthValues) {
		Arrays.fill(lengthValues, 0);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_unary_plus_value_contains_bracket ===
import java.util.Arrays;
class T {
	void f(int[] arr, int[] other) {
		Arrays.fill(arr, +other[0]);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_value_contains_bracket ===
import java.util.Arrays;
class T {
	void f(int[] arr, int[] a, int[] b) {
		Arrays.fill(arr, -a[b[0]]);
	}
}
// === end ===

// === case: prefer_bulk_operation_entry_set_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> target, Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_entry_set_put_all_braced ===
import java.util.Map;
class T {
	void f(Map<String, String> target, Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_add_all ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_add_all ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_block_comment_wrong_target ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> real) {
		real.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_preserves_leading_if_statement ===
import java.util.Map;
class T {
	void f(boolean flag, Map<String, String> source, Map<String, String> target) {
		if (flag)
			target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_add ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_multi_line ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_put ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_indexed_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_indexed_add_all_braced ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: prefer_bulk_operation_system_arraycopy ===
class T {
	void f(int[] dst, int[] src) {
		System.arraycopy(src, 0, dst, 0, src.length);
	}
}
// === end ===

// === case: prefer_bulk_operation_system_arraycopy_braced ===
class T {
	void f(int[] dst, int[] src) {
		System.arraycopy(src, 0, dst, 0, src.length);
	}
}
// === end ===

// === case: prefer_collection_interface_multi_same_line ===
import java.util.List;
import java.util.Map;
final class T {
	void f(List<String> a, Map<String, Integer> b) {}
}
// === end ===

// === case: prefer_collection_interface_param ===
import java.util.Set;
final class T {
	void f(Set<String> s) {}
}
// === end ===

// === case: prefer_collection_interface_return ===
import java.util.ArrayList;
import java.util.List;
class T {
	private List<String> f() {
		return new ArrayList<>();
	}
}
// === end ===

// === case: prefer_collection_interface_return_import_already_present ===
import java.util.ArrayList;
import java.util.List;
class T {
	private List<String> f() {
		return new ArrayList<>();
	}
}
// === end ===

// === case: prefer_direct_boolean_return_braced_both_branches ===
class T {
	boolean f(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_braced_then_trailing ===
class T {
	boolean f(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_chain_second_fires ===
class T {
	boolean f(int x, int y) {
		if (x > 0)
			++x;
		return y > 0;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_combine_and ===
class T {
	boolean f(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: prefer_direct_boolean_return_extract_postfix_inc ===
class T {
	boolean f(int i) {
		++i;
		return true;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_inline_forward ===
class T {
	boolean f(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_method_call_condition ===
class T {
	boolean f(String s) {
		return !s.isEmpty();
	}
}
// === end ===

// === case: prefer_direct_boolean_return_next_line_with_else ===
class T {
	boolean f(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_not_ident_double_neg ===
class T {
	boolean f(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_same_literal ===
class T {
	boolean f(boolean flag) {
		return true;
	}
}
// === end ===

// === case: prefer_do_while_unbraced_body ===
class T {
	void f(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: prefer_exact_assertion_false_instance_of ===
import static org.junit.jupiter.api.Assertions.assertNotInstanceOf;
class T {
	void f(Object o) {
		assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: prefer_exact_assertion_junit5_message_last ===
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
class T {
	void f(Object o) {
		assertInstanceOf(String.class, o, "should be a string");
	}
}
// === end ===

// === case: prefer_exact_assertion_negated ===
import static org.junit.jupiter.api.Assertions.assertNotInstanceOf;
class T {
	void f(Object o) {
		assertNotInstanceOf(String.class, o);
	}
}
// === end ===

// === case: prefer_exact_assertion_plain_negation ===
import static org.junit.jupiter.api.Assertions.assertFalse;
class T {
	void f(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: prefer_exact_assertion_qualified_call ===
import org.junit.jupiter.api.Assertions;
class T {
	void f(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: prefer_exact_assertion_qualified_junit4_negation_fallback ===
class T {
	void f(Object o) {
		org.junit.Assert.assertFalse(o instanceof String);
	}
}
// === end ===

// === case: prefer_exact_assertion_true_instance_of ===
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
class T {
	void f(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: prefer_import_reflection_same_package ===
package com.etk2000.checkstyle.gradle.fix;
class T {
	PreferImportFixer fixer;
}
// === end ===

// === case: prefer_import_used_via_fqn_keeps_import ===
import java.util.Map;
class T {
	Map<String, Integer> field;
}
// === end ===

// === case: prefer_math_method_abs ===
class T {
	int f(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: prefer_math_method_clamp ===
class T {
	int f(int v, int lo, int hi) {
		return Math.clamp(v, lo, hi);
	}
}
// === end ===

// === case: prefer_math_method_if_compound_assign ===
class T {
	int f(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: prefer_math_method_if_decl_assign_return ===
class T {
	int f(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: prefer_math_method_if_else_return ===
class T {
	int f(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: prefer_math_method_if_init_overwrite ===
class T {
	int f(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: prefer_math_method_if_plain_assign_bare ===
class T {
	void f(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: prefer_math_method_if_trailing_return ===
class T {
	int f(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: prefer_math_method_max ===
class T {
	int f(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: prefer_math_method_max_pre_decrement ===
class T {
	int f(int a, int b) {
		return Math.max(--a, b);
	}
}
// === end ===

// === case: prefer_math_method_min ===
class T {
	int f(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: prefer_specific_api_arrays_as_list ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a", "b");
	}
}
// === end ===

// === case: prefer_specific_api_arrays_as_list_removes_unused_import ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a", "b");
	}
}
// === end ===

// === case: prefer_specific_api_assert_junit4 ===
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void run() {
		assertTrue(1 == 1);
		assertNull(new Object());
		assertNull("msg", new Object());
		assertTrue("msg", 1 == 1);
	}
}
// === end ===

// === case: prefer_specific_api_assert_junit5 ===
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void run() {
		assertTrue(1 == 1);
		assertNull(new Object());
		assertNull(new Object(), "msg");
		assertTrue(1 == 1, "msg");
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a");
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_adds_import ===
import java.util.List;
class T {
	Object run() {
		return List.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_import_already_present ===
import java.util.List;
class T {
	List<String> run() {
		return List.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_import_between_groups ===
import java.util.List;

import javax.annotation.Nonnull;
class T {
	@Nonnull
	Object run() {
		return List.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_multiple_imports ===
import java.util.List;
import java.util.Map;
import java.util.Set;
class T {
	Object a() {
		return List.of();
	}
	Object b() {
		return Map.of();
	}
	Object c() {
		return Set.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_multiple_imports_with_group_separator ===
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
class T {
	@Nonnull
	Object a() {
		return List.of();
	}
	@Nonnull
	Object b() {
		return Set.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_partial_import ===
import java.util.List;
import java.util.Set;
class T {
	List<String> a() {
		return List.of();
	}
	Object b() {
		return Set.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_removes_both_collections_calls ===
import java.util.List;
class T {
	List<String> a() {
		return List.of("a");
	}
	void b(List<String> list) {
		list.sort(null);
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_removes_unused_import ===
import java.util.List;
class T {
	Object run() {
		return List.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_retains_collections_with_surviving_call ===
import java.util.Collections;
import java.util.List;
class T {
	List<String> a() {
		return List.of("a");
	}
	void b(List<String> list) {
		Collections.reverse(list);
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_wildcard_import_not_triggered ===
import java.util.*;
class T {
	Object run() {
		return List.of();
	}
}
// === end ===

// === case: prefer_specific_api_collections_singleton_list_removes_unused_import ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a");
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_no_comparator ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.sort(null);
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_removes_unused_import ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.sort(null);
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_with_comparator ===
import java.util.Comparator;
import java.util.List;
class T {
	void run(List<String> list) {
		list.sort(Comparator.naturalOrder());
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_with_comparator_removes_unused_import ===
import java.util.Comparator;
import java.util.List;
class T {
	void run(List<String> list) {
		list.sort(Comparator.naturalOrder());
	}
}
// === end ===

// === case: prefer_specific_api_collections_unmodifiable_as_list ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a", "b");
	}
}
// === end ===

// === case: prefer_specific_api_equals_empty ===
class T {
	void run(String s) {
		if (s.isEmpty())
			return;
	}
}
// === end ===

// === case: prefer_specific_api_get_first ===
import java.util.List;
class T {
	String run(List<String> list) {
		return list.getFirst();
	}
}
// === end ===

// === case: prefer_specific_api_index_of_char ===
class T {
	int f(String s) {
		return s.indexOf('x');
	}
}
// === end ===

// === case: prefer_specific_api_length_is_empty ===
class T {
	boolean run(String s) {
		return s.isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_length_is_empty_negated ===
class T {
	void run(String s) {
		if (!s.isEmpty())
			return;
	}
}
// === end ===

// === case: prefer_specific_api_map_chain ===
import java.util.Map;
class T {
	void run(Map<String, String> map) {
		if (map.containsKey("k"))
			return;
	}
}
// === end ===

// === case: prefer_specific_api_no_imports_no_trigger ===
class T {
	boolean a(String s) {
		return s.isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_qualified_assert ===
class T {
	void run(boolean flag) {
		org.junit.Assert.assertTrue(flag);
	}
}
// === end ===

// === case: prefer_specific_api_redundant_import_plus_usage_rewrite ===
import java.util.List;
class T {
	List<String> run() {
		return List.of("a");
	}
}
// === end ===

// === case: prefer_specific_api_remove_first ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.removeFirst();
	}
}
// === end ===

// === case: prefer_specific_api_replace_all ===
class T {
	String run(String s) {
		return s.replace("foo", "bar");
	}
}
// === end ===

// === case: prefer_specific_api_size_is_empty ===
import java.util.List;
class T {
	boolean run(List<String> list) {
		return list.isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_size_is_empty_reversed ===
import java.util.List;
class T {
	void run(List<String> list) {
		if (!list.isEmpty())
			return;
	}
}
// === end ===

// === case: prefer_specific_api_stream_count ===
import java.util.List;
class T {
	long run(List<String> list) {
		return list.size();
	}
}
// === end ===

// === case: prefer_specific_api_stream_find_first_is_present ===
import java.util.List;
class T {
	boolean run(List<String> list) {
		return !list.isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_stream_for_each ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.forEach(System.out::println);
	}
}
// === end ===

// === case: prefer_specific_api_string_format ===
class T {
	String run(String name) {
		return "Hello %s".formatted(name);
	}
}
// === end ===

// === case: prefer_specific_api_string_format_single_arg ===
class T {
	String run() {
		return "literal";
	}
}
// === end ===

// === case: prefer_specific_api_strip_is_blank ===
class T {
	boolean run(String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_strip_is_blank_negated ===
class T {
	boolean run(String s) {
		return !s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_strip_length_less_than_one ===
class T {
	boolean run(String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_to_array_new_zero ===
import java.util.List;
class T {
	String[] run(List<String> list) {
		return list.toArray(String[]::new);
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank ===
class T {
	boolean run(String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank_negated ===
class T {
	boolean run(String s) {
		return !s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank_reversed ===
class T {
	boolean run(String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: prefer_specific_api_trim_length_less_than_one ===
class T {
	boolean run(String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: prefer_standard_charsets ===
import java.nio.charset.StandardCharsets;
class T {
	byte[] run(String s) throws Exception {
		return s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: prefer_standard_charsets_adds_regular_after_existing_static ===
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
class T {
	byte[] run(String s) throws Exception {
		return requireNonNull(s).getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: prefer_standard_charsets_constructor_type_args ===
import java.nio.charset.StandardCharsets;
class T {
	String run(byte[] data) throws Exception {
		return new <String>String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: prefer_standard_charsets_import_already_present ===
import java.nio.charset.StandardCharsets;

class T {
	byte[] run(String s) throws Exception {
		return s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: prefer_static_import_chained_calls ===
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

import java.util.List;

class T {
	List<String> f(List<String> list, String p, String s) {
		return list.stream().filter(not(requireNonNull(p)::startsWith)).filter(not(requireNonNull(s)::endsWith)).toList();
	}
}
// === end ===

// === case: prefer_static_import_collectors_to_set ===
import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.Stream;

class T {
	Set<String> a(Stream<String> s) {
		return s.collect(toSet());
	}
	Set<String> b(Stream<String> s) {
		return s.collect(toSet());
	}
}
// === end ===

// === case: prefer_static_import_constant_alias_alone_in_chunk ===
import static foo.Foo.X;

class T {
	private int a;

	private int b;

	int f() {
		return X + a + b;
	}
}
// === end ===

// === case: prefer_static_import_constant_alias_inside_field_chunk ===
import static foo.Foo.X;

import foo.Foo;

class T {
	private static final int A = 1;
	private static final int Z = 2;

	int f() {
		return A + X + Z + Foo.OTHER;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_blank_final_auto_fix ===
import static foo.Foo.X;

class T {
	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_fqn_lhs_auto_fix ===
package x;

import static foo.Foo.X;

class T {
	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_qualified_lhs_auto_fix ===
import static foo.Foo.X;

class T {
	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_same_line_decl_and_cinit_auto_fix ===
import static foo.Foo.X;

class T {   int f() { return X; } }
// === end ===

// === case: prefer_static_import_constant_import_becomes_unused_after_fix ===
import static foo.Foo.X;

class T {
	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_line_alias ===
import static foo.LongClassName.LONG_CONSTANT_NAME;

class T {
	int f() {
		return LONG_CONSTANT_NAME;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_annotation_preserved ===
import static foo.Foo.X;
import static foo.Foo.Y;

class T {
	int f() {
		return X + Y;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_single_line ===
import static foo.Foo.X;
import static foo.Foo.Y;

class T {
	int f() {
		return X + Y;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_string_literal_sibling_preserved ===
import static foo.Foo.X;

class T {
	private static final Object Y = "hello";

	Object f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_objects_require_non_null ===
import static java.util.Objects.requireNonNull;

class T {
	Object f(Object a, Object b) {
		final var x = requireNonNull(a);
		final var y = requireNonNull(b);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: prefer_static_import_objects_require_non_null_removes_unused_import ===
import static java.util.Objects.requireNonNull;

class T {
	Object f(Object a, Object b) {
		final var x = requireNonNull(a);
		final var y = requireNonNull(b);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: prefer_static_import_predicate_not ===
import static java.util.function.Predicate.not;

import java.util.List;

class T {
	List<String> f(List<String> list) {
		return list.stream().filter(not(String::isEmpty)).filter(not(String::isBlank)).toList();
	}
}
// === end ===

// === case: prefer_var_diamond ===
import java.util.ArrayList;
class T {
	void f() {
		final var l = new ArrayList<>();
	}
}
// === end ===

// === case: prefer_var_diamond_anonymous_class ===
import java.util.Comparator;
class T {
	void f() {
		final var cmp = new Comparator<>() {
			@Override
			public int compare(Object a, Object b) {
				return 0;
			}
		};
	}
}
// === end ===

// === case: prefer_var_diamond_constructor_args ===
import java.util.ArrayList;
class T {
	void f() {
		final var l = new ArrayList<Object>(16);
	}
}
// === end ===

// === case: prefer_var_diamond_fq_constructor_name ===
class T {
	void f() {
		final var l = new java.util.ArrayList<>();
	}
}
// === end ===

// === case: prefer_var_diamond_fqn ===
import java.util.ArrayList;
class T {
	void f() {
		final var l = new ArrayList<>();
	}
}
// === end ===

// === case: prefer_var_diamond_mixed_qualified_and_bare ===
import java.util.HashMap;
class T {
	void f() {
		final var m = new HashMap<>();
	}
}
// === end ===

// === case: prefer_var_diamond_multiple_args ===
import java.util.HashMap;
class T {
	void f() {
		final var m = new HashMap<>();
	}
}
// === end ===

// === case: prefer_var_explicit_array_init ===
class T {
	void f() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: prefer_var_explicit_array_init_method_call_arg ===
class T {
	void f() {
		final var result = String.join(",", "a", "b");
	}
}
// === end ===

// === case: prefer_var_explicit_array_init_typed ===
class T {
	void f() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: prefer_var_final_local_interaction ===
class T {
	void f() {
		final var x = 5;
	}
}
// === end ===

// === case: prefer_var_for_each ===
import java.util.List;
class T {
	void f() {
		for (var item : List.of("a"))
			System.out.println(item);
	}
}
// === end ===

// === case: prefer_var_for_each_annotation_prev_line ===
import java.util.List;
import javax.annotation.Nonnull;
class T {
	void f() {
		for (@Nonnull var item : List.of("a"))
			System.out.println(item);
	}
}
// === end ===

// === case: prefer_var_for_init ===
class T {
	void f() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: prefer_var_generic_type ===
import java.util.List;
class T {
	void f() {
		final List<String> l = List.of();
	}
}
// === end ===

// === case: prefer_var_local_string ===
class T {
	void f() {
		final var s = "hi";
	}
}
// === end ===

// === case: prefer_var_local_with_final ===
class T {
	void f() {
		final var x = 5;
	}
}
// === end ===

// === case: prefer_var_tab_indented ===
class T {
	void f() {
			final var x = 5;
	}
}
// === end ===

// === case: prefer_var_try_with_resources ===
import java.io.ByteArrayInputStream;
class T {
	void f() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			in.read();
		}
	}
}
// === end ===

// === case: record_formatting_brace_newline ===
record R(int a) {}
// === end ===

// === case: record_formatting_component_collapse_mixed ===
record R(int a, int b) {}
// === end ===

// === case: record_formatting_component_expand_wide_line ===
record WideRecord(
		int aaaaaaaaaa,
		int bbbbbbbbbb,
		int cccccccccc,
		int dddddddddd,
		int eeeeeeeeee,
		int ffffffffff,
		int gggggggggg,
		int hhhhhhhhhh
) {}
// === end ===

// === case: record_formatting_empty_body_split ===
record R(int a) {}
// === end ===

// === case: record_formatting_implements_multi_line ===
interface Foo {}
record R(int a) implements
		Foo {}
// === end ===

// === case: record_formatting_implements_no_space ===
interface Foo {}
record R(int a) implements Foo {}
// === end ===

// === case: record_formatting_no_space_before_brace ===
record R(int a) {}
// === end ===

// === case: record_formatting_non_empty_body_same_line ===
record R(int a) {
	int b() { return a; }
}
// === end ===

// === case: record_formatting_tab_before_brace ===
record R(int a) {}
// === end ===

// === case: record_formatting_two_spaces_before_brace ===
record R(int a) {}
// === end ===

// === case: redundant_array_creation ===
import java.util.ArrayList;
import java.util.Collections;

class T {
	void f() {
		Collections.addAll(new ArrayList<>(), "a", "b");
	}
}
// === end ===

// === case: redundant_array_creation_constructor ===
class T {
	void f() {
		new ProcessBuilder("cmd", "arg");
	}
}
// === end ===

// === case: redundant_array_creation_empty_array ===
import java.util.ArrayList;
import java.util.Collections;

class T {
	void f() {
		Collections.addAll(new ArrayList<>());
	}
}
// === end ===

// === case: redundant_array_creation_string_join ===
class T {
	void f() {
		String.join(",", "a", "b");
	}
}
// === end ===

// === case: redundant_cast_bare_wrap ===
class T {
	String f(String s) {
		return s;
	}
}
// === end ===

// === case: redundant_cast_null_assignment ===
class T {
	void f() {
		final String s = null;
	}
}
// === end ===

// === case: redundant_cast_receiver_paren ===
class T {
	int f(String s) {
		return s.length();
	}
}
// === end ===

// === case: redundant_cast_receiver_paren_cross_line ===
class T {
	int f(String s) {
		return
s.length();
	}
}
// === end ===

// === case: redundant_cast_text_block_prior_context ===
class T {
	int f(String s) {
		final var x = """
/*
""";
		return s.length();
	}
}
// === end ===

// === case: redundant_cast_widening ===
class T {
	void f(int x) {
		final var y = x;
	}
}
// === end ===

// === case: redundant_equality_branch_assign_bare_collapse ===
class T {
	void f(int r, int a, int b) {
		r = b;
		System.out.println(r);
	}
}
// === end ===

// === case: redundant_equality_branch_assign_with_decl_and_return ===
class T {
	int f(int a, int b) {
		return b;
	}
}
// === end ===

// === case: redundant_equality_branch_not_equal ===
class T {
	int f(int a, int b) {
		return a;
	}
}
// === end ===

// === case: redundant_equality_branch_trailing_return ===
class T {
	int f(int a, int b) {
		return b;
	}
}
// === end ===

// === case: redundant_import ===
class T {
	String s;
}
// === end ===

// === case: redundant_import_contiguous_suppresses_duplicate ===
import java.util.List;

class T {
	List<String> s;
}
// === end ===

// === case: redundant_import_orphaned_suppresses_duplicate ===
import java.io.File;

import javax.annotation.Nonnull;

class T {
	@Nonnull
	File f;
	String s;
}
// === end ===

// === case: redundant_modifier ===
interface T {
	void method();
}
// === end ===

// === case: redundant_modifier_private_enum_constructor ===
enum Color {
	RED(1);

	Color(int code) {
	}
}
// === end ===

// === case: redundant_modifier_static_interface_field ===
interface T {
	int VALUE = 5;
}
// === end ===

// === case: redundant_numeric_suffix ===
class T {
	double d = 1.0;
	long x = 100;
}
// === end ===

// === case: redundant_numeric_suffix_hex_and_binary_and_float ===
class T {
	float b = 100;
	long a = 0xFF;
	long c = 0b1010;
}
// === end ===

// === case: super_call ===
class Child extends Object {
	Child() {
	}
}
// === end ===

// === case: super_call_tab_indented ===
class Outer {
	class Inner extends Object {
		Inner() {
		}
	}
}
// === end ===

// === case: trailing_newline_double ===
class T {}
// === end ===

// === case: trailing_newline_plus_other ===
class T {int x = 5;}
// === end ===

// === case: trailing_newline_plus_whitespace ===
class T {}
// === end ===

// === case: trailing_newline_real_multiline ===
class Foo {
	int value() {
		return 42;
	}
}
// === end ===

// === case: trailing_newline_single ===
class T {}
// === end ===

// === case: trailing_newline_tab_indented ===
class T {
	int x;
}
// === end ===

// === case: trailing_whitespace ===
class T {
	int x;
}
// === end ===

// === case: trailing_whitespace_tab_only ===
class T {
	int x;
}
// === end ===

// === case: unnecessary_this ===
class T {
	int value;
	int get() {
		return value;
	}
}
// === end ===

// === case: unnecessary_this_chained ===
class T {
	String value;
	int get() {
		return value.length();
	}
}
// === end ===

// === case: unused_import ===

class T {
}
// === end ===

// === case: unused_import_orphaned_in_group_removes_blank_line ===
package p;

class T {
}
// === end ===

// === case: unused_import_orphaned_middle_group_removes_blank_line ===
import java.io.File;

import javax.annotation.Nonnull;

class T {
	@Nonnull
	File f;
}
// === end ===

// === case: upper_ell ===
class T {
	long x = 3000000000L;
}
// === end ===

// === case: upper_ell_hex ===
class T {
	long x = 0xB00000000L;
}
// === end ===