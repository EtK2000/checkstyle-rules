package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}

class InputAnnotationOwnLineBlankViolation {
	@A // violation: No blank line after annotation 'A'.

	int blankBeforeField;

	@A // violation: No blank line after annotation 'A'.

	@B
	int blankBetweenAnnotations;

	@A // violation: No blank line after annotation 'A'.

	void blankBeforeMethod() {}
}