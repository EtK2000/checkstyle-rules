// === case: collapse_blank_inside_block_comment ===
// target: line=1 col=0
class T {
	/* note


	*/
}
// === end ===

// === case: collapse_double_blank_to_single ===
// target: col=0
int x;


int y;
// === end ===

// === case: collapse_triple_blank_to_single ===
// target: col=0
int x;



int y;
// === end ===

// === case: last_line_no_blank_after ===
// target: col=0
int x;
// === end ===

// === case: next_line_not_blank ===
// target: col=0
int x;
int y;
// === end ===

// === case: single_blank_not_fixed ===
// target: col=0
int x;

int y;
// === end ===

// === case: skip_blank_inside_text_block ===
// target: line=2 col=0
class T {
	String json = """
		line1


		line2
		""";
}
// === end ===

// === case: whitespace_only_lines_collapsed ===
// target: col=0
int x;
	
   
int y;
// === end ===