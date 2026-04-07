package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingChunkViolation {
	int nonFinal;
	final int finalNoValue; // violation: Field 'finalNoValue' (final without inline value) must appear before non-final fields.
	final int finalWithValue = 1; // violation: Field 'finalWithValue' (final with inline value) must appear before final without inline value fields.

	InputFieldSortingChunkViolation(int value) {
		this.finalNoValue = value;
	}
}