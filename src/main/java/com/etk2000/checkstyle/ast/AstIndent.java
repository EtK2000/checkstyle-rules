package com.etk2000.checkstyle.ast;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Answers the project's canonical indentation for a position in the tree, so a fixer emitting a
 * replacement line can place it where the style guide says it belongs rather than inheriting
 * whatever indentation the source happened to carry. It reads no line text and no columns, so a
 * misindented, space-indented or CRLF buffer gives the same answer as a canonical one.
 *
 * <p>{@link #tabsFor} answers the indent of the line a <em>statement</em> at this tree position
 * would start on. A node interior to a statement therefore answers that statement's indent, never
 * the continuation indent of the physical line it currently sits on; continuation layout belongs to
 * {@code JavaSpanReindenter}, which this pairs with by supplying its {@code baseTabs}.
 *
 * <p>The rule, measured against canonically formatted source rather than assumed: an ancestor adds
 * a level when it is an {@code OBJBLOCK}, an {@code SLIST}, a {@code switch} reached through one of
 * its cases, or a control-flow keyword whose <em>body</em> is the path back down and is not itself
 * an {@code SLIST}. The last qualifier is what keeps a braced body from counting twice, since the
 * brace already carries the level. Two constructs share a level with their parent and so add
 * nothing: an {@code else} whose body is an {@code if} (an {@code else if} chain stays flat however
 * long it grows), and the block opening a braced {@code case} (its brace sits on the label's line).
 */
public final class AstIndent {
	/** The child that is {@code parent}'s body, as opposed to its condition, resources or terminator. */
	@CheckReturnValue
	@Nullable
	private static DetailAST bodyOf(@Nonnull DetailAST parent) {
		return switch (parent.getType()) {
			case TokenTypes.LITERAL_CATCH, TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF,
					TokenTypes.LITERAL_SYNCHRONIZED, TokenTypes.LITERAL_WHILE -> {
				final var rparen = parent.findFirstToken(TokenTypes.RPAREN);
				yield rparen == null ? null : rparen.getNextSibling();
			}
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FINALLY -> parent.getFirstChild();
			case TokenTypes.LITERAL_TRY -> parent.findFirstToken(TokenTypes.SLIST);
			default -> null;
		};
	}

	@CheckReturnValue
	private static int contribution(@Nonnull DetailAST parent, @Nonnull DetailAST child) {
		// a block's closing brace aligns with the line that opened it, so the block it closes
		// contributes nothing to its own closer
		if (child.getType() == TokenTypes.RCURLY)
			return 0;
		return switch (parent.getType()) {
			case TokenTypes.LITERAL_ELSE -> child == parent.getFirstChild()
					&& child.getType() != TokenTypes.LITERAL_IF
					&& child.getType() != TokenTypes.SLIST ? 1 : 0;
			case TokenTypes.LITERAL_SWITCH -> child.getType() == TokenTypes.CASE_GROUP
					|| child.getType() == TokenTypes.SWITCH_RULE ? 1 : 0;
			case TokenTypes.OBJBLOCK -> 1;
			case TokenTypes.SLIST -> opensABracedCase(parent, child) ? 0 : 1;
			default -> child == bodyOf(parent) && child.getType() != TokenTypes.SLIST ? 1 : 0;
		};
	}

	/**
	 * Whether {@code child} is the block a braced {@code case} opens on its label's line, so that it
	 * shares the label's level rather than the case body's. Being the group's <em>first</em>
	 * statement is what decides it: a block appearing later in the same case body is an ordinary
	 * nested block, and statements written after the braced block still sit at the body's level.
	 */
	@CheckReturnValue
	private static boolean opensABracedCase(@Nonnull DetailAST group, @Nonnull DetailAST child) {
		return child.getType() == TokenTypes.SLIST
				&& group.getParent() != null
				&& group.getParent().getType() == TokenTypes.CASE_GROUP
				&& group.getFirstChild() == child;
	}

	/**
	 * The number of tabs a statement at {@code node}'s position is canonically written with. Walks
	 * iteratively, so a pathologically deep tree costs memory rather than the stack.
	 */
	@CheckReturnValue
	public static int tabsFor(@Nonnull DetailAST node) {
		var tabs = 0;
		for (var child = node; child.getParent() != null; child = child.getParent())
			tabs += contribution(child.getParent(), child);
		return tabs;
	}

	private AstIndent() {
	}
}