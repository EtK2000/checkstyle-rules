package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisLambdaViolation {
	int field;

	void lambdaWithoutShadowing() {
		Runnable r = () -> System.out.println(this.field); // violation: no shadowing
	}
}