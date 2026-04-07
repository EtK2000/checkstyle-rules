package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.List;

// Already combined on one line
class InputFieldConsolidationCleanAlreadyCombined {
	int height, width;
}

// Different types cannot be combined
class InputFieldConsolidationCleanDifferentTypes {
	int count;
	String name;
}

// Prev has initializer prevents combining
class InputFieldConsolidationCleanPrevInitializer {
	int active = 1;
	int passive;
}

// Curr has initializer prevents combining
class InputFieldConsolidationCleanCurrInitializer {
	int alpha;
	int beta = 1;
}

// Both have initializers
class InputFieldConsolidationCleanBothInitializers {
	int first = 1;
	int second = 2;
}

// Different visibility prevents combining
class InputFieldConsolidationCleanVisibility {
	private int priv;
	int pub;
}

// Static vs instance prevents combining
class InputFieldConsolidationCleanStatic {
	static int shared;

	int local;
}

// Final vs non-final prevents combining
class InputFieldConsolidationCleanFinal {
	final int fixed;
	int flex;

	InputFieldConsolidationCleanFinal(int fixed) {
		this.fixed = fixed;
	}
}

// Different annotation names prevent combining
class InputFieldConsolidationCleanDifferentAnnotations {
	@Deprecated
	int newer;
	@SuppressWarnings("unused")
	int old;
}

// Same annotation name, different params prevent combining
class InputFieldConsolidationCleanDifferentParams {
	@SuppressWarnings("a")
	int first;
	@SuppressWarnings("b")
	int second;
}

// Same annotation name, different named param values prevent combining
class InputFieldConsolidationCleanDifferentNamedParams {
	@Multi(a = 1, b = 2)
	int first;
	@Multi(a = 1, b = 3)
	int second;
}

// Same annotation with array value in swapped order (different expression, not combinable)
class InputFieldConsolidationCleanSwappedArrayParam {
	@SuppressWarnings({"all", "unused"})
	int first;
	@SuppressWarnings({"unused", "all"})
	int second;
}

// One has annotation, other doesn't
class InputFieldConsolidationCleanAnnotationMismatch {
	@Deprecated
	int annotated;
	int plain;
}

// Comment between same-type fields (creates line gap)
class InputFieldConsolidationCleanCommentBetween {
	int alpha;
	// separator comment
	int beta;
}

// Blank line between same-type fields (creates line gap)
class InputFieldConsolidationCleanBlankLine {
	int alpha;

	int beta;
}

// Javadoc on curr field (creates line gap)
class InputFieldConsolidationCleanJavadocCurr {
	int x;
	/** The Y coordinate */
	int y;
}

// Array vs non-array of same base type are different types
class InputFieldConsolidationCleanArrayMismatch {
	int scalar;
	int[] vector;
}

// Different generic types prevent combining
class InputFieldConsolidationCleanGenericMismatch {
	List<Integer> numbers;
	List<String> words;
}

// Single field, nothing to combine
class InputFieldConsolidationCleanSingle {
	int only;
}

// Multidimensional array vs single array are different types
class InputFieldConsolidationCleanArrayDimension {
	int[] flat;
	int[][] matrix;
}

// Compound array (Type[] name[]) vs single array (different dimension)
class InputFieldConsolidationCleanCompoundArrayMismatch {
	String[] flat;
	String[] compound[];
}