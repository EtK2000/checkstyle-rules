package com.etk2000.checkstyle.inputs.nounnecessarythis;

// === case: remove_this ===
class InputThisRemoveSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_array_element_assignment_target ===
class InputThisArrayElementAssignmentTargetSliceViolation {
	int[] values;

	void m() {
		values[0] = 1;
	}
}
// === end ===

// === case: remove_this_assignment_right_hand_side ===
class InputThisAssignmentRightHandSideSliceViolation {
	int field, other;

	void m() {
		other = field;
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

// === case: remove_this_block_comment_spanning_lines ===
// skip-reason: cannot remove the 'this.' qualifier: the text between it and the member cannot be folded onto one line without losing a comment
class InputThisBlockCommentSpanningLinesSliceViolation {
	String field;

	String m() {
		return this /* a
				b */ .field;
	}
}
// === end ===

// === case: remove_this_comment_after_dot ===
class InputThisCommentAfterDotSliceViolation {
	String field;

	String m() {
		return /* c */ field;
	}
}
// === end ===

// === case: remove_this_comment_before_dot ===
class InputThisCommentBeforeDotSliceViolation {
	String field;

	String m() {
		return /* c */ field;
	}
}
// === end ===

// === case: remove_this_comment_before_dot_on_a_call ===
class InputThisCommentBeforeDotOnACallSliceViolation {
	void caller() {
		/* c */ doSomething();
	}

	void doSomething() {}
}
// === end ===

// === case: remove_this_compound_assignment ===
class InputThisCompoundAssignmentSliceViolation {
	int field;

	void m() {
		field += 1;
	}
}
// === end ===

// === case: remove_this_division_before_carried_comment ===
class InputThisDivisionBeforeCarriedCommentSliceViolation {
	int divisor, field;

	int m() {
		return divisor/ /* c */ field;
	}
}
// === end ===

// === case: remove_this_dot_on_next_line ===
class InputThisDotOnNextLineSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_dot_on_next_line_with_line_comment ===
// skip-reason: cannot remove the 'this.' qualifier: the text between it and the member cannot be folded onto one line without losing a comment
class InputThisDotOnNextLineWithLineCommentSliceViolation {
	String field;

	String m() {
		return this // why
				.field;
	}
}
// === end ===

// === case: remove_this_in_colon_switch ===
class InputThisInColonSwitchSliceViolation {
	String field;

	void m(int k) {
		switch (k) {
			case 1:
				System.out.println(field);
				// falls through
			case 2:
				System.out.println(k);
				break;

			default:
				System.out.println(-k);
				break;
		}
	}
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

// === case: remove_this_in_multiline_chain ===
class InputThisInMultilineChainSliceViolation {
	String field;

	int m() {
		return field
				.trim()
				.length();
	}
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

// === case: remove_this_in_record_accessor_override ===
record InputThisInRecordAccessorOverrideSliceViolation(int field) {
	@Override
	public int field() {
		return field;
	}
}
// === end ===

// === case: remove_this_in_record_compact_constructor ===
record InputThisRecordSliceViolation(int field) {
	InputThisRecordSliceViolation {
		System.out.println(field);
	}
}
// === end ===

// === case: remove_this_in_string_neighbourhood ===
class InputThisInStringNeighbourhoodSliceViolation {
	String field;

	String m() {
		return "this.field" + field;
	}
}
// === end ===

// === case: remove_this_in_switch_expression_yield ===
class InputThisInSwitchExpressionYieldSliceViolation {
	String field;

	String m(int k) {
		return switch (k) {
			case 1 -> {
				System.out.println(k);
				yield field;
			}
			default -> "";
		};
	}
}
// === end ===

// === case: remove_this_in_three_declarator_field ===
class InputThisInThreeDeclaratorFieldSliceViolation {
	int alpha = 1, beta = 2, gamma = alpha;
}
// === end ===

// === case: remove_this_in_try_resource_variable_access ===
// imports: java.io.IOException
// imports: java.io.StringReader
class InputThisTryResourceVariableAccessSliceViolation {
	final StringReader reader = new StringReader("");

	void m() throws IOException {
		try (reader) {
			System.out.println(reader.read());
		}
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

// === case: remove_this_qualified_assignment_target ===
class InputThisQualifiedAssignmentTargetSliceViolation {
	static class Inner {
		int field;
	}

	Inner inner;

	void m() {
		inner.field = 1;
	}
}
// === end ===

// === case: remove_this_receiver_parameter ===
class InputThisReceiverParameterSliceViolation {
	String field;

	String m(InputThisReceiverParameterSliceViolation this) {
		return field;
	}
}
// === end ===

// === case: remove_this_space_after_dot ===
class InputThisSpaceAfterDotSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_space_before_dot ===
class InputThisSpaceBeforeDotSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_spaced_dot ===
class InputThisSpacedDotSliceViolation {
	String field;

	String m() {
		return field;
	}
}
// === end ===

// === case: remove_this_supplementary_in_span ===
class InputThisSupplementaryInSpanSliceViolation {
	String field;

	String m() {
		return /* 𝒜 */ field;
	}
}
// === end ===

// === case: remove_this_two_comments_in_span ===
class InputThisTwoCommentsInSpanSliceViolation {
	String field;

	String m() {
		return /* a */ /* b */ field;
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

	void localAnnotationBinding() {
		final var holder = new Object() {
			@interface Local {
				String name = "a";
			}
		};

		System.out.println(holder + name);
	}

	void localClassBinding() {
		class Local {
			void print(Object name) {
				System.out.println(name);
			}
		}

		new Local().print(name);
	}

	void localEnumBinding() {
		enum Local {
			ONE;

			static final String name = "e";
		}

		System.out.println(Local.ONE + name);
	}

	void localInterfaceBinding() {
		interface Local {
			void print(Object name);
		}

		System.out.println(Local.class + name);
	}

	void localRecordBinding() {
		record Local(Object name) {}

		System.out.println(new Local("r") + name);
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

// === case: remove_this_with_trailing_comment ===
class InputThisWithTrailingCommentSliceViolation {
	String field;

	String m() {
		return field; /* keeps this.field as written */
	}
}
// === end ===