package com.etk2000.checkstyle.inputs.preferstaticimport;

import static java.util.function.BinaryOperator.minBy;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InputPreferStaticImportImportConflictClean {
	BinaryOperator<Integer> exposeImportedMinBy(Comparator<Integer> c) {
		return minBy(c);
	}

	Optional<Integer> twoCollectorsMinByCallsAreShadowedByStaticImport(Stream<Integer> a, Stream<Integer> b, Comparator<Integer> c) {
		final var x = a.collect(Collectors.minBy(c));
		final var y = b.collect(Collectors.minBy(c));
		return x.isPresent() ? x : y;
	}
}