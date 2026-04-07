package com.etk2000.checkstyle.inputs.instanceofbeforecast;

class InputInstanceofBeforeCastViolation {
	void castBeforeInstanceof(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String) // violation
			System.out.println("empty");
	}

	void castBuriedDeep(Object obj) {
		if (obj != null && ((String) obj).length() > 0 && obj instanceof String) // violation
			System.out.println("ok");
	}

	void castInAssignment(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String) // violation
			System.out.println("ok");
	}

	void castInElse(Object obj) {
		if (obj instanceof String)
			System.out.println("is string");
		else
			System.out.println(((String) obj).length()); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}

	void castInNegatedThen(Object obj) {
		if (!(obj instanceof String))
			System.out.println(((String) obj).length()); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}

	int castInTernaryFalseBranch(Object obj) {
		return obj instanceof String ? 0 : ((String) obj).length(); // violation: Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.
	}
}