// === case: dependency_cycle ===
// skip-reason: cannot reorder: a field dependency cycle has no valid order
class InputConstructorAssignDependencyCycleSliceViolation {
	int alpha, beta, gamma;

	InputConstructorAssignDependencyCycleSliceViolation() {
		this.alpha = beta + 1;
		this.beta = gamma + 1;
		this.gamma = alpha + 1;
	}
}
// === end ===

// === case: no_shadow_dependency_reorder ===
class InputConstructorAssignNoShadowDependencyReorderSliceViolation {
	int alpha, beta;

	InputConstructorAssignNoShadowDependencyReorderSliceViolation() {
		this.alpha = 5;
		this.beta = alpha + 1;
	}
}
// === end ===