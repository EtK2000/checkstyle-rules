package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.DocComments;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The comments of a file as its lines spell them, and which of them are tool directives.
 */
public final class SourceComments {
	/**
	 * One comment of a buffer, delimiters included, from its {@link DocComments#position start}
	 * to just past its end.
	 */
	public record Comment(long start, long end, @Nonnull String text) {}

	/**
	 * The lines the comments counting lines act on ({@link #lineCountWindows}): each window's first
	 * and last line (1-based, inclusive), ordered by first line, and in {@code reach} the furthest
	 * last line of the windows up to each, which makes {@link #reaches} a binary search.
	 */
	public record LineCountWindows(@Nonnull List<long[]> windows, @Nonnull long[] reach) {
		/** True when one of the windows holds a line from {@code fromLine} to {@code toLine} (1-based, inclusive). */
		@CheckReturnValue
		public boolean reaches(int fromLine, int toLine) {
			var low = 0;
			var high = windows.size();
			while (low < high) {
				final var mid = (low + high) >>> 1;
				if (windows.get(mid)[0] <= toLine)
					low = mid + 1;
				else
					high = mid;
			}
			return low > 0 && reach[low - 1] >= fromLine;
		}
	}

	// a count of more digits than this covers every line there is
	private static final int MAX_LINE_COUNT_DIGITS = 9;
	private static final List<String> DIRECTIVES = List.of("$NON-NLS-", "@formatter:", "CHECKSTYLE", "NOPMD", "NOSONAR", "language=", "noinspection", "spotless:");
	// comments that switch a tool off or on for the lines after them, which a rewrite would carry
	// to the other side of the lines they were written around
	private static final List<String> RANGE_DIRECTIVES = List.of(
			"@formatter:off",
			"@formatter:on",
			"CHECKSTYLE.OFF",
			"CHECKSTYLE.ON",
			"CHECKSTYLE:OFF",
			"CHECKSTYLE:ON",
			"spotless:off",
			"spotless:on"
	);
	private static final List<String> REGION_MARKERS = List.of("#endregion", "#region", "</editor-fold>", "<editor-fold", "endregion", "region");
	// the count of lines a Checkstyle suppression comment covers after it (or, signed `-`, before
	// it), as SuppressWithNearbyCommentFilter configurations spell one, past LINE_COUNT_MARKER
	private static final Pattern LINE_COUNT = Pattern.compile("\\b(?i:next|for)\\s+([+-]?)(\\d+)\\s+(?i:lines?)\\b");
	private static final String LINE_COUNT_MARKER = "CHECKSTYLE";
	private static final String STATEMENT_DIRECTIVE = "noinspection";

	/**
	 * {@code comment}'s text without its delimiters, the extra stars of a {@code /**} opener, or
	 * the whitespace around it.
	 */
	@CheckReturnValue
	@Nonnull
	public static String body(@Nonnull String comment) {
		if (comment.startsWith("//"))
			return comment.substring(2).strip();
		var start = 2;
		final var end = Math.max(start, comment.length() - 2);
		while (start < end && comment.charAt(start) == '*')
			++start;
		return comment.substring(start, end).strip();
	}

	/** The index of the first of {@code comments}, in source order, that starts at or past {@code position}. */
	@CheckReturnValue
	public static int commentsFrom(@Nonnull List<Comment> comments, long position) {
		var low = 0;
		var high = comments.size();
		while (low < high) {
			final var mid = (low + high) >>> 1;
			if (comments.get(mid).start() < position)
				low = mid + 1;
			else
				high = mid;
		}
		return low;
	}

	/** True when the source from {@code from} up to {@code to} is whitespace. */
	@CheckReturnValue
	public static boolean isBlank(@Nonnull LineColumns lines, long from, long to) {
		final var fromLine = DocComments.line(from);
		final var toLine = DocComments.line(to);
		for (var l = fromLine; l <= toLine && to > from; ++l) {
			final var source = lines.lines().get(l - 1);
			final var end = l == toLine ? lines.charIndex(l, (int) to) : source.length();
			for (var i = l == fromLine ? lines.charIndex(l, (int) from) : 0; i < end; ++i) {
				if (!Character.isWhitespace(source.charAt(i)))
					return false;
			}
		}
		return true;
	}

	/**
	 * True when {@code comment} is a tool directive ({@code // NOSONAR}, {@code //$NON-NLS-1$}, a
	 * Checkstyle suppression comment such as {@code // CHECKSTYLE IGNORE X FOR NEXT 1 LINES},
	 * {@code // noinspection}, {@code // language=SQL}, a formatter switch), anywhere in its text,
	 * case-sensitively. Some act on the line they sit on, others on the declaration or the lines after
	 * them ({@link #isStatementDirective} among them).
	 */
	@CheckReturnValue
	public static boolean isDirective(@Nonnull String comment) {
		for (var marker : DIRECTIVES) {
			if (comment.contains(marker))
				return true;
		}
		return false;
	}

	/**
	 * True when {@code comment} switches a tool off or on for the lines after it, or covers a count
	 * of them: it holds one of {@link #RANGE_DIRECTIVES}, opens or closes a folding region, its text
	 * starting with one of {@link #REGION_MARKERS}, a word marker as a word of its own
	 * ({@code //region Tuning}, {@code // endregion}, {@code //#region}, {@code // <editor-fold desc="x">}),
	 * or is a Checkstyle suppression comment with a {@linkplain #lineCount line count}
	 * ({@code // CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES}).
	 */
	@CheckReturnValue
	public static boolean isRangeDirective(@Nonnull String comment) {
		for (var directive : RANGE_DIRECTIVES) {
			if (comment.contains(directive))
				return true;
		}
		final var body = body(comment);
		for (var marker : REGION_MARKERS) {
			if (body.startsWith(marker) && (marker.startsWith("<") || body.length() == marker.length() || Character.isWhitespace(body.charAt(marker.length()))))
				return true;
		}
		return lineCount(comment) != null;
	}

	/**
	 * True when {@code comment} is a directive acting on the declaration or statement after it
	 * wherever it sits ahead of it, a line comment whose text starts with {@code noinspection}: a
	 * subset of what {@link #isDirective} matches.
	 */
	@CheckReturnValue
	public static boolean isStatementDirective(@Nonnull String comment) {
		return comment.startsWith("//") && comment.substring(2).stripLeading().startsWith(STATEMENT_DIRECTIVE);
	}

	/**
	 * The comments of {@code lines} ({@code comments}, in source order) that lead the declaration
	 * {@code declarator} opens as tool directives: a {@linkplain #isStatementDirective directive
	 * acting on the next declaration} ({@code //noinspection}) anywhere ahead of its first name, and
	 * a {@linkplain #isDirective tool directive} on a line of its own ({@code // language=SQL},
	 * {@code // CHECKSTYLE IGNORE X FOR NEXT 1 LINES}) above that name's line, or starting on one
	 * and spanning lines, closing on that name's line at the latest. Ahead of the
	 * declaration's first token that means among the comments right above it, with nothing but
	 * whitespace, blank lines included, and other comments between, the line above's trailing
	 * comment among them; after it, among its annotations or between them and its type. Any other
	 * tool directive sharing the name's line acts on that line rather than on the declaration.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<Comment> leadingDirectives(@Nonnull LineColumns lines, @Nonnull List<Comment> comments, @Nonnull DetailAST declarator) {
		final var directives = new ArrayList<Comment>();
		final var start = DocComments.firstToken(declarator);
		final var name = declarator.findFirstToken(TokenTypes.IDENT);
		final var from = commentsFrom(comments, start);
		var to = start;
		for (var i = from - 1; i >= 0; --i) {
			final var comment = comments.get(i);
			if (!isBlank(lines, comment.end(), to))
				break;
			if (leads(lines, comment, name.getLineNo()))
				directives.addFirst(comment);
			to = comment.start();
		}
		final var namePosition = DocComments.position(name.getLineNo(), name.getColumnNo());
		for (var i = from; i < comments.size() && comments.get(i).start() < namePosition; ++i) {
			if (leads(lines, comments.get(i), name.getLineNo()))
				directives.add(comments.get(i));
		}
		return directives;
	}

	/**
	 * True when {@code comment} leads the declaration whose name is on {@code nameLine}: a statement
	 * directive anywhere ahead of it, or a tool directive starting above that line with nothing ahead
	 * of it on its first line, and nothing after it on its last unless it spans lines.
	 */
	@CheckReturnValue
	private static boolean leads(@Nonnull LineColumns lines, @Nonnull Comment comment, int nameLine) {
		if (isStatementDirective(comment.text()))
			return true;
		final var startLine = DocComments.line(comment.start());
		final var endLine = DocComments.line(comment.end());
		final var lastLine = lines.lines().get(endLine - 1);
		return startLine < nameLine
				&& isBlank(lines, DocComments.position(startLine, 0), comment.start())
				&& (endLine > startLine || isBlank(lines, comment.end(), DocComments.position(endLine, lastLine.codePointCount(0, lastLine.length()))))
				&& isDirective(comment.text());
	}

	/**
	 * The match of the count of lines {@code comment} covers, its sign as group 1 and its digits
	 * as group 2, when it is a Checkstyle suppression comment counting lines
	 * ({@code CHECKSTYLE} followed by {@code NEXT} or {@code FOR}, the count and {@code LINE} or
	 * {@code LINES}, the words in any case), or null.
	 */
	@CheckReturnValue
	@Nullable
	private static Matcher lineCount(@Nonnull String comment) {
		final var marker = comment.indexOf(LINE_COUNT_MARKER);
		if (marker < 0)
			return null;
		final var matcher = LINE_COUNT.matcher(comment);
		return matcher.find(marker + LINE_COUNT_MARKER.length()) ? matcher : null;
	}

	/**
	 * The lines each {@linkplain #isRangeDirective range directive} of {@code comments} counting
	 * lines acts on, as its first and last (1-based, inclusive):
	 * {@code // CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES} counts the two after its own last line,
	 * and a count signed {@code -} the lines above its first.
	 */
	@CheckReturnValue
	@Nonnull
	public static LineCountWindows lineCountWindows(@Nonnull List<Comment> comments) {
		final var windows = new ArrayList<long[]>();
		for (var comment : comments) {
			final var matcher = lineCount(comment.text());
			if (matcher == null)
				continue;
			final var counted = matcher.group(2).length() > MAX_LINE_COUNT_DIGITS ? Integer.MAX_VALUE : Long.parseLong(matcher.group(2));
			if (counted == 0)
				continue;
			if ("-".equals(matcher.group(1)))
				windows.add(new long[]{DocComments.line(comment.start()) - counted, DocComments.line(comment.start()) - 1L});
			else
				windows.add(new long[]{DocComments.line(comment.end()) + 1L, DocComments.line(comment.end()) + counted});
		}
		windows.sort(Comparator.comparingLong(window -> window[0]));
		final var reach = new long[windows.size()];
		for (var i = 0; i < reach.length; ++i)
			reach[i] = i == 0 ? windows.get(i)[1] : Math.max(reach[i - 1], windows.get(i)[1]);
		return new LineCountWindows(windows, reach);
	}

	/**
	 * True when a {@linkplain #isRangeDirective range directive} of {@code comments} (in source
	 * order) sits, wholly or in part, on a line from {@code fromLine} to {@code toLine} (1-based,
	 * inclusive).
	 */
	@CheckReturnValue
	public static boolean rangeDirectiveOn(@Nonnull List<Comment> comments, int fromLine, int toLine) {
		// comments never overlap, so only the one right before the first starting on fromLine can reach into it
		for (var i = Math.max(0, commentsFrom(comments, DocComments.position(fromLine, 0)) - 1); i < comments.size(); ++i) {
			final var comment = comments.get(i);
			if (DocComments.line(comment.start()) > toLine)
				break;
			if (DocComments.line(comment.end()) >= fromLine && isRangeDirective(comment.text()))
				return true;
		}
		return false;
	}

	/** Lexes every comment out of {@code lines}, in source order. */
	@CheckReturnValue
	@Nonnull
	public static List<Comment> scan(@Nonnull List<String> lines) {
		final var comments = new ArrayList<Comment>();
		StringBuilder block = null;
		var blockStart = 0L;
		var inTextBlock = false;
		for (var lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
			final var line = lines.get(lineIndex);
			if (block != null)
				block.append('\n');
			var i = 0;
			while (i < line.length()) {
				if (block != null) {
					if (line.startsWith("*/", i)) {
						block.append("*/");
						i += 2;
						comments.add(new Comment(blockStart, DocComments.position(lineIndex + 1, line.codePointCount(0, i)), block.toString()));
						block = null;
					}
					else
						block.append(line.charAt(i++));
				}
				else if (inTextBlock) {
					if (line.startsWith("\"\"\"", i)) {
						inTextBlock = false;
						i += 3;
					}
					else
						i += line.charAt(i) == '\\' ? 2 : 1;
				}
				else if (line.startsWith("//", i)) {
					final var start = DocComments.position(lineIndex + 1, line.codePointCount(0, i));
					comments.add(new Comment(start, DocComments.position(lineIndex + 1, line.codePointCount(0, line.length())), line.substring(i)));
					break;
				}
				else if (line.startsWith("/*", i)) {
					block = new StringBuilder("/*");
					blockStart = DocComments.position(lineIndex + 1, line.codePointCount(0, i));
					i += 2;
				}
				else if (line.startsWith("\"\"\"", i)) {
					inTextBlock = true;
					i += 3;
				}
				else if (line.charAt(i) == '"' || line.charAt(i) == '\'') {
					final var quote = line.charAt(i++);
					while (i < line.length() && line.charAt(i) != quote)
						i += line.charAt(i) == '\\' ? 2 : 1;
					++i;
				}
				else
					++i;
			}
		}
		return comments;
	}

	private SourceComments() {
	}
}