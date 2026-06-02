package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: objects_is_null_two_uses ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportObjectsIsNullTwoUsesSliceViolation {
	void isNullChecks(Object a, Object b) {
		final var nullA = Objects.isNull(a); // violation [minSdk>=19]: Replace 'Objects.isNull' with a static import of 'isNull'.
		final var nullB = Objects.isNull(b); // violation [minSdk>=19]: Replace 'Objects.isNull' with a static import of 'isNull'.
		System.out.println(nullA || nullB);
	}
}
// === end ===

// === case: objects_non_null_two_uses ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportObjectsNonNullTwoUsesSliceViolation {
	void nonNullChecks(Object a, Object b) {
		final var someA = Objects.nonNull(a); // violation [minSdk>=19]: Replace 'Objects.nonNull' with a static import of 'nonNull'.
		final var someB = Objects.nonNull(b); // violation [minSdk>=19]: Replace 'Objects.nonNull' with a static import of 'nonNull'.
		System.out.println(someA && someB);
	}
}
// === end ===

// === case: objects_require_non_null_else_get_two_uses ===
// imports: java.util.Objects
// imports: java.util.function.Supplier
// multi-fix-expected
class InputPreferStaticImportObjectsRequireNonNullElseGetTwoUsesSliceViolation {
	Object requireNonNullElseGetChecks(Object a, Object b, Supplier<Object> fallback) {
		final var x = Objects.requireNonNullElseGet(a, fallback); // violation [minSdk>=30]: Replace 'Objects.requireNonNullElseGet' with a static import of 'requireNonNullElseGet'.
		final var y = Objects.requireNonNullElseGet(b, fallback); // violation [minSdk>=30]: Replace 'Objects.requireNonNullElseGet' with a static import of 'requireNonNullElseGet'.
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_else_two_uses ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportObjectsRequireNonNullElseTwoUsesSliceViolation {
	Object requireNonNullElseChecks(Object a, Object b) {
		final var x = Objects.requireNonNullElse(a, "fallback-a"); // violation [minSdk>=30]: Replace 'Objects.requireNonNullElse' with a static import of 'requireNonNullElse'.
		final var y = Objects.requireNonNullElse(b, "fallback-b"); // violation [minSdk>=30]: Replace 'Objects.requireNonNullElse' with a static import of 'requireNonNullElse'.
		return x.toString() + y.toString();
	}
}
// === end ===

// === case: objects_require_non_null_two_uses ===
// imports: java.util.Objects
// multi-fix-expected
class InputPreferStaticImportObjectsRequireNonNullTwoUsesSliceViolation {
	void requireNonNullChecks(Object a, Object b) {
		final var x = Objects.requireNonNull(a); // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		final var y = Objects.requireNonNull(b); // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
		System.out.println(x + ":" + y);
	}
}
// === end ===