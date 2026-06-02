package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: objects_is_null_two_uses ===
// imports: java.util.Objects
class InputPreferStaticImportObjectsIsNullTwoUsesSliceViolation {
	void isNullChecks(Object a, Object b) {
		final var nullA = Objects.isNull(a);
		final var nullB = Objects.isNull(b);
		System.out.println(nullA || nullB);
	}
}
// === end ===

// === case: objects_non_null_two_uses ===
// imports: java.util.Objects
class InputPreferStaticImportObjectsNonNullTwoUsesSliceViolation {
	void nonNullChecks(Object a, Object b) {
		final var someA = Objects.nonNull(a);
		final var someB = Objects.nonNull(b);
		System.out.println(someA && someB);
	}
}
// === end ===

// === case: objects_require_non_null_else_get_two_uses ===
// imports: java.util.Objects
// imports: java.util.function.Supplier
class InputPreferStaticImportObjectsRequireNonNullElseGetTwoUsesSliceViolation {
	Object requireNonNullElseGetChecks(Object a, Object b, Supplier<Object> fallback) {
		final var x = Objects.requireNonNullElseGet(a, fallback);
		final var y = Objects.requireNonNullElseGet(b, fallback);
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_else_two_uses ===
// imports: java.util.Objects
class InputPreferStaticImportObjectsRequireNonNullElseTwoUsesSliceViolation {
	Object requireNonNullElseChecks(Object a, Object b) {
		final var x = Objects.requireNonNullElse(a, "fallback-a");
		final var y = Objects.requireNonNullElse(b, "fallback-b");
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_two_uses ===
// imports: java.util.Objects
class InputPreferStaticImportObjectsRequireNonNullTwoUsesSliceViolation {
	void requireNonNullChecks(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		System.out.println(x + ":" + y);
	}
}
// === end ===