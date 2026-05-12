package com.etk2000.checkstyle.inputs.recordformatting;

interface Foo {}

class InputRecordFormattingBracesViolation {
	record NoSpaceEmpty(int a){} // violation: Record opening brace must have exactly one space before it.

	record TwoSpacesEmpty(int a)  {} // violation: Record opening brace must have exactly one space before it.

	record TabBetweenEmpty(int a)	{} // violation: Record opening brace must have exactly one space before it.

	record NoSpaceBody(int a){ // violation: Record opening brace must have exactly one space before it.
		void m() {}
	}

	record BraceNextLineEmpty(int a)
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).

	record BraceNextLineBody(int a)
	{ // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
		void m() {}
	}

	record EmptyBodySplit(int a) {
	} // violation: Empty record body must be '{}' on one line.

	record NonEmptyBodySameLine(int a) { void m() {} } // violation: Non-empty record body must place '}' on its own line.

	record ImplementsNoSpace(int a) implements Foo{} // violation: Record opening brace must have exactly one space before it.

	record ImplementsBraceNextLine(int a) implements Foo
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).

	record ImplementsMultiLine(int a) implements
			Foo
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}