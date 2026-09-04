package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper for a classpath supertype whose inherited field {@code text} is a {@link ClasspathDoc}
 * rather than a {@code String}, so inside a subclass it hides an outer {@code String text}.
 */
public class ClasspathDocField {
	protected final ClasspathDoc text = new ClasspathDoc();
}