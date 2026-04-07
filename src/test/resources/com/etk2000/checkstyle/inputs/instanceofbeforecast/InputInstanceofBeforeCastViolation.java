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

	// cast in else block after positive instanceof
	void castInElse(Object obj) {
		if (obj instanceof String)
			System.out.println("is string");
		else
			System.out.println(((String) obj).length()); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}

	// cast in then block after negated instanceof
	void castInNegatedThen(Object obj) {
		if (!(obj instanceof String))
			System.out.println(((String) obj).length()); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}

	// cast in ternary false branch
	int castInTernaryFalseBranch(Object obj) {
		return obj instanceof String ? 0 : ((String) obj).length(); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}
}