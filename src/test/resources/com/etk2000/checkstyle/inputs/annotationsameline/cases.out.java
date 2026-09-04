package com.etk2000.checkstyle.inputs.annotationsameline;

@interface A {}
@interface B {}
@interface C {}

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