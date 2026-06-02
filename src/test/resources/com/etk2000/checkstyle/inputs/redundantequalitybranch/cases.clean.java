package com.etk2000.checkstyle.inputs.redundantequalitybranch;

class InputRedundantEqualityBranchClean {
	int assignElseThrows(int a, int b) {
		final int r;
		if (a == b)
			r = a;
		else
			throw new IllegalStateException();
		return r;
	}

	int assignNoElse(int a, int b) {
		var r = b;
		if (a == b)
			r = a;
		return r;
	}

	int branchValueIsConstant(int a, int b) {
		final int r;
		if (a == b)
			r = 0;
		else
			r = b;
		return r;
	}

	int branchValueNotOperand(int a, int b, int c) {
		final int r;
		if (a == b)
			r = c;
		else
			r = b;
		return r;
	}

	int differentTargets(int a, int b) {
		var r1 = 0;
		var r2 = 0;
		if (a == b)
			r1 = a;
		else
			r2 = b;
		return r1 + r2;
	}

	int elseIfChain(int a, int b, int c) {
		if (a == b)
			return a;
		else if (a > 0)
			return b;
		else
			return c;
	}

	int impureOperand() {
		final int r;
		if (next() == next())
			r = next();
		else
			r = 0;
		return r;
	}

	int impureRhs(int a, int b) {
		final int r;
		if (a == b)
			r = next();
		else
			r = b;
		return r;
	}

	int multiStatementBlock(int a, int b) {
		final int r;
		if (a == b) {
			System.out.println(a);
			r = a;
		}
		else
			r = b;
		return r;
	}

	void nestedThenIf(int a, int b, int[] out) {
		if (a == b) {
			if (a > 0)
				out[0] = a;
		}
		else
			out[0] = b;
	}

	int next() {
		return 42;
	}

	int nonEqualityCondition(int a, int b) {
		final int r;
		if (a > b)
			r = a;
		else
			r = b;
		return r;
	}

	int returnElseThrows(int a, int b) {
		if (a == b)
			return a;
		else
			throw new IllegalStateException();
	}

	int returnLiteralValues(int a, int b) {
		if (a == b)
			return 42;
		else
			return 0;
	}

	int returnNoTrailingStatement(int a, int b) {
		if (a == b)
			return a;
	}

	int returnTrailingLiteralValues(int a, int b) {
		if (a != b)
			return 42;
		return 0;
	}

	int returnTrailingNotReturn(int a, int b) {
		if (a == b)
			return a;
		System.out.println(a);
		return b;
	}

	int thenThrows(int a, int b) {
		if (a == b)
			throw new IllegalStateException();
		return b;
	}

	int trailingReturnNotMatching(int a, int b, int c) {
		if (a == b)
			return a;
		return c;
	}
}