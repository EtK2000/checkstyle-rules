package com.etk2000.checkstyle.inputs.directbooleanreturn;

class InputPreferDirectBooleanReturnViolation {
	boolean bracedIfElse(boolean flag) {
		if (flag) { // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		}
		else {
			return false;
		}
	}

	boolean bracedIfThenTrailing(boolean flag) {
		if (flag) { // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		}
		return false;
	}

	boolean bracedIfThenTrailingNegated(boolean flag) {
		if (flag) { // violation: Redundant if returning a boolean literal, return the condition directly.
			return false;
		}
		return true;
	}

	boolean chainInnerFires(int x, int y) {
		if (x > 0)
			return true;
		if (y > 0) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		return false;
	}

	boolean comparisonNegated(int x) {
		if (x > 0) // violation: Redundant if returning a boolean literal, return the condition directly.
			return false;
		return true;
	}

	boolean doubleNegativeCond(boolean flag) {
		if (!flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return false;
		return true;
	}

	boolean landConditionForward(boolean a, boolean b) {
		if (a && b) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		return false;
	}

	boolean methodCallCond(String s) {
		if (s.isEmpty()) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		return false;
	}

	boolean notIdentForward(boolean flag) {
		if (!flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		return false;
	}

	boolean simpleForward(boolean flag) {
		if (flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		return false;
	}

	boolean simpleNegated(boolean flag) {
		if (flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return false;
		return true;
	}

	boolean withElseFalseTrue(boolean flag) {
		if (flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return false;
		else
			return true;
	}

	boolean withElseTrueFalse(boolean flag) {
		if (flag) // violation: Redundant if returning a boolean literal, return the condition directly.
			return true;
		else
			return false;
	}
}