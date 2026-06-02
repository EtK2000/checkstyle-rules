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
