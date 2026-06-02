// === case: block_unterminated_annotation_skips ===
// target: line=1 col=0
	@B
	@A(
	void f() {}
// === end ===

// === case: embedded_unterminated_annotation_skips ===
// target: col=0
	final @B(
// === end ===

// === case: javadoc_continuation_at_violation_with_blank_below ===
// target: col=0
	 * javadoc continuation

	void f() {}
// === end ===
