// === case: array_list_to_list_fqn_warns ===
// imports: java.util.ArrayList
class InputCollectionInterfaceArrayListFqnSliceViolation {
	ArrayList<String> m() { return null; }
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

// === case: record_component_flags_once_with_a_compact_constructor_warns ===
// imports: java.util.ArrayList
record InputCollectionInterfaceCompactCtorSliceViolation(ArrayList<String> rows) {
	InputCollectionInterfaceCompactCtorSliceViolation {
		rows = new ArrayList<>(rows);
	}
}
// === end ===

// === case: same_file_override_pair_is_flagged_on_both_sides_warns ===
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

// === case: unloadable_supertype_still_flags_warns ===
// imports: java.util.ArrayList
class InputCollectionInterfaceUnloadableSupertypeSliceViolation extends UnknownForeignBase {
	ArrayList<String> rows(ArrayList<String> values) {
		return values;
	}
}
// === end ===