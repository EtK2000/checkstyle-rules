package com.etk2000.checkstyle.inputs.preferprefixincrement;

// === case: array_operand ===
class InputPreferPrefixIncrementArrayOperandSliceViolation {
	void m(int[] arr, int i) {
		arr[i]++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: braceless_else ===
class InputPrefixBracelessElseSliceViolation {
	void m(boolean flag) {
		var i = 0;
		if (flag)
			++i;
		else
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: braceless_for_body ===
class InputPrefixBracelessForBodySliceViolation {
	void m() {
		var i = 0;
		for (var j = 0; j < 10; ++j)
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: braceless_for_each_body ===
class InputPreferPrefixIncrementBracelessForEachBodySliceViolation {
	void m(int[] values) {
		var i = 0;
		for (var v : values)
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
		System.out.println(i);
	}
}
// === end ===

// === case: braceless_if ===
class InputPrefixBracelessIfSliceViolation {
	void m(boolean flag) {
		var i = 0;
		if (flag)
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: braceless_while ===
class InputPrefixBracelessWhileSliceViolation {
	void m(boolean flag) {
		var i = 0;
		while (flag)
			i--; // violation: Use prefix decrement (--x) instead of postfix (x--).
	}
}
// === end ===

// === case: call_qualified_operand ===
class InputPreferPrefixIncrementCallQualifiedOperandSliceViolation {
	static class Holder {
		int count;
	}

	Holder holder() {
		return new Holder();
	}

	void m() {
		holder().count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: cast_operand ===
class InputPreferPrefixIncrementCastOperandSliceViolation {
	static class Holder {
		int count;
	}

	void m(Object o) {
		((Holder) o).count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: chained_qualified_operand ===
class InputPreferPrefixIncrementChainedQualifiedOperandSliceViolation {
	static class Inner {
		int count;
	}

	static class Outer {
		final Inner inner = new Inner();
	}

	void m(Outer o) {
		o.inner.count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: comment_before_operator ===
class InputPreferPrefixIncrementCommentBeforeOperatorSliceViolation {
	void m() {
		var i = 0;
		i /* keep */ ++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: for_loop_update ===
class InputPrefixForLoopUpdateSliceViolation {
	void m() {
		for (var i = 0; i < 10; i++) { // violation: Use prefix increment (++x) instead of postfix (x++).
			System.out.println(i);
		}
	}
}
// === end ===

// === case: for_loop_update_multi ===
class InputPreferPrefixIncrementForLoopUpdateMultiSliceViolation {
	void m() {
		var j = 0;
		for (var i = 0; i < 2; i++, j++) // violation: Use prefix increment (++x) instead of postfix (x++). // violation: Use prefix increment (++x) instead of postfix (x++).
			System.out.println(i + j);
	}
}
// === end ===

// === case: lambda_body ===
class InputPreferPrefixIncrementLambdaBodySliceViolation {
	private int count;

	void m() {
		final Runnable r = () -> {
			count++; // violation: Use prefix increment (++x) instead of postfix (x++).
		};
		r.run();
	}
}
// === end ===

// === case: main ===
class InputPrefixViolation {
	void bracedDoWhile(boolean flag) {
		var i = 0;
		do {
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
		} while (flag);
	}

	void bracelessDoWhile(boolean flag) {
		var i = 0;
		do
			i++; // violation: Use prefix increment (++x) instead of postfix (x++).
		while (flag);
	}

	void standaloneStatement() {
		var i = 0;
		i++; // violation: Use prefix increment (++x) instead of postfix (x++).
		i--; // violation: Use prefix decrement (--x) instead of postfix (x--).
	}
}
// === end ===

// === case: multi_char_identifier ===
class InputPrefixMultiCharIdentSliceViolation {
	void m() {
		var count = 0;
		count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: multi_char_identifier_decrement ===
class InputPrefixMultiCharDecrementSliceViolation {
	void m() {
		var count = 0;
		count--; // violation: Use prefix decrement (--x) instead of postfix (x--).
	}
}
// === end ===

// === case: multiline_operand ===
// skip-reason: operand starts on an earlier line than its ++/--
class InputPreferPrefixIncrementMultilineOperandSliceViolation {
	static class Holder {
		int count;
	}

	void m(Holder h) {
		h
				.count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: nested_postfix_operand ===
class InputPreferPrefixIncrementNestedPostfixOperandSliceViolation {
	void m(int[] arr, int i) {
		arr[i++]++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: operator_at_end_of_line ===
class InputPrefixOperatorAtEndOfLineSliceViolation {
	void m() {
		var i = 0;
		i++ // violation: Use prefix increment (++x) instead of postfix (x++).
		;
	}
}
// === end ===

// === case: postfix_decrement_to_prefix ===
class InputPrefixPostfixDecrementSliceViolation {
	void m() {
		var i = 0;
		i--; // violation: Use prefix decrement (--x) instead of postfix (x--).
	}
}
// === end ===

// === case: postfix_increment_to_prefix ===
class InputPrefixPostfixIncrementSliceViolation {
	void m() {
		var i = 0;
		i++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: qualified_operand ===
class InputPreferPrefixIncrementQualifiedOperandSliceViolation {
	static class Holder {
		int count;
	}

	void m(Holder h) {
		h.count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: slist_contexts ===
// multi-fix-expected
class InputPreferPrefixIncrementSlistContextsSliceViolation {
	private static int total;

	static {
		total++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}

	private int count;

	{
		count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}

	InputPreferPrefixIncrementSlistContextsSliceViolation() {
		count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}

	void m() {
		try {
			System.out.println(count);
		}
		catch (RuntimeException e) {
			count--; // violation: Use prefix decrement (--x) instead of postfix (x--).
		}
	}
}
// === end ===

// === case: spaced_operator ===
class InputPreferPrefixIncrementSpacedOperatorSliceViolation {
	void m() {
		var i = 0;
		i ++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: supplementary_before_operand ===
class InputPreferPrefixIncrementSupplementaryBeforeOperandSliceViolation {
	void m() {
		for (var 𝐀 = 0; 𝐀 < 2; 𝐀++) // violation: Use prefix increment (++x) instead of postfix (x++).
			System.out.println(𝐀);
	}
}
// === end ===

// === case: supplementary_operand ===
class InputPreferPrefixIncrementSupplementaryOperandSliceViolation {
	void m() {
		var 𝐀count = 0;
		𝐀count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: switch_case_group_body ===
class InputPreferPrefixIncrementSwitchCaseGroupBodySliceViolation {
	void m(int k) {
		var i = 0;
		switch (k) {
			case 1:
				i++; // violation: Use prefix increment (++x) instead of postfix (x++).
				// falls through

			case 2:
				System.out.println(i);
				break;

			default:
				System.out.println(k);
		}
	}
}
// === end ===

// === case: this_qualified_operand ===
class InputPreferPrefixIncrementThisQualifiedOperandSliceViolation {
	private int count;

	void m(int count) {
		this.count = count;
		this.count++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===

// === case: underscore_in_ident ===
class InputPrefixUnderscoreSliceViolation {
	void m() {
		var my_var = 0;
		my_var++; // violation: Use prefix increment (++x) instead of postfix (x++).
	}
}
// === end ===