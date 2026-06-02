package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: predicate_not_two_uses ===
// imports: java.util.List
// imports: java.util.function.Predicate
class InputPreferStaticImportPredicateNotTwoUsesSliceViolation {
	List<String> twoUses(List<String> list) {
		return list.stream()
				.filter(Predicate.not(String::isEmpty))
				.filter(Predicate.not(s -> s.startsWith("#")))
				.toList();
	}
}
// === end ===