package com.etk2000.checkstyle.inputs.astutil;

import javax.annotation.Nonnull;

@javax.annotation.CheckReturnValue
class InputAstUtil {
	@Nonnull
	int field;
	java.util.List qualifiedField;

	void castAndResolve(Object obj) {
		String s = (String) obj;
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
		var x = "hello";
	}
}