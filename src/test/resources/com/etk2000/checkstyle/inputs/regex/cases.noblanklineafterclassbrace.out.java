// === case: class_brace ===
class InputNoBlankLineAfterClassBraceClassBraceSliceViolation {
	int x;
}
// === end ===

// === case: enum_brace ===
enum InputNoBlankLineAfterClassBraceEnumBraceSliceViolation {
	VALUE
}
// === end ===

// === case: interface_brace ===
interface InputNoBlankLineAfterClassBraceInterfaceBraceSliceViolation {
	void method();
}
// === end ===

// === case: record_brace ===
record InputNoBlankLineAfterClassBraceRecordBraceSliceViolation(int x) {
	int y() { return x; }
}
// === end ===