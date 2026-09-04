package com.etk2000.checkstyle.inputs.preferstaticimport;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InputPreferStaticImportClean {
	static class Checker {
		boolean isNull(Object v) {
			return v == null;
		}
	}

	void callReceiverIsNeverFlagged(Object a, Object b) {
		System.out.println(supplier().isNull(a));
		System.out.println(supplier().isNull(b));
	}

	void classLiteralReceiverIsNeverFlagged() {
		System.out.println(Objects.class.getSimpleName());
		System.out.println(Objects.class.getName());
	}

	void methodReferenceIsNeverFlagged(Stream<String> first, Stream<String> second) {
		System.out.println(first.anyMatch(Objects::isNull));
		System.out.println(second.anyMatch(Objects::isNull));
	}

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

	Checker supplier() {
		return new Checker();
	}

	/**
	 * A type witness puts {@code TYPE_ARGUMENTS} between the class name and the method name, and an
	 * unqualified invocation has nowhere to write one, so there is no static-import form of this call
	 * to report it in favour of.
	 */
	Map<String, Integer> typeWitnessIsNeverFlagged(List<String> first, List<String> second) {
		final var a = first.stream().collect(Collectors.<String, Integer>toMap(k -> k, k -> 1));
		final var b = second.stream().collect(Collectors.<String, Integer>toMap(k -> k, k -> 2));
		return a.isEmpty() ? b : a;
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