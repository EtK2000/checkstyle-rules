package com.etk2000.checkstyle.testhelpers;

/** Test helper for a classpath supertype whose inherited methods return a {@link ClasspathDoc} or a {@code String}. */
public class ClasspathDocSource {
	public static ClasspathDoc create() {
		return new ClasspathDoc();
	}

	public String label() {
		return "";
	}

	protected ClasspathDoc source() {
		return new ClasspathDoc();
	}

	public ClasspathDoc text() {
		return new ClasspathDoc();
	}

	public ClasspathDoc text(int index) {
		return new ClasspathDoc();
	}
}