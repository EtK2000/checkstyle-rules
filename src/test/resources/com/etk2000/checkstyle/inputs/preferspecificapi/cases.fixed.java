// === case: equals_empty_qualified_string_parameter ===
class InputSpecificApiStringMethodEqualsEmptyQualifiedStringParameterSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: shadowing_inverse_get_zero ===
// imports: java.util.List
// imports: java.util.Map
class InputSpecificApiReflectionShadowingInverseGetZeroSliceViolation {
	private final Map<Integer, String> shared = Map.of();

	void shadowingInverseGetZero(List<List<String>> lists) {
		lists.forEach(shared -> System.out.println(shared.getFirst()));
	}
}
// === end ===

// === case: to_array_qualified ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayQualifiedSliceViolation {
	void toArrayQualified(List<String> list) {
		final var arr = list.toArray(String[]::new);
	}
}
// === end ===