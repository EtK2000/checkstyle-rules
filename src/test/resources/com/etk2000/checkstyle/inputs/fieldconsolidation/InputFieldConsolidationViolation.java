package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.List;
import java.util.Map;

@interface Multi {
	int a() default 0;
	int b() default 0;
}

class InputFieldConsolidationViolationPrimitives {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationReferences {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}

class InputFieldConsolidationViolationThree {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha;
	@Deprecated()
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha;
	@SuppressWarnings({"unused", "all"})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(b = 2, a = 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationStatic {
	static int global;
	static int shared; // violation: Fields 'shared' and 'global' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationArray {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}

// Compound array (Type[] name[]) matches Type[][] (same type)
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha;
	String[] beta[]; // violation: Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.
}

class InputFieldConsolidationViolationGeneric {
	List<String> names;
	List<String> words; // violation: Fields 'words' and 'names' (type 'List<String>') should be declared on one line.
}

class InputFieldConsolidationViolationFinal {
	final int alpha;
	final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}

class InputFieldConsolidationViolationProtected {
	protected int alpha;
	protected int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationBoolean {
	boolean active;
	boolean visible; // violation: Fields 'visible' and 'active' (type 'boolean') should be declared on one line.
}

class InputFieldConsolidationViolationLong {
	long elapsed;
	long remaining; // violation: Fields 'remaining' and 'elapsed' (type 'long') should be declared on one line.
}

class InputFieldConsolidationViolationNestedGeneric {
	Map<String, List<Integer>> alphaMap;
	Map<String, List<Integer>> betaMap; // violation: Fields 'betaMap' and 'alphaMap' (type 'Map<String,List<Integer>>') should be declared on one line.
}

class InputFieldConsolidationViolationWildcard {
	List<? extends Number> alphaList;
	List<? extends Number> betaList; // violation: Fields 'betaList' and 'alphaList' (type 'List<? extends Number>') should be declared on one line.
}

enum InputFieldConsolidationViolationEnum {
	A,
	B;

	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationWildcardSuper {
	List<? super Integer> alphaSuper;
	List<? super Integer> betaSuper; // violation: Fields 'betaSuper' and 'alphaSuper' (type 'List<? super Integer>') should be declared on one line.
}

class InputFieldConsolidationViolationUnboundedWildcard {
	List<?> alphaUnbounded;
	List<?> betaUnbounded; // violation: Fields 'betaUnbounded' and 'alphaUnbounded' (type 'List<?>') should be declared on one line.
}

class InputFieldConsolidationViolationBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}

class InputFieldConsolidationViolationOtherPrimitives {
	byte alphaByte;
	byte betaByte; // violation: Fields 'betaByte' and 'alphaByte' (type 'byte') should be declared on one line.
	char alphaChar;
	char betaChar; // violation: Fields 'betaChar' and 'alphaChar' (type 'char') should be declared on one line.
	double alphaDouble;
	double betaDouble; // violation: Fields 'betaDouble' and 'alphaDouble' (type 'double') should be declared on one line.
	float alphaFloat;
	float betaFloat; // violation: Fields 'betaFloat' and 'alphaFloat' (type 'float') should be declared on one line.
	short alphaShort;
	short betaShort; // violation: Fields 'betaShort' and 'alphaShort' (type 'short') should be declared on one line.
}

class InputFieldConsolidationViolationInnerClass {
	static class Inner {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
	}
}

class InputFieldConsolidationViolationFqnAnnotation {
	@java.lang.Deprecated
	int alpha;
	@java.lang.Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

record InputFieldConsolidationViolationRecord(int x) {
	static String first;
	static String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}

class InputFieldConsolidationViolationFqnType {
	java.util.List<String> alpha;
	java.util.List<String> beta; // violation: Fields 'beta' and 'alpha' (type 'java.util.List<String>') should be declared on one line.
}

class InputFieldConsolidationViolationAnnotationOrder {
	@Deprecated
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

@interface Inner {}

@interface Container {
	Inner value();
}

class InputFieldConsolidationViolationNestedAnnotation {
	@Container(@Inner)
	int alpha;
	@Container(@Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

@interface ArrayParam {
	int[] value();
}

class InputFieldConsolidationViolationNamedArrayParam {
	@ArrayParam({1, 2})
	int alpha;
	@ArrayParam({1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

@interface ViolationTypeAnn {}

class InputFieldConsolidationViolationAnnotatedBound {
	List<? extends @ViolationTypeAnn Number> alpha;
	List<? extends @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? extends @ViolationTypeAnn Number>') should be declared on one line.
}

class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(value = @Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationNamedArrayParamExplicit {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationGenericArrayTypeArg {
	List<String[]> alpha;
	List<String[]> beta; // violation: Fields 'beta' and 'alpha' (type 'List<String[]>') should be declared on one line.
}

class InputFieldConsolidationViolationShorthandVsExplicitNested {
	@Container(@Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationShorthandVsExplicitExpr {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings(value = "unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationAfterMultiVar {
	int a, b;
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationFqnTypeNoGenerics {
	java.lang.Object alpha;
	java.lang.Object beta; // violation: Fields 'beta' and 'alpha' (type 'java.lang.Object') should be declared on one line.
}

class InputFieldConsolidationViolationAnonymousClass {
	Runnable r = new Runnable() {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

		@Override
		public void run() {}
	};
}