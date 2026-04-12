package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiStringFormatViolation {
	void formatOneArg(String name) {
		final var s = String.format("Hello %s", name); // violation: Use '.formatted(...)' instead of 'String.format(...)'.
	}

	void formatSingleCast(Object obj) {
		final var s = String.format((String) obj); // violation: Use 'the value directly' instead of 'String.format(value)'.
	}

	void formatSingleLiteral() {
		final var s = String.format("literal"); // violation: Use 'the value directly' instead of 'String.format(value)'.
	}

	void formatSingleMethodCall(Object obj) {
		final var s = String.format(obj.toString()); // violation: Use 'the value directly' instead of 'String.format(value)'.
	}

	void formatSingleVariable(String fmt) {
		final var s = String.format(fmt); // violation: Use 'the value directly' instead of 'String.format(value)'.
	}

	void formatTwoArgs(String name, int age) {
		final var s = String.format("Hello %s, age %d", name, age); // violation: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}