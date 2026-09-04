package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper for a classpath supertype and static-import owner declaring a field named after a
 * {@code java.lang} type in every visibility, static and instance, so a subclass or importer in
 * another package sees only some of them.
 */
public class ClasspathScopeBase {
	public static final Object Boolean = null;
	static final Object Byte = null;
	private static final Object Character = null;
	protected static final Object Long = null;

	private final Object Double = null;
	public final Object Float = null;
	protected final Object Integer = null;
	final Object Short = null;
}