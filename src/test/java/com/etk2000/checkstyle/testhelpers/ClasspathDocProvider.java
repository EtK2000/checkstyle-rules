package com.etk2000.checkstyle.testhelpers;

/** Test helper for a classpath interface whose default methods return a {@link ClasspathDoc}. */
public interface ClasspathDocProvider {
	default ClasspathDoc doc() {
		return new ClasspathDoc();
	}

	default ClasspathDoc doc(int index) {
		return new ClasspathDoc();
	}
}