package com.etk2000.checkstyle.inputs.nofinalparameters;

// === case: annotated_final_param ===
class InputNoFinalParametersAnnotatedFinalParamSliceViolation {
	void annotatedFinal(@SuppressWarnings("unused") String s) {}
}
// === end ===

// === case: catch_multi_with_final ===
class InputNoFinalParametersCatchMultiWithFinalSliceViolation {
	void m() {
		try {
			System.out.println();
		}
		catch (RuntimeException | Error e) {
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
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: constructor_param ===
class InputNoFinalParametersConstructorParamSliceViolation {
	InputNoFinalParametersConstructorParamSliceViolation(int x) {}
}
// === end ===

// === case: final_annotated_param ===
class InputNoFinalParametersFinalAnnotatedParamSliceViolation {
	void finalAnnotated(@SuppressWarnings("unused") int x) {}
}
// === end ===

// === case: first_param_final ===
class InputNoFinalParametersFirstParamFinalSliceViolation {
	void firstParamFinal(int x, String y) {}
}
// === end ===

// === case: for_each_annotated_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachAnnotatedWithFinalSliceViolation {
	void m(List<String> list) {
		for (@SuppressWarnings("unused") var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_explicit_type_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachExplicitTypeWithFinalSliceViolation {
	void m(List<Integer> list) {
		for (int item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_with_final ===
// imports: java.util.List
class InputNoFinalParametersForEachWithFinalSliceViolation {
	void m(List<String> list) {
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_init_multi_with_final ===
class InputNoFinalParametersForInitMultiSliceViolation {
	void m() {
		for (int i = 0, size = 10; i < size;)
			break;
	}
}
// === end ===

// === case: for_init_single_with_final ===
// imports: java.util.List
class InputNoFinalParametersForInitSingleSliceViolation {
	void m(List<String> list) {
		for (var size = list.size(); size > 0;)
			break;
	}
}
// === end ===

// === case: main ===
// imports: java.util.List
class InputNoFinalParametersViolation {
	void bothFinal(int x, String y) {}

	void lambdaWithFinal(List<String> list) {
		list.sort((String a, String b) -> a.compareTo(b));
	}
}
// === end ===

// === case: second_param_final ===
class InputNoFinalParametersSecondParamFinalSliceViolation {
	void secondParamFinal(int x, String y) {}
}
// === end ===

// === case: single_final_method ===
class InputNoFinalParametersSingleFinalMethodSliceViolation {
	void singleFinal(int x) {}
}
// === end ===

// === case: varargs_final ===
class InputNoFinalParametersVarargsFinalSliceViolation {
	void varargsFinal(String... args) {}
}
// === end ===