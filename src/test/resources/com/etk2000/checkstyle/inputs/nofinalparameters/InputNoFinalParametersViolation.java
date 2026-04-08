package com.etk2000.checkstyle.inputs.nofinalparameters;

import java.util.List;

class InputNoFinalParametersViolation {
	InputNoFinalParametersViolation(final int x) {} // violation: Parameter 'x' must not be final.

	void annotatedFinal(@SuppressWarnings("unused") final String s) {} // violation: Parameter 's' must not be final.

	void bothFinal(final int x, final String y) {} // violation: Parameter 'x' must not be final. // violation: Parameter 'y' must not be final.

	void catchMultiWithFinal() {
		try {
			System.out.println();
		}
		catch (final RuntimeException | Error e) { // violation: Parameter 'e' must not be final.
			System.out.println(e);
		}
	}

	void catchWithFinal() {
		try {
			System.out.println();
		}
		catch (final Exception e) { // violation: Parameter 'e' must not be final.
			System.out.println(e);
		}
	}

	void finalAnnotated(final @SuppressWarnings("unused") int x) {} // violation: Parameter 'x' must not be final.

	void firstParamFinal(final int x, String y) {} // violation: Parameter 'x' must not be final.

	void forEachAnnotatedWithFinal(List<String> list) {
		for (@SuppressWarnings("unused") final var item : list) // violation: For-each variable 'item' must not be final.
			System.out.println(item);
	}

	void forEachWithFinal(List<String> list) {
		for (final var item : list) // violation: For-each variable 'item' must not be final.
			System.out.println(item);
	}

	void forInitMultiWithFinal() {
		for (final int i = 0, size = 10; i < size;) // violation (warning): For-loop variable 'i' must not be final, move it before the loop. // violation (warning): For-loop variable 'size' must not be final, move it before the loop.
			break;
	}

	void forInitWithFinal(List<String> list) {
		for (final var size = list.size(); size > 0;) // violation (warning): For-loop variable 'size' must not be final, move it before the loop.
			break;
	}

	void lambdaWithFinal(List<String> list) {
		list.sort((final String a, final String b) -> a.compareTo(b)); // violation: Parameter 'a' must not be final. // violation: Parameter 'b' must not be final.
	}

	void secondParamFinal(int x, final String y) {} // violation: Parameter 'y' must not be final.

	void singleFinal(final int x) {} // violation: Parameter 'x' must not be final.

	void varargsFinal(final String... args) {} // violation: Parameter 'args' must not be final.
}