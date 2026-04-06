package com.etk2000.checkstyle.inputs.casebraces;

class InputCaseBracesClean {
	void method(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				break;

			case 2: {
				final var y = 1;
				System.out.println(y);
				break;
			}
			default:
				System.out.println("default");
				break;
		}

		// default with braces and variable definition
		switch (x) {
			case 1:
				break;

			default: {
				final var z = 2;
				System.out.println(z);
				break;
			}
		}
	}
}