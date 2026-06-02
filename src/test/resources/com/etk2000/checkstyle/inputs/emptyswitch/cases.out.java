package com.etk2000.checkstyle.inputs.emptyswitch;

// === case: main ===
class InputEmptySwitchViolation {
	void emptySwitch(int x) {
		switch (x) {
		}
	}

	void emptySwitchExpression(int x) {
		switch (getVal()) {
		}
	}

	int getVal() {
		return 0;
	}
}
// === end ===