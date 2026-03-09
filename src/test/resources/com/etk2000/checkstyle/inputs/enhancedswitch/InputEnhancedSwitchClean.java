package com.etk2000.checkstyle.inputs.enhancedswitch;

class InputEnhancedSwitchClean {
	// already enhanced — no violation
	int alreadyEnhanced(int x) {
		return switch (x) {
			case 1 -> 10;
			case 2 -> 20;
			default -> 0;
		};
	}

	// empty switch — no violation
	void emptySwitch(int x) {
		switch (x) {
		}
	}

	// fall-through with code — NOT convertible
	int fallThroughWithCode(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
			case 2:
				return 2;
			default:
				return 0;
		}
	}

	// mixed: one case not convertible blocks the whole switch — no violation
	int mixedConvertibility(int x) {
		switch (x) {
			case 1:
				return 1;
			case 2:
				System.out.println("two");
				System.out.println("still two");
				break;

			default:
				return 0;
		}
		return -1;
	}

	// multi-statement body — NOT convertible
	int multiStatement(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				System.out.println("still one");
				break;

			default:
				return 0;
		}
		return -1;
	}
}