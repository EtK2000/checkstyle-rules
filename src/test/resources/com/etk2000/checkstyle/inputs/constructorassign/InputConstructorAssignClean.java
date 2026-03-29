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
}