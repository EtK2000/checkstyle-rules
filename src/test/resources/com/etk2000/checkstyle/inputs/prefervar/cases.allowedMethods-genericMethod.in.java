package com.etk2000.checkstyle.inputs.prefervar;

// === case: allowed_method_type_args_explicit_type ===
class InputPreferVarAllowedMethodTypeArgsExplicitTypeSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final String s = InputPreferVarAllowedMethodTypeArgsExplicitTypeSliceViolation.<String>genericMethod(1); // violation (warning): Prefer explicit type over type arguments on 'genericMethod'.
	}
}
// === end ===

// === case: allowed_method_type_args_var ===
class InputPreferVarAllowedMethodTypeArgsVarSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = InputPreferVarAllowedMethodTypeArgsVarSliceViolation.<String>genericMethod(1); // violation (warning): Prefer explicit type over type arguments on 'genericMethod'.
	}
}
// === end ===

// === case: allowed_method_var ===
class InputPreferVarAllowedMethodVarSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = genericMethod(1); // violation (warning): Using 'var' with 'genericMethod' loses generic type information, consider using an explicit type.
	}
}
// === end ===

// === case: non_allowed_method_calls ===
class InputPreferVarNonAllowedMethodCallsSliceViolation {
	void m() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: paren_wrapped_allowed_method ===
class InputPreferVarParenWrappedAllowedMethodSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() {
		final var s = (genericMethod(1)); // violation (warning): Using 'var' with 'genericMethod' loses generic type information, consider using an explicit type.
	}
}
// === end ===