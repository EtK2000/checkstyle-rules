// === case: missing_before_case ===
class InputBlankLineAfterBreakMissingBeforeCaseSliceViolation {
	void method(int x) {
		switch (x) {
			case 1:
				break;

			case 2:
				break;

			default:
				break;
		}
	}
}
// === end ===

// === case: missing_before_default ===
class InputBlankLineAfterBreakMissingBeforeDefaultSliceViolation {
	void method(int x) {
		switch (x) {
			case 1:
				break;

			default:
				break;
		}
	}
}
// === end ===