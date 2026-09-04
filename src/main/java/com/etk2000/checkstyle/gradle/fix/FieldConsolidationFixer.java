package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.LineLength.MAX_LINE_LENGTH;

import com.etk2000.checkstyle.FieldConsolidationCheck;
import com.etk2000.checkstyle.FieldInitializers;
import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.LineColumns;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.SourceComments;
import com.etk2000.checkstyle.SourceComments.Comment;
import com.etk2000.checkstyle.SourceComments.LineCountWindows;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.ast.DocComments;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Folds the whole run of field declarations around a {@link FieldConsolidationCheck} violation
 * into one declaration, in one rewrite read off the parse.
 *
 * <p>The merged declaration lists the run's declarators in the order FieldSorting wants among one
 * declaration's names, or as written where FieldSorting is suppressed, so it needs no second pass
 * to sort them. When that order is the source
 * order and none of its names spans lines up to the {@code ,} or {@code ;} after it, the first
 * declaration is kept as written up to its {@code ;}, except that C-style array brackets on its
 * names move onto its type; otherwise only its part ahead of its first name is. Every other
 * declarator follows as written, its initializer and the comments inside it included and its own
 * brackets dropped, wrapped greedily at {@link LineLength#MAX_LINE_LENGTH} columns after a
 * {@code ,}; one spanning several lines is joined onto one
 * ({@link FieldConsolidationCheck#oneLineInitializer}). The first declaration's trailing comment
 * stays trailing. Every other comment of the run, ahead of a later declaration, among the
 * declarators or trailing them, moves above the merged declaration, joined into one line comment
 * by {@link FieldRewriteVerifier#joinComments}; one repeating a comment the merged declaration
 * keeps is dropped.
 *
 * <p>A run is skipped whole, never merged in part, when a comment to move spans several lines, when a comment left in place closes on a later declaration's line, when the first
 * declaration's line opens inside a block comment that starts after other code (the moved comments
 * would have no line of their own), when a tool directive moves, trails the first declaration,
 * sits on the line the other names join or sits in a rebuilt declarator, when a
 * {@linkplain SourceComments#isRangeDirective range directive} sits on a line of the run or
 * counts lines reaching one of its declarations, or when
 * {@link FieldRewriteVerifier} finds the rewrite changed more than the layout or moved a pair of
 * fields that may not trade places. An event on a declaration with no partner ahead of it, left
 * behind by a merge earlier in the pass, is stale.
 */
class FieldConsolidationFixer implements CheckstyleFixer {
	/**
	 * One run's merged declaration: the lines {@code out} that replace those from line {@code start}
	 * to its first declaration's {@code ;} on {@code semiLine}, and each later declaration's first
	 * line and the line past its last, {@code moved}, which leave. {@code end} is the line of the
	 * run's last {@code ;}.
	 */
	private record Block(int start, int semiLine, int end, @Nonnull List<String> out, @Nonnull List<long[]> moved) implements Rendered {}

	/** One declaration of a run: its declarators, and the positions of its first token and its {@code ;}. */
	private record Declaration(@Nonnull List<DetailAST> declarators, long start, long semi) {}

	/** The field declarators of the parse under {@code root}, by the line their name is on. */
	private record FieldLines(@Nonnull DetailAST root, @Nonnull Map<Integer, List<DetailAST>> byLine) {}

	/** A run that cannot be merged, and the skip reason why. */
	private record Refused(@Nonnull String reason) implements Rendered {}

	/**
	 * The skip reason of each run of the buffer {@code lines} already refused or failing
	 * verification, by the position of its first token, and the parse of it last seen,
	 * {@code root}. Kept by content rather than by parse, since the verifier's own parses push the
	 * buffer's out of the shared cache.
	 */
	private record Refusals(@Nonnull List<String> lines, @Nonnull DetailAST root, @Nonnull Map<Long, String> byStart) {}

	/**
	 * The {@link #runs} last found, the class body of the parse they were found in, the lines and
	 * comments they were read with, the {@linkplain SourceComments#lineCountWindows lines counted}
	 * by those comments, the runs' {@linkplain #declarations declarations}, the run each
	 * declarator of a later declaration belongs to, and whether FieldSorting is suppressed in the
	 * class body ({@link #fieldSortingSuppressed}), which keeps the fields in the order written.
	 */
	private record RunsMemo(
			@Nonnull DetailAST objBlock,
			@Nonnull LineColumns lines,
			@Nonnull List<Comment> comments,
			@Nonnull LineCountWindows lineCountWindows,
			@Nonnull List<List<List<DetailAST>>> runs,
			@Nullable List<List<Declaration>> declarations,
			@Nonnull Map<DetailAST, Integer> laterRunOf,
			boolean inOrder
	) {}

	/** What {@link #render} makes of one run. */
	private sealed interface Rendered permits Block, Refused {}

	private static final char BOM = 0xFEFF;
	private static final String SKIP_CARRIED_COMMENT = SkipMessages.get("field.consolidate.skip.carried.comment");
	private static final String SKIP_DIRECTIVE = SkipMessages.get("field.consolidate.skip.directive");
	private static final String SKIP_DIRECTIVE_WIDENED = SkipMessages.get("field.consolidate.skip.directive.widened");
	private static final String SKIP_MOVED_COMMENT = SkipMessages.get("field.consolidate.skip.moved.comment");
	private static final String SKIP_PINNED = SkipMessages.get("field.consolidate.skip.pinned");
	private static final String SKIP_RANGE_DIRECTIVE = SkipMessages.get("field.consolidate.skip.range.directive");
	private static final String SKIP_STALE = SkipMessages.get("field.consolidate.skip.stale");
	private static final String SKIP_UNVERIFIED = SkipMessages.get("field.consolidate.skip.unverified");
	private static final ThreadLocal<FieldLines> FIELD_LINES = new ThreadLocal<>();
	private static final ThreadLocal<Refusals> REFUSALS = new ThreadLocal<>();
	private static final ThreadLocal<RunsMemo> RUNS_MEMO = new ThreadLocal<>();

	/**
	 * The rewrite of the lines from the first {@code blocks} start to the last one's end: each
	 * block's merged declaration in place of its first declaration, its later declarations gone,
	 * and every other line as it was. A later declaration leaves with the blank lines right above
	 * it, so no blank line doubles up or ends up ahead of a closing brace.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixResult assemble(@Nonnull List<String> lines, @Nonnull List<Block> blocks) {
		final var ordered = new ArrayList<>(blocks);
		ordered.sort(Comparator.comparingInt(Block::start));
		final var moved = new ArrayList<long[]>();
		for (var block : ordered)
			moved.addAll(block.moved());
		moved.sort(Comparator.comparingLong(span -> span[0]));
		final var start = ordered.getFirst().start();
		final var end = ordered.stream().mapToInt(Block::end).max().orElseThrow();
		final var out = new ArrayList<String>();
		final var blanks = new ArrayList<String>();
		var next = 0;
		var line = start;
		while (line <= end) {
			final var source = lines.get(line - 1);
			if (next < ordered.size() && ordered.get(next).start() == line) {
				out.addAll(blanks);
				blanks.clear();
				out.addAll(ordered.get(next).out());
				line = ordered.get(next).semiLine();
				++next;
			}
			else if (holds(moved, line))
				blanks.clear();
			else if (source.isBlank())
				blanks.add(source);
			else {
				out.addAll(blanks);
				blanks.clear();
				out.add(source);
			}
			++line;
		}
		return new FixResult(start - 1, end - 1, out);
	}

	/**
	 * Just past the last C-style array bracket after {@code declarator}'s name, or past the name
	 * when it carries none.
	 */
	@CheckReturnValue
	private static long bracketsEnd(@Nonnull DetailAST declarator) {
		final var ident = declarator.findFirstToken(TokenTypes.IDENT);
		final var identStart = position(ident);
		var end = endOf(ident);
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(declarator.findFirstToken(TokenTypes.TYPE));
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (!AstText.isImaginaryToken(node) && position(node) > identStart)
				end = Math.max(end, endOf(node));
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return end;
	}

	/**
	 * The line comments moved above a declaration starting on {@code line} go above: {@code line},
	 * or when a block comment opened on an earlier line closes on it ahead of the declaration, the
	 * line that comment opens on, followed back the same way, since a comment put on {@code line}
	 * would land inside it. -1 when that line holds code ahead of the comment, which the moved
	 * comments would then be put above.
	 */
	@CheckReturnValue
	private static int carriedCommentStart(@Nonnull LineColumns lines, @Nonnull List<Comment> comments, int line) {
		var start = line;
		var i = SourceComments.commentsFrom(comments, DocComments.position(start, 0)) - 1;
		while (i >= 0 && DocComments.line(comments.get(i).end()) == start) {
			var to = comments.get(i).start();
			start = DocComments.line(to);
			--i;
			while (i >= 0 && DocComments.line(comments.get(i).end()) == start) {
				if (!SourceComments.isBlank(lines, comments.get(i).end(), to))
					return -1;
				to = comments.get(i).start();
				if (DocComments.line(to) != start)
					break;
				--i;
			}
			if (DocComments.line(to) == start && !SourceComments.isBlank(lines, DocComments.position(start, 0), to))
				return -1;
		}
		return start;
	}

	@CheckReturnValue
	private static int charIndex(@Nonnull LineColumns lines, long position) {
		return lines.charIndex(DocComments.line(position), (int) position);
	}

	/** Drops the memoized {@link #runs}, field lines and refusals, which hold a whole parse or buffer. */
	static void clearCache() {
		FIELD_LINES.remove();
		REFUSALS.remove();
		RUNS_MEMO.remove();
	}

	/** How many C-style array dimensions {@code declarator} carries after its name. */
	@CheckReturnValue
	private static int cStyleDimensions(@Nonnull DetailAST declarator) {
		final var identStart = position(declarator.findFirstToken(TokenTypes.IDENT));
		var dimensions = 0;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(declarator.findFirstToken(TokenTypes.TYPE));
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.ARRAY_DECLARATOR && position(node) > identStart)
				++dimensions;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return dimensions;
	}

	/** The run declaration {@code group} makes up, or null when it has no {@code ;} of its own. */
	@CheckReturnValue
	@Nullable
	private static Declaration declaration(@Nonnull List<DetailAST> group) {
		final var semi = FieldConsolidationCheck.semi(group);
		return semi == null ? null : new Declaration(group, DocComments.firstToken(group.getFirst()), position(semi));
	}

	/** The {@link #declaration declarations} of every run of {@code runs}, or null when one has no {@code ;} of its own. */
	@CheckReturnValue
	@Nullable
	private static List<List<Declaration>> declarations(@Nonnull List<List<List<DetailAST>>> runs) {
		final var all = new ArrayList<List<Declaration>>(runs.size());
		for (var groups : runs) {
			final var run = new ArrayList<Declaration>(groups.size());
			for (var group : groups) {
				final var declaration = declaration(group);
				if (declaration == null)
					return null;
				run.add(declaration);
			}
			all.add(run);
		}
		return all;
	}

	/** Just past {@code declarator}'s last token from its name on, its declaration's {@code ;} aside. */
	@CheckReturnValue
	private static long declaratorEnd(@Nonnull DetailAST declarator) {
		final var identStart = position(declarator.findFirstToken(TokenTypes.IDENT));
		var end = identStart;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(declarator);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (!AstText.isImaginaryToken(node) && node.getType() != TokenTypes.SEMI && position(node) >= identStart)
				end = Math.max(end, endOf(node));
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return end;
	}

	/**
	 * {@code declarator} as written from its name to its last token, its C-style brackets
	 * dropped. A declarator spanning lines keeps its line breaks.
	 */
	@CheckReturnValue
	@Nonnull
	static String declaratorText(@Nonnull LineColumns lines, @Nonnull DetailAST declarator) {
		return declarator.findFirstToken(TokenTypes.IDENT).getText() + text(lines, bracketsEnd(declarator), declaratorEnd(declarator));
	}

	/** Just past {@code token}'s text, which only a text block's content spreads over several lines. */
	@CheckReturnValue
	private static long endOf(@Nonnull DetailAST token) {
		final var text = token.getText();
		final var lastBreak = text.lastIndexOf('\n');
		if (lastBreak < 0)
			return DocComments.position(token.getLineNo(), token.getColumnNo() + text.codePointCount(0, text.length()));
		final var breaks = (int) text.chars().filter(c -> c == '\n').count();
		return DocComments.position(token.getLineNo() + breaks, text.codePointCount(lastBreak + 1, text.length()));
	}

	/**
	 * Whether FieldSorting is suppressed in {@code objBlock}: by a {@code @SuppressWarnings} on its
	 * type, or by the run's configured filters on any line of the body, where the run never sorts
	 * fields, so a merge must not either.
	 */
	@CheckReturnValue
	private static boolean fieldSortingSuppressed(@Nonnull DetailAST objBlock) {
		if (FieldSortingCheck.isSuppressed(objBlock))
			return true;
		final var last = AstQuery.lastLine(objBlock);
		for (var line = objBlock.getLineNo(); line <= last; ++line) {
			if (FixContext.isSuppressed(FieldSortingCheck.class, line))
				return true;
		}
		return false;
	}

	@Nullable
	private static FixAttempt fixRun(@Nonnull List<String> lines, @Nonnull List<String> parsed, @Nonnull DetailAST root, int line, int column) {
		final var target = target(root, line, column);
		if (target == null)
			return null;
		final var objBlock = target.getParent();
		final var cached = RUNS_MEMO.get();
		final var comments = cached != null && cached.objBlock() == objBlock ? cached.comments() : SourceComments.scan(parsed);
		final var groupedRuns = runs(parsed, comments, objBlock);
		final var memo = RUNS_MEMO.get();
		final var columns = memo.lines();
		final var targetRun = memo.laterRunOf().get(target);
		if (targetRun == null)
			return new SkipResult(SKIP_STALE);
		final var refusals = refusals(lines, root);
		final var runStart = DocComments.firstToken(groupedRuns.get(targetRun).getFirst().getFirst());
		final var refusal = refusals.get(runStart);
		if (refusal != null)
			return new SkipResult(refusal);
		final var all = memo.declarations();
		if (all == null)
			return null;
		final var inOrder = memo.inOrder();
		final var rendered = render(columns, all.get(targetRun), comments, memo.lineCountWindows(), inOrder);
		if (rendered instanceof Refused refused) {
			refusals.put(runStart, refused.reason());
			return new SkipResult(refused.reason());
		}
		final var own = (Block) rendered;
		// a run interleaved with this one has its events inside this rewrite, which the pipeline puts
		// off to the next pass, so it is folded in now; one that cannot be merged stays as written
		final var blocks = new ArrayList<>(List.of(own));
		final var left = new ArrayList<>(all);
		left.remove(targetRun.intValue());
		boolean grown;
		do {
			grown = false;
			final var start = blocks.stream().mapToInt(Block::start).min().orElseThrow();
			final var end = blocks.stream().mapToInt(Block::end).max().orElseThrow();
			for (var it = left.iterator(); it.hasNext(); ) {
				final var run = it.next();
				if (DocComments.line(run.getFirst().start()) > end || DocComments.line(run.getLast().semi()) < start)
					continue;
				it.remove();
				if (render(columns, run, comments, memo.lineCountWindows(), inOrder) instanceof Block block) {
					blocks.add(block);
					grown = true;
				}
			}
		}
		while (grown);
		final var folded = verified(lines, assemble(lines, blocks));
		final var attempt = folded instanceof SkipResult && blocks.size() > 1 ? verified(lines, assemble(lines, List.of(own))) : folded;
		if (attempt instanceof SkipResult(var reason))
			refusals.put(runStart, reason);
		return attempt;
	}

	/** True when one of {@code spans}, each a start and an exclusive end, sorted and apart, holds {@code position}. */
	@CheckReturnValue
	private static boolean holds(@Nonnull List<long[]> spans, long position) {
		var low = 0;
		var high = spans.size() - 1;
		while (low <= high) {
			final var mid = (low + high) >>> 1;
			final var span = spans.get(mid);
			if (position < span[0])
				high = mid - 1;
			else if (position >= span[1])
				low = mid + 1;
			else
				return true;
		}
		return false;
	}

	/**
	 * {@code declarator} as a merged declaration lists it: its {@link #declaratorText}, or when that
	 * spans lines, its name and its {@linkplain FieldConsolidationCheck#oneLineInitializer
	 * initializer joined onto one line}, which a declarator of a run always has.
	 */
	@CheckReturnValue
	@Nonnull
	private static String mergedText(@Nonnull LineColumns lines, @Nonnull DetailAST declarator) {
		final var text = declaratorText(lines, declarator);
		if (text.indexOf('\n') < 0)
			return text;
		return declarator.findFirstToken(TokenTypes.IDENT).getText() + " = " + FieldConsolidationCheck.oneLineInitializer(lines, declarator);
	}

	@CheckReturnValue
	private static long position(@Nonnull DetailAST node) {
		return DocComments.position(node.getLineNo(), node.getColumnNo());
	}

	/**
	 * The skip reasons of the {@link Refusals} of the buffer {@code lines}, parsed as {@code root},
	 * kept from earlier events while it is unchanged. The same parse is the same buffer, so only a
	 * new one is compared.
	 */
	@CheckReturnValue
	@Nonnull
	private static Map<Long, String> refusals(@Nonnull List<String> lines, @Nonnull DetailAST root) {
		final var memo = REFUSALS.get();
		if (memo != null && memo.root() == root)
			return memo.byStart();
		final var kept = memo != null && memo.lines().equals(lines)
				? new Refusals(memo.lines(), root, memo.byStart())
				: new Refusals(List.copyOf(lines), root, new HashMap<>());
		REFUSALS.set(kept);
		return kept.byStart();
	}

	/**
	 * The merged declaration {@code run} folds into, its names sorted unless {@code inOrder} (a
	 * type that suppresses FieldSorting), or a {@link Refused} when a comment that has
	 * to move spans several lines or has {@linkplain #carriedCommentStart no line of its own} to go
	 * to, when one left in place closes on a later declaration's line, or when one that moves, trails
	 * the first declaration, sits in the head on the line the other names join or sits in a rebuilt
	 * declarator is a {@link SourceComments#isDirective tool directive}. A comment in a kept
	 * span (the first declaration up to its {@code ;}, or up to its first name when its names are
	 * reordered, its brackets move or one of its names spans lines up to the {@code ,} or {@code ;}
	 * after it, and each rebuilt declarator's own text) stays in place, and so do one trailing the
	 * first declaration and one starting on no line of the run's declarations; every other one is
	 * moved above the first declaration, and above a block comment its line opens inside. A run is
	 * also refused when a {@linkplain SourceComments#isRangeDirective range directive} sits on a
	 * line from its first declaration to its last one's {@code ;}, or one of {@code lineCountWindows}
	 * holds a line of one of its declarations, since the merge would move names into or out of the
	 * lines the directive acts on.
	 */
	@Nonnull
	private static Rendered render(
			@Nonnull LineColumns parsed,
			@Nonnull List<Declaration> run,
			@Nonnull List<Comment> comments,
			@Nonnull LineCountWindows lineCountWindows,
			boolean inOrder
	) {
		final var first = run.getFirst();
		final var firstDeclarator = first.declarators().getFirst();
		final var startLine = DocComments.line(first.start());
		final var lineStart = DocComments.position(startLine, 0);
		final var semiLine = DocComments.line(first.semi());
		final var endLine = DocComments.line(run.getLast().semi());
		if (SourceComments.rangeDirectiveOn(comments, startLine, endLine))
			return new Refused(SKIP_RANGE_DIRECTIVE);
		for (var declaration : run) {
			if (lineCountWindows.reaches(DocComments.line(declaration.start()), DocComments.line(declaration.semi())))
				return new Refused(SKIP_RANGE_DIRECTIVE);
		}
		final var kept = new ArrayList<long[]>();
		final var declarators = new ArrayList<DetailAST>();
		for (var declaration : run)
			declarators.addAll(declaration.declarators());
		final var ordered = inOrder ? declarators : sorted(parsed, declarators);
		final var cores = new ArrayList<DetailAST>();
		final String head;
		final var dimensions = cStyleDimensions(firstDeclarator);
		var split = false;
		for (var declarator : first.declarators()) {
			final var bound = FieldConsolidationCheck.declaratorBound(declarator);
			split |= bound == null || bound.getLineNo() != declarator.findFirstToken(TokenTypes.IDENT).getLineNo();
		}
		final var rebuilt = dimensions > 0 || split || !ordered.equals(declarators);
		if (rebuilt) {
			final var ident = position(firstDeclarator.findFirstToken(TokenTypes.IDENT));
			var typeEnd = lineStart;
			final var stack = new ArrayDeque<DetailAST>();
			stack.push(firstDeclarator.findFirstToken(TokenTypes.TYPE));
			while (!stack.isEmpty()) {
				final var node = stack.pop();
				if (!AstText.isImaginaryToken(node) && position(node) < ident)
					typeEnd = Math.max(typeEnd, endOf(node));
				for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
					stack.push(child);
			}
			kept.add(new long[]{lineStart, ident});
			head = text(parsed, lineStart, typeEnd) + "[]".repeat(dimensions) + text(parsed, typeEnd, ident);
			cores.addAll(ordered);
		}
		else {
			kept.add(new long[]{lineStart, first.semi()});
			head = text(parsed, lineStart, first.semi()).stripTrailing();
			cores.addAll(ordered.subList(first.declarators().size(), ordered.size()));
		}
		for (var declarator : cores) {
			final var ident = declarator.findFirstToken(TokenTypes.IDENT);
			kept.add(new long[]{position(ident), endOf(ident)});
			kept.add(new long[]{bracketsEnd(declarator), declaratorEnd(declarator)});
		}

		final var movedSpans = new ArrayList<long[]>();
		for (var k = 1; k < run.size(); ++k)
			movedSpans.add(new long[]{DocComments.line(run.get(k).start()), DocComments.line(run.get(k).semi()) + 1L});
		final var keptTexts = new HashSet<String>();
		final var moved = new LinkedHashSet<String>();
		final var joinLine = rebuilt ? DocComments.line(position(firstDeclarator.findFirstToken(TokenTypes.IDENT))) : semiLine;
		final var keptCores = new ArrayList<long[]>();
		for (var span : kept.subList(1, kept.size())) {
			if (span[0] < span[1])
				keptCores.add(span);
		}
		keptCores.sort(Comparator.comparingLong(span -> span[0]));
		for (var c = SourceComments.commentsFrom(comments, lineStart); c < comments.size() && DocComments.line(comments.get(c).start()) <= endLine; ++c) {
			final var comment = comments.get(c);
			final var commentLine = DocComments.line(comment.start());
			if (commentLine > semiLine && !holds(movedSpans, commentLine)) {
				// one closing on a later declaration's line would lose its */ when that line leaves
				if (holds(movedSpans, DocComments.line(comment.end())))
					return new Refused(SKIP_MOVED_COMMENT);
				continue;
			}
			final int keptAt;
			if (comment.start() >= kept.getFirst()[0] && comment.start() < kept.getFirst()[1])
				keptAt = 0;
			else
				keptAt = holds(keptCores, comment.start()) ? 1 : -1;
			final var trailing = comment.start() > first.semi() && commentLine == semiLine;
			final var onJoinLine = keptAt == 0 && (commentLine == joinLine || DocComments.line(comment.end()) == joinLine);
			// one trailing the first declaration, kept in the head on the line the other names join,
			// or inside a declarator the merge lines up with others (every kept span past the head)
			// ends up on a line holding names it was never written for, and a line-scoped tool
			// directive there would cover them too
			if ((trailing || onJoinLine || keptAt > 0) && SourceComments.isDirective(comment.text()))
				return new Refused(SKIP_DIRECTIVE_WIDENED);
			if (keptAt >= 0)
				keptTexts.add(comment.text());
			else if (DocComments.line(comment.end()) != commentLine)
				return new Refused(SKIP_MOVED_COMMENT);
			else if (trailing)
				keptTexts.add(comment.text());
			else if (!keptTexts.contains(comment.text()))
				moved.add(comment.text());
		}
		moved.removeAll(keptTexts);
		for (var text : moved) {
			if (SourceComments.isDirective(text))
				return new Refused(SKIP_DIRECTIVE);
		}

		final var semiText = parsed.lines().get(semiLine - 1);
		final var tail = semiText.substring(charIndex(parsed, first.semi()) + 1);
		final var indent = LineText.extractIndent(parsed.lines().get(startLine - 1));
		final var out = new ArrayList<>(List.of(head.split("\n", -1)));
		var current = out.removeLast();
		for (var k = 0; k < cores.size(); ++k) {
			final var core = mergedText(parsed, cores.get(k));
			final var separator = k == 0 && rebuilt ? "" : ", ";
			final var closing = k == cores.size() - 1 ? ";" + tail : ",";
			if (separator.isEmpty() || LineLength.tabExpandedLength(current + separator + core + closing) <= MAX_LINE_LENGTH)
				current += separator + core;
			else {
				out.add(current + ",");
				current = indent + "\t\t" + core;
			}
		}
		out.add(current + ";" + tail);
		if (moved.isEmpty())
			return new Block(startLine, semiLine, endLine, out, movedSpans);
		final var blockStart = carriedCommentStart(parsed, comments, startLine);
		if (blockStart < 0)
			return new Refused(SKIP_CARRIED_COMMENT);
		out.addAll(0, parsed.lines().subList(blockStart - 1, startLine - 1));
		out.addFirst(LineText.extractIndent(parsed.lines().get(blockStart - 1)) + FieldRewriteVerifier.joinComments(new ArrayList<>(moved)));
		return new Block(blockStart, semiLine, endLine, out, movedSpans);
	}

	/**
	 * The runs {@link FieldConsolidationCheck#runs} finds in {@code objBlock}, read off
	 * {@code lines}, whose {@code comments} tell which declarations are documented or led by a tool
	 * directive.
	 */
	@CheckReturnValue
	@Nonnull
	static List<List<List<DetailAST>>> runs(@Nonnull List<String> lines, @Nonnull List<Comment> comments, @Nonnull DetailAST objBlock) {
		// a parse is shared while the buffer is unchanged, so the events of one class body that
		// is left as written (a refused run) find their runs here rather than redoing them
		final var memo = RUNS_MEMO.get();
		if (memo != null && memo.objBlock() == objBlock)
			return memo.runs();
		var root = objBlock;
		while (root.getParent() != null)
			root = root.getParent();
		final var docStarts = new TreeSet<Long>();
		for (var comment : comments) {
			if (DocComments.isDocComment(comment.text()))
				docStarts.add(comment.start());
		}
		final var docs = new DocComments(DocComments.tokenPositions(root), docStarts);
		final var columns = new LineColumns(lines);
		final var inOrder = fieldSortingSuppressed(objBlock);
		final var runs = FieldConsolidationCheck.runs(objBlock, columns, () -> docs, () -> comments, inOrder);
		final var laterRunOf = new IdentityHashMap<DetailAST, Integer>();
		for (var r = 0; r < runs.size(); ++r) {
			for (var group : runs.get(r).subList(1, runs.get(r).size())) {
				for (var declarator : group)
					laterRunOf.putIfAbsent(declarator, r);
			}
		}
		RUNS_MEMO.set(new RunsMemo(objBlock, columns, comments, SourceComments.lineCountWindows(comments), runs, declarations(runs), laterRunOf, inOrder));
		return runs;
	}

	/**
	 * {@code declarators} in the order {@link FieldSortingFixer#orderDeclarators} gives one
	 * declaration's names, each after the names it reads and the earlier ones it is
	 * {@linkplain FieldInitializers#pinnedAfter pinned after}, so the merge leaves FieldSorting
	 * nothing to reorder, or as written when they wait for each other in a cycle.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> sorted(@Nonnull LineColumns lines, @Nonnull List<DetailAST> declarators) {
		final var names = new ArrayList<String>(declarators.size());
		final var texts = new ArrayList<String>(declarators.size());
		final var byText = new HashMap<String, DetailAST>();
		for (var declarator : declarators) {
			final var text = mergedText(lines, declarator);
			names.add(FieldSortingCheck.fieldName(declarator));
			texts.add(text);
			byText.put(text, declarator);
		}
		final var isFinal = declarators.getFirst().findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.FINAL) != null;
		final var waitsFor = new HashMap<String, Set<String>>();
		for (var entry : FieldSortingCheck.fieldDependencies(declarators).entrySet())
			waitsFor.put(entry.getKey(), new HashSet<>(entry.getValue()));
		for (var entry : FieldInitializers.pinnedAfter(declarators).entrySet())
			waitsFor.computeIfAbsent(entry.getKey(), name -> new HashSet<>()).addAll(entry.getValue());
		final var ordered = FieldSortingFixer.orderDeclarators(names, texts, waitsFor, isFinal);
		if (ordered == null)
			return declarators;
		final var reordered = new ArrayList<DetailAST>(ordered.size());
		for (var text : ordered)
			reordered.add(byText.get(text));
		return reordered;
	}

	/**
	 * The field declarator named on {@code line} nearest {@code column}, one at or past it
	 * winning a tie: a sibling fixer may have shifted the name along its line.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST target(@Nonnull DetailAST root, int line, int column) {
		var fields = FIELD_LINES.get();
		if (fields == null || fields.root() != root) {
			final var byLine = new HashMap<Integer, List<DetailAST>>();
			final var stack = new ArrayDeque<DetailAST>();
			stack.push(root);
			while (!stack.isEmpty()) {
				final var node = stack.pop();
				if (node.getType() == TokenTypes.VARIABLE_DEF && node.getParent() != null && node.getParent().getType() == TokenTypes.OBJBLOCK)
					byLine.computeIfAbsent(node.findFirstToken(TokenTypes.IDENT).getLineNo(), key -> new ArrayList<>()).add(node);
				for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
					stack.push(child);
			}
			fields = new FieldLines(root, byLine);
			FIELD_LINES.set(fields);
		}
		DetailAST best = null;
		var bestScore = Long.MAX_VALUE;
		for (var node : fields.byLine().getOrDefault(line, List.of())) {
			final var ident = node.findFirstToken(TokenTypes.IDENT);
			final var score = Math.abs((long) ident.getColumnNo() - column) * 2 + (ident.getColumnNo() < column ? 1 : 0);
			if (score < bestScore) {
				best = node;
				bestScore = score;
			}
		}
		return best;
	}

	/** The source from {@code from} up to {@code to}, lines joined with {@code \n}. */
	@CheckReturnValue
	@Nonnull
	private static String text(@Nonnull LineColumns lines, long from, long to) {
		if (to <= from)
			return "";
		final var fromLine = DocComments.line(from);
		final var toLine = DocComments.line(to);
		final var source = lines.lines();
		if (fromLine == toLine)
			return source.get(fromLine - 1).substring(charIndex(lines, from), charIndex(lines, to));
		final var sb = new StringBuilder(source.get(fromLine - 1).substring(charIndex(lines, from)));
		for (var l = fromLine + 1; l < toLine; ++l)
			sb.append('\n').append(source.get(l - 1));
		return sb.append('\n').append(source.get(toLine - 1), 0, charIndex(lines, to)).toString();
	}

	/**
	 * {@code result} when it only re-lays the fields out and keeps every pair of fields that may
	 * not trade places in order, else a skip: the re-parse is what turns a misread run into a skip
	 * rather than corrupt or retyped output.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt verified(@Nonnull List<String> lines, @Nonnull FixResult result) {
		if (!FieldRewriteVerifier.preserves(lines, result))
			return new SkipResult(SKIP_UNVERIFIED);
		return FieldRewriteVerifier.keepsInitializationOrder(lines, result) ? result : new SkipResult(SKIP_PINNED);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		// the parse refuses a byte-order mark, which carries no code; columns on its line count without it
		final var bom = !lines.getFirst().isEmpty() && lines.getFirst().charAt(0) == BOM;
		final List<String> parsed;
		if (bom) {
			parsed = new ArrayList<>(lines);
			parsed.set(0, lines.getFirst().substring(1));
		}
		else
			parsed = lines;
		final var codePointColumn = Math.max(0, LineText.columnOfCharIndex(lines.get(lineIndex), charColumn) - (bom && lineIndex == 0 ? 1 : 0));
		return FixerAst.withAst(parsed, root -> fixRun(lines, parsed, root, lineIndex + 1, codePointColumn));
	}
}