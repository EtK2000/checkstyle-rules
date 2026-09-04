package com.etk2000.checkstyle.inputs.preferimport;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

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

class InputPreferImportObscuredClean {
	@interface AnnotationTypeConstant {
		Object Integer = null;

		int value() default java.lang.Integer.MAX_VALUE;
	}

	@interface Cap {
		int value();
	}

	@interface Caps {
		int[] value();
	}

	@interface Limit {
		int max();
	}

	@interface Outer {
		Cap value();
	}

	static class AnnotationArgArray {
		Object Integer;

		@Caps({java.lang.Integer.MAX_VALUE, java.lang.Integer.MIN_VALUE})
		void m() {}
	}

	static class AnnotationArgField {
		Object Integer;

		@Cap(java.lang.Integer.MAX_VALUE)
		void m() {}
	}

	static class AnnotationArgNamed {
		Object Integer;

		@Limit(max = java.lang.Integer.MAX_VALUE)
		void m() {}
	}

	static class AnnotationArgNested {
		Object Integer;

		@Outer(@Cap(java.lang.Integer.MAX_VALUE))
		void m() {}
	}

	record Box(Object value) {}

	interface Constants {
		Object Integer = null;

		default int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class ConstantsImplementor implements Constants {
		int read() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class CtorParam {
		CtorParam(Object Integer) {
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	static class DoubleNested {
		class Middle {
			class Leaf {
				int max() {
					return java.lang.Integer.MAX_VALUE;
				}
			}
		}

		Object Integer;
	}

	enum EnumConstant {
		Integer;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	enum EnumConstantArguments {
		Integer(java.lang.Integer.MAX_VALUE);

		EnumConstantArguments(int value) {
			System.out.println(value);
		}
	}

	enum EnumConstantBody {
		Integer {
			@Override
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		};

		abstract int max();
	}

	enum EnumConstantNestedClass {
		Integer;

		static class Reader {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}
	}

	static class FieldAfterUse {
		int max = java.lang.Integer.MAX_VALUE;
		Object Integer;
	}

	static class FieldAnnotated {
		@SuppressWarnings("unused")
		Object Integer;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class FieldCStyleArray {
		Object Integer[];

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class FieldInstance {
		Object Integer;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class FieldMultiFirst {
		Object Integer, Other;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class FieldMultiLast {
		Object Alpha, Integer;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class FieldOwnAnnotation {
		@Cap(java.lang.Integer.MAX_VALUE)
		Object Integer;
	}

	static class FieldStatic {
		static Object Integer;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class GenericBase<T> {
		Object Integer;
	}

	static class GenericSub extends GenericBase<String> {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class InnerOfOuter {
		class Inner {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}

		Object Integer;
	}

	static class LocalInInstanceInitializer {
		{
			final Object Integer = null;
			System.out.println(Integer);
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	static class LocalInStaticInitializer {
		static {
			final Object Integer = null;
			System.out.println(Integer);
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	record RecordCompact(Object Integer) {
		RecordCompact {
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	record RecordComponent(Object Integer) {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	record RecordStaticMethod(Object Integer) {
		static int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class StaticNestedOfOuter {
		static class Reader {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}

		Object Integer;
	}

	static class SuperBase {
		Object Integer;
	}

	static class SuperLeaf extends SuperMid {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class SuperMid extends SuperBase {}

	static class SuperSub extends SuperBase {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	@Cap(java.lang.Integer.MAX_VALUE)
	void annotationOwnParam(Object Integer) {
		System.out.println(Integer);
	}

	Object anonymousCapturedLocal() {
		final Object Integer = null;
		System.out.println(Integer);
		return new Object() {
			@Override
			public String toString() {
				return String.valueOf(java.lang.Integer.MAX_VALUE);
			}
		};
	}

	Object anonymousOwnField() {
		return new Object() {
			Object Integer;

			@Override
			public String toString() {
				return String.valueOf(java.lang.Integer.MAX_VALUE);
			}
		};
	}

	Object anonymousSameFileSuper() {
		return new SuperBase() {
			@Override
			public String toString() {
				return String.valueOf(java.lang.Integer.MAX_VALUE);
			}
		};
	}

	String callResultChained(Object Charset) {
		return java.nio.charset.Charset.defaultCharset().name();
	}

	List<String> callWitness(Object List) {
		return java.util.List.<String>of("a");
	}

	int catchAnnotated() {
		try {
			return 0;
		}
		catch (@SuppressWarnings("unused") RuntimeException Integer) {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	int catchMulti() {
		try {
			return 0;
		}
		catch (IllegalStateException | IllegalArgumentException Integer) {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	int catchParam() {
		try {
			return 0;
		}
		catch (RuntimeException Integer) {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	int changesMeaning() {
		final Integer Integer = 5;
		return java.lang.Integer.MAX_VALUE + Integer;
	}

	int compoundAssignment(Object Integer) {
		var sum = 1;
		sum += java.lang.Integer.SIZE;
		return sum;
	}

	Object fieldChain(Object System) {
		return java.lang.System.out;
	}

	void forEachBody(List<Object> items) {
		for (Object Integer : items) {
			System.out.println(Integer);
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	void forEachDeclarationAnnotation(List<Object> items) {
		for (@Cap(java.lang.Integer.MAX_VALUE) Object Integer : items)
			System.out.println(Integer);
	}

	void forEachIterable() {
		for (Object Integer : new Object[]{java.lang.Integer.MAX_VALUE})
			System.out.println(Integer);
	}

	void forInitCondition() {
		for (var Integer = 0; Integer < java.lang.Integer.MAX_VALUE; ++Integer)
			System.out.println(Integer);
	}

	void forInitDeclarationAnnotation() {
		for (@Cap(java.lang.Integer.MAX_VALUE) var Integer = 0; Integer < 1; ++Integer)
			System.out.println(Integer);
	}

	void forInitUpdate() {
		for (var Integer = 0; Integer < 1; Integer += java.lang.Integer.SIZE)
			System.out.println(Integer);
	}

	void forMultiInit() {
		for (int i = 0, Integer = 0; i < 1; ++i)
			System.out.println(java.lang.Integer.MAX_VALUE + Integer);
	}

	IntUnaryOperator lambdaAnnotated() {
		return (@SuppressWarnings("unused") var Integer) -> java.lang.Integer.MAX_VALUE;
	}

	IntUnaryOperator lambdaBlockBody() {
		return Integer -> {
			System.out.println(Integer);
			return java.lang.Integer.MAX_VALUE;
		};
	}

	IntUnaryOperator lambdaExplicit() {
		return (int Integer) -> java.lang.Integer.MAX_VALUE;
	}

	IntUnaryOperator lambdaImplicit() {
		return Integer -> java.lang.Integer.MAX_VALUE;
	}

	IntBinaryOperator lambdaImplicitMultiFirst() {
		return (Integer, other) -> java.lang.Integer.MAX_VALUE + other;
	}

	IntBinaryOperator lambdaImplicitMultiLast() {
		return (other, Integer) -> java.lang.Integer.MAX_VALUE + other;
	}

	IntUnaryOperator lambdaNested() {
		return Integer -> {
			final IntSupplier inner = () -> java.lang.Integer.MAX_VALUE;
			return inner.getAsInt() + Integer;
		};
	}

	IntUnaryOperator lambdaVar() {
		return (var Integer) -> java.lang.Integer.MAX_VALUE;
	}

	int localAnnotated() {
		@SuppressWarnings("unused")
		final Object Integer = null;
		return java.lang.Integer.MAX_VALUE;
	}

	Object localCapturedInLocalClass() {
		final Object Integer = null;
		class Local {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}

		System.out.println(Integer);
		return new Local();
	}

	int localCStyleArray() {
		final Object Integer[] = null;
		System.out.println(Integer);
		return java.lang.Integer.MAX_VALUE;
	}

	void localDeclarationAnnotation() {
		@Cap(java.lang.Integer.MAX_VALUE)
		final Object Integer = null;
		System.out.println(Integer);
	}

	int localMultiFirst() {
		final int Integer = 0, max = java.lang.Integer.MAX_VALUE;
		return Integer + max;
	}

	int localMultiLast() {
		final int other = 0, Integer = 0;
		return java.lang.Integer.MAX_VALUE + other + Integer;
	}

	int localOuterBlock(boolean flag) {
		final Object Integer = null;
		System.out.println(Integer);
		if (flag)
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	Object localOwnInitializer() {
		final Object Charset = java.nio.charset.Charset.defaultCharset();
		return Charset;
	}

	int localTyped() {
		final Object Integer = null;
		System.out.println(Integer);
		return java.lang.Integer.MAX_VALUE;
	}

	int localVar() {
		final var Integer = new Object();
		System.out.println(Integer);
		return java.lang.Integer.MAX_VALUE;
	}

	Object localVarOwnInitializer() {
		final var Charset = java.nio.charset.Charset.defaultCharset();
		return Charset;
	}

	Runnable methodRef(Object Thread) {
		return java.lang.Thread::yield;
	}

	Supplier<?> methodRefCharset(Object Charset) {
		return java.nio.charset.Charset::defaultCharset;
	}

	Supplier<List<String>> methodRefWitness(Object Collections) {
		return java.util.Collections::<String>emptyList;
	}

	Object nestedTypeQualifier(Object Entry) {
		return java.util.Map.Entry.comparingByKey();
	}

	int param(Object Integer) {
		return java.lang.Integer.MAX_VALUE;
	}

	int paramAnnotated(@SuppressWarnings("unused") Object Integer) {
		return java.lang.Integer.MAX_VALUE;
	}

	int paramCStyleArray(Object Integer[]) {
		return java.lang.Integer.MAX_VALUE;
	}

	void paramDeclarationAnnotation(@Cap(java.lang.Integer.MAX_VALUE) Object Integer) {
		System.out.println(Integer);
	}

	int paramMultiFirst(Object Integer, Object other) {
		return java.lang.Integer.MAX_VALUE;
	}

	int paramMultiLast(Object other, Object Integer) {
		return java.lang.Integer.MAX_VALUE;
	}

	int paramMultiMiddle(Object first, Object Integer, Object last) {
		return java.lang.Integer.MAX_VALUE;
	}

	int paramVarargs(Object... Integer) {
		return java.lang.Integer.MAX_VALUE;
	}

	boolean patternAnd(Object o) {
		return o instanceof String Integer && java.lang.Integer.MAX_VALUE > 0;
	}

	int patternConservativeElseOfPositive(Object o) {
		if (o instanceof String Integer)
			System.out.println(Integer);
		else
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	int patternConservativeNegatedThen(Object o) {
		if (!(o instanceof String Integer))
			return java.lang.Integer.MAX_VALUE;
		System.out.println(Integer);
		return 0;
	}

	boolean patternConservativeNonShortCircuitAnd(Object o) {
		return o instanceof String Integer & java.lang.Integer.MAX_VALUE > 0;
	}

	int patternConservativeOtherSwitchCase(Object o) {
		return switch (o) {
			case String Integer -> 0;
			default -> java.lang.Integer.MAX_VALUE;
		};
	}

	int patternElseOfNegated(Object o) {
		if (!(o instanceof String Integer))
			return 0;
		else
			return java.lang.Integer.MAX_VALUE;
	}

	int patternFinal(Object o) {
		if (o instanceof final String Integer)
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	int patternFlowScopeAfterNegatedReturn(Object o) {
		if (!(o instanceof String Integer))
			return 0;
		return java.lang.Integer.MAX_VALUE;
	}

	int patternIfThen(Object o) {
		if (o instanceof String Integer)
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	boolean patternOrOfNegated(Object o) {
		return !(o instanceof String Integer) || java.lang.Integer.MAX_VALUE > 0;
	}

	int patternRecordInstanceof(Object o) {
		if (o instanceof Box(Object Integer))
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	int patternSwitchGuard(Object o) {
		return switch (o) {
			case String Integer when java.lang.Integer.MAX_VALUE > 0 -> 1;
			default -> 0;
		};
	}

	int patternSwitchRecord(Object o) {
		return switch (o) {
			case Box(Object Integer) -> java.lang.Integer.MAX_VALUE;
			default -> 0;
		};
	}

	int patternSwitchType(Object o) {
		return switch (o) {
			case String Integer -> java.lang.Integer.MAX_VALUE;
			default -> 0;
		};
	}

	int patternTernary(Object o) {
		return o instanceof String Integer ? java.lang.Integer.MAX_VALUE : 0;
	}

	int patternWhileCondition(Object o) {
		while (o instanceof String Integer)
			return java.lang.Integer.MAX_VALUE;
		return 0;
	}

	void prefixShorterChain(Object System) {
		java.lang.System.out.println(System);
	}

	int primitiveBinder() {
		final var Integer = 0;
		return java.lang.Integer.MAX_VALUE + Integer;
	}

	void resource() {
		try (var Integer = new StringReader("")) {
			System.out.println(Integer);
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	void resourceDeclarationAnnotation() {
		try (@Cap(java.lang.Integer.MAX_VALUE) var Integer = new StringReader("")) {
			System.out.println(Integer);
		}
	}

	void resourceSecond() {
		try (var first = new StringReader(""); var Integer = new StringReader("")) {
			System.out.println(first);
			System.out.println(Integer);
			System.out.println(java.lang.Integer.MAX_VALUE);
		}
	}

	void staticCall(Object Thread) {
		java.lang.Thread.yield();
	}

	Object staticCallCharset(Object Charset) {
		return java.nio.charset.Charset.defaultCharset();
	}

	void switchArrowCaseBlockLocal(int k) {
		switch (k) {
			case 1 -> {
				final Object Integer = null;
				System.out.println(Integer);
				System.out.println(java.lang.Integer.MAX_VALUE);
			}
			default -> System.out.println(k);
		}
	}

	int ternaryBranch(boolean flag, Object Integer) {
		return flag ? java.lang.Integer.MAX_VALUE : 0;
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