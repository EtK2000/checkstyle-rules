package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags redundant annotation syntax:
 * <ul>
 *   <li>{@code @A()} should be {@code @A} (empty parentheses)</li>
 *   <li>{@code @A(value = x)} should be {@code @A(x)} (explicit {@code value} key)</li>
 * </ul>
 */
public class RedundantAnnotationSyntaxCheck extends AbstractAstCheck {
	/** Which redundant syntax an annotation carries. */
	public enum AnnotationRedundancy {
		EMPTY_PARENS("annotation.syntax.empty.parens"),
		EXPLICIT_VALUE("annotation.syntax.explicit.value");

		private final String messageKey;

		AnnotationRedundancy(@Nonnull String messageKey) {
			this.messageKey = messageKey;
		}

		@CheckReturnValue
		@Nonnull
		public String messageKey() {
			return messageKey;
		}
	}

	/**
	 * The redundancy the check reported at a position, with the nodes whose positions bound the
	 * text that goes: {@code name} is the {@code IDENT} or {@code DOT} spelling the annotation's
	 * type, and {@code key} and {@code value} are the {@code value} identifier and what it is
	 * assigned. Both of the last two are null for {@link AnnotationRedundancy#EMPTY_PARENS} and
	 * neither is for {@link AnnotationRedundancy#EXPLICIT_VALUE}.
	 *
	 * <p>{@code targetName} is the name as the message spells it, which is the unqualified one:
	 * {@code @com.example.A()} reports {@code '@A'}.
	 *
	 * @see #redundantSyntaxAt
	 */
	public record AnnotationSyntaxTarget(
			@Nonnull AnnotationRedundancy redundancy,
			@Nonnull DetailAST annotation,
			@Nonnull DetailAST name,
			@Nonnull DetailAST rparen,
			@Nullable DetailAST key,
			@Nullable DetailAST value,
			@Nonnull String targetName
	) {}

	/**
	 * The node spelling {@code annotation}'s type: one {@code IDENT} for a simple name, the
	 * {@code DOT} chain for a qualified one. Neither is the annotation's own position, which is the
	 * {@code @}, and neither has to be on the annotation's line: {@code @com.example} with
	 * {@code .A()} below it puts the chain's last identifier on the next one.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST nameOf(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident : annotation.findFirstToken(TokenTypes.DOT);
	}

	/**
	 * The redundant syntax of the annotation whose {@code @} stands at {@code (line, column)}, or
	 * null when nothing there is one the check reports. {@code line} is 0-based and {@code column}
	 * is a 0-based code-point column.
	 *
	 * <p>The position is the annotation's, because that is the node {@link #visitToken} logs
	 * against.
	 */
	@CheckReturnValue
	@Nullable
	public static AnnotationSyntaxTarget redundantSyntaxAt(@Nonnull DetailAST root, int line, int column) {
		final var annotation = AstQuery.findNodeAt(
				root, line, column, ast -> ast.getType() == TokenTypes.ANNOTATION
		);
		return annotation == null ? null : targetFor(annotation);
	}

	/**
	 * What {@code annotation} carries that the check reports, or null when it carries neither.
	 *
	 * <p>Both answers need the closing parenthesis, since it is what bounds the text either fix
	 * deletes. The grammar gives every annotation that opened one, so requiring it up front rather
	 * than only on the empty-parentheses arm cannot narrow what is reported.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationSyntaxTarget targetFor(@Nonnull DetailAST annotation) {
		final var name = nameOf(annotation);
		final var rparen = annotation.findFirstToken(TokenTypes.RPAREN);
		if (annotation.findFirstToken(TokenTypes.LPAREN) == null || name == null || rparen == null)
			return null;

		final var pair = annotation.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
		if (pair == null) {
			// a comment is not a member, so parentheses holding only one are still empty
			return annotation.findFirstToken(TokenTypes.EXPR) == null
					&& annotation.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT) == null
					&& annotation.findFirstToken(TokenTypes.ANNOTATION) == null
					? new AnnotationSyntaxTarget(
							AnnotationRedundancy.EMPTY_PARENS,
							annotation,
							name,
							rparen,
							null,
							null,
							AstText.annotationName(annotation)
					)
					: null;
		}

		if (annotation.findFirstToken(TokenTypes.COMMA) != null)
			return null;

		final var key = pair.findFirstToken(TokenTypes.IDENT);
		final var assign = pair.findFirstToken(TokenTypes.ASSIGN);
		final var value = assign == null ? null : assign.getNextSibling();
		return key == null || value == null || !"value".equals(key.getText())
				? null
				: new AnnotationSyntaxTarget(
						AnnotationRedundancy.EXPLICIT_VALUE,
						annotation,
						name,
						rparen,
						key,
						value,
						AstText.annotationName(annotation)
				);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATION};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast);
		if (target != null)
			log(ast, target.redundancy().messageKey(), target.targetName());
	}
}