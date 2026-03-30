package com.etk2000.checkstyle.inputs.prefix;

class InputPrefixClean {
	void arrayIndexPostfix() {
		int i = 0;
		int[] arr = new int[5];
		int x = arr[i++];
	}

	void assignmentPostfix() {
		int i = 0;
		int x = i++;
	}

	void expressionPostfix() {
		int i = 0;
		int x = 1 + i++;
	}

	void methodArgPostfix() {
		int i = 0;
		System.out.println(i++);
	}

	void prefixUsage() {
		int i = 0;
		++i;
		--i;
	}

	int returnPostfix() {
		int i = 0;
		return i++;
	}
}