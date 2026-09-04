package com.etk2000.checkstyle;

import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces optimal lambda parameter types:
 * <ul>
 *   <li>Prefer implicit types over explicit types or {@code var}
 *       when no annotations are present</li>
 *   <li>Prefer {@code var} over explicit types when annotations are present
 *       (annotations require {@code var} or explicit types)</li>
 *   <li>Prefer naked single parameter ({@code x ->}) over
 *       parenthesized ({@code (x) ->})</li>
 * </ul>
 */
public class LambdaParameterTypeCheck extends AbstractAstCheck {
	/**
	 * One parameter as the rewrite needs it. {@code annotations} is the text from where the
	 * parameter's own text begins (just past the {@code (} or the preceding {@code ,}) through its
	 * last annotation, so a comment written ahead of the annotations rides along with them; it is
	 * null when the parameter carries none.
	 */
	public record LambdaParam(@Nonnull String name, @Nullable TextSpan annotations, boolean implicitType) {}

	/** A lambda's parameter list: the span from its {@code (} to its {@code )}, and its parameters. */
	public record LambdaParams(
			@Nonnull TextSpan listSpan,
			@Nonnull List<LambdaParam> params,
			boolean anyAnnotated
	) {}

	static final String MSG_IMPLICIT = "lambda.param.use.implicit";
	static final String MSG_PARENS = "lambda.param.unnecessary.parens";
	static final String MSG_VAR = "lambda.param.use.var";

	/**
	 * Whether the rewrite can fold {@code params} onto its start line without losing anything. A list
	 * written across lines is folded onto one, so a comment anywhere inside it would be deleted, and
	 * a folded line that no longer fits would trade one violation for another. Reporting either costs
	 * a violation whose only outcome is "cannot fix", so the check stays silent and the fixer refuses.
	 */
	@CheckReturnValue
	public static boolean collapsible(
			@Nonnull List<String> lines, @Nonnull List<String> masked, @Nonnull LambdaParams params
	) {
		final var span = params.listSpan();
		final var first = span.start().line();
		final var last = span.end().line();
		if (first == last)
			return true;
		// every other TextSpan consumer goes through AstSpan's bounds check; indexing one directly
		// here would let a substring throw out of visitToken and fail the file rather than one fix
		if (first < 0 || last >= lines.size() || last >= masked.size()
				|| span.start().index() > lines.get(first).length()
				|| span.end().index() > lines.get(last).length())
			return false;

		for (var line = first; line <= last; ++line) {
			final var from = line == first ? span.start().index() : 0;
			final var to = line == last ? span.end().index() : lines.get(line).length();
			if (!lines.get(line).substring(from, to).equals(masked.get(line).substring(from, to)))
				return false;
		}

		final var rewritten = rewrittenList(lines, params);
		if (rewritten == null)
			return false;
		final var folded = lines.get(first).substring(0, span.start().index())
				+ rewritten
				+ lines.get(last).substring(span.end().index());
		return LineLength.tabExpandedLength(folded) <= LineLength.MAX_LINE_LENGTH;
	}

	@CheckReturnValue
	private static List<DetailAST> collectParamDefs(@Nonnull DetailAST parameters) {
		final var paramDefs = new ArrayList<DetailAST>();
		for (var child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.PARAMETER_DEF)
				paramDefs.add(child);
		}
		return paramDefs;
	}

	/**
	 * {@code lambda}'s parameter list, or null when it has no parenthesized list to rewrite (a naked
	 * single parameter, or a shape whose tokens the parse did not produce).
	 */
	@CheckReturnValue
	@Nullable
	private static LambdaParams describe(@Nonnull DetailAST lambda, @Nonnull List<String> lines) {
		final var parameters = lambda.findFirstToken(TokenTypes.PARAMETERS);
		final var lparen = lambda.findFirstToken(TokenTypes.LPAREN);
		final var rparen = lambda.findFirstToken(TokenTypes.RPAREN);
		if (parameters == null || lparen == null || rparen == null)
			return null;

		final var listStart = AstSpan.tokenStart(lines, lparen);
		final var listEnd = AstSpan.tokenEnd(lines, rparen);
		if (listStart == null || listEnd == null)
			return null;

		final var params = new ArrayList<LambdaParam>();
		var anyAnnotated = false;
		var textStart = AstSpan.tokenEnd(lines, lparen);
		for (var child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA) {
				textStart = AstSpan.tokenEnd(lines, child);
				continue;
			}
			if (child.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident == null || textStart == null)
				return null;
			final var lastAnnotation = lastAnnotationOf(child);
			final TextSpan annotations;
			if (lastAnnotation == null)
				annotations = null;
			else {
				final var annotationsEnd = AstSpan.spanEnd(lines, lastAnnotation);
				if (annotationsEnd == null)
					return null;
				annotations = new TextSpan(textStart, annotationsEnd);
				anyAnnotated = true;
			}
			params.add(new LambdaParam(ident.getText(), annotations, isImplicitType(child)));
		}
		return params.isEmpty() ? null : new LambdaParams(new TextSpan(listStart, listEnd), params, anyAnnotated);
	}

	@CheckReturnValue
	@Nonnull
	private static String getTypeName(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";
		final var child = type.getFirstChild();
		if (child == null)
			return "";
		final var primitiveName = switch (child.getType()) {
			case TokenTypes.LITERAL_BOOLEAN -> "boolean";
			case TokenTypes.LITERAL_BYTE -> "byte";
			case TokenTypes.LITERAL_CHAR -> "char";
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			case TokenTypes.LITERAL_SHORT -> "short";
			default -> null;
		};
		if (primitiveName != null)
			return primitiveName;
		return AstText.typeText(type);
	}

	@CheckReturnValue
	private static boolean hasAnnotations(@Nonnull DetailAST paramDef) {
		final var mods = paramDef.findFirstToken(TokenTypes.MODIFIERS);
		return mods != null && mods.findFirstToken(TokenTypes.ANNOTATION) != null;
	}

	@CheckReturnValue
	private static boolean isImplicitType(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		return type == null || type.getFirstChild() == null;
	}

	@CheckReturnValue
	private static boolean isVarType(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null && "var".equals(ident.getText());
	}

	/** {@code paramDef}'s last {@code MODIFIERS} annotation, or null when it carries none. */
	@CheckReturnValue
	@Nullable
	private static DetailAST lastAnnotationOf(@Nonnull DetailAST paramDef) {
		final var mods = paramDef.findFirstToken(TokenTypes.MODIFIERS);
		if (mods == null)
			return null;
		DetailAST last = null;
		for (var child = mods.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION)
				last = child;
		}
		return last;
	}

	/**
	 * The parameter list of the lambda reported at {@code (line, column)}, or null when no lambda is
	 * reported there. A violation lands either on the {@code LAMBDA} itself (the unnecessary-parens
	 * rule) or on one of its {@code PARAMETER_DEF}s, so both are matched.
	 */
	@CheckReturnValue
	@Nullable
	public static LambdaParams paramsAt(
			@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column
	) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LAMBDA && reportsAt(node, line, column))
				return describe(node, lines);
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return null;
	}

	/** Whether {@code lambda} or one of its parameters is positioned at {@code (line, column)}. */
	@CheckReturnValue
	private static boolean reportsAt(@Nonnull DetailAST lambda, int line, int column) {
		if (lambda.getLineNo() == line && lambda.getColumnNo() == column)
			return true;
		final var parameters = lambda.findFirstToken(TokenTypes.PARAMETERS);
		if (parameters == null)
			return false;
		for (var param : collectParamDefs(parameters)) {
			if (param.getLineNo() == line && param.getColumnNo() == column)
				return true;
		}
		return false;
	}

	/**
	 * The parameter list as the rewrite will write it, parentheses included, or the bare name when
	 * the parentheses go too. Java requires one form across a lambda's parameters, so a group with
	 * any annotated parameter puts every parameter on {@code var}.
	 *
	 * <p>Null when an annotation's text cannot be sliced, which is what a span crossing a comment or
	 * a literal does. Returning the parameter without its annotations instead would delete them.
	 */
	@CheckReturnValue
	@Nullable
	public static String rewrittenList(@Nonnull List<String> lines, @Nonnull LambdaParams params) {
		final var written = params.params();
		if (written.size() == 1 && !params.anyAnnotated())
			return written.getFirst().name();

		final var sb = new StringBuilder("(");
		for (var i = 0; i < written.size(); ++i) {
			if (i > 0)
				sb.append(", ");
			final var text = rewrittenParam(lines, written.get(i), params.anyAnnotated());
			if (text == null)
				return null;
			sb.append(text);
		}
		return sb.append(')').toString();
	}

	@CheckReturnValue
	@Nullable
	private static String rewrittenParam(
			@Nonnull List<String> lines, @Nonnull LambdaParam param, boolean anyAnnotated
	) {
		if (!anyAnnotated || param.implicitType())
			return param.name();
		final var annotations = param.annotations();
		if (annotations == null)
			return "var " + param.name();
		final var text = AstSpan.sliceSpan(lines, annotations.start(), annotations.end());
		return text == null ? null : text.strip() + " var " + param.name();
	}

	private List<String> maskedLines = List.of(), sourceLines = List.of();

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		sourceLines = List.of(getLines());
		maskedLines = JavaLineScanner.maskAll(sourceLines);
	}

	@CheckReturnValue
	private boolean collapsible(@Nonnull DetailAST lambda) {
		final var described = describe(lambda, sourceLines);
		return described != null && collapsible(sourceLines, maskedLines, described);
	}

	@Override
	public void finishTree(@Nullable DetailAST rootAST) {
		maskedLines = List.of();
		sourceLines = List.of();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LAMBDA};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;

		final var paramDefs = collectParamDefs(params);
		if (paramDefs.isEmpty() || !collapsible(ast))
			return;

		if (paramDefs.size() == 1 && isImplicitType(paramDefs.getFirst()) && !hasAnnotations(paramDefs.getFirst())) {
			log(ast, MSG_PARENS);
			return;
		}

		var anyAnnotated = false;
		for (var param : paramDefs) {
			if (hasAnnotations(param)) {
				anyAnnotated = true;
				break;
			}
		}

		for (var param : paramDefs) {
			if (isImplicitType(param))
				continue;

			if (anyAnnotated) {
				if (!isVarType(param))
					log(param, MSG_VAR, getTypeName(param));
			}
			else
				log(param, MSG_IMPLICIT, getTypeName(param));
		}
	}
}