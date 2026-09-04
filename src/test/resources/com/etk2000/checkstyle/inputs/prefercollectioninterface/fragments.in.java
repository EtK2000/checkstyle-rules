// === case: block_comment_import_ignored ===
// target: line=3 col=8
/*
import java.util.Vector;
*/
	static Vector<String> getItems() {
// === end ===

// === case: block_comment_package_ignored ===
// target: line=3 col=8
/*
package java.util;
*/
	static Vector<String> getItems() {
// === end ===

// === case: cast_type_target_is_stale ===
// target: line=5 col=10
import java.util.ArrayList;
import java.util.List;

class C {
	ArrayList<String> get(List<String> items) {
		return (ArrayList<String>) items;
	}
}
// === end ===

// === case: linked_list_skipped ===
// target: line=1 col=8
import java.util.LinkedList;
	static LinkedList<String> getItems() {
// === end ===

// === case: local_variable_type_target_is_stale ===
// target: line=4 col=2
import java.util.ArrayList;

class C {
	ArrayList<String> get() {
		ArrayList<String> items = new ArrayList<>();
		return items;
	}
}
// === end ===

// === case: mid_identifier_column_is_stale ===
// target: line=1 col=9
import java.util.ArrayList;
	static ArrayList<String> getItems() {
// === end ===

// === case: multiple_params_first ===
// target: line=2 col=8
import java.util.ArrayList;
import java.util.HashMap;
	void f(ArrayList<String> a, HashMap<String, Integer> b) {}
// === end ===

// === case: multiple_params_second ===
// target: line=2 col=24
import java.util.ArrayList;
import java.util.HashMap;
	void f(List<String> a, HashMap<String, Integer> b) {}
// === end ===

// === case: new_expression_type_target_is_stale ===
// target: line=4 col=13
import java.util.ArrayList;

class C {
	ArrayList<String> get() {
		return new ArrayList<>();
	}
}
// === end ===

// === case: no_match ===
// target: line=1 col=8
import java.util.List;
	void f(List<String> items) {}
// === end ===

// === case: package_and_static_import_resolved ===
// target: line=3 col=1
package com.foo;
import static java.util.Collections.emptyList;
import java.util.ArrayList;
	ArrayList<String> m() { return null; }
// === end ===

// === case: record_accessor_qualifier_segment_skipped ===
// target: line=1 col=13
record Rows(java.util.ArrayList<String> items, java.lang.String tag) {
	public java.lang.String tag() {
		return tag;
	}
}
// === end ===

// === case: record_component_qualifier_segment_skipped ===
// target: line=0 col=17
record Rows(java.util.ArrayList<String> items) {}
// === end ===

// === case: record_word_in_comment_not_a_pair ===
// target: line=1 col=8
import java.util.ArrayList;
	static ArrayList<String> getItems() { // record
// === end ===

// === case: record_word_in_string_not_a_pair ===
// target: line=1 col=8
import java.util.ArrayList;
	static ArrayList<String> getItems() { return of("record");
// === end ===

// === case: replacement_import_already_present ===
// target: line=2 col=1
import java.util.ArrayList;
import java.util.List;
	ArrayList<String> m() { return null; }
// === end ===

// === case: return_and_param_param ===
// target: line=2 col=21
import java.util.ArrayList;
import java.util.HashSet;
	ArrayList<String> f(HashSet<Integer> items) {
// === end ===

// === case: return_and_param_return ===
// target: line=2 col=1
import java.util.ArrayList;
import java.util.HashSet;
	ArrayList<String> f(HashSet<Integer> items) {
// === end ===

// === case: unparseable_record_declaration_skipped ===
// target: line=1 col=12
import java.util.ArrayList;
	record Box(ArrayList<String> items) {
// === end ===

// === case: unresolvable_qualified_name_skipped ===
// target: line=0 col=18
	static com.bogus.Widget<String> getItems() {
// === end ===

// === case: unresolvable_simple_name_skipped ===
// target: line=0 col=8
	static Widget<String> getItems() {
// === end ===