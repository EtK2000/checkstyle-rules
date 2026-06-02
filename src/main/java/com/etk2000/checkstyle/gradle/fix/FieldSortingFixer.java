package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for FieldSortingCheck violations. Sorts enum constants alphabetically
 * and splits same-line constants. Also sorts field declarations by chunk,
 * type, and name.
 *
 * <p>The rule (which field/constant sorts where) is read from
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
	 * declaration folded into one entry). The rule-relevant facts ({@code chunk},
	 * {@code isStatic}, {@code sortType}, {@code annotations},
	 * {@code typeArgAnnotations}, {@code anonInit}) come from
	 * {@link FieldSortingCheck#classifyField}; {@code modifiers} is the
	 * declaration's modifier keyword set, which the check's rule ignores but a
	 * merge must match; {@code typeName} is the full generic
	 * type source text used only for consolidation identity (two fields merge only
	 * when their full type text is textually equal). {@code declPrefix} is the
	 * source text of the declaration line up to the first declarator name (indent +
	 * modifiers + type + trailing space), used by the rebuild to render a merged or
	 * reordered declarator list.
	 *
	 * <p>{@code cStyleArray} marks a declaration where any declarator carries its
	 * own C-style brackets ({@code int y[], x;}). Both rebuild routes render a
	 * declarator list as {@code declPrefix + names}, which carries no per-declarator
	 * suffix, so such a declaration can be moved as whole lines but never rebuilt or
	 * merged into another one: {@code int zebra, beta[];} consolidated with
	 * {@code int alpha;} would silently retype {@code beta} to {@code int}.
	 *
	 * <p>{@code declLineOffset} is the index within {@code lines} of the line the
	 * declaration proper starts on (the first declarator name's line); the lines
	 * before it are the entry's stacked annotations and attached comments. It is
	 * read off the AST rather than by scanning for an annotation/comment prefix,
	 * because a declaration can begin on a line that opens with an annotation
	 * ({@code @Deprecated private int y, x;}) or start below a multi-line
	 * annotation whose continuation lines open with neither.
	 */
	private record FieldEntry(
			@Nonnull List<String> annotations,
			@Nonnull Set<Integer> modifiers,
			@Nonnull String name,
			@Nonnull List<String> names,
			@Nonnull String typeName,
			@Nonnull String sortType,
			@Nonnull List<List<String>> typeArgAnnotations,
			int chunk,
			boolean isStatic,
			boolean anonInit,
			boolean hasInitializer,
			boolean cStyleArray,
			@Nonnull String declPrefix,
			@Nonnull List<String> lines,
			@Nonnull Set<String> dependencies,
			int startLineIndex,
			int declLineOffset
	) {}

	private static final Pattern FIELD_PATTERN = Pattern.compile(
			"^\\s*+(?:(?:@\\w++(?:\\([^()]*+(?:\\([^()]*+\\)[^()]*+)*+\\))?+\\s++)*+)"
					+ "(?:(?:public|private|protected|static|final|transient|volatile)\\s++)*+"
					+ "((?:boolean|byte|char|double|float|int|long|short|(?:\\w++\\.)*+[A-Z]\\w*+)(?:<[^>]*+>)?+(?:\\[\\])*+)"
					+ "\\s++(\\w++)"
	);
	private static final Pattern NEWLINE = Pattern.compile("\n");

	@CheckReturnValue
	@Nonnull
	private static List<String> buildDeclLines(@Nonnull String prefix, @Nonnull List<String> names, @Nonnull String suffix, @Nonnull String baseIndent) {
		return LineLength.wrapFieldList(prefix, names, suffix, baseIndent + "\t\t");
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> buildReplacement(@Nonnull List<EnumEntry> sorted, @Nonnull String terminal) {
		final var result = new ArrayList<String>();
		for (var i = 0; i < sorted.size(); ++i) {
			final var entry = sorted.get(i);
			result.addAll(entry.leadingLines());
			for (var j = 0; j < entry.contentLines().size(); ++j) {
				var line = entry.contentLines().get(j);
				if (j == entry.contentLines().size() - 1) {
					line += i < sorted.size() - 1 ? "," : terminal;
					if (!entry.trailing().isEmpty())
						line += " " + entry.trailing();
				}
				result.add(line);
			}
		}
		return result;
	}

	@CheckReturnValue
	private static boolean canConsolidate(@Nonnull FieldEntry entry) {
		final var declLine = entry.lines.getLast();
		final var stripped = declLine.stripLeading();
		return !entry.cStyleArray && !stripped.contains("=") && entry.lines.size() <= 2
				&& JavaLineScanner.firstLineComment(declLine, JavaLineScanner.LexerState.NONE) < 0;
	}

	/**
	 * True when {@code candidate} can be folded into {@code base}'s declarator
	 * list. The merged declaration keeps only {@code base}'s prefix, so every part
	 * of that prefix has to match: the modifier set as well as the type and the
	 * annotations. The check's own rule is blind to modifiers beyond {@code final}
	 * and {@code static} (they carry no sort order), so a merge across them would
	 * silently retype {@code transient}/{@code volatile} or widen visibility
	 * without the check ever objecting.
	 */
	@CheckReturnValue
	private static boolean canMergeWith(@Nonnull FieldEntry base, @Nonnull FieldEntry candidate) {
		return candidate.chunk == base.chunk
				&& candidate.typeName.equals(base.typeName)
				&& candidate.modifiers.equals(base.modifiers)
				&& candidate.annotations.equals(base.annotations)
				&& candidate.typeArgAnnotations.equals(base.typeArgAnnotations)
				&& canConsolidate(candidate)
				&& candidate.dependencies.isEmpty()
				&& base.dependencies.isEmpty()
				&& !hasNonAnnotationLeadingLines(candidate);
	}

	/**
	 * Converts an AST column (code points) to an index into {@code line}'s chars,
	 * clamped to the line so a position just past the last token still slices.
	 */
	@CheckReturnValue
	private static int charIndexOf(@Nonnull String line, int column) {
		return LineText.charIndexOfColumn(line, Math.clamp(column, 0, line.codePointCount(0, line.length())));
	}

	/**
	 * Collects into {@code out} every identifier in {@code root}'s subtree that
	 * names a field in {@code names}. Unlike the check's {@code fieldDependencies}
	 * (which skips anonymous-class bodies, since references there are deferred to
	 * call time), this walks the whole initializer, so an anonymous-class field
	 * that mentions another field in its body is treated as referencing it. The
	 * fixer needs the broader set to refuse reordering an anon-class field ahead
	 * of a field it references.
	 */
	private static void collectFieldRefs(@Nonnull DetailAST root, @Nonnull Set<String> names, @Nonnull Set<String> out) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.IDENT && names.contains(node.getText()))
				out.add(node.getText());
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
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
			// only real source tokens carry a length; imaginary container tokens
			// (empty ANNOTATIONS/ELIST/MODIFIERS ...) whose text is the token
			// name would inflate the end column, so skip them. An IDENT is always
			// a real token, and its text is user-chosen, so a constant or field
			// named `IDENT` must not be mistaken for one of them
			if (node.getFirstChild() == null
					&& (node.getType() == TokenTypes.IDENT || !node.getText().equals(TokenUtil.getTokenName(node.getType())))) {
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

	@Nullable
	private static FixAttempt fixFieldOrder(@Nonnull List<String> lines, @Nonnull DetailAST objBlock, int lineIndex) {
		final var stripped = precomputeStrippedLines(lines);
		final var fields = parseFieldsFromAst(lines, stripped, objBlock);
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

		if (group.size() == 1)
			return sortSingleEntry(group.getFirst(), stripped);

		// static fields carrying annotations aren't safely reorderable by this
		// text rebuild (the modifier/annotation interplay across the group can't
		// be reproduced faithfully)
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

		final var sorted = orderGroup(group);
		if (sorted == null)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_DEPENDENCY_CYCLE);

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
		for (var k = declStart; k <= lastIdx; ++k) {
			final var raw = entry.lines.get(k);
			final var strp = stripped[entry.startLineIndex + k];
			final var codeEnd = k == lastIdx && semi >= 0 ? semi : raw.length();
			// a comment inside declPrefix (indent/modifiers/type, before the first
			// name) travels with the rebuilt prefix, so only scan from the first
			// name onward on the declaration line
			final var scanStart = k == declStart ? entry.declPrefix.length() : 0;
			final var end = Math.min(codeEnd, Math.min(raw.length(), strp.length()));
			for (var i = scanStart; i < end; ++i) {
				if (raw.charAt(i) != strp.charAt(i))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns true when {@code entry.lines} carries any leading line that is not
	 * an annotation. Consolidation drops the candidate's leading lines (only the
	 * base's leading annotations remain), so a candidate carrying Javadoc, a
	 * block comment, or a trailing-line line-comment above its decl would lose
	 * that comment on merge. The decl line itself is excluded from the scan.
	 */
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

	@CheckReturnValue
	private static boolean isCommentOnly(@Nonnull String text) {
		return JavaLineScanner.stripCommentsAndStrings(text, JavaLineScanner.LexerState.NONE).isBlank();
	}

	private static boolean isSubsumedByConsolidation(@Nonnull String stripped, @Nonnull List<String> replacement) {
		final var matcher = FIELD_PATTERN.matcher(stripped);
		if (!matcher.find())
			return false;
		final var afterName = JavaLineScanner.stripCommentsAndStrings(stripped.substring(matcher.end()), JavaLineScanner.LexerState.NONE).stripLeading();
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
	 * Index of the constant's trailing {@code ,}/{@code ;} separator in
	 * {@code line}, or -1 when the line has none. A separator counts only at the
	 * top level of the constant: not inside argument parens ({@code B(1, 2)}),
	 * not inside an enum-constant body ({@code INSTANCE { a, b }}), and not inside
	 * a string/char literal or comment. Paren/brace depth may start negative
	 * because callers pass the last physical line of a multi-line constant (e.g.
	 * {@code "),"}), so the top level is depth {@code <= 0}. Like its callers this
	 * does not classify text blocks; the input must be text-block-stripped or a
	 * single-line construct with no text-block opener.
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
	 * Extends upward from the declaration's first source line over any comment
	 * or block-comment continuation lines directly above it (blanks and code
	 * stop the walk), returning the 0-based index the field's line span should
	 * start at. Annotations are already part of the AST node span, so only
	 * comments are gathered here.
	 */
	@CheckReturnValue
	private static int leadingLookback(@Nonnull List<String> lines, @Nonnull String[] stripped, int declFirstLine) {
		var start = declFirstLine;
		for (var j = declFirstLine - 1; j >= 0; --j) {
			final var raw = lines.get(j).stripLeading();
			if (raw.startsWith("//")) {
				start = j;
				continue;
			}
			if (stripped[j].isBlank() && !lines.get(j).isBlank()
					&& (raw.startsWith("/*") || raw.startsWith("*"))) {
				start = j;
				continue;
			}
			break;
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
	 * of its own is a note about the list, so binding it to whichever constant
	 * happened to be last in source order would make it travel with the sort.
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
	 * The modifier keywords on {@code varDef} as token types, annotations
	 * excluded (those are compared separately, by canonical key).
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<Integer> modifierKeys(@Nonnull DetailAST varDef) {
		final var result = new HashSet<Integer>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				result.add(child.getType());
		}
		return result;
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
	 * Sorts {@code group} by the check's rule (chunk, anonymous-initializer,
	 * type, annotations, type-argument annotations, name), then refines with a
	 * dependency reorder so a field is declared before any field that reads it.
	 * Returns {@code null} when a dependency cycle leaves the order unstable.
	 */
	@CheckReturnValue
	@Nullable
	private static List<FieldEntry> orderGroup(@Nonnull List<FieldEntry> group) {
		final var sorted = new ArrayList<>(group);
		sorted.sort(Comparator
				.comparingInt(FieldEntry::chunk)
				.thenComparingInt(f -> f.anonInit() ? 0 : 1)
				.thenComparing(FieldEntry::sortType, FieldSortingCheck::compareTypes)
				.thenComparing(FieldEntry::annotations, FieldSortingCheck::compareAnnotations)
				.thenComparing(FieldEntry::typeArgAnnotations, FieldSortingCheck::compareTypeArgAnnotations)
				.thenComparing(FieldEntry::name, String.CASE_INSENSITIVE_ORDER));

		// TODO: O(n^2) dependency reorder. for n > ~1000 fields this becomes
		// O(n^4) worst case (n^2 iterations of an n^2 swap loop). real classes
		// don't approach that size, so a topological-sort rewrite is deferred.
		final var sortedNames = new ArrayList<Set<String>>(sorted.size());
		for (var entry : sorted)
			sortedNames.add(new HashSet<>(entry.names()));
		var maxIter = (long) sorted.size() * sorted.size();
		var changed = true;
		while (changed && --maxIter >= 0) {
			changed = false;
			for (var i = 0; i < sorted.size(); ++i) {
				final var entry = sorted.get(i);
				for (var j = i + 1; j < sorted.size(); ++j) {
					var depends = false;
					for (var n : sortedNames.get(j)) {
						if (entry.dependencies().contains(n)) {
							depends = true;
							break;
						}
					}
					if (depends) {
						sorted.remove(i);
						sorted.add(j, entry);
						final var movedNames = sortedNames.remove(i);
						sortedNames.add(j, movedNames);
						changed = true;
						break;
					}
				}
				if (changed)
					break;
			}
		}
		return changed ? null : sorted;
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
	private static List<FieldEntry> parseFieldsFromAst(@Nonnull List<String> lines, @Nonnull String[] stripped, @Nonnull DetailAST objBlock) {
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

		final var allDefs = new ArrayList<DetailAST>();
		for (var g : groups)
			allDefs.addAll(g);
		final var depMap = FieldSortingCheck.fieldDependencies(allDefs);
		final var allNames = new HashSet<String>();
		for (var d : allDefs)
			allNames.add(FieldSortingCheck.fieldName(d));

		final var fields = new ArrayList<FieldEntry>(groups.size());
		for (var g : groups) {
			final var first = g.getFirst();
			final var info = FieldSortingCheck.classifyField(first);

			final var names = new ArrayList<String>(g.size());
			for (var d : g)
				names.add(FieldSortingCheck.fieldName(d));

			final var type = first.findFirstToken(TokenTypes.TYPE);
			final var firstIdent = first.findFirstToken(TokenTypes.IDENT);
			final var typeName = sourceBetween(
					lines, type.getLineNo(), type.getColumnNo(), firstIdent.getLineNo(), firstIdent.getColumnNo()
			).stripTrailing();
			final var identLineText = lines.get(firstIdent.getLineNo() - 1);
			final var declPrefix = identLineText.substring(0, charIndexOf(identLineText, firstIdent.getColumnNo()));

			var hasInit = false;
			var cStyleArray = false;
			for (var d : g) {
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

			final var startLineIndex = leadingLookback(lines, stripped, declFirstLine);

			final var deps = new HashSet<String>();
			for (var d : g) {
				final var dd = depMap.get(FieldSortingCheck.fieldName(d));
				if (dd != null)
					deps.addAll(dd);
			}
			if (info.anonInit()) {
				final var assign = first.findFirstToken(TokenTypes.ASSIGN);
				if (assign != null)
					collectFieldRefs(assign, allNames, deps);
			}
			deps.removeAll(names);

			fields.add(new FieldEntry(
					info.annotationKeys(),
					modifierKeys(first),
					info.name(),
					names,
					typeName,
					info.sortType(),
					info.typeArgAnnotationKeys(),
					info.chunk(),
					info.isStatic(),
					info.anonInit(),
					hasInit,
					cStyleArray,
					declPrefix,
					new ArrayList<>(lines.subList(startLineIndex, declLastLine + 1)),
					deps,
					startLineIndex,
					firstIdent.getLineNo() - 1 - startLineIndex
			));
		}
		return fields;
	}

	/**
	 * Precomputes a per-line "stripped" form for the entire file: each entry
	 * is the corresponding line with all comment, string, char, and text-block
	 * content replaced by spaces, with the incoming {@link JavaLineScanner.LexerState} threaded
	 * across lines so an unterminated block comment or text block from earlier
	 * lines is reflected in the stripped output of later lines.
	 */
	@CheckReturnValue
	@Nonnull
	private static String[] precomputeStrippedLines(@Nonnull List<String> lines) {
		final var result = new String[lines.size()];
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lines.size(); ++i) {
			final var line = lines.get(i);
			result[i] = JavaLineScanner.stripCommentsAndStrings(line, state);
			state = JavaLineScanner.stateAfter(line, state);
		}
		return result;
	}

	@CheckReturnValue
	@Nonnull
	private static String prefixed(@Nonnull String trivia, @Nonnull String text) {
		return trivia.isEmpty() ? text : trivia + " " + text;
	}

	/**
	 * Rebuilds a contiguous same-group field run into sorted order, inserting a
	 * blank line between chunks and consolidating mergeable same-type
	 * declarations. Returns {@code null} when the rebuild is a no-op or would drop
	 * a structural line, or a {@link SkipResult} for a known conflict reordering
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

			final var groupNames = new ArrayList<>(entry.names);
			var j2 = i2 + 1;
			while (j2 < sorted.size() && canMergeWith(entry, sorted.get(j2))) {
				groupNames.addAll(sorted.get(j2).names);
				++j2;
			}
			groupNames.sort(String.CASE_INSENSITIVE_ORDER);

			if (groupNames.size() == 1) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}

			for (var k = 0; k < entry.lines.size() - 1; ++k)
				replacement.add(entry.lines.get(k));
			replacement.addAll(buildDeclLines(entry.declPrefix, groupNames, ";", LineText.extractIndent(entry.lines.getLast())));
			i2 = j2;
		}

		final var original = new ArrayList<>(lines.subList(startIdx, endIdx + 1));
		if (replacement.equals(original)) {
			// the sorted+dependency-reordered order equals the source, so the
			// check's violation can't be resolved by reordering. Distinguish the
			// known unfixable conflicts for a precise skip reason.
			// anon-class init field with a body reference: the check wants the
			// anon field first, but it references a field that must precede it
			for (var f : group) {
				if (!f.dependencies.isEmpty() && f.anonInit)
					return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_ANON_CLASS_REFERENCED_FIELD);
			}
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
			return null;

		return new FixResult(startIdx, endIdx, replacement);
	}

	/**
	 * Rebuilds an enum's constants from the AST: emits each constant on its own line,
	 * reordered alphabetically. Only the character span the constants occupy is
	 * replaced (from just past the enum's opening brace to past the trailing
	 * {@code ;}/{@code ,}), so text sharing the first or last line of that span (the
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
	 * <p>Returns {@code null} when there are fewer than two constants or the rebuild
	 * equals the source, and a {@link SkipResult} when a constant's trivia cannot be
	 * relocated: an unclosed comment or text block, or multi-line trailing trivia.
	 */
	@Nullable
	private static FixAttempt reconstructEnumFromAst(@Nonnull List<String> lines, @Nonnull DetailAST objBlock) {
		final var consts = new ArrayList<DetailAST>();
		for (var c = objBlock.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ENUM_CONSTANT_DEF)
				consts.add(c);
		}
		if (consts.size() < 2)
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

		// the sort moves each constant together with the trivia it owns, so every unit,
		// and the header and tail that stay put, has to be lexically self-contained.
		// A comment opening in one unit and closing in another travels away from its
		// terminator and swallows whatever the sort moves in between, which for a
		// header-line opener yields an enum that still compiles minus a constant.
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

		final var sorted = new ArrayList<>(entries);
		sorted.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
		final var replacement = new ArrayList<String>();
		replacement.add(prefix.stripTrailing());
		replacement.addAll(buildReplacement(sorted, span.terminal()));
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

		final var original = new ArrayList<>(lines.subList(startLine - 1, span.endLine()));
		if (replacement.equals(original))
			return null;
		return new FixResult(startLine - 1, span.endLine() - 1, replacement);
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
		return new FixResult(startIdx, endIdx, replacement);
	}

	/**
	 * Reorders the names of a single multi-name declaration into case-insensitive
	 * order in place. The type/modifier prefix is {@code entry.declPrefix}; the
	 * terminator plus any trailing comment is preserved as the suffix. Returns
	 * {@code null} when the terminator can't be located or the rebuild reproduces
	 * the source.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult sortNamesWithin(@Nonnull FieldEntry entry, @Nonnull List<String> sortedNames, @Nonnull String[] stripped) {
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
		rebuilt.addAll(buildDeclLines(entry.declPrefix, sortedNames, suffix, LineText.extractIndent(fieldLines.get(declStart))));
		if (rebuilt.equals(fieldLines))
			return null;
		return new FixResult(entry.startLineIndex, entry.startLineIndex + fieldLines.size() - 1, rebuilt);
	}

	/**
	 * Handles a group of one field entry: only a multi-variable declaration with
	 * out-of-order names is reorderable. Returns a {@link SkipResult} for the
	 * unsafe multi-var forms (initialized, or carrying an interior comment) and
	 * {@code null} when there is nothing to reorder.
	 */
	@Nullable
	private static FixAttempt sortSingleEntry(@Nonnull FieldEntry entry, @Nonnull String[] stripped) {
		if (entry.names.size() < 2)
			return null;
		final var sortedNames = new ArrayList<>(entry.names);
		sortedNames.sort(String.CASE_INSENSITIVE_ORDER);
		// the unsafe-form guards run before the already-sorted test: a declaration the
		// check flagged for some other reason (chunk order, declarator type) but whose
		// names happen to be in order is still unfixable, and must say why rather than
		// fall through to the pipeline's generic "not fixable"
		if (entry.hasInitializer)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_INITIALIZED);
		if (hasInteriorComment(entry, stripped))
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_MULTI_VAR_COMMENT);
		if (entry.cStyleArray)
			return new SkipResult(SkipMessages.FIELD_SORTING_SKIP_C_STYLE_ARRAY);
		if (sortedNames.equals(entry.names))
			return null;
		return sortNamesWithin(entry, sortedNames, stripped);
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
	 * True when a direct {@code ENUM_CONSTANT_DEF} child of {@code objBlock} sits
	 * at the reported {@code (lineIndex, column)} (0-based), meaning the violation
	 * is an enum-constant ordering/same-line issue rather than a field issue.
	 */
	@CheckReturnValue
	private static boolean violationIsEnumConstant(@Nonnull DetailAST objBlock, int lineIndex, int column) {
		for (var c = objBlock.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ENUM_CONSTANT_DEF
					&& c.getLineNo() == lineIndex + 1 && c.getColumnNo() == column)
				return true;
		}
		return false;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// a null result covers both an unparseable buffer and no field/enum member at
		// the reported position (a synthetic or stale column); the pipeline records
		// it as not-fixable rather than a topic skip reason
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