package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureInstanceInitViolation {
	void method() {}

	{ } // violation: '<instance init>' (constructor/instance initializer) must appear before instance method section.
}