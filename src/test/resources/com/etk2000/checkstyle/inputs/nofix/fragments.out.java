// === case: fix_until_settled_one_pass_merge_stale_line ===
class T {
	// reset on connect
	private int retries = 0, volume = 5;
	private final boolean debug = false;
}
// === end ===

// === case: fix_until_settled_one_pass_merge_suppressed_line ===
class T {
	private int count = 5, total = 0;
}
// === end ===

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