package com.etk2000.checkstyle.inputs.instanceofbeforecast;

// === case: main ===
class InputInstanceofBeforeCastViolation {
	void castBeforeInstanceof(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String)
			System.out.println("empty");
	}

	void castBuriedDeep(Object obj) {
		if (obj != null && ((String) obj).length() > 0 && obj instanceof String)
			System.out.println("ok");
	}

	void castInAssignment(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String)
			System.out.println("ok");
	}

	void castInElse(Object obj) {
		if (obj instanceof String)
			System.out.println("is string");
		else
			System.out.println(((String) obj).length());
	}

	void castInNegatedThen(Object obj) {
		if (!(obj instanceof String))
			System.out.println(((String) obj).length());
	}

	int castInTernaryFalseBranch(Object obj) {
		return obj instanceof String ? 0 : ((String) obj).length();
	}
}
// === end ===