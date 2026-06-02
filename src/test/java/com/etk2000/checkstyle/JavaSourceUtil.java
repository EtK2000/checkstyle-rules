package com.etk2000.checkstyle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class JavaSourceUtil {
	@CheckReturnValue
	@Nonnull
	public static String stripJavaComments(@Nonnull String source) {
		final var out = new StringBuilder(source.length());
		final var len = source.length();
		var i = 0;
		while (i < len) {
			final var c = source.charAt(i);
			if (c == '"' && i + 2 < len && source.charAt(i + 1) == '"' && source.charAt(i + 2) == '"') {
				out.append("\"\"\"");
				i += 3;
				while (i + 2 < len) {
					if (source.charAt(i) == '"' && source.charAt(i + 1) == '"' && source.charAt(i + 2) == '"') {
						out.append("\"\"\"");
						i += 3;
						break;
					}
					out.append(source.charAt(i));
					++i;
				}
				continue;
			}
			if (c == '"' || c == '\'') {
				out.append(c);
				++i;
				while (i < len) {
					final var sc = source.charAt(i);
					if (sc == '\\' && i + 1 < len) {
						out.append(sc);
						out.append(source.charAt(i + 1));
						i += 2;
						continue;
					}
					out.append(sc);
					++i;
					if (sc == c || sc == '\n')
						break;
				}
				continue;
			}
			if (c == '/' && i + 1 < len && source.charAt(i + 1) == '/') {
				i += 2;
				while (i < len && source.charAt(i) != '\n')
					++i;
				continue;
			}
			if (c == '/' && i + 1 < len && source.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < len && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/'))
					++i;
				if (i + 1 < len)
					i += 2;
				else
					i = len;
				out.append(' ');
				continue;
			}
			out.append(c);
			++i;
		}
		return out.toString();
	}

	@CheckReturnValue
	@Nonnull
	public static List<String> walkJavaSources(@Nonnull Path root) throws IOException {
		final var sources = new ArrayList<String>();
		try (var paths = Files.walk(root)) {
			for (var path : paths.filter(p -> p.toString().endsWith(".java")).toList())
				sources.add(Files.readString(path, StandardCharsets.UTF_8));
		}
		if (sources.isEmpty())
			throw new IllegalStateException("no Java sources found under " + root + " - check the test's working directory");
		return sources;
	}

	private JavaSourceUtil() {
	}
}