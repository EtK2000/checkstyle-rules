package com.etk2000.checkstyle.inputs.preferpatternmatchinginstanceof;

// === case: main ===
class InputPatternInstanceofViolation {
	// &&: cast in if-body, instanceof in && condition
	void andCastInBody(Object obj) {
		if (obj instanceof String && !((String) obj).isEmpty())
			System.out.println(((String) obj).length()); // also a cast in body
	}

	// &&: cast in right operand of && (only executes when instanceof is true)
	void andCastInCondition(Object obj) {
		if (obj instanceof String && ((String) obj).isEmpty())
			System.out.println("empty string");
	}

	void castDeepInBody(Object obj) {
		if (obj instanceof Number) {
			System.out.println("found number");
			final var n = (Number) obj;
			System.out.println(n.intValue());
		}
	}

	void castInArgument(Object obj) {
		if (obj instanceof String)
			System.out.println((String) obj);
	}

	void castToLocal(Object obj) {
		if (obj instanceof String) {
			final var s = (String) obj;
			System.out.println(s);
		}
	}

	void inlineCast(Object obj) {
		if (obj instanceof String)
			System.out.println(((String) obj).length());
	}

	// nested ternary: inner ternary has instanceof + cast
	String nestedTernaryCast(boolean flag, Object obj) {
		return flag ? (obj instanceof String ? ((String) obj) : "") : "";
	}

	// ternary: instanceof as condition with cast in true-branch
	int ternaryCast(Object obj) {
		return obj instanceof String ? ((String) obj).length() : -1;
	}
}
// === end ===