package com.etk2000.checkstyle.inputs.prefix;

class InputPrefixViolation {
	void bracelessElse(boolean flag) {
		var i = 0;
		if (flag)
			++i;
		else
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}

	void bracelessIf(boolean flag) {
		var i = 0;
		if (flag)
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}

	void bracelessWhile(boolean flag) {
		var i = 0;
		while (flag)
			i--; // violation: Use prefix decrement (--x) instead of postfix (x--).
	}

	void forLoopUpdate() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
		}
	}

	void standaloneStatement() {
		var i = 0;
		i++;
		i--;
	}
}