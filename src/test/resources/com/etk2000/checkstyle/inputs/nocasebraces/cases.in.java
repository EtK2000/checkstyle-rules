package com.etk2000.checkstyle.inputs.nocasebraces;

// === case: case_label ===
class InputCaseBracesCaseLabelSliceViolation {
	void method(int x) {
		switch (x) {
			case 1: { // violation: Unnecessary braces in case block, only use braces when a variable is defined in the case's scope.
				System.out.println("one");
				break;
			}
			default:
				System.out.println("default");
				break;
		}
	}
}
// === end ===

// === case: default_label ===
class InputCaseBracesDefaultLabelSliceViolation {
	void method(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				break;

			default: { // violation: Unnecessary braces in case block, only use braces when a variable is defined in the case's scope.
				System.out.println("default");
				break;
			}
		}
	}
}
// === end ===

// === case: int_var ===
class InputCaseBracesMissingIntVarSliceViolation {
	void method(int x) {
		switch (x) {
			case 1:
				final var y = 1; // violation: Case block defines a variable, add braces to limit scope.
				System.out.println(y);
				break;

			default:
				break;
		}
	}
}
// === end ===

// === case: string_var ===
class InputCaseBracesMissingStringVarSliceViolation {
	void method(int x) {
		switch (x) {
			case 2:
				final var s = "hello"; // violation: Case block defines a variable, add braces to limit scope.
				System.out.println(s);
				break;

			default:
				break;
		}
	}
}
// === end ===