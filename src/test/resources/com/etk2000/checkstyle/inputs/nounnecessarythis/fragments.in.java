// === case: stale_column_after_sibling_edit ===
// target: line=4 col=11
class S {
	String name;

	String m() {
		return name + this.name;
	}
}
// === end ===

// === case: unparseable_buffer ===
// target: col=7
this.x;
// === end ===