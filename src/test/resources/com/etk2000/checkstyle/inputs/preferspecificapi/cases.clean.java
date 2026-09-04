package com.etk2000.checkstyle.inputs.preferspecificapi;

import static com.etk2000.checkstyle.testhelpers.ClasspathDocSource.*;
import static com.nonexistent.Headings.*;
import static com.nonexistent.Texts.label;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.etk2000.checkstyle.testhelpers.ClasspathDoc;
import com.etk2000.checkstyle.testhelpers.ClasspathDocField;
import com.etk2000.checkstyle.testhelpers.ClasspathDocProvider;
import com.etk2000.checkstyle.testhelpers.ClasspathDocSource;
import com.etk2000.checkstyle.testhelpers.ClasspathValueSource;

import java.awt.Container;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.nio.CharBuffer;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

class InputSpecificApiStripReceiverClean {
	static class Holder {
		String name;
	}

	static class Text {
		boolean isEmpty() {
			return true;
		}

		int length() {
			return 0;
		}

		Text replaceAll(String regex, String replacement) {
			return this;
		}

		Text strip() {
			return this;
		}
	}

	record TextRecord(Text text) {
		boolean blank() {
			return text().strip().isEmpty();
		}
	}

	static class TextSource {
		Text source() {
			return new Text();
		}
	}

	static class TextSourceUser extends TextSource {
		boolean blank() {
			return source().strip().isEmpty();
		}
	}

	static final Text BLANK = new Text();

	Holder obj;

	Text text;

	boolean constantStripIsEmpty() {
		return BLANK.strip().isEmpty();
	}

	boolean parenReceiverStripLength(String a, String b) {
		return (a + b).strip().length() == 0;
	}

	Text sameFileCallReceiverReplaceAll() {
		return text().replaceAll("x", "y");
	}

	boolean sameFileCallReceiverStripIsEmpty() {
		return text().strip().isEmpty();
	}

	Text sameFileDisagreeingOverloadsReplaceAll() {
		return text(1).replaceAll("x", "y");
	}

	boolean sameFileDisagreeingOverloadsStripIsEmpty() {
		return text(1).strip().isEmpty();
	}

	boolean sameFileTypeStripIsEmpty(Text value) {
		return value.strip().isEmpty();
	}

	boolean sameFileTypeStripIsEmptyNegated(Text value) {
		return !value.strip().isEmpty();
	}

	boolean sameFileTypeStripLengthComparisons(Text value) {
		return value.strip().length() == 0 || value.strip().length() != 0 || value.strip().length() > 0
				|| value.strip().length() >= 1 || value.strip().length() < 1 || value.strip().length() <= 0;
	}

	boolean sameFileTypeStripLengthReversedComparisons(Text value) {
		return 0 == value.strip().length() || 0 != value.strip().length() || 0 < value.strip().length()
				|| 1 <= value.strip().length() || 1 > value.strip().length() || 0 >= value.strip().length();
	}

	boolean sameFileVarLocalStripIsEmpty() {
		final var value = new Text();
		return value.strip().isEmpty();
	}

	Text text() {
		return text;
	}

	Text text(int index) {
		return text;
	}

	String text(String value) {
		return value;
	}

	boolean thisQualifiedSameFileCallReceiverStripIsEmpty(Text text) {
		return this.text().strip().isEmpty();
	}

	boolean thisQualifiedSameFileFieldStripIsEmpty(Text text) {
		return this.text.strip().isEmpty();
	}

	boolean unresolvableTypeStripIsEmpty(Unknown value) {
		return value.strip().isEmpty();
	}
}

/**
 * Call receivers not shown to return a String: a same-file method reached at an arity only its
 * varargs or receiver parameter admits, a super call reaching a same-file supertype's method, and a
 * call on a receiver of unknown type, whatever a same-named method in this file returns.
 */
class InputSpecificApiCallReceiverClean {
	static class Base {
		Template template() {
			return new Template();
		}

		Text text() {
			return new Text();
		}
	}

	static class Derived extends Base {
		Template superCallReplaceAll() {
			return super.template().replaceAll("x", "y");
		}

		boolean superCallStripIsEmpty() {
			return super.text().strip().isEmpty();
		}
	}

	static class Row {
		Template receiverParameterReplaceAll() {
			return template().replaceAll("x", "y");
		}

		boolean receiverParameterStripIsEmpty() {
			return text().strip().isEmpty();
		}

		Template template(Row this) {
			return new Template();
		}

		Text text(Row this) {
			return new Text();
		}
	}

	static class Template {
		Template replaceAll(String regex, String replacement) {
			return this;
		}
	}

	static class Text {
		boolean isEmpty() {
			return true;
		}

		Text strip() {
			return this;
		}
	}

	String foreignCallReplaceAllBesideASameFileMethod(Attribute attribute) {
		return attribute.getValue().replaceAll("x", "y");
	}

	boolean foreignCallStripIsEmptyBesideASameFileMethod(Holder holder) {
		return holder.source().strip().isEmpty();
	}

	Integer getValue() {
		return null;
	}

	void lambdaParameterCallReplaceAll(List<Source> sources) {
		sources.forEach(source -> System.out.println(source.template().replaceAll("x", "y")));
	}

	void lambdaParameterCallStripIsEmpty(List<Source> sources) {
		sources.forEach(source -> System.out.println(source.text().strip().isEmpty()));
	}

	Text source() {
		return new Text();
	}

	Template template(String... parts) {
		return new Template();
	}

	Text text(String... parts) {
		return new Text();
	}

	void varargsCallReplaceAll() {
		System.out.println(template().replaceAll("x", "y"));
		System.out.println(template("a").replaceAll("x", "y"));
		System.out.println(template("a", "b").replaceAll("x", "y"));
	}

	void varargsCallStripIsEmpty() {
		System.out.println(text().strip().isEmpty());
		System.out.println(text("a").strip().isEmpty());
		System.out.println(text("a", "b").strip().isEmpty());
	}
}

/**
 * Calls whose nearest same-file level returns a String while a supertype further up declares an
 * overload of the same arity that does not, which {@code text("k")} binds to, and a record accessor
 * that does not beside a varargs overload that does, which {@code text()} binds the accessor over.
 */
class InputSpecificApiOverloadFurtherUpClean {
	static class Base {
		Doc text(String key) {
			return new Doc();
		}
	}

	static class Derived extends Base {
		Doc nearestLevelReplaceAll() {
			return text("k").replaceAll("x", "y");
		}

		boolean nearestLevelStripIsEmpty() {
			return text("k").strip().isEmpty();
		}

		String text(int index) {
			return "";
		}
	}

	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	static class Leaf extends Derived {
		Doc bareReplaceAll() {
			return text("k").replaceAll("x", "y");
		}

		boolean bareStripIsEmpty() {
			return text("k").strip().isEmpty();
		}

		Doc superReplaceAll() {
			return super.text("k").replaceAll("x", "y");
		}

		boolean superStripIsEmpty() {
			return super.text("k").strip().isEmpty();
		}

		Doc thisReplaceAll(String text) {
			return this.text("k").replaceAll("x", "y");
		}

		boolean thisStripIsEmpty(String text) {
			return this.text("k").strip().isEmpty();
		}
	}

	record Labelled(Doc text) {
		Doc accessorReplaceAll() {
			return text().replaceAll("x", "y");
		}

		boolean accessorStripIsEmpty() {
			return text().strip().isEmpty();
		}

		String text(String... parts) {
			return "";
		}
	}
}

class InputSpecificApiOverloadSpelledLikeAnOverrideClean {
	static class Base<T> {
		Doc text(T value) {
			return new Doc();
		}
	}

	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	static class KeyedBase {
		static class Key {
		}

		Doc text(Key key) {
			return new Doc();
		}
	}

	static class KeyedDerived extends KeyedBase {
		static class Key {
		}

		Doc nestedKeyReplaceAll(KeyedBase.Key key) {
			return text(key).replaceAll("x", "y");
		}

		boolean nestedKeyStripIsEmpty(KeyedBase.Key key) {
			return text(key).strip().isEmpty();
		}

		String text(Key key) {
			return "";
		}
	}

	static class TypedDerived<T extends CharSequence> extends Base<Integer> {
		String text(T value) {
			return "";
		}

		Doc typeVariableReplaceAll() {
			return text(5).replaceAll("x", "y");
		}

		boolean typeVariableStripIsEmpty() {
			return text(5).strip().isEmpty();
		}
	}
}

class InputSpecificApiOverrideOfATypeVariableClean {
	static class Base<T> {
		<U> CharSequence label(U key) {
			return "";
		}

		CharSequence source(T key) {
			return "";
		}
	}

	static class Derived<T> extends Base<T> {
		String genericMethodReplaceAll(T key) {
			return label(key).replaceAll("x", "y");
		}

		boolean genericMethodStripIsEmpty(T key) {
			return label(key).strip().isEmpty();
		}

		@Override
		<U> String label(U key) {
			return "";
		}

		@Override
		String source(T key) {
			return "";
		}
	}
}

class InputSpecificApiClasspathMemberImportClean {
	static class Base {
		Doc text(Entry entry) {
			return new Doc();
		}
	}

	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	abstract static class EntryMapOuter implements Map<String, String> {
		static class Base {
			Doc text(Entry entry) {
				return new Doc();
			}
		}
	}

	abstract static class MapDerived extends Base implements Map<String, String> {
		Doc importEntryReplaceAll(KeyStore.Entry entry) {
			return text(entry).replaceAll("x", "y");
		}

		boolean importEntryStripIsEmpty(KeyStore.Entry entry) {
			return text(entry).strip().isEmpty();
		}

		String text(Entry entry) {
			return "";
		}
	}

	abstract static class MapMid extends Base implements Map<String, String> {
	}

	abstract static class MidDerived extends MapMid {
		Doc importEntryReplaceAll(KeyStore.Entry entry) {
			return text(entry).replaceAll("x", "y");
		}

		boolean importEntryStripIsEmpty(KeyStore.Entry entry) {
			return text(entry).strip().isEmpty();
		}

		String text(Entry entry) {
			return "";
		}
	}

	static class OuterDerived extends EntryMapOuter.Base {
		Doc mapEntryReplaceAll(Map.Entry<String, String> entry) {
			return text(entry).replaceAll("x", "y");
		}

		boolean mapEntryStripIsEmpty(Map.Entry<String, String> entry) {
			return text(entry).strip().isEmpty();
		}

		String text(Entry entry) {
			return "";
		}
	}

	Map<String, String> anonymousMap() {
		return new AbstractMap<>() {
			class Derived extends Base {
				Doc importEntryReplaceAll(KeyStore.Entry entry) {
					return text(entry).replaceAll("x", "y");
				}

				boolean importEntryStripIsEmpty(KeyStore.Entry entry) {
					return text(entry).strip().isEmpty();
				}

				String text(Entry entry) {
					return "";
				}
			}

			@Override
			public Set<Entry<String, String>> entrySet() {
				return Set.of();
			}
		};
	}
}

class InputSpecificApiClasspathMemberEnumClean {
	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	static class EnumDesc {
	}

	enum Kind implements Source {
		ONE;

		Doc sameFileDescReplaceAll(InputSpecificApiClasspathMemberEnumClean.EnumDesc desc) {
			return text(desc).replaceAll("x", "y");
		}

		boolean sameFileDescStripIsEmpty(InputSpecificApiClasspathMemberEnumClean.EnumDesc desc) {
			return text(desc).strip().isEmpty();
		}

		public String text(EnumDesc desc) {
			return "";
		}
	}

	interface Source {
		default Doc text(EnumDesc desc) {
			return new Doc();
		}
	}
}

class InputSpecificApiClasspathMemberSameFileClean {
	static class Base {
		Doc text(Entry entry) {
			return new Doc();
		}
	}

	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	static class Entry {
	}

	abstract static class MapDerived extends Base implements Map<String, String> {
		Doc sameFileEntryReplaceAll(InputSpecificApiClasspathMemberSameFileClean.Entry entry) {
			return text(entry).replaceAll("x", "y");
		}

		boolean sameFileEntryStripIsEmpty(InputSpecificApiClasspathMemberSameFileClean.Entry entry) {
			return text(entry).strip().isEmpty();
		}

		String text(Entry entry) {
			return "";
		}
	}
}

class InputSpecificApiClasspathMemberTypeVariableClean<Entry> {
	class Base {
		Doc text(Entry entry) {
			return new Doc();
		}
	}

	static class Doc {
		boolean isEmpty() {
			return true;
		}

		Doc replaceAll(String regex, String replacement) {
			return this;
		}

		Doc strip() {
			return this;
		}
	}

	abstract class MapDerived extends Base implements Map<String, String> {
		String text(Entry entry) {
			return "";
		}

		Doc typeVariableReplaceAll() {
			return text(held).replaceAll("x", "y");
		}

		boolean typeVariableStripIsEmpty() {
			return text(held).strip().isEmpty();
		}
	}

	Entry held;
}

class InputSpecificApiClasspathSupertypeCallClean {
	enum Constant implements ClasspathDocProvider {
		INDEXED {
			ClasspathDoc docReplaceAll() {
				return doc(5).replaceAll("x", "y");
			}
		};

		String doc(String key) {
			return key;
		}
	}

	static class Deep extends Mid {
		ClasspathDoc textReplaceAll() {
			return text().replaceAll("x", "y");
		}
	}

	static class Leaf extends ClasspathDocSource {
		class Inner {
			ClasspathDoc enclosingTextReplaceAll() {
				return text().replaceAll("x", "y");
			}
		}

		ClasspathDoc createReplaceAll() {
			return create().replaceAll("x", "y");
		}

		ClasspathDoc sourceReplaceAll() {
			return source().replaceAll("x", "y");
		}

		boolean sourceStripIsEmpty() {
			return source().strip().isEmpty();
		}

		ClasspathDoc superTextReplaceAll() {
			return super.text().replaceAll("x", "y");
		}

		boolean superTextStripIsEmpty() {
			return super.text().strip().isEmpty();
		}

		ClasspathDoc textReplaceAll() {
			return text().replaceAll("x", "y");
		}

		boolean textStripIsEmpty() {
			return text().strip().isEmpty();
		}

		boolean textStripLengthIsZero() {
			return text().strip().length() == 0;
		}

		ClasspathDoc thisTextReplaceAll(String text) {
			return this.text().replaceAll("x", "y");
		}

		boolean thisTextStripIsEmpty(String text) {
			return this.text().strip().isEmpty();
		}
	}

	static class Mid extends ClasspathDocSource {
	}

	static class Overload extends ClasspathDocSource {
		String text(String key) {
			return key;
		}

		ClasspathDoc textReplaceAll() {
			return text(5).replaceAll("x", "y");
		}

		boolean textStripIsEmpty() {
			return text(5).strip().isEmpty();
		}
	}

	static class Provided implements ClasspathDocProvider {
		ClasspathDoc docReplaceAll() {
			return doc().replaceAll("x", "y");
		}

		boolean docStripIsEmpty() {
			return doc().strip().isEmpty();
		}
	}

	static class Shadow {
		class Inner extends ClasspathDocSource {
			ClasspathDoc textReplaceAll() {
				return text().replaceAll("x", "y");
			}
		}

		String text() {
			return "";
		}
	}

	ClasspathDocSource anonymousTextReplaceAll() {
		return new ClasspathDocSource() {
			ClasspathDoc textReplaceAll() {
				return text().replaceAll("x", "y");
			}
		};
	}
}

/**
 * Calls inherited from a classpath supertype whose method returns its type variable, bound to a
 * type that is not {@code String}, to a same-file type, to a type parameter nothing binds, or left
 * to inference by a diamond or by the method's own type variable.
 */
class InputSpecificApiClasspathTypeArgumentClean {
	static class Atomic extends AtomicReference<ClasspathDoc> {
		ClasspathDoc getReplaceAll() {
			return get().replaceAll("x", "y");
		}
	}

	static class Base<T> extends ArrayList<T> {
	}

	static class Bounded<T extends ClasspathDoc> extends ArrayList<T> {
		ClasspathDoc firstReplaceAll() {
			return get(0).replaceAll("x", "y");
		}
	}

	static class Docs extends ClasspathValueSource<ClasspathDoc> {
		ClasspathDoc valueReplaceAll() {
			return value().replaceAll("x", "y");
		}

		boolean valueStripIsEmpty() {
			return value().strip().isEmpty();
		}

		boolean valueStripLengthIsZero() {
			return value().strip().length() == 0;
		}
	}

	abstract static class Executor extends AbstractExecutorService {
		ClasspathDoc invokeAnyReplaceAll(List<Callable<ClasspathDoc>> tasks) throws Exception {
			return invokeAny(tasks).replaceAll("x", "y");
		}
	}

	static class Keyed extends HashMap<String, ClasspathDoc> {
		ClasspathDoc getReplaceAll() {
			return get("k").replaceAll("x", "y");
		}
	}

	static class Local extends ThreadLocal<ClasspathDoc> {
		ClasspathDoc getReplaceAll() {
			return get().replaceAll("x", "y");
		}
	}

	static class Shadow {
		class Inner extends ArrayList<ClasspathDoc> {
			ClasspathDoc firstReplaceAll() {
				return get(0).replaceAll("x", "y");
			}
		}

		String get(int index) {
			return "";
		}
	}

	static class Substituted extends Base<ClasspathDoc> {
		ClasspathDoc firstReplaceAll() {
			return get(0).replaceAll("x", "y");
		}
	}

	static class Template {
		boolean isEmpty() {
			return true;
		}

		Template replaceAll(String regex, String replacement) {
			return this;
		}

		Template strip() {
			return this;
		}
	}

	static class Templates extends ArrayList<Template> {
		Template firstReplaceAll() {
			return get(0).replaceAll("name", "x");
		}

		boolean firstStripIsEmpty() {
			return get(0).strip().isEmpty();
		}

		Template superFirstReplaceAll() {
			return super.get(0).replaceAll("name", "x");
		}

		Template thisFirstReplaceAll(String get) {
			return this.get(0).replaceAll("name", "x");
		}
	}

	static class Weak extends WeakReference<ClasspathDoc> {
		Weak() {
			super(null);
		}

		ClasspathDoc getReplaceAll() {
			return get().replaceAll("x", "y");
		}
	}

	List<ClasspathDoc> diamondFirstReplaceAll() {
		return new ArrayList<>() {
			private ClasspathDoc firstReplaceAll() {
				return get(0).replaceAll("x", "y");
			}
		};
	}
}

/**
 * Bare calls past a same-file supertype's method that a subtype does not inherit, a private one or
 * an interface's static one, which bind to an enclosing class's method that does not return
 * {@code String}.
 */
class InputSpecificApiNonInheritedMemberClean {
	static class Base {
		private String make() {
			return "";
		}
	}

	class Extended extends Base {
		ClasspathDoc makeReplaceAll() {
			return make().replaceAll("x", "y");
		}

		boolean makeStripIsEmpty() {
			return make().strip().isEmpty();
		}
	}

	class ImplementsPrivate implements PrivateMaker {
		ClasspathDoc makeReplaceAll() {
			return make().replaceAll("x", "y");
		}
	}

	class ImplementsStatic implements StaticMaker {
		ClasspathDoc makeReplaceAll() {
			return make().replaceAll("x", "y");
		}

		boolean makeStripIsEmpty() {
			return make().strip().isEmpty();
		}
	}

	interface PrivateMaker {
		private String make() {
			return "";
		}
	}

	interface StaticMaker {
		static String make() {
			return "";
		}
	}

	ClasspathDoc make() {
		return new ClasspathDoc();
	}
}

/**
 * Bare calls no enclosing class declares, bound to a static import whose method returns a type
 * variable the call infers, or a type that is not {@code String}.
 */
class InputSpecificApiStaticImportClean {
	ClasspathDoc createReplaceAll() {
		return create().replaceAll("x", "y");
	}

	boolean createStripIsEmpty() {
		return create().strip().isEmpty();
	}

	ClasspathDoc requireNonNullReplaceAll(ClasspathDoc doc) {
		return requireNonNull(doc).replaceAll("x", "y");
	}

	boolean requireNonNullStripIsEmpty(ClasspathDoc doc) {
		return requireNonNull(doc).strip().isEmpty();
	}
}

/**
 * Bare calls inherited from a classpath supertype written with a type-use annotation, simple or
 * qualified, beside an enclosing class's method returning {@code String} that they hide.
 */
class InputSpecificApiAnnotatedSupertypeClean {
	class Qualified extends com.etk2000.checkstyle.testhelpers.@Mark ClasspathDocSource {
		ClasspathDoc textReplaceAll() {
			return text().replaceAll("x", "y");
		}
	}

	class Simple extends @Mark ClasspathDocSource {
		ClasspathDoc textReplaceAll() {
			return text().replaceAll("x", "y");
		}

		boolean textStripIsEmpty() {
			return text().strip().isEmpty();
		}
	}

	@Target(ElementType.TYPE_USE)
	@interface Mark {}

	String text() {
		return "";
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

class InputSpecificApiBareCallInheritedFromAnUnresolvableSupertypeClean extends InputSpecificApiUnresolvableBase {
	String replaceAllOnTheInheritedCall() {
		return name().replaceAll("x", "y");
	}

	boolean stripIsEmptyOnTheInheritedCall() {
		return name().strip().isEmpty();
	}
}

class InputSpecificApiBareCallStaticImportedFromAnUnresolvableOwnerClean {
	String replaceAllOnTheImportedCall() {
		return label().replaceAll("x", "y");
	}

	String replaceAllOnTheImportedOnDemandCall() {
		return title().replaceAll("x", "y");
	}

	boolean stripIsEmptyOnTheImportedCall() {
		return label().strip().isEmpty();
	}
}

class InputSpecificApiLambdaParameterReceiverClean {
	void replaceAllOnALambdaParameter(List<String> names) {
		names.replaceAll(name -> name.replaceAll("x", "y"));
	}

	boolean stripIsEmptyOnALambdaParameter(List<String> names) {
		return names.removeIf(name -> name.strip().isEmpty());
	}

	boolean stripLengthIsZeroOnALambdaParameter(List<String> names) {
		return names.removeIf(name -> name.strip().length() == 0);
	}

	boolean stripLengthIsZeroReversedOnALambdaParameter(List<String> names) {
		return names.removeIf(name -> 0 == name.strip().length());
	}
}

class InputSpecificApiUnprovenVarClean {
	String replaceAllOnAVarLocalWithAnUnprovenInitializer(Attr attr) {
		final var text = attr.getValue();
		return text.replaceAll("x", "y");
	}

	void stripIsEmptyOnAVarForEachVariable(List<String> names) {
		for (var name : names)
			System.out.println(name.strip().isEmpty());
	}

	boolean stripIsEmptyOnAVarLocalWithAnUnprovenInitializer(Attr attr) {
		final var text = attr.getValue();
		return text.strip().isEmpty();
	}
}

class InputSpecificApiShadowedStringReceiverClean {
	class ClasspathFieldSubclass extends ClasspathDocField {
		ClasspathDoc replaceAllOnTheInheritedField() {
			return text.replaceAll("x", "y");
		}

		boolean stripIsEmptyOnTheInheritedField() {
			return text.strip().isEmpty();
		}
	}

	class UnresolvableSubclass extends InputSpecificApiUnresolvableBase {
		boolean stripIsEmptyOnAnOuterField() {
			return text.strip().isEmpty();
		}
	}

	String text = "";

	void equalsEmptyAfterANegatedPattern(Object value) {
		if (!(value instanceof ClasspathDoc text))
			return;
		System.out.println(text.equals(""));
	}

	ClasspathDoc replaceAllAfterANegatedPattern(Object value) {
		if (!(value instanceof ClasspathDoc text))
			return null;
		return text.replaceAll("x", "y");
	}

	void stripIsEmptyAfterANegatedPattern(Object value) {
		if (!(value instanceof ClasspathDoc text))
			return;
		System.out.println(text.strip().isEmpty());
	}

	boolean stripIsEmptyInACase(Object value) {
		return switch (value) {
			case ClasspathDoc text -> text.strip().isEmpty();
			default -> value == null;
		};
	}

	boolean stripIsEmptyInAConditionalArm(Object value) {
		return value instanceof ClasspathDoc text ? text.strip().isEmpty() : value == null;
	}

	void stripIsEmptyInANegatedPatternElse(Object value) {
		if (!(value instanceof ClasspathDoc text))
			System.out.println(value);
		else
			System.out.println(text.strip().isEmpty());
	}

	boolean stripIsEmptyInAnOrRightOperand(Object value) {
		return !(value instanceof ClasspathDoc text) || text.strip().isEmpty();
	}

	boolean stripIsEmptyOnAnIntersectionCast(Object value) {
		return ((String & Comparable<String>) value).strip().isEmpty();
	}
}