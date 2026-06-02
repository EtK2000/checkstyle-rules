// === case: blank_line_with_spaces ===
// target: col=0
   
// === end ===

// === case: blank_line_with_tab ===
// target: col=0
	
// === end ===

// === case: no_trailing_whitespace ===
// target: col=0
int x = 5;
// === end ===

// === case: trailing_spaces ===
// target: col=0
	int x = 5;   
// === end ===

// === case: trailing_tab ===
// target: col=0
	int x = 5;	
// === end ===