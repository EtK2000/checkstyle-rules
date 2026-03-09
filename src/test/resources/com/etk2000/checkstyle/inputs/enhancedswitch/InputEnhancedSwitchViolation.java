package com.etk2000.checkstyle.inputs.enhancedswitch;

class InputEnhancedSwitchViolation {
	// all return — convertible
	int allReturn(int x) {
		switch (x) { // violation: can use enhanced switch
			case 1:
				return 10;
			case 2:
				return 20;
			default:
				return 0;
		}
	}

	// all throw — convertible
	void allThrow(int x) {
		switch (x) { // violation: can use enhanced switch
			case 1:
				throw new RuntimeException("one");
			case 2:
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("other");
		}
	}

	// just break — convertible (becomes case X -> {})
	void justBreak(int x) {
		switch (x) { // violation: can use enhanced switch
			case 1:
				break;

			case 2:
				break;

			default:
				break;
		}
	}

	// single statement + break — convertible
	void singleStmtBreak(int x) {
		switch (x) { // violation: can use enhanced switch
			case 1:
				System.out.println("one");
				break;

			case 2:
				System.out.println("two");
				break;

			default:
				System.out.println("other");
				break;
		}
	}

	// stacked fall-through labels with return — convertible
	int stackedLabels(int x) {
		switch (x) { // violation: can use enhanced switch
			case 1:
			case 2:
				return 10;
			case 3:
				return 30;
			default:
				return 0;
		}
	}

	// yield in traditional switch expression — convertible
	int yieldTraditional(int x) {
		return switch (x) { // violation: can use enhanced switch
			case 1:
				yield 10;
			case 2:
				yield 20;
			default:
				yield 0;
		};
	}
}