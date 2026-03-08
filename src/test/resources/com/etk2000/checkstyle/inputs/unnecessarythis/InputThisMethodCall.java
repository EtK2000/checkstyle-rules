package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisMethodCall {
	void doSomething() {}

	void method() {
		this.doSomething();
	}
}