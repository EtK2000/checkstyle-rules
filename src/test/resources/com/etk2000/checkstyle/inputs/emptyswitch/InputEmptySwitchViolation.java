package com.etk2000.checkstyle.inputs.emptyswitch;

class InputEmptySwitchViolation {
	void emptySwitch(int x) {
		switch (x) { // violation: empty switch
		}
	}

	void emptySwitchExpression(int x) {
		switch (getVal()) { // violation: empty switch with side effects
		}
	}

	int getVal() {
		return 0;
	}
}