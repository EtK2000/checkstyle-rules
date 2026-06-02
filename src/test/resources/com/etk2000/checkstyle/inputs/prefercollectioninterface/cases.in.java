package com.etk2000.checkstyle.inputs.prefercollectioninterface;

// === case: array_deque_to_deque ===
// imports: java.util.ArrayDeque
class InputCollectionInterfaceArrayDequeSliceViolation {
	void f(ArrayDeque<String> items) {} // violation: Use 'Deque' instead of 'ArrayDeque'.
}
// === end ===

// === case: array_list_to_list ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListReturnSliceViolation {
	ArrayList<String> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
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
	java.util.ArrayList<String> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_annotated_generic_arg ===
// imports: java.util.ArrayList
class InputCollectionInterfaceAnnotatedGenericArgSliceViolation {
	void f(ArrayList<@SuppressWarnings("unused") String> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_annotated_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnnotatedTypeArgSliceViolation {
	void f(List<@SuppressWarnings("unused") ArrayList<String>> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_bounded_type_param ===
// imports: java.util.ArrayList
class InputCollectionInterfaceBoundedTypeParamSliceViolation {
	<T extends Comparable<T>> ArrayList<T> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_concrete_bound ===
// imports: java.util.ArrayList
class InputCollectionInterfaceConcreteInBoundSliceViolation {
	<T extends ArrayList<String>> ArrayList<T> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_intersection_bound ===
// imports: java.util.ArrayList
class InputCollectionInterfaceIntersectionBoundSliceViolation {
	<T extends Comparable<T> & java.io.Serializable> ArrayList<T> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_multi_level_nested_generic ===
// imports: java.util.ArrayList
// imports: java.util.Map
class InputCollectionInterfaceMultiLevelNestingSliceViolation {
	Map<String, Map<Integer, ArrayList<String>>> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_nested_generic_param ===
// imports: java.util.ArrayList
// imports: java.util.Map
class InputCollectionInterfaceNestedGenericParamSliceViolation {
	void f(Map<String, ArrayList<Integer>> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_nested_generic_return ===
// imports: java.util.ArrayList
// imports: java.util.Map
class InputCollectionInterfaceNestedGenericReturnSliceViolation {
	Map<String, ArrayList<Integer>> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_in_wildcard_extends ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceWildcardExtendsSliceViolation {
	void f(List<? extends ArrayList<String>> items) {} // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_raw_type ===
// imports: java.util.ArrayList
@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawTypeSliceViolation {
	ArrayList m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: array_list_to_list_wildcard_import ===
// imports: java.util.*
class InputCollectionInterfaceArrayListWildcardImportSliceViolation {
	ArrayList<String> m() { return null; } // violation: Use 'List' instead of 'ArrayList'.
}
// === end ===

// === case: concurrent_hash_map_fqn ===
class InputCollectionInterfaceConcurrentHashMapFqnSliceViolation {
	java.util.concurrent.ConcurrentHashMap<String, Integer> lookup() { return null; } // violation: Use 'Map' instead of 'ConcurrentHashMap'.
}
// === end ===

// === case: hash_map_to_map ===
// imports: java.util.HashMap
class InputCollectionInterfaceHashMapToMapSliceViolation {
	void f(HashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: hash_map_to_map_fqn ===
// imports: java.util.HashMap
class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
	void f(java.util.HashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: hash_set_to_set ===
// imports: java.util.HashSet
class InputCollectionInterfaceHashSetToSetSliceViolation {
	void f(HashSet<String> items) {} // violation: Use 'Set' instead of 'HashSet'.
}
// === end ===

// === case: hash_set_to_set_in_wildcard_super ===
// imports: java.util.HashSet
// imports: java.util.Set
class InputCollectionInterfaceWildcardSuperSliceViolation {
	void f(Set<? super HashSet<Integer>> items) {} // violation: Use 'Set' instead of 'HashSet'.
}
// === end ===

// === case: linked_hash_map_to_map ===
// imports: java.util.LinkedHashMap
class InputCollectionInterfaceLinkedHashMapSliceViolation {
	void f(LinkedHashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'LinkedHashMap'.
}
// === end ===

// === case: linked_hash_set_to_set ===
// imports: java.util.LinkedHashSet
class InputCollectionInterfaceLinkedHashSetSliceViolation {
	void f(LinkedHashSet<String> items) {} // violation: Use 'Set' instead of 'LinkedHashSet'.
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.HashSet
class InputCollectionInterfaceBothReturnAndParam {
	static ArrayList<String> process(HashSet<Integer> items) { // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'Set' instead of 'HashSet'.
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(ArrayList<String> a, HashMap<String, Integer> b) {} // violation: Use 'List' instead of 'ArrayList'. // violation: Use 'Map' instead of 'HashMap'.
}
// === end ===

// === case: priority_queue_to_queue ===
// imports: java.util.PriorityQueue
class InputCollectionInterfacePriorityQueueSliceViolation {
	void f(PriorityQueue<String> items) {} // violation: Use 'Queue' instead of 'PriorityQueue'.
}
// === end ===

// === case: tree_map_to_map ===
// imports: java.util.TreeMap
class InputCollectionInterfaceTreeMapSliceViolation {
	TreeMap<String, Integer> m() { return null; } // violation: Use 'Map' instead of 'TreeMap'.
}
// === end ===

// === case: tree_set_to_set ===
// imports: java.util.TreeSet
class InputCollectionInterfaceTreeSetSliceViolation {
	void f(TreeSet<String> items) {} // violation: Use 'Set' instead of 'TreeSet'.
}
// === end ===