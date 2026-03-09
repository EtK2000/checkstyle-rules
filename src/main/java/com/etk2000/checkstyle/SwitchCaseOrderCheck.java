package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces sorted case labels in switch statements.
 * Named constants sort alphabetically first, then numeric literals sort numerically.
 * Default must be last.
 */
public class SwitchCaseOrderCheck extends AbstractCheck {
	private static final String MSG_INTERNAL_ORDER = "switch.case.internal.order";
	private static final String MSG_ORDER = "switch.case.order";

	@CheckReturnValue
	private static int compareLabels(@Nonnull String a, @Nonnull String b) {
		final var aNumeric = isNumericLabel(a);
		final var bNumeric = isNumericLabel(b);

		// named constants sort before numeric literals
		if (aNumeric != bNumeric)
			return aNumeric ? 1 : -1;

		if (aNumeric)
			return Double.compare(parseNumeric(a), parseNumeric(b));

		return a.compareToIgnoreCase(b);
	}

	@CheckReturnValue
	@Nonnull
	private static String extractLabelText(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.CHAR_LITERAL, TokenTypes.IDENT, TokenTypes.NUM_DOUBLE,
			     TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG,
			     TokenTypes.STRING_LITERAL -> expr.getText();
			case TokenTypes.DOT -> {
				var last = expr.getFirstChild();
				while (last.getNextSibling() != null)
					last = last.getNextSibling();
				yield last.getText();
			}
			case TokenTypes.EXPR -> extractLabelText(expr.getFirstChild());
			case TokenTypes.UNARY_MINUS -> "-" + extractLabelText(expr.getFirstChild());
			case TokenTypes.UNARY_PLUS -> extractLabelText(expr.getFirstChild());
			default -> expr.getText();
		};
	}

	@CheckReturnValue
	private static DetailAST findFirstCaseExpr(@Nonnull DetailAST literalCase) {
		for (var child = literalCase.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				return child;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static String getFirstLabel(@Nonnull DetailAST caseGroupOrRule) {
		for (var child = caseGroupOrRule.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.LITERAL_CASE) {
				final var firstExpr = findFirstCaseExpr(child);
				if (firstExpr != null)
					return extractLabelText(firstExpr);
			}
		}
		return "";
	}

	@CheckReturnValue
	@Nonnull
	private static ArrayList<String> getLabels(@Nonnull DetailAST caseGroupOrRule) {
		final var labels = new ArrayList<String>();
		for (var child = caseGroupOrRule.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.LITERAL_CASE) {
				// enhanced switch: comma-separated labels are EXPR children of LITERAL_CASE
				for (var expr = child.getFirstChild(); expr != null; expr = expr.getNextSibling()) {
					if (expr.getType() == TokenTypes.EXPR)
						labels.add(extractLabelText(expr));
				}
			}
		}
		return labels;
	}

	@CheckReturnValue
	private static boolean hasDefault(@Nonnull DetailAST caseGroupOrRule) {
		for (var child = caseGroupOrRule.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.LITERAL_DEFAULT)
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isHexLiteral(@Nonnull String text) {
		if (text.length() > 2 && text.charAt(0) == '0')
			return text.charAt(1) == 'x' || text.charAt(1) == 'X';
		if (text.length() > 3 && text.charAt(0) == '-' && text.charAt(1) == '0')
			return text.charAt(2) == 'x' || text.charAt(2) == 'X';
		return false;
	}

	@CheckReturnValue
	private static boolean isNumericLabel(@Nonnull String label) {
		if (label.isEmpty())
			return false;

		final var first = label.charAt(0);
		return first == '-' ? label.length() > 1 && Character.isDigit(label.charAt(1)) : Character.isDigit(first);
	}

	@CheckReturnValue
	private static double parseNumeric(@Nonnull String label) {
		var text = label;

		// strip suffixes (L/f/d), but not from hex literals where A-F are valid digits
		if (!text.isEmpty() && !isHexLiteral(text)) {
			final var last = Character.toLowerCase(text.charAt(text.length() - 1));
			if (last == 'l' || last == 'f' || last == 'd')
				text = text.substring(0, text.length() - 1);
		}
		else if (!text.isEmpty() && isHexLiteral(text)) {
			// hex literals can only have L suffix
			final var last = Character.toLowerCase(text.charAt(text.length() - 1));
			if (last == 'l')
				text = text.substring(0, text.length() - 1);
		}

		// handle binary literals (0b/0B prefix) — Long.decode doesn't support them
		if (text.length() > 2 && text.charAt(0) == '0'
				&& (text.charAt(1) == 'b' || text.charAt(1) == 'B'))
			return Long.parseLong(text.substring(2), 2);
		if (text.length() > 3 && text.charAt(0) == '-' && text.charAt(1) == '0'
				&& (text.charAt(2) == 'b' || text.charAt(2) == 'B'))
			return -Long.parseLong(text.substring(3), 2);

		// handle hex, octal via decode for integer types
		try {
			return Long.decode(text);
		}
		catch (NumberFormatException ignored) {
		}

		return Double.parseDouble(text);
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_SWITCH};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		String prevLabel = null;
		var prevWasDefault = false;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.CASE_GROUP && child.getType() != TokenTypes.SWITCH_RULE)
				continue;

			final var isDefault = hasDefault(child);

			// default not last: if there's a non-default case after default
			if (prevWasDefault && !isDefault) {
				final var currentLabel = getFirstLabel(child);
				if (!currentLabel.isEmpty())
					log(child, MSG_ORDER, "default", currentLabel);
			}

			if (!isDefault) {
				final var currentLabel = getFirstLabel(child);

				// check ordering between cases
				if (prevLabel != null && !currentLabel.isEmpty() && compareLabels(prevLabel, currentLabel) > 0)
					log(child, MSG_ORDER, currentLabel, prevLabel);

				// check internal ordering of comma-separated labels
				final var labels = getLabels(child);
				for (var i = 1; i < labels.size(); ++i) {
					if (compareLabels(labels.get(i - 1), labels.get(i)) > 0)
						log(child, MSG_INTERNAL_ORDER, labels.get(i), labels.get(i - 1));
				}

				prevLabel = currentLabel.isEmpty() ? prevLabel : currentLabel;
			}

			prevWasDefault = isDefault;
		}
	}
}