package com.etk2000.checkstyle.inputs.prefercollectioninterface;

// === case: array_deque_to_deque ===
// imports: java.util.ArrayDeque
// imports: java.util.Deque
class InputCollectionInterfaceArrayDequeSliceViolation {
	void f(Deque<String> items) {}
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

// === case: array_list_to_list_in_annotated_generic_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnnotatedGenericArgSliceViolation {
	void f(List<@SuppressWarnings("unused") String> items) {}
}
// === end ===

// === case: array_list_to_list_in_annotated_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceAnnotatedTypeArgSliceViolation {
	void f(List<@SuppressWarnings("unused") List<String>> items) {}
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

// === case: array_list_to_list_in_intersection_bound ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfaceIntersectionBoundSliceViolation {
	<T extends Comparable<T> & java.io.Serializable> List<T> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_multi_level_nested_generic ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
class InputCollectionInterfaceMultiLevelNestingSliceViolation {
	Map<String, Map<Integer, List<String>>> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_nested_generic_param ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
class InputCollectionInterfaceNestedGenericParamSliceViolation {
	void f(Map<String, List<Integer>> items) {}
}
// === end ===

// === case: array_list_to_list_in_nested_generic_return ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
class InputCollectionInterfaceNestedGenericReturnSliceViolation {
	Map<String, List<Integer>> m() { return null; }
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

// === case: concurrent_hash_map_fqn ===
// imports: java.util.Map
class InputCollectionInterfaceConcurrentHashMapFqnSliceViolation {
	Map<String, Integer> lookup() { return null; }
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

// === case: hash_set_to_set_in_wildcard_super ===
// imports: java.util.HashSet
// imports: java.util.Set
class InputCollectionInterfaceWildcardSuperSliceViolation {
	void f(Set<? super Set<Integer>> items) {}
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

// === case: priority_queue_to_queue ===
// imports: java.util.PriorityQueue
// imports: java.util.Queue
class InputCollectionInterfacePriorityQueueSliceViolation {
	void f(Queue<String> items) {}
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