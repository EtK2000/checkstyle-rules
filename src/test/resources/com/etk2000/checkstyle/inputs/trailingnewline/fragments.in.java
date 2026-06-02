// === case: all_blank_lines ===
// target: line=1 col=0


// === end ===

// === case: delete_multiple_trailing_empty_lines ===
// target: line=2 col=0
class T {}


// === end ===

// === case: delete_single_trailing_empty_line ===
// target: line=1 col=0
class T {}

// === end ===

// === case: delete_trailing_whitespace_only_line ===
// target: line=1 col=0
class T {}
	
// === end ===

// === case: last_line_has_content ===
// target: col=0
class T {}
// === end ===