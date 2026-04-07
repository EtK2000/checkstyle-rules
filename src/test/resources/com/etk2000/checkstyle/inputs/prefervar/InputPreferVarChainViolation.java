package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarChainViolation {
	void chainedGenericReturnExplicitType() {
		// chain: GenericReturnHelper.create() → GenericReturnHelper, find(int) → T (method-level)
		final String s = GenericReturnHelper.create().find(1);
	}

	void chainedGenericReturnVar() {
		// chain resolves: create() → GenericReturnHelper, find() needs target type
		final var s = GenericReturnHelper.create().find(1); // violation (warning): Using 'var' with 'find' loses generic type information, consider using an explicit type.
	}
}