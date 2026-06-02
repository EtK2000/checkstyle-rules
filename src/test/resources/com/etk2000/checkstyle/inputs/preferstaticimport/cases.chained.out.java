package com.etk2000.checkstyle.inputs.preferstaticimport;

// === case: chained_calls ===
// imports: java.util.List
// imports: java.util.Objects
// imports: java.util.function.Predicate
// imports: static java.util.Objects.requireNonNull
// imports: static java.util.function.Predicate.not
class InputPreferStaticImportChainedCallsSliceViolation {
	List<String> chainedCalls(List<String> list, String prefix, String suffix) {
		return list.stream()
				.filter(not(requireNonNull(prefix)::startsWith))
				.filter(not(requireNonNull(suffix)::endsWith))
				.toList();
	}
}
// === end ===