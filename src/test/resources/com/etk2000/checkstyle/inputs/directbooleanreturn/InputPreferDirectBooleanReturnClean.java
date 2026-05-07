package com.etk2000.checkstyle.inputs.directbooleanreturn;

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

	boolean elseNonLiteral(boolean flag) {
		if (flag) {
			return true;
		}
		else {
			return compute();
		}
	}

	boolean emptyBracedIfBody(boolean flag) {
		if (flag) {}
		return false;
	}

	boolean ifBodyNonLiteralReturn(boolean flag) {
		if (flag)
			return compute();
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

	boolean nonAdjacent(boolean flag) {
		if (flag)
			return true;
		compute();
		return false;
	}

	boolean sameLiteralBoth(boolean flag) {
		if (flag)
			return true;
		return true;
	}

	boolean sameLiteralBothElseForm(boolean flag) {
		if (flag)
			return true;
		else
			return true;
	}

	boolean trailingNonLiteralReturn(boolean flag) {
		if (flag)
			return true;
		return compute();
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