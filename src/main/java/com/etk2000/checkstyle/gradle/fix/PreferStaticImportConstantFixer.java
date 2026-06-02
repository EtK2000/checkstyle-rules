package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FqnResolver.resolveFqcn;
import static com.etk2000.checkstyle.gradle.fix.FqnResolver.stripCommentsAndBom;
import static com.etk2000.checkstyle.gradle.fix.FqnResolver.stripCommentsForClassification;

import com.etk2000.checkstyle.AstUtil;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.PreferStaticImportConstantCheck;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Removes a {@code static final X = Class.Member;} alias field and requests
 * {@code import static <FQCN>.<Member>;} to replace the lost name. Handles
 * parenthesized RHS ({@code (Class.Member)}), fully-qualified RHS
 * ({@code com.foo.Class.Member}), and nested-class RHS
 * ({@code Outer.Inner.Member}). Surrounding blank lines collapse via
 * {@link LineDeletion}.
 *
 * <p>Skips with an explicit reason rather than silently, for the cases the
 * check intentionally still fires on: an unresolvable or non-alias initializer,
 * a non-private field (may be referenced externally), a conflicting existing
 * static import, a rename that would collide with or shadow an existing
 * identifier, a multi-variable declaration carrying content that can't be
 * rebuilt safely, and a split assignment (field declared blank-final and
 * assigned in a {@code static { ... }} block, which needs two non-contiguous
 * edits that the single-range FixResult can't express). See
 * {@code prefer.static.import.constant.skip*} in {@code messages.properties}
 * for each reason's wording.</p>
 */
class PreferStaticImportConstantFixer implements CheckstyleFixer {
	@FunctionalInterface
	interface ParseFn {
		@Nonnull
		DetailAST parse(@Nonnull List<String> lines) throws CheckstyleException;
	}

	private record MultiVarParse(int firstNameStart, @Nonnull List<VarSegment> segments) {}

	/** A single identifier-to-identifier substitution applied by {@link #rewriteOutsideLiterals}. */
	private record Rewrite(@Nonnull String target, @Nonnull String replacement) {}

	private record VarSegment(@Nonnull String name, @Nonnull String rhs) {}

	private static final LexerState IN_BLOCK_COMMENT = new LexerState(true, false);

	/**
	 * @return {@code true} when the file already contains
	 * {@code import static <prefix>.<memberName>;} where {@code <prefix>}
	 * is something other than {@code expectedFqcnPrefix}. Adding our
	 * own static import would create a duplicate-member compile error.
	 */
	@CheckReturnValue
	private static boolean conflictsWithExistingStaticImport(
			@Nonnull List<String> lines,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull String expectedFqcnPrefix,
			@Nonnull String memberName
	) {
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (inTextBlockMask[lineIdx])
				continue;
			final var stripped = stripCommentsAndBom(lines.get(lineIdx), lineIdx);
			final var parsed = ImportLine.parse(stripped);
			if (parsed == null || !parsed.staticImport() || parsed.wildcard())
				continue;
			final var fqn = parsed.fqn();
			final var lastDot = fqn.lastIndexOf('.');
			if (lastDot <= 0 || lastDot == fqn.length() - 1)
				continue;
			final var importedMember = fqn.substring(lastDot + 1);
			if (!importedMember.equals(memberName))
				continue;
			final var importedPrefix = fqn.substring(0, lastDot);
			if (!importedPrefix.equals(expectedFqcnPrefix))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsFinalToken(@Nonnull String text) {
		final var len = text.length();
		var i = 0;
		while (i < len) {
			while (i < len && Character.isWhitespace(text.charAt(i)))
				++i;
			final var start = i;
			while (i < len && !Character.isWhitespace(text.charAt(i)))
				++i;
			if (i - start == 5 && "final".regionMatches(0, text, start, 5))
				return true;
		}
		return false;
	}

	/**
	 * Drops blank lines from a replacement that consists entirely of blanks.
	 * When a field deletion and a sibling static initializer deletion are
	 * separated only by a blank line (the typical {@code field;\n\nstatic
	 * { ... }} spacing), the in-between blank survives the slice loop into
	 * the replacement list. Returning an empty replacement in that case lets
	 * the class body collapse cleanly instead of leaving a stranded blank
	 * line. If the replacement contains any non-blank content (e.g. sibling
	 * fields between the deleted ones), it's kept verbatim so the user's
	 * spacing around real content isn't disturbed.
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> dropAllBlankReplacement(@Nonnull List<String> replacement) {
		for (var line : replacement) {
			if (!line.isBlank())
				return replacement;
		}
		return replacement.isEmpty() ? replacement : List.of();
	}

	@CheckReturnValue
	@Nullable
	private static String extractIdentForward(@Nonnull String line, int col) {
		if (col < 0 || col >= line.length() || !Character.isJavaIdentifierStart(line.charAt(col)))
			return null;
		var i = col;
		while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
			++i;
		return line.substring(col, i);
	}

	/**
	 * Post-processes the basic field/static-block deletion so the resulting
	 * source matches the converged pipeline output without relying on
	 * follow-up fixers. Three cosmetic concerns are handled here:
	 *
	 * <ul>
	 *   <li><b>Stranded blank line residue</b> between a deleted field and
	 *       a deleted sibling static initializer.</li>
	 *   <li><b>Empty record body</b> ({@code record X(...) { field }} where
	 *       the only member was the deleted field) collapses the braces
	 *       to {@code {}} on the {@code record} line.</li>
	 *   <li><b>Trailing semicolon on the last enum constant</b> when the
	 *       deleted field was the only non-constant member.</li>
	 * </ul>
	 *
	 * <p>When AST inspection isn't possible (re-parse fails or
	 * {@code fieldDef} is null), the helper still strips stranded blank
	 * lines from the start/end of the replacement so cinit deletions
	 * don't leave a hollow class body.</p>
	 */
	@CheckReturnValue
	@Nonnull
	static FixResult finalizeBodyFix(
			@Nonnull List<String> lines,
			@Nullable DetailAST fieldDef,
			@Nullable DetailAST staticInit,
			int baseStart,
			int baseEnd,
			@Nonnull List<String> baseReplacement,
			@Nonnull Set<String> importsToAdd
	) {
		final var stripped = dropAllBlankReplacement(baseReplacement);
		final var defaultResult = new FixResult(baseStart, baseEnd, stripped, importsToAdd);
		if (fieldDef == null)
			return defaultResult;
		final var objBlock = fieldDef.getParent();
		if (objBlock == null || objBlock.getType() != TokenTypes.OBJBLOCK)
			return defaultResult;
		final var parent = objBlock.getParent();
		if (parent == null)
			return defaultResult;
		final var parentType = parent.getType();

		final var remainingEnumConsts = new ArrayList<DetailAST>();
		var hasOtherRemaining = false;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child == fieldDef || child == staticInit)
				continue;
			final var t = child.getType();
			if (t == TokenTypes.LCURLY || t == TokenTypes.RCURLY || t == TokenTypes.SEMI || t == TokenTypes.EMPTY_STAT)
				continue;
			if (t == TokenTypes.ENUM_CONSTANT_DEF) {
				remainingEnumConsts.add(child);
				continue;
			}
			hasOtherRemaining = true;
			break;
		}
		if (hasOtherRemaining)
			return defaultResult;

		if (parentType == TokenTypes.ENUM_DEF && !remainingEnumConsts.isEmpty())
			return finalizeEnumTrailingSemi(lines, objBlock, remainingEnumConsts, baseStart, baseEnd, stripped, importsToAdd, defaultResult);

		if (!remainingEnumConsts.isEmpty())
			return defaultResult;
		if (parentType != TokenTypes.RECORD_DEF)
			return defaultResult;
		return finalizeEmptyRecordBody(lines, objBlock, baseStart, baseEnd, importsToAdd, defaultResult);
	}

	@CheckReturnValue
	@Nonnull
	private static FixResult finalizeEmptyRecordBody(
			@Nonnull List<String> lines,
			@Nonnull DetailAST objBlock,
			int baseStart,
			int baseEnd,
			@Nonnull Set<String> importsToAdd,
			@Nonnull FixResult defaultResult
	) {
		final var lcurly = objBlock.findFirstToken(TokenTypes.LCURLY);
		final var rcurly = objBlock.findFirstToken(TokenTypes.RCURLY);
		if (lcurly == null || rcurly == null)
			return defaultResult;
		final var lcurlyLine = lcurly.getLineNo() - 1;
		final var lcurlyCol = lcurly.getColumnNo();
		final var rcurlyLine = rcurly.getLineNo() - 1;
		final var rcurlyCol = rcurly.getColumnNo();
		if (lcurlyLine < 0 || rcurlyLine >= lines.size() || lcurlyLine >= rcurlyLine)
			return defaultResult;
		if (lcurlyLine < baseStart - 1 || rcurlyLine > baseEnd + 1)
			return defaultResult;

		final var openText = lines.get(lcurlyLine);
		if (lcurlyCol >= openText.length() || openText.charAt(lcurlyCol) != '{')
			return defaultResult;
		if (!openText.substring(lcurlyCol + 1).isBlank())
			return defaultResult;

		final var closeText = lines.get(rcurlyLine);
		if (rcurlyCol >= closeText.length() || closeText.charAt(rcurlyCol) != '}')
			return defaultResult;
		if (!closeText.substring(0, rcurlyCol).isBlank())
			return defaultResult;

		final var collapsed = openText.substring(0, lcurlyCol) + "{}" + closeText.substring(rcurlyCol + 1);
		return new FixResult(lcurlyLine, rcurlyLine, List.of(collapsed), importsToAdd);
	}

	@CheckReturnValue
	@Nonnull
	private static FixResult finalizeEnumTrailingSemi(
			@Nonnull List<String> lines,
			@Nonnull DetailAST objBlock,
			@Nonnull List<DetailAST> remainingEnumConsts,
			int baseStart,
			int baseEnd,
			@Nonnull List<String> strippedReplacement,
			@Nonnull Set<String> importsToAdd,
			@Nonnull FixResult defaultResult
	) {
		DetailAST trailingSemi = null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.SEMI) {
				trailingSemi = child;
				break;
			}
		}
		if (trailingSemi == null)
			return defaultResult;
		final var semiLine = trailingSemi.getLineNo() - 1;
		final var semiCol = trailingSemi.getColumnNo();
		if (semiLine < 0 || semiLine >= lines.size())
			return defaultResult;
		if (semiLine >= baseStart && semiLine <= baseEnd)
			return defaultResult;
		final var semiText = lines.get(semiLine);
		if (semiCol < 0 || semiCol >= semiText.length() || semiText.charAt(semiCol) != ';')
			return defaultResult;
		if (!semiText.substring(semiCol + 1).isBlank())
			return defaultResult;

		final var lastConst = remainingEnumConsts.getLast();
		final var lastConstLine = lastConst.getLineNo() - 1;
		if (lastConstLine == semiLine) {
			final var newSemiLine = (semiText.substring(0, semiCol) + semiText.substring(semiCol + 1)).stripTrailing();
			final var newStart = Math.min(baseStart, semiLine);
			final var newEnd = Math.max(baseEnd, semiLine);
			final var newReplacement = new ArrayList<String>(newEnd - newStart + 1);
			for (var i = newStart; i <= newEnd; ++i) {
				if (i == semiLine) {
					newReplacement.add(newSemiLine);
					continue;
				}
				if (i >= baseStart && i <= baseEnd) {
					final var localIdx = i - baseStart;
					if (localIdx < strippedReplacement.size())
						newReplacement.add(strippedReplacement.get(localIdx));
					continue;
				}
				if (i > semiLine && i < baseStart && lines.get(i).isBlank())
					continue;
				newReplacement.add(lines.get(i));
			}
			return new FixResult(newStart, newEnd, newReplacement, importsToAdd);
		}

		// Defensive: structurally unreachable in well-formed enums. Enum
		// constants always appear before the terminating `;` in the
		// declaration; the `;` always appears before non-constant members
		// (the deleted field). If we reach here, lastConstLine <= semiLine and
		// semiLine < baseStart hold from the grammar. Keep the guard so a
		// malformed AST can't crash the fixer.
		if (lastConstLine > semiLine || semiLine >= baseStart)
			return defaultResult;
		if (!semiText.substring(0, semiCol).isBlank())
			return defaultResult;
		var dropStart = semiLine;
		while (dropStart > lastConstLine + 1 && lines.get(dropStart - 1).isBlank())
			--dropStart;
		final var newStart = Math.min(baseStart, dropStart);
		final var newEnd = Math.max(baseEnd, semiLine);
		final var newReplacement = new ArrayList<String>(newEnd - newStart + 1);
		for (var i = newStart; i <= newEnd; ++i) {
			if (i >= dropStart && i <= semiLine)
				continue;
			if (i >= baseStart && i <= baseEnd) {
				final var localIdx = i - baseStart;
				if (localIdx < strippedReplacement.size())
					newReplacement.add(strippedReplacement.get(localIdx));
				continue;
			}
			if (i > semiLine && i < baseStart && lines.get(i).isBlank())
				continue;
			newReplacement.add(lines.get(i));
		}
		return new FixResult(newStart, newEnd, newReplacement, importsToAdd);
	}

	/**
	 * Scans backward from the violation line to find the line that contains
	 * the {@code final} keyword. For multi-var declarations on continuation
	 * lines, the modifiers (and {@code final}) live on an earlier line; this
	 * helper finds that line so the full declaration can be stitched. Lines
	 * that begin inside a multi-line text block or block comment are skipped
	 * so the word {@code final} appearing in such content is not mistaken
	 * for a real modifier.
	 */
	@CheckReturnValue
	private static int findDeclarationStart(
			@Nonnull List<String> lines,
			int violationLine,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull boolean[] inBlockCommentMask
	) {
		for (var i = violationLine; i >= 0; --i) {
			if (inTextBlockMask[i] || inBlockCommentMask[i])
				continue;
			if (containsFinalToken(stripAnnotations(stripCommentsForClassification(lines.get(i), inTextBlockMask[i]))))
				return i;
		}
		return violationLine;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findFieldDef(@Nullable DetailAST root, @Nonnull String name, int lineNo, int columnNo) {
		final var stack = new ArrayDeque<DetailAST>();
		for (var sibling = root; sibling != null; sibling = sibling.getNextSibling())
			stack.push(sibling);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.VARIABLE_DEF) {
				final var parent = node.getParent();
				if (parent != null && parent.getType() == TokenTypes.OBJBLOCK) {
					final var ident = node.findFirstToken(TokenTypes.IDENT);
					if (ident != null && name.equals(ident.getText())
							&& ident.getLineNo() == lineNo
							&& ident.getColumnNo() == columnNo)
						return node;
				}
			}
			for (var c = node.getFirstChild(); c != null; c = c.getNextSibling())
				stack.push(c);
		}
		return null;
	}

	/**
	 * Parses {@code lines} and locates the {@link DetailAST} VARIABLE_DEF
	 * whose identifier sits at the given {@code lineIndex} / {@code column}.
	 * Returns {@code null} when the file can't be parsed cleanly (mid-fix
	 * sibling damage) or no field at that location exists; the caller falls
	 * back to a textual-only path.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findFieldDefForInline(@Nonnull List<String> lines, int lineIndex, int column) {
		return findFieldDefForInlineUsing(lines, lineIndex, column, PreferStaticImportConstantFixer::parseLinesToAst);
	}

	@CheckReturnValue
	@Nullable
	static DetailAST findFieldDefForInlineUsing(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull ParseFn parser
	) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var lineText = lines.get(lineIndex);
		final var name = extractIdentForward(lineText, column);
		if (name == null)
			return null;
		final DetailAST root;
		try {
			root = parser.parse(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError |
		       AssertionError ignored) {
			return null;
		}
		return findFieldDef(root, name, lineIndex + 1, column);
	}

	/**
	 * If the file can't be parsed (e.g. mid-fix syntax errors from a sibling
	 * fixer), returns a conservative
	 * {@code "potential shadow (file does not parse cleanly)"} so the caller
	 * still bails.
	 *
	 * <p>The {@code skipLines1} set holds the 1-based line numbers occupied by
	 * the field declaration (and, for the cinit path, the cinit assignment
	 * line). An exact set, rather than a contiguous range, is used so that
	 * nested-class members appearing on lines between a field declaration and
	 * its sibling static initializer are still detected as shadows.</p>
	 */
	@CheckReturnValue
	@Nullable
	private static String findShadowKind(
			@Nonnull List<String> lines,
			@Nonnull String name,
			@Nonnull Set<Integer> skipLines1
	) {
		return findShadowKindUsing(lines, name, skipLines1, PreferStaticImportConstantFixer::parseLinesToAst);
	}

	@CheckReturnValue
	@Nullable
	static String findShadowKindUsing(
			@Nonnull List<String> lines,
			@Nonnull String name,
			@Nonnull Set<Integer> skipLines1,
			@Nonnull ParseFn parser
	) {
		final DetailAST root;
		try {
			root = parser.parse(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError |
		       AssertionError ignored) {
			return "potential shadow (file does not parse cleanly)";
		}
		return walkForShadow(root, name, skipLines1);
	}

	/**
	 * @return {@code {lineIndex, columnOfSemicolon}}, or {@code {-1, -1}} if no
	 * terminating {@code ;} at paren-depth 0 is found before EOF.
	 */
	// TODO: extend caller to propagate a text-block mask so subsequent lines that
	// start mid-text-block are honored (callers currently pass the start line's
	// mask bit only; defensive observation, no realistic exploit).
	@CheckReturnValue
	@Nonnull
	static int[] findStatementEnd(
			@Nonnull List<String> lines,
			int startLine,
			boolean startsInTextBlock,
			boolean startsInBlockComment
	) {
		var parenDepth = 0;
		var state = new LexerState(startsInBlockComment, startsInTextBlock);
		for (var i = startLine; i < lines.size(); ++i) {
			final var line = lines.get(i);
			final var masked = JavaLineScanner.stripCommentsAndStrings(line, state);
			for (var j = 0; j < masked.length(); ++j) {
				final var c = masked.charAt(j);
				if (c == '(')
					++parenDepth;
				else if (c == ')') {
					if (--parenDepth < 0)
						return new int[]{-1, -1};
				}
				else if (c == ';' && parenDepth == 0)
					return new int[]{i, j};
			}
			final var next = JavaLineScanner.stateAfter(line, state);
			// String/char literals don't cross newlines in valid Java. If the line ends inside one,
			// the source is malformed; bail rather than continuing on the next line. stateAfter
			// doesn't carry string/char state, so detect it via the sentinel probe: end-of-line sits
			// inside a literal/comment (the appended char is masked away), but not a block comment or
			// text block (those legitimately continue), and the line carries no comment marker.
			if (!next.inBlockComment() && !next.inTextBlock()
					&& JavaLineScanner.stripCommentsAndStrings(line + "X", state).charAt(line.length()) == ' '
					&& JavaLineScanner.firstCommentMarker(line, state) < 0)
				return new int[]{-1, -1};
			state = next;
		}
		return new int[]{-1, -1};
	}

	/**
	 * Handles a violation on a blank-final field whose value is assigned in a
	 * sibling {@code static { ... }} block. Re-parses the file's AST to locate
	 * the cinit assignment statement (matching either bare {@code FIELD = ...}
	 * or {@code EnclosingClass.FIELD = ...}). Returns a {@link FixResult}
	 * spanning {@code [fieldLine..cinitLine]} that removes both the field
	 * declaration and the cinit assignment statement, preserving everything
	 * in between verbatim. Bails to {@code SKIP_CINIT} when the AST won't
	 * parse, no matching cinit assignment is found, the assignment shares a
	 * line with other statements, or its RHS can't be parsed as an alias.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixCinit(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			int endLine,
			int endCol,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull boolean[] inBlockCommentMask
	) {
		final var fieldName = extractIdentForward(lines.get(lineIndex), column);
		if (fieldName == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final DetailAST root;
		try {
			root = parseLinesToAst(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError |
		       AssertionError ignored) {
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		}

		final var fieldDef = findFieldDef(root, fieldName, lineIndex + 1, column);
		if (fieldDef == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var objBlock = fieldDef.getParent();
		if (objBlock == null || objBlock.getType() != TokenTypes.OBJBLOCK)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var enclosingTypeName = AstUtil.getEnclosingTypeName(objBlock);
		final var packageName = AstUtil.getPackageName(root);
		final var cinitAssign = PreferStaticImportConstantCheck.findStaticInitAssign(objBlock, fieldName);
		if (cinitAssign == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var cinitLine = cinitAssign.getLineNo() - 1;
		if (cinitLine < 0 || cinitLine >= lines.size())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var cinitLineText = lines.get(cinitLine);
		// TODO: a literal '=' or ';' in a string/char literal earlier on the
		// line could misalign these scans, though the AST already ensures the
		// line is a real assignment; a comment-/literal-aware scan would be
		// more robust.
		final var rhsEqPos = cinitLineText.indexOf('=');
		final var rhsSemiPos = cinitLineText.indexOf(';', rhsEqPos + 1);
		if (rhsEqPos < 0 || rhsSemiPos < 0)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var lhsNode = cinitAssign.getFirstChild();
		if (lhsNode == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		var leftmostLhs = lhsNode;
		while (leftmostLhs.getFirstChild() != null)
			leftmostLhs = leftmostLhs.getFirstChild();
		final var lhsLeftmostColumn = leftmostLhs.getColumnNo();
		if (lineIndex == cinitLine && leftmostLhs.getLineNo() - 1 != cinitLine)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		if (lineIndex != cinitLine) {
			final var trailing = stripCommentsForClassification(cinitLineText.substring(rhsSemiPos + 1));
			if (!trailing.isEmpty())
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		}

		final var beforeAssignStart = lineIndex == cinitLine ? lhsLeftmostColumn : 0;
		if (beforeAssignStart < 0 || beforeAssignStart > rhsEqPos)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		final var beforeAssign = stripCommentsForClassification(cinitLineText.substring(beforeAssignStart, rhsEqPos));
		final var beforeAssignTrimmed = beforeAssign.replaceAll("\\s+", "");
		final var qualifiedLhs = enclosingTypeName == null ? null : enclosingTypeName + "." + fieldName;
		final var fqLhs = (packageName == null || qualifiedLhs == null) ? null : packageName + "." + qualifiedLhs;
		if (!beforeAssignTrimmed.equals(fieldName)
				&& (qualifiedLhs == null || !beforeAssignTrimmed.equals(qualifiedLhs))
				&& (fqLhs == null || !beforeAssignTrimmed.equals(fqLhs)))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var rhsText = cinitLineText.substring(rhsEqPos + 1, rhsSemiPos).strip();
		final var aliasParts = parseAliasFromRhs(rhsText);
		if (aliasParts == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		final var classChain = aliasParts[0];
		final var constName = aliasParts[1];

		final var stitched = stitchDeclaration(lines, lineIndex, endLine);
		final var fieldDeclEndInStitched = stitched.length() - lines.get(endLine).length() + endCol + 1;
		final var fieldDeclOnly = stitched.substring(0, fieldDeclEndInStitched);
		if (!hasPrivateModifier(fieldDeclOnly))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY);

		final var fqcn = resolveFqcn(lines, inTextBlockMask, classChain, FixContext.getFilePath());
		if (fqcn == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (conflictsWithExistingStaticImport(lines, inTextBlockMask, fqcn, constName))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT);

		final var renameNeeded = !fieldName.equals(constName);
		if (renameNeeded) {
			// Build an exact line set covering ONLY the field declaration's own
			// lines (annotations/modifiers above the IDENT through the field's
			// terminating semicolon) and the cinit assignment line, so the
			// field-decl IDENT and the cinit-LHS IDENT aren't mistakenly
			// reported as shadows of themselves. Using a contiguous range would
			// swallow nested-class same-name fields living on lines between the
			// field decl and the cinit block.
			final var astFieldLineForShadow = Math.max(0, fieldDef.getLineNo() - 1);
			final var fieldDeclStartForShadow = Math.min(lineIndex, astFieldLineForShadow);
			final var shadowSkipLines1 = new HashSet<Integer>();
			for (var i = fieldDeclStartForShadow; i <= endLine; ++i)
				shadowSkipLines1.add(i + 1);
			shadowSkipLines1.add(cinitLine + 1);
			final var shadowKind = findShadowKind(lines, fieldName, shadowSkipLines1);
			if (shadowKind != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(shadowKind)
				);
			}
			if (findShadowKind(lines, constName, shadowSkipLines1) != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_RENAME_TARGET.formatted(constName)
				);
			}
		}

		final var importsToAdd = Set.of("static " + fqcn + "." + constName);

		DetailAST staticInit = null;
		DetailAST staticInitRcurly = null;
		final var cinitExpr = cinitAssign.getParent();
		if (cinitExpr != null) {
			final var slist = cinitExpr.getParent();
			if (slist != null && slist.getType() == TokenTypes.SLIST) {
				var onlyCinit = true;
				for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child == cinitExpr
							|| child.getType() == TokenTypes.RCURLY
							|| child.getType() == TokenTypes.SEMI
							|| child.getType() == TokenTypes.EMPTY_STAT)
						continue;
					onlyCinit = false;
					break;
				}
				if (onlyCinit) {
					final var maybeStaticInit = slist.getParent();
					final var lastChild = slist.getLastChild();
					if (maybeStaticInit != null && maybeStaticInit.getType() == TokenTypes.STATIC_INIT
							&& lastChild != null && lastChild.getType() == TokenTypes.RCURLY
							&& maybeStaticInit.getLineNo() - 1 >= 0
							&& maybeStaticInit.getLineNo() - 1 < lines.size()
							&& lastChild.getLineNo() - 1 >= 0
							&& lastChild.getLineNo() - 1 < lines.size()
							&& lastChild.getLineNo() >= maybeStaticInit.getLineNo()
							&& !staticBlockHasComments(
							lines,
							maybeStaticInit.getLineNo() - 1,
							lastChild.getLineNo() - 1,
							maybeStaticInit.getColumnNo(),
							lastChild.getColumnNo(),
							cinitLine,
							lhsLeftmostColumn,
							rhsSemiPos
					)) {
						staticInit = maybeStaticInit;
						staticInitRcurly = lastChild;
					}
				}
			}
		}

		final var targets = renameNeeded
				? List.of(classChain + "." + constName, fieldName)
				: List.of(classChain + "." + constName);

		if (lineIndex == cinitLine) {
			if (fieldDef.getLineNo() - 1 != lineIndex)
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
			final var fieldDeclStart = fieldDef.getColumnNo();
			final var lineText = lines.get(lineIndex);
			final int spliceMidEndCol;
			final int spliceTailStartCol;
			if (staticInit != null) {
				if (staticInit.getLineNo() - 1 != cinitLine
						|| staticInitRcurly.getLineNo() - 1 != cinitLine)
					return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
				spliceMidEndCol = staticInit.getColumnNo();
				spliceTailStartCol = staticInitRcurly.getColumnNo() + 1;
			}
			else {
				spliceMidEndCol = lhsLeftmostColumn;
				spliceTailStartCol = rhsSemiPos + 1;
			}
			if (fieldDeclStart < 0 || fieldDeclStart > endCol
					|| endCol >= spliceMidEndCol
					|| spliceMidEndCol > lineText.length()
					|| spliceTailStartCol < spliceMidEndCol
					|| spliceTailStartCol > lineText.length())
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
			final var rawNewLine = lineText.substring(0, fieldDeclStart)
					+ lineText.substring(endCol + 1, spliceMidEndCol)
					+ lineText.substring(spliceTailStartCol);
			if (!renameNeeded)
				return new FixResult(lineIndex, lineIndex, List.of(rawNewLine), importsToAdd);
			final var rewrittenSameLine = rewriteOutsideLiterals(List.of(rawNewLine), -1, -1, targets, constName);
			final var newLine = rewrittenSameLine.getOrDefault(0, rawNewLine);
			final var modifiedLines = rewriteOutsideLiterals(lines, lineIndex, lineIndex, targets, constName);
			if (modifiedLines.isEmpty())
				return new FixResult(lineIndex, lineIndex, List.of(newLine), importsToAdd);
			final var rangeStart = Math.min(lineIndex, modifiedLines.firstKey());
			final var rangeEnd = Math.max(lineIndex, modifiedLines.lastKey());
			final var replacement = new ArrayList<String>();
			for (var i = rangeStart; i <= rangeEnd; ++i) {
				if (i == lineIndex)
					replacement.add(newLine);
				else
					replacement.add(modifiedLines.getOrDefault(i, lines.get(i)));
			}
			return new FixResult(rangeStart, rangeEnd, replacement, importsToAdd);
		}

		final var staticStartLine = staticInit == null ? -1 : staticInit.getLineNo() - 1;
		final var staticEndLine = staticInit == null ? -1 : staticInitRcurly.getLineNo() - 1;
		if (fieldDef.getLineNo() <= 0)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		if (!stripCommentsForClassification(lines.get(endLine).substring(endCol + 1)).isEmpty())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		final var astFieldLine = Math.max(0, fieldDef.getLineNo() - 1);
		final var fieldDeclStartLine = Math.min(lineIndex, astFieldLine);
		if (fieldDeclStartLine < lineIndex) {
			final var fieldStartLineText = lines.get(fieldDeclStartLine);
			final var rawFieldCol = Math.max(0, fieldDef.getColumnNo());
			final var fieldStartCol = Math.min(rawFieldCol, fieldStartLineText.length());
			final var prefix = fieldStartLineText.substring(0, fieldStartCol);
			if (!stripCommentsForClassification(prefix, inTextBlockMask[fieldDeclStartLine]).isBlank())
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		}
		for (var i = fieldDeclStartLine + 1; i < lineIndex; ++i) {
			// Lines that begin mid-text-block or mid-block-comment have no
			// source-level tokens; `hasCommentMarker` would scan the line as
			// if it were code and could fire on `//` characters that are
			// actually content of a text block or comment that opened on an
			// earlier line.
			if (inTextBlockMask[i] || inBlockCommentMask[i])
				continue;
			final var line = lines.get(i);
			if (hasCommentMarker(line, 0, line.length()))
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		}

		var rangeStart = Math.min(fieldDeclStartLine, cinitLine);
		var rangeEnd = Math.max(endLine, cinitLine);
		if (staticInit != null) {
			rangeStart = Math.min(rangeStart, staticStartLine);
			rangeEnd = Math.max(rangeEnd, staticEndLine);
		}
		final var modifiedLines = renameNeeded
				? rewriteOutsideLiterals(lines, fieldDeclStartLine, endLine, targets, constName)
				: new TreeMap<Integer, String>();
		if (!modifiedLines.isEmpty()) {
			rangeStart = Math.min(rangeStart, modifiedLines.firstKey());
			rangeEnd = Math.max(rangeEnd, modifiedLines.lastKey());
		}
		final var replacement = new ArrayList<String>();
		for (var i = rangeStart; i <= rangeEnd; ++i) {
			if (i >= fieldDeclStartLine && i <= endLine)
				continue;
			if (i == cinitLine)
				continue;
			if (staticInit != null && i >= staticStartLine && i <= staticEndLine)
				continue;
			replacement.add(modifiedLines.getOrDefault(i, lines.get(i)));
		}
		return finalizeBodyFix(lines, fieldDef, staticInit, rangeStart, rangeEnd, replacement, importsToAdd);
	}

	/**
	 * Handles a violation on one variable of a multi-variable declaration by
	 * deleting EVERY convertible alias in that declaration and adding a static
	 * import for each, so the declaration converges in a single pass. Converting
	 * only the reported variable rewrites the whole declaration, which invalidates
	 * the coordinates the sibling violations were reported at: their fixes were
	 * then dropped and their imports lost. Non-alias variables stay in place; when
	 * none remain the declaration is deleted outright.
	 *
	 * <p>The rebuild preserves the modifiers+type prefix verbatim from the
	 * original source (including annotations and in-prefix block comments)
	 * and each kept variable's RHS verbatim (including string/char literals
	 * and inline block comments). Bails only when a decl line contains a
	 * {@code //} line comment, because stitching collapses newlines and the
	 * line-comment would then extend through the rest of the declaration.</p>
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixMultiVar(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			int endLine,
			int endCol,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull boolean[] inBlockCommentMask
	) {
		final var varName = extractIdentForward(lines.get(lineIndex), column);
		if (varName == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);

		// The fix() entry check strips comments before testing trailing
		// content, so a `; /* note */` survives that gate. The multi-var
		// rebuild discards everything after the `;`, dropping the comment.
		// Bail on raw non-whitespace content after the `;` so the trailing
		// comment isn't silently lost.
		final var endLineText = lines.get(endLine);
		if (endCol + 1 < endLineText.length()
				&& !endLineText.substring(endCol + 1).isBlank())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);

		final var declStart = findDeclarationStart(lines, lineIndex, inTextBlockMask, inBlockCommentMask);
		// The AST's VARIABLE_DEF starts above `declStart` when modifiers or annotations sit on
		// their own line above the `final` keyword. Those lines belong to the declaration being
		// dropped, so both the deletion span and every scan below have to reach them.
		final var fieldDef = findFieldDefForInline(lines, lineIndex, column);
		final var declTop = fieldDef == null
				? declStart
				: Math.clamp(fieldDef.getLineNo() - 1, 0, declStart);
		// Deleting from a `declTop` that begins inside a block comment would strand the
		// unterminated opener on a prior line (the multi-var path has no IDENT-line guard).
		if (inBlockCommentMask[declTop])
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);
		for (var i = declTop; i <= endLine; ++i) {
			if (inTextBlockMask[i] || inBlockCommentMask[i])
				continue;
			if (hasUnconfinedLineComment(lines.get(i)))
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);
		}

		final var stitched = stitchDeclaration(lines, declStart, endLine);
		final var parse = parseMultiVarSegments(stitched);
		if (parse == null || parse.segments().size() < 2)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);

		var targetIndex = -1;
		for (var i = 0; i < parse.segments().size(); ++i) {
			if (parse.segments().get(i).name().equals(varName)) {
				targetIndex = i;
				break;
			}
		}
		if (targetIndex < 0)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);

		final var aliasParts = parseAliasFromRhs(parse.segments().get(targetIndex).rhs());
		if (aliasParts == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);
		final var classChain = aliasParts[0];
		final var constName = aliasParts[1];

		if (!hasPrivateModifier(stitched))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY);

		final var fqcn = resolveFqcn(lines, inTextBlockMask, classChain, FixContext.getFilePath());
		if (fqcn == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (conflictsWithExistingStaticImport(lines, inTextBlockMask, fqcn, constName))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT);

		final var convertible = new ArrayList<Integer>();
		final var claimedMembers = new LinkedHashMap<String, String>();
		final var renamedLocals = new ArrayList<String>();
		final var renamedMembers = new ArrayList<String>();
		final var siblingImports = new TreeSet<String>();
		final var siblingRewrites = new ArrayList<Rewrite>();
		for (var i = 0; i < parse.segments().size(); ++i) {
			final var seg = parse.segments().get(i);
			final var parts = i == targetIndex ? aliasParts : parseAliasFromRhs(seg.rhs());
			if (parts == null)
				continue;
			final var segFqcn = i == targetIndex
					? fqcn
					: resolveFqcn(lines, inTextBlockMask, parts[0], FixContext.getFilePath());
			if (segFqcn == null || conflictsWithExistingStaticImport(lines, inTextBlockMask, segFqcn, parts[1]))
				continue;
			// Two aliases of the same member name from different classes cannot both be
			// static-imported. Converting one and leaving the other declared just hands the
			// collision to that sibling's own violation, whose conflict scan runs before this
			// pass's imports are inserted and so cannot see them.
			final var claimedBy = claimedMembers.putIfAbsent(parts[1], segFqcn);
			if (claimedBy != null && !claimedBy.equals(segFqcn))
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT);
			convertible.add(i);
			siblingImports.add("static " + segFqcn + "." + parts[1]);
			siblingRewrites.add(new Rewrite(parts[0] + "." + parts[1], parts[1]));
			if (!seg.name().equals(parts[1])) {
				siblingRewrites.add(new Rewrite(seg.name(), parts[1]));
				renamedLocals.add(seg.name());
				renamedMembers.add(parts[1]);
			}
		}

		// every converted alias loses its local name, so each rename has to clear the same
		// shadow and collision checks, not just the reported variable's
		if (!renamedLocals.isEmpty()) {
			final var shadowSkipLines = new HashSet<Integer>();
			for (var i = declTop; i <= endLine; ++i)
				shadowSkipLines.add(i + 1);
			for (var renamed : renamedLocals) {
				final var shadowKind = findShadowKind(lines, renamed, shadowSkipLines);
				if (shadowKind != null) {
					return new SkipResult(
							SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(shadowKind)
					);
				}
			}
			for (var member : renamedMembers) {
				if (findShadowKind(lines, member, shadowSkipLines) != null) {
					return new SkipResult(
							SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_RENAME_TARGET.formatted(member)
					);
				}
			}
		}

		final var importsToAdd = Set.copyOf(siblingImports);
		final var modifiedLines = rewriteOutsideLiterals(lines, declTop, endLine, siblingRewrites);

		// the rebuild below would emit a bare `private static final int ;`
		if (convertible.size() == parse.segments().size()) {
			if (modifiedLines.isEmpty()) {
				final var baseDeletion = LineDeletion.deleteRange(lines, declTop, endLine, importsToAdd);
				if (baseDeletion == null)
					return null;
				return finalizeBodyFix(
						lines,
						fieldDef,
						null,
						baseDeletion.startLine(),
						baseDeletion.endLine(),
						baseDeletion.replacement(),
						importsToAdd
				);
			}
			final var wideStart = Math.min(declTop, modifiedLines.firstKey());
			final var wideEnd = Math.max(endLine, modifiedLines.lastKey());
			final var wide = new ArrayList<String>();
			for (var i = wideStart; i <= wideEnd; ++i) {
				if (i >= declTop && i <= endLine)
					continue;
				wide.add(modifiedLines.getOrDefault(i, lines.get(i)));
			}
			return new FixResult(wideStart, wideEnd, wide, importsToAdd);
		}

		final var sb = new StringBuilder();
		sb.append(stitched, 0, parse.firstNameStart());
		var first = true;
		for (var i = 0; i < parse.segments().size(); ++i) {
			if (convertible.contains(i))
				continue;
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(parse.segments().get(i).name()).append(" = ").append(parse.segments().get(i).rhs());
		}
		sb.append(';');
		final var newDecl = sb.toString();

		final var rangeStart = modifiedLines.isEmpty()
				? declStart
				: Math.min(declStart, modifiedLines.firstKey());
		final var rangeEnd = modifiedLines.isEmpty()
				? endLine
				: Math.max(endLine, modifiedLines.lastKey());
		final var newDeclLines = List.of(newDecl.split("\n", -1));
		final var replacement = new ArrayList<String>();
		for (var i = rangeStart; i <= rangeEnd; ++i) {
			if (i == declStart)
				replacement.addAll(newDeclLines);
			else if (i > declStart && i <= endLine)
				continue;
			else
				replacement.add(modifiedLines.getOrDefault(i, lines.get(i)));
		}
		return new FixResult(rangeStart, rangeEnd, replacement, importsToAdd);
	}

	/**
	 * Scans {@code text} for a real {@code //} or {@code /*} comment marker
	 * whose start column falls within {@code [from, to)}. The scan tracks
	 * string, char, and text-block literal state from the beginning of the
	 * line so a {@code //} sitting inside {@code "//"} or {@code """ ... //
	 * ... """} is not mistaken for a comment marker.
	 */
	@CheckReturnValue
	static boolean hasCommentMarker(@Nonnull String text, int from, int to) {
		final var upper = Math.min(to, text.length());
		final var lower = Math.max(0, from);
		var offset = 0;
		while (offset < text.length()) {
			final var rel = JavaLineScanner.firstCommentMarker(text.substring(offset), LexerState.NONE);
			if (rel < 0)
				return false;
			final var col = offset + rel;
			if (col >= lower && col < upper)
				return true;
			if (col >= upper)
				return false;
			// Marker sits before the range. A `//` runs to end of line, so no marker can start in
			// the range after it; a `/*` is skipped past its close and the scan continues.
			if (text.charAt(col + 1) == '/')
				return false;
			final var closeRel = JavaLineScanner.multilineLiteralCloseIndex(text.substring(col + 2), IN_BLOCK_COMMENT);
			if (closeRel < 0)
				return false;
			offset = col + 2 + closeRel;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasPrivateModifier(@Nonnull String declarationText) {
		final var stripped = stripAnnotations(stripCommentsForClassification(declarationText));
		final var eq = stripped.indexOf('=');
		final var prefix = eq < 0 ? stripped : stripped.substring(0, eq);
		for (var token : prefix.split("\\s+")) {
			if (token.equals("private"))
				return true;
		}
		return false;
	}

	/**
	 * State-aware scan returning {@code true} when {@code line} contains a
	 * real {@code //} line-comment marker (i.e. one not sitting inside a
	 * string, char, text-block, or block-comment literal). Used by
	 * {@link #fixMultiVar} to bail when stitching the declaration would
	 * collapse newlines and let the {@code //} comment extend through the
	 * rest of the declaration.
	 */
	@CheckReturnValue
	private static boolean hasUnconfinedLineComment(@Nonnull String line) {
		var offset = 0;
		while (offset < line.length()) {
			final var rel = JavaLineScanner.firstCommentMarker(line.substring(offset), LexerState.NONE);
			if (rel < 0)
				return false;
			final var col = offset + rel;
			if (line.charAt(col + 1) == '/')
				return true;
			final var closeRel = JavaLineScanner.multilineLiteralCloseIndex(line.substring(col + 2), IN_BLOCK_COMMENT);
			if (closeRel < 0)
				return false;
			offset = col + 2 + closeRel;
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static String identifyLambdaShadow(@Nonnull DetailAST node, @Nonnull String name) {
		for (var c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.IDENT && name.equals(c.getText()))
				return "lambda parameter";
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String identifyParameterDefShadow(@Nonnull DetailAST node, @Nonnull String name) {
		if (!matchesIdent(node, name))
			return null;
		final var parent = node.getParent();
		if (parent == null)
			return "parameter in an unrecognized context";
		if (parent.getType() == TokenTypes.LITERAL_CATCH)
			return "catch parameter";
		if (parent.getType() == TokenTypes.LAMBDA)
			return "lambda parameter";
		if (parent.getType() == TokenTypes.PARAMETERS) {
			final var grandparent = parent.getParent();
			if (grandparent != null) {
				if (grandparent.getType() == TokenTypes.METHOD_DEF)
					return "method parameter";
				if (grandparent.getType() == TokenTypes.CTOR_DEF)
					return "constructor parameter";
				if (grandparent.getType() == TokenTypes.LAMBDA)
					return "lambda parameter";
			}
		}
		return "parameter in an unrecognized context";
	}

	@CheckReturnValue
	@Nullable
	private static String identifyShadow(
			@Nonnull DetailAST node,
			@Nonnull String name,
			@Nonnull Set<Integer> skipLines1
	) {
		return switch (node.getType()) {
			case TokenTypes.ANNOTATION_DEF ->
					matchesIdent(node, name) ? "annotation type with the same name" : null;
			case TokenTypes.CLASS_DEF ->
					matchesIdent(node, name) ? "nested class with the same name" : null;
			case TokenTypes.ENUM_CONSTANT_DEF ->
					matchesIdent(node, name) ? "enum constant with the same name" : null;
			case TokenTypes.ENUM_DEF ->
					matchesIdent(node, name) ? "nested enum with the same name" : null;
			case TokenTypes.INTERFACE_DEF ->
					matchesIdent(node, name) ? "nested interface with the same name" : null;
			case TokenTypes.LAMBDA -> identifyLambdaShadow(node, name);
			case TokenTypes.METHOD_DEF ->
					matchesIdent(node, name) ? "method with the same name" : null;
			case TokenTypes.PARAMETER_DEF -> identifyParameterDefShadow(node, name);
			case TokenTypes.RECORD_DEF ->
					matchesIdent(node, name) ? "nested record with the same name" : null;
			case TokenTypes.RESOURCE ->
					matchesIdent(node, name) ? "try-with-resources variable" : null;
			case TokenTypes.TYPE_PARAMETER ->
					matchesIdent(node, name) ? "type parameter with the same name" : null;
			case TokenTypes.VARIABLE_DEF -> identifyVariableDefShadow(node, name, skipLines1);
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String identifyVariableDefShadow(
			@Nonnull DetailAST node,
			@Nonnull String name,
			@Nonnull Set<Integer> skipLines1
	) {
		if (!matchesIdent(node, name))
			return null;
		if (skipLines1.contains(node.getLineNo()))
			return null;
		final var parent = node.getParent();
		if (parent == null)
			return "variable in an unrecognized context";
		return switch (parent.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> "for-each variable";
			case TokenTypes.FOR_INIT -> "for-loop variable";
			case TokenTypes.OBJBLOCK -> "field with the same name";
			default -> "local variable";
		};
	}

	/**
	 * Detects whether an identifier match at {@code matchStart} of length
	 * {@code matchLen} on {@code line} is actually a Java labelled-statement
	 * label rather than an expression reference. A label has whitespace (or
	 * start-of-line) before the identifier and a single {@code :} (not
	 * {@code ::}, which is a method reference) as the next non-whitespace
	 * character.
	 *
	 * <p>Two expression-context shapes also produce {@code IDENT : non-colon}
	 * and must be excluded so the rename pass doesn't skip a real reference:
	 * <ul>
	 *   <li>Ternary expressions ({@code flag ? IDENT : 0}): the preceding
	 *       non-whitespace character is {@code ?}, marking the true-branch of
	 *       a ternary. The {@code :} is the ternary's separator, not a label
	 *       boundary.</li>
	 *   <li>Switch case labels ({@code case IDENT:}): the word {@code case}
	 *       precedes the identifier with only whitespace between. The
	 *       {@code :} terminates the case label, not a labelled statement.</li>
	 * </ul>
	 *
	 * <p>TODO: Single-line scope only. A {@code case} keyword on the prior
	 * line ({@code case\n\tIDENT:}) is not detected as a case label, because
	 * the lookback only inspects the current line. Multi-line context would
	 * need to be threaded in via the rewrite callsite (currently
	 * {@code rewriteOutsideLiterals}). Tightening this contract is feasible
	 * but invasive; deferred until a fixture surfaces the regression.</p>
	 */
	@CheckReturnValue
	static boolean isLabelOccurrence(@Nonnull String line, int matchStart, int matchLen, char prev) {
		if (matchStart != 0 && !Character.isWhitespace(prev))
			return false;
		var j = matchStart + matchLen;
		while (j < line.length() && Character.isWhitespace(line.charAt(j)))
			++j;
		if (j >= line.length() || line.charAt(j) != ':')
			return false;
		if (j + 1 < line.length() && line.charAt(j + 1) == ':')
			return false;
		var k = matchStart - 1;
		while (k >= 0) {
			// Skip back over `/* ... */` block comments preceding the
			// identifier. Without this, a `case /* note */ FOO:` lookback would
			// see `*/` as the four characters preceding the identifier and miss
			// the case label.
			if (k >= 1 && line.charAt(k) == '/' && line.charAt(k - 1) == '*') {
				var m = k - 2;
				while (m >= 1 && !(line.charAt(m) == '*' && line.charAt(m - 1) == '/'))
					--m;
				if (m < 1)
					break;
				k = m - 2;
				continue;
			}
			if (!Character.isWhitespace(line.charAt(k)))
				break;
			--k;
		}
		if (k >= 0 && line.charAt(k) == '?')
			return false;
		final var caseEnd = k + 1;
		return caseEnd < 4 || !"case".regionMatches(0, line, caseEnd - 4, 4)
				|| (caseEnd - 4 != 0 && Character.isJavaIdentifierPart(line.charAt(caseEnd - 5)));
	}

	/**
	 * Detects {@code TYPE A = ..., B = ...;} where the check fires once per
	 * variable but the fixer can only edit one contiguous range. Two shapes
	 * indicate a multi-var declaration:
	 * <ul>
	 *   <li>A {@code ,} at paren-depth 0 between the first {@code =} and the
	 *       terminating {@code ;} (seen when the violation is on the first
	 *       variable; the stitched declaration spans the whole statement).</li>
	 *   <li>No {@code final} keyword in the prefix before the first {@code =}
	 *       (seen when the violation is on a continuation line; the stitched
	 *       declaration starts from that line and misses the modifiers of the
	 *       overall declaration).</li>
	 * </ul>
	 * Commas inside generic type arguments ({@code Map<String, Integer>}) sit
	 * before the first {@code =}, so they don't trigger the depth-0 scan.
	 */
	@CheckReturnValue
	private static boolean isMultiVariableDeclaration(@Nonnull String declarationText, boolean startsInTextBlock) {
		final var text = stripAnnotations(stripCommentsForClassification(declarationText, startsInTextBlock));
		final var eq = text.indexOf('=');
		if (eq < 0)
			return false;
		var hasFinal = false;
		for (var token : text.substring(0, eq).split("\\s+")) {
			if (token.equals("final")) {
				hasFinal = true;
				break;
			}
		}
		if (!hasFinal)
			return true;
		var depth = 0;
		for (var i = eq + 1; i < text.length(); ++i) {
			final var c = text.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')')
				--depth;
			else if (depth == 0 && c == ',')
				return true;
			else if (depth == 0 && c == ';')
				return false;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean matchesIdent(@Nonnull DetailAST node, @Nonnull String name) {
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && name.equals(ident.getText());
	}

	/**
	 * Parses {@code = [(]+ Ident ( . Ident )+ [)]+ ;} starting from the first
	 * {@code =} in the stitched declaration.
	 *
	 * @return {@code {classChain, memberName}} on success, where
	 * {@code classChain} joins all segments before the final dot with
	 * {@code "."}. Returns {@code null} when the RHS isn't a pure
	 * parenthesized dot chain.
	 */
	@CheckReturnValue
	@Nullable
	private static String[] parseAlias(@Nonnull String declarationText) {
		final var text = stripAnnotations(stripCommentsForClassification(declarationText));
		final var eq = text.indexOf('=');
		if (eq < 0)
			return null;
		var pos = skipWhitespace(text, eq + 1);

		var parenDepth = 0;
		while (pos < text.length() && text.charAt(pos) == '(') {
			++parenDepth;
			pos = skipWhitespace(text, pos + 1);
		}

		final var segments = new ArrayList<String>();
		while (true) {
			if (pos >= text.length()
					|| !Character.isJavaIdentifierStart(text.charAt(pos)))
				return null;
			final var segStart = pos;
			while (pos < text.length()
					&& Character.isJavaIdentifierPart(text.charAt(pos)))
				++pos;
			segments.add(text.substring(segStart, pos));
			pos = skipWhitespace(text, pos);
			if (pos >= text.length() || text.charAt(pos) != '.')
				break;
			pos = skipWhitespace(text, pos + 1);
		}
		if (segments.size() < 2)
			return null;

		while (pos < text.length() && text.charAt(pos) == ')') {
			if (parenDepth == 0)
				return null;
			--parenDepth;
			pos = skipWhitespace(text, pos + 1);
		}
		if (parenDepth != 0)
			return null;

		if (pos >= text.length() || text.charAt(pos) != ';')
			return null;

		final var member = segments.removeLast();
		return new String[]{String.join(".", segments), member};
	}

	@CheckReturnValue
	@Nullable
	private static String[] parseAliasFromRhs(@Nonnull String rhs) {
		return parseAlias("X = " + rhs + ";");
	}

	@CheckReturnValue
	@Nonnull
	static DetailAST parseLinesToAst(@Nonnull List<String> lines) throws CheckstyleException {
		return JavaParser.parseFileText(
				new FileText(new File("synthetic.java"), lines),
				JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	/**
	 * Extracts the local field name from a stitched declaration. The field
	 * name is the identifier token immediately before the {@code =}. Comments
	 * and annotations are stripped first so annotation arguments can't be
	 * mistaken for the field name.
	 *
	 * @return the field name, or {@code null} when the declaration shape is
	 * unparseable.
	 */
	@CheckReturnValue
	@Nullable
	private static String parseLocalFieldName(@Nonnull String declarationText) {
		final var stripped = stripAnnotations(stripCommentsForClassification(declarationText));
		final var eq = stripped.indexOf('=');
		if (eq < 0)
			return null;
		var end = eq;
		while (end > 0 && (Character.isWhitespace(stripped.charAt(end - 1))
				|| stripped.charAt(end - 1) == ']' || stripped.charAt(end - 1) == '['))
			--end;
		var start = end;
		while (start > 0 && Character.isJavaIdentifierPart(stripped.charAt(start - 1)))
			--start;
		return start < end ? stripped.substring(start, end) : null;
	}

	/**
	 * State-aware scan over the original stitched declaration that returns
	 * each variable's name and verbatim RHS, plus the position of the first
	 * variable's name (used to extract the modifiers+type prefix verbatim,
	 * preserving any annotations or in-prefix comments). Tracks string and
	 * char literals, block comments, annotations (`@Name(...)`), and `()`/`{}`
	 * depth so commas/semicolons inside these constructs don't split the
	 * declaration. Line comments (`//`) are not handled here; callers must
	 * bail upstream because stitching collapses newlines and `//` would then
	 * extend through the rest of the declaration.
	 *
	 * <p><b>Known limitation:</b> the scan operates on the raw source text and
	 * does not apply the Java unicode-escape pre-processor (JLS 3.3). A
	 * literal {@code ,} (comma) or {@code /*} (block-comment
	 * opener) embedded in an identifier or string-literal opening will not be
	 * interpreted as the corresponding character. Aliases written with such
	 * escapes are unlikely in practice (the check operates on aliases like
	 * {@code Foo.X}); if encountered, the fixer may misparse the declaration.
	 * Callers that need full lexical correctness should pre-process the source
	 * with a JLS-conformant scanner before invoking this method.</p>
	 */
	@CheckReturnValue
	@Nullable
	private static MultiVarParse parseMultiVarSegments(@Nonnull String stitched) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(stitched, LexerState.NONE);
		final var len = masked.length();
		var i = 0;
		var depth = 0;
		var lastIdentStart = -1;
		var lastIdentEnd = -1;

		// Phase 1: walk to the first depth-0 '=', remembering the last identifier run before it (the
		// first variable's name). Masking blanks string/char/comment/text-block content; annotation
		// argument lists sit at paren depth > 0, so any '='/','/';' inside them is skipped without a
		// dedicated annotation scan.
		while (i < len) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '{') {
				++depth;
				lastIdentStart = lastIdentEnd = -1;
				++i;
			}
			else if (c == ')' || c == '}') {
				if (--depth < 0)
					return null;
				lastIdentStart = lastIdentEnd = -1;
				++i;
			}
			else if (depth == 0 && c == '=')
				break;
			else if (Character.isJavaIdentifierPart(c)) {
				if (lastIdentEnd != i)
					lastIdentStart = i;
				lastIdentEnd = i + 1;
				++i;
			}
			else
				++i;
		}

		if (i >= len || lastIdentStart < 0)
			return null;

		final var firstNameStart = lastIdentStart;
		final var segments = new ArrayList<VarSegment>();
		while (true) {
			final var name = stitched.substring(lastIdentStart, lastIdentEnd);
			++i;
			final var rhsStart = i;
			while (i < len) {
				final var c = masked.charAt(i);
				if (c == '(' || c == '{')
					++depth;
				else if (c == ')' || c == '}') {
					if (--depth < 0)
						return null;
				}
				else if (depth == 0 && (c == ',' || c == ';'))
					break;
				++i;
			}
			if (i >= len)
				return null;
			segments.add(new VarSegment(name, stitched.substring(rhsStart, i).strip()));
			if (masked.charAt(i) == ';')
				return new MultiVarParse(firstNameStart, segments);
			do ++i;
			while (i < len && Character.isWhitespace(masked.charAt(i)));
			if (i >= len || !Character.isJavaIdentifierStart(masked.charAt(i)))
				return null;
			lastIdentStart = i;
			while (i < len && Character.isJavaIdentifierPart(masked.charAt(i)))
				++i;
			lastIdentEnd = i;
			while (i < len && Character.isWhitespace(masked.charAt(i)))
				++i;
			if (i >= len || masked.charAt(i) != '=')
				return null;
		}
	}

	@CheckReturnValue
	@Nonnull
	private static SortedMap<Integer, String> rewriteOutsideLiterals(
			@Nonnull List<String> lines,
			int skipStart,
			int skipEnd,
			@Nonnull List<Rewrite> rewrites
	) {
		final var modified = new TreeMap<Integer, String>();
		final var maskedLines = JavaLineScanner.maskAll(lines);
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (lineIdx >= skipStart && lineIdx <= skipEnd)
				continue;
			final var line = lines.get(lineIdx);
			final var masked = maskedLines.get(lineIdx);
			final var sb = new StringBuilder();
			var i = 0;
			while (i < line.length()) {
				var matched = false;
				for (var rewrite : rewrites) {
					final var target = rewrite.target();
					if (i + target.length() > line.length())
						continue;
					// Match on the masked line so an occurrence inside a string/char/comment/text-block
					// is not rewritten; the boundary guards still read the original line.
					if (!masked.regionMatches(i, target, 0, target.length()))
						continue;
					final var prev = i == 0 ? ' ' : line.charAt(i - 1);
					final var next = i + target.length() >= line.length()
							? ' '
							: line.charAt(i + target.length());
					if (Character.isJavaIdentifierPart(prev) || prev == '.')
						continue;
					if (Character.isJavaIdentifierPart(next))
						continue;
					if (isLabelOccurrence(line, i, target.length(), prev))
						continue;
					sb.append(rewrite.replacement());
					i += target.length();
					matched = true;
					break;
				}
				if (!matched) {
					sb.append(line.charAt(i));
					++i;
				}
			}
			if (!sb.toString().equals(line))
				modified.put(lineIdx, sb.toString());
		}
		return modified;
	}

	@CheckReturnValue
	@Nonnull
	static SortedMap<Integer, String> rewriteOutsideLiterals(
			@Nonnull List<String> lines,
			int skipStart,
			int skipEnd,
			@Nonnull List<String> targets,
			@Nonnull String replacement
	) {
		final var rewrites = new ArrayList<Rewrite>(targets.size());
		for (var target : targets)
			rewrites.add(new Rewrite(target, replacement));
		return rewriteOutsideLiterals(lines, skipStart, skipEnd, rewrites);
	}

	@CheckReturnValue
	private static int skipWhitespace(@Nonnull String s, int pos) {
		while (pos < s.length() && Character.isWhitespace(s.charAt(pos)))
			++pos;
		return pos;
	}

	@CheckReturnValue
	static boolean staticBlockHasComments(
			@Nonnull List<String> lines,
			int staticStartLine,
			int staticEndLine,
			int staticStartCol,
			int staticEndCol,
			int cinitLine,
			int lhsLeftmostColumn,
			int rhsSemiPos
	) {
		for (var i = staticStartLine; i <= staticEndLine; ++i) {
			final var line = lines.get(i);
			final var from = i == staticStartLine ? staticStartCol : 0;
			final var to = i == staticEndLine ? staticEndCol + 1 : line.length();
			if (i == staticStartLine && i != cinitLine
					&& !line.substring(0, Math.clamp(staticStartCol, 0, line.length())).isBlank())
				return true;
			if (i == staticEndLine && i != cinitLine
					&& !line.substring(Math.clamp(staticEndCol + 1, 0, line.length())).isBlank())
				return true;
			if (i == cinitLine) {
				if (hasCommentMarker(line, from, lhsLeftmostColumn)
						|| hasCommentMarker(line, rhsSemiPos + 1, to))
					return true;
			}
			else if (hasCommentMarker(line, from, to))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String stitchDeclaration(@Nonnull List<String> lines, int startLine, int endLine) {
		final var sb = new StringBuilder();
		for (var i = startLine; i <= endLine; ++i) {
			if (i > startLine)
				sb.append('\n');
			sb.append(lines.get(i));
		}
		return sb.toString();
	}

	/**
	 * Drops annotation prefixes (everything from a {@code @} to its matching
	 * closing paren, or just the {@code @Name} token if there's no paren) so
	 * a string-literal annotation argument containing the word "private"
	 * can't masquerade as a modifier.
	 */
	@CheckReturnValue
	@Nonnull
	private static String stripAnnotations(@Nonnull String text) {
		final var sb = new StringBuilder();
		for (var i = 0; i < text.length(); ++i) {
			final var c = text.charAt(i);
			if (c != '@') {
				sb.append(c);
				continue;
			}
			var j = i + 1;
			while (j < text.length() && Character.isJavaIdentifierPart(text.charAt(j)))
				++j;
			while (j < text.length() && Character.isWhitespace(text.charAt(j)))
				++j;
			if (j >= text.length() || text.charAt(j) != '(') {
				i = j - 1;
				continue;
			}
			var depth = 0;
			for (; j < text.length(); ++j) {
				final var cj = text.charAt(j);
				if (cj == '(')
					++depth;
				else if (cj == ')') {
					--depth;
					if (depth == 0) {
						++j;
						break;
					}
				}
			}
			i = j - 1;
		}
		return sb.toString();
	}

	/**
	 * Iterative depth-first AST walk. Avoids stack overflow on
	 * generator-produced files with thousand-level-deep call chains where a
	 * recursive walker would blow the JVM stack.
	 */
	@CheckReturnValue
	@Nullable
	private static String walkForShadow(
			@Nullable DetailAST root,
			@Nonnull String name,
			@Nonnull Set<Integer> skipLines1
	) {
		final var stack = new ArrayDeque<DetailAST>();
		for (var sibling = root; sibling != null; sibling = sibling.getNextSibling())
			stack.push(sibling);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var kind = identifyShadow(node, name, skipLines1);
			if (kind != null)
				return kind;
			for (var c = node.getFirstChild(); c != null; c = c.getNextSibling())
				stack.push(c);
		}
		return null;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var masks = FqnResolver.computeLineMasks(lines);
		final var inBlockCommentMask = masks.inBlockComment();
		final var inTextBlockMask = masks.inTextBlock();
		final var endPos = findStatementEnd(lines, lineIndex, inTextBlockMask[lineIndex], inBlockCommentMask[lineIndex]);
		final var endLine = endPos[0];
		final var endCol = endPos[1];
		if (endLine < 0)
			return null;

		final var stitched = stitchDeclaration(lines, lineIndex, endLine);
		final var fieldDeclEndInStitched = stitched.length() - lines.get(endLine).length() + endCol + 1;
		final var fieldDeclOnly = stitched.substring(0, fieldDeclEndInStitched);
		if (stripAnnotations(stripCommentsForClassification(fieldDeclOnly, inTextBlockMask[lineIndex])).indexOf('=') < 0) {
			// If the violation line starts inside a multi-line block comment
			// (the closing `*/` appears on this line), deleting the field would
			// strand the opening `/*` on a prior line. Bail rather than corrupt
			// the source.
			if (inBlockCommentMask[lineIndex])
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);
			return fixCinit(lines, lineIndex, column, endLine, endCol, inTextBlockMask, inBlockCommentMask);
		}

		final var trailing = stripCommentsForClassification(lines.get(endLine).substring(endCol + 1));
		if (!trailing.isEmpty())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (isMultiVariableDeclaration(stitched, inTextBlockMask[lineIndex]))
			return fixMultiVar(lines, lineIndex, column, endLine, endCol, inTextBlockMask, inBlockCommentMask);

		// Single-var: if the decl line starts inside a multi-line block comment,
		// deleting the line would strand the opening `/*` on a prior line. The
		// prev-line check below catches some such cases but not all (e.g.
		// when the `/*` opened multiple lines back).
		if (inBlockCommentMask[lineIndex])
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (lineIndex > 0) {
			final var prev = lines.get(lineIndex - 1).stripLeading();
			if (prev.startsWith("@") || prev.startsWith("//") || prev.startsWith("/*"))
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);
			if (prev.startsWith("*") && !prev.contains(";") && !prev.contains("="))
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);
		}

		final var parsed = parseAlias(stitched);
		if (parsed == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);
		final var classChain = parsed[0];
		final var constName = parsed[1];

		if (!hasPrivateModifier(stitched))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY);

		final var fqcn = resolveFqcn(lines, inTextBlockMask, classChain, FixContext.getFilePath());
		if (fqcn == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (conflictsWithExistingStaticImport(lines, inTextBlockMask, fqcn, constName))
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT);

		final var localFieldName = parseLocalFieldName(stitched);
		final var renameNeeded = localFieldName != null && !localFieldName.equals(constName);

		// The AST's VARIABLE_DEF can begin on a line above `lineIndex` (the IDENT
		// line) when modifiers or annotations sit on their own line(s) above the
		// field name. Those lines belong to the declaration: the shadow scan must
		// skip them (so the field's own annotation/modifier line isn't read as a
		// shadow of itself) and the deletion must reach them, since deleting only
		// the IDENT line would strand a dangling annotation/modifier (invalid Java).
		final var fieldDef = findFieldDefForInline(lines, lineIndex, column);
		final var declTop = fieldDef == null
				? lineIndex
				: Math.clamp(fieldDef.getLineNo() - 1, 0, lineIndex);
		// A block comment that opens above `declTop` and closes on it makes `declTop`
		// begin inside a comment; deleting from there would strand the unterminated
		// opener on a prior line. The IDENT-line guard above misses this when modifiers
		// push `declTop` above `lineIndex`.
		if (inBlockCommentMask[declTop])
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (renameNeeded) {
			final var shadowSkipLines1 = new HashSet<Integer>();
			for (var i = declTop; i <= endLine; ++i)
				shadowSkipLines1.add(i + 1);
			final var shadowKind = findShadowKind(lines, localFieldName, shadowSkipLines1);
			if (shadowKind != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(shadowKind)
				);
			}
			if (findShadowKind(lines, constName, shadowSkipLines1) != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_RENAME_TARGET.formatted(constName)
				);
			}
		}
		final var importsToAdd = Set.of("static " + fqcn + "." + constName);

		final var targets = renameNeeded
				? List.of(classChain + "." + constName, localFieldName)
				: List.of(classChain + "." + constName);
		final var modifiedLines = rewriteOutsideLiterals(lines, declTop, endLine, targets, constName);

		if (modifiedLines.isEmpty()) {
			final var baseDeletion = LineDeletion.deleteRange(lines, declTop, endLine, importsToAdd);
			if (baseDeletion == null)
				return null;
			return finalizeBodyFix(
					lines,
					fieldDef,
					null,
					baseDeletion.startLine(),
					baseDeletion.endLine(),
					baseDeletion.replacement(),
					importsToAdd
			);
		}

		final var rangeStart = Math.min(declTop, modifiedLines.firstKey());
		final var rangeEnd = Math.max(endLine, modifiedLines.lastKey());
		final var replacement = new ArrayList<String>();
		for (var i = rangeStart; i <= rangeEnd; ++i) {
			if (i >= declTop && i <= endLine)
				continue;
			replacement.add(modifiedLines.getOrDefault(i, lines.get(i)));
		}
		return new FixResult(rangeStart, rangeEnd, replacement, importsToAdd);
	}
}