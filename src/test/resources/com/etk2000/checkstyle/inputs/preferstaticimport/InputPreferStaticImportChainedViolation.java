package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

class InputPreferStaticImportChainedViolation {
	List<String> chainedCalls(List<String> list, String prefix, String suffix) {
		return list.stream()
				.filter(Predicate.not(Objects.requireNonNull(prefix)::startsWith)) // violation: Replace 'Predicate.not' with a static import of 'not'. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.filter(Predicate.not(Objects.requireNonNull(suffix)::endsWith)) // violation: Replace 'Predicate.not' with a static import of 'not'. // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.toList();
	}
}