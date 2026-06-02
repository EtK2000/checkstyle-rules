package com.etk2000.checkstyle.inputs.nounnecessarythis;

// === case: remove_this ===
class InputThisRemoveSliceViolation {
	String field;

	String m() {
		return this.field; // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}
// === end ===

// === case: remove_this_at_start_of_line ===
class InputThisStartOfLineSliceViolation {
	void caller() {
this.doSomething(); // violation: Unnecessary 'this.doSomething', only use when shadowing or in field assignment.
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_expression ===
class InputThisRemoveInExpressionSliceViolation {
	int value;

	int m() {
		return this.value + 1; // violation: Unnecessary 'this.value', only use when shadowing or in field assignment.
	}
}
// === end ===

// === case: remove_this_in_instance_initializer ===
class InputThisInstanceInitSliceViolation {
	int field;

	{
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}
// === end ===

// === case: remove_this_in_lambda_body ===
class InputThisLambdaSliceViolation {
	int field;

	void lambdaWithoutShadowing() {
		final Runnable r = () -> System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}
// === end ===

// === case: remove_this_in_method_call ===
class InputThisRemoveInMethodCallSliceViolation {
	void caller() {
		this.doSomething(); // violation: Unnecessary 'this.doSomething', only use when shadowing or in field assignment.
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_parens ===
class InputThisRemoveInParensSliceViolation {
	int bar;

	void caller() {
		foo(this.bar); // violation: Unnecessary 'this.bar', only use when shadowing or in field assignment.
	}

	void foo(int x) {}
}
// === end ===

// === case: remove_this_in_record_compact_constructor ===
record InputThisRecordSliceViolation(int field) {
	InputThisRecordSliceViolation {
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}
// === end ===

// === case: remove_this_in_void_method ===
class InputThisVoidMethodSliceViolation {
	int field;

	void method() {
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}
// === end ===