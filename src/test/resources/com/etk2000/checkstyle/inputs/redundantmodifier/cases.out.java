package com.etk2000.checkstyle.inputs.redundantmodifier;

// === case: remove_abstract ===
interface InputRedundantModifierAbstractSliceViolation {
	void method();
}
// === end ===

// === case: remove_final_from_interface_field ===
interface InputRedundantModifierFinalFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_final_from_interface_field_followed_by_tab ===
interface InputRedundantModifierFinalFieldTabSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_final_from_resource ===
class InputRedundantModifierFinalResourceSliceViolation {
	void m(AutoCloseable closeable) throws Exception {
		try (var resource = closeable) {
			resource.toString();
		}
	}
}
// === end ===

// === case: remove_final_from_resource_explicit_type ===
class InputRedundantModifierFinalResourceExplicitTypeSliceViolation {
	void m(AutoCloseable closeable) throws Exception {
		try (AutoCloseable resource = closeable) {
			resource.toString();
		}
	}
}
// === end ===

// === case: remove_final_from_resource_multi ===
class InputRedundantModifierFinalResourceMultiSliceViolation {
	void m(AutoCloseable first, AutoCloseable second) throws Exception {
		try (var a = first; var b = second) {
			a.toString();
			b.toString();
		}
	}
}
// === end ===

// === case: remove_public_abstract_from_annotation_element ===
@interface InputRedundantModifierPublicAbstractAnnotationElementSliceViolation {
	int value();
}
// === end ===

// === case: remove_public_abstract_from_interface_method ===
interface InputRedundantModifierPublicAbstractMethodSliceViolation {
	void method();
}
// === end ===

// === case: remove_public_final_from_interface_field ===
interface InputRedundantModifierPublicFinalFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_public_from_interface ===
interface InputRedundantModifierPublicInterfaceSliceViolation {
	void method();
}
// === end ===

// === case: remove_public_from_interface_field ===
interface InputRedundantModifierPublicFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_public_on_own_line ===
interface InputRedundantModifierPublicOwnLineSliceViolation {
	void method();
}
// === end ===

// === case: remove_public_static_final_from_annotation_field ===
@interface InputRedundantModifierPublicStaticFinalAnnotationFieldSliceViolation {
	int FIELD = 1;
}
// === end ===

// === case: remove_public_static_final_from_interface_field ===
interface InputRedundantModifierPublicStaticFinalFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_public_static_from_interface_field ===
interface InputRedundantModifierPublicStaticFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_public_static_from_interface_nested_class ===
interface InputRedundantModifierPublicStaticNestedClassSliceViolation {
	class Nested {}
}
// === end ===

// === case: remove_static_final_from_interface_field ===
interface InputRedundantModifierStaticFinalFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_static_from_interface_field ===
interface InputRedundantModifierStaticFieldSliceViolation {
	int X = 1;
}
// === end ===

// === case: remove_static_from_interface_nested_annotation ===
interface InputRedundantModifierStaticNestedAnnotationSliceViolation {
	@interface Nested {}
}
// === end ===

// === case: remove_static_from_interface_nested_class ===
interface InputRedundantModifierStaticNestedClassSliceViolation {
	class Nested {}
}
// === end ===

// === case: remove_static_from_interface_nested_enum ===
interface InputRedundantModifierStaticNestedEnumSliceViolation {
	enum Nested {}
}
// === end ===

// === case: remove_static_from_interface_nested_interface ===
interface InputRedundantModifierStaticNestedInterfaceSliceViolation {
	interface Nested {}
}
// === end ===

// === case: remove_static_with_annotation ===
interface InputRedundantModifierAnnotatedStaticFieldSliceViolation {
	@Deprecated
	int X = 1;
}
// === end ===

// === case: remove_strictfp ===
class InputRedundantModifierStrictfpSliceViolation {
	void m() {}
}
// === end ===