package com.etk2000.checkstyle.inputs.redundantmodifier;

// === case: remove_abstract ===
interface InputRedundantModifierAbstractSliceViolation {
	abstract void method(); // violation: Redundant 'abstract' modifier.
}
// === end ===

// === case: remove_final_from_interface_field ===
interface InputRedundantModifierFinalFieldSliceViolation {
	final int X = 1; // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_final_from_interface_field_followed_by_tab ===
interface InputRedundantModifierFinalFieldTabSliceViolation {
	final	int X = 1; // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_final_from_resource ===
class InputRedundantModifierFinalResourceSliceViolation {
	void m(AutoCloseable closeable) throws Exception {
		try (final var resource = closeable) { // violation: Redundant 'final' modifier.
			resource.toString();
		}
	}
}
// === end ===

// === case: remove_final_from_resource_explicit_type ===
class InputRedundantModifierFinalResourceExplicitTypeSliceViolation {
	void m(AutoCloseable closeable) throws Exception {
		try (final AutoCloseable resource = closeable) { // violation: Redundant 'final' modifier.
			resource.toString();
		}
	}
}
// === end ===

// === case: remove_final_from_resource_multi ===
class InputRedundantModifierFinalResourceMultiSliceViolation {
	void m(AutoCloseable first, AutoCloseable second) throws Exception {
		try (final var a = first; final var b = second) { // violation: Redundant 'final' modifier. // violation: Redundant 'final' modifier.
			a.toString();
			b.toString();
		}
	}
}
// === end ===

// === case: remove_public_abstract_from_annotation_element ===
@interface InputRedundantModifierPublicAbstractAnnotationElementSliceViolation {
	public abstract int value(); // violation: Redundant 'public' modifier. // violation: Redundant 'abstract' modifier.
}
// === end ===

// === case: remove_public_abstract_from_interface_method ===
interface InputRedundantModifierPublicAbstractMethodSliceViolation {
	public abstract void method(); // violation: Redundant 'public' modifier. // violation: Redundant 'abstract' modifier.
}
// === end ===

// === case: remove_public_final_from_interface_field ===
interface InputRedundantModifierPublicFinalFieldSliceViolation {
	public final int X = 1; // violation: Redundant 'public' modifier. // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_public_from_interface ===
interface InputRedundantModifierPublicInterfaceSliceViolation {
	public void method(); // violation: Redundant 'public' modifier.
}
// === end ===

// === case: remove_public_from_interface_field ===
interface InputRedundantModifierPublicFieldSliceViolation {
	public int X = 1; // violation: Redundant 'public' modifier.
}
// === end ===

// === case: remove_public_on_own_line ===
interface InputRedundantModifierPublicOwnLineSliceViolation {
	public // violation: Redundant 'public' modifier.
	void method();
}
// === end ===

// === case: remove_public_static_final_from_annotation_field ===
@interface InputRedundantModifierPublicStaticFinalAnnotationFieldSliceViolation {
	public static final int FIELD = 1; // violation: Redundant 'public' modifier. // violation: Redundant 'static' modifier. // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_public_static_final_from_interface_field ===
interface InputRedundantModifierPublicStaticFinalFieldSliceViolation {
	public static final int X = 1; // violation: Redundant 'public' modifier. // violation: Redundant 'static' modifier. // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_public_static_from_interface_field ===
interface InputRedundantModifierPublicStaticFieldSliceViolation {
	public static int X = 1; // violation: Redundant 'public' modifier. // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_public_static_from_interface_nested_class ===
interface InputRedundantModifierPublicStaticNestedClassSliceViolation {
	public static class Nested {} // violation: Redundant 'public' modifier. // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_final_from_interface_field ===
interface InputRedundantModifierStaticFinalFieldSliceViolation {
	static final int X = 1; // violation: Redundant 'static' modifier. // violation: Redundant 'final' modifier.
}
// === end ===

// === case: remove_static_from_interface_field ===
interface InputRedundantModifierStaticFieldSliceViolation {
	static int X = 1; // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_from_interface_nested_annotation ===
interface InputRedundantModifierStaticNestedAnnotationSliceViolation {
	static @interface Nested {} // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_from_interface_nested_class ===
interface InputRedundantModifierStaticNestedClassSliceViolation {
	static class Nested {} // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_from_interface_nested_enum ===
interface InputRedundantModifierStaticNestedEnumSliceViolation {
	static enum Nested {} // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_from_interface_nested_interface ===
interface InputRedundantModifierStaticNestedInterfaceSliceViolation {
	static interface Nested {} // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_static_with_annotation ===
interface InputRedundantModifierAnnotatedStaticFieldSliceViolation {
	@Deprecated
	static int X = 1; // violation: Redundant 'static' modifier.
}
// === end ===

// === case: remove_strictfp ===
class InputRedundantModifierStrictfpSliceViolation {
	strictfp void m() {} // violation: Redundant 'strictfp' modifier.
}
// === end ===