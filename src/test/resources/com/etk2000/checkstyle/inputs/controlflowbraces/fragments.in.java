// === case: cuddled_else_unparseable_buffer_refused ===
// target: col=1
	} else x = 5 6;
// === end ===

// === case: for_keyword_unparseable_buffer_refused ===
// target: col=1
	for (var item : list) x = 5 6;
// === end ===

// === case: keyword_prefixed_identifiers_refused ===
// target: col=1
	elseIf = forEach + whileLoop + else𝐀 + for𝐀 + while𝐀 + if𝐀 5 6;
// === end ===

// === case: no_control_flow_keyword_refused ===
// target: col=1
	x = 5 6;
// === end ===

// === case: non_do_while_stale_position_refused ===
// target: line=2 col=5
class T {
	void m(int x) {
		if (x > 0) {
			--x;
		}
	}
}
// === end ===

// === case: non_do_while_unparseable_buffer_refused ===
// target: col=1
	if (cond) x = 5 6;
// === end ===

// === case: unparseable_buffer_refused ===
// target: col=1
	do x = 5 6;
	while (x > 0);
// === end ===

// === case: while_keyword_unparseable_buffer_refused ===
// target: col=1
	while (cond) x = 5 6;
// === end ===
