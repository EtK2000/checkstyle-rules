package com.etk2000.checkstyle.inputs.noenumtrailingsemicolon;

import java.io.Serializable;

enum InputEnumSemicolonClean {
	ALPHA,
	BETA;

	void method() {
	}
}

enum CleanWithField {
	X,
	Y;

	static final int COUNT = 2;
}

enum CleanNoConstantsWithMethod {
	;

	static int count() {
		return 0;
	}
}

enum CleanNoSemicolon {
	A,
	B,
	C
}

enum CleanEmpty {
}

enum CleanWithConstructor {
	X(1);

	CleanWithConstructor(int value) {
	}
}

enum CleanWithStaticField {
	X;

	static final int COUNT = 1;
}

enum CleanWithInnerClass {
	X;

	static class Inner {
	}
}

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

enum CleanWithStaticInit {
	X;

	static {
		Math.random();
	}
}

enum CleanWithInstanceInit {
	X;

	{
		hashCode();
	}
}

enum CleanWithInterface implements Serializable {
	X;

	static final long serialVersionUID = 1;
}

enum CleanEnumInEnum {
	X;

	enum Inner {
		Y
	}
}

enum CleanCommentWithSemicolon {
	X /* ; */
}

enum CleanBlockCommentWithSemicolon {
	X
	/* ; */
}

enum CleanWithGenericInterface implements Comparable<CleanWithGenericInterface> {
	X;

	@Override
	public int compareTo(CleanWithGenericInterface o) {
		return 0;
	}
}

enum CleanAnnotatedConstant {
	@Deprecated
	X;

	void method() {
	}
}