package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface LocatorA {}
@interface LocatorB {}
@interface LocatorB𝒜 {}
@interface LocatorC {}
@interface LocatorNest {
	LocatorA value();
}
@interface LocatorNum {
	int value();
}
@interface LocatorÄ {}

// positions for AnnotationSameLineLocatorTest to address directly. Several are positions the check
// never reports
class InputLocatorPositions {
	@LocatorA
	@interface ElementHolder {
		String value();
	}

	enum EnumConstant {
		@LocatorA
		@LocatorB
		ALPHA
	}

	record RecordComponent(
			@LocatorA
			String name
	) {}

	static class ConstructorParameter {
		ConstructorParameter(
				@LocatorA
				String param
		) {}
	}

	static class Resource implements AutoCloseable {
		@Override
		public void close() {}
	}

	@LocatorA
	int fieldAnnotation;

	void arrayLevelAnnotation(String @LocatorA [] param) {
		System.out.println(param.length);
	}

	void catchParameter() {
		try {
			Thread.sleep(1);
		}
		catch (
				@LocatorA
				InterruptedException e
		) {
			e.printStackTrace();
		}
	}

	void commentAfterAt(@/* c */LocatorB @LocatorA String param) {
		System.out.println(param);
	}

	void decoyInComment() {
		// @LocatorB @LocatorA String decoy
		System.out.println("comment decoy");
	}

	void decoyInString() {
		final var decoy = "@LocatorB @LocatorA String param";
		System.out.println(decoy);
	}

	void decoyInTextBlock() {
		final var decoy = """
				@LocatorB @LocatorA String param""";
		System.out.println(decoy);
	}

	void exactColumnOrder(@LocatorB @LocatorA String param) {
		System.out.println(param);
	}

	void exactColumnSameLine(
			@LocatorA
			String param
	) {
		System.out.println(param);
	}

	void forEachVariable(List<String> list) {
		for (
				@LocatorA
				var item : list
		)
			System.out.println(item);
	}

	void forInitVariable() {
		for (
				@LocatorA
				var i = 0; i < 1; ++i
		)
			System.out.println(i);
	}

	void lambdaParameter() {
		final Consumer<String> consumer = (
				@LocatorA
				var s
		) -> System.out.println(s);
		consumer.accept("x");
	}

	void localVariableAnnotation() {
		@LocatorA
		final var local = "x";
		System.out.println(local);
	}

	@LocatorA
	void methodAnnotation() {}

	void nestedAnnotationValue(@LocatorNest(@LocatorA) @LocatorB String param) {
		System.out.println(param);
	}

	void resourceAnnotation() throws Exception {
		try (@LocatorB @LocatorA var r = new Resource()) {
			System.out.println(r);
		}
	}

	void siblingInOrder(@LocatorC @LocatorA @LocatorB String param) {
		System.out.println(param);
	}

	void spaceAfterAt(@ LocatorB @LocatorA String param) {
		System.out.println(param);
	}

	void spanningAnnotation(
			@LocatorNum(
			1) @LocatorA String param
	) {
		System.out.println(param);
	}

	void splitQualifiedType(
			@LocatorA java.util
			.List<String> param
	) {
		System.out.println(param);
	}

	void supplementaryAhead(String 𝒜name, @LocatorB @LocatorA String param) {
		System.out.println(𝒜name + param);
	}

	void twoGroupsOnOneLine(@LocatorB @LocatorA String first, @LocatorÄ @LocatorC String second) {
		System.out.println(first + second);
	}

	<@LocatorA T> void typeParameterAnnotation(T param) {
		System.out.println(param);
	}

	void typeUseAnnotation(List<@LocatorA String> param) {
		System.out.println(param);
	}

	void unannotatedParameter(String param) {
		System.out.println(param);
	}
}