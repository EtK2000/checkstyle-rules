package com.etk2000.checkstyle.inputs.preferimport;

@java.lang.SuppressWarnings("unused") // violation: qualified annotation
class InputPreferImportViolation
		extends java.util.ArrayList<String> // violation: qualified extends
		implements java.io.Serializable { // violation: qualified implements
	java.util.Map<String, Integer> field; // violation: qualified field type

	void castAndInstanceof(Object obj) {
		if (obj instanceof java.util.List) // violation: qualified instanceof
			System.out.println((java.util.List<?>) obj); // violation: qualified cast
	}

	java.util.List<String> method(java.util.Set<Integer> param) // violation: qualified return type and param type
			throws java.io.IOException { // violation: qualified throws
		java.util.List<String> local = null; // violation: qualified local type
		return local;
	}

	java.util.List<java.util.Map<String, Integer>> nestedGenerics() { // violation: qualified return type and generic arg
		return null;
	}
}