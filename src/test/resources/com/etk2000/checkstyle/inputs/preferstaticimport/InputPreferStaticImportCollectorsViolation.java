package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InputPreferStaticImportCollectorsViolation {
	String joiningTwoUses(Stream<String> a, Stream<String> b) {
		final var s1 = a.collect(Collectors.joining(", ")); // violation: Replace 'Collectors.joining' with a static import of 'joining'.
		final var s2 = b.collect(Collectors.joining("/")); // violation: Replace 'Collectors.joining' with a static import of 'joining'.
		return s1 + s2;
	}

	Map<Integer, List<String>> mixedCollectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var groupA = a.collect(Collectors.groupingBy(String::length)); // violation: Replace 'Collectors.groupingBy' with a static import of 'groupingBy'.
		final var groupB = b.collect(Collectors.groupingBy(String::length)); // violation: Replace 'Collectors.groupingBy' with a static import of 'groupingBy'.
		groupA.putAll(groupB);
		return groupA;
	}

	Set<String> toSetTwoUses(Stream<String> a, Stream<String> b) {
		final var setA = a.collect(Collectors.toSet()); // violation: Replace 'Collectors.toSet' with a static import of 'toSet'.
		final var setB = b.collect(Collectors.toSet()); // violation: Replace 'Collectors.toSet' with a static import of 'toSet'.
		setA.addAll(setB);
		return setA;
	}
}