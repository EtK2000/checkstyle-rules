package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}

class InputAnnotationSameLineClean {
	record Data(@A String name, @A @B int value) {}

	InputAnnotationSameLineClean(@A String param) {}

	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (@A Exception e) {
			e.printStackTrace();
		}
	}

	void forEach(List<String> list) {
		for (@A var item : list)
			System.out.println(item);
	}

	void forEachMulti(List<String> list) {
		for (@A @B var item : list)
			System.out.println(item);
	}

	void forInit() {
		for (@A var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void forInitMulti() {
		for (@A @B var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void lambda() {
		final Consumer<String> c = (@A String s) -> {};
	}

	void method(@A String param) {}

	void multiParam(@A @B String param1, @A String param2) {}

	void noAnnotation(String param) {}

	// boundary: stacked field is NOT checked by SameLineCheck (own-line context)
	@A
	int stackedField;

	// boundary: stacked local is NOT checked (own-line context)
	void stackedLocal() {
		@A
		final var x = "test";
	}

	// boundary: stacked method is NOT checked by SameLineCheck (own-line context)
	@A
	void stackedMethod() {}
}