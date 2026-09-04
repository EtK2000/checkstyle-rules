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

// === case: do_execute_for_name_static_import_stays_in_its_file_a ===
import static java.nio.charset.Charset.forName;

import java.nio.charset.Charset;

class A {
	Charset f() {
		return forName("UTF-8");
	}
}
// === end ===

// === case: do_execute_for_name_static_import_stays_in_its_file_b ===
import java.nio.charset.Charset;

class B extends Base {
	Charset f() {
		return forName("UTF-8");
	}
}
// === end ===

// === case: do_execute_for_name_wildcard_import_stays_in_its_file_a ===
import java.nio.charset.Charset;

class A {
	Charset f() {
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: do_execute_for_name_wildcard_import_stays_in_its_file_b ===
package com.example;

import java.nio.charset.*;

class B {
	Charset f() {
		return Charset.forName("UTF-8");
	}
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

// === case: fix_until_settled_one_pass_merge_stale_line ===
class T {
	private int volume = 5;
	private final boolean debug = false;
	private int retries = 0; // reset on connect
}
// === end ===

// === case: fix_until_settled_one_pass_merge_suppressed_line ===
class T {
	private int count = 5;
	private int total = 0;
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

// === case: test_array_type_style_escaped_name_skipped ===
class T {
	int field[];
}
// === end ===

// === case: test_array_type_style_multi_var_depth_mismatch_skipped ===
class T {
	int alpha[], beta[][];
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

// === case: test_multiline_open_close_literal_separator_skip ===
class T {
	void m() {
		foo(a,
				/* c1
				http://x */ b,
				c);
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

// === case: test_no_violations ===
class Clean {
	int x = 100;
	int[] a = {1, 2};
}
// === end ===

// === case: test_prefer_import_ambiguous_inherited_member_type_skipped ===
import com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture;

import java.lang.Thread.State;

class T {
	abstract static class Sub extends ClasspathThreadFuture {
		java.lang.Thread.State state;
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: test_prefer_import_inherited_member_type_skipped ===
import java.lang.Thread.State;
import java.util.concurrent.Future;

abstract class T implements Future<State> {
	abstract java.lang.Thread.State state();
}
// === end ===

// === case: test_prefer_import_static_import_owner_unreadable_skipped ===
import static com.unknown.Missing.*;

class T {
	java.lang.Double value;
}
// === end ===

// === case: test_prefer_import_static_imported_member_type_skipped ===
import static java.awt.geom.Point2D.*;

class T {
	double max() {
		return java.lang.Double.MAX_VALUE;
	}
}
// === end ===

// === case: test_prefer_import_unreadable_scope_warning_not_fixed ===
class T extends MissingSupertype {
	int max() {
		return java.lang.Integer.MAX_VALUE;
	}
}
// === end ===

// === case: test_prefer_import_unreadable_supertype_member_type_skipped ===
import java.lang.Thread.State;

abstract class T extends MissingSupertype implements Comparable<State> {
	abstract java.lang.Thread.State state();
}
// === end ===

// === case: test_prefer_math_method_skips_multiline_ternary_with_comment ===
class T {
	int f(int a, int b) {
		return a > b
			// note
			? a : b;
	}
}
// === end ===

// === case: test_prefer_specific_api_strip_on_same_file_type_not_reported ===
class T {
	static class Text {
		boolean isEmpty() {
			return true;
		}

		Text strip() {
			return this;
		}
	}

	boolean f(Text text) {
		return text.strip().isEmpty();
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_comment_skipped ===
import java.nio.charset.Charset;

class T {
	Charset f() {
		return Charset.forName(/* default */ "UTF-8");
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_inherited_bare_call_warns ===
import java.nio.charset.Charset;

class T {
	static class Base {
		static Charset forName(String name) {
			return Charset.defaultCharset();
		}
	}

	static class Derived extends Base {
		Charset f() {
			return forName("UTF-8");
		}
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_multiline_skipped ===
import java.nio.charset.Charset;

class T {
	Charset f() {
		return Charset.forName(
				"UTF-8"
		);
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_static_star_import_warns ===
import static java.nio.charset.Charset.*;

import java.nio.charset.Charset;

class T {
	Charset f() {
		return forName("UTF-8");
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_top_level_charset_class_warns ===
import java.nio.charset.*;

class T {
	Object f() {
		return Charset.forName("UTF-8");
	}
}

class Charset {
	static Object forName(String name) {
		return name;
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_wildcard_import_in_package_warns ===
package com.example;

import java.nio.charset.*;

class T {
	Charset f() {
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: test_prefer_standard_charsets_for_name_wildcard_import_sibling_charset_warns ===
import java.nio.charset.*;

class T {
	Object f() {
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: test_prefer_standard_charsets_literal_warning_not_fixed ===
import java.nio.charset.Charset;

class T {
	boolean f() {
		return Charset.isSupported("UTF-8");
	}
}
// === end ===

// === case: test_prefer_standard_charsets_sibling_shim_warns ===
class T {
	byte[] encode(String s) throws Exception {
		return s.getBytes("UTF-8");
	}

	String name() {
		return StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: test_prefer_standard_charsets_standard_charsets_foreign_import_warns ===
import com.example.StandardCharsets;

class T {
	byte[] f(String s) throws Exception {
		return s.getBytes("UTF-8");
	}

	Object g() {
		return StandardCharsets.class;
	}
}
// === end ===

// === case: test_prefer_standard_charsets_standard_charsets_top_level_shim_warns ===
class T {
	byte[] f(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}

final class StandardCharsets {
}
// === end ===

// === case: test_prefer_standard_charsets_unreadable_supertype_not_fixed ===
class T extends MissingSupertype {
	byte[] encode(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: test_prefer_var_narrowing_without_uses_not_fixed ===
import java.util.ArrayList;
import java.util.List;

class T {
	void f() {
		final List<String> items = new ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: test_prefer_var_receiver_overload_warning_not_fixed ===
import java.util.ArrayList;
import java.util.Collection;

class T {
	void f() {
		final Collection<Integer> ids = new ArrayList<>();
		System.out.println(ids.remove(1));
	}
}
// === end ===

// === case: test_prefer_var_ternary_argument_warning_not_fixed ===
import java.util.ArrayList;
import java.util.List;

class T {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void f(boolean flag) {
		final List<String> items = new ArrayList<>();
		take(flag ? items : null);
	}
}
// === end ===

// === case: test_prefer_var_try_resource_warning_not_fixed ===
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;

class T {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void f() throws IOException {
		try (Closeable c = openReader()) {
			System.out.println(use(c));
		}
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