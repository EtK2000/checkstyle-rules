package com.etk2000.checkstyle.inputs.nounnecessarythis;

// === case: remove_this ===
class InputThisRemoveSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_at_start_of_line ===
class InputThisStartOfLineSliceViolation {
	void caller() {
doSomething();
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_expression ===
class InputThisRemoveInExpressionSliceViolation {
	int value;

	int m() {
		return value + 1;
	}
}
// === end ===

// === case: remove_this_in_instance_initializer ===
class InputThisInstanceInitSliceViolation {
	int field;

	{
		System.out.println(field);
	}
}
// === end ===

// === case: remove_this_in_lambda_body ===
class InputThisLambdaSliceViolation {
	int field;

	void lambdaWithoutShadowing() {
		final Runnable r = () -> System.out.println(field);
	}
}
// === end ===

// === case: remove_this_in_method_call ===
class InputThisRemoveInMethodCallSliceViolation {
	void caller() {
		doSomething();
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_parens ===
class InputThisRemoveInParensSliceViolation {
	int bar;

	void caller() {
		foo(bar);
	}

	void foo(int x) {}
}
// === end ===

// === case: remove_this_in_record_compact_constructor ===
record InputThisRecordSliceViolation(int field) {
	InputThisRecordSliceViolation {
		System.out.println(field);
	}
}
// === end ===

// === case: remove_this_in_void_method ===
class InputThisVoidMethodSliceViolation {
	int field;

	void method() {
		System.out.println(field);
	}
}
// === end ===