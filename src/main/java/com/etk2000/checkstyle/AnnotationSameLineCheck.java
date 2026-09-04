package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces inline annotation formatting: all annotations
 * on the same line as the declaration and in alphabetical order. Applies to
 * method parameters, constructor parameters, catch parameters, lambda
 * parameters, record components, and for-each and for-init loop variables.
 */
public class AnnotationSameLineCheck extends AbstractAstCheck {
	/** Which placement rule an annotation group breaks. */
	public enum AnnotationPlacement {
		ALPHABETICAL_ORDER("annotation.alphabetical.order"),
		SAME_LINE("annotation.same.line");

		private final String messageKey;

		AnnotationPlacement(@Nonnull String messageKey) {
			this.messageKey = messageKey;
		}

		@CheckReturnValue
		@Nonnull
		public String messageKey() {
			return messageKey;
		}
	}

	/**
	 * One reported annotation, with everything the fix needs around it: the whole group in source
	 * order and the node whose line the group has to end up on.
	 *
	 * @param previousName the annotation {@code reported} has to move in front of, or null for
	 *                     {@link AnnotationPlacement#SAME_LINE}, which names no second annotation
	 */
	public record AnnotationPlacementTarget(
			@Nonnull AnnotationPlacement placement,
			@Nonnull DetailAST owner,
			@Nonnull List<DetailAST> annotations,
			@Nonnull DetailAST reported,
			@Nonnull DetailAST anchor,
			@Nonnull String targetName,
			@Nullable String previousName
	) {}

	/**
	 * The token the group's annotations have to share a line with: the declaration's type, failing
	 * that its name, failing that the declaration itself.
	 *
	 * <p>Resolved to the leftmost token rather than taken as the node, because a {@code TYPE} over a
	 * qualified name is positioned on its outer dot. Without that, {@code @A java.util} with
	 * {@code .List<String> p} below reports a violation whose declaration does start on the
	 * annotation's line, which no fix can clear.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST declarationAnchor(@Nonnull DetailAST modifiersOrAnnotations) {
		if (modifiersOrAnnotations.getType() == TokenTypes.ANNOTATIONS) {
			final var nextSibling = modifiersOrAnnotations.getNextSibling();
			if (nextSibling != null)
				return AstSpan.startToken(nextSibling);
			return AstSpan.startToken(modifiersOrAnnotations.getParent());
		}

		final var parent = modifiersOrAnnotations.getParent();
		final var type = parent.findFirstToken(TokenTypes.TYPE);
		if (type != null)
			return AstSpan.startToken(type);

		final var ident = parent.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return AstSpan.startToken(ident);

		return AstSpan.startToken(parent);
	}

	@CheckReturnValue
	private static boolean isSameLineContext(@Nonnull DetailAST ast) {
		final var parent = ast.getParent();
		if (parent == null)
			return false;

		// ANNOTATIONS token is used by RECORD_COMPONENT_DEF instead of MODIFIERS
		if (ast.getType() == TokenTypes.ANNOTATIONS)
			return parent.getType() == TokenTypes.RECORD_COMPONENT_DEF;

		return switch (parent.getType()) {
			case TokenTypes.PARAMETER_DEF -> true;
			case TokenTypes.VARIABLE_DEF -> {
				final var grandparent = parent.getParent();
				yield grandparent != null
						&& (grandparent.getType() == TokenTypes.FOR_EACH_CLAUSE
						|| grandparent.getType() == TokenTypes.FOR_INIT);
			}
			default -> false;
		};
	}

	/**
	 * What this check reports at {@code line} (0-based) and {@code column} (a 0-based code-point
	 * column), or null when it reports nothing there. The position is an annotation's own, so a
	 * group member the check leaves alone answers null as surely as a token that is not an
	 * annotation at all.
	 */
	@CheckReturnValue
	@Nullable
	public static AnnotationPlacementTarget placementAt(@Nonnull DetailAST root, int line, int column) {
		final var annotation = AstQuery.findNodeAt(
				root, line, column, ast -> ast.getType() == TokenTypes.ANNOTATION
		);
		if (annotation == null)
			return null;

		for (var target : placementTargets(annotation.getParent())) {
			if (target.reported() == annotation)
				return target;
		}
		return null;
	}

	/**
	 * Every placement {@code modifiersOrAnnotations} breaks, in the order {@code visitToken} reports
	 * them: at most one {@link AnnotationPlacement#SAME_LINE}, since the rest of the group moves with
	 * it, or one {@link AnnotationPlacement#ALPHABETICAL_ORDER} per annotation out of order.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AnnotationPlacementTarget> placementTargets(@Nullable DetailAST modifiersOrAnnotations) {
		if (modifiersOrAnnotations == null || !isSameLineContext(modifiersOrAnnotations))
			return List.of();

		final var annotations = AstQuery.collectAnnotations(modifiersOrAnnotations);
		if (annotations.isEmpty())
			return List.of();

		final var anchor = declarationAnchor(modifiersOrAnnotations);
		for (var annotation : annotations) {
			if (annotation.getLineNo() != anchor.getLineNo()) {
				return List.of(new AnnotationPlacementTarget(
						AnnotationPlacement.SAME_LINE,
						modifiersOrAnnotations,
						annotations,
						annotation,
						anchor,
						AstText.annotationName(annotation),
						null
				));
			}
		}

		// alphabetical order is only asked once every annotation is inline, since the same-line fix
		// sorts the group on its way in and would otherwise be asked for twice
		final var targets = new ArrayList<AnnotationPlacementTarget>();
		String previousName = null;
		for (var annotation : annotations) {
			final var name = AstText.annotationName(annotation);
			if (previousName != null && name.compareTo(previousName) < 0) {
				targets.add(new AnnotationPlacementTarget(
						AnnotationPlacement.ALPHABETICAL_ORDER,
						modifiersOrAnnotations,
						annotations,
						annotation,
						anchor,
						name,
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
		for (var target : placementTargets(ast)) {
			final var previousName = target.previousName();
			if (previousName == null)
				log(target.reported(), target.placement().messageKey(), target.targetName());
			else
				log(target.reported(), target.placement().messageKey(), target.targetName(), previousName);
		}
	}
}