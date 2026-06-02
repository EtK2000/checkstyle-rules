package com.etk2000.checkstyle.inputs.infiniteemptyloop;

// === case: do_while_true_braced ===
class InputInfiniteEmptyLoopDoWhileTrueBracedSliceViolation {
	void m() {
		do {
		} while (true);
	}
}
// === end ===

// === case: do_while_true_naked ===
class InputInfiniteEmptyLoopDoWhileTrueNakedSliceViolation {
	void m() {
		do;
		while (true);
	}
}
// === end ===

// === case: for_empty_braced ===
class InputInfiniteEmptyLoopForEmptyBracedSliceViolation {
	void m() {
		for (;;) {
		}
	}
}
// === end ===

// === case: for_empty_naked ===
class InputInfiniteEmptyLoopForEmptyNakedSliceViolation {
	void m() {
		for (;;);
	}
}
// === end ===

// === case: for_true_braced ===
class InputInfiniteEmptyLoopForTrueBracedSliceViolation {
	void m() {
		for (;true;) {
		}
	}
}
// === end ===

// === case: for_true_naked ===
class InputInfiniteEmptyLoopForTrueNakedSliceViolation {
	void m() {
		for (;true;);
	}
}
// === end ===

// === case: while_true_braced ===
class InputInfiniteEmptyLoopWhileTrueBracedSliceViolation {
	void m() {
		while (true) {
		}
	}
}
// === end ===

// === case: while_true_naked ===
class InputInfiniteEmptyLoopWhileTrueNakedSliceViolation {
	void m() {
		while (true);
	}
}
// === end ===