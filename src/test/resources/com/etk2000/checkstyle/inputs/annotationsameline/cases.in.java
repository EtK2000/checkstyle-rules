package com.etk2000.checkstyle.inputs.annotationsameline;

@interface A {}
@interface B {}
@interface C {}

// === case: annotation_with_params ===
class InputAnnotationSameLineAnnotationWithParamsSliceViolation {
	void m(
			@SuppressWarnings("unchecked") // violation: Annotation 'SuppressWarnings' must be on the same line as the declaration.
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
				@A // violation: Annotation 'A' must be on the same line as the declaration.
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
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineForEachSliceViolation {
	void m(List<String> list) {
		for (
				@A // violation: Annotation 'A' must be on the same line as the declaration.
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
				@A // violation: Annotation 'A' must be on the same line as the declaration.
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
				@A // violation: Annotation 'A' must be on the same line as the declaration.
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
				@A // violation: Annotation 'A' must be on the same line as the declaration.
				var s
		) -> {};
	}
}
// === end ===

// === case: contexts_method_param ===
class InputAnnotationSameLineMethodParamSliceViolation {
	void m(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: contexts_method_param_multi_annotation ===
class InputAnnotationSameLineMethodParamMultiSliceViolation {
	void m(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			@B
			String param
	) {}
}
// === end ===

// === case: contexts_method_param_out_of_order ===
class InputAnnotationSameLineMethodParamOutOfOrderSliceViolation {
	void m(
			@B // violation: Annotation 'B' must be on the same line as the declaration.
			@A
			String param
	) {}
}
// === end ===

// === case: contexts_record_component ===
class InputAnnotationSameLineRecordComponentSliceViolation {
	record Data(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			String name
	) {}
}
// === end ===

// === case: inline_reorder_three_annotations ===
class InputAnnotationSameLineInlineReorderThreeAnnotationsSliceViolation {
	void foo(@C @A @B String param) {} // violation: Annotation 'A' must appear before 'C' (alphabetical order).
}
// === end ===

// === case: inline_reorder_two_annotations ===
class InputAnnotationSameLineInlineReorderTwoAnnotationsSliceViolation {
	void foo(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: merge_single_annotation_line ===
class InputAnnotationSameLineMergeSliceViolation {
	void m(
			@Deprecated // violation: Annotation 'Deprecated' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===

// === case: merge_three_annotation_lines ===
class InputAnnotationSameLineMergeThreeAnnotationLinesSliceViolation {
	void m(
			@A // violation: Annotation 'A' must be on the same line as the declaration.
			@B
			@C
			String param
	) {}
}
// === end ===

// === case: preserves_declaration_indentation ===
class InputAnnotationSameLinePreservesDeclarationIndentationSliceViolation {
	static class Outer {
		static class Inner {
			void m(
					@A // violation: Annotation 'A' must be on the same line as the declaration.
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
		catch (@B @A Exception e) { // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: reorder_contexts_constructor_param ===
class InputAnnotationSameLineOrderConstructorParamSliceViolation {
	InputAnnotationSameLineOrderConstructorParamSliceViolation(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: reorder_contexts_for_each ===
// imports: java.util.List
class InputAnnotationSameLineOrderForEachSliceViolation {
	void m(List<String> list) {
		for (@B @A var item : list) // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			System.out.println(item);
	}
}
// === end ===

// === case: reorder_contexts_for_init ===
class InputAnnotationSameLineOrderForInitSliceViolation {
	void m() {
		for (@B @A var i = 0; i < 10; ++i) // violation: Annotation 'A' must appear before 'B' (alphabetical order).
			System.out.println(i);
	}
}
// === end ===

// === case: reorder_contexts_lambda_param ===
// imports: java.util.function.Consumer
class InputAnnotationSameLineOrderLambdaParamSliceViolation {
	void m() {
		final Consumer<String> c = (@B @A var s) -> {}; // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	}
}
// === end ===

// === case: reorder_contexts_method_param ===
class InputAnnotationSameLineOrderMethodParamSliceViolation {
	void m(@B @A String param) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: reorder_contexts_multi_param ===
class InputAnnotationSameLineOrderMultiParamSliceViolation {
	void m(
			@C @A String param1, // violation: Annotation 'A' must appear before 'C' (alphabetical order).
			@A @B String param2
	) {}
}
// === end ===

// === case: reorder_contexts_record_component ===
class InputAnnotationSameLineOrderRecordComponentSliceViolation {
	record Data(@B @A String name) {} // violation: Annotation 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationSameLineSortsAlphabeticallySliceViolation {
	void m(
			@C // violation: Annotation 'C' must be on the same line as the declaration.
			@A
			@B
			String param
	) {}
}
// === end ===

// === case: two_annotations_on_same_line ===
class InputAnnotationSameLineTwoAnnotationsOnSameLineSliceViolation {
	void m(
			@A @B // violation: Annotation 'A' must be on the same line as the declaration.
			String param
	) {}
}
// === end ===