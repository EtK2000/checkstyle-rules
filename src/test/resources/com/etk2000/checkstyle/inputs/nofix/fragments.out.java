// === case: test_field_sorting_field_violation_now_fixed ===
class T {
	static final int A = 0;
	static final String Z = "z";
}
// === end ===

// === case: test_multiline_close_co_occurring_inline_block_now_fixed ===
class T {
	void m() {
		foo(x -> {
			bar(x);
		});
	}
}
// === end ===