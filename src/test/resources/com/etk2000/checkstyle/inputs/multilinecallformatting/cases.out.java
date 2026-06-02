package com.etk2000.checkstyle.inputs.multilinecall;

// === case: android_resource_id_lambda_not_on_opening ===
// imports: java.util.function.Consumer
class InputMultilineCallResourceIdAndroidLambdaNotOnOpeningSliceViolation {
	void m() {
		method(android.R.string.ok, x -> {
			System.out.println(x);
		});
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: android_resource_id_question_two_after ===
class InputMultilineCallAndroidResourceIdQuestionTwoAfterSliceViolation {
	void m() {
		method(android.R.string.ok, true
				? "a"
				: "b"
		);
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: anon_class_closing_not_on_closing ===
class InputMultilineCallAnonClassClosingNotOnClosingSliceViolation {
	void m() {
		method(new Runnable() {
			public void run() {
			}
		});
	}

	void method(Runnable r) {
	}
}
// === end ===

// === case: anon_class_not_on_opening ===
class InputMultilineCallAnonClassNotOnOpeningSliceViolation {
	void m() {
		method(new Runnable() {
			public void run() {
			}
		});
	}

	void method(Runnable r) {
	}
}
// === end ===

// === case: braceless_lambda_closing_on_body_line ===
// imports: java.util.function.Consumer
class InputMultilineCallLambdaBracelessClosingOnBodyLineSliceViolation {
	void m() {
		method(v -> System.out.println(v));
	}

	void method(Consumer<Integer> c) {
	}
}
// === end ===

// === case: braceless_lambda_not_on_opening ===
// imports: java.util.function.Consumer
class InputMultilineCallLambdaBracelessNotOnOpeningSliceViolation {
	void m() {
		method(v ->
				System.out.println(v)
		);
	}

	void method(Consumer<Integer> c) {
	}
}
// === end ===

// === case: braceless_lambda_over_120 ===
// imports: java.util.function.Consumer
class InputMultilineCallBracelessLambdaOver120SliceViolation {
	void m() {
		method(v ->
				System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
		);
	}

	void method(Consumer<Integer> c) {
	}
}
// === end ===

// === case: call_nested_generic_comma_sharing ===
// imports: java.util.Map
class InputMultilineCallSharedLineNestedGenericCommaSliceViolation {
	void m() {
		method(Map.of("k", 1), other, z);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: chained_constructor_closing_on_chain_line ===
// imports: org.json.JSONObject
class InputMultilineCallChainedConstructorClosingOnChainLineSliceViolation {
	void m() {
		method(new JSONObject().put("key", "value").put("key2", "value2"));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: chained_constructor_not_on_opening_line ===
// imports: org.json.JSONObject
class InputMultilineCallChainedConstructorNotOnOpeningLineSliceViolation {
	void m() {
		method(new JSONObject()
				.put("key", "value")
				.put("key2", "value2")
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: chained_constructor_over_120 ===
// imports: org.json.JSONObject
class InputMultilineCallChainedConstructorOver120SliceViolation {
	void m() {
		method(new JSONObject()
				.put("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "value")
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: closing_collapse_comment_fallback ===
class InputMultilineCallClosingCollapseCommentFallbackSliceViolation {
	void m() {
		method(
				1, // inner note
				2
		);
	}

	void method(int a, int b) {
	}
}
// === end ===

// === case: closing_collapse_exactly_120 ===
class InputMultilineCallClosingCollapseExactly120SliceViolation {
	void m() {
		method("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 2);
	}

	void method(Object a, int b) {
	}
}
// === end ===

// === case: closing_collapse_over_120 ===
class InputMultilineCallClosingCollapseOver120SliceViolation {
	void m() {
		method(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				2
		);
	}

	void method(Object a, int b) {
	}
}
// === end ===

// === case: closing_collapse_textblock_fallback ===
class InputMultilineCallClosingCollapseTextblockFallbackSliceViolation {
	void m() {
		method(
				"""
				hello
				""",
				2
		);
	}

	void method(Object a, int b) {
	}
}
// === end ===

// === case: colon_after_multiline_true_branch ===
class InputMultilineCallColonAfterMultilineTrueBranchSliceViolation {
	void m() {
		method(true
				? "a" + "b"
				: "c"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: colon_on_question_line ===
class InputMultilineCallTernaryPositionColonOnQuestionLineSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: colon_two_lines_after_true ===
class InputMultilineCallTernaryPositionColonTwoLinesAfterTrueSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: computeifabsent_braceless_lambda_not_exempt ===
// imports: java.util.Map
class InputMultilineCallComputeIfAbsentBracelessLambdaNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.computeIfAbsent("k", x ->
				x.trim());
	}
}
// === end ===

// === case: computeifabsent_dotted_key_not_on_opening ===
// imports: java.util.Map
class InputMultilineCallComputeIfAbsentDottedKeyNotOnOpeningSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.computeIfAbsent("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_key_not_on_opening ===
// imports: java.util.function.Consumer
class InputMultilineCallComputeIfAbsentKeyNotOnOpeningSliceViolation {
	void computeIfAbsent(Object a, Consumer<Integer> c) {
	}

	void m() {
		computeIfAbsent("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_key_not_on_opening_only ===
// imports: java.util.Map
class InputMultilineCallComputeIfAbsentKeyNotOnOpeningOnlySliceViolation {
	Map<String, String> cache;

	void m() {
		cache.computeIfAbsent("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_lambda_not_on_closing ===
// imports: java.util.function.Consumer
class InputMultilineCallComputeIfAbsentLambdaNotOnClosingSliceViolation {
	void computeIfAbsent(Object a, Consumer<Integer> c) {
	}

	void m() {
		computeIfAbsent("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_method_ref_not_exempt ===
// imports: java.util.Map
class InputMultilineCallComputeIfAbsentMethodRefNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.computeIfAbsent("k", String::trim);
	}
}
// === end ===

// === case: computeifabsent_three_args_not_exempt ===
// imports: java.util.function.Consumer
class InputMultilineCallComputeIfAbsentThreeArgsNotExemptSliceViolation {
	void computeIfAbsent(Object a, Object b, Consumer<Integer> c) {
	}

	void m() {
		computeIfAbsent("k", "v", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_type_witness_not_on_opening ===
// imports: java.util.Map
class InputMultilineCallComputeIfAbsentTypeWitnessNotOnOpeningSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.<String>computeIfAbsent("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: computeifabsent_variable_not_exempt ===
// imports: java.util.Map
// imports: java.util.function.Function
class InputMultilineCallComputeIfAbsentVariableNotExemptSliceViolation {
	Function<String, String> factory;
	Map<String, String> cache;

	void m() {
		cache.computeIfAbsent("k", factory);
	}
}
// === end ===

// === case: computeifpresent_two_arg_lambda_not_exempt ===
// imports: java.util.Map
class InputMultilineCallComputeIfPresentTwoArgLambdaNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.computeIfPresent("k", (k, v) -> {
			System.out.println(v);
		});
	}
}
// === end ===

// === case: constructor_first_arg_on_opening ===
class InputMultilineCallConstructorFirstArgOnOpeningSliceViolation {
	static class Foo {
		Foo(int a, int b) {
		}
	}

	void m() {
		new Foo(1, 2);
	}
}
// === end ===

// === case: constructor_not_on_closing_line ===
// imports: java.util.ArrayList
// imports: java.util.Collections
// imports: java.util.List
class InputMultilineCallConstructorNotOnClosingLineSliceViolation {
	void m() {
		method(new ArrayList<>(
				Collections.nCopies(3, 1)
		));
	}

	void method(List<Integer> list) {
	}
}
// === end ===

// === case: constructor_not_on_opening_line ===
// imports: java.util.ArrayList
// imports: java.util.Collections
// imports: java.util.List
class InputMultilineCallConstructorNotOnOpeningLineSliceViolation {
	void m() {
		method(new ArrayList<>(
				Collections.nCopies(3, 1)
		));
	}

	void method(List<Integer> list) {
	}
}
// === end ===

// === case: constructor_own_rparen_closing ===
class InputMultilineCallConstructorOwnRparenClosingSliceViolation {
	static class Foo {
		Foo(int a, int b) {
		}
	}

	void m() {
		new Foo(1, 2);
	}
}
// === end ===

// === case: constructor_stacked_closing_not_stacked ===
// imports: java.util.ArrayList
class InputMultilineCallMethodCallConstructorStackedClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(new ArrayList<>(other(
				arg
		)));
	}

	void method(Object a) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: constructor_ternary_question_on_condition_line ===
class InputMultilineCallConstructorTernaryQuestionOnConditionLineSliceViolation {
	void m() {
		new Foo(true
				? "a"
				: "b"
		);
	}
}
// === end ===

// === case: constructor_unstacked_closing_stacked ===
// imports: java.util.ArrayList
class InputMultilineCallMethodCallConstructorUnstackedClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(new ArrayList<>(other(arg)));
	}

	void method(Object a) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: ctordef_first_two_sharing ===
class InputMultilineCallSharedLineCtorDefSliceViolation {
	InputMultilineCallSharedLineCtorDefSliceViolation(int a, int b, int c) {
	}
}
// === end ===

// === case: def_annotated_sharing ===
class InputMultilineCallSharedLineDefAnnotatedSliceViolation {
	void m(int a, @Deprecated String b, int c) {
	}
}
// === end ===

// === case: def_first_two_sharing ===
class InputMultilineCallSharedLineDefFirstTwoSharingSliceViolation {
	void m(int a, int b, int c) {
	}
}
// === end ===

// === case: def_generic_comma_sharing ===
// imports: java.util.Map
class InputMultilineCallSharedLineDefGenericCommaSliceViolation {
	void m(Map<String, Integer> a, int b, int c) {
	}
}
// === end ===

// === case: def_last_two_sharing ===
class InputMultilineCallSharedLineDefLastTwoSharingSliceViolation {
	void m(int a, int b, int c) {
	}
}
// === end ===

// === case: def_varargs_sharing ===
class InputMultilineCallSharedLineDefVarargsSliceViolation {
	void m(int a, String b, int... xs) {
	}
}
// === end ===

// === case: definition_constructor_closing ===
class InputMultilineCallDefinitionConstructorClosingSliceViolation {
	InputMultilineCallDefinitionConstructorClosingSliceViolation(int a, int b) {
	}
}
// === end ===

// === case: definition_constructor_opening ===
class InputMultilineCallDefinitionConstructorOpeningSliceViolation {
	InputMultilineCallDefinitionConstructorOpeningSliceViolation(int a, int b) {
	}
}
// === end ===

// === case: definition_method_closing ===
class InputMultilineCallDefinitionMethodClosingSliceViolation {
	void m(int a, int b) {
	}
}
// === end ===

// === case: definition_method_closing_nested ===
class InputMultilineCallDefinitionMethodClosingNestedSliceViolation {
	static class Inner {
		void m(int a, int b) {
		}
	}
}
// === end ===

// === case: definition_method_opening ===
class InputMultilineCallDefinitionMethodOpeningSliceViolation {
	void m(int a, int b) {
	}
}
// === end ===

// === case: definition_method_over_120 ===
class InputMultilineCallDefinitionMethodOver120SliceViolation {
	void mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm(
			int a,
			int b
	) {
	}
}
// === end ===

// === case: delay_not_on_closing_line ===
class InputMultilineCallPostDelayedDelayNotOnClosingLineSliceViolation {
	void m() {
		handler.postDelayed(() -> System.out.println("delayed"), 1000);
	}
}
// === end ===

// === case: false_branch_multiline ===
class InputMultilineCallFalseBranchMultilineSliceViolation {
	void m() {
		method(true
				? "a"
				: "b" + "c"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: first_two_sharing ===
class InputMultilineCallSharedLineFirstTwoSharingSliceViolation {
	void m() {
		method(1, 2, 3);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: get_string_not_on_closing ===
class InputMultilineCallSpecialMethodGetStringNotOnClosingSliceViolation {
	void m() {
		method(requireContext().getString(
				1
		));
	}

	void method(Object a) {
	}

	Object requireContext() {
		return null;
	}
}
// === end ===

// === case: get_string_tracked_var_type_witness_not_on_closing ===
class InputMultilineCallSpecialMethodGetStringTrackedVarTypeWitnessNotOnClosingSliceViolation {
	Object thing;

	void m() {
		final var ctx = thing.<Object>getContext();
		method(ctx.getString(
				1
		));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: get_string_type_witness_not_on_closing ===
class InputMultilineCallSpecialMethodGetStringTypeWitnessNotOnClosingSliceViolation {
	void m() {
		method(fragment.<Object>getContext().getString(
				1
		));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: lambda_not_on_closing_line ===
// imports: java.util.function.Consumer
class InputMultilineCallLambdaNotOnClosingLineSliceViolation {
	void m() {
		method(x -> {
			System.out.println(x);
		});
	}

	void method(Consumer<Integer> c) {
	}
}
// === end ===

// === case: lambda_not_on_opening_line ===
// imports: java.util.function.Consumer
class InputMultilineCallLambdaNotOnOpeningLineSliceViolation {
	void m() {
		method(x -> {
			System.out.println(x);
		});
	}

	void method(Consumer<Integer> c) {
	}
}
// === end ===

// === case: last_two_sharing ===
class InputMultilineCallSharedLineLastTwoSharingSliceViolation {
	void m() {
		method(1, 2, 3);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: list_of_fqn_not_on_opening ===
// skip-reason: multiline formatting fix not yet supported for this shape
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfFqnNotOnOpeningSliceViolation {
	void m() {
		method(
				java.util.List.of(
						1, 2, 3
				)
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: list_of_fqn_type_witness_not_on_opening ===
// skip-reason: multiline formatting fix not yet supported for this shape
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfFqnTypeWitnessNotOnOpeningSliceViolation {
	void m() {
		method(
				java.util.List.<Integer>of(
						1, 2, 3
				)
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: list_of_not_on_opening ===
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfNotOnOpeningSliceViolation {
	void m() {
		method(List.of(
				1, 2, 3
		));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: list_of_type_witness_not_on_opening ===
// imports: java.util.List
class InputMultilineCallSpecialMethodListOfTypeWitnessNotOnOpeningSliceViolation {
	void m() {
		method(List.<Integer>of(
				1, 2, 3
		));
	}

	void method(Object a) {
	}
}
// === end ===

// === case: method_chain_after_closing ===
class InputMultilineCallMethodChainAfterClosingSliceViolation {
	void bar() {
	}

	InputMultilineCallMethodChainAfterClosingSliceViolation foo(int a, int b) {
		return this;
	}

	void m() {
		foo(1, 2).bar();
	}
}
// === end ===

// === case: method_first_arg_on_opening ===
class InputMultilineCallOpeningMethodFirstArgOnOpeningSliceViolation {
	void m() {
		method(1, 2);
	}

	void method(int a, int b) {
	}
}
// === end ===

// === case: method_three_on_one_line ===
class InputMultilineCallSharedLineThreeOnOneLineSliceViolation {
	void m() {
		method(1, 2, 3, 4);
	}

	void method(int a, int b, int c, int d) {
	}
}
// === end ===

// === case: method_two_args_on_closing ===
class InputMultilineCallClosingMethodTwoArgsOnClosingSliceViolation {
	void m() {
		method(1, 2);
	}

	void method(int a, int b) {
	}
}
// === end ===

// === case: nested_stack_multiline_over_120 ===
class InputMultilineCallNestedStackMultilineOver120SliceViolation {
	Object arg;

	void m() {
		method(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				other(arg)
		);
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: nested_stack_over_120 ===
class InputMultilineCallNestedStackOver120SliceViolation {
	Object arg;

	void m() {
		method(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				other(arg)
		);
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: nested_ternary_false_branch_preserved ===
class InputMultilineCallNestedTernaryFalseBranchPreservedSliceViolation {
	void m() {
		method(true
				? "a"
				: (b ? "c" : "d")
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: new_foo_first_two_sharing ===
class InputMultilineCallSharedLineNewFooSliceViolation {
	static class Foo {
		Foo(int a, int b, int c) {
		}
	}

	void m() {
		new Foo(1, 2, 3);
	}
}
// === end ===

// === case: non_special_two_arg_inline_lambda ===
// imports: java.util.function.Consumer
class InputMultilineCallNonSpecialTwoArgInlineLambdaSliceViolation {
	void m() {
		store("k", x -> {
			System.out.println(x);
		});
	}

	void store(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: nonspecial_receiver_of_sharing ===
class InputMultilineCallSharedLineNonSpecialOfSliceViolation {
	Object obj;

	void m() {
		obj.of(1, 2, 3);
	}
}
// === end ===

// === case: opening_collapse_comment_fallback ===
class InputMultilineCallOpeningCollapseCommentFallbackSliceViolation {
	void m() {
		method(
				1,
				2 // trailing note
		);
	}

	void method(int a, int b) {
	}
}
// === end ===

// === case: opening_collapse_exactly_120 ===
class InputMultilineCallOpeningCollapseExactly120SliceViolation {
	void m() {
		method("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 2);
	}

	void method(Object a, int b) {
	}
}
// === end ===

// === case: opening_collapse_over_120 ===
class InputMultilineCallOpeningCollapseOver120SliceViolation {
	void m() {
		method(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				2
		);
	}

	void method(Object a, int b) {
	}
}
// === end ===

// === case: opening_collapse_textblock_fallback ===
class InputMultilineCallOpeningCollapseTextblockFallbackSliceViolation {
	void m() {
		method(
				1,
				"""
				hello
				"""
		);
	}

	void method(int a, Object b) {
	}
}
// === end ===

// === case: postdelayed_anon_class_not_on_opening ===
class InputMultilineCallPostDelayedAnonClassNotOnOpeningSliceViolation {
	void m() {
		handler.postDelayed(new Runnable() {
			public void run() {
				doThing();
			}
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_anon_class_with_field ===
class InputMultilineCallPostDelayedAnonClassWithFieldSliceViolation {
	void m() {
		handler.postDelayed(new Runnable() {
			int count;

			public void run() {
				++count;
			}
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_anon_class_with_method ===
class InputMultilineCallPostDelayedAnonClassWithMethodSliceViolation {
	void m() {
		handler.postDelayed(new Runnable() {
			public void run() {
				stop();
			}

			void stop() {
			}
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_braced_lambda_fits_one_line ===
class InputMultilineCallPostDelayedBracedLambdaFitsOneLineSliceViolation {
	void m() {
		handler.postDelayed(() -> System.out.println("delayed"), 1000);
	}
}
// === end ===

// === case: postdelayed_comment_join ===
// skip-reason: cannot pull the argument onto the opening paren line: a comment on a joined line would swallow the rest
class InputMultilineCallPostDelayedCommentJoinSliceViolation {
	void m() {
		handler.postDelayed(() -> {
			doThing(); // note
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_lambda_not_on_opening_but_delay_on_closing ===
class InputMultilineCallPostDelayedLambdaNotOnOpeningButDelayOnClosingSliceViolation {
	void m() {
		handler.postDelayed(() -> System.out.println("delayed"), 1000);
	}
}
// === end ===

// === case: postdelayed_lambda_not_on_opening_line ===
class InputMultilineCallPostDelayedLambdaNotOnOpeningLineSliceViolation {
	void m() {
		handler.postDelayed(() -> System.out.println("delayed"), 1000);
	}
}
// === end ===

// === case: postdelayed_multi_statement_fallback ===
class InputMultilineCallPostDelayedMultiStatementFallbackSliceViolation {
	void m() {
		handler.postDelayed(() -> {
			System.out.println("a");
			System.out.println("b");
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_over_120_fallback ===
class InputMultilineCallPostDelayedOver120FallbackSliceViolation {
	void m() {
		handler.postDelayed(() -> {
			System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		}, 1000);
	}
}
// === end ===

// === case: postdelayed_return_body_fallback ===
class InputMultilineCallPostDelayedReturnBodyFallbackSliceViolation {
	void m() {
		handler.postDelayed(() -> {
			return;
		}, 1000);
	}
}
// === end ===

// === case: println_first_arg_on_opening ===
class InputMultilineCallOpeningPrintlnFirstArgOnOpeningSliceViolation {
	void m() {
		System.out.println(1, 2, 3);
	}
}
// === end ===

// === case: println_three_args_on_closing ===
class InputMultilineCallClosingPrintlnThreeArgsOnClosingSliceViolation {
	void m() {
		System.out.println(1, 2, 3);
	}
}
// === end ===

// === case: put_anon_class_not_on_closing ===
// imports: java.util.Map
class InputMultilineCallPutAnonClassNotOnClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new Runnable() {
			public void run() {
			}
		});
	}
}
// === end ===

// === case: put_bare_new_not_on_closing ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutBareNewNotOnClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new JSONObject(
				"x"
		));
	}
}
// === end ===

// === case: put_bare_no_receiver_not_on_closing ===
// imports: java.util.function.Consumer
class InputMultilineCallPutBareNoReceiverNotOnClosingSliceViolation {
	void m() {
		put("k", x -> {
			System.out.println(x);
		});
	}

	void put(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: put_braced_lambda_not_on_closing ===
// imports: java.util.Map
class InputMultilineCallPutBracedLambdaNotOnClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: put_braceless_lambda_closing_on_body_line ===
// imports: java.util.Map
class InputMultilineCallPutBracelessLambdaClosingOnBodyLineSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", v -> System.out.println(v));
	}
}
// === end ===

// === case: put_chained_constructor_closing_on_chain_line ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutChainedConstructorClosingOnChainLineSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new JSONObject().put("a", 1).put("b", 2));
	}
}
// === end ===

// === case: put_chained_constructor_key_not_on_opening ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutChainedConstructorKeyNotOnOpeningSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new JSONObject()
				.put("a", 1)
				.put("b", 2)
		);
	}
}
// === end ===

// === case: put_key_comment_on_value_head_line ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyCommentOnValueHeadLineSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new JSONObject() // note
				.put("a", 1)
				.put("b", 2)
		);
	}
}
// === end ===

// === case: put_key_infeasible_comment_closing ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyInfeasibleCommentClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put(
				"k", // note
				new JSONObject()
						.put("a", 1)
						.put("b", 2)
		);
	}
}
// === end ===

// === case: put_key_infeasible_comment_closing_nested ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyInfeasibleCommentClosingNestedSliceViolation {
	Map<String, String> cache;

	void m() {
		if (cache != null) {
			cache.put(
					"k", // note
					new JSONObject()
							.put("a", 1)
							.put("b", 2)
			);
		}
	}
}
// === end ===

// === case: put_key_infeasible_length_closing ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyInfeasibleLengthClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put(
				"aKeyNameLongEnoughToPushTheCollapsedFormPastOneHundredTwentyColumnsWithRoom",
				new JSONObject().put("k", 1)
		);
	}
}
// === end ===

// === case: put_key_masked_slashslash_string ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyMaskedSlashslashStringSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("a // b", new JSONObject()
				.put("x", 1)
				.put("y", 2)
		);
	}
}
// === end ===

// === case: put_key_not_on_opening_blockcomment_value ===
// imports: java.util.Map
class InputMultilineCallPutKeyNotOnOpeningBlockCommentValueSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", () -> {
			/* line1
					line2 */
			System.out.println("x");
		});
	}
}
// === end ===

// === case: put_key_not_on_opening_braced_lambda ===
// imports: java.util.Map
class InputMultilineCallPutKeyNotOnOpeningBracedLambdaSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: put_key_not_on_opening_only ===
// imports: java.util.Map
class InputMultilineCallPutKeyNotOnOpeningOnlySliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: put_key_not_on_opening_textblock_tail_value ===
// imports: java.util.Map
class InputMultilineCallPutKeyNotOnOpeningTextBlockTailValueSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", () -> {
			return """
							text""";
		});
	}
}
// === end ===

// === case: put_key_not_on_opening_textblock_value ===
// imports: java.util.Map
class InputMultilineCallPutKeyNotOnOpeningTextBlockValueSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", () -> {
			return """
							hello
							""";
		});
	}
}
// === end ===

// === case: put_key_own_line_chained_value ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyOwnLineChainedValueSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("productCharacteristics", new JSONObject().put("shortSavingDepositName", "monthly"));
	}
}
// === end ===

// === case: put_key_own_line_chained_value_exactly_120 ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyOwnLineChainedValueExactly120SliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new JSONObject().put("bbbbbbbbbbbbbbbbbbbb", "cccccccc"));
	}
}
// === end ===

// === case: put_key_own_line_multiline_chained_value ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutKeyOwnLineMultilineChainedValueSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("estValueOfOptionsFVData", new JSONObject()
				.put("ilsValue", 1)
				.put("value", 2)
		);
	}
}
// === end ===

// === case: put_key_own_line_new_jsonarray_value ===
// imports: org.json.JSONArray
// imports: org.json.JSONObject
class InputMultilineCallPutKeyOwnLineNewJsonArrayValueSliceViolation {
	void m() {
		new JSONObject().put("tiles", new JSONArray().put(new JSONObject()
				.put("source", "trading")
				.put("kind", "fx")
		)
		);
	}
}
// === end ===

// === case: put_method_ref_not_exempt ===
// imports: java.util.Map
class InputMultilineCallPutMethodRefNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", String::trim);
	}
}
// === end ===

// === case: put_nested_key_not_on_opening ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutNestedKeyNotOnOpeningSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("View", new JSONObject().put("id", 1));
	}
}
// === end ===

// === case: put_new_jsonobject_exactly_120 ===
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectExactly120SliceViolation {
	void m() {
		new JSONObject().put("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk", 1);
	}
}
// === end ===

// === case: put_new_jsonobject_nested_single_split ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectNestedSingleSplitSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("View", new JSONObject().put("Account", new JSONObject().put("id", 1)));
	}
}
// === end ===

// === case: put_new_jsonobject_same_line_receiver_closing ===
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectSameLineReceiverClosingSliceViolation {
	void m() {
		new JSONObject().put("key", "value");
	}
}
// === end ===

// === case: put_new_jsonobject_single_split ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectSingleSplitSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", new JSONObject().put("a", 1));
	}
}
// === end ===

// === case: put_new_jsonobject_text_block_value ===
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectTextBlockValueSliceViolation {
	void m() {
		new JSONObject().put(
				"k",
				"""
				text"""
		);
	}
}
// === end ===

// === case: put_new_jsonobject_trailing_comment ===
// imports: org.json.JSONObject
// skip-reason: cannot collapse: a comment sits between new JSONObject() and .put
class InputMultilineCallPutNewJsonObjectTrailingCommentSliceViolation {
	void m() {
		new JSONObject() // note
				.put("k", 1);
	}
}
// === end ===

// === case: put_new_jsonobject_value_types ===
// imports: org.json.JSONObject
class InputMultilineCallPutNewJsonObjectValueTypesSliceViolation {
	Object arg;

	void m() {
		new JSONObject().put("k", "str");
		new JSONObject().put("k", 'c');
		new JSONObject().put("k", 1L);
		new JSONObject().put("k", 1.5);
		new JSONObject().put("k", 1.5f);
		new JSONObject().put("k", true);
		new JSONObject().put("k", false);
		new JSONObject().put("k", null);
		new JSONObject().put("k", arg);
		new JSONObject().put("k", System.out);
		new JSONObject().put("k", -1);
		new JSONObject().put("k", +1);
		new JSONObject().put("k", - -1);
		new JSONObject().put("k", hashCode());
		new JSONObject().put("k", arg.hashCode());
	}
}
// === end ===

// === case: put_non_inline_block_value_not_exempt ===
// imports: java.util.Map
class InputMultilineCallPutNonInlineBlockValueNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", other(
				"v"
		));
	}

	Object other(Object a) {
		return a;
	}
}
// === end ===

// === case: put_putall_not_exempt ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutAllNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.putAll("k", new JSONObject().put("a", 1).put("b", 2));
	}
}
// === end ===

// === case: put_putifabsent_not_exempt ===
// imports: java.util.Map
// imports: org.json.JSONObject
class InputMultilineCallPutIfAbsentNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.putIfAbsent("k", new JSONObject().put("a", 1).put("b", 2));
	}
}
// === end ===

// === case: put_special_method_not_on_closing ===
// imports: java.util.List
// imports: java.util.Map
class InputMultilineCallPutSpecialMethodNotOnClosingSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", List.of(
				1, 2, 3
		));
	}
}
// === end ===

// === case: put_three_args_not_exempt ===
// imports: java.util.Map
class InputMultilineCallPutThreeArgsNotExemptSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.put("k", "extra", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: put_type_witness_key_not_on_opening ===
// imports: java.util.Map
class InputMultilineCallPutTypeWitnessKeyNotOnOpeningSliceViolation {
	Map<String, String> cache;

	void m() {
		cache.<String>put("k", x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: question_after_multiline_condition ===
class InputMultilineCallQuestionAfterMultilineConditionSliceViolation {
	void m() {
		method(a && b
				? "c"
				: "d"
		);
	}

	void method(Object o) {
	}
}
// === end ===

// === case: question_and_colon_both_shared ===
class InputMultilineCallQuestionAndColonBothSharedSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: question_on_condition_line ===
class InputMultilineCallTernaryPositionQuestionOnConditionLineSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: question_two_lines_after_condition ===
class InputMultilineCallTernaryPositionQuestionTwoLinesAfterConditionSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: resource_id_braceless_lambda_on_body_line ===
// imports: java.util.function.Consumer
class InputMultilineCallResourceIdBracelessLambdaOnBodyLineSliceViolation {
	void m() {
		method(R.string.ok, v -> System.out.println(v));
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: resource_id_colon_on_question_line ===
class InputMultilineCallResourceIdColonOnQuestionLineSliceViolation {
	void m() {
		method(R.string.ok, true
				? "a"
				: "b"
		);
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: resource_id_lambda_not_on_closing ===
// imports: java.util.function.Consumer
class InputMultilineCallResourceIdLambdaNotOnClosingSliceViolation {
	void m() {
		method(R.string.ok, x -> {
			System.out.println(x);
		});
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: resource_id_lambda_not_on_opening ===
// imports: java.util.function.Consumer
class InputMultilineCallResourceIdLambdaNotOnOpeningSliceViolation {
	void m() {
		method(R.string.ok, x -> {
			System.out.println(x);
		});
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: resource_id_stacked_closing_not_stacked ===
class InputMultilineCallMethodCallResourceIdStackedClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(R.string.ok, other(
				arg
		));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: resource_id_standard_closing_stacked ===
class InputMultilineCallMethodCallResourceIdStandardClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(R.string.ok, other(arg));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: resource_id_ternary_not_on_opening ===
class InputMultilineCallThisTernaryResourceIdNotOnOpeningSliceViolation {
	void m() {
		method(R.string.ok, true
				? "a"
				: "b"
		);
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: resource_id_unstacked_closing_not_stacked ===
class InputMultilineCallMethodCallResourceIdUnstackedClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(R.string.ok, other(arg));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: resource_id_unstacked_closing_stacked ===
class InputMultilineCallMethodCallResourceIdUnstackedClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(R.string.ok,
				other(
						arg
				));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: shared_and_both ===
class InputMultilineCallSharedLineAndBothSliceViolation {
	void m() {
		method(1, 2, 3);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: shared_and_closing ===
class InputMultilineCallSharedLineAndClosingSliceViolation {
	void m() {
		method(1, 2, 3);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: shared_and_opening ===
class InputMultilineCallSharedLineAndOpeningSliceViolation {
	void m() {
		method(1, 2, 3);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: shared_anon_class ===
class InputMultilineCallSharedAnonClassSliceViolation {
	void m() {
		method(
				new Runnable() {
					public void run() {
						step1();
					}
				},
				other
		);
	}

	void method(Runnable a, Object b) {
	}
}
// === end ===

// === case: shared_block_comment ===
class InputMultilineCallSharedLineBlockCommentSliceViolation {
	void m() {
		method(
				/* line1
				line2 */ first,
				other,
				z
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_comment_swallow ===
class InputMultilineCallSharedLineCommentSwallowSliceViolation {
	void m() {
		method(
				"a" // note
				+ "b",
				other,
				z
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_exactly_120 ===
class InputMultilineCallSharedLineExactly120SliceViolation {
	void m() {
		method("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", b, c);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_last_arg_comment ===
class InputMultilineCallSharedLineLastArgCommentSliceViolation {
	void m() {
		method(
				1,
				2,
				3 // note
		);
	}

	void method(int a, int b, int c) {
	}
}
// === end ===

// === case: shared_leading_comment_after_comma ===
class InputMultilineCallSharedLeadingCommentAfterCommaSliceViolation {
	void m() {
		method(
				a, // note
				b,
				c
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_multiline_arg ===
class InputMultilineCallSharedLineMultilineArgSliceViolation {
	void m() {
		method("a" + "b", other, z);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_nested_generic_trailing ===
// imports: java.util.Map
class InputMultilineCallSharedLineNestedGenericTrailingSliceViolation {
	void m() {
		method(x, Map.of("k", 1), z);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_over_120 ===
class InputMultilineCallSharedLineOver120SliceViolation {
	void m() {
		method(
				"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
				other,
				z
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_string_with_comma ===
class InputMultilineCallSharedLineStringCommaSliceViolation {
	void m() {
		method("a, b", c, d);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_text_block ===
class InputMultilineCallSharedLineTextBlockSliceViolation {
	void m() {
		method(
				"""
				txt""",
				other,
				z
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_text_block_last_arg ===
class InputMultilineCallSharedLineTextBlockLastArgSliceViolation {
	void m() {
		method(
				other,
				z,
				"""
				txt"""
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_text_block_middle_arg ===
class InputMultilineCallSharedLineTextBlockMiddleArgSliceViolation {
	void m() {
		method(
				other,
				"""
				txt""",
				z
		);
	}

	void method(Object a, Object b, Object c) {
	}
}
// === end ===

// === case: shared_trailing_comment_middle_arg ===
class InputMultilineCallSharedLineTrailingCommentMiddleArgSliceViolation {
	void m() {
		method(
				a,
				b,
				c, // note
				d
		);
	}

	void method(Object a, Object b, Object c, Object d) {
	}
}
// === end ===

// === case: shared_trailing_comment_nonlast_arg ===
class InputMultilineCallSharedLineTrailingCommentNonLastSliceViolation {
	void m() {
		method(
				a, // note
				b,
				c,
				d
		);
	}

	void method(Object a, Object b, Object c, Object d) {
	}
}
// === end ===

// === case: single_line_ternary_wrong_close ===
class InputMultilineCallTernarySingleLineTernaryWrongCloseSliceViolation {
	void m() {
		method(true ? "a" : "b");
	}

	void method(Object a) {
	}
}
// === end ===

// === case: stacked_calls_closing_not_stacked ===
class InputMultilineCallMethodCallStackedCallsClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(other(
				arg
		));
	}

	void method(Object a) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: super_closing ===
class InputMultilineCallSuperClosingSliceViolation {
	static class Base {
		Base(int a, int b, int c) {}
	}

	static class Inner extends Base {
		Inner() {
			super(1, 2, 3);
		}
	}
}
// === end ===

// === case: super_opening ===
class InputMultilineCallSuperOpeningSliceViolation {
	static class Base {
		Base(int a, int b, int c) {}
	}

	static class Inner extends Base {
		Inner() {
			super(1, 2, 3);
		}
	}
}
// === end ===

// === case: super_over_120 ===
class InputMultilineCallSuperOver120SliceViolation {
	static class Base {
		Base(Object a, int b, int c) {}
	}

	static class Inner extends Base {
		Inner() {
			super(
					"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
					2,
					3
			);
		}
	}
}
// === end ===

// === case: super_shared_line ===
class InputMultilineCallSuperSharedLineSliceViolation {
	static class Base {
		Base(int a, int b, int c) {}
	}

	static class Inner extends Base {
		Inner() {
			super(1, 2, 3);
		}
	}
}
// === end ===

// === case: super_ternary_question_on_condition_line ===
class InputMultilineCallSuperTernaryQuestionOnConditionLineSliceViolation extends SuperTernaryBase {
	InputMultilineCallSuperTernaryQuestionOnConditionLineSliceViolation() {
		super(true
				? "a"
				: "b"
		);
	}
}
// === end ===

// === case: ternary_block_comment_branch ===
class InputMultilineCallTernaryBlockCommentBranchSliceViolation {
	void m() {
		method(true
				? /* line1
				line2 */ "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_branch_string_with_operators ===
class InputMultilineCallTernaryBranchStringWithOperatorsSliceViolation {
	void m() {
		method(true
				? "x ? y"
				: "z )"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_closing_over_120 ===
class InputMultilineCallTernaryClosingOver120SliceViolation {
	void m() {
		method(true
				? "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_colon_and_closing ===
class InputMultilineCallTernaryColonAndClosingSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_dot_chain_true_branch ===
class InputMultilineCallTernaryDotChainTrueBranchSliceViolation {
	void m() {
		method(true
				? obj.toString()
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_exactly_120 ===
class InputMultilineCallTernaryExactly120SliceViolation {
	void m() {
		method(true
				? "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_multiline_condition_comment_swallow ===
// skip-reason: cannot re-lay-out the ternary: a comment on a joined line would swallow the rest
class InputMultilineCallTernaryMultilineConditionCommentSwallowSliceViolation {
	void m() {
		method(cond1 + // note
				cond2 ?
				"a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_multiline_condition_nested_call ===
class InputMultilineCallTernaryMultilineConditionNestedCallSliceViolation {
	void m() {
		method(value(a, b)
				? "x"
				: "y"
		);
	}

	void method(Object a) {
	}

	Object value(Object a, Object b) {
		return a;
	}
}
// === end ===

// === case: ternary_not_on_opening ===
class InputMultilineCallTernaryNotOnOpeningSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_not_on_opening_and_question ===
class InputMultilineCallTernaryNotOnOpeningAndQuestionSliceViolation {
	void m() {
		method(true
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_on_closing ===
class InputMultilineCallTernaryOnClosingSliceViolation {
	void m() {
		method(true ? "a" : "b");
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_over_120 ===
class InputMultilineCallTernaryOver120SliceViolation {
	void m() {
		method(true
				? "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_single_line_fits ===
class InputMultilineCallTernarySingleLineFitsSliceViolation {
	void m() {
		method(true ? "a" : "b");
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_single_line_over_120 ===
class InputMultilineCallTernarySingleLineOver120SliceViolation {
	void m() {
		method(true
				? "aLongEnoughStringLiteralValueThatMakesTheCollapsedTernaryExpressionExceedOneHundredTwentyColumnsForSure"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_textblock_branch ===
class InputMultilineCallTernaryTextblockBranchSliceViolation {
	void m() {
		method(true
				? """
				abc"""
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_textblock_false_branch ===
class InputMultilineCallTernaryTextblockFalseBranchSliceViolation {
	void m() {
		method(true
				? "a"
				: """
				abc"""
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: ternary_trailing_comment ===
class InputMultilineCallTernaryTrailingCommentSliceViolation {
	void m() {
		method(true // note
				? "a"
				: "b"
		);
	}

	void method(Object a) {
	}
}
// === end ===

// === case: this_braceless_lambda_on_body_line ===
// imports: java.util.function.Consumer
class InputMultilineCallThisBracelessLambdaOnBodyLineSliceViolation {
	void m() {
		method(this, v -> System.out.println(v));
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: this_lambda_not_on_closing ===
// imports: java.util.function.Consumer
class InputMultilineCallThisLambdaNotOnClosingSliceViolation {
	void m() {
		method(this, x -> {
			System.out.println(x);
		});
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: this_lambda_not_on_opening ===
// imports: java.util.function.Consumer
class InputMultilineCallThisLambdaNotOnOpeningSliceViolation {
	void m() {
		method(this, x -> {
			System.out.println(x);
		});
	}

	void method(Object a, Consumer<Integer> c) {
	}
}
// === end ===

// === case: this_question_on_condition_line ===
class InputMultilineCallThisQuestionOnConditionLineSliceViolation {
	void m() {
		method(this, true
				? "a"
				: "b"
		);
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: this_single_line_ternary_wrong_close ===
class InputMultilineCallThisTernarySingleLineWrongCloseSliceViolation {
	void m() {
		method(this, true ? "a" : "b");
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: this_stacked_calls_closing_not_stacked ===
class InputMultilineCallMethodCallThisStackedCallsClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(this, other(
				arg
		));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: this_standard_closing_stacked ===
class InputMultilineCallMethodCallThisStandardClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(this, other(arg));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: this_ternary_not_on_opening ===
class InputMultilineCallThisTernaryNotOnOpeningSliceViolation {
	void m() {
		method(this, true
				? "a"
				: "b"
		);
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: this_ternary_on_closing ===
class InputMultilineCallThisTernaryOnClosingSliceViolation {
	void m() {
		method(this, true ? "a" : "b");
	}

	void method(Object a, Object b) {
	}
}
// === end ===

// === case: this_unstacked_calls_closing_not_stacked ===
class InputMultilineCallMethodCallThisUnstackedCallsClosingNotStackedSliceViolation {
	Object arg;

	void m() {
		method(this, other(arg));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: this_unstacked_calls_closing_stacked ===
class InputMultilineCallMethodCallThisUnstackedCallsClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(this,
				other(
						arg
				));
	}

	void method(Object a, Object b) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===

// === case: unstacked_calls_closing_stacked ===
class InputMultilineCallMethodCallUnstackedCallsClosingStackedSliceViolation {
	Object arg;

	void m() {
		method(other(arg));
	}

	void method(Object a) {
	}

	Object other(Object... args) {
		return args[0];
	}
}
// === end ===