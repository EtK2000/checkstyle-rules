package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallLinesLeakA {
	void m() {
		populatesLineCache();
	}

	void populatesLineCache() {
	}
}