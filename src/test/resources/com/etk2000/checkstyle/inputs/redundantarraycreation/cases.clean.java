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

	void commentBraceInArrayInitializer() {
		final String[] arr = {"a", /* } */ "b"};
	}

	void explicitSizeArrayToVarargs() {
		Arrays.asList(new Object[5]);
	}

	void listOfHasNonVarargsOverload() {
		List.of(new Object[]{"a"});
	}

	private void localVarArgs(String... args) {}

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
		Arrays.asList(new long[]{1L});
	}

	void primitiveArrayToReferenceShortVarargs() {
		Arrays.asList(new short[]{1});
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