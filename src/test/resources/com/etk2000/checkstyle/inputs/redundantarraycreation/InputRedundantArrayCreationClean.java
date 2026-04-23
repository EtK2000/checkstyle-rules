package com.etk2000.checkstyle.inputs.redundantarraycreation;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
class InputRedundantArrayCreationClean {
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

	void explicitSizeArrayToVarargs() {
		Arrays.asList(new Object[5]);
	}

	void listOfHasNonVarargsOverload() {
		List.of(new Object[]{"a"});
	}

	private void localVarArgs(String... args) {}

	void primitiveArrayToReferenceBooleanVarargs() {
		List.of(new boolean[]{true});
	}

	void primitiveArrayToReferenceByteVarargs() {
		List.of(new byte[]{1});
	}

	void primitiveArrayToReferenceCharVarargs() {
		List.of(new char[]{'a'});
	}

	void primitiveArrayToReferenceDoubleVarargs() {
		List.of(new double[]{1.0});
	}

	void primitiveArrayToReferenceFloatVarargs() {
		List.of(new float[]{1.0f});
	}

	void primitiveArrayToReferenceIntVarargs() {
		List.of(new int[]{1, 2});
	}

	void primitiveArrayToReferenceLongVarargs() {
		List.of(new long[]{1L});
	}

	void primitiveArrayToReferenceShortVarargs() {
		List.of(new short[]{1});
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