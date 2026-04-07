package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces stacked annotation formatting: each annotation
 * on its own line, no blank lines between annotations or before the declaration,
 * and alphabetical order. Applies to classes, interfaces, enums, records,
 * annotation types, methods, constructors, fields, local variables, enum
 * constants, annotation fields, and package declarations.
 */
public class AnnotationOwnLineCheck extends AbstractCheck {
	private static final String MSG_BLANK_LINE = "annotation.own.line.blank";
	private static final String MSG_KEY = "annotation.own.line";
	private static final String MSG_ORDER = "annotation.alphabetical.order";

	@CheckReturnValue
	private static int declarationLine(@Nonnull DetailAST modifiersOrAnnotations) {
		// for ANNOTATIONS under PACKAGE_DEF or ENUM_CONSTANT_DEF
		if (modifiersOrAnnotations.getType() == TokenTypes.ANNOTATIONS) {
			final var parent = modifiersOrAnnotations.getParent();
			for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.ANNOTATIONS)
					return child.getLineNo();
			}
			return parent.getLineNo();
		}

		// for MODIFIERS, find the first non-ANNOTATION sibling within MODIFIERS
		for (var child = modifiersOrAnnotations.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				return child.getLineNo();
		}

		// all children are annotations, so find the next sibling of MODIFIERS in the parent
		final var nextSibling = modifiersOrAnnotations.getNextSibling();
		if (nextSibling != null)
			return nextSibling.getLineNo();

		return modifiersOrAnnotations.getParent().getLineNo();
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
			     TokenTypes.CTOR_DEF, TokenTypes.ENUM_CONSTANT_DEF,
			     TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF,
			     TokenTypes.METHOD_DEF, TokenTypes.RECORD_DEF -> true;
			case TokenTypes.VARIABLE_DEF -> {
				final var grandparent = parent.getParent();
				yield grandparent == null
						|| (grandparent.getType() != TokenTypes.FOR_EACH_CLAUSE
						&& grandparent.getType() != TokenTypes.FOR_INIT);
			}
			default -> false;
		};
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATIONS, TokenTypes.MODIFIERS};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (!isOwnLineContext(ast))
			return;

		final var annotations = AstUtil.collectAnnotations(ast);
		if (annotations.isEmpty())
			return;

		final var declLine = declarationLine(ast);
		final var reportedLines = new HashSet<Integer>();

		// check same-line violations (multiple annotations or annotation + declaration)
		for (final var annotation : annotations) {
			final var line = annotation.getLineNo();
			if (reportedLines.contains(line))
				continue;

			var sharesLine = false;
			for (final var other : annotations) {
				if (other != annotation && other.getLineNo() == line) {
					sharesLine = true;
					break;
				}
			}

			if (sharesLine || line == declLine) {
				log(annotation, MSG_KEY, AstUtil.annotationName(annotation));
				reportedLines.add(line);
			}
		}

		// check blank lines between consecutive annotations, and between last annotation and declaration
		for (var i = 0; i < annotations.size(); ++i) {
			final var currentLine = annotations.get(i).getLineNo();
			final var nextLine = i + 1 < annotations.size()
					? annotations.get(i + 1).getLineNo()
					: declLine;
			if (nextLine - currentLine > 1)
				log(annotations.get(i), MSG_BLANK_LINE, AstUtil.annotationName(annotations.get(i)));
		}

		// check alphabetical order
		String previousName = null;
		for (final var annotation : annotations) {
			final var name = AstUtil.annotationName(annotation);
			if (previousName != null && name.compareTo(previousName) < 0)
				log(annotation, MSG_ORDER, name, previousName);
			previousName = name;
		}
	}
}