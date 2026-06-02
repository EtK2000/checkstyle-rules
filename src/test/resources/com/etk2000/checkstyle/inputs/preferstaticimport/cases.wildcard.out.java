package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: wildcard_collectors_to_set_two_uses ===
// imports: java.util.Set
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// imports: static java.util.stream.Collectors.toSet
class InputPreferStaticImportWildcardCollectorsToSetTwoUsesSliceViolation {
	Set<String> collectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var x = a.collect(toSet());
		final var y = b.collect(toSet());
		x.addAll(y);
		return x;
	}
}
// === end ===

// === case: wildcard_objects_require_non_null_two_uses ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportWildcardObjectsRequireNonNullTwoUsesSliceViolation {
	Object objectsTwoUses(Object a, Object b) {
		final var x = requireNonNull(a);
		final var y = requireNonNull(b);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: wildcard_predicate_not_two_uses ===
// imports: java.util.List
// imports: java.util.function.Predicate
// imports: static java.util.function.Predicate.not
class InputPreferStaticImportWildcardPredicateNotTwoUsesSliceViolation {
	List<String> predicateTwoUses(List<String> list) {
		return list.stream()
				.filter(not(String::isEmpty))
				.filter(not(String::isBlank))
				.toList();
	}
}
// === end ===