package com.etk2000.checkstyle.inputs.annotationownline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}
@interface C {}

@A
class InputAnnotationOwnLineClean {
	@A
	enum Color {
		@A
		RED,

		@A
		@B
		GREEN
	}

	@A
	interface Inner {}

	@A
	@interface Meta {
		@A
		int priority();

		@A
		@B
		String value();
	}

	@A
	@B
	record Pair(@A int x, int y) {
		@A
		Pair {}
	}

	@A
	int field;

	@A
	@B
	String multiAnnotatedField;

	int plainField;

	@A
	InputAnnotationOwnLineClean() {}

	// for-each variable is NOT checked by OwnLineCheck (inline context, boundary)
	void forEach(List<String> list) {
		for (@A var item : list)
			System.out.println(item);
	}

	// for-init variable is NOT checked by OwnLineCheck (inline context, inside parentheses)
	void forInit() {
		for (@A var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	// lambda parameter annotation is NOT checked (inline context, boundary)
	void lambda() {
		final Consumer<String> c = (@A String s) -> {};
	}

	void locals() {
		@A
		final var x = "hello";

		@A
		@B
		final var y = 42;
	}

	@A
	void method() {}

	@A
	@B
	@C
	void multiAnnotatedMethod() {}

	// parameter annotation is NOT checked by OwnLineCheck (inline context, boundary)
	void paramMethod(@A String param) {}

	void plainMethod() {}

	// catch parameter is NOT checked (inline context, boundary)
	void tryCatch() {
		try {
			Thread.sleep(1);
		}
		catch (@A Exception e) {
			e.printStackTrace();
		}
	}
}