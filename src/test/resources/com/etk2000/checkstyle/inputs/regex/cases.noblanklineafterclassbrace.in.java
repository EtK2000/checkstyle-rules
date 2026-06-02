// === case: class_brace ===
class InputNoBlankLineAfterClassBraceClassBraceSliceViolation {

	int x; // violation: blank line after class brace
}
// === end ===

// === case: enum_brace ===
enum InputNoBlankLineAfterClassBraceEnumBraceSliceViolation {

	VALUE // violation: blank line after enum brace
}
// === end ===

// === case: interface_brace ===
interface InputNoBlankLineAfterClassBraceInterfaceBraceSliceViolation {

	void method(); // violation: blank line after interface brace
}
// === end ===

// === case: record_brace ===
record InputNoBlankLineAfterClassBraceRecordBraceSliceViolation(int x) {

	int y() { return x; } // violation: blank line after record brace
}
// === end ===