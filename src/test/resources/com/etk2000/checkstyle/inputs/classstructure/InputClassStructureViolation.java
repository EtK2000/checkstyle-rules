package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureViolation {
	void instanceMethod() {}

	static void staticMethod() {} // violation: 'staticMethod' (static method) must appear before instance method section.
}