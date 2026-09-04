package com.etk2000.checkstyle.inputs.lambdaparametertype;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * Positions for {@code LambdaParameterTypeLocatorTest}, which addresses them directly rather than
 * through a reported violation. Nothing here is a slice: the file carries no markers and is never
 * fix-run.
 */
public class InputLocatorPositions {
	BinaryOperator<String> continuationLine() {
		return (String alpha,
				String beta) -> alpha + beta;
	}

	IntFunction<IntUnaryOperator> nestedOnOneLine() {
		return (int outer) -> (int inner) -> outer + inner;
	}

	/**
	 * Two lambdas whose parameter lists start at the same column on consecutive lines, so a locator
	 * off by one line answers with the wrong one rather than with nothing.
	 */
	IntUnaryOperator[] sameColumnPair() {
		return new IntUnaryOperator[]{
				(int alpha) -> alpha,
				(int beta) -> beta
		};
	}

	/** The violation lands on the {@code LAMBDA} itself here, not on a {@code PARAMETER_DEF}. */
	IntUnaryOperator unnecessaryParens() {
		return (x) -> x;
	}
}