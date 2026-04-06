package com.etk2000.checkstyle.inputs.unnecessarythis;

import java.util.function.IntConsumer;

class InputThisClean {
	int field;

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

	// lambda: this.field needed because lambda-local variable shadows it
	void lambdaLocalShadowing() {
		final Runnable r = () -> {
			final var field = 99;
			System.out.println(this.field);
		};
	}

	// lambda: this.field needed because lambda parameter shadows it
	void lambdaParamShadowing() {
		final IntConsumer c = field -> System.out.println(this.field);
	}

	// lambda: this.field needed because outer method param shadows it
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