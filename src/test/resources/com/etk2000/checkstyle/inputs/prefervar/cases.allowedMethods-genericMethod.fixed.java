package com.etk2000.checkstyle.inputs.prefervar;

// === case: allowed_method_type_args_resource ===
class InputPreferVarAllowedMethodTypeArgsResourceSliceViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void m() throws Exception {
		try (java.io.BufferedReader reader = InputPreferVarAllowedMethodTypeArgsResourceSliceViolation.<java.io.BufferedReader>genericMethod(1)) {
			System.out.println(reader.readLine());
		}
	}
}
// === end ===