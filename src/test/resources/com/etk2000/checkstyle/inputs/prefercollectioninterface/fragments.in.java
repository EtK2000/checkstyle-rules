// === case: linked_list_skipped ===
// target: line=1 col=8
import java.util.LinkedList;
	static LinkedList<String> getItems() {
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
