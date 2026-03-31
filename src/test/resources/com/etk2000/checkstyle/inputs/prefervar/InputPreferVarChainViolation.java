package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarChainViolation {
	void chainedGenericReturnExplicitType() {
		// chain: GenericReturnHelper.create() → GenericReturnHelper, find(int) → T (method-level)
		String s = GenericReturnHelper.create().find(1);
	}

	void chainedGenericReturnVar() {
		// chain resolves: create() → GenericReturnHelper, find() needs target type
		var s = GenericReturnHelper.create().find(1); // violation: var with generic return type
	}
}