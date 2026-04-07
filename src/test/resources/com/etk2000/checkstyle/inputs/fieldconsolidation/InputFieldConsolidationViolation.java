package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.List;

@interface Multi {
	int a() default 0;
	int b() default 0;
}

// Two same-type primitive fields
class InputFieldConsolidationViolationPrimitives {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Two same-type reference fields
class InputFieldConsolidationViolationReferences {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}

// Three consecutive same-type fields
class InputFieldConsolidationViolationThree {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}

// @Deprecated and @Deprecated() are both parameterless, treated as equal
class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha;
	@Deprecated()
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same parameterless annotation on both
class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with identical params
class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with a complex param (array value)
class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha;
	@SuppressWarnings({"unused", "all"})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with named params in different order
class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(b = 2, a = 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Static same-type fields
class InputFieldConsolidationViolationStatic {
	static int global;
	static int shared; // violation: Fields 'shared' and 'global' (type 'int') should be declared on one line.
}

// Array type match (Java-style and C-style normalize to same type)
class InputFieldConsolidationViolationArray {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}

// Compound array (Type[] name[]) matches Type[][] (same type)
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha;
	String[] beta[]; // violation: Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.
}

// Generic type match
class InputFieldConsolidationViolationGeneric {
	List<String> names;
	List<String> words; // violation: Fields 'words' and 'names' (type 'List<String>') should be declared on one line.
}

// Final without inline value
class InputFieldConsolidationViolationFinal {
	final int alpha;
	final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}