package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Direct-AST tests for which statement {@link JsonObjectPutCollapse} treats as the one to join.
 * These cannot be fixture slices: a braceless control-flow body that spans lines is itself a
 * {@code ControlFlowBracesCheck} violation, so a slice would force a topic-wide suppression of that
 * check and mask real brace problems in the other slices.
 */
public class JsonObjectPutCollapseTest {
	private static final String ANNOTATED_ENUM_CONSTANT =
			"enum E {\n\t@Deprecated\n\tA(new JSONObject()\n\t\t\t.put(\"k\", 1));\n}\n";
	private static final String ANNOTATED_FIELD =
			"class T {\n\t@Deprecated\n\tObject payload = new JSONObject()\n\t\t\t.put(\"k\", 1);\n}\n";
	private static final String ARRAY_INITIALIZER =
			"class T {\n\tvoid m(Object other) {\n\t\tObject[] a = {\n\t\t\t\tnew JSONObject()\n\t\t\t\t\t\t.put(\"k\", 1),\n\t\t\t\tother\n\t\t};\n\t}\n}\n";
	private static final String ARROW_SWITCH_EXPRESSION =
			"class T {\n\tvoid m(int k) {\n\t\tpayload = switch (k) {\n\t\t\tcase 1 -> new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t\t\tdefault -> null;\n\t\t};\n\t}\n}\n";
	private static final String ARROW_SWITCH_STATEMENT =
			"class T {\n\tvoid m(int k) {\n\t\tswitch (k) {\n\t\t\tcase 1 -> payload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t\t\tdefault -> payload = null;\n\t\t}\n\t}\n}\n";
	private static final String BRACED_IF =
			"class T {\n\tvoid m(boolean flag) {\n\t\tif (flag) {\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t\t}\n\t}\n}\n";
	private static final String BRACELESS_DO_WHILE =
			"class T {\n\tvoid m(boolean flag) {\n\t\tdo\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t\twhile (flag);\n\t}\n}\n";
	private static final String BRACELESS_ELSE =
			"class T {\n\tvoid m(boolean flag) {\n\t\tif (flag)\n\t\t\tpayload = null;\n\t\telse\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
	private static final String BRACELESS_FOR =
			"class T {\n\tvoid m() {\n\t\tfor (int i = 0; i < 2; ++i)\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", i);\n\t}\n}\n";
	private static final String BRACELESS_IF =
			"class T {\n\tvoid m(boolean flag) {\n\t\tif (flag)\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
	private static final String BRACELESS_IF_ELSE =
			"class T {\n\tvoid m(boolean flag) {\n\t\tif (flag)\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t\telse\n\t\t\tpayload = null;\n\t}\n}\n";
	private static final String BRACELESS_WHILE =
			"class T {\n\tvoid m(boolean flag) {\n\t\twhile (flag)\n\t\t\tpayload = new JSONObject()\n\t\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
	private static final String COLON_SWITCH =
			"class T {\n\tvoid m(int k) {\n\t\tswitch (k) {\n\t\t\tcase 1:\n\t\t\t\tpayload = new JSONObject()\n\t\t\t\t\t\t.put(\"k\", 1);\n\t\t\t\tbreak;\n\t\t}\n\t}\n}\n";
	private static final String ENUM_CONSTANT =
			"enum E {\n\tA(new JSONObject()\n\t\t\t.put(\"k\", 1));\n}\n";
	private static final String FIELD_INITIALIZER =
			"class T {\n\tObject payload = new JSONObject()\n\t\t\t.put(\"k\", 1);\n}\n";
	private static final String FOR_CONDITION =
			"class T {\n\tvoid m() {\n\t\tfor (; new JSONObject()\n\t\t\t\t.put(\"k\", 1) != null; )\n\t\t\tbreak;\n\t}\n}\n";
	private static final String FOR_EACH_ITERABLE =
			"class T {\n\tvoid m() {\n\t\tfor (Object o : new JSONObject()\n\t\t\t\t.put(\"k\", 1))\n\t\t\tbreak;\n\t}\n}\n";
	private static final String FOR_HEADER =
			"class T {\n\tvoid m() {\n\t\tfor (JSONObject o = new JSONObject()\n\t\t\t\t.put(\"k\", 1); o != null; )\n\t\t\tbreak;\n\t}\n}\n";
	private static final String FOR_ITERATOR =
			"class T {\n\tvoid m(boolean flag) {\n\t\tfor (; flag; payload = new JSONObject()\n\t\t\t\t.put(\"k\", 1))\n\t\t\tbreak;\n\t}\n}\n";
	private static final String INLINE_ANNOTATED_FIELD =
			"class T {\n\t@Deprecated Object payload = new JSONObject()\n\t\t\t.put(\"k\", 1);\n}\n";
	private static final String LABELED_STATEMENT =
			"class T {\n\tvoid m() {\n\t\tlabel:\n\t\tpayload = new JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
	private static final String RETURN_STATEMENT =
			"class T {\n\tObject m() {\n\t\treturn new JSONObject()\n\t\t\t\t.put(\"k\", 1);\n\t}\n}\n";
	private static final String SYNCHRONIZED_LOCK =
			"class T {\n\tvoid m() {\n\t\tsynchronized (new JSONObject()\n\t\t\t\t.put(\"k\", 1)) {\n\t\t\tg();\n\t\t}\n\t}\n}\n";
	private static final String TERNARY_ARM =
			"class T {\n\tvoid m(boolean c) {\n\t\tpayload = c ? new JSONObject()\n\t\t\t\t.put(\"k\", 1) : null;\n\t}\n}\n";
	private static final String TEXT_BLOCK_OPENS_ON_LAST_LINE =
			"class T {\n\tvoid m() {\n\t\tObject o = new JSONObject()\n\t\t\t\t.put(\"k\", 1); String s = \"\"\"\n\t\t\t\tx\"\"\";\n\t}\n}\n";
	private static final String TRY_RESOURCE =
			"class T {\n\tvoid m() {\n\t\ttry (Object o = new JSONObject()\n\t\t\t\t.put(\"k\", 1)) {\n\t\t\tg();\n\t\t}\n\t}\n}\n";

	private static DetailAST parse(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("jopc", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	private static DetailAST putCall(@Nonnull DetailAST root) {
		for (var node = root; node != null; node = node.getNextSibling()) {
			if (node.getType() == TokenTypes.METHOD_CALL)
				return node;
			final var child = putCall(node.getFirstChild());
			if (child != null)
				return child;
		}
		return null;
	}

	private static int[] spanOf(@Nonnull String source) throws Exception {
		final var root = parse(source);
		final var put = putCall(root);
		return JsonObjectPutCollapse.lineSpan(root, List.of(source.split("\n", -1)), put.getLineNo() - 1, put.getColumnNo());
	}

	private static boolean violates(@Nonnull String source) throws Exception {
		return JsonObjectPutCollapse.isViolation(putCall(parse(source)), source.split("\n", -1));
	}

	@Test
	public void annotatedEnumConstantExcludesTheAnnotationLine() throws Exception {
		assertTrue(violates(ANNOTATED_ENUM_CONSTANT));
		assertArrayEquals(new int[]{2, 3}, spanOf(ANNOTATED_ENUM_CONSTANT));
	}

	@Test
	public void annotatedFieldExcludesTheAnnotationLine() throws Exception {
		assertTrue(violates(ANNOTATED_FIELD));
		assertArrayEquals(new int[]{2, 3}, spanOf(ANNOTATED_FIELD));
	}

	@Test
	public void arrayInitializerJoinsOnlyTheElement() throws Exception {
		assertTrue(violates(ARRAY_INITIALIZER));
		assertArrayEquals(new int[]{3, 4}, spanOf(ARRAY_INITIALIZER));
	}

	@Test
	public void arrowSwitchExpressionJoinsOnlyTheCaseBody() throws Exception {
		assertTrue(violates(ARROW_SWITCH_EXPRESSION));
		assertArrayEquals(new int[]{3, 4}, spanOf(ARROW_SWITCH_EXPRESSION));
	}

	@Test
	public void arrowSwitchStatementJoinsOnlyTheCaseBody() throws Exception {
		assertTrue(violates(ARROW_SWITCH_STATEMENT));
		assertArrayEquals(new int[]{3, 4}, spanOf(ARROW_SWITCH_STATEMENT));
	}

	@Test
	public void bracedIfJoinsOnlyTheBodyStatement() throws Exception {
		assertTrue(violates(BRACED_IF));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACED_IF));
	}

	@Test
	public void bracelessDoWhileJoinsBodyWithoutTheWhile() throws Exception {
		assertTrue(violates(BRACELESS_DO_WHILE));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACELESS_DO_WHILE));
	}

	@Test
	public void bracelessElseJoinsBodyWithoutTheIf() throws Exception {
		assertTrue(violates(BRACELESS_ELSE));
		assertArrayEquals(new int[]{5, 6}, spanOf(BRACELESS_ELSE));
	}

	@Test
	public void bracelessForJoinsBodyWithoutTheHeader() throws Exception {
		assertTrue(violates(BRACELESS_FOR));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACELESS_FOR));
	}

	@Test
	public void bracelessIfElseJoinsBodyWithoutTheElseBranch() throws Exception {
		assertTrue(violates(BRACELESS_IF_ELSE));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACELESS_IF_ELSE));
	}

	@Test
	public void bracelessIfJoinsBodyWithoutTheIf() throws Exception {
		assertTrue(violates(BRACELESS_IF));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACELESS_IF));
	}

	@Test
	public void bracelessWhileJoinsBodyWithoutTheWhile() throws Exception {
		assertTrue(violates(BRACELESS_WHILE));
		assertArrayEquals(new int[]{3, 4}, spanOf(BRACELESS_WHILE));
	}

	@Test
	public void colonSwitchJoinsOnlyTheCaseBody() throws Exception {
		assertTrue(violates(COLON_SWITCH));
		assertArrayEquals(new int[]{4, 5}, spanOf(COLON_SWITCH));
	}

	@Test
	public void enumConstantJoinsTheConstant() throws Exception {
		assertTrue(violates(ENUM_CONSTANT));
		assertArrayEquals(new int[]{1, 2}, spanOf(ENUM_CONSTANT));
	}

	@Test
	public void fieldInitializerJoinsTheDeclaration() throws Exception {
		assertTrue(violates(FIELD_INITIALIZER));
		assertArrayEquals(new int[]{1, 2}, spanOf(FIELD_INITIALIZER));
	}

	@Test
	public void forConditionRefusesCollapse() throws Exception {
		assertFalse(violates(FOR_CONDITION));
		assertNull(spanOf(FOR_CONDITION));
	}

	@Test
	public void forEachIterableRefusesCollapse() throws Exception {
		assertFalse(violates(FOR_EACH_ITERABLE));
		assertNull(spanOf(FOR_EACH_ITERABLE));
	}

	@Test
	public void forHeaderIsNotAViolation() throws Exception {
		assertFalse(violates(FOR_HEADER));
	}

	@Test
	public void forHeaderRefusesCollapse() throws Exception {
		assertNull(spanOf(FOR_HEADER));
	}

	@Test
	public void forIteratorRefusesCollapse() throws Exception {
		assertFalse(violates(FOR_ITERATOR));
		assertNull(spanOf(FOR_ITERATOR));
	}

	@Test
	public void inlineAnnotatedFieldJoinsTheWholeDeclaration() throws Exception {
		assertTrue(violates(INLINE_ANNOTATED_FIELD));
		assertArrayEquals(new int[]{1, 2}, spanOf(INLINE_ANNOTATED_FIELD));
	}

	@Test
	public void labeledStatementJoinsBodyWithoutTheLabel() throws Exception {
		assertTrue(violates(LABELED_STATEMENT));
		assertArrayEquals(new int[]{3, 4}, spanOf(LABELED_STATEMENT));
	}

	@Test
	public void returnStatementJoinsTheWholeReturn() throws Exception {
		assertTrue(violates(RETURN_STATEMENT));
		assertArrayEquals(new int[]{2, 3}, spanOf(RETURN_STATEMENT));
	}

	@Test
	public void synchronizedLockRefusesCollapse() throws Exception {
		assertFalse(violates(SYNCHRONIZED_LOCK));
		assertNull(spanOf(SYNCHRONIZED_LOCK));
	}

	@Test
	public void ternaryArmJoinsTheWholeStatement() throws Exception {
		assertTrue(violates(TERNARY_ARM));
		assertArrayEquals(new int[]{2, 3}, spanOf(TERNARY_ARM));
	}

	@Test
	public void textBlockOpeningOnTheLastLineStillCollapses() throws Exception {
		// the boundary partner of the width gate's text-block refusal: the gate only refuses a text block
		// open at the END of an interior line, because a literal opened on the span's own last line is
		// closed by a later line the join never touches
		assertTrue(violates(TEXT_BLOCK_OPENS_ON_LAST_LINE));
		assertArrayEquals(new int[]{2, 3}, spanOf(TEXT_BLOCK_OPENS_ON_LAST_LINE));
	}

	@Test
	public void tryResourceRefusesCollapse() throws Exception {
		assertFalse(violates(TRY_RESOURCE));
		assertNull(spanOf(TRY_RESOURCE));
	}
}