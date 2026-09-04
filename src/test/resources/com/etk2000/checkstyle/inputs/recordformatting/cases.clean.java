package com.etk2000.checkstyle.inputs.recordformatting;

import java.util.List;
import java.util.Map;

interface Foo {}

interface Bar {}

class InputRecordFormattingClean {
	record EmptyA() {}

	record OneA(int a) {}

	record TwoA(int a, int b) {}

	record ThreeA(int a, int b, int c) {}

	record GenericA<T>(T a) {}

	record BoundedGenericA<T extends Number>(T a) {}

	record MultiTypeParamA<K, V>(K k, V v) {}

	record VarargsA(int a, int... rest) {}

	record GenericComponentA(List<String> list) {}

	record NestedGenericA(Map<String, List<Integer>> map) {}

	record WildcardA(List<? extends Number> nums) {}

	record ImplementsOneA(int a) implements Foo {}

	record ImplementsMultiA(int a) implements Foo, Bar {}

	record GenericImplementsA<T>(T a) implements Foo {}

	record WithBodyA(int a) {
		void m() {}
	}

	record WithStaticFieldA(int a) {
		static final int CONST = 5;
	}

	record TwoB(
			int a,
			int b
	) {}

	record ThreeB(
			int a,
			int b,
			int c
	) {}

	record GenericB<T>(
			T a,
			T b
	) {}

	record ImplementsOneB(
			int a,
			int b
	) implements Foo {}

	record ImplementsMultiLineClean(int a) implements
			Foo,
			Bar {}

	record WithBodyB(
			int a,
			int b
	) {
		void m() {}
	}

	record Outer(int a) {
		record Inner(int b) {}
	}

	// A comment between `)` and `{` belongs to the header, not the body, even though the parser
	// parks it inside OBJBLOCK ahead of the LCURLY. These three pin that: the body still reads as
	// empty, so `{}` on one line stays correct and the spacing rule still sees ' ' before the brace.
	record CommentBeforeBraceA(int a) /* c */ {}

	record CommentBeforeBraceGluedA(int a)/* c */ {}

	record CommentBeforeBraceTwoCommentsA(int a) /* c */ /* d */ {}

	// Same placement with a real member: the brace pair is not adjacent, so the body is non-empty
	// for the member's sake, and the parked comment must not be what decides it either way.
	record CommentBeforeBraceWithBodyA(int a) /* c */ {
		void m() {}
	}

	// A supplementary character before the `{` makes the code-point column disagree with the char
	// index. These are correctly spaced and must stay clean; the violation-side partners are the
	// fix_supplementary_comment_* slices.
	record SupplementaryCommentBeforeBraceA(int a) /* 𝐀 */ {}

	record TwoSupplementaryCommentsBeforeBraceA(int a) /* 𝐀𝐁 */ {}

	record SupplementaryComponentNameA(int a𝐀b) /* c */ {}

	// A comment between the braces is body content, so the body is non-empty and `}` belongs on its
	// own line. Every flavour, since only a block comment can ever share the line with the braces.
	record LineCommentBodyB(int a) { // note
	}

	record BlockCommentBodyB(int a) {
		/* note */
	}

	record JavadocBodyB(int a) {
		/** note */
	}

	record MultiLineCommentBodyB(int a) { /* note
	more */ }

	record NoComponentCommentBodyB() {
		// note
	}

	record OuterCommentHost(int a) {
		record InnerCommentBody(int b) {
			// note
		}
	}

	// Component placements where a comment attaches into a neighbouring component's subtree and
	// would drag its line number if AstQuery.firstLine did not skip comments.
	record LeadingParenCommentB( // leading
			int a,
			int b
	) {}

	record InterComponentTrailingCommentB(
			int a, // first
			int b
	) {}

	record OwnLineBetweenComponentsB(
			int a,
			// about b
			int b
	) {}

	record OwnLineBeforeRparenB(
			int a,
			int b
			// trailing note
	) {}

	record ClosingParenLineCommentB(
			int a,
			int b
	/* why */) {}

	record ImplementsCommentA(int a) implements /* c */ Foo {}

	record ImplementsCommentMultiB(int a) implements
			Foo,
			Bar /* c */ {}

	/** Attaches as RECORD_DEF's previous sibling, outside OBJBLOCK, so it cannot reach the body. */
	record JavadocBeforeRecordA(int a) {}

	// A comment trailing the `}` normally attaches forward to the next token and leaves OBJBLOCK,
	// but with nothing after it to attach to it is parked AFTER the RCURLY, still inside OBJBLOCK.
	// The body is empty either way; the real case is the top-level record at the end of this file.
	record TrailingCommentAfterBraceA(int a) {} // trailing

	void localRecords() {
		record LocalA(int x) {}

		record LocalB(
				int x,
				int y
		) {}
	}
}

// Nothing but comments follow, so this trailing comment has no token to attach forward to and is
// parked inside OBJBLOCK after the RCURLY. The body is still empty.
record TopLevelTrailingCommentA(int a) {} // trailing