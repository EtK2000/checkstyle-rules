package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper: a classpath generic class with an inner class, whose type takes the enclosing
 * instance's type arguments ({@code ClasspathGenericOuter<String>.Inner}), which a raw
 * {@code ClasspathGenericOuter.Inner} leaves out.
 */
public class ClasspathGenericOuter<T> {
	public class Inner {
		public T value() {
			return null;
		}
	}
}