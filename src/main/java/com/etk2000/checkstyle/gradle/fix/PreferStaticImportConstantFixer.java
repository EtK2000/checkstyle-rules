package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * <p>Skips with explicit reasons in three cases the check intentionally still
 * fires on: non-private fields (may be referenced externally), conflicting
 * existing static imports (a different constant of the same name is already
 * statically imported), and split assignment (field declared blank-final and
 * assigned in a {@code static { ... }} block, which needs two non-contiguous
 * edits that the single-range FixResult can't express).</p>
 */
class PreferStaticImportConstantFixer implements CheckstyleFixer {
	@FunctionalInterface
	interface ParseFn {
		@Nonnull
		DetailAST parse(@Nonnull List<String> lines) throws CheckstyleException;
	}

	private record MultiVarParse(int firstNameStart, @Nonnull List<VarSegment> segments) {}

	private record VarSegment(@Nonnull String name, @Nonnull String rhs) {}

	/**
	 * Forward scan of the entire file tracking text-block and block-comment
	 * state across lines. {@code mask[i]} is {@code true} when line {@code i}
	 * begins inside a multi-line text block, so per-line callers can skip
	 * text-block content.
	 */
	@CheckReturnValue
	@Nonnull
	private static boolean[] computeInTextBlockMask(@Nonnull List<String> lines) {
		final var mask = new boolean[lines.size()];
		var inTextBlock = false;
		var inBlockComment = false;
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			mask[lineIdx] = inTextBlock;
			final var line = lines.get(lineIdx);
			var inString = false;
			var inChar = false;
			var i = 0;
			while (i < line.length()) {
				final var c = line.charAt(i);
				if (inBlockComment) {
					if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
						inBlockComment = false;
						i += 2;
					}
					else
						++i;
					continue;
				}
				if (inTextBlock) {
					if (c == '\\' && i + 1 < line.length())
						i += 2;
					else if (isTripleQuote(line, i)) {
						inTextBlock = false;
						i += 3;
					}
					else
						++i;
					continue;
				}
				if (inString) {
					if (c == '\\' && i + 1 < line.length())
						i += 2;
					else if (c == '"') {
						inString = false;
						++i;
					}
					else
						++i;
					continue;
				}
				if (inChar) {
					if (c == '\\' && i + 1 < line.length())
						i += 2;
					else if (c == '\'') {
						inChar = false;
						++i;
					}
					else
						++i;
					continue;
				}
				if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
					inBlockComment = true;
					i += 2;
					continue;
				}
				if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
					break;
				if (isTextBlockOpener(line, i)) {
					inTextBlock = true;
					i += 3;
					continue;
				}
				if (c == '"') {
					inString = true;
					++i;
					continue;
				}
				if (c == '\'') {
					inChar = true;
					++i;
					continue;
				}
				++i;
			}
		}
		return mask;
	}

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
			final var line = lines.get(lineIdx);
			final var stripped = stripCommentsForClassification(line);
			if (!stripped.startsWith("import static ") || !stripped.endsWith(";"))
				continue;
			final var fqn = stripped.substring("import static ".length(), stripped.length() - 1)
					.replaceAll("\\s+", "");
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
	 * Scans backward from the violation line to find the line that contains
	 * the {@code final} keyword. For multi-var declarations on continuation
	 * lines, the modifiers (and {@code final}) live on an earlier line; this
	 * helper finds that line so the full declaration can be stitched.
	 */
	@CheckReturnValue
	private static int findDeclarationStart(
			@Nonnull List<String> lines,
			int violationLine,
			@Nonnull boolean[] inTextBlockMask
	) {
		for (var i = violationLine; i >= 0; --i) {
			if (inTextBlockMask[i])
				continue;
			if (containsFinalToken(stripAnnotations(stripCommentsForClassification(lines.get(i), false))))
				return i;
		}
		return violationLine;
	}

	@CheckReturnValue
	@Nullable
	private static String findEnclosingTypeName(@Nonnull DetailAST objBlock) {
		final var parent = objBlock.getParent();
		if (parent == null)
			return null;
		final var type = parent.getType();
		if (type != TokenTypes.CLASS_DEF && type != TokenTypes.INTERFACE_DEF
				&& type != TokenTypes.ENUM_DEF && type != TokenTypes.RECORD_DEF
				&& type != TokenTypes.ANNOTATION_DEF)
			return null;
		final var ident = parent.findFirstToken(TokenTypes.IDENT);
		return ident == null ? null : ident.getText();
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

	@CheckReturnValue
	@Nullable
	private static String findPackageName(@Nonnull DetailAST root) {
		var top = root;
		while (top.getParent() != null)
			top = top.getParent();
		for (var child = top.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.PACKAGE_DEF)
				continue;
			final var dot = child.findFirstToken(TokenTypes.DOT);
			if (dot != null) {
				final var text = FullIdent.createFullIdent(dot).getText();
				return text == null || text.isEmpty() ? null : text;
			}
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null) {
				final var text = ident.getText();
				return text.isEmpty() ? null : text;
			}
		}
		return null;
	}

	/**
	 * If the file can't be parsed (e.g. mid-fix syntax errors from a sibling
	 * fixer), returns a conservative
	 * {@code "potential shadow (file does not parse cleanly)"} so the caller
	 * still bails.
	 */
	@CheckReturnValue
	@Nullable
	private static String findShadowKind(
			@Nonnull List<String> lines,
			@Nonnull String name,
			int fieldDeclStartLine,
			int fieldDeclEndLine
	) {
		return findShadowKindUsing(lines, name, fieldDeclStartLine, fieldDeclEndLine, PreferStaticImportConstantFixer::parseLinesToAst);
	}

	@CheckReturnValue
	@Nullable
	static String findShadowKindUsing(
			@Nonnull List<String> lines,
			@Nonnull String name,
			int fieldDeclStartLine,
			int fieldDeclEndLine,
			@Nonnull ParseFn parser
	) {
		final DetailAST root;
		try {
			root = parser.parse(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError | AssertionError ignored) {
			return "potential shadow (file does not parse cleanly)";
		}
		return walkForShadow(root, name, fieldDeclStartLine + 1, fieldDeclEndLine + 1);
	}

	/**
	 * @return {@code {lineIndex, columnOfSemicolon}}, or {@code {-1, -1}} if no
	 * terminating {@code ;} at paren-depth 0 is found before EOF.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] findStatementEnd(
			@Nonnull List<String> lines,
			int startLine,
			boolean startsInTextBlock
	) {
		var parenDepth = 0;
		var inBlockComment = false;
		var inString = false;
		var inChar = false;
		var inTextBlock = startsInTextBlock;
		for (var i = startLine; i < lines.size(); ++i) {
			final var line = lines.get(i);
			for (var j = 0; j < line.length(); ++j) {
				final var c = line.charAt(j);
				if (inBlockComment) {
					if (c == '*' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
						inBlockComment = false;
						++j;
					}
					continue;
				}
				if (inTextBlock) {
					if (c == '\\' && j + 1 < line.length())
						++j;
					else if (isTripleQuote(line, j)) {
						inTextBlock = false;
						j += 2;
					}
					continue;
				}
				if (inString) {
					if (c == '"' && !isEscaped(line, j))
						inString = false;
					continue;
				}
				if (inChar) {
					if (c == '\'' && !isEscaped(line, j))
						inChar = false;
					continue;
				}
				if (isTextBlockOpener(line, j)) {
					inTextBlock = true;
					j += 2;
				}
				else if (c == '"')
					inString = true;
				else if (c == '\'')
					inChar = true;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/')
					break;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
					inBlockComment = true;
					++j;
				}
				else if (c == '(')
					++parenDepth;
				else if (c == ')')
					--parenDepth;
				else if (c == ';' && parenDepth == 0)
					return new int[]{i, j};
			}
			inString = false;
			inChar = false;
		}
		return new int[]{-1, -1};
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findUniqueCinitAssignment(
			@Nonnull DetailAST objBlock,
			@Nonnull String fieldName,
			@Nullable String enclosingTypeName,
			@Nullable String packageName
	) {
		DetailAST found = null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.STATIC_INIT)
				continue;
			final var slist = child.findFirstToken(TokenTypes.SLIST);
			if (slist == null)
				continue;
			for (var stmt = slist.getFirstChild(); stmt != null; stmt = stmt.getNextSibling()) {
				if (stmt.getType() != TokenTypes.EXPR)
					continue;
				final var inner = stmt.getFirstChild();
				if (inner == null || inner.getType() != TokenTypes.ASSIGN)
					continue;
				final var lhs = inner.getFirstChild();
				if (lhs == null)
					continue;
				final var matches = (lhs.getType() == TokenTypes.IDENT && fieldName.equals(lhs.getText()))
						|| isQualifiedFieldRef(lhs, fieldName, enclosingTypeName, packageName);
				if (!matches)
					continue;
				if (found != null)
					return null;
				found = inner;
			}
		}
		return found;
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
			@Nonnull boolean[] inTextBlockMask
	) {
		final var fieldName = extractIdentForward(lines.get(lineIndex), column);
		if (fieldName == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final DetailAST root;
		try {
			root = parseLinesToAst(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError | AssertionError ignored) {
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
		}

		final var fieldDef = findFieldDef(root, fieldName, lineIndex + 1, column);
		if (fieldDef == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var objBlock = fieldDef.getParent();
		if (objBlock == null || objBlock.getType() != TokenTypes.OBJBLOCK)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var enclosingTypeName = findEnclosingTypeName(objBlock);
		final var packageName = findPackageName(root);
		final var cinitAssign = findUniqueCinitAssignment(objBlock, fieldName, enclosingTypeName, packageName);
		if (cinitAssign == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var cinitLine = cinitAssign.getLineNo() - 1;
		if (cinitLine < 0 || cinitLine >= lines.size())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);

		final var cinitLineText = lines.get(cinitLine);
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

		if (lineIndex == cinitLine) {
			if (fieldDef.getLineNo() - 1 != lineIndex)
				return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT);
			final var fieldDeclStart = fieldDef.getColumnNo();
			final var lineText = lines.get(lineIndex);
			final int spliceMidEndCol;
			final int spliceTailStartCol;
			if (staticInit != null
					&& staticInit.getLineNo() - 1 == cinitLine
					&& staticInitRcurly.getLineNo() - 1 == cinitLine) {
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
			final var newLine = lineText.substring(0, fieldDeclStart)
					+ lineText.substring(endCol + 1, spliceMidEndCol)
					+ lineText.substring(spliceTailStartCol);
			return new FixResult(lineIndex, lineIndex, List.of(newLine), importsToAdd);
		}

		final var staticStartLine = staticInit == null ? -1 : staticInit.getLineNo() - 1;
		final var staticEndLine = staticInit == null ? -1 : staticInitRcurly.getLineNo() - 1;
		if (fieldDef.getLineNo() <= 0)
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

		var rangeStart = Math.min(fieldDeclStartLine, cinitLine);
		var rangeEnd = Math.max(endLine, cinitLine);
		if (staticInit != null) {
			rangeStart = Math.min(rangeStart, staticStartLine);
			rangeEnd = Math.max(rangeEnd, staticEndLine);
		}
		final var replacement = new ArrayList<String>();
		for (var i = rangeStart; i <= rangeEnd; ++i) {
			if (i >= fieldDeclStartLine && i <= endLine)
				continue;
			if (i == cinitLine)
				continue;
			if (staticInit != null && i >= staticStartLine && i <= staticEndLine)
				continue;
			replacement.add(lines.get(i));
		}
		return new FixResult(rangeStart, rangeEnd, replacement, importsToAdd);
	}

	/**
	 * Handles a violation on a single variable of a multi-variable declaration
	 * by deleting that variable from the declaration and adding the static
	 * import. The remaining variables stay in place; on the next pass, the
	 * check fires again for each remaining alias variable until the
	 * declaration converges.
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
			@Nonnull boolean[] inTextBlockMask
	) {
		final var varName = extractIdentForward(lines.get(lineIndex), column);
		if (varName == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR);

		final var declStart = findDeclarationStart(lines, lineIndex, inTextBlockMask);
		for (var i = declStart; i <= endLine; ++i) {
			if (inTextBlockMask[i])
				continue;
			if (lines.get(i).contains("//"))
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

		final var renameNeeded = !varName.equals(constName);
		if (renameNeeded) {
			final var shadowKind = findShadowKind(lines, varName, declStart, endLine);
			if (shadowKind != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(shadowKind)
				);
			}
		}

		final var sb = new StringBuilder();
		sb.append(stitched, 0, parse.firstNameStart());
		var first = true;
		for (var i = 0; i < parse.segments().size(); ++i) {
			if (i == targetIndex)
				continue;
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(parse.segments().get(i).name()).append(" = ").append(parse.segments().get(i).rhs());
		}
		sb.append(';');
		final var newDecl = sb.toString();

		final var importsToAdd = Set.of("static " + fqcn + "." + constName);
		final var targets = renameNeeded
				? List.of(classChain + "." + constName, varName)
				: List.of(classChain + "." + constName);
		final var modifiedLines = rewriteOutsideLiterals(lines, declStart, endLine, targets, constName);

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

	@CheckReturnValue
	private static boolean hasCommentMarker(@Nonnull String text, int from, int to) {
		final var upper = Math.min(to, text.length()) - 1;
		for (var i = Math.max(0, from); i < upper; ++i) {
			final var c = text.charAt(i);
			if (c == '/' && (text.charAt(i + 1) == '/' || text.charAt(i + 1) == '*'))
				return true;
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
			int skipStartLine1,
			int skipEndLine1
	) {
		return switch (node.getType()) {
			case TokenTypes.LAMBDA -> identifyLambdaShadow(node, name);
			case TokenTypes.PARAMETER_DEF -> identifyParameterDefShadow(node, name);
			case TokenTypes.RESOURCE ->
					matchesIdent(node, name) ? "try-with-resources variable" : null;
			case TokenTypes.VARIABLE_DEF ->
					identifyVariableDefShadow(node, name, skipStartLine1, skipEndLine1);
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String identifyVariableDefShadow(
			@Nonnull DetailAST node,
			@Nonnull String name,
			int skipStartLine1,
			int skipEndLine1
	) {
		if (!matchesIdent(node, name))
			return null;
		final var line = node.getLineNo();
		if (line >= skipStartLine1 && line <= skipEndLine1)
			return null;
		final var parent = node.getParent();
		if (parent == null)
			return "variable in an unrecognized context";
		return switch (parent.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> "for-each variable";
			case TokenTypes.FOR_INIT -> "for-loop variable";
			case TokenTypes.OBJBLOCK -> "another field with the same name";
			default -> "local variable";
		};
	}

	@CheckReturnValue
	private static boolean isEscaped(@Nonnull String line, int pos) {
		var backslashes = 0;
		for (var i = pos - 1; i >= 0 && line.charAt(i) == '\\'; --i)
			++backslashes;
		return backslashes % 2 != 0;
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
	private static boolean isPureDotChainOrIdent(@Nonnull DetailAST ast) {
		var cur = ast;
		while (true) {
			if (cur.getType() == TokenTypes.IDENT)
				return true;
			if (cur.getType() != TokenTypes.DOT)
				return false;
			final var left = cur.getFirstChild();
			if (left == null)
				return false;
			final var right = left.getNextSibling();
			if (right == null || right.getType() != TokenTypes.IDENT)
				return false;
			cur = left;
		}
	}

	@CheckReturnValue
	private static boolean isQualifiedFieldRef(
			@Nonnull DetailAST lhs,
			@Nonnull String fieldName,
			@Nullable String enclosingTypeName,
			@Nullable String packageName
	) {
		if (lhs.getType() != TokenTypes.DOT || enclosingTypeName == null)
			return false;
		if (!isPureDotChainOrIdent(lhs))
			return false;
		final var lhsText = FullIdent.createFullIdent(lhs).getText();
		if (lhsText.equals(enclosingTypeName + "." + fieldName))
			return true;
		return packageName != null
				&& lhsText.equals(packageName + "." + enclosingTypeName + "." + fieldName);
	}

	@CheckReturnValue
	private static boolean isTextBlockOpener(@Nonnull String text, int pos) {
		if (!isTripleQuote(text, pos))
			return false;
		for (var i = pos + 3; i < text.length(); ++i) {
			final var c = text.charAt(i);
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isTripleQuote(@Nonnull String text, int pos) {
		return pos + 2 < text.length()
				&& text.charAt(pos) == '"'
				&& text.charAt(pos + 1) == '"'
				&& text.charAt(pos + 2) == '"';
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
	private static DetailAST parseLinesToAst(@Nonnull List<String> lines) throws CheckstyleException {
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
		while (end > 0 && Character.isWhitespace(stripped.charAt(end - 1)))
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
	 */
	@CheckReturnValue
	@Nullable
	private static MultiVarParse parseMultiVarSegments(@Nonnull String stitched) {
		final var len = stitched.length();
		var i = 0;
		var inBlock = false;
		var inString = false;
		var inChar = false;
		var inTextBlock = false;
		var depth = 0;
		var lastIdentStart = -1;
		var lastIdentEnd = -1;

		while (i < len) {
			final var c = stitched.charAt(i);
			if (inBlock) {
				if (c == '*' && i + 1 < len && stitched.charAt(i + 1) == '/') {
					inBlock = false;
					++i;
				}
				++i;
				continue;
			}
			if (inTextBlock) {
				if (c == '\\' && i + 1 < len)
					i += 2;
				else if (isTripleQuote(stitched, i)) {
					inTextBlock = false;
					i += 3;
				}
				else
					++i;
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < len)
					i += 2;
				else {
					if (c == '"')
						inString = false;
					++i;
				}
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < len)
					i += 2;
				else {
					if (c == '\'')
						inChar = false;
					++i;
				}
				continue;
			}
			if (c == '/' && i + 1 < len && stitched.charAt(i + 1) == '*') {
				inBlock = true;
				i += 2;
				lastIdentStart = lastIdentEnd = -1;
				continue;
			}
			if (isTextBlockOpener(stitched, i)) {
				inTextBlock = true;
				i += 3;
				lastIdentStart = lastIdentEnd = -1;
				continue;
			}
			if (c == '"') {
				inString = true;
				++i;
				lastIdentStart = lastIdentEnd = -1;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				++i;
				lastIdentStart = lastIdentEnd = -1;
				continue;
			}
			if (c == '@') {
				final var afterAnno = skipAnnotation(stitched, i);
				if (afterAnno < 0)
					return null;
				i = afterAnno;
				lastIdentStart = lastIdentEnd = -1;
				continue;
			}
			if (c == '(' || c == '{') {
				++depth;
				lastIdentStart = lastIdentEnd = -1;
				++i;
				continue;
			}
			if (c == ')' || c == '}') {
				--depth;
				lastIdentStart = lastIdentEnd = -1;
				++i;
				continue;
			}
			if (depth == 0 && c == '=')
				break;
			if (Character.isJavaIdentifierPart(c)) {
				if (lastIdentEnd != i)
					lastIdentStart = i;
				lastIdentEnd = i + 1;
			}
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
				final var c = stitched.charAt(i);
				if (inBlock) {
					if (c == '*' && i + 1 < len && stitched.charAt(i + 1) == '/') {
						inBlock = false;
						++i;
					}
					++i;
					continue;
				}
				if (inTextBlock) {
					if (c == '\\' && i + 1 < len)
						i += 2;
					else if (isTripleQuote(stitched, i)) {
						inTextBlock = false;
						i += 3;
					}
					else
						++i;
					continue;
				}
				if (inString) {
					if (c == '\\' && i + 1 < len)
						i += 2;
					else {
						if (c == '"')
							inString = false;
						++i;
					}
					continue;
				}
				if (inChar) {
					if (c == '\\' && i + 1 < len)
						i += 2;
					else {
						if (c == '\'')
							inChar = false;
						++i;
					}
					continue;
				}
				if (c == '/' && i + 1 < len && stitched.charAt(i + 1) == '*') {
					inBlock = true;
					i += 2;
					continue;
				}
				if (isTextBlockOpener(stitched, i)) {
					inTextBlock = true;
					i += 3;
					continue;
				}
				if (c == '"') {
					inString = true;
					++i;
					continue;
				}
				if (c == '\'') {
					inChar = true;
					++i;
					continue;
				}
				if (c == '(' || c == '{') {
					++depth;
					++i;
					continue;
				}
				if (c == ')' || c == '}') {
					--depth;
					++i;
					continue;
				}
				if (depth == 0 && (c == ',' || c == ';'))
					break;
				++i;
			}
			if (i >= len)
				return null;
			segments.add(new VarSegment(name, stitched.substring(rhsStart, i).strip()));
			if (stitched.charAt(i) == ';')
				return new MultiVarParse(firstNameStart, segments);
			++i;
			while (i < len) {
				final var c = stitched.charAt(i);
				if (Character.isWhitespace(c)) {
					++i;
					continue;
				}
				if (c == '/' && i + 1 < len && stitched.charAt(i + 1) == '*') {
					i += 2;
					while (i < len) {
						if (stitched.charAt(i) == '*' && i + 1 < len && stitched.charAt(i + 1) == '/') {
							i += 2;
							break;
						}
						++i;
					}
					continue;
				}
				break;
			}
			if (i >= len || !Character.isJavaIdentifierStart(stitched.charAt(i)))
				return null;
			lastIdentStart = i;
			while (i < len && Character.isJavaIdentifierPart(stitched.charAt(i)))
				++i;
			lastIdentEnd = i;
			while (i < len) {
				final var c = stitched.charAt(i);
				if (Character.isWhitespace(c)) {
					++i;
					continue;
				}
				if (c == '/' && i + 1 < len && stitched.charAt(i + 1) == '*') {
					i += 2;
					while (i < len) {
						if (stitched.charAt(i) == '*' && i + 1 < len && stitched.charAt(i + 1) == '/') {
							i += 2;
							break;
						}
						++i;
					}
					continue;
				}
				break;
			}
			if (i >= len || stitched.charAt(i) != '=')
				return null;
		}
	}

	@CheckReturnValue
	@Nullable
	private static String resolveFqcn(
			@Nonnull List<String> lines,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull String classChain,
			@Nullable String filePath
	) {
		final var firstDot = classChain.indexOf('.');
		if (firstDot >= 0) {
			final var first = classChain.substring(0, firstDot);
			if (!first.isEmpty() && Character.isLowerCase(first.charAt(0)))
				return classChain;
			final var leftmostFqn = resolveSimpleClass(lines, inTextBlockMask, first, filePath);
			return leftmostFqn == null ? null : leftmostFqn + classChain.substring(firstDot);
		}
		return resolveSimpleClass(lines, inTextBlockMask, classChain, filePath);
	}

	@CheckReturnValue
	@Nullable
	private static String resolveSimpleClass(
			@Nonnull List<String> lines,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull String simpleClass,
			@Nullable String filePath
	) {
		String packageName = null;
		String wildcardCandidate = null;
		var wildcardCount = 0;
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (inTextBlockMask[lineIdx])
				continue;
			final var line = lines.get(lineIdx);
			final var stripped = stripCommentsForClassification(line);
			if (stripped.startsWith("package ") && stripped.endsWith(";")) {
				packageName = stripped.substring("package ".length(), stripped.length() - 1)
						.replaceAll("\\s+", "");
				continue;
			}
			if (!stripped.startsWith("import "))
				continue;
			if (stripped.startsWith("import static "))
				continue;
			if (!stripped.endsWith(";"))
				continue;
			final var fqn = stripped.substring("import ".length(), stripped.length() - 1)
					.replaceAll("\\s+", "");
			if (fqn.endsWith(".*")) {
				final var wildcardPrefix = fqn.substring(0, fqn.length() - 2);
				if (wildcardPrefix.isEmpty() || wildcardPrefix.startsWith(".") || wildcardPrefix.endsWith("."))
					continue;
				++wildcardCount;
				if (wildcardCandidate == null)
					wildcardCandidate = wildcardPrefix + "." + simpleClass;
				continue;
			}
			final var lastDot = fqn.lastIndexOf('.');
			if (lastDot <= 0 || lastDot == fqn.length() - 1)
				continue;
			final var simple = fqn.substring(lastDot + 1);
			if (simple.equals(simpleClass))
				return fqn;
		}

		if (filePath != null) {
			try {
				final var parentDir = Path.of(filePath).getParent();
				if (parentDir != null && Files.exists(parentDir.resolve(simpleClass + ".java"))) {
					if (packageName != null && !packageName.isEmpty())
						return packageName + "." + simpleClass;
					return simpleClass;
				}
			}
			catch (InvalidPathException ignored) {
			}
		}

		return wildcardCount == 1 ? wildcardCandidate : null;
	}

	@CheckReturnValue
	@Nonnull
	private static SortedMap<Integer, String> rewriteOutsideLiterals(
			@Nonnull List<String> lines,
			int skipStart,
			int skipEnd,
			@Nonnull List<String> targets,
			@Nonnull String replacement
	) {
		final var modified = new TreeMap<Integer, String>();
		var inBlockComment = false;
		var inTextBlock = false;
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			final var sb = new StringBuilder();
			var inString = false;
			var inChar = false;
			final var skipThisLine = lineIdx >= skipStart && lineIdx <= skipEnd;
			var i = 0;
			while (i < line.length()) {
				final var c = line.charAt(i);
				if (inBlockComment) {
					sb.append(c);
					if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
						sb.append('/');
						i += 2;
						inBlockComment = false;
					}
					else
						++i;
					continue;
				}
				if (inTextBlock) {
					if (c == '\\' && i + 1 < line.length()) {
						sb.append(c);
						sb.append(line.charAt(i + 1));
						i += 2;
						continue;
					}
					if (c == '"' && i + 2 < line.length()
							&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
						sb.append("\"\"\"");
						i += 3;
						inTextBlock = false;
					}
					else {
						sb.append(c);
						++i;
					}
					continue;
				}
				if (inString) {
					sb.append(c);
					if (c == '\\' && i + 1 < line.length()) {
						sb.append(line.charAt(i + 1));
						i += 2;
					}
					else if (c == '"') {
						inString = false;
						++i;
					}
					else
						++i;
					continue;
				}
				if (inChar) {
					sb.append(c);
					if (c == '\\' && i + 1 < line.length()) {
						sb.append(line.charAt(i + 1));
						i += 2;
					}
					else if (c == '\'') {
						inChar = false;
						++i;
					}
					else
						++i;
					continue;
				}
				if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
					sb.append("/*");
					i += 2;
					inBlockComment = true;
					continue;
				}
				if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					sb.append(line, i, line.length());
					i = line.length();
					continue;
				}
				if (isTextBlockOpener(line, i)) {
					sb.append("\"\"\"");
					i += 3;
					inTextBlock = true;
					continue;
				}
				if (c == '"') {
					sb.append('"');
					inString = true;
					++i;
					continue;
				}
				if (c == '\'') {
					sb.append('\'');
					inChar = true;
					++i;
					continue;
				}
				var matched = false;
				if (!skipThisLine) {
					for (var target : targets) {
						if (i + target.length() > line.length())
							continue;
						if (!line.regionMatches(i, target, 0, target.length()))
							continue;
						final var prev = i == 0 ? ' ' : line.charAt(i - 1);
						final var next = i + target.length() >= line.length()
								? ' '
								: line.charAt(i + target.length());
						if (Character.isJavaIdentifierPart(prev) || prev == '.')
							continue;
						if (Character.isJavaIdentifierPart(next))
							continue;
						sb.append(replacement);
						i += target.length();
						matched = true;
						break;
					}
				}
				if (!matched) {
					sb.append(c);
					++i;
				}
			}
			if (!skipThisLine && !sb.toString().equals(line))
				modified.put(lineIdx, sb.toString());
		}
		return modified;
	}

	@CheckReturnValue
	private static int skipAnnotation(@Nonnull String text, int atPos) {
		final var len = text.length();
		var i = atPos + 1;
		while (i < len && Character.isJavaIdentifierPart(text.charAt(i)))
			++i;
		while (i < len && text.charAt(i) == '.') {
			do ++i;
			while (i < len && Character.isJavaIdentifierPart(text.charAt(i)));
		}
		while (i < len && Character.isWhitespace(text.charAt(i)))
			++i;
		if (i >= len || text.charAt(i) != '(')
			return i;
		var depth = 1;
		++i;
		var inString = false;
		var inChar = false;
		var inBlock = false;
		var inTextBlock = false;
		while (i < len && depth > 0) {
			final var c = text.charAt(i);
			if (inBlock) {
				if (c == '*' && i + 1 < len && text.charAt(i + 1) == '/') {
					inBlock = false;
					++i;
				}
				++i;
				continue;
			}
			if (inTextBlock) {
				if (c == '\\' && i + 1 < len)
					i += 2;
				else if (isTripleQuote(text, i)) {
					inTextBlock = false;
					i += 3;
				}
				else
					++i;
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < len) {
					i += 2;
					continue;
				}
				if (c == '"')
					inString = false;
				++i;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < len) {
					i += 2;
					continue;
				}
				if (c == '\'')
					inChar = false;
				++i;
				continue;
			}
			if (c == '/' && i + 1 < len && text.charAt(i + 1) == '*') {
				inBlock = true;
				i += 2;
				continue;
			}
			if (isTextBlockOpener(text, i)) {
				inTextBlock = true;
				i += 3;
				continue;
			}
			if (c == '"') {
				inString = true;
				++i;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				++i;
				continue;
			}
			if (c == '(')
				++depth;
			else if (c == ')')
				--depth;
			++i;
		}
		return depth == 0 ? i : -1;
	}

	@CheckReturnValue
	private static int skipWhitespace(@Nonnull String s, int pos) {
		while (pos < s.length() && Character.isWhitespace(s.charAt(pos)))
			++pos;
		return pos;
	}

	@CheckReturnValue
	private static boolean staticBlockHasComments(
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
					&& !line.substring(0, Math.min(staticStartCol, line.length())).isBlank())
				return true;
			if (i == staticEndLine && i != cinitLine
					&& !line.substring(Math.min(staticEndCol + 1, line.length())).isBlank())
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
	 * Strips block comments, line comments, and string/char literal contents
	 * from a single text. String/char delimiters are preserved (so
	 * {@code "private"} becomes {@code ""}, keeping the empty pair as a
	 * placeholder), but contents are dropped so downstream callers don't
	 * accidentally match modifier keywords or {@code =} tokens hidden inside
	 * an annotation argument literal.
	 */
	@CheckReturnValue
	@Nonnull
	private static String stripCommentsForClassification(@Nonnull String line) {
		return stripCommentsForClassification(line, false);
	}

	@CheckReturnValue
	@Nonnull
	private static String stripCommentsForClassification(@Nonnull String line, boolean startsInTextBlock) {
		final var sb = new StringBuilder();
		var inBlock = false;
		var inString = false;
		var inChar = false;
		var inTextBlock = startsInTextBlock;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlock) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlock = false;
					++i;
				}
				continue;
			}
			if (inTextBlock) {
				if (c == '\\' && i + 1 < line.length()) {
					++i;
					continue;
				}
				if (isTripleQuote(line, i)) {
					inTextBlock = false;
					sb.append(" \"\"\" ");
					i += 2;
				}
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < line.length()) {
					++i;
					continue;
				}
				if (c == '"') {
					inString = false;
					sb.append('"');
				}
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < line.length()) {
					++i;
					continue;
				}
				if (c == '\'') {
					inChar = false;
					sb.append('\'');
				}
				continue;
			}
			if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				inBlock = true;
				++i;
				continue;
			}
			if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
				while (i < line.length() && line.charAt(i) != '\n')
					++i;
				continue;
			}
			if (isTextBlockOpener(line, i)) {
				sb.append(" \"\"\" ");
				inTextBlock = true;
				i += 2;
				continue;
			}
			if (c == '"') {
				sb.append('"');
				inString = true;
				continue;
			}
			if (c == '\'') {
				sb.append('\'');
				inChar = true;
				continue;
			}
			sb.append(c);
		}
		return sb.toString().strip();
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
			int skipStartLine1,
			int skipEndLine1
	) {
		final var stack = new ArrayDeque<DetailAST>();
		for (var sibling = root; sibling != null; sibling = sibling.getNextSibling())
			stack.push(sibling);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var kind = identifyShadow(node, name, skipStartLine1, skipEndLine1);
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
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var inTextBlockMask = computeInTextBlockMask(lines);
		final var endPos = findStatementEnd(lines, lineIndex, inTextBlockMask[lineIndex]);
		final var endLine = endPos[0];
		final var endCol = endPos[1];
		if (endLine < 0)
			return null;

		final var stitched = stitchDeclaration(lines, lineIndex, endLine);
		final var fieldDeclEndInStitched = stitched.length() - lines.get(endLine).length() + endCol + 1;
		final var fieldDeclOnly = stitched.substring(0, fieldDeclEndInStitched);
		if (stripAnnotations(stripCommentsForClassification(fieldDeclOnly, inTextBlockMask[lineIndex])).indexOf('=') < 0)
			return fixCinit(lines, lineIndex, column, endLine, endCol, inTextBlockMask);

		final var trailing = stripCommentsForClassification(lines.get(endLine).substring(endCol + 1));
		if (!trailing.isEmpty())
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP);

		if (isMultiVariableDeclaration(stitched, inTextBlockMask[lineIndex]))
			return fixMultiVar(lines, lineIndex, column, endLine, inTextBlockMask);

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

		final var importsToAdd = Set.of("static " + fqcn + "." + constName);
		final var localFieldName = parseLocalFieldName(stitched);
		final var renameNeeded = localFieldName != null && !localFieldName.equals(constName);
		if (renameNeeded) {
			final var shadowKind = findShadowKind(lines, localFieldName, lineIndex, endLine);
			if (shadowKind != null) {
				return new SkipResult(
						SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(shadowKind)
				);
			}
		}

		final var targets = renameNeeded
				? List.of(classChain + "." + constName, localFieldName)
				: List.of(classChain + "." + constName);
		final var modifiedLines = rewriteOutsideLiterals(lines, lineIndex, endLine, targets, constName);

		if (modifiedLines.isEmpty())
			return LineDeletion.deleteRange(lines, lineIndex, endLine, importsToAdd);

		final var rangeStart = Math.min(lineIndex, modifiedLines.firstKey());
		final var rangeEnd = Math.max(endLine, modifiedLines.lastKey());
		final var replacement = new ArrayList<String>();
		for (var i = rangeStart; i <= rangeEnd; ++i) {
			if (i >= lineIndex && i <= endLine)
				continue;
			replacement.add(modifiedLines.getOrDefault(i, lines.get(i)));
		}
		return new FixResult(rangeStart, rangeEnd, replacement, importsToAdd);
	}
}