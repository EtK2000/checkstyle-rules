package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper for a classpath type that is not {@code String} but declares {@code replaceAll} and
 * {@code strip}, with neither {@code replace} nor {@code isBlank}, so a rewrite to either does not
 * compile.
 */
public class ClasspathDoc {
	public boolean isEmpty() {
		return true;
	}

	public int length() {
		return 0;
	}

	public ClasspathDoc replaceAll(String regex, String replacement) {
		return this;
	}

	public ClasspathDoc strip() {
		return this;
	}
}