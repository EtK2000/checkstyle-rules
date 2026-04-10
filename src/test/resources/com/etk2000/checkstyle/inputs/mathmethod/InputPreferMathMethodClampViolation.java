package com.etk2000.checkstyle.inputs.mathmethod;

class InputPreferMathMethodClampViolation {
	int clampMaxMin(int value, int lo, int hi) {
		return Math.max(lo, Math.min(hi, value)); // violation: Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(lo, Math.min(hi, value))'.
	}

	int clampMaxMinReversedArgs(int value, int lo, int hi) {
		return Math.max(Math.min(hi, value), lo); // violation: Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(Math.min(hi, value), lo)'.
	}

	int clampMinMax(int value, int lo, int hi) {
		return Math.min(hi, Math.max(lo, value)); // violation: Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(hi, Math.max(lo, value))'.
	}

	int clampMinMaxReversedArgs(int value, int lo, int hi) {
		return Math.min(Math.max(lo, value), hi); // violation: Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(Math.max(lo, value), hi)'.
	}
}