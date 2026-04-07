package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingNameViolation {
	final int zebra = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}