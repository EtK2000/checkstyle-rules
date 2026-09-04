package com.etk2000.checkstyle.inputs.preferdirectbooleanreturn;

class InputPreferDirectBooleanReturnClean {
	boolean assignBoolForm(boolean flag) {
		final boolean y;
		if (flag)
			y = true;
		else
			y = false;
		return y;
	}

	void bareReturnVoidMethod(boolean flag) {
		if (flag)
			return;
		return;
	}

	boolean bothNonLiteralElseForm(boolean flag, String s) {
		if (flag)
			return s.isEmpty();
		else
			return s.isBlank();
	}

	boolean bothNonLiteralReturns(boolean flag, String s) {
		if (flag)
			return s.isEmpty();
		return s.startsWith("x");
	}

	boolean bracelessLoopBodyIf(boolean[] flags) {
		for (var flag : flags)
			if (flag)
				return true;
		return false;
	}

	boolean budgetBracedParentOneOverMax(boolean flag, String s) {
		if (flag) {
			if (s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				return true;
			else
				return true;
		}
		return false;
	}

	/** One column past {@code opposite_prefix_length_exactly_max}, whose budget counts the shared header. */
	boolean budgetInlineParentPrefixOneOverMax(boolean flag, String s) {
		if (flag) if (s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")) return true;
		else return false;
		throw new IllegalStateException();
	}

	/** One column past {@code opposite_suffix_length_exactly_max}, whose budget counts the trailing {@code &#125;}. */
	/**
	 * One column past {@code opposite_masked_atomic_length_exactly_max}. The parenthesis inside the
	 * literal no longer forces the {@code !(...)} wrap, so the budget measures the two characters that
	 * wrap would have added.
	 */
	boolean budgetNegatedMaskedAtomicOneOverMax(String s) {
		if (s.equals("(aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
			return false;
		return true;
	}

	boolean budgetSuffixOneOverMax(boolean flag, String s) {
		if (flag) {
			if (s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				return true;
			else
				return false; }
		throw new IllegalStateException();
	}

	boolean budgetWrappedParentOneOverMax(boolean flag, String s) {
		if (flag)
			if (s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				return true;
			else
				return true;
		return false;
	}

	/**
	 * The {@code same_literal_extract_budget_braced_exactly_max} slice's condition, which emits a line
	 * of exactly the budget under a braced parent. The block an unbraced parent needs indents every
	 * emitted statement one tab further, so the same collapse no longer fits and is not reported.
	 */
	boolean budgetWrappedParentOverMaxByTheAddedTab(boolean flag, String s) {
		if (flag)
			if (s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
				return true;
			else
				return true;
		return false;
	}

	boolean chainedElseIf(boolean flag, boolean other) {
		if (flag)
			return true;
		else if (other)
			return false;
		return true;
	}

	private boolean compute() {
		return true;
	}

	/**
	 * The one parent where the flagged {@code if}'s next sibling is a non-null non-return node (the
	 * {@code DO_WHILE} token), which is what keeps the trailing-return form out of every unbraced parent
	 * and so keeps the block wrap's precondition true.
	 */
	boolean doBodyIfHasNoPairedReturn(boolean flag) {
		do
			if (flag)
				return true;
		while (flag);
		return false;
	}

	boolean emptyBracedIfBody(boolean flag) {
		if (flag) {}
		return false;
	}

	boolean ifBodyNonReturn(boolean flag) {
		if (flag)
			compute();
		return false;
	}

	void ifIsLastStmtVoidMethod(boolean flag) {
		if (flag)
			return;
	}

	boolean ifNoOpposite(boolean flag) {
		if (flag)
			return true;
		compute();
		return compute();
	}

	boolean innerNotSibling(boolean outer, boolean flag) {
		if (outer) {
			if (flag)
				return true;
		}
		return false;
	}

	boolean innerNotSiblingForLoop(boolean[] flags) {
		for (var flag : flags) {
			if (flag)
				return true;
		}
		return false;
	}

	boolean labeledIf(boolean flag) {
		label: if (flag)
			return true;
		return false;
	}

	boolean lengthExemptLongCollapse(String someRatherLongVariableName, String anotherRatherLongVariableName) {
		if (someRatherLongVariableName.startsWith("prefix"))
			return anotherRatherLongVariableName.endsWith("suffix") && !anotherRatherLongVariableName.isBlank();
		return false;
	}

	boolean lengthExemptLongCollapseTrailing(String firstReasonablyLongParameterName, String secondReasonablyLongParameterName) {
		if (firstReasonablyLongParameterName.startsWith("somePrefix"))
			return true;
		return secondReasonablyLongParameterName.endsWith("someSuffix");
	}

	boolean mixedClutterMixesOperators(boolean a, boolean b) {
		if (a || b)
			return compute();
		return false;
	}

	boolean mixedClutterMixesOperatorsTrailing(boolean a, boolean b, boolean c) {
		if (a && b)
			return true;
		return c;
	}

	boolean mixedClutterThreeOperators(boolean a, boolean b, boolean c, boolean d) {
		if (a && b)
			return c && d;
		return false;
	}

	boolean mixedClutterThreeOperatorsTrailing(boolean a, boolean b, boolean c, boolean d) {
		if (a || b)
			return true;
		return c || d;
	}

	boolean multilineCondMultiStmtBody(boolean a, boolean b) {
		if (a
				&& b) {
			compute();
			return true;
		}
		return false;
	}

	boolean multilineCondNonAdjacent(boolean a, boolean b) {
		if (a
				&& b)
			return true;
		compute();
		return false;
	}

	boolean multilineTextBlockReturnValue(boolean flag) {
		if (flag)
			return """
					text
					""".isEmpty();
		return false;
	}

	boolean multilineValueExprExempt(boolean flag, boolean a, boolean b) {
		if (flag) {
			return a
					&& b;
		}
		return false;
	}

	boolean multiStmtElseBody(boolean flag) {
		if (flag)
			return true;
		else {
			compute();
			return false;
		}
	}

	boolean multiStmtIfBody(boolean flag) {
		if (flag) {
			compute();
			return true;
		}
		return false;
	}

	boolean nextLineNonLiteralReturnNoSibling(boolean flag) {
		if (flag)
			return compute();
		throw new IllegalStateException();
	}

	boolean noCollapsibleTrailingReturn(boolean flag) {
		if (flag)
			return true;
		throw new IllegalStateException();
	}

	boolean nonAdjacent(boolean flag) {
		if (flag)
			return true;
		compute();
		return false;
	}

	boolean oppositeLongConditionOverflowsLine(
			boolean conditionFlagWithAnExtremelyLongDescriptiveParamName,
			boolean valueFlagWithAnEquallyLongDescriptiveParameterName
	) {
		if (conditionFlagWithAnExtremelyLongDescriptiveParamName && valueFlagWithAnEquallyLongDescriptiveParameterName)
			return true;
		return false;
	}

	boolean patternVariableUsedAfterIf(Object o) {
		if (!(o instanceof String s))
			return false;
		final var length = s.length();
		return length > 0;
	}

	boolean sameLiteralBothOperandsSideEffect(String a, String b) {
		if (a.isEmpty() == b.isEmpty())
			return true;
		return true;
	}

	boolean sameLiteralOrShortCircuit(boolean a) {
		if (a || compute())
			return true;
		return true;
	}

	boolean sameLiteralParenOrRightOfAnd(boolean flag, String s) {
		if (s.isEmpty() && (s.isBlank() || flag))
			return true;
		return true;
	}

	boolean sameLiteralTernaryCondition(boolean flag, String a, String b) {
		if (flag ? a.isEmpty() : b.isEmpty())
			return true;
		return true;
	}

	boolean yieldForm(int x, boolean flag) {
		return switch (x) {
			case 1 -> {
				if (flag)
					yield true;
				yield false;
			}
			default -> false;
		};
	}
}