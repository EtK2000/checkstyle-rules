package com.etk2000.checkstyle.inputs.classstructureorder;

// === case: constructor_then_static ===
class InputClassStructureConstructorThenStaticViolation {
	InputClassStructureConstructorThenStaticViolation() {}

	static void staticMethod() {} // violation: 'staticMethod' (static method) must appear before constructor/instance initializer section.
}
// === end ===

// === case: enum_static_after_instance ===
enum InputClassStructureEnumViolation {
	A,
	B;

	void instanceMethod() {}

	static void staticMethod() {} // violation: 'staticMethod' (static method) must appear before instance method section.
}
// === end ===

// === case: field_after_method ===
class InputClassStructureFieldAfterMethodViolation {
	void method() {}

	int field; // violation: 'field' (instance field) must appear before instance method section.
}
// === end ===

// === case: inner_type_after_method ===
class InputClassStructureInnerTypeAfterMethodViolation {
	void method() {}

	static class Inner {} // violation: 'Inner' (inner type) must appear before instance method section.
}
// === end ===

// === case: instance_init_after_method ===
class InputClassStructureInstanceInitAfterMethodViolation {
	void method() {}

	{ } // violation: '<instance init>' (constructor/instance initializer) must appear before instance method section.
}
// === end ===

// === case: multiple_violations ===
class InputClassStructureMultipleViolationsViolation {
	void instance1() {}

	static void static1() {} // violation: 'static1' (static method) must appear before instance method section.

	void instance2() {}

	static void static2() {} // violation: 'static2' (static method) must appear before instance method section.
}
// === end ===

// === case: nested_scopes ===
class InputClassStructureNestedScopesViolation {
	void outerInstance() {}

	static class Inner { // violation: 'Inner' (inner type) must appear before instance method section.
		static void innerStatic() {}

		void innerInstance() {}
	}

	static void outerStatic() {} // violation: 'outerStatic' (static method) must appear before instance method section.
}
// === end ===

// === case: static_after_instance_method ===
class InputClassStructureStaticAfterInstanceMethodViolation {
	void instanceMethod() {}

	static void staticMethod() {} // violation: 'staticMethod' (static method) must appear before instance method section.
}
// === end ===