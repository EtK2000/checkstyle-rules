package com.etk2000.checkstyle.inputs.preferbulkoperation;

import android.content.ContentValues;
import android.util.SparseArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

class InputPreferBulkOperationClean {
	enum Weekday {
		MONDAY,
		TUESDAY
	}

	static class Bag {
		void add(String item) {
		}
	}

	static class BagWithAddAll {
		void add(String item) {
		}

		void addAll(Collection<String> items) {
		}
	}

	static class PatternShadowedInheritedField extends UnknownBase {
		// `items` here is the field inherited from a supertype this file cannot see. The pattern
		// variable of the same name is an array, and a scope-blind read of it would retype the
		// field as an array and rewrite the loop to a Collections.addAll that does not compile
		void addAllShadowed(List<String> target, Object o) {
			if (o instanceof String[] items)
				target.add(items[0]);
			for (var item : items)
				target.add(item);
		}
	}

	record PrimitiveVarargsSource(int... source) {
		void addAllTo(List<Integer> target) {
			for (var item : source)
				target.add(item);
		}
	}

	record TypeVariableVarargsSource<T>(T... source) {
		void addAllTo(List<T> target) {
			for (var item : source)
				target.add(item);
		}
	}

	// the size() and get() receivers are different lists, so the loop is not a bulk copy. Both are
	// parenthesized because a grouping paren used to make every receiver compare equal
	void addAllIndexedParenthesizedDifferentReceivers(List<String> target, List<String> a, List<String> b) {
		for (var i = 0; i < (a).size(); ++i)
			target.add((b).get(i));
	}

	void addAllSourceIterable(List<String> target, Iterable<String> source) {
		for (var item : source)
			target.add(item);
	}

	void addAllSourcePrimitiveArray(List<Integer> target, int[] source) {
		for (var item : source)
			target.add(item);
	}

	void addAllSourcePrimitiveArrayFromCall(List<Integer> target, String csv) {
		for (var codePoint : csv.codePoints().toArray())
			target.add(codePoint);
	}

	void addAllSourcePrimitiveVarargs(List<Integer> target, int... source) {
		for (var item : source)
			target.add(item);
	}

	void addAllSourceSameFileClassVarargs(List<Bag> target, Bag... source) {
		for (var item : source)
			target.add(item);
	}

	void addAllSourceSameFileEnumValues(List<Weekday> target) {
		for (var item : Weekday.values())
			target.add(item);
	}

	void addAllSourceStream(List<String> target, Stream<String> source) {
		source.forEach(target::add);
	}

	void addAllSourceUnresolvable(List<String> target, UnknownIterable source) {
		for (var item : source)
			target.add(item);
	}

	void addAllTargetLacksAddAll(SparseArray<String> target, List<String> source) {
		for (var item : source)
			target.add(item);
	}

	void addAllTargetSameFileClass(Bag target, List<String> source) {
		for (var item : source)
			target.add(item);
	}

	void addAllTargetSameFileClassDeclaringAddAll(BagWithAddAll target, List<String> source) {
		for (var item : source)
			target.add(item);
	}

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

	void forEachMethodRefAddTargetLacksAddAll(List<String> source, SparseArray<String> target) {
		source.forEach(target::add);
	}

	void forEachMethodRefPutSourceSparseArray(SparseArray<String> source, Map<String, String> target) {
		source.forEach(target::put);
	}

	void forEachMethodRefPutTargetContentValues(Map<String, String> source, ContentValues target) {
		source.forEach(target::put);
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

	void forEachTextBlockSource(List<String> target) {
		for (var item : List.of(
				"""
						value
						"""
		))
			target.add(item);
	}

	void forEachTextBlockSourceContainingCommentMarker(List<String> target) {
		for (var item : List.of(
				"""
						// not a comment
						"""
		))
			target.add(item);
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

	void indexedAddSourceSparseArray(List<String> target, SparseArray<String> source) {
		for (var i = 0; i < source.size(); ++i)
			target.add(source.get(i));
	}

	void indexedAddTargetLacksAddAll(SparseArray<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i)
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

	void lambdaAddSourceIterable(List<String> target, Iterable<String> source) {
		source.forEach(item -> target.add(item));
	}

	void lambdaAddTargetLacksAddAll(SparseArray<String> target, List<String> source) {
		source.forEach(item -> target.add(item));
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

	void lambdaPutSourceSparseArray(Map<String, String> target, SparseArray<String> source) {
		source.forEach((k, v) -> target.put(k, v));
	}

	void lambdaPutTargetContentValues(ContentValues target, Map<String, String> source) {
		source.forEach((k, v) -> target.put(k, v));
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

	void nestedForEachInnerSourceIterable(Map<String, Iterable<String>> map, List<String> target) {
		map.forEach((k, v) -> v.forEach(item -> target.add(item)));
	}

	void nestedForEachOuterCallIsNotForEach(Map<String, List<String>> map, List<String> target) {
		map.computeIfAbsent("k", v -> v.forEach(item -> target.add(item)));
	}

	void nestedForEachOuterReceiverIsCall(List<String> target) {
		nestedMap().forEach((k, v) -> v.forEach(item -> target.add(item)));
	}

	void nestedForEachRawOuterReceiver(Map map, List<String> target) {
		map.forEach((k, v) -> v.forEach(item -> target.add(item)));
	}

	void nestedForEachWildcardTypeArgument(Map<String, ?> map, List<String> target) {
		map.forEach((k, v) -> v.forEach(item -> target.add(item)));
	}

	Map<String, List<String>> nestedMap() {
		return Map.of();
	}

	void putAllSourceUnresolvable(Map<String, String> target, UnknownMap source) {
		for (var entry : source.entrySet())
			target.put(entry.getKey(), entry.getValue());
	}

	void putAllTargetContentValues(ContentValues target, Map<String, String> source) {
		for (var entry : source.entrySet())
			target.put(entry.getKey(), entry.getValue());
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