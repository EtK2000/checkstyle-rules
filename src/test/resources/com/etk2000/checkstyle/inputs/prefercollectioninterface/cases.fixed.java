// === case: array_list_to_list_fqn ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListFqnSliceViolation {
	ArrayList<String> m() { return null; }
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

// === case: enum_owner_splits_the_two_tiers ===
// imports: java.util.ArrayList
// imports: java.util.Set
enum InputCollectionInterfaceEnumOwnerSliceViolation {
	;

	ArrayList<String> f(Set<Integer> items) {
		return null;
	}
}
// === end ===

// === case: final_owner_splits_the_two_tiers ===
// imports: java.util.ArrayList
// imports: java.util.Set
final class InputCollectionInterfaceFinalOwnerSliceViolation {
	ArrayList<String> f(Set<Integer> items) {
		return null;
	}
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.HashSet
class InputCollectionInterfaceBothReturnAndParam {
	static ArrayList<String> process(HashSet<Integer> items) {
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(ArrayList<String> a, HashMap<String, Integer> b) {}
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
// imports: java.util.ArrayList
// imports: java.util.List
class InputCollectionInterfacePrivateSuperCollapseBase {
	private static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfacePrivateSuperCollapseSliceViolation extends InputCollectionInterfacePrivateSuperCollapseBase {
	static void dump(ArrayList<String> values) {
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
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: parameter_is_a_warning_when_an_inherited_enum_overload_could_rebind ===
// imports: java.util.ArrayList
// imports: java.util.List
enum InputCollectionInterfaceEnumOverloadSliceViolation {
	;

	void compareTo(ArrayList<String> other) {
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

	void wait(ArrayList<String> other) {
		System.out.println(parts.equals(other));
	}
}
// === end ===

// === case: record_component_flags_once_with_a_compact_constructor ===
// imports: java.util.ArrayList
record InputCollectionInterfaceCompactCtorSliceViolation(ArrayList<String> rows) {
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

record InputCollectionInterfaceUnpinnedRecord(ArrayList<String> items) implements InputCollectionInterfaceUnpinnedRows {}
// === end ===

// === case: record_component_pinned_by_a_same_file_interface_accessor ===
// imports: java.util.ArrayList
interface InputCollectionInterfacePinnedRows {
	ArrayList<String> items();
}

record InputCollectionInterfacePinnedRecord(ArrayList<String> items) implements InputCollectionInterfacePinnedRows {}
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