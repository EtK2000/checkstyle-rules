package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}

class InputAnnotationOwnLineOrderViolation {
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	int fieldOrder;

	@C
	@A // violation: Annotation 'A' must appear before 'C' (alphabetical order).
	@B
	void methodOrder() {}

	@C
	@B // violation: Annotation 'B' must appear before 'C' (alphabetical order).
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	void reverseOrder() {}
}