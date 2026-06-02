// === case: collect_java_files_filters_non_java_a ===
class A {}
// === end ===

// === case: collect_java_files_filters_non_java_c ===
class C {}
// === end ===

// === case: collect_java_files_recursive_a ===
class A {}
// === end ===

// === case: collect_java_files_recursive_b ===
class B {}
// === end ===

// === case: do_execute_dry_run_does_not_modify_file ===
class T {
	int[] a = {1, 2,};
}
// === end ===

// === case: do_execute_dry_run_fixable_but_all_skipped ===
class T {
	void f(boolean a, boolean b) {
		if (a
				&& b) {
			System.out.println(a);
		}
	}
}
// === end ===

// === case: do_execute_dry_run_fixable_count_matches_violations ===
class T {
	@SuppressWarnings("unused")
	String c;
	@Deprecated
	String b;
	int x = 0;
}
// === end ===

// === case: do_execute_dry_run_returns_correct_count ===
class T {
	int b, a;
	int[] c = {1, 2,};
}
// === end ===

// === case: do_execute_dry_run_second_pass_flag ===
import java.nio.charset.Charset;
class T {
	Charset c = Charset.forName("UTF-8");
}
// === end ===

// === case: do_execute_dry_run_suppresses_summary_output ===
class T {
	int[] a = {1, 2,};
}
// === end ===

// === case: do_execute_zero_violations_returns_zeros ===
class T {
	void m() {}
}
// === end ===

// === case: e2e_mixed_fixable_and_unfixable_violations ===
class T {
	int[] c = {1, 2,};
	void f() {
		int a, b;
	}
}
// === end ===

// === case: e2e_multiple_files_aggregates_count_a ===
class A {
	int x = 0;
}
// === end ===

// === case: e2e_multiple_files_aggregates_count_b ===
class B {
	int[] a = {1,};
}
// === end ===

// === case: test_all_skipped_has_reasons ===
class T {
	void f(boolean a, boolean b) {
		if (a
				&& b) {
			System.out.println(a);
		}
	}
}
// === end ===

// === case: test_apply_fixes_skips_unknown_violations ===
class T {
	int[] a = {1, 2,};
}
// === end ===

// === case: test_array_type_style_multi_var_with_initializer_skipped ===
class T {
	void m() {
		final int gamma[] = {1}, delta = 0;
		gamma[0] = delta;
	}
}
// === end ===

// === case: test_clean_file_no_violations_no_reasons ===
class T {
	void method() {}
}
// === end ===

// === case: test_field_consolidation_block_comment_before_field_name_skipped ===
class T {
	int /* note */ alpha;
	int /* note */ beta;
}
// === end ===

// === case: test_field_consolidation_block_comment_post_name_skipped ===
class T {
	int alpha;
	int beta /* doc */;
}
// === end ===

// === case: test_field_consolidation_wrapping_pre_existing_multi_line_not_flagged ===
class T {
	private boolean areInvestmentFundsTreatedAsPensionLiquidity,
			arePensionsTreatedAsSeparateLiquidity,
			areUnvestedRsusExcludedFromSum,
			areUnvestedRsusTreatedAsSeparateLiquidity;
}
// === end ===

// === case: test_field_sorting_enum_already_sorted ===
enum T {
	ALPHA,
	BETA
}
// === end ===

// === case: test_field_sorting_field_violation_now_fixed ===
class T {
	static final String Z = "z";
	static final int A = 0;
}
// === end ===

// === case: test_multiline_close_co_occurring_inline_block_now_fixed ===
class T {
	void m() {
		foo(
				x -> {
					bar(x);
				}
		);
	}
}
// === end ===

// === case: test_multiline_close_co_occurring_skip ===
class T {
	void m() {
		foo(1,
				2);
	}
}
// === end ===

// === case: test_multiline_close_pullup_comment_skip ===
class T {
	void m() {
		foo(x -> {
			bar(x);
		} // note
		);
	}
}
// === end ===

// === case: test_multiline_pull_up_tail_comment_skip ===
class T {
	void m() {
		foo(
				x -> {
					bar(x);
				} // note
		);
	}
}
// === end ===

// === case: test_multiline_put_comment_skip ===
class T {
	void m() {
		new JSONObject() // note
				.put("k", 1);
	}
}
// === end ===

// === case: test_multiline_put_unsupported_shape_skip ===
class T {
	void m() {
		foo(arg1,
				arg2);
	}
}
// === end ===

// === case: test_no_violations ===
class Clean {
	int x = 100;
	int[] a = {1, 2};
}
// === end ===

// === case: test_prefer_math_method_skips_multiline_ternary ===
class T {
	int f(int a, int b) {
		return a > b
			? a : b;
	}
}
// === end ===

// === case: test_prefer_var_warning_not_fixed ===
class T {
	void f(int a, int b) {
		final float x = a + b;
	}
}
// === end ===

// === case: test_unused_import_unterminated_block_comment_skipped ===
package x;

import java.util.List; /* note
 */
class X {
	void m() {}
}
// === end ===