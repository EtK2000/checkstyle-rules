package com.etk2000.checkstyle.inputs.fieldsorting;

enum InputFieldSortingEnumSameLineViolation {
	ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}

enum InputFieldSortingEnumSameLineViolationTriple {
	ALPHA, BETA, GAMMA // violation: Enum constant 'BETA' must be on its own line. violation: Enum constant 'GAMMA' must be on its own line.
}

enum InputFieldSortingEnumSameLineViolationWithParams {
	APPLE("red"), BANANA("yellow") // violation: Enum constant 'BANANA' must be on its own line.
}

class InputFieldSortingEnumSameLineViolationInner {
	enum Inner {
		ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
	}
}

enum InputFieldSortingEnumSameLineViolationMisordered {
	ZEBRA, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}