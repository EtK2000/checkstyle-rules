package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}
@interface C {}

class InputAnnotationSameLineOrderViolation {
	record Data(@B @A String name) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	InputAnnotationSameLineOrderViolation(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (@B @A Exception e) { // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			e.printStackTrace();
		}
	}

	void forEach(List<String> list) {
		for (@B @A var item : list) // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			System.out.println(item);
	}

	void forInit() {
		for (@B @A var i = 0; i < 10; ++i) // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			System.out.println(i);
	}

	void lambda() {
		final Consumer<String> c = (@B @A String s) -> {}; // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	}

	void method(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	void multiParam(
			@C @A String param1, // violation: Annotation 'A' must appear before 'C' (alphabetical order).
			@A @B String param2
	) {}
}