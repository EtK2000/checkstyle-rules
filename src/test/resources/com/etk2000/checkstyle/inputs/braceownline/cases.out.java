package com.etk2000.checkstyle.inputs.braceownline;

// === case: annotation_type ===
class InputBraceOwnLineAnnotationTypeSliceViolation {
	@interface A {
		int value();
	}
}
// === end ===

// === case: anonymous_class ===
class InputBraceOwnLineAnonymousClassSliceViolation {
	void m() {
		final Object o = new Object() {
			@Override
			public String toString() {
				return "";
			}
		};
		System.out.println(o);
	}
}
// === end ===

// === case: anonymous_class_call_argument ===
class InputBraceOwnLineAnonymousClassCallArgumentSliceViolation {
	void m() {
		System.out.println(new Object() {
			@Override
			public String toString() {
				return "";
			}
		});
	}
}
// === end ===

// === case: anonymous_class_ctor_args ===
class InputBraceOwnLineAnonymousClassCtorArgsSliceViolation {
	void m() {
		final Thread t = new Thread("t") {
			@Override
			public void run() {
				System.out.println(getName());
			}
		};
		t.start();
	}
}
// === end ===

// === case: anonymous_class_empty ===
class InputBraceOwnLineAnonymousClassEmptySliceViolation {
	void m() {
		final var o = new Object() {};
		System.out.println(o);
	}
}
// === end ===

// === case: anonymous_class_generic_double_brace ===
class InputBraceOwnLineAnonymousClassGenericDoubleBraceSliceViolation {
	void m() {
		final var local = new ThreadLocal<String>() {
			{
				set("a");
			}
		};
		System.out.println(local);
	}
}
// === end ===

// === case: between_comment_and_brace_line_comment ===
class InputBraceOwnLineBetweenCommentAndBraceLineCommentSliceViolation {
	void m(int x) {
		if (x > 0) {
			// m
			// b
			--x;
			++x;
		}
	}
}
// === end ===

// === case: between_comment_and_brace_line_opens_block_comment ===
class InputBraceOwnLineBetweenCommentAndBraceLineOpensBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* m */
			/* a
			b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: between_comment_and_one_line_block ===
class InputBraceOwnLineBetweenCommentAndOneLineBlockSliceViolation {
	void m(int x) {
		if (x > 0) {
			// m
			--x; ++x;
		}
	}
}
// === end ===

// === case: between_comment_continuation_not_indented ===
class InputBraceOwnLineBetweenCommentContinuationNotIndentedSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* a
b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: between_line_comment_and_brace_line_opens_block_comment ===
class InputBraceOwnLineBetweenLineCommentAndBraceLineOpensBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) {
			// m
			/* a
			b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: blank_line_between ===
class InputBraceOwnLineBlankLineBetweenSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: block_comment_line_between ===
class InputBraceOwnLineBlockCommentLineBetweenSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* why */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: both_line_comments ===
class InputBraceOwnLineBothLineCommentsSliceViolation {
	void m(int x) {
		if (x > 0) { // a
			// b
			--x;
			++x;
		}
	}
}
// === end ===

// === case: brace_line_block_comment ===
class InputBraceOwnLineBraceLineBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) { /* why */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: brace_line_comment ===
class InputBraceOwnLineBraceLineCommentSliceViolation {
	void m(int x) {
		if (x > 0) { // why
			--x;
			++x;
		}
	}
}
// === end ===

// === case: catch_annotated_param ===
class InputBraceOwnLineCatchAnnotatedParamSliceViolation {
	void m() {
		try {
			System.out.println("a");
		}
		catch (@SuppressWarnings("unused") RuntimeException e) {
			System.out.println("b");
		}
	}
}
// === end ===

// === case: catch_single_statement ===
class InputBraceOwnLineCatchSingleStatementSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: class_extends_implements_multiline ===
class InputBraceOwnLineClassExtendsImplementsMultilineSliceViolation {
	abstract static class A
			extends Object
			implements Runnable {
		int a;
	}
}
// === end ===

// === case: comment_between_and_statement_on_brace_line ===
class InputBraceOwnLineCommentBetweenAndStatementOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) {
			// m
			--x;
			++x;
		}
	}
}
// === end ===

// === case: comment_line_between ===
class InputBraceOwnLineCommentLineBetweenSliceViolation {
	void m(int x) {
		if (x > 0) {
			// why
			--x;
			++x;
		}
	}
}
// === end ===

// === case: compact_constructor ===
class InputBraceOwnLineCompactConstructorSliceViolation {
	record R(int a) {
		R {
			if (a < 0)
				throw new IllegalArgumentException();
		}
	}
}
// === end ===

// === case: constructor ===
class InputBraceOwnLineConstructorSliceViolation {
	private int a;

	InputBraceOwnLineConstructorSliceViolation(int a) {
		this.a = a;
	}
}
// === end ===

// === case: cuddled_else_multi_statement ===
class InputBraceOwnLineCuddledElseMultiStatementSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		} else {
			++x;
			--x;
		}
	}
}
// === end ===

// === case: declaration_body ===
class InputBraceOwnLineDeclarationBodySliceViolation {
	void m(int x) {
		if (x > 0) {
			final var ownLineBrace = x + 1;
		}
	}
}
// === end ===

// === case: do_cuddled_while ===
class InputBraceOwnLineDoCuddledWhileSliceViolation {
	void m(int x, int y) {
		do {
			--x;
			++y;
		} while (x > 0);
	}
}
// === end ===

// === case: do_multi_statement ===
class InputBraceOwnLineDoMultiStatementSliceViolation {
	void m(int x, int y) {
		do {
			--x;
			++y;
		}
		while (x > 0);
	}
}
// === end ===

// === case: double_nested_class ===
class InputBraceOwnLineDoubleNestedClassSliceViolation {
	static class Outer {
		static class Inner {
			int a;
		}
	}
}
// === end ===

// === case: else_if_multi_statement ===
class InputBraceOwnLineElseIfMultiStatementSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
		else if (x < 0) {
			++x;
			--x;
		}
	}
}
// === end ===

// === case: else_multi_statement ===
class InputBraceOwnLineElseMultiStatementSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
		else {
			++x;
			--x;
		}
	}
}
// === end ===

// === case: enum_decl ===
class InputBraceOwnLineEnumDeclSliceViolation {
	enum E {
		A,
		B
	}
}
// === end ===

// === case: enum_implements ===
class InputBraceOwnLineEnumImplementsSliceViolation {
	enum E implements Runnable {
		A;

		@Override
		public void run() {
			System.out.println(this);
		}
	}
}
// === end ===

// === case: fallback_block_comment_after_brace ===
class InputBraceOwnLineFallbackBlockCommentAfterBraceSliceViolation {
	void m(int x) {
		if (x > 0) { /* important */
			--x;
		}
	}
}
// === end ===

// === case: fallback_close_brace_after_text_block ===
class InputBraceOwnLineFallbackCloseBraceAfterTextBlockSliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) {
			s = """
} """; }
		System.out.println(s);
	}
}
// === end ===

// === case: fallback_comment_before_brace_on_brace_line ===
class InputBraceOwnLineFallbackCommentBeforeBraceOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* note */
			--x;
		}
	}
}
// === end ===

// === case: fallback_comment_before_close_brace ===
class InputBraceOwnLineFallbackCommentBeforeCloseBraceSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
		/* trailing note */ }
	}
}
// === end ===

// === case: fallback_cuddled_else_statement_on_brace_line ===
class InputBraceOwnLineFallbackCuddledElseStatementOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		} else { ++x;
		}
	}
}
// === end ===

// === case: fallback_do_body_on_brace_line ===
class InputBraceOwnLineFallbackDoBodyOnBraceLineSliceViolation {
	void m(int x) {
		do { --x;
		} while (x > 0);
	}
}
// === end ===

// === case: fallback_do_comment_before_brace_on_brace_line ===
class InputBraceOwnLineFallbackDoCommentBeforeBraceOnBraceLineSliceViolation {
	void m(int x) {
		do {
			/* note */
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: fallback_do_comment_on_do_and_body ===
class InputBraceOwnLineFallbackDoCommentOnDoAndBodySliceViolation {
	void m(int x) {
		do { // note
			--x; // pending
		}
		while (x > 0);
	}
}
// === end ===

// === case: fallback_do_comment_on_do_and_brace ===
class InputBraceOwnLineFallbackDoCommentOnDoAndBraceSliceViolation {
	void m(int x) {
		do { // note
			// brace note
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: fallback_else_rebind ===
class InputBraceOwnLineFallbackElseRebindSliceViolation {
	boolean advance() {
		return true;
	}

	void m(int x, boolean a) {
		if (a) {
			if (x > 0 && advance());
		}
		else
			System.out.println(x);
	}
}
// === end ===

// === case: fallback_else_statement_on_brace_line ===
class InputBraceOwnLineFallbackElseStatementOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
		else { ++x;
		}
	}
}
// === end ===

// === case: fallback_for_statement_on_brace_line ===
class InputBraceOwnLineFallbackForStatementOnBraceLineSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) { System.out.println(i);
		}
	}
}
// === end ===

// === case: fallback_line_comment_after_brace ===
class InputBraceOwnLineFallbackLineCommentAfterBraceSliceViolation {
	void m(int x) {
		if (x > 0) { // note
			--x;
		}
	}
}
// === end ===

// === case: fallback_multiline_header ===
class InputBraceOwnLineFallbackMultilineHeaderSliceViolation {
	void m(boolean a, boolean b, int x) {
		if (a
				&& b) {
			--x;
		}
	}
}
// === end ===

// === case: fallback_statement_on_brace_line ===
class InputBraceOwnLineFallbackStatementOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) { --x;
		}
	}
}
// === end ===

// === case: fallback_while_statement_on_brace_line ===
class InputBraceOwnLineFallbackWhileStatementOnBraceLineSliceViolation {
	void m(int x) {
		while (x > 0) { --x;
		}
	}
}
// === end ===

// === case: finally_single_statement ===
class InputBraceOwnLineFinallySingleStatementSliceViolation {
	void m() {
		try {
			System.out.println("a");
		}
		finally {
			System.out.println("done");
		}
	}
}
// === end ===

// === case: for_each_multi_statement ===
// imports: java.util.List
class InputBraceOwnLineForEachMultiStatementSliceViolation {
	void m(List<String> list) {
		for (var item : list) {
			System.out.println(item);
			System.out.println(list);
		}
	}
}
// === end ===

// === case: for_multi_statement ===
class InputBraceOwnLineForMultiStatementSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) {
			System.out.println(i);
			System.out.println(x);
		}
	}
}
// === end ===

// === case: generic_method ===
// imports: java.util.List
class InputBraceOwnLineGenericMethodSliceViolation {
	<T> T first(List<T> list) {
		return list.getFirst();
	}
}
// === end ===

// === case: gnu_indent ===
class InputBraceOwnLineGnuIndentSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: gnu_indent_blank_line_in_body ===
class InputBraceOwnLineGnuIndentBlankLineInBodySliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;

			++x;
		}
	}
}
// === end ===

// === case: gnu_indent_line_without_brace_indent ===
class InputBraceOwnLineGnuIndentLineWithoutBraceIndentSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
// note
			++x;
		}
	}
}
// === end ===

// === case: gnu_indent_one_line_block ===
class InputBraceOwnLineGnuIndentOneLineBlockSliceViolation {
	void m(int x) {
		if (x > 0) { --x; ++x; }
	}
}
// === end ===

// === case: gnu_indent_text_block_body ===
class InputBraceOwnLineGnuIndentTextBlockBodySliceViolation {
	void m(int x) {
		if (x > 0) {
			final var s = """
					text
					""";
			System.out.println(s + x);
		}
	}
}
// === end ===

// === case: gnu_indent_text_block_content_unshiftable ===
// skip-reason: cannot re-indent the block: a text block's value would change
class InputBraceOwnLineGnuIndentTextBlockContentUnshiftableSliceViolation {
	void m(int x) {
		if (x > 0)
			{
				final var s = """
text
						""";
				System.out.println(s + x);
			}
	}
}
// === end ===

// === case: header_at_limit ===
class InputBraceOwnLineHeaderAtLimitSliceViolation {
	void m(int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, int x) {
		if (aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa + x > bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_between_and_brace_comments ===
class InputBraceOwnLineHeaderBetweenAndBraceCommentsSliceViolation {
	void m(int x) {
		if (x > 0) { // a
			// m
			// b
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_block_comment_ends_on_brace_line ===
class InputBraceOwnLineHeaderBlockCommentEndsOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) { /* a
			b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_char_brace ===
class InputBraceOwnLineHeaderCharBraceSliceViolation {
	void m(char c, int x) {
		if (c == '{') {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_comment_and_empty_braces ===
class InputBraceOwnLineHeaderCommentAndEmptyBracesSliceViolation {
	void noop() { // a
	}
}
// === end ===

// === case: header_comment_and_one_line_block ===
class InputBraceOwnLineHeaderCommentAndOneLineBlockSliceViolation {
	void m(int x) {
		if (x > 0) { // a
			--x; ++x;
		}
	}
}
// === end ===

// === case: header_comment_and_statement_on_brace_line ===
class InputBraceOwnLineHeaderCommentAndStatementOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) { // a
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_ends_in_text_block ===
class InputBraceOwnLineHeaderEndsInTextBlockSliceViolation {
	void m(String s, int x) {
		if (x > 0
				&& s.equals("""
				text
				""")) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_multiline_block_comment_opens ===
class InputBraceOwnLineHeaderMultilineBlockCommentOpensSliceViolation {
	void m(int x) {
		if (x > 0) { /* a
			b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_over_limit ===
class InputBraceOwnLineHeaderOverLimitSliceViolation {
	void m(int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, int x) {
		if (aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa + x > bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_string_with_brace ===
class InputBraceOwnLineHeaderStringWithBraceSliceViolation {
	void m(String s, int x) {
		if (s.equals("else {")) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_trailing_block_comment ===
class InputBraceOwnLineHeaderTrailingBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) { /* why */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: header_trailing_line_comment ===
class InputBraceOwnLineHeaderTrailingLineCommentSliceViolation {
	void m(int x) {
		if (x > 0) { // why
			--x;
			++x;
		}
	}
}
// === end ===

// === case: if_comment_only_block ===
class InputBraceOwnLineIfCommentOnlyBlockSliceViolation {
	void m(int x) {
		if (x > 0) {
			// todo
		}
	}
}
// === end ===

// === case: if_empty_block ===
class InputBraceOwnLineIfEmptyBlockSliceViolation {
	void m(int x) {
		if (x > 0) {
		}
	}
}
// === end ===

// === case: if_multi_statement ===
class InputBraceOwnLineIfMultiStatementSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: infinite_for ===
class InputBraceOwnLineInfiniteForSliceViolation {
	void m(int x) {
		for (;;) {
			--x;
			if (x < 0)
				break;
		}
	}
}
// === end ===

// === case: inner_class ===
class InputBraceOwnLineInnerClassSliceViolation {
	class Inner {
		int a;
	}
}
// === end ===

// === case: interface_decl ===
class InputBraceOwnLineInterfaceDeclSliceViolation {
	interface I {
		void run();
	}
}
// === end ===

// === case: interface_default_method ===
class InputBraceOwnLineInterfaceDefaultMethodSliceViolation {
	interface I {
		default void d() {
			System.out.println("d");
		}
	}
}
// === end ===

// === case: interface_private_method ===
class InputBraceOwnLineInterfacePrivateMethodSliceViolation {
	interface I {
		private void p() {
			System.out.println("p");
		}
	}
}
// === end ===

// === case: javadoc_between_header_and_brace ===
class InputBraceOwnLineJavadocBetweenHeaderAndBraceSliceViolation {
	void m() {
		/** why */
		System.out.println("m");
	}
}
// === end ===

// === case: labeled_bare_block ===
class InputBraceOwnLineLabeledBareBlockSliceViolation {
	void m(int x) {
		check: {
			if (x > 0)
				break check;
			--x;
		}
	}
}
// === end ===

// === case: labeled_for ===
class InputBraceOwnLineLabeledForSliceViolation {
	void m(int x) {
		outer:
		for (var i = 0; i < x; ++i) {
			System.out.println(i);
			break outer;
		}
	}
}
// === end ===

// === case: labeled_for_same_line ===
class InputBraceOwnLineLabeledForSameLineSliceViolation {
	void m(int x) {
		outer: for (var i = 0; i < x; ++i) {
			System.out.println(i);
			break outer;
		}
	}
}
// === end ===

// === case: lambda_block ===
class InputBraceOwnLineLambdaBlockSliceViolation {
	void m() {
		final Runnable r = () -> {
			System.out.println("a");
			System.out.println("b");
		};
		r.run();
	}
}
// === end ===

// === case: lambda_multi_params ===
// imports: java.util.Map
class InputBraceOwnLineLambdaMultiParamsSliceViolation {
	void m(Map<String, String> map) {
		map.forEach((k, v) -> {
			System.out.println(k);
			System.out.println(v);
		});
	}
}
// === end ===

// === case: lambda_nested ===
// imports: java.util.function.Function
class InputBraceOwnLineLambdaNestedSliceViolation {
	void m() {
		final Function<Integer, Function<Integer, Integer>> f = x -> {
			System.out.println(x);
			return y -> {
				System.out.println(y);
				return x + y;
			};
		};
		System.out.println(f);
	}
}
// === end ===

// === case: lambda_single_param ===
// imports: java.util.List
class InputBraceOwnLineLambdaSingleParamSliceViolation {
	void m(List<String> list, int x) {
		list.forEach(item -> {
			System.out.println(item);
			System.out.println(x);
		});
	}
}
// === end ===

// === case: lambda_var_params ===
// imports: java.util.Map
class InputBraceOwnLineLambdaVarParamsSliceViolation {
	void m(Map<String, String> map) {
		map.forEach((var k, var v) -> {
			System.out.println(k);
			System.out.println(v);
		});
	}
}
// === end ===

// === case: local_class ===
class InputBraceOwnLineLocalClassSliceViolation {
	void m() {
		class Local {
			int a;
		}

		System.out.println(new Local().a);
	}
}
// === end ===

// === case: local_class_annotated ===
class InputBraceOwnLineLocalClassAnnotatedSliceViolation {
	void m() {
		@SuppressWarnings("unused")
		class Local {
			int a;
		}

		System.out.println(new Local().a);
	}
}
// === end ===

// === case: local_enum ===
class InputBraceOwnLineLocalEnumSliceViolation {
	void m() {
		enum Local {
			A
		}

		System.out.println(Local.A);
	}
}
// === end ===

// === case: method ===
class InputBraceOwnLineMethodSliceViolation {
	void allman(int x) {
		--x;
	}
}
// === end ===

// === case: method_annotated ===
class InputBraceOwnLineMethodAnnotatedSliceViolation {
	@Override
	public String toString() {
		return "";
	}
}
// === end ===

// === case: method_empty_body ===
class InputBraceOwnLineMethodEmptyBodySliceViolation {
	void noop() {
	}
}
// === end ===

// === case: method_empty_braces ===
class InputBraceOwnLineMethodEmptyBracesSliceViolation {
	void noop() {}
}
// === end ===

// === case: method_multiline_params ===
class InputBraceOwnLineMethodMultilineParamsSliceViolation {
	void m(
			int a,
			int b
	) {
		System.out.println(a + b);
	}
}
// === end ===

// === case: method_throws_next_line ===
// imports: java.io.IOException
class InputBraceOwnLineMethodThrowsNextLineSliceViolation {
	void read()
			throws IOException {
		throw new IOException();
	}
}
// === end ===

// === case: method_with_allman_if ===
class InputBraceOwnLineMethodWithAllmanIfSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: multi_catch ===
class InputBraceOwnLineMultiCatchSliceViolation {
	void m() {
		try {
			System.out.println("a");
		}
		catch (IllegalStateException | IllegalArgumentException e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: multiline_block_comment_between ===
class InputBraceOwnLineMultilineBlockCommentBetweenSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* a
				b */
			--x;
			++x;
		}
	}
}
// === end ===

// === case: multiline_header ===
class InputBraceOwnLineMultilineHeaderSliceViolation {
	void m(boolean a, boolean b, int x) {
		if (a
				&& b) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: multiline_header_trailing_comment ===
class InputBraceOwnLineMultilineHeaderTrailingCommentSliceViolation {
	void m(boolean a, boolean b, int x) {
		if (a
				&& b) { // why
			--x;
			++x;
		}
	}
}
// === end ===

// === case: nested_class ===
class InputBraceOwnLineNestedClassSliceViolation {
	static class Inner {
		int a;
	}
}
// === end ===

// === case: nested_try ===
class InputBraceOwnLineNestedTrySliceViolation {
	void m() {
		try {
			try {
				Thread.sleep(1);
			}
			finally {
				System.out.println("inner");
			}
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: nested_two_sites ===
class InputBraceOwnLineNestedTwoSitesSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) {
			if (i > 1) {
				System.out.println(i);
				System.out.println(x);
			}
		}
	}
}
// === end ===

// === case: one_line_block ===
class InputBraceOwnLineOneLineBlockSliceViolation {
	void m(int x) {
		if (x > 0) { --x; ++x; }
	}
}
// === end ===

// === case: one_line_block_single_statement ===
class InputBraceOwnLineOneLineBlockSingleStatementSliceViolation {
	void m(int x) {
		if (x > 0) { --x; }
	}
}
// === end ===

// === case: receiver_parameter ===
class InputBraceOwnLineReceiverParameterSliceViolation {
	void m(InputBraceOwnLineReceiverParameterSliceViolation this) {
		System.out.println(this);
	}
}
// === end ===

// === case: record_method ===
class InputBraceOwnLineRecordMethodSliceViolation {
	record R(int a) {
		int twice() {
			return a * 2;
		}
	}
}
// === end ===

// === case: sealed_class_permits_multiline ===
class InputBraceOwnLineSealedClassPermitsMultilineSliceViolation {
	abstract static sealed class S
			permits P {
		int a;
	}

	static final class P extends S {}
}
// === end ===

// === case: shallower_indent ===
class InputBraceOwnLineShallowerIndentSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: single_statement_spanning_lines ===
class InputBraceOwnLineSingleStatementSpanningLinesSliceViolation {
	void m(int[] arr) {
		if (arr != null) {
			arr = new int[] {
			};
		}
	}
}
// === end ===

// === case: statement_on_brace_line_multi ===
class InputBraceOwnLineStatementOnBraceLineMultiSliceViolation {
	void m(int x) {
		if (x > 0) { --x;
			++x;
		}
	}
}
// === end ===

// === case: static_initializer ===
class InputBraceOwnLineStaticInitializerSliceViolation {
	static int count;

	static {
		count = 1;
	}
}
// === end ===

// === case: switch_empty ===
class InputBraceOwnLineSwitchEmptySliceViolation {
	void m(int x) {
		switch (x) {
		}
	}
}
// === end ===

// === case: switch_expression ===
class InputBraceOwnLineSwitchExpressionSliceViolation {
	void m(int x) {
		final var y = switch (x) {
			case 1 -> 2;
			default -> 3;
		};
		System.out.println(y);
	}
}
// === end ===

// === case: switch_rule_block ===
class InputBraceOwnLineSwitchRuleBlockSliceViolation {
	void m(int x, int y) {
		switch (x) {
			case 1 -> {
				--x;
				++y;
			}
			default -> ++x;
		}
	}
}
// === end ===

// === case: switch_rule_block_default ===
class InputBraceOwnLineSwitchRuleBlockDefaultSliceViolation {
	void m(int x, int y) {
		switch (x) {
			case 1 -> ++x;
			default -> {
				--x;
				++y;
			}
		}
	}
}
// === end ===

// === case: switch_statement_arrow ===
class InputBraceOwnLineSwitchStatementArrowSliceViolation {
	void m(int x) {
		switch (x) {
			case 1 -> --x;
			default -> ++x;
		}
	}
}
// === end ===

// === case: switch_statement_colon ===
class InputBraceOwnLineSwitchStatementColonSliceViolation {
	void m(int x, int y) {
		switch (x) {
			case 1:
				--x;
			case 2:
				++y;
				break;

			default:
				++x;
		}
	}
}
// === end ===

// === case: synchronized_method ===
class InputBraceOwnLineSynchronizedMethodSliceViolation {
	synchronized void m() {
		System.out.println("m");
	}
}
// === end ===

// === case: synchronized_single_statement ===
class InputBraceOwnLineSynchronizedSingleStatementSliceViolation {
	void m(int x) {
		synchronized (this) {
			--x;
		}
	}
}
// === end ===

// === case: top_level_class ===
class InputBraceOwnLineTopLevelClassSliceViolation {
	int a;
}
// === end ===

// === case: try_catch_finally ===
class InputBraceOwnLineTryCatchFinallySliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
		finally {
			System.out.println("done");
		}
	}
}
// === end ===

// === case: try_single_statement ===
class InputBraceOwnLineTrySingleStatementSliceViolation {
	void m() {
		try {
			Thread.sleep(1);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}
// === end ===

// === case: try_with_existing_resource ===
// imports: java.io.IOException
// imports: java.io.InputStream
class InputBraceOwnLineTryWithExistingResourceSliceViolation {
	void m(InputStream in) throws IOException {
		try (in) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources ===
// imports: java.io.IOException
// imports: java.io.InputStream
class InputBraceOwnLineTryWithResourcesSliceViolation {
	void m() throws IOException {
		try (var in = open()) {
			System.out.println(in.read());
		}
	}

	InputStream open() {
		return InputStream.nullInputStream();
	}
}
// === end ===

// === case: try_with_resources_multiline_header ===
// imports: java.io.IOException
// imports: java.io.InputStream
class InputBraceOwnLineTryWithResourcesMultilineHeaderSliceViolation {
	void m() throws IOException {
		try (var a = open();
				var b = open()) {
			System.out.println(a.read() + b.read());
		}
	}

	InputStream open() {
		return InputStream.nullInputStream();
	}
}
// === end ===

// === case: while_empty_block ===
class InputBraceOwnLineWhileEmptyBlockSliceViolation {
	void m(int x) {
		while (x > 0) {
		}
	}
}
// === end ===

// === case: while_multi_statement ===
class InputBraceOwnLineWhileMultiStatementSliceViolation {
	void m(int x) {
		while (x > 0) {
			--x;
			System.out.println(x);
		}
	}
}
// === end ===