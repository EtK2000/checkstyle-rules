package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.ArrayList;

class InputMultilineCallMethodCallViolation {
	Object arg;

	void constructorStackedClosingNotStacked() {
		method(new ArrayList<>(other( // violation: closing parens not stacked
				arg
		)
		)); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void constructorUnstackedClosingStacked() {
		method(new ArrayList<>( // violation: closing parens stacked but calls not stacked
				other(
						arg
				))); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void method(Object a) {
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}

	void resourceIdStackedClosingNotStacked() {
		method(R.string.ok, other( // violation: closing parens not stacked
						arg
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void resourceIdStandardClosingStacked() {
		method( // violation: closing parens stacked
				R.string.ok,
				other(
						arg
		)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void resourceIdUnstackedClosingNotStacked() {
		method(R.string.ok, // violation: In multiline calls/signatures, no arguments on the opening paren line.
				other(
						arg
				)
		);
	}

	void resourceIdUnstackedClosingStacked() {
		method(R.string.ok, // violation: In multiline calls/signatures, no arguments on the opening paren line.
				other(
						arg
				)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void stackedCallsClosingNotStacked() {
		method(other( // violation: closing parens not stacked
						arg
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void thisStackedCallsClosingNotStacked() {
		method(this, other( // violation: closing parens not stacked
						arg
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void thisStandardClosingStacked() {
		method( // violation: closing parens stacked
				this,
				other(
						arg
		)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void thisUnstackedCallsClosingNotStacked() {
		method(this, // violation: In multiline calls/signatures, no arguments on the opening paren line.
				other(
						arg
				)
		);
	}

	void thisUnstackedCallsClosingStacked() {
		method(this, // violation: In multiline calls/signatures, no arguments on the opening paren line.
				other(
						arg
				)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void unstackedCallsClosingStacked() {
		method( // violation: closing parens stacked but calls not stacked
				other(
						arg
				)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}
}