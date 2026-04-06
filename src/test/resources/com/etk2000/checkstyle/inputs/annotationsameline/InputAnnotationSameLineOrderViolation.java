package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}
@interface C {}

class InputAnnotationSameLineOrderViolation {
	// violation: record component annotations out of order
	record Data(@B @A String name) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	// violation: constructor parameter annotations out of order
	InputAnnotationSameLineOrderViolation(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	// violation: catch parameter annotations out of order
	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (@B @A Exception e) { // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			e.printStackTrace();
		}
	}

	// violation: inline annotations out of order (for-each)
	void forEach(List<String> list) {
		for (@B @A var item : list) // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			System.out.println(item);
	}

	// violation: lambda parameter annotations out of order
	void lambda() {
		final Consumer<String> c = (@B @A String s) -> {}; // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	}

	// violation: inline annotations out of alphabetical order (method parameter)
	void method(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).

	// violation: inline annotations out of order (multiple params, first param wrong)
	void multiParam(
			@C @A String param1, // violation: Annotation 'A' must appear before 'C' (alphabetical order).
			@A @B String param2
	) {}
}