package com.etk2000.checkstyle.inputs.preferprefixincrement;

class InputPrefixClean {
	private int count;

	void arrayIndexPostfix() {
		var i = 0;
		final var arr = new int[5];
		final var x = arr[i++];
	}

	void assignmentPostfix() {
		var i = 0;
		final var x = i++;
	}

	void doWhileConditionPostfix() {
		var i = 0;
		do ++i;
		while (i++ < 10);
	}

	void expressionLambdaPostfix() {
		final Runnable r = () -> count++;
		r.run();
	}

	void expressionPostfix() {
		var i = 0;
		final var x = 1 + i++;
	}

	void forInitPostfix() {
		var i = 0;
		for (i++; i < 5; ++i)
			System.out.println(i);
	}

	void ifConditionPostfix() {
		var i = 0;
		if (i++ > 0)
			System.out.println(i);
	}

	void labeledPostfix() {
		var i = 0;
		label: i++;
		System.out.println(i);
	}

	void methodArgPostfix() {
		var i = 0;
		System.out.println(i++);
	}

	void prefixUsage() {
		var i = 0;
		++i;
		--i;
	}

	int returnPostfix() {
		var i = 0;
		return i++;
	}

	void switchExpressionPostfix(int k) {
		var i = 0;
		final var value = switch (k) {
			case 1 -> i++;
			default -> --i;
		};
		System.out.println(value);
	}

	void switchRulePostfix(int k) {
		var i = 0;
		switch (k) {
			case 1 -> i++;
			default -> --i;
		}
		System.out.println(i);
	}

	void whileConditionPostfix() {
		var i = 0;
		while (i++ < 5)
			System.out.println(i);
	}
}