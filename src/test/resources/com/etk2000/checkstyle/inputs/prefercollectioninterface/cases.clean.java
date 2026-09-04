package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.AbstractList;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.xml.transform.Transformer;

class InputCollectionInterfaceClean {
	static int count() {
		return 0;
	}

	static void doNothing() {}

	static List<String> getItems() {
		return List.of();
	}

	static Map<String, Integer> getMap() {
		return Map.of();
	}

	static List<String> process(Set<Integer> items) {
		return List.of();
	}

	static void processCollection(Collection<String> items) {}

	static void processMap(Map<String, Integer> items) {}

	static void processMultiple(List<String> a, Map<String, Integer> b) {}

	static void processSet(Set<String> items) {}

	static void processString(String text) {}
}

class InputCollectionInterfaceLocalVar {
	static void method() {
		final var list = new ArrayList<String>();
		final var map = new HashMap<String, Integer>();
		final var set = new HashSet<String>();
	}
}

class InputCollectionInterfaceField {
	final List<String> list = new ArrayList<>();
	int size;
}

class InputCollectionInterfaceConstructor {
	final List<String> items;
	int size;

	InputCollectionInterfaceConstructor(List<String> items) {
		this.items = items;
	}
}

class InputCollectionInterfaceNestedGeneric {
	static Map<String, List<Integer>> nested() {
		return Map.of();
	}

	static void process(Map<String, Set<Integer>> items) {}
}

class InputCollectionInterfaceDequeParam {
	static void process(Deque<String> items) {}
}

class InputCollectionInterfaceQueueParam {
	static void process(Queue<String> items) {}
}

class InputCollectionInterfaceAbstractParam {
	static void process(AbstractList<String> items) {}
}

@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawInterface {
	static List getItems() {
		return List.of();
	}
}

class InputCollectionInterfaceLinkedList {
	static LinkedList<String> asList() {
		return new LinkedList<>();
	}
}

class InputCollectionInterfaceFqnClean {
	static java.util.List<String> fqnInterface() {
		return java.util.List.of();
	}
}

class InputCollectionInterfaceAnnotatedTypeArg {
	static void process(List<@SuppressWarnings("unused") String> items) {}

	static void processAnnotatedGenericArg(Set<@SuppressWarnings("unused") String> items) {}
}

class InputCollectionInterfaceWildcard {
	static void processExtends(List<? extends Number> items) {}

	static void processSuper(List<? super Integer> items) {}
}

class InputCollectionInterfaceMultiLevelNesting {
	static Map<String, Map<Integer, List<String>>> deepNested() {
		return Map.of();
	}
}

class InputCollectionInterfaceBoundedTypeParam {
	static <T extends Comparable<T>> List<T> sorted(List<T> items) {
		return items;
	}
}

class InputCollectionInterfaceIntersectionBound {
	static <T extends Comparable<T> & java.io.Serializable> List<T> sorted(List<T> items) {
		return items;
	}
}

class InputCollectionInterfaceConcreteInBound {
	static <T extends ArrayList<String>> void process(List<T> items) {}
}

class InputCollectionInterfaceOverloadWouldCollapse {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}

	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseIsPositional {
	static void pair(ArrayList<String> first, List<String> second) {
		System.out.println(first);
		System.out.println(second);
	}

	static void pair(List<String> first, ArrayList<String> second) {
		System.out.println(first);
		System.out.println(second);
	}
}

class InputCollectionInterfaceOverloadCollapseAcrossConcreteTypes {
	static void dump(HashSet<String> values) {
		System.out.println(values);
	}

	static void dump(TreeSet<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseArrayParam {
	static void dump(ArrayList[] values) {
		System.out.println(values.length);
	}

	static void dump(List[] values) {
		System.out.println(values.length);
	}
}

class InputCollectionInterfaceOverloadCollapseFqnConcrete {
	static void dump(List<String> values) {
		System.out.println(values);
	}

	static void dump(java.util.ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseFqnInterface {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}

	static void dump(java.util.List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseSameFileParam {
	static class Marker {
	}

	static void dump(ArrayList<String> values, Marker marker) {
		System.out.println(values);
		System.out.println(marker);
	}

	static void dump(List<String> values, Marker marker) {
		System.out.println(values);
		System.out.println(marker);
	}
}

class InputCollectionInterfaceCtorOverloadWouldCollapse {
	InputCollectionInterfaceCtorOverloadWouldCollapse(ArrayList<String> values) {
		System.out.println(values);
	}

	InputCollectionInterfaceCtorOverloadWouldCollapse(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseVarargsParam {
	static void dump(ArrayList<String>... values) {
		System.out.println(values.length);
	}

	static void dump(List<String>... values) {
		System.out.println(values.length);
	}
}

class InputCollectionInterfaceOverloadCollapseVarargsAgainstArray {
	static void render(ArrayList<String>[] values) {
		System.out.println(values.length);
	}

	static void render(List<String>... values) {
		System.out.println(values.length);
	}
}

class InputCollectionInterfaceCollapseForwardSub extends InputCollectionInterfaceCollapseForwardBase {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceCollapseForwardBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceCollapseCycleFirst extends InputCollectionInterfaceCollapseCycleSecond {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceCollapseCycleSecond extends InputCollectionInterfaceCollapseCycleFirst {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

record InputCollectionInterfaceRecordComponentCollapse(ArrayList<String> items) {
	InputCollectionInterfaceRecordComponentCollapse(List<String> items) {
		this(new ArrayList<>(items));
	}
}

class InputCollectionInterfaceInnerTypeSpellingCollapse {
	static class Box {}

	void f(Box box, ArrayList<String> values) {
		System.out.println(box);
		System.out.println(values);
	}

	void f(InputCollectionInterfaceInnerTypeSpellingCollapse.Box box, List<String> values) {
		System.out.println(box);
		System.out.println(values);
	}
}

class InputCollectionInterfaceTypeVariableCollapse {
	void f(Object value, List<String> values) {
		System.out.println(value);
		System.out.println(values);
	}

	<T> void f(T value, ArrayList<String> values) {
		System.out.println(value);
		System.out.println(values);
	}
}

abstract class InputCollectionInterfaceCrossFileCollapse extends AbstractMap<String, Integer> {
	public final void putAll(HashMap<String, Integer> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfacePrivateSubtypeBase {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfacePrivateSubtypeSub extends InputCollectionInterfacePrivateSubtypeBase {
	private static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceIntersectionBoundCollapse {
	void f(Comparable<String> value, List<String> values) {
		System.out.println(value);
		System.out.println(values);
	}

	<T extends Comparable<String> & Serializable> void f(T value, ArrayList<String> values) {
		System.out.println(value);
		System.out.println(values);
	}
}

record InputCollectionInterfaceVarargsComponentCollapse(ArrayList<String>... rows) {
	InputCollectionInterfaceVarargsComponentCollapse(List<String>... rows) {
		this(new ArrayList[0]);
	}
}

record InputCollectionInterfaceCanonicalCtorCollapse(List<String> rows) {
	InputCollectionInterfaceCanonicalCtorCollapse(ArrayList<String> rows) {
		this((List<String>) rows);
	}
}

class InputCollectionInterfaceInheritedMemberBase {
	static class InheritedMember {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceInheritedMemberSub extends InputCollectionInterfaceInheritedMemberBase {
	static class InheritedMemberUser extends InheritedMember {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceQualifiedSupertypeOuter {
	static class QualifiedBase {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}

	static class QualifiedSub extends InputCollectionInterfaceQualifiedSupertypeOuter.QualifiedBase {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceUnresolvableQualifiedName {
	com.example.model.ArrayList build() {
		return null;
	}
}

// the Graph* types below carry no collection type at all, so this check is silent on them
// whatever it decides. They exist for TypeGraphTest, which reads this file directly
class InputCollectionInterfaceGraphAnonymousBase {
	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceGraphAnonymousHolder {
	final InputCollectionInterfaceGraphAnonymousBase held = new InputCollectionInterfaceGraphAnonymousBase() {
		void extra() {}
	};
}

enum InputCollectionInterfaceGraphEnum {
	ONE {
		void extra() {}
	};

	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceGraphLocalHolder {
	void m() {
		class GraphLocalBase {
			void dump(List<String> values) {
				System.out.println(values);
			}
		}

		class GraphLocalSub extends GraphLocalBase {
			void extra() {}
		}

		System.out.println(new GraphLocalSub());
	}
}

interface InputCollectionInterfaceGraphDiamondIface {
	default void other() {}
}

class InputCollectionInterfaceGraphDiamondBase {
	void extra() {}
}

class InputCollectionInterfaceGraphDiamondSub extends InputCollectionInterfaceGraphDiamondBase
		implements InputCollectionInterfaceGraphDiamondIface {}

abstract class InputCollectionInterfaceFqnCrossFileCollapse extends java.util.AbstractMap<String, Integer> {
	public final void putAll(HashMap<String, Integer> values) {
		System.out.println(values);
	}
}

abstract class InputCollectionInterfaceOverridesClasspathMethod extends Transformer {
	@Override
	public final Properties getOutputProperties() {
		return new Properties();
	}

	@Override
	public final void setOutputProperties(Properties properties) {
		System.out.println(properties);
	}
}

class InputCollectionInterfaceClassTypeVariableCollapse<T> {
	void f(Object value, List<String> values) {
		System.out.println(value);
		System.out.println(values);
	}

	void f(T value, ArrayList<String> values) {
		System.out.println(value);
		System.out.println(values);
	}
}

abstract class InputCollectionInterfaceCrossFileImplementsCollapse implements Map<String, Integer> {
	public final void putAll(HashMap<String, Integer> values) {
		System.out.println(values);
	}
}

abstract class InputCollectionInterfaceSecondClauseCollapse implements Serializable, Map<String, Integer> {
	public final void putAll(HashMap<String, Integer> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceTransitiveMemberBase {
	static class TransitiveMember {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceTransitiveMemberMid<T> extends InputCollectionInterfaceTransitiveMemberBase {}

class InputCollectionInterfaceTransitiveMemberSub extends InputCollectionInterfaceTransitiveMemberMid<String> {
	static class TransitiveMemberUser extends TransitiveMember {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

interface InputCollectionInterfaceIfaceMemberHolder {
	class IfaceMember {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceIfaceMemberUser implements InputCollectionInterfaceIfaceMemberHolder {
	static class IfaceMemberSub extends IfaceMember {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

record InputCollectionInterfaceEmptyRecord() {}

record InputCollectionInterfaceEmptyRecordWithCtor() {
	InputCollectionInterfaceEmptyRecordWithCtor {
	}
}

class InputCollectionInterfaceOuterTypeVariableCollapse<T> {
	void f(Object value, List<String> values) {
		System.out.println(value);
		System.out.println(values);
	}

	<U> U f(T value, ArrayList<String> values) {
		System.out.println(value);
		System.out.println(values);
		return null;
	}
}

interface InputCollectionInterfaceSecondSuperMemberIface {
	class SecondSuperMember {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceSecondSuperMemberBase {}

class InputCollectionInterfaceSecondSuperMemberUser extends InputCollectionInterfaceSecondSuperMemberBase
		implements InputCollectionInterfaceSecondSuperMemberIface {
	static class SecondSuperMemberSub extends SecondSuperMember {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceQualifiedTypeArgConcrete {
	java.util.Map<String, ArrayList<Integer>> build() {
		return null;
	}
}

class InputCollectionInterfaceAnnotatedTypeArgConcrete {
	void f(List<@SuppressWarnings("unused") ArrayList<String>> items) {
		System.out.println(items);
	}
}

class InputCollectionInterfaceMultiLevelNestingConcrete {
	Map<String, Map<Integer, ArrayList<String>>> m() {
		return null;
	}
}

class InputCollectionInterfaceNestedGenericParamConcrete {
	void f(Map<String, ArrayList<Integer>> items) {
		System.out.println(items);
	}
}

class InputCollectionInterfaceNestedGenericReturnConcrete {
	Map<String, ArrayList<Integer>> m() {
		return null;
	}
}

class InputCollectionInterfaceWildcardSuperConcrete {
	void f(Set<? super HashSet<Integer>> items) {
		System.out.println(items);
	}
}

class InputCollectionInterfaceWildcardExtendsReturn {
	List<? extends ArrayList<String>> rows() {
		return List.of();
	}
}

class InputCollectionInterfaceSameFileMemberShadow {
	static class Properties {}

	Properties config() {
		return null;
	}
}

class InputCollectionInterfaceBodyNeedsConcreteType {
	void f(ArrayList<String> values) {
		values.ensureCapacity(10);
	}
}

class InputCollectionInterfaceSuperCtorRejectsTheInterfaceBase {
	InputCollectionInterfaceSuperCtorRejectsTheInterfaceBase(ArrayList<String> values) {
		values.ensureCapacity(10);
	}
}

class InputCollectionInterfaceSuperCtorRejectsTheInterface extends InputCollectionInterfaceSuperCtorRejectsTheInterfaceBase {
	InputCollectionInterfaceSuperCtorRejectsTheInterface(ArrayList<String> values) {
		super(values);
	}
}

class InputCollectionInterfaceCalleeOverloadRejects {
	void f(TreeSet<String> values) {
		Collections.unmodifiableSortedSet(values);
	}
}

class InputCollectionInterfaceUnqualifiedCallArgument {
	void f(ArrayList<String> values) {
		helper(values);
	}

	void helper(Object value) {
		System.out.println(value);
	}
}

class InputCollectionInterfaceUnresolvableReceiverArgument {
	void f(ArrayList<String> values) {
		UnknownReceiver.take(values);
	}
}

class InputCollectionInterfaceThisDelegationArgument {
	InputCollectionInterfaceThisDelegationArgument(Object value) {
		System.out.println(value);
	}

	InputCollectionInterfaceThisDelegationArgument(ArrayList<String> values) {
		this(values);
	}
}

class InputCollectionInterfaceAssignsToAConcreteField {
	private ArrayList<String> stored;

	void f(ArrayList<String> values) {
		stored = values;
	}
}

class InputCollectionInterfaceReturnTypeRejectsTheInterface {
	Serializable f(ArrayList<String> values) {
		return values;
	}
}

class InputCollectionInterfaceBodyMethodReference {
	void f(ArrayList<String> values) {
		final Runnable clear = values::clear;
		clear.run();
	}
}

class InputCollectionInterfaceBodyUnmodelledPosition {
	void f(ArrayList<String> values) {
		synchronized (values) {
			System.out.println(values.size());
		}
	}
}

class InputCollectionInterfaceBodyAssignsToALocal {
	void f(ArrayList<String> values) {
		final var copy = values;
		System.out.println(copy.size());
	}
}

class InputCollectionInterfaceBodyReturnsFromALambda {
	Supplier<ArrayList<String>> f(ArrayList<String> values) {
		return () -> {
			return values;
		};
	}
}

class InputCollectionInterfaceCovariantReturnUse {
	void f(ConcurrentHashMap<String, Integer> lookup) {
		System.out.println(lookup.keySet().iterator());
	}
}

record InputCollectionInterfaceUnresolvableRecordIface(ArrayList<String> rows)
		implements UnknownRecordIface {}

record InputCollectionInterfaceClasspathRecordIface(HashMap<String, String> get)
		implements Supplier<HashMap<String, String>> {}

class InputCollectionInterfaceRecordBodyCompactCtor {
	record Holder(ArrayList<String> rows) {
		Holder {
			rows.ensureCapacity(10);
		}
	}
}

class InputCollectionInterfaceRecordBodyExplicitCtor {
	record Holder(ArrayList<String> rows) {
		Holder(ArrayList<String> rows, int extra) {
			this(rows);
			rows.ensureCapacity(extra);
		}
	}
}

class InputCollectionInterfaceRecordBodyMethod {
	record Holder(ArrayList<String> rows) {
		void grow() {
			rows.ensureCapacity(10);
		}
	}
}

// the file declares its own List, so rewriting would rebind every other use of the name
class InputCollectionInterfaceReplacementNameBound {
	static class List {}

	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}

// the bound widens, but the invariant level above it makes the whole thing a substitution
class InputCollectionInterfaceBoundUnderAnInvariantLevel {
	void f(java.util.List<Set<? extends ArrayList<String>>> items) {
		System.out.println(items);
	}
}

// the receiver is declared here, so the classpath homonym is not what the call resolves to
class InputCollectionInterfaceSameFileReceiverArgument {
	static class Helper {
		static void take(Object value) {
			System.out.println(value);
		}
	}

	void f(ArrayList<String> values) {
		Helper.take(values);
	}
}

// the use is the array itself rather than an element or its length, so it does not survive
class InputCollectionInterfaceBareArrayUse {
	void f(ArrayList<String>[] rows) {
		System.out.println(rows);
	}
}

// an accessor returning the array whole is the same refused use, so neither half of the pair moves
record InputCollectionInterfaceBareArrayComponentUse(ArrayList<String>[] rows) {
	@Override
	public ArrayList<String>[] rows() {
		return rows;
	}
}

// a constructor's own type parameter erases to its bound, so the rewrite collapses the pair
class InputCollectionInterfaceCtorTypeVariableCollapse {
	InputCollectionInterfaceCtorTypeVariableCollapse(Object value, List<String> values) {
		System.out.println(value);
		System.out.println(values);
	}

	<T> InputCollectionInterfaceCtorTypeVariableCollapse(T value, ArrayList<String> values) {
		System.out.println(value);
		System.out.println(values);
	}
}

// the record's own type parameter is the scope this time
record InputCollectionInterfaceRecordTypeVariableCollapse<T>(T value, ArrayList<String> rows) {
	InputCollectionInterfaceRecordTypeVariableCollapse(Object value, List<String> rows) {
		this(null, new ArrayList<>(rows));
	}
}

// a component is a parameter and a return at once, so raising a bound substitutes rather than widens
record InputCollectionInterfaceRecordWildcardComponent(List<? extends ArrayList<String>> rows) {}

// only the first dimension of an array is the element the widening reaches
class InputCollectionInterfaceMultiDimensionalArray {
	void f(ArrayList<String>[][] grid) {
		System.out.println(grid);
	}
}

// the super constructor's other overload is not a candidate, so nothing accepts the interface
class InputCollectionInterfaceSuperCtorCandidateSkippedBase {
	InputCollectionInterfaceSuperCtorCandidateSkippedBase(String label) {
		System.out.println(label);
	}

	InputCollectionInterfaceSuperCtorCandidateSkippedBase(ArrayList<String> values) {
		values.ensureCapacity(10);
	}
}

class InputCollectionInterfaceSuperCtorCandidateSkipped extends InputCollectionInterfaceSuperCtorCandidateSkippedBase {
	InputCollectionInterfaceSuperCtorCandidateSkipped(ArrayList<String> values) {
		super(values);
	}
}

// the DOT branch has its own copy of the type-argument guard, and generics are invariant there too
class InputCollectionInterfaceQualifiedTypeArgInsideGenerics {
	void f(Map<String, java.util.ArrayList<Integer>> index) {
		System.out.println(index);
	}
}