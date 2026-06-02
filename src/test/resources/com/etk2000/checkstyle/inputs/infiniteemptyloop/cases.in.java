package com.etk2000.checkstyle.inputs.infiniteemptyloop;

// === case: do_while_true_braced ===
class InputInfiniteEmptyLoopDoWhileTrueBracedSliceViolation {
	void m() {
		do { // violation: Empty infinite do-while loop, this will hang.
		} while (true);
	}
}
// === end ===

// === case: do_while_true_naked ===
class InputInfiniteEmptyLoopDoWhileTrueNakedSliceViolation {
	void m() {
		do; // violation: Empty infinite do-while loop, this will hang.
		while (true);
	}
}
// === end ===

// === case: for_empty_braced ===
class InputInfiniteEmptyLoopForEmptyBracedSliceViolation {
	void m() {
		for (;;) { // violation: Empty infinite for loop, this will hang.
		}
	}
}
// === end ===

// === case: for_empty_naked ===
class InputInfiniteEmptyLoopForEmptyNakedSliceViolation {
	void m() {
		for (;;); // violation: Empty infinite for loop, this will hang.
	}
}
// === end ===

// === case: for_true_braced ===
class InputInfiniteEmptyLoopForTrueBracedSliceViolation {
	void m() {
		for (;true;) { // violation: Empty infinite for loop, this will hang.
		}
	}
}
// === end ===

// === case: for_true_naked ===
class InputInfiniteEmptyLoopForTrueNakedSliceViolation {
	void m() {
		for (;true;); // violation: Empty infinite for loop, this will hang.
	}
}
// === end ===

// === case: while_true_braced ===
class InputInfiniteEmptyLoopWhileTrueBracedSliceViolation {
	void m() {
		while (true) { // violation: Empty infinite while loop, this will hang.
		}
	}
}
// === end ===

// === case: while_true_naked ===
class InputInfiniteEmptyLoopWhileTrueNakedSliceViolation {
	void m() {
		while (true); // violation: Empty infinite while loop, this will hang.
	}
}
// === end ===