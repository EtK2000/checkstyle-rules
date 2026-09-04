package com.etk2000.checkstyle.testhelpers;

/** Test helper for a classpath field named {@code Charset} whose type has a {@code forName} of its own. */
public class ClasspathCharsetHolder {
	public static final ClasspathForName Charset = new ClasspathForName();
}