package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.ArrayList;

class InputMultilineCallMethodCallViolation {
	Object arg;

	void constructorStackedClosingNotStacked() {
		method(new ArrayList<>(other( // violation: closing parens not stacked
				arg
		)
		));
	}

	void constructorUnstackedClosingStacked() {
		method(new ArrayList<>( // violation: closing parens stacked but calls not stacked
				other(
						arg
				)));
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
		);
	}

	void resourceIdStandardClosingStacked() {
		method( // violation: closing parens stacked
				R.string.ok,
				other(
						arg
		));
	}

	void resourceIdUnstackedClosingNotStacked() {
		method(R.string.ok, // violation: R.xxx on opening but call not stacked
				other(
						arg
				)
		);
	}

	void resourceIdUnstackedClosingStacked() {
		method(R.string.ok, // violation: closing parens stacked but calls not stacked
				other(
						arg
				));
	}

	void stackedCallsClosingNotStacked() {
		method(other( // violation: closing parens not stacked
						arg
				)
		);
	}

	void thisStackedCallsClosingNotStacked() {
		method(this, other( // violation: closing parens not stacked
						arg
				)
		);
	}

	void thisStandardClosingStacked() {
		method( // violation: closing parens stacked
				this,
				other(
						arg
		));
	}

	void thisUnstackedCallsClosingNotStacked() {
		method(this, // violation: this on opening but call not stacked
				other(
						arg
				)
		);
	}

	void thisUnstackedCallsClosingStacked() {
		method(this, // violation: closing parens stacked but calls not stacked
				other(
						arg
				));
	}

	void unstackedCallsClosingStacked() {
		method( // violation: closing parens stacked but calls not stacked
				other(
						arg
				));
	}
}