package com.etk2000.checkstyle.inputs.preferprefixincrement;

// === case: array_operand ===
class InputPreferPrefixIncrementArrayOperandSliceViolation {
	void m(int[] arr, int i) {
		++arr[i];
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
			++i;
	}
}
// === end ===

// === case: braceless_for_body ===
class InputPrefixBracelessForBodySliceViolation {
	void m() {
		var i = 0;
		for (var j = 0; j < 10; ++j)
			++i;
	}
}
// === end ===

// === case: braceless_for_each_body ===
class InputPreferPrefixIncrementBracelessForEachBodySliceViolation {
	void m(int[] values) {
		var i = 0;
		for (var v : values)
			++i;
		System.out.println(i);
	}
}
// === end ===

// === case: braceless_if ===
class InputPrefixBracelessIfSliceViolation {
	void m(boolean flag) {
		var i = 0;
		if (flag)
			++i;
	}
}
// === end ===

// === case: braceless_while ===
class InputPrefixBracelessWhileSliceViolation {
	void m(boolean flag) {
		var i = 0;
		while (flag)
			--i;
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
		++holder().count;
	}
}
// === end ===

// === case: cast_operand ===
class InputPreferPrefixIncrementCastOperandSliceViolation {
	static class Holder {
		int count;
	}

	void m(Object o) {
		++((Holder) o).count;
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
		++o.inner.count;
	}
}
// === end ===

// === case: comment_before_operator ===
class InputPreferPrefixIncrementCommentBeforeOperatorSliceViolation {
	void m() {
		var i = 0;
		++i /* keep */;
	}
}
// === end ===

// === case: for_loop_update ===
class InputPrefixForLoopUpdateSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i) {
			System.out.println(i);
		}
	}
}
// === end ===

// === case: for_loop_update_multi ===
class InputPreferPrefixIncrementForLoopUpdateMultiSliceViolation {
	void m() {
		var j = 0;
		for (var i = 0; i < 2; ++i, ++j)
			System.out.println(i + j);
	}
}
// === end ===

// === case: lambda_body ===
class InputPreferPrefixIncrementLambdaBodySliceViolation {
	private int count;

	void m() {
		final Runnable r = () -> {
			++count;
		};
		r.run();
	}
}
// === end ===

// === case: main ===
class InputPrefixViolation {
	void bracedDoWhile(boolean flag) {
		var i = 0;
		do ++i;
		while (flag);
	}

	void bracelessDoWhile(boolean flag) {
		var i = 0;
		do ++i;
		while (flag);
	}

	void standaloneStatement() {
		var i = 0;
		++i;
		--i;
	}
}
// === end ===

// === case: multi_char_identifier ===
class InputPrefixMultiCharIdentSliceViolation {
	void m() {
		var count = 0;
		++count;
	}
}
// === end ===

// === case: multi_char_identifier_decrement ===
class InputPrefixMultiCharDecrementSliceViolation {
	void m() {
		var count = 0;
		--count;
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
				.count++;
	}
}
// === end ===

// === case: nested_postfix_operand ===
class InputPreferPrefixIncrementNestedPostfixOperandSliceViolation {
	void m(int[] arr, int i) {
		++arr[i++];
	}
}
// === end ===

// === case: operator_at_end_of_line ===
class InputPrefixOperatorAtEndOfLineSliceViolation {
	void m() {
		var i = 0;
		++i
		;
	}
}
// === end ===

// === case: postfix_decrement_to_prefix ===
class InputPrefixPostfixDecrementSliceViolation {
	void m() {
		var i = 0;
		--i;
	}
}
// === end ===

// === case: postfix_increment_to_prefix ===
class InputPrefixPostfixIncrementSliceViolation {
	void m() {
		var i = 0;
		++i;
	}
}
// === end ===

// === case: qualified_operand ===
class InputPreferPrefixIncrementQualifiedOperandSliceViolation {
	static class Holder {
		int count;
	}

	void m(Holder h) {
		++h.count;
	}
}
// === end ===

// === case: slist_contexts ===
class InputPreferPrefixIncrementSlistContextsSliceViolation {
	private static int total;

	static {
		++total;
	}

	private int count;

	{
		++count;
	}

	InputPreferPrefixIncrementSlistContextsSliceViolation() {
		++count;
	}

	void m() {
		try {
			System.out.println(count);
		}
		catch (RuntimeException e) {
			--count;
		}
	}
}
// === end ===

// === case: spaced_operator ===
class InputPreferPrefixIncrementSpacedOperatorSliceViolation {
	void m() {
		var i = 0;
		++i;
	}
}
// === end ===

// === case: supplementary_before_operand ===
class InputPreferPrefixIncrementSupplementaryBeforeOperandSliceViolation {
	void m() {
		for (var 𝐀 = 0; 𝐀 < 2; ++𝐀)
			System.out.println(𝐀);
	}
}
// === end ===

// === case: supplementary_operand ===
class InputPreferPrefixIncrementSupplementaryOperandSliceViolation {
	void m() {
		var 𝐀count = 0;
		++𝐀count;
	}
}
// === end ===

// === case: switch_case_group_body ===
class InputPreferPrefixIncrementSwitchCaseGroupBodySliceViolation {
	void m(int k) {
		var i = 0;
		switch (k) {
			case 1:
				++i;
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
		++this.count;
	}
}
// === end ===

// === case: underscore_in_ident ===
class InputPrefixUnderscoreSliceViolation {
	void m() {
		var my_var = 0;
		++my_var;
	}
}
// === end ===