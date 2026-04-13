package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.Objects;
import java.util.function.Supplier;

class InputPreferStaticImportObjectsViolation {
	void isNullChecks(Object a, Object b) {
		final var nullA = Objects.isNull(a); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		final var nullB = Objects.isNull(b); // violation: Replace 'Objects.isNull' with a static import of 'isNull'.
		System.out.println(nullA || nullB);
	}

	void nonNullChecks(Object a, Object b) {
		final var someA = Objects.nonNull(a); // violation: Replace 'Objects.nonNull' with a static import of 'nonNull'.
		final var someB = Objects.nonNull(b); // violation: Replace 'Objects.nonNull' with a static import of 'nonNull'.
		System.out.println(someA && someB);
	}

	void requireNonNullChecks(Object a, Object b) {
		final var x = Objects.requireNonNull(a); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		final var y = Objects.requireNonNull(b); // violation: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		System.out.println(x + ":" + y);
	}

	Object requireNonNullElseChecks(Object a, Object b) {
		final var x = Objects.requireNonNullElse(a, "fallback-a"); // violation: Replace 'Objects.requireNonNullElse' with a static import of 'requireNonNullElse'.
		final var y = Objects.requireNonNullElse(b, "fallback-b"); // violation: Replace 'Objects.requireNonNullElse' with a static import of 'requireNonNullElse'.
		return x.toString() + y.toString();
	}

	Object requireNonNullElseGetChecks(Object a, Object b, Supplier<Object> fallback) {
		final var x = Objects.requireNonNullElseGet(a, fallback); // violation: Replace 'Objects.requireNonNullElseGet' with a static import of 'requireNonNullElseGet'.
		final var y = Objects.requireNonNullElseGet(b, fallback); // violation: Replace 'Objects.requireNonNullElseGet' with a static import of 'requireNonNullElseGet'.
		return x.toString() + y.toString();
	}
}