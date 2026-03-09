package com.etk2000.checkstyle.inputs.emptyswitch;

class InputEmptySwitchClean {
	int hasCase(int x) {
		return switch (x) {
			case 1 -> 10;
			default -> 0;
		};
	}

	void onlyDefault(int x) {
		switch (x) {
			default:
				break;
		}
	}
}