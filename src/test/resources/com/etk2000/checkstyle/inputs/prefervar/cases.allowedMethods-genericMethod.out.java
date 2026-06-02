package com.etk2000.checkstyle.inputs.prefervar;

// === case: allowed_method_type_args_explicit_type ===
class InputPreferVarAllowedMethodTypeArgsExplicitTypeSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = InputPreferVarAllowedMethodTypeArgsExplicitTypeSliceViolation.<String>genericMethod(1);
	}
}
// === end ===

// === case: allowed_method_type_args_var ===
class InputPreferVarAllowedMethodTypeArgsVarSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = InputPreferVarAllowedMethodTypeArgsVarSliceViolation.<String>genericMethod(1);
	}
}
// === end ===

// === case: allowed_method_var ===
class InputPreferVarAllowedMethodVarSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = genericMethod(1);
	}
}
// === end ===

// === case: non_allowed_method_calls ===
class InputPreferVarNonAllowedMethodCallsSliceViolation {
	void m() {
		final var s = String.valueOf(42);
	}
}
// === end ===

// === case: paren_wrapped_allowed_method ===
class InputPreferVarParenWrappedAllowedMethodSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = (genericMethod(1));
	}
}
// === end ===