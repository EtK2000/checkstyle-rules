package com.etk2000.checkstyle.inputs.lambdaparam;

import java.util.List;
import java.util.function.IntConsumer;

class InputLambdaParamViolationImplicit {
	void explicitArrayType() {
		final Consumer<String[]> c = (String[] x) -> System.out.println(x.length); // violation: Lambda parameter should use implicit type instead of 'String'.
	}

	void explicitMultipleTypes(List<String> list) {
		list.sort((String x, String y) -> x.compareTo(y)); // violation: Lambda parameter should use implicit type instead of 'String'.
	}

	void explicitPrimitiveType() {
		final IntConsumer c = (int x) -> System.out.println(x); // violation: Lambda parameter should use implicit type instead of 'int'.
	}

	void explicitSingleType(List<String> list) {
		list.forEach((String x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'String'.
	}

	void varMultipleParams(List<String> list) {
		list.sort((var x, var y) -> x.compareTo(y)); // violation: Lambda parameter should use implicit type instead of 'var'.
	}

	void varSingleParam(List<String> list) {
		list.forEach((var x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'var'.
	}
}