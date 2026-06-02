// === case: method_body ===
class InputNoBlankLineBeforeClosingBraceMethodBodySliceViolation {
	void method() {
		System.out.println("ok");

	} // violation: blank line before closing brace
}
// === end ===