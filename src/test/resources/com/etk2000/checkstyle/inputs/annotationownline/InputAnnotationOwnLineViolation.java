package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}

// violation: two annotations on same line (class)
@A @B // violation: Annotation 'A' must be on its own line.
class InputAnnotationOwnLineViolation {
	// violation: annotation on same line as enum declaration
	@A enum InlineEnum {} // violation: Annotation 'A' must be on its own line.

	// violation: annotation on same line as enum constant
	enum Status {
		@A ACTIVE, // violation: Annotation 'A' must be on its own line.

		@A @B INACTIVE // violation: Annotation 'A' must be on its own line.
	}

	// violation: annotation on same line as interface declaration
	@A interface InlineInner {} // violation: Annotation 'A' must be on its own line.

	// violation: annotation inline with annotation type and its field
	@A @interface InlineMeta { // violation: Annotation 'A' must be on its own line.
		@A String value(); // violation: Annotation 'A' must be on its own line.
	}

	// violation: annotation on same line as record
	@A @B record InlineRec(int x) {} // violation: Annotation 'A' must be on its own line.

	// violation: annotation on same line as compact constructor
	record InlineCompact(int v) {
		@A InlineCompact {} // violation: Annotation 'A' must be on its own line.
	}

	// violation: annotation on same line as field declaration
	@A int inlineField; // violation: Annotation 'A' must be on its own line.

	// violation: two annotations on same line (field)
	@A @B // violation: Annotation 'A' must be on its own line.
	int twoAnnotationsField;

	// violation: three annotations on one line + declaration (deduplication: 1 per line)
	@A @B @C int allInOne; // violation: Annotation 'A' must be on its own line.

	// violation: annotation on same line as constructor
	@A InputAnnotationOwnLineViolation() {} // violation: Annotation 'A' must be on its own line.

	// violation: annotation inline with method
	@A void inlineMethod() {} // violation: Annotation 'A' must be on its own line.

	// violation: annotation inline with local variable
	void locals() {
		@A final var x = "test"; // violation: Annotation 'A' must be on its own line.

		@A @B final var y = 42; // violation: Annotation 'A' must be on its own line.
	}

	// violation: two annotations on same line (method)
	@A @B // violation: Annotation 'A' must be on its own line.
	void twoAnnotationsMethod() {}
}