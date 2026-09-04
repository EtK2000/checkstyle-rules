package com.etk2000.checkstyle.testhelpers;

/**
 * Test helper declaring member types named after {@code java.lang} types (and one testhelpers
 * class) in every kind, visibility and static-ness, so a static import of it brings in only some.
 */
public class ClasspathMemberTypes {
	static class Byte {}

	private static class Character {}

	public static class ClasspathScopeBase {}

	public @interface Deprecated {}

	public class Double {}

	public static class Exception {}

	public record Integer() {}

	public interface Runnable {}

	protected static class Short {}
}