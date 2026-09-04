package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: block_comment_after_dot ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansBlockCommentAfterDotSliceViolation {
	Object first(Object a) {
		return Objects./* keep */requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects./* keep */requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: block_comment_before_dot ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansBlockCommentBeforeDotSliceViolation {
	Object first(Object a) {
		return Objects/* keep */.requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects/* keep */.requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: block_comment_left_open_in_span ===
// imports: java.util.Objects
// multi-fix-expected
// skip-reason: cannot add a static import: the text between the class name and the method name cannot be folded onto one line without losing a comment
class InputPreferStaticImportSpansBlockCommentLeftOpenInSpanSliceViolation {
	Object first(Object a) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				/* open
				still */.requireNonNull(a);
	}

	Object second(Object b) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				/* open
				still */.requireNonNull(b);
	}
}
// === end ===

// === case: chain_head_call ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansChainHeadCallSliceViolation {
	String first(Object a) {
		return Objects.requireNonNull(a).toString(); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	String second(Object b) {
		return Objects.requireNonNull(b).toString(); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: char_literal_ahead_of_span ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansCharLiteralAheadOfSpanSliceViolation {
	Object first(Object a) {
		return take('/', Objects/* c */.requireNonNull(a)); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return take('/', Objects/* c */.requireNonNull(b)); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object take(char separator, Object value) {
		return separator + value.toString();
	}
}
// === end ===

// === case: division_before_carried_comment ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansDivisionBeforeCarriedCommentSliceViolation {
	int first(int n, Integer len) {
		return n/Objects/* c */.requireNonNull(len); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	int second(int n, Integer len) {
		return n/Objects/* c */.requireNonNull(len) + 1; // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: dot_at_end_of_line ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansDotAtEndOfLineSliceViolation {
	Object first(Object a) {
		return Objects. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				requireNonNull(a);
	}

	Object second(Object b) {
		return Objects. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				requireNonNull(b);
	}
}
// === end ===

// === case: dot_at_end_of_line_over_the_line_budget ===
// imports: java.util.Objects
// multi-fix-expected
// skip-reason: cannot add a static import: folding the call onto one line would push it past the line length limit
class InputPreferStaticImportSpansDotAtEndOfLineOverTheLineBudgetSliceViolation {
	Object first(Object someReasonablyLongParameterName, Object anotherFairlyLongName) {
		return someReasonablyLongParameterName.toString() + anotherFairlyLongName + Objects. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				requireNonNull(someReasonablyLongParameterName);
	}

	Object second(Object someReasonablyLongParameterName, Object anotherFairlyLongName) {
		return anotherFairlyLongName.toString() + someReasonablyLongParameterName + Objects. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				requireNonNull(anotherFairlyLongName);
	}
}
// === end ===

// === case: line_comment_in_span ===
// imports: java.util.Objects
// multi-fix-expected
// skip-reason: cannot add a static import: the text between the class name and the method name cannot be folded onto one line without losing a comment
class InputPreferStaticImportSpansLineCommentInSpanSliceViolation {
	Object first(Object a) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				// why
				.requireNonNull(a);
	}

	Object second(Object b) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				// why
				.requireNonNull(b);
	}
}
// === end ===

// === case: nested_same_call ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansNestedSameCallSliceViolation {
	Object first(Object a) {
		return Objects.requireNonNull(Objects.requireNonNull(a)); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects.requireNonNull(Objects.requireNonNull(b)); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: receiver_at_end_of_line ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverAtEndOfLineSliceViolation {
	Object first(Object a) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.requireNonNull(a);
	}

	Object second(Object b) {
		return Objects // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.requireNonNull(b);
	}
}
// === end ===

// === case: receiver_in_anonymous_class_body ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInAnonymousClassBodySliceViolation {
	interface Pair {
		boolean left(Object v);

		boolean right(Object v);
	}

	Pair first() {
		return new Pair() {
			@Override
			public boolean left(Object v) {
				return Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
			}

			@Override
			public boolean right(Object v) {
				return Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
			}
		};
	}
}
// === end ===

// === case: receiver_in_block_lambda_body ===
// imports: java.util.List
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInBlockLambdaBodySliceViolation {
	void first(List<Object> items) {
		items.forEach(v -> {
			Objects . requireNonNull(v); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
			System.out.println(v);
		});
	}

	void second(List<Object> items) {
		items.forEach(v -> {
			Objects . requireNonNull(v); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
			System.out.println(v);
		});
	}
}
// === end ===

// === case: receiver_in_braceless_if_body ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInBracelessIfBodySliceViolation {
	Object first(boolean flag, Object v) {
		if (flag)
			return Objects . requireNonNull(v); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		return v;
	}

	Object second(boolean flag, Object v) {
		if (flag)
			return Objects . requireNonNull(v); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		return v;
	}
}
// === end ===

// === case: receiver_in_enum_constant_body ===
// imports: java.util.Objects
// multi-fix-expected
enum InputPreferStaticImportSpansReceiverInEnumConstantBodySliceViolation {
	FIRST {
		@Override
		boolean check(Object v) {
			return Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		}
	},
	SECOND {
		@Override
		boolean check(Object v) {
			return Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		}
	};

	abstract boolean check(Object v);
}
// === end ===

// === case: receiver_in_field_initializer ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInFieldInitializerSliceViolation {
	boolean firstValueIsNull = Objects/* c */.isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
	boolean secondValueIsNull = Objects/* c */.isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
}
// === end ===

// === case: receiver_in_instance_initializer ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInInstanceInitializerSliceViolation {
	boolean first, second;

	{
		first = Objects . isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		second = Objects . isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
	}
}
// === end ===

// === case: receiver_in_static_initializer ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInStaticInitializerSliceViolation {
	static boolean first, second;

	static {
		first = Objects . isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		second = Objects . isNull(null); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
	}
}
// === end ===

// === case: receiver_in_switch_rule ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInSwitchRuleSliceViolation {
	boolean first(int kind, Object v) {
		return switch (kind) {
			case 1 -> Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
			default -> false;
		};
	}

	boolean second(int kind, Object v) {
		return switch (kind) {
			case 1 -> Objects . isNull(v); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
			default -> false;
		};
	}
}
// === end ===

// === case: receiver_in_ternary_branch ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansReceiverInTernaryBranchSliceViolation {
	Object first(boolean flag, Object v, Object w) {
		return flag ? Objects . requireNonNull(v) : w; // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(boolean flag, Object v, Object w) {
		return flag ? Objects . requireNonNull(w) : v; // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: spaced_dot ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansSpacedDotSliceViolation {
	Object first(Object a) {
		return Objects . requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects . requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: string_with_slash_ahead_of_span ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansStringWithSlashAheadOfSpanSliceViolation {
	String first(Object a) {
		return "a/b" + Objects/* c */.requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	String second(Object b) {
		return "a/b" + Objects/* c */.requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: supplementary_inside_carried_comment ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansSupplementaryInsideCarriedCommentSliceViolation {
	Object first(Object a) {
		return Objects/* 𝐀 */.requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects/* 𝐀 */.requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: trailing_comment_after_call ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansTrailingCommentAfterCallSliceViolation {
	Object first(Object a) {
		return Objects.requireNonNull(a); /* keep */ // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects.requireNonNull(b); /* keep */ // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===

// === case: two_calls_on_one_line ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansTwoCallsOnOneLineSliceViolation {
	boolean first(Object a, Object b) {
		return Objects.isNull(a) || Objects . isNull(b); // violation: Replace 'Objects.isNull' with a static import of 'isNull'. // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
	}

	boolean second(Object a, Object b) {
		return Objects.isNull(b) || Objects . isNull(a); // violation: Replace 'Objects.isNull' with a static import of 'isNull'. // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
	}
}
// === end ===

// === case: two_comments_in_span ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportSpansTwoCommentsInSpanSliceViolation {
	Object first(Object a) {
		return Objects/* a */./* b */requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}

	Object second(Object b) {
		return Objects/* a */./* b */requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
	}
}
// === end ===