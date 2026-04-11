package com.etk2000.checkstyle.inputs.constructorassign;

// C1: simple assignments only, alphabetical
class InputConstructorAssignClean {
	static {}

	int alpha, beta, gamma;
	Object delta;

	// C2: simple then multi-line, both alphabetical
	InputConstructorAssignClean(int alpha, int beta, int gamma, Object delta) {
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;

		this.delta = new Object() {
			@Override
			public String toString() {
				return delta.toString();
			}
		};
	}

	// C10: multiple constructors checked independently
	InputConstructorAssignClean(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}

	// C11: instance initializer
	{
		this.alpha = 1;
		this.beta = 2;
	}

	// C12: empty instance initializer (no assignments)
	{}

	// C4: dependency overriding alphabetical in group 1
	InputConstructorAssignClean(int alpha) {
		this.alpha = alpha;
		this.beta = this.alpha + 1;
		this.gamma = this.alpha + 2;
	}
}

// C14: mixed case field names
class InputConstructorAssignMixedCase {
	int alpha;
	Object Beta;

	InputConstructorAssignMixedCase(int alpha, Object Beta) {
		this.alpha = alpha;
		this.Beta = Beta;
	}
}

// C3: simple then multi-line then var-dependent (full 3-group ordering)
class InputConstructorAssignThreeGroups {
	int alpha, beta, delta;
	Object gamma;

	InputConstructorAssignThreeGroups(int alpha, int beta, Object obj) {
		this.alpha = alpha;
		this.beta = beta;

		this.gamma = new Object() {
			@Override
			public String toString() {
				return obj.toString();
			}
		};

		final var computed = alpha + beta;
		this.delta = computed;
	}
}

// C6: single var sub-group, alphabetical
class InputConstructorAssignSingleVarGroup {
	int alpha, beta;

	InputConstructorAssignSingleVarGroup(int x) {
		final var computed = x * 2;
		this.alpha = computed;
		this.beta = computed + 1;
	}
}

// C7: multiple var sub-groups in declaration order
class InputConstructorAssignMultipleVarGroups {
	int alpha, beta, delta, gamma;

	InputConstructorAssignMultipleVarGroups(int x) {
		final var first = x + 1;
		this.alpha = first;
		this.beta = first + 1;

		final var second = x + 2;
		this.delta = second;
		this.gamma = second + 1;
	}
}

// C8: multi-var assignment in latest sub-group
class InputConstructorAssignMultiVar {
	int alpha, beta, gamma;

	InputConstructorAssignMultiVar(int x) {
		final var first = x + 1;
		this.alpha = first;

		final var second = x + 2;
		this.beta = second;
		this.gamma = first * second;
	}
}

// C9: constructor parameter not treated as local var
class InputConstructorAssignParamNotVar {
	int alpha;
	String beta;

	InputConstructorAssignParamNotVar(int alpha, String param) {
		this.alpha = alpha;
		this.beta = param.trim();
	}
}

// C5: dependency overriding alphabetical in var group
class InputConstructorAssignVarGroupDependency {
	int alpha, beta;

	InputConstructorAssignVarGroupDependency(int beta, int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = computed + this.beta;
	}
}

// C15: multi-line assignment using a local var is group 3, not group 2
class InputConstructorAssignMultiLineWithVar {
	int alpha;
	Object beta;

	InputConstructorAssignMultiLineWithVar(int x) {
		this.alpha = x;

		final var factory = x * 2;
		this.beta = new Object() {
			@Override
			public String toString() {
				return String.valueOf(factory);
			}
		};
	}
}

// C16: single-line assignment using a local var is group 3, not group 1
class InputConstructorAssignSingleLineWithVar {
	int alpha, beta;

	InputConstructorAssignSingleLineWithVar(int x) {
		this.alpha = x;

		final var computed = x * 2;
		this.beta = computed;
	}
}

// two multi-line assignments in correct alphabetical order (clean counterpart of V3)
class InputConstructorAssignMultiAlphaClean {
	Object alpha, beta;

	InputConstructorAssignMultiAlphaClean(Object a, Object b) {
		this.alpha = new Object() {
			@Override
			public String toString() {
				return a.toString();
			}
		};
		this.beta = new Object() {
			@Override
			public String toString() {
				return b.toString();
			}
		};
	}
}

// non-field-assignment statements ignored (method calls, local assignments)
class InputConstructorAssignNonFieldStatements {
	int alpha, beta;

	InputConstructorAssignNonFieldStatements(int alpha, int beta) {
		this.alpha = alpha;
		System.identityHashCode(alpha);
		this.beta = beta;
	}
}

// single assignment: pairwise loop not entered, no violations possible
class InputConstructorAssignSingleAssignment {
	int alpha;

	InputConstructorAssignSingleAssignment(int alpha) {
		this.alpha = alpha;
	}
}

// C13: compact constructor (record)
record InputConstructorAssignRecord(int alpha, int beta) {
	InputConstructorAssignRecord {
		final var temp = alpha + beta;
		this.alpha = temp;
		this.beta = temp + 1;
	}
}