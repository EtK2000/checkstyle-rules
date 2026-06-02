package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.List;
import java.util.Map;

enum InputFieldSortingCleanEnum {
	ALPHA,
	BETA,
	GAMMA;

	static final int MAX = 10;
	static final int MIN = 1;
}

enum InputFieldSortingCleanEnumOuter {
	ALPHA,
	BETA;

	enum Inner {
		FIRST,
		SECOND
	}
}

enum InputFieldSortingCleanEnumSingle {
	ONLY_ONE
}

enum InputFieldSortingCleanEnumSortedShapes {
	/* c */ ALPHA,
	ZETA
}

enum InputFieldSortingCleanEnumSortedJavadoc {
	/**
	 * alpha.
	 */
	ALPHA,
	/**
	 * zeta.
	 */
	ZETA
}

enum InputFieldSortingCleanEnumSortedAnnotationForms {
	@Deprecated
	ALPHA,
	@SuppressWarnings("/* not a comment */")
	BETA,
	@SuppressWarnings({"a", "b"})
	GAMMA,
	@SuppressWarnings(value = "unused")
	ZETA
}

enum InputFieldSortingCleanEnumSortedNestedTypeAfterTerminator {
	ALPHA,
	ZETA;

	interface Marker {
	}
}

enum InputFieldSortingCleanEnumSortedConstantBodies {
	ALPHA {
		// a note
	},
	ZETA {
		// z note
	}
}

enum InputFieldSortingCleanEnumWithBodies {
	ADD {
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	},
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	};

	abstract int apply(int a, int b);
}

enum InputFieldSortingCleanEnumWithMembers {
	APPLE("red"),
	BANANA("yellow"),
	CHERRY("red");

	final String color;

	InputFieldSortingCleanEnumWithMembers(String color) {
		this.color = color;
	}

	String getColor() {
		return color;
	}
}

enum InputFieldSortingCleanEnumWithSeparators {
	ALPHA,

	// Beta is the second letter
	BETA,

	@Deprecated
	GAMMA
}

enum InputFieldSortingCleanEnumWithTrailingComments {
	ALPHA, // first
	BETA; /* last */

	static final int MAX = 10;
}

enum InputFieldSortingCleanEnumWithHeaderComment { // note
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithAnnotationComment {
	@Deprecated
	// note
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithStructuralAnnotationArg {
	@SuppressWarnings("{ } , // /*")
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithTextBlockArg {
	ALPHA("""
			x
			"""),
	BETA("""
			y
			""");

	InputFieldSortingCleanEnumWithTextBlockArg(String value) {
	}
}

class InputFieldSortingClean {
	enum InnerSorted {
		FIRST,
		SECOND,
		THIRD
	}

	static final Runnable STATIC_TASK = new Runnable() {
		@Override
		public void run() {
			System.out.println(GAMMA);
		}
	};
	static final int ALPHA = 1;
	static final int BETA = 2;
	static final long NOW = System.currentTimeMillis();
	static final long FUTURE = NOW + 1000;
	static final long PAST = NOW - 1000;
	static final String GAMMA = "g";

	static int delta;

	final Comparable<String> comparator = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return name.compareTo(o);
		}
	};
	final Runnable task = new Runnable() {
		@Override
		public void run() {
			System.out.println(name);
		}
	};
	final int count = 0;
	final String name = "default";

	final List<String> items;
	final Map<String, Integer> lookup;

	boolean active;
	byte flags;
	char letter;
	double ratio;
	double[] ratios;
	float scale;
	int index;
	int elements[];
	int[] indices;
	int[][] matrix;
	long timestamp;
	short code;
	java.util.concurrent.atomic.AtomicInteger counter;
	List<String> data;
	String label;
	String[] labels;

	InputFieldSortingClean(List<String> items, Map<String, Integer> lookup) {
		this.items = items;
		this.lookup = lookup;
	}
}

@SuppressWarnings("FieldSorting")
enum InputFieldSortingCleanSuppressedEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings({"FieldSorting"})
enum InputFieldSortingCleanSuppressedArrayEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings({"FieldSorting", "unused"})
enum InputFieldSortingCleanSuppressedMixedEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings("FieldSorting")
class InputFieldSortingCleanSuppressedClass {
	static String z;
	static int a;
	String name;
	int count;
}

@SuppressWarnings({"unused", "FieldSorting"})
enum InputFieldSortingCleanSuppressedKeySecond {
	ZEBRA,
	ALPHA
}

class InputFieldSortingCleanSuppressedOuter {
	@SuppressWarnings("FieldSorting")
	enum Inner {
		ZEBRA,
		ALPHA
	}
}

@SuppressWarnings(value = "FieldSorting")
enum InputFieldSortingCleanSuppressedExplicitValue {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"FieldSorting"})
enum InputFieldSortingCleanSuppressedExplicitValueArray {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"FieldSorting", "unused"})
enum InputFieldSortingCleanSuppressedExplicitValueMixed {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"unused", "FieldSorting"})
enum InputFieldSortingCleanSuppressedExplicitValueMixedSecond {
	ZEBRA,
	ALPHA
}

@java.lang.SuppressWarnings("FieldSorting")
enum InputFieldSortingCleanSuppressedQualified {
	ZEBRA,
	ALPHA
}

enum InputFieldSortingCleanSuppressedConstantBody {
	@SuppressWarnings("FieldSorting")
	INSTANCE {
		String name;
		int count;
	}
}

@interface Ann {}

@interface Bnn {}

class InputFieldSortingCleanAnnotationOrder {
	@Deprecated
	int alpha;
	@SuppressWarnings("unused")
	int beta;

	@Deprecated
	long first, second;

	String plain;
	@Deprecated
	String annotated;
}

class InputFieldSortingCleanAnnotationMulti {
	@Ann
	@Bnn
	String abField;
	@Bnn
	String bField;
}

class InputFieldSortingCleanAnnotationConsolidated {
	@Deprecated
	String alpha, beta;
	@SuppressWarnings("unused")
	String delta, gamma;
}

class InputFieldSortingCleanAnnotationDifferentTypes {
	@SuppressWarnings("unused")
	int count;
	@Deprecated
	String name;
}

class InputFieldSortingCleanAnnotationQualified {
	String plain;
	@java.lang.Deprecated
	String qualified;
}

class InputFieldSortingCleanAnnotationStatic {
	static String plain;
	@Deprecated
	static String annotated;
}

@interface ParamAnn {
	int value() default 0;
}

class InputFieldSortingCleanAnnotationParams {
	@ParamAnn
	int noParams;
	@ParamAnn(1)
	int withParam;
	@ParamAnn(2)
	int withOtherParam;
}

class InputFieldSortingCleanTypeArgAnnotation {
	List<String> plain;
	List<@Ann String> annotated;
}

class InputFieldSortingCleanTypeArgAnnotationAlphabetical {
	List<@Ann String> aAnnotated;
	List<@Bnn String> bAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationCount {
	List<@Ann String> oneAnn;
	List<@Ann @Bnn String> twoAnns;
}

class InputFieldSortingCleanTypeArgAnnotationMultiArg {
	Map<String, Integer> plain;
	Map<String, @Ann Integer> annotatedSecond;
}

class InputFieldSortingCleanTypeArgAnnotationSameFallsToName {
	List<@Ann String> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationIdenticalMultiArg {
	Map<@Ann String, @Bnn Integer> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationMixedWithFieldAnnotation {
	List<@Ann String> plain;
	@Deprecated
	List<String> fieldAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationOverridesName {
	List<@Ann String> beta;
	List<@Bnn String> alpha;
}

class InputFieldSortingCleanTypeArgAnnotationPositionAware {
	Map<String, @Ann String> firstArgUnannotated;
	Map<@Ann String, String> firstArgAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationArray {
	List<String>[] plain;
	List<@Ann String>[] annotated;
}

class InputFieldSortingCleanTypeArgAnnotationWildcardBound {
	List<? extends @Ann Number> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationWildcardBoundDifferent {
	List<? extends @Ann Number> alpha;
	List<? extends @Bnn Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationLowerBound {
	List<? super @Ann Number> alpha;
	List<? super @Bnn Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationWildcard {
	List<@Ann ? extends Number> alpha;
	List<@Bnn ? extends Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationNested {
	Map<String, List<Integer>> alpha;
	Map<String, List<@Ann Integer>> beta;
}

@SuppressWarnings("PreferImport")
class InputFieldSortingCleanTypeArgAnnotationQualified {
	java.util.Set<String> plain;
	java.util.Set<@Ann String> annotated;
}

interface InputFieldSortingCleanInterface {
	int FIRST = 1;
	int SECOND = 2;
}

record InputFieldSortingCleanRecord(int zebra, int alpha) {
	static int a = 1;
	static int b = 2;
}