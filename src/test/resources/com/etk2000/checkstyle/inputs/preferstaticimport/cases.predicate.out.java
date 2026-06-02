package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: predicate_not_two_uses ===
// imports: java.util.List
// imports: java.util.function.Predicate
// imports: static java.util.function.Predicate.not
class InputPreferStaticImportPredicateNotTwoUsesSliceViolation {
	List<String> twoUses(List<String> list) {
		return list.stream()
				.filter(not(String::isEmpty))
				.filter(not(s -> s.startsWith("#")))
				.toList();
	}
}
// === end ===