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

// === case: array_list_to_list_in_nested_extends_bounds ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceNestedExtendsBoundsSliceViolation {
	void f(List<? extends List<? extends List<String>>> items) {
		System.out.println(items);
	}
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

// === case: array_list_to_list_with_a_type_use_annotation_on_a_qualified_name ===
// skip-reason: class not found or not a concrete collection
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
class InputCollectionInterfaceQualifiedTypeUseAnnotationSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {}

	void f(java.util.@Ann ArrayList<String> rows) {
		System.out.println(rows);
	}
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

// === case: array_return_type_is_a_warning_when_a_caller_pins_it ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayReturnPinnedSliceViolation {
	private final ArrayList<String>[] pinned = rows();

	private List<String>[] rows() {
		return null;
	}

	void use() {
		System.out.println(pinned);
	}
}
// === end ===

// === case: array_return_type_is_auto_fixable ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayReturnSliceViolation {
	private List<String>[] rows() {
		return null;
	}

	void use() {
		System.out.println(rows());
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

// === case: constructor_parameter_rebinds_onto_the_implied_canonical_constructor ===
// imports: java.util.HashSet
// imports: java.util.Set
record InputCollectionInterfaceImpliedCanonicalSliceViolation(int size) {
	InputCollectionInterfaceImpliedCanonicalSliceViolation(Set<String> seed) {
		this(seed.size());
	}
}
// === end ===

// === case: constructor_parameter_with_a_same_arity_overload_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceCtorRebindSliceViolation {
	InputCollectionInterfaceCtorRebindSliceViolation(List<String> rows) {
		System.out.println(rows);
	}

	InputCollectionInterfaceCtorRebindSliceViolation(String name) {
		System.out.println(name);
	}
}
// === end ===

// === case: enum_owner_splits_the_two_tiers ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
// imports: java.util.List
// imports: java.util.Set
enum InputCollectionInterfaceEnumOwnerSliceViolation {
	;

	List<String> f(Set<Integer> items) {
		return null;
	}
}
// === end ===

// === case: final_owner_splits_the_two_tiers ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
// imports: java.util.List
// imports: java.util.Set
final class InputCollectionInterfaceFinalOwnerSliceViolation {
	List<String> f(Set<Integer> items) {
		return null;
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
final class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
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

// === case: overload_rebind_ignores_a_private_supertype_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePrivateSuperRebindBase {
	private static void dump(List<String> values) {
		System.out.println(values);
	}
}

final class InputCollectionInterfacePrivateSuperRebindSliceViolation extends InputCollectionInterfacePrivateSuperRebindBase {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_rebind_ignores_a_static_interface_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceStaticIfaceRebindMethod {
	static void dump(List<String> values) {
		System.out.println(values);
	}
}

final class InputCollectionInterfaceStaticIfaceRebindSliceViolation implements InputCollectionInterfaceStaticIfaceRebindMethod {
	void dump(List<String> values) {
		System.out.println(values);
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
	void concatenated(List<String> values) {
		System.out.println("rows: " + values);
	}

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

// === case: parameter_is_a_warning_when_a_varargs_overload_could_take_the_call ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputCollectionInterfaceVarargsSiblingRebindSliceViolation {
	void dump(List<String> values) {
		System.out.println(values);
	}

	void dump(String name, String... rest) {
		System.out.println(name);
		System.out.println(rest.length);
	}
}
// === end ===

// === case: parameter_is_a_warning_when_an_enum_constant_body_inherits_an_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
enum InputCollectionInterfaceEnumConstantInheritedOverloadSliceViolation {
	ONE {
		void compareTo(List<String> values) {
			System.out.println(values);
		}
	}
}
// === end ===

// === case: parameter_is_a_warning_when_an_inherited_enum_overload_could_rebind ===
// imports: java.util.ArrayList
// imports: java.util.List
enum InputCollectionInterfaceEnumOverloadSliceViolation {
	;

	void compareTo(List<String> other) {
		System.out.println(other);
	}

	void matches(List<String> other) {
		System.out.println(other);
	}
}
// === end ===

// === case: parameter_is_a_warning_when_an_inherited_object_overload_could_rebind ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputCollectionInterfaceObjectOverloadSliceViolation {
	private final ArrayList<String> parts = new ArrayList<>();

	void matches(List<String> other) {
		System.out.println(parts.equals(other));
	}

	void wait(List<String> other) {
		System.out.println(parts.equals(other));
	}
}
// === end ===

// === case: parameter_is_a_warning_when_its_coupled_return_is_pinned_by_a_caller ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceCoupledLockstepSliceViolation {
	private final ArrayList<String> pinned = keep(new ArrayList<>());

	private List<String> keep(List<String> values) {
		return values;
	}

	void use() {
		System.out.println(pinned);
	}
}
// === end ===

// === case: parameter_is_auto_fixable_when_its_coupled_return_also_moves ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceCoupledFixableSliceViolation {
	private final List<String> pinned = keep(new ArrayList<>());

	private List<String> keep(List<String> values) {
		return values;
	}

	void use() {
		System.out.println(pinned);
	}
}
// === end ===

// === case: parameter_reassigned_before_use ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceReassignedParamSliceViolation {
	void f(List<String> values) {
		values = new ArrayList<>();
		System.out.println(values);
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

// === case: parameter_sharing_the_returns_interface_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputCollectionInterfaceSharedReturnIfaceSliceViolation {
	List<String> f(List<String> values) {
		return values;
	}
}
// === end ===

// === case: parameter_with_a_same_arity_overload_on_a_sealed_owner_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputCollectionInterfaceOverloadRebindSliceViolation {
	void dump(List<String> values) {
		System.out.println(values);
	}

	void dump(String name) {
		System.out.println(name);
	}
}
// === end ===

// === case: parameter_with_a_same_arity_supertype_overload_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSupertypeRebindBase {
	void dump(String name) {
		System.out.println(name);
	}
}

final class InputCollectionInterfaceSupertypeRebindSliceViolation extends InputCollectionInterfaceSupertypeRebindBase {
	void dump(List<String> values) {
		System.out.println(values);
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

// === case: private_member_is_auto_fixable_in_both_positions ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
// imports: java.util.List
// imports: java.util.Set
class InputCollectionInterfacePrivateMemberSliceViolation {
	private List<String> f(Set<Integer> items) {
		return null;
	}
}
// === end ===

// === case: private_return_type_is_a_warning_behind_a_method_reference ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.function.Function
// imports: java.util.function.Supplier
class InputCollectionInterfaceMethodRefReturnSliceViolation {
	private List<String> rows(String key) {
		return null;
	}

	void use() {
		final Function<String, ArrayList<String>> lookup = this::rows;
		final Supplier<ArrayList<String>> maker = ArrayList::new;
		System.out.println(lookup.apply("k"));
		System.out.println(maker.get());
	}
}
// === end ===

// === case: private_return_type_is_a_warning_when_a_caller_pins_it ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePinnedReturnSliceViolation {
	private final ArrayList<String> cached = rows();

	private List<String> rows() {
		return null;
	}

	void use() {
		System.out.println(cached);
	}
}
// === end ===

// === case: private_return_type_is_a_warning_when_an_overload_could_rebind_the_result ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceResultRebindSliceViolation {
	private List<String> rows() {
		return null;
	}

	private void take(ArrayList<String> values) {
		System.out.println(values);
	}

	private void take(List<String> values) {
		System.out.println(values);
	}

	void use() {
		take(rows());
	}
}
// === end ===

// === case: private_return_type_is_a_warning_when_the_result_is_coupled_to_another_return ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceCoupledReturnSliceViolation {
	private final ArrayList<String> cached = copy();

	private List<String> copy() {
		return rows();
	}

	private List<String> rows() {
		return null;
	}

	void use() {
		System.out.println(cached);
	}
}
// === end ===

// === case: private_return_type_is_auto_fixable_when_the_caller_takes_the_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAcceptedReturnSliceViolation {
	private final List<String> cached = rows();

	private List<String> rows() {
		return null;
	}

	void use() {
		System.out.println(cached);
	}
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

// === case: receiver_parameter_beside_a_collection_parameter ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceReceiverParamSliceViolation {
	void f(InputCollectionInterfaceReceiverParamSliceViolation this, List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: record_accessor_is_refused_when_its_name_is_interrupted ===
// skip-reason: record component and accessor cannot be rewritten as one edit
class InputCollectionInterfaceInterruptedAccessorSliceViolation {
	private record Rows(java.util.ArrayList<String> items) {
		@Override
		public java./* q */util.ArrayList<String> items() {
			return items;
		}
	}
}
// === end ===

// === case: record_accessor_overload_with_parameters_is_flagged ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceAccessorOverloadSliceViolation(List<String> items) {
	List<String> items(int limit) {
		return items;
	}
}
// === end ===

// === case: record_accessor_with_a_split_qualifier_rewrites_with_its_component ===
// imports: java.util.List
class InputCollectionInterfaceSplitQualifierAccessorSliceViolation {
	private record Rows(List<String> items) {
		@Override
		public List<String> items() {
			return items;
		}
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

// === case: record_component_array_is_auto_fixable ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArrayRecordSliceViolation {
	private record Rows(List<String>[] items) {}
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

// === case: record_component_flags_under_a_private_interface_method ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfaceUnpinnedRows {
	private List<String> items() {
		return null;
	}
}

record InputCollectionInterfaceUnpinnedRecord(List<String> items) implements InputCollectionInterfaceUnpinnedRows {}
// === end ===

// === case: record_component_flags_under_an_unrelated_interface ===
// imports: java.io.Serializable
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceUnrelatedIfaceSliceViolation(List<String> rows) implements Serializable {}
// === end ===

// === case: record_component_in_a_nested_record_is_a_warning ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceNestedRecordHolder {
	record Rows(List<String> items) {}
}
// === end ===

// === case: record_component_in_a_private_record_is_auto_fixable ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePrivateRecordHolder {
	private record Rows(List<String> items) {}
}
// === end ===

// === case: record_component_in_an_unnameable_owner_is_auto_fixable ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceUnnameableRecordOwners {
	final Runnable held = new Runnable() {
		record InAnonymous(List<String> items) {}

		@Override
		public void run() {
		}
	};

	void m() {
		record InMethod(List<String> items) {}

		System.out.println(new InMethod(new ArrayList<>()));
	}
}
// === end ===

// === case: record_component_is_a_warning_when_a_body_method_returns_it ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceRecordCoupledBodySliceViolation {
	private record Rows(List<String> items) {
		List<String> items(int limit) {
			return items;
		}
	}
}
// === end ===

// === case: record_component_is_a_warning_when_an_accessor_call_pins_it ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePinnedAccessorSliceViolation {
	private record Rows(List<String> items) {}

	private final Rows rows = new Rows(new ArrayList<>());

	private final ArrayList<String> pinned = rows.items();

	void use() {
		System.out.println(pinned);
	}
}
// === end ===

// === case: record_component_is_auto_fixable_when_the_accessor_call_takes_the_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAcceptedAccessorSliceViolation {
	private record Rows(List<String> items) {}

	private final List<String> accepted = new Rows(new ArrayList<>()).items();

	void use() {
		System.out.println(accepted);
	}
}
// === end ===

// === case: record_component_is_refused_when_its_name_is_interrupted ===
// skip-reason: record component and accessor cannot be rewritten as one edit
class InputCollectionInterfaceInterruptedComponentSliceViolation {
	private record Rows(java./* q */util.ArrayList<String> items) {}
}
// === end ===

// === case: record_component_pinned_by_a_same_file_interface_accessor ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputCollectionInterfacePinnedRows {
	List<String> items();
}

record InputCollectionInterfacePinnedRecord(ArrayList<String> items) implements InputCollectionInterfacePinnedRows {}
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

// === case: record_component_with_a_split_qualifier_rewrites_with_its_accessor ===
// imports: java.util.List
class InputCollectionInterfaceSplitQualifierComponentSliceViolation {
	private record Rows(
			List<String> items
	) {
		@Override
		public List<String> items() {
			return items;
		}
	}
}
// === end ===

// === case: record_component_with_a_supplementary_char_before_the_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceSupplementaryComponentSliceViolation {
	private interface Marker {}

	private record Rows(String a𝐀b, List<String> items)
			implements Marker {}
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

// === case: record_method_not_named_like_a_component_is_flagged ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceUnrelatedRecordMethodSliceViolation(int count) {
	List<String> rows() {
		return null;
	}
}
// === end ===

// === case: return_type_scan_ignores_a_differing_arity_call ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceArityMismatchCallSliceViolation {
	private final List<String> cached = rows();
	private final String label = rows("k");

	private List<String> rows() {
		return null;
	}

	private String rows(String key) {
		return key;
	}

	void use() {
		System.out.println(cached);
		System.out.println(label);
	}
}
// === end ===

// === case: return_type_scan_ignores_a_same_named_call_on_another_type ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.List
class InputCollectionInterfaceUnrelatedCallSliceViolation {
	private final HashMap<String, String> index = new HashMap<>();

	void use() {
		synchronized (index.values()) {
			System.out.println(index);
		}
	}

	private List<String> values() {
		return null;
	}
}
// === end ===

// === case: return_type_scan_ignores_an_unrelated_same_file_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceUnrelatedRowsSource {
	static String rows() {
		return "";
	}
}

class InputCollectionInterfaceUnrelatedRowsSliceViolation {
	String label() {
		return InputCollectionInterfaceUnrelatedRowsSource.rows();
	}

	private List<String> rows() {
		return null;
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
final class InputCollectionInterfaceWrappedSignatureSliceViolation {
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

// === case: varargs_parameter_is_a_warning_when_a_fixed_arity_overload_could_take_the_call ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputCollectionInterfaceVarargsSelfRebindSliceViolation {
	void dump() {}

	void dump(List<String>... rows) {
		System.out.println(rows.length);
	}
}
// === end ===