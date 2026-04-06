package com.etk2000.checkstyle.inputs.casebraces;

class InputCaseBracesMissingViolation {
	void method(int x) {
		switch (x) {
			case 1: // violation: braces required when variable defined in case scope
				final var y = 1;
				System.out.println(y);
				break;

			case 2: // violation: braces required when variable defined in case scope
				final var s = "hello";
				System.out.println(s);
				break;

			case 3:
				System.out.println("no variable, no braces — ok");
				break;
		}
	}
}