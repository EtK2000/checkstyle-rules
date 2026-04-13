package com.etk2000.checkstyle.inputs.preferstaticimport;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InputPreferStaticImportClean {
	void singleObjectsCallIsBelowDefaultThreshold(Object x) {
		final var checked = Objects.requireNonNull(x);
		System.out.println(checked);
	}

	void singlePredicateCallIsBelowDefaultThreshold(Stream<String> stream) {
		final var filtered = stream.filter(Predicate.not(String::isEmpty));
		System.out.println(filtered);
	}

	List<String> staticImportAlreadyUsed(List<String> list) {
		return list.stream()
				.filter(not(String::isEmpty))
				.filter(not(s -> s.startsWith("#")))
				.toList();
	}

	void unrelatedQualifiedCalls(String s) {
		final var parsed = Integer.parseInt(s);
		final var lower = String.valueOf(parsed).toLowerCase();
		final var width = String.valueOf(lower).length();
		System.out.println(width);
	}

	List<String> usesCarvedOutCollectorsToList(Stream<String> a, Stream<String> b, Stream<String> c) {
		final var x = a.collect(Collectors.toList());
		final var y = b.collect(Collectors.toList());
		final var z = c.collect(Collectors.toUnmodifiableList());
		return x.isEmpty() ? (y.isEmpty() ? z : y) : x;
	}
}