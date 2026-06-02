package com.etk2000.checkstyle.inputs.prefermathmethod;

// === case: if_init_overwrite_merge_into_decl ===
class InputPreferMathMethodIfInitOverwriteMergeIntoDeclSliceViolation {
	void m(int a, int b) {
		final var r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_decl_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int r) {
		final int s;
		r = Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_plain_assign_decl_without_trailing_return_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclWithoutTrailingReturnFallsBackToBareSliceViolation {
	void m(int a, int b) {
		final int r;
		r = Math.max(a, b);
	}
}
// === end ===

// === case: if_plain_assign_return_clause_false_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnClauseFalseFallsBackToBareSliceViolation {
	void m(int a, int b) {
		final int r;
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_return_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int s) {
		final int r;
		r = Math.max(a, b);
		return s;
	}
}
// === end ===