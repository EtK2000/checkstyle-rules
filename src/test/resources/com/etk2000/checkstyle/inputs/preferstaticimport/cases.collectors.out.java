package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: collectors_grouping_by_two_uses ===
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// imports: static java.util.stream.Collectors.groupingBy
class InputPreferStaticImportCollectorsGroupingByTwoUsesSliceViolation {
	Map<Integer, List<String>> mixedCollectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var groupA = a.collect(groupingBy(String::length));
		final var groupB = b.collect(groupingBy(String::length));
		groupA.putAll(groupB);
		return groupA;
	}
}
// === end ===

// === case: collectors_joining_two_uses ===
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// imports: static java.util.stream.Collectors.joining
class InputPreferStaticImportCollectorsJoiningTwoUsesSliceViolation {
	String joiningTwoUses(Stream<String> a, Stream<String> b) {
		final var s1 = a.collect(joining(", "));
		final var s2 = b.collect(joining("/"));
		return s1 + s2;
	}
}
// === end ===

// === case: collectors_to_set_two_uses ===
// imports: java.util.Set
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
// imports: static java.util.stream.Collectors.toSet
class InputPreferStaticImportCollectorsToSetTwoUsesSliceViolation {
	Set<String> toSetTwoUses(Stream<String> a, Stream<String> b) {
		final var setA = a.collect(toSet());
		final var setB = b.collect(toSet());
		setA.addAll(setB);
		return setA;
	}
}
// === end ===