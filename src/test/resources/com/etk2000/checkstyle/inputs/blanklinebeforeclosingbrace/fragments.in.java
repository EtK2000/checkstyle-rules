// === case: delete_multiple_blanks_before_close_brace ===
// target: col=0
	int x;


}
// === end ===

// === case: delete_single_blank_before_close_brace ===
// target: col=0
	int x;

}
// === end ===

// === case: delete_when_line_index_is_blank ===
// target: line=2 col=0
	int x;


}
// === end ===

// === case: delete_when_line_index_is_blank_triple ===
// target: line=2 col=0
	int x;



}
// === end ===

// === case: delete_when_line_index_is_blank_triple_from_last ===
// target: line=3 col=0
content



}
// === end ===

// === case: delete_when_line_index_is_whitespace_only ===
// target: line=1 col=0
content
	
}
// === end ===

// === case: delete_when_line_index_reaches_zero ===
// target: line=1 col=0


}
// === end ===

// === case: delete_whitespace_only_before_close_brace ===
// target: col=0
	int x;
	
}
// === end ===

// === case: last_line ===
// target: col=0
	int x;
// === end ===

// === case: next_line_not_blank ===
// target: col=0
	int x;
	int y;
// === end ===