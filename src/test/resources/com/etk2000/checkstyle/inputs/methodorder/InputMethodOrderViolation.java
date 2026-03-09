package com.etk2000.checkstyle.inputs.methodorder;

class InputMethodOrderViolation {
	void zeta() {
	}

	void alpha() { // violation: alpha must appear before zeta
	}

	void gamma() {
	}

	void beta() { // violation: beta must appear before gamma
	}
}