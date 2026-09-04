package com.etk2000.checkstyle.gradle.fix;

import static java.util.Objects.requireNonNull;

import com.etk2000.checkstyle.FieldInitializers;
import com.etk2000.checkstyle.FieldInitializers.Kind;
import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineColumns;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.SourceComments;
import com.etk2000.checkstyle.ast.DocComments;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for FieldSortingCheck violations. Sorts field declarations by chunk,
 * type, and name, and splits same-line enum constants. It never reorders enum
 * constants: code anywhere may depend on their declared order.
 *
 * <p>The rule (which field sorts where) is read from
 * {@link FieldSortingCheck}'s AST classifier and comparators, never re-derived
 * from regex.
 */
class FieldSortingFixer implements CheckstyleFixer {
	private record EnumEntry(
			@Nonnull String name,
			@Nonnull List<String> leadingLines,
			@Nonnull List<String> contentLines,
			@Nonnull String trailing
	) {}

	/**
	 * The rewritable extent of an enum's constant list. {@code endLine}/{@code endCol}
	 * sit just past its last token, which is the trailing {@code ;} or {@code ,} when
	 * one is present. A comma line of {@code 0} means that constant is not followed by
	 * one.
	 */
	private record EnumSpan(
			@Nonnull int[] commaLines,
			@Nonnull int[] commaCols,
			int endLine,
			int endCol,
			@Nonnull String terminal
	) {
		@CheckReturnValue
		int commaCol(int index) {
			return commaCols[index];
		}

		@CheckReturnValue
		int commaLine(int index) {
			return commaLines[index];
		}
	}

	/**
	 * One physical field declaration (all declarators of a multi-variable
	 * declaration folded into one entry). {@code run} is the index of the
	 * {@link FieldConsolidationFixer#runs run} it belongs to, or -1 when it joins none; only
	 * entries of one run are merged. {@code declPrefix} is the
	 * source text of the declaration line up to the first declarator name (indent +
	 * modifiers + type + trailing space). {@code declarators} are the
	 * {@link FieldConsolidationFixer#declaratorText} of each name, in {@code names} order.
	 *
	 * <p>{@code anonInit} is the first declarator's; {@code anonMixed} marks a
	 * declaration whose declarators disagree on it.
	 *
	 * <p>{@code kinds} are the {@link FieldInitializers#kindOf kinds} of {@code names}. {@code pins}
	 * names the earlier fields of other declarations this one must stay after
	 * ({@link FieldInitializers#pinnedAfter}), and {@code declaratorPins} the same per name,
	 * fields of its own declaration included.
	 *
	 * <p>{@code declLineOffset} is the index within {@code lines} of the line the
	 * declaration proper starts on (the first declarator name's line); the lines
	 * before it are the entry's stacked annotations and attached comments.
	 *
	 * <p>{@code detachedDirective} marks a declaration {@linkplain SourceComments#leadingDirectives
	 * led by a tool directive} above its {@code lines}, one trailing the line above or set apart by
	 * a blank line, which stays where it is when the fields around it move.
	 */
	private record FieldEntry(
			@Nonnull List<String> annotations,
			int run,
			@Nonnull String name,
			@Nonnull List<String> names,
			@Nonnull List<String> declarators,
			@Nonnull String sortType,
			@Nonnull List<List<String>> typeArgAnnotations,
			int chunk,
			boolean isStatic,
			boolean anonInit,
			boolean anonMixed,
			boolean hasInitializer,
			boolean cStyleArray,
			boolean sharesLine,
			@Nonnull String declPrefix,
			@Nonnull List<String> lines,
			@Nonnull Set<String> dependencies,
			@Nonnull Map<String, Set<String>> declaratorDependencies,
			@Nonnull List<Kind> kinds,
			@Nonnull Set<String> pins,
			@Nonnull Map<String, Set<String>> declaratorPins,
			int startLineIndex,
			int declLineOffset,
			boolean detachedDirective
	) {}

	// FieldSortingCheck's chunk for non-final fields; the two before it are the finals
	private static final int NON_FINAL_CHUNK = 2;
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			"^\\s*+(?:(?:@\\w++(?:\\([^()]*+(?:\\([^()]*+\\)[^()]*+)*+\\))?+\\s++)*+)"
					+ "(?:(?:public|private|protected|static|final|transient|volatile)\\s++)*+"
					+ "((?:boolean|byte|char|double|float|int|long|short|(?:\\w++\\.)*+[A-Z]\\w*+)(?:<[^>]*+>)?+(?:\\[\\])*+)"
					+ "\\s++(\\w++)"
	);
	private static final Pattern NEWLINE = Pattern.compile("\n");

	/**
	 * Index of the line that opens the block comment covering {@code from}, or
	 * -1 when that {@code /*} sits after code on its line.
	 */
	@CheckReturnValue
	private static int blockCommentOpener(
			@Nonnull List<String> lines,
			@Nonnull String[] stripped,
			@Nonnull LexerState[] entryState,
			int from
	) {
		var opener = from;
		while (opener > 0 && entryState[opener].inBlockComment())
			--opener;
		return entryState[opener].inBlockComment() || !isWholeLineComment(lines, stripped, opener) ? -1 : opener;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> buildDeclLines(@Nonnull String prefix, @Nonnull List<String> declarators, @Nonnull String suffix, @Nonnull String baseIndent) {
		return LineLength.wrapFieldList(prefix, declarators, suffix, baseIndent + "\t\t");
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> buildReplacement(@Nonnull List<EnumEntry> entries, @Nonnull String terminal) {
		final var replacement = new ArrayList<String>();
		for (var i = 0; i < entries.size(); ++i) {
			final var entry = entries.get(i);
			replacement.addAll(entry.leadingLines());
			for (var j = 0; j < entry.contentLines().size(); ++j) {
				var line = entry.contentLines().get(j);
				if (j == entry.contentLines().size() - 1) {
					line += i < entries.size() - 1 ? "," : terminal;
					if (!entry.trailing().isEmpty())
						line += " " + entry.trailing();
				}
				replacement.add(line);
			}
		}
		return replacement;
	}

	/**
	 * The merged declaration keeps the entry's lines above its declaration line as
	 * leading lines, so the declaration has to start on its last line: a wrapped
	 * initializer's head ({@code long delay = 60} over {@code * 1000;}) would otherwise
	 * be re-emitted as if it were an annotation. The merged line is rebuilt from the
	 * prefix and the declarators, each on one line, so a comment past the prefix would be
	 * lost.
	 */
	@CheckReturnValue
	private static boolean canConsolidate(@Nonnull FieldEntry entry) {
		final var declLine = entry.lines.getLast();
		for (var declarator : entry.declarators) {
			if (declarator.indexOf('\n') >= 0)
				return false;
		}
		return !entry.cStyleArray && entry.lines.size() <= 2
				&& entry.declLineOffset == entry.lines.size() - 1
				&& JavaLineScanner.firstCommentMarker(declLine.substring(entry.declPrefix.length()), JavaLineScanner.LexerState.NONE) < 0;
	}

	/**
	 * True when {@code candidate} can be folded into {@code base}'s declarator
	 * list: both belong to one run of {@link FieldConsolidationFixer#runs}, the declarations
	 * {@code FieldConsolidationCheck} would merge, so this fixer never merges what that one keeps
	 * apart, and the candidate fits on one line. The candidate's comments move above the
	 * merged declaration (see {@link #movedComments}); neither may carry a doc comment, which
	 * the merge would give to every name.
	 */
	@CheckReturnValue
	private static boolean canMergeWith(@Nonnull FieldEntry base, @Nonnull FieldEntry candidate) {
		return base.run >= 0
				&& base.run == candidate.run
				&& canConsolidate(candidate)
				&& candidate.dependencies.isEmpty()
				&& base.dependencies.isEmpty()
				&& !hasNonAnnotationLeadingLines(candidate)
				&& movedComments(base, candidate) != null
				&& !spreadsDocComment(base, candidate)
				&& !spreadsDocComment(candidate, base)
				&& !declLineHoldsDirective(base);
	}

	/**
	 * Converts an AST column (code points) to an index into {@code line}'s chars,
	 * clamped to the line so a position just past the last token still slices.
	 */
	@CheckReturnValue
	private static int charIndexOf(@Nonnull String line, int column) {
		return LineText.charIndexOfColumn(line, Math.clamp(column, 0, line.codePointCount(0, line.length())));
	}

	/** True when {@code sorted} lists no field of an earlier chunk after one of a later chunk. */
	@CheckReturnValue
	private static boolean chunksInOrder(@Nonnull List<FieldEntry> sorted) {
		for (var i = 1; i < sorted.size(); ++i) {
			if (sorted.get(i).chunk < sorted.get(i - 1).chunk)
				return false;
		}
		return true;
	}

	/**
	 * The indent to emit constants at: the existing indent of the first constant that
	 * already starts a line of its own, else one tab in from the enum's own line (the
	 * case where every constant only ever shared the header line).
	 */
	@CheckReturnValue
	@Nonnull
	private static String constantIndent(@Nonnull List<String> lines, @Nonnull List<DetailAST> consts, @Nonnull String enumIndent) {
		for (var cd : consts) {
			final var raw = lines.get(cd.getLineNo() - 1);
			final var head = raw.substring(0, Math.min(charIndexOf(raw, cd.getColumnNo()), raw.length()));
			if (head.isBlank())
				return head;
		}
		return enumIndent + "\t";
	}

	/** True when two of {@code consts}, an enum's constants in source order, have their names on one line. */
	@CheckReturnValue
	private static boolean constantsShareALine(@Nonnull List<DetailAST> consts) {
		for (var k = 1; k < consts.size(); ++k) {
			if (consts.get(k).findFirstToken(TokenTypes.IDENT).getLineNo() == consts.get(k - 1).findFirstToken(TokenTypes.IDENT).getLineNo())
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsFieldWord(@Nonnull String text, @Nonnull String word) {
		var idx = text.indexOf(word);
		while (idx >= 0) {
			final var before = idx == 0 || !Character.isJavaIdentifierPart(text.charAt(idx - 1));
			final var after = idx + word.length() >= text.length()
					|| !Character.isJavaIdentifierPart(text.charAt(idx + word.length()));
			if (before && after)
				return true;
			idx = text.indexOf(word, idx + 1);
		}
		return false;
	}

	/**
	 * True when a comment on {@code entry}'s declaration line is a
	 * {@link SourceComments#isDirective tool directive}, which a merge would stretch over the
	 * names it appends to that line.
	 */
	@CheckReturnValue
	private static boolean declLineHoldsDirective(@Nonnull FieldEntry entry) {
		for (var comment : SourceComments.scan(List.of(entry.lines.getLast()))) {
			if (SourceComments.isDirective(comment.text()))
				return true;
		}
		return false;
	}

	/**
	 * Position (1-based line, 0-based column) just after the last source
	 * character of {@code root}'s subtree.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] endPos(@Nonnull DetailAST root) {
		var line = -1;
		var col = -1;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (isSourceToken(node)) {
				final var l = node.getLineNo();
				// getColumnNo counts code points, so the token's length has to as well;
				// mixing in a char length drops a character per supplementary code point
				final var text = node.getText();
				final var c = node.getColumnNo() + text.codePointCount(0, text.length());
				if (l > line || (l == line && c > col)) {
					line = l;
					col = c;
				}
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return new int[]{line, col};
	}

	@CheckReturnValue
	@Nonnull
	private static EnumSpan enumSpan(@Nonnull DetailAST objBlock, @Nonnull List<DetailAST> consts) {
		final var commaLines = new int[consts.size()];
		final var commaCols = new int[consts.size()];
		var terminal = "";
		var index = -1;
		var endLine = consts.getFirst().getLineNo();
		var endCol = consts.getFirst().getColumnNo();
		for (var c = objBlock.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
				++index;
				final var end = endPos(c);
				endLine = end[0];
				endCol = end[1];
				continue;
			}
			if (index < 0)
				continue;
			if (c.getType() == TokenTypes.COMMA) {
				commaLines[index] = c.getLineNo();
				commaCols[index] = c.getColumnNo();
				endLine = c.getLineNo();
				endCol = c.getColumnNo() + 1;
				continue;
			}
			if (c.getType() == TokenTypes.SEMI) {
				terminal = ";";
				endLine = c.getLineNo();
				endCol = c.getColumnNo() + 1;
			}
			break;
		}
		return new EnumSpan(commaLines, commaCols, endLine, endCol, terminal);
	}

	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> fieldsOf(@Nonnull DetailAST objBlock) {
		final var fields = new ArrayList<DetailAST>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				fields.add(child);
		}
		return fields;
	}

	@Nullable
	private static FixAttempt fixFieldOrder(@Nonnull List<String> lines, @Nonnull DetailAST objBlock, int lineIndex) {
		final var stripped = precomputeStrippedLines(lines);
		final var entryState = precomputeLexerStates(lines);
		final var fields = parseFieldsFromAst(lines, stripped, entryState, objBlock);
		if (fields.isEmpty())
			return null;

		FieldEntry violation = null;
		for (var f : fields) {
			if (lineIndex >= f.startLineIndex && lineIndex < f.startLineIndex + f.lines.size()) {
				violation = f;
				break;
			}
		}
		if (violation == null)
			return null;
		final var violationStatic = violation.isStatic;

		final var group = new ArrayList<FieldEntry>();
		for (var f : fields) {
			if (f.isStatic == violationStatic)
				group.add(f);
		}

		// the rebuild moves an entry's whole lines, which would duplicate or carry off
		// whatever else shares them
		for (var f : group) {
			if (f.sharesLine)
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_SHARED_LINE);
		}

		final var readAtInit = initTimeForwardReads(objBlock);
		if (holdsInitTimeRead(group, readAtInit) && group.size() == 1)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_INIT_FORWARD_READ);
		if (group.size() == 1)
			return sortSingleEntry(lines, group.getFirst(), stripped);

		if (violationStatic) {
			for (var f : group) {
				if (!f.annotations.isEmpty())
					return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_STATIC_FIELD);
			}
		}

		final var multiVarSecondaryNames = new HashSet<String>();
		for (var f : fields) {
			for (var k = 1; k < f.names.size(); ++k)
				multiVarSecondaryNames.add(f.names.get(k));
		}

		final var spanStart = group.getFirst().startLineIndex;
		final var spanEnd = group.getLast().startLineIndex + group.getLast().lines.size() - 1;
		if (hasOrphanedCommentLine(lines, stripped, fields, spanStart, spanEnd))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ORPHANED_COMMENT);
		if (holdsRangeDirective(lines, spanStart, spanEnd))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_RANGE_DIRECTIVE);

		for (var f : group) {
			if (f.anonMixed)
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_ANON_MIXED);
		}

		final var nameDeps = new HashMap<String, Set<String>>();
		for (var f : group)
			nameDeps.putAll(f.declaratorDependencies);
		final var freePlainNames = freePlainNames(group, nameDeps);
		for (var f : group) {
			final var free = freePlainNames.contains(f.name);
			for (var name : f.names) {
				if (freePlainNames.contains(name) != free)
					return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_ANON_READER_MIXED);
			}
		}

		final var sorted = orderGroup(group, freePlainNames, true);
		if (sorted == null || !chunksInOrder(sorted)) {
			// the pins only keep source order, so when the group sorts without them they are the obstacle
			final var unpinned = orderGroup(group, freePlainNames, false);
			if (unpinned != null && chunksInOrder(unpinned))
				return new SkipResult(holdsInitTimeRead(group, readAtInit) ? SkipMessages.FIELD_SORTING_SKIP_INIT_FORWARD_READ : SkipMessages.FIELD_SORTING_SKIP_PINNED);
			return new SkipResult(unpinned == null ? unorderableReason(group, nameDeps) : SkipMessages.FIELD_SORTING_SKIP_DEPENDENCY_LATER_CHUNK);
		}
		if (holdsInitTimeRead(group, readAtInit))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_INIT_FORWARD_READ);
		if (movesDetachedDirective(group, sorted))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_DETACHED_DIRECTIVE);

		final var firstGroupLine = group.getFirst().startLineIndex;
		final var lastGroupLine = group.getLast().startLineIndex;
		for (var f : fields) {
			if (f.isStatic != violationStatic
					&& f.startLineIndex > firstGroupLine
					&& f.startLineIndex < lastGroupLine)
				return reorderInterleavedGroup(lines, group, sorted);
		}

		return rebuildContiguousGroup(lines, group, sorted, multiVarSecondaryNames);
	}

	/**
	 * Names of the plain fields in {@code group} that read no anonymous-initializer field
	 * of their chunk, directly or through other fields. Judged per declarator, as the
	 * check does.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> freePlainNames(@Nonnull List<FieldEntry> group, @Nonnull Map<String, Set<String>> nameDeps) {
		final var anonNamesByChunk = new HashMap<Integer, Set<String>>();
		for (var entry : group) {
			if (entry.anonInit())
				anonNamesByChunk.computeIfAbsent(entry.chunk(), chunk -> new HashSet<>()).addAll(entry.names());
		}

		final var freeNames = new HashSet<String>();
		for (var entry : group) {
			if (entry.anonInit())
				continue;
			final var anonNames = anonNamesByChunk.get(entry.chunk());
			for (var name : entry.names()) {
				if (anonNames == null) {
					freeNames.add(name);
					continue;
				}
				final var reads = FieldSortingCheck.transitiveDependencies(name, nameDeps);
				reads.retainAll(anonNames);
				if (reads.isEmpty())
					freeNames.add(name);
			}
		}
		return freeNames;
	}

	/**
	 * True when {@code varDef}'s declarator carries C-style array brackets after
	 * its name ({@code int y[]}). Checkstyle hangs an {@code ARRAY_DECLARATOR}
	 * under the {@code TYPE} for either bracket style, and only the C-style one
	 * sits after the declarator's identifier, so the two are told apart by
	 * position. The subtree is walked rather than only {@code TYPE}'s direct
	 * children so a nested declarator ({@code int y[][]}) counts however
	 * checkstyle chooses to nest it.
	 */
	@CheckReturnValue
	private static boolean hasCStyleArray(@Nonnull DetailAST varDef) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(varDef.findFirstToken(TokenTypes.TYPE));
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.ARRAY_DECLARATOR
					&& (node.getLineNo() > ident.getLineNo()
							|| (node.getLineNo() == ident.getLineNo() && node.getColumnNo() > ident.getColumnNo())))
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * True when {@code line}, entered in {@code state}, holds a
	 * {@linkplain DocComments#isDocComment doc comment}. Whether it documents the field is not
	 * asked: one anywhere on the line counts, since a line cannot tell, and the re-parse would
	 * refuse a merge that spread it.
	 */
	@CheckReturnValue
	private static boolean hasDocComment(@Nonnull String line, @Nonnull LexerState state) {
		var from = 0;
		var current = state;
		while (from < line.length()) {
			final var marker = JavaLineScanner.firstCommentMarker(line.substring(from), current);
			if (marker < 0)
				return false;
			final var at = from + marker;
			// a comment carried in from an earlier line was judged on the line that opened it
			final var carried = current.inBlockComment() && marker == 0;
			if (!carried && DocComments.isDocComment(line.substring(at)))
				return true;
			if (!carried && line.startsWith("//", at))
				return false;
			final var close = line.indexOf("*/", carried ? at : at + 2);
			if (close < 0)
				return false;
			from = close + 2;
			current = LexerState.NONE;
		}
		return false;
	}

	/**
	 * True when a comment appears between the first declarator name and the
	 * terminating {@code ;} of a multi-variable declaration (a continuation or
	 * interior comment), which the name-only rebuild would drop. A trailing
	 * comment after the {@code ;} is not interior and does not count.
	 */
	@CheckReturnValue
	private static boolean hasInteriorComment(@Nonnull FieldEntry entry, @Nonnull String[] stripped) {
		final var declStart = entry.declLineOffset;
		final var lastIdx = entry.lines.size() - 1;
		final var semi = lastTerminalSepIndex(stripped[entry.startLineIndex + lastIdx]);
		var state = LexerState.NONE;
		for (var k = declStart; k <= lastIdx; ++k) {
			final var raw = entry.lines.get(k);
			final var codeEnd = k == lastIdx && semi >= 0 ? semi : raw.length();
			// a comment inside declPrefix (indent/modifiers/type, before the first
			// name) travels with the rebuilt prefix, so only scan from the first
			// name onward on the declaration line
			final var scanStart = Math.min(k == declStart ? entry.declPrefix.length() : 0, raw.length());
			final var end = Math.clamp(Math.min(codeEnd, raw.length()), scanStart, raw.length());
			// a string or char literal in an initializer is no comment, so the masked line is no use here
			final var atStart = JavaLineScanner.stateAfter(raw.substring(0, scanStart), state);
			if (JavaLineScanner.firstCommentMarker(raw.substring(scanStart, end), atStart) >= 0)
				return true;
			state = JavaLineScanner.stateAfter(raw, state);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasNonAnnotationLeadingLines(@Nonnull FieldEntry entry) {
		for (var i = 0; i < entry.lines.size() - 1; ++i) {
			final var stripped = entry.lines.get(i).stripLeading();
			if (!stripped.startsWith("@"))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasOrphanedCommentLine(
			@Nonnull List<String> lines,
			@Nonnull String[] stripped,
			@Nonnull List<FieldEntry> fields,
			int startIdx,
			int endIdx
	) {
		final var owned = new boolean[endIdx - startIdx + 1];
		for (var f : fields) {
			for (var k = 0; k < f.lines.size(); ++k) {
				final var idx = f.startLineIndex + k - startIdx;
				if (idx >= 0 && idx < owned.length)
					owned[idx] = true;
			}
		}
		for (var i = 0; i < owned.length; ++i) {
			if (!owned[i] && isWholeLineComment(lines, stripped, startIdx + i))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasUnaccountedLines(@Nonnull List<String> original, @Nonnull List<String> replacement) {
		final var replacementSet = new HashSet<>(replacement);
		for (var line : original) {
			final var stripped = line.stripLeading();
			if (stripped.isEmpty() || stripped.startsWith("//") || stripped.startsWith("/*")
					|| stripped.startsWith("*") || stripped.startsWith("@"))
				continue;
			if (!replacementSet.contains(line) && !isSubsumedByConsolidation(stripped, replacement))
				return true;
		}
		return false;
	}

	/**
	 * True when {@code entries}, one static or instance group, hold a field whose initializer reads
	 * a later field while it runs ({@code readAtInit}, whose pairs never cross the groups). Sorting
	 * always puts what a field reads first, so it would move the later field to or above its
	 * reader, which would then see its value instead of its default.
	 */
	@CheckReturnValue
	private static boolean holdsInitTimeRead(@Nonnull List<FieldEntry> entries, @Nonnull Map<String, Set<String>> readAtInit) {
		for (var entry : entries) {
			for (var name : entry.names) {
				if (readAtInit.containsKey(name))
					return true;
			}
		}
		return false;
	}

	/**
	 * True when a {@linkplain SourceComments#isRangeDirective range directive} sits on a line from
	 * {@code from} to {@code to} (0-based, inclusive), or one counting lines counts one of them.
	 */
	@CheckReturnValue
	private static boolean holdsRangeDirective(@Nonnull List<String> lines, int from, int to) {
		final var comments = SourceComments.scan(lines);
		return SourceComments.rangeDirectiveOn(comments, from + 1, to + 1) || SourceComments.lineCountWindows(comments).reaches(from + 1, to + 1);
	}

	/** {@link FieldSortingCheck#initTimeForwardReads} over every field of {@code objBlock}. */
	@CheckReturnValue
	@Nonnull
	private static Map<String, Set<String>> initTimeForwardReads(@Nonnull DetailAST objBlock) {
		return FieldSortingCheck.initTimeForwardReads(fieldsOf(objBlock));
	}

	@CheckReturnValue
	private static boolean isCommentOnly(@Nonnull String text) {
		return JavaLineScanner.stripCommentsAndStrings(text, JavaLineScanner.LexerState.NONE).isBlank();
	}

	/**
	 * Only real source tokens carry a position worth measuring; imaginary container
	 * tokens (empty ANNOTATIONS/ELIST/MODIFIERS ...) have the token name as text. An
	 * IDENT is always a real token, and its text is user-chosen, so a constant or field
	 * named {@code IDENT} must not be mistaken for one of them.
	 */
	@CheckReturnValue
	private static boolean isSourceToken(@Nonnull DetailAST node) {
		return node.getFirstChild() == null
				&& (node.getType() == TokenTypes.IDENT || !node.getText().equals(TokenUtil.getTokenName(node.getType())));
	}

	private static boolean isSubsumedByConsolidation(@Nonnull String stripped, @Nonnull List<String> replacement) {
		// a comment in the prefix moved above the merged declaration, so it no longer reads as one
		final var masked = JavaLineScanner.stripCommentsAndStrings(stripped, JavaLineScanner.LexerState.NONE);
		final var matcher = FIELD_PATTERN.matcher(masked);
		if (!matcher.find())
			return false;
		final var afterName = masked.substring(matcher.end()).stripLeading();
		if (afterName.isEmpty())
			return false;
		final var next = afterName.charAt(0);
		if (next != ';' && next != ',' && next != '=')
			return false;
		final var fieldName = matcher.group(2);
		for (var rLine : replacement) {
			if (containsFieldWord(rLine, fieldName))
				return true;
		}
		return false;
	}

	/**
	 * Whether line {@code j} is comment text end to end: the mask blanked it but
	 * the raw line carries characters. Text-block content also masks to blank, so
	 * callers that must tell trivia from string content pair this with the line's
	 * entry {@link LexerState}.
	 */
	@CheckReturnValue
	private static boolean isWholeLineComment(@Nonnull List<String> lines, @Nonnull String[] stripped, int j) {
		return stripped[j].isBlank() && !lines.get(j).isBlank();
	}

	/**
	 * Index of the last top-level {@code ,}/{@code ;} separator in {@code line},
	 * or -1 when the line has none. A separator counts only at the top level of
	 * the declaration: not inside argument parens ({@code f(1, 2)}), not inside an
	 * anonymous-class or array-initializer body ({@code { a, b }}), and not inside
	 * a string/char literal or comment. Paren/brace depth may go negative
	 * because callers pass the last physical line of a multi-line field
	 * declaration (e.g. {@code "});"}), so the top level is depth {@code <= 0}.
	 * Text blocks are not classified, so the input must be text-block-stripped or
	 * a single-line construct with no text-block opener.
	 */
	@CheckReturnValue
	static int lastTerminalSepIndex(@Nonnull String line) {
		var lastSep = -1;
		var parenDepth = 0;
		var braceDepth = 0;
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !LineText.isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !LineText.isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else if (c == '(')
				++parenDepth;
			else if (c == ')')
				--parenDepth;
			else if (c == '{')
				++braceDepth;
			else if (c == '}')
				--braceDepth;
			else if ((c == ',' || c == ';') && parenDepth <= 0 && braceDepth <= 0)
				lastSep = i;
		}
		return lastSep;
	}

	/**
	 * Extends upward from the declaration's first source line over the comment
	 * lines directly above it (blanks and code stop the walk), returning the
	 * 0-based index the field's line span should start at. Annotations are
	 * already part of the AST node span, so only comments are gathered here.
	 *
	 * <p>A line qualifies only when {@code stripped} agrees it is entirely
	 * comment, so a text block's content line that reads as {@code // ...} is
	 * string content rather than trivia. A block comment's continuation lines are
	 * claimed only together with the {@code /*} that opens them: claiming a
	 * closing {@code *}{@code /} without its opener would move the closer above
	 * the opener and comment the following field out while still compiling.
	 */
	@CheckReturnValue
	private static int leadingLookback(
			@Nonnull List<String> lines,
			@Nonnull String[] stripped,
			@Nonnull LexerState[] entryState,
			int declFirstLine
	) {
		var start = declFirstLine;
		for (var j = declFirstLine - 1; j >= 0; --j) {
			if (!isWholeLineComment(lines, stripped, j) || entryState[j].inTextBlock())
				break;
			if (!entryState[j].inBlockComment()) {
				start = j;
				continue;
			}
			final var opener = blockCommentOpener(lines, stripped, entryState, j);
			if (opener < 0)
				break;
			start = opener;
			j = opener;
		}
		return start;
	}

	/**
	 * True when {@code region} opens a block comment or text block it does not
	 * close. Scans line by line because a {@code //} only masks to the end of its
	 * own line.
	 */
	@CheckReturnValue
	private static boolean leavesLiteralOpen(@Nonnull String region) {
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : NEWLINE.split(region, -1))
			state = JavaLineScanner.stateAfter(line, state);
		return state.inMultilineLiteral();
	}

	/**
	 * Trivia that belongs to the end of the constant list rather than to any one
	 * constant: whole lines sitting between the source-last constant's {@code ,} and
	 * the list terminator. Anything on the {@code ,}'s own line was written against
	 * that constant and stays with it ({@link #trailingTrivia}); a comment on a line
	 * of its own is a note about the list.
	 */
	@CheckReturnValue
	@Nonnull
	private static String listTailTrivia(@Nonnull List<String> lines, @Nonnull EnumSpan span, @Nonnull List<DetailAST> consts) {
		if (span.terminal().isEmpty())
			return "";
		final var last = consts.size() - 1;
		final var boundary = Math.max(span.commaLine(last), endPos(consts.get(last))[0]);
		if (boundary >= span.endLine())
			return "";
		return sourceBetween(lines, boundary + 1, 0, span.endLine(), span.endCol() - span.terminal().length()).strip();
	}

	/**
	 * The comments {@code candidate} carries that a merge into {@code base} moves above the
	 * merged declaration: those on a leading line the base does not repeat, and those on its
	 * declaration line when its prefix differs from the base's. Null when one of its lines leaves a
	 * block comment open, since a moved comment goes on one line, or when one the base does not
	 * repeat is a {@link SourceComments#isDirective tool directive}, which acts on its own
	 * line.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> movedComments(@Nonnull FieldEntry base, @Nonnull FieldEntry candidate) {
		final var moved = new ArrayList<String>();
		final var declIdx = candidate.lines.size() - 1;
		for (var k = 0; k <= declIdx; ++k) {
			final var line = candidate.lines.get(k);
			if (JavaLineScanner.stateAfter(line, LexerState.NONE).inBlockComment())
				return null;
			final var repeated = k < declIdx
					? k < base.lines.size() - 1 && line.equals(base.lines.get(k))
					: candidate.declPrefix.equals(base.declPrefix);
			if (!repeated) {
				for (var comment : SourceComments.scan(List.of(line)))
					moved.add(comment.text());
			}
		}
		final var kept = new ArrayList<String>();
		for (var comment : SourceComments.scan(base.lines))
			kept.add(comment.text());
		for (var text : moved) {
			if (SourceComments.isDirective(text) && !kept.contains(text))
				return null;
		}
		return moved;
	}

	/**
	 * True when {@code sorted}, the rewrite of {@code group}, changes what follows a tool directive
	 * left in place above an entry ({@link FieldEntry#detachedDirective}): the entry, or one ahead
	 * of it, the line above's trailing directive being an earlier entry's, ends up elsewhere, so
	 * the directive would act on another field.
	 */
	@CheckReturnValue
	private static boolean movesDetachedDirective(@Nonnull List<FieldEntry> group, @Nonnull List<FieldEntry> sorted) {
		for (var i = 0; i < group.size(); ++i) {
			if (group.get(i).detachedDirective && (i >= sorted.size() || !sorted.subList(0, i + 1).equals(group.subList(0, i + 1))))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static int nodeFirstLine(@Nonnull DetailAST root) {
		var min = root.getLineNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() < min)
				min = node.getLineNo();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return min;
	}

	@CheckReturnValue
	private static int nodeLastLine(@Nonnull DetailAST root) {
		var max = root.getLineNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() > max)
				max = node.getLineNo();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return max;
	}

	/**
	 * The declarators of one declaration, {@code texts} for {@code names}, in the order the check
	 * wants among a declaration's own names: in a {@code final} one those with a value first, then
	 * by name, each after the names of the declaration it waits for ({@code dependencies}). Null
	 * when the names wait for each other in a cycle.
	 */
	@CheckReturnValue
	@Nullable
	static List<String> orderDeclarators(
			@Nonnull List<String> names,
			@Nonnull List<String> texts,
			@Nonnull Map<String, Set<String>> dependencies,
			boolean isFinal
	) {
		final var positions = new HashMap<String, List<Integer>>();
		for (var i = 0; i < names.size(); ++i)
			positions.computeIfAbsent(names.get(i), name -> new ArrayList<>()).add(i);
		final var waiting = new int[names.size()];
		final var readers = new ArrayList<List<Integer>>(names.size());
		for (var i = 0; i < names.size(); ++i)
			readers.add(new ArrayList<>());
		for (var i = 0; i < names.size(); ++i) {
			for (var read : dependencies.getOrDefault(names.get(i), Set.of())) {
				if (read.equals(names.get(i)))
					continue;
				for (var at : positions.getOrDefault(read, List.of())) {
					++waiting[i];
					readers.get(at).add(i);
				}
			}
		}
		final var ready = new PriorityQueue<Integer>(Comparator
				.<Integer>comparingInt(i -> isFinal && !texts.get(i).substring(names.get(i).length()).stripLeading().startsWith("=") ? 1 : 0)
				.thenComparing(names::get, String.CASE_INSENSITIVE_ORDER)
				.thenComparingInt(i -> i));
		for (var i = 0; i < names.size(); ++i) {
			if (waiting[i] == 0)
				ready.add(i);
		}
		final var ordered = new ArrayList<String>(names.size());
		while (!ready.isEmpty()) {
			final var next = ready.poll();
			ordered.add(texts.get(next));
			for (var reader : readers.get(next)) {
				if (--waiting[reader] == 0)
					ready.add(reader);
			}
		}
		return ordered.size() == names.size() ? ordered : null;
	}

	/**
	 * Orders {@code group} by the check's rule, emitting each field only after
	 * the fields it reads and, {@code withPins}, the earlier fields it is pinned to
	 * ({@link FieldEntry#pins}); fields tied on the rule keep their source order. An
	 * anonymous-initializer field also waits for every free plain field of its chunk
	 * (see {@link #freePlainNames}) it may trade places with, which is the set the check requires
	 * ahead of it. Returns {@code null} when some fields can never be emitted.
	 */
	@CheckReturnValue
	@Nullable
	private static List<FieldEntry> orderGroup(
			@Nonnull List<FieldEntry> group,
			@Nonnull Set<String> freePlainNames,
			boolean withPins
	) {
		final var ruleOrder = Comparator
				.comparingInt(FieldEntry::chunk)
				.thenComparingInt(f -> f.anonInit() ? 1 : 0)
				.thenComparing(FieldEntry::sortType, FieldSortingCheck::compareTypes)
				.thenComparing(FieldEntry::annotations, FieldSortingCheck::compareAnnotations)
				.thenComparing(FieldEntry::typeArgAnnotations, FieldSortingCheck::compareTypeArgAnnotations)
				.thenComparing(FieldEntry::name, String.CASE_INSENSITIVE_ORDER);
		final var ready = new PriorityQueue<Integer>((a, b) -> {
			final var cmp = ruleOrder.compare(group.get(a), group.get(b));
			return cmp != 0 ? cmp : Integer.compare(a, b);
		});

		final var dependents = new HashMap<String, List<Integer>>();
		final var plainWaiters = new HashMap<Integer, List<Integer>>();
		final var unplaced = new int[group.size()];
		for (var i = 0; i < group.size(); ++i) {
			final var entry = group.get(i);
			final var waitsFor = new HashSet<>(entry.dependencies());
			if (withPins)
				waitsFor.addAll(entry.pins());
			for (var name : waitsFor)
				dependents.computeIfAbsent(name, key -> new ArrayList<>()).add(i);
			unplaced[i] = waitsFor.size();
			if (!entry.anonInit())
				continue;
			for (var j = 0; j < group.size(); ++j) {
				final var plain = group.get(j);
				if (!plain.anonInit()
						&& plain.chunk() == entry.chunk()
						&& freePlainNames.contains(plain.name())
						&& !(withPins && pinnedTogether(entry, plain))) {
					++unplaced[i];
					plainWaiters.computeIfAbsent(j, key -> new ArrayList<>()).add(i);
				}
			}
		}
		for (var i = 0; i < group.size(); ++i) {
			if (unplaced[i] == 0)
				ready.add(i);
		}

		final var ordered = new ArrayList<FieldEntry>(group.size());
		while (!ready.isEmpty()) {
			final var index = ready.poll();
			final var next = group.get(index);
			ordered.add(next);
			final var waiting = new ArrayList<>(plainWaiters.getOrDefault(index, List.of()));
			for (var name : next.names())
				waiting.addAll(dependents.getOrDefault(name, List.of()));
			for (var waiter : waiting) {
				if (--unplaced[waiter] == 0)
					ready.add(waiter);
			}
		}
		return ordered.size() == group.size() ? ordered : null;
	}

	/**
	 * Builds the {@link FieldEntry} list for a class body's {@code OBJBLOCK} in
	 * source order. Consecutive {@code VARIABLE_DEF} declarators separated by a
	 * {@code COMMA} sibling (the shape checkstyle emits for {@code int x, y, z;})
	 * are folded into one entry so the rule/consolidation logic sees one physical
	 * declaration with multiple names. Each entry's line span runs from its first
	 * source token (its topmost annotation, extended upward over attached
	 * comments) to its terminating {@code ;}.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<FieldEntry> parseFieldsFromAst(
			@Nonnull List<String> lines,
			@Nonnull String[] stripped,
			@Nonnull LexerState[] entryState,
			@Nonnull DetailAST objBlock
	) {
		final var groups = new ArrayList<List<DetailAST>>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var prev = child.getPreviousSibling();
			if (prev != null && prev.getType() == TokenTypes.COMMA && !groups.isEmpty())
				groups.getLast().add(child);
			else {
				final var g = new ArrayList<DetailAST>();
				g.add(child);
				groups.add(g);
			}
		}
		if (groups.isEmpty())
			return List.of();

		// per static/instance group, as the check builds them: a static field is always
		// initialized before every instance field, so a reference across groups never
		// constrains the order
		final var instanceDefs = new ArrayList<DetailAST>();
		final var staticDefs = new ArrayList<DetailAST>();
		for (var g : groups) {
			if (FieldSortingCheck.classifyField(g.getFirst()).isStatic())
				staticDefs.addAll(g);
			else
				instanceDefs.addAll(g);
		}
		final var depMap = new HashMap<>(FieldSortingCheck.fieldDependencies(instanceDefs));
		depMap.putAll(FieldSortingCheck.fieldDependencies(staticDefs));
		final var pinMap = new HashMap<>(FieldInitializers.pinnedAfter(instanceDefs));
		pinMap.putAll(FieldInitializers.pinnedAfter(staticDefs));
		final var runOf = new HashMap<DetailAST, Integer>();
		final var comments = SourceComments.scan(lines);
		final var runs = FieldConsolidationFixer.runs(lines, comments, objBlock);
		for (var i = 0; i < runs.size(); ++i) {
			for (var declaration : runs.get(i))
				runOf.put(declaration.getFirst(), i);
		}

		final var columns = new LineColumns(lines);
		final var fields = new ArrayList<FieldEntry>(groups.size());
		for (var g : groups) {
			final var first = g.getFirst();
			final var info = FieldSortingCheck.classifyField(first);

			final var names = new ArrayList<String>(g.size());
			final var declarators = new ArrayList<String>(g.size());
			for (var d : g) {
				names.add(FieldSortingCheck.fieldName(d));
				declarators.add(FieldConsolidationFixer.declaratorText(columns, d));
			}

			final var firstIdent = first.findFirstToken(TokenTypes.IDENT);
			final var identLineText = lines.get(firstIdent.getLineNo() - 1);
			final var declPrefix = identLineText.substring(0, charIndexOf(identLineText, firstIdent.getColumnNo()));

			var anonMixed = false;
			var hasInit = false;
			var cStyleArray = false;
			for (var d : g) {
				if (FieldSortingCheck.classifyField(d).anonInit() != info.anonInit())
					anonMixed = true;
				if (d.findFirstToken(TokenTypes.ASSIGN) != null)
					hasInit = true;
				if (hasCStyleArray(d))
					cStyleArray = true;
			}

			final var declFirstLine = nodeFirstLine(first) - 1;
			var declLastLine = nodeLastLine(first);
			for (var d : g)
				declLastLine = Math.max(declLastLine, nodeLastLine(d));
			--declLastLine;

			final var startLineIndex = leadingLookback(lines, stripped, entryState, declFirstLine);

			final var declStart = startPos(first);
			// checkstyle hangs a multi-variable declaration's `;` under its first declarator
			var declEnd = endPos(first);
			for (var d : g) {
				final var end = endPos(d);
				if (end[0] > declEnd[0] || end[0] == declEnd[0] && end[1] > declEnd[1])
					declEnd = end;
			}
			final var startLine = declStart[0] - 1;
			final var endLine = declEnd[0] - 1;
			// the masked line is char-aligned with the raw one, so the code-point columns
			// are converted against the raw line before they slice the masked one. A block
			// comment is masked to blanks, so one that the field's first line closes or its
			// last line leaves open is caught through the lexer state instead: the rebuild
			// would move the field away from the rest of that comment
			final var sharesLine = !stripped[startLine].substring(0, charIndexOf(lines.get(startLine), declStart[1])).isBlank()
					|| !stripped[endLine].substring(charIndexOf(lines.get(endLine), declEnd[1])).isBlank()
					|| entryState[startLine].inBlockComment()
							&& blockCommentOpener(lines, stripped, entryState, startLine) < startLineIndex
					|| endLine + 1 < entryState.length && entryState[endLine + 1].inBlockComment();

			final var deps = new HashSet<String>();
			final var declaratorDeps = new HashMap<String, Set<String>>();
			final var kinds = new ArrayList<Kind>(g.size());
			final var pins = new HashSet<String>();
			final var declaratorPins = new HashMap<String, Set<String>>();
			for (var d : g) {
				final var name = FieldSortingCheck.fieldName(d);
				final var dd = depMap.get(name);
				if (dd != null) {
					deps.addAll(dd);
					declaratorDeps.put(name, dd);
				}
				kinds.add(FieldInitializers.kindOf(d));
				final var pinned = pinMap.get(name);
				if (pinned != null) {
					pins.addAll(pinned);
					declaratorPins.put(name, pinned);
				}
			}
			deps.removeAll(names);
			pins.removeAll(names);
			var detachedDirective = false;
			for (var directive : SourceComments.leadingDirectives(columns, comments, first))
				detachedDirective |= DocComments.line(directive.start()) - 1 < startLineIndex;

			fields.add(new FieldEntry(
					info.annotationKeys(),
					runOf.getOrDefault(first, -1),
					info.name(),
					names,
					declarators,
					info.sortType(),
					info.typeArgAnnotationKeys(),
					info.chunk(),
					info.isStatic(),
					info.anonInit(),
					anonMixed,
					hasInit,
					cStyleArray,
					sharesLine,
					declPrefix,
					new ArrayList<>(lines.subList(startLineIndex, declLastLine + 1)),
					deps,
					declaratorDeps,
					kinds,
					pins,
					declaratorPins,
					startLineIndex,
					firstIdent.getLineNo() - 1 - startLineIndex,
					detachedDirective
			));
		}
		return fields;
	}

	/**
	 * True when a name of {@code a} and one of {@code b} are {@linkplain FieldInitializers#pinned pinned}.
	 */
	@CheckReturnValue
	private static boolean pinnedTogether(@Nonnull FieldEntry a, @Nonnull FieldEntry b) {
		for (var i = 0; i < a.names().size(); ++i) {
			for (var j = 0; j < b.names().size(); ++j) {
				if (FieldInitializers.pinned(a.kinds().get(i), b.kinds().get(j)))
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static LexerState[] precomputeLexerStates(@Nonnull List<String> lines) {
		final var states = new LexerState[lines.size()];
		var state = LexerState.NONE;
		for (var i = 0; i < lines.size(); ++i) {
			states[i] = state;
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		}
		return states;
	}

	@CheckReturnValue
	@Nonnull
	private static String[] precomputeStrippedLines(@Nonnull List<String> lines) {
		final var stripped = new String[lines.size()];
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lines.size(); ++i) {
			final var line = lines.get(i);
			stripped[i] = JavaLineScanner.stripCommentsAndStrings(line, state);
			state = JavaLineScanner.stateAfter(line, state);
		}
		return stripped;
	}

	@CheckReturnValue
	@Nonnull
	private static String prefixed(@Nonnull String trivia, @Nonnull String text) {
		return trivia.isEmpty() ? text : trivia + " " + text;
	}

	/**
	 * Rebuilds a contiguous same-group field run into sorted order, inserting a
	 * blank line between chunks and consolidating mergeable declarations of one
	 * {@link FieldConsolidationFixer#runs run}. Returns {@code null} when the rebuild is a no-op, or a
	 * {@link SkipResult} when it would drop a line no field owns, when
	 * {@link FieldRewriteVerifier} rejects it, or for a known conflict reordering
	 * cannot resolve.
	 */
	@Nullable
	private static FixAttempt rebuildContiguousGroup(@Nonnull List<String> lines, @Nonnull List<FieldEntry> group, @Nonnull List<FieldEntry> sorted, @Nonnull Set<String> multiVarSecondaryNames) {
		final var startIdx = group.getFirst().startLineIndex;
		final var endIdx = group.getLast().startLineIndex + group.getLast().lines.size() - 1;

		final var replacement = new ArrayList<String>();
		var prevChunk = -1;
		var i2 = 0;
		while (i2 < sorted.size()) {
			final var entry = sorted.get(i2);
			if (!replacement.isEmpty() && entry.chunk != prevChunk)
				replacement.add("");
			prevChunk = entry.chunk;

			if (!canConsolidate(entry)) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}

			final var names = new ArrayList<>(entry.names);
			final var texts = new ArrayList<>(entry.declarators);
			final var dependencies = withPins(entry.declaratorDependencies, entry.declaratorPins);
			// the candidates come in sorted order, their moved comments are joined in source order
			final var movedFrom = new TreeMap<Integer, List<String>>();
			var j2 = i2 + 1;
			while (j2 < sorted.size() && canMergeWith(entry, sorted.get(j2))) {
				final var candidate = sorted.get(j2);
				names.addAll(candidate.names);
				texts.addAll(candidate.declarators);
				dependencies.putAll(withPins(candidate.declaratorDependencies, candidate.declaratorPins));
				movedFrom.put(candidate.startLineIndex, requireNonNull(movedComments(entry, candidate)));
				++j2;
			}
			final var moved = new ArrayList<String>();
			for (var comments : movedFrom.values())
				moved.addAll(comments);
			final var ordered = orderDeclarators(names, texts, dependencies, entry.chunk < NON_FINAL_CHUNK);
			if (ordered == null || j2 == i2 + 1 && ordered.equals(entry.declarators)) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}

			final var kept = new ArrayList<String>();
			for (var comment : SourceComments.scan(entry.lines))
				kept.add(comment.text());
			final var hoisted = new ArrayList<String>();
			for (var text : moved) {
				if (!kept.contains(text) && !hoisted.contains(text))
					hoisted.add(text);
			}
			final var indent = LineText.extractIndent(entry.lines.getLast());
			// the moved comments go right above the declaration's first annotation or its own line,
			// below any comment lines leading it
			var codeLine = 0;
			while (codeLine < entry.lines.size() - 1 && !entry.lines.get(codeLine).stripLeading().startsWith("@"))
				++codeLine;
			for (var k = 0; k < entry.lines.size() - 1; ++k) {
				if (k == codeLine && !hoisted.isEmpty())
					replacement.add(indent + FieldRewriteVerifier.joinComments(hoisted));
				replacement.add(entry.lines.get(k));
			}
			if (codeLine == entry.lines.size() - 1 && !hoisted.isEmpty())
				replacement.add(indent + FieldRewriteVerifier.joinComments(hoisted));
			replacement.addAll(buildDeclLines(entry.declPrefix, ordered, ";", indent));
			i2 = j2;
		}

		final var original = new ArrayList<>(lines.subList(startIdx, endIdx + 1));
		if (replacement.equals(original)) {
			for (var f : group) {
				for (var dep : f.dependencies) {
					if (multiVarSecondaryNames.contains(dep))
						return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_DEPENDENCY);
				}
			}
			// the check orders declarators, this rebuild orders whole declarations:
			// a secondary name that sorts past a sibling field can only be separated
			// from its declaration by splitting it, which the name-only rebuild
			// cannot do (consolidating them instead would drop a per-declarator
			// suffix). Normally consolidation absorbs the sibling into the
			// multi-variable declaration and the order comes out right
			for (var f : group) {
				if (f.names.size() > 1)
					return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_INTERLEAVED);
			}
			return null;
		}

		if (hasUnaccountedLines(original, replacement))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_UNACCOUNTED_LINE);

		return verified(lines, new FixResult(startIdx, endIdx, replacement));
	}

	/**
	 * Rebuilds an enum's constants from the AST: emits each constant on its own line, in the order
	 * written. Only the character span the constants occupy is replaced (from just past the
	 * enum's opening brace to past the trailing {@code ;}/{@code ,}), so text sharing the first or last line of that span (the
	 * {@code enum E} header, the closing brace) is re-emitted around the rebuild
	 * rather than deleted.
	 *
	 * <p>Because the parse is {@code WITHOUT_COMMENTS}, comments are recovered
	 * positionally rather than by scanning for markers: Java permits nothing but
	 * whitespace and comments between a constant's last token and its {@code ,}, or
	 * between that {@code ,} and the next constant, so those gaps are carried verbatim.
	 * Each gap has exactly one owner, which is what keeps a comment from being emitted
	 * twice or a separator from being swept into one.
	 *
	 * <p>Returns {@code null} when there are fewer than two constants or no two share a line, and a
	 * {@link SkipResult} when a {@linkplain SourceComments#isRangeDirective range directive} reaches
	 * the span, a {@linkplain SourceComments#isDirective tool directive} sits on a line two constants
	 * share, a constant's trivia cannot be relocated (an unclosed comment or text block, or
	 * multi-line trailing trivia), or {@link FieldRewriteVerifier} rejects the rebuild.
	 */
	@Nullable
	private static FixAttempt reconstructEnumFromAst(@Nonnull List<String> lines, @Nonnull DetailAST objBlock) {
		final var consts = new ArrayList<DetailAST>();
		for (var c = objBlock.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ENUM_CONSTANT_DEF)
				consts.add(c);
		}
		// only the same-line report is an error, so a call for an enum whose constants each have a
		// line of their own comes from an order warning, which is never auto-fixed
		if (consts.size() < 2 || !constantsShareALine(consts))
			return null;
		final var lcurly = objBlock.findFirstToken(TokenTypes.LCURLY);
		if (lcurly == null)
			return null;

		final var span = enumSpan(objBlock, consts);
		final var startLine = lcurly.getLineNo();
		final var startCol = lcurly.getColumnNo() + 1;
		if (span.endLine() < startLine)
			return null;
		// A non-last constant with no recorded `,` means enumSpan stopped early on an
		// unexpected sibling. Unreachable on a parsed buffer (only RCURLY and SEMI can
		// follow a constant that another constant follows), but the arithmetic below
		// would then read from line 1 and sweep the whole file into that constant's
		// leading lines, so the whole-file corruption is worth a guard it cannot cost.
		for (var k = 0; k < consts.size() - 1; ++k) {
			if (span.commaLine(k) <= 0)
				return null;
		}
		// the split moves constants off the lines such a comment counts or switches
		if (holdsRangeDirective(lines, startLine - 1, span.endLine() - 1))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_RANGE_DIRECTIVE);
		if (sharedLineHoldsDirective(lines, consts))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ENUM_SPLIT_DIRECTIVE);
		final var enumIndent = LineText.extractIndent(lines.get(startLine - 1));
		final var indent = constantIndent(lines, consts, enumIndent);
		final var entries = new ArrayList<EnumEntry>(consts.size());
		final var headerRaw = lines.get(startLine - 1);
		var prefix = headerRaw.substring(0, charIndexOf(headerRaw, startCol));

		for (var k = 0; k < consts.size(); ++k) {
			final var cd = consts.get(k);
			final var startAt = cd.getLineNo();
			final var startOf = cd.getColumnNo();
			final var end = endPos(cd);

			// where this constant's leading gap begins: the previous constant's `,` when
			// the two share a line (`ZETA, /* mid */ ALPHA`: the comment introduces
			// ALPHA), otherwise the line below it, so anything left on the `,`'s line
			// trails the previous constant instead
			final var sameLineAsPrevComma = k > 0 && span.commaLine(k - 1) == startAt;
			final var fromLine = k == 0 ? startLine : (sameLineAsPrevComma ? startAt : span.commaLine(k - 1) + 1);
			final var fromCol = k == 0 ? startCol : (sameLineAsPrevComma ? span.commaCol(k - 1) + 1 : 0);

			// `pending` is trivia that has to ride on the next emitted line rather than
			// occupy one of its own: a comment sitting ahead of an annotation or ident on
			// their line
			final var leading = new ArrayList<String>();
			var pending = "";
			if (fromLine == startAt)
				pending = sourceBetween(lines, fromLine, fromCol, startAt, startOf).strip();
			else {
				final var gap = NEWLINE.split(sourceBetween(lines, fromLine, fromCol, startAt, startOf), -1);
				// gap[0] is the tail of fromLine; for the first constant that is the header
				// line, whose comments belong to the header rather than to any constant
				var first = 0;
				if (k == 0 && !gap[0].isBlank()) {
					prefix += gap[0].stripTrailing();
					first = 1;
				}
				for (var g = first; g < gap.length - 1; ++g) {
					if (!gap[g].isBlank())
						leading.add(gap[g].stripTrailing());
				}
				pending = gap[gap.length - 1].strip();
			}

			var contentAt = startAt;
			var contentFrom = startOf;
			final var annotations = cd.findFirstToken(TokenTypes.ANNOTATIONS);
			final var ident = cd.findFirstToken(TokenTypes.IDENT);
			if (ident != null && annotations != null && annotations.getFirstChild() != null) {
				for (var ann = annotations.getFirstChild(); ann != null; ann = ann.getNextSibling()) {
					final var annEnd = endPos(ann);
					final var annLines = NEWLINE.split(
							sourceBetween(lines, ann.getLineNo(), ann.getColumnNo(), annEnd[0], annEnd[1]), -1
					);
					for (var a = 0; a < annLines.length; ++a)
						leading.add(a == 0 ? indent + prefixed(pending, annLines[a]) : annLines[a]);
					final var next = ann.getNextSibling();
					final var toLine = next != null ? next.getLineNo() : ident.getLineNo();
					final var toCol = next != null ? next.getColumnNo() : ident.getColumnNo();
					final var gap = NEWLINE.split(sourceBetween(lines, annEnd[0], annEnd[1], toLine, toCol), -1);
					if (gap.length > 1) {
						if (!gap[0].isBlank())
							leading.set(leading.size() - 1, leading.getLast() + gap[0].stripTrailing());
						for (var g = 1; g < gap.length - 1; ++g) {
							if (!gap[g].isBlank())
								leading.add(gap[g].stripTrailing());
						}
					}
					pending = gap[gap.length - 1].strip();
				}
				contentAt = ident.getLineNo();
				contentFrom = ident.getColumnNo();
			}

			final var contentLines = new ArrayList<String>();
			final var parts = NEWLINE.split(sourceBetween(lines, contentAt, contentFrom, end[0], end[1]), -1);
			for (var pi = 0; pi < parts.length; ++pi)
				contentLines.add(pi == 0 ? indent + prefixed(pending, parts[pi]) : parts[pi]);

			entries.add(new EnumEntry(
					FieldSortingCheck.fieldName(cd), leading, contentLines, trailingTrivia(lines, span, consts, k)
			));
		}

		final var endRaw = lines.get(span.endLine() - 1);
		final var suffix = endRaw.substring(Math.min(charIndexOf(endRaw, span.endCol()), endRaw.length()));
		final var listTail = listTailTrivia(lines, span, consts);

		// the rebuild re-lays each constant together with the trivia it owns, so every unit,
		// and the header and tail that stay put, has to be lexically self-contained.
		// A comment opening in one unit and closing in another is cut from its terminator
		// and swallows whatever the rebuild puts in between, which for a header-line
		// opener yields an enum that still compiles minus a constant.
		for (var entry : entries) {
			final var unit = String.join("\n", entry.leadingLines()) + '\n'
					+ String.join("\n", entry.contentLines()) + '\n' + entry.trailing();
			if (leavesLiteralOpen(unit))
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ENUM_SPLIT_COMMENT);
			// trailing trivia rides on the separator line, so a multi-line one would fold
			// a `//` over everything after it
			if (entry.trailing().indexOf('\n') >= 0)
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ENUM_TRAILING_MULTILINE);
		}
		// the suffix is re-emitted at the position it already held, so it only has to be
		// self-contained when trailingTrivia folded it into a constant instead
		final var foldedSuffix = isCommentOnly(suffix) && leavesLiteralOpen(suffix);
		if (leavesLiteralOpen(prefix) || leavesLiteralOpen(listTail) || foldedSuffix)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ENUM_SPLIT_COMMENT);

		final var replacement = new ArrayList<String>();
		replacement.add(prefix.stripTrailing());
		replacement.addAll(buildReplacement(entries, span.terminal()));
		// a multi-line block comment here keeps its interior alignment and its blank
		// lines, which re-indenting every line (or dropping the blanks) would rewrite
		if (!listTail.isEmpty()) {
			final var tailLines = NEWLINE.split(listTail, -1);
			replacement.add(indent + tailLines[0].strip());
			replacement.addAll(List.of(tailLines).subList(1, tailLines.length));
		}

		// a comment-only tail already went to the source-last constant; only real code
		// past the span still needs a line of its own
		if (!isCommentOnly(suffix)) {
			final var stripped = suffix.strip();
			replacement.add((stripped.startsWith("}") ? enumIndent : indent) + stripped);
		}

		return verified(lines, new FixResult(startLine - 1, span.endLine() - 1, replacement));
	}

	/**
	 * Reorders a same-group field run that is physically interleaved with an
	 * opposite-static field: the group's fields are swapped into their existing
	 * physical slots (the interleaved field stays put). Only a plain single-chunk
	 * dependency-free reorder is safe this way; anything else returns a
	 * {@link SkipResult}.
	 */
	@Nonnull
	private static FixAttempt reorderInterleavedGroup(@Nonnull List<String> lines, @Nonnull List<FieldEntry> group, @Nonnull List<FieldEntry> sorted) {
		final var chunk = group.getFirst().chunk;
		for (var f : group) {
			if (f.chunk != chunk || !f.dependencies.isEmpty())
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_INTERLEAVED_STATIC);
		}

		final var startIdx = group.getFirst().startLineIndex;
		final var endIdx = group.getLast().startLineIndex + group.getLast().lines.size() - 1;
		final var replacement = new ArrayList<String>();
		var slot = 0;
		var i = startIdx;
		while (i <= endIdx) {
			final var slotEntry = slot < group.size() ? group.get(slot) : null;
			if (slotEntry != null && i == slotEntry.startLineIndex) {
				replacement.addAll(sorted.get(slot).lines);
				i = slotEntry.startLineIndex + slotEntry.lines.size();
				++slot;
			}
			else {
				replacement.add(lines.get(i));
				++i;
			}
		}

		final var original = new ArrayList<>(lines.subList(startIdx, endIdx + 1));
		if (replacement.equals(original))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_INTERLEAVED_STATIC);
		// the lines between the slots are copied verbatim, so an initializer block or method
		// there stays put while the fields swap around it; the re-parse refuses that
		return verified(lines, new FixResult(startIdx, endIdx, replacement));
	}

	/**
	 * True when a {@linkplain SourceComments#isDirective tool directive} sits, wholly or in part, on a
	 * line two of {@code consts}, an enum's constants in source order, share: one where a constant
	 * starts on the line the one before it ends ({@code GREEN("g",} / {@code "x"), BLUE("b"); // NOSONAR}).
	 * One acting on its line ({@code // NOSONAR}, {@code //$NON-NLS-2$}) would cover only one of them
	 * once split.
	 */
	@CheckReturnValue
	private static boolean sharedLineHoldsDirective(@Nonnull List<String> lines, @Nonnull List<DetailAST> consts) {
		final var shared = new HashSet<Integer>();
		for (var k = 1; k < consts.size(); ++k) {
			final var line = consts.get(k).getLineNo();
			if (line == endPos(consts.get(k - 1))[0])
				shared.add(line);
		}
		for (var comment : SourceComments.scan(lines)) {
			if (!SourceComments.isDirective(comment.text()))
				continue;
			for (var line : shared) {
				if (DocComments.line(comment.start()) <= line && DocComments.line(comment.end()) >= line)
					return true;
			}
		}
		return false;
	}

	/**
	 * Rewrites a single multi-name declaration in place with its declarators in
	 * {@code sortedDeclarators} order. The type/modifier prefix is {@code entry.declPrefix}; the
	 * terminator plus any trailing comment is preserved as the suffix. Returns
	 * {@code null} when the terminator can't be located or the rebuild reproduces
	 * the source.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult sortNamesWithin(@Nonnull FieldEntry entry, @Nonnull List<String> sortedDeclarators, @Nonnull String[] stripped) {
		final var fieldLines = entry.lines;
		final var declStart = entry.declLineOffset;
		final var lastIdx = fieldLines.size() - 1;
		final var semi = lastTerminalSepIndex(stripped[entry.startLineIndex + lastIdx]);
		if (semi < 0)
			return null;
		final var suffix = fieldLines.getLast().substring(semi);

		final var rebuilt = new ArrayList<String>();
		for (var k = 0; k < declStart; ++k)
			rebuilt.add(fieldLines.get(k));
		rebuilt.addAll(buildDeclLines(entry.declPrefix, sortedDeclarators, suffix, LineText.extractIndent(fieldLines.get(declStart))));
		if (rebuilt.equals(fieldLines))
			return null;
		return new FixResult(entry.startLineIndex, entry.startLineIndex + fieldLines.size() - 1, rebuilt);
	}

	/**
	 * Handles a group of one field entry: only a multi-variable declaration with
	 * out-of-order names is reorderable. Returns a {@link SkipResult} for the
	 * unsafe multi-var forms (a declarator spanning lines, an interior comment, or a
	 * C-style array declarator), when its names wait for each other in a cycle, when a tool
	 * directive on its line would have to follow names it may count by position, when a
	 * {@linkplain SourceComments#isRangeDirective range directive} reaches a declaration spanning
	 * lines before or after the reorder, or when
	 * {@link FieldRewriteVerifier} rejects the rebuild, and {@code null} when there is
	 * nothing to reorder.
	 */
	@Nullable
	private static FixAttempt sortSingleEntry(@Nonnull List<String> lines, @Nonnull FieldEntry entry, @Nonnull String[] stripped) {
		if (entry.names.size() < 2)
			return null;
		// the unsafe-form guards run before the already-sorted test: a declaration the
		// check flagged for some other reason (chunk order, declarator type) but whose
		// names happen to be in order is still unfixable, and must say why rather than
		// fall through to the pipeline's generic "not fixable"
		for (var declarator : entry.declarators) {
			if (declarator.indexOf('\n') >= 0)
				return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_INITIALIZED);
		}
		if (hasInteriorComment(entry, stripped))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_COMMENT);
		if (entry.cStyleArray)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_C_STYLE_ARRAY);
		final var ordered = orderDeclarators(entry.names, entry.declarators, withPins(entry.declaratorDependencies, entry.declaratorPins), entry.chunk < NON_FINAL_CHUNK);
		if (ordered == null)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_DEPENDENCY_CYCLE);
		if (ordered.equals(entry.declarators))
			return null;
		// a directive there may count the names by position (`//$NON-NLS-2$` marks the second value)
		if (declLineHoldsDirective(entry))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_DIRECTIVE);
		final var rewrite = sortNamesWithin(entry, ordered, stripped);
		if (rewrite == null)
			return null;
		// names re-wrapped across lines move into or out of the lines such a comment counts or switches
		final var declarationLines = entry.lines.size() - entry.declLineOffset;
		final var rewrittenLines = rewrite.replacement().size() - entry.declLineOffset;
		if ((declarationLines > 1 || rewrittenLines > 1) && holdsRangeDirective(lines, entry.startLineIndex, entry.startLineIndex + entry.lines.size() - 1))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_RANGE_DIRECTIVE);
		return verified(lines, rewrite);
	}

	/**
	 * Returns the source substring between two positions (1-based lines, 0-based
	 * columns), joining intermediate lines with {@code \n}. The end position is
	 * exclusive.
	 */
	@CheckReturnValue
	@Nonnull
	private static String sourceBetween(@Nonnull List<String> lines, int line1, int col1, int line2, int col2) {
		final var start = charIndexOf(lines.get(line1 - 1), col1);
		final var end = charIndexOf(lines.get(line2 - 1), col2);
		if (line1 == line2)
			return lines.get(line1 - 1).substring(start, end);
		final var sb = new StringBuilder();
		sb.append(lines.get(line1 - 1).substring(start)).append('\n');
		for (var l = line1 + 1; l < line2; ++l)
			sb.append(lines.get(l - 1)).append('\n');
		sb.append(lines.get(line2 - 1), 0, end);
		return sb.toString();
	}

	/**
	 * True when {@code candidate} carries a doc comment that {@code base} does not repeat: on a
	 * leading line the base does not repeat verbatim, or on its declaration line outside a
	 * prefix the base shares. A merge would give it to every name.
	 */
	@CheckReturnValue
	private static boolean spreadsDocComment(@Nonnull FieldEntry base, @Nonnull FieldEntry candidate) {
		var state = LexerState.NONE;
		final var declIdx = candidate.lines.size() - 1;
		for (var k = 0; k < declIdx; ++k) {
			final var line = candidate.lines.get(k);
			final var repeated = k < base.lines.size() - 1 && line.equals(base.lines.get(k));
			if (!repeated && hasDocComment(line, state))
				return true;
			state = JavaLineScanner.stateAfter(line, state);
		}
		// past a shared prefix only the declarators are left, which canConsolidate already cleared
		return !candidate.declPrefix.equals(base.declPrefix) && hasDocComment(candidate.lines.get(declIdx), state);
	}

	/**
	 * Position (1-based line, 0-based column) of the first source character of
	 * {@code root}'s subtree.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] startPos(@Nonnull DetailAST root) {
		var line = Integer.MAX_VALUE;
		var col = Integer.MAX_VALUE;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (isSourceToken(node) && (node.getLineNo() < line || node.getLineNo() == line && node.getColumnNo() < col)) {
				line = node.getLineNo();
				col = node.getColumnNo();
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return new int[]{line, col};
	}

	/**
	 * The comment text trailing constant {@code index}: whatever sits between its last
	 * token and its {@code ,}, plus, when the next constant starts on a later line,
	 * the remainder of the {@code ,}'s own line. For the source-last constant the walk
	 * stops at the end of its own line when the list terminator sits below it, leaving
	 * the lines in between to {@link #listTailTrivia}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String trailingTrivia(@Nonnull List<String> lines, @Nonnull EnumSpan span, @Nonnull List<DetailAST> consts, int index) {
		final var end = endPos(consts.get(index));
		final var hasComma = span.commaLine(index) > 0;
		final var ownLine = lines.get(end[0] - 1);
		final var terminatorBelow = !hasComma && span.endLine() > end[0];
		final var stopLine = hasComma ? span.commaLine(index) : (terminatorBelow ? end[0] : span.endLine());
		final var stopCol = hasComma
				? span.commaCol(index)
				: (terminatorBelow ? ownLine.codePointCount(0, ownLine.length()) : span.endCol() - span.terminal().length());
		if (stopLine < end[0] || (stopLine == end[0] && stopCol < end[1]))
			return "";
		var trivia = sourceBetween(lines, end[0], end[1], stopLine, stopCol);
		if (hasComma && index + 1 < consts.size() && consts.get(index + 1).getLineNo() != span.commaLine(index)) {
			final var raw = lines.get(span.commaLine(index) - 1);
			trivia += raw.substring(Math.min(charIndexOf(raw, span.commaCol(index) + 1), raw.length()));
		}
		if (index + 1 == consts.size()) {
			// a trailing `,` and the list terminator can be separated by comments; only what
			// sits on the `,`'s own line belongs to this constant
			if (hasComma && !span.terminal().isEmpty()) {
				final var raw = lines.get(span.commaLine(index) - 1);
				final var to = span.commaLine(index) == span.endLine()
						? charIndexOf(raw, span.endCol() - span.terminal().length())
						: raw.length();
				trivia += raw.substring(Math.min(charIndexOf(raw, span.commaCol(index) + 1), to), to);
			}
			final var raw = lines.get(span.endLine() - 1);
			final var tail = raw.substring(Math.min(charIndexOf(raw, span.endCol()), raw.length()));
			if (isCommentOnly(tail))
				trivia += tail;
		}
		return trivia.strip();
	}

	/**
	 * Why {@link #orderGroup} found no order. Without a dependency cycle between names,
	 * a field depending on a later chunk is the cause (an anonymous-initializer field
	 * can wait on a free plain field that itself waits across chunks); failing that, a
	 * cycle only exists between whole multi-variable declarations.
	 */
	@CheckReturnValue
	@Nonnull
	private static String unorderableReason(@Nonnull List<FieldEntry> group, @Nonnull Map<String, Set<String>> nameDeps) {
		final var chunkByName = new HashMap<String, Integer>();
		for (var f : group) {
			for (var name : f.names) {
				if (FieldSortingCheck.transitiveDependencies(name, nameDeps).contains(name))
					return SkipMessages.FIELD_SORTING_SKIP_DEPENDENCY_CYCLE;
				chunkByName.put(name, f.chunk);
			}
		}
		for (var f : group) {
			for (var dep : f.dependencies) {
				if (chunkByName.getOrDefault(dep, f.chunk) > f.chunk)
					return SkipMessages.FIELD_SORTING_SKIP_DEPENDENCY_LATER_CHUNK;
			}
		}
		return SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_DEPENDENCY;
	}

	/**
	 * {@code result} when it only re-lays the members out and keeps every pair of fields that may
	 * not trade places in order, else a skip. Every rewrite here is rebuilt from line text, and
	 * the re-parse is what turns a misreading into a skip rather than corrupt output.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt verified(@Nonnull List<String> lines, @Nonnull FixResult result) {
		if (!FieldRewriteVerifier.preserves(lines, result))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_UNVERIFIED);
		return FieldRewriteVerifier.keepsInitializationOrder(lines, result) ? result : new SkipResult(SkipMessages.FIELD_SORTING_SKIP_PINNED);
	}

	@CheckReturnValue
	private static boolean violationIsEnumConstant(@Nonnull DetailAST objBlock, int lineIndex, int column) {
		for (var c = objBlock.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ENUM_CONSTANT_DEF
					&& c.getLineNo() == lineIndex + 1 && c.getColumnNo() == column)
				return true;
		}
		return false;
	}

	/** {@code dependencies} with each name's {@code pins} added, as one map of what each name waits for. */
	@CheckReturnValue
	@Nonnull
	private static Map<String, Set<String>> withPins(@Nonnull Map<String, Set<String>> dependencies, @Nonnull Map<String, Set<String>> pins) {
		final var waitsFor = new HashMap<String, Set<String>>();
		for (var entry : dependencies.entrySet())
			waitsFor.put(entry.getKey(), new HashSet<>(entry.getValue()));
		for (var entry : pins.entrySet())
			waitsFor.computeIfAbsent(entry.getKey(), key -> new HashSet<>()).addAll(entry.getValue());
		return waitsFor;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		return FixerAst.withAst(
				lines,
				root -> {
					final var objBlock = FieldSortingCheck.objblockAt(root, lineIndex, column);
					if (objBlock == null)
						return null;
					return violationIsEnumConstant(objBlock, lineIndex, column)
							? reconstructEnumFromAst(lines, objBlock)
							: fixFieldOrder(lines, objBlock, lineIndex);
				}
		);
	}
}