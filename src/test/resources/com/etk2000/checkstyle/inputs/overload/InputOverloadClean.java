package com.etk2000.checkstyle.inputs.overload;

class InputOverloadClean {
	void method() {}

	void method(int a) {}

	void method(int a, int b) {}

	void other(int a) {}

	void differentMethod() {}

	void other(int a, int b) {}

	void same(int a) {}

	void same(String b) {}
}