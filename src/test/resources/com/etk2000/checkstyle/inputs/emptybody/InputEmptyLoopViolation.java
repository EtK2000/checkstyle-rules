package com.etk2000.checkstyle.inputs.emptybody;

import java.util.List;

class InputEmptyLoopViolation {
	void emptyDoWhileBlock(int x) {
		do { // violation: empty do-while body
		} while (x > 0);
	}

	void emptyDoWhileStatement(int x) {
		do; // violation: empty do-while body
		while (x > 0);
	}

	void emptyForBlock(int x) {
		for (int i = 0; i < x; ++i) { // violation: empty for body
		}
	}

	void emptyForEachStatement(List<String> list) {
		for (String s : list); // violation: empty for body
	}

	void emptyForStatement(int x) {
		for (int i = 0; i < x; ++i); // violation: empty for body
	}

	void emptyWhileBlock(int x) {
		while (x > 0) { // violation: empty while body
		}
	}

	void emptyWhileStatement(int x) {
		while (x > 0); // violation: empty while body
	}
}