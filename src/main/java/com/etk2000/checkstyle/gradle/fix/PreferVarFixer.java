package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferVarCheck;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferVarFixer implements CheckstyleFixer {
	private record VarConversion(@Nullable String line, @Nullable String skipReason) {}

	private static final VarConversion UNRECOGNIZED = new VarConversion(null, null);

	/**
	 * Whether the token at {@code pos} is the keyword {@code var}. Any non-identifier may follow
	 * it: a tab or a block comment separates the type from the name just as a space does, and
	 * reading those as an ordinary type name rewrites {@code var} to itself.
	 */
	@CheckReturnValue
	private static boolean declaresVarAt(@Nonnull String line, int pos) {
		if (!line.startsWith("var", pos))
			return false;

		final var after = pos + 3;
		return after < line.length() && !Character.isJavaIdentifierPart(line.charAt(after));
	}

	@CheckReturnValue
	private static boolean emptyArgumentsFollow(@Nonnull String line, int from) {
		final var open = skipToCode(line, from);
		if (open < 0 || open >= line.length() || line.charAt(open) != '(')
			return false;
		final var close = skipToCode(line, open + 1);
		return close >= 0 && close < line.length() && line.charAt(close) == ')';
	}

	@CheckReturnValue
	private static boolean endsWithOpenParen(@Nonnull String line, @Nonnull String maskedLine) {
		final var stripped = line.stripTrailing();
		if (stripped.isEmpty() || stripped.charAt(stripped.length() - 1) != '(')
			return false;
		// the '(' must also be the last code on the masked line: ending with '(' in the raw
		// text alone is satisfied by a comment whose own text ends in one (`// ... (`), and
		// joining onto that line would move the declaration inside the comment
		final var maskedTail = maskedLine.stripTrailing();
		if (maskedTail.isEmpty() || maskedTail.charAt(maskedTail.length() - 1) != '(')
			return false;
		return !stripped.stripLeading().startsWith("@");
	}

	@CheckReturnValue
	private static int findAssignmentEquals(@Nonnull String scan, int from) {
		var parenDepth = 0;
		for (var i = from; i < scan.length(); ++i) {
			final var ch = scan.charAt(i);
			if (ch == '(')
				++parenDepth;
			// the enclosing group closed, so everything after it belongs to an outer construct: without
			// this the scan leaves a for-each or try-with-resources head and takes an assignment in the
			// body for the declaration's own
			else if (ch == ')') {
				if (--parenDepth < 0)
					return -1;
			}
			else if (ch == '=' && parenDepth == 0 && i + 1 < scan.length() && scan.charAt(i + 1) != '=')
				return i;
		}
		return -1;
	}

	/**
	 * Fixes explicit array initializers: converts {@code var x = new Type[]{...}}
	 * to {@code Type[] x = {...}}, or removes {@code new Type[]} from
	 * {@code Type[] x = new Type[]{...}}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixExplicitArrayInit(@Nonnull String line, @Nonnull String masked, int column) {
		final var eqIdx = findAssignmentEquals(masked, column);
		if (eqIdx < 0)
			return null;

		// searched in the mask so `new ` occurring inside a string literal or a comment is
		// never taken for a real constructor call
		final var newIdx = masked.indexOf("new ", eqIdx + 1);
		if (newIdx < 0)
			return null;

		final var betweenEqAndNew = line.substring(eqIdx + 1, newIdx);
		if (!betweenEqAndNew.isBlank())
			return null;

		var pos = newIdx + 4;
		final var typeStart = pos;

		pos = LineText.qualifiedNameEnd(line, pos);

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

		var bracketCount = 0;
		while (pos + 1 < line.length() && line.charAt(pos) == '[' && line.charAt(pos + 1) == ']') {
			pos += 2;
			++bracketCount;
		}
		if (bracketCount == 0)
			return null;

		final var arrayType = line.substring(typeStart, pos);

		// searched in the mask so a `{` inside a comment between the `[]` and the real
		// initializer is not taken for it, and refused outright when anything but whitespace
		// sits between them: that text would be spliced away with the `new Type[]`
		final var braceIdx = masked.indexOf('{', pos);
		if (braceIdx < 0 || !line.substring(pos, braceIdx).isBlank())
			return null;

		// read from the mask so an annotation argument's paren inside a string or char literal
		// (`@SuppressWarnings("(")`) cannot desynchronise the depth count and run the scan off the
		// end of the line, and so `var ` inside a string is never taken for the declared type
		final var declStart = skipModifiers(masked, column);
		if (declaresVarAt(masked, declStart)) {
			return line.substring(0, declStart) + arrayType + line.substring(declStart + 3, eqIdx + 1)
					+ " " + line.substring(braceIdx);
		}

		// the `{...}` shorthand is legal only where the declared type is itself an array, so a
		// declaration this could not read keeps its `new Type[]`: `var x = {1}` does not compile
		return masked.substring(Math.min(declStart, eqIdx), eqIdx).contains("[]")
				? line.substring(0, eqIdx + 1) + " " + line.substring(braceIdx)
				: null;
	}

	/**
	 * Replaces {@code <Object>} (or {@code <Object, Object>}, etc.) with
	 * {@code <>} on a {@code new} expression when the declared type is
	 * {@code var}. With {@code var}, the diamond operator infers
	 * {@code Object} by default, so explicit type arguments are redundant.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixRedundantObjectTypeArgs(@Nonnull String line, @Nonnull String masked, int column) {
		final var eqIdx = findAssignmentEquals(masked, column);
		if (eqIdx < 0)
			return null;

		// anchored on the reported declaration: matching any `var ` on the line would pair the
		// first statement's declaration with a second statement's `new`
		if (!declaresVarAt(line, skipModifiers(line, column)))
			return null;

		// searched in the mask so `new ` occurring inside a string literal or a comment is
		// never taken for a real constructor call
		final var newIdx = masked.indexOf("new ", eqIdx + 1);
		if (newIdx < 0)
			return null;

		var pos = newIdx + 4;
		pos = LineText.qualifiedNameEnd(line, pos);

		if (pos >= line.length() || line.charAt(pos) != '<')
			return null;

		final var angleStart = pos;

		var depth = 1;
		++pos;
		while (pos < line.length() && depth > 0) {
			if (line.charAt(pos) == '<')
				++depth;
			else if (line.charAt(pos) == '>')
				--depth;
			++pos;
		}
		if (depth != 0)
			return null;

		if (!isAllObjectTypeArgs(line.substring(angleStart + 1, pos - 1)))
			return null;

		return line.substring(0, angleStart + 1) + line.substring(pos - 1);
	}

	@CheckReturnValue
	@Nonnull
	private static VarConversion fixTypeToVar(
			@Nonnull List<String> masked,
			@Nonnull String line,
			int lineIndex,
			int column,
			boolean textMultiVarBail,
			boolean spliceIntoDiamonds
	) {
		var pos = skipModifiers(line, column);

		// PreferVarCheck reports the TYPE node for a for-each variable and a try-with-resources
		// resource, and a qualified type's TYPE node carries the column of an inner '.', so walk
		// back over the leading qualified name to replace the whole type, not just a suffix.
		// guarded on '.' so a column landing inside an identifier is never swept into the
		// replacement
		if (pos < line.length() && line.charAt(pos) == '.')
			pos = LineText.qualifiedNameStart(line, pos);

		if (declaresVarAt(line, pos))
			return UNRECOGNIZED;

		final var typeStart = pos;

		// a stale column (a sibling fixer shifted this line earlier in the pass) can land inside
		// an identifier, where replacing from there leaves the token's head behind (`retuvar x`)
		if (typeStart > 0 && Character.isJavaIdentifierPart(line.codePointBefore(typeStart)))
			return UNRECOGNIZED;

		pos = LineText.qualifiedNameEnd(line, pos);
		if (pos == typeStart)
			return UNRECOGNIZED;

		// the type has to be code. `masked` blanks string, char, text-block and comment content,
		// so a run that survives there identically is real source; a stale column inside a
		// literal would otherwise splice `var` into that literal's text
		if (!masked.get(lineIndex).regionMatches(typeStart, line, typeStart, pos - typeStart))
			return UNRECOGNIZED;

		var typeArgs = "";
		if (pos < line.length() && line.charAt(pos) == '<') {
			final var argsStart = pos;
			var depth = 1;
			++pos;
			while (pos < line.length() && depth > 0) {
				if (line.charAt(pos) == '<')
					++depth;
				else if (line.charAt(pos) == '>')
					--depth;
				++pos;
			}
			// a generic type wrapped onto the next line never closes here, and consuming to
			// end-of-line would emit `var` alone and delete the rest of the declaration
			if (depth != 0)
				return new VarConversion(null, SkipMessages.PREFER_VAR_SKIP_WRAPPED_TYPE_ARGUMENTS);
			typeArgs = line.substring(argsStart, pos);
		}

		while (pos + 1 < line.length() && line.charAt(pos) == '[' && line.charAt(pos + 1) == ']')
			pos += 2;

		// confirm a `<type> <name>` shape: a stale column (from another fixer shifting the
		// line in the full pipeline) that lands on a '.' inside a non-type dotted expression
		// must not be spliced as if it were a declaration type. the variable name follows
		// the type, possibly after whitespace/comments, or on the next line; only a
		// same-line non-identifier token (e.g. ')' from a stale column inside foo(a.b))
		// means this isn't a declaration, so bail then
		final var nameStart = skipToCode(line, pos);
		if (nameStart < 0)
			return UNRECOGNIZED;
		if (nameStart < line.length()) {
			if (!Character.isJavaIdentifierStart(line.codePointAt(nameStart)))
				return UNRECOGNIZED;

			// the token after the name is what separates a declaration from a statement that
			// merely looks like one (`return someValue;`): only an assignment, a for-each ':'
			// or a C-style '[' can follow a declarator's name
			final var afterName = skipToCode(line, LineText.identEnd(line, nameStart));
			if (afterName < 0)
				return UNRECOGNIZED;
			if (afterName < line.length()) {
				final var ch = line.charAt(afterName);
				// no '[': a C-style declarator (`String x[] = ...`) is a real declaration, but
				// `var x[]` is not legal Java, so it cannot be converted at all
				final var assignment = ch == '=' && (afterName + 1 >= line.length() || line.charAt(afterName + 1) != '=');
				if (!assignment && ch != ':')
					return UNRECOGNIZED;
			}
		}

		// Scan the masked source (literal/comment content blanked, positions preserved) so
		// commas/colons there aren't counted. A top-level ':' ends the scan only as a for-each
		// colon; a ternary ':' (matched by an earlier '?') must not stop the search, else a
		// multi-var like `T x = c ? a : b, y = d;` slips past the comma bail. The scan follows
		// the declaration onto continuation lines, because whether the declared arguments are
		// load-bearing depends on a diamond that may sit below the reported line.
		final var diamonds = new ArrayList<Integer>();
		var parenDepth = 0;
		var braceDepth = 0;
		var ternaryDepth = 0;
		var nestedDiamond = false;
		var diamondBelow = false;
		var terminated = false;
		scan:
		for (var scanLineIndex = lineIndex; scanLineIndex < masked.size(); ++scanLineIndex) {
			final var scan = masked.get(scanLineIndex);
			for (var scanPos = scanLineIndex == lineIndex ? pos : 0; scanPos < scan.length(); ++scanPos) {
				final var ch = scan.charAt(scanPos);
				if (ch == '(')
					++parenDepth;
				else if (ch == ')') {
					// the declaration's own closing paren (a try-with-resources head or a
					// for-each clause) ends it just as a ';' does
					if (--parenDepth < 0) {
						terminated = true;
						break scan;
					}
				}
				else if (ch == '{')
					++braceDepth;
				else if (ch == '}')
					--braceDepth;
				else if (ch == '<' && scanPos + 1 < scan.length() && scan.charAt(scanPos + 1) == '>') {
					if (parenDepth > 0)
						nestedDiamond = true;
					// a diamond nested in parens belongs to the chain, not to the declaration, so the
					// declared arguments are dropped rather than spliced into it
					else if (spliceIntoDiamonds) {
						if (scanLineIndex == lineIndex)
							diamonds.add(scanPos);
						else
							diamondBelow = true;
					}
				}
				// a switch-expression body or a braced lambda holds its own ';' and ':'
				// separators, none of which end the declaration
				else if (braceDepth > 0 || parenDepth != 0)
					continue;
				else if (ch == '?')
					++ternaryDepth;
				// only reached when the AST was unavailable: this cannot tell a declarator ','
				// from one separating explicit type arguments, so it over-refuses
				else if (ch == ',') {
					if (textMultiVarBail)
						return UNRECOGNIZED;
				}
				else if (ch == ';') {
					terminated = true;
					break scan;
				}
				else if (ch == ':') {
					if (ternaryDepth == 0) {
						terminated = true;
						break scan;
					}
					--ternaryDepth;
				}
			}
		}

		// The declared arguments are the only thing pinning a diamond's type, so they move onto
		// the constructor rather than being dropped along with the type.
		if (!typeArgs.isEmpty()) {
			// without an AST the arguments' target cannot be verified: splicing them into a diamond
			// that belongs to a chain receiver (`new Holder<>().names()`) retypes that receiver, and
			// dropping them instead rebinds the variable to the constructor's own inference
			if (textMultiVarBail)
				return new VarConversion(null, SkipMessages.PREFER_VAR_SKIP_UNREACHABLE_DIAMOND);

			// the only diamond belongs to a nested call, where the declared arguments do not
			// apply; retyping it would change that call's inferred type
			if (nestedDiamond && diamonds.isEmpty())
				return new VarConversion(null, SkipMessages.PREFER_VAR_SKIP_UNREACHABLE_DIAMOND);
			// a diamond on a continuation line would need a multi-line edit to receive them
			if (diamondBelow)
				return new VarConversion(null, SkipMessages.PREFER_VAR_SKIP_UNREACHABLE_DIAMOND);
			// the declaration never ends, so the scan cannot prove no diamond follows
			if (!terminated)
				return new VarConversion(null, SkipMessages.PREFER_VAR_SKIP_UNREACHABLE_DIAMOND);
		}

		if (typeArgs.isEmpty() || diamonds.isEmpty())
			return new VarConversion(line.substring(0, typeStart) + "var" + line.substring(pos), null);

		// dropping all-`Object` arguments only holds where every diamond is unconstrained: one
		// carrying constructor arguments infers from them instead, so `List<Object> l =
		// new ArrayList<>(strings)` would silently rebind to ArrayList<String>
		if (isAllObjectTypeArgs(typeArgs.substring(1, typeArgs.length() - 1))
				&& diamonds.stream().allMatch(diamond -> emptyArgumentsFollow(line, diamond + 2)))
			return new VarConversion(line.substring(0, typeStart) + "var" + line.substring(pos), null);

		// right-to-left so each splice leaves the earlier indices valid; the type itself is
		// replaced last because it starts before every diamond. Both arms of a ternary or switch
		// initializer receive the arguments, since either may end up as the variable's value
		final var fixed = new StringBuilder(line);
		for (var i = diamonds.size() - 1; i >= 0; --i)
			fixed.replace(diamonds.get(i), diamonds.get(i) + 2, typeArgs);
		return new VarConversion(fixed.replace(typeStart, pos, "var").toString(), null);
	}

	/**
	 * Whether every comma-separated type argument in {@code content} (the text between the
	 * angle brackets) is a bare {@code Object}. Empty content is not, so an existing
	 * {@code <>} does not qualify.
	 */
	@CheckReturnValue
	private static boolean isAllObjectTypeArgs(@Nonnull String content) {
		if (content.isEmpty())
			return false;
		for (var part : content.split(",", -1)) {
			final var trimmed = part.trim();
			if (!"Object".equals(trimmed) && !"java.lang.Object".equals(trimmed))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isAnnotationOrFinalOnly(@Nonnull String line) {
		final var stripped = line.strip();
		if (stripped.isEmpty())
			return false;
		final var parsed = AnnotationFixerUtil.parseAnnotations(stripped);
		if (parsed.remaining().isEmpty())
			return !parsed.annotations().isEmpty();
		return "final".equals(parsed.remaining());
	}

	@CheckReturnValue
	private static boolean isVarDeclaredAt(@Nonnull String line, int column) {
		if (column < 0 || column >= line.length())
			return false;
		return declaresVarAt(line, skipModifiers(line, column));
	}

	@CheckReturnValue
	private static int skipModifiers(@Nonnull String line, int column) {
		var pos = column;

		while (pos < line.length() && line.charAt(pos) == '@') {
			pos = LineText.qualifiedNameEnd(line, pos + 1);
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
			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;
		}

		if (line.startsWith("final ", pos))
			pos += 6;

		return pos;
	}

	/**
	 * The index of the first code character at or after {@code from}, skipping whitespace and
	 * comments. Returns {@code line.length()} when only whitespace and comments follow, or
	 * {@code -1} when a block comment opens here and does not close on this line.
	 */
	@CheckReturnValue
	private static int skipToCode(@Nonnull String line, int from) {
		var at = from;
		while (at < line.length()) {
			final var ch = line.charAt(at);
			if (Character.isWhitespace(ch))
				++at;
			else if (ch == '/' && at + 1 < line.length() && line.charAt(at + 1) == '*') {
				final var close = line.indexOf("*/", at + 2);
				if (close < 0)
					return -1;
				at = close + 2;
			}
			else if (ch == '/' && at + 1 < line.length() && line.charAt(at + 1) == '/')
				return line.length();
			else
				return at;
		}
		return at;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		// the reported column counts code points while every scan below indexes chars, so a
		// supplementary character earlier on the line would otherwise shift the splice into
		// the middle of an identifier and emit a mangled type
		final var charColumn = LineText.charIndexOfColumn(line, column);
		// a column that no longer names a position on this line (a sibling fixer shifted the
		// line earlier in the pass) cannot identify the reported violation, and the -1 LineText
		// returns for it would throw inside the scans below
		if (charColumn < 0 || charColumn >= line.length())
			return new SkipResult(SkipMessages.PREFER_VAR_SKIP_STALE_COLUMN);

		// one mask per fix, threaded into every helper: the per-line variant seeds
		// LexerState.NONE, which misreads a line continuing a text block
		final var maskedLines = JavaLineScanner.maskAll(lines);
		final var maskedLine = maskedLines.get(lineIndex);

		// only an explicit false refuses, so an unparseable buffer still runs
		if (Boolean.FALSE.equals(FixerAst.withAst(
				lines, root -> PreferVarCheck.isConvertibleDeclarationAt(root, lineIndex, column)
		)))
			return new SkipResult(SkipMessages.PREFER_VAR_SKIP_UNRECOGNIZED);

		final var arrayResult = fixExplicitArrayInit(line, maskedLine, charColumn);
		if (arrayResult != null)
			return new FixResult(lineIndex, lineIndex, List.of(arrayResult));

		final var diamondResult = fixRedundantObjectTypeArgs(line, maskedLine, charColumn);
		if (diamondResult != null)
			return new FixResult(lineIndex, lineIndex, List.of(diamondResult));

		// the array-init path declined, so its initializer spans lines. Falling through would
		// apply the unrelated type-to-`var` rewrite, leaving the reported violation standing on
		// a declaration the fixer can no longer recognise
		if (Boolean.TRUE.equals(FixerAst.withAst(
				lines, root -> PreferVarCheck.isExplicitArrayInitAt(root, lineIndex, column)
		)))
			return new SkipResult(SkipMessages.PREFER_VAR_SKIP_SPLIT_ARRAY_INITIALIZER);

		// the check's own predicate decides multi-var; the text scan can only guess
		final var multiVar = FixerAst.withAst(
				lines, root -> PreferVarCheck.isMultiVarDeclarationAt(root, lineIndex, column)
		);
		if (Boolean.TRUE.equals(multiVar))
			return new SkipResult(SkipMessages.PREFER_VAR_SKIP);

		final var spliceIntoDiamonds = !Boolean.FALSE.equals(FixerAst.withAst(
				lines, root -> PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, lineIndex, column)
		));
		final var conversion = fixTypeToVar(
				maskedLines,
				line,
				lineIndex,
				charColumn,
				multiVar == null,
				spliceIntoDiamonds
		);
		if (conversion.skipReason() != null)
			return new SkipResult(conversion.skipReason());

		final var varResult = conversion.line();
		if (varResult != null) {
			var scan = lineIndex - 1;
			while (scan >= 0 && (lines.get(scan).isBlank() || isAnnotationOrFinalOnly(lines.get(scan))))
				--scan;
			if (scan >= 0 && scan < lineIndex && endsWithOpenParen(lines.get(scan), maskedLines.get(scan))) {
				final var joined = new StringBuilder(lines.get(scan).stripTrailing());
				var separator = "";
				for (var i = scan + 1; i < lineIndex; ++i) {
					final var content = lines.get(i).strip();
					if (content.isEmpty())
						continue;
					joined.append(separator).append(content);
					separator = " ";
				}
				joined.append(separator).append(varResult.stripLeading());
				return new FixResult(scan, lineIndex, List.of(joined.toString()));
			}
			return new FixResult(lineIndex, lineIndex, List.of(varResult));
		}

		if (isVarDeclaredAt(line, charColumn))
			return new SkipResult(SkipMessages.PREFER_VAR_ALREADY_VAR);
		return new SkipResult(SkipMessages.PREFER_VAR_SKIP_UNRECOGNIZED);
	}
}