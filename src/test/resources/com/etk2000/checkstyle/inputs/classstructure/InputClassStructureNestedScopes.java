package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureNestedScopes {
	void outerInstance() {}

	static class Inner {
		static void innerStatic() {}

		void innerInstance() {}
	}

	static void outerStatic() {}
}