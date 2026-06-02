package com.etk2000.checkstyle.inputs.instanceofbeforecast;

class InputInstanceofBeforeCastClean {
	// correct order: instanceof before cast (without pattern matching)
	void correctOrder(Object obj) {
		if (obj instanceof String && ((String) obj).isEmpty())
			System.out.println("empty");
	}

	// correct order in chained &&
	void correctOrderChained(Object obj) {
		if (obj instanceof String s && s.length() > 0 && s.startsWith("a"))
			System.out.println("starts with a");
	}

	// correct order with pattern matching
	void correctOrderPatternMatching(Object obj) {
		if (obj instanceof String s && s.isEmpty())
			System.out.println("empty");
	}

	// no instanceof at all, just a cast
	void noCastNoInstanceof(Object obj) {
		if (((String) obj).isEmpty())
			System.out.println("empty");
	}

	// no matching cast (different type)
	void noMatchingCast(Object obj) {
		if (((Number) obj).intValue() > 0 && obj instanceof String)
			System.out.println("ok");
	}

	// no matching cast (different expression)
	void noMatchingExpr(Object a, Object b) {
		if (((String) a).isEmpty() && b instanceof String)
			System.out.println("ok");
	}

	// || is not checked (different short-circuit semantics)
	void orNotChecked(Object obj) {
		if (((String) obj).isEmpty() || obj instanceof String)
			System.out.println("ok");
	}

	// pattern matching already used
	void patternMatching(Object obj) {
		if (obj instanceof String s && s.isEmpty())
			System.out.println("empty");
	}

	// standalone instanceof without &&
	void standaloneInstanceof(Object obj) {
		if (obj instanceof String)
			System.out.println("is string");
	}
}