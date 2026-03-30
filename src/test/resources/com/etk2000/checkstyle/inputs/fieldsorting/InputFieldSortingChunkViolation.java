package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingChunkViolation {
	int nonFinal;
	final int finalNoValue; // violation: final without inline value after non-final
	final int finalWithValue = 1; // violation: final with inline value after non-final

	InputFieldSortingChunkViolation(int finalNoValue) {
		this.finalNoValue = finalNoValue;
	}
}