package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.gradle.fix.ControlFlowBracesFixer;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags an opening brace written on a later line than its header, for control statements and
 * declarations alike: the brace below {@code if (x)} moves up to end that line.
 *
 * <p>A record's own brace is left to {@link RecordFormattingCheck}. A brace with no header (an
 * instance initializer, a bare block, an array initializer, a colon {@code case} block, an enum
 * constant body) is never reported.
 *
 * <p>A single-statement body whose braces {@link ControlFlowBracesCheck} reports as unnecessary is
 * left to that check, which removes them. Only when its fixer refuses to is the brace reported here
 * too, since pulling it up is then the only fix left.
 */
public class BraceOwnLineCheck extends AbstractAstCheck {
	/**
	 * A brace and the header it opens.
	 *
	 * @param owner          the statement or declaration the brace belongs to
	 * @param report         the token the violation is reported at
	 * @param brace          the opening brace
	 * @param close          the matching closing brace
	 * @param headerEndLine  one-based line of the header's last token
	 */
	public record BraceSite(
			@Nonnull DetailAST owner,
			@Nonnull DetailAST report,
			@Nonnull DetailAST brace,
			@Nonnull DetailAST close,
			int headerEndLine
	) {
		@CheckReturnValue
		public boolean ownLine() {
			return brace.getLineNo() > headerEndLine;
		}
	}

	private static final int[] OWNER_TOKENS = {
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.COMPACT_CTOR_DEF,
			TokenTypes.CTOR_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.LABELED_STAT,
			TokenTypes.LAMBDA,
			TokenTypes.LITERAL_CATCH,
			TokenTypes.LITERAL_DO,
			TokenTypes.LITERAL_ELSE,
			TokenTypes.LITERAL_FINALLY,
			TokenTypes.LITERAL_FOR,
			TokenTypes.LITERAL_IF,
			TokenTypes.LITERAL_NEW,
			TokenTypes.LITERAL_SWITCH,
			TokenTypes.LITERAL_SYNCHRONIZED,
			TokenTypes.LITERAL_TRY,
			TokenTypes.LITERAL_WHILE,
			TokenTypes.METHOD_DEF,
			TokenTypes.STATIC_INIT,
			TokenTypes.SWITCH_RULE
	};
	private static final String MSG_BRACE_OWN_LINE = "brace.own.line";

	/**
	 * The opening brace {@code owner} itself opens, or {@code null} when it has none: a braceless
	 * body, an abstract method, an anonymous-class-free {@code new}, a {@code synchronized}
	 * modifier, an {@code else} whose body is an {@code if}, or an expression switch rule.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST braceOf(@Nonnull DetailAST owner) {
		return switch (owner.getType()) {
			case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF,
			     TokenTypes.LITERAL_NEW -> {
				final var objBlock = owner.findFirstToken(TokenTypes.OBJBLOCK);
				yield objBlock == null ? null : objBlock.findFirstToken(TokenTypes.LCURLY);
			}
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE -> {
				final var body = owner.getFirstChild();
				yield body != null && body.getType() == TokenTypes.SLIST ? body : null;
			}
			case TokenTypes.LITERAL_SWITCH -> owner.findFirstToken(TokenTypes.LCURLY);
			default -> owner.findFirstToken(TokenTypes.SLIST);
		};
	}

	/**
	 * The last token of {@code owner}'s header ahead of {@code body}, its brace or its braceless
	 * statement: the latest-positioned node in the owner's subtree that starts before the body. A
	 * child starting at or past the body holds the body or what follows it (an {@code else}, a
	 * {@code catch}), so its subtree is skipped.
	 */
	@CheckReturnValue
	@Nonnull
	public static DetailAST headerEnd(@Nonnull DetailAST owner, @Nonnull DetailAST body) {
		var last = owner;
		final var stack = new ArrayDeque<DetailAST>();
		for (var child = owner.getFirstChild(); child != null; child = child.getNextSibling())
			stack.push(child);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (!isBefore(node, body))
				continue;
			if (isBefore(last, node))
				last = node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return last;
	}

	@CheckReturnValue
	private static boolean isBefore(@Nonnull DetailAST a, @Nonnull DetailAST b) {
		return a.getLineNo() < b.getLineNo() || a.getLineNo() == b.getLineNo() && a.getColumnNo() < b.getColumnNo();
	}

	@CheckReturnValue
	private static boolean isOwnerType(int type) {
		return Arrays.stream(OWNER_TOKENS).anyMatch(t -> t == type);
	}

	/**
	 * The token a violation on {@code owner} is reported at: a declaration's name, a label, a
	 * switch rule's {@code case} / {@code default}, or else the owner's own keyword.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST reportOf(@Nonnull DetailAST owner) {
		return switch (owner.getType()) {
			case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.COMPACT_CTOR_DEF, TokenTypes.CTOR_DEF,
			     TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.LABELED_STAT, TokenTypes.METHOD_DEF ->
					owner.findFirstToken(TokenTypes.IDENT);
			case TokenTypes.SWITCH_RULE -> owner.getFirstChild();
			default -> owner;
		};
	}

	/**
	 * The brace site whose violation is reported at {@code (line, column)}, zero-based line and
	 * code-point column, or {@code null} when no owner reports there.
	 */
	@CheckReturnValue
	@Nullable
	public static BraceSite siteAt(@Nonnull DetailAST root, int line, int column) {
		final var report = AstQuery.findNodeAt(root, line, column, node -> siteReportedAt(node) != null);
		return report == null ? null : siteReportedAt(report);
	}

	/**
	 * The brace {@code owner} opens together with its header, or {@code null} when it opens none.
	 */
	@CheckReturnValue
	@Nullable
	public static BraceSite siteOf(@Nonnull DetailAST owner) {
		final var brace = braceOf(owner);
		if (brace == null)
			return null;
		final var close = brace.getType() == TokenTypes.SLIST ? brace.getLastChild() : brace.getParent().getLastChild();
		return new BraceSite(owner, reportOf(owner), brace, close, headerEnd(owner, brace).getLineNo());
	}

	@CheckReturnValue
	@Nullable
	private static BraceSite siteReportedAt(@Nonnull DetailAST node) {
		if (isOwnerType(node.getType()) && reportOf(node) == node)
			return siteOf(node);
		final var parent = node.getParent();
		return parent != null && isOwnerType(parent.getType()) && reportOf(parent) == node ? siteOf(parent) : null;
	}

	@Nullable
	private List<String> lines;

	@Override
	public void beginTree(@Nonnull DetailAST rootAst) {
		// finishTree is skipped when a check throws mid-file, so the previous file's buffer can still be here
		lines = null;
	}

	@Override
	public void finishTree(@Nonnull DetailAST rootAst) {
		lines = null;
		// the fixer's caches live on this thread, which a build or an IDE keeps pooled long after
		// the file is done
		ControlFlowBracesFixer.releaseCache();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return OWNER_TOKENS.clone();
	}

	/**
	 * Whether {@link ControlFlowBracesCheck} reports the block's braces as unnecessary and its fixer
	 * removes them, which takes the own-line brace away with them.
	 */
	@CheckReturnValue
	private boolean unwrapRemovesBrace(@Nonnull BraceSite site) {
		return switch (site.owner().getType()) {
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF,
			     TokenTypes.LITERAL_WHILE -> {
				if (!ControlFlowBracesCheck.hasUnnecessaryBraces(site.brace()))
					yield false;
				// one unmodifiable buffer per file lets the fixer's caches recognize it without
				// comparing every line again at every site
				if (lines == null)
					lines = List.of(getLines());
				yield ControlFlowBracesFixer.unwrapsBraces(lines, site.owner());
			}
			default -> false;
		};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var site = siteOf(ast);
		if (site != null && site.ownLine() && !unwrapRemovesBrace(site))
			log(site.report(), MSG_BRACE_OWN_LINE);
	}
}