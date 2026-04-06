package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}

class InputAnnotationOwnLineOrderViolation {
	// violation: B before A (alphabetical order)
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	int fieldOrder;

	// violation: C before A and B
	@C
	@A // violation: Annotation 'A' must appear before 'C' (alphabetical order).
	@B
	void methodOrder() {}

	// violation: reverse order on class member
	@C
	@B // violation: Annotation 'B' must appear before 'C' (alphabetical order).
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	void reverseOrder() {}
}