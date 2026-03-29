package com.etk2000.checkstyle.inputs.patterninstanceof;

class InputPatternInstanceofClean {
	void alreadyPatternMatching(Object obj) {
		if (obj instanceof String s)
			System.out.println(s);
	}

	void instanceofWithoutCast(Object obj) {
		if (obj instanceof String)
			System.out.println("is a string");
	}

	void instanceofWithUnrelatedCast(Object obj, Object other) {
		if (obj instanceof String)
			System.out.println((String) other);
	}
}