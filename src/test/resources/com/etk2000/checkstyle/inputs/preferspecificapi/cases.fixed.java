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

// === case: to_array_non_zero_first_rejected ===
// imports: java.util.List
class InputSpecificApiToArrayNonZeroFirstRejectedSliceViolation {
	int m(List<String> a, List<String> b) {
		return a.toArray(String[]::new).length + b.toArray(String[]::new).length;
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