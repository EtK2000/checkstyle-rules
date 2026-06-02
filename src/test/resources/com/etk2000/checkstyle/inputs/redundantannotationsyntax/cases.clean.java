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
@interface Multi {
	String key();
	String value();
}

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

	@A("field-level")
	int field;

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