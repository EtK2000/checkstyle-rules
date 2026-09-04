package com.etk2000.checkstyle.testhelpers;

/** Test helper for a classpath supertype whose inherited method returns its type parameter. */
public class ClasspathValueSource<T> {
	public T value() {
		return null;
	}
}