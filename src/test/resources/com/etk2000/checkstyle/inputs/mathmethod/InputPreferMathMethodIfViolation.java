package com.etk2000.checkstyle.inputs.mathmethod;

import java.util.function.IntBinaryOperator;

class InputPreferMathMethodIfViolation {
	int x;

	int absDeclArrayTarget(int[] r, int i, int a) {
		if (a < 0) // violation: Use 'Math.abs(a)' here.
			r[i] = -a;
		else
			r[i] = a;
		return r[i];
	}

	int absDeclGe(int a) {
		int r;
		if (a >= 0) // violation: Use 'Math.abs(a)' here.
			r = a;
		else
			r = -a;
		return r;
	}

	int absDeclGeZeroLeft(int a) {
		int r;
		if (0 >= a) // violation: Use 'Math.abs(a)' here.
			r = -a;
		else
			r = a;
		return r;
	}

	int absDeclGt(int a) {
		int r;
		if (a > 0) // violation: Use 'Math.abs(a)' here.
			r = a;
		else
			r = -a;
		return r;
	}

	int absDeclGtZeroLeft(int a) {
		int r;
		if (0 > a) // violation: Use 'Math.abs(a)' here.
			r = -a;
		else
			r = a;
		return r;
	}

	int absDeclLe(int a) {
		int r;
		if (a <= 0) // violation: Use 'Math.abs(a)' here.
			r = -a;
		else
			r = a;
		return r;
	}

	int absDeclLeZeroLeft(int a) {
		int r;
		if (0 <= a) // violation: Use 'Math.abs(a)' here.
			r = a;
		else
			r = -a;
		return r;
	}

	int absDeclLt(int a) {
		int r;
		if (a < 0) // violation: Use 'Math.abs(a)' here.
			r = -a;
		else
			r = a;
		return r;
	}

	int absDeclLtZeroLeft(int a) {
		int r;
		if (0 < a) // violation: Use 'Math.abs(a)' here.
			r = a;
		else
			r = -a;
		return r;
	}

	int absDeclThisTarget(int a) {
		if (a < 0) // violation: Use 'Math.abs(a)' here.
			this.x = -a;
		else
			this.x = a;
		return x;
	}

	int absIfElseReturnGe(int a) {
		if (a >= 0) // violation: Use 'Math.abs(a)' here.
			return a;
		else
			return -a;
	}

	int absIfElseReturnGeZeroLeft(int a) {
		if (0 >= a) // violation: Use 'Math.abs(a)' here.
			return -a;
		else
			return a;
	}

	int absIfElseReturnGt(int a) {
		if (a > 0) // violation: Use 'Math.abs(a)' here.
			return a;
		else
			return -a;
	}

	int absIfElseReturnGtZeroLeft(int a) {
		if (0 > a) // violation: Use 'Math.abs(a)' here.
			return -a;
		else
			return a;
	}

	int absIfElseReturnLe(int a) {
		if (a <= 0) // violation: Use 'Math.abs(a)' here.
			return -a;
		else
			return a;
	}

	int absIfElseReturnLeZeroLeft(int a) {
		if (0 <= a) // violation: Use 'Math.abs(a)' here.
			return a;
		else
			return -a;
	}

	int absIfElseReturnLt(int a) {
		if (a < 0) // violation: Use 'Math.abs(a)' here.
			return -a;
		else
			return a;
	}

	int absIfElseReturnLtZeroLeft(int a) {
		if (0 < a) // violation: Use 'Math.abs(a)' here.
			return a;
		else
			return -a;
	}

	int absTrailingReturnGe(int a) {
		if (a >= 0) // violation: Use 'Math.abs(a)' here.
			return a;
		return -a;
	}

	int absTrailingReturnGeZeroLeft(int a) {
		if (0 >= a) // violation: Use 'Math.abs(a)' here.
			return -a;
		return a;
	}

	int absTrailingReturnGt(int a) {
		if (a > 0) // violation: Use 'Math.abs(a)' here.
			return a;
		return -a;
	}

	int absTrailingReturnGtZeroLeft(int a) {
		if (0 > a) // violation: Use 'Math.abs(a)' here.
			return -a;
		return a;
	}

	int absTrailingReturnLe(int a) {
		if (a <= 0) // violation: Use 'Math.abs(a)' here.
			return -a;
		return a;
	}

	int absTrailingReturnLeZeroLeft(int a) {
		if (0 <= a) // violation: Use 'Math.abs(a)' here.
			return a;
		return -a;
	}

	int absTrailingReturnLt(int a) {
		if (a < 0) // violation: Use 'Math.abs(a)' here.
			return -a;
		return a;
	}

	int absTrailingReturnLtZeroLeft(int a) {
		if (0 < a) // violation: Use 'Math.abs(a)' here.
			return a;
		return -a;
	}

	int comparisonNotEquality(int a, int b) {
		final int r;
		if (a < b) // violation: Use 'Math.min(a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxCompoundAssign(int r, int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r += a;
		else
			r += b;
		return r;
	}

	int maxCompoundAssignBitAnd(int r, int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r &= a;
		else
			r &= b;
		return r;
	}

	int maxCompoundAssignMul(int r, int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r *= a;
		else
			r *= b;
		return r;
	}

	int maxDeclArrayTarget(int[] r, int i, int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r[i] = a;
		else
			r[i] = b;
		return r[i];
	}

	int maxDeclGe(int a, int b) {
		int r;
		if (a >= b) // violation: Use 'Math.max(a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxDeclGt(int a, int b) {
		int r;
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxDeclLe(int a, int b) {
		int r;
		if (a <= b) // violation: Use 'Math.max(a, b)' here.
			r = b;
		else
			r = a;
		return r;
	}

	int maxDeclLt(int a, int b) {
		int r;
		if (a < b) // violation: Use 'Math.max(a, b)' here.
			r = b;
		else
			r = a;
		return r;
	}

	int maxDeclPreDecrement(int a, int b) {
		int r;
		if (--a > b) // violation: Use 'Math.max(--a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxDeclPreIncrement(int a, int b) {
		int r;
		if (++a > b) // violation: Use 'Math.max(++a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxDeclPreIncrementRightOperand(int a, int b) {
		int r;
		if (a > ++b) // violation: Use 'Math.max(a, ++b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int maxFieldTarget(InputPreferMathMethodIfViolation t, int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			t.x = a;
		else
			t.x = b;
		return t.x;
	}

	int maxIfElseReturnGe(int a, int b) {
		if (a >= b) // violation: Use 'Math.max(a, b)' here.
			return a;
		else
			return b;
	}

	int maxIfElseReturnGt(int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			return a;
		else
			return b;
	}

	int maxIfElseReturnLe(int a, int b) {
		if (a <= b) // violation: Use 'Math.max(a, b)' here.
			return b;
		else
			return a;
	}

	int maxIfElseReturnLt(int a, int b) {
		if (a < b) // violation: Use 'Math.max(a, b)' here.
			return b;
		else
			return a;
	}

	int maxIfReturnPreIncrement(int a, int b) {
		if (++a > b) // violation: Use 'Math.max(++a, b)' here.
			return a;
		else
			return b;
	}

	int maxInitOverwrite(int a, int b) {
		var r = b;
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			r = a;
		return r;
	}

	int maxInTryBlock(int a, int b) {
		try {
			if (a > b) // violation: Use 'Math.max(a, b)' here.
				return a;
			return b;
		}
		finally {
			System.out.println();
		}
	}

	IntBinaryOperator maxLambdaIfReturn() {
		return (a, b) -> {
			if (a > b) // violation: Use 'Math.max(a, b)' here.
				return a;
			return b;
		};
	}

	int maxThisTarget(int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			this.x = a;
		else
			this.x = b;
		return x;
	}

	int maxTrailingReturnGe(int a, int b) {
		if (a >= b) // violation: Use 'Math.max(a, b)' here.
			return a;
		return b;
	}

	int maxTrailingReturnGt(int a, int b) {
		if (a > b) // violation: Use 'Math.max(a, b)' here.
			return a;
		return b;
	}

	int maxTrailingReturnLe(int a, int b) {
		if (a <= b) // violation: Use 'Math.max(a, b)' here.
			return b;
		return a;
	}

	int maxTrailingReturnLt(int a, int b) {
		if (a < b) // violation: Use 'Math.max(a, b)' here.
			return b;
		return a;
	}

	int minDeclGe(int a, int b) {
		int r;
		if (a >= b) // violation: Use 'Math.min(a, b)' here.
			r = b;
		else
			r = a;
		return r;
	}

	int minDeclGt(int a, int b) {
		int r;
		if (a > b) // violation: Use 'Math.min(a, b)' here.
			r = b;
		else
			r = a;
		return r;
	}

	int minDeclLe(int a, int b) {
		int r;
		if (a <= b) // violation: Use 'Math.min(a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int minDeclLt(int a, int b) {
		int r;
		if (a < b) // violation: Use 'Math.min(a, b)' here.
			r = a;
		else
			r = b;
		return r;
	}

	int minIfElseReturnGe(int a, int b) {
		if (a >= b) // violation: Use 'Math.min(a, b)' here.
			return b;
		else
			return a;
	}

	int minIfElseReturnGt(int a, int b) {
		if (a > b) // violation: Use 'Math.min(a, b)' here.
			return b;
		else
			return a;
	}

	int minIfElseReturnLe(int a, int b) {
		if (a <= b) // violation: Use 'Math.min(a, b)' here.
			return a;
		else
			return b;
	}

	int minIfElseReturnLt(int a, int b) {
		if (a < b) // violation: Use 'Math.min(a, b)' here.
			return a;
		else
			return b;
	}

	int minThisTarget(int a, int b) {
		if (a < b) // violation: Use 'Math.min(a, b)' here.
			this.x = a;
		else
			this.x = b;
		return x;
	}

	int minTrailingReturnGe(int a, int b) {
		if (a >= b) // violation: Use 'Math.min(a, b)' here.
			return b;
		return a;
	}

	int minTrailingReturnGt(int a, int b) {
		if (a > b) // violation: Use 'Math.min(a, b)' here.
			return b;
		return a;
	}

	int minTrailingReturnLe(int a, int b) {
		if (a <= b) // violation: Use 'Math.min(a, b)' here.
			return a;
		return b;
	}

	int minTrailingReturnLt(int a, int b) {
		if (a < b) // violation: Use 'Math.min(a, b)' here.
			return a;
		return b;
	}
}