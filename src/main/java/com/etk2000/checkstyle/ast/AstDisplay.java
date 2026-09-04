package com.etk2000.checkstyle.ast;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/** Renders an AST subtree as the source text a violation message shows. */
public final class AstDisplay {
	/**
	 * Builds human-readable text for an expression AST.
	 * Unlike {@link AstText#exprText} which is designed for equality comparison,
	 * this includes operators, dots, and brackets for display in messages.
	 * Uses an iterative stack to avoid StackOverflowError on deeply nested
	 * expressions.
	 */
	@CheckReturnValue
	@Nonnull
	public static String displayText(@Nonnull DetailAST ast) {
		final var sb = new StringBuilder();
		final var stack = new ArrayDeque<>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var task = stack.pop();
			if (task instanceof String text) {
				sb.append(text);
				continue;
			}
			final var node = (DetailAST) task;
			if (AstText.isCommentToken(node.getType()))
				continue;

			// a cased token can still arrive as a leaf: the STAR of `import java.util.*` is
			// childless, and every binary arm below would push its null operand onto a
			// null-hostile ArrayDeque. It also makes `first` non-null for the rest of the loop.
			final var first = AstText.firstRealChild(node);
			if (first == null) {
				sb.append(node.getText());
				continue;
			}

			// the operands have to be selected past any comment node, not just rendered past one:
			// `a./*c*/b` puts the comment where the DOT's right operand belongs, so a blind
			// getNextSibling renders the comment and drops `b` entirely
			final var second = AstText.nextRealSibling(first);
			switch (node.getType()) {
				case TokenTypes.BAND -> {
					stack.push(second);
					stack.push(" & ");
					stack.push(first);
				}
				case TokenTypes.BNOT -> {
					stack.push(first);
					stack.push("~");
				}
				case TokenTypes.BOR -> {
					stack.push(second);
					stack.push(" | ");
					stack.push(first);
				}
				case TokenTypes.BSR -> {
					stack.push(second);
					stack.push(" >>> ");
					stack.push(first);
				}
				case TokenTypes.BXOR -> {
					stack.push(second);
					stack.push(" ^ ");
					stack.push(first);
				}
				case TokenTypes.DEC -> {
					stack.push(first);
					stack.push("--");
				}
				case TokenTypes.DIV -> {
					stack.push(second);
					stack.push(" / ");
					stack.push(first);
				}
				case TokenTypes.DOT -> {
					stack.push(second);
					stack.push(".");
					stack.push(first);
				}
				case TokenTypes.EQUAL -> {
					stack.push(second);
					stack.push(" == ");
					stack.push(first);
				}
				case TokenTypes.EXPR -> {
					if (second == null)
						stack.push(first);
					else
						sb.append(AstText.exprText(node));
				}
				case TokenTypes.GE -> {
					stack.push(second);
					stack.push(" >= ");
					stack.push(first);
				}
				case TokenTypes.GT -> {
					stack.push(second);
					stack.push(" > ");
					stack.push(first);
				}
				case TokenTypes.INC -> {
					stack.push(first);
					stack.push("++");
				}
				case TokenTypes.INDEX_OP -> {
					stack.push("]");
					stack.push(second);
					stack.push("[");
					stack.push(first);
				}
				case TokenTypes.LAND -> {
					stack.push(second);
					stack.push(" && ");
					stack.push(first);
				}
				case TokenTypes.LE -> {
					stack.push(second);
					stack.push(" <= ");
					stack.push(first);
				}
				case TokenTypes.LNOT -> {
					stack.push(first);
					stack.push("!");
				}
				case TokenTypes.LOR -> {
					stack.push(second);
					stack.push(" || ");
					stack.push(first);
				}
				case TokenTypes.LT -> {
					stack.push(second);
					stack.push(" < ");
					stack.push(first);
				}
				case TokenTypes.METHOD_CALL -> {
					final var elist = node.findFirstToken(TokenTypes.ELIST);
					final var callArgs = new ArrayList<DetailAST>();
					for (var child = elist == null ? null : elist.getFirstChild(); child != null; child = child.getNextSibling()) {
						if (child.getType() == TokenTypes.EXPR)
							callArgs.add(child);
					}
					stack.push(")");
					for (var k = callArgs.size() - 1; k >= 0; --k) {
						stack.push(callArgs.get(k));
						if (k > 0)
							stack.push(", ");
					}
					stack.push("(");
					stack.push(first);
				}
				case TokenTypes.MINUS -> {
					stack.push(second);
					stack.push(" - ");
					stack.push(first);
				}
				case TokenTypes.MOD -> {
					stack.push(second);
					stack.push(" % ");
					stack.push(first);
				}
				case TokenTypes.NOT_EQUAL -> {
					stack.push(second);
					stack.push(" != ");
					stack.push(first);
				}
				case TokenTypes.PLUS -> {
					stack.push(second);
					stack.push(" + ");
					stack.push(first);
				}
				case TokenTypes.POST_DEC -> {
					stack.push("--");
					stack.push(first);
				}
				case TokenTypes.POST_INC -> {
					stack.push("++");
					stack.push(first);
				}
				case TokenTypes.QUESTION -> {
					final var colon = AstText.nextRealSibling(second);
					stack.push(AstText.nextRealSibling(colon));
					stack.push(" : ");
					stack.push(second);
					stack.push(" ? ");
					stack.push(first);
				}
				case TokenTypes.SL -> {
					stack.push(second);
					stack.push(" << ");
					stack.push(first);
				}
				case TokenTypes.SR -> {
					stack.push(second);
					stack.push(" >> ");
					stack.push(first);
				}
				case TokenTypes.STAR -> {
					stack.push(second);
					stack.push(" * ");
					stack.push(first);
				}
				case TokenTypes.TYPECAST -> {
					final var operandParts = new ArrayList<DetailAST>();
					for (var part = AstText.nextRealSibling(node.findFirstToken(TokenTypes.RPAREN)); part != null; part = AstText.nextRealSibling(part))
						operandParts.add(part);
					for (var k = operandParts.size() - 1; k >= 0; --k)
						stack.push(operandParts.get(k));
					stack.push(") ");
					stack.push(AstText.exprText(first));
					stack.push("(");
				}
				case TokenTypes.UNARY_MINUS -> {
					stack.push(first);
					stack.push("-");
				}
				case TokenTypes.UNARY_PLUS -> {
					stack.push(first);
					stack.push("+");
				}
				// A compound node type the switch does not format (e.g. LITERAL_NEW, a method
				// reference, a lambda) falls back to leaf-text concatenation so its operands
				// are rendered instead of dropped. Leaves never reach here.
				default -> sb.append(AstText.exprText(node));
			}
		}
		return sb.toString();
	}

	private AstDisplay() {
	}
}