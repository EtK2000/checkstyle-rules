package com.etk2000.checkstyle.inputs.preferimport;

@java.lang.SuppressWarnings("unused") // violation: Use an import instead of fully qualified name 'java.lang.SuppressWarnings'.
class InputPreferImportViolation
		extends java.util.ArrayList<String> // violation: Use an import instead of fully qualified name 'java.util.ArrayList'.
		implements java.io.Serializable { // violation: Use an import instead of fully qualified name 'java.io.Serializable'.
	java.util.Map<String, Integer> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.

	void castAndInstanceof(Object obj) {
		if (obj instanceof java.util.List) // violation: Use an import instead of fully qualified name 'java.util.List'.
			System.out.println((java.util.List<?>) obj); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}

	java.util.List<String> method(java.util.Set<Integer> param) // violation: Use an import instead of fully qualified name 'java.util.Set'.
			throws java.io.IOException { // violation: Use an import instead of fully qualified name 'java.io.IOException'.
		final java.util.List<String> local = null; // violation: Use an import instead of fully qualified name 'java.util.List'.
		return local;
	}

	java.util.List<java.util.Map<String, Integer>> nestedGenerics() { // violation: Use an import instead of fully qualified name 'java.util.Map'.
		return null;
	}

	void newExpression() {
		final var list = new java.util.ArrayList<String>(); // violation: Use an import instead of fully qualified name 'java.util.ArrayList'.
	}
}