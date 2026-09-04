package com.etk2000.checkstyle.inputs.nounnecessarythis;

// === case: remove_this ===
class InputThisRemoveSliceViolation {
	String field;

	String m() {
		return this.field; // violation: Remove the unnecessary 'this.' from 'this.field' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_at_start_of_line ===
class InputThisStartOfLineSliceViolation {
	void caller() {
this.doSomething(); // violation: Remove the unnecessary 'this.' from 'this.doSomething' (only needed when shadowing or assigning a field).
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_expression ===
class InputThisRemoveInExpressionSliceViolation {
	int value;

	int m() {
		return this.value + 1; // violation: Remove the unnecessary 'this.' from 'this.value' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_in_field_initializer_reading_earlier_declarator ===
class InputThisFieldInitEarlierDeclaratorSliceViolation {
	int beta = 2, alpha = this.beta; // violation: Remove the unnecessary 'this.' from 'this.beta' (only needed when shadowing or assigning a field).
}
// === end ===

// === case: remove_this_in_field_initializer_reading_earlier_field ===
class InputThisFieldInitEarlierFieldSliceViolation {
	int beta;
	long alpha = this.beta; // violation: Remove the unnecessary 'this.' from 'this.beta' (only needed when shadowing or assigning a field).
}
// === end ===

// === case: remove_this_in_field_initializer_with_later_field_sharing_prefix ===
class InputThisFieldInitLaterPrefixFieldSliceViolation {
	int beta;
	long alpha = this.beta; // violation: Remove the unnecessary 'this.' from 'this.beta' (only needed when shadowing or assigning a field).
	String betaTwo;
}
// === end ===

// === case: remove_this_in_instance_initializer ===
class InputThisInstanceInitSliceViolation {
	int field;

	{
		System.out.println(this.field); // violation: Remove the unnecessary 'this.' from 'this.field' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_in_lambda_body ===
class InputThisLambdaSliceViolation {
	int field;

	void lambdaWithoutShadowing() {
		final Runnable r = () -> System.out.println(this.field); // violation: Remove the unnecessary 'this.' from 'this.field' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_in_method_call ===
class InputThisRemoveInMethodCallSliceViolation {
	void caller() {
		this.doSomething(); // violation: Remove the unnecessary 'this.' from 'this.doSomething' (only needed when shadowing or assigning a field).
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_in_parens ===
class InputThisRemoveInParensSliceViolation {
	int bar;

	void caller() {
		foo(this.bar); // violation: Remove the unnecessary 'this.' from 'this.bar' (only needed when shadowing or assigning a field).
	}

	void foo(int x) {}
}
// === end ===

// === case: remove_this_in_record_compact_constructor ===
record InputThisRecordSliceViolation(int field) {
	InputThisRecordSliceViolation {
		System.out.println(this.field); // violation: Remove the unnecessary 'this.' from 'this.field' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_in_void_method ===
class InputThisVoidMethodSliceViolation {
	int field;

	void method() {
		System.out.println(this.field); // violation: Remove the unnecessary 'this.' from 'this.field' (only needed when shadowing or assigning a field).
	}
}
// === end ===

// === case: remove_this_with_bindings_of_other_names ===
// imports: java.io.IOException
// imports: java.io.StringReader
// imports: java.util.List
// imports: java.util.function.Predicate
class InputThisBindingsOfOtherNamesSliceViolation {
	record Box(Object other) {}

	final Object name = "n";
	final boolean matches = name instanceof String other && other.equals(this.name); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	final Predicate<Object> sameName = other -> this.name.equals(other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	final Runnable dump = () -> {
		final var other = "tmp";
		System.out.println(this.name + other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	};

	void catchParameter() {
		try {
			System.out.println(name);
		}
		catch (RuntimeException other) {
			System.out.println(this.name + other.getMessage()); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
		}
	}

	void forEachVariable(List<Object> values) {
		for (var other : values)
			System.out.println(this.name.toString() +other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	}

	void instanceofPattern(Object value) {
		if (value instanceof String other)
			System.out.println(this.name + other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	}

	void localClassBinding() {
		class Local {
			void print(Object name) {
				System.out.println(name);
			}
		}

		new Local().print(this.name); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	}

	void recordPattern(Object value) {
		if (value instanceof Box(var other))
			System.out.println(this.name.toString() +other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
	}

	void switchPattern(Object value) {
		switch (value) {
			case String other -> System.out.println(this.name + other); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
			default -> System.out.println(value);
		}
	}

	void tryResource() throws IOException {
		try (var other = new StringReader("")) {
			System.out.println(this.name.toString() +other.read()); // violation: Remove the unnecessary 'this.' from 'this.name' (only needed when shadowing or assigning a field).
		}
	}
}
// === end ===