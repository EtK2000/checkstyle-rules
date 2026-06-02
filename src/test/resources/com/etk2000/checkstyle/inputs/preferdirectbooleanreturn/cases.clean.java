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