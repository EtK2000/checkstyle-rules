package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}

class InputAnnotationSameLineClean {
	// clean: record component with inline annotation
	record Data(@A String name, @A @B int value) {}

	// clean: constructor parameter annotation inline
	InputAnnotationSameLineClean(@A String param) {}

	// clean: catch parameter with inline annotation
	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (@A Exception e) {
			e.printStackTrace();
		}
	}

	// clean: for-each with inline annotation
	void forEach(List<String> list) {
		for (@A var item : list)
			System.out.println(item);
	}

	// clean: for-each with multiple inline annotations
	void forEachMulti(List<String> list) {
		for (@A @B var item : list)
			System.out.println(item);
	}

	// clean: for-init with inline annotation
	void forInit() {
		for (@A var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	// clean: for-init with multiple inline annotations
	void forInitMulti() {
		for (@A @B var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	// clean: lambda parameter with inline annotation
	void lambda() {
		final Consumer<String> c = (@A String s) -> {};
	}

	// clean: parameter annotations inline with declaration
	void method(@A String param) {}

	// clean: multiple annotations inline on parameter
	void multiParam(@A @B String param1, @A String param2) {}

	// clean: no annotation on parameter
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