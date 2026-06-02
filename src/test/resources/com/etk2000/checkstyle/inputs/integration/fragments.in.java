// === case: all_fixed_no_skip_reasons ===
class T {
	long x = 3000000000l;
	long y = 4000000000l;
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
		final @Deprecated int x;
	}
}
// === end ===

// === case: annotation_own_line_embedded_after_multiple_modifiers ===
class T {
	private final @Deprecated String field;
}
// === end ===

// === case: annotation_own_line_embedded_after_static_final ===
class T {
	static final @Deprecated int CONST = 1;
}
// === end ===

// === case: annotation_own_line_embedded_leading_and_embedded ===
class T {
	void f() {
		@Override final @Deprecated int x;
	}
}
// === end ===

// === case: annotation_own_line_reorder ===
class T {
	@Override
	@Deprecated
	void method() {}
}
// === end ===

// === case: annotation_own_line_split ===
class T {
	@Override @Deprecated void method() {}
}
// === end ===

// === case: annotation_same_line_inline_reorder ===
class T {
	void method(@Override @Deprecated String param) {}
}
// === end ===

// === case: annotation_same_line_join ===
class T {
	void method(
			@Deprecated
			String param
	) {}
}
// === end ===

// === case: annotation_syntax_empty_parens ===
class T {
	@Deprecated()
	void method() {}
}
// === end ===

// === case: annotation_syntax_explicit_value ===
class T {
	@SuppressWarnings(value = "unchecked")
	void method() {}
}
// === end ===

// === case: annotation_syntax_explicit_value_string_decoy ===
class T {
	@SuppressWarnings(value = "value = x")
	void method() {}
}
// === end ===

// === case: array_trailing_comma ===
class T {
	int[] a = {1, 2,};
}
// === end ===

// === case: array_type_style_c_style_field ===
class T {
	int x[];
}
// === end ===

// === case: array_type_style_compound_local ===
class T {
	void m() {
		final int x[][] = {{1}};
		x[0][0] = 1;
	}
}
// === end ===

// === case: array_type_style_method_multi_param ===
class T {
	void m(int x[], int y) {
		x[0] = y;
	}
}
// === end ===

// === case: array_type_style_method_parameter ===
class T {
	void m(int x[]) {
		x[0] = 1;
	}
}
// === end ===

// === case: array_type_style_method_return_type ===
class T {
	int m()[] {
		return null;
	}
}
// === end ===

// === case: array_type_style_mixed_java_and_c ===
class T {
	int[] x[];
}
// === end ===

// === case: array_type_style_record_component ===
class T {
	record R(int x[]) {}
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
		this.beta = beta;
		this.alpha = alpha;
	}
}
// === end ===

// === case: constructor_assign_dependency_swap ===
class T {
	int alpha, beta;

	T(int alpha) {
		this.beta = this.alpha + 1;
		this.alpha = alpha;
	}
}
// === end ===

// === case: constructor_assign_multi_line_before_simple ===
class T {
	int alpha;
	Object beta;

	T(int alpha, Object beta) {
		this.beta = new Object() {
			@Override
			public String toString() {
				return beta.toString();
			}
		};
		this.alpha = alpha;
	}
}
// === end ===

// === case: constructor_assign_var_before_simple ===
class T {
	int alpha, beta;

	T(int x) {
		final var computed = x * 2;
		this.alpha = computed;
		this.beta = x;
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i)
		{
			System.out.println(i);
		}
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_for_each_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var item : source)
		{
			target.add(item);
		}
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_if ===
class T {
	void f(int x) {
		if (x > 0)
		{
			--x;
		}
	}
}
// === end ===

// === case: control_flow_brace_on_own_line_while ===
class T {
	void f(int x) {
		while (x > 0)
		{
			--x;
		}
	}
}
// === end ===

// === case: control_flow_missing_braces_else ===
class T {
	void f(int x) {
		if (x > 0)
			--x;
		else
			for (var i = 0; i < x; ++i)
				++x;
	}
}
// === end ===

// === case: control_flow_missing_braces_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i)
			if (i > 0)
				System.out.println(i);
	}
}
// === end ===

// === case: control_flow_missing_braces_for_each ===
import java.util.List;
class T {
	void f() {
		for (var item : List.of("a"))
			if (item != null)
				System.out.println(item);
	}
}
// === end ===

// === case: control_flow_missing_braces_if ===
class T {
	void f(int x) {
		if (x > 0)
			for (var i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: control_flow_missing_braces_while ===
class T {
	void f(int x) {
		while (x > 0)
			if (x > 5)
				--x;
	}
}
// === end ===

// === case: control_flow_one_liner_for ===
class T {
	void f(int x) {
		for (var i = 0; i < x; ++i) System.out.println(i);
	}
}
// === end ===

// === case: control_flow_one_liner_for_array_fill ===
import java.util.Arrays;
class T {
	void f(int[] arr) {
		for (var i = 0; i < arr.length; ++i) arr[i] = 0;
	}
}
// === end ===

// === case: control_flow_one_liner_for_each_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var item : source) target.add(item);
	}
}
// === end ===

// === case: control_flow_one_liner_if_else ===
class T {
	void f(int x) {
		if (x > 0) --x; else ++x;
	}
}
// === end ===

// === case: control_flow_one_liner_while ===
class T {
	void f(int x) {
		while (x > 0) --x;
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_comment_on_brace_nested ===
class T {
	void f(int x, int y) {
		if (x > 0) { // guard
			if (y > 0) --x;
		}
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_else ===
class T {
	void f(int x) {
		if (x > 0)
			++x;
		else {
			--x;
		}
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_for_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var item : source) {
			target.add(item);
		}
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_if ===
class T {
	void f(int x) {
		if (x > 0) {
			--x;
		}
	}
}
// === end ===

// === case: control_flow_unnecessary_braces_while ===
class T {
	void f(int x) {
		while (x > 0) {
			--x;
		}
	}
}
// === end ===

// === case: do_execute_charset_for_name ===
import java.nio.charset.Charset;
class T {
	Charset c = Charset.forName("UTF-8");
}
// === end ===

// === case: do_while_assign_chained_rhs ===
class T {
	void f(java.util.List<String> list, int x) {
		do x = list.subList(0, 1).size();
		while (x > 0);
	}
}
// === end ===

// === case: do_while_assign_new_rhs ===
class T {
	void f(int x, Object o) {
		do o = new Object();
		while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_simple_body ===
class T {
	void f(int x) {
		do {
			--x;
		} while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_simple_body_compound_while ===
class T {
	void f(int x) {
		do {
			--x;
		} while (x > 0 && x < 100);
	}
}
// === end ===

// === case: do_while_braced_tier2 ===
class T {
	void f(int x) {
		do {
			System.out.println(x);
		} while (x > 0);
	}
}
// === end ===

// === case: do_while_braced_tier3 ===
class T {
	void f(java.util.List<String> list) {
		do {
			list.subList(0, 1).clear();
		} while (!list.isEmpty());
	}
}
// === end ===

// === case: do_while_braced_tier3_complex_rhs ===
class T {
	void f(int x, int y) {
		do {
			x += 5 * y;
		} while (x < 100);
	}
}
// === end ===

// === case: do_while_braced_tier3_this_chained_call ===
class T {
	T helper() { return this; }
	T chain() { return this; }
	void f(int x) {
		do {
			this.helper().chain();
		} while (x > 0);
	}
}
// === end ===

// === case: do_while_missing_braces ===
class T {
	void f(int x) {
		do
			if (x > 0)
				--x;
		while (x > 0);
	}
}
// === end ===

// === case: do_while_own_line_simple_body ===
class T {
	void f(int x) {
		do
			--x;
		while (x > 0);
	}
}
// === end ===

// === case: do_while_own_line_tier2 ===
class T {
	void f(int x) {
		do
			System.out.println(x);
		while (x > 0);
	}
}
// === end ===

// === case: do_while_simple_body_one_liner_splits ===
class T {
	void f(int x) {
		do --x; while (x > 0);
	}
}
// === end ===

// === case: do_while_tier2_while_on_same_line ===
class T {
	void f(int x) {
		do System.out.println(x); while (x > 0);
	}
}
// === end ===

// === case: do_while_tier3_as_tier2 ===
class T {
	void f(java.util.List<String> list) {
		do list.subList(0, 1).clear();
		while (!list.isEmpty());
	}
}
// === end ===

// === case: do_while_tier3_one_liner ===
class T {
	void f(java.util.List<String> list) {
		do list.subList(0, 1).clear(); while (!list.isEmpty());
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

// === case: e2e_dry_vs_normal_run_fixable_count ===
class T {
	int x = 0;
	int[] a = {1,};
	long y = 3000000000l;
}
// === end ===

// === case: enum_nested_two_tab_indent ===
class Outer {
	static class Inner {
		enum Nested {
			ZETA,
			ALPHA
		}
	}
}
// === end ===

// === case: enum_trailing_comma ===
enum Color {
	RED,
	GREEN,
}
// === end ===

// === case: enum_trailing_semicolon ===
enum Semi {
	A,
	B;
}
// === end ===

// === case: enum_trailing_semicolon_constant_body ===
enum SemiBody {
	X {
		@Override
		public String toString() {
			return "x";
		}
	};
}
// === end ===

// === case: enum_trailing_semicolon_deep_tab ===
class T {
	enum E {
		X;
	}
}
// === end ===

// === case: enum_trailing_semicolon_delete_line ===
enum SemiEmpty {
	;
}
// === end ===

// === case: enum_trailing_semicolon_delete_own_line ===
enum SemiOwn {
	X
	;
}
// === end ===

// === case: enum_trailing_semicolon_inline ===
enum SemiInline { X; }
// === end ===

// === case: enum_trailing_semicolon_with_comment ===
enum SemiComment {
	X; // remark
}
// === end ===

// === case: explicit_initialization ===
class T {
	boolean b = false;
	int x = 0;
	Object o = null;
}
// === end ===

// === case: explicit_initialization_comma_in_comment ===
class T {
	int x = 0 /* , */, y;
}
// === end ===

// === case: explicit_initialization_comment_preserving ===
class T {
	int x = 0 /* c */;
}
// === end ===

// === case: explicit_initialization_multi_declaration ===
class T {
	int a = 0, b;
}
// === end ===

// === case: explicit_initialization_multi_declaration_both ===
class T {
	int a = 0, b = 0;
}
// === end ===

// === case: explicit_initialization_tab_comment ===
class T {
	int x = 0; // note
}
// === end ===

// === case: field_consolidation_and_annotation_reorder_with_comment_between ===
class T {
	@Deprecated
	int alpha;
	@Deprecated
	int beta;
	// trailing comment

	@B
	@A
	void f() {}
}
// === end ===

// === case: field_consolidation_annotated ===
class T {
	@Deprecated
	int alpha;
	@Deprecated
	int beta;
}
// === end ===

// === case: field_consolidation_annotated_multiple_own_line ===
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
class T {
	@CheckReturnValue
	@Nonnull
	String alpha;
	@CheckReturnValue
	@Nonnull
	String beta;
}
// === end ===

// === case: field_consolidation_both_c_style_array ===
class T {
	int alpha[];
	int beta[];
}
// === end ===

// === case: field_consolidation_both_c_style_array_wrapping ===
class T {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa[];
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb[];
}
// === end ===

// === case: field_consolidation_c_style_curr_java_prev ===
class T {
	int[] alpha;
	int beta[];
}
// === end ===

// === case: field_consolidation_c_style_prev_java_curr ===
class T {
	int alpha[];
	int[] beta;
}
// === end ===

// === case: field_consolidation_comma_merge ===
class T {
	int alpha, beta;
	int gamma;
}
// === end ===

// === case: field_consolidation_final ===
class T {
	final int alpha;
	final int beta;
	T(int a, int b) { alpha = a; beta = b; }
}
// === end ===

// === case: field_consolidation_generic_type ===
import java.util.List;
class T {
	List<String> names;
	List<String> words;
}
// === end ===

// === case: field_consolidation_multi_var_prev ===
class T {
	int a, b;
	int c;
}
// === end ===

// === case: field_consolidation_protected ===
class T {
	protected int alpha;
	protected int beta;
}
// === end ===

// === case: field_consolidation_simple ===
class T {
	int alpha;
	int beta;
}
// === end ===

// === case: field_consolidation_static ===
class T {
	static int global;
	static int shared;
}
// === end ===

// === case: field_consolidation_three_fields ===
class T {
	int a;
	int b;
	int c;
}
// === end ===

// === case: field_consolidation_wrapping_four_long_fields ===
class T {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
	boolean ccccccccccccccccccccccccccccccccccc;
	boolean ddddddddddddddddddddddddddddddddddd;
}
// === end ===

// === case: field_consolidation_wrapping_three_fields ===
class T {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
	boolean ccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: field_consolidation_wrapping_two_long_fields ===
class T {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: field_sorting_annotation_consolidation ===
class T {
	@NonNull
	final String currencyCode;
	@Nullable
	final String engName, engSymbol;
	@NonNull
	final String equityNumber;
	@Nullable
	final String exchange, hebName, hebSymbol, itemType;
	@NonNull
	final String source;
	@Nullable
	final String stockType;
	@NonNull
	final String subAccount, subAccountName;

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
	@SuppressWarnings("unused")
	String beta;
	@Deprecated
	String alpha;
}
// === end ===

// === case: field_sorting_annotation_order ===
class T {
	@Deprecated
	String annotated;
	String plain;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order ===
@interface TA {}
class T {
	List<@TA String> annotated;
	List<String> plain;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_fqn ===
@interface TA {}
@SuppressWarnings("PreferImport")
class T {
	java.util.Set<@TA String> annotated;
	java.util.Set<String> plain;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard ===
@interface TA {}
class T {
	List<@TA ? extends Number> annotated;
	List<? extends Number> plain;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard_bound ===
@interface TA {}
class T {
	List<? extends @TA Number> annotated;
	List<? extends Number> plain;
}
// === end ===

// === case: field_sorting_annotation_type_arg_order_wildcard_lower_bound ===
@interface TA {}
class T {
	List<? super @TA Number> annotated;
	List<? super Number> plain;
}
// === end ===

// === case: field_sorting_chunk_order ===
class T {
	int nonFinal;
	final int finalWithValue = 1;

	T() {
		this.nonFinal = 0;
	}
}
// === end ===

// === case: field_sorting_dependency_order ===
class T {
	static final int B = A + 1;
	static final int A = 0;
}
// === end ===

// === case: field_sorting_enum_inner_class ===
class T {
	enum E {
		BETA,
		ALPHA
	}
}
// === end ===

// === case: field_sorting_enum_reorder ===
enum T {
	BETA,
	ALPHA
}
// === end ===

// === case: field_sorting_enum_same_line ===
enum T {
	ALPHA, BETA
}
// === end ===

// === case: field_sorting_enum_same_line_and_reorder ===
enum T {
	ZEBRA, ALPHA
}
// === end ===

// === case: field_sorting_enum_semicolon ===
enum T {
	BETA,
	ALPHA;
	int x;
}
// === end ===

// === case: field_sorting_enum_with_annotations ===
enum T {
	@Deprecated
	BETA,
	ALPHA
}
// === end ===

// === case: field_sorting_enum_with_args ===
enum T {
	CHERRY("r"),
	APPLE("g")
}
// === end ===

// === case: field_sorting_enum_with_bodies ===
enum T {
	SUB {
		int v() {
			return 1;
		}
	},
	ADD {
		int v() {
			return 0;
		}
	};
	abstract int v();
}
// === end ===

// === case: field_sorting_enum_with_trailing_comments ===
enum T {
	BETA, // b
	ALPHA // a
}
// === end ===

// === case: field_sorting_name_order ===
class T {
	final int z = 1;
	final int a = 0;
}
// === end ===

// === case: field_sorting_type_order ===
class T {
	final String name = "x";
	final int count = 0;
}
// === end ===

// === case: final_local_variable ===
class T {
	void f() {
		int x = 5;
		var y = "hello";
	}
}
// === end ===

// === case: final_local_variable_tab_indented ===
class T {
	void f() {
		if (true) {
			int x = 5;
		}
	}
}
// === end ===

// === case: fix_lambda_param_remove_parens ===
import java.util.List;
class T {
	void f(List<String> list) {
		list.forEach((x) -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_remove_type ===
import java.util.List;
class T {
	void f(List<String> list) {
		list.forEach((String x) -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_replace_type_with_var ===
import java.util.List;
@interface A {}
class T {
	void f(List<String> list) {
		list.forEach((@A String x) -> System.out.println(x));
	}
}
// === end ===

// === case: fix_lambda_param_replace_type_with_var_multi_param ===
import java.util.List;
@interface A {}
class T {
	void f(List<String> list) {
		list.sort((@A String x, String y) -> x.compareTo(y));
	}
}
// === end ===

// === case: fix_order_bottom_to_top ===
class T {
	int[] a = {1,};
	int[] b = {2,};
	int[] c = {3,};
}
// === end ===

// === case: fixer_returns_null_for_duplicate_on_same_line ===
class T {
	void f() {
		int x, y;
	}
}
// === end ===

// === case: jit_inefficiency_append_concat ===
class T {
	void f(StringBuilder sb, String v) {
		sb.append("key=" + v);
	}
}
// === end ===

// === case: jit_inefficiency_boxed_constructor ===
class T {
	Integer value() {
		return new Integer(42);
	}
}
// === end ===

// === case: jit_inefficiency_empty_string_concat ===
class T {
	String f(int x) {
		return "" + x;
	}
}
// === end ===

// === case: jit_inefficiency_new_string ===
class T {
	String f() {
		return new String("hello");
	}
}
// === end ===

// === case: jit_inefficiency_string_buffer ===
class T {
	String f() {
		final var sb = new StringBuffer("hi");
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
		for (var x : list)
			arr[0] = arr[0] + x;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_buried_in_if ===
import java.util.List;
class T {
	String f(List<String> list) {
		String names = list.get(0);
		log("start");
		for (var i = 1; i < list.size(); ++i) {
			final var x = list.get(i);
			if (x != null && !x.isEmpty())
				names = names + ", " + x;
		}
		return names;
	}

	void log(String s) {}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_decl_with_gap ===
import java.util.List;
class T {
	String f(List<String> list, int seed) {
		String s = "";
		final var n = seed * 2;
		for (var x : list)
			s = s + x;
		return s + n;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_explicit_chained ===
import java.util.List;
class T {
	String f(List<String> list) {
		String s = "";
		for (var x : list)
			s = s + ", " + x;
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
		for (var x : list)
			this.f = this.f + x;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_for_loop ===
import java.util.List;
class T {
	String f(List<String> list) {
		String s = "";
		for (var x : list)
			s += x;
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_mid_loop_read ===
import java.util.List;
class T {
	String f(List<String> list) {
		String s = "";
		for (var x : list) {
			if (s.length() < 100)
				s = s + x;
		}
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_non_empty_init ===
import java.util.List;
class T {
	String f(List<String> list) {
		String s = "prefix:";
		for (var x : list)
			s = s + x;
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_reverse_form ===
import java.util.List;
class T {
	String f(List<String> list) {
		String s = "";
		for (var x : list)
			s = x + s;
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_string_concat_in_loop_tier2_do_while ===
class T {
	String f() {
		String s = "";
		do s += "y";
		while (s.length() < 5);
		return s;
	}
}
// === end ===

// === case: jit_inefficiency_to_array_sized ===
import java.util.List;
class T {
	String[] f(List<String> list) {
		return list.toArray(new String[5]);
	}
}
// === end ===

// === case: mixed_fix_and_skip_from_same_check ===
class T {
	enum E {
		B, A
	}
	int a;
	int b;
}
// === end ===

// === case: multiline_close_move_general ===
class T {
	void m() {
		foo(
				1, 2);
	}
}
// === end ===

// === case: multiline_close_move_method_def ===
class T {
	void m(
			int a,
			int b) {
	}
}
// === end ===

// === case: multiline_close_move_tab_indented ===
class T {
	class Inner {
		void m() {
			foo(
					1, 2);
		}
	}
}
// === end ===

// === case: multiline_close_pullup_getstring_context_local ===
class T {
	void m() {
		final var ctx = requireContext();
		foo(ctx.getString(1)
		);
	}
}
// === end ===

// === case: multiline_close_pullup_lambda ===
class T {
	void m() {
		foo(x -> {
			bar(x);
		}
		);
	}
}
// === end ===

// === case: multiline_close_pullup_ternary ===
class T {
	void m() {
		foo(true ? "a" : "b"
		);
	}
}
// === end ===

// === case: multiline_open_move_general ===
class T {
	void m() {
		foo(1,
				2
		);
	}
}
// === end ===

// === case: multiline_open_move_tab_indented ===
class T {
	class Inner {
		void m() {
			foo(1,
					2
			);
		}
	}
}
// === end ===

// === case: multiline_put_collapsible_bare ===
class T {
	void m() {
		new JSONObject()
				.put("k", 1);
	}
}
// === end ===

// === case: multiline_put_collapsible_nested_converges ===
class T {
	Map<String, Object> cache;

	void m() {
		cache.put("View", new JSONObject()
				.put("Account", new JSONObject()
						.put("id", 1)
				)
		);
	}
}
// === end ===

// === case: multiline_put_collapsible_prefixed ===
class T {
	Map<String, Object> cache;

	void m() {
		cache.put("k", new JSONObject()
				.put("a", 1)
		);
	}
}
// === end ===

// === case: multiline_put_collapsible_tab_indented ===
class T {
	class Inner {
		void m() {
			new JSONObject()
					.put("k", 1);
		}
	}
}
// === end ===

// === case: multiline_put_collapsible_trailing_comment_last_line ===
class T {
	void m() {
		new JSONObject()
				.put("k", 1); // note
	}
}
// === end ===

// === case: multiple_checks_skip_reasons ===
class T {
	int x = 0;
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
	int[] a = {1,};
	long x = 100L;
}
// === end ===

// === case: no_final_parameters_catch ===
class T {
	void f() {
		try {
			System.out.println();
		}
		catch (final Exception e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: no_final_parameters_constructor ===
class T {
	T(final int x) {}
}
// === end ===

// === case: no_final_parameters_for_each ===
import java.util.List;
class T {
	void f(List<String> list) {
		for (final var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: no_final_parameters_method ===
class T {
	void f(final int x, final String y) {}
}
// === end ===

// === case: no_final_parameters_second_param ===
class T {
	void f(int x, final String y) {}
}
// === end ===

// === case: postfix_decrement ===
class T {
	void run() {
		int i = 5;
		i--;
	}
}
// === end ===

// === case: postfix_increment ===
class T {
	void run() {
		int i = 0;
		i++;
	}
}
// === end ===

// === case: postfix_increment_for_loop ===
class T {
	void run() {
		for (var i = 0; i < 10; i++)
			System.out.println(i);
	}
}
// === end ===

// === case: prefer_bulk_operation_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var item : source)
			target.add(item);
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill ===
class T {
	void f(int[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = 0;
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_braced ===
class T {
	void f(int[] arr) {
		for (var i = 0; i < arr.length; ++i) {
			arr[i] = 0;
		}
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_source_name_starts_with_length ===
class T {
	void f(int[] lengthValues) {
		for (var i = 0; i < lengthValues.length; ++i)
			lengthValues[i] = 0;
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_unary_plus_value_contains_bracket ===
class T {
	void f(int[] arr, int[] other) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = +other[0];
	}
}
// === end ===

// === case: prefer_bulk_operation_array_fill_value_contains_bracket ===
class T {
	void f(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = -a[b[0]];
	}
}
// === end ===

// === case: prefer_bulk_operation_entry_set_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet())
			target.put(entry.getKey(), entry.getValue());
	}
}
// === end ===

// === case: prefer_bulk_operation_entry_set_put_all_braced ===
import java.util.Map;
class T {
	void f(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet()) {
			target.put(entry.getKey(), entry.getValue());
		}
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_add_all ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		list.forEach(item -> other.add(item));
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_add_all ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		list.forEach(item -> {
			other.add(item);
		});
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_block_comment_wrong_target ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> real) {
		source.forEach((k, v) -> {
			/* future cleanup:
			   target.put(k, v);
			*/
			real.put(k, v);
		});
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_block_body_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> {
			target.put(k, v);
		});
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_preserves_leading_if_statement ===
import java.util.Map;
class T {
	void f(boolean flag, Map<String, String> source, Map<String, String> target) {
		if (flag) source.forEach((k, v) -> target.put(k, v));
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_lambda_put_all ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(k, v));
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_add ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		list.forEach(other::add);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_multi_line ===
import java.util.List;
class T {
	void f(List<String> list, List<String> other) {
		list.forEach(
				other::add
		);
	}
}
// === end ===

// === case: prefer_bulk_operation_for_each_method_ref_put ===
import java.util.Map;
class T {
	void f(Map<String, String> source, Map<String, String> target) {
		source.forEach(target::put);
	}
}
// === end ===

// === case: prefer_bulk_operation_indexed_add_all ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i)
			target.add(source.get(i));
	}
}
// === end ===

// === case: prefer_bulk_operation_indexed_add_all_braced ===
import java.util.List;
class T {
	void f(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i) {
			target.add(source.get(i));
		}
	}
}
// === end ===

// === case: prefer_bulk_operation_system_arraycopy ===
class T {
	void f(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i)
			dst[i] = src[i];
	}
}
// === end ===

// === case: prefer_bulk_operation_system_arraycopy_braced ===
class T {
	void f(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i) {
			dst[i] = src[i];
		}
	}
}
// === end ===

// === case: prefer_collection_interface_multi_same_line ===
import java.util.ArrayList;
import java.util.HashMap;
class T {
	void f(ArrayList<String> a, HashMap<String, Integer> b) {}
}
// === end ===

// === case: prefer_collection_interface_param ===
import java.util.HashSet;
class T {
	void f(HashSet<String> s) {}
}
// === end ===

// === case: prefer_collection_interface_return ===
import java.util.ArrayList;
class T {
	ArrayList<String> f() {
		return new ArrayList<>();
	}
}
// === end ===

// === case: prefer_collection_interface_return_import_already_present ===
import java.util.ArrayList;
import java.util.List;
class T {
	ArrayList<String> f() {
		return new ArrayList<>();
	}
}
// === end ===

// === case: prefer_direct_boolean_return_braced_both_branches ===
class T {
	boolean f(boolean flag) {
		if (flag) {
			return true;
		}
		else {
			return false;
		}
	}
}
// === end ===

// === case: prefer_direct_boolean_return_braced_then_trailing ===
class T {
	boolean f(boolean flag) {
		if (flag) {
			return true;
		}
		return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_chain_second_fires ===
class T {
	boolean f(int x, int y) {
		if (x > 0)
			++x;
		if (y > 0)
			return true;
		return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_combine_and ===
class T {
	boolean f(boolean flag, String s) {
		if (flag)
			return s.isEmpty();
		return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_extract_postfix_inc ===
class T {
	boolean f(int i) {
		if (i++ > 0)
			return true;
		return true;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_inline_forward ===
class T {
	boolean f(boolean flag) {
		if (flag) return true;
		return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_method_call_condition ===
class T {
	boolean f(String s) {
		if (s.isEmpty())
			return false;
		return true;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_next_line_with_else ===
class T {
	boolean f(boolean flag) {
		if (flag)
			return false;
		else
			return true;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_not_ident_double_neg ===
class T {
	boolean f(boolean flag) {
		if (!flag)
			return false;
		return true;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_same_literal ===
class T {
	boolean f(boolean flag) {
		if (flag)
			return true;
		return true;
	}
}
// === end ===

// === case: prefer_do_while_unbraced_body ===
class T {
	void f(int i) {
		++i;
		while (i < 10)
			++i;
	}
}
// === end ===

// === case: prefer_exact_assertion_false_instance_of ===
import static org.junit.jupiter.api.Assertions.assertFalse;
class T {
	void f(Object o) {
		assertFalse(o instanceof Integer);
	}
}
// === end ===

// === case: prefer_exact_assertion_helper_qualifier_unchanged ===
class T {
	static class H {
		static void assertTrue(boolean b) {}
		static void assertFalse(boolean b) {}
	}
	void f(Object o, boolean flag, int a, int b) {
		H.assertTrue(!(o instanceof String));
		H.assertTrue(!flag);
		H.assertTrue(a == b);
	}
}
// === end ===

// === case: prefer_exact_assertion_junit5_message_last ===
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void f(Object o) {
		assertTrue(o instanceof String, "should be a string");
	}
}
// === end ===

// === case: prefer_exact_assertion_negated ===
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void f(Object o) {
		assertTrue(!(o instanceof String));
	}
}
// === end ===

// === case: prefer_exact_assertion_plain_negation ===
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void f(boolean flag) {
		assertTrue(!flag);
	}
}
// === end ===

// === case: prefer_exact_assertion_qualified_call ===
import org.junit.jupiter.api.Assertions;
class T {
	void f(Object o) {
		Assertions.assertTrue(o instanceof String);
	}
}
// === end ===

// === case: prefer_exact_assertion_qualified_junit4_negation_fallback ===
class T {
	void f(Object o) {
		org.junit.Assert.assertTrue(!(o instanceof String));
	}
}
// === end ===

// === case: prefer_exact_assertion_true_instance_of ===
import static org.junit.jupiter.api.Assertions.assertTrue;
class T {
	void f(Object o) {
		assertTrue(o instanceof String);
	}
}
// === end ===

// === case: prefer_import_reflection_same_package ===
package com.etk2000.checkstyle.gradle.fix;
class T {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer;
}
// === end ===

// === case: prefer_import_used_via_fqn_keeps_import ===
import java.util.Map;
class T {
	java.util.Map<String, Integer> field;
}
// === end ===

// === case: prefer_math_method_abs ===
class T {
	int f(int a) {
		return a < 0 ? -a : a;
	}
}
// === end ===

// === case: prefer_math_method_clamp ===
class T {
	int f(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
// === end ===

// === case: prefer_math_method_if_compound_assign ===
class T {
	int f(int r, int a, int b) {
		if (a > b)
			r += a;
		else
			r += b;
		return r;
	}
}
// === end ===

// === case: prefer_math_method_if_decl_assign_return ===
class T {
	int f(int a, int b) {
		final int r;
		if (a > b)
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: prefer_math_method_if_else_return ===
class T {
	int f(int a, int b) {
		if (a > b)
			return a;
		else
			return b;
	}
}
// === end ===

// === case: prefer_math_method_if_init_overwrite ===
class T {
	int f(int a, int b) {
		var r = b;
		if (a > b)
			r = a;
		return r;
	}
}
// === end ===

// === case: prefer_math_method_if_plain_assign_bare ===
class T {
	void f(int r, int a, int b) {
		if (a > b)
			r = a;
		else
			r = b;
		System.out.println(r);
	}
}
// === end ===

// === case: prefer_math_method_if_trailing_return ===
class T {
	int f(int a, int b) {
		if (a > b)
			return a;
		return b;
	}
}
// === end ===

// === case: prefer_math_method_max ===
class T {
	int f(int a, int b) {
		return a > b ? a : b;
	}
}
// === end ===

// === case: prefer_math_method_max_pre_decrement ===
class T {
	int f(int a, int b) {
		return --a > b ? a : b;
	}
}
// === end ===

// === case: prefer_math_method_min ===
class T {
	int f(int a, int b) {
		return a < b ? a : b;
	}
}
// === end ===

// === case: prefer_specific_api_arrays_as_list ===
import java.util.List;
class T {
	List<String> run() {
		return Arrays.asList("a", "b");
	}
}
// === end ===

// === case: prefer_specific_api_arrays_as_list_removes_unused_import ===
import java.util.Arrays;
import java.util.List;
class T {
	List<String> run() {
		return Arrays.asList("a", "b");
	}
}
// === end ===

// === case: prefer_specific_api_assert_junit4 ===
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNotEquals;
class T {
	void run() {
		assertEquals(true, 1 == 1);
		assertEquals(new Object(), null);
		assertEquals("msg", null, new Object());
		assertNotEquals("msg", false, 1 == 1);
	}
}
// === end ===

// === case: prefer_specific_api_assert_junit5 ===
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNotEquals;
class T {
	void run() {
		assertEquals(true, 1 == 1);
		assertEquals(new Object(), null);
		assertEquals(null, new Object(), "msg");
		assertNotEquals(false, 1 == 1, "msg");
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory ===
import java.util.List;
class T {
	List<String> run() {
		return Collections.singletonList("a");
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_adds_import ===
import java.util.Collections;
class T {
	Object run() {
		return Collections.emptyList();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_import_already_present ===
import java.util.List;
class T {
	List<String> run() {
		return Collections.emptyList();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_import_between_groups ===
import java.util.Collections;

import javax.annotation.Nonnull;
class T {
	@Nonnull
	Object run() {
		return Collections.emptyList();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_multiple_imports ===
import java.util.Collections;
class T {
	Object a() {
		return Collections.emptyList();
	}
	Object b() {
		return Collections.emptyMap();
	}
	Object c() {
		return Collections.emptySet();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_multiple_imports_with_group_separator ===
import java.util.Collections;

import javax.annotation.Nonnull;
class T {
	@Nonnull
	Object a() {
		return Collections.emptyList();
	}
	@Nonnull
	Object b() {
		return Collections.emptySet();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_partial_import ===
import java.util.Collections;
import java.util.List;
class T {
	List<String> a() {
		return Collections.emptyList();
	}
	Object b() {
		return Collections.emptySet();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_removes_both_collections_calls ===
import java.util.Collections;
import java.util.List;
class T {
	List<String> a() {
		return Collections.singletonList("a");
	}
	void b(List<String> list) {
		Collections.sort(list);
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_removes_unused_import ===
import java.util.Collections;
class T {
	Object run() {
		return Collections.emptyList();
	}
}
// === end ===

// === case: prefer_specific_api_collections_factory_retains_collections_with_surviving_call ===
import java.util.Collections;
import java.util.List;
class T {
	List<String> a() {
		return Collections.singletonList("a");
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
		return Collections.emptyList();
	}
}
// === end ===

// === case: prefer_specific_api_collections_singleton_list_removes_unused_import ===
import java.util.Collections;
import java.util.List;
class T {
	List<String> run() {
		return Collections.singletonList("a");
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_no_comparator ===
import java.util.List;
class T {
	void run(List<String> list) {
		Collections.sort(list);
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_removes_unused_import ===
import java.util.Collections;
import java.util.List;
class T {
	void run(List<String> list) {
		Collections.sort(list);
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_with_comparator ===
import java.util.Comparator;
import java.util.List;
class T {
	void run(List<String> list) {
		Collections.sort(list, Comparator.naturalOrder());
	}
}
// === end ===

// === case: prefer_specific_api_collections_sort_with_comparator_removes_unused_import ===
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
class T {
	void run(List<String> list) {
		Collections.sort(list, Comparator.naturalOrder());
	}
}
// === end ===

// === case: prefer_specific_api_collections_unmodifiable_as_list ===
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
class T {
	List<String> run() {
		return Collections.unmodifiableList(Arrays.asList("a", "b"));
	}
}
// === end ===

// === case: prefer_specific_api_equals_empty ===
class T {
	void run(String s) {
		if (s.equals(""))
			return;
	}
}
// === end ===

// === case: prefer_specific_api_get_first ===
import java.util.List;
class T {
	String run(List<String> list) {
		return list.get(0);
	}
}
// === end ===

// === case: prefer_specific_api_index_of_char ===
class T {
	int f(String s) {
		return s.indexOf("x");
	}
}
// === end ===

// === case: prefer_specific_api_length_is_empty ===
class T {
	boolean run(String s) {
		return s.length() == 0;
	}
}
// === end ===

// === case: prefer_specific_api_length_is_empty_negated ===
class T {
	void run(String s) {
		if (s.length() > 0)
			return;
	}
}
// === end ===

// === case: prefer_specific_api_map_chain ===
import java.util.Map;
class T {
	void run(Map<String, String> map) {
		if (map.keySet().contains("k"))
			return;
	}
}
// === end ===

// === case: prefer_specific_api_no_imports_no_trigger ===
class T {
	boolean a(String s) {
		return s.equals("");
	}
}
// === end ===

// === case: prefer_specific_api_qualified_assert ===
class T {
	void run(boolean flag) {
		org.junit.Assert.assertEquals(true, flag);
	}
}
// === end ===

// === case: prefer_specific_api_redundant_import_plus_usage_rewrite ===
import java.lang.String;
import java.util.Arrays;
import java.util.List;
class T {
	List<String> run() {
		return Arrays.asList("a");
	}
}
// === end ===

// === case: prefer_specific_api_remove_first ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.remove(0);
	}
}
// === end ===

// === case: prefer_specific_api_replace_all ===
class T {
	String run(String s) {
		return s.replaceAll("foo", "bar");
	}
}
// === end ===

// === case: prefer_specific_api_size_is_empty ===
import java.util.List;
class T {
	boolean run(List<String> list) {
		return list.size() == 0;
	}
}
// === end ===

// === case: prefer_specific_api_size_is_empty_reversed ===
import java.util.List;
class T {
	void run(List<String> list) {
		if (0 < list.size())
			return;
	}
}
// === end ===

// === case: prefer_specific_api_stream_count ===
import java.util.List;
class T {
	long run(List<String> list) {
		return list.stream().count();
	}
}
// === end ===

// === case: prefer_specific_api_stream_find_first_is_present ===
import java.util.List;
class T {
	boolean run(List<String> list) {
		return list.stream().findFirst().isPresent();
	}
}
// === end ===

// === case: prefer_specific_api_stream_for_each ===
import java.util.List;
class T {
	void run(List<String> list) {
		list.stream().forEach(System.out::println);
	}
}
// === end ===

// === case: prefer_specific_api_string_format ===
class T {
	String run(String name) {
		return String.format("Hello %s", name);
	}
}
// === end ===

// === case: prefer_specific_api_string_format_single_arg ===
class T {
	String run() {
		return String.format("literal");
	}
}
// === end ===

// === case: prefer_specific_api_strip_is_blank ===
class T {
	boolean run(String s) {
		return s.strip().isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_strip_is_blank_negated ===
class T {
	boolean run(String s) {
		return s.strip().length() != 0;
	}
}
// === end ===

// === case: prefer_specific_api_strip_length_less_than_one ===
class T {
	boolean run(String s) {
		return s.strip().length() < 1;
	}
}
// === end ===

// === case: prefer_specific_api_to_array_new_zero ===
import java.util.List;
class T {
	String[] run(List<String> list) {
		return list.toArray(new String[0]);
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank ===
class T {
	boolean run(String s) {
		return s.trim().isEmpty();
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank_negated ===
class T {
	boolean run(String s) {
		return s.trim().length() != 0;
	}
}
// === end ===

// === case: prefer_specific_api_trim_is_blank_reversed ===
class T {
	boolean run(String s) {
		return 0 == s.trim().length();
	}
}
// === end ===

// === case: prefer_specific_api_trim_length_less_than_one ===
class T {
	boolean run(String s) {
		return s.trim().length() < 1;
	}
}
// === end ===

// === case: prefer_specific_api_zero_fixes_no_trigger ===
import java.util.List;
class T {
	List<String> a;
}
// === end ===

// === case: prefer_standard_charsets ===
class T {
	byte[] run(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: prefer_standard_charsets_adds_regular_after_existing_static ===
import static java.util.Objects.requireNonNull;

class T {
	byte[] run(String s) throws Exception {
		return requireNonNull(s).getBytes("UTF-8");
	}
}
// === end ===

// === case: prefer_standard_charsets_constructor_type_args ===
class T {
	String run(byte[] data) throws Exception {
		return new <String>String(data, "UTF-8");
	}
}
// === end ===

// === case: prefer_standard_charsets_import_already_present ===
import java.nio.charset.StandardCharsets;

class T {
	byte[] run(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: prefer_static_import_chained_calls ===
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

class T {
	List<String> f(List<String> list, String p, String s) {
		return list.stream().filter(Predicate.not(Objects.requireNonNull(p)::startsWith)).filter(Predicate.not(Objects.requireNonNull(s)::endsWith)).toList();
	}
}
// === end ===

// === case: prefer_static_import_collectors_to_set ===
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class T {
	Set<String> a(Stream<String> s) {
		return s.collect(Collectors.toSet());
	}
	Set<String> b(Stream<String> s) {
		return s.collect(Collectors.toSet());
	}
}
// === end ===

// === case: prefer_static_import_constant_alias_alone_in_chunk ===
import foo.Foo;

class T {
	private int a;

	private static final int X = Foo.X;

	private int b;

	int f() {
		return X + a + b;
	}
}
// === end ===

// === case: prefer_static_import_constant_alias_inside_field_chunk ===
import foo.Foo;

class T {
	private static final int A = 1;
	private static final int X = Foo.X;
	private static final int Z = 2;

	int f() {
		return A + X + Z + Foo.OTHER;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_blank_final_auto_fix ===
import foo.Foo;

class T {
	private static final int X;

	static {
		X = Foo.X;
	}

	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_fqn_lhs_auto_fix ===
package x;
import foo.Foo;

class T {
	private static final int X;

	static {
		x.T.X = Foo.X;
	}

	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_qualified_lhs_auto_fix ===
import foo.Foo;

class T {
	private static final int X;

	static {
		T.X = Foo.X;
	}

	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_cinit_same_line_decl_and_cinit_auto_fix ===
import foo.Foo;

class T { private static final int X; static { X = Foo.X; } int f() { return X; } }
// === end ===

// === case: prefer_static_import_constant_import_becomes_unused_after_fix ===
import foo.Foo;

class T {
	private static final int X = Foo.X;

	int f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_line_alias ===
import foo.LongClassName;

class T {
	private static final int LONG_CONSTANT_NAME =
			LongClassName.LONG_CONSTANT_NAME;

	int f() {
		return LONG_CONSTANT_NAME;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_annotation_preserved ===
import foo.Foo;

class T {
	@Deprecated private static final int X = Foo.X, Y = Foo.Y;

	int f() {
		return X + Y;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_single_line ===
import foo.Foo;

class T {
	private static final int X = Foo.X, Y = Foo.Y;

	int f() {
		return X + Y;
	}
}
// === end ===

// === case: prefer_static_import_constant_multi_var_string_literal_sibling_preserved ===
import foo.Foo;

class T {
	private static final Object X = Foo.X, Y = "hello";

	Object f() {
		return X;
	}
}
// === end ===

// === case: prefer_static_import_objects_require_non_null ===
import java.util.Objects;

class T {
	Object f(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: prefer_static_import_objects_require_non_null_removes_unused_import ===
import java.util.Objects;

class T {
	Object f(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: prefer_static_import_predicate_not ===
import java.util.List;
import java.util.function.Predicate;

class T {
	List<String> f(List<String> list) {
		return list.stream().filter(Predicate.not(String::isEmpty)).filter(Predicate.not(String::isBlank)).toList();
	}
}
// === end ===

// === case: prefer_var_diamond ===
import java.util.ArrayList;
class T {
	void f() {
		final var l = new ArrayList<Object>();
	}
}
// === end ===

// === case: prefer_var_diamond_anonymous_class ===
import java.util.Comparator;
class T {
	void f() {
		final var cmp = new Comparator<Object>() {
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
		final var l = new java.util.ArrayList<Object>();
	}
}
// === end ===

// === case: prefer_var_diamond_fqn ===
import java.util.ArrayList;
class T {
	void f() {
		final var l = new ArrayList<java.lang.Object>();
	}
}
// === end ===

// === case: prefer_var_diamond_mixed_qualified_and_bare ===
import java.util.HashMap;
class T {
	void f() {
		final var m = new HashMap<Object, java.lang.Object>();
	}
}
// === end ===

// === case: prefer_var_diamond_multiple_args ===
import java.util.HashMap;
class T {
	void f() {
		final var m = new HashMap<Object, Object>();
	}
}
// === end ===

// === case: prefer_var_explicit_array_init ===
class T {
	void f() {
		final var a = new String[]{"a"};
	}
}
// === end ===

// === case: prefer_var_explicit_array_init_method_call_arg ===
class T {
	void f() {
		String result = String.join(",", new String[]{"a", "b"});
	}
}
// === end ===

// === case: prefer_var_explicit_array_init_typed ===
class T {
	void f() {
		final String[] a = new String[]{"a"};
	}
}
// === end ===

// === case: prefer_var_final_local_interaction ===
class T {
	void f() {
		int x = 5;
	}
}
// === end ===

// === case: prefer_var_for_each ===
import java.util.List;
class T {
	void f() {
		for (String item : List.of("a"))
			System.out.println(item);
	}
}
// === end ===

// === case: prefer_var_for_each_annotation_prev_line ===
import java.util.List;
import javax.annotation.Nonnull;
class T {
	void f() {
		for (
				@Nonnull
				String item : List.of("a"))
			System.out.println(item);
	}
}
// === end ===

// === case: prefer_var_for_init ===
class T {
	void f() {
		for (int i = 0; i < 10; ++i)
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
		final String s = "hi";
	}
}
// === end ===

// === case: prefer_var_local_with_final ===
class T {
	void f() {
		final int x = 5;
	}
}
// === end ===

// === case: prefer_var_tab_indented ===
class T {
	void f() {
			final int x = 5;
	}
}
// === end ===

// === case: prefer_var_try_with_resources ===
import java.io.ByteArrayInputStream;
class T {
	void f() throws Exception {
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) {
			in.read();
		}
	}
}
// === end ===

// === case: record_formatting_brace_newline ===
record R(int a)
{}
// === end ===

// === case: record_formatting_component_collapse_mixed ===
record R(int a,
		int b) {}
// === end ===

// === case: record_formatting_component_expand_wide_line ===
record WideRecord(int aaaaaaaaaa,
		int bbbbbbbbbb, int cccccccccc, int dddddddddd, int eeeeeeeeee, int ffffffffff, int gggggggggg, int hhhhhhhhhh) {}
// === end ===

// === case: record_formatting_empty_body_split ===
record R(int a) {
}
// === end ===

// === case: record_formatting_implements_multi_line ===
interface Foo {}
record R(int a) implements
		Foo
{}
// === end ===

// === case: record_formatting_implements_no_space ===
interface Foo {}
record R(int a) implements Foo{}
// === end ===

// === case: record_formatting_no_space_before_brace ===
record R(int a){}
// === end ===

// === case: record_formatting_non_empty_body_same_line ===
record R(int a) { int b() { return a; } }
// === end ===

// === case: record_formatting_tab_before_brace ===
record R(int a)	{}
// === end ===

// === case: record_formatting_two_spaces_before_brace ===
record R(int a)  {}
// === end ===

// === case: redundant_array_creation ===
import java.util.ArrayList;
import java.util.Collections;

class T {
	void f() {
		Collections.addAll(new ArrayList<>(), new String[]{"a", "b"});
	}
}
// === end ===

// === case: redundant_array_creation_constructor ===
class T {
	void f() {
		new ProcessBuilder(new String[]{"cmd", "arg"});
	}
}
// === end ===

// === case: redundant_array_creation_empty_array ===
import java.util.ArrayList;
import java.util.Collections;

class T {
	void f() {
		Collections.addAll(new ArrayList<>(), new String[]{});
	}
}
// === end ===

// === case: redundant_array_creation_string_join ===
class T {
	void f() {
		String.join(",", new String[]{"a", "b"});
	}
}
// === end ===

// === case: redundant_cast_bare_wrap ===
class T {
	String f(String s) {
		return ((String) s);
	}
}
// === end ===

// === case: redundant_cast_null_assignment ===
class T {
	void f() {
		final String s = (String) null;
	}
}
// === end ===

// === case: redundant_cast_receiver_paren ===
class T {
	int f(String s) {
		return ((String) s).length();
	}
}
// === end ===

// === case: redundant_cast_receiver_paren_cross_line ===
class T {
	int f(String s) {
		return
((String) s).length();
	}
}
// === end ===

// === case: redundant_cast_text_block_prior_context ===
class T {
	int f(String s) {
		String x = """
/*
""";
		return ((String) s).length();
	}
}
// === end ===

// === case: redundant_cast_widening ===
class T {
	void f(int x) {
		final long y = (long) x;
	}
}
// === end ===

// === case: redundant_equality_branch_assign_bare_collapse ===
class T {
	void f(int r, int a, int b) {
		if (a == b)
			r = a;
		else
			r = b;
		System.out.println(r);
	}
}
// === end ===

// === case: redundant_equality_branch_assign_with_decl_and_return ===
class T {
	int f(int a, int b) {
		final int r;
		if (a == b)
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: redundant_equality_branch_not_equal ===
class T {
	int f(int a, int b) {
		final int r;
		if (a != b)
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: redundant_equality_branch_trailing_return ===
class T {
	int f(int a, int b) {
		if (a == b)
			return a;
		return b;
	}
}
// === end ===

// === case: redundant_import ===
import java.lang.String;

class T {
	String s;
}
// === end ===

// === case: redundant_import_contiguous_suppresses_duplicate ===
import java.lang.String;
import java.util.List;

class T {
	List<String> s;
}
// === end ===

// === case: redundant_import_orphaned_suppresses_duplicate ===
import java.io.File;

import java.lang.String;

import javax.annotation.Nonnull;

class T {
	@Nonnull
	File f;
	String s;
}
// === end ===

// === case: redundant_modifier ===
interface T {
	public void method();
}
// === end ===

// === case: redundant_modifier_private_enum_constructor ===
enum Color {
	RED(1);

	private Color(int code) {
	}
}
// === end ===

// === case: redundant_modifier_static_interface_field ===
interface T {
	static int VALUE = 5;
}
// === end ===

// === case: redundant_numeric_suffix ===
class T {
	double d = 1.0d;
	long x = 100L;
}
// === end ===

// === case: redundant_numeric_suffix_hex_and_binary_and_float ===
class T {
	float b = 100F;
	long a = 0xFFL;
	long c = 0b1010L;
}
// === end ===

// === case: super_call ===
class Child extends Object {
	Child() {
		super();
	}
}
// === end ===

// === case: super_call_tab_indented ===
class Outer {
	class Inner extends Object {
		Inner() {
			super();
		}
	}
}
// === end ===

// === case: trailing_newline_double ===
class T {}


// === end ===

// === case: trailing_newline_plus_other ===
class T {int x = (int) 5;}

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
		return this.value;
	}
}
// === end ===

// === case: unnecessary_this_chained ===
class T {
	String value;
	int get() {
		return this.value.length();
	}
}
// === end ===

// === case: unused_import ===
import java.util.List;

class T {
}
// === end ===

// === case: unused_import_orphaned_in_group_removes_blank_line ===
package p;

import java.util.List;

class T {
}
// === end ===

// === case: unused_import_orphaned_middle_group_removes_blank_line ===
import java.io.File;

import java.util.List;

import javax.annotation.Nonnull;

class T {
	@Nonnull
	File f;
}
// === end ===

// === case: upper_ell ===
class T {
	long x = 3000000000l;
}
// === end ===

// === case: upper_ell_hex ===
class T {
	long x = 0xB00000000l;
}
// === end ===

// === case: verify_clean_accepts_clean_output ===
class T {
	void method() {}
}
// === end ===

// === case: verify_clean_accepts_unfixable_violations ===
class T {
	void f(boolean a, boolean b) {
		if (a
				&& b) {
			System.out.println(a);
		}
	}
}
// === end ===

// === case: verify_clean_handles_multi_pass_stabilization ===
import java.util.Collections;
class T {
	Object run() {
		return Collections.emptyList();
	}
}
// === end ===