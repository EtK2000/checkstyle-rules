package com.etk2000.checkstyle.inputs.multilinecall;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

class InputMultilineCallClean {
	Object arg, arg1, arg2, x;
	InputMultilineCallClean obj;

	Object a(Object a) {
		return a;
	}

	void allArgsOnSameLine() {
		method(
				1, 2, 3
		);
	}

	void androidResourceIdAndLambda() {
		method(android.R.string.ok, x -> {
			System.out.println(x);
		});
	}

	Object b(Object a) {
		return a;
	}

	void cleanMultilineDefinition(
			int a,
			int b,
			int c
	) {
	}

	void constructorWithMethodCallArg() {
		method(new ArrayList<>(other(
				arg
		)));
	}

	void constructorWithMethodCallArgStandard() {
		method(new ArrayList<>(
				other(x)
		));
	}

	void correctMultiline() {
		System.out.println(
				"hello"
		);
	}

	void correctMultilineMultipleArgs() {
		method(
				1,
				2,
				3
		);
	}

	void emptyMultiline() {
		method(
		);
	}

	Object getActivity() {
		return null;
	}

	Object getContext() {
		return null;
	}

	void getQuantityStringContextParameter(Context ctx) {
		method(ctx.getResources().getQuantityString(
				1,
				2
		));
	}

	void getQuantityStringDirectReceiver() {
		method(requireContext().getResources().getQuantityString(
				1,
				2
		));
	}

	void getQuantityStringTrackedVariable() {
		final var ctx = requireContext();
		method(ctx.getResources().getQuantityString(
				1,
				2
		));
	}

	void getStringContextParameter(Context ctx) {
		method(ctx.getString(
				1
		));
	}

	void getStringDirectReceiver() {
		method(requireContext().getString(
				1
		));
	}

	void getStringDottedContextCall() {
		method(fragment.getContext().getString(
				1
		));
	}

	void getStringFullyQualifiedContextParameter(android.content.Context ctx) {
		method(ctx.getString(
				1
		));
	}

	void getStringGetActivityReceiver() {
		method(getActivity().getString(
				1
		));
	}

	void getStringGetContextReceiver() {
		method(getContext().getString(
				1
		));
	}

	void getStringRequireActivityReceiver() {
		method(requireActivity().getString(
				1
		));
	}

	void getStringTrackedVariable() {
		final var ctx = requireContext();
		method(ctx.getString(
				1
		));
	}

	void method() {
	}

	void method(Object a) {
	}

	void method(Runnable r) {
	}

	void method(java.util.function.BiConsumer<Integer, Integer> c) {
	}

	void method(java.util.function.Consumer<Integer> c) {
	}

	void method(Object a, Object b) {
	}

	void method(int a, int b, int c) {
	}

	void multiLineChainedConstructor() {
		method(new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void nestedChainedConstructorInWrapper() {
		method(
				wrapper(new JSONObject().put("key", "value"))
		);
	}

	void nestedLambdaInPostDelayed() {
		handler.postDelayed(
				wrapper(() -> System.out.println("delayed")),
				1000
		);
	}

	void nestedSingleLine() {
		method(
				Math.max(1, 2),
				Math.min(3, 4)
		);
	}

	void nestedSpecialMethodInWrapper() {
		method(
				wrapper(List.of(1, 2, 3))
		);
	}

	Object other(Object... args) {
		return args[0];
	}

	void postDelayedWithBracedLambda() {
		handler.postDelayed(() -> {
			System.out.println("delayed");
		}, 1000);
	}

	void postDelayedWithBracelessLambda() {
		handler.postDelayed(
				() -> System.out.println("delayed"),
				1000
		);
	}

	Object requireActivity() {
		return null;
	}

	Object requireContext() {
		return null;
	}

	void resourceIdAndBracelessLambdaMultiline() {
		method(R.string.ok, v ->
				System.out.println(v)
		);
	}

	void resourceIdAndChainedConstructor() {
		method(R.string.ok, new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void resourceIdAndLambda() {
		method(R.string.ok, x -> {
			System.out.println(x);
		});
	}

	void resourceIdAndMethodCallArg() {
		method(R.string.ok, other(
				arg
		));
	}

	void resourceIdAndMethodCallArgStandard() {
		method(
				R.string.ok,
				other(x)
		);
	}


	void resourceIdAndSingleLineTernary() {
		method(R.string.ok, true ? "a" : "b");
	}

	void resourceIdAndTernary() {
		method(R.string.ok, true
				? "a"
				: "b"
		);
	}

	void singleAnonClassOnCallLine() {
		method(new Runnable() {
			public void run() {
			}
		});
	}

	void singleArraysAsList() {
		method(Arrays.asList(
				1, 2, 3
		));
	}

	void singleBracelessLambdaMultiline() {
		method(v ->
				System.out.println(v)
		);
	}

	void singleConstructorOnCallLine() {
		method(new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		));
	}

	void singleLambdaOnCallLine() {
		method(x -> {
			System.out.println(x);
		});
	}

	void singleLambdaWithParens() {
		method((a, b) -> {
			System.out.println(a + b);
		});
	}

	void singleLineCalls() {
		System.out.println("hello");
		Math.max(1, 2);
	}

	void singleLineChainedConstructor() {
		method(new ArrayList<>().size());
	}

	void singleLineTernaryOnOpening() {
		method(true ? "a" : "b");
	}

	void singleListCopyOf() {
		method(List.copyOf(
				Arrays.asList(1, 2, 3)
		));
	}

	void singleListOf() {
		method(List.of(
				1, 2, 3
		));
	}

	void singleMapCopyOf(Map<String, Integer> m) {
		method(Map.copyOf(
				m
		));
	}

	void singleMapOf() {
		method(Map.of(
				"a", 1,
				"b", 2
		));
	}

	void singleMethodCallArgDotted() {
		method(obj.other(
				arg
		));
	}

	void singleMethodCallArgInline() {
		method(other(
				arg1,
				arg2
		));
	}

	void singleMethodCallArgNested() {
		method(a(b(
				arg
		)));
	}

	void singleMethodCallArgStandard() {
		method(
				other(x)
		);
	}

	void singleSetCopyOf(Set<Integer> s) {
		method(Set.copyOf(
				s
		));
	}

	void singleSetOf() {
		method(Set.of(
				1, 2, 3
		));
	}

	void singleTernaryArg() {
		method(true
				? "a"
				: "b"
		);
	}

	void thisAndAnonClass() {
		method(this, new Runnable() {
			public void run() {
			}
		});
	}

	void thisAndBracelessLambdaMultiline() {
		method(this, v ->
				System.out.println(v)
		);
	}

	void thisAndChainedConstructor() {
		method(this, new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void thisAndConstructor() {
		method(this, new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		));
	}

	void thisAndLambda() {
		method(this, x -> {
			System.out.println(x);
		});
	}

	void thisAndListOf() {
		method(this, List.of(
				1, 2, 3
		));
	}

	void thisAndMethodCallArg() {
		method(this, other(
				arg
		));
	}

	void thisAndMethodCallArgStandard() {
		method(
				this,
				other(x)
		);
	}

	void thisAndSingleLineTernary() {
		method(this, true ? "a" : "b");
	}

	void thisAndTernary() {
		method(this, true
				? "a"
				: "b"
		);
	}

	Object wrapper(Object a) {
		return a;
	}
}