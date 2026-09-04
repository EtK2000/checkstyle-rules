package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces stacked annotation formatting: each annotation
 * on its own line, no blank lines between annotations or before the declaration,
 * and alphabetical order. Applies to classes, interfaces, enums, records,
 * annotation types, methods, constructors, fields, local variables, enum
 * constants, annotation fields, and package declarations.
 */
public class AnnotationOwnLineCheck extends AbstractAstCheck {
	/** Which layout rule a stacked annotation group breaks. */
	public enum AnnotationLayout {
		ALPHABETICAL_ORDER("annotation.alphabetical.order"),
		BLANK_AFTER("annotation.own.line.blank"),
		BLANK_INTERNAL("annotation.own.line.blank.internal"),
		OWN_LINE("annotation.own.line");

		private final String messageKey;

		AnnotationLayout(@Nonnull String messageKey) {
			this.messageKey = messageKey;
		}

		@CheckReturnValue
		@Nonnull
		public String messageKey() {
			return messageKey;
		}
	}

	/**
	 * One reported violation: the annotation it names, the group it belongs to, and the blank line it
	 * is about.
	 *
	 * @param reportedColumn the 0-based code-point column the check logs at, which for
	 *                       {@link AnnotationLayout#BLANK_AFTER} is the annotation's own column
	 *                       clamped to the line it is reported on. Checkstyle expands the tabs ahead
	 *                       of a logged column by reading that line up to it, so a column past its
	 *                       end throws out of {@code visitToken} and fails the whole file
	 * @param blankLine      the 1-based blank line the layout is about, or 0 when it is about none
	 * @param previousName   the annotation {@code reported} has to move in front of, or null for
	 *                       every layout but {@link AnnotationLayout#ALPHABETICAL_ORDER}
	 */
	public record AnnotationLayoutTarget(
			@Nonnull AnnotationLayout layout,
			@Nonnull DetailAST holder,
			@Nonnull DetailAST reported,
			int reportedColumn,
			int blankLine,
			@Nullable String previousName
	) {
		@CheckReturnValue
		@Nonnull
		public DetailAST anchor() {
			return declarationAnchor(holder);
		}

		@CheckReturnValue
		@Nonnull
		public List<DetailAST> annotations() {
			return AstQuery.collectAnnotations(holder);
		}

		/** The 1-based line the check logs this violation at. */
		@CheckReturnValue
		public int reportedLine() {
			return switch (layout) {
				case BLANK_AFTER -> AstQuery.lastLine(reported);
				case BLANK_INTERNAL -> blankLine;
				default -> reported.getLineNo();
			};
		}

		@CheckReturnValue
		@Nonnull
		public String targetName() {
			return AstText.annotationName(reported);
		}
	}

	/**
	 * The token the group's annotations have to sit above: the declaration's first modifier keyword,
	 * failing that whatever follows the group, failing that the declaration itself.
	 *
	 * <p>Resolved to the leftmost token rather than taken as the node, because a {@code TYPE} over a
	 * qualified name is positioned on its outer dot. Without that, {@code @A java.util} with
	 * {@code .List<String> x;} below puts the declaration on the second line and the annotation
	 * sharing its first line goes unreported.
	 *
	 * <p>The two fallbacks end on the declaration node itself, which is positioned at the group, so
	 * they are left unresolved: taking the leftmost token there would answer the annotation's own
	 * position and report every group that reaches them.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST declarationAnchor(@Nonnull DetailAST modifiersOrAnnotations) {
		if (modifiersOrAnnotations.getType() == TokenTypes.ANNOTATIONS) {
			final var parent = modifiersOrAnnotations.getParent();
			for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.ANNOTATIONS)
					return AstSpan.startToken(child);
			}
			return parent;
		}

		for (var child = modifiersOrAnnotations.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				return AstSpan.startToken(child);
		}

		final var nextSibling = modifiersOrAnnotations.getNextSibling();
		if (nextSibling != null)
			return AstSpan.startToken(nextSibling);

		return modifiersOrAnnotations.getParent();
	}

	/**
	 * Whether {@code line} (1-based) falls strictly inside a text block written in {@code annotation}.
	 *
	 * <p>Asked of the tree rather than of a line scan, because a scan has to start somewhere known. It
	 * cannot start at the annotation's own first line: that line may be the closing line of a block
	 * comment, and a quote left inside the comment's tail puts the scan one string out of step, so a
	 * literal that is open reads as closed. Starting at the file's first line is correct but costs a
	 * fold over the whole file per annotation. The literal's own delimiters carry its exact span, so
	 * neither is needed.
	 */
	@CheckReturnValue
	private static boolean insideTextBlock(@Nonnull DetailAST annotation, int line) {
		for (var begin : AstQuery.collectMatching(
				annotation, ast -> ast.getType() == TokenTypes.TEXT_BLOCK_LITERAL_BEGIN
		)) {
			final var end = begin.findFirstToken(TokenTypes.TEXT_BLOCK_LITERAL_END);
			if (end != null && begin.getLineNo() < line && line < end.getLineNo())
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isOwnLineContext(@Nonnull DetailAST ast) {
		final var parent = ast.getParent();
		if (parent == null)
			return false;

		if (ast.getType() == TokenTypes.ANNOTATIONS) {
			return parent.getType() == TokenTypes.ENUM_CONSTANT_DEF
					|| parent.getType() == TokenTypes.PACKAGE_DEF;
		}

		return switch (parent.getType()) {
			case TokenTypes.ANNOTATION_DEF, TokenTypes.ANNOTATION_FIELD_DEF,
			     TokenTypes.CLASS_DEF, TokenTypes.COMPACT_CTOR_DEF,
			     TokenTypes.CTOR_DEF, TokenTypes.ENUM_DEF,
			     TokenTypes.INTERFACE_DEF, TokenTypes.METHOD_DEF,
			     TokenTypes.RECORD_DEF -> true;
			case TokenTypes.VARIABLE_DEF -> {
				final var grandparent = parent.getParent();
				yield grandparent == null
						|| (grandparent.getType() != TokenTypes.FOR_EACH_CLAUSE
						&& grandparent.getType() != TokenTypes.FOR_INIT);
			}
			default -> false;
		};
	}

	/**
	 * What this check reports at {@code line} (0-based) and {@code column} (a 0-based code-point
	 * column), or null when it reports nothing there. Two of the four layouts are logged away from
	 * the annotation's own token, so a position that holds no annotation can still answer.
	 */
	@CheckReturnValue
	@Nullable
	public static AnnotationLayoutTarget layoutAt(
			@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column
	) {
		AnnotationLayoutTarget found = null;
		for (var holder : AstQuery.collectMatching(
				root,
				ast -> ast.getType() == TokenTypes.MODIFIERS || ast.getType() == TokenTypes.ANNOTATIONS
		)) {
			for (var target : layoutTargets(holder, lines)) {
				if (target.reportedLine() != line + 1 || target.reportedColumn() != column)
					continue;

				// re-emitting the group spans the gap after it as well, so an OWN_LINE or
				// ALPHABETICAL_ORDER answer subsumes a BLANK_AFTER reported at the same position.
				// BLANK_INTERNAL shares a position with nothing, being column 0 of a blank line
				if (found == null || found.layout() == AnnotationLayout.BLANK_AFTER)
					found = target;
			}
		}
		return found;
	}

	/**
	 * Every layout {@code modifiersOrAnnotations} breaks, in the order {@code visitToken} reports
	 * them: one {@link AnnotationLayout#OWN_LINE} per line that shares annotations or holds the
	 * declaration, then at most one blank-line layout per annotation, then one
	 * {@link AnnotationLayout#ALPHABETICAL_ORDER} per annotation out of order.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AnnotationLayoutTarget> layoutTargets(
			@Nullable DetailAST modifiersOrAnnotations, @Nonnull List<String> lines
	) {
		if (modifiersOrAnnotations == null || !isOwnLineContext(modifiersOrAnnotations))
			return List.of();

		final var annotations = AstQuery.collectAnnotations(modifiersOrAnnotations);
		if (annotations.isEmpty())
			return List.of();

		final var anchor = declarationAnchor(modifiersOrAnnotations);
		final var declLine = anchor.getLineNo();
		final var targets = new ArrayList<AnnotationLayoutTarget>();

		final var lineCount = new HashMap<Integer, Integer>();
		for (var annotation : annotations)
			lineCount.merge(annotation.getLineNo(), 1, Integer::sum);

		final var reportedLines = new HashSet<Integer>();
		for (var annotation : annotations) {
			final var line = annotation.getLineNo();
			if (reportedLines.contains(line))
				continue;

			if (lineCount.get(line) > 1 || line == declLine) {
				targets.add(new AnnotationLayoutTarget(
						AnnotationLayout.OWN_LINE,
						modifiersOrAnnotations,
						annotation,
						annotation.getColumnNo(),
						0,
						null
				));
				reportedLines.add(line);
			}
		}

		for (var i = 0; i < annotations.size(); ++i) {
			final var annotation = annotations.get(i);
			final var lastLine = AstQuery.lastLine(annotation);

			final var internalScanEnd = Math.min(lastLine, lines.size());
			for (var line = annotation.getLineNo(); line < internalScanEnd; ++line) {
				// a blank line written inside a text block is part of the string, and deleting it
				// would change the value, so it is not a blank line this check is about
				if (lines.get(line).isBlank() && !insideTextBlock(annotation, line + 1)) {
					targets.add(new AnnotationLayoutTarget(
							AnnotationLayout.BLANK_INTERNAL,
							modifiersOrAnnotations,
							annotation,
							0,
							line + 1,
							null
					));
					break;
				}
			}

			final var nextLine = i + 1 < annotations.size()
					? annotations.get(i + 1).getLineNo()
					: declLine;
			if (nextLine - lastLine > 1) {
				final var betweenScanEnd = Math.min(nextLine - 1, lines.size());
				var state = JavaLineScanner.LexerState.NONE;
				for (var line = lastLine; line < betweenScanEnd; ++line) {
					if (!state.inBlockComment() && lines.get(line).isBlank()) {
						targets.add(new AnnotationLayoutTarget(
								AnnotationLayout.BLANK_AFTER,
								modifiersOrAnnotations,
								annotation,
								Math.min(annotation.getColumnNo(), lines.get(lastLine - 1).length()),
								line + 1,
								null
						));
						break;
					}
					state = JavaLineScanner.stateAfter(lines.get(line), state);
				}
			}
		}

		String previousName = null;
		for (var annotation : annotations) {
			final var name = AstText.annotationName(annotation);
			if (previousName != null && name.compareTo(previousName) < 0) {
				targets.add(new AnnotationLayoutTarget(
						AnnotationLayout.ALPHABETICAL_ORDER,
						modifiersOrAnnotations,
						annotation,
						annotation.getColumnNo(),
						0,
						previousName
				));
			}
			previousName = name;
		}
		return targets;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATIONS, TokenTypes.MODIFIERS};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// asked before the lines are, so a node with no file behind it stays as silent as it was
		// before the blank-line scans needed them, and so most modifier lists cost nothing
		if (!isOwnLineContext(ast))
			return;

		for (var target : layoutTargets(ast, List.of(getLines()))) {
			final var key = target.layout().messageKey();
			final var name = target.targetName();
			switch (target.layout()) {
				case ALPHABETICAL_ORDER -> log(target.reported(), key, name, target.previousName());
				case BLANK_AFTER, BLANK_INTERNAL -> log(target.reportedLine(), target.reportedColumn(), key, name);
				case OWN_LINE -> log(target.reported(), key, name);
			}
		}
	}
}