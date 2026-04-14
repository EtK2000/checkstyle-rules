package com.etk2000.checkstyle.inputs.preferbulkoperation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class InputPreferBulkOperationClean {
	void addConditional(List<String> target, List<String> source) {
		for (var item : source) {
			if (!item.isEmpty())
				target.add(item);
		}
	}

	void addMultiStatement(List<String> target, List<String> source) {
		for (var item : source) {
			System.out.println(item);
			target.add(item);
		}
	}

	void addPositional(List<String> target, List<String> source) {
		for (var item : source)
			target.add(0, item);
	}

	void addTransformed(List<String> target, List<String> source) {
		for (var item : source)
			target.add(item.toUpperCase());
	}

	void arrayCopyCloneRhs(Object[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = arr[i].clone();
	}

	void arrayCopyDifferentIndex(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i)
			dst[i + 1] = src[i];
	}

	void arrayCopyMismatchedRhs(int[] dst, int[] src, int[] other) {
		for (var i = 0; i < src.length; ++i)
			dst[i] = other[i];
	}

	void arrayCopyNonZeroStart(int[] dst, int[] src) {
		for (var i = 1; i < src.length; ++i)
			dst[i] = src[i];
	}

	void arrayFillLoopVarReference(int[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = i;
	}

	void arrayFillLoopVarReferenceDeeplyNested(int[] arr, int[] a, int[] b, int[] c) {
		// Deeply-nested PURE expression referencing the loop variable. The UNARY_MINUS
		// wrapper keeps the RHS out of the INDEX_OP (arraycopy) branch, so the fill
		// branch runs; its `referencesVar` call must walk through 4 levels to find `i`,
		// exercising the iterative walk.
		for (var i = 0; i < arr.length; ++i)
			arr[i] = -a[b[c[i]]];
	}

	void arrayFillMethodCallRhs(int[] arr, List<String> list) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = list.size();
	}

	void arrayFillNonConstant(int[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = i * 2;
	}

	void arrayFillNonZeroStart(int[] arr) {
		for (var i = 1; i < arr.length; ++i)
			arr[i] = 0;
	}

	void forEachLambdaBlockBodyMultiStatement(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> {
			System.out.println(k);
			target.put(k, v);
		});
	}

	void forEachLambdaNonPutBody(Map<String, String> source) {
		source.forEach((k, v) -> System.out.println(k));
	}

	void forEachLambdaReversedArgs(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(v, k));
	}

	void forEachLambdaSingleParam(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}

	void forEachLambdaTransformed(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(k, v.toUpperCase()));
	}

	void forEachMethodRefRemove(List<String> list, List<String> other) {
		list.forEach(other::remove);
	}

	void forEachStreamSource(List<String> list, List<String> other) {
		list.stream().forEach(other::add);
	}

	void forEachWithVariable(List<String> list, Consumer<String> c) {
		list.forEach(c);
	}

	void indexedAddMismatchedSource(List<String> target, List<String> source, List<String> other) {
		for (var i = 0; i < source.size(); ++i)
			target.add(other.get(i));
	}

	void indexedAddNonZeroStart(List<String> target, List<String> source) {
		for (var i = 1; i < source.size(); ++i)
			target.add(source.get(i));
	}

	void indexedAddTransformed(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i)
			target.add(source.get(i).toUpperCase());
	}

	void indexedLeComparison(int[] arr) {
		for (var i = 0; i <= arr.length - 1; ++i)
			arr[i] = 0;
	}

	void indexedNonLtComparison(List<String> target, List<String> source) {
		for (var i = 0; i != source.size(); ++i)
			target.add(source.get(i));
	}

	void indexedNonSimpleIncrement(int[] arr) {
		for (var i = 0; i < arr.length; i += 1)
			arr[i] = 0;
	}

	void putTransformed(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet())
			target.put(entry.getKey(), entry.getValue().toUpperCase());
	}

	void unrelatedLoop(List<String> source) {
		for (var item : source)
			System.out.println(item);
	}
}