package com.etk2000.checkstyle.inputs.overload;

class InputOverloadViolation {
	void method(int a, int b) {}

	void method(int a) {} // violation: Overload 'method' with 1 parameters must appear before overload with 2 parameters.
}