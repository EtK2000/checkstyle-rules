package com.etk2000.checkstyle.inputs.collectioninterface;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class InputCollectionInterfaceReturnArrayList {
	static ArrayList<String> getItems() { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList<>();
	}
}

class InputCollectionInterfaceParamHashMap {
	static void process(HashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'HashMap'.
}

class InputCollectionInterfaceParamHashSet {
	static void process(HashSet<String> items) {} // violation: Use 'Set' instead of 'HashSet'.
}

class InputCollectionInterfaceReturnTreeMap {
	static TreeMap<String, Integer> getItems() { // violation: Use 'Map' instead of 'TreeMap'.
		return new TreeMap<>();
	}
}

class InputCollectionInterfaceParamLinkedHashSet {
	static void process(LinkedHashSet<String> items) {} // violation: Use 'Set' instead of 'LinkedHashSet'.
}

class InputCollectionInterfaceParamLinkedHashMap {
	static void process(LinkedHashMap<String, Integer> items) {} // violation: Use 'Map' instead of 'LinkedHashMap'.
}

class InputCollectionInterfaceParamTreeSet {
	static void process(TreeSet<String> items) {} // violation: Use 'Set' instead of 'TreeSet'.
}

class InputCollectionInterfaceBothReturnAndParam {
	static ArrayList<String> process(HashSet<Integer> items) { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList<>(); // violation on parameter: Use 'Set' instead of 'HashSet'.
	}
}

class InputCollectionInterfaceMultipleParams {
	static void process(ArrayList<String> a, HashMap<String, Integer> b) {} // violation: Use 'List' instead of 'ArrayList'.
	// violation on second param: Use 'Map' instead of 'HashMap'.
}

@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawType {
	static ArrayList getItems() { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList();
	}
}

class InputCollectionInterfaceNestedGenericReturn {
	static Map<String, ArrayList<Integer>> nested() { // violation: Use 'List' instead of 'ArrayList'.
		return Map.of();
	}
}

class InputCollectionInterfaceNestedGenericParam {
	static void process(Map<String, ArrayList<Integer>> items) {} // violation: Use 'List' instead of 'ArrayList'.
}

class InputCollectionInterfaceConstructorParam {
	final List<String> items;
	int size;

	InputCollectionInterfaceConstructorParam(ArrayList<String> items) { // violation: Use 'List' instead of 'ArrayList'.
		this.items = items;
	}
}

class InputCollectionInterfaceFqnViolation {
	static java.util.ArrayList<String> fqnConcrete() { // violation: Use 'List' instead of 'ArrayList'.
		return new java.util.ArrayList<>();
	}
}

class InputCollectionInterfaceAnnotatedTypeArgViolation {
	static void process(List<@SuppressWarnings("unused") ArrayList<String>> items) {} // violation: Use 'List' instead of 'ArrayList'.
}

class InputCollectionInterfaceAnnotatedGenericArgViolation {
	static void process(ArrayList<@SuppressWarnings("unused") String> items) {} // violation: Use 'List' instead of 'ArrayList'.
}

class InputCollectionInterfaceWildcardViolation {
	static void processExtends(List<? extends ArrayList<String>> items) {} // violation: Use 'List' instead of 'ArrayList'.

	static void processSuper(Set<? super HashSet<Integer>> items) {} // violation: Use 'Set' instead of 'HashSet'.
}

class InputCollectionInterfaceMultiLevelNestingViolation {
	static Map<String, Map<Integer, ArrayList<String>>> deepNested() { // violation: Use 'List' instead of 'ArrayList'.
		return Map.of();
	}
}

class InputCollectionInterfaceArrayDeque {
	static void process(ArrayDeque<String> items) {} // violation: Use 'Deque' instead of 'ArrayDeque'.
}

class InputCollectionInterfacePriorityQueue {
	static void process(PriorityQueue<String> items) {} // violation: Use 'Queue' instead of 'PriorityQueue'.
}

class InputCollectionInterfaceBoundedWithViolation {
	static <T extends Comparable<T>> ArrayList<T> sorted(List<T> items) { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList<>(items);
	}
}

class InputCollectionInterfaceIntersectionBoundWithViolation {
	static <T extends Comparable<T> & java.io.Serializable> ArrayList<T> sorted(List<T> items) { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList<>(items);
	}
}

class InputCollectionInterfaceConcreteInBoundWithViolation {
	static <T extends ArrayList<String>> ArrayList<T> process(List<T> items) { // violation: Use 'List' instead of 'ArrayList'.
		return new ArrayList<>(items);
	}
}