package com.etk2000.checkstyle.gradle.fix;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferCollectionInterfaceFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	private static String findCollectionInterface(@Nonnull Class<?> clazz) {
		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
			return null;

		if (List.class.isAssignableFrom(clazz) && Deque.class.isAssignableFrom(clazz))
			return null;

		// alphabetical, except Collection last (matches everything)
		if (Deque.class.isAssignableFrom(clazz))
			return "Deque";
		if (List.class.isAssignableFrom(clazz))
			return "List";
		if (Map.class.isAssignableFrom(clazz))
			return "Map";
		if (Queue.class.isAssignableFrom(clazz))
			return "Queue";
		if (Set.class.isAssignableFrom(clazz))
			return "Set";
		if (Collection.class.isAssignableFrom(clazz))
			return "Collection";
		return null;
	}

	@CheckReturnValue
	private static int findIdentEnd(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
			++i;
		return i;
	}

	@CheckReturnValue
	@Nonnull
	private static String resolveFromImports(@Nonnull List<String> lines, @Nonnull String typeName) {
		for (var line : lines) {
			if (line.startsWith("import ") && line.endsWith("." + typeName + ";"))
				return line.substring(7, line.length() - 1);
		}
		return "java.util." + typeName;
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column >= line.length())
			return null;

		final var end = findIdentEnd(line, column);
		if (end <= column)
			return null;

		final var typeName = line.substring(column, end);
		final var fqcn = resolveFromImports(lines, typeName);

		try {
			final var clazz = Class.forName(fqcn, false, getClass().getClassLoader());
			final var iface = findCollectionInterface(clazz);
			if (iface == null)
				return null;

			final var fixed = line.substring(0, column) + iface + line.substring(end);
			return new FixResult(lineIndex, lineIndex, List.of(fixed), Set.of("java.util." + iface));
		}
		catch (ClassNotFoundException | NoClassDefFoundError e) {
			return null;
		}
	}
}