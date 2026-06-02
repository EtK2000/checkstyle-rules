package com.etk2000.checkstyle.inputs.multilinecall;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class InputMultilineCallClean {
	Object arg, arg1, arg2, x;
	InputMultilineCallClean obj;
	Map<String, String> cache;

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

	void computeIfAbsentComplexKey() {
		cache.computeIfAbsent(arg + "x", k -> {
			System.out.println(k);
		});
	}

	void computeIfAbsentExpressionKey() {
		cache.computeIfAbsent(arg.toString(), k -> {
			System.out.println(k);
		});
	}

	void computeIfAbsentKeyCommentSuppressed() {
		cache.computeIfAbsent(
				"k", // note
				k -> {
					System.out.println(k);
				});
	}

	void computeIfAbsentNestedLambdaInWrapper() {
		cache.computeIfAbsent(
				"k",
				wrapper(() -> {
					System.out.println("x");
				})
		);
	}

	void computeIfAbsentNoArgs() {
		cache.computeIfAbsent(
		);
	}

	void computeIfAbsentSimpleKey() {
		cache.computeIfAbsent("k", k -> {
			System.out.println(k);
		});
	}

	void computeIfAbsentSingleLambdaArg() {
		cache.computeIfAbsent(x -> {
			System.out.println(x);
		});
	}

	void computeIfAbsentTypeWitness() {
		cache.<String>computeIfAbsent("k", k -> {
			System.out.println(k);
		});
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

	void getQuantityStringDottedContextCallTypeWitness() {
		method(fragment.<Object>getContext().getResources().getQuantityString(
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

	void getStringDottedContextCallTypeWitness() {
		method(fragment.<Object>getContext().getString(
				1
		));
	}

	void getStringDottedTrackedVariable() {
		final var ctx = obj.getContext();
		method(ctx.getString(
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

	void getStringTrackedVariableTypeWitness() {
		final var ctx = obj.<Object>getContext();
		method(ctx.getString(
				1
		));
	}

	void method() {
	}

	void method(BiConsumer<Integer, Integer> c) {
	}

	void method(Consumer<Integer> c) {
	}

	void method(Object a) {
	}

	void method(Runnable r) {
	}

	void method(Object a, Object b) {
	}

	void method(int a, int b, int c) {
	}

	void multiLineAnonClassArg() {
		method(
				new Runnable() {
					public void run() {
						step1();
					}
				},
				other
		);
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

	void postDelayedAnonClassStacked() {
		handler.postDelayed(new Runnable() {
			public void run() {
				doThing();
			}
		}, 1000);
	}

	void postDelayedMethodRefStaysClean() {
		handler.postDelayed(this::doThing, 1000);
	}

	void postDelayedOneLineSingleExpr() {
		handler.postDelayed(() -> doThing(), 1000);
	}

	void postDelayedReturnBodyStacked() {
		handler.postDelayed(() -> {
			return;
		}, 1000);
	}

	void postDelayedWithBracelessLambda() {
		handler.postDelayed(
				() -> System.out.println("delayed"),
				1000
		);
	}

	void putAnonClass() {
		cache.put("k", new Runnable() {
			public void run() {
			}
		});
	}

	void putBareNoReceiverStaysClean() {
		put("k", arg);
	}

	void putBracedLambda() {
		cache.put("k", x -> {
			System.out.println(x);
		});
	}

	void putBracelessLambda() {
		cache.put("k", v ->
				System.out.println(v)
		);
	}

	void putChainedConstructor() {
		cache.put("k", new JSONObject()
				.put("a", 1)
				.put("b", 2)
		);
	}

	void putJsonObjectNonSimpleValuesStayClean() {
		new JSONObject()
				.put("k", new JSONObject());
		new JSONObject()
				.put("k", arg.toString().trim());
		new JSONObject()
				.put("k", x != null ? arg1 : arg2);
		new JSONObject()
				.put("k", arg1 + arg2);
		new JSONObject()
				.put("k", () -> arg);
		new JSONObject()
				.put("k", arg.toString().length);
		new JSONObject()
				.put("k", new Object());
		new JSONObject()
				.put("k", -arg.toString().length());
	}

	void putJsonObjectSinglePutJustOverLimit() {
		new JSONObject()
				.put("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk", 1);
	}

	void putJsonObjectSinglePutMissingValue() {
		new JSONObject()
				.put("k");
	}

	void putJsonObjectSinglePutOnOneLine() {
		cache.put("k", new JSONObject().put("a", 1));
	}

	void putJsonObjectSinglePutOverLimit() {
		cache.put("someExtremelyLongKeyNameThatByItselfAlreadyForcesUsWellBeyondTheColumnBudget", new JSONObject()
				.put("anotherFairlyLongInnerKeyNameHere", 123456789)
		);
	}

	void putJsonObjectSinglePutThreeArgs() {
		new JSONObject()
				.put("a", "b", arg);
	}

	void putJsonObjectSinglePutTopLevelOneLine() {
		new JSONObject().put("a", 1);
	}

	void putJsonObjectStringWithSlashesOverLimit() {
		new JSONObject()
				.put("k", "a // this string literal contains slashes and is long enough to push the collapsed form well beyond the limit");
	}

	void putKeyNotOnOpeningTrailingComment() {
		cache.put(
				"k", // note
				new JSONObject()
						.put("a", 1)
						.put("b", 2)
		);
	}

	void putKeyOwnLineChainedValueOver120() {
		cache.put(
				"aKeyNameLongEnoughThatTheCollapsedSingleLineFormWouldExceedOneHundredTwentyColumns",
				new JSONObject().put("innerKeyNameAlsoContributingLength", "theValueString")
		);
	}

	void putMultiLineKeyCommentOnSecondLineSuppressed() {
		cache.put(
				"prefix"
						+ "suffix", // note
				new JSONObject()
						.put("a", 1)
						.put("b", 2)
		);
	}

	void putNestedInlineBlockPut() {
		cache.put("View", new JSONObject()
				.put("Account", new JSONObject().put("id", 1))
		);
	}

	void putNestedMultilinePut() {
		cache.put("View", new JSONObject()
				.put("Account", new JSONObject()
						.put("id", 1)
						.put("name", "x")
				)
		);
	}

	void putNonJsonObjectReceiverStaysClean() {
		new HashMap<>()
				.put("k", 1);
	}

	void putSingleLambdaArg() {
		cache.put(x -> {
			System.out.println(x);
		});
	}

	void putSingleLine() {
		cache.put("k", "v");
	}

	void putSpecialMethodValue() {
		cache.put("k", List.of(
				1, 2, 3
		));
	}

	void putTernaryValue() {
		cache.put("k", true
				? new JSONObject()
				: arg);
	}

	void putThisFirstArg() {
		cache.put(this, x -> {
			System.out.println(x);
		});
	}

	void putTypeWitness() {
		cache.<String>put("k", new JSONObject()
				.put("a", 1)
				.put("b", 2)
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

	void singleArraysAsListFqn() {
		method(java.util.Arrays.asList(
				1, 2, 3
		));
	}

	void singleBracelessLambdaMultiline() {
		method(v ->
				System.out.println(v)
		);
	}

	void singleConstructorOnCallLine() {
		method(new ArrayList<>(Arrays.asList(
				1, 2, 3
		)));
	}

	void singleConstructorWithRegularArg() {
		method(new ArrayList<>(
				Collections.nCopies(3, 1)
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

	void singleListOfFqnTypeWitness() {
		method(java.util.List.<Integer>of(
				1, 2, 3
		));
	}

	void singleListOfTypeWitness() {
		method(List.<Integer>of(
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
		method(this, new ArrayList<>(Arrays.asList(
				1, 2, 3
		)));
	}

	void thisAndConstructorWithRegularArg() {
		method(this, new ArrayList<>(
				Collections.nCopies(3, 1)
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

class InputMultilineCallGetQuantityStringNotContext {
	void bareGetResourcesReceiver() {
		method(
				getResources().getQuantityString(
						1,
						2
				)
		);
	}

	Object getBundle() {
		return null;
	}

	Object getResources() {
		return null;
	}

	void method(Object a) {
	}

	void noGetResourcesInChain(Context ctx) {
		method(
				ctx.getQuantityString(
						1,
						2
				)
		);
	}

	void nonContextParameterGetResources(String notContext) {
		method(
				notContext.getResources().getQuantityString(
						1,
						2
				)
		);
	}

	void unknownReceiverGetResources() {
		final var bundle = getBundle();
		method(
				bundle.getResources().getQuantityString(
						1,
						2
				)
		);
	}
}

class InputMultilineCallGetStringNotContext {
	void bareGetString() {
		method(
				getString(
						1
				)
		);
	}

	void dottedNonContextMethodGetString() {
		method(
				something.notAContextMethod().getString(
						1
				)
		);
	}

	Object getBundle() {
		return null;
	}

	Object getResources() {
		return null;
	}

	String getString(int id) {
		return null;
	}

	void method(Object a) {
	}

	void nonContextAssignmentGetString() {
		final var res = getResources();
		method(
				res.getString(
						1
				)
		);
	}

	void nonContextParameterGetString(String notContext) {
		method(
				notContext.getString(
						1
				)
		);
	}

	void unknownReceiverGetString() {
		final var bundle = getBundle();
		method(
				bundle.getString(
						"key"
				)
		);
	}
}