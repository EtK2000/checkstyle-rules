package com.etk2000.checkstyle;

import com.etk2000.checkstyle.format.SpanReformat;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Maps an AST subtree back to the source text it was parsed from: the boundaries of the span it
 * occupies, and the text inside those boundaries taken verbatim.
 *
 * <p>This sits beside {@link LineText} and {@link JavaLineScanner} rather than in
 * {@code com.etk2000.checkstyle.ast} because it needs both of them, and that package's contract
 * (see its {@code package-info}) is that {@code AstText} and {@code AstQuery} depend on nothing
 * outside checkstyle's own API. Siting the span primitives there would invert that and make the
 * {@code ast} package depend back on the root it sits below.
 *
 * <p>Public because {@code com.etk2000.checkstyle.gradle.fix} consumes it. Note the two column
 * spaces this class straddles: a {@link DetailAST} position counts code points, while a
 * {@link TextPos#index} is a char index into its own line. Everything here converts at the
 * boundary, so a caller that builds a {@code TextPos} must convert too rather than passing a
 * column through.
 */
public final class AstSpan {
	/**
	 * A resolved source position: a 0-based line index and a <em>char</em> index into that line.
	 * Distinct from a {@link DetailAST} position, whose column counts code points, so every value
	 * that reaches a {@code substring} here is this type rather than a bare column.
	 *
	 * @see LineText#charIndexOfColumn
	 */
	public record TextPos(int line, int index) {}

	/**
	 * Whether the source actually reads {@code node}'s text at {@code node}'s position. An imaginary
	 * node carries its own token name as {@code getText()} and borrows its position from a child (or,
	 * when childless, from the token that follows), so its length says nothing about the source and
	 * must never measure a span.
	 *
	 * <p>Asking the source rather than comparing the text to the node's token name: an identifier may
	 * legitimately be spelled like one, and {@code TokenTypes.IDENT} appears throughout this project.
	 * Reading that as structure truncated the span before it, and the slice went to disk.
	 */
	@CheckReturnValue
	private static boolean isRealToken(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var lineIndex = node.getLineNo() - 1;
		if (lineIndex < 0 || lineIndex >= lines.size())
			return false;
		final var text = lines.get(lineIndex);
		final var start = LineText.charIndexOfColumn(text, node.getColumnNo());
		return start >= 0 && text.startsWith(node.getText(), start);
	}

	/** The verbatim source text of {@code node}'s own subtree, or null when it does not slice. */
	@CheckReturnValue
	@Nullable
	public static String sliceNode(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		return sliceSpan(lines, spanStart(lines, node), spanEnd(lines, node));
	}

	/**
	 * Slices the source text of the span {@code [start, end)}, or {@code null} when either end is
	 * absent or they run backwards. A single-line span is returned verbatim. A multi-line span is
	 * refused when a comment or a text block falls inside it (collapsing it to one line would
	 * comment out the trailing code, or strip the line terminator a text block's opening delimiter
	 * requires); otherwise its lines are joined, each stripped, with a single space where one is
	 * needed to keep adjacent tokens apart (see {@link SpanReformat#joinsTight}).
	 */
	@CheckReturnValue
	@Nullable
	public static String sliceSpan(@Nonnull List<String> lines, @Nullable TextPos start, @Nullable TextPos end) {
		if (start == null || end == null)
			return null;
		// substring throws rather than returning empty when begin > end, and a throw out of
		// visitScopedToken fails the whole file rather than skipping one fix
		if (start.line() > end.line() || (start.line() == end.line() && start.index() > end.index()))
			return null;
		// spanStart and spanEnd bound their own output, but a caller-built TextPos does not, and an
		// IndexOutOfBoundsException out of visitToken fails the whole file rather than one fix
		if (start.line() < 0 || end.line() >= lines.size())
			return null;
		if (start.index() < 0 || start.index() > lines.get(start.line()).length()
				|| end.index() < 0 || end.index() > lines.get(end.line()).length())
			return null;

		if (start.line() == end.line())
			return lines.get(start.line()).substring(start.index(), end.index());
		// The operand starts on a code token, so the lexer state at its start column is NONE. Scan
		// only the operand region [from, to) of each line (not the whole line) so a comment BEFORE
		// the operand cannot mask one inside it, and carry the state over that region alone.
		var state = JavaLineScanner.LexerState.NONE;
		final var joined = new StringBuilder();
		for (var i = start.line(); i <= end.line(); ++i) {
			final var line = lines.get(i);
			final var from = i == start.line() ? start.index() : 0;
			final var to = i == end.line() ? end.index() : line.length();
			final var region = line.substring(from, to);
			if (state.inMultilineLiteral())
				return null;
			if (JavaLineScanner.firstCommentMarker(region, state) >= 0)
				return null;
			final var fragment = region.strip();
			if (!fragment.isEmpty()) {
				if (!joined.isEmpty() && !SpanReformat.joinsTight(joined, fragment))
					joined.append(' ');
				joined.append(fragment);
			}
			state = JavaLineScanner.stateAfter(region, state);
		}
		return joined.isEmpty() ? null : joined.toString();
	}

	/**
	 * The position immediately past the last token in {@code ast}'s subtree, or null when that
	 * token does not describe a span on its own line. Tokens do not overlap, so the real token
	 * whose start position is furthest right is the last one; its length gives the end.
	 *
	 * <p>Found in two passes rather than by filtering one. Taking the furthest-right node that
	 * {@link #isRealToken} accepts would make any rejection of the true last token fall back to
	 * something to its left, silently shortening the span; a short span still looks like source and
	 * goes to disk. So the first pass takes the furthest-right position over every node, imaginary
	 * ones included, which is sound because structure always borrows its position from a real token.
	 * The second pass looks only at that one position and takes the longest text the source actually
	 * reads there, so a node borrowing the position contributes its token name only if nothing real
	 * is there to beat it. Nothing verified at the end position means no answer, not a nearer one.
	 *
	 * <p>Verifying only at the end position also keeps the cost off the node count: the conversion
	 * in {@code isRealToken} walks the line, and running it per node is quadratic on a generated
	 * single-line file.
	 */
	@CheckReturnValue
	@Nullable
	public static TextPos spanEnd(@Nonnull List<String> lines, @Nonnull DetailAST ast) {
		var top = ast;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			// both sides are AST columns, so this comparison stays in code-point space
			if (node.getLineNo() > top.getLineNo()
					|| (node.getLineNo() == top.getLineNo() && node.getColumnNo() > top.getColumnNo()))
				top = node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}

		DetailAST best = null;
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() == top.getLineNo() && node.getColumnNo() == top.getColumnNo()
					&& (best == null || node.getText().length() > best.getText().length())
					&& isRealToken(lines, node))
				best = node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		if (best == null)
			return null;

		// no bounds re-checked here: isRealToken already required the line to be in range, the column
		// to convert, and the text to fit on the line, all on these same inputs. Re-testing them
		// would be three branches no input can reach
		final var line = best.getLineNo() - 1;
		final var text = lines.get(line);
		// convert the token's start, then add its char length: adding a char length to a
		// code-point column mixes the units and loses one char per supplementary code point
		// earlier on the line
		return new TextPos(line, LineText.charIndexOfColumn(text, best.getColumnNo()) + best.getText().length());
	}

	/**
	 * The position of the first token in {@code ast}'s subtree (its leftmost, topmost position), or
	 * null when that position does not fall on a line of {@code lines}.
	 */
	@CheckReturnValue
	@Nullable
	public static TextPos spanStart(@Nonnull List<String> lines, @Nonnull DetailAST ast) {
		var line = ast.getLineNo();
		var col = ast.getColumnNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			// both sides are AST columns, so this comparison stays in code-point space
			if (node.getLineNo() < line || (node.getLineNo() == line && node.getColumnNo() < col)) {
				line = node.getLineNo();
				col = node.getColumnNo();
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}

		final var lineIndex = line - 1;
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var index = LineText.charIndexOfColumn(lines.get(lineIndex), col);
		return index < 0 ? null : new TextPos(lineIndex, index);
	}

	private AstSpan() {
	}
}