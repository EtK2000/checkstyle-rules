package com.etk2000.checkstyle.inputs.enumsemicolon;

import java.io.Serializable;

// V1: single constant with trailing semicolon
enum InputEnumSemicolonViolation {
	X; // violation: No trailing semicolon in enum without body declarations.
}

// V2: multiple constants with trailing semicolon
enum ViolationMultipleConstants {
	A,
	B,
	C; // violation: No trailing semicolon in enum without body declarations.
}

// V3: no constants, just semicolon
enum ViolationNoConstants {
	; // violation: No trailing semicolon in enum without body declarations.
}

// V4: constant with body, trailing semicolon, no body members
enum ViolationConstantWithBody {
	X {
		@Override
		public String toString() {
			return "x";
		}
	}; // violation: No trailing semicolon in enum without body declarations.
}

// V5: nested enum with unnecessary semicolon
class ViolationOuter {
	enum Inner {
		X; // violation: No trailing semicolon in enum without body declarations.
	}
}

// V6: multiline enum, semicolon on last constant line
enum ViolationMultiline {
	ALPHA,
	BETA,
	GAMMA; // violation: No trailing semicolon in enum without body declarations.
}

// V7: constructor args, no body declarations
enum ViolationConstructorArgs {
	X(1),
	Y(2); // violation: No trailing semicolon in enum without body declarations.
}

// V8: implements interface, no body declarations
enum ViolationWithInterface implements Serializable {
	X; // violation: No trailing semicolon in enum without body declarations.
}

// V9: annotated enum definition
@Deprecated
enum ViolationAnnotated {
	X; // violation: No trailing semicolon in enum without body declarations.
}

// V10: semicolon on its own line, separate from constants
enum ViolationSemicolonOwnLine {
	X
	; // violation: No trailing semicolon in enum without body declarations.
}

// V11: block comment between semicolon and closing brace (comment invisible in AST)
enum ViolationBlockCommentBeforeBrace {
	X; // violation: No trailing semicolon in enum without body declarations.
	/* this comment does not make the semicolon necessary */
}

// V12: enum-in-enum, inner enum has unnecessary semicolon
enum ViolationEnumInEnum {
	X;

	enum InnerViolation {
		Y; // violation: No trailing semicolon in enum without body declarations.
	}
}

// V13: annotated constant, no body declarations
enum ViolationAnnotatedConstant {
	@Deprecated
	X; // violation: No trailing semicolon in enum without body declarations.
}