package com.etk2000.checkstyle.inputs.constructorassign;

class InputConstructorAssignClean {
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

	// multiple constructors: each checked independently
	InputConstructorAssignClean(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}

	// instance initializer: also checked
	{
		this.alpha = 1;
		this.beta = 2;
	}

	// dependency: beta depends on alpha, so beta after alpha is fine
	// even though gamma depends on alpha too, gamma > beta alphabetically
	InputConstructorAssignClean(int alpha) {
		this.alpha = alpha;
		this.beta = this.alpha + 1;
		this.gamma = this.alpha + 2;
	}
}