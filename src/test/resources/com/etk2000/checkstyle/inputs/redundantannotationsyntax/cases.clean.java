package com.etk2000.checkstyle.inputs.redundantannotationsyntax;

@interface A {
	String[] value();
}
@interface B {
	String key();
}
@interface E {}
@interface F {
	E value();
}
@interface G {
	E[] value();
}
@interface Multi {
	String key();
	String value();
}
@interface Near {
	String values() default "";
}
@interface Num {
	int value();
}
@interface Ä {}

@A("class-level")
class InputRedundantAnnotationSyntaxClean {
	@F(@E)
	int annotationValue;

	@A({"a", "b"})
	int arrayValue;

	@Deprecated
	int bareAnnotation;

	@A(/* c */ "x")
	int commentThenStringValue;

	@A({})
	int emptyArrayValue;

	@A("")
	int emptyStringValue;

	@/* c */E
	int commentAfterAt;

	@A("field-level")
	int field;

	@G({@E, @E})
	int markerArrayValue;

	@B(
			key = "y"
	)
	int multiLineNonValueKey;

	@A(
			"y"
	)
	int multiLineShorthand;

	@Num(
			1)
	int multiLineShorthandNumeric;

	@Near(values = "x")
	int nearMissKey;

	@Ä
	int nonAsciiMarker;

	@ E
	long spaceAfterAt;

	@com.example
			.A
	int splitQualifiedName;

	@A("""
			x""")
	int textBlockValue;

	@E
	String textBlockDecoy = """
			@X()
			value = y""";

	java.util.List<@E String> typeUseTypeArg;

	@Deprecated
	long value = 1;

	@Multi(key = "a", value = "b")
	int multipleParams;

	@B(key = "x")
	int nonValueKey;

	void locals() {
		@A("local")
		final var x = 1;
	}

	void method(@A("param") String param) {}
}