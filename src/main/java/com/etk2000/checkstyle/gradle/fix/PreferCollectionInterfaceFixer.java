package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferCollectionInterfaceCheck;
import com.etk2000.checkstyle.ReflectionUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferCollectionInterfaceFixer implements CheckstyleFixer {
	private static final String IMPORT_PREFIX = "import ";
	private static final String PACKAGE_PREFIX = "package ";
	private static final String STATIC_IMPORT_PREFIX = "import static ";

	@CheckReturnValue
	@Nonnull
	private static Set<String> collectImports(@Nonnull List<String> lines) {
		final var imports = new HashSet<String>();
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var trimmed = JavaLineScanner.stripCommentsAndStrings(line, state).strip();
			state = JavaLineScanner.stateAfter(line, state);
			if (trimmed.startsWith(IMPORT_PREFIX) && !trimmed.startsWith(STATIC_IMPORT_PREFIX) && trimmed.endsWith(";"))
				imports.add(trimmed.substring(IMPORT_PREFIX.length(), trimmed.length() - 1).strip());
		}
		return imports;
	}

	@CheckReturnValue
	@Nullable
	private static String findPackageName(@Nonnull List<String> lines) {
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var trimmed = JavaLineScanner.stripCommentsAndStrings(line, state).strip();
			state = JavaLineScanner.stateAfter(line, state);
			if (trimmed.startsWith(PACKAGE_PREFIX) && trimmed.endsWith(";"))
				return trimmed.substring(PACKAGE_PREFIX.length(), trimmed.length() - 1).strip();
		}
		return null;
	}

	/**
	 * The type name ending at {@code end}, including any dotted qualifier preceding the simple name
	 * at {@code column}. The backward scan walks whole code points, so a supplementary identifier
	 * character inside a qualifier segment cannot truncate the name at the surrogate pair.
	 */
	@CheckReturnValue
	@Nonnull
	private static String qualifiedNameEndingAt(@Nonnull String line, int column, int end) {
		return line.substring(LineText.qualifiedNameStart(line, column), end);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// a buffer another fixer left unparseable answers "no pair" indistinguishably from a genuine
		// non-record position, and taking the line-local path there would rewrite one half alone
		final var root = FixerAst.parseOrNull(lines);
		if (root == null) {
			// masked, so the keyword's spelling inside a comment or literal does not refuse every
			// fix in a file that declares no record at all
			for (var text : FixerAst.maskAll(lines)) {
				if (LineText.indexOfWord(text, "record") >= 0)
					return new SkipResult(SkipMessages.RECORD_PAIR_HALF);
			}
		}
		else {
			final var pair = PreferCollectionInterfaceCheck.recordTypePairAt(root, lines, lineIndex, column);
			if (pair != null)
				return fixRecordPair(lines, pair);

			// the locator cannot span every shape it recognises (an array-typed accessor, say), and
			// the line-local path below would rewrite that half on its own
			if (PreferCollectionInterfaceCheck.isRecordPairPosition(root, lineIndex, column))
				return new SkipResult(SkipMessages.RECORD_PAIR_HALF);

			// a sibling fixer that changed the line count leaves this violation pointing at text it
			// never described, and the scan below would happily rewrite whatever sits there now
			if (!PreferCollectionInterfaceCheck.namesATypeIdentifierAt(root, lineIndex, column))
				return new SkipResult(SkipMessages.COLLECTION_INTERFACE_STALE);
		}

		final var line = lines.get(lineIndex);
		// the reported column counts code points while every scan below indexes chars, so a
		// supplementary character earlier on the line would shift the window off the type
		final var charColumn = LineText.charIndexOfColumn(line, column);
		if (charColumn < 0 || charColumn >= line.length())
			return null;

		final var end = LineText.identEnd(line, charColumn);
		if (end <= charColumn)
			return null;

		// the column names no identifier START, so it drifted into the middle of one, where the
		// backward walk below would silently re-anchor on a name the check never reported
		if (LineText.identStart(line, charColumn) != charColumn)
			return new SkipResult(SkipMessages.COLLECTION_INTERFACE_STALE);

		final var fullName = qualifiedNameEndingAt(line, charColumn, end);
		final String iface;
		if (fullName.indexOf('.') >= 0)
			iface = ReflectionUtil.findCollectionInterface(fullName);
		else {
			final var fqcn = ReflectionUtil.resolveClassName(fullName, findPackageName(lines), collectImports(lines));
			iface = fqcn == null ? null : ReflectionUtil.findCollectionInterface(fqcn);
		}
		if (iface == null)
			return new SkipResult(SkipMessages.COLLECTION_INTERFACE_SKIP);

		final var replaceStart = end - fullName.length();
		final var fixed = line.substring(0, replaceStart) + iface + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed), Set.of(PreferCollectionInterfaceCheck.COLLECTION_PACKAGE + iface));
	}

	/**
	 * Rewrites a record component and its explicit accessor as one edit. The accessor half defers
	 * rather than fixing, so the pair can never be half-applied whatever order the two violations
	 * arrive in.
	 */
	@CheckReturnValue
	@Nonnull
	private FixAttempt fixRecordPair(
			@Nonnull List<String> lines,
			@Nonnull PreferCollectionInterfaceCheck.RecordTypePair pair
	) {
		if (pair.accessorPresent() && pair.accessor() == null)
			return new SkipResult(SkipMessages.RECORD_PAIR_HALF);

		final var spans = new ArrayList<PreferCollectionInterfaceCheck.TypeNameSpan>();
		spans.add(pair.component());
		if (pair.accessor() != null)
			spans.add(pair.accessor());

		String iface = null;
		for (var span : spans) {
			if (!spanIsIntact(lines, span))
				return new SkipResult(SkipMessages.RECORD_PAIR_HALF);

			final var resolved = resolvedInterface(lines, span);
			if (resolved == null || (iface != null && !iface.equals(resolved)))
				return new SkipResult(SkipMessages.RECORD_PAIR_HALF);

			iface = resolved;
		}
		if (!pair.atComponent())
			return new SkipResult(SkipMessages.RECORD_PAIR_DEFERRED);

		final var spanStart = pair.component().startLine();
		final var spanEnd = spans.stream().mapToInt(PreferCollectionInterfaceCheck.TypeNameSpan::endLine).max().orElse(spanStart);
		final var replacement = new ArrayList<>(lines.subList(spanStart, spanEnd + 1));
		// later span first, so the earlier one's offsets are still valid when it is spliced
		for (var i = spans.size() - 1; i >= 0; --i) {
			final var span = spans.get(i);
			final var head = replacement.get(span.startLine() - spanStart).substring(0, span.startColumn());
			final var tail = replacement.get(span.endLine() - spanStart).substring(span.endColumn());
			replacement.subList(span.startLine() - spanStart, span.endLine() - spanStart + 1).clear();
			replacement.add(span.startLine() - spanStart, head + iface + tail);
		}
		return new FixResult(
				spanStart,
				spanEnd,
				replacement,
				Set.of(PreferCollectionInterfaceCheck.COLLECTION_PACKAGE + iface)
		);
	}

	/**
	 * The interface {@code span} would be rewritten to, or null when it does not name a concrete
	 * collection. The name comes from the AST, so a qualifier broken across lines still resolves.
	 */
	@CheckReturnValue
	@Nullable
	private String resolvedInterface(@Nonnull List<String> lines, @Nonnull PreferCollectionInterfaceCheck.TypeNameSpan span) {
		if (span.spelling().indexOf('.') >= 0)
			return ReflectionUtil.findCollectionInterface(span.spelling());

		final var fqcn = ReflectionUtil.resolveClassName(span.spelling(), findPackageName(lines), collectImports(lines));
		return fqcn == null ? null : ReflectionUtil.findCollectionInterface(fqcn);
	}

	/**
	 * Whether the source between {@code span}'s ends is the spelled name and nothing else. A
	 * comment or anything other than whitespace inside a qualified name would be deleted by a
	 * splice that assumes otherwise.
	 */
	@CheckReturnValue
	private boolean spanIsIntact(@Nonnull List<String> lines, @Nonnull PreferCollectionInterfaceCheck.TypeNameSpan span) {
		final var text = new StringBuilder();
		for (var i = span.startLine(); i <= span.endLine(); ++i) {
			final var line = lines.get(i);
			final var from = i == span.startLine() ? span.startColumn() : 0;
			final var to = i == span.endLine() ? span.endColumn() : line.length();
			if (from < 0 || to < 0 || from > line.length() || to > line.length() || from > to)
				return false;

			text.append(line, from, to);
		}
		return text.toString().replaceAll("\\s", "").equals(span.spelling());
	}
}