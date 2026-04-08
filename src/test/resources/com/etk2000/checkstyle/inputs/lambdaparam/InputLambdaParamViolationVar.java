package com.etk2000.checkstyle.inputs.lambdaparam;

import java.util.List;

@interface C {}
@interface D {}

class InputLambdaParamViolationVar {
	void annotatedBothParams(List<String> list) {
		list.sort((@C String x, @D String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}

	void annotatedExplicitMultiParam(List<String> list) {
		list.sort((@C String x, String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}

	void annotatedExplicitSingle(List<String> list) {
		list.forEach((@C String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}

	void annotatedMultiAnnotation(List<String> list) {
		list.forEach((@C @D String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}

	void annotatedSecondParam(List<String> list) {
		list.sort((String x, @C String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}