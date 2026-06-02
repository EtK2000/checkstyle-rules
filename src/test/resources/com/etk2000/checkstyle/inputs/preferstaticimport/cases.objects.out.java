package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: objects_is_null_two_uses ===
// imports: java.util.Objects
// imports: static java.util.Objects.isNull
class InputPreferStaticImportObjectsIsNullTwoUsesSliceViolation {
	void isNullChecks(Object a, Object b) {
		final var nullA = isNull(a);
		final var nullB = isNull(b);
		System.out.println(nullA || nullB);
	}
}
// === end ===

// === case: objects_non_null_two_uses ===
// imports: java.util.Objects
// imports: static java.util.Objects.nonNull
class InputPreferStaticImportObjectsNonNullTwoUsesSliceViolation {
	void nonNullChecks(Object a, Object b) {
		final var someA = nonNull(a);
		final var someB = nonNull(b);
		System.out.println(someA && someB);
	}
}
// === end ===

// === case: objects_require_non_null_else_get_two_uses ===
// imports: java.util.Objects
// imports: java.util.function.Supplier
// imports: static java.util.Objects.requireNonNullElseGet
class InputPreferStaticImportObjectsRequireNonNullElseGetTwoUsesSliceViolation {
	Object requireNonNullElseGetChecks(Object a, Object b, Supplier<Object> fallback) {
		final var x = requireNonNullElseGet(a, fallback);
		final var y = requireNonNullElseGet(b, fallback);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_else_two_uses ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNullElse
class InputPreferStaticImportObjectsRequireNonNullElseTwoUsesSliceViolation {
	Object requireNonNullElseChecks(Object a, Object b) {
		final var x = requireNonNullElse(a, "fallback-a");
		final var y = requireNonNullElse(b, "fallback-b");
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_two_uses ===
// imports: java.util.Objects
// imports: static java.util.Objects.requireNonNull
class InputPreferStaticImportObjectsRequireNonNullTwoUsesSliceViolation {
	void requireNonNullChecks(Object a, Object b) {
		final var x = requireNonNull(a);
		final var y = requireNonNull(b);
		System.out.println(x + ":" + y);
	}
}
// === end ===