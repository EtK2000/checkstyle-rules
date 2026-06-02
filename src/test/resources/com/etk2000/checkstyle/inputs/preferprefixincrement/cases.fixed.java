package com.etk2000.checkstyle.inputs.preferprefixincrement;

// === case: for_loop_update ===
class InputPrefixForLoopUpdateSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===