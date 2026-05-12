package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Enforces record-declaration formatting.
 * <p>
 * Components must follow one of two layouts:
 * <ul>
 *   <li>Style A (single-line): all components on the same line as the {@code record} keyword.</li>
 *   <li>Style B (multi-line): each component on its own line, with no component sharing the
 *       opening-paren line or the closing-paren line.</li>
 * </ul>
 * Any other arrangement is a violation.
 * <p>
 * Braces: the opening brace must be on the same line as the anchor (closing paren, or last line
 * of an implements clause), with exactly one space between. Empty body keeps both braces on the
 * anchor line; non-empty body splits the closing brace to its own line.
 */
public class RecordFormattingCheck extends AbstractCheck {
	private static final String MSG_BRACES_EMPTY_BODY_SPLIT = "record.formatting.empty.body.braces.split";
	private static final String MSG_BRACES_NON_EMPTY_BODY_SAME_LINE = "record.formatting.non.empty.body.braces.same.line";
	private static final String MSG_COMPONENT_MULTI_PER_LINE = "record.formatting.component.multiple.per.line";
	private static final String MSG_COMPONENT_ON_CLOSING_PAREN = "record.formatting.component.on.closing.paren";
	private static final String MSG_COMPONENT_ON_OPENING_PAREN = "record.formatting.component.on.opening.paren";
	private static final String MSG_OPEN_BRACE_BAD_SPACING = "record.formatting.open.brace.bad.spacing";
	private static final String MSG_OPEN_BRACE_NOT_ON_ANCHOR_LINE = "record.formatting.open.brace.not.on.anchor.line";

	@CheckReturnValue
	private static boolean isObjBlockEmpty(@Nonnull DetailAST objBlock) {
		var nonBraceChildren = 0;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var t = child.getType();
			if (t != TokenTypes.LCURLY && t != TokenTypes.RCURLY)
				++nonBraceChildren;
		}
		return nonBraceChildren == 0;
	}

	private void checkBraces(@Nonnull DetailAST recordDef, @Nonnull DetailAST rparen, @Nonnull DetailAST objBlock) {
		DetailAST lcurly = null, rcurly = null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.LCURLY)
				lcurly = child;
			else if (child.getType() == TokenTypes.RCURLY)
				rcurly = child;
		}
		if (lcurly == null || rcurly == null)
			return;

		var anchorLine = rparen.getLineNo();
		final var implClause = recordDef.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
		if (implClause != null)
			anchorLine = AstUtil.lastLine(implClause);

		final var lcurlyLine = lcurly.getLineNo();

		if (lcurlyLine != anchorLine)
			log(lcurly, MSG_OPEN_BRACE_NOT_ON_ANCHOR_LINE);
		else {
			final var fileLines = getLines();
			if (lcurlyLine >= 1 && lcurlyLine <= fileLines.length) {
				final var lineText = fileLines[lcurlyLine - 1];
				final var lcurlyCol = lcurly.getColumnNo();
				if (lineText != null && lcurlyCol >= 1 && lcurlyCol < lineText.length()) {
					final var charBefore = lineText.charAt(lcurlyCol - 1);
					final var twoBefore = lcurlyCol >= 2 ? lineText.charAt(lcurlyCol - 2) : 0;
					if (charBefore != ' ' || (lcurlyCol >= 2 && Character.isWhitespace(twoBefore)))
						log(lcurly, MSG_OPEN_BRACE_BAD_SPACING);
				}
			}
		}

		final var emptyBody = isObjBlockEmpty(objBlock);
		if (emptyBody) {
			if (lcurly.getLineNo() != rcurly.getLineNo())
				log(rcurly, MSG_BRACES_EMPTY_BODY_SPLIT);
		}
		else if (lcurly.getLineNo() == rcurly.getLineNo())
			log(rcurly, MSG_BRACES_NON_EMPTY_BODY_SAME_LINE);
	}

	private void checkComponents(@Nonnull DetailAST lparen, @Nonnull DetailAST rparen, @Nonnull List<DetailAST> components) {
		final var lparenLine = lparen.getLineNo();
		final var rparenLine = rparen.getLineNo();

		// Style A target: parens on one line. Java syntax guarantees every component
		// fits on that line, so no per-component check is needed.
		if (lparenLine == rparenLine)
			return;

		final var firstComp = components.getFirst();
		final var lastComp = components.getLast();

		if (AstUtil.firstLine(firstComp) == lparenLine)
			log(firstComp, MSG_COMPONENT_ON_OPENING_PAREN);

		if (AstUtil.lastLine(lastComp) == rparenLine)
			log(lastComp, MSG_COMPONENT_ON_CLOSING_PAREN);

		for (var i = 1; i < components.size(); ++i) {
			final var prev = components.get(i - 1);
			final var curr = components.get(i);
			if (AstUtil.firstLine(curr) <= AstUtil.lastLine(prev))
				log(curr, MSG_COMPONENT_MULTI_PER_LINE);
		}
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.RECORD_DEF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		final var objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
		if (lparen == null || rparen == null || objBlock == null)
			return;

		final var recordComponents = ast.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		final var components = new ArrayList<DetailAST>();
		if (recordComponents != null) {
			for (var child = recordComponents.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.RECORD_COMPONENT_DEF)
					components.add(child);
			}
		}

		if (!components.isEmpty())
			checkComponents(lparen, rparen, components);

		checkBraces(ast, rparen, objBlock);
	}
}