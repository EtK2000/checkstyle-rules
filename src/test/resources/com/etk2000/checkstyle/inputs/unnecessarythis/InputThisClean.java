package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisClean {
	int field;

	InputThisClean(int field) {
		this.field = field;
	}

	{
		this.field = 42;
	}

	void method(int field) {
		System.out.println(this.field);
	}

	void localShadowing() {
		int field = 42;
		System.out.println(this.field);
	}
}