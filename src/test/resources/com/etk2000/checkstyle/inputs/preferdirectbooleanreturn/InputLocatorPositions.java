package com.etk2000.checkstyle.inputs.preferdirectbooleanreturn;

import java.util.List;

/**
 * Positions for {@code PreferDirectBooleanReturnLocatorTest}, which addresses them directly rather
 * than through a reported violation. One collapsible pair per method so a test names a method
 * instead of a line, and nothing here is a slice: the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	boolean combineAnd(boolean flag, String s) {
		if (flag)
			return s.isEmpty();
		return false;
	}

	boolean elseIfInner(boolean a, boolean b) {
		if (a)
			return false;
		else if (b)
			return true;
		else
			return false;
	}

	boolean lengthRefused(
			boolean conditionFlagWithAnExtremelyLongDescriptiveParamName,
			boolean valueFlagWithAnEquallyLongDescriptiveParameterName
	) {
		if (conditionFlagWithAnExtremelyLongDescriptiveParamName && valueFlagWithAnEquallyLongDescriptiveParameterName)
			return true;
		return false;
	}

	boolean oppositeForward(boolean flag) {
		if (flag)
			return true;
		return false;
	}

	boolean redundantDrop(boolean[] flags) {
		if (flags[0])
			return true;
		return true;
	}

	boolean redundantHoist(List<String> list) {
		if (list.remove("x"))
			return true;
		return true;
	}

	boolean supplementaryBeforeIf(boolean flag) {
		/* 𝕏 */ if (flag)
			return true;
		return false;
	}

	boolean tabIndentedPair(boolean flag, boolean other) {
		if (other) {
			if (flag)
				return true;
			return false;
		}
		return other;
	}
}