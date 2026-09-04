package com.etk2000.checkstyle.inputs.braceownline;

// === case: fallback_line_comment_after_brace ===
class InputBraceOwnLineFallbackLineCommentAfterBraceSliceViolation {
	void m(int x) {
		if (x > 0) // note
			--x;
	}
}
// === end ===

// === case: if_empty_block ===
class InputBraceOwnLineIfEmptyBlockSliceViolation {
	void m(int x) {
	}
}
// === end ===

// === case: lambda_var_params ===
// imports: java.util.Map
class InputBraceOwnLineLambdaVarParamsSliceViolation {
	void m(Map<String, String> map) {
		map.forEach((k, v) -> {
			System.out.println(k);
			System.out.println(v);
		});
	}
}
// === end ===