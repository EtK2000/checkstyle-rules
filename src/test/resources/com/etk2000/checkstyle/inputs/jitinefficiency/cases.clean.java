package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
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
		STATIC_INIT_PATTERN = Pattern.compile("[a-z]+");
	}

	private final StringBuffer sharedBuffer = new StringBuffer();
	private final Pattern instancePattern;
	private Integer fieldTotal;

	{
		instancePattern = Pattern.compile("[A-Z]+");
	}

	void appendChained(StringBuilder sb, String key, String value) {
		sb.append(key).append('=').append(value);
		sb.append("plain");
	}

	void appendNumericAddIsClean(StringBuilder sb, int a, int b) {
		sb.append(a + b);
	}

	void appendTextBlockIsClean(StringBuilder sb) {
		sb.append("""prefix""" + 42);
	}

	Object newNonBoxedTypeIsClean() {
		return new Foo(42);
	}

	Object newStringBufferInputStreamIsClean(byte[] bytes) {
		return new StringBufferInputStream(bytes);
	}

	String newStringComplexArgIsClean() {
		return new String(getValue());
	}

	String newStringEmptyArgIsClean() {
		return new String();
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

	StringBuffer fieldInitBufferIsClean() {
		return sharedBuffer;
	}

	void boxedFieldAccumulatedInLoopIsClean(int n) {
		for (var i = 0; i < n; ++i)
			fieldTotal += 1;
		System.out.println(fieldTotal);
	}

	void boxedForInitAccumulatedInLoopIsClean(int n) {
		for (Integer i = 0; i < n; ++i)
			System.out.println(i);
	}

	void mapValuesInLoopIsClean(Map<String, Integer> map, int n) {
		for (var i = 0; i < n; ++i)
			System.out.println(map.values().size());
	}

	void matcherMatchesInLoopIsClean(Matcher matcher, int n) {
		for (var i = 0; i < n; ++i)
			System.out.println(matcher.matches());
	}

	String newStringFromByteArrayIsClean(byte[] data) {
		return new String(data);
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
		for (var i = Color.values().length - 1; i >= 0; --i)
			System.out.println(i);
	}

	void splitInForEachIterable(String csv) {
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
		while (it.hasNext()) {
			final var x = it.next();
			it.set(x.toUpperCase());
		}
	}

	void appendOnStringBufferIsClean(StringBuffer buf, String x, String y) {
		buf.append(x).append(y);
	}

	void anonymousClassNonCollectionWithInit() {
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
		System.out.println(java.util.regex.Pattern.matches("\\d+", "x"));
	}

	void noArgRegexMethodOnString() {
		final var s = "x".toUpperCase();
		System.out.println(s);
	}

	void appendChainedDifferentReceiverIsClean(StringBuilder a, StringBuilder b, String value) {
		a.append(value);
		b.append(value);
	}

	void regexInLambdaInsideLoop(java.util.List<String> lines) {
		for (var line : lines) {
			final Runnable r = () -> System.out.println(line.matches("\\d+"));
			r.run();
		}
	}

	void regexInAnonymousClassInsideLoop(java.util.List<String> lines) {
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
		final var multi = list.toArray(new String[5][3]);
		System.out.println(multi.length);
	}

	void assignWithoutSelfReadIsClean(int n, Integer source) {
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

class InputJitInefficiencyExplicitFormClean {
	static class SomeUnknownType {
		Object field;
	}

	int n;
	String f;

	void explicitArrayElementNonString(int n) {
		final var arr = new int[3];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i] + 1;
	}

	void explicitArrayRebindInRhsIsClean(List<String> list) {
		var arr = new String[3];
		arr[0] = "";
		for (var x : list)
			arr[0] = (arr = getArr())[0] + x;
	}

	void explicitCrossMethodScopedVarIsClean(List<String> list) {
		for (var x : list)
			s = s + x;
	}

	void explicitCrossMethodScopedVarSource() {
		final String s = "in source";
		System.out.println(s);
	}

	void explicitArrayLhsNotBare(int n) {
		final var arr = new String[3];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i].trim() + "!";
	}

	void explicitDifferentVar(int n) {
		String s = "";
		final var t = "x";
		for (var i = 0; i < n; ++i)
			s = t + i;
		System.out.println(s);
	}

	void explicitFieldNonString(int times) {
		this.n = 0;
		for (var i = 0; i < times; ++i)
			this.n = this.n + 1;
		System.out.println(this.n);
	}

	void explicitNonString(int times) {
		int n = 0;
		for (var i = 0; i < times; ++i)
			n = n + 1;
		System.out.println(n);
	}

	void explicitNotBare(List<String> list) {
		String s = "abc";
		for (var x : list)
			s = s.trim() + x;
		System.out.println(s);
	}

	void explicitOutsideLoop() {
		var s = "a";
		s = s + "b";
		System.out.println(s);
	}

	void explicitRhsMethodCall(int n) {
		String s = "";
		for (var i = 0; i < n; ++i)
			s = compute();
		System.out.println(s);
	}

	void explicitTernaryRhs(int n, boolean flag) {
		String s = "";
		for (var i = 0; i < n; ++i)
			s = flag ? s : "x";
		System.out.println(s);
	}

	void explicitLambdaInLoop(List<String> list) {
		for (var x : list) {
			final Runnable r = () -> {
				String t = "";
				t = t + x;
				System.out.println(t);
			};
			r.run();
		}
	}

	void explicitMethodCallArrayReceiverLhs(List<String> list) {
		for (var x : list)
			getArr()[0] = getArr()[0] + x;
	}

	void explicitMethodCallReceiverLhs(List<String> list) {
		for (var x : list)
			getSelf().f = getSelf().f + x;
	}

	void explicitUnresolvableMiddleSegment(SomeUnknownType obj, List<String> list) {
		for (var x : list)
			obj.field.value = obj.field.value + x;
	}

	void explicitVarFromUnknownMethod(List<String> list) {
		var result = list.get(0).toUpperCase();
		for (var x : list)
			result = result + x;
		System.out.println(result);
	}

	private String compute() {
		return "x";
	}

	private String[] getArr() {
		return new String[]{"a"};
	}

	private InputJitInefficiencyExplicitFormClean getSelf() {
		return this;
	}
}