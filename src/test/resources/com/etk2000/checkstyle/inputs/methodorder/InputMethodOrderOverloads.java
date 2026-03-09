package com.etk2000.checkstyle.inputs.methodorder;

class InputMethodOrderOverloads {
	// overloads with same name are not flagged (OverloadMethodOrderCheck handles those)
	void process() {
	}

	void process(int x) {
	}

	void process(int x, int y) {
	}

	// next method alphabetically after overloads
	void validate() {
	}
}