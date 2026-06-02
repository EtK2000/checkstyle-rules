package com.etk2000.checkstyle.inputs.recordformatting;

import java.util.List;
import java.util.Map;

interface Foo {}

interface Bar {}

class InputRecordFormattingClean {
	record EmptyA() {}

	record OneA(int a) {}

	record TwoA(int a, int b) {}

	record ThreeA(int a, int b, int c) {}

	record GenericA<T>(T a) {}

	record BoundedGenericA<T extends Number>(T a) {}

	record MultiTypeParamA<K, V>(K k, V v) {}

	record VarargsA(int a, int... rest) {}

	record GenericComponentA(List<String> list) {}

	record NestedGenericA(Map<String, List<Integer>> map) {}

	record WildcardA(List<? extends Number> nums) {}

	record ImplementsOneA(int a) implements Foo {}

	record ImplementsMultiA(int a) implements Foo, Bar {}

	record GenericImplementsA<T>(T a) implements Foo {}

	record WithBodyA(int a) {
		void m() {}
	}

	record WithStaticFieldA(int a) {
		static final int CONST = 5;
	}

	record TwoB(
			int a,
			int b
	) {}

	record ThreeB(
			int a,
			int b,
			int c
	) {}

	record GenericB<T>(
			T a,
			T b
	) {}

	record ImplementsOneB(
			int a,
			int b
	) implements Foo {}

	record ImplementsMultiLineClean(int a) implements
			Foo,
			Bar {}

	record WithBodyB(
			int a,
			int b
	) {
		void m() {}
	}

	record Outer(int a) {
		record Inner(int b) {}
	}

	void localRecords() {
		record LocalA(int x) {}

		record LocalB(
				int x,
				int y
		) {}
	}
}