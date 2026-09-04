package com.etk2000.checkstyle.inputs.preferspecificapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Container;
import java.io.File;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;

class InputSpecificApiClean {
	static {}

	{}

	void assertEqualsWithThreeNonLiterals() {
		assertEquals("a", "b", "msg");
	}

	void assertEqualsWithTwoNonLiterals() {
		assertEquals("a", "b");
	}

	void assertFalseAlready() {
		assertFalse(1 == 2);
	}

	void assertNotEqualsWithThreeNonLiterals() {
		assertNotEquals("a", "b", "msg");
	}

	void assertNotEqualsWithTwoNonLiterals() {
		assertNotEquals("a", "b");
	}

	void assertNotNullAlready() {
		assertNotNull(new Object());
	}

	void assertNotSameNonNull() {
		assertNotSame("a", "b");
	}

	void assertNotSameWithTrailingMessageTrue() {
		assertNotSame(true, Boolean.TRUE, "msg");
	}

	void assertNotSameWithTrue() {
		assertNotSame(true, Boolean.TRUE);
	}

	void assertNullAlready() {
		assertNull(null);
	}

	void assertSameNonNull() {
		assertSame("a", "a");
	}

	void assertSameWithFalse() {
		assertSame(false, Boolean.FALSE);
	}

	void assertSameWithTrailingMessageFalse() {
		assertSame(false, Boolean.FALSE, "msg");
	}

	void assertTrueAlready() {
		assertTrue(1 == 1);
	}

	void collectionsCopyOfAlreadyClean(List<String> list) {
		final var result = List.copyOf(list);
	}

	void collectionsEmptyListAlreadyClean() {
		final var list = List.of();
	}

	void collectionsSynchronizedList(List<String> list) {
		final var result = Collections.synchronizedList(list);
	}

	void collectToSet(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	void collectToUnmodifiableSet(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}

	void collectWithCustomCollector(List<String> list) {
		final var result = list.stream()
				.collect(Collectors.joining(", "));
	}

	void containerRemoveZero(Container container) {
		container.remove(0);
	}

	void containsAlready(String s) {
		if (s.contains("foo"))
			System.out.println("found");
	}

	void containsKeyAlready(Map<String, String> map) {
		if (map.containsKey("key"))
			System.out.println("found");
	}

	void containsValueAlready(Map<String, String> map) {
		if (map.containsValue("value"))
			System.out.println("found");
	}

	void defaultListModelRemoveSizeMinusOne(DefaultListModel<String> model) {
		model.remove(model.size() - 1);
	}

	void defaultListModelRemoveZero(DefaultListModel<String> model) {
		model.remove(0);
	}

	void entrySetContains(Map<String, String> map) {
		if (map.entrySet().contains(Map.entry("k", "v")))
			System.out.println("found");
	}

	void equalsNonEmpty(String s) {
		if (s.equals("hello"))
			System.out.println("hello");
	}

	void equalsNonLiteral(String s, String other) {
		if (s.equals(other))
			System.out.println("equal");
	}

	void equalsNullSafe(String s) {
		if ("".equals(s))
			System.out.println("empty");
	}

	void fileLengthNotEmpty(File file) {
		if (file.length() > 0)
			System.out.println("file has content");
	}

	void fileLengthZero(File file) {
		if (file.length() == 0)
			System.out.println("file is empty");
	}

	void forEachAlready(List<String> list) {
		list.forEach(System.out::println);
	}

	void getFirst(List<String> list) {
		System.out.println(list.getFirst());
	}

	void getLast(List<String> list) {
		System.out.println(list.getLast());
	}

	void getNonZeroIndex(List<String> list) {
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void indexOfCharListReceiver(List<String> names) {
		System.out.println(names.indexOf("x"));
	}

	void indexOfCharMultiUnicodeApostrophe(String s) {
		System.out.println(s.indexOf("\uuu0027"));
	}

	void indexOfCharStringBuilderReceiver(StringBuilder sb) {
		System.out.println(sb.indexOf("x"));
	}

	void indexOfCharUnicodeBackslashBeforeEscapeChar(String s) {
		System.out.println(s.indexOf("\u005cn"));
	}

	void indexOfCharUnicodeBackslashPair(String s) {
		System.out.println(s.indexOf("\u005c\u005c"));
	}

	void indexOfCharVariant(String s) {
		if (s.indexOf('c') != -1)
			System.out.println("found char");
	}

	void indexOfEqualsZero(String s) {
		if (s.indexOf("foo") == 0)
			System.out.println("starts with");
	}

	void indexOfGreaterThanZero(String s) {
		if (s.indexOf("foo") > 0)
			System.out.println("found after first char");
	}

	void indexOfLessEqualZero(String s) {
		if (s.indexOf("foo") <= 0)
			System.out.println("at start or not found");
	}

	void indexOfNonLiteralArg(String s, String target) {
		if (s.indexOf(target) != -1)
			System.out.println("found variable");
	}

	void indexOfNotEqualZero(String s) {
		if (s.indexOf("foo") != 0)
			System.out.println("not at start");
	}

	void indexOfTwoArgs(String s) {
		if (s.indexOf("foo", 5) != -1)
			System.out.println("found after index 5");
	}

	void isEmptyAlready(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}

	void keySetSize(Map<String, String> map) {
		System.out.println(map.keySet().size());
	}

	void lengthComparisonNotEmpty(StringBuilder sb) {
		if (sb.length() > 1)
			System.out.println("more than one char");
	}

	void listSortAlready(List<String> list) {
		list.sort(Comparator.naturalOrder());
	}

	void listSortNull(List<String> list) {
		list.sort(null);
	}

	void mapRemoveZero(Map<Integer, String> map) {
		map.remove(0);
	}

	void parallelStreamCount(List<String> list) {
		final var count = list.parallelStream().count();
	}

	void parallelStreamForEach(List<String> list) {
		list.parallelStream().forEach(System.out::println);
	}

	void removeFirst(List<String> list) {
		list.removeFirst();
	}

	void removeFirstDequeReceiver(Deque<Integer> pending) {
		pending.remove(0);
	}

	void removeLast(List<String> list) {
		list.removeLast();
	}

	void removeLastDequeReceiver(Deque<Integer> pending) {
		pending.remove(pending.size() - 1);
	}

	void replaceAllWithRegex(String s) {
		final var result = s.replaceAll("foo.*bar", "baz");
	}

	void replaceAllWithVariable(String s, String pattern) {
		final var result = s.replaceAll(pattern, "baz");
	}

	void replaceAlready(String s) {
		final var result = s.replace("foo", "bar");
	}

	void sequentialAccess(List<String> list) {
		System.out.println(list.get(0));
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void sequentialAccessFromEnd(List<String> list) {
		System.out.println(list.get(list.size() - 1));
		System.out.println(list.get(list.size() - 2));
	}

	void sequentialRemove(List<String> list) {
		list.remove(0);
		list.remove(1);
	}

	void sequentialRemoveFromEnd(List<String> list) {
		list.remove(list.size() - 1);
		list.remove(list.size() - 2);
	}

	void sizeComparisonNotEmpty(List<String> list) {
		if (list.size() > 1)
			System.out.println("more than one");
	}

	void sizeEqualsTwo(List<String> list) {
		if (list.size() == 2)
			System.out.println("pair");
	}

	void streamFilterCount(List<String> list) {
		final var count = list.stream().filter(s -> !s.isEmpty()).count();
	}

	void streamFilterFindFirstIsPresent(List<String> list) {
		if (list.stream().filter(s -> !s.isEmpty()).findFirst().isPresent())
			System.out.println("found");
	}

	void streamFilterForEach(List<String> list) {
		list.stream().filter(s -> !s.isEmpty()).forEach(System.out::println);
	}

	void stringFormatBarePercent() {
		final var s = String.format("%");
	}

	void stringFormatEscapedPercent() {
		final var s = String.format("50%% off");
	}

	void stringFormatMethodCallWithArgs(Object obj, String name) {
		final var s = String.format(obj.toString(), name);
	}

	void stringFormatNonLiteral(String fmt, String name) {
		final var s = String.format(fmt, name);
	}

	void stringFormatOctalEscapedPercent() {
		final var s = String.format("50\045 off");
	}

	void stringFormatPercentN() {
		final var s = String.format("%n");
	}

	void stringFormatTextBlock() {
		final var s = String.format(
				"""
				plain"""
		);
	}

	void stringFormatUnicodeEscapedBackslashOctalPercent() {
		final var s = String.format("\u005c045");
	}

	void stringFormatUnicodeEscapedPercent() {
		final var s = String.format("\u0025");
	}

	void stringIsEmptyWithoutTrim(String s) {
		if (s.isEmpty())
			System.out.println("empty without trim");
	}

	void stripAlone(String s) {
		final var stripped = s.strip();
	}

	void stripEqualsNotEmpty(String s) {
		if (s.strip().equals("x"))
			System.out.println("equals x");
	}

	void stripLeadingIsEmpty(String s) {
		if (s.stripLeading().isEmpty())
			System.out.println("leading stripped empty");
	}

	void stripLengthEqualsOne(String s) {
		if (s.strip().length() == 1)
			System.out.println("single char");
	}

	void stripLengthGreaterThanOne(String s) {
		if (s.strip().length() > 1)
			System.out.println("more than one char after strip");
	}

	void stripTrailingIsEmpty(String s) {
		if (s.stripTrailing().isEmpty())
			System.out.println("trailing stripped empty");
	}

	void toArrayAlreadyCorrect(List<String> list) {
		final var arr = list.toArray(String[]::new);
	}

	void toArrayMultiDimensional(List<String[]> list) {
		final var arr = list.toArray(new String[0][]);
	}

	void toArrayNoArg(List<String> list) {
		final var arr = list.toArray();
	}

	void toArrayNonZeroSize(List<String> list) {
		final var arr = list.toArray(new String[10]);
	}

	void toArraySizedAllocation(List<String> list) {
		final var arr = list.toArray(new String[list.size()]);
	}

	void toListDirect(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}

	void trimAlone(String s) {
		final var trimmed = s.trim();
	}

	void trimEqualsNotEmpty(String s) {
		if (s.trim().equals("x"))
			System.out.println("equals x");
	}

	void trimIsBlankAlready(String s) {
		if (s.isBlank())
			System.out.println("already using isBlank");
	}

	void trimLengthEqualsOne(String s) {
		if (s.trim().length() == 1)
			System.out.println("single char");
	}

	void trimLengthGreaterThanOne(String s) {
		if (s.trim().length() > 1)
			System.out.println("more than one char after trim");
	}
}

class InputSpecificApiReflectionClean {
	static final Map<Integer, String> SHARED = Map.of();

	List<String> listField = List.of();

	Map<Integer, String> mapField = Map.of();

	Map<Integer, String>[] mapRows;

	void arrayElementReceiverGetZero() {
		System.out.println(mapRows[0].get(0));
	}

	void chainedCallResolvesToMap(Map<Integer, String> map) {
		System.out.println(Collections.synchronizedMap(map).get(0));
	}

	void lambdaParamMapGetZero(List<Map<Integer, String>> maps) {
		maps.forEach((Map<Integer, String> m) -> System.out.println(m.get(0)));
	}

	void mapFieldGetZero() {
		System.out.println(mapField.get(0));
	}

	void mapParamGetZero(Map<Integer, String> map) {
		System.out.println(map.get(0));
	}

	void objectEqualsEmpty(Object obj) {
		if (obj.equals(""))
			System.out.println("empty");
	}

	void shadowingCatchGetZero() {
		try {
			System.out.println("x");
		}
		catch (IllegalStateException listField) {
			System.out.println(listField.get(0));
		}
	}

	void shadowingLambdaParamGetZero(List<Map<Integer, String>> maps) {
		maps.forEach((Map<Integer, String> listField) -> System.out.println(listField.get(0)));
	}

	void staticFieldReceiverGetZero() {
		System.out.println(InputSpecificApiReflectionClean.SHARED.get(0));
	}

	void thisQualifiedMapFieldGetZero(String mapField) {
		System.out.println(this.mapField.get(0));
	}
}

class InputSpecificApiResourceReceiverClean {
	void sameFileResourceReceiverGetLast() throws Exception {
		try (var buffer = new InputSpecificApiResourceBuffer()) {
			System.out.println(buffer.get(buffer.size() - 1));
		}
	}

	void sameFileResourceReceiverGetZero() throws Exception {
		try (var buffer = new InputSpecificApiResourceBuffer()) {
			System.out.println(buffer.get(0));
		}
	}

	void sameFileResourceReceiverIndexOf() throws Exception {
		try (var buffer = new InputSpecificApiResourceBuffer()) {
			if (buffer.indexOf("xy") >= 0)
				System.out.println();
		}
	}

	void sameFileResourceReceiverRemoveLast() throws Exception {
		try (var buffer = new InputSpecificApiResourceBuffer()) {
			buffer.remove(buffer.size() - 1);
		}
	}

	void sameFileResourceReceiverRemoveZero() throws Exception {
		try (var buffer = new InputSpecificApiResourceBuffer()) {
			buffer.remove(0);
		}
	}
}

class InputSpecificApiResourceBuffer implements AutoCloseable {
	@Override
	public void close() {
	}

	Object get(int index) {
		return null;
	}

	int indexOf(String needle) {
		return needle.length();
	}

	boolean remove(int index) {
		return index >= 0;
	}

	int size() {
		return 0;
	}
}

class InputSpecificApiInheritedFieldBase {
	Map<Integer, String> inheritedMap = Map.of();
}

class InputSpecificApiInheritedFieldClean extends InputSpecificApiInheritedFieldBase {
	void inheritedMapFieldGetZero() {
		System.out.println(inheritedMap.get(0));
	}
}

class InputSpecificApiReplaceAllReplacementClean {
	static class TextReplacer {
		String replaceAll(String regex, String replacement) {
			return regex + replacement;
		}
	}

	static final String REPLACEMENT = "y";

	void castReplacement(String s, Object o) {
		System.out.println(s.replaceAll("x", (String) o));
	}

	void concatenationNeedingAnEscape(String s) {
		System.out.println(s.replaceAll("x", "a" + "\\$"));
	}

	void concatenationWithVariable(String s, String y) {
		System.out.println(s.replaceAll("x", "a" + y));
	}

	void constantReplacement(String s) {
		System.out.println(s.replaceAll("x", REPLACEMENT));
	}

	void dollarBeforeLetter(String s) {
		System.out.println(s.replaceAll("x", "$a"));
	}

	void dollarBeforeNamedGroup(String s) {
		System.out.println(s.replaceAll("x", "${name}"));
	}

	void dollarBeforeOpenBrace(String s) {
		System.out.println(s.replaceAll("x", "${"));
	}

	void dollarGroupNine(String s) {
		System.out.println(s.replaceAll("x", "$9"));
	}

	void dollarGroupOne(String s) {
		System.out.println(s.replaceAll("x", "$1"));
	}

	void dollarGroupTen(String s) {
		System.out.println(s.replaceAll("x", "$10"));
	}

	void dollarOnly(String s) {
		System.out.println(s.replaceAll("x", "$"));
	}

	void doubleDollar(String s) {
		System.out.println(s.replaceAll("x", "$$"));
	}

	void escapedBackslashBeforeDollar(String s) {
		System.out.println(s.replaceAll("x", "\\\\$"));
	}

	void escapedDollarThenGroup(String s) {
		System.out.println(s.replaceAll("x", "\\$$1"));
	}

	void loneBackslash(String s) {
		System.out.println(s.replaceAll("x", "\\"));
	}

	void methodCallReplacement(String s, int n) {
		System.out.println(s.replaceAll("x", String.valueOf(n)));
	}

	void nullReplacement(String s) {
		System.out.println(s.replaceAll("x", null));
	}

	void octalBackslash(String s) {
		System.out.println(s.replaceAll("x", "\134"));
	}

	void octalDollar(String s) {
		System.out.println(s.replaceAll("x", "\44"));
	}

	void parenthesizedReplacement(String s) {
		System.out.println(s.replaceAll("x", ("y")));
	}

	void ternaryReplacement(String s, boolean flag) {
		System.out.println(s.replaceAll("x", flag ? "a" : "b"));
	}

	String textBlockNeedingAnEscape(String s) {
		return s.replaceAll(
				"x",
				"""
				\\$"""
		);
	}

	String textBlockWholeMatch(String s) {
		return s.replaceAll(
				"x",
				"""
				[$0]"""
		);
	}

	void trailingBackslash(String s) {
		System.out.println(s.replaceAll("x", "a\\"));
	}

	void trailingDollar(String s) {
		System.out.println(s.replaceAll("x", "a$"));
	}

	void unicodeBackslash(String s) {
		System.out.println(s.replaceAll("x", "\u005c\u005c"));
	}

	void unicodeDollar(String s) {
		System.out.println(s.replaceAll("x", "\u0024"));
	}

	void unicodeDollarGroupOne(String s) {
		System.out.println(s.replaceAll("x", "\u00241"));
	}

	void userTypeReceiver(TextReplacer replacer) {
		System.out.println(replacer.replaceAll("x", "y"));
	}

	void variableReplacement(String s, String y) {
		System.out.println(s.replaceAll("x", y));
	}
}

class InputSpecificApiTrimFormsClean {
	static class Holder {
		String name;
	}

	static class PaddedText {
		int length() {
			return 0;
		}

		PaddedText trim() {
			return this;
		}
	}

	Holder obj;

	String getText() {
		return "";
	}

	boolean literalReceiverTrimLength() {
		return "  ".trim().length() == 0;
	}

	boolean parenReceiverTrimLength(String a, String b) {
		return (a + b).trim().length() == 0;
	}

	boolean sameFileTypeTrimLength(PaddedText text) {
		return text.trim().length() == 0;
	}

	boolean ternaryReceiverTrimLength(boolean flag, String a, String b) {
		return (flag ? a : b).trim().length() == 0;
	}

	boolean trimIsEmpty(String s) {
		return s.trim().isEmpty();
	}

	boolean trimIsEmptyDottedReceiver() {
		return obj.name.trim().isEmpty();
	}

	boolean trimIsEmptyMethodReceiver() {
		return getText().trim().isEmpty();
	}

	boolean trimIsEmptyNegated(String s) {
		return !s.trim().isEmpty();
	}

	boolean trimIsEmptyPatternInString(String s) {
		return ".trim().isEmpty()".isEmpty() || s.trim().isEmpty();
	}

	boolean trimLengthGreaterThanZeroDottedReceiver() {
		return obj.name.trim().length() > 0;
	}

	boolean trimLengthGreaterThanZeroMethodReceiver() {
		return getText().trim().length() > 0;
	}

	boolean trimLengthReversedMethodReceiver() {
		return 0 == getText().trim().length();
	}
}

class InputSpecificApiEqualsEmptyNonStringReceiverClean {
	static class TextSequence implements CharSequence {
		@Override
		public char charAt(int index) {
			return 'a';
		}

		@Override
		public int length() {
			return 0;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this;
		}
	}

	final StringBuilder builder = new StringBuilder();

	String name = "";

	void castReceiverEqualsEmpty(Object o) {
		System.out.println(((StringBuilder) o).equals(""));
	}

	void charBufferEqualsEmpty(CharBuffer buffer) {
		System.out.println(buffer.equals(""));
	}

	void charSequenceEqualsEmpty(CharSequence cs) {
		System.out.println(cs.equals(""));
	}

	void collectionEqualsEmpty(Collection<String> values) {
		System.out.println(values.equals(""));
	}

	void dottedReceiverEqualsEmpty(InputSpecificApiEqualsEmptyNonStringReceiverClean other) {
		System.out.println(other.name.equals(""));
	}

	String getName() {
		return name;
	}

	void listEqualsEmpty(List<String> values) {
		System.out.println(values.equals(""));
	}

	void mapEqualsEmpty(Map<String, String> values) {
		System.out.println(values.equals(""));
	}

	void optionalEqualsEmpty(Optional<String> value) {
		System.out.println(value.equals(""));
	}

	void qualifiedStringBuilderEqualsEmpty(java.lang.StringBuilder sb) {
		System.out.println(sb.equals(""));
	}

	void sameFileCharSequenceEqualsEmpty(TextSequence text) {
		System.out.println(text.equals(""));
	}

	void stringBufferEqualsEmpty(StringBuffer sb) {
		System.out.println(sb.equals(""));
	}

	void stringBuilderAppendChainEqualsEmpty(StringBuilder sb) {
		System.out.println(sb.append("x").equals(""));
	}

	void stringBuilderEqualsEmpty(StringBuilder sb) {
		System.out.println(sb.equals(""));
	}

	void thisStringBuilderFieldEqualsEmpty(StringBuilder builder) {
		System.out.println(this.builder.equals(""));
	}

	<T extends CharSequence> void typeVariableEqualsEmpty(T text) {
		System.out.println(text.equals(""));
	}

	void unqualifiedCallReceiverEqualsEmpty() {
		System.out.println(getName().equals(""));
	}
}

/**
 * Single-argument {@code String.format} shapes the check stops short of: the format string is
 * still processed when there is nothing to substitute, and none of these arguments can be read
 * well enough to show it carries no conversion.
 */
class InputSpecificApiFormatStripNonLiteralClean {
	String msg;

	void formatSingleCast(Object obj) {
		final var s = String.format((String) obj);
	}

	void formatSingleMethodCall(Object obj) {
		final var s = String.format(obj.toString());
	}

	void formatSingleVariable(String fmt) {
		final var s = String.format(fmt);
	}

	void formatStripAssignmentRightHandSide(String a, String b) {
		msg = String.format(a + b);
	}

	String formatStripParenthesizesNonPrimary(String a, String b) {
		return String.format(a + b).trim();
	}
}

/**
 * A receiver that is itself an unqualified call carries no type at all, so the rules that confirm
 * an erased signature before rewriting have nothing to confirm against and stay silent. Declaring
 * {@code getList} and {@code getName} here would not change that: the resolver reads a chain
 * through a dotted receiver, and a bare call has none.
 */
class InputSpecificApiUnwrittenReceiverClean {
	void chainedCallIndexOfChar() {
		System.out.println(getName().indexOf("x"));
	}

	void chainedCallRemoveFirst() {
		getList().remove(0);
	}

	void chainedCallRemoveLast() {
		getList().remove(getList().size() - 1);
	}
}

/**
 * Shapes the check deliberately stays silent on, migrated from fragments when the fixer stopped
 * matching text: every literal notation other than exactly "0"/"1" is a non-match, a negation
 * binds tighter than the comparison, and a receiver whose type cannot be confirmed is refused.
 */
class InputSpecificApiLiteralBoundaryClean {
	static class Holder {
		String name;
	}

	Holder obj;

	String foo() {
		return "";
	}

	String getStr() {
		return "";
	}

	void lengthAlreadyNegated(String str) {
		if (!str.length() > 0)
			return;
	}

	void lengthComplexReceiver() {
		if (getStr().length() > 0)
			return;
	}

	void lengthDottedReceiver() {
		if (obj.name.length() > 0)
			return;
	}

	void lengthEqualsZeroFollowedByLetter(String s) {
		if (s.length() == 0xF)
			return;
	}

	void lengthLessThanFollowedByDigit(String s) {
		if (s.length() < 10)
			return;
	}

	void lengthLessThanOneFollowedByDecimal(String s) {
		if (s.length() < 1.5)
			return;
	}

	void lengthLessThanOneFollowedByUnderscore(String s) {
		if (s.length() < 1_0)
			return;
	}

	void lengthReversedAfterDigitsRejected(String s) {
		if (300 == s.length())
			return;
	}

	void lengthReversedMethodReceiver() {
		if (0 == foo().length())
			return;
	}

	void stripLengthAlreadyNegated(String s) {
		if (!s.strip().length() > 0)
			return;
	}

	void stripLengthEqualsZeroFollowedByLetter(String s) {
		if (s.strip().length() == 0xF)
			return;
	}

	void stripLengthLessThanFollowedByDigit(String s) {
		if (s.strip().length() < 10)
			return;
	}

	void stripLengthLessThanOneFollowedByDecimal(String s) {
		if (s.strip().length() < 1.5)
			return;
	}

	void stripLengthLessThanOneFollowedByUnderscore(String s) {
		if (s.strip().length() < 1_0)
			return;
	}

	void stripLengthReversedAfterDigitsRejected(String s) {
		if (300 == s.strip().length())
			return;
	}

	void trimLengthAlreadyNegated(String s) {
		if (!s.trim().length() > 0)
			return;
	}

	void trimLengthEqualsZeroFollowedByLetter(String s) {
		if (s.trim().length() == 0xF)
			return;
	}

	void trimLengthLessThanFollowedByDigit(String s) {
		if (s.trim().length() < 10)
			return;
	}

	void trimLengthLessThanOneFollowedByDecimal(String s) {
		if (s.trim().length() < 1.5)
			return;
	}

	void trimLengthLessThanOneFollowedByUnderscore(String s) {
		if (s.trim().length() < 1_0)
			return;
	}

	void trimLengthReversedAfterDigitsRejected(String s) {
		if (300 == s.trim().length())
			return;
	}
}