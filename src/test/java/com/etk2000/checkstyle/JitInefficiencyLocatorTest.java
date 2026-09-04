package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JitInefficiencyCheck.JitTarget;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link JitInefficiencyCheck#locateAt}, the seam the fixer dispatches on.
 *
 * <p>The fixer no longer reads the line, so a reported position the locator cannot resolve is a
 * fix that silently stops happening, and a position it resolves to the wrong node is a rewrite
 * applied to someone else's violation. Both are invisible to a fixture-driven test, which only
 * sees the output: that is what these assert.
 */
public class JitInefficiencyLocatorTest {
	/** The node type every category reports on, measured over the corpus rather than assumed. */
	private static final Map<JitInefficiencyCategory, Set<Integer>> REPORTED_ON = reportedOn();
	private static final String CORPUS = "jitinefficiency/cases.in.java";
	private static final String FIXTURE = "jitinefficiency/InputLocatorPositions.java";

	/** The 0-based line of the first statement in the fixture method named {@code method}. */
	@CheckReturnValue
	private static int bodyLineOf(@Nonnull String method) throws Exception {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(parseFixture());
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				final var body = node.findFirstToken(TokenTypes.SLIST);
				if (ident != null && body != null && method.equals(ident.getText()))
					return body.getFirstChild().getLineNo() - 1;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new IllegalArgumentException("no method named " + method + " in " + FIXTURE);
	}

	/** An AST column counts code points, so a char index into a line carrying one must be converted. */
	@CheckReturnValue
	private static int codePointColumnOf(@Nonnull String line, int charIndex) {
		return line.codePointCount(0, charIndex);
	}

	@CheckReturnValue
	private static int columnOf(@Nonnull String line, @Nonnull String needle) {
		final var index = line.indexOf(needle);
		if (index < 0)
			throw new IllegalArgumentException("no [" + needle + "] in [" + line + "]");
		return index;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return Files.readAllLines(Path.of(resource(FIXTURE).toURI()), StandardCharsets.UTF_8);
	}

	/**
	 * The target at the start of {@code needle} on the single statement line of {@code method}.
	 * A {@code LITERAL_NEW} reports on its {@code new} keyword and a {@code PLUS} on its operator,
	 * so the needle is the token itself rather than the construct's name.
	 */
	@CheckReturnValue
	@Nullable
	private static JitTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		final var line = bodyLineOf(method);
		final var text = fixtureLines().get(line);
		return JitInefficiencyCheck.locateAt(parseFixture(), line, codePointColumnOf(text, columnOf(text, needle)));
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var file = new File(resource(FIXTURE).toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Map<JitInefficiencyCategory, Set<Integer>> reportedOn() {
		final var byCategory = new EnumMap<JitInefficiencyCategory, Set<Integer>>(JitInefficiencyCategory.class);
		byCategory.put(JitInefficiencyCategory.APPEND_CONCAT, Set.of(TokenTypes.METHOD_CALL));
		byCategory.put(JitInefficiencyCategory.BOXED_ACCUMULATOR, Set.of(TokenTypes.VARIABLE_DEF));
		byCategory.put(JitInefficiencyCategory.BOXED_CONSTRUCTOR, Set.of(TokenTypes.LITERAL_NEW));
		byCategory.put(JitInefficiencyCategory.DOUBLE_BRACE, Set.of(TokenTypes.LITERAL_NEW));
		byCategory.put(JitInefficiencyCategory.EMPTY_STRING_CONCAT, Set.of(TokenTypes.PLUS));
		byCategory.put(JitInefficiencyCategory.ENUM_VALUES_IN_LOOP, Set.of(TokenTypes.METHOD_CALL));
		byCategory.put(JitInefficiencyCategory.ITERATOR_LOOP, Set.of(TokenTypes.LITERAL_WHILE));
		byCategory.put(JitInefficiencyCategory.MAP_KEYSET_GET, Set.of(TokenTypes.LITERAL_FOR));
		byCategory.put(JitInefficiencyCategory.NEW_STRING, Set.of(TokenTypes.LITERAL_NEW));
		// two detectors, one for `Pattern.compile("x")` and one for `new SimpleDateFormat("x")`
		byCategory.put(JitInefficiencyCategory.REUSABLE_OBJECT, Set.of(TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL));
		byCategory.put(JitInefficiencyCategory.STRING_BUFFER, Set.of(TokenTypes.LITERAL_NEW));
		byCategory.put(JitInefficiencyCategory.STRING_CONCAT_IN_LOOP, Set.of(TokenTypes.ASSIGN, TokenTypes.PLUS_ASSIGN));
		byCategory.put(JitInefficiencyCategory.STRING_REGEX_IN_LOOP, Set.of(TokenTypes.METHOD_CALL));
		byCategory.put(JitInefficiencyCategory.TOARRAY_SIZED, Set.of(TokenTypes.METHOD_CALL));
		return byCategory;
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource(@Nonnull String path) {
		return requireNonNull(
				JitInefficiencyLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path),
				"Test input file not found: " + path
		);
	}

	/**
	 * Mirrors {@code CheckstyleFixAction.tabColumnToCharIndex}, which the fix pipeline applies to a
	 * reported column before calling the fixer.
	 */
	@CheckReturnValue
	private static int tabColumnToCodePointColumn(@Nonnull String line, int tabExpandedColumn) {
		var visualColumn = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (visualColumn >= tabExpandedColumn)
				return i;
			if (line.charAt(i) == '\t')
				visualColumn += LineLength.TAB_WIDTH - (visualColumn % LineLength.TAB_WIDTH);
			else
				++visualColumn;
		}
		return line.length();
	}

	/** Every target the check's own output drives. */
	@CheckReturnValue
	@Nonnull
	private static List<JitTarget> targetsOverTheCorpus(@Nonnull List<String> misses) throws Exception {
		final var url = resource(CORPUS);
		final var lines = Files.readAllLines(Path.of(url.toURI()), StandardCharsets.UTF_8);
		final var root = JavaParser.parseFileText(
				new FileText(new File(url.toURI()), lines), JavaParser.Options.WITHOUT_COMMENTS
		);
		final var targets = new ArrayList<JitTarget>();
		for (var event : BaseCheckTest.runCheck(JitInefficiencyCheck.class, CORPUS)) {
			final var text = lines.get(event.getLine() - 1);
			final var target = JitInefficiencyCheck.locateAt(
					root, event.getLine() - 1, tabColumnToCodePointColumn(text, event.getColumn() - 1)
			);
			if (target == null)
				misses.add("line " + event.getLine() + ": " + text.strip());
			else
				targets.add(target);
		}
		return targets;
	}

	@Test
	public void aForeignQualifierCarriesNoReplacement() throws Exception {
		for (var method : List.of("boxedConstructorForeignQualifier", "newStringForeignQualifier", "stringBufferForeignQualifier")) {
			final var target = locate(method, "new ");
			assertNotNull(target, method);
			assertNull(target.replacement(), method + " names a class that is not java.lang's");
		}
	}

	@Test
	public void aParenthesizedOperandResolvesPastTheParens() throws Exception {
		final var target = locate("emptyConcatParenthesizedOperand", "+ (");
		assertNotNull(target);
		assertEquals(JitInefficiencyCategory.EMPTY_STRING_CONCAT, target.category());
		assertNotNull(target.argument());
		assertEquals(TokenTypes.PLUS, target.argument().getType(), "the operand is the inner sum, not its opening paren");
	}

	@Test
	public void aPositionNoNodeStartsAtResolvesToNothing() throws Exception {
		final var line = bodyLineOf("emptyConcatLeft");
		final var text = fixtureLines().get(line);
		// the second character of the call's name: inside a token, so no node begins there at all
		final var inside = codePointColumnOf(text, columnOf(text, "System") + 1);
		assertNull(JitInefficiencyCheck.locateAt(parseFixture(), line, inside));
	}

	@Test
	public void bothQualifiedSpellingsOfAJdkClassCarryItsSimpleName() throws Exception {
		assertEquals("Integer", requireNonNull(locate("boxedConstructor", "new ")).replacement());
		assertEquals("Integer", requireNonNull(locate("boxedConstructorQualified", "new ")).replacement());
		assertEquals("String", requireNonNull(locate("newString", "new ")).replacement());
		assertEquals("StringBuffer", requireNonNull(locate("stringBuffer", "new ")).replacement());
		assertEquals("StringBuffer", requireNonNull(locate("stringBufferQualified", "new ")).replacement());
	}

	@Test
	public void everyReportedPositionInTheCorpusResolvesToACategory() throws Exception {
		final var misses = new ArrayList<String>();
		final var targets = targetsOverTheCorpus(misses);

		final var seen = EnumSet.noneOf(JitInefficiencyCategory.class);
		targets.forEach(target -> seen.add(target.category()));

		assertEquals(List.of(), misses, "reported positions the locator could not resolve");
		assertEquals(
				EnumSet.allOf(JitInefficiencyCategory.class),
				seen,
				"categories the corpus never drives, which would leave a dispatch arm unreachable"
		);
		assertEquals(468, targets.size(), "reported positions resolved; update when the corpus grows");
	}

	/** Resolving to the right <em>category</em> is not enough: the fixer splices the node it is handed. */
	@Test
	public void everyReportedPositionResolvesToItsOwnTokenType() throws Exception {
		final var wrong = new ArrayList<String>();
		for (var target : targetsOverTheCorpus(new ArrayList<>())) {
			if (!REPORTED_ON.get(target.category()).contains(target.node().getType())) {
				wrong.add(target.category()
						+ " resolved to " + TokenUtil.getTokenName(target.node().getType())
						+ " at line " + target.node().getLineNo()
				);
			}
		}
		assertEquals(List.of(), wrong, "positions that resolved to a node the category does not report on");
	}

	@Test
	public void theCategoriesWithNoRewriteCarryNoPayload() throws Exception {
		final var manual = EnumSet.of(
				JitInefficiencyCategory.BOXED_ACCUMULATOR,
				JitInefficiencyCategory.DOUBLE_BRACE,
				JitInefficiencyCategory.ENUM_VALUES_IN_LOOP,
				JitInefficiencyCategory.ITERATOR_LOOP,
				JitInefficiencyCategory.MAP_KEYSET_GET,
				JitInefficiencyCategory.REUSABLE_OBJECT,
				JitInefficiencyCategory.STRING_REGEX_IN_LOOP
		);
		final var carried = new ArrayList<String>();
		for (var target : targetsOverTheCorpus(new ArrayList<>())) {
			if (manual.contains(target.category()) && (target.replacement() != null || target.argument() != null))
				carried.add(target.category() + " at line " + target.node().getLineNo());
		}
		assertEquals(List.of(), carried, "manual categories carrying a rewrite payload");
	}

	@Test
	public void theCodePointColumnLocatesWhereTheCharIndexMisses() throws Exception {
		final var line = bodyLineOf("supplementaryBeforeBoxed");
		final var text = fixtureLines().get(line);
		final var charIndex = columnOf(text, "new ");
		final var codePointColumn = codePointColumnOf(text, charIndex);
		final var root = parseFixture();

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		final var hit = JitInefficiencyCheck.locateAt(root, line, codePointColumn);
		assertNotNull(hit, "the code-point column must locate the constructor");
		assertEquals(JitInefficiencyCategory.BOXED_CONSTRUCTOR, hit.category());
		assertNull(JitInefficiencyCheck.locateAt(root, line, charIndex), "the char index must not");
	}

	@Test
	public void theInnerPlusOfAChainIsWhatReports() throws Exception {
		final var chain = locate("emptyConcatChain", "+ a");
		assertNotNull(chain);
		assertEquals(JitInefficiencyCategory.EMPTY_STRING_CONCAT, chain.category());
		assertNotNull(chain.argument());
		assertEquals("a", chain.argument().getText(), "the surviving operand of the inner sum");

		final var reversed = locate("emptyConcatRight", "+ \"\"");
		assertNotNull(reversed);
		assertNotNull(reversed.argument());
		assertEquals("x", reversed.argument().getText(), "and on the other side, the left operand");
	}

	/**
	 * {@code "" + null} and {@code "" + someCharArray} both compile as a {@code String.valueOf}
	 * call and neither produces what the concatenation did, so the check answers for the rewrite
	 * rather than leaving each fixer to rediscover it.
	 */
	@Test
	public void theTwoOperandsValueOfWouldChangeAreReportedAsSuch() throws Exception {
		final var nullOperand = requireNonNull(locate("nullOperand", "+ null")).argument();
		final var charArray = requireNonNull(locate("charArrayOperand", "+ chars")).argument();
		final var plain = requireNonNull(locate("emptyConcatLeft", "+ x")).argument();
		assertNotNull(nullOperand);
		assertNotNull(charArray);
		assertNotNull(plain);

		assertFalse(JitInefficiencyCheck.concatenationSurvivesValueOf(nullOperand), "\"\" + null is the text null");
		assertFalse(JitInefficiencyCheck.concatenationSurvivesValueOf(charArray), "\"\" + char[] is its toString");
		assertTrue(JitInefficiencyCheck.concatenationSurvivesValueOf(plain), "and an int operand is unaffected");
	}
}