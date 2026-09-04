// === case: embedded_annotation_with_deep_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithDeepNestedAnnotationViolation {
	void m() {
		@A(@B("test"))
		final var x = 1;
	}
}
// === end ===

// === case: order_split_qualified_name ===
class InputAnnotationOwnLineOrderSplitQualifiedNameViolation {
	@B
	@Deprecated
	void f() {}
}
// === end ===