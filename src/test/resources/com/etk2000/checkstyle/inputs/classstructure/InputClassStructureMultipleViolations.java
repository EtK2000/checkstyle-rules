package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureMultipleViolations {
	void instance1() {}

	static void static1() {} // violation: 'Inner' (inner type) must appear before instance method section.

	void instance2() {}

	static void static2() {} // violation: 'static2' (static method) must appear before instance method section.
}