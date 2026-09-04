package com.etk2000.checkstyle.inputs.preferprefixincrement;

// === case: for_loop_update ===
class InputPrefixForLoopUpdateSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: main ===
class InputPrefixViolation {
	void bracedDoWhile(boolean flag) {
		var i = 0;
		do ++i;
		while (flag);
	}

	void bracelessDoWhile(boolean flag) {
		var i = 0;
		do ++i;
		while (flag);
	}

	void standaloneStatement() {
		var i = 0;
		++i;
		--i;
	}
}
// === end ===