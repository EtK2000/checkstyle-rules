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

	void varLocal() {
		final var x = "hello";
	}
}