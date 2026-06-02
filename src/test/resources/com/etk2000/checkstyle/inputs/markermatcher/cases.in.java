package com.etk2000.checkstyle.inputs.markermatcher;

// === case: wrong_message ===
class InputMarkerMatcherWrongMessage {
	int x;

	void m() {
		System.out.println(this.x); // violation: completely wrong message text that the check will never emit
	}
}
// === end ===