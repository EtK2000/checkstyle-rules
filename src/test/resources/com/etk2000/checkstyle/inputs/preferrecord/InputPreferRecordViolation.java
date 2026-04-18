package com.etk2000.checkstyle.inputs.preferrecord;

import java.util.List;

import javax.annotation.Nonnull;

@interface Ann {
}

class InputPreferRecordViolation { // violation: class should be a record
	final int x;
	final int y;

	InputPreferRecordViolation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class ArrayField { // violation: class should be a record
	final int[] values;

	ArrayField(int[] values) {
		this.values = values;
	}
}

class AnnotatedArrayField { // violation: class should be a record
	@Nonnull
	final int[] values;

	AnnotatedArrayField(@Nonnull int[] values) {
		this.values = values;
	}
}

class AnnotatedFields { // violation: class should be a record
	final int value;
	@Nonnull
	final String name;

	AnnotatedFields(@Nonnull String name, int value) {
		this.name = name;
		this.value = value;
	}
}

class AnnotatedFieldOnly { // violation: class should be a record
	@Nonnull
	final String value;

	AnnotatedFieldOnly(String value) {
		this.value = value;
	}
}

class AnnotatedParamOnly { // violation: class should be a record
	final String value;

	AnnotatedParamOnly(@Nonnull String value) {
		this.value = value;
	}
}

class ConstructorLocalAssignment { // violation: class should be a record
	final int value;

	ConstructorLocalAssignment(int value) {
		final var temp = value;
		this.value = temp;
	}
}

class ConstructorBareAssignment { // violation: class should be a record
	final int value;

	ConstructorBareAssignment(int value) {
		value = value;
		this.value = value;
	}
}

class EmptyConstructor { // violation: class should be a record
	final int value;

	EmptyConstructor(int value) {
		this.value = value;
	}
}

class GenericAnnotatedTypeArg { // violation: class should be a record
	final List<@Ann String> items;

	GenericAnnotatedTypeArg(List<@Ann String> items) {
		this.items = items;
	}
}

class GenericClassAnnotated { // violation: class should be a record
	@Nonnull
	final List<String> items;

	GenericClassAnnotated(@Nonnull List<String> items) {
		this.items = items;
	}
}

class GenericClassConcrete { // violation: class should be a record
	final List<String> items;

	GenericClassConcrete(List<String> items) {
		this.items = items;
	}
}

class GenericClass<T> { // violation: class should be a record
	final T data;

	GenericClass(T data) {
		this.data = data;
	}
}

class HasStaticAndInstanceFields { // violation: class should be a record
	static final int CONSTANT = 42;
	final String data;

	HasStaticAndInstanceFields(String data) {
		this.data = data;
	}
}

class HasSideEffects { // violation: class should be a record
	final int id;

	HasSideEffects(int id) {
		this.id = id;
		System.out.println(id);
	}
}

class HasValidation { // violation: class should be a record
	final int value;

	HasValidation(int value) {
		if (value < 0)
			throw new IllegalArgumentException();
		this.value = value;
	}
}

class MethodsPresent { // violation: class should be a record
	final int value;

	MethodsPresent(int value) {
		this.value = value;
	}

	int doubleValue() {
		return value * 2;
	}
}

class MultipleConstructors { // violation: class should be a record
	final int value;

	MultipleConstructors() {
		this.value = 0;
		System.out.println("created");
	}

	MultipleConstructors(int value) {
		this.value = value;
	}
}

class MultipleFieldTypes { // violation: class should be a record
	final int value;
	final String name;

	MultipleFieldTypes(String name, int value) {
		this.name = name;
		this.value = value;
	}
}

class NonOverrideEquals { // violation: class should be a record
	final int id;

	NonOverrideEquals(int id) {
		this.id = id;
	}

	public boolean equals(int other) {
		return id == other;
	}
}

class OverrideNonRecordMethod { // violation: class should be a record
	final int value;

	OverrideNonRecordMethod(int value) {
		this.value = value;
	}

	@Override
	protected Object clone() {
		return new OverrideNonRecordMethod(value);
	}
}

class SameLineFields { // violation: class should be a record
	final int x, y;

	SameLineFields(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class SingleField { // violation: class should be a record
	final long timestamp;

	SingleField(long timestamp) {
		this.timestamp = timestamp;
	}
}

class WithGenericImplements implements Comparable<WithGenericImplements> { // violation (warning): class should be a record
	final int value;

	WithGenericImplements(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(WithGenericImplements o) {
		return Integer.compare(value, o.value);
	}
}

class WithImplements implements Cloneable { // violation (warning): class should be a record
	final int value;

	WithImplements(int value) {
		this.value = value;
	}
}

class WithMultipleImplements implements Cloneable, Comparable<WithMultipleImplements> { // violation (warning): class should be a record
	final int value;

	WithMultipleImplements(int value) {
		this.value = value;
	}

	@Override
	public int compareTo(WithMultipleImplements o) {
		return Integer.compare(value, o.value);
	}
}

@SuppressWarnings("unused")
class SuppressedWrongKey { // violation: class should be a record
	final int value;

	SuppressedWrongKey(int value) {
		this.value = value;
	}
}

@SuppressWarnings(value = "unused")
class SuppressedWrongKeyExplicit { // violation: class should be a record
	final int value;

	SuppressedWrongKeyExplicit(int value) {
		this.value = value;
	}
}

@SuppressWarnings("PreferRecord")
class SuppressedSibling {
	final int value;

	SuppressedSibling(int value) {
		this.value = value;
	}
}