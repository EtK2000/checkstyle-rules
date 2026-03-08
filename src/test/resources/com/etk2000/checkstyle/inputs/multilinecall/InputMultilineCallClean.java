package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

class InputMultilineCallClean {
	void singleLineCalls() {
		System.out.println("hello");
		Math.max(1, 2);
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

	void allArgsOnSameLine() {
		method(
				1, 2, 3
		);
	}

	void emptyMultiline() {
		method(
		);
	}

	void nestedSingleLine() {
		method(
				Math.max(1, 2),
				Math.min(3, 4)
		);
	}

	void singleBracelessLambdaMultiline() {
		method(v ->
				System.out.println(v)
		);
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

	void singleAnonClassOnCallLine() {
		method(new Runnable() {
			public void run() {
			}
		});
	}

	void singleConstructorOnCallLine() {
		method(new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		));
	}

	void thisAndBracelessLambdaMultiline() {
		method(this, v ->
				System.out.println(v)
		);
	}

	void thisAndLambda() {
		method(this, x -> {
			System.out.println(x);
		});
	}

	void thisAndConstructor() {
		method(this, new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		));
	}

	void thisAndAnonClass() {
		method(this, new Runnable() {
			public void run() {
			}
		});
	}

	void resourceIdAndBracelessLambdaMultiline() {
		method(R.string.ok, v ->
				System.out.println(v)
		);
	}

	void resourceIdAndLambda() {
		method(R.string.ok, x -> {
			System.out.println(x);
		});
	}

	void androidResourceIdAndLambda() {
		method(android.R.string.ok, x -> {
			System.out.println(x);
		});
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

	void singleListOf() {
		method(List.of(
				1, 2, 3
		));
	}

	void singleMapOf() {
		method(Map.of(
				"a", 1,
				"b", 2
		));
	}

	void singleArraysAsList() {
		method(Arrays.asList(
				1, 2, 3
		));
	}

	void thisAndListOf() {
		method(this, List.of(
				1, 2, 3
		));
	}

	void getStringDirectReceiver() {
		method(requireContext().getString(
				1
		));
	}

	void getStringTrackedVariable() {
		final var ctx = requireContext();
		method(ctx.getString(
				1
		));
	}

	void getStringContextParameter(Context ctx) {
		method(ctx.getString(
				1
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

	void getQuantityStringContextParameter(Context ctx) {
		method(ctx.getResources().getQuantityString(
				1,
				2
		));
	}

	void singleTernaryArg() {
		method(true
				? "a"
				: "b"
		);
	}

	void singleLineTernaryOnOpening() {
		method(true ? "a" : "b");
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

	void resourceIdAndTernary() {
		method(R.string.ok, true
				? "a"
				: "b"
		);
	}

	void resourceIdAndSingleLineTernary() {
		method(R.string.ok, true ? "a" : "b");
	}

	void singleLineChainedConstructor() {
		method(new ArrayList<>().size());
	}

	void multiLineChainedConstructor() {
		method(new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void thisAndChainedConstructor() {
		method(this, new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void resourceIdAndChainedConstructor() {
		method(R.string.ok, new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
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

	void getStringDottedContextCall() {
		method(fragment.getContext().getString(
				1
		));
	}

	void cleanMultilineDefinition(
			int a,
			int b,
			int c
	) {
	}

	Object getActivity() {
		return null;
	}

	Object getContext() {
		return null;
	}

	Object requireActivity() {
		return null;
	}

	Object requireContext() {
		return null;
	}

	void method() {
	}

	void method(int a, int b, int c) {
	}

	void method(Object a) {
	}

	void method(Object a, Object b) {
	}

	void method(Runnable r) {
	}

	void method(java.util.function.Consumer<Integer> c) {
	}

	void method(java.util.function.BiConsumer<Integer, Integer> c) {
	}
}