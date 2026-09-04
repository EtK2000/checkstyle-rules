package com.etk2000.checkstyle.inputs.annotationownline;

import java.util.List;
import java.util.function.Consumer;

@interface A {}
@interface B {}
@interface C {}
@interface V {
	String[] value();
}
@interface Ä {}

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
		final Consumer<String> c = (@A var s) -> {};
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
	/* block comment */
	void methodWithBlockCommentAfterAnnotation() {}

	@A
	@B
	/*
	comment with a truly blank line

	below it */
	void methodWithBlockCommentTrulyBlankLineAfterAnnotation() {}

	@A
	@B
	/* first */ /* second comment

	with blank line inside reopened comment */
	void methodWithCloseReopenBlockCommentBlankLineAfterAnnotation() {}

	@A
	@B
	/** Javadoc comment. */
	void methodWithJavadocAfterAnnotation() {}

	@A
	@B
	/**
	 * Javadoc comment.
	 *
	 * <p>With blank line inside.
	 */
	void methodWithJavadocWithBlankLineAfterAnnotation() {}

	@A
	@B
	// line comment
	void methodWithLineCommentAfterAnnotation() {}

	@A
	// line comment
	@B
	void methodWithLineCommentBetweenAnnotations() {}

	@A
	@B
	/*
	 * multi-line
	 * block comment
	 */
	void methodWithMultiLineBlockCommentAfterAnnotation() {}

	@A
	@B
	/*
	 * block comment
	 *
	 * with blank line inside
	 */
	void methodWithMultiLineBlockCommentWithBlankLineAfterAnnotation() {}

	@A
	@B
	@C
	void multiAnnotatedMethod() {}

	// one MODIFIERS per declarator, both positioned on the same '@A', so both must stay silent
	void multiDeclaratorInOrder() {
		@A
		@B
		final int alpha, beta;
		alpha = 1;
		beta = 2;
		assert alpha + beta > 0;
	}

	@V({
		"a",
		"b"
	})
	void multiLineAnnotation() {}

	@A
	@V({
		"a",
		"b"
	})
	void multiLineAnnotationAfterSingleLine() {}

	@A
	@V({
		"b"
	})
	void multiLineSingleThenMultiLine() {}

	// parameter annotation is NOT checked by OwnLineCheck (inline context, boundary)
	void paramMethod(@A String param) {}

	// a pattern variable's MODIFIERS is NOT checked, in all four positions one can appear
	void patternVariable(Object obj) {
		if (obj instanceof @A String s)
			assert !s.isEmpty();
		if (obj instanceof @A final Integer i)
			assert i != 0;
		final var fromSwitch = switch (obj) {
			case @A Long l -> l.intValue();
			default -> 0;
		};
		assert fromSwitch >= 0;
	}

	void patternVariableInRecordPattern(Object obj) {
		if (obj instanceof Pair(@A int x, int y))
			assert x + y > 0;
	}

	void plainMethod() {}

	// a receiver parameter's MODIFIERS is NOT checked (inline context, boundary)
	void receiverParameter(@A InputAnnotationOwnLineClean this) {}

	// equal names, the compareTo == 0 boundary of the order rule
	@A
	@A
	void repeatableStacked() {}

	// the declaration's type starts on the line below, so its annotation is already on its own line
	@A
	java.util
			.List<String> splitQualifiedDeclarationType() {
		return List.of();
	}

	// a blank line inside a text block is part of the string, not a blank line in the declaration
	@V("""
			a

			b""")
	void textBlockInternalBlank() {}

	// the literal opens on a continuation line rather than the annotation's own, and with no blank
	// line anywhere the scan has nothing to classify. Its violation partner, which puts a blank
	// before the literal opens, is the slice internal_blank_before_text_block
	@V(
			"""
			a""")
	void textBlockOpeningOnAContinuationLine() {}

	// catch parameter is NOT checked (inline context, boundary)
	void tryCatch() {
		try {
			Thread.sleep(1);
		}
		catch (@A Exception e) {
			e.printStackTrace();
		}
	}

	// a try-with-resources variable's MODIFIERS is NOT checked (boundary)
	void tryWithResources() throws java.io.IOException {
		try (@A var reader = new java.io.StringReader("x")) {
			assert reader.read() >= 0;
		}
	}

	<@A T> void typeParameterAnnotation(T value) {
		assert value != null;
	}

	// every type-use position is NOT checked: the annotation hangs off the type, not the declaration
	void typeUse(Object obj) {
		final List<@A String> inTypeArgument = List.of();
		final String @A [] onArrayLevel = {};
		final var inCast = (@A String) obj;
		final var onNew = new @A Object();
		if (obj instanceof @A CharSequence text)
			assert !text.isEmpty();
		assert inTypeArgument != null && onArrayLevel != null && inCast != null && onNew != null;
	}

	void typeUseOnQualifiedNestedType(java.util.@A List<String> rows) {
		assert rows != null;
	}

	void typeUseOnThrows() throws @A Exception {}
}