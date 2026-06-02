package com.etk2000.checkstyle.inputs.noarraytrailingcomma;

// === case: comma_at_start_of_line ===
class InputArrayCommaStartOfLineSliceViolation {
	int[] a = {1
,}; // violation: Remove trailing comma from array initializer.
}
// === end ===

// === case: multiple_elements_inline ===
class InputArrayCommaMultipleElementsInlineSliceViolation {
	int[] a = {1, 2, 3,}; // violation: Remove trailing comma from array initializer.
}
// === end ===

// === case: nested_array ===
class InputArrayCommaNestedSliceViolation {
	int[][] c = {{1, 2,}, {3, 4}}; // violation: Remove trailing comma from array initializer.
}
// === end ===

// === case: new_array_syntax ===
class InputArrayCommaNewArraySyntaxSliceViolation {
	int[] b = new int[]{4, 5,}; // violation: Remove trailing comma from array initializer.
}
// === end ===

// === case: remove_trailing_comma_inline ===
class InputArrayCommaRemoveInlineSliceViolation {
	int[] a = {1, 2,}; // violation: Remove trailing comma from array initializer.
}
// === end ===

// === case: remove_trailing_comma_multiline ===
class InputArrayCommaMultilineSliceViolation {
	int[] a = {
		1,
		2, // violation: Remove trailing comma from array initializer.
	};
}
// === end ===

// === case: remove_trailing_comma_with_space_before ===
class InputArrayCommaSpaceBeforeSliceViolation {
	int[] a = {
		1 , // violation: Remove trailing comma from array initializer.
	};
}
// === end ===

// === case: single_element_array ===
class InputArrayCommaSingleElementSliceViolation {
	int[] a = {1,}; // violation: Remove trailing comma from array initializer.
}
// === end ===