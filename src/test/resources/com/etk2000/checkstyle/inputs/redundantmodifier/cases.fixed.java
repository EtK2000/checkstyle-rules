package com.etk2000.checkstyle.inputs.redundantmodifier;

// Per-case full-pipeline overrides for FullPipelineRegressionTest, where a
// sibling fixer also fires and the result diverges from the RedundantModifier-
// only cases.out.java. Only divergent cases are listed.

// === case: remove_final_from_resource_explicit_type ===
class InputRedundantModifierFinalResourceExplicitTypeSliceViolation {
	void m(AutoCloseable closeable) throws Exception {
		try (var resource = closeable) {
			resource.toString();
		}
	}
}
// === end ===