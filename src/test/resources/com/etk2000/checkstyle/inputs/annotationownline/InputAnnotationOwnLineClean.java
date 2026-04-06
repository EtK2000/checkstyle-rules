package com.etk2000.checkstyle.inputs.annotationownline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}
@interface C {}

// stacked on class, alphabetically ordered
@A
class InputAnnotationOwnLineClean {
	// stacked on enum declaration
	@A
	enum Color {
		@A
		RED,

		@A
		@B
		GREEN
	}

	// stacked on interface
	@A
	interface Inner {}

	// stacked on annotation type
	@A
	@interface Meta {
		// stacked on annotation field, alphabetically ordered
		@A
		int priority();

		@A
		@B
		String value();
	}

	// stacked on record with compact constructor
	@A
	@B
	record Pair(@A int x, int y) {
		@A
		Pair {}
	}

	// stacked on field, no blank line between annotation and declaration
	@A
	int field;

	@A
	@B
	String multiAnnotatedField;

	// no annotation at all (clean)
	int plainField;

	// stacked on constructor
	@A
	InputAnnotationOwnLineClean() {}

	// for-each variable is NOT checked by OwnLineCheck (inline context, boundary)
	void forEach(List<String> list) {
		for (@A var item : list)
			System.out.println(item);
	}

	// lambda parameter annotation is NOT checked (inline context, boundary)
	void lambda() {
		Consumer<String> c = (@A String s) -> {};
	}

	// stacked on local variable
	void locals() {
		@A
		final var x = "hello";

		@A
		@B
		final var y = 42;
	}

	// stacked on method, alphabetically ordered
	@A
	void method() {}

	// multiple annotations each on own line, alphabetically ordered
	@A
	@B
	@C
	void multiAnnotatedMethod() {}

	// parameter annotation is NOT checked by OwnLineCheck (inline context, boundary)
	void paramMethod(@A String param) {}

	// no annotation on method (clean)
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