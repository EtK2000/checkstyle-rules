package com.etk2000.checkstyle.inputs.methodalphabeticalorder;

class InputMethodOrderClean {
	static void alpha() {
	}

	static void beta() {
	}

	static void gamma() {
	}

	void delta() {
	}

	void epsilon() {
	}

	void zeta() {
	}
}

class InputMethodOrderOverloads {
	void process() {
	}

	void process(int x) {
	}

	void process(int x, int y) {
	}

	void validate() {
	}
}