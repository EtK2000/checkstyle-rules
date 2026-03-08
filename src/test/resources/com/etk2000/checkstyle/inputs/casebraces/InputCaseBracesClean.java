package com.etk2000.checkstyle.inputs.casebraces;

class InputCaseBracesClean {
	void method(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				break;

			case 2: {
				int y = 1;
				System.out.println(y);
				break;
			}
		}
	}
}