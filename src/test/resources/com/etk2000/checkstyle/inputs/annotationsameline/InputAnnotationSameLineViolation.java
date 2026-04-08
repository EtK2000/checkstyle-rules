package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}

class InputAnnotationSameLineViolation {
	record Data(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String name
	) {}

	InputAnnotationSameLineViolation(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}

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

	void forEach(List<String> list) {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var item : list
		)
			System.out.println(item);
	}

	void forEachMulti(List<String> list) {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				@B
				var item : list
		)
			System.out.println(item);
	}

	void forInit() {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var i = 0; i < 10; ++i
		)
			System.out.println(i);
	}

	void lambda() {
		final Consumer<String> c = (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var s
		) -> {};
	}

	void method(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}

	void multiAnnotation(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			@B
			String param
	) {}

	void placementAndOrder(
			@B // violation: Annotation 'B' must be on the same line as the declaration.
			@A
			String param
	) {}
}