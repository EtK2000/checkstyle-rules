package com.etk2000.checkstyle.inputs.preferbulkoperation;

import java.util.List;
import java.util.Map;

class InputPreferBulkOperationViolation {
	void addAllBraced(List<String> target, List<String> source) {
		for (var item : source) { // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(item);
		}
	}

	void addAllBraceless(List<String> target, List<String> source) {
		for (var item : source) // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(item);
	}

	void addAllIndexed(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i) // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(source.get(i));
	}

	void arrayCopy(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i) // violation: Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.
			dst[i] = src[i];
	}

	void arrayFill(int[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.
			arr[i] = 0;
	}

	void arrayFillDeeplyNestedConstant(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, -a[b[0]])' instead of a loop that assigns a constant.
			arr[i] = -a[b[0]];
	}

	void arrayFillUnaryPlusConstant(int[] arr, int[] other) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, +other[0])' instead of a loop that assigns a constant.
			arr[i] = +other[0];
	}

	void forEachLambdaAddAll(List<String> list, List<String> other) {
		list.forEach(item -> other.add(item)); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}

	void forEachLambdaAddAllBlockBody(List<String> list, List<String> other) {
		list.forEach(item -> { // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
			other.add(item);
		});
	}

	void forEachLambdaAddAllParenthesized(List<String> list, List<String> other) {
		list.forEach((item) -> other.add(item)); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}

	void forEachLambdaPutAll(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(k, v)); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}

	void forEachLambdaPutAllBlockBody(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			target.put(k, v);
		});
	}

	void forEachMethodRefAdd(List<String> list, List<String> other) {
		list.forEach(other::add); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}

	void forEachMethodRefPut(Map<String, String> source, Map<String, String> target) {
		source.forEach(target::put); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}

	void putAllEntrySet(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet()) // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			target.put(entry.getKey(), entry.getValue());
	}
}