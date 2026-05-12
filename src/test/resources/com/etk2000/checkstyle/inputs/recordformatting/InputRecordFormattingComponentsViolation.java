package com.etk2000.checkstyle.inputs.recordformatting;

class InputRecordFormattingComponentsViolation {
	record Mixed2(int a, // violation: First record component must not share the line with the opening paren.
			int b) {} // violation: Last record component must not share the line with the closing paren.

	record OpeningShared3(int a, // violation: First record component must not share the line with the opening paren.
			int b,
			int c
	) {}

	record ClosingShared3(
			int a,
			int b,
			int c) {} // violation: Last record component must not share the line with the closing paren.

	record MultiPerLineMiddle(
			int a,
			int b, int c, // violation: Each record component must be on its own line.
			int d
	) {}

	record AllOnMiddleLine(
			int a, int b, int c // violation: Each record component must be on its own line. // violation: Each record component must be on its own line.
	) {}

	record MixedGeneric<T>(T a, // violation: First record component must not share the line with the opening paren.
			T b) {} // violation: Last record component must not share the line with the closing paren.
}