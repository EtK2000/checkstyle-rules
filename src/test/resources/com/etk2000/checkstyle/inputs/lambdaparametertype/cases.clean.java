package com.etk2000.checkstyle.inputs.lambdaparametertype;

import java.util.List;

@interface A {}
@interface B {}

class InputLambdaParamClean {
	void annotatedMultiParam(List<String> list) {
		list.sort((@A var x, @B var y) -> x.compareTo(y));
	}

	void annotatedSecondOnly(List<String> list) {
		list.sort((var x, @A var y) -> x.compareTo(y));
	}

	void annotatedVar(List<String> list) {
		list.forEach((@A var s) -> System.out.println(s));
	}

	void annotatedVarMultiAnnotation(List<String> list) {
		list.forEach((@A @B var s) -> System.out.println(s));
	}

	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	void constructorParam() {
		new Thread(() -> System.out.println("thread"));
	}

	void methodParam(String param) {
		System.out.println(param);
	}

	void multiImplicitParam(List<String> list) {
		list.sort((x, y) -> x.compareTo(y));
	}

	void nakedParam(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}

	void noParam() {
		final Runnable r = () -> System.out.println("hello");
		r.run();
	}
}