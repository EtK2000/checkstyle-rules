package com.etk2000.checkstyle.inputs.annotationsyntax;

@interface A {
	String[] value();
}
@interface B {
	String key();
}
@interface Multi {
	String key();
	String value();
}

@A("class-level")
class InputRedundantAnnotationSyntaxClean {
	@A({"a", "b"})
	int arrayValue;

	@Deprecated
	int bareAnnotation;

	@A({})
	int emptyArrayValue;

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