package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}

class InputAnnotationSameLineViolation {
	// violation: record component annotation on separate line
	record Data(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String name
	) {}

	// violation: constructor parameter annotation on separate line
	InputAnnotationSameLineViolation(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}

	// violation: catch parameter annotation on separate line
	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				Exception e
		) {
			e.printStackTrace();
		}
	}

	// violation: for-each annotation on separate line
	void forEach(List<String> list) {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var item : list
		)
			System.out.println(item);
	}

	// violation: for-each with multiple stacked annotations (only first reported)
	void forEachMulti(List<String> list) {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				@B
				var item : list
		)
			System.out.println(item);
	}

	// violation: for-init annotation on separate line
	void forInit() {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var i = 0; i < 10; ++i
		)
			System.out.println(i);
	}

	// violation: lambda parameter annotation on separate line
	void lambda() {
		final Consumer<String> c = (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				String s
		) -> {};
	}

	// violation: method parameter annotation on separate line
	void method(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}

	// violation: multiple parameter annotations on separate lines
	void multiAnnotation(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			@B
			String param
	) {}
}