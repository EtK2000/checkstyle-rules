package com.etk2000.checkstyle.inputs.redundantarraycreation;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

// positions for RedundantArrayCreationLocatorTest to address directly. No violation markers: most
// of these are positions the check never reports
@SuppressWarnings("unused")
class InputLocatorPositions {
	void castWrappedCreation() {
		Arrays.asList((Object) new String[]{"a"});
	}

	void chainedReceiver() {
		new StringBuilder("x%s").toString().formatted(new Object[]{"a"});
	}

	void exactColumn() {
		Arrays.asList(new Object[]{"a"});
	}

	void explicitSizeCreation() {
		Arrays.asList(new Object[5]);
	}

	private void localVarArgs(String... args) {}

	void nestedCreationInsideAnElement() {
		Arrays.asList(new Object[]{new Object[]{"a"}});
	}

	void nestedOwnerCalls() {
		Arrays.asList(new Object[]{String.join(",", new CharSequence[]{"a"}), "b"});
	}

	void nonVarargsOverload() {
		List.of(new Object[]{"a"});
	}

	String[] notAnArgument() {
		return new String[]{"a"};
	}

	void notTheLastArgument() {
		String.format("%s", new Object[]{}, "x");
	}

	void primitiveToPrimitiveVarargs() {
		IntStream.of(new int[]{1, 2});
	}

	void primitiveToReferenceVarargs() {
		Arrays.asList(new int[]{1});
	}

	void supplementaryAhead() {
		System.out.println("𝒜" + String.join(",", new CharSequence[]{"a"}));
	}

	void twoCreationsInOneArgumentList() {
		String.format("%s", new Object[]{"a"}, new Object[]{"b"});
	}

	void twoCreationsOnOneLine() {
		System.out.println(String.join(",", new CharSequence[]{"a"}) + String.join(";", new CharSequence[]{"b"}));
	}

	void unqualifiedCall() {
		localVarArgs(new String[]{"a"});
	}

	void variableReceiver(PrintWriter writer) {
		writer.format("%s", new Object[]{"a"});
	}
}