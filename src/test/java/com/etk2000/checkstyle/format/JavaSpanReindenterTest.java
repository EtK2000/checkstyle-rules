package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.ast.AstIndent;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.JavaParser.Options;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class JavaSpanReindenterTest {
	private static final int HOST_BODY_TABS = 2;
	// unparseable spans, plus: a block opening on a continuation line steps in from that line, which
	// AstIndent leaves to the reindenter by contract, and `else` / `if` split across lines indents the
	// if as else's body where AstIndent keeps an else-if flat (ControlFlowBraces merges that shape)
	private static final Set<String> AST_INDENT_EXEMPT = Set.of(
			"chainAfterLambdaCloseInBlock",
			"continuationLineOpensAnonClass",
			"elseThenIfNextLine",
			"headerThenCloser",
			"spanEndsMidBody",
			"spanEndsOnHeader",
			"ternaryInIfConditionUnchanged"
	);

	/**
	 * Canonical layouts: each row's expected lines are reindented from the same lines with every indent
	 * stripped, and are a fixed point. A {@code [V]} prefix marks a line that begins inside a text block or
	 * block comment, so its leading whitespace is content and the stripped input keeps it.
	 */
	@Nonnull
	private static Stream<Arguments> canonicalLayouts() {
		return Stream.of(
				row("bracelessIfBody", "\tif (x)", "\t\ta();", "\tb();"),
				row("bracelessIfElseBodies", "\tif (x)", "\t\ta();", "\telse", "\t\tb();", "\tc();"),
				row("bracelessElseIfChain", "\tif (x)", "\t\ta();", "\telse if (y)", "\t\tb();", "\telse", "\t\tc();", "\td();"),
				row("bracelessForBodySemicolonsInHeader", "\tfor (var i = 0; i < n; ++i)", "\t\tc(i);", "\td();"),
				row("bracelessInfiniteForBody", "\tfor (;;)", "\t\tc();", "\td();"),
				row("bracelessEnhancedForBody", "\tfor (var i : l)", "\t\tc(i);", "\td();"),
				row("bracelessWhileBody", "\twhile (y)", "\t\td();", "\te();"),
				row("bracelessSynchronizedHeaderThenAllmanBrace", "\tsynchronized (lock)", "\t{", "\t\ta();", "\t}", "\tb();"),
				row("doTier2BodyOnDoLine", "\tdo --x;", "\twhile (x > 0);", "\te();"),
				row("doTier3BodyOnOwnLine", "\tdo", "\t\tlist.add(new Foo());", "\twhile (x > 0);", "\te();"),
				row("doBracedWhileOnOwnLine", "\tdo {", "\t\ta();", "\t}", "\twhile (x > 0);", "\te();"),
				row("doBracedWhileCuddled", "\tdo {", "\t\ta();", "\t} while (x > 0);", "\te();"),
				row("bracelessIfMultiLineCondition", "\tif (a", "\t\t\t&& b)", "\t\tc();", "\td();"),
				row("bracelessForMultiLineHeader", "\tfor (var i = 0;", "\t\t\ti < n;", "\t\t\t++i)", "\t\tc(i);", "\td();"),
				row("labelOwnLineOverBracelessWhile", "\touter:", "\twhile (y)", "\t\tbreak outer;", "\tdone();"),
				row("labelSameLineBracelessWhile", "\touter: while (y)", "\t\tbreak outer;", "\tdone();"),
				row("labelOverPlainStatement", "\touter:", "\tdone();"),
				row("nestedBracelessForIf", "\tfor (var i : l)", "\t\tif (i > 0)", "\t\t\tc(i);", "\td();"),
				row("nestedBracelessWhileForIf", "\twhile (y)", "\t\tfor (var i : l)", "\t\t\tif (i > 0)", "\t\t\t\tc(i);", "\td();"),
				row(
						"bracedForBracelessIf",
						"\tfor (var activity : activities) {",
						"\t\tif (activity instanceof SmsConsentActivity)",
						"\t\t\tconsentTask.set(activity);",
						"\t}",
						"\tdone();"
				),
				spanRow(
						"reportNestedLambdaBracedForBracelessIf",
						2,
						"\t\tregister(\"k\", () -> {",
						"\t\t\tlisteners.forEach(listener -> {",
						"\t\t\t\tfor (var activity : activities) {",
						"\t\t\t\t\tif (activity instanceof SmsConsentActivity)",
						"\t\t\t\t\t\tconsentTask.set(activity);",
						"\t\t\t\t}",
						"\t\t\t});",
						"\t\t});"
				),
				spanRow(
						"userReproConsentScreen",
						2,
						"\t\tDeviceRun.waitUntil(\"the consent screen opens\", DeviceRun.TIMEOUT_MS, () -> {",
						"\t\t\tDeviceRun.onMain(() -> {",
						"\t\t\t\tfor (var activity : DeviceRun.liveActivities()) {",
						"\t\t\t\t\tif (activity instanceof SmsConsentActivity)",
						"\t\t\t\t\t\tconsentTask.set(activity.getTaskId());",
						"\t\t\t\t}",
						"\t\t\t});",
						"\t\t\treturn consentTask.get() != -1;",
						"\t\t});"
				),
				row("bracelessForIfElse", "\tfor (var i : l)", "\t\tif (i > 0)", "\t\t\tc(i);", "\t\telse", "\t\t\td(i);", "\te();"),
				row("danglingElseBindsInnerIf", "\tif (a)", "\t\tif (b)", "\t\t\tx();", "\t\telse", "\t\t\ty();", "\tz();"),
				row("danglingElsesBindInnerThenOuter", "\tif (a)", "\t\tif (b)", "\t\t\tx();", "\t\telse", "\t\t\ty();", "\telse", "\t\tz();", "\tw();"),
				row("bracelessForDoTier3", "\tfor (var i : l)", "\t\tdo", "\t\t\tlist.add(new Foo(i));", "\t\twhile (more());", "\tdone();"),
				row("doBodyBracelessWhile", "\tdo", "\t\twhile (y)", "\t\t\tx();", "\twhile (z);", "\tdone();"),
				row("bracelessForBracedIfBracelessElse", "\tfor (var i : l)", "\t\tif (i > 0) {", "\t\t\tc(i);", "\t\t}", "\t\telse", "\t\t\td(i);", "\te();"),
				row("bracelessBodyHoldsBracedLambda", "\tif (x)", "\t\tfoo(() -> {", "\t\t\ta();", "\t\t});", "\tb();"),
				row("bracelessBodyLastBeforeInnerLambdaClose", "\tn(() -> {", "\t\tif (x)", "\t\t\ta();", "\t});", "\tb();"),
				row("bracelessBodyOperatorContinuation", "\tif (x)", "\t\ty = a", "\t\t\t\t+ b;", "\tz();"),
				row("bracelessBodyChain", "\tif (x)", "\t\tbuilder", "\t\t\t\t.a()", "\t\t\t\t.b();", "\tz();"),
				row("bracelessBodyParenArgs", "\tif (x)", "\t\tfoo(", "\t\t\t\targ", "\t\t);", "\tz();"),
				row("bracelessElseBodyContinuation", "\tif (x)", "\t\ta();", "\telse", "\t\ty = a", "\t\t\t\t+ b;", "\tz();"),
				row("doTier3BodyContinuation", "\tdo", "\t\tx = a", "\t\t\t\t+ b;", "\twhile (x < n);", "\tz();"),
				row("ternaryContinuation", "\tfinal var z = cond", "\t\t\t? 1", "\t\t\t: 2;", "\tnext();"),
				row("plusContinuation", "\tfinal var w = first", "\t\t\t+ second;", "\tnext();"),
				row("returnAndContinuation", "\treturn p", "\t\t\t&& q;"),
				row("orContinuationDoesNotCascade", "\tfinal var o = p", "\t\t\t|| q", "\t\t\t|| r;", "\tnext();"),
				row("assignmentRhsOnNextLine", "\tx =", "\t\t\tvalue;", "\tnext();"),
				row("bracelessLambdaInBlock", "\tfinal Runnable r = () ->", "\t\t\tfoo();", "\tnext();"),
				row("continuationAfterCloserLine", "\tfinal var x = foo(", "\t\t\targ", "\t)", "\t\t\t+ 1;", "\tnext();"),
				row("continuationLineOpensParen", "\tfinal var z = cond", "\t\t\t? foo(", "\t\t\t\t\targ", "\t\t\t)", "\t\t\t: 2;", "\tnext();"),
				row(
						"continuationLineOpensAnonClass",
						"\tfinal Runnable r = cond",
						"\t\t\t? new Runnable() {",
						"\t\t\t\t@Override",
						"\t\t\t\tpublic void run() {",
						"\t\t\t\t}",
						"\t\t\t}",
						"\t\t\t: null;",
						"\tnext();"
				),
				row("assertMessageContinuation", "\tassert x", "\t\t\t: \"msg\";", "\tnext();"),
				row("trailingColonTernaryNotLabel", "\tfinal var z = cond ? 1 :", "\t\t\t2;", "\tnext();"),
				row("chainStatementThenNext", "\tlist.stream()", "\t\t\t.map(f)", "\t\t\t.toList();", "\tnext();"),
				row("multiDeclaratorContinuation", "\tint a = 1,", "\t\t\tb = 2;", "\tuse(a, b);"),
				row("lambdaCloseThenStatement", "\tfoo(() -> {", "\t\ta();", "\t});", "\tb();"),
				row("ifBlockCloseThenStatement", "\tif (x) {", "\t\ta();", "\t}", "\tb();"),
				row("bracedIfElseOwnLine", "\tif (x) {", "\t\ta();", "\t}", "\telse {", "\t\tb();", "\t}", "\tc();"),
				row("bracedIfBracelessElseOwnLine", "\tif (x) {", "\t\ta();", "\t}", "\telse", "\t\tb();", "\tc();"),
				row("cuddledElseBracelessBody", "\tif (x) {", "\t\ta();", "\t} else", "\t\tb();", "\tc();"),
				row(
						"tryCatchFinallyOwnLine",
						"\ttry {",
						"\t\ta();",
						"\t}",
						"\tcatch (Exception e) {",
						"\t\tb();",
						"\t}",
						"\tfinally {",
						"\t\tc();",
						"\t}",
						"\td();"
				),
				row("localClassCloseThenStatement", "\tclass L {", "\t\tint n;", "\t}", "\tuse(new L());"),
				row("oneLineIfNotHeader", "\tif (x) a();", "\tb();"),
				row("emptyStatementWhileNotHeader", "\twhile (poll());", "\tnext();"),
				row("loadBearingSemicolonBody", "\tif (x)", "\t\t; // handled by the caller", "\tnext();"),
				row("consecutiveTerminatedStatements", "\ta();", "\tb();"),
				row("colonCaseAndDefaultBodies", "\tswitch (k) {", "\t\tcase 1:", "\t\t\ta();", "\t\t\tbreak;", "", "\t\tdefault:", "\t\t\tb();", "\t}", "\tnext();"),
				row("fallThroughLabels", "\tswitch (k) {", "\t\tcase 1:", "\t\tcase 2:", "\t\t\ta();", "\t\t\tbreak;", "", "\t\tdefault:", "\t\t\tb();", "\t}"),
				row("commaColonLabels", "\tswitch (k) {", "\t\tcase 1, 2:", "\t\t\ta();", "\t\t\tbreak;", "", "\t\tdefault:", "\t\t\tb();", "\t}"),
				row("guardedPatternColonLabel", "\tswitch (o) {", "\t\tcase Integer i when i > 0:", "\t\t\ta(i);", "\t\t\tbreak;", "", "\t\tdefault:", "\t\t\tb();", "\t}"),
				row(
						"bracedCaseTrailingSibling",
						"\tswitch (k) {",
						"\t\tcase 1: {",
						"\t\t\tfinal var v = 1;",
						"\t\t\tmark(v);",
						"\t\t}",
						"\t\t\tmore();",
						"\t\t\tbreak;",
						"",
						"\t\tdefault:",
						"\t\t\tb();",
						"\t}"
				),
				row("blockLaterInCaseBody", "\tswitch (k) {", "\t\tcase 1:", "\t\t\tmore();", "\t\t\t{", "\t\t\t\tmark();", "\t\t\t}", "\t\t\tbreak;", "\t}"),
				row(
						"nestedColonSwitch",
						"\tswitch (x) {",
						"\t\tcase 1:",
						"\t\t\tswitch (n) {",
						"\t\t\t\tcase 2:",
						"\t\t\t\t\tmark();",
						"\t\t\t\t\tbreak;",
						"\t\t\t}",
						"\t\t\tbreak;",
						"\t}"
				),
				row("caseBodyBracelessIf", "\tswitch (k) {", "\t\tcase 1:", "\t\t\tif (x)", "\t\t\t\ta();", "\t\t\tbreak;", "", "\t\tcase 2:", "\t\t\tb();", "\t}"),
				row("switchExpressionColonYield", "\tfinal var r = switch (k) {", "\t\tcase 1:", "\t\t\tyield 2;", "\t\tdefault:", "\t\t\tyield 3;", "\t};", "\tuse(r);"),
				row("arrowSwitchSingleStatements", "\tswitch (k) {", "\t\tcase 1 -> a();", "\t\tdefault -> b();", "\t}", "\tnext();"),
				row("arrowSwitchBlock", "\tswitch (k) {", "\t\tcase 1 -> {", "\t\t\ta();", "\t\t}", "\t\tdefault -> b();", "\t}"),
				row("arrowCaseExpressionNextLine", "\tswitch (k) {", "\t\tcase 1 ->", "\t\t\t\tfoo();", "\t\tdefault -> b();", "\t}"),
				row("keywordPrefixIdentifierIffy", "\tiffy = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierDoCount", "\tdoCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierDouble", "\tdouble d = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierForCount", "\tforCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierWhileCount", "\twhileCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierElseCount", "\telseCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierCaseCount", "\tcaseCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierDefaultCount", "\tdefaultCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordPrefixIdentifierTryCount", "\ttryCount = a", "\t\t\t+ b;", "\tnext();"),
				row("keywordInStringContinuation", "\tfinal var s = \"if (x)\"", "\t\t\t+ \"else\";", "\tnext();"),
				row("semicolonInStringNotTerminator", "\tfinal var s = \"a;\"", "\t\t\t+ \"b\";", "\tnext();"),
				row("semicolonCharLiteralNotTerminator", "\tfinal var c = flag ? ';'", "\t\t\t: ',';", "\tnext();"),
				row("semicolonInLineCommentNotTerminator", "\tfinal var x = a // done;", "\t\t\t+ b;", "\tnext();"),
				row("keywordLineCommentOwnLine", "\t// else", "\tb();", "\t// if (x)", "\tc();"),
				row("keywordTrailingCommentAfterStatement", "\ta(); // if (x)", "\tb();"),
				row("keywordBlockCommentOwnLine", "\t/* if (x) */", "\tb();"),
				row("headerTrailingLineComment", "\tif (x) // why", "\t\ta();", "\tb();"),
				row("headerTrailingCommentWithBrace", "\tif (x) // {", "\t\ta();", "\tb();"),
				row("elseTrailingLineComment", "\tif (x)", "\t\ta();", "\telse // fallback", "\t\tb();", "\tc();"),
				row("headerWithoutSpace", "\tif(x)", "\t\ta();", "\tb();"),
				row("stackedAnnotationThenDeclaration", "\t@SuppressWarnings(\"unused\")", "\tfinal var x = 1;", "\tnext();"),
				row(
						"anonClassOverrideMethod",
						"\tfinal var r = new Runnable() {",
						"\t\t@Override",
						"\t\tpublic void run() {",
						"\t\t\tif (x)",
						"\t\t\t\ta();",
						"\t\t}",
						"\t};",
						"\tr.run();"
				),
				row("arrayInitializerElements", "\tfinal int[] a = {", "\t\t1,", "\t\t2", "\t};", "\tuse(a);"),
				row("localEnumConstants", "\tenum Mode {", "\t\tA,", "\t\tB", "\t}", "\tuse(Mode.A);"),
				row("annotationArrayValueThenDeclaration", "\t@SuppressWarnings({", "\t\t\"a\",", "\t\t\"b\"", "\t})", "\tfinal var x = 1;"),
				row("arrayElementContinuation", "\tfinal String[] a = {", "\t\t\"x\"", "\t\t\t\t+ \"y\",", "\t\t\"z\"", "\t};"),
				row("textBlockStatementThenNext", "\tfinal var s = \"\"\"", "[V]\t\t\tif (y)", "[V]\t\t\t\"\"\";", "\tuse(s);"),
				row("textBlockInBracelessBody", "\tif (x)", "\t\tlog(\"\"\"", "[V]\t\t\t\ttext", "[V]\t\t\t\t\"\"\");", "\tnext();"),
				row("textBlockCloseThenContinuation", "\tfinal var s = \"\"\"", "[V]\t\t\ttext", "[V]\t\t\t\"\"\"", "\t\t\t+ suffix;", "\tnext();"),
				row("blockCommentInteriorKeywords", "\t/*", "[V]\t * if (x)", "[V]\t * a;", "[V]\t */", "\tb();"),
				row("blockCommentBetweenHeaderAndBody", "\tif (x) /* why", "[V]\t\t because */", "\t\ta();", "\tb();"),
				row("commentLineBetweenHeaderAndBody", "\tif (x)", "\t\t// note", "\t\ta();", "\tb();"),
				row("blankLineBetweenHeaderAndBody", "\tif (x)", "", "\t\ta();", "\tb();"),
				row("commentLineInsideContinuation", "\tfinal var x = a", "\t\t\t// note", "\t\t\t+ b;", "\tnext();"),
				spanRow("depthZeroBracelessHeader", 0, "if (x)", "\ta();", "b();"),
				spanRow("depthZeroContinuation", 0, "final var x = foo(", "\t\targ", ")", "\t\t+ 1;", "next();"),
				spanRow("depthZeroChainAfterCall", 0, "builder(", "\t\targ", ")", "\t\t.build();"),
				spanRow("spanEndsOnHeader", 0, "m(() -> {", "\tif (x)"),
				spanRow("spanEndsMidBody", 0, "m(() -> {", "\tif (x)", "\t\ta();"),
				spanRow("headerThenCloser", 0, "m(() -> {", "\tif (x)", "});"),
				spanRow("ternaryInIfConditionUnchanged", 0, "if (check(flag", "\t\t? a", "\t\t: b", "))"),
				row("allmanBraceAfterHeader", "\tif (x)", "\t{", "\t\ta();", "\t}", "\tb();"),
				row("allmanBraceAfterElse", "\tif (x)", "\t\ta();", "\telse", "\t{", "\t\tb();", "\t}", "\tc();"),
				row("elseThenIfNextLine", "\tif (a)", "\t\tx();", "\telse", "\t\tif (b)", "\t\t\ty();", "\tz();"),
				row("braceOnHeaderContinuationLine", "\tif (a", "\t\t\t&& b) {", "\t\tc();", "\t}", "\td();"),
				row("tryResourcesBraceOnContinuationLine", "\ttry (var a = open();", "\t\t\tvar b = open()) {", "\t\tuse(a, b);", "\t}", "\tnext();"),
				row("chainAfterLambdaCloseInBlock", "\tlist.stream()", "\t\t\t.map(x -> {", "\t\t\t\treturn x;", "\t\t\t})", "\t\t\t.toList();", "\tnext();")
		);
	}

	private static void collectStatementStarts(@Nonnull DetailAST node, @Nonnull List<DetailAST> starts) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (isStatementStart(child))
				starts.add(child);
			collectStatementStarts(child, starts);
		}
	}

	@Nonnull
	private static DetailAST firstToken(@Nonnull DetailAST node) {
		var first = node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var candidate = firstToken(child);
			if (candidate.getLineNo() < first.getLineNo() || candidate.getLineNo() == first.getLineNo() && candidate.getColumnNo() < first.getColumnNo())
				first = candidate;
		}
		return first;
	}

	@Nonnull
	private static List<String> inputOf(@Nonnull List<String> expected) {
		return expected.stream()
				.map(line -> line.startsWith("[V]") ? line.substring(3) : line.strip())
				.toList();
	}

	private static boolean isStatementStart(@Nonnull DetailAST node) {
		final var parent = node.getParent();
		return switch (node.getType()) {
			case TokenTypes.COMMA, TokenTypes.LCURLY, TokenTypes.RCURLY, TokenTypes.SEMI, TokenTypes.SLIST -> false;
			case TokenTypes.LITERAL_CASE, TokenTypes.LITERAL_DEFAULT -> parent.getType() == TokenTypes.CASE_GROUP || parent.getType() == TokenTypes.SWITCH_RULE;
			case TokenTypes.LITERAL_CATCH, TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FINALLY -> true;
			default -> switch (parent.getType()) {
				case TokenTypes.LABELED_STAT, TokenTypes.OBJBLOCK, TokenTypes.SLIST -> true;
				case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE -> node == parent.getFirstChild();
				case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE -> {
					final var previous = node.getPreviousSibling();
					yield previous != null && previous.getType() == TokenTypes.RPAREN;
				}
				default -> false;
			};
		};
	}

	@Nonnull
	private static List<String> outputOf(@Nonnull List<String> expected) {
		return expected.stream()
				.map(line -> line.startsWith("[V]") ? line.substring(3) : line)
				.toList();
	}

	@Nonnull
	private static Arguments row(@Nonnull String name, @Nonnull String... body) {
		final var lines = new ArrayList<String>(body.length + 2);
		lines.add("m(() -> {");
		lines.addAll(List.of(body));
		lines.add("});");
		return Arguments.of(name, 0, lines);
	}

	@Nonnull
	private static Arguments spanRow(@Nonnull String name, int baseTabs, @Nonnull String... lines) {
		return Arguments.of(name, baseTabs, List.of(lines));
	}

	@Test
	public void testAnnotatedParameterInteriorContinuation() {
		assertEquals(
				List.of("method(", "\t\t@Deprecated int x", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "@Deprecated int x", ")"), 0)
		);
	}

	@Test
	public void testArrayIndexContinuationStepTwo() {
		assertEquals(
				List.of("arr[", "\t\ti + 1", "]"),
				JavaSpanReindenter.reindent(List.of("arr[", "i + 1", "]"), 0)
		);
	}

	@Test
	public void testArrayInitBraceStepOne() {
		assertEquals(
				List.of("new int[] {", "\t1, 2, 3", "}"),
				JavaSpanReindenter.reindent(List.of("new int[] {", "1, 2, 3", "}"), 0)
		);
	}

	@Test
	public void testBaseTabsDeepNesting() {
		assertEquals(
				List.of("\t\t\tmethod(", "\t\t\t\t\targ", "\t\t\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 3)
		);
	}

	@Test
	public void testBaseTabsShiftsWholeSpan() {
		assertEquals(
				List.of("\tmethod(", "\t\t\targ", "\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 1)
		);
	}

	@Test
	public void testBaseTabsTwo() {
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\targ", "\t\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 2)
		);
	}

	@Test
	public void testBlockCommentSingleLineBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tfoo(/* )]} */ y)", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "foo(/* )]} */ y)", ")"), 0)
		);
	}

	@Test
	public void testBraceBodyStepOne() {
		assertEquals(
				List.of("method(x -> {", "\tbody();", "});"),
				JavaSpanReindenter.reindent(List.of("method(x -> {", "body();", "});"), 0)
		);
	}

	@Test
	public void testBraceCloseThenParenContinuationAlignsWithOpener() {
		assertEquals(
				List.of("a(() -> {", "\tbody();", "}, x)"),
				JavaSpanReindenter.reindent(List.of("a(() -> {", "body();", "}, x)"), 0)
		);
	}

	@Test
	public void testBracelessLambdaContinuationStepTwo() {
		assertEquals(
				List.of("cache.put(\"k\", v ->", "\t\tSystem.out.println(v)", ")"),
				JavaSpanReindenter.reindent(List.of("cache.put(\"k\", v ->", "System.out.println(v)", ")"), 0)
		);
	}

	@MethodSource("canonicalLayouts")
	@ParameterizedTest(name = "{0}")
	public void testCanonicalLayout(@Nonnull String name, int baseTabs, @Nonnull List<String> expected) {
		assertEquals(outputOf(expected), JavaSpanReindenter.reindent(inputOf(expected), baseTabs));
	}

	@MethodSource("canonicalLayouts")
	@ParameterizedTest(name = "{0}")
	public void testCanonicalLayoutAgreesWithAstIndent(@Nonnull String name, int baseTabs, @Nonnull List<String> expected) throws Exception {
		if (AST_INDENT_EXEMPT.contains(name))
			return;

		final var hosted = new ArrayList<String>();
		hosted.add("class T {");
		hosted.add("\tvoid m() {");
		for (var line : outputOf(expected))
			hosted.add(line.isEmpty() ? line : "\t".repeat(HOST_BODY_TABS - baseTabs) + line);
		hosted.add("\t}");
		hosted.add("}");
		final var root = JavaParser.parseFileText(new FileText(new File("T.java"), hosted), Options.WITHOUT_COMMENTS);
		final var starts = new ArrayList<DetailAST>();
		collectStatementStarts(root, starts);
		for (var start : starts) {
			final var first = firstToken(start);
			final var line = hosted.get(first.getLineNo() - 1);
			if (line.substring(0, first.getColumnNo()).isBlank())
				assertEquals(AstIndent.tabsFor(start), line.length() - line.stripLeading().length(), line);
		}
	}

	@MethodSource("canonicalLayouts")
	@ParameterizedTest(name = "{0}")
	public void testCanonicalLayoutIsFixedPoint(@Nonnull String name, int baseTabs, @Nonnull List<String> expected) {
		assertEquals(outputOf(expected), JavaSpanReindenter.reindent(outputOf(expected), baseTabs));
	}

	@Test
	public void testChainAfterNonChainContinuationStepsInAgain() {
		assertEquals(
				List.of("method(", "\t\tobj", "\t\t\t\t.a()", "\t\t+ x", "\t\t\t\t.b()", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "obj", ".a()", "+ x", ".b()", ")"), 0)
		);
	}

	@Test
	public void testChainedPutTwoLevels() {
		assertEquals(
				List.of("cache.put(\"k\", new JSONObject()", "\t\t.put(\"a\", 1)", "\t\t.put(\"b\", 2)", ")"),
				JavaSpanReindenter.reindent(List.of("cache.put(\"k\", new JSONObject()", ".put(\"a\", 1)", ".put(\"b\", 2)", ")"), 0)
		);
	}

	@Test
	public void testChainReceiverOnOwnContinuationLine() {
		assertEquals(
				List.of("\t\tcache.put(", "\t\t\t\t\"k\",", "\t\t\t\tnew JSONObject()", "\t\t\t\t\t\t.put(\"a\", 1)", "\t\t\t\t\t\t.put(\"b\", 2)", "\t\t);"),
				JavaSpanReindenter.reindent(List.of("cache.put(", "\"k\",", "new JSONObject()", ".put(\"a\", 1)", ".put(\"b\", 2)", ");"), 2)
		);
	}

	@Test
	public void testCharLiteralBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tc = ')'", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "c = ')'", ")"), 0)
		);
	}

	@Test
	public void testConstructorWithMethodCallArgOracle() {
		assertEquals(
				List.of("\t\tmethod(new ArrayList<>(other(", "\t\t\t\targ", "\t\t)))"),
				JavaSpanReindenter.reindent(List.of("method(new ArrayList<>(other(", "arg", ")))"), 2)
		);
	}

	@Test
	public void testContentLineAtBaseAfterBalancedSpan() {
		assertEquals(
				List.of("List.of(", "\t\tx", ");", "y"),
				JavaSpanReindenter.reindent(List.of("List.of(", "x", ");", "y"), 0)
		);
	}

	@Test
	public void testDeepBracelessNestingNoStackOverflow() {
		final var input = new ArrayList<String>();
		input.add("m(() -> {");
		for (var i = 0; i < 200; ++i)
			input.add("if (c)");
		input.add("x();");
		input.add("});");
		final var out = assertDoesNotThrow(() -> JavaSpanReindenter.reindent(input, 0));
		assertEquals("\t".repeat(201) + "x();", out.get(201));
		assertEquals("});", out.getLast());
	}

	@Test
	public void testDeepNestingNoStackOverflow() {
		final var input = new ArrayList<String>();
		for (var i = 0; i < 200; ++i)
			input.add("a(");
		input.add("x");
		for (var i = 0; i < 200; ++i)
			input.add(")");
		final var out = assertDoesNotThrow(() -> JavaSpanReindenter.reindent(input, 0));
		assertEquals(input.size(), out.size());
		assertEquals("\t".repeat(400) + "x", out.get(200));
	}

	@Test
	public void testEmptyInteriorLineStaysEmpty() {
		assertEquals(
				List.of("method(", "", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "", "arg", ")"), 0)
		);
	}

	@Test
	public void testEmptyListReturnedAsIs() {
		assertEquals(List.of(), JavaSpanReindenter.reindent(List.of(), 0));
	}

	@Test
	public void testEscapedQuoteKeepsStringMasked() {
		assertEquals(
				List.of("method(", "\t\ts = \"\\\")\"", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "s = \"\\\")\"", ")"), 0)
		);
	}

	@Test
	public void testFirstLineMisindentedForcedToBase() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("    method(", "arg", ")"), 0)
		);
	}

	@Test
	public void testGenericAngleBracketsNotCounted() {
		assertEquals(
				List.of("method(", "\t\tMap<String, List<Integer>> m", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "Map<String, List<Integer>> m", ")"), 0)
		);
	}

	@Test
	public void testIdempotentOnCanonicalNestedPut() {
		final var canonical = List.of(
				"\t\tcache.put(\"View\", new JSONObject()",
				"\t\t\t\t.put(\"Account\", new JSONObject()",
				"\t\t\t\t\t\t.put(\"id\", 1)",
				"\t\t\t\t)",
				"\t\t);"
		);
		assertEquals(canonical, JavaSpanReindenter.reindent(canonical, 2));
	}

	@Test
	public void testIdempotentOnCanonicalParen() {
		final var canonical = List.of("method(", "\t\targ", ")");
		assertEquals(canonical, JavaSpanReindenter.reindent(canonical, 0));
	}

	@Test
	public void testLeadingSpacesNormalizedToTabs() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "    arg", ")"), 0)
		);
	}

	@Test
	public void testLineCommentBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tx() // )])}", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "x() // )])}", ")"), 0)
		);
	}

	@Test
	public void testMixedTabsSpacesNormalized() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "\t   arg", ")"), 0)
		);
	}

	@Test
	public void testMultiLineBlockCommentInteriorVerbatim() {
		assertEquals(
				List.of("method(", "\t\tx = a; /* start", "  comment ( with ) brackets", "  end */ z", ")"),
				JavaSpanReindenter.reindent(
						List.of("method(", "x = a; /* start", "  comment ( with ) brackets", "  end */ z", ")"), 0
				)
		);
	}

	@Test
	public void testMultipleOpensOneLevel() {
		assertEquals(
				List.of("method(new ArrayList<>(other(", "\t\targ", ")))"),
				JavaSpanReindenter.reindent(List.of("method(new ArrayList<>(other(", "arg", ")))"), 0)
		);
	}

	@Test
	public void testNestedChainedPutDepthFour() {
		assertEquals(
				List.of(
						"cache.put(\"View\", new JSONObject()",
						"\t\t.put(\"Account\", new JSONObject()",
						"\t\t\t\t.put(\"id\", 1)",
						"\t\t\t\t.put(\"name\", \"x\")",
						"\t\t)",
						")"
				),
				JavaSpanReindenter.reindent(
						List.of(
								"cache.put(\"View\", new JSONObject()",
								".put(\"Account\", new JSONObject()",
								".put(\"id\", 1)",
								".put(\"name\", \"x\")",
								")",
								")"
						),
						0
				)
		);
	}

	@Test
	public void testOverClosedClampsAtBase() {
		assertEquals(
				List.of("method(", ")", ")", ")"),
				JavaSpanReindenter.reindent(List.of("method(", ")", ")", ")"), 0)
		);
	}

	@Test
	public void testParenSingleLevelBase0() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 0)
		);
	}

	@Test
	public void testPerLineClosersProgressiveDedent() {
		assertEquals(
				List.of("a(", "\t\tb(", "\t\t\t\tc(", "\t\t\t\t\t\tx", "\t\t\t\t)", "\t\t)", ")"),
				JavaSpanReindenter.reindent(List.of("a(", "b(", "c(", "x", ")", ")", ")"), 0)
		);
	}

	@Test
	public void testSingleLineSpanReturnedAsIs() {
		assertEquals(List.of("method(x);"), JavaSpanReindenter.reindent(List.of("method(x);"), 0));
	}

	@Test
	public void testStackedClosersSingleDedent() {
		assertEquals(
				List.of("a(b(c(", "\t\tx", ")))"),
				JavaSpanReindenter.reindent(List.of("a(b(c(", "x", ")))"), 0)
		);
	}

	@Test
	public void testStringUnbalancedBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tfoo(\"a)b\")", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "foo(\"a)b\")", ")"), 0)
		);
	}

	@Test
	public void testTextBlockCloseTrailingClosersSplitToOwnLine() {
		assertEquals(
				List.of("m(() -> {", "\treturn \"\"\"", "\t\t\ttext\"\"\";", "});"),
				JavaSpanReindenter.reindent(List.of("m(() -> {", "return \"\"\"", "\t\t\ttext\"\"\"; });"), 0)
		);
	}

	@Test
	public void testTextBlockInteriorVerbatim() {
		assertEquals(
				List.of("\t\tmethod(\"\"\"", "  raw ( content", "  more }", "  \"\"\")"),
				JavaSpanReindenter.reindent(List.of("method(\"\"\"", "  raw ( content", "  more }", "  \"\"\")"), 2)
		);
	}

	@Test
	public void testTrailingCloserSplitRejectsCodeAfterLiteralClose() {
		assertEquals(
				List.of("m(() -> {", "\treturn \"\"\"", "\t\t\ttext\"\"\"; foo();"),
				JavaSpanReindenter.reindent(List.of("m(() -> {", "return \"\"\"", "\t\t\ttext\"\"\"; foo();"), 0)
		);
	}

	@Test
	public void testUnterminatedBlockCommentNoCrash() {
		final var input = List.of("method(", "x = a; /* never closes", "  still comment ) (", "  and more");
		final var out = assertDoesNotThrow(() -> JavaSpanReindenter.reindent(input, 0));
		assertEquals(
				List.of("method(", "\t\tx = a; /* never closes", "  still comment ) (", "  and more"),
				out
		);
	}

	@Test
	public void testWhitespaceOnlyInteriorLineBecomesEmpty() {
		assertEquals(
				List.of("method(", "", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "   ", "arg", ")"), 0)
		);
	}
}