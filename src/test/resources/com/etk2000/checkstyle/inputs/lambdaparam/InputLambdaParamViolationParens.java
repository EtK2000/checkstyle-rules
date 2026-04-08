package com.etk2000.checkstyle.inputs.lambdaparam;

import java.util.List;

class InputLambdaParamViolationParens {
	void bracedBody(List<String> list) {
		list.forEach((x) -> { // violation: Remove unnecessary parentheses around single lambda parameter.
			System.out.println(x);
		});
	}

	void expressionBody(List<String> list) {
		list.forEach((x) -> System.out.println(x)); // violation: Remove unnecessary parentheses around single lambda parameter.
	}
}