package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: objects_require_non_null_single_use ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportObjectsRequireNonNullSingleUseSliceViolation {
	void singleUse(Object x) {
		final var checked = requireNonNull(x);
		System.out.println(checked);
	}
}
// === end ===

// === case: predicate_not_single_use ===
// imports: java.util.function.Predicate
// imports: java.util.stream.Stream
// imports: static java.util.function.Predicate.not
class InputPreferStaticImportPredicateNotSingleUseSliceViolation {
	void singleUse(Stream<String> stream) {
		final var filtered = stream.filter(not(String::isEmpty));
		System.out.println(filtered);
	}
}
// === end ===