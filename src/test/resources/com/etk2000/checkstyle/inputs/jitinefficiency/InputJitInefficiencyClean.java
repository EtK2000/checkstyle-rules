package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class InputJitInefficiencyClean {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	private static final Color[] CACHED_VALUES = Color.values();
	private static final Pattern P = Pattern.compile("\\d+");
	private static final Pattern STATIC_INIT_PATTERN;
	private static StringBuffer SHARED_BUFFER;

	static {
		// Pattern.compile in static init runs once, not on every method call
		STATIC_INIT_PATTERN = Pattern.compile("[a-z]+");
	}

	private final Pattern instancePattern;

	{
		// Pattern.compile in instance init runs once per construction, not per call
		instancePattern = Pattern.compile("[A-Z]+");
	}

	void appendChained(StringBuilder sb, String key, String value) {
		sb.append(key).append('=').append(value);
		sb.append("plain");
	}

	void appendNumericAddIsClean(StringBuilder sb, int a, int b) {
		sb.append(a + b);
	}

	void boxedFactories() {
		final var a = Integer.valueOf(42);
		final var b = Long.valueOf(100L);
		final var c = Boolean.TRUE;
		final var d = Boolean.FALSE;
		final var e = Boolean.valueOf(false);
		final var f = Double.valueOf(3.14);
		final var g = Float.valueOf(1.5f);
		final var h = Short.valueOf((short) 1);
		final var i = Byte.valueOf((byte) 1);
		final var j = Character.valueOf('c');
		System.out.println(a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + g + " " + h + " " + i + " " + j);
	}

	void boxedParseAndArrays() {
		final var n = Long.parseLong("100");
		final var arr = new Integer[5];
		final var multi = new String[3][];
		System.out.println(n + arr.length + multi.length);
	}

	StringBuffer fieldAssignBufferIsClean() {
		SHARED_BUFFER = new StringBuffer();
		return SHARED_BUFFER;
	}

	void finalBoxedNotModified() {
		final var fixed = compute();
		System.out.println(fixed);
	}

	void iteratorWhileWithRemove(List<String> list) {
		final var it = list.iterator();
		while (it.hasNext()) {
			final var x = it.next();
			if (x.isEmpty())
				it.remove();
		}
	}

	void keySetWithoutGet(Map<String, Integer> map) {
		for (var key : map.keySet())
			System.out.println(key);
	}

	void nestedAnonymousClassNoInit() {
		final var list = new ArrayList<String>() {
			void extra() {
				add("a");
			}
		};
		System.out.println(list);
	}

	void noEmptyStringConcat(int x) {
		final var s = String.valueOf(x);
		final var t = "label: " + x;
		final var u = " " + x;
		System.out.println(s + t + u);
	}

	void plainStringValueAndCharsetCtor(byte[] data) {
		final var s = "hello";
		final var copy = s;
		final var multi = new String(data, java.nio.charset.StandardCharsets.UTF_8);
		System.out.println(copy + multi);
	}

	void primitiveAccumulator(List<Long> nums) {
		var sum = 0L;
		for (var v : nums)
			sum += v;
		System.out.println(sum);
	}

	void regexCachedOutsideLoop(List<String> lines) {
		for (var line : lines) {
			if (P.matcher(line).matches())
				System.out.println(line);
		}
	}

	void regexOutsideLoop(String s) {
		final var ok = s.matches("\\d+");
		System.out.println(ok);
	}

	void stringConcatOutsideLoop() {
		var s = "a";
		s += "b";
		System.out.println(s);
	}

	void toArrayZero(List<String> list) {
		final var a = list.toArray(new String[0]);
		final var b = list.toArray(String[]::new);
		final var c = list.toArray(new String[0][]);
		System.out.println(a.length + b.length + c.length);
	}

	void useEntrySet(Map<String, Integer> map) {
		for (var entry : map.entrySet())
			System.out.println(entry.getKey() + entry.getValue());
	}

	void useEnhancedFor(List<String> list) {
		for (var x : list)
			System.out.println(x);
	}

	void valuesCachedToLocal() {
		final var values = Color.values();
		for (var i = 0; i < values.length; ++i)
			System.out.println(values[i]);
	}

	void valuesOutsideLoop() {
		final var arr = Color.values();
		System.out.println(arr.length);
	}

	void booleanAccumulatorIsClean() {
		Boolean flag = false;
		for (var i = 0; i < 10; ++i)
			flag = !flag;
		System.out.println(flag);
	}

	void characterAccumulatorIsClean() {
		Character c = 'a';
		for (var i = 0; i < 5; ++i)
			c = (char) (c + 1);
		System.out.println(c);
	}

	void enumValuesInForInit() {
		// FOR_INIT runs once, not per iteration
		for (var i = Color.values().length - 1; i >= 0; --i)
			System.out.println(i);
	}

	void splitInForEachIterable(String csv) {
		// for-each iterable is evaluated once
		for (var part : csv.split(","))
			System.out.println(part);
	}

	void mapKeySetGetDifferentKey(java.util.Map<String, Integer> map, String otherKey) {
		for (var key : map.keySet())
			System.out.println(key + map.get(otherKey));
	}

	void mapKeySetGetDifferentMap(java.util.Map<String, Integer> map, java.util.Map<String, Integer> other) {
		for (var key : map.keySet())
			System.out.println(key + other.get(key));
	}

	void iteratorWhileWithListIteratorMethod(java.util.ListIterator<String> it) {
		// .set() on the iterator means enhanced-for can't replace this loop
		while (it.hasNext()) {
			final var x = it.next();
			it.set(x.toUpperCase());
		}
	}

	void appendOnStringBufferIsClean(StringBuffer buf, String x, String y) {
		buf.append(x).append(y);
	}

	void anonymousClassNonCollectionWithInit() {
		// instance init in a non-Collection/Map anonymous class is not double-brace
		final var r = new Runnable() {
			{
				System.out.println("setup");
			}

			@Override
			public void run() {
			}
		};
		r.run();
	}

	void uppercaseReceiverNotString() {
		// uppercase receiver -> static call, not a String instance
		System.out.println(java.util.regex.Pattern.matches("\\d+", "x"));
	}

	void noArgRegexMethodOnString() {
		// matches/replaceAll/split take args; .toUpperCase() does not
		final var s = "x".toUpperCase();
		System.out.println(s);
	}

	void appendChainedDifferentReceiverIsClean(StringBuilder a, StringBuilder b, String value) {
		a.append(value);
		b.append(value);
	}

	void regexInLambdaInsideLoop(java.util.List<String> lines) {
		// LAMBDA body breaks the loop-ancestor scan
		for (var line : lines) {
			final Runnable r = () -> System.out.println(line.matches("\\d+"));
			r.run();
		}
	}

	void regexInAnonymousClassInsideLoop(java.util.List<String> lines) {
		// OBJBLOCK body breaks the loop-ancestor scan
		for (var line : lines) {
			System.out.println(new Object() {
				@Override
				public String toString() {
					return Boolean.toString(line.matches("\\d+"));
				}
			});
		}
	}

	void multiDimSizedToArrayIsClean(List<String> list) {
		// check skips multi-dim arrays entirely, even with non-zero sizes
		final var multi = list.toArray(new String[5][3]);
		System.out.println(multi.length);
	}

	void assignWithoutSelfReadIsClean(int n, Integer source) {
		// accumulator detection requires the assignment to read the LHS
		Integer x = 0;
		for (var i = 0; i < n; ++i)
			x = source;
		System.out.println(x);
	}

	void byteAccumulatorClean(byte b) {
		final Byte fixed = b;
		for (var i = 0; i < 5; ++i)
			System.out.println(fixed);
	}

	void shortAccumulatorClean(short s) {
		final Short fixed = s;
		for (var i = 0; i < 5; ++i)
			System.out.println(fixed);
	}

	private Long compute() {
		return 0L;
	}
}