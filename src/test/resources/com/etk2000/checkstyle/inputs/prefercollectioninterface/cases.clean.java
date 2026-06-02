package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class InputCollectionInterfaceClean {
	static int count() {
		return 0;
	}

	static void doNothing() {}

	static List<String> getItems() {
		return List.of();
	}

	static Map<String, Integer> getMap() {
		return Map.of();
	}

	static List<String> process(Set<Integer> items) {
		return List.of();
	}

	static void processCollection(Collection<String> items) {}

	static void processMap(Map<String, Integer> items) {}

	static void processMultiple(List<String> a, Map<String, Integer> b) {}

	static void processSet(Set<String> items) {}

	static void processString(String text) {}
}

class InputCollectionInterfaceLocalVar {
	static void method() {
		final var list = new ArrayList<String>();
		final var map = new HashMap<String, Integer>();
		final var set = new HashSet<String>();
	}
}

class InputCollectionInterfaceField {
	final List<String> list = new ArrayList<>();
	int size;
}

class InputCollectionInterfaceConstructor {
	final List<String> items;
	int size;

	InputCollectionInterfaceConstructor(List<String> items) {
		this.items = items;
	}
}

class InputCollectionInterfaceNestedGeneric {
	static Map<String, List<Integer>> nested() {
		return Map.of();
	}

	static void process(Map<String, Set<Integer>> items) {}
}

class InputCollectionInterfaceDequeParam {
	static void process(Deque<String> items) {}
}

class InputCollectionInterfaceQueueParam {
	static void process(Queue<String> items) {}
}

class InputCollectionInterfaceAbstractParam {
	static void process(AbstractList<String> items) {}
}

@SuppressWarnings("rawtypes")
class InputCollectionInterfaceRawInterface {
	static List getItems() {
		return List.of();
	}
}

class InputCollectionInterfaceLinkedList {
	static LinkedList<String> asList() {
		return new LinkedList<>();
	}
}

class InputCollectionInterfaceFqnClean {
	static java.util.List<String> fqnInterface() {
		return java.util.List.of();
	}
}

class InputCollectionInterfaceAnnotatedTypeArg {
	static void process(List<@SuppressWarnings("unused") String> items) {}

	static void processAnnotatedGenericArg(Set<@SuppressWarnings("unused") String> items) {}
}

class InputCollectionInterfaceWildcard {
	static void processExtends(List<? extends Number> items) {}

	static void processSuper(List<? super Integer> items) {}
}

class InputCollectionInterfaceMultiLevelNesting {
	static Map<String, Map<Integer, List<String>>> deepNested() {
		return Map.of();
	}
}

class InputCollectionInterfaceBoundedTypeParam {
	static <T extends Comparable<T>> List<T> sorted(List<T> items) {
		return items;
	}
}

class InputCollectionInterfaceIntersectionBound {
	static <T extends Comparable<T> & java.io.Serializable> List<T> sorted(List<T> items) {
		return items;
	}
}

class InputCollectionInterfaceConcreteInBound {
	static <T extends ArrayList<String>> void process(List<T> items) {}
}

class InputCollectionInterfaceOverloadWouldCollapse {
	static void dump(ArrayList<String> values) {
		System.out.println(values);
	}

	static void dump(List<String> values) {
		System.out.println(values);
	}
}

class InputCollectionInterfaceOverloadCollapseIsPositional {
	static void pair(ArrayList<String> first, List<String> second) {
		System.out.println(first);
		System.out.println(second);
	}

	static void pair(List<String> first, ArrayList<String> second) {
		System.out.println(first);
		System.out.println(second);
	}
}