package com.etk2000.checkstyle.inputs.preferrecord;

import java.util.List;
import java.util.Map;

@interface Ann {}

abstract class AbstractClass {
	final int value;

	AbstractClass(int value) {
		this.value = value;
	}
}

class AllConstructorsIneligible {
	final int value;

	AllConstructorsIneligible() {
		this.value = 0;
	}

	AllConstructorsIneligible(String s) {
		this.value = Integer.parseInt(s);
	}
}

class AllConstructorsWrongTypes {
	final long value;

	AllConstructorsWrongTypes(int v) {
		this.value = v;
	}

	AllConstructorsWrongTypes(int a, int b) {
		this.value = a;
	}

	AllConstructorsWrongTypes(String s) {
		this.value = s.length();
	}
}

record AlreadyRecord(int x, int y) {}

class ArrayFieldWrongType {
	final int[] values;

	ArrayFieldWrongType(int values) {
		this.values = values;
	}
}

class ChildClass extends AbstractClass {
	final String name;

	ChildClass(String name) {
		super(0);
		this.name = name;
	}
}

class GenericAnnotatedTypeArgWrongType {
	final List<@Ann String> items;

	GenericAnnotatedTypeArgWrongType(Map<@Ann String, @Ann String> items) {
		this.items = items;
	}
}

class GenericFieldWrongType {
	final List<String> items;

	GenericFieldWrongType(Map<String, String> items) {
		this.items = items;
	}
}

class ConstructorLocalAssignment {
	final int value;

	ConstructorLocalAssignment(int value) {
		final var temp = value;
		this.value = temp;
	}
}

class ConstructorMixedAssignment {
	final int first;
	final int second;

	ConstructorMixedAssignment(int first, int second) {
		this.first = first;
		this.second = 42;
	}
}

class ConstructorRenamedAssignment {
	final int val;

	ConstructorRenamedAssignment(int value) {
		this.val = value;
	}
}

class ConstructorNonParamAssignment {
	static final int DEFAULT_VALUE = 30;
	final int value;

	ConstructorNonParamAssignment(int value) {
		this.value = DEFAULT_VALUE;
	}
}

class ConstructorSwappedAssignment {
	final int a;
	final int b;

	ConstructorSwappedAssignment(int a, int b) {
		this.a = b;
		this.b = a;
	}
}

class ConstructorOtherAssignment {
	final int value;

	ConstructorOtherAssignment(ConstructorOtherAssignment other, int value) {
		other.value = value;
		this.value = value;
	}
}

class ConstructorWithExpression {
	final int end;
	final int start;

	ConstructorWithExpression(int start) {
		this.end = start + 10;
		this.start = start;
	}
}

class CustomEquals {
	final int id;

	CustomEquals(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof CustomEquals c && c.id == id;
	}
}

class CustomHashCode {
	final String name;

	CustomHashCode(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

class CustomToString {
	final double value;

	CustomToString(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "val=" + value;
	}
}

class InlineInitializedFields {
	final int x = 1;
	final int y = 2;
}

class FewerParamsThanFields {
	final int a;
	final int b;

	FewerParamsThanFields(int value) {
		this.a = value;
		this.b = value;
	}
}

class EmptyInstanceInit {
	final int value;

	{}

	EmptyInstanceInit(int value) {
		this.value = value;
	}
}

class InstanceInit {
	final Map<String, String> map;

	{
		map = Map.of();
	}
}

class MixedInitialization {
	final int computed;
	final int constant = 42;

	MixedInitialization(int computed) {
		this.computed = computed;
	}
}

class NoFields {
	void doSomething() {}
}

class NoInstanceFields {
	static final int CONSTANT = 42;

	static int count() {
		return CONSTANT;
	}
}

class NonFinalField {
	int mutable;
	final int immutable;

	NonFinalField(int mutable, int immutable) {
		this.immutable = immutable;
		this.mutable = mutable;
	}
}

class RhsNotSimple {
	final Object data;

	RhsNotSimple() {
		this.data = new Object();
	}
}

class WithExtendsAndImplements extends AbstractClass implements Cloneable {
	final String name;

	WithExtendsAndImplements(String name) {
		super(0);
		this.name = name;
	}
}

class WithImplementsNonFinalField implements Cloneable {
	int mutable;
	final int immutable;

	WithImplementsNonFinalField(int mutable, int immutable) {
		this.immutable = immutable;
		this.mutable = mutable;
	}
}

class WrongParamType {
	final long value;

	WrongParamType(int value) {
		this.value = value;
	}
}

@SuppressWarnings("PreferRecord")
class SuppressedSimple {
	final int value;

	SuppressedSimple(int value) {
		this.value = value;
	}
}

@SuppressWarnings({"PreferRecord"})
class SuppressedArray {
	final int value;

	SuppressedArray(int value) {
		this.value = value;
	}
}

@SuppressWarnings({"PreferRecord", "unused"})
class SuppressedMixed {
	final int value;

	SuppressedMixed(int value) {
		this.value = value;
	}
}

@SuppressWarnings({"unused", "PreferRecord"})
class SuppressedKeySecond {
	final int value;

	SuppressedKeySecond(int value) {
		this.value = value;
	}
}

@SuppressWarnings("PreferRecord")
class SuppressedWithImplements implements Cloneable {
	final int value;

	SuppressedWithImplements(int value) {
		this.value = value;
	}
}

class SuppressedInnerOuter {
	@SuppressWarnings("PreferRecord")
	static class Inner {
		final int value;

		Inner(int value) {
			this.value = value;
		}
	}
}

@SuppressWarnings(value = "PreferRecord")
class SuppressedExplicitValue {
	final int value;

	SuppressedExplicitValue(int value) {
		this.value = value;
	}
}

@SuppressWarnings(value = {"PreferRecord"})
class SuppressedExplicitValueArray {
	final int value;

	SuppressedExplicitValueArray(int value) {
		this.value = value;
	}
}

@SuppressWarnings(value = {"PreferRecord", "unused"})
class SuppressedExplicitValueMixed {
	final int value;

	SuppressedExplicitValueMixed(int value) {
		this.value = value;
	}
}

@SuppressWarnings(value = {"unused", "PreferRecord"})
class SuppressedExplicitValueMixedSecond {
	final int value;

	SuppressedExplicitValueMixedSecond(int value) {
		this.value = value;
	}
}

@java.lang.SuppressWarnings("PreferRecord")
class SuppressedQualified {
	final int value;

	SuppressedQualified(int value) {
		this.value = value;
	}
}