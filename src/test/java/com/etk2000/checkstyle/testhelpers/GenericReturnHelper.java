package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper simulating a class like Android's View that has
 * a method-level generic return type not inferable from arguments.
 */
public class GenericReturnHelper {
	public static GenericReturnHelper create() {
		return new GenericReturnHelper();
	}

	public <T> T find(int id) {
		return null;
	}
}