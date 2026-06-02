package com.etk2000.checkstyle.inputs.upperell;

class InputUpperEllClean {
	long binaryLiteral() {
		return 0b1010L;
	}

	long decimalLiteral() {
		return 100L;
	}

	long hexLiteral() {
		return 0xFFL;
	}

	int noSuffix() {
		return 42;
	}

	long underscoreLiteral() {
		return 1_000L;
	}
}