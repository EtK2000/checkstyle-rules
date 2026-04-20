package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.List;
import java.util.Map;

@interface Multi {
	int a() default 0;
	int b() default 0;
}

// Two same-type primitive fields
class InputFieldConsolidationViolationPrimitives {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Two same-type reference fields
class InputFieldConsolidationViolationReferences {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}

// Three consecutive same-type fields
class InputFieldConsolidationViolationThree {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}

// @Deprecated and @Deprecated() are both parameterless, treated as equal
class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha;
	@Deprecated()
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same parameterless annotation on both
class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with identical params
class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with a complex param (array value)
class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha;
	@SuppressWarnings({"unused", "all"})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Same annotation with named params in different order
class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(b = 2, a = 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Static same-type fields
class InputFieldConsolidationViolationStatic {
	static int global;
	static int shared; // violation: Fields 'shared' and 'global' (type 'int') should be declared on one line.
}

// Array type match (Java-style and C-style normalize to same type)
class InputFieldConsolidationViolationArray {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}

// Compound array (Type[] name[]) matches Type[][] (same type)
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha;
	String[] beta[]; // violation: Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.
}

// Generic type match
class InputFieldConsolidationViolationGeneric {
	List<String> names;
	List<String> words; // violation: Fields 'words' and 'names' (type 'List<String>') should be declared on one line.
}

// Final without inline value
class InputFieldConsolidationViolationFinal {
	final int alpha;
	final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}

// Protected fields
class InputFieldConsolidationViolationProtected {
	protected int alpha;
	protected int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Boolean primitive type
class InputFieldConsolidationViolationBoolean {
	boolean active;
	boolean visible; // violation: Fields 'visible' and 'active' (type 'boolean') should be declared on one line.
}

// Long primitive type
class InputFieldConsolidationViolationLong {
	long elapsed;
	long remaining; // violation: Fields 'remaining' and 'elapsed' (type 'long') should be declared on one line.
}

// Nested generic type match
class InputFieldConsolidationViolationNestedGeneric {
	Map<String, List<Integer>> alphaMap;
	Map<String, List<Integer>> betaMap; // violation: Fields 'betaMap' and 'alphaMap' (type 'Map<String,List<Integer>>') should be declared on one line.
}

// Wildcard generic type match
class InputFieldConsolidationViolationWildcard {
	List<? extends Number> alphaList;
	List<? extends Number> betaList; // violation: Fields 'betaList' and 'alphaList' (type 'List<? extends Number>') should be declared on one line.
}

// Fields in enum body
enum InputFieldConsolidationViolationEnum {
	A,
	B;

	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Wildcard super type match
class InputFieldConsolidationViolationWildcardSuper {
	List<? super Integer> alphaSuper;
	List<? super Integer> betaSuper; // violation: Fields 'betaSuper' and 'alphaSuper' (type 'List<? super Integer>') should be declared on one line.
}

// Unbounded wildcard type match
class InputFieldConsolidationViolationUnboundedWildcard {
	List<?> alphaUnbounded;
	List<?> betaUnbounded; // violation: Fields 'betaUnbounded' and 'alphaUnbounded' (type 'List<?>') should be declared on one line.
}

// Both C-style arrays
class InputFieldConsolidationViolationBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}

// Remaining primitive types
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

// Fields inside inner class
class InputFieldConsolidationViolationInnerClass {
	static class Inner {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
	}
}

// Same FQN annotation on both
class InputFieldConsolidationViolationFqnAnnotation {
	@java.lang.Deprecated
	int alpha;
	@java.lang.Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Fields inside a record
record InputFieldConsolidationViolationRecord(int x) {
	static String first;
	static String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}

// FQN type match (both fields use fully qualified name)
class InputFieldConsolidationViolationFqnType {
	java.util.List<String> alpha;
	java.util.List<String> beta; // violation: Fields 'beta' and 'alpha' (type 'java.util.List<String>') should be declared on one line.
}

// Annotations in different order (sorted canonically, so they match)
class InputFieldConsolidationViolationAnnotationOrder {
	@Deprecated
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Nested annotation shorthand (@Container(@Inner))
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

// Named param with array value
@interface ArrayParam {
	int[] value();
}

class InputFieldConsolidationViolationNamedArrayParam {
	@ArrayParam({1, 2})
	int alpha;
	@ArrayParam({1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Type-use annotation on wildcard bound
@interface ViolationTypeAnn {}

class InputFieldConsolidationViolationAnnotatedBound {
	List<? extends @ViolationTypeAnn Number> alpha;
	List<? extends @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? extends @ViolationTypeAnn Number>') should be declared on one line.
}

// Named param with nested annotation (explicit 'value =')
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(value = @Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Named param with array value (explicit 'value =')
class InputFieldConsolidationViolationNamedArrayParamExplicit {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Array type inside generic type argument
class InputFieldConsolidationViolationGenericArrayTypeArg {
	List<String[]> alpha;
	List<String[]> beta; // violation: Fields 'beta' and 'alpha' (type 'List<String[]>') should be declared on one line.
}

// Shorthand vs explicit 'value =' for nested annotation
class InputFieldConsolidationViolationShorthandVsExplicitNested {
	@Container(@Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// Shorthand vs explicit 'value =' for single value
class InputFieldConsolidationViolationShorthandVsExplicitExpr {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings(value = "unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

// After multi-variable declaration
class InputFieldConsolidationViolationAfterMultiVar {
	int a, b;
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}