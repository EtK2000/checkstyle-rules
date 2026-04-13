package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class InputPreferStaticImportWildcardViolation {
	Set<String> collectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var x = a.collect(Collectors.toSet()); // violation: Replace 'Collectors.toSet' with a static import of 'toSet'.
		final var y = b.collect(Collectors.toSet()); // violation: Replace 'Collectors.toSet' with a static import of 'toSet'.
		x.addAll(y);
		return x;
	}

	Object objectsTwoUses(Object a, Object b) {
		final var x = Objects.requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		final var y = Objects.requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		return x.toString() + y.toString();
	}

	List<String> predicateTwoUses(List<String> list) {
		return list.stream()
				.filter(Predicate.not(String::isEmpty)) // violation: Replace 'Predicate.not' with a static import of 'not'.
				.filter(Predicate.not(String::isBlank)) // violation: Replace 'Predicate.not' with a static import of 'not'.
				.toList();
	}
}