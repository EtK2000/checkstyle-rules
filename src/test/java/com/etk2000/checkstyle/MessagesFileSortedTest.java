package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessagesFileSortedTest {
	private record MsgParseResult(int endIndex, @Nonnull String literal) {}

	@Nonnull
	private static List<String> findMsgConstantLiterals(@Nonnull String source) {
		final var literals = new ArrayList<String>();
		final var len = source.length();
		var i = 0;
		while (i < len) {
			final var c = source.charAt(i);
			if (c == '"' && i + 2 < len && source.charAt(i + 1) == '"' && source.charAt(i + 2) == '"') {
				i = skipTextBlock(source, i);
				continue;
			}
			if (c == '"') {
				i = skipQuotedSequence(source, i, '"');
				continue;
			}
			if (c == '\'') {
				i = skipQuotedSequence(source, i, '\'');
				continue;
			}
			if (c == 'M' && (i == 0 || !Character.isJavaIdentifierPart(source.charAt(i - 1)))
					&& i + 4 < len && source.charAt(i + 1) == 'S' && source.charAt(i + 2) == 'G' && source.charAt(i + 3) == '_') {
				var end = i + 4;
				while (end < len && (Character.isUpperCase(source.charAt(end)) || Character.isDigit(source.charAt(end)) || source.charAt(end) == '_'))
					++end;
				if (end > i + 4) {
					final var parsed = tryParseMsgValue(source, end);
					if (parsed != null) {
						literals.add(parsed.literal());
						i = parsed.endIndex();
						continue;
					}
				}
				i = end;
				continue;
			}
			++i;
		}
		return literals;
	}

	@Nonnull
	private static List<String> findUnreferencedKeys(@Nonnull List<String> keys, @Nonnull List<String> sources) {
		final var stripped = sources.stream().map(JavaSourceUtil::stripJavaComments).toList();
		final var unused = new ArrayList<String>();
		for (var key : keys) {
			final var literal = '"' + key + '"';
			if (stripped.stream().noneMatch(content -> content.contains(literal)))
				unused.add(key);
		}
		return unused;
	}

	@Nonnull
	private static List<String> findUnresolvedMsgLiterals(@Nonnull Set<String> keys, @Nonnull List<String> sources) {
		final var unresolved = new ArrayList<String>();
		for (var src : sources) {
			for (var literal : findMsgConstantLiterals(JavaSourceUtil.stripJavaComments(src))) {
				if (!keys.contains(literal))
					unresolved.add(literal);
			}
		}
		return unresolved;
	}

	private static int findUnsortedKeyIndex(@Nonnull List<String> keys) {
		for (var i = 1; i < keys.size(); ++i) {
			if (keys.get(i).compareTo(keys.get(i - 1)) < 0)
				return i;
		}
		return -1;
	}

	@Nonnull
	private static List<String> readMessageKeys() throws IOException {
		try (var reader = new BufferedReader(new InputStreamReader(
				MessagesFileSortedTest.class.getResourceAsStream("/com/etk2000/checkstyle/messages.properties"),
				StandardCharsets.UTF_8
		))) {
			return readMessageKeys(reader);
		}
	}

	@Nonnull
	private static List<String> readMessageKeys(@Nonnull BufferedReader reader) throws IOException {
		final var keys = new ArrayList<String>();
		var lineNum = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			++lineNum;
			if (line.isBlank() || line.startsWith("#"))
				continue;
			if (line.endsWith("\\"))
				throw new IllegalStateException("messages.properties continuation line not supported (line " + lineNum + ")");

			final var eqIndex = line.indexOf('=');
			final var colonIndex = line.indexOf(':');
			if (colonIndex >= 0 && (eqIndex < 0 || colonIndex < eqIndex))
				throw new IllegalStateException("messages.properties uses '=' separator only, found ':' first (line " + lineNum + ")");
			if (eqIndex <= 0)
				continue;

			final var key = line.substring(0, eqIndex);
			if (key.indexOf('\\') >= 0)
				throw new IllegalStateException("messages.properties keys must not contain backslash escapes (line " + lineNum + ")");

			keys.add(key);
		}
		return keys;
	}

	private static int skipQuotedSequence(@Nonnull String source, int start, char quote) {
		final var len = source.length();
		var i = start + 1;
		while (i < len) {
			final var c = source.charAt(i);
			if (c == '\\' && i + 1 < len) {
				i += 2;
				continue;
			}
			++i;
			if (c == quote || c == '\n')
				return i;
		}
		return i;
	}

	private static int skipTextBlock(@Nonnull String source, int start) {
		final var len = source.length();
		var i = start + 3;
		while (i + 2 < len) {
			if (source.charAt(i) == '"' && source.charAt(i + 1) == '"' && source.charAt(i + 2) == '"')
				return i + 3;
			++i;
		}
		return len;
	}

	@Nullable
	private static MsgParseResult tryParseMsgValue(@Nonnull String source, int afterIdent) {
		final var len = source.length();
		var pos = afterIdent;
		while (pos < len && Character.isWhitespace(source.charAt(pos)))
			++pos;
		if (pos >= len || source.charAt(pos) != '=')
			return null;
		if (pos + 1 < len && source.charAt(pos + 1) == '=')
			return null;

		do ++pos;
		while (pos < len && Character.isWhitespace(source.charAt(pos)));
		if (pos >= len || source.charAt(pos) != '"')
			return null;

		final var combined = new StringBuilder();
		while (pos < len && source.charAt(pos) == '"') {
			if (pos + 2 < len && source.charAt(pos + 1) == '"' && source.charAt(pos + 2) == '"')
				return null;

			++pos;
			while (pos < len && source.charAt(pos) != '"') {
				if (source.charAt(pos) == '\\' && pos + 1 < len)
					pos += 2;
				else {
					combined.append(source.charAt(pos));
					++pos;
				}
			}
			if (pos >= len)
				return null;
			++pos;

			var probe = pos;
			while (probe < len && Character.isWhitespace(source.charAt(probe)))
				++probe;
			if (probe >= len || source.charAt(probe) != '+')
				break;

			pos = probe + 1;
			while (pos < len && Character.isWhitespace(source.charAt(pos)))
				++pos;
			if (pos >= len || source.charAt(pos) != '"')
				return null;
		}
		return new MsgParseResult(pos, combined.toString());
	}

	@Test
	public void testEveryMessageKeyIsReferenced() throws Exception {
		final var unused = findUnreferencedKeys(
				readMessageKeys(),
				JavaSourceUtil.walkJavaSources(Path.of("src/main/java"))
		);
		if (!unused.isEmpty())
			fail("Unused message keys in messages.properties: " + unused);
	}

	@Test
	public void testEveryMsgConstantResolvesToAKey() throws Exception {
		final var unresolved = findUnresolvedMsgLiterals(
				new HashSet<>(readMessageKeys()),
				JavaSourceUtil.walkJavaSources(Path.of("src/main/java"))
		);
		if (!unresolved.isEmpty())
			fail("MSG_* constants reference missing keys: " + unresolved);
	}

	@Test
	public void testFindMsgConstantLiteralsHandlesConcat() {
		assertEquals(List.of("foo.bar"), findMsgConstantLiterals("String MSG_X = \"foo.\" + \"bar\";"));
		assertEquals(List.of("abc"), findMsgConstantLiterals("String MSG_X = \"a\" + \"b\" + \"c\";"));
		assertEquals(List.of("ab"), findMsgConstantLiterals("String MSG_X = \"a\" + \"\" + \"b\";"));
		assertEquals(List.of("ab"), findMsgConstantLiterals("String MSG_X = \"a\"+\"b\";"));
	}

	@Test
	public void testFindMsgConstantLiteralsHandlesEscapeAtEof() {
		assertTrue(findMsgConstantLiterals("String MSG_X = \"a\\").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsIgnoresBareMsgPrefix() {
		assertTrue(findMsgConstantLiterals("MSG_ = \"foo\";").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsIgnoresEqualityComparison() {
		assertTrue(findMsgConstantLiterals("if (MSG_X == \"foo\") { }").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsIgnoresInsideStringLiteral() {
		assertTrue(findMsgConstantLiterals("String s = \"MSG_X = \\\"foo\\\"\";").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsIgnoresMsgUsageWithoutAssignment() {
		assertTrue(findMsgConstantLiterals("log(MSG_KEY, x);").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsParsesSimpleDeclaration() {
		assertEquals(List.of("foo.bar"), findMsgConstantLiterals("String MSG_X = \"foo.bar\";"));
		assertEquals(List.of("a", "b"), findMsgConstantLiterals("String MSG_A = \"a\"; String MSG_B = \"b\";"));
		assertEquals(List.of(""), findMsgConstantLiterals("String MSG_X = \"\";"));
		assertEquals(List.of("foo"), findMsgConstantLiterals("String MSG_X=\"foo\";"));
	}

	@Test
	public void testFindMsgConstantLiteralsRecoversFromUnterminatedString() {
		assertEquals(
				List.of("foo"),
				findMsgConstantLiterals("String s = \"unterminated\nString MSG_X = \"foo\";")
		);
	}

	@Test
	public void testFindMsgConstantLiteralsRequiresIdentifierBoundary() {
		assertTrue(findMsgConstantLiterals("String XMSG_X = \"foo\";").isEmpty());
		assertEquals(List.of("foo"), findMsgConstantLiterals(";MSG_X = \"foo\";"));
		assertEquals(List.of("foo"), findMsgConstantLiterals("MSG_X = \"foo\";"));
	}

	@Test
	public void testFindMsgConstantLiteralsRequiresUppercaseIdentifier() {
		assertTrue(findMsgConstantLiterals("String MSG_Foo = \"foo\";").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsCharLiteral() {
		assertEquals(
				List.of("foo"),
				findMsgConstantLiterals("char c = '\"'; String MSG_X = \"foo\";")
		);
		assertTrue(findMsgConstantLiterals("char c = 'M';").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsConcatWithNonLiteral() {
		assertTrue(findMsgConstantLiterals("String MSG_X = \"a\" + CONST + \"b\";").isEmpty());
		assertTrue(findMsgConstantLiterals("String MSG_X = \"a\" +").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsNonStringAssignment() {
		assertTrue(findMsgConstantLiterals("String MSG_X = OTHER_CONST;").isEmpty());
		assertTrue(findMsgConstantLiterals("String MSG_X = ;").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsParenthesizedConcat() {
		assertTrue(findMsgConstantLiterals("String MSG_X = (\"a\" + \"b\");").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsTextBlockInConcat() {
		assertTrue(findMsgConstantLiterals("String MSG_X = \"a\" + \"\"\"b\"\"\";").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsTextBlockValue() {
		assertTrue(findMsgConstantLiterals("String MSG_X = \"\"\"foo\"\"\";").isEmpty());
	}

	@Test
	public void testFindMsgConstantLiteralsSkipsTopLevelTextBlock() {
		assertEquals(
				List.of("foo"),
				findMsgConstantLiterals("String s = \"\"\"\nMSG_X = \"bogus\"\n\"\"\"; String MSG_Y = \"foo\";")
		);
	}

	@Test
	public void testFindUnreferencedKeysDetectsOrphan() {
		assertEquals(
				List.of("orphan"),
				findUnreferencedKeys(List.of("foo", "orphan"), List.of("class T { String s = \"foo\"; }"))
		);
	}

	@Test
	public void testFindUnreferencedKeysIgnoresKeyOnlyInComment() {
		assertEquals(
				List.of("foo"),
				findUnreferencedKeys(List.of("foo"), List.of("class T { /* see \"foo\" */ }"))
		);
	}

	@Test
	public void testFindUnreferencedKeysMatchesDirectLogCall() {
		assertTrue(findUnreferencedKeys(
				List.of("foo"),
				List.of("class T { void m() { log(\"foo\"); } }")
		).isEmpty());
	}

	@Test
	public void testFindUnresolvedMsgLiteralsDetectsMissing() {
		assertEquals(
				List.of("missing"),
				findUnresolvedMsgLiterals(
						Set.of("present"),
						List.of("class T { static final String MSG_X = \"missing\"; }")
				)
		);
	}

	@Test
	public void testFindUnresolvedMsgLiteralsIgnoresCommentedDeclaration() {
		assertTrue(findUnresolvedMsgLiterals(
				Set.of(),
				List.of("class T { /* String MSG_OLD = \"obsolete\"; */ }")
		).isEmpty());
	}

	@CsvSource({
			"'a;b;c', -1",
			"'a', -1",
			"'b;a', 1",
			"'a;a;b', -1",
			"'a;b;a', 2"
	})
	@ParameterizedTest
	public void testFindUnsortedKeyIndex(String csvKeys, int expected) {
		assertEquals(expected, findUnsortedKeyIndex(List.of(csvKeys.split(";"))));
	}

	@Test
	public void testMessagesAreSortedAlphabetically() throws Exception {
		final var keys = readMessageKeys();
		final var idx = findUnsortedKeyIndex(keys);
		if (idx >= 0)
			fail("messages.properties is not sorted: '" + keys.get(idx) + "' must appear before '" + keys.get(idx - 1) + "'");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"\n",
			"# header",
			"#noSpaceComment",
			"   ",
			"=val",
			"nokey",
			"# a=1\n# b=2"
	})
	public void testReadMessageKeysIgnoresNonKeyLines(String input) throws Exception {
		assertTrue(
				readMessageKeys(new BufferedReader(new StringReader(input))).isEmpty(),
				"expected no keys for: " + input
		);
	}

	@Test
	public void testReadMessageKeysParsesAcceptedShapes() throws Exception {
		assertEquals(List.of("a"), readMessageKeys(new BufferedReader(new StringReader("a=v"))));
		assertEquals(List.of("a", "b"), readMessageKeys(new BufferedReader(new StringReader("a=1\nb=2"))));
		assertEquals(List.of("foo.bar"), readMessageKeys(new BufferedReader(new StringReader("foo.bar="))));
		assertEquals(List.of("a", "b"), readMessageKeys(new BufferedReader(new StringReader("a=1\n\n# comment\nb=2"))));
		assertEquals(List.of("a"), readMessageKeys(new BufferedReader(new StringReader("a=b=c"))));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"key=val\\",
			"key:val",
			"k\\:y=val"
	})
	public void testReadMessageKeysRejectsUnsupportedSyntax(String input) {
		assertThrows(
				IllegalStateException.class,
				() -> readMessageKeys(new BufferedReader(new StringReader(input)))
		);
	}
}