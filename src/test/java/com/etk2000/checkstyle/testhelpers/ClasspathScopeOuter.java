package com.etk2000.checkstyle.testhelpers;

/** Test helper for a nested classpath class, as a supertype and as a static-import owner. */
public class ClasspathScopeOuter {
	public static class Inner {
		public static final Object Runtime = null;

		protected final Object Thread = null;
	}
}