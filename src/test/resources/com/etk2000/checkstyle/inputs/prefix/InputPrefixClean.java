package com.etk2000.checkstyle.inputs.prefix;

class InputPrefixClean {
	void arrayIndexPostfix() {
		var i = 0;
		final var arr = new int[5];
		final var x = arr[i++];
	}

	void assignmentPostfix() {
		var i = 0;
		final var x = i++;
	}

	void expressionPostfix() {
		var i = 0;
		final var x = 1 + i++;
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
}