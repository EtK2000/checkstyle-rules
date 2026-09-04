package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: block_comment_after_dot ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansBlockCommentAfterDotSliceViolation {
	Object first(Object a) {
		return /* keep */ requireNonNull(a);
	}

	Object second(Object b) {
		return /* keep */ requireNonNull(b);
	}
}
// === end ===

// === case: block_comment_before_dot ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansBlockCommentBeforeDotSliceViolation {
	Object first(Object a) {
		return /* keep */ requireNonNull(a);
	}

	Object second(Object b) {
		return /* keep */ requireNonNull(b);
	}
}
// === end ===

// === case: block_comment_left_open_in_span ===
// imports: java.util.Objects
// skip-reason: cannot add a static import: the text between the class name and the method name cannot be folded onto one line without losing a comment
class InputPreferStaticImportSpansBlockCommentLeftOpenInSpanSliceViolation {
	Object first(Object a) {
		return Objects
				/* open
				still */.requireNonNull(a);
	}

	Object second(Object b) {
		return Objects
				/* open
				still */.requireNonNull(b);
	}
}
// === end ===

// === case: chain_head_call ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansChainHeadCallSliceViolation {
	String first(Object a) {
		return requireNonNull(a).toString();
	}

	String second(Object b) {
		return requireNonNull(b).toString();
	}
}
// === end ===

// === case: char_literal_ahead_of_span ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansCharLiteralAheadOfSpanSliceViolation {
	Object first(Object a) {
		return take('/', /* c */ requireNonNull(a));
	}

	Object second(Object b) {
		return take('/', /* c */ requireNonNull(b));
	}

	Object take(char separator, Object value) {
		return separator + value.toString();
	}
}
// === end ===

// === case: division_before_carried_comment ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansDivisionBeforeCarriedCommentSliceViolation {
	int first(int n, Integer len) {
		return n/ /* c */ requireNonNull(len);
	}

	int second(int n, Integer len) {
		return n/ /* c */ requireNonNull(len) + 1;
	}
}
// === end ===

// === case: dot_at_end_of_line ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansDotAtEndOfLineSliceViolation {
	Object first(Object a) {
		return requireNonNull(a);
	}

	Object second(Object b) {
		return requireNonNull(b);
	}
}
// === end ===

// === case: dot_at_end_of_line_over_the_line_budget ===
// imports: java.util.Objects
// skip-reason: cannot add a static import: folding the call onto one line would push it past the line length limit
class InputPreferStaticImportSpansDotAtEndOfLineOverTheLineBudgetSliceViolation {
	Object first(Object someReasonablyLongParameterName, Object anotherFairlyLongName) {
		return someReasonablyLongParameterName.toString() + anotherFairlyLongName + Objects.
				requireNonNull(someReasonablyLongParameterName);
	}

	Object second(Object someReasonablyLongParameterName, Object anotherFairlyLongName) {
		return anotherFairlyLongName.toString() + someReasonablyLongParameterName + Objects.
				requireNonNull(anotherFairlyLongName);
	}
}
// === end ===

// === case: line_comment_in_span ===
// imports: java.util.Objects
// skip-reason: cannot add a static import: the text between the class name and the method name cannot be folded onto one line without losing a comment
class InputPreferStaticImportSpansLineCommentInSpanSliceViolation {
	Object first(Object a) {
		return Objects
				// why
				.requireNonNull(a);
	}

	Object second(Object b) {
		return Objects
				// why
				.requireNonNull(b);
	}
}
// === end ===

// === case: nested_same_call ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansNestedSameCallSliceViolation {
	Object first(Object a) {
		return requireNonNull(requireNonNull(a));
	}

	Object second(Object b) {
		return requireNonNull(requireNonNull(b));
	}
}
// === end ===

// === case: receiver_at_end_of_line ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansReceiverAtEndOfLineSliceViolation {
	Object first(Object a) {
		return requireNonNull(a);
	}

	Object second(Object b) {
		return requireNonNull(b);
	}
}
// === end ===

// === case: receiver_in_anonymous_class_body ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansReceiverInAnonymousClassBodySliceViolation {
	interface Pair {
		boolean left(Object v);

		boolean right(Object v);
	}

	Pair first() {
		return new Pair() {
			@Override
			public boolean left(Object v) {
				return isNull(v);
			}

			@Override
			public boolean right(Object v) {
				return isNull(v);
			}
		};
	}
}
// === end ===

// === case: receiver_in_block_lambda_body ===
// imports: java.util.List
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansReceiverInBlockLambdaBodySliceViolation {
	void first(List<Object> items) {
		items.forEach(v -> {
			requireNonNull(v);
			System.out.println(v);
		});
	}

	void second(List<Object> items) {
		items.forEach(v -> {
			requireNonNull(v);
			System.out.println(v);
		});
	}
}
// === end ===

// === case: receiver_in_braceless_if_body ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansReceiverInBracelessIfBodySliceViolation {
	Object first(boolean flag, Object v) {
		if (flag)
			return requireNonNull(v);
		return v;
	}

	Object second(boolean flag, Object v) {
		if (flag)
			return requireNonNull(v);
		return v;
	}
}
// === end ===

// === case: receiver_in_enum_constant_body ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
enum InputPreferStaticImportSpansReceiverInEnumConstantBodySliceViolation {
	FIRST {
		@Override
		boolean check(Object v) {
			return isNull(v);
		}
	},
	SECOND {
		@Override
		boolean check(Object v) {
			return isNull(v);
		}
	};

	abstract boolean check(Object v);
}
// === end ===

// === case: receiver_in_field_initializer ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansReceiverInFieldInitializerSliceViolation {
	boolean firstValueIsNull = /* c */ isNull(null);
	boolean secondValueIsNull = /* c */ isNull(null);
}
// === end ===

// === case: receiver_in_instance_initializer ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansReceiverInInstanceInitializerSliceViolation {
	boolean first, second;

	{
		first = isNull(null);
		second = isNull(null);
	}
}
// === end ===

// === case: receiver_in_static_initializer ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansReceiverInStaticInitializerSliceViolation {
	static boolean first, second;

	static {
		first = isNull(null);
		second = isNull(null);
	}
}
// === end ===

// === case: receiver_in_switch_rule ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansReceiverInSwitchRuleSliceViolation {
	boolean first(int kind, Object v) {
		return switch (kind) {
			case 1 -> isNull(v);
			default -> false;
		};
	}

	boolean second(int kind, Object v) {
		return switch (kind) {
			case 1 -> isNull(v);
			default -> false;
		};
	}
}
// === end ===

// === case: receiver_in_ternary_branch ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansReceiverInTernaryBranchSliceViolation {
	Object first(boolean flag, Object v, Object w) {
		return flag ? requireNonNull(v) : w;
	}

	Object second(boolean flag, Object v, Object w) {
		return flag ? requireNonNull(w) : v;
	}
}
// === end ===

// === case: spaced_dot ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansSpacedDotSliceViolation {
	Object first(Object a) {
		return requireNonNull(a);
	}

	Object second(Object b) {
		return requireNonNull(b);
	}
}
// === end ===

// === case: string_with_slash_ahead_of_span ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansStringWithSlashAheadOfSpanSliceViolation {
	String first(Object a) {
		return "a/b" + /* c */ requireNonNull(a);
	}

	String second(Object b) {
		return "a/b" + /* c */ requireNonNull(b);
	}
}
// === end ===

// === case: supplementary_inside_carried_comment ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansSupplementaryInsideCarriedCommentSliceViolation {
	Object first(Object a) {
		return /* 𝐀 */ requireNonNull(a);
	}

	Object second(Object b) {
		return /* 𝐀 */ requireNonNull(b);
	}
}
// === end ===

// === case: trailing_comment_after_call ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansTrailingCommentAfterCallSliceViolation {
	Object first(Object a) {
		return requireNonNull(a); /* keep */
	}

	Object second(Object b) {
		return requireNonNull(b); /* keep */
	}
}
// === end ===

// === case: two_calls_on_one_line ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportSpansTwoCallsOnOneLineSliceViolation {
	boolean first(Object a, Object b) {
		return isNull(a) || isNull(b);
	}

	boolean second(Object a, Object b) {
		return isNull(b) || isNull(a);
	}
}
// === end ===

// === case: two_comments_in_span ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportSpansTwoCommentsInSpanSliceViolation {
	Object first(Object a) {
		return /* a */ /* b */ requireNonNull(a);
	}

	Object second(Object b) {
		return /* a */ /* b */ requireNonNull(b);
	}
}
// === end ===