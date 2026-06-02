// === case: list_of_fqn_not_on_opening ===
// skip-reason: multiline formatting fix not yet supported for this shape
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfFqnNotOnOpeningSliceViolation {
	void m() {
		method(List.of(
				1, 2, 3
		));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: list_of_fqn_type_witness_not_on_opening ===
// skip-reason: multiline formatting fix not yet supported for this shape
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfFqnTypeWitnessNotOnOpeningSliceViolation {
	void m() {
		method(List.<Integer>of(
				1, 2, 3
		));
	}

	void method(Object a) {
	}
}
// === end ===