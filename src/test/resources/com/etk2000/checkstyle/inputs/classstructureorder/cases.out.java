package com.etk2000.checkstyle.inputs.classstructureorder;

// === case: constructor_then_static ===
class InputClassStructureConstructorThenStaticViolation {
	InputClassStructureConstructorThenStaticViolation() {}

	static void staticMethod() {}
}
// === end ===

// === case: enum_static_after_instance ===
enum InputClassStructureEnumViolation {
	A,
	B;

	void instanceMethod() {}

	static void staticMethod() {}
}
// === end ===

// === case: field_after_method ===
class InputClassStructureFieldAfterMethodViolation {
	void method() {}

	int field;
}
// === end ===

// === case: inner_type_after_method ===
class InputClassStructureInnerTypeAfterMethodViolation {
	void method() {}

	static class Inner {}
}
// === end ===

// === case: instance_init_after_method ===
class InputClassStructureInstanceInitAfterMethodViolation {
	void method() {}

	{ }
}
// === end ===

// === case: multiple_violations ===
class InputClassStructureMultipleViolationsViolation {
	void instance1() {}

	static void static1() {}

	void instance2() {}

	static void static2() {}
}
// === end ===

// === case: nested_scopes ===
class InputClassStructureNestedScopesViolation {
	void outerInstance() {}

	static class Inner {
		static void innerStatic() {}

		void innerInstance() {}
	}

	static void outerStatic() {}
}
// === end ===

// === case: static_after_instance_method ===
class InputClassStructureStaticAfterInstanceMethodViolation {
	void instanceMethod() {}

	static void staticMethod() {}
}
// === end ===