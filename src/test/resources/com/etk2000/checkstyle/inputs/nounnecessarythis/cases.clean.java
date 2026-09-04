package com.etk2000.checkstyle.inputs.nounnecessarythis;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

record InputThisCleanRecord(int field) {
	InputThisCleanRecord {
		this.field = field + 1;
	}
}

class InputThisCleanShadowingBindings {
	record Box(Object name) {}

	final Object name = "n";
	final boolean matches = name instanceof String name && name.equals(this.name);
	final Predicate<Object> sameName = name -> this.name.equals(name);
	final Runnable dump = () -> {
		final var name = "tmp";
		System.out.println(this.name + name);
	};
	final ToIntBiFunction<Object, Object> pairName = (name, other) -> this.name.hashCode() + name.hashCode() + other.hashCode();

	void catchParameter() {
		try {
			System.out.println(name);
		}
		catch (RuntimeException name) {
			System.out.println(this.name + name.getMessage());
		}
	}

	void forEachVariable(List<Object> values) {
		for (var name : values)
			System.out.println(this.name.toString() +name);
	}

	void forVariable() {
		for (var name = 0; name < 2; ++name)
			System.out.println(this.name.toString() +name);
	}

	void instanceofPattern(Object value) {
		if (value instanceof String name)
			System.out.println(this.name + name);
	}

	void recordPattern(Object value) {
		if (value instanceof Box(var name))
			System.out.println(this.name.toString() +name);
	}

	void switchPattern(Object value) {
		switch (value) {
			case String name -> System.out.println(this.name + name);
			default -> System.out.println(value);
		}
	}

	void tryResource() throws IOException {
		try (var name = new StringReader("")) {
			System.out.println(this.name.toString() +name.read());
		}
	}
}

class InputThisClean {
	int field;

	{}

	{
		this.field = 42;
	}

	// instance initializer: this.field needed because local variable shadows it
	{
		final var field = 99;
		System.out.println(this.field);
	}

	InputThisClean(int field) {
		this.field = field;
	}

	void lambdaLocalShadowing() {
		final Runnable r = () -> {
			final var field = 99;
			System.out.println(this.field);
		};
	}

	void lambdaParamShadowing() {
		final IntConsumer c = field -> System.out.println(this.field);
	}

	void lambdaWithShadowing(int field) {
		final Runnable r = () -> System.out.println(this.field);
	}

	void localShadowing() {
		final var field = 42;
		System.out.println(this.field);
	}

	void method(int field) {
		System.out.println(this.field);
	}
}

class InputThisCleanBase {
	int field;
}

// every cell here keeps its `this.` for a structural reason rather than a binding one: the DOT's
// first child is not LITERAL_THIS, or its second child is not an IDENT
class InputThisCleanQualifiers extends InputThisCleanBase {
	class Inner {
		int value;
	}

	int[] values;
	Inner inner;

	void arrayFieldAssignment(int[] other) {
		this.values = other;
	}

	Runnable methodReferenceOnThis() {
		return this::run;
	}

	int parenthesizedThis() {
		return (this).field;
	}

	<X> X pick() {
		return null;
	}

	void qualifiedFieldAssignment(Inner other) {
		this.inner = other;
	}

	Inner qualifiedNew() {
		return this.new Inner();
	}

	void register(Object value) {
		System.out.println(value);
	}

	void run() {}

	int superField() {
		return super.field;
	}

	void thisAsAnArgument() {
		register(this);
	}

	// a type witness puts TYPE_ARGUMENTS where the member name would be, so the check never fires
	<X> X typeWitness() {
		return this.<X>pick();
	}
}

// binding forms `collectBindings` reaches through an arm no other clean cell drives
class InputThisCleanBindingForms {
	record Box(Object name) {}

	record Nest(Box name) {}

	final Object name = "n";

	// an anonymous body is deliberately not excluded, so a name bound inside one still shadows
	void anonymousClassBodyBinding() {
		final var doubler = new InputThisCleanBase() {
			int doubled() {
				final var name = 2;
				return name * field;
			}
		};
		System.out.println(this.name.toString() +doubler.doubled());
	}

	void commaSeparatedForInit() {
		for (int i = 0, name = 1; i < 2; ++i)
			System.out.println(this.name.toString() +name);
	}

	void finallyBlockShadowing() {
		try {
			System.out.println(this.name);
		}
		finally {
			final var name = "f";
			System.out.println(name);
		}
	}

	void guardedSwitchPattern(Object value) {
		switch (value) {
			case String name when name.isEmpty() -> System.out.println(this.name);
			default -> System.out.println(value);
		}
	}

	void multiCatch() {
		try {
			System.out.println(name);
		}
		catch (IllegalArgumentException | IllegalStateException name) {
			System.out.println(this.name + name.getMessage());
		}
	}

	void negatedInstanceof(Object value) {
		if (!(value instanceof String name))
			System.out.println(this.name);
	}

	void nestedRecordPattern(Object value) {
		if (value instanceof Nest(Box(var name)))
			System.out.println(this.name.toString() +name);
	}

	void twoTryResources() throws IOException {
		try (var other = new StringReader(""); var name = new StringReader("")) {
			System.out.println(this.name.toString() +other.read() + name.read());
		}
	}

	Predicate<String> typedLambdaParam() {
		return (String name) -> this.name.equals(name);
	}

	Predicate<String> varLambdaParam() {
		return (var name) -> this.name.equals(name);
	}
}

// a resource written as a bare variable access binds that name, so the qualifier inside the block
// is kept although nothing shadows the field. The qualified form `try (this.reader)` binds nothing
// and is reported, which is the partner slice in cases.in.java
class InputThisCleanBareTryResource {
	final StringReader reader = new StringReader("");

	void m() throws IOException {
		try (reader) {
			System.out.println(this.reader.read());
		}
	}
}

class InputThisCleanOuter {
	class Inner {
		int field;

		int read() {
			return InputThisCleanOuter.this.field;
		}
	}

	int field;
}