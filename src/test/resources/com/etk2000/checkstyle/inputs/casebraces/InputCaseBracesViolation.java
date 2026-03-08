package com.etk2000.checkstyle.inputs.casebraces;

class InputCaseBracesViolation {
	void method(int x) {
		switch (x) {
			case 1: {
				System.out.println("one");
				break;
			}
		}
	}
}