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
			@SuppressWarnings("unchecked") String param
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
				@A Exception e
		) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: contexts_constructor_param ===
class InputAnnotationSameLineConstructorParamSliceViolation {
	InputAnnotationSameLineConstructorParamSliceViolation(
			@A String param
	) {}
}
// === end ===

// === case: contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineForEachSliceViolation {
	void m(List<String> list) {
		for (
				@A var item : list
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
				@A @B var item : list
		)
			System.out.println(item);
	}
}
// === end ===

// === case: contexts_for_init ===
class InputAnnotationSameLineForInitSliceViolation {
	void m() {
		for (
				@A var i = 0; i < 10; ++i
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
				@A var s
		) -> {};
	}
}
// === end ===

// === case: contexts_method_param ===
class InputAnnotationSameLineMethodParamSliceViolation {
	void m(
			@A String param
	) {}
}
// === end ===

// === case: contexts_method_param_multi_annotation ===
class InputAnnotationSameLineMethodParamMultiSliceViolation {
	void m(
			@A @B String param
	) {}
}
// === end ===

// === case: contexts_method_param_out_of_order ===
class InputAnnotationSameLineMethodParamOutOfOrderSliceViolation {
	void m(
			@A @B String param
	) {}
}
// === end ===

// === case: contexts_receiver_param ===
class InputAnnotationSameLineContextsReceiverParamSliceViolation {
	void m(
			@A InputAnnotationSameLineContextsReceiverParamSliceViolation this
	) {}
}
// === end ===

// === case: contexts_record_component ===
class InputAnnotationSameLineRecordComponentSliceViolation {
	record Data(
			@A String name
	) {}
}
// === end ===

// === case: entry_state_block_comment_before_inline_annotations ===
// imports: java.util.List
class InputAnnotationSameLineEntryStateBlockCommentSliceViolation {
	void m(List<String> list) {
		for (/* sorted by
		, uh */ @A @B var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: inline_reorder_after_annotation_with_named_arg ===
class InputAnnotationSameLineInlineReorderAfterNamedArgSliceViolation {
	void foo(@A @B(num = 1) @C String param) {}
}
// === end ===

// === case: inline_reorder_after_annotation_with_string_comma ===
class InputAnnotationSameLineInlineReorderAfterStringCommaSliceViolation {
	void foo(@A @B("a, @Y @X") @C String param) {}
}
// === end ===

// === case: inline_reorder_after_annotation_with_string_paren ===
class InputAnnotationSameLineInlineReorderAfterStringParenSliceViolation {
	void foo(@A @B("a(b") @C String param) {}
}
// === end ===

// === case: inline_reorder_three_annotations ===
class InputAnnotationSameLineInlineReorderThreeAnnotationsSliceViolation {
	void foo(@A @B @C String param) {}
}
// === end ===

// === case: inline_reorder_two_annotations ===
class InputAnnotationSameLineInlineReorderTwoAnnotationsSliceViolation {
	void foo(@A @B String param) {}
}
// === end ===

// === case: keeps_non_annotation_modifier ===
class InputAnnotationSameLineKeepsNonAnnotationModifierSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (@A @B final RuntimeException e) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: merge_single_annotation_line ===
class InputAnnotationSameLineMergeSliceViolation {
	void m(
			@Deprecated String param
	) {}
}
// === end ===

// === case: merge_three_annotation_lines ===
class InputAnnotationSameLineMergeThreeAnnotationLinesSliceViolation {
	void m(
			@A @B @C String param
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
		public void visit(@A @B String first, String second) {}
	};
}
// === end ===

// === case: order_array_arg_with_commas ===
class InputAnnotationSameLineOrderArrayArgWithCommasSliceViolation {
	void m(@A @Arr({2, 1}) String param) {}
}
// === end ===

// === case: order_char_literal_paren ===
class InputAnnotationSameLineOrderCharLiteralParenSliceViolation {
	void m(@A @C @Chr('(') String param) {}
}
// === end ===

// === case: order_char_literal_quote ===
class InputAnnotationSameLineOrderCharLiteralQuoteSliceViolation {
	void m(@A @C @Chr('"') String param) {}
}
// === end ===

// === case: order_comment_after_at ===
class InputAnnotationSameLineOrderCommentAfterAtSliceViolation {
	void m(@A @/* c */B String param) {}
}
// === end ===

// === case: order_comment_between_annotations ===
class InputAnnotationSameLineOrderCommentBetweenAnnotationsSliceViolation {
	void m(@A @B /* c */ String param) {}
}
// === end ===

// === case: order_comment_inside_argument_list ===
class InputAnnotationSameLineOrderCommentInsideArgumentListSliceViolation {
	void m(@A @Num(/* c */ 1) String param) {}
}
// === end ===

// === case: order_enum_constructor_param ===
class InputAnnotationSameLineOrderEnumConstructorParamSliceViolation {
	enum E {
		ALPHA("a");

		E(@A @B String name) {}
	}
}
// === end ===

// === case: order_generic_method_param ===
class InputAnnotationSameLineOrderGenericMethodParamSliceViolation {
	<T> void m(@A @B T param) {}
}
// === end ===

// === case: order_generic_record ===
class InputAnnotationSameLineOrderGenericRecordSliceViolation {
	record Pair<L, R>(@A @B L left, R right) {}
}
// === end ===

// === case: order_local_class_param ===
class InputAnnotationSameLineOrderLocalClassParamSliceViolation {
	void m() {
		class Local {
			void inner(@A @B String param) {}
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
		catch (@A @B IllegalStateException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: order_multi_declarator_for_init ===
class InputAnnotationSameLineOrderMultiDeclaratorForInitSliceViolation {
	void m() {
		for (@A @B var i = 0, j = 1; i < j; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: order_multiple_named_elements ===
class InputAnnotationSameLineOrderMultipleNamedElementsSliceViolation {
	void m(@A @Multi(a = 1, b = 2) String param) {}
}
// === end ===

// === case: order_nested_annotation_value ===
class InputAnnotationSameLineOrderNestedAnnotationValueSliceViolation {
	void m(@B @Nest(@A) String param) {}
}
// === end ===

// === case: order_non_ascii_name ===
class InputAnnotationSameLineOrderNonAsciiNameSliceViolation {
	void m(@A @Ä String param) {}
}
// === end ===

// === case: order_qualified_name ===
class InputAnnotationSameLineOrderQualifiedNameSliceViolation {
	void m(@A @java.lang.Deprecated String param) {}
}
// === end ===

// === case: order_receiver_parameter ===
class InputAnnotationSameLineOrderReceiverParameterSliceViolation {
	void m(@A @B InputAnnotationSameLineOrderReceiverParameterSliceViolation this) {}
}
// === end ===

// === case: order_record_component_second_of_two ===
class InputAnnotationSameLineOrderRecordComponentSecondOfTwoSliceViolation {
	record Data(@A String name, @B @C int value) {}
}
// === end ===

// === case: order_space_after_at ===
class InputAnnotationSameLineOrderSpaceAfterAtSliceViolation {
	void m(@A @ B String param) {}
}
// === end ===

// === case: order_string_with_at ===
class InputAnnotationSameLineOrderStringWithAtSliceViolation {
	void m(@A @C @Val("@X @Y") String param) {}
}
// === end ===

// === case: order_string_with_escaped_quote ===
class InputAnnotationSameLineOrderStringWithEscapedQuoteSliceViolation {
	void m(@A @C @Val("a\"b") String param) {}
}
// === end ===

// === case: order_string_with_line_comment_text ===
class InputAnnotationSameLineOrderStringWithLineCommentTextSliceViolation {
	void m(@A @C @Val("// @Z") String param) {}
}
// === end ===

// === case: order_supplementary_name ===
class InputAnnotationSameLineOrderSupplementaryNameSliceViolation {
	void m(@A @B𝒜 String param) {}
}
// === end ===

// === case: order_three_out_of_order ===
class InputAnnotationSameLineOrderThreeOutOfOrderSliceViolation {
	void m(@A @B @C String param) {}
}
// === end ===

// === case: order_two_groups_on_one_line ===
class InputAnnotationSameLineOrderTwoGroupsOnOneLineSliceViolation {
	void m(@A @B String first, @C @D String second) {}
}
// === end ===

// === case: order_varargs ===
class InputAnnotationSameLineOrderVarargsSliceViolation {
	void m(@A @B String... param) {}
}
// === end ===

// === case: preserves_declaration_indentation ===
class InputAnnotationSameLinePreservesDeclarationIndentationSliceViolation {
	static class Outer {
		static class Inner {
			void m(
					@A String param
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
		catch (@A @B Exception e) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: reorder_contexts_constructor_param ===
class InputAnnotationSameLineOrderConstructorParamSliceViolation {
	InputAnnotationSameLineOrderConstructorParamSliceViolation(@A @B String param) {}
}
// === end ===

// === case: reorder_contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineOrderForEachSliceViolation {
	void m(List<String> list) {
		for (@A @B var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: reorder_contexts_for_init ===
class InputAnnotationSameLineOrderForInitSliceViolation {
	void m() {
		for (@A @B var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: reorder_contexts_lambda_param ===
// imports: java.util.function.Consumer
class InputAnnotationSameLineOrderLambdaParamSliceViolation {
	void m() {
		final Consumer<String> c = (@A @B var s) -> {};
	}
}
// === end ===

// === case: reorder_contexts_method_param ===
class InputAnnotationSameLineOrderMethodParamSliceViolation {
	void m(@A @B String param) {}
}
// === end ===

// === case: reorder_contexts_multi_param ===
class InputAnnotationSameLineOrderMultiParamSliceViolation {
	void m(
			@A @C String param1,
			@A @B String param2
	) {}
}
// === end ===

// === case: reorder_contexts_record_component ===
class InputAnnotationSameLineOrderRecordComponentSliceViolation {
	record Data(@A @B String name) {}
}
// === end ===

// === case: same_line_annotations_two_per_line ===
class InputAnnotationSameLineAnnotationsTwoPerLineSliceViolation {
	void m(
			@A @B @C @D String param
	) {}
}
// === end ===

// === case: same_line_blank_line_inside_run ===
class InputAnnotationSameLineBlankLineInsideRunSliceViolation {
	void m(
			@A @B String param
	) {}
}
// === end ===

// === case: same_line_block_comment_before_inline_annotation ===
class InputAnnotationSameLineBlockCommentBeforeInlineAnnotationSliceViolation {
	void m(
			/* c */
			@A @B String param
	) {}
}
// === end ===

// === case: same_line_block_comment_crossing_lines ===
class InputAnnotationSameLineBlockCommentCrossingLinesSliceViolation {
	void m(
			/* one
			two */
			@A @B String param
	) {}
}
// === end ===

// === case: same_line_comment_above_group_starting_mid_line ===
class InputAnnotationSameLineCommentAboveGroupStartingMidLineSliceViolation {
	void m(String[] items) {
		/* note
		more */
		for (@A @B var item : items)
			System.out.println(item);
	}
}
// === end ===

// === case: same_line_comment_after_declaration ===
class InputAnnotationSameLineCommentAfterDeclarationSliceViolation {
	void m(
			@A @B String param // note
	) {}
}
// === end ===

// === case: same_line_comment_before_first_annotation ===
class InputAnnotationSameLineCommentBeforeFirstAnnotationSliceViolation {
	void m(
			/* c */ @A @B String param
	) {}
}
// === end ===

// === case: same_line_comment_between_annotation_lines ===
class InputAnnotationSameLineCommentBetweenAnnotationLinesSliceViolation {
	void m(
			// why
			@A @B String param
	) {}
}
// === end ===

// === case: same_line_comment_between_last_annotation_and_decl ===
class InputAnnotationSameLineCommentBetweenLastAnnotationAndDeclSliceViolation {
	void m(
			// why
			@A String param
	) {}
}
// === end ===

// === case: same_line_comment_inside_argument_list ===
// skip-reason: cannot join the annotations onto the declaration line: one of them cannot be written on a single line
class InputAnnotationSameLineCommentInsideArgumentListSliceViolation {
	void m(
			@Num(
			/* c */ 1)
			@A String param
	) {}
}
// === end ===

// === case: same_line_hoists_above_open_block_comment ===
class InputAnnotationSameLineHoistsAboveOpenBlockCommentSliceViolation {
	/* c */
	void m(/* outer
			*/ @A int b
	) {}
}
// === end ===

// === case: same_line_hoists_above_open_block_comment_spanning ===
class InputAnnotationSameLineHoistsAboveOpenBlockCommentSpanningSliceViolation {
	/* c */
	void m(/* outer
			still open
			*/ @A int b
	) {}
}
// === end ===

// === case: same_line_line_budget_just_over ===
class InputAnnotationSameLineLineBudgetJustOverSliceViolation {
	void m(
			@A @Val("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx") String param
	) {}
}
// === end ===

// === case: same_line_line_budget_just_under ===
class InputAnnotationSameLineLineBudgetJustUnderSliceViolation {
	void m(
			@A @Val("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx") String param
	) {}
}
// === end ===

// === case: same_line_multiline_args_after_sibling ===
class InputAnnotationSameLineMultilineArgsAfterSiblingSliceViolation {
	void m(
			@A @C @Num(1) String param
	) {}
}
// === end ===

// === case: same_line_multiline_args_string_paren ===
class InputAnnotationSameLineMultilineArgsStringParenSliceViolation {
	void m(
			@A @C @Val("(" + ")") String param
	) {}
}
// === end ===

// === case: same_line_split_qualified_name ===
class InputAnnotationSameLineSplitQualifiedNameSliceViolation {
	void m(
			@java.lang.Deprecated String param
	) {}
}
// === end ===

// === case: same_line_text_block_argument ===
// skip-reason: cannot join the annotations onto the declaration line: one of them cannot be written on a single line
class InputAnnotationSameLineTextBlockArgumentSliceViolation {
	void m(
			@A
			@Val("""
					value""")
			String param
	) {}
}
// === end ===

// === case: same_line_trailing_line_comment ===
class InputAnnotationSameLineTrailingLineCommentSliceViolation {
	void m(
			// why
			@A @B String param
	) {}
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationSameLineSortsAlphabeticallySliceViolation {
	void m(
			@A @B @C String param
	) {}
}
// === end ===

// === case: two_annotations_on_same_line ===
class InputAnnotationSameLineTwoAnnotationsOnSameLineSliceViolation {
	void m(
			@A @B String param
	) {}
}
// === end ===