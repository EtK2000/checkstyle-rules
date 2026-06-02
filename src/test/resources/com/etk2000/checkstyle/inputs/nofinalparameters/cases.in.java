package com.etk2000.checkstyle.inputs.nofinalparameters;

// === case: annotated_final_param ===
class InputNoFinalParametersAnnotatedFinalParamSliceViolation {
	void annotatedFinal(@SuppressWarnings("unused") final String s) {} // violation: Remove 'final' modifier from parameter 's'.
}
// === end ===

// === case: catch_multi_with_final ===
class InputNoFinalParametersCatchMultiWithFinalSliceViolation {
	void m() {
		try {
			System.out.println();
		}
		catch (final RuntimeException | Error e) { // violation: Remove 'final' modifier from parameter 'e'.
			System.out.println(e);
		}
	}
}
// === end ===

// === case: catch_with_final ===
class InputNoFinalParametersCatchWithFinalSliceViolation {
	void m() {
		try {
			System.out.println();
		}
		catch (final Exception e) { // violation: Remove 'final' modifier from parameter 'e'.
			System.out.println(e);
		}
	}
}
// === end ===

// === case: constructor_param ===
class InputNoFinalParametersConstructorParamSliceViolation {
	InputNoFinalParametersConstructorParamSliceViolation(final int x) {} // violation: Remove 'final' modifier from parameter 'x'.
}
// === end ===

// === case: final_annotated_param ===
class InputNoFinalParametersFinalAnnotatedParamSliceViolation {
	void finalAnnotated(final @SuppressWarnings("unused") int x) {} // violation: Remove 'final' modifier from parameter 'x'.
}
// === end ===

// === case: first_param_final ===
class InputNoFinalParametersFirstParamFinalSliceViolation {
	void firstParamFinal(final int x, String y) {} // violation: Remove 'final' modifier from parameter 'x'.
}
// === end ===

// === case: for_each_annotated_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachAnnotatedWithFinalSliceViolation {
	void m(List<String> list) {
		for (@SuppressWarnings("unused") final var item : list) // violation: Remove 'final' modifier from for-each variable 'item'.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_explicit_type_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachExplicitTypeWithFinalSliceViolation {
	void m(List<Integer> list) {
		for (final int item : list) // violation: Remove 'final' modifier from for-each variable 'item'.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachWithFinalSliceViolation {
	void m(List<String> list) {
		for (final var item : list) // violation: Remove 'final' modifier from for-each variable 'item'.
			System.out.println(item);
	}
}
// === end ===

// === case: for_init_multi_with_final ===
class InputNoFinalParametersForInitMultiSliceViolation {
	void m() {
		for (final int i = 0, size = 10; i < size;) // violation: For-loop variable 'i' must not be final, move it before the loop. // violation: For-loop variable 'size' must not be final, move it before the loop.
			break;
	}
}
// === end ===

// === case: for_init_single_with_final ===
// imports: java.util.List
class InputNoFinalParametersForInitSingleSliceViolation {
	void m(List<String> list) {
		for (final var size = list.size(); size > 0;) // violation: For-loop variable 'size' must not be final, move it before the loop.
			break;
	}
}
// === end ===

// === case: main ===
// multi-fix-expected
// imports: java.util.List
class InputNoFinalParametersViolation {
	void bothFinal(final int x, final String y) {} // violation: Remove 'final' modifier from parameter 'x'. // violation: Remove 'final' modifier from parameter 'y'.

	void lambdaWithFinal(List<String> list) {
		list.sort((final String a, final String b) -> a.compareTo(b)); // violation: Remove 'final' modifier from parameter 'a'. // violation: Remove 'final' modifier from parameter 'b'.
	}
}
// === end ===

// === case: second_param_final ===
class InputNoFinalParametersSecondParamFinalSliceViolation {
	void secondParamFinal(int x, final String y) {} // violation: Remove 'final' modifier from parameter 'y'.
}
// === end ===

// === case: single_final_method ===
class InputNoFinalParametersSingleFinalMethodSliceViolation {
	void singleFinal(final int x) {} // violation: Remove 'final' modifier from parameter 'x'.
}
// === end ===

// === case: varargs_final ===
class InputNoFinalParametersVarargsFinalSliceViolation {
	void varargsFinal(final String... args) {} // violation: Remove 'final' modifier from parameter 'args'.
}
// === end ===