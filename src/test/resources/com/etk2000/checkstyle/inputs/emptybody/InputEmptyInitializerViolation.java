package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyInitializerViolation {
	static {} // violation: Empty static initializer block, remove it.

	static { // violation: Empty static initializer block, remove it.
	}

	{} // violation: Empty instance initializer block, remove it.

	{ // violation: Empty instance initializer block, remove it.
	}
}