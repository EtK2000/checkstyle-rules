package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarGenericReturnClean {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	static <T> T findByType(Class<T> type) {
		return null;
	}

	void autoDetectedGenericExplicitType() {
		String s = cast("hello");
	}

	void inferableFromParamType() {
		var s = findByType(String.class);
	}
}