package com.etk2000.checkstyle.inputs.preferrecord;

import java.util.Map;

/** Abstract class: records can't be abstract. */
abstract class AbstractClass {
	final int value;

	AbstractClass(int value) {
		this.value = value;
	}
}

/** All constructors have non-simple field assignments. */
class AllConstructorsIneligible {
	final int value;

	AllConstructorsIneligible() {
		this.value = 0;
	}

	AllConstructorsIneligible(String s) {
		this.value = Integer.parseInt(s);
	}
}

record AlreadyRecord(int x, int y) {}

/** Class extending another class: records implicitly extend Record. */
class ChildClass extends AbstractClass {
	final String name;

	ChildClass(String name) {
		super(0);
		this.name = name;
	}
}

/** Constructor RHS is not a simple identifier (expression). */
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

/** Fields initialized inline: can't be record components. */
class InlineInitializedFields {
	final int x = 1;
	final int y = 2;
}

/** Instance initializer present. */
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

/** Constructor creates a new object on RHS (not a simple parameter). */
class RhsNotSimple {
	final Object data;

	RhsNotSimple() {
		this.data = new Object();
	}
}