class InputNoBlankLineAfterClassBraceClassViolation {

	int x; // violation: blank line after class brace
}

interface InputNoBlankLineAfterClassBraceInterfaceViolation {

	void method(); // violation: blank line after interface brace
}

enum InputNoBlankLineAfterClassBraceEnumViolation {

	VALUE // violation: blank line after enum brace
}

record InputNoBlankLineAfterClassBraceRecordViolation(int x) {

	int y() { return x; } // violation: blank line after record brace
}