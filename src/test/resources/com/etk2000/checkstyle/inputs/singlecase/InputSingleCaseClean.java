package com.etk2000.checkstyle.inputs.singlecase;

class InputSingleCaseClean {
	int method(int x) {
		switch (x) {
			case 1:
				return 1;
			case 2:
				return 2;
			default:
				return 0;
		}
	}

	void throwCases(int x) {
		switch (x) {
			case 1:
				throw new RuntimeException("one");
			case 2:
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("default");
		}
	}

	int multiLineCaseBreaksChain(int x) {
		switch (x) {
			case 1:
				return 1;

			case 2:
				System.out.println("multi-line");
				return 2;

			case 3:
				return 3;
			default:
				return 0;
		}
	}
}