package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Direct-AST tests for {@link MultilineCallFormattingCheck}'s fixer entry point.
 */
public class MultilineCallFormattingCheckTest {
	private static DetailAST findFirst(@Nonnull DetailAST root, int tokenType) {
		for (var node = root; node != null; node = node.getNextSibling()) {
			if (node.getType() == tokenType)
				return node;
			final var child = findFirst(node.getFirstChild(), tokenType);
			if (child != null)
				return child;
		}
		return null;
	}

	@Nonnull
	private static List<String> lines(@Nonnull String source) {
		return List.of(source.split("\n", -1));
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("mcf", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	/**
	 * A split {@code new JSONObject().put(...)} is a collapse ({@code analyzeLayout} classifies it
	 * {@code multiline.put.collapsible}, not a closing move), so
	 * {@link MultilineCallFormattingCheck#closingParenMove} at its {@code )} returns null.
	 */
	@Test
	public void closingParenMoveReturnsNullForSplitCollapsiblePut() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tnew JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = putCall.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallFormattingCheck.closingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
	}

	/**
	 * The key of an inline-block {@code put} is a text block whose interior contains {@code //}; the
	 * feasibility scan threads the lexer state, so that {@code //} is masked (text-block content, not a
	 * real trailing comment) and the opening pull-up stays feasible: {@code openingParenMove} returns a
	 * (non-null) move rather than the check wrongly suppressing the violation.
	 */
	@Test
	public void openingParenMoveFiresForTextBlockKeyWithInteriorSlashes() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"\"\"\n\t\t\t\ta // b\n\t\t\t\t\"\"\",\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"x\", 1)\n\t\t\t\t\t\t.put(\"y\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNotNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	/**
	 * A split {@code new JSONObject().put(...)} is a collapse ({@code analyzeLayout} classifies it
	 * {@code multiline.put.collapsible}, not an opening move), so
	 * {@link MultilineCallFormattingCheck#openingParenMove} at its {@code (} returns null.
	 */
	@Test
	public void openingParenMoveReturnsNullForSplitCollapsiblePut() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tnew JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	/**
	 * A multi-line-value {@code put} whose key line ends in a {@code //} line comment that follows an
	 * inline block comment still cannot pull the key onto the {@code (} line, so the check suppresses the
	 * opening violation and {@link MultilineCallFormattingCheck#openingParenMove} returns null.
	 */
	@Test
	public void openingParenMoveReturnsNullWhenBlockCommentThenLineCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", /* c */ // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	/**
	 * A multi-line-value {@code put} whose key line carries a {@code //} comment cannot pull the key onto
	 * the {@code (} line (joining the head would swallow the comment), so the check suppresses the opening
	 * violation and {@link MultilineCallFormattingCheck#openingParenMove} returns null.
	 */
	@Test
	public void openingParenMoveReturnsNullWhenCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	/**
	 * A single-line-value {@code put} whose collapsed one-line form would exceed the max width cannot pull
	 * the key onto the {@code (} line, so the check suppresses the opening violation and
	 * {@link MultilineCallFormattingCheck#openingParenMove} returns null.
	 */
	@Test
	public void openingParenMoveReturnsNullWhenLengthInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"aKeyNameLongEnoughToPushTheCollapsedFormPastOneHundredTwentyColumnsWithRoom\",\n\t\t\t\tnew JSONObject().put(\"k\", 1)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	/**
	 * A plain call whose first argument is on the {@code (} line AND whose last arguments are on the
	 * {@code )} line has both an opening and a closing (and shared-line) violation. A plain push-down
	 * resolves only the opening one, so {@link MultilineCallFormattingCheck#openingParenMove} must
	 * return null rather than emit a move that leaves the pipeline non-convergent.
	 */
	@Test
	public void openingParenMoveReturnsNullWhenOpeningNotSoleViolation() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2, 3);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), call.getLineNo() - 1, call.getColumnNo()));
	}

	/**
	 * The load-bearing check/fixer consistency case: an infeasible-opening {@code put} (a {@code //}
	 * comment on the key line) whose value also ends on the {@code )} line. The check suppresses the
	 * opening violation but still emits the closing one, so {@link MultilineCallFormattingCheck#openingParenMove}
	 * returns null while {@link MultilineCallFormattingCheck#closingParenMove} returns a push-down move
	 * (not a pull-up): the fixer moves the {@code )} down and leaves the key where it is.
	 */
	@Test
	public void openingSuppressedClosingResolvableWhenCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2));\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = putCall.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallFormattingCheck.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
		final var move = MultilineCallFormattingCheck.closingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo());
		assertNotNull(move);
		assertFalse(move.pullUp());
	}
}