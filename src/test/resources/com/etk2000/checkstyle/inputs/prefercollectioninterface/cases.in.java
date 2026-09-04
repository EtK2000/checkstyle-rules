package com.etk2000.checkstyle.inputs.prefercollectioninterface;

// === case: abstract_method_parameter_is_flagged ===
// imports: java.util.ArrayList
abstract class InputCollectionInterfaceAbstractParamSliceViolation {
	abstract void f(ArrayList<String> values); // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_deque_to_deque ===
// imports: java.util.ArrayDeque
class InputCollectionInterfaceArrayDequeSliceViolation {
	void f(ArrayDeque<String> items) {} // violation (warning): Use 'Deque' instead of 'ArrayDeque'.
}
// === end ===

// === case: array_element_call_uses_the_interface ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayElementCallSliceViolation {
	void f(ArrayList<String>[] rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
		rows[0].add("x");
	}
}
// === end ===

// === case: array_list_to_list ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListReturnSliceViolation {
	ArrayList<String> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_constructor_param ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListCtorParamSliceViolation {
	InputCollectionInterfaceArrayListCtorParamSliceViolation(ArrayList<String> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_fqn ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListFqnSliceViolation {
	java.util.ArrayList<String> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
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

	void f(List<? extends @MemberNamed(ArrayList = "x") ArrayList<String>> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(items);
	}
}
// === end ===

// === case: array_list_to_list_in_annotated_generic_arg ===
// imports: java.util.ArrayList
class InputCollectionInterfaceAnnotatedGenericArgSliceViolation {
	void f(ArrayList<@SuppressWarnings("unused") String> items) {} // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_bounded_type_param ===
// imports: java.util.ArrayList
class InputCollectionInterfaceBoundedTypeParamSliceViolation {
	<T extends Comparable<T>> ArrayList<T> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_concrete_bound ===
// imports: java.util.ArrayList
class InputCollectionInterfaceConcreteInBoundSliceViolation {
	<T extends ArrayList<String>> ArrayList<T> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_extends_bounds_on_both_qualified_segments ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
class InputCollectionInterfaceQualifiedSegmentBoundsSliceViolation {
	static class Outer<A> {
		class Inner<B> {}
	}

	void f(Outer<? extends ArrayList<String>>.Inner<? extends HashSet<Integer>> items) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'Set' instead of 'HashSet'.
		System.out.println(items);
	}
}
// === end ===

// === case: array_list_to_list_in_intersection_bound ===
// imports: java.util.ArrayList
class InputCollectionInterfaceIntersectionBoundSliceViolation {
	<T extends Comparable<T> & java.io.Serializable> ArrayList<T> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_nested_extends_bounds ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceNestedExtendsBoundsSliceViolation {
	void f(List<? extends List<? extends ArrayList<String>>> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(items);
	}
}
// === end ===

// === case: array_list_to_list_in_wildcard_extends ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceWildcardExtendsSliceViolation {
	void f(List<? extends ArrayList<String>> items) {} // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_raw_type ===
// imports: java.util.ArrayList
@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawTypeSliceViolation {
	ArrayList m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_wildcard_import ===
// imports: java.util.*
class InputCollectionInterfaceArrayListWildcardImportSliceViolation {
	ArrayList<String> m() { return null; } // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_with_a_type_use_annotation_on_a_qualified_name ===
// skip-reason: class not found or not a concrete collection
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
class InputCollectionInterfaceQualifiedTypeUseAnnotationSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {}

	void f(java.util.@Ann ArrayList<String> rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(rows);
	}
}
// === end ===

// === case: array_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayOfCollectionSliceViolation {
	void f(ArrayList<String>[] rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(rows.length);
	}
}
// === end ===

// === case: body_calling_an_interface_method_still_flags ===
// imports: java.util.ArrayList
class InputCollectionInterfaceBodyCallsIfaceMethodSliceViolation {
	void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		values.add("x");
	}
}
// === end ===

// === case: concurrent_hash_map_fqn ===
class InputCollectionInterfaceConcurrentHashMapFqnSliceViolation {
	java.util.concurrent.ConcurrentHashMap<String, Integer> lookup() { return null; } // violation (warning): Use 'Map' instead of 'ConcurrentHashMap'.
}
// === end ===

// === case: concurrent_hash_map_interface_call_still_flags ===
// imports: java.util.concurrent.ConcurrentHashMap
class InputCollectionInterfaceConcurrentMapCallSliceViolation {
	void f(ConcurrentHashMap<String, Integer> lookup) { // violation (warning): Use 'Map' instead of 'ConcurrentHashMap'.
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
	InputCollectionInterfaceCtorCollapseSliceViolation(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'.
		super(values);
	}
}
// === end ===

// === case: constructor_parameter_with_a_same_arity_overload_is_a_warning ===
// imports: java.util.ArrayList
class InputCollectionInterfaceCtorRebindSliceViolation {
	InputCollectionInterfaceCtorRebindSliceViolation(ArrayList<String> rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
enum InputCollectionInterfaceEnumOwnerSliceViolation {
	;

	ArrayList<String> f(HashSet<Integer> items) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation: Use 'Set' instead of 'HashSet'.
		return null;
	}
}
// === end ===

// === case: final_owner_splits_the_two_tiers ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
final class InputCollectionInterfaceFinalOwnerSliceViolation {
	ArrayList<String> f(HashSet<Integer> items) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation: Use 'Set' instead of 'HashSet'.
		return null;
	}
}
// === end ===

// === case: hash_map_to_map ===
// imports: java.util.HashMap
class InputCollectionInterfaceHashMapToMapSliceViolation {
	void f(HashMap<String, Integer> items) {} // violation (warning): Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: hash_map_to_map_fqn ===
// imports: java.util.HashMap
final class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
	void f(java.util.HashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: hash_set_to_set ===
// imports: java.util.HashSet
class InputCollectionInterfaceHashSetToSetSliceViolation {
	void f(HashSet<String> items) {} // violation (warning): Use 'Set' instead of 'HashSet'.
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

	void f(First.Box box, ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
interface InputCollectionInterfacePackagePrivateIfaceMember {
	ArrayList<String> all(); // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: linked_hash_map_to_map ===
// imports: java.util.LinkedHashMap
class InputCollectionInterfaceLinkedHashMapSliceViolation {
	void f(LinkedHashMap<String, Integer> items) {} // violation (warning): Use 'Map' instead of 'LinkedHashMap'.
}
// === end ===

// === case: linked_hash_set_to_set ===
// imports: java.util.LinkedHashSet
class InputCollectionInterfaceLinkedHashSetSliceViolation {
	void f(LinkedHashSet<String> items) {} // violation (warning): Use 'Set' instead of 'LinkedHashSet'.
}
// === end ===

// === case: local_supertype_shadowing_a_classpath_name_still_flags ===
// imports: java.util.AbstractMap
// imports: java.util.HashMap
class InputCollectionInterfaceLocalShadowsClasspathSliceViolation {
	void m() {
		class AbstractMap {
			void unrelated() {}
		}

		class ShadowedSub extends AbstractMap {
			void putAll(HashMap<String, Integer> values) { // violation: Use 'Map' instead of 'HashMap'.
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
// multi-fix-expected
class InputCollectionInterfaceBothReturnAndParam {
	static ArrayList<String> process(HashSet<Integer> items) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'Set' instead of 'HashSet'.
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(ArrayList<String> a, HashMap<String, Integer> b) {} // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: nested_inheritance_cycle_terminates ===
// imports: java.util.ArrayList
class InputCollectionInterfaceNestedCycleOuter extends InputCollectionInterfaceNestedCyclePartner {
	static class Inner extends UnknownNestedBase {
		void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
			ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'.
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
			ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'.
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
			ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'.
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
		ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	static ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_a_subtypes_other_supertype ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOtherSupertypeSliceViolation {
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	static ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_an_anonymous_subclass ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnonymousBase {
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
		ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'.
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
	ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: overload_collapse_across_an_inherited_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceInheritedCollapseBase {
	static ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
		ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'List' instead of 'ArrayList'.
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
	static void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	static void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
			ArrayList<String> dump(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'List' instead of 'ArrayList'.
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
// multi-fix-expected
class InputCollectionInterfaceOverloadCollapseReturnTypeSliceViolation {
	static ArrayList<String> dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}

	static ArrayList<String> dump(List<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return null;
	}
}
// === end ===

// === case: overload_of_a_different_arity_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceOverloadOfADifferentArityStillFlagsSliceViolation {
	static void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	void dump(ArrayList<String>... values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: parameter_at_a_non_zero_argument_index ===
// imports: java.util.ArrayList
// imports: java.util.Objects
class InputCollectionInterfaceNonZeroArgumentIndexSliceViolation {
	void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(Objects.equals(null, values));
	}
}
// === end ===

// === case: parameter_in_operand_positions_only ===
// imports: java.util.ArrayList
// imports: java.util.List
// multi-fix-expected
class InputCollectionInterfaceOperandPositionsSliceViolation {
	void concatenated(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println("rows: " + values);
	}

	void equality(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values == null);
	}

	void inequality(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values != null);
	}

	void iterated(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		for (var value : values)
			System.out.println(value);
	}

	void typeTest(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values instanceof List);
	}
}
// === end ===

// === case: parameter_is_a_warning_when_an_inherited_object_overload_could_rebind ===
// imports: java.util.ArrayList
final class InputCollectionInterfaceObjectOverloadSliceViolation {
	private final ArrayList<String> parts = new ArrayList<>();

	void matches(ArrayList<String> other) { // violation: Use 'List' instead of 'ArrayList'.
		System.out.println(parts.equals(other));
	}

	void wait(ArrayList<String> other) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(parts.equals(other));
	}
}
// === end ===

// === case: parameter_is_a_warning_when_its_coupled_return_is_pinned_by_a_caller ===
// imports: java.util.ArrayList
class InputCollectionInterfaceCoupledLockstepSliceViolation {
	private final ArrayList<String> pinned = keep(new ArrayList<>());

	private ArrayList<String> keep(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'List' instead of 'ArrayList'.
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

	private ArrayList<String> keep(ArrayList<String> values) { // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'List' instead of 'ArrayList'.
		return values;
	}

	void use() {
		System.out.println(pinned);
	}
}
// === end ===

// === case: parameter_reassigned_before_use ===
// imports: java.util.ArrayList
class InputCollectionInterfaceReassignedParamSliceViolation {
	void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		values = new ArrayList<>();
		System.out.println(values);
	}
}
// === end ===

// === case: parameter_returned_through_an_interface_return_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceReturnedAsInterfaceSliceViolation {
	List<String> f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: parameter_sharing_the_returns_interface_is_a_warning ===
// imports: java.util.ArrayList
final class InputCollectionInterfaceSharedReturnIfaceSliceViolation {
	ArrayList<String> f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: parameter_with_a_same_arity_overload_on_a_sealed_owner_is_a_warning ===
// imports: java.util.ArrayList
final class InputCollectionInterfaceOverloadRebindSliceViolation {
	void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}

	void dump(String name) {
		System.out.println(name);
	}
}
// === end ===

// === case: parameter_with_a_same_arity_supertype_overload_is_a_warning ===
// imports: java.util.ArrayList
class InputCollectionInterfaceSupertypeRebindBase {
	void dump(String name) {
		System.out.println(name);
	}
}

final class InputCollectionInterfaceSupertypeRebindSliceViolation extends InputCollectionInterfaceSupertypeRebindBase {
	void dump(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}
}
// === end ===

// === case: priority_queue_to_queue ===
// imports: java.util.PriorityQueue
class InputCollectionInterfacePriorityQueueSliceViolation {
	void f(PriorityQueue<String> items) {} // violation (warning): Use 'Queue' instead of 'PriorityQueue'.
}
// === end ===

// === case: private_member_is_auto_fixable_in_both_positions ===
// imports: java.util.ArrayList
// imports: java.util.HashSet
class InputCollectionInterfacePrivateMemberSliceViolation {
	private ArrayList<String> f(HashSet<Integer> items) { // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'Set' instead of 'HashSet'.
		return null;
	}
}
// === end ===

// === case: private_return_type_is_a_warning_behind_a_method_reference ===
// imports: java.util.ArrayList
// imports: java.util.function.Function
class InputCollectionInterfaceMethodRefReturnSliceViolation {
	private ArrayList<String> rows(String key) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return null;
	}

	void use() {
		final Function<String, ArrayList<String>> lookup = this::rows;
		System.out.println(lookup.apply("k"));
	}
}
// === end ===

// === case: private_return_type_is_a_warning_when_a_caller_pins_it ===
// imports: java.util.ArrayList
class InputCollectionInterfacePinnedReturnSliceViolation {
	private final ArrayList<String> cached = rows();

	private ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
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
	private ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
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
class InputCollectionInterfaceCoupledReturnSliceViolation {
	private final ArrayList<String> cached = copy();

	private ArrayList<String> copy() { // violation (warning): Use 'List' instead of 'ArrayList'.
		return rows();
	}

	private ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
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

	private ArrayList<String> rows() { // violation: Use 'List' instead of 'ArrayList'.
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
abstract class InputCollectionInterfaceCrossFileNoCollisionPublicMember extends AbstractMap<String, Integer> {
	public void store(HashMap<String, Integer> values) { // violation (warning): Use 'Map' instead of 'HashMap'.
		System.out.println(values);
	}
}
// === end ===

// === case: public_member_overriding_nothing_is_a_warning ===
// imports: java.util.Properties
// imports: javax.xml.transform.Transformer
abstract class InputCollectionInterfaceNonOverridePublicMember extends Transformer {
	public Properties buildProperties(Properties seed) { // violation (warning): Use 'Map' instead of 'Properties'. // violation (warning): Use 'Map' instead of 'Properties'.
		System.out.println(seed);
		return new Properties();
	}
}
// === end ===

// === case: receiver_parameter_beside_a_collection_parameter ===
// imports: java.util.ArrayList
class InputCollectionInterfaceReceiverParamSliceViolation {
	void f(InputCollectionInterfaceReceiverParamSliceViolation this, ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}
}
// === end ===

// === case: record_accessor_overload_with_parameters_is_flagged ===
// imports: java.util.ArrayList
// multi-fix-expected
record InputCollectionInterfaceAccessorOverloadSliceViolation(ArrayList<String> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
	ArrayList<String> items(int limit) { // violation (warning): Use 'List' instead of 'ArrayList'.
		return items;
	}
}
// === end ===

// === case: record_accessor_with_a_split_qualifier_rewrites_with_its_component ===
// multi-fix-expected
class InputCollectionInterfaceSplitQualifierAccessorSliceViolation {
	private record Rows(java.util.ArrayList<String> items) { // violation: Use 'List' instead of 'ArrayList'.
		@Override
		public java.util.
				ArrayList<String> items() { // violation: Use 'List' instead of 'ArrayList'.
			return items;
		}
	}
}
// === end ===

// === case: record_component_and_its_explicit_accessor_are_flagged_together ===
// imports: java.util.ArrayList
// multi-fix-expected
record InputCollectionInterfaceExplicitAccessorSliceViolation(ArrayList<String> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
	@Override
	public ArrayList<String> items() { // violation (warning): Use 'List' instead of 'ArrayList'.
		return items;
	}
}
// === end ===

// === case: record_component_flags_once_with_a_compact_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceCompactCtorSliceViolation(ArrayList<String> rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
	InputCollectionInterfaceCompactCtorSliceViolation {
		rows = new ArrayList<>(rows);
	}
}
// === end ===

// === case: record_component_flags_under_a_private_interface_method ===
// imports: java.util.ArrayList
// multi-fix-expected
interface InputCollectionInterfaceUnpinnedRows {
	private ArrayList<String> items() { // violation: Use 'List' instead of 'ArrayList'.
		return null;
	}
}

record InputCollectionInterfaceUnpinnedRecord(ArrayList<String> items) implements InputCollectionInterfaceUnpinnedRows {} // violation (warning): Use 'List' instead of 'ArrayList'.
// === end ===

// === case: record_component_flags_under_an_unrelated_interface ===
// imports: java.io.Serializable
// imports: java.util.ArrayList
record InputCollectionInterfaceUnrelatedIfaceSliceViolation(ArrayList<String> rows) implements Serializable {} // violation (warning): Use 'List' instead of 'ArrayList'.
// === end ===

// === case: record_component_in_a_nested_record_is_a_warning ===
// imports: java.util.ArrayList
class InputCollectionInterfaceNestedRecordHolder {
	record Rows(ArrayList<String> items) {} // violation (warning): Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: record_component_in_a_private_record_is_auto_fixable ===
// imports: java.util.ArrayList
class InputCollectionInterfacePrivateRecordHolder {
	private record Rows(ArrayList<String> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: record_component_in_an_unnameable_owner_is_auto_fixable ===
// imports: java.util.ArrayList
// multi-fix-expected
class InputCollectionInterfaceUnnameableRecordOwners {
	final Runnable held = new Runnable() {
		record InAnonymous(ArrayList<String> items) {} // violation: Use 'List' instead of 'ArrayList'.

		@Override
		public void run() {
		}
	};

	void m() {
		record InMethod(ArrayList<String> items) {} // violation: Use 'List' instead of 'ArrayList'.

		System.out.println(new InMethod(new ArrayList<>()));
	}
}
// === end ===

// === case: record_component_is_a_warning_when_a_body_method_returns_it ===
// imports: java.util.ArrayList
class InputCollectionInterfaceRecordCoupledBodySliceViolation {
	private record Rows(ArrayList<String> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
		ArrayList<String> items(int limit) { // violation (warning): Use 'List' instead of 'ArrayList'.
			return items;
		}
	}
}
// === end ===

// === case: record_component_is_a_warning_when_an_accessor_call_pins_it ===
// imports: java.util.ArrayList
class InputCollectionInterfacePinnedAccessorSliceViolation {
	private record Rows(ArrayList<String> items) {} // violation (warning): Use 'List' instead of 'ArrayList'.

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
	private record Rows(ArrayList<String> items) {} // violation: Use 'List' instead of 'ArrayList'.

	private final List<String> accepted = new Rows(new ArrayList<>()).items();

	void use() {
		System.out.println(accepted);
	}
}
// === end ===

// === case: record_component_pinned_by_a_same_file_interface_accessor ===
// imports: java.util.ArrayList
interface InputCollectionInterfacePinnedRows {
	ArrayList<String> items(); // violation (warning): Use 'List' instead of 'ArrayList'.
}

record InputCollectionInterfacePinnedRecord(ArrayList<String> items) implements InputCollectionInterfacePinnedRows {}
// === end ===

// === case: record_component_survives_a_differing_arity_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordArityCtorSliceViolation(ArrayList<String> rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
	InputCollectionInterfaceRecordArityCtorSliceViolation(List<String> rows, int limit) {
		this(new ArrayList<>(rows.subList(0, limit)));
	}
}
// === end ===

// === case: record_component_uses_the_collection_interface ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordComponentSliceViolation(ArrayList<String> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
	List<String> copy() {
		return new ArrayList<>(items);
	}
}
// === end ===

// === case: record_flags_only_the_collection_component ===
// imports: java.util.ArrayList
// imports: java.util.List
record InputCollectionInterfaceRecordMultiComponentSliceViolation(String name, ArrayList<String> rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
	List<String> copy() {
		return new ArrayList<>(rows);
	}
}
// === end ===

// === case: record_method_not_named_like_a_component_is_flagged ===
// imports: java.util.ArrayList
record InputCollectionInterfaceUnrelatedRecordMethodSliceViolation(int count) {
	ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
		return null;
	}
}
// === end ===

// === case: return_type_scan_ignores_a_same_named_call_on_another_type ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
class InputCollectionInterfaceUnrelatedCallSliceViolation {
	private final HashMap<String, String> index = new HashMap<>();

	void use() {
		synchronized (index.values()) {
			System.out.println(index);
		}
	}

	private ArrayList<String> values() { // violation: Use 'List' instead of 'ArrayList'.
		return null;
	}
}
// === end ===

// === case: same_file_override_pair_is_flagged_on_both_sides ===
// imports: java.util.ArrayList
// imports: java.util.List
// multi-fix-expected
class InputCollectionInterfaceOverridePairBase {
	void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverridePairSliceViolation extends InputCollectionInterfaceOverridePairBase {
	@Override
	void f(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}
}
// === end ===

// === case: signature_wrapped_onto_a_continuation_line ===
// imports: java.util.ArrayList
final class InputCollectionInterfaceWrappedSignatureSliceViolation {
	void f(
			String name,
			ArrayList<String> rows // violation: Use 'List' instead of 'ArrayList'.
	) {
		System.out.println(name + rows);
	}
}
// === end ===

// === case: supplementary_char_before_the_collection_type ===
// imports: java.util.ArrayList
class InputCollectionInterfaceSupplementaryBeforeTypeSliceViolation {
	void f(String a𝐀b, ArrayList<String> items) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(a𝐀b);
		System.out.println(items);
	}
}
// === end ===

// === case: tree_map_to_map ===
// imports: java.util.TreeMap
class InputCollectionInterfaceTreeMapSliceViolation {
	TreeMap<String, Integer> m() { return null; } // violation (warning): Use 'Map' instead of 'TreeMap'.
}
// === end ===

// === case: tree_set_to_set ===
// imports: java.util.TreeSet
class InputCollectionInterfaceTreeSetSliceViolation {
	void f(TreeSet<String> items) {} // violation (warning): Use 'Set' instead of 'TreeSet'.
}
// === end ===

// === case: unloadable_supertype_still_flags ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceUnloadableSupertypeSliceViolation extends UnknownForeignBase {
	ArrayList<String> rows(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'. // violation (warning): Use 'List' instead of 'ArrayList'.
		return values;
	}
}
// === end ===

// === case: varargs_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
class InputCollectionInterfaceVarargsOfCollectionSliceViolation {
	void f(ArrayList<String>... rows) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(rows.length);
	}
}
// === end ===