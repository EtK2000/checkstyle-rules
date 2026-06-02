package com.etk2000.checkstyle.inputs.classstructureorder;

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

class InputClassStructureOnlyInstance {
	void a() {}

	void b() {}
}

class InputClassStructureOnlyStatic {
	static void a() {}

	static void b() {}
}