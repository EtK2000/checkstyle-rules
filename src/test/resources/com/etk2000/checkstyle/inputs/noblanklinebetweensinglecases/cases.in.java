package com.etk2000.checkstyle.inputs.noblanklinebetweensinglecases;

// === case: bracedcaseblankline_case ===
class InputBracedCaseBlankLineCaseSliceViolation {
	int method(int x) {
		switch (x) {
			case 1: {
				return x + 1;
			}

			case 2: { // violation: Remove blank line after braced case (closing brace provides separation).
				return x + 2;
			}
		}
		return 0;
	}
}
// === end ===

// === case: bracedcaseblankline_default ===
class InputBracedCaseBlankLineDefaultSliceViolation {
	int method(int x) {
		switch (x) {
			case 1: {
				return x + 1;
			}

			default: // violation: Remove blank line after braced case (closing brace provides separation).
				return 0;
		}
	}
}
// === end ===

// === case: remove_single_blank_line ===
class InputRemoveSingleBlankLineViolation {
	int method(String s) {
		switch (s) {
			case "a":
				return 1;
			case "b":
				return 2;

			case "c": // violation: Remove blank line between single-line switch cases.
				return 3;
			default:
				return 0;
		}
	}
}
// === end ===

// === case: singlecase ===
class InputSingleCaseViolation {
	int method(int x) {
		switch (x) {
			case 1:
				return 1;

			case 2: // violation: Remove blank line between single-line switch cases.
				return 2;
			default:
				return 0;
		}
	}
}
// === end ===

// === case: singlecasethrow ===
class InputSingleCaseThrowViolation {
	void method(int x) {
		switch (x) {
			case 1:
				throw new RuntimeException("one");

			case 2: // violation: Remove blank line between single-line switch cases.
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("default");
		}
	}
}
// === end ===

// === case: singlecaseyield ===
class InputSingleCaseYieldViolation {
	int method(int x) {
		return switch (x) {
			case 1:
				yield 1;

			case 2: // violation: Remove blank line between single-line switch cases.
				yield 2;
			default:
				yield 0;
		};
	}
}
// === end ===