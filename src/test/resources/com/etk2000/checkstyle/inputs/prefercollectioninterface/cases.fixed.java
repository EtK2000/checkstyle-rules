// === case: array_list_to_list_fqn ===
// imports: java.util.List
class InputCollectionInterfaceArrayListFqnSliceViolation {
	List<String> m() { return null; }
}
// === end ===

// === case: array_list_to_list_wildcard_import ===
// imports: java.util.*
class InputCollectionInterfaceArrayListWildcardImportSliceViolation {
	List<String> m() { return null; }
}
// === end ===

// === case: hash_map_to_map_fqn ===
// imports: java.util.Map
class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
	void f(Map<String, Integer> items) {}
}
// === end ===