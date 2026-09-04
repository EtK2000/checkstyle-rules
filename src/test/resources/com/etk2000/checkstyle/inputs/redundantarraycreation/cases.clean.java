package com.etk2000.checkstyle.inputs.redundantarraycreation;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
class InputRedundantArrayCreationClean {
	// a CTOR_CALL is not a registered token, and the constructor it names is declared here, so
	// reflection could not confirm it is varargs even if it were
	static class Boxed {
		Boxed() {
			this(new String[]{"a"});
		}

		Boxed(String... parts) {}
	}

	// an ENUM_CONSTANT_DEF is not a registered token either
	enum Mode {
		A(new String[]{"x"});

		Mode(String... parts) {}
	}

	@SuppressWarnings({"unused", "rawtypes"})
	void annotationArrayValueNotFlagged() {}

	void arrayArgNotLast() {
		String.format("%s", new Object[]{}, "x");
	}

	void arrayInForEach() {
		for (var x : new int[]{1, 2, 3})
			System.out.println(x);
	}

	void arrayInReturn() {
		toArray();
	}

	void bareMethodCall() {
		localVarArgs(new String[]{"a"});
	}

	void castAfterArrayCreation() {
		Arrays.asList((Object) new String[]{"a", "b"});
	}

	void castBeforeElements() {
		Arrays.asList((CharSequence[]) new String[]{"a", "b"});
	}

	// the receiver is a chain rather than a simple name, which reflection does not resolve back to
	// a declaring type, so the varargs target is never confirmed
	void chainedReceiverNotResolvable() {
		new StringBuilder("x%s").toString().formatted(new Object[]{"a"});
	}

	void explicitSizeArrayToVarargs() {
		Arrays.asList(new Object[5]);
	}

	void listOfHasNonVarargsOverload() {
		List.of(new Object[]{"a"});
	}

	private void localVarArgs(String... args) {}

	// a lone element whose type cannot be read off its shape keeps the wrapper, because unwrapping
	// an array-typed or null one would re-bind it as the whole varargs array instead of its first
	// element. Two or more elements cannot bind the fixed-arity form, so they are still reported
	void loneArrayVariableElement(Object[] row) {
		Arrays.asList(new Object[]{row});
	}

	// a cast is only proof when the type it names is not itself an array
	void loneCastToArrayElement(Object value) {
		Arrays.asList(new Object[]{(Object[]) value});
	}

	void loneMethodCallElement() {
		String.join(",", new CharSequence[]{toString()});
	}

	void loneNestedArrayCreationElement() {
		Arrays.asList(new Object[]{new Object[]{"a"}});
	}

	void loneNullElement() {
		Arrays.asList(new Object[]{null});
	}

	void loneNullElementWithTrailingComma() {
		Arrays.asList(new Object[]{null,});
	}

	void nonVarargsConstructorArrayArg() {
		new String(new char[]{'a', 'b'});
	}

	void primitiveArrayToReferenceBooleanVarargs() {
		Arrays.asList(new boolean[]{true});
	}

	void primitiveArrayToReferenceByteVarargs() {
		Arrays.asList(new byte[]{1});
	}

	void primitiveArrayToReferenceCharVarargs() {
		Arrays.asList(new char[]{'a'});
	}

	void primitiveArrayToReferenceDoubleVarargs() {
		Arrays.asList(new double[]{1.0});
	}

	void primitiveArrayToReferenceFloatVarargs() {
		Arrays.asList(new float[]{1.0f});
	}

	void primitiveArrayToReferenceIntVarargs() {
		Arrays.asList(new int[]{1, 2});
	}

	void primitiveArrayToReferenceLongVarargs() {
		Arrays.asList(new long[]{1});
	}

	void primitiveArrayToReferenceShortVarargs() {
		Arrays.asList(new short[]{1});
	}

	// reflection loads the target through the check's own class loader, which never carries the
	// sources being analysed, so a varargs method declared in this file is never confirmed
	void sameFileVarargsTargetNotResolvable() {
		InputRedundantArrayCreationCleanHelper.join(new String[]{"a"});
	}

	void stringLiteralReceiverNotResolvable() {
		"x%s".formatted(new Object[]{"a"});
	}

	private String[] toArray() {
		return new String[]{"a"};
	}

	void variableAssignment() {
		final String[] arr = {"a", "b"};
	}

	void variablePassedToVarargs() {
		final String[] arr = {"a"};
		Arrays.asList(arr);
	}

	void zeroArgCallToVarargs() {
		Arrays.asList();
	}
}

@SuppressWarnings("unused")
class InputRedundantArrayCreationCleanHelper {
	static String join(String... parts) {
		return String.valueOf(parts.length);
	}
}