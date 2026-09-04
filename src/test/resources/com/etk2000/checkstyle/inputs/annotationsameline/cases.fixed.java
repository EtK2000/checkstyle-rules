// === case: keeps_non_annotation_modifier ===
class InputAnnotationSameLineKeepsNonAnnotationModifierSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (@A @B RuntimeException e) {
			e.printStackTrace();
		}
	}
}
// === end ===

// === case: order_qualified_name ===
class InputAnnotationSameLineOrderQualifiedNameSliceViolation {
	void m(@A @Deprecated String param) {}
}
// === end ===

// === case: same_line_split_qualified_name ===
class InputAnnotationSameLineSplitQualifiedNameSliceViolation {
	void m(
			@Deprecated String param
	) {}
}
// === end ===