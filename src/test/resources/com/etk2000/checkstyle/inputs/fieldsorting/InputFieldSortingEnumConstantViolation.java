package com.etk2000.checkstyle.inputs.fieldsorting;

enum InputFieldSortingEnumConstantViolation {
	ZEBRA,
	ALPHA; // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).

	static final int MAX = 10;
	static final int MIN = 1;
}

enum InputFieldSortingEnumConstantViolationMultiple {
	CHARLIE,
	BRAVO, // violation: Enum constant 'BRAVO' must appear before 'CHARLIE' (alphabetical order).
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BRAVO' (alphabetical order).
}

enum InputFieldSortingEnumConstantViolationWithMembers {
	CHERRY("red"),
	BANANA("yellow"), // violation: Enum constant 'BANANA' must appear before 'CHERRY' (alphabetical order).
	APPLE("green"); // violation: Enum constant 'APPLE' must appear before 'BANANA' (alphabetical order).

	final String color;

	InputFieldSortingEnumConstantViolationWithMembers(String color) {
		this.color = color;
	}

	String getColor() {
		return color;
	}
}

enum InputFieldSortingEnumConstantViolationWithBodies {
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	},
	ADD { // violation: Enum constant 'ADD' must appear before 'SUBTRACT' (alphabetical order).
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	};

	abstract int apply(int a, int b);
}

class InputFieldSortingEnumConstantViolationInner {
	enum Misordered {
		SECOND,
		FIRST // violation: Enum constant 'FIRST' must appear before 'SECOND' (alphabetical order).
	}
}

enum InputFieldSortingEnumConstantViolationOuterEnum {
	ALPHA,
	BETA;

	enum InnerMisordered {
		YELLOW,
		XENON // violation: Enum constant 'XENON' must appear before 'YELLOW' (alphabetical order).
	}
}

@SuppressWarnings("unused")
enum InputFieldSortingEnumConstantViolationWrongKey {
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}

@SuppressWarnings(value = "unused")
enum InputFieldSortingEnumConstantViolationWrongKeyExplicit {
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}

@SuppressWarnings("FieldSorting")
enum InputFieldSortingEnumConstantViolationSuppressedSibling {
	ZEBRA,
	ALPHA
}