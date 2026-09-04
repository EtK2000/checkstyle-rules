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