package com.etk2000.checkstyle.inputs.preferimport;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;

@SuppressWarnings("unused")
class InputPreferImportClean
		extends ArrayList<String>
		implements Serializable {
	@interface Ann {}

	static class Base {}

	static class Boom extends RuntimeException {}

	static class Chain {
		Chain java, lang, String;

		int valueOf(int i) {
			return i;
		}
	}

	static class Holder {
		Holder Integer;

		int parseInt(String s) {
			return s.length();
		}
	}

	interface Iface {}

	static class Impl implements InputPreferImportClean.Iface {}

	static class Inner {}

	static class Middle {
		static class Leaf {}

		Middle.Leaf leaf;
	}

	@interface SelfAnnotation {
		class N {}

		SelfAnnotation.N value();
	}

	enum SelfEnum {
		CONSTANT;

		class N {}

		SelfEnum.N value() {
			return null;
		}
	}

	interface SelfInterface {
		class N {}

		SelfInterface.N get();
	}

	record SelfRecord() {
		class N {}

		SelfRecord.N value() {
			return null;
		}
	}

	static class Sub extends InputPreferImportClean.Base {}

	List<String> field;
	List<? super Set> lowerBound;
	List<Map<String, Integer>> nested;
	List<InputPreferImportClean.Inner> selfQualified;
	List<?> unbounded;
	List<? extends Map> upperBound;
	Map<List, Set> multiArg;

	void castAndInstanceof(Object obj) {
		if (obj instanceof List)
			System.out.println((List<?>) obj);
	}

	@InputPreferImportClean.Ann
	InputPreferImportClean.Inner enclosingSelfReferences() throws InputPreferImportClean.Boom {
		return new InputPreferImportClean.Inner();
	}

	Chain get() {
		return null;
	}

	void impureReceivers(Holder[] arr, Object x) {
		System.out.println(arr[0].Integer.parseInt("1"));
		System.out.println(((Chain) x).java.lang.String.valueOf(1));
		System.out.println(get().java.lang.String.valueOf(1));
		final IntUnaryOperator op = ((Chain) x).java.lang.String::valueOf;
		System.out.println(op.applyAsInt(1));
	}

	List<String> method(Set<Integer> param)
			throws IOException {
		final var local = List.of("a");
		return local;
	}

	void nestedExpressionReceivers() {
		Map.Entry.comparingByKey();
		System.out.println(Map.Entry.class);
		final Runnable r = Map.Entry::comparingByKey;
		r.run();
	}

	void newAndWitness() {
		new ArrayList<Map>();
		this.<Map>pick();
	}

	<T> T pick() {
		return null;
	}
}

class InputPreferImportTextBlockClean {
	String describe() {
		final var textBlock = """
				in text block
				""";
		return textBlock.trim();
	}
}