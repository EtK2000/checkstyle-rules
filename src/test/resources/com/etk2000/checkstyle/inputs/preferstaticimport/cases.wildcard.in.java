package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: wildcard_collectors_to_set_two_uses ===
// imports: java.util.Set
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// multi-fix-expected
class InputPreferStaticImportWildcardCollectorsToSetTwoUsesSliceViolation {
	Set<String> collectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var x = a.collect(Collectors.toSet()); // violation [minSdk>=24]: Replace 'Collectors.toSet' with a static import of 'toSet'.
		final var y = b.collect(Collectors.toSet()); // violation [minSdk>=24]: Replace 'Collectors.toSet' with a static import of 'toSet'.
		x.addAll(y);
		return x;
	}
}
// === end ===

// === case: wildcard_objects_require_non_null_two_uses ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportWildcardObjectsRequireNonNullTwoUsesSliceViolation {
	Object objectsTwoUses(Object a, Object b) {
		final var x = Objects.requireNonNull(a); // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		final var y = Objects.requireNonNull(b); // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: wildcard_predicate_not_two_uses ===
// imports: java.util.List
// imports: java.util.function.Predicate
// multi-fix-expected
class InputPreferStaticImportWildcardPredicateNotTwoUsesSliceViolation {
	List<String> predicateTwoUses(List<String> list) {
		return list.stream()
				.filter(Predicate.not(String::isEmpty)) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'.
				.filter(Predicate.not(String::isBlank)) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'.
				.toList();
	}
}
// === end ===