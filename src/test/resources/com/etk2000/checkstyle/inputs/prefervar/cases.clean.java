package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

class InputPreferVarClean {
	static class Api {
		static class Cache<K, V> {
		}
	}

	static class Remap {
		static class Swapped<B, A> extends Pairing<A, B> {
		}
	}

	static class Holder {
		static class Slot<A, B> implements InputPreferVarClean.Slot<String> {
		}
	}

	interface Slot<T> {
	}

	static class Pairing<A, B> {
	}

	static class Impl {
		static class Cache<V> extends Api.Cache<String, V> {
		}
	}

	static class MyFunc<T> implements Function<T, Integer> {
		@Override
		public Integer apply(T value) {
			return 0;
		}
	}

	static class MyPair<A> extends Src<A, Integer> {
	}

	static class Nest<A> {
		class Inner<B> {
		}

		class Mid<B> {
			class Deep<C> {
			}
		}

		class Plain {
		}
	}

	static class Point {
		int x;
	}

	static class Src<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	@interface Ann {
	}

	static {
		final var inStaticInit = 5;
		System.out.println(inStaticInit);
	}

	String field = "not flagged";

	void annotatedLocalVariable() {
		@Nonnull
		final var s = "hello";
	}

	void boxedTypeFromLiteralUnboxes() {
		final Boolean flag = true;
		final Boolean boxedFalse = false;
		final Byte b = 5;
		final Character c = 'a';
		final Double d = 5.0;
		final Float f = 5f;
		final Integer i = 5;
		final Long l = 5L;
		final Short s = 5;
		final Integer parsed = Integer.parseInt("1");
		final Long len = Long.parseLong("2");
		final Integer cast = (Integer) new Object();
		final Integer decoded = Integer.decode("1");
		final Integer alias = parsed;
		final java.lang.Integer qualifiedBox = 5;
		final java.lang.Long qualifiedBoxLong = 5L;
	}

	void callOnAnUnresolvableBareMethod() {
		final String name = absentFactory();
		System.out.println(name);
	}

	void conditionalDiamondWhoseClassCannotTakeTheDeclaredArguments(boolean flag, int key) {
		final Function<String, Integer> everyArmTernary = flag ? new MyFunc<>() : new MyFunc<>();
		final Function<String, Integer> everyArmSwitch = switch (key) { default -> new MyFunc<>(); };
		final Src<String, Integer> firstArmCannotTake = flag ? new MyPair<>() : new Src<>();
		final Src<String, Integer> secondArmCannotTake = flag ? new Src<>() : new MyPair<>();
		final Src<String, Integer> switchArmCannotTake = switch (key) { case 1 -> new Src<>(); default -> new MyPair<>(); };
		final Src<String, Integer> nestedArmCannotTake = flag ? new Src<>() : key == 1 ? new Src<>() : new MyPair<>();
		final java.util.function.Function<String, Integer> qualifiedArmCannotTake = flag ? new MyFunc<>() : new MyFunc<>();
		final java.util.List<String> parenthesisedCondition = (flag) ? new ArrayList<>() : new ArrayList<>();
		System.out.println(everyArmTernary + "" + everyArmSwitch + parenthesisedCondition);
		System.out.println(firstArmCannotTake + "" + secondArmCannotTake + switchArmCannotTake);
		System.out.println(nestedArmCannotTake + "" + qualifiedArmCannotTake);
	}

	void conditionalInitializerColonFormSwitch(int key) {
		final List<String> colonForm = switch (key) {
			case 1:
				yield new ArrayList<>();
			default:
				yield new ArrayList<>();
		};
		System.out.println(colonForm);
	}

	void conditionalInitializerWithNonNewArm(boolean flag, int key) {
		final List<String> ternary = flag ? new ArrayList<>() : Collections.emptyList();
		final List<String> switched = switch (key) { case 1 -> new ArrayList<>(); default -> Collections.emptyList(); };
		final List<String> blockRule = switch (key) { case 1 -> { yield new ArrayList<>(); } default -> new ArrayList<>(); };
		final java.util.List<String> qualifiedTernary = flag ? new ArrayList<>() : Collections.emptyList();
		System.out.println(ternary + "" + switched + blockRule + qualifiedTernary);
	}

	void declarationShapesTheCheckLeavesAlone(Point point) {
		final var diamond = new ArrayList<>();
		final var typed = new ArrayList<String>();
		point.x = 5;
		System.out.println(point.x);
		System.out.println(diamond.size() + typed.size());
	}

	void declaredTypeArgumentsExceedConstructedClassArity() {
		// Properties declares no type parameters, so the two supplied have no diamond to move to
		final Map<Object, Object> props = new Properties();
		System.out.println(props.size());
	}

	void declaredTypeArgumentsOfANestedQualifierSegment(@Nonnull Nest<Object>.Mid<String>.Deep<Integer> deep) {
		final Nest<Object>.Mid<String>.Deep<Integer> nested = deep;
		System.out.println(nested);
	}

	void declaredTypeArgumentsOfAnInnerGenericClass(@Nonnull Nest<Integer> outer) {
		final Nest<Integer>.Inner<?> inner = outer.new Inner<String>();
		System.out.println(inner);
	}

	void declaredTypeArgumentsOfAQualifierSegment(@Nonnull Nest<String> outer) {
		final Nest<String>.Plain plain = outer.new Plain();
		System.out.println(plain);
	}

	void diamondWhoseClassCannotTakeTheDeclaredArguments() {
		// MyFunc declares one type parameter, Function supplies two
		final Function<String, Integer> converter = new MyFunc<>();
		System.out.println(converter);
	}

	void diamondWithConstructorArguments() {
		final var sized = new ArrayList<Object>(16);
		final var tuned = new HashMap<Object, Object>(8, 0.5f);
		System.out.println(sized.size() + tuned.size());
	}

	void forEach() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
		for (final var item : list)
			System.out.println(item);
	}

	void forEachAnnotated() {
		final var list = List.of("a", "b");
		for (@Nonnull var item : list)
			System.out.println(item);
		for (@Nonnull final var item : list)
			System.out.println(item);
	}

	void forEachBoxedElementType(@Nonnull List<Integer> counts, int[] sizes) {
		for (Integer count : counts)
			System.out.println(count);
		for (Integer boxed : sizes)
			System.out.println(boxed);
	}

	void forEachGenericType() {
		final var map = Map.of("a", 1);
		for (var entry : map.entrySet())
			System.out.println(entry);
	}

	void forEachPrimitiveElementType(@Nonnull List<Integer> counts) {
		for (int count : counts)
			System.out.println(count);
		for (long scaled : counts)
			System.out.println(scaled);
		for (double ratio : counts)
			System.out.println(ratio);
	}

	void forEachPrimitiveElementTypeOverANonIdentIterable() {
		for (int size : new int[]{1, 2})
			System.out.println(size);
	}

	void forEachWidenedPrimitiveElementTypeOverAnArray(int[] sizes) {
		for (long scaled : sizes)
			System.out.println(scaled);
		for (double ratio : sizes)
			System.out.println(ratio);
	}

	void forEachWideningElementType(@Nonnull List<String> names, @Nonnull List<Integer> counts) {
		for (Object each : names)
			System.out.println(each);
		for (CharSequence each : names)
			System.out.println(each);
		for (Comparable<String> each : names)
			System.out.println(each);
		for (Serializable each : names)
			System.out.println(each);
		for (Number each : counts)
			System.out.println(each);
	}

	void forEachWideningSupertypeArrayElementType(@Nonnull List<String[]> rows) {
		for (Object[] row : rows)
			System.out.println(row.length);
		for (CharSequence[] row : rows)
			System.out.println(row.length);
	}

	void forLoopInit() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void forLoopInitReferenceType() {
		final var list = List.of("a", "b");
		for (var it = list.iterator(); it.hasNext(); )
			System.out.println(it.next());
	}

	void genericTypeArgumentsFromAQualifiedSource(
			@Nonnull Map<String, java.lang.Object[]> arrayArg,
			@Nonnull Map<String, java.util.List<?>> nestedWildcard,
			@Nonnull Map<String, Nest<Object>.Mid<String>.Deep<Integer>> deeplyQualified
	) {
		final Map<String, java.lang.Object[]> qualifiedArrayArg = arrayArg;
		final Map<String, java.util.List<?>> qualifiedNestedWildcard = nestedWildcard;
		final Map<String, Nest<Object>.Mid<String>.Deep<Integer>> qualifierSegmentWidened = deeplyQualified;
		System.out.println(qualifiedArrayArg + "" + qualifiedNestedWildcard + qualifierSegmentWidened);
	}

	void genericTypeArgumentsPinnedByDeclaration() {
		final List<?> wildcard = List.of(1, 2);
		final List<?> empty = List.of();
		final List<String> targetTyped = List.of();
		final Map<String, List<Integer>> targetTypedNested = Map.of();
		final Map<String, List<?>> nestedWildcard = Map.of();
		final Map<String, Object> widenedArgs = Map.of("k", 3);
		final List<Number> widenedList = List.of(1, 2);
		final Map<String, List<Object>> nestedWidened = Map.of("k", List.of(1));
		final List<Map<String, Object>> deeplyWidened = List.of(Map.of("k", 1));
		final List rawDiamond = new ArrayList<>();
		final List<String> emptyFactory = Collections.emptyList();
		final java.util.List<java.lang.Object> qualifiedWidened = List.of(1, 2);
		final java.util.List<?> qualifiedWildcard = List.of(1, 2);
		final java.util.Map<String, Object> qualifiedWidenedArgs = Map.of("k", 3);
		final List<java.io.Serializable> qualifiedSerializableArg = List.of(1, 2);
		final List<java.lang.CharSequence> qualifiedCharSequenceArg = List.of("a", "b");
		final List<java.lang.Number> qualifiedNumberArg = List.of(1, 2);
		final Map<String, java.util.List<Object>> qualifiedNestedWidened = Map.of("k", List.of(1));
	}

	void knownParseMethodWithVar() {
		final var bp = Boolean.parseBoolean("true");
		final var byp = Byte.parseByte("5");
		final var dp = Double.parseDouble("5.0");
		final var fp = Float.parseFloat("5.0");
		final var ip = Integer.parseInt("5");
		final var lp = Long.parseLong("5");
		final var sp = Short.parseShort("5");
	}

	void literalTypeMismatchUnfixable() {
		final byte b = 5;
		final byte bBin = 0b101;
		final byte bHex = 0xF;
		final byte bNeg = -5;
		final byte bOct = 07;
		final byte bPlus = +5;
		final byte bSep = 1_2;
		final short s = 5;
		final short sBin = 0b101;
		final short sHex = 0xFF;
		final short sNeg = -5;
		final short sOct = 077;
		final short sPlus = +5;
		final short sSep = 1_000;
		final char ci = 65;
		final char ciHex = 0x41;
		final int ic = 'a';
		final int icEscape = '\n';
		final int icNeg = -'a';
		final int icUnicode = '\u0041';
		final double dc = 'a';
		final float fc = 'a';
		final long lc = 'a';
		final float fl = 5L;
		final double di = 5;
		final double diBin = 0b101;
		final double diHex = 0xFF;
		final double diNeg = -5;
		final double diOct = 077;
		final double diSep = 1_000;
		final float fi = 5;
		final float fiBin = 0b101;
		final float fiHex = 0xFF;
		final float fiNeg = -5;
		final float fiOct = 077;
		final float fiSep = 1_000;
		final long li = 5;
		final long liBin = 0b101;
		final long liHex = 0xFF;
		final long liNeg = -5;
		final long liOct = 077;
		final long liSep = 1_000;
		final double df = 5.0f;
		final double dfUpper = 5.0F;
		final double dfExp = 5e2f;
		final double dfNeg = -5.0f;
		final double dfSep = 1_000.0f;
		final double dl = 5L;
		final double dlNeg = -5L;
		final double dlSep = 1_000L;
		final float fd = 5.0;
	}

	void localVariableNoInit() {
		final int x;
		final String s;
	}

	void localVariableNoInitMultiVar() {
		final int x, y;
	}

	void localVariables() {
		final var x = 42;
		final Runnable emptyAnon = new Runnable() {};
		final Runnable twoMethodAnon = new Runnable() {
			public void other() {
			}

			@Override
			public void run() {
			}
		};
		System.out.println(emptyAnon + "" + twoMethodAnon);
		final var s = "hello";
		final var list = List.of(1, 2, 3);
		final String nullStr = null;
		final String castNull = (String) null;
		final String nestedCastNull = (String) (Object) null;
		final String parenNull = (null);
		final String castParenNull = (String) (null);
		final Runnable rParen = (() -> System.out.println("hi"));
		final Function<String, Integer> fParen = (String::length);
		final Runnable parenAnon = (new Runnable() {
			@Override
			public void run() {}
		});
		final Runnable explicitComplexAnon = new Runnable() {
			int count;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
		final int uninitialized;
		final int[] numbers = {1, 2, 3};
		final int[][] matrix = {{1, 2}, {3, 4}};
		final String[] names = {"a", "b"};
		final var sized = new String[5];
		final Runnable r = () -> System.out.println("hello");
		final Supplier<String> s2 = () -> "world";
		final Function<String, Integer> f = String::length;
		final Runnable anon = new Runnable() {
			@Override
			public void run() {
				System.out.println("anonymous");
			}
		};
		final var complexAnon = new Runnable() {
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void newAnonymousClassWithNonObjectTypeArg() {
		final var cmp = new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return 0;
			}
		};
	}

	void newWithAnnotatedObjectTypeArg() {
		final var list = new ArrayList<@Ann Object>();
	}

	void newWithConstructorArgs() {
		final var list = new ArrayList<String>(16);
		final var map = new HashMap<String, Integer>(8, 0.5f);
	}

	void newWithDiamondAlreadyUsed() {
		final var list = new ArrayList<>();
		final var map = new HashMap<>();
	}

	void newWithFirstArgObjectSecondNot() {
		final var map = new HashMap<Object, String>();
	}

	void newWithMixedTypeArgs() {
		final var map = new HashMap<String, Object>();
	}

	void newWithNonObjectQualifiedTypeArg() {
		final var list = new ArrayList<java.lang.String>();
	}

	void newWithNonObjectTypeArg() {
		final var list = new ArrayList<String>();
		final var map = new HashMap<String, Integer>();
	}

	void newWithoutTypeArgs() {
		final var obj = new Object();
	}

	void qualifiedNewWhoseSameFileClassRemapsItsTypeParameters() {
		// Swapped takes two parameters like Pairing, but passes them up reversed
		final Pairing<String, Integer> remapped = new Remap.Swapped<>();
		System.out.println(remapped);
	}

	void qualifiedNewWhoseSameFileClassTakesADifferentArity() {
		// Holder.Slot declares two parameters, and Absent.Slot is not declared here at all
		final Slot<String> collided = new Holder.Slot<>();
		final Slot<String> offFile = new Absent.Slot<>();
		System.out.println(collided + "" + offFile);
	}

	void qualifiedNewWhoseSimpleNameCollidesWithTheDeclaredType() {
		// Impl.Cache takes one parameter, not two, despite the matching simple names
		final Api.Cache<String, Integer> cache = new Impl.Cache<>();
		System.out.println(cache);
	}

	void reassignedWideningDeclarationStaysSilent(boolean flag) {
		Object widened = new StringBuilder();
		if (flag)
			widened = "x";
		List<?> wildcard = new ArrayList<String>();
		if (flag)
			wildcard = new LinkedList<>();
		System.out.println(widened + "" + wildcard);
	}

	void supertypeDeclarationWidensTheValue() {
		final Object literal = "x";
		final Object constructed = new StringBuilder();
		final Object qualified = new java.util.concurrent.atomic.AtomicInteger();
		final Object fromCall = compute();
		final Object[] widenedArray = new String[0];
		final CharSequence[] widenedCharArray = new String[0];
		final Serializable ser = "z";
		final CharSequence text = "y";
		final Number count = 1;
		final Comparable<String> cmp = "z";
		final java.lang.Object qualifiedLiteral = "x";
		final java.io.Serializable qualifiedSerializable = "z";
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}

	void tryWithResourcesAnnotated() throws Exception {
		try (@Nonnull var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}

	void tryWithResourcesAnonymousClassBody() throws Exception {
		try (Src<String, Integer> anon = new Src<String, Integer>() {
			@Override
			public void close() {
			}
		}) {
			System.out.println(anon);
		}
	}

	void tryWithResourcesConditionalArms(boolean flag, @Nonnull Src<String, Integer> existing) throws Exception {
		try (Src<String, Integer> nonNewArm = flag ? new Src<>() : existing) {
			System.out.println(nonNewArm);
		}
		try (Src<String, Integer> armCannotTake = flag ? new Src<>() : new MyPair<>()) {
			System.out.println(armCannotTake);
		}
	}

	void tryWithResourcesDeclaredSupertype() throws Exception {
		try (InputStream in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}

	void tryWithResourcesExistingVariable(@Nonnull ByteArrayInputStream existing) throws Exception {
		try (existing) {
			System.out.println(existing.read());
		}
	}

	void tryWithResourcesMultipleReferences(@Nonnull ByteArrayInputStream a, @Nonnull ByteArrayInputStream b) throws Exception {
		try (a; b) {
			System.out.println(a.read() + b.read());
		}
	}

	void tryWithResourcesNarrowingClose(@Nonnull String text) throws Exception {
		try (Reader reader = new StringReader(text)) {
			System.out.println(reader.read());
		}
	}

	void tryWithResourcesWhoseClassCannotTakeTheDeclaredArguments() throws Exception {
		// MyPair declares one type parameter, so both declared arguments cannot move
		try (Src<String, Integer> pair = new MyPair<>()) {
			System.out.println(pair);
		}
	}

	void tryWithResourcesWidenedTypeArgument(@Nonnull Src<String, Object> existing) throws Exception {
		try (Src<String, Object> widened = existing) {
			System.out.println(widened);
		}
	}

	void tryWithResourcesWildcardTypeArgument() throws Exception {
		try (Src<?, ?> pair = new MyPair<String>()) {
			System.out.println(pair);
		}
	}
}

class InputPreferVarGenericReturnClean {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	static <T> int count(T value) {
		return 0;
	}

	static <T> T findByType(Class<T> type) {
		return null;
	}

	void autoDetectedGenericExplicitType() {
		final String s = cast("hello");
	}

	void genericMethodReturningAPrimitive() {
		final var n = count("a");
		System.out.println(n);
	}

	void inferableFromParamType() {
		final var s = findByType(String.class);
	}
}

class InputPreferVarReflectionClean {
	List<String> field = List.of();

	void explicitTypeOnGeneric() {
		final List<String> list = Collections.emptyList();
		final Optional<Integer> opt = Optional.empty();
	}

	void inferableFromArgs() {
		final var list = List.of("a", "b");
		final var min = Collections.min(list);
	}

	void instanceCallClassLevelTypeParam(List<String> items) {
		final var first = items.getFirst();
	}

	void instanceCallViaField() {
		final var first = field.getFirst();
	}

	void instanceCallViaLocal() {
		final var list = List.of("a");
		final var first = list.getFirst();
	}

	void instanceCallViaParam(List<String> items) {
		final var size = items.size();
	}
}

class InputPreferVarChainClean {
	void chainClassLevelTypeParam(List<String> list) {
		final var first = Collections.unmodifiableList(list).getFirst();
	}

	void chainNonGeneric(List<String> list) {
		final var size = Collections.unmodifiableList(list).size();
	}
}