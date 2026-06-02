package com.etk2000.checkstyle.inputs.preferrecord;

@interface Ann {
}

// === case: annotated_array_field ===
// imports: javax.annotation.Nonnull
class InputPreferRecordAnnotatedArrayFieldSliceViolation {
	@Nonnull
	final int[] values;

	InputPreferRecordAnnotatedArrayFieldSliceViolation(@Nonnull int[] values) {
		this.values = values;
	}
}
// === end ===

// === case: annotated_field_only ===
// imports: javax.annotation.Nonnull
class InputPreferRecordAnnotatedFieldOnlySliceViolation {
	@Nonnull
	final String value;

	InputPreferRecordAnnotatedFieldOnlySliceViolation(String value) {
		this.value = value;
	}
}
// === end ===

// === case: annotated_fields ===
// imports: javax.annotation.Nonnull
class InputPreferRecordAnnotatedFieldsSliceViolation {
	final int value;
	@Nonnull
	final String name;

	InputPreferRecordAnnotatedFieldsSliceViolation(@Nonnull String name, int value) {
		this.name = name;
		this.value = value;
	}
}
// === end ===

// === case: annotated_param_only ===
// imports: javax.annotation.Nonnull
class InputPreferRecordAnnotatedParamOnlySliceViolation {
	final String value;

	InputPreferRecordAnnotatedParamOnlySliceViolation(@Nonnull String value) {
		this.value = value;
	}
}
// === end ===

// === case: array_field ===
class InputPreferRecordArrayFieldSliceViolation {
	final int[] values;

	InputPreferRecordArrayFieldSliceViolation(int[] values) {
		this.values = values;
	}
}
// === end ===

// === case: constructor_bare_assignment ===
class InputPreferRecordConstructorBareAssignmentSliceViolation {
	final int value;

	InputPreferRecordConstructorBareAssignmentSliceViolation(int value) {
		value = value;
		this.value = value;
	}
}
// === end ===

// === case: empty_constructor ===
class InputPreferRecordEmptyConstructorSliceViolation {
	final int value;

	InputPreferRecordEmptyConstructorSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: final_class ===
final class InputPreferRecordFinalClassSliceViolation {
	final int value;

	InputPreferRecordFinalClassSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: generic_annotated_type_arg ===
// imports: java.util.List
class InputPreferRecordGenericAnnotatedTypeArgSliceViolation {
	final List<@Ann String> items;

	InputPreferRecordGenericAnnotatedTypeArgSliceViolation(List<@Ann String> items) {
		this.items = items;
	}
}
// === end ===

// === case: generic_class ===
class InputPreferRecordGenericClassSliceViolation<T> {
	final T data;

	InputPreferRecordGenericClassSliceViolation(T data) {
		this.data = data;
	}
}
// === end ===

// === case: generic_class_annotated ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferRecordGenericClassAnnotatedSliceViolation {
	@Nonnull
	final List<String> items;

	InputPreferRecordGenericClassAnnotatedSliceViolation(@Nonnull List<String> items) {
		this.items = items;
	}
}
// === end ===

// === case: generic_class_concrete ===
// imports: java.util.List
class InputPreferRecordGenericClassConcreteSliceViolation {
	final List<String> items;

	InputPreferRecordGenericClassConcreteSliceViolation(List<String> items) {
		this.items = items;
	}
}
// === end ===

// === case: has_side_effects ===
class InputPreferRecordHasSideEffectsSliceViolation {
	final int id;

	InputPreferRecordHasSideEffectsSliceViolation(int id) {
		this.id = id;
		System.out.println(id);
	}
}
// === end ===

// === case: has_static_and_instance_fields ===
class InputPreferRecordHasStaticAndInstanceFieldsSliceViolation {
	static final int CONSTANT = 42;
	final String data;

	InputPreferRecordHasStaticAndInstanceFieldsSliceViolation(String data) {
		this.data = data;
	}
}
// === end ===

// === case: has_validation ===
class InputPreferRecordHasValidationSliceViolation {
	final int value;

	InputPreferRecordHasValidationSliceViolation(int value) {
		if (value < 0)
			throw new IllegalArgumentException();
		this.value = value;
	}
}
// === end ===

// === case: input_prefer_record_violation ===
class InputPreferRecordViolation {
	final int x, y;

	InputPreferRecordViolation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
// === end ===

// === case: methods_present ===
class InputPreferRecordMethodsPresentSliceViolation {
	final int value;

	InputPreferRecordMethodsPresentSliceViolation(int value) {
		this.value = value;
	}

	int doubleValue() {
		return value * 2;
	}
}
// === end ===

// === case: multiple_constructors ===
class InputPreferRecordMultipleConstructorsSliceViolation {
	final int value;

	InputPreferRecordMultipleConstructorsSliceViolation() {
		this.value = 0;
		System.out.println("created");
	}

	InputPreferRecordMultipleConstructorsSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: multiple_field_types ===
class InputPreferRecordMultipleFieldTypesSliceViolation {
	final int value;
	final String name;

	InputPreferRecordMultipleFieldTypesSliceViolation(String name, int value) {
		this.name = name;
		this.value = value;
	}
}
// === end ===

// === case: nested_violation ===
class InputPreferRecordNestedViolationSliceOuter {
	static class InputPreferRecordNestedViolationSliceViolation {
		final int value;

		InputPreferRecordNestedViolationSliceViolation(int value) {
			this.value = value;
		}
	}
}
// === end ===

// === case: non_override_equals ===
class InputPreferRecordNonOverrideEqualsSliceViolation {
	final int id;

	InputPreferRecordNonOverrideEqualsSliceViolation(int id) {
		this.id = id;
	}

	public boolean equals(int other) {
		return id == other;
	}
}
// === end ===

// === case: override_non_record_method ===
class InputPreferRecordOverrideNonRecordMethodSliceViolation {
	final int value;

	InputPreferRecordOverrideNonRecordMethodSliceViolation(int value) {
		this.value = value;
	}

	@Override
	protected Object clone() {
		return new InputPreferRecordOverrideNonRecordMethodSliceViolation(value);
	}
}
// === end ===

// === case: same_line_fields ===
class InputPreferRecordSameLineFieldsSliceViolation {
	final int x, y;

	InputPreferRecordSameLineFieldsSliceViolation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
// === end ===

// === case: single_field ===
class InputPreferRecordSingleFieldSliceViolation {
	final long timestamp;

	InputPreferRecordSingleFieldSliceViolation(long timestamp) {
		this.timestamp = timestamp;
	}
}
// === end ===

// === case: suppressed_wrong_key ===
@SuppressWarnings("unused")
class InputPreferRecordSuppressedWrongKeySliceViolation {
	final int value;

	InputPreferRecordSuppressedWrongKeySliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: suppressed_wrong_key_explicit ===
@SuppressWarnings("unused")
class InputPreferRecordSuppressedWrongKeyExplicitSliceViolation {
	final int value;

	InputPreferRecordSuppressedWrongKeyExplicitSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: with_generic_implements ===
class InputPreferRecordWithGenericImplementsSliceViolation implements Comparable<InputPreferRecordWithGenericImplementsSliceViolation> {
	final int value;

	InputPreferRecordWithGenericImplementsSliceViolation(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(InputPreferRecordWithGenericImplementsSliceViolation o) {
		return Integer.compare(value, o.value);
	}
}
// === end ===

// === case: with_implements ===
class InputPreferRecordWithImplementsSliceViolation implements Cloneable {
	final int value;

	InputPreferRecordWithImplementsSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: with_implements_suppressed_wrong_key ===
@SuppressWarnings("unused")
class InputPreferRecordWithImplementsSuppressedWrongKeySliceViolation implements Cloneable {
	final int value;

	InputPreferRecordWithImplementsSuppressedWrongKeySliceViolation(int value) {
		this.value = value;
	}
}

@SuppressWarnings("PreferRecord")
class InputPreferRecordWithImplementsSuppressedWrongKeySliceSibling {
	final int value;

	InputPreferRecordWithImplementsSuppressedWrongKeySliceSibling(int value) {
		this.value = value;
	}
}
// === end ===

// === case: with_multiple_implements ===
class InputPreferRecordWithMultipleImplementsSliceViolation implements Cloneable, Comparable<InputPreferRecordWithMultipleImplementsSliceViolation> {
	final int value;

	InputPreferRecordWithMultipleImplementsSliceViolation(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(InputPreferRecordWithMultipleImplementsSliceViolation o) {
		return Integer.compare(value, o.value);
	}
}
// === end ===