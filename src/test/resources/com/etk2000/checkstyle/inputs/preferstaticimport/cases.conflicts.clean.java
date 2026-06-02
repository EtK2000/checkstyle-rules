package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

class InputPreferStaticImportConflictsClean {
	@SuppressWarnings("unused")
	boolean isNullCallsHaveShadowingLocalVar(Object Objects, Object x, Object y) {
		final var a = Objects.isNull(x);
		final var b = Objects.isNull(y);
		return a || b;
	}

	boolean not(Object x) {
		return x == null;
	}

	Object requireNonNull(Object value) {
		return value;
	}

	void usesObjectsRequireNonNullButLocalShadows(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		System.out.println(x + ":" + y);
	}

	void usesPredicateNotButLocalShadows(Stream<String> stream) {
		final var s1 = stream.filter(Predicate.not(String::isEmpty));
		final var s2 = stream.filter(Predicate.not(String::isBlank));
		System.out.println(s1 + ":" + s2);
	}
}