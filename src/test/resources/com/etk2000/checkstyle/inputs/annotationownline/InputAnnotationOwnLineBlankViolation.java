package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}

class InputAnnotationOwnLineBlankViolation {
	// violation: blank line between annotation and field declaration
	@A
	// violation: No blank line after annotation 'A'.
	int blankBeforeField;

	// violation: blank line between two annotations
	@A
	// violation: No blank line after annotation 'A'.
	@B
	int blankBetweenAnnotations;

	// violation: blank line between annotation and method
	@A
	// violation: No blank line after annotation 'A'.
	void blankBeforeMethod() {}
}