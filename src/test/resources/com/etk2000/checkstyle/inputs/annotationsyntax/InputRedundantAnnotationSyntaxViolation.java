package com.etk2000.checkstyle.inputs.annotationsyntax;

@interface A {
	String[] value();
}
@interface B {}
@interface C {
	int value();
}
@interface D {
	B value();
}

@A() // violation: Remove empty parentheses from annotation '@A'.
class InputRedundantAnnotationSyntaxViolation {
	@B() // violation: Remove empty parentheses from annotation '@B'.
	int emptyParensField;

	@D(value = @B) // violation: Remove redundant 'value =' from annotation '@D'.
	int explicitValueAnnotation;

	@A(value = {"x", "y"}) // violation: Remove redundant 'value =' from annotation '@A'.
	int explicitValueArray;

	@A(value = "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int explicitValueField;

	@C(value = 42) // violation: Remove redundant 'value =' from annotation '@C'.
	int explicitValueNumeric;

	@B( // violation: Remove empty parentheses from annotation '@B'.
	)
	int multiLineEmptyParens;

	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int multiLineExplicitValue;

	void emptyParensParam(@B() String param) {} // violation: Remove empty parentheses from annotation '@B'.

	void explicitValueParam(@A(value = "x") String param) {} // violation: Remove redundant 'value =' from annotation '@A'.

	void locals() {
		@B() // violation: Remove empty parentheses from annotation '@B'.
		final var x = 1;
	}
}