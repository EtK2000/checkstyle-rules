package com.etk2000.checkstyle.inputs.lambdaparametertype;

import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

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

	void multilineAnnotatedVarAlreadyClean() {
		final Comparator<String> c = (@A var x,
				@B var y) -> x.compareTo(y);
		c.toString();
	}

	void multilineBudgetRefusesByOneColumnOverTheFirstLine() {
		final Comparator<String> c = (String firstParameterPaddedTo37CharactersAbc, String secondParameterPaddedTo37CharsAbcdefg
		) -> 0 + 12345;
		c.toString();
	}

	void multilineBudgetRefusesFold() {
		final Comparator<String> c = (String firstParameterPaddedTo37CharactersAbc,
				String secondParameterPaddedTo37CharsAbcdefg) -> 0;
		c.toString();
	}

	void multilineBudgetRefusesWiderThanFirstLine() {
		final Comparator<String> c = (
				String firstParameterWithAnExtremelyLongDescriptiveNameHere, String secondParameterWithALongName
		) -> 0;
		c.toString();
	}

	void multilineCommentInsideList() {
		final Comparator<String> c = (String x, // first
				String y) -> x.compareTo(y);
		c.toString();
	}

	void multilineCommentOnInteriorLine() {
		final Comparator<String> c = (
				/* note */ String x,
				String y
		) -> x.compareTo(y);
		c.toString();
	}

	void multilineCommentOnLastLine() {
		final Comparator<String> c = (String x,
				String y /* note */) -> x.compareTo(y);
		c.toString();
	}

	void multilineImplicitParamsAlreadyClean() {
		final Comparator<String> c = (x,
				y) -> x.compareTo(y);
		c.toString();
	}

	void multilineSingleNakedParamArrowOnNextLine() {
		final UnaryOperator<String> u = x
				-> x;
		u.toString();
	}

	void multilineStringLiteralInsideList() {
		final Comparator<String> c = (@A("x") String s,
				@A("y") String t) -> 0;
		c.toString();
	}

	void nakedParam(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}

	void noParam() {
		final Runnable r = () -> System.out.println("hello");
		r.run();
	}
}