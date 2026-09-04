// === case: array_list_to_list_fqn ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListFqnSliceViolation {
	ArrayList<String> m() { return null; }
}
// === end ===

// === case: array_list_to_list_in_a_qualified_type_argument ===
// imports: java.util.List
class InputCollectionInterfaceQualifiedTypeArgSliceViolation {
	java.util.Map<String, List<Integer>> build() {
		return null;
	}
}
// === end ===

// === case: array_list_to_list_wildcard_import ===
// imports: java.util.*
class InputCollectionInterfaceArrayListWildcardImportSliceViolation {
	ArrayList<String> m() { return null; }
}
// === end ===

// === case: array_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayOfCollectionSliceViolation {
	void f(ArrayList<String>[] rows) {
		System.out.println(rows.length);
	}
}
// === end ===

// === case: concurrent_hash_map_fqn ===
class InputCollectionInterfaceConcurrentHashMapFqnSliceViolation {
	java.util.concurrent.ConcurrentHashMap<String, Integer> lookup() { return null; }
}
// === end ===

// === case: constructor_collapse_ignores_a_supertype_constructor ===
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

// === case: hash_map_to_map_fqn ===
// imports: java.util.HashMap
class InputCollectionInterfaceHashMapToMapFqnSliceViolation {
	void f(HashMap<String, Integer> items) {}
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.Set
class InputCollectionInterfaceBothReturnAndParam {
	static ArrayList<String> process(Set<Integer> items) {
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(List<String> a, Map<String, Integer> b) {}
}
// === end ===

// === case: nested_inheritance_cycle_terminates ===
// imports: java.util.ArrayList
class InputCollectionInterfaceNestedCycleOuter extends InputCollectionInterfaceNestedCyclePartner {
	static class Inner extends UnknownNestedBase {
		void f(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}

class InputCollectionInterfaceNestedCyclePartner extends InputCollectionInterfaceNestedCycleOuter {}
// === end ===

// === case: overload_collapse_ignores_a_private_supertype_method ===
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

// === case: same_file_override_pair_is_flagged_on_both_sides ===
// imports: java.util.ArrayList
class InputCollectionInterfaceOverridePairBase {
	void f(ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverridePairSliceViolation extends InputCollectionInterfaceOverridePairBase {
	@Override
	void f(ArrayList<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: signature_wrapped_onto_a_continuation_line ===
// imports: java.util.ArrayList
class InputCollectionInterfaceWrappedSignatureSliceViolation {
	void f(
			String name,
			ArrayList<String> rows
	) {
		System.out.println(name + rows);
	}
}
// === end ===

// === case: supplementary_char_before_the_collection_type ===
// imports: java.util.ArrayList
class InputCollectionInterfaceSupplementaryBeforeTypeSliceViolation {
	void f(String a𝐀b, ArrayList<String> items) {
		System.out.println(a𝐀b);
		System.out.println(items);
	}
}
// === end ===

// === case: unloadable_supertype_still_flags ===
// imports: java.util.ArrayList
class InputCollectionInterfaceUnloadableSupertypeSliceViolation extends UnknownForeignBase {
	ArrayList<String> rows(ArrayList<String> values) {
		return values;
	}
}
// === end ===

// === case: varargs_of_collection_uses_the_interface ===
// imports: java.util.ArrayList
class InputCollectionInterfaceVarargsOfCollectionSliceViolation {
	void f(ArrayList<String>... rows) {
		System.out.println(rows.length);
	}
}
// === end ===