package com.etk2000.checkstyle.inputs.preferbulkoperation;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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

	void arrayCopyRhsIndexNotLoopVar(int[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = arr[0];
	}

	void arrayFillDotBoundNotLength(int[] arr) {
		for (var i = 0; i < Integer.MAX_VALUE; ++i)
			arr[i] = 0;
	}

	void arrayFillLiteralBound(int[] arr) {
		for (var i = 0; i < 10; ++i)
			arr[i] = 0;
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

	void arrayFillMismatchedArray(int[] arr, int[] other) {
		for (var i = 0; i < arr.length; ++i)
			other[i] = 0;
	}

	void arrayFillMultiLineCommentedValue(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = -a[b[ // note
					0]];
	}

	void arrayFillNonConstant(int[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = i * 2;
	}

	void arrayFillNonZeroStart(int[] arr) {
		for (var i = 1; i < arr.length; ++i)
			arr[i] = 0;
	}

	void arrayFillTextBlock(String[] arr) {
		for (var i = 0; i < arr.length; ++i)
			arr[i] = """
					value
					""";
	}

	void forEachLambdaBlockBodyMultiStatement(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> {
			System.out.println(k);
			target.put(k, v);
		});
	}

	void forEachLambdaComplexTargetNonAdd(List<String> list, List<String> a, List<String> b, boolean cond) {
		list.forEach(item -> (cond ? a : b).remove(item));
	}

	void forEachLambdaComplexTargetTransformed(Map<String, String> source, Map<String, String> a, Map<String, String> b, boolean cond) {
		source.forEach((k, v) -> (cond ? a : b).put(k, v.toUpperCase()));
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

	void forEachMethodRefSplitQualifier(List<String> list, List<String> other) {
		list.forEach(
				other
						::add
		);
	}

	void forEachMultiLinePreOperandCommentSource(List<String> target, List<String> src) {
		for (var item : /* pre */ src // in
				.subList(0, 1))
			target.add(item);
	}

	void forEachStreamSource(List<String> list, List<String> other) {
		list.stream().forEach(other::add);
	}

	void forEachValuesSourcePut(Map<String, String> target, Map<String, String> source) {
		for (var v : source.values())
			target.put(v, v);
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

	void indexedComparisonLhsNotLoopVar(int[] arr, int limit) {
		for (var i = 0; limit < arr.length; ++i)
			arr[i] = 0;
	}

	void indexedLeComparison(int[] arr) {
		for (var i = 0; i <= arr.length - 1; ++i)
			arr[i] = 0;
	}

	void indexedMultiVarInit(int[] arr) {
		for (int i = 0, j = 0; i < arr.length; ++i)
			arr[i] = j;
	}

	void indexedNonLtComparison(List<String> target, List<String> source) {
		for (var i = 0; i != source.size(); ++i)
			target.add(source.get(i));
	}

	void indexedNonSimpleIncrement(int[] arr) {
		for (var i = 0; i < arr.length; i += 1)
			arr[i] = 0;
	}

	void indexedPutBodyNotAdd(Map<String, String> target, List<String> keys, List<String> vals) {
		for (var i = 0; i < keys.size(); ++i)
			target.put(keys.get(i), vals.get(i));
	}

	void lambdaArgumentToAdd(List<String> names, List<Runnable> tasks) {
		names.forEach(name -> tasks.add(() -> System.out.println(name)));
	}

	void lambdaArgumentToPut(Map<String, String> source, Map<String, Supplier<String>> target) {
		source.forEach((k, v) -> target.put(k, () -> v));
	}

	void lambdaKeyArgumentToPut(Map<String, String> source, Map<Supplier<String>, String> target) {
		source.forEach((k, v) -> target.put(() -> k, k));
	}

	void methodRefArgumentToAdd(List<String> names, List<IntSupplier> tasks) {
		names.forEach(name -> tasks.add(name::length));
	}

	void methodRefArgumentToPut(Map<String, String> source, Map<String, Supplier<String>> target) {
		source.forEach((k, v) -> target.put(k, v::toString));
	}

	void methodRefKeyArgumentToPut(Map<String, String> source, Map<Supplier<String>, String> target) {
		source.forEach((k, v) -> target.put(k::toString, k));
	}

	void putKeyMismatch(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet())
			target.put(entry.getValue(), entry.getValue());
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