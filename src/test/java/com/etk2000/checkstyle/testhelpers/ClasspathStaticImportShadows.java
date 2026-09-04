package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper whose static members a single static import brings in under {@code java.lang}
 * names: a member type {@code Thread} declaring fields, and fields and a method that name no type.
 */
public class ClasspathStaticImportShadows {
	public static class Thread {
		public static class Nested {
			protected static final Object Long = null;
		}

		protected static final Object Integer = null;
		protected static final String StandardCharsets = "";
	}

	public static final Object Double = null;
	public static final Object Exception = null;

	public static Object Number() {
		return null;
	}
}