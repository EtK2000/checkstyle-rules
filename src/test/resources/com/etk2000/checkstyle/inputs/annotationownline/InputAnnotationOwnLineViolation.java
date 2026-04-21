package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}

@A @B // violation: Annotation 'A' must be on its own line.
class InputAnnotationOwnLineViolation {
	@A enum InlineEnum {} // violation: Annotation 'A' must be on its own line.

	enum Status {
		@A ACTIVE, // violation: Annotation 'A' must be on its own line.

		@A @B INACTIVE // violation: Annotation 'A' must be on its own line.
	}

	@A interface InlineInner {} // violation: Annotation 'A' must be on its own line.

	@A @interface InlineMeta { // violation: Annotation 'A' must be on its own line.
		@A String value(); // violation: Annotation 'A' must be on its own line.
	}

	@A @B record InlineRec(int x) {} // violation: Annotation 'A' must be on its own line.

	record InlineCompact(int v) {
		@A InlineCompact {} // violation: Annotation 'A' must be on its own line.
	}

	static final @A int embeddedField = 1; // violation: Annotation 'A' must be on its own line.

	@A int inlineField; // violation: Annotation 'A' must be on its own line.

	@A @B // violation: Annotation 'A' must be on its own line.
	int twoAnnotationsField;

	@A @B @C int allInOne; // violation: Annotation 'A' must be on its own line.

	@A InputAnnotationOwnLineViolation() {} // violation: Annotation 'A' must be on its own line.

	@A void inlineMethod() {} // violation: Annotation 'A' must be on its own line.

	void locals() {
		@A final var x = "test"; // violation: Annotation 'A' must be on its own line.

		@A @B final var y = 42; // violation: Annotation 'A' must be on its own line.

		final @A var afterFinal = 1; // violation: Annotation 'A' must be on its own line.

		final @A @B var afterFinalMultiple = 2; // violation: Annotation 'A' must be on its own line.
	}

	@A @B // violation: Annotation 'A' must be on its own line.
	void twoAnnotationsMethod() {}
}