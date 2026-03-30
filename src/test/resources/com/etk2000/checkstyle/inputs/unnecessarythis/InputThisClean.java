package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisClean {
	int field;

	InputThisClean(int field) {
		this.field = field;
	}

	{
		this.field = 42;
	}

	// lambda: this.field needed because outer method param shadows it
	void lambdaWithShadowing(int field) {
		Runnable r = () -> System.out.println(this.field);
	}

	void localShadowing() {
		int field = 42;
		System.out.println(this.field);
	}

	void method(int field) {
		System.out.println(this.field);
	}
}