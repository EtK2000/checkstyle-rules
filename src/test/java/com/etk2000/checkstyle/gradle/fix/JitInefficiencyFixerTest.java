package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.TopLevelScan;
import com.etk2000.checkstyle.TopLevelScan.Brackets;
import com.etk2000.checkstyle.TopLevelScan.Underflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class JitInefficiencyFixerTest {
	private static final String TOPIC = "jitinefficiency";

	private static final TopLevelScan ASSIGNMENT_SCAN = new TopLevelScan(Brackets.ALL, Underflow.CLAMP);

	private final CheckstyleFixer fixer = new JitInefficiencyFixer();

	@Test
	public void anUnparseableBufferYieldsNoFix() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_array_lhs_classic_for_unparseable_header_bails");
	}

	@CsvSource(delimiter = '|', value = {
			"a = b|2",
			"= b|0",
			"a =|2",
			"a == b = c|7",
			"a != b = c|7",
			"a <= b = c|7",
			"a >= b = c|7",
			"a >>= b|-1",
			"a <<= b|-1",
			"a >>>= b|-1",
			"a += b|3",
			"a -= b|3",
			"a *= b|3",
			"a /= b|3",
			"a %= b|3",
			"a &= b|3",
			"'a |= b'|3",
			"a ^= b|3",
			"a == b|-1",
			"a != b|-1",
			"a <= b|-1",
			"a >= b|-1",
			"f(a = b)|-1",
			"a = b, c = d|2"
	})
	@ParameterizedTest
	public void testAssignmentEqualsTarget(String text, int expected) {
		assertEquals(expected, ASSIGNMENT_SCAN.indexOf(text, JitInefficiencyFixer.ASSIGNMENT_EQUALS));
	}

	@CsvSource(delimiter = '|', value = {
			"a.b = c;|a.b|false|true",
			"a.b += c;|a.b|false|true",
			"a.b -= c;|a.b|false|true",
			"a.b *= c;|a.b|false|true",
			"a.b /= c;|a.b|false|true",
			"a.b %= c;|a.b|false|true",
			"a.b &= c;|a.b|false|true",
			"'a.b |= c;'|a.b|false|true",
			"a.b ^= c;|a.b|false|true",
			"x.a.b = c;|a.b|false|false",
			"a.b == c;|a.b|false|false",
			"a.b >= c;|a.b|false|false",
			"a.b <= c;|a.b|false|false",
			"a.b != c;|a.b|false|false",
			"a.b >>= c;|a.b|false|false",
			"a.bc = d;|a.b|false|false",
			"1a.b = c;|a.b|false|false",
			"a.b|a.b|false|false",
			"a.b = c;|a.b|true|false",
			"*/ a.b = c;|a.b|true|true"
	})
	@ParameterizedTest
	public void testContainsChainAssignment(String line, String chain, boolean inBlockComment, boolean expected) {
		final var entryState = new LexerState(inBlockComment, false);
		assertEquals(expected, JitInefficiencyFixer.containsChainAssignment(line, chain, entryState));
	}

	@CsvSource(delimiter = '|', value = {
			"k = c;|k|false|true",
			"k += c;|k|false|true",
			"k -= c;|k|false|true",
			"k *= c;|k|false|true",
			"k /= c;|k|false|true",
			"k %= c;|k|false|true",
			"k &= c;|k|false|true",
			"'k |= c;'|k|false|true",
			"k ^= c;|k|false|true",
			"k <<= c;|k|false|true",
			"k >>= c;|k|false|true",
			"k >>>= c;|k|false|true",
			"++k;|k|false|true",
			"--k;|k|false|true",
			"k++;|k|false|true",
			"k--;|k|false|true",
			"k == c;|k|false|false",
			"k >= c;|k|false|false",
			"k <= c;|k|false|false",
			"k != c;|k|false|false",
			"k << c;|k|false|false",
			"obj.k = c;|k|false|false",
			"kc = d;|k|false|false",
			"++kc;|k|false|false",
			"k|k|false|false",
			"k = c;|k|true|false",
			"*/ k = c;|k|true|true"
	})
	@ParameterizedTest
	public void testMutatesIdentifier(String line, String name, boolean inBlockComment, boolean expected) {
		final var entryState = new LexerState(inBlockComment, false);
		assertEquals(expected, JitInefficiencyFixer.mutatesIdentifier(line, name, entryState));
	}
}