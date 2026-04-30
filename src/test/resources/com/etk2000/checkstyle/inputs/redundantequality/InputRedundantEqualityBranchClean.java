package com.etk2000.checkstyle.inputs.redundantequality;

class InputRedundantEqualityBranchClean {
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

	int next() {
		return 42;
	}

	int trailingReturnNotMatching(int a, int b, int c) {
		if (a == b)
			return a;
		return c;
	}
}