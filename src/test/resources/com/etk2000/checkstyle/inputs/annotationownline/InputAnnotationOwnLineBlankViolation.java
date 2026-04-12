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

	@A // violation: No blank line after annotation 'A'.
	/* block comment */

	void blankLineAfterBlockCommentBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.
	/** Javadoc comment. */

	void blankLineAfterJavadocBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.
	// line comment

	void blankLineAfterLineCommentBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.
	/*
	 * multi-line
	 * block comment
	 */

	void blankLineAfterMultiLineBlockCommentBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.

	/* block comment */
	void blankLineBeforeBlockCommentBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.

	/** Javadoc comment. */
	void blankLineBeforeJavadocBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.

	// line comment
	void blankLineBeforeLineCommentBetweenAnnotationAndDecl() {}

	@A // violation: No blank line after annotation 'A'.

	/*
	 * multi-line
	 * block comment
	 */
	void blankLineBeforeMultiLineBlockCommentBetweenAnnotationAndDecl() {}

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