// === case: all_fixable_multiple_violations ===
class T {
	int x = 0;
	int y = 0;
}
// === end ===

// === case: all_fixable_single_violation ===
class T {
	int x = 0;
}
// === end ===

// === case: control_flow_braces_text_block_body ===
class T {
	void f(String s) {
		do s = """
				text
				""";
		while (true);
	}
}
// === end ===

// === case: mixed_one_fixable_one_skipped ===
class T {
	int x = 0;
	void f(String s) {
		do s = """
				text
				""";
		while (true);
	}
}
// === end ===

// === case: mixed_two_fixable_one_skipped ===
class T {
	int x = 0;
	int y = 0;
	void f(String s) {
		do s = """
				text
				""";
		while (true);
	}
}
// === end ===

// === case: no_violations ===
class T {
	void m() {}
}
// === end ===

// === case: prefer_direct_boolean_return_multiline_condition ===
class T {
	boolean f(boolean a, boolean b) {
		if (a
				/* spanning
				comment */ && b) return true;
		return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_two_statements_on_if_line ===
class T {
	boolean f(boolean a) {
		if (a) return true; return false;
	}
}
// === end ===

// === case: prefer_direct_boolean_return_unicode_escape ===
class T {
	boolean f(char c) {
		if (c == '\u0041')
			return true;
		return false;
	}
}
// === end ===

// === case: prefer_exact_assertion_comparison_form ===
import org.junit.jupiter.api.Assertions;
class T {
	void f(Object a, Object b) {
		Assertions.assertTrue(a == b);
	}
}
// === end ===

// === case: prefer_math_method_multiline_ternary ===
class T {
	int f(int a, int b) {
		return a > b
			? a : b;
	}
}
// === end ===

// === case: redundant_plus_unused_import_pass_through_blank ===
import java.lang.String;

class T {}
// === end ===

// === case: redundant_plus_unused_import_second_suppressed ===
import java.lang.String;
class T {}
// === end ===