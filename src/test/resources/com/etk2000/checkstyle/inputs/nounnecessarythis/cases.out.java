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

// === case: remove_this_in_field_initializer_reading_earlier_declarator ===
class InputThisFieldInitEarlierDeclaratorSliceViolation {
	int beta = 2, alpha = beta;
}
// === end ===

// === case: remove_this_in_field_initializer_reading_earlier_field ===
class InputThisFieldInitEarlierFieldSliceViolation {
	int beta;
	long alpha = beta;
}
// === end ===

// === case: remove_this_in_field_initializer_with_later_field_sharing_prefix ===
class InputThisFieldInitLaterPrefixFieldSliceViolation {
	int beta;
	long alpha = beta;
	String betaTwo;
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

// === case: remove_this_with_bindings_of_other_names ===
// imports: java.io.IOException
// imports: java.io.StringReader
// imports: java.util.List
// imports: java.util.function.Predicate
class InputThisBindingsOfOtherNamesSliceViolation {
	record Box(Object other) {}

	final Object name = "n";
	final boolean matches = name instanceof String other && other.equals(name);
	final Predicate<Object> sameName = other -> name.equals(other);
	final Runnable dump = () -> {
		final var other = "tmp";
		System.out.println(name + other);
	};

	void catchParameter() {
		try {
			System.out.println(name);
		}
		catch (RuntimeException other) {
			System.out.println(name + other.getMessage());
		}
	}

	void forEachVariable(List<Object> values) {
		for (var other : values)
			System.out.println(name.toString() +other);
	}

	void instanceofPattern(Object value) {
		if (value instanceof String other)
			System.out.println(name + other);
	}

	void localClassBinding() {
		class Local {
			void print(Object name) {
				System.out.println(name);
			}
		}

		new Local().print(name);
	}

	void recordPattern(Object value) {
		if (value instanceof Box(var other))
			System.out.println(name.toString() +other);
	}

	void switchPattern(Object value) {
		switch (value) {
			case String other -> System.out.println(name + other);
			default -> System.out.println(value);
		}
	}

	void tryResource() throws IOException {
		try (var other = new StringReader("")) {
			System.out.println(name.toString() +other.read());
		}
	}
}
// === end ===