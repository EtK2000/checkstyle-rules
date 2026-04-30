package com.etk2000.checkstyle.inputs.mathmethod;

import java.util.List;

class InputPreferMathMethodClean {
	int x;

	int alreadyUsingMathAbs(int a) {
		return Math.abs(a);
	}

	int alreadyUsingMathMax(int a, int b) {
		return Math.max(a, b);
	}

	int alreadyUsingMathMin(int a, int b) {
		return Math.min(a, b);
	}

	int differentOperandInFalseBranch(int a, int b, int c) {
		return a > b ? a : c;
	}

	int differentOperandInTrueBranch(int a, int b, int c) {
		return a > b ? c : b;
	}

	int equalityComparison(int a, int b) {
		return a == b ? a : b;
	}

	void ifBareReturnNoValue(int a, int b) {
		if (a > b)
			return;
		System.out.println(b);
	}

	int ifBooleanCondition(boolean flag, int a, int b) {
		final int r;
		if (flag)
			r = a;
		else
			r = b;
		return r;
	}

	int ifBranchIsLoop(int a, int b) {
		var r = 0;
		if (a > b) {
			for (var i = 0; i < a; ++i)
				r += i;
		}
		else
			r = b;
		return r;
	}

	void ifBranchIsMethodCall(int a, int b) {
		if (a > b)
			System.out.println(a);
		else
			System.out.println(b);
	}

	int ifBranchIsThrow(int a, int b) {
		if (a > b)
			throw new RuntimeException();
		else
			throw new IllegalStateException();
	}

	int ifCompoundCondition(int a, int b, int c) {
		final int r;
		if (a > b && c > 0)
			r = a;
		else
			r = b;
		return r;
	}

	int ifDifferentArrayIndex(int[] r, int i, int j, int a, int b) {
		if (a > b)
			r[i] = a;
		else
			r[j] = b;
		return r[i] + r[j];
	}

	int ifDifferentBranchOperand(int a, int b, int c) {
		int r;
		if (a > b)
			r = a;
		else
			r = c;
		return r;
	}

	int ifDifferentTargets(int a, int b) {
		var r1 = 0;
		var r2 = 0;
		if (a > b)
			r1 = a;
		else
			r2 = b;
		return r1 + r2;
	}

	void ifElseBareReturn(int a, int b) {
		if (a > b)
			return;
		else
			return;
	}

	int ifElseIfChain(int a, int b, int c) {
		final int r;
		if (a > b)
			r = a;
		else if (a > c)
			r = c;
		else
			r = b;
		return r;
	}

	int ifElseIfChainOuterWouldMatch(int a, int b, int c) {
		final int r;
		if (a > b)
			r = a;
		else if (c > 0)
			r = c;
		else
			r = b;
		return r;
	}

	int ifImpureCondition(int b) {
		int r;
		if (next() > b)
			r = next();
		else
			r = b;
		return r;
	}

	int ifImpureRhs(int a, int b) {
		int r;
		if (a > b)
			r = next();
		else
			r = b;
		return r;
	}

	int ifMultiStatementBlock(int a, int b) {
		final int r;
		if (a > b) {
			System.out.println("max");
			r = a;
		}
		else
			r = b;
		return r;
	}

	int ifMultiStatementBlockInElse(int a, int b) {
		final int r;
		if (a > b)
			r = a;
		else {
			System.out.println("min");
			r = b;
		}
		return r;
	}

	int ifNonZeroComparisonForAbs(int a) {
		final int r;
		if (a < 1)
			r = -a;
		else
			r = a;
		return r;
	}

	int ifNotEqualReturnFixedValue(int a, int b) {
		if (a != b)
			return 42;
		return 0;
	}

	int ifPostDecrementCondition(int a, int b) {
		final int r;
		if (a-- > b)
			r = a;
		else
			r = b;
		return r;
	}

	int ifPostIncrementCondition(int a, int b) {
		int r;
		if (a++ > b)
			r = a;
		else
			r = b;
		return r;
	}

	void ifReturnAsLastStatement(int a, int b) {
		if (a > b)
			return;
	}

	int ifReturnPostIncrement(int a, int b) {
		if (a++ > b)
			return a;
		else
			return b;
	}

	int ifReturnThenNonReturn(int a, int b) {
		if (a > b)
			return a;
		System.out.println(b);
		return b;
	}

	int ifReturnTrailingThrow(int a, int b) {
		if (a > b)
			return a;
		throw new RuntimeException();
	}

	int ifSameLeafDifferentStructure(InputPreferMathMethodClean t, int b, int tx) {
		if (b > 0)
			t.x = b;
		else
			tx = b;
		return tx + t.x;
	}

	int ifWrongBranchOrderForAbs(int a) {
		final int r;
		if (a < 0)
			r = a;
		else
			r = -a;
		return r;
	}

	int ifWrongBranchOrderForAbsReversed(int a) {
		if (a >= 0)
			return -a;
		else
			return a;
	}

	int methodCallInFalseBranch(int a, int b) {
		return a > b ? a : Math.max(a, b);
	}

	int methodCallInRightOperand(int a, List<String> list) {
		return a > list.size() ? a : list.size();
	}

	int methodCallInTrueBranch(int a, int b) {
		return a > b ? Math.max(a, b) : b;
	}

	int methodCallOperands(List<String> list, int b) {
		return list.size() > b ? list.size() : b;
	}

	int nestedSameMethodNotClamp(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	int next() {
		return 42;
	}

	int nonComparisonCondition(boolean flag, int a, int b) {
		return flag ? a : b;
	}

	int nonZeroComparisonForAbs(int a) {
		return a < 1 ? -a : a;
	}

	int notEqualComparison(int a, int b) {
		return a != b ? a : b;
	}

	int postDecrementInCondition(int a, int b) {
		return a-- > b ? a : b;
	}

	int postIncrementInCondition(int a, int b) {
		return a++ > b ? a : b;
	}

	int trailingReturnPostIncrement(int a, int b) {
		if (a++ > b)
			return a;
		return b;
	}

	int wrongBranchOrderForAbs(int a) {
		return a < 0 ? a : -a;
	}

	int wrongBranchOrderForAbsReversed(int a) {
		return a >= 0 ? -a : a;
	}
}