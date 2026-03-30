package com.etk2000.checkstyle.inputs.instanceofbeforecast;

class InputInstanceofBeforeCastViolation {
	// cast before instanceof in &&
	void castBeforeInstanceof(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String) // violation
			System.out.println("empty");
	}

	// cast buried deeper in left operand
	void castBuriedDeep(Object obj) {
		if (obj != null && ((String) obj).length() > 0 && obj instanceof String) // violation
			System.out.println("ok");
	}

	// cast in variable assignment before instanceof
	void castInAssignment(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String) // violation
			System.out.println("ok");
	}
}