package com.etk2000.checkstyle.inputs.astutil;

import javax.annotation.Nonnull;

@javax.annotation.CheckReturnValue // intentional FQN: tested by AstUtilTest.testTypeTextQualified
class InputAstUtil {
	@Nonnull
	int field;
	java.util.List qualifiedField; // intentional FQN: tested by AstUtilTest.testTypeTextQualified

	void castAndResolve(Object obj) {
		final String s = (String) obj;
		System.out.println(s);
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

	void varLocal() {
		final var x = "hello";
	}
}