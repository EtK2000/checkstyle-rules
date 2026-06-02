package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferPrefixIncrementCheck.PostfixSpan;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct AST tests for {@link PreferPrefixIncrementCheck#postfixSpanAt}. The
 * fixer only ever calls it with the operator column, so the operand-column half
 * of its contract, the root climb, and the span invariants the fixer's splice
 * relies on are only reachable from here.
 */
public class PreferPrefixIncrementCheckTest {
	/** Line index (zero-based) of the statement {@link #parse} wraps. */
	private static final int STATEMENT_LINE = 2;

	@Nullable
	private static DetailAST findFirst(@Nullable DetailAST root, int tokenType) {
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
	private static DetailAST parse(@Nonnull String statement) throws Exception {
		final var tmp = File.createTempFile("prefix", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), "class T {\n\tvoid m(int[] arr, int i, T t) {\n\t\t" + statement + "\n\t}\n}\n");
		return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
	}

	@Nonnull
	private static DetailAST postfixNode(@Nonnull DetailAST root) {
		final var node = findFirst(root, TokenTypes.POST_INC);
		return node == null ? findFirst(root, TokenTypes.POST_DEC) : node;
	}

	/**
	 * The fixer joins to the check on the reported column, and accepts it at either
	 * end of the span, so a change to the log site would not otherwise fail a test.
	 */
	@Test
	public void testReportedColumnIsTheOperator() throws Exception {
		final var violations = BaseCheckTest.runCheckInline(
				PreferPrefixIncrementCheck.class,
				"class T {\n\tvoid m() {\n\t\tvar i = 0;\n\t\ti++;\n\t}\n}\n"
		);
		assertEquals(1, violations.size());
		final var violation = violations.getFirst();
		assertEquals(4, violation.getLine());
		assertEquals(SeverityLevel.ERROR, violation.getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violation.getMessage());
		assertEquals(10, violation.getColumn());
	}

	/**
	 * The fixer splices between the two columns without re-validating them, so a
	 * span must always describe a forward, on-line range.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"i++;", "i--;", "arr[i]++;", "t.count++;", "this.count++;", "arr[i++]++;"})
	public void testSpanColumnsAreOrderedAndOnTheLine(@Nonnull String statement) throws Exception {
		final var root = parse(statement);
		final var operator = postfixNode(root);
		final var span = PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, operator.getColumnNo());
		assertNotNull(span, statement);
		assertEquals(
				span.operandColumn(),
				Math.min(span.operandColumn(), span.operatorColumn() - 1),
				"operand must start left of the operator: " + statement
		);
		assertEquals(STATEMENT_LINE, span.operandLine(), statement);
		assertEquals(STATEMENT_LINE, span.operatorLine(), statement);

		final var line = "\t\t" + statement;
		final var codePoints = line.codePointCount(0, line.length());
		assertEquals(
				span.operatorColumn(),
				Math.min(span.operatorColumn(), codePoints - 2),
				"the operator must fit on the line: " + statement
		);
		assertEquals(
				span.operandColumn(),
				Math.max(span.operandColumn(), 0),
				"the operand must sit on the line: " + statement
		);
	}

	@Test
	public void testSpanFoundFromNestedNode() throws Exception {
		final var root = parse("i++;");
		final var operator = postfixNode(root);
		final var expected = PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, operator.getColumnNo());
		assertNotNull(expected);
		assertEquals(expected, PreferPrefixIncrementCheck.postfixSpanAt(operator, STATEMENT_LINE, operator.getColumnNo()));
		// a node past the statement: neither its subtree nor its following siblings
		// hold the postfix, so only the climb to the root can reach it
		final var afterStatement = operator.getParent().getNextSibling();
		assertNotNull(afterStatement);
		assertEquals(expected, PreferPrefixIncrementCheck.postfixSpanAt(afterStatement, STATEMENT_LINE, operator.getColumnNo()));
	}

	@Test
	public void testSpanFoundFromOperandColumn() throws Exception {
		final var root = parse("t.count++;");
		final var operator = postfixNode(root);
		final var byOperator = PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, operator.getColumnNo());
		assertNotNull(byOperator);
		assertEquals(byOperator, PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, byOperator.operandColumn()));
	}

	/**
	 * Settles whether the traversal has to follow root siblings. A buffer with a
	 * package declaration and two top-level classes is the shape that would produce
	 * them, and the postfix sits in the second class, so a lookup scoped to the
	 * first top-level node could not find it.
	 */
	@Test
	public void testSpanFoundInSecondTopLevelClass() throws Exception {
		final var tmp = File.createTempFile("prefix", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), "package p;\n\nclass A {\n}\n\nclass B {\n\tvoid m(int i) {\n\t\ti++;\n\t}\n}\n");
		final var root = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		assertNull(root.getNextSibling(), "parse root carries siblings; every locator must iterate them");
		final var operator = postfixNode(root);
		assertNotNull(PreferPrefixIncrementCheck.postfixSpanAt(root, operator.getLineNo() - 1, operator.getColumnNo()));
	}

	@Test
	public void testSpanNullBetweenOperandAndOperator() throws Exception {
		final var root = parse("t.count++;");
		final var operator = postfixNode(root);
		final var span = PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, operator.getColumnNo());
		assertNotNull(span);
		assertNull(PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, span.operandColumn() + 1));
	}

	@Test
	public void testSpanNullOnAnotherLine() throws Exception {
		final var root = parse("i++;");
		final var operator = postfixNode(root);
		assertNull(PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE + 1, operator.getColumnNo()));
	}

	@Test
	public void testSpanSkipsValueConsumingPostfix() throws Exception {
		final var root = parse("arr[i++]++;");
		final var inner = findFirst(findFirst(root, TokenTypes.INDEX_OP), TokenTypes.POST_INC);
		assertNotNull(inner);
		assertNull(PreferPrefixIncrementCheck.postfixSpanAt(root, STATEMENT_LINE, inner.getColumnNo()));
	}

	@Test
	public void testSpanTracksOperatorKind() throws Exception {
		final var increment = parse("i++;");
		final var decrement = parse("i--;");
		assertEquals(
				new PostfixSpan(STATEMENT_LINE, 2, STATEMENT_LINE, 3, true),
				PreferPrefixIncrementCheck.postfixSpanAt(increment, STATEMENT_LINE, 3)
		);
		assertEquals(
				new PostfixSpan(STATEMENT_LINE, 2, STATEMENT_LINE, 3, false),
				PreferPrefixIncrementCheck.postfixSpanAt(decrement, STATEMENT_LINE, 3)
		);
	}
}