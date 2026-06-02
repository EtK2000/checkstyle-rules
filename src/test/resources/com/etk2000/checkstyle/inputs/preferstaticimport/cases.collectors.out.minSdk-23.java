package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: collectors_grouping_by_two_uses ===
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
class InputPreferStaticImportCollectorsGroupingByTwoUsesSliceViolation {
	Map<Integer, List<String>> mixedCollectorsTwoUses(Stream<String> a, Stream<String> b) {
		final var groupA = a.collect(Collectors.groupingBy(String::length));
		final var groupB = b.collect(Collectors.groupingBy(String::length));
		groupA.putAll(groupB);
		return groupA;
	}
}
// === end ===

// === case: collectors_joining_two_uses ===
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
class InputPreferStaticImportCollectorsJoiningTwoUsesSliceViolation {
	String joiningTwoUses(Stream<String> a, Stream<String> b) {
		final var s1 = a.collect(Collectors.joining(", "));
		final var s2 = b.collect(Collectors.joining("/"));
		return s1 + s2;
	}
}
// === end ===

// === case: collectors_to_set_two_uses ===
// imports: java.util.Set
// imports: java.util.stream.Collectors
// imports: java.util.stream.Stream
class InputPreferStaticImportCollectorsToSetTwoUsesSliceViolation {
	Set<String> toSetTwoUses(Stream<String> a, Stream<String> b) {
		final var setA = a.collect(Collectors.toSet());
		final var setB = b.collect(Collectors.toSet());
		setA.addAll(setB);
		return setA;
	}
}
// === end ===