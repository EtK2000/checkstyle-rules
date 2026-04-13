package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.List;
import java.util.function.Predicate;

class InputPreferStaticImportPredicateViolation {
	List<String> twoUses(List<String> list) {
		return list.stream()
				.filter(Predicate.not(String::isEmpty)) // violation: Replace 'Predicate.not' with a static import of 'not'.
				.filter(Predicate.not(s -> s.startsWith("#"))) // violation: Replace 'Predicate.not' with a static import of 'not'.
				.toList();
	}
}