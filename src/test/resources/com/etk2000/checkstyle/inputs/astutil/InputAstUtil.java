package com.etk2000.checkstyle.inputs.astutil;

import javax.annotation.Nonnull;

@javax.annotation.CheckReturnValue // intentional FQN: tested by AstUtilTest.testTypeTextQualified
class InputAstUtil {
	@Nonnull
	int field;
	int noAnnotationField, primitiveField;
	java.util.List qualifiedField; // intentional FQN: tested by AstUtilTest.testTypeTextQualified

	InputAstUtil(String ctorParam) {}

	void castAndResolve(Object obj) {
		final String s = (String) obj;
		System.out.println(s);
	}

	void castWrongExpr(Object obj) {
		final String s = (String) this;
	}

	void castWrongType(Object obj) {
		final Integer n = (Integer) obj;
	}

	void emptyBlock() {
	}

	void emptyStatement(boolean flag) {
		if (flag);
	}

	void multiLine(
			int a,
			int b
	) {}

	void primitiveLocal() {
		final int x = 1;
	}

	void varAnonymousClassLocal() {
		// Thread has multiple methods, so this is not convertible to a lambda.
		final var x = new Thread() {
			@Override
			public void run() {}
		};
	}

	void varLocal() {
		final var x = "hello";
	}

	void varNewArrayInitializerLocal() {
		final var x = new int[]{1, 2, 3};
	}

	void varNewArrayLocal() {
		final var x = new String[10];
	}

	void varNewGenericLocal() {
		final var x = new java.util.HashMap<String, Integer>();
	}

	void varNewLocal() {
		final var x = new StringBuilder();
	}
}