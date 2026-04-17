package com.etk2000.checkstyle.inputs.enumsemicolon;

import java.io.Serializable;

// C1: constants with method after semicolon
enum InputEnumSemicolonClean {
	ALPHA,
	BETA;

	void method() {
	}
}

// C2: constants with field after semicolon
enum CleanWithField {
	X,
	Y;

	static final int COUNT = 2;
}

// C3: no constants, method after semicolon
enum CleanNoConstantsWithMethod {
	;

	static int count() {
		return 0;
	}
}

// C4: no semicolon at all
enum CleanNoSemicolon {
	A,
	B,
	C
}

// C5: empty enum
enum CleanEmpty {
}

// C6: constructor after semicolon
enum CleanWithConstructor {
	X(1);

	CleanWithConstructor(int value) {
	}
}

// C7: static field after semicolon
enum CleanWithStaticField {
	X;

	static final int COUNT = 1;
}

// C8: inner class after semicolon
enum CleanWithInnerClass {
	X;

	static class Inner {
	}
}

// C9: constant with body, semicolon, then method
enum CleanConstantBodyWithMethod {
	X {
		@Override
		public String toString() {
			return "x";
		}
	};

	void method() {
	}
}

// C10: static initializer after semicolon
enum CleanWithStaticInit {
	X;

	static {
		Math.random();
	}
}

// C11: instance initializer after semicolon
enum CleanWithInstanceInit {
	X;

	{
		hashCode();
	}
}

// C12: implements interface with body declarations
enum CleanWithInterface implements Serializable {
	X;

	static final long serialVersionUID = 1;
}

// C13: enum-in-enum, outer semicolon is necessary (inner enum is a body declaration)
enum CleanEnumInEnum {
	X;

	enum Inner {
		Y
	}
}

// C14: comment containing semicolon (no SEMI token in AST, no false positive)
enum CleanCommentWithSemicolon {
	X /* ; */
}

// C15: block comment containing semicolon on its own line
enum CleanBlockCommentWithSemicolon {
	X
	/* ; */
}

// C16: implements generic interface with body declarations
enum CleanWithGenericInterface implements Comparable<CleanWithGenericInterface> {
	X;

	@Override
	public int compareTo(CleanWithGenericInterface o) {
		return 0;
	}
}

// C17: annotated constant with body declarations (annotation on constant, not enum)
enum CleanAnnotatedConstant {
	@Deprecated
	X;

	void method() {
	}
}