package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface V {
	String[] value();
}

class InputAnnotationOwnLineBlankViolation {
	@A // violation: No blank line after annotation 'A'.

	int blankBeforeField;

	@A // violation: No blank line after annotation 'A'.

	@B
	int blankBetweenAnnotations;

	@A // violation: No blank line after annotation 'A'.

	void blankBeforeMethod() {}

	@V({
		"a"
	}) // violation: No blank line after annotation 'V'.

	void multiLineBlankBeforeMethod() {}

	@A // violation: No blank line after annotation 'A'.

	@V({
		"b"
	})
	void multiLineBlankBetweenAnnotations() {}

	@V({ // violation (next line): No blank line inside annotation 'V'.

		"a"
	})
	void multiLineBlankInsideAnnotation() {}
}