package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

public class MultilineCallMovesTest {
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
		final var sourceFile = File.createTempFile("mcf", ".java");
		try {
			Files.writeString(sourceFile.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(sourceFile, StandardCharsets.UTF_8.name())));
		}
		finally {
			sourceFile.delete();
		}
	}

	@Test
	public void argListResolversReturnNullForTextBlockOnParenLines() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(\"\"\"\n\t\t\t\ttext\"\"\");\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		for (var coordinate : List.of(new int[]{call.getLineNo() - 1, call.getColumnNo()}, new int[]{rparen.getLineNo() - 1, rparen.getColumnNo()})) {
			assertNull(MultilineCallMoves.closingParenMove(root, lines(source), coordinate[0], coordinate[1]));
			assertNull(MultilineCallMoves.openingParenMove(root, lines(source), coordinate[0], coordinate[1]));
			assertNull(MultilineCallMoves.resolvableArgListOwner(root, lines(source), coordinate[0], coordinate[1]));
			assertNull(MultilineCallMoves.resolvableSharedLineArgs(root, lines(source), coordinate[0], coordinate[1]));
		}
	}

	@Test
	public void closingAndOpeningMovesReturnNullForTheOpenClosePair() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallMoves.closingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), call.getLineNo() - 1, call.getColumnNo()));
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
	}

	@Test
	public void closingParenMoveReturnsNullForSplitCollapsiblePut() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tnew JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = putCall.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallMoves.closingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
	}

	@Test
	public void openingParenMoveFiresForTextBlockKeyWithInteriorSlashes() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"\"\"\n\t\t\t\ta // b\n\t\t\t\t\"\"\",\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"x\", 1)\n\t\t\t\t\t\t.put(\"y\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNotNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	@Test
	public void openingParenMoveReturnsNullForSplitCollapsiblePut() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tnew JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	@Test
	public void openingParenMoveReturnsNullWhenBlockCommentThenLineCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", /* c */ // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	@Test
	public void openingParenMoveReturnsNullWhenCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	@Test
	public void openingParenMoveReturnsNullWhenLengthInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"aKeyNameLongEnoughToPushTheCollapsedFormPastOneHundredTwentyColumnsWithRoom\",\n\t\t\t\tnew JSONObject().put(\"k\", 1)\n\t\t);\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
	}

	@Test
	public void openingParenMoveReturnsNullWhenOpeningNotSoleViolation() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2, 3);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), call.getLineNo() - 1, call.getColumnNo()));
	}

	@Test
	public void openingSuppressedClosingResolvableWhenCommentInfeasible() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tcache.put(\n\t\t\t\t\"k\", // note\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"a\", 1)\n\t\t\t\t\t\t.put(\"b\", 2));\n\t}\n}\n";
		final var root = parse(source);
		final var putCall = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = putCall.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallMoves.openingParenMove(root, lines(source), putCall.getLineNo() - 1, putCall.getColumnNo()));
		final var move = MultilineCallMoves.closingParenMove(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo());
		assertNotNull(move);
		assertFalse(move.pullUp());
	}

	@Test
	public void resolvableArgListOwnerReturnsNullWhenASharedLineAccompaniesThePair() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2, 3);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		assertNull(MultilineCallMoves.resolvableArgListOwner(root, lines(source), call.getLineNo() - 1, call.getColumnNo()));
		assertNull(MultilineCallMoves.resolvableArgListOwner(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
	}

	@Test
	public void resolvableArgListOwnerReturnsTheOwnerForTheOpenClosePairAtBothCoordinates() throws Exception {
		final var source = "class T {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}\n";
		final var root = parse(source);
		final var call = findFirst(root, TokenTypes.METHOD_CALL);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		assertSame(call, MultilineCallMoves.resolvableArgListOwner(root, lines(source), call.getLineNo() - 1, call.getColumnNo()));
		assertSame(call, MultilineCallMoves.resolvableArgListOwner(root, lines(source), rparen.getLineNo() - 1, rparen.getColumnNo()));
	}
}