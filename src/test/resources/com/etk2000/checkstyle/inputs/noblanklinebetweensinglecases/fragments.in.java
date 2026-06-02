// === case: blank_lines_with_whitespace ===
// target: line=2 col=0
			return 1;
	
		case B:
// === end ===

// === case: first_line ===
// target: col=0
		case A:
// === end ===

// === case: mixed_blank_and_whitespace_lines ===
// target: line=4 col=0
			return 1;

   
	
		case B:
// === end ===

// === case: no_blank_lines_above ===
// target: line=1 col=0
			return 1;
		case B:
// === end ===

// === case: remove_multiple_blank_lines ===
// target: line=4 col=0
			return 1;



		case B:
// === end ===