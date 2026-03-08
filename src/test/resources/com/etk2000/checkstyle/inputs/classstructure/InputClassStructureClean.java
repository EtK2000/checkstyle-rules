package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureClean {
	static class Inner {}

	interface InnerInterface {}

	record InnerRecord() {}

	static int STATIC_FIELD = 1;

	static { }

	static void staticMethod() {}

	int instanceField;

	InputClassStructureClean() {}

	{ }

	void instanceMethod() {}
}