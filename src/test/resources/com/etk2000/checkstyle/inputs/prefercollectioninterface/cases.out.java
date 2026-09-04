package com.etk2000.checkstyle.inputs.prefercollectioninterface;

// === case: abstract_method_parameter_is_flagged ===
// imports: java.util.ArrayList
// imports: java.util.List
abstract class InputCollectionInterfaceAbstractParamSliceViolation {
	abstract void f(List<String> values);
}
// === end ===

// === case: array_deque_to_deque ===
// imports: java.util.ArrayDeque
// imports: java.util.Deque
class InputCollectionInterfaceArrayDequeSliceViolation {
	void f(Deque<String> items) {}
}
// === end ===

// === case: array_element_call_uses_the_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayElementCallSliceViolation {
	void f(List<String>[] rows) {
		rows[0].add("x");
	}
}
// === end ===

// === case: array_list_to_list ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayListReturnSliceViolation {
	List<String> m() { return null; }
}
// === end ===

// === case: array_list_to_list_constructor_param ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayListCtorParamSliceViolation {
	InputCollectionInterfaceArrayListCtorParamSliceViolation(List<String> items) {}
}
// === end ===

// === case: array_list_to_list_fqn ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayListFqnSliceViolation {
	List<String> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_an_annotated_extends_bound ===
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnnotatedExtendsBoundSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface MemberNamed {
		String ArrayList() default "";
	}

	void f(List<? extends @MemberNamed(ArrayList = "x") List<String>> items) {
		System.out.println(items);
	}
}
// === end ===

// === case: array_list_to_list_in_annotated_generic_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnnotatedGenericArgSliceViolation {
	void f(List<@SuppressWarnings("unused") String> items) {}
}
// === end ===

// === case: array_list_to_list_in_bounded_type_param ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceBoundedTypeParamSliceViolation {
	<T extends Comparable<T>> List<T> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_concrete_bound ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceConcreteInBoundSliceViolation {
	<T extends ArrayList<String>> List<T> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_extends_bounds_on_both_qualified_segments ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
// imports: java.util.List
// imports: java.util.Set
class InputCollectionInterfaceQualifiedSegmentBoundsSliceViolation {
	static class Outer<A> {
		class Inner<B> {}
	}

	void f(Outer<? extends List<String>>.Inner<? extends Set<Integer>> items) {
		System.out.println(items);
	}
}
// === end ===

// === case: array_list_to_list_in_intersection_bound ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceIntersectionBoundSliceViolation {
	<T extends Comparable<T> & java.io.Serializable> List<T> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_wildcard_extends ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceWildcardExtendsSliceViolation {
	void f(List<? extends List<String>> items) {}
}
// === end ===

// === case: array_list_to_list_raw_type ===
// imports: java.util.ArrayList
// imports: java.util.List
@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawTypeSliceViolation {
	List m() { return null; }
}
// === end ===

// === case: array_list_to_list_wildcard_import ===
// imports: java.util.*
// imports: java.util.List
class InputCollectionInterfaceArrayListWildcardImportSliceViolation {
	List<String> m() { return null; }
}
// === end ===

// === case: array_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayOfCollectionSliceViolation {
	void f(List<String>[] rows) {
		System.out.println(rows.length);
	}
}
// === end ===

// === case: body_calling_an_interface_method_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceBodyCallsIfaceMethodSliceViolation {
	void f(List<String> values) {
		values.add("x");
	}
}
// === end ===

// === case: concurrent_hash_map_fqn ===
// imports: java.util.Map
class InputCollectionInterfaceConcurrentHashMapFqnSliceViolation {
	Map<String, Integer> lookup() { return null; }
}
// === end ===

// === case: concurrent_hash_map_interface_call_still_flags ===
// imports: java.util.Map
// imports: java.util.concurrent.ConcurrentHashMap
class InputCollectionInterfaceConcurrentMapCallSliceViolation {
	void f(Map<String, Integer> lookup) {
		lookup.get("x");
	}
}
// === end ===

// === case: constructor_collapse_ignores_a_supertype_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceCtorCollapseBase {
	InputCollectionInterfaceCtorCollapseBase(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceCtorCollapseSliceViolation extends InputCollectionInterfaceCtorCollapseBase {
	InputCollectionInterfaceCtorCollapseSliceViolation(List<String> values) {
		super(values);
	}
}
// === end ===

// === case: hash_map_to_map ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputCollectionInterfaceHashMapToMapSliceViolation {
	void f(Map<String, Integer> items) {}
}
// === end ===

// === case: hash_map_to_map_fqn ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
	void f(Map<String, Integer> items) {}
}
// === end ===

// === case: hash_set_to_set ===
// imports: java.util.HashSet
// imports: java.util.Set
class InputCollectionInterfaceHashSetToSetSliceViolation {
	void f(Set<String> items) {}
}
// === end ===

// === case: inner_types_with_a_shared_simple_name_still_flag ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSharedInnerNameSliceViolation {
	static class First {
		static class Box {}
	}

	static class Second {
		static class Box {}
	}

	void f(First.Box box, List<String> values) {
		System.out.println(box);
		System.out.println(values);
	}

	void f(Second.Box box, List<String> values) {
		System.out.println(box);
		System.out.println(values);
	}
}
// === end ===

// === case: interface_member_return_type_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfacePackagePrivateIfaceMember {
	List<String> all();
}
// === end ===

// === case: linked_hash_map_to_map ===
// imports: java.util.LinkedHashMap
// imports: java.util.Map
class InputCollectionInterfaceLinkedHashMapSliceViolation {
	void f(Map<String, Integer> items) {}
}
// === end ===

// === case: linked_hash_set_to_set ===
// imports: java.util.LinkedHashSet
// imports: java.util.Set
class InputCollectionInterfaceLinkedHashSetSliceViolation {
	void f(Set<String> items) {}
}
// === end ===

// === case: local_supertype_shadowing_a_classpath_name_still_flags ===
// imports: java.util.AbstractMap
// imports: java.util.HashMap
// imports: java.util.Map
class InputCollectionInterfaceLocalShadowsClasspathSliceViolation {
	void m() {
		class AbstractMap {
			void unrelated() {}
		}

		class ShadowedSub extends AbstractMap {
			void putAll(Map<String, Integer> values) {
				System.out.println(values);
			}
		}

		System.out.println(new ShadowedSub());
	}
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.HashSet
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.Set
class InputCollectionInterfaceBothReturnAndParam {
	static List<String> process(Set<Integer> items) {
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(List<String> a, Map<String, Integer> b) {}
}
// === end ===

// === case: nested_inheritance_cycle_terminates ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceNestedCycleOuter extends InputCollectionInterfaceNestedCyclePartner {
	static class Inner extends UnknownNestedBase {
		void f(List<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceNestedCyclePartner extends InputCollectionInterfaceNestedCycleOuter {}
// === end ===

// === case: overload_collapse_across_a_generic_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceGenericSupertypeBase<T> {
	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceGenericSupertypeSliceViolation extends InputCollectionInterfaceGenericSupertypeBase<String> {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_a_local_subclass ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceLocalSubclassSliceViolation {
	void m() {
		class LocalDumpBase {
			void dump(List<String> values) {
				System.out.println(values);
			}
		}

		class LocalDumpSub extends LocalDumpBase {
			List<String> dump(ArrayList<String> values) {
				return values;
			}
		}

		System.out.println(new LocalDumpSub());
	}
}
// === end ===

// === case: overload_collapse_across_a_local_subclass_with_a_duplicate_name ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceDuplicateLocalNameSliceViolation {
	void one() {
		class DuplicateDumpBase {
			void dump(List<String> values) {
				System.out.println(values);
			}
		}

		class DuplicateDumpSub extends DuplicateDumpBase {
			List<String> dump(ArrayList<String> values) {
				return values;
			}
		}

		System.out.println(new DuplicateDumpSub());
	}

	void two() {
		class DuplicateDumpBase {
			void other() {}
		}

		System.out.println(new DuplicateDumpBase());
	}
}
// === end ===

// === case: overload_collapse_across_a_local_subclass_with_an_anonymous_sibling ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnonymousSiblingSliceViolation {
	void m() {
		class AnonymousSiblingBase {
			void dump(List<String> values) {
				System.out.println(values);
			}
		}

		final Object held = new AnonymousSiblingBase() {};

		class AnonymousSiblingSub extends AnonymousSiblingBase {
			List<String> dump(ArrayList<String> values) {
				return values;
			}
		}

		System.out.println(held);
		System.out.println(new AnonymousSiblingSub());
	}
}
// === end ===

// === case: overload_collapse_across_a_nested_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceNestedSupertypeSliceViolation {
	static class NestedDumpBase {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}

	static class NestedDumpSub extends NestedDumpBase {
		List<String> dump(ArrayList<String> values) {
			return values;
		}
	}
}
// === end ===

// === case: overload_collapse_across_a_record_implemented_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceRecordIface {
	default void dump(List<String> values) {
		System.out.println(values);
	}
}

record InputCollectionInterfaceRecordSliceViolation(int count) implements InputCollectionInterfaceRecordIface {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_a_second_implemented_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceSecondIfaceFirst {}

interface InputCollectionInterfaceSecondIfaceSecond {
	default void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceSecondIfaceSliceViolation implements InputCollectionInterfaceSecondIfaceFirst, InputCollectionInterfaceSecondIfaceSecond {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_a_subtype_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSubtypeCollapseBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceSubtypeCollapseSliceViolation extends InputCollectionInterfaceSubtypeCollapseBase {
	static List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_a_subtypes_other_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOtherSupertypeSliceViolation {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}

interface InputCollectionInterfaceOtherSupertypeIface {
	default void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOtherSupertypeSub extends InputCollectionInterfaceOtherSupertypeSliceViolation
		implements InputCollectionInterfaceOtherSupertypeIface {}
// === end ===

// === case: overload_collapse_across_a_transitive_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceTransitiveCollapseRoot {
	static List<String> dump(ArrayList<String> values) {
		return values;
	}
}

class InputCollectionInterfaceTransitiveCollapseMiddle extends InputCollectionInterfaceTransitiveCollapseRoot {}

class InputCollectionInterfaceTransitiveCollapseSliceViolation extends InputCollectionInterfaceTransitiveCollapseMiddle {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_across_a_transitive_supertype_from_the_leaf ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceLeafRoot {
	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceLeafMiddle extends InputCollectionInterfaceLeafRoot {}

class InputCollectionInterfaceLeafSliceViolation extends InputCollectionInterfaceLeafMiddle {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_an_anonymous_subclass ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnonymousBase {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}

class InputCollectionInterfaceAnonymousSliceViolation {
	final InputCollectionInterfaceAnonymousBase held = new InputCollectionInterfaceAnonymousBase() {
		@Override
		void dump(List<String> values) {
			System.out.println(values);
		}
	};
}
// === end ===

// === case: overload_collapse_across_an_enum_constant_body ===
// imports: java.util.ArrayList
// imports: java.util.List
enum InputCollectionInterfaceEnumConstantSliceViolation {
	ONE {
		List<String> dump(ArrayList<String> values) {
			return values;
		}
	};

	void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_across_an_implemented_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceImplementedCollapseIface {
	default void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceImplementedCollapseSliceViolation implements InputCollectionInterfaceImplementedCollapseIface {
	List<String> dump(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_an_inherited_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceInheritedCollapseBase {
	static List<String> dump(ArrayList<String> values) {
		return values;
	}
}

class InputCollectionInterfaceInheritedCollapseSliceViolation extends InputCollectionInterfaceInheritedCollapseBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_ignores_a_foreign_anonymous_base ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceForeignAnonymousSliceViolation {
	final Thread held = new Thread() {
		List<String> dump(List<String> values) {
			return new ArrayList<>(values);
		}
	};
}
// === end ===

// === case: overload_collapse_ignores_a_private_supertype_method ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePrivateSuperCollapseBase {
	private static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfacePrivateSuperCollapseSliceViolation extends InputCollectionInterfacePrivateSuperCollapseBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_ignores_a_sibling_subclass ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSiblingCollapseBase {}

class InputCollectionInterfaceSiblingCollapseOther extends InputCollectionInterfaceSiblingCollapseBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceSiblingCollapseSliceViolation extends InputCollectionInterfaceSiblingCollapseBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_ignores_a_static_interface_method ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceStaticIfaceMethod {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceStaticIfaceSliceViolation implements InputCollectionInterfaceStaticIfaceMethod {
	void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_collapse_prefers_a_shadowing_local_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceShadowedTopLevelBase {
	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceShadowingLocalSliceViolation {
	void m() {
		class InputCollectionInterfaceShadowedTopLevelBase {
			void unrelated() {}
		}

		class ShadowingLocalSub extends InputCollectionInterfaceShadowedTopLevelBase {
			List<String> dump(List<String> values) {
				return values;
			}
		}

		System.out.println(new ShadowingLocalSub());
	}
}
// === end ===

// === case: overload_collapse_still_flags_the_return_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOverloadCollapseReturnTypeSliceViolation {
	static List<String> dump(ArrayList<String> values) {
		return values;
	}

	static List<String> dump(List<String> values) {
		return null;
	}
}
// === end ===

// === case: overload_of_a_different_arity_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOverloadOfADifferentArityStillFlagsSliceViolation {
	static void dump(List<String> values) {
		System.out.println(values);
	}

	static void dump(List<String> values, int limit) {
		System.out.println(values.size() + limit);
	}
}
// === end ===

// === case: overload_of_a_varargs_arity_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceVarargsArityBase {
	void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceVarargsAritySliceViolation extends InputCollectionInterfaceVarargsArityBase {
	void dump(List<String>... values) {
		System.out.println(values.length);
	}
}
// === end ===

// === case: parameter_at_a_non_zero_argument_index ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Objects
class InputCollectionInterfaceNonZeroArgumentIndexSliceViolation {
	void f(List<String> values) {
		System.out.println(Objects.equals(null, values));
	}
}
// === end ===

// === case: parameter_in_operand_positions_only ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOperandPositionsSliceViolation {
	void equality(List<String> values) {
		System.out.println(values == null);
	}

	void inequality(List<String> values) {
		System.out.println(values != null);
	}

	void iterated(List<String> values) {
		for (var value : values)
			System.out.println(value);
	}

	void typeTest(List<String> values) {
		System.out.println(values instanceof List);
	}
}
// === end ===

// === case: parameter_returned_through_an_interface_return_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceReturnedAsInterfaceSliceViolation {
	List<String> f(List<String> values) {
		return values;
	}
}
// === end ===

// === case: priority_queue_to_queue ===
// imports: java.util.PriorityQueue
// imports: java.util.Queue
class InputCollectionInterfacePriorityQueueSliceViolation {
	void f(Queue<String> items) {}
}
// === end ===

// === case: public_member_on_a_package_private_owner_is_a_warning ===
// imports: java.util.AbstractMap
// imports: java.util.HashMap
// imports: java.util.Map
abstract class InputCollectionInterfaceCrossFileNoCollisionPublicMember extends AbstractMap<String, Integer> {
	public void store(Map<String, Integer> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: public_member_overriding_nothing_is_a_warning ===
// imports: java.util.Map
// imports: java.util.Properties
// imports: javax.xml.transform.Transformer
abstract class InputCollectionInterfaceNonOverridePublicMember extends Transformer {
	public Map buildProperties(Map seed) {
		System.out.println(seed);
		return new Properties();
	}
}
// === end ===

// === case: record_component_and_its_explicit_accessor_are_flagged_together ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceExplicitAccessorSliceViolation(List<String> items) {
	@Override
	public List<String> items() {
		return items;
	}
}
// === end ===

// === case: record_component_flags_once_with_a_compact_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceCompactCtorSliceViolation(List<String> rows) {
	InputCollectionInterfaceCompactCtorSliceViolation {
		rows = new ArrayList<>(rows);
	}
}
// === end ===

// === case: record_component_flags_under_an_unrelated_interface ===
// imports: java.io.Serializable
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceUnrelatedIfaceSliceViolation(List<String> rows) implements Serializable {}
// === end ===

// === case: record_component_survives_a_differing_arity_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordArityCtorSliceViolation(List<String> rows) {
	InputCollectionInterfaceRecordArityCtorSliceViolation(List<String> rows, int limit) {
		this(new ArrayList<>(rows.subList(0, limit)));
	}
}
// === end ===

// === case: record_component_uses_the_collection_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordComponentSliceViolation(List<String> items) {
	List<String> copy() {
		return new ArrayList<>(items);
	}
}
// === end ===

// === case: record_flags_only_the_collection_component ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordMultiComponentSliceViolation(String name, List<String> rows) {
	List<String> copy() {
		return new ArrayList<>(rows);
	}
}
// === end ===

// === case: same_file_override_pair_is_flagged_on_both_sides ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOverridePairBase {
	void f(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverridePairSliceViolation extends InputCollectionInterfaceOverridePairBase {
	@Override
	void f(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: signature_wrapped_onto_a_continuation_line ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceWrappedSignatureSliceViolation {
	void f(
			String name,
			List<String> rows
	) {
		System.out.println(name + rows);
	}
}
// === end ===

// === case: supplementary_char_before_the_collection_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSupplementaryBeforeTypeSliceViolation {
	void f(String a𝐀b, List<String> items) {
		System.out.println(a𝐀b);
		System.out.println(items);
	}
}
// === end ===

// === case: tree_map_to_map ===
// imports: java.util.TreeMap
// imports: java.util.Map
class InputCollectionInterfaceTreeMapSliceViolation {
	Map<String, Integer> m() { return null; }
}
// === end ===

// === case: tree_set_to_set ===
// imports: java.util.TreeSet
// imports: java.util.Set
class InputCollectionInterfaceTreeSetSliceViolation {
	void f(Set<String> items) {}
}
// === end ===

// === case: unloadable_supertype_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceUnloadableSupertypeSliceViolation extends UnknownForeignBase {
	List<String> rows(List<String> values) {
		return values;
	}
}
// === end ===

// === case: varargs_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceVarargsOfCollectionSliceViolation {
	void f(List<String>... rows) {
		System.out.println(rows.length);
	}
}
// === end ===