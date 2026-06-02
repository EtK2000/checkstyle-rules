package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: collectors_grouping_by_two_uses ===
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// multi-fix-expected
class InputPreferStaticImportCollectorsGroupingByTwoUsesSliceViolation {
	Map<Integer, List<String>> mixedCollectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var groupA = a.collect(Collectors.groupingBy(String::length)); // violation [minSdk>=24]: Replace 'Collectors.groupingBy' with a static import of 'groupingBy'.
		final var groupB = b.collect(Collectors.groupingBy(String::length)); // violation [minSdk>=24]: Replace 'Collectors.groupingBy' with a static import of 'groupingBy'.
		groupA.putAll(groupB);
		return groupA;
	}
}
// === end ===

// === case: collectors_joining_two_uses ===
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// multi-fix-expected
class InputPreferStaticImportCollectorsJoiningTwoUsesSliceViolation {
	String joiningTwoUses(Stream<String> a, Stream<String> b) {
		final var s1 = a.collect(Collectors.joining(", ")); // violation [minSdk>=24]: Replace 'Collectors.joining' with a static import of 'joining'.
		final var s2 = b.collect(Collectors.joining("/")); // violation [minSdk>=24]: Replace 'Collectors.joining' with a static import of 'joining'.
		return s1 + s2;
	}
}
// === end ===

// === case: collectors_to_set_two_uses ===
// imports: java.util.Set
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// multi-fix-expected
class InputPreferStaticImportCollectorsToSetTwoUsesSliceViolation {
	Set<String> toSetTwoUses(Stream<String> a, Stream<String> b) {
		final var setA = a.collect(Collectors.toSet()); // violation [minSdk>=24]: Replace 'Collectors.toSet' with a static import of 'toSet'.
		final var setB = b.collect(Collectors.toSet()); // violation [minSdk>=24]: Replace 'Collectors.toSet' with a static import of 'toSet'.
		setA.addAll(setB);
		return setA;
	}
}
// === end ===