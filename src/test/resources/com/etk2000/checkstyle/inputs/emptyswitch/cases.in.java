package com.etk2000.checkstyle.inputs.emptyswitch;

// === case: main ===
class InputEmptySwitchViolation {
	void emptySwitch(int x) {
		switch (x) { // violation: Remove the empty switch statement (preserve any side effects in the expression).
		}
	}

	void emptySwitchExpression(int x) {
		switch (getVal()) { // violation: Remove the empty switch statement (preserve any side effects in the expression).
		}
	}

	int getVal() {
		return 0;
	}
}
// === end ===