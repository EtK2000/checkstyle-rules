package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingTypeViolation {
	final String name = "x"; // violation: reference type before primitive
	final int count = 0; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
}

enum InputFieldSortingTypeViolationEnumConstantBody {
	INSTANCE {
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	}
}