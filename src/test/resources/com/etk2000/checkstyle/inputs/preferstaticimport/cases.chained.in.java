package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: chained_calls ===
// imports: java.util.List
// imports: java.util.Objects
// imports: java.util.function.Predicate
// multi-fix-expected
class InputPreferStaticImportChainedCallsSliceViolation {
	List<String> chainedCalls(List<String> list, String prefix, String suffix) {
		return list.stream()
				.filter(Predicate.not(Objects.requireNonNull(prefix)::startsWith)) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'. // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.filter(Predicate.not(Objects.requireNonNull(suffix)::endsWith)) // violation [minSdk>=33]: Replace 'Predicate.not' with a static import of 'not'. // violation [minSdk>=19]: Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.
				.toList();
	}
}
// === end ===