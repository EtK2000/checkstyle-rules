package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferVarFixer implements CheckstyleFixer {
	/**
	 * Fixes explicit array initializers: converts {@code var x = new Type[]{...}}
	 * to {@code Type[] x = {...}}, or removes {@code new Type[]} from
	 * {@code Type[] x = new Type[]{...}}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixExplicitArrayInit(@Nonnull String line) {
		final var eqIdx = line.indexOf('=');
		if (eqIdx < 0)
			return null;

		// find "new " after the "="
		final var newIdx = line.indexOf("new ", eqIdx + 1);
		if (newIdx < 0)
			return null;

		// extract array type: scan type name + generics + brackets
		var pos = newIdx + 4;
		final var typeStart = pos;

		// scan type name (identifier chars + dots for qualified names)
		while (pos < line.length() && (Character.isJavaIdentifierPart(line.charAt(pos)) || line.charAt(pos) == '.'))
			++pos;

		// scan generic parameters <...>
		if (pos < line.length() && line.charAt(pos) == '<') {
			var depth = 1;
			++pos;
			while (pos < line.length() && depth > 0) {
				if (line.charAt(pos) == '<')
					++depth;
				else if (line.charAt(pos) == '>')
					--depth;
				++pos;
			}
		}

		// scan array brackets []
		var bracketCount = 0;
		while (pos + 1 < line.length() && line.charAt(pos) == '[' && line.charAt(pos + 1) == ']') {
			pos += 2;
			++bracketCount;
		}
		if (bracketCount == 0)
			return null;

		final var arrayType = line.substring(typeStart, pos);

		// find the opening brace of the array init
		final var braceIdx = line.indexOf('{', pos);
		if (braceIdx < 0)
			return null;

		// check if declared type is var
		final var beforeEq = line.substring(0, eqIdx).stripTrailing();
		final var declEnd = beforeEq.length();
		// walk backwards past the variable name
		var nameStart = declEnd - 1;
		while (nameStart >= 0 && Character.isJavaIdentifierPart(line.charAt(nameStart)))
			--nameStart;
		++nameStart;

		// walk backwards past whitespace to find the declared type end
		var typeEnd = nameStart - 1;
		while (typeEnd >= 0 && Character.isWhitespace(line.charAt(typeEnd)))
			--typeEnd;
		++typeEnd;

		// find the declared type start (scan backwards past [], generics, identifiers, dots)
		var declTypeStart = typeEnd;
		// scan backwards past []
		while (declTypeStart >= 2 && line.charAt(declTypeStart - 1) == ']' && line.charAt(declTypeStart - 2) == '[')
			declTypeStart -= 2;
		// scan backwards past generics
		if (declTypeStart > 0 && line.charAt(declTypeStart - 1) == '>') {
			var depth = 1;
			declTypeStart -= 2;
			while (declTypeStart >= 0 && depth > 0) {
				if (line.charAt(declTypeStart) == '>')
					++depth;
				else if (line.charAt(declTypeStart) == '<')
					--depth;
				--declTypeStart;
			}
			++declTypeStart;
		}
		// scan backwards past type name
		while (declTypeStart > 0
				&& (Character.isJavaIdentifierPart(line.charAt(declTypeStart - 1))
				|| line.charAt(declTypeStart - 1) == '.'))
			--declTypeStart;

		final var declType = line.substring(declTypeStart, typeEnd);

		if ("var".equals(declType)) {
			// replace var with the array type, remove "new Type[]"
			return line.substring(0, declTypeStart) + arrayType + line.substring(typeEnd, eqIdx + 1)
					+ " " + line.substring(braceIdx);
		}
		// declared type already matches, just remove "new Type[]"
		return line.substring(0, eqIdx + 1) + " " + line.substring(braceIdx);
	}

	/**
	 * Replaces an explicit type with {@code var}, skipping annotations
	 * and the {@code final} keyword if present.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixTypeToVar(@Nonnull String line, int column) {
		if (column < 0 || column >= line.length())
			return null;

		var pos = column;

		// skip annotations: @Name, @Name(args), @pkg.Name, with whitespace
		while (pos < line.length() && line.charAt(pos) == '@') {
			++pos;
			// scan annotation name (identifier chars + dots)
			while (pos < line.length() && (Character.isJavaIdentifierPart(line.charAt(pos)) || line.charAt(pos) == '.'))
				++pos;
			// skip parenthesized arguments if present
			if (pos < line.length() && line.charAt(pos) == '(') {
				var depth = 1;
				++pos;
				while (pos < line.length() && depth > 0) {
					if (line.charAt(pos) == '(')
						++depth;
					else if (line.charAt(pos) == ')')
						--depth;
					++pos;
				}
			}
			// skip whitespace after annotation
			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;
		}

		// skip "final " keyword if present
		if (line.startsWith("final ", pos))
			pos += 6;

		// guard: already var
		if (line.startsWith("var ", pos))
			return null;

		final var typeStart = pos;

		// scan type token: identifier chars + dots (qualified names)
		while (pos < line.length() && (Character.isJavaIdentifierPart(line.charAt(pos)) || line.charAt(pos) == '.'))
			++pos;
		if (pos == typeStart)
			return null;

		// scan generic params <...>
		if (pos < line.length() && line.charAt(pos) == '<') {
			var depth = 1;
			++pos;
			while (pos < line.length() && depth > 0) {
				if (line.charAt(pos) == '<')
					++depth;
				else if (line.charAt(pos) == '>')
					--depth;
				++pos;
			}
		}

		// scan array brackets []
		while (pos + 1 < line.length() && line.charAt(pos) == '[' && line.charAt(pos + 1) == ']')
			pos += 2;

		// guard: multi-var declaration (comma at depth 0 before semicolon)
		var scanPos = pos;
		var parenDepth = 0;
		var inString = false;
		var inChar = false;
		while (scanPos < line.length()) {
			final var ch = line.charAt(scanPos);
			if (inString) {
				if (ch == '"' && line.charAt(scanPos - 1) != '\\')
					inString = false;
			}
			else if (inChar) {
				if (ch == '\'' && line.charAt(scanPos - 1) != '\\')
					inChar = false;
			}
			else if (ch == '"')
				inString = true;
			else if (ch == '\'')
				inChar = true;
			else if (ch == '(')
				++parenDepth;
			else if (ch == ')')
				--parenDepth;
			else if (ch == ',' && parenDepth == 0)
				return null;
			else if (ch == ';' || (ch == ':' && parenDepth == 0))
				break;
			++scanPos;
		}

		return line.substring(0, typeStart) + "var" + line.substring(pos);
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// try explicit array init path first (MSG_VAR_EXPLICIT_ARRAY)
		final var arrayResult = fixExplicitArrayInit(line);
		if (arrayResult != null)
			return new FixResult(lineIndex, lineIndex, List.of(arrayResult));

		// try type-to-var path (MSG_LOCAL, MSG_FOREACH, MSG_TRY)
		final var varResult = fixTypeToVar(line, column);
		if (varResult != null)
			return new FixResult(lineIndex, lineIndex, List.of(varResult));

		return null;
	}
}