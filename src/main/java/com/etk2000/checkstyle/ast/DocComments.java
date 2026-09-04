package com.etk2000.checkstyle.ast;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * javac's doc comment rules, for every check and fixer that must not move or spread one. A doc
 * comment documents the declaration whose first token is the next token after it, giving its text,
 * and a {@code @deprecated} tag, to every name that declaration declares. With anything else next,
 * an enum's terminating {@code ;} included, it documents nothing.
 *
 * <p>{@code tokens} holds the {@link #position} of every source token of a file and
 * {@code docComments} the start position of every doc comment in it.
 */
public record DocComments(@Nonnull NavigableSet<Long> tokens, @Nonnull NavigableSet<Long> docComments) {
	private static final Set<Integer> DECLARATIONS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.ANNOTATION_FIELD_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.COMPACT_CTOR_DEF,
			TokenTypes.CTOR_DEF,
			TokenTypes.ENUM_CONSTANT_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.METHOD_DEF,
			TokenTypes.PACKAGE_DEF,
			TokenTypes.RECORD_COMPONENT_DEF,
			TokenTypes.RECORD_DEF,
			TokenTypes.VARIABLE_DEF
	);

	/** The position of the first token of every declaration under {@code root}. */
	@CheckReturnValue
	@Nonnull
	public static Set<Long> declarationStarts(@Nonnull DetailAST root) {
		final var starts = new TreeSet<Long>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (DECLARATIONS.contains(node.getType()))
				starts.add(firstToken(node));
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return starts;
	}

	/** The position of the earliest source token in {@code node}'s subtree. */
	@CheckReturnValue
	public static long firstToken(@Nonnull DetailAST node) {
		var first = Long.MAX_VALUE;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (isSourceToken(current))
				first = Math.min(first, position(current.getLineNo(), current.getColumnNo()));
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return first;
	}

	/**
	 * True for a doc comment's text, delimiters included: a block comment opening with
	 * {@code /**}, though not the empty {@code /**}{@code /}, or a {@code ///} line comment.
	 */
	@CheckReturnValue
	public static boolean isDocComment(@Nonnull String text) {
		return text.startsWith("/**") && !text.startsWith("/**/") || text.startsWith("///");
	}

	@CheckReturnValue
	private static boolean isSourceToken(@Nonnull DetailAST node) {
		return !AstText.isImaginaryToken(node) && !AstText.isCommentToken(node.getType());
	}

	/** The 1-based line of a {@link #position}. */
	@CheckReturnValue
	public static int line(long position) {
		return (int) (position >>> 32);
	}

	/** A 1-based line and a 0-based column packed so positions sort in source order. */
	@CheckReturnValue
	public static long position(int line, int column) {
		return (long) line << 32 | column;
	}

	/** The position of every source token under {@code root}. */
	@CheckReturnValue
	@Nonnull
	public static NavigableSet<Long> tokenPositions(@Nonnull DetailAST root) {
		final var positions = new TreeSet<Long>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (isSourceToken(node))
				positions.add(position(node.getLineNo(), node.getColumnNo()));
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return positions;
	}

	/** The position of the token a doc comment starting at {@code commentStart} would document, or null past the last token. */
	@CheckReturnValue
	@Nullable
	public Long documentedToken(long commentStart) {
		return tokens.higher(commentStart);
	}

	/** True when a doc comment documents the declaration {@code declaration} opens. */
	@CheckReturnValue
	public boolean isDocumented(@Nonnull DetailAST declaration) {
		final var first = firstToken(declaration);
		final var previous = tokens.lower(first);
		return !docComments.subSet(previous == null ? Long.MIN_VALUE : previous, false, first, false).isEmpty();
	}
}