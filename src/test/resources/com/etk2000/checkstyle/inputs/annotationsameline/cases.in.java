package com.etk2000.checkstyle.inputs.annotationsameline;

@interface A {}
@interface Arr {
	int[] value();
}
@interface B {}
@interface B𝒜 {}
@interface C {}
@interface Chr {
	char value();
}
@interface D {}
@interface Multi {
	int a();

	int b();
}
@interface Nest {
	A value();
}
@interface Num {
	int value();
}
@interface Val {
	String value();
}
@interface Ä {}

// === case: annotation_with_params ===
class InputAnnotationSameLineAnnotationWithParamsSliceViolation {
	void m(
			@SuppressWarnings("unchecked") // violation: Annotation '@SuppressWarnings' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: contexts_catch_param ===
class InputAnnotationSameLineCatchParamSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (
				@A // violation: Annotation '@A' must be on the same line as the declaration.
				Exception e
		) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: contexts_constructor_param ===
class InputAnnotationSameLineConstructorParamSliceViolation {
	InputAnnotationSameLineConstructorParamSliceViolation(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineForEachSliceViolation {
	void m(List<String> list) {
		for (
				@A // violation: Annotation '@A' must be on the same line as the declaration.
				var item : list
		)
			System.out.println(item);
	}
}
// === end ===

// === case: contexts_for_each_multi_annotation ===
// imports: java.util.List
class InputAnnotationSameLineForEachMultiSliceViolation {
	void m(List<String> list) {
		for (
				@A // violation: Annotation '@A' must be on the same line as the declaration.
				@B
				var item : list
		)
			System.out.println(item);
	}
}
// === end ===

// === case: contexts_for_init ===
class InputAnnotationSameLineForInitSliceViolation {
	void m() {
		for (
				@A // violation: Annotation '@A' must be on the same line as the declaration.
				var i = 0; i < 10; ++i
		)
			System.out.println(i);
	}
}
// === end ===

// === case: contexts_lambda_param ===
// imports: java.util.function.Consumer
class InputAnnotationSameLineLambdaParamSliceViolation {
	void m() {
		final Consumer<String> c = (
				@A // violation: Annotation '@A' must be on the same line as the declaration.
				var s
		) -> {};
	}
}
// === end ===

// === case: contexts_method_param ===
class InputAnnotationSameLineMethodParamSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: contexts_method_param_multi_annotation ===
class InputAnnotationSameLineMethodParamMultiSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			@B
			String param
	) {}
}
// === end ===

// === case: contexts_method_param_out_of_order ===
class InputAnnotationSameLineMethodParamOutOfOrderSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			@A
			String param
	) {}
}
// === end ===

// === case: contexts_receiver_param ===
class InputAnnotationSameLineContextsReceiverParamSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			InputAnnotationSameLineContextsReceiverParamSliceViolation this
	) {}
}
// === end ===

// === case: contexts_record_component ===
class InputAnnotationSameLineRecordComponentSliceViolation {
	record Data(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			String name
	) {}
}
// === end ===

// === case: entry_state_block_comment_before_inline_annotations ===
// imports: java.util.List
class InputAnnotationSameLineEntryStateBlockCommentSliceViolation {
	void m(List<String> list) {
		for (/* sorted by
		, uh */ @B @A var item : list) // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			System.out.println(item);
	}
}
// === end ===

// === case: inline_reorder_after_annotation_with_named_arg ===
class InputAnnotationSameLineInlineReorderAfterNamedArgSliceViolation {
	void foo(@B(num = 1) @C @A String param) {} // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: inline_reorder_after_annotation_with_string_comma ===
class InputAnnotationSameLineInlineReorderAfterStringCommaSliceViolation {
	void foo(@B("a, @Y @X") @C @A String param) {} // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: inline_reorder_after_annotation_with_string_paren ===
class InputAnnotationSameLineInlineReorderAfterStringParenSliceViolation {
	void foo(@B("a(b") @C @A String param) {} // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: inline_reorder_three_annotations ===
class InputAnnotationSameLineInlineReorderThreeAnnotationsSliceViolation {
	void foo(@C @A @B String param) {} // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: inline_reorder_two_annotations ===
class InputAnnotationSameLineInlineReorderTwoAnnotationsSliceViolation {
	void foo(@B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: keeps_non_annotation_modifier ===
class InputAnnotationSameLineKeepsNonAnnotationModifierSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (@B final @A RuntimeException e) { // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: merge_single_annotation_line ===
class InputAnnotationSameLineMergeSliceViolation {
	void m(
			@Deprecated // violation: Annotation '@Deprecated' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: merge_three_annotation_lines ===
class InputAnnotationSameLineMergeThreeAnnotationLinesSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			@B
			@C
			String param
	) {}
}
// === end ===

// === case: order_anonymous_class_param ===
class InputAnnotationSameLineOrderAnonymousClassParamSliceViolation {
	interface Visitor {
		void visit(String first, String second);
	}

	Visitor visitor = new Visitor() {
		@Override
		public void visit(@B @A String first, String second) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	};
}
// === end ===

// === case: order_array_arg_with_commas ===
class InputAnnotationSameLineOrderArrayArgWithCommasSliceViolation {
	void m(@Arr({2, 1}) @A String param) {} // violation: Annotation '@A' must appear before '@Arr' (alphabetical order).
}
// === end ===

// === case: order_char_literal_paren ===
class InputAnnotationSameLineOrderCharLiteralParenSliceViolation {
	void m(@Chr('(') @C @A String param) {} // violation: Annotation '@C' must appear before '@Chr' (alphabetical order). // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_char_literal_quote ===
class InputAnnotationSameLineOrderCharLiteralQuoteSliceViolation {
	void m(@Chr('"') @C @A String param) {} // violation: Annotation '@C' must appear before '@Chr' (alphabetical order). // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_comment_after_at ===
class InputAnnotationSameLineOrderCommentAfterAtSliceViolation {
	void m(@/* c */B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_comment_between_annotations ===
class InputAnnotationSameLineOrderCommentBetweenAnnotationsSliceViolation {
	void m(@B /* c */ @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_comment_inside_argument_list ===
class InputAnnotationSameLineOrderCommentInsideArgumentListSliceViolation {
	void m(@Num(/* c */ 1) @A String param) {} // violation: Annotation '@A' must appear before '@Num' (alphabetical order).
}
// === end ===

// === case: order_enum_constructor_param ===
class InputAnnotationSameLineOrderEnumConstructorParamSliceViolation {
	enum E {
		ALPHA("a");

		E(@B @A String name) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	}
}
// === end ===

// === case: order_generic_method_param ===
class InputAnnotationSameLineOrderGenericMethodParamSliceViolation {
	<T> void m(@B @A T param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_generic_record ===
class InputAnnotationSameLineOrderGenericRecordSliceViolation {
	record Pair<L, R>(@B @A L left, R right) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_local_class_param ===
class InputAnnotationSameLineOrderLocalClassParamSliceViolation {
	void m() {
		class Local {
			void inner(@B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
		}

		new Local().inner("x");
	}
}
// === end ===

// === case: order_multi_catch ===
class InputAnnotationSameLineOrderMultiCatchSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (@B @A IllegalStateException | IllegalArgumentException e) { // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: order_multi_declarator_for_init ===
class InputAnnotationSameLineOrderMultiDeclaratorForInitSliceViolation {
	void m() {
		for (@B @A var i = 0, j = 1; i < j; ++i) // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			System.out.println(i);
	}
}
// === end ===

// === case: order_multiple_named_elements ===
class InputAnnotationSameLineOrderMultipleNamedElementsSliceViolation {
	void m(@Multi(a = 1, b = 2) @A String param) {} // violation: Annotation '@A' must appear before '@Multi' (alphabetical order).
}
// === end ===

// === case: order_nested_annotation_value ===
class InputAnnotationSameLineOrderNestedAnnotationValueSliceViolation {
	void m(@Nest(@A) @B String param) {} // violation: Annotation '@B' must appear before '@Nest' (alphabetical order).
}
// === end ===

// === case: order_non_ascii_name ===
class InputAnnotationSameLineOrderNonAsciiNameSliceViolation {
	void m(@Ä @A String param) {} // violation: Annotation '@A' must appear before '@Ä' (alphabetical order).
}
// === end ===

// === case: order_qualified_name ===
class InputAnnotationSameLineOrderQualifiedNameSliceViolation {
	void m(@java.lang.Deprecated @A String param) {} // violation: Annotation '@A' must appear before '@Deprecated' (alphabetical order).
}
// === end ===

// === case: order_receiver_parameter ===
class InputAnnotationSameLineOrderReceiverParameterSliceViolation {
	void m(@B @A InputAnnotationSameLineOrderReceiverParameterSliceViolation this) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_record_component_second_of_two ===
class InputAnnotationSameLineOrderRecordComponentSecondOfTwoSliceViolation {
	record Data(@A String name, @C @B int value) {} // violation: Annotation '@B' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_space_after_at ===
class InputAnnotationSameLineOrderSpaceAfterAtSliceViolation {
	void m(@ B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_string_with_at ===
class InputAnnotationSameLineOrderStringWithAtSliceViolation {
	void m(@Val("@X @Y") @C @A String param) {} // violation: Annotation '@C' must appear before '@Val' (alphabetical order). // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_string_with_escaped_quote ===
class InputAnnotationSameLineOrderStringWithEscapedQuoteSliceViolation {
	void m(@Val("a\"b") @C @A String param) {} // violation: Annotation '@C' must appear before '@Val' (alphabetical order). // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_string_with_line_comment_text ===
class InputAnnotationSameLineOrderStringWithLineCommentTextSliceViolation {
	void m(@Val("// @Z") @C @A String param) {} // violation: Annotation '@C' must appear before '@Val' (alphabetical order). // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: order_supplementary_name ===
class InputAnnotationSameLineOrderSupplementaryNameSliceViolation {
	void m(@B𝒜 @A String param) {} // violation: Annotation '@A' must appear before '@B𝒜' (alphabetical order).
}
// === end ===

// === case: order_three_out_of_order ===
class InputAnnotationSameLineOrderThreeOutOfOrderSliceViolation {
	void m(@C @B @A String param) {} // violation: Annotation '@B' must appear before '@C' (alphabetical order). // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: order_two_groups_on_one_line ===
class InputAnnotationSameLineOrderTwoGroupsOnOneLineSliceViolation {
	void m(@B @A String first, @D @C String second) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order). // violation: Annotation '@C' must appear before '@D' (alphabetical order).
}
// === end ===

// === case: order_varargs ===
class InputAnnotationSameLineOrderVarargsSliceViolation {
	void m(@B @A String... param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: preserves_declaration_indentation ===
class InputAnnotationSameLinePreservesDeclarationIndentationSliceViolation {
	static class Outer {
		static class Inner {
			void m(
					@A // violation: Annotation '@A' must be on the same line as the declaration.
					String param
			) {}
		}
	}
}
// === end ===

// === case: reorder_contexts_catch_param ===
class InputAnnotationSameLineOrderCatchParamSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (@B @A Exception e) { // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: reorder_contexts_constructor_param ===
class InputAnnotationSameLineOrderConstructorParamSliceViolation {
	InputAnnotationSameLineOrderConstructorParamSliceViolation(@B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: reorder_contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineOrderForEachSliceViolation {
	void m(List<String> list) {
		for (@B @A var item : list) // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			System.out.println(item);
	}
}
// === end ===

// === case: reorder_contexts_for_init ===
class InputAnnotationSameLineOrderForInitSliceViolation {
	void m() {
		for (@B @A var i = 0; i < 10; ++i) // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			System.out.println(i);
	}
}
// === end ===

// === case: reorder_contexts_lambda_param ===
// imports: java.util.function.Consumer
class InputAnnotationSameLineOrderLambdaParamSliceViolation {
	void m() {
		final Consumer<String> c = (@B @A var s) -> {}; // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	}
}
// === end ===

// === case: reorder_contexts_method_param ===
class InputAnnotationSameLineOrderMethodParamSliceViolation {
	void m(@B @A String param) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: reorder_contexts_multi_param ===
class InputAnnotationSameLineOrderMultiParamSliceViolation {
	void m(
			@C @A String param1, // violation: Annotation '@A' must appear before '@C' (alphabetical order).
			@A @B String param2
	) {}
}
// === end ===

// === case: reorder_contexts_record_component ===
class InputAnnotationSameLineOrderRecordComponentSliceViolation {
	record Data(@B @A String name) {} // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: same_line_annotations_two_per_line ===
class InputAnnotationSameLineAnnotationsTwoPerLineSliceViolation {
	void m(
			@D @B // violation: Annotation '@D' must be on the same line as the declaration.
			@C @A
			String param
	) {}
}
// === end ===

// === case: same_line_blank_line_inside_run ===
class InputAnnotationSameLineBlankLineInsideRunSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.

			@A String param
	) {}
}
// === end ===

// === case: same_line_block_comment_before_inline_annotation ===
class InputAnnotationSameLineBlockCommentBeforeInlineAnnotationSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			/* c */ @A String param
	) {}
}
// === end ===

// === case: same_line_block_comment_crossing_lines ===
class InputAnnotationSameLineBlockCommentCrossingLinesSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			/* one
			two */
			@A String param
	) {}
}
// === end ===

// === case: same_line_comment_above_group_starting_mid_line ===
class InputAnnotationSameLineCommentAboveGroupStartingMidLineSliceViolation {
	void m(String[] items) {
		for (@B /* note // violation: Annotation '@B' must be on the same line as the declaration.
		more */ @A var item : items)
			System.out.println(item);
	}
}
// === end ===

// === case: same_line_comment_after_declaration ===
class InputAnnotationSameLineCommentAfterDeclarationSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			@A String param // note
	) {}
}
// === end ===

// === case: same_line_comment_before_first_annotation ===
class InputAnnotationSameLineCommentBeforeFirstAnnotationSliceViolation {
	void m(
			/* c */ @B // violation: Annotation '@B' must be on the same line as the declaration.
			@A String param
	) {}
}
// === end ===

// === case: same_line_comment_between_annotation_lines ===
class InputAnnotationSameLineCommentBetweenAnnotationLinesSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			// why
			@A String param
	) {}
}
// === end ===

// === case: same_line_comment_between_last_annotation_and_decl ===
class InputAnnotationSameLineCommentBetweenLastAnnotationAndDeclSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			// why
			String param
	) {}
}
// === end ===

// === case: same_line_comment_inside_argument_list ===
// skip-reason: cannot join the annotations onto the declaration line: one of them cannot be written on a single line
class InputAnnotationSameLineCommentInsideArgumentListSliceViolation {
	void m(
			@Num( // violation: Annotation '@Num' must be on the same line as the declaration.
			/* c */ 1)
			@A String param
	) {}
}
// === end ===

// === case: same_line_hoists_above_open_block_comment ===
class InputAnnotationSameLineHoistsAboveOpenBlockCommentSliceViolation {
	void m(/* outer
			*/ @A /* c */ // violation: Annotation '@A' must be on the same line as the declaration.
			int b
	) {}
}
// === end ===

// === case: same_line_hoists_above_open_block_comment_spanning ===
class InputAnnotationSameLineHoistsAboveOpenBlockCommentSpanningSliceViolation {
	void m(/* outer
			still open
			*/ @A /* c */ // violation: Annotation '@A' must be on the same line as the declaration.
			int b
	) {}
}
// === end ===

// === case: same_line_line_budget_just_over ===
class InputAnnotationSameLineLineBudgetJustOverSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			@Val("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx") String param
	) {}
}
// === end ===

// === case: same_line_line_budget_just_under ===
class InputAnnotationSameLineLineBudgetJustUnderSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			@Val("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx") String param
	) {}
}
// === end ===

// === case: same_line_multiline_args_after_sibling ===
class InputAnnotationSameLineMultilineArgsAfterSiblingSliceViolation {
	void m(
			@C @Num( // violation: Annotation '@C' must be on the same line as the declaration.
			1)
			@A String param
	) {}
}
// === end ===

// === case: same_line_multiline_args_string_paren ===
class InputAnnotationSameLineMultilineArgsStringParenSliceViolation {
	void m(
			@C @Val("(" // violation: Annotation '@C' must be on the same line as the declaration.
					+ ")")
			@A String param
	) {}
}
// === end ===

// === case: same_line_split_qualified_name ===
class InputAnnotationSameLineSplitQualifiedNameSliceViolation {
	void m(
			@java.lang // violation: Annotation '@Deprecated' must be on the same line as the declaration.
			.Deprecated String param
	) {}
}
// === end ===

// === case: same_line_text_block_argument ===
// skip-reason: cannot join the annotations onto the declaration line: one of them cannot be written on a single line
class InputAnnotationSameLineTextBlockArgumentSliceViolation {
	void m(
			@A // violation: Annotation '@A' must be on the same line as the declaration.
			@Val("""
					value""")
			String param
	) {}
}
// === end ===

// === case: same_line_trailing_line_comment ===
class InputAnnotationSameLineTrailingLineCommentSliceViolation {
	void m(
			@B // violation: Annotation '@B' must be on the same line as the declaration.
			@A // why
			String param
	) {}
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationSameLineSortsAlphabeticallySliceViolation {
	void m(
			@C // violation: Annotation '@C' must be on the same line as the declaration.
			@A
			@B
			String param
	) {}
}
// === end ===

// === case: two_annotations_on_same_line ===
class InputAnnotationSameLineTwoAnnotationsOnSameLineSliceViolation {
	void m(
			@A @B // violation: Annotation '@A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===