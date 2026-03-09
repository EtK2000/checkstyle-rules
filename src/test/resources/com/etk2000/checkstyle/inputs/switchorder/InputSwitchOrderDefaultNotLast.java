package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderDefaultNotLast {
	// enhanced switch
	int enhanced(int x) {
		return switch (x) {
			default -> -1; // violation: default must be last
			case 1 -> 1;
			case 2 -> 2;
		};
	}

	// traditional switch
	int traditional(int x) {
		switch (x) {
			default: // violation: default must be last
				return -1;
			case 1:
				return 1;
			case 2:
				return 2;
		}
	}
}