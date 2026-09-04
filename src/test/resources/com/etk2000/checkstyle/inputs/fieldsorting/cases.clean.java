package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

enum InputFieldSortingCleanEnum {
	ALPHA,
	BETA,
	GAMMA;

	static final int MAX = 10;
	static final int MIN = 1;
}

enum InputFieldSortingCleanEnumOuter {
	ALPHA,
	BETA;

	enum Inner {
		FIRST,
		SECOND
	}
}

enum InputFieldSortingCleanEnumSingle {
	ONLY_ONE
}

enum InputFieldSortingCleanEnumSortedShapes {
	/* c */ ALPHA,
	ZETA
}

enum InputFieldSortingCleanEnumSortedJavadoc {
	/**
	 * alpha.
	 */
	ALPHA,
	/**
	 * zeta.
	 */
	ZETA
}

enum InputFieldSortingCleanEnumSortedAnnotationForms {
	@Deprecated
	ALPHA,
	@SuppressWarnings("/* not a comment */")
	BETA,
	@SuppressWarnings({"a", "b"})
	GAMMA,
	@SuppressWarnings(value = "unused")
	ZETA
}

enum InputFieldSortingCleanEnumSortedNestedTypeAfterTerminator {
	ALPHA,
	ZETA;

	interface Marker {
	}
}

enum InputFieldSortingCleanEnumSortedConstantBodies {
	ALPHA {
		// a note
	},
	ZETA {
		// z note
	}
}

enum InputFieldSortingCleanEnumWithBodies {
	ADD {
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	},
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	};

	abstract int apply(int a, int b);
}

enum InputFieldSortingCleanEnumWithMembers {
	APPLE("red"),
	BANANA("yellow"),
	CHERRY("red");

	final String color;

	InputFieldSortingCleanEnumWithMembers(String color) {
		this.color = color;
	}

	String getColor() {
		return color;
	}
}

enum InputFieldSortingCleanEnumWithSeparators {
	ALPHA,

	// Beta is the second letter
	BETA,

	@Deprecated
	GAMMA
}

enum InputFieldSortingCleanEnumWithTrailingComments {
	ALPHA, // first
	BETA; /* last */

	static final int MAX = 10;
}

enum InputFieldSortingCleanEnumWithHeaderComment { // note
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithAnnotationComment {
	@Deprecated
	// note
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithStructuralAnnotationArg {
	@SuppressWarnings("{ } , // /*")
	ALPHA,
	BETA
}

enum InputFieldSortingCleanEnumWithTextBlockArg {
	ALPHA("""
			x
			"""),
	BETA("""
			y
			""");

	InputFieldSortingCleanEnumWithTextBlockArg(String value) {
	}
}

class InputFieldSortingClean {
	enum InnerSorted {
		FIRST,
		SECOND,
		THIRD
	}

	static final int ALPHA = 1;
	static final int BETA = 2;
	static final long NOW = System.currentTimeMillis();
	static final long FUTURE = NOW + 1000;
	static final long PAST = NOW - 1000;
	static final String GAMMA = "g";
	static final Runnable STATIC_TASK = new Runnable() {
		@Override
		public void run() {
			System.out.println(GAMMA);
		}
	};

	static int delta;

	final int count = 0;
	final String name = "default";
	final Comparable<String> comparator = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return name.compareTo(o);
		}
	};
	final Runnable task = new Runnable() {
		@Override
		public void run() {
			System.out.println(name);
		}
	};

	final List<String> items;
	final Map<String, Integer> lookup;

	boolean active;
	byte flags;
	char letter;
	double ratio;
	double[] ratios;
	float scale;
	int index;
	int elements[];
	int[] indices;
	int[][] matrix;
	long timestamp;
	short code;
	java.util.concurrent.atomic.AtomicInteger counter;
	List<String> data;
	String label;
	String[] labels;

	InputFieldSortingClean(List<String> items, Map<String, Integer> lookup) {
		this.items = items;
		this.lookup = lookup;
	}
}

class InputFieldSortingCleanAnonAfterFieldTypeAgrees {
	final Object data = "x";
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			System.out.println("hi");
		}
	};
}

class InputFieldSortingCleanAnonAfterFieldTypeOverridden {
	final Runnable data = () -> System.out.println("x");
	final Comparable<Integer> action = new Comparable<>() {
		@Override
		public int compareTo(Integer o) {
			return 0;
		}
	};
}

class InputFieldSortingCleanAnonBodyReferencesEarlierField {
	final Map<String, Object> data = new HashMap<>();
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			data.clear();
		}
	};
}

class InputFieldSortingCleanAnonNestedGenerics {
	final Map<String, Object> data = new HashMap<>();
	final Object action = new HashMap<String, List<Integer>>() {
		@Override
		public void clear() {
			data.clear();
		}
	};
}

class InputFieldSortingCleanAnonCtorArgReadsEarlierPlain {
	final String label = "w";
	final Thread worker = new Thread(label) {
	};
}

class InputFieldSortingCleanAnonReadByLaterPlain {
	final Thread worker = new Thread() {
	};
	final String label = worker.getName();
}

class InputFieldSortingCleanAnonPairBodyForwardReference {
	final int count = 0;
	final Runnable alpha = new Runnable() {
		@Override
		public void run() {
			beta.run();
		}
	};
	final Runnable beta = new Runnable() {
		@Override
		public void run() {
			System.out.println("beta");
		}
	};
}

class InputFieldSortingCleanAnonPairCtorArgDependencyOverType {
	final TimerTask job = new TimerTask() {
		@Override
		public void run() {
		}
	};
	final Thread worker = new Thread(job) {
	};
}

class InputFieldSortingCleanAnonBodyReadsNonFinal {
	final String name = "x";
	final Runnable task = new Runnable() {
		@Override
		public void run() {
			++counter;
		}
	};

	int counter;
}

class InputFieldSortingCleanAnonChunks {
	final String label = "w";
	final Thread worker = new Thread() {
	};

	String note;
	Thread pending = new Thread() {
	};
}

class InputFieldSortingCleanAnonStatic {
	static final String LABEL = "w";
	static final Thread WORKER = new Thread() {
	};
	static final String WORKER_NAME = WORKER.getName();

	final Thread worker = new Thread() {
	};
}

class InputFieldSortingCleanAnonAfterSeveralPlains {
	final String alpha = "a";
	final String beta = "b";
	final Thread task = new Thread() {
	};
}

class InputFieldSortingCleanAnonEachReadByOwnPlain {
	final Runnable a = new Runnable() {
		@Override
		public void run() {
		}
	};
	final String ra = String.valueOf(a);
	final Runnable b = new Runnable() {
		@Override
		public void run() {
		}
	};
	final String rb = String.valueOf(b);
}

class InputFieldSortingCleanAnonTransitiveChain {
	final Thread worker = new Thread() {
	};
	final String label = worker.getName();
	final String name = label + "!";
}

class InputFieldSortingCleanAnonTransitiveThroughAnon {
	final Thread worker = new Thread() {
	};
	final Thread helper = new Thread(worker) {
	};
	final String label = helper.getName();
}

class InputFieldSortingCleanAnonFreePlainFirst {
	final String name = "n";
	final Thread worker = new Thread() {
	};
	final String label = worker.getName();
}

class InputFieldSortingCleanAnonOverridesAnnotation {
	@Deprecated
	final Runnable alpha = () -> System.out.println("a");
	final Runnable beta = new Thread() {
	};
}

class InputFieldSortingCleanAnonOverridesName {
	final Runnable beta = () -> System.out.println("b");
	final Runnable alpha = new Thread() {
	};
}

class InputFieldSortingCleanAnonOverridesTypeArgAnnotation {
	final Comparable<@Bnn String> alpha = o -> 0;
	final Comparable<@Ann String> beta = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return 0;
		}
	};
}

class InputFieldSortingCleanAnonBoundaries {
	final Object lock = new Object();
	final Runnable hook = () -> System.out.println("h");
	final Runnable printer = System.out::println;
	final String label = "w";
}

class InputFieldSortingCleanAnonMultiVarPlainFirst {
	final Object label = "w", task = new Thread() {
	};
}

class InputFieldSortingCleanAnonMultiVarReadByPlain {
	final Object label = "w", task = new Thread() {
	};
	final String name = String.valueOf(task);
}

@SuppressWarnings("FieldSorting")
class InputFieldSortingCleanAnonSuppressed {
	final Thread worker = new Thread() {
	};
	final String label = "w";
}

class InputFieldSortingCleanAnonConstructionTimeReadOverridesName {
	final Object beta = new Object() {
	};
	final Object alpha = new Object() {
		final String snapshot = String.valueOf(beta);
	};
}

class InputFieldSortingCleanAnonConstructionTimeReads {
	static final String LABEL = "w";
	static final Object STATIC_FIELD_HOLDER = new Object() {
		static final String COPY = LABEL;
	};
	static final Object STATIC_INIT_HOLDER = new Object() {
		static {
			System.out.println(LABEL);
		}
	};

	final String label = "w";
	final Object fieldHolder = new Object() {
		final String copy = label;
	};
	final Object initHolder = new Object() {
		final int seed = 1;

		{
			System.out.println(label);
		}
	};
	final Object lambdaHolder = new Object() {
		final Supplier<String> copy = () -> label;
	};
	final Object nestedHolder = new Object() {
		final Object inner = new Object() {
			final String copy = label;
		};
	};
}

class InputFieldSortingCleanAnonShadowRemovesLaterChunkDependency {
	final Object holder = new Object() {
		final long counter = 1;
		final long seed = counter;
	};

	long counter;
}

class InputFieldSortingCleanAnonShadowedMemberAfterField {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final String copy = label;
	};
}

class InputFieldSortingCleanAnonMultiVarAllReadersViaSibling {
	final Object anon = new Object() {
	};
	final int a = anon.hashCode(), b = a + 1;
}

@SuppressWarnings("FieldSorting")
enum InputFieldSortingCleanSuppressedEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings({"FieldSorting"})
enum InputFieldSortingCleanSuppressedArrayEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings({"FieldSorting", "unused"})
enum InputFieldSortingCleanSuppressedMixedEnum {
	ZEBRA,
	ALPHA
}

@SuppressWarnings("FieldSorting")
class InputFieldSortingCleanSuppressedClass {
	static String z;
	static int a;
	String name;
	int count;
}

@SuppressWarnings({"unused", "FieldSorting"})
enum InputFieldSortingCleanSuppressedKeySecond {
	ZEBRA,
	ALPHA
}

class InputFieldSortingCleanSuppressedOuter {
	@SuppressWarnings("FieldSorting")
	enum Inner {
		ZEBRA,
		ALPHA
	}
}

@SuppressWarnings(value = "FieldSorting")
enum InputFieldSortingCleanSuppressedExplicitValue {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"FieldSorting"})
enum InputFieldSortingCleanSuppressedExplicitValueArray {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"FieldSorting", "unused"})
enum InputFieldSortingCleanSuppressedExplicitValueMixed {
	ZEBRA,
	ALPHA
}

@SuppressWarnings(value = {"unused", "FieldSorting"})
enum InputFieldSortingCleanSuppressedExplicitValueMixedSecond {
	ZEBRA,
	ALPHA
}

@java.lang.SuppressWarnings("FieldSorting")
enum InputFieldSortingCleanSuppressedQualified {
	ZEBRA,
	ALPHA
}

enum InputFieldSortingCleanSuppressedConstantBody {
	@SuppressWarnings("FieldSorting")
	INSTANCE {
		String name;
		int count;
	}
}

@interface Ann {}

@interface Bnn {}

class InputFieldSortingCleanAnnotationOrder {
	@Deprecated
	int alpha;
	@SuppressWarnings("unused")
	int beta;

	@Deprecated
	long first, second;

	String plain;
	@Deprecated
	String annotated;
}

class InputFieldSortingCleanAnnotationMulti {
	@Ann
	@Bnn
	String abField;
	@Bnn
	String bField;
}

class InputFieldSortingCleanAnnotationConsolidated {
	@Deprecated
	String alpha, beta;
	@SuppressWarnings("unused")
	String delta, gamma;
}

class InputFieldSortingCleanAnnotationDifferentTypes {
	@SuppressWarnings("unused")
	int count;
	@Deprecated
	String name;
}

class InputFieldSortingCleanAnnotationQualified {
	String plain;
	@java.lang.Deprecated
	String qualified;
}

class InputFieldSortingCleanAnnotationStatic {
	static String plain;
	@Deprecated
	static String annotated;
}

@interface ParamAnn {
	int value() default 0;
}

class InputFieldSortingCleanAnnotationParams {
	@ParamAnn
	int noParams;
	@ParamAnn(1)
	int withParam;
	@ParamAnn(2)
	int withOtherParam;
}

class InputFieldSortingCleanTypeArgAnnotation {
	List<String> plain;
	List<@Ann String> annotated;
}

class InputFieldSortingCleanTypeArgAnnotationAlphabetical {
	List<@Ann String> aAnnotated;
	List<@Bnn String> bAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationCount {
	List<@Ann String> oneAnn;
	List<@Ann @Bnn String> twoAnns;
}

class InputFieldSortingCleanTypeArgAnnotationMultiArg {
	Map<String, Integer> plain;
	Map<String, @Ann Integer> annotatedSecond;
}

class InputFieldSortingCleanTypeArgAnnotationSameFallsToName {
	List<@Ann String> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationIdenticalMultiArg {
	Map<@Ann String, @Bnn Integer> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationMixedWithFieldAnnotation {
	List<@Ann String> plain;
	@Deprecated
	List<String> fieldAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationOverridesName {
	List<@Ann String> beta;
	List<@Bnn String> alpha;
}

class InputFieldSortingCleanTypeArgAnnotationPositionAware {
	Map<String, @Ann String> firstArgUnannotated;
	Map<@Ann String, String> firstArgAnnotated;
}

class InputFieldSortingCleanTypeArgAnnotationArray {
	List<String>[] plain;
	List<@Ann String>[] annotated;
}

class InputFieldSortingCleanTypeArgAnnotationWildcardBound {
	List<? extends @Ann Number> alpha, beta;
}

class InputFieldSortingCleanTypeArgAnnotationWildcardBoundDifferent {
	List<? extends @Ann Number> alpha;
	List<? extends @Bnn Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationLowerBound {
	List<? super @Ann Number> alpha;
	List<? super @Bnn Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationWildcard {
	List<@Ann ? extends Number> alpha;
	List<@Bnn ? extends Number> beta;
}

class InputFieldSortingCleanTypeArgAnnotationNested {
	Map<String, List<Integer>> alpha;
	Map<String, List<@Ann Integer>> beta;
}

@SuppressWarnings("PreferImport")
class InputFieldSortingCleanTypeArgAnnotationQualified {
	java.util.Set<String> plain;
	java.util.Set<@Ann String> annotated;
}

interface InputFieldSortingCleanInterface {
	int FIRST = 1;
	int SECOND = 2;
}

record InputFieldSortingCleanRecord(int zebra, int alpha) {
	static int a = 1;
	static int b = 2;
}

class InputFieldSortingCleanArrayCStyleSecondaryName {
	int alpha, zebra[];
	int beta;
}

class InputFieldSortingCleanInterleavedMultiVarSecondary {
	int alpha, zebra;
	static int s;
	int beta;
}

class InputFieldSortingCleanMultivarSecondaryNameDependency {
	int x, y, z;
	int a = y + 1;
}

class InputFieldSortingCleanFirstNameBeforeSingle {
	long alpha, zeta;
	long beta;
}

class InputFieldSortingCleanFirstNameBeforeMultiNameDeclaration {
	long alpha, zeta;
	long beta, gamma;
}

class InputFieldSortingCleanFirstNameDependencyOverridesOrder {
	long beta = 1;
	long alpha = beta, zeta;
}

class InputFieldSortingCleanSecondaryNameDependencyOverridesOrder {
	long beta = 1;
	long alpha, zeta = beta;
}

class InputFieldSortingCleanFirstNameBeforeThreeNameDeclaration {
	long beta, delta, gamma;
	long charlie;
}

class InputFieldSortingCleanInitCallReadsEarlierStatic {
	static final StringBuilder LOG = new StringBuilder();
	static final Object TIMEOUT = readTimeout();

	static Object readTimeout() {
		LOG.append("read");
		return LOG;
	}
}

class InputFieldSortingCleanInitQualifiedCallReadsEarlierStaticTransitively {
	static final StringBuilder LOG = new StringBuilder();
	static final Object TIMEOUT = InputFieldSortingCleanInitQualifiedCallReadsEarlierStaticTransitively.readTimeout();

	static Object pick(int depth) {
		return depth == 0 ? LOG : pick(depth - 1);
	}

	static Object readTimeout() {
		return pick(3);
	}
}

class InputFieldSortingCleanInitThisCallReadsEarlierField {
	final StringBuilder log = new StringBuilder();
	final Object timeout = this.readTimeout();

	Object readTimeout() {
		return log;
	}
}

class InputFieldSortingCleanInitOuterThisCallReadsEarlierField {
	final StringBuilder log = new StringBuilder();
	final Object timeout = InputFieldSortingCleanInitOuterThisCallReadsEarlierField.this.readTimeout();

	Object readTimeout() {
		return log;
	}
}

class InputFieldSortingCleanInitNewSelfConstructorReadsEarlierStatic {
	static final StringBuilder LOG = new StringBuilder();
	static final Object INSTANCE = new InputFieldSortingCleanInitNewSelfConstructorReadsEarlierStatic();

	InputFieldSortingCleanInitNewSelfConstructorReadsEarlierStatic() {
		LOG.append("created");
	}
}

class InputFieldSortingCleanInitNewSelfInitializerReadsEarlierStatic {
	static final StringBuilder LOG = new StringBuilder();
	static final Object INSTANCE = new InputFieldSortingCleanInitNewSelfInitializerReadsEarlierStatic();

	{
		LOG.append("created");
	}
}

class InputFieldSortingCleanInitNewSelfFieldReadsEarlierStatic {
	static final StringBuilder LOG = new StringBuilder();
	static final Object INSTANCE = new InputFieldSortingCleanInitNewSelfFieldReadsEarlierStatic();

	final int length = LOG.length();
}

record InputFieldSortingCleanInitNewSelfCompactConstructorReadsEarlierStatic(int value) {
	static final StringBuilder LOG = new StringBuilder();
	static final Object INSTANCE = new InputFieldSortingCleanInitNewSelfCompactConstructorReadsEarlierStatic(1);

	InputFieldSortingCleanInitNewSelfCompactConstructorReadsEarlierStatic {
		LOG.append(value);
	}
}

class InputFieldSortingCleanInitNewSelfSkipsStaticInitializers {
	static final Object INSTANCE = new InputFieldSortingCleanInitNewSelfSkipsStaticInitializers();
	static final StringBuilder LOG = new StringBuilder();
	static final StringBuilder TRACE = LOG;
}

class InputFieldSortingCleanPinnedAnonClassAnonBeforeFieldTypeAgrees {
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			System.out.println("hi");
		}
	};
	final Object data = "x";
}

class InputFieldSortingCleanPinnedAnonClassAnonBeforeFieldTypeOverridden {
	final Comparable<Integer> action = new Comparable<>() {
		@Override
		public int compareTo(Integer o) {
			return 0;
		}
	};
	final Runnable data = () -> System.out.println("x");
}

class InputFieldSortingCleanPinnedAnonClassAnonBodyReferencesLaterField {
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			data.clear();
		}
	};
	final Map<String, Object> data = new HashMap<>();
}

class InputFieldSortingAnonClassLambdaNotAnon {
	final Runnable action = () -> System.out.println("hello");
	final String name = "test";
}

class InputFieldSortingCleanPinnedAnonClassAnonInitializerNestedGenerics {
	final Object action = new HashMap<String, List<Integer>>() {
		@Override
		public void clear() {
			data.clear();
		}
	};
	final Map<String, Object> data = new HashMap<>();
}

class InputFieldSortingCleanPinnedAnonClassAnonOverridesAnnotation {
	final Runnable beta = new Thread() {
	};
	@Deprecated
	final Runnable alpha = () -> System.out.println("a");
}

class InputFieldSortingCleanPinnedAnonClassAnonOverridesName {
	final Runnable alpha = new Thread() {
	};
	final Runnable beta = () -> System.out.println("b");
}

class InputFieldSortingCleanPinnedAnonClassAnonOverridesTypeArgAnnotation {
	final Comparable<@TAnnA String> beta = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return 0;
		}
	};
	final Comparable<@TAnnB String> alpha = o -> 0;
}

class InputFieldSortingCleanPinnedAnonClassExemptionScopedToChunk {
	final Thread worker = new Thread() {
	};

	Thread pending = new Thread() {
	};
	String note = String.valueOf(worker);
}

class InputFieldSortingCleanPinnedAnonClassFreePlainWithPlainDependency {
	final String base = "b";
	final Thread worker = new Thread() {
	};
	final String label = base + "!";
}

class InputFieldSortingCleanPinnedAnonClassMultiVarAnonFirst {
	final Object task = new Thread() {
	}, label = "w";
}

class InputFieldSortingCleanPinnedAnonClassMultiVarAnonMixedPrecedesReaderMixed {
	final Object anon = new Object() {
	}, label = "w";
	final Object p = "1", q = anon;
}

class InputFieldSortingCleanPinnedAnonClassMultiVarMixedSatisfiable {
	final Object label = "w", task = new Thread() {
	};
	final Object alpha = "a";
}

class InputFieldSortingCleanPinnedAnonClassMultiVarReaderOtherChunkAnon {
	final Object anon0 = new Object() {
	};

	Thread anon2 = new Thread() {
	};
	int p = 1, q = anon0.hashCode();
}

class InputFieldSortingCleanPinnedAnonClassPairAnnotationOrder {
	@Deprecated
	final Thread alpha = new Thread() {
	};
	final Thread beta = new Thread() {
	};
}

class InputFieldSortingCleanPinnedAnonClassPairBodyReferenceNameOrder {
	final Runnable beta = new Runnable() {
		@Override
		public void run() {
			System.out.println("beta");
		}
	};
	final Runnable alpha = new Runnable() {
		@Override
		public void run() {
			beta.run();
		}
	};
}

class InputFieldSortingCleanPinnedAnonClassPairNameOrder {
	final Thread beta = new Thread() {
	};
	final Thread alpha = new Thread() {
	};
}

class InputFieldSortingCleanPinnedAnonClassPairTypeArgAnnotationOrder {
	final Comparable<@TAnnB String> alpha = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return 0;
		}
	};
	final Comparable<@TAnnA String> beta = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return 1;
		}
	};
}

class InputFieldSortingCleanPinnedAnonClassPairTypeOrder {
	final Thread action = new Thread() {
	};
	final Comparable<String> order = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return 0;
		}
	};
}

class InputFieldSortingCleanPinnedAnonClassReaderPlainBetweenAnons {
	final Thread worker = new Thread() {
	};
	final Thread helper = new Thread() {
	};
	final String label = helper.getName();
}

class InputFieldSortingCleanPinnedAnonClassShadowUnmasksNameOrder {
	final Object zeta = new Object() {
	};
	final Object alpha = new Object() {
		final Object zeta = "inner";
		final Object copy = zeta;
	};
}

class InputFieldSortingCleanPinnedFieldAnnotationIgnoresAtInInitializer {
	Object beta = x > 0 ? new @TypeUse Object() : null;
	Object alpha = "hello";
}

class InputFieldSortingCleanPinnedFieldConsolidationSkipsAnonymousClassWithPlainInstance {
	final Object first = new Object() {
	};
	final Object plain = "p";
}

class InputFieldSortingCleanPinnedFieldConsolidationSkipsAnonymousClassWithPlainStatic {
	private static final Object USER_LIST = new Object() {}.getClass();
	private static final Object RAW = String.class;
}

class InputFieldSortingCleanPinnedFieldConsolidationTwoAnonymousClassFieldsMerge {
	private static final Object ZETA = new Object() {};
	private static final Object ALPHA = new Object() {};
}

class InputFieldSortingCleanPinnedFieldExtractNamesInitializerAngleExplicitTypeArg {
	int yankee = List.<Integer>of().size(), zulu = 5;
	int alpha = 7;
}

class InputFieldSortingCleanPinnedInitCallWithoutRead {
	static final StringBuilder LOG = new StringBuilder();
	static final Object TIMEOUT = readTimeout();

	static Object readTimeout() {
		return "t";
	}
}

class InputFieldSortingCleanPinnedInitForwardReadAnonymousMemberShadow {
	private Object total = new Object() {
		final int count = 1;
		final int seen = count;
	};
	private int count = 3;
}

class InputFieldSortingCleanPinnedInitForwardReadInAnonymousMethod {
	private Object total = new Object() {
		@Override
		public String toString() {
			return String.valueOf(InputFieldSortingCleanPinnedInitForwardReadInAnonymousMethod.this.count);
		}
	};
	private int count = 3;
}

class InputFieldSortingCleanPinnedInitForwardReadThroughCalledMethod {
	static final Object TIMEOUT = readTimeout();
	static final StringBuilder LOG = new StringBuilder();

	static Object readTimeout() {
		return LOG;
	}
}

class InputFieldSortingCleanPinnedInitForwardReadThroughNewSelf {
	static final Object INSTANCE = new InputFieldSortingCleanPinnedInitForwardReadThroughNewSelf();
	static final StringBuilder LOG = new StringBuilder();

	InputFieldSortingCleanPinnedInitForwardReadThroughNewSelf() {
		LOG.append("created");
	}
}

class InputFieldSortingCleanPinnedInitNewSelfWithoutRead {
	static final StringBuilder LOG = new StringBuilder();
	static final Object INSTANCE = new InputFieldSortingCleanPinnedInitNewSelfWithoutRead();

	InputFieldSortingCleanPinnedInitNewSelfWithoutRead() {
		System.out.println("created");
	}
}

class InputFieldSortingCleanPinnedIdsTaken {
	static final class Ids {
		private static int last;

		static int next() {
			return ++last;
		}
	}

	private final int zeta = Ids.next();
	private final int alpha = Ids.next();
}

class InputFieldSortingCleanPinnedRunsBeforeLiteral {
	private int zeta = Integer.parseInt("1");
	private int alpha = 2;
}

class InputFieldSortingCleanPinnedLiteralBeforeRuns {
	private int zeta = 2;
	private int alpha = Integer.parseInt("1");
}

class InputFieldSortingCleanPinnedTypeOrder {
	private Object zeta = Integer.valueOf(Integer.parseInt("1"));
	private Integer alpha = 1;
}

class InputFieldSortingCleanPinnedAnnotationOrder {
	@Deprecated
	private int zeta = Integer.parseInt("1");
	private int alpha = 2;
}

class InputFieldSortingCleanPinnedWithinDeclaration {
	private int zeta = Integer.parseInt("1"), alpha = 2;
}

class InputFieldSortingCleanPinnedAnonymousClassMethodRunAtCreation {
	private int total = new Object() {
		int doubled() {
			return count * 2;
		}
	}.doubled();
	private int count = 3;
}

class InputFieldSortingCleanPinnedInnerClassReadsEarlierField {
	private final StringBuilder log = new StringBuilder("x");
	private final Recorder recorder = new Recorder();

	private class Recorder {
		private final int start = log.length();
	}
}

final class InputFieldSortingCleanPinnedMethodReferenceReadsEarlierField {
	private static final Locale LOCALE = Locale.forLanguageTag("tr");
	private static final List<String> KEYS = Stream.of("ID", "TITLE").map(InputFieldSortingCleanPinnedMethodReferenceReadsEarlierField::normalize).toList();

	private static String normalize(String key) {
		return key.toLowerCase(LOCALE);
	}
}

abstract class InputFieldSortingCleanPinnedTemplateMethodBase {
	protected final String describe() {
		return "unit=" + unit();
	}

	protected abstract String unit();
}

class InputFieldSortingCleanPinnedInheritedTemplateMethod extends InputFieldSortingCleanPinnedTemplateMethodBase {
	private final StringBuilder suffix = new StringBuilder("px");
	private final String label = describe();

	@Override
	protected String unit() {
		return suffix.toString();
	}
}

class InputFieldSortingCleanPinnedSuperConstructorBase {
	InputFieldSortingCleanPinnedSuperConstructorBase() {
		layout();
	}

	void layout() {
	}
}

class InputFieldSortingCleanPinnedAnonymousSuperConstructorCallsOverride {
	private final InputFieldSortingCleanPinnedSuperConstructorBase widget = new InputFieldSortingCleanPinnedSuperConstructorBase() {
		@Override
		void layout() {
			System.out.println(width);
		}
	};
	private final Integer width = 5;
}

class InputFieldSortingCleanPinnedLaterDeclarator {
	private int zeta = 1, mid = Integer.parseInt("1");
	int alpha = 2;
}

class InputFieldSortingCleanPinnedTypeArgAnnotationOrder {
	private List<@TAnnB String> zeta = new ArrayList<>(Integer.parseInt("1"));
	private List<@TAnnA String> alpha = new ArrayList<>();
}

final class InputFieldSortingCleanPinnedMethodReferenceReceiverCall {
	private static final Map<String, Integer> OVERRIDES = new HashMap<>();
	private static final Function<String, Integer> RESOLVE = buildIndex()::get;

	private static Map<String, Integer> buildIndex() {
		return new HashMap<>(OVERRIDES);
	}
}

final class InputFieldSortingCleanPinnedFactoryOfDeclaredTypeNamedLikeJdk {
	static final class Duration {
		private long millis;

		private Duration(long millis) {
			this.millis = millis;
		}

		static Duration ofMillis(long millis) {
			return CACHE.computeIfAbsent(millis, Duration::new);
		}
	}

	private static final Map<Long, Duration> CACHE = new HashMap<>();
	public static final Duration ZERO = Duration.ofMillis(0);
}

final class InputFieldSortingCleanPinnedStaticReadOfNestedEnum {
	enum Mode {
		FAST,
		SLOW;

		Mode() {
			ALL.add(this);
		}
	}

	static final Set<Mode> ALL = new HashSet<>();
	static final Mode DEFAULT = Mode.FAST;
}

class InputFieldSortingCleanPinnedCallQualifiedByOtherType {
	static final StringBuilder LOG = new StringBuilder();
	static final Object TIMEOUT = String.valueOf(3);

	static Object valueOf(int ignored) {
		return LOG;
	}
}

class InputFieldSortingCleanAnonymousInitBlockLocalNotADependency {
	final Object holder = new Object() {
		final int seed = 1;

		{
			final var label = "local";
			System.out.println(label);
		}
	};
	final String label = String.valueOf("w");
}

class InputFieldSortingCleanPinnedTypeArgAnnotationJdkFactory {
	List<@Zebra String> beta = List.of();
	List<String> alpha = List.of();
}

@SuppressWarnings("unused")
class InputFieldSortingCleanPinnedInitializerParenComma {
	int max = Math.max(1, 2);
	int alpha = 7;
}

class InputFieldSortingCleanPinnedTypeArgAnnotationSameAnnotationsJdkFactory {
	@Deprecated
	List<@TAnnA String> zebra = List.of();
	@Deprecated
	List<@TAnnA String> alpha = List.of();
}

final class InputFieldSortingCleanPinnedLambdaParameterNamedLikeLaterField {
	private final DoubleUnaryOperator discount = rate -> rate * 0.9;
	private final double rate = initialRate();

	private double initialRate() {
		return discount.applyAsDouble(100.0);
	}
}

final class InputFieldSortingCleanPinnedParenthesizedCall {
	record Widget(int width) {}

	private static final Map<String, Object> WIDGETS = new HashMap<>();
	private static final int CLOCK_WIDTH = ((Widget) register("clock", new Widget(120))).width();

	private static Object register(String id, Object widget) {
		WIDGETS.put(id, widget);
		return widget;
	}
}

final class InputFieldSortingCleanPinnedNestedTypeReadsEnclosingStatic {
	static final class Registry {
		static final List<String> NAMES = new ArrayList<>();
		static final int RATE = sampleRate;
	}

	static int sampleRate = Integer.getInteger("metrics.rate", 10);

	static {
		Registry.NAMES.add("default");
	}
}

class InputFieldSortingCleanInheritedMethodCallMatchingLaterFieldBase {
	int size() {
		return 2;
	}
}

class InputFieldSortingCleanInheritedMethodCallMatchingLaterField extends InputFieldSortingCleanInheritedMethodCallMatchingLaterFieldBase {
	private int total = this.size();
	private int size = 3;
}

class InputFieldSortingCleanMethodNameMatchingLaterField {
	private int total = List.of(1, 2).size();
	private int size = 3;
}

class InputFieldSortingCleanQualifiedReadOfOtherObject {
	private static InputFieldSortingCleanQualifiedReadOfOtherObject other;
	private int total = other.count * 2;
	private int count = 3;
}

class InputFieldSortingCleanQualifiedReadOfOtherTypeLiteralTarget {
	private static Object total = System.out;
	private static Object out = "o";
}

class InputFieldSortingCleanQualifiedReadOfOtherType {
	private static Object total = System.out;
	private static Object out = new Thread();
}

class InputFieldSortingCleanSuperReadBase {
	int count = 1;
}

class InputFieldSortingCleanSuperRead extends InputFieldSortingCleanSuperReadBase {
	private int total = super.count * 2;
	private int count = 3;
}

class InputFieldSortingCleanPinnedLambdaLocalWithNamedAnnotationElement {
	private final Runnable task = () -> {
		@SuppressWarnings(value = "unused") final var unused = 0;
	};
	private final Object marker = "m";
}

final class InputFieldSortingCleanPinnedThisInStringConcatenation {
	private final String user = System.getProperty("user.name");
	private final String tag = "session:" + this;

	@Override
	public String toString() {
		return user;
	}
}

class InputFieldSortingCleanOuterThisReadOfAShadowedName {
	int count;

	class Holder {
		int base = InputFieldSortingCleanOuterThisReadOfAShadowedName.this.count;
		int count = 0;
	}
}

class InputFieldSortingCleanOuterThisReadOfAShadowedNameAcrossChunks {
	int count;

	class Holder {
		final int base = InputFieldSortingCleanOuterThisReadOfAShadowedNameAcrossChunks.this.count;
		int count;
	}
}

class InputFieldSortingCleanSameSimpleNameOtherNestedType {
	static class Node {
		static int a = InputFieldSortingCleanSameSimpleNameOtherTree.Node.count;
		static int count = 5;
	}
}

class InputFieldSortingCleanSameSimpleNameOtherTree {
	static class Node {
		static int count = 1;
	}
}

class InputFieldSortingCleanOtherPackageOfTheOwnName {
	private static int total = other.pkg.InputFieldSortingCleanOtherPackageOfTheOwnName.count * 2;
	private static int count = 3;
}

enum InputFieldSortingCleanCaseLabelColor {
	BLUE,
	RED
}

class InputFieldSortingCleanDivisionByAFoldedZero {
	static final Object REGISTRY = System.getProperty("registry");
	static final int BAD = 1 / (0 + 0);
}

class InputFieldSortingCleanNegativeArrayDimension {
	static Object registry = "r";
	static int[] bad = new int[-1];
}

class InputFieldSortingCleanArrayDimensionPastTheCap {
	static Object registry = "r";
	static int[] huge = new int[65537];
}

class InputFieldSortingCleanCaseLabelNamingAConstantVariable {
	static final int LIMIT = 5;
	static final int CODE = switch (Integer.getInteger("x", 0)) {
		case LIMIT -> 1;
		default -> 0;
	};
}

class InputFieldSortingCleanCaseLabelNamedLikeALaterField {
	final InputFieldSortingCleanCaseLabelColor color = InputFieldSortingCleanCaseLabelColor.RED;
	final int code = switch (color) {
		case RED -> 1;
		default -> 0;
	};
	String RED;
}