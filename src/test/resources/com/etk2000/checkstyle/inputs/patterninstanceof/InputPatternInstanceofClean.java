package com.etk2000.checkstyle.inputs.patterninstanceof;

class InputPatternInstanceofClean {
	void alreadyPatternMatching(Object obj) {
		if (obj instanceof String s)
			System.out.println(s);
	}

	// &&: cast is BEFORE instanceof (left operand) -- bad code, but not this check's concern
	void andCastBeforeInstanceof(Object obj) {
		if (((String) obj).isEmpty() && obj instanceof String)
			System.out.println("ok");
	}

	void instanceofWithoutCast(Object obj) {
		if (obj instanceof String)
			System.out.println("is a string");
	}

	void instanceofWithUnrelatedCast(Object obj, Object other) {
		if (obj instanceof String)
			System.out.println((String) other);
	}

	boolean ternaryNoCast(Object obj) {
		return obj instanceof String ? true : false;
	}
}