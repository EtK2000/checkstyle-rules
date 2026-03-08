package com.etk2000.checkstyle.inputs;

class InputNestedScopes {
	void outerInstance() {}

	static class Inner {
		static void innerStatic() {}

		void innerInstance() {}
	}

	static void outerStatic() {}
}