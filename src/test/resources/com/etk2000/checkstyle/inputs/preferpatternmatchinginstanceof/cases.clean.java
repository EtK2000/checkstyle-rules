package com.etk2000.checkstyle.inputs.preferpatternmatchinginstanceof;

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

	// parenthesized operand, but nothing casts to the tested type
	void andParenthesizedNoCast(Object obj) {
		if ((obj instanceof String) && obj.hashCode() > 0)
			System.out.println("ok");
	}

	// instanceof is the right operand, so nothing after it is gated on the test
	void andRightOperandInstanceof(Object obj, boolean flag) {
		if (flag && (obj instanceof String))
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