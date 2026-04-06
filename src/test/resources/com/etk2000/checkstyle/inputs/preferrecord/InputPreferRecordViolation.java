package com.etk2000.checkstyle.inputs.preferrecord;

import javax.annotation.Nonnull;

class InputPreferRecordViolation { // violation: class should be a record
	final int x;
	final int y;

	InputPreferRecordViolation(int x, int y) {
		this.x = x;
		this.y = y;
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

/** Constructor has a local var, but field assignment is still simple. */
class ConstructorLocalAssignment { // violation: class should be a record
	final int value;

	ConstructorLocalAssignment(int value) {
		final var temp = value;
		this.value = temp;
	}
}

/** Constructor has bare field assignment (no this. prefix), skipped by check. */
class ConstructorBareAssignment { // violation: class should be a record
	final int value;

	ConstructorBareAssignment(int value) {
		value = value;
		this.value = value;
	}
}

/** Constructor assigns to another object's field, skipped by check. */
class ConstructorOtherAssignment { // violation: class should be a record
	final int value;

	ConstructorOtherAssignment(ConstructorOtherAssignment other, int value) {
		other.value = value;
		this.value = value;
	}
}

class EmptyConstructor { // violation: class should be a record
	final int value;

	EmptyConstructor(int value) {
		this.value = value;
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

/** Constructor has side effects, but field assignments are simple. */
class HasSideEffects { // violation: class should be a record
	final int id;

	HasSideEffects(int id) {
		this.id = id;
		System.out.println(id);
	}
}

/** Constructor has validation, but field assignments are simple. */
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

/** At least one constructor has simple assignments (the canonical one). */
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

/**
 * equals without @Override: still eligible for record.
 */
class NonOverrideEquals { // violation: class should be a record
	final int id;

	NonOverrideEquals(int id) {
		this.id = id;
	}

	public boolean equals(int other) {
		return id == other;
	}
}

/**
 * @Override on a non-record method: still eligible for record.
 */
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

class WithImplements implements Cloneable { // violation: class should be a record
	final int value;

	WithImplements(int value) {
		this.value = value;
	}
}