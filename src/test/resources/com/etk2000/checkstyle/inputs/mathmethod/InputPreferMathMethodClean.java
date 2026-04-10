package com.etk2000.checkstyle.inputs.mathmethod;

import java.util.List;

class InputPreferMathMethodClean {
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

	int wrongBranchOrderForAbs(int a) {
		return a < 0 ? a : -a;
	}

	int wrongBranchOrderForAbsReversed(int a) {
		return a >= 0 ? -a : a;
	}
}