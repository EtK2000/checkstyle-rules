package com.etk2000.checkstyle.inputs.fieldsorting;

@interface AnnV {}

@interface BnnV {}

class InputFieldSortingAnnotationViolationAnnotatedBeforeUnannotated {
	@Deprecated
	String annotated;
	String plain; // violation: Field 'plain' (unannotated) must appear before 'annotated' (annotated @Deprecated), same type.
}

class InputFieldSortingAnnotationViolationWrongAnnotationOrder {
	@SuppressWarnings("unused")
	int beta;
	@Deprecated
	int alpha; // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
}

class InputFieldSortingAnnotationViolationMultiAnnotation {
	@BnnV
	String bField;
	@AnnV
	@BnnV
	String abField; // violation: Field 'abField' (annotated @AnnV) must appear before 'bField' (annotated @BnnV), same type.
}

class InputFieldSortingAnnotationViolationSameAnnotationName {
	@Deprecated
	String zebra;

	@Deprecated
	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}

class InputFieldSortingAnnotationViolationQualified {
	@java.lang.SuppressWarnings("unused")
	int beta;
	@java.lang.Deprecated
	int alpha; // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
}

class InputFieldSortingAnnotationViolationStatic {
	@SuppressWarnings("unused")
	static String beta;
	@Deprecated
	static String alpha; // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
}