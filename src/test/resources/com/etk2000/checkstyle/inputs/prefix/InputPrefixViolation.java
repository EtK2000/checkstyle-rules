package com.etk2000.checkstyle.inputs.prefix;

class InputPrefixViolation {
	void bracelessElse(boolean flag) {
		int i = 0;
		if (flag)
			++i;
		else
			i++;
	}

	void bracelessIf(boolean flag) {
		int i = 0;
		if (flag)
			i++;
	}

	void forLoopUpdate() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
		}
	}

	void standaloneStatement() {
		int i = 0;
		i++;
		i--;
	}
}