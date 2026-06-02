package com.etk2000.checkstyle.inputs.constructorassignmentorder;

class InputConstructorAssignClean {
	static {}

	int alpha, beta, gamma;
	Object delta;

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

	InputConstructorAssignClean(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}

	{
		this.alpha = 1;
		this.beta = 2;
	}

	{}

	InputConstructorAssignClean(int alpha) {
		this.alpha = alpha;
		this.beta = this.alpha + 1;
		this.gamma = this.alpha + 2;
	}
}

class InputConstructorAssignMixedCase {
	int alpha;
	Object Beta;

	InputConstructorAssignMixedCase(int alpha, Object Beta) {
		this.alpha = alpha;
		this.Beta = Beta;
	}
}

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

class InputConstructorAssignSingleVarGroup {
	int alpha, beta;

	InputConstructorAssignSingleVarGroup(int x) {
		final var computed = x * 2;
		this.alpha = computed;
		this.beta = computed + 1;
	}
}

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

class InputConstructorAssignParamNotVar {
	int alpha;
	String beta;

	InputConstructorAssignParamNotVar(int alpha, String param) {
		this.alpha = alpha;
		this.beta = param.trim();
	}
}

class InputConstructorAssignVarGroupDependency {
	int alpha, beta;

	InputConstructorAssignVarGroupDependency(int beta, int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = computed + this.beta;
	}
}

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

class InputConstructorAssignSingleLineWithVar {
	int alpha, beta;

	InputConstructorAssignSingleLineWithVar(int x) {
		this.alpha = x;

		final var computed = x * 2;
		this.beta = computed;
	}
}

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

class InputConstructorAssignNonFieldStatements {
	int alpha, beta;

	InputConstructorAssignNonFieldStatements(int alpha, int beta) {
		this.alpha = alpha;
		System.identityHashCode(alpha);
		this.beta = beta;
	}
}

class InputConstructorAssignSingleAssignment {
	int alpha;

	InputConstructorAssignSingleAssignment(int alpha) {
		this.alpha = alpha;
	}
}

record InputConstructorAssignRecord(int alpha, int beta) {
	InputConstructorAssignRecord {
		final var temp = alpha + beta;
		this.alpha = temp;
		this.beta = temp + 1;
	}
}

class InputConstructorAssignBareDependencySatisfied {
	int alpha, beta;

	InputConstructorAssignBareDependencySatisfied() {
		this.beta = 5;
		this.alpha = beta + 1;
	}
}

class InputConstructorAssignBareVarDependencySatisfied {
	int alpha, beta;

	InputConstructorAssignBareVarDependencySatisfied(int x) {
		final var c = x;
		this.beta = c;
		this.alpha = c + beta;
	}
}

class InputConstructorAssignBareMethodArgSuppression {
	int alpha, zeta;

	InputConstructorAssignBareMethodArgSuppression(int seed) {
		this.zeta = seed * 2;
		this.alpha = Math.max(zeta, seed);
	}
}

class InputConstructorAssignQualifiedMethodArgSuppression {
	int alpha, zeta;

	InputConstructorAssignQualifiedMethodArgSuppression(int seed) {
		this.zeta = seed * 2;
		this.alpha = Math.max(this.zeta, seed);
	}
}

class InputConstructorAssignParamShadowClean {
	int tracks, value;

	InputConstructorAssignParamShadowClean(int value) {
		this.tracks = value;
		this.value = value;
	}
}

class InputConstructorAssignLocalShadowClean {
	int tracks, value;

	InputConstructorAssignLocalShadowClean(int x) {
		final var value = x * 2;
		this.tracks = value + 1;
		this.value = value;
	}
}

class InputConstructorAssignMemberAccessClean {
	int beta, value;

	InputConstructorAssignMemberAccessClean(InputConstructorAssignMemberAccessClean other) {
		this.beta = other.value;
		this.value = other.beta;
	}
}

class InputConstructorAssignMethodNameClean {
	static int value() {
		return 7;
	}

	int beta, value;

	InputConstructorAssignMethodNameClean() {
		this.beta = value();
		this.value = 3;
	}
}

class InputConstructorAssignLambdaNestedClean {
	int value;
	Runnable task;

	InputConstructorAssignLambdaNestedClean(int seed) {
		this.value = seed;
		this.task = () -> System.out.println(value);
	}
}

class InputConstructorAssignArrayRhsClean {
	int value;
	int[] tracks;

	InputConstructorAssignArrayRhsClean(int seed) {
		this.value = seed;
		this.tracks = new int[]{value};
	}
}

class InputConstructorAssignBareSubstringClean {
	int beta, gamma;

	InputConstructorAssignBareSubstringClean(int betaValue) {
		this.beta = betaValue;
		this.gamma = betaValue + 1;
	}
}

class InputConstructorAssignLoneThisClean {
	static int register(Object o) {
		return 0;
	}

	int alpha, zeta;

	InputConstructorAssignLoneThisClean() {
		this.alpha = register(this);
		this.zeta = register(this);
	}
}

class InputConstructorAssignBareSuffixSubstringClean {
	int tracks, value;

	InputConstructorAssignBareSuffixSubstringClean(int myvalue) {
		this.tracks = myvalue;
		this.value = myvalue;
	}
}

class InputConstructorAssignBlockCommentBraceInGapClean {
	int alpha, beta;

	InputConstructorAssignBlockCommentBraceInGapClean(int a, int b) {
		/* opens a scope {
		still inside the comment */
		this.alpha = a;
		this.beta = b;
	}
}

class InputConstructorAssignCircularDependencyClean {
	int alpha, beta;

	InputConstructorAssignCircularDependencyClean() {
		this.alpha = this.beta + 1;
		this.beta = this.alpha + 1;
	}
}

class InputConstructorAssignCommentPreservesOrderClean {
	int alpha, beta;

	InputConstructorAssignCommentPreservesOrderClean(int x) {
		this.alpha = x; // beta
		this.beta = x;
	}
}

class InputConstructorAssignEscapedCharQuoteClean {
	int alpha, beta;

	InputConstructorAssignEscapedCharQuoteClean(int x) {
		this.alpha = '\'';
		this.beta = x;
	}
}

class InputConstructorAssignEscapedQuoteInStringClean {
	String alpha, beta;

	InputConstructorAssignEscapedQuoteInStringClean(String x) {
		this.alpha = "a\"b";
		this.beta = x;
	}
}

class InputConstructorAssignMaskedVarInBlockCommentClean {
	int alpha, beta;

	InputConstructorAssignMaskedVarInBlockCommentClean(int x) {
		this.beta = /* size */ x;

		final var size = x + 1;
		this.alpha = size;
	}
}

class InputConstructorAssignMaskedVarInLineCommentClean {
	int alpha, beta;

	InputConstructorAssignMaskedVarInLineCommentClean(int x) {
		this.beta = x; // size

		final var size = x + 1;
		this.alpha = size;
	}
}

class InputConstructorAssignTextBlockLocalVarInteriorClean {
	String a, b;

	InputConstructorAssignTextBlockLocalVarInteriorClean(String x) {
		this.b = x;

		final var s = """
				this.a = injected
				""";
		this.a = s;
	}
}

class InputConstructorAssignVarSubstringClean {
	static int sizeLimit() {
		return 10;
	}

	int alpha, beta;

	InputConstructorAssignVarSubstringClean(int x) {
		this.beta = sizeLimit();

		final var size = x + 1;
		this.alpha = size;
	}
}

class InputConstructorAssignDependencyForcedGroupOrderClean {
	int a, z;

	InputConstructorAssignDependencyForcedGroupOrderClean() {
		final var local = 5;
		this.z = local;
		this.a = z + 1;
	}
}

class InputConstructorAssignThisFieldNotLocalClean {
	int alpha, beta, value;

	InputConstructorAssignThisFieldNotLocalClean() {
		final var value = 5;
		this.alpha = this.value;
		this.beta = 1;
	}
}

class InputConstructorAssignMultiBeforeSimpleForcedClean {
	int alpha;
	Object beta;

	InputConstructorAssignMultiBeforeSimpleForcedClean() {
		this.beta = new Object() {
			@Override
			public String toString() {
				return "b";
			}
		};
		this.alpha = beta.hashCode();
	}
}

class InputConstructorAssignVarBeforeMultiForcedClean {
	int alpha;
	Object beta;

	InputConstructorAssignVarBeforeMultiForcedClean(int x) {
		final var local = x;
		this.alpha = local;
		this.beta = new Object() {
			@Override
			public String toString() {
				return String.valueOf(alpha);
			}
		};
	}
}