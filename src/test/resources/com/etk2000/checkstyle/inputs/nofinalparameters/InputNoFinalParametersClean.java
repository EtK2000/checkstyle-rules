package com.etk2000.checkstyle.inputs.nofinalparameters;

import java.io.ByteArrayInputStream;
import java.util.List;

class InputNoFinalParametersClean {
	final int field = 1;

	InputNoFinalParametersClean(int x) {}

	InputNoFinalParametersClean(int x, String y) {}

	void annotatedParamWithoutFinal(@SuppressWarnings("unused") int x) {}

	void catchMultiWithoutFinal() {
		try {
			System.out.println();
		}
		catch (RuntimeException | Error e) {
			System.out.println(e);
		}
	}

	void catchWithoutFinal() {
		try {
			System.out.println();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	void finalize(int x) {}

	void forEachAnnotatedWithoutFinal(List<String> list) {
		for (@SuppressWarnings("unused") var item : list)
			System.out.println(item);
	}

	void forEachWithoutFinal(List<String> list) {
		for (var item : list)
			System.out.println(item);
	}

	void forInitExpressionOnly() {
		var i = 0;
		for (i = 1; i < 10; ++i)
			System.out.println(i);
	}

	void forInitWithoutFinal() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void lambdaParamWithoutFinal(List<String> list) {
		list.sort((a, b) -> a.compareTo(b));
	}

	void lambdaWithoutParams() {
		final Runnable r = () -> {};
		r.run();
	}

	void multipleAnnotatedParams(@SuppressWarnings("unused") int x, @SuppressWarnings("unused") String y) {}

	void multipleParams(int a, String b, double c) {}

	void noParams() {}

	void singleParam(int x) {}

	void stringContainingFinal() {
		System.out.println("method(final int x)");
	}

	void tryWithResources() throws Exception {
		try (var stream = new ByteArrayInputStream(new byte[0])) {
			stream.read();
		}
	}
}