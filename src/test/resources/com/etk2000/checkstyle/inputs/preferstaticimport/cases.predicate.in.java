package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: predicate_not_two_uses ===
// imports: java.util.List
// imports: java.util.function.Predicate
// multi-fix-expected
class InputPreferStaticImportPredicateNotTwoUsesSliceViolation {
	List<String> twoUses(List<String> list) {
		return list.stream()
				.filter(Predicate.not(String::isEmpty)) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'.
				.filter(Predicate.not(s -> s.startsWith("#"))) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'.
				.toList();
	}
}
// === end ===