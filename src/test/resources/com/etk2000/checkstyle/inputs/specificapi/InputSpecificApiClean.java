package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class InputSpecificApiClean {
	void collectToSet(List<String> list) {
		Set<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	void collectToUnmodifiableList(List<String> list) {
		List<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableList());
	}

	void collectWithCustomCollector(List<String> list) {
		String result = list.stream()
				.collect(Collectors.joining(", "));
	}

	void getFirst(List<String> list) {
		System.out.println(list.getFirst());
	}

	void getLast(List<String> list) {
		System.out.println(list.getLast());
	}

	void getNonZeroIndex(List<String> list) {
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void mapRemoveZero(Map<Integer, String> map) {
		map.remove(0);
	}

	void removeFirst(List<String> list) {
		list.removeFirst();
	}

	void removeLast(List<String> list) {
		list.removeLast();
	}

	void sequentialAccess(List<String> list) {
		System.out.println(list.get(0));
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void sequentialAccessFromEnd(List<String> list) {
		System.out.println(list.get(list.size() - 1));
		System.out.println(list.get(list.size() - 2));
	}

	void sequentialRemove(List<String> list) {
		list.remove(0);
		list.remove(1);
	}

	void sequentialRemoveFromEnd(List<String> list) {
		list.remove(list.size() - 1);
		list.remove(list.size() - 2);
	}

	void sizeComparisonNotEmpty(List<String> list) {
		if (list.size() > 1)
			System.out.println("more than one");
	}

	void sizeEqualsTwo(List<String> list) {
		if (list.size() == 2)
			System.out.println("pair");
	}

	void toListDirect(List<String> list) {
		List<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}
}