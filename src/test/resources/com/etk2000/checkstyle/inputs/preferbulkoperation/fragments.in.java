// === case: array_copy_ident_suffixed_rhs_rejected ===
// target: col=0
		for (var i = 0; i < arr.length; ++i)
			arr[i] = arr[i]extra;
// === end ===

// === case: array_copy_unmatched_close_bracket_rejected ===
// target: col=0
		for (var i = 0; i < a.length; ++i)
			x] = y[0];
// === end ===

// === case: array_fill_empty_value_returns_null ===
// target: col=0
		for (var i = 0; i < arr.length; ++i)
			arr[i] = ;
// === end ===

// === case: for_each_add_all_braceless_missing_close_paren_returns_null ===
// target: col=0
		for (var item : source
			target.add(item);
// === end ===

// === case: for_each_add_all_braceless_missing_target_returns_null ===
// target: col=0
		for (var item : source) .add(item);
// === end ===

// === case: for_each_lambda_bails_on_empty_block_body_before_put ===
// target: col=0
		source.forEach((k, v) -> {.put(k, v));
// === end ===

// === case: for_each_lambda_empty_source_returns_null ===
// target: col=0
		.forEach(target::put);
// === end ===

// === case: for_each_lambda_return_null_multi_line_unclosed ===
// target: col=0
		source.forEach((k, v) ->
			target.put(k, v)
// === end ===

// === case: for_each_lambda_source_ends_with_dot_returns_null ===
// target: col=0
		source..forEach((k, v) -> target.put(k, v));
// === end ===

// === case: for_each_lambda_source_starts_with_dot_returns_null ===
// target: col=0
		.something.forEach(target::put);
// === end ===

// === case: for_each_method_ref_empty_target_returns_null ===
// target: col=0
		source.forEach(::put);
// === end ===

// === case: for_each_method_ref_no_close_paren_returns_null ===
// target: col=0
		list.forEach(other::add
// === end ===

// === case: guard_braced_unclosed ===
// target: col=0
		for (var item : source) {
			target.add(item);
// === end ===

// === case: guard_braceless_no_semicolon ===
// target: col=0
		for (var item : source)
			target.add(item)
// === end ===

// === case: indexed_add_all_missing_target_returns_null ===
// target: col=0
		for (var i = 0; i < source.size(); ++i) .add(source.get(i));
// === end ===

// === case: put_all_entry_set_missing_target_returns_null ===
// target: col=0
		for (var entry : source.entrySet()) {
			.put(entry.getKey(), entry.getValue());
		}
// === end ===