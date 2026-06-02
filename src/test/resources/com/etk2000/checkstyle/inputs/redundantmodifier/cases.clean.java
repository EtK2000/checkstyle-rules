package com.etk2000.checkstyle.inputs.redundantmodifier;

class InputRedundantModifierClean {
	int field;

	void method() {}
}

@interface InputRedundantModifierCleanAnnotation {
	int value();
}

abstract class InputRedundantModifierCleanClass {
	static class StaticNested {}

	public static final int CONSTANT = 1;

	public abstract void abstractMethod();

	void cleanResource(AutoCloseable closeable) throws Exception {
		try (var resource = closeable) {
			resource.toString();
		}
	}

	void cleanResourceExplicitType(AutoCloseable closeable) throws Exception {
		try (AutoCloseable resource = closeable) {
			resource.toString();
		}
	}

	void method() {}
}

interface InputRedundantModifierCleanInterface {
	class Nested {}

	@interface NestedAnnotation {}

	enum NestedEnum {}

	int VALUE = 1;

	void method();
}