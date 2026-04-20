package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.List;
import java.util.Map;

@interface CleanMulti {
	int a() default 0;
	int b() default 0;
}

class InputFieldConsolidationCleanAlreadyCombined {
	int height, width;
}

class InputFieldConsolidationCleanDifferentTypes {
	int count;
	String name;
}

class InputFieldConsolidationCleanPrevInitializer {
	int active = 1;
	int passive;
}

class InputFieldConsolidationCleanCurrInitializer {
	int alpha;
	int beta = 1;
}

class InputFieldConsolidationCleanBothInitializers {
	int first = 1;
	int second = 2;
}

class InputFieldConsolidationCleanVisibility {
	private int priv;
	int pub;
}

class InputFieldConsolidationCleanStatic {
	static int shared;

	int local;
}

class InputFieldConsolidationCleanFinal {
	final int fixed;
	int flex;

	InputFieldConsolidationCleanFinal(int fixed) {
		this.fixed = fixed;
	}
}

class InputFieldConsolidationCleanDifferentAnnotations {
	@Deprecated
	int newer;
	@SuppressWarnings("unused")
	int old;
}

class InputFieldConsolidationCleanDifferentParams {
	@SuppressWarnings("a")
	int first;
	@SuppressWarnings("b")
	int second;
}

class InputFieldConsolidationCleanDifferentNamedParams {
	@CleanMulti(a = 1, b = 2)
	int first;
	@CleanMulti(a = 1, b = 3)
	int second;
}

class InputFieldConsolidationCleanSwappedArrayParam {
	@SuppressWarnings({"all", "unused"})
	int first;
	@SuppressWarnings({"unused", "all"})
	int second;
}

class InputFieldConsolidationCleanAnnotationMismatch {
	@Deprecated
	int annotated;
	int plain;
}

class InputFieldConsolidationCleanCommentBetween {
	int alpha;
	// separator comment
	int beta;
}

class InputFieldConsolidationCleanBlankLine {
	int alpha;

	int beta;
}

class InputFieldConsolidationCleanJavadocCurr {
	int x;
	/** The Y coordinate */
	int y;
}

class InputFieldConsolidationCleanArrayMismatch {
	int scalar;
	int[] vector;
}

class InputFieldConsolidationCleanGenericMismatch {
	List<Integer> numbers;
	List<String> words;
}

class InputFieldConsolidationCleanSingle {
	int only;
}

class InputFieldConsolidationCleanArrayDimension {
	int[] flat;
	int[][] matrix;
}

class InputFieldConsolidationCleanCompoundArrayMismatch {
	String[] flat;
	String[] compound[];
}

enum InputFieldConsolidationCleanEnumSeparated {
	A,
	B;

	int beta;

	void doSomething() {}
}

interface InputFieldConsolidationCleanInterface {
	int ALPHA = 1;
	int BETA = 2;
}

class InputFieldConsolidationCleanFqnVsSimple {
	java.util.List<String> alpha;
	List<String> beta;
}

class InputFieldConsolidationCleanDifferentWildcards {
	List<? extends Number> bounded;
	List<? super Number> lower;
}

class InputFieldConsolidationCleanWildcardVsConcrete {
	List<Number> concrete;
	List<? extends Number> wildcard;
}

class InputFieldConsolidationCleanNestedGenericMismatch {
	Map<String, List<Integer>> intMap;
	Map<String, List<String>> strMap;
}

class InputFieldConsolidationCleanFqnAnnotation {
	@java.lang.Deprecated
	int alpha;
	@Deprecated
	int beta;
}

class InputFieldConsolidationCleanUnboundedVsBounded {
	List<? extends Number> bounded;
	List<?> unbounded;
}

class InputFieldConsolidationCleanMethodBetween {
	int alpha;

	void doSomething() {}

	int beta;
}

class InputFieldConsolidationCleanConstructorBetween {
	int alpha;

	InputFieldConsolidationCleanConstructorBetween() {}

	int beta;
}

class InputFieldConsolidationCleanStaticInitBetween {
	static int alpha;

	static {
		System.out.println();
	}

	static int beta;
}

class InputFieldConsolidationCleanInstanceInitBetween {
	int alpha;

	{
		System.out.println();
	}

	int beta;
}

class InputFieldConsolidationCleanInnerTypeBetween {
	int alpha;

	static class Inner {}

	int beta;
}

@interface TypeAnn {}

class InputFieldConsolidationCleanTypeUseAnnotation {
	List<@TypeAnn String> annotated;
	List<String> plain;
}

@interface CleanInner {}

@interface CleanOtherInner {}

@interface CleanContainer {
	CleanInner value();
}

@interface CleanContainerOther {
	CleanOtherInner value();
}

class InputFieldConsolidationCleanNestedAnnotationDiff {
	@CleanContainer(@CleanInner)
	int alpha;
	@CleanContainerOther(@CleanOtherInner)
	int beta;
}

class InputFieldConsolidationCleanAnnotatedBoundMismatch {
	List<? extends @TypeAnn Number> annotated;
	List<? extends Number> plain;
}

class InputFieldConsolidationCleanGenericArrayVsPlain {
	List<String[]> array;
	List<String> plain;
}

class InputFieldConsolidationCleanAnonymousClass {
	Runnable r = new Runnable() {
		int alpha = 1;
		int beta = 2;

		@Override
		public void run() {}
	};
}