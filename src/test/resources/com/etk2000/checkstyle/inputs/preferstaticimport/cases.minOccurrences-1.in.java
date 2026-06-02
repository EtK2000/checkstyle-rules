package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: objects_require_non_null_single_use ===
// imports: java.util.Objects
class InputPreferStaticImportObjectsRequireNonNullSingleUseSliceViolation {
	void singleUse(Object x) {
		final var checked = Objects.requireNonNull(x); // violation [minOccurrences==1]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		System.out.println(checked);
	}
}
// === end ===

// === case: predicate_not_single_use ===
// imports: java.util.function.Predicate
// imports: java.util.stream.Stream
class InputPreferStaticImportPredicateNotSingleUseSliceViolation {
	void singleUse(Stream<String> stream) {
		final var filtered = stream.filter(Predicate.not(String::isEmpty)); // violation [minOccurrences==1]: Replace 'Predicate.not' with a static import of 'not'.
		System.out.println(filtered);
	}
}
// === end ===