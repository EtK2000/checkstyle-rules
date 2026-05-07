package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

class InputJitInefficiencyLoopViolation {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	void boxedAccumulatorFor(List<Long> nums) {
		Long sum = 0L; // violation: Boxed accumulator 'sum' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var v : nums)
			sum += v;
		System.out.println(sum);
	}

	void boxedAccumulatorMinusAssign() {
		Long count = 100L; // violation: Boxed accumulator 'count' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < 10; ++i)
			count -= 1;
		System.out.println(count);
	}

	void boxedAccumulatorStarAssign() {
		Integer prod = 1; // violation: Boxed accumulator 'prod' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		while (prod < 1000)
			prod *= 2;
		System.out.println(prod);
	}

	void boxedAccumulatorDivAssign() {
		Long n = 1024L; // violation: Boxed accumulator 'n' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		while (n > 1)
			n /= 2;
		System.out.println(n);
	}

	void boxedAccumulatorModAssign() {
		Long m = 1000L; // violation: Boxed accumulator 'm' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < 5; ++i)
			m %= 7;
		System.out.println(m);
	}

	void boxedAccumulatorSelfRead(int n) {
		Integer count = 0; // violation: Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			count = count + 1;
		System.out.println(count);
	}

	void boxedFqnConstructor() {
		final var x = new java.lang.Integer(42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		System.out.println(x);
	}

	void regexInForCondition(String s) {
		for (var i = 0; s.matches("\\d+"); ++i) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(i);
	}

	void regexInForIterator(String s) {
		for (var i = 0; i < 10; s = s.replaceAll("a", "b")) // violation: '.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(i);
	}

	void boxedAccumulatorWhile(boolean cond) {
		Integer count = 0; // violation: Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		while (cond) {
			count += 1;
			cond = count < 10;
		}
		System.out.println(count);
	}

	void boxedAccumulatorClassicFor(int n) {
		Double total = 0.0; // violation: Boxed accumulator 'total' (type 'Double') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			total += i;
		System.out.println(total);
	}

	void boxedAccumulatorDoWhile() {
		Float fSum = 0f; // violation: Boxed accumulator 'fSum' (type 'Float') is autoboxed in a loop, prefer the primitive type.
		do fSum += 1f;
		while (fSum < 10f);
		System.out.println(fSum);
	}

	void enumValuesDoWhile() {
		var i = 0;
		do {
			final var arr = Color.values(); // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
			i += arr.length;
		}
		while (i < 10);
	}

	void enumValuesInForEach() {
		for (var i = 0; i < 100; ++i) {
			for (var c : Color.values()) // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
				System.out.println(c);
		}
	}

	void matchesInDoWhile(String s) {
		var i = 0;
		do {
			if (s.matches("\\d+")) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
				++i;
		}
		while (i < 5);
	}

	void splitInClassicFor(String s) {
		for (var i = 0; i < 10; ++i) {
			final var parts = s.split(","); // violation: '.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(parts.length);
		}
	}

	void stringConcatInClassicFor(int n) {
		String result = "";
		for (var i = 0; i < n; ++i)
			result += i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatInDoWhile() {
		String s = "";
		do s += "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}

	void enumValuesInWhile(boolean cond) {
		while (cond) {
			final var arr = Color.values(); // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
			cond = arr.length > 0;
		}
	}

	void iteratorWhileNoRemove(List<String> list) {
		final var it = list.iterator();
		while (it.hasNext()) { // violation: Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.
			final var x = it.next();
			System.out.println(x);
		}
	}

	void mapKeySetGet(Map<String, Integer> map) {
		for (var key : map.keySet()) { // violation: Iterate '.entrySet()' instead of '.keySet()' + '.get(...)' (avoids double lookup).
			final var value = map.get(key);
			System.out.println(key + value);
		}
	}

	void regexMatchesInForEach(List<String> lines) {
		for (var line : lines) {
			if (line.matches("\\d+")) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
				System.out.println(line);
		}
	}

	void regexReplaceAllInFor(List<String> lines) {
		for (var i = 0; i < lines.size(); ++i) {
			final var s = lines.get(i);
			final var t = s.replaceAll("foo.*", "bar"); // violation: '.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(t);
		}
	}

	void regexSplitInWhile(Iterator<String> it) {
		while (it.hasNext()) { // violation: Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.
			final var line = it.next();
			final var parts = line.split(","); // violation: '.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(parts.length);
		}
	}

	void stringConcatInForEach(List<String> list) {
		String result = "";
		for (var x : list)
			result += x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatInWhile(boolean cond) {
		String result = "";
		while (cond) {
			result += getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			cond = result.length() < 5;
		}
		System.out.println(result);
	}

	private String getNext() {
		return "x";
	}
}