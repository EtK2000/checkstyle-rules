// === case: multiple_params_first ===
import java.util.ArrayList;
import java.util.HashMap;
	void f(List<String> a, HashMap<String, Integer> b) {}
// === end ===

// === case: multiple_params_second ===
import java.util.ArrayList;
import java.util.HashMap;
	void f(List<String> a, Map<String, Integer> b) {}
// === end ===

// === case: package_and_static_import_resolved ===
package com.foo;
import static java.util.Collections.emptyList;
import java.util.ArrayList;
	List<String> m() { return null; }
// === end ===

// === case: record_word_in_comment_not_a_pair ===
import java.util.ArrayList;
	static List<String> getItems() { // record
// === end ===

// === case: record_word_in_string_not_a_pair ===
import java.util.ArrayList;
	static List<String> getItems() { return of("record");
// === end ===

// === case: replacement_import_already_present ===
import java.util.ArrayList;
import java.util.List;
	List<String> m() { return null; }
// === end ===

// === case: return_and_param_param ===
import java.util.ArrayList;
import java.util.HashSet;
	ArrayList<String> f(Set<Integer> items) {
// === end ===

// === case: return_and_param_return ===
import java.util.ArrayList;
import java.util.HashSet;
	List<String> f(HashSet<Integer> items) {
// === end ===