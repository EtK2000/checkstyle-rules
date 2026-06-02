package com.etk2000.checkstyle.inputs.fieldsorting;

@interface AnnV {}

@interface BnnV {}

@interface TAnnA {}

@interface TAnnB {}

@interface TAnnParam {
	int value() default 0;
}

// === case: annotation_annotated_before_unannotated ===
class InputFieldSortingAnnotationAnnotatedBeforeUnannotatedSliceViolation {
	String plain;
	@Deprecated
	String annotated;
}
// === end ===

// === case: annotation_array_value_close_brace_own_line ===
class InputFieldSortingAnnotationArrayValueCloseBraceOwnLineSliceViolation {
	int alpha;
	@SuppressWarnings({
			"unused",
			"rawtypes"
	})
	int zebra;
}
// === end ===

// === case: annotation_explicit_value_keyword_normalization ===
class InputFieldSortingAnnotationExplicitValueKeywordNormalizationSliceViolation {
	@SuppressWarnings(value = "unused")
	int alpha = 2;
	@SuppressWarnings(value = "unused")
	int beta = 1;
}
// === end ===

// === case: annotation_multi_annotation ===
class InputFieldSortingAnnotationMultiAnnotationSliceViolation {
	@AnnV
	@BnnV
	String abField;
	@BnnV
	String bField;
}
// === end ===

// === case: annotation_multi_line_close_trailing_constants ===
enum InputFieldSortingAnnotationMultiLineCloseTrailingConstantsSliceViolation {
	ALPHA,
	@SuppressWarnings(
		"unchecked"
	)
	BETA
}
// === end ===

// === case: annotation_qualified ===
class InputFieldSortingAnnotationQualifiedSliceViolation {
	@java.lang.Deprecated
	int alpha;
	@java.lang.SuppressWarnings("unused")
	int beta;
}
// === end ===

// === case: annotation_same_annotation_name ===
class InputFieldSortingAnnotationSameAnnotationNameSliceViolation {
	@Deprecated
	String alpha, zebra;
}
// === end ===

// === case: annotation_same_line_trailing_constants ===
enum InputFieldSortingAnnotationSameLineTrailingConstantsSliceViolation {
	ALPHA,
	@Deprecated
	BETA
}
// === end ===

// === case: annotation_static ===
// skip-reason: cannot safely reorder static fields with annotations
class InputFieldSortingAnnotationStaticSliceViolation {
	@SuppressWarnings("unused")
	static String beta;
	@Deprecated
	static String alpha;
}
// === end ===

// === case: annotation_wrong_annotation_order ===
class InputFieldSortingAnnotationWrongAnnotationOrderSliceViolation {
	@Deprecated
	int alpha;
	@SuppressWarnings("unused")
	int beta;
}
// === end ===

// === case: anonclass_anon_before_field_by_type ===
class InputFieldSortingAnonClassAnonBeforeFieldByTypeSliceViolation {
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			System.out.println("hi");
		}
	};
	final Object data = "x";
}
// === end ===

// === case: anonclass_anon_before_field_by_type_redundant ===
class InputFieldSortingAnonClassAnonBeforeFieldByTypeRedundantSliceViolation {
	final Comparable<Integer> action = new Comparable<>() {
		@Override
		public int compareTo(Integer o) {
			return 0;
		}
	};
	final Runnable data = () -> System.out.println("x");
}
// === end ===

// === case: anonclass_anon_initializer_after_field ===
// skip-reason: cannot reorder anonymous-class initializer field across a field it references
// imports: java.util.HashMap
// imports: java.util.Map
class InputFieldSortingAnonClassAnonInitializerAfterFieldSliceViolation {
	final Map<String, Object> data = new HashMap<>();
	final Runnable action = new Runnable() {
		@Override
		public void run() {
			data.clear();
		}
	};
}

class InputFieldSortingAnonClassLambdaNotAnon {
	final Runnable action = () -> System.out.println("hello");
	final String name = "test";
}
// === end ===

// === case: anonclass_anon_initializer_nested_generics ===
// skip-reason: cannot reorder anonymous-class initializer field across a field it references
// imports: java.util.HashMap
// imports: java.util.List
// imports: java.util.Map
class InputFieldSortingAnonClassAnonInitializerNestedGenericsSliceViolation {
	final Map<String, Object> data = new HashMap<>();
	final Object action = new HashMap<String, List<Integer>>() {
		@Override
		public void clear() {
			data.clear();
		}
	};
}
// === end ===

// === case: anonclass_lambda_forward_reference ===
class InputFieldSortingAnonClassLambdaForwardReferenceSliceViolation {
	final String name = "test";
	final Runnable action = () -> System.out.println(name);
}
// === end ===

// === case: anonclass_pattern_in_string_initializer ===
class InputFieldSortingAnonClassPatternInStringInitializerSliceViolation {
	final String alpha = "hello";
	final String zebra = "new Runnable() { run(); }";
}
// === end ===

// === case: array_c_style_mixed_style_reorders ===
class InputFieldSortingArrayCStyleMixedStyleReordersSliceViolation {
	int alpha[];
	int[] zebra;
}
// === end ===

// === case: array_c_style_plain_sibling_reorders ===
class InputFieldSortingArrayCStylePlainSiblingReordersSliceViolation {
	int codes;
	int elements[];
}
// === end ===

// === case: array_c_style_reorders ===
class InputFieldSortingArrayCStyleReordersSliceViolation {
	int alpha[];
	int zebra[];
}
// === end ===

// === case: array_c_style_secondary_bracket_self_rebuild ===
class InputFieldSortingArrayCStyleSecondaryBracketSelfRebuildSliceViolation {
	int alpha, zebra[];
	String beta;
}
// === end ===

// === case: array_c_style_secondary_name_skips ===
// skip-reason: cannot reorder a field past a name bound in a multi-variable declaration
class InputFieldSortingArrayCStyleSecondaryNameSkipsSliceViolation {
	int alpha, zebra[];
	int beta;
}
// === end ===

// === case: array_c_style_two_dimensional_reorders ===
class InputFieldSortingArrayCStyleTwoDimensionalReordersSliceViolation {
	int alpha[][];
	int zebra[][];
}
// === end ===

// === case: array_c_style_whitespace_inside_brackets ===
class InputFieldSortingArrayCStyleWhitespaceInsideBracketsSliceViolation {
	int alpha;
	int zebra[ ];
}
// === end ===

// === case: array_c_style_with_string_group_consolidation ===
class InputFieldSortingArrayCStyleWithStringGroupConsolidationSliceViolation {
	int elements[];
	String alpha, zebra;
}
// === end ===

// === case: array_distinct_element_types ===
class InputFieldSortingArrayDistinctElementTypesSliceViolation {
	int[] codes;
	String[] names;
}
// === end ===

// === case: array_int_after_string ===
class InputFieldSortingArrayIntAfterStringSliceViolation {
	int plain;
	int[] arr;
	String name;
}
// === end ===

// === case: array_mixed_bracket_style_multi_var_skips ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingArrayMixedBracketStyleMultiVarSkipsSliceViolation {
	private String[] y[], x;
}
// === end ===

// === case: array_mixed_bracket_style_single_declarator ===
class InputFieldSortingArrayMixedBracketStyleSingleDeclaratorSliceViolation {
	String[] alpha[];
	String[] zebra[];
}
// === end ===

// === case: array_multi_var_reorders ===
class InputFieldSortingArrayMultiVarReordersSliceViolation {
	int[] alpha, zebra;
}
// === end ===

// === case: array_multidim_and_scalar ===
class InputFieldSortingArrayMultidimAndScalarSliceViolation {
	int scalar;
	int[] vector;
	int[][] matrix;
}
// === end ===

// === case: array_primitive_after_array ===
class InputFieldSortingArrayPrimitiveAfterArraySliceViolation {
	char letter;
	double[] values;
}
// === end ===

// === case: case_insensitive_sort ===
enum InputFieldSortingCaseInsensitiveSortSliceViolation {
	Alpha,
	beta
}
// === end ===

// === case: chunk_mixed_chunks ===
class InputFieldSortingChunkMixedChunksSliceViolation {
	final int finalWithValue = 1;

	final int finalNoValue;

	int nonFinal;

	InputFieldSortingChunkMixedChunksSliceViolation(int value) {
		this.finalNoValue = value;
	}
}
// === end ===

// === case: dependency_alpha_after_dep_chain ===
class InputFieldSortingDependencyAlphaAfterDepChainSliceViolation {
	static final int ALPHA = 5;
	static final int BASE = 10;
	static final int DERIVED = BASE + 1;
}
// === end ===

// === case: dependency_forward_ref ===
class InputFieldSortingDependencyForwardRefSliceViolation {
	int alpha = 10;
	int beta = this.alpha + 1;
}
// === end ===

// === case: enumconstant_annotated_after_previous ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousSliceViolation {
	@Deprecated
	ALPHA,
	ZETA
}
// === end ===

// === case: enumconstant_annotated_after_previous_comment ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousCommentSliceViolation {
	/* mid */ @Deprecated
	ALPHA,
	ZETA
}
// === end ===

// === case: enumconstant_annotated_same_line ===
enum InputFieldSortingEnumConstantAnnotatedSameLineSliceViolation {
	ALPHA,
	@Deprecated
	ZETA
}
// === end ===

// === case: enumconstant_annotated_same_line_leading_comment ===
enum InputFieldSortingEnumConstantAnnotatedSameLineLeadingCommentSliceViolation {
	ALPHA,
	/* keep */ @Deprecated
	ZETA
}
// === end ===

// === case: enumconstant_basic ===
enum InputFieldSortingEnumConstantBasicSliceViolation {
	ALPHA,
	ZEBRA;

	static final int MAX = 10;
	static final int MIN = 1;
}

@SuppressWarnings("FieldSorting")
enum InputFieldSortingEnumConstantViolationSuppressedSibling {
	ZEBRA,
	ALPHA
}
// === end ===

// === case: enumconstant_body_with_text_block ===
enum InputFieldSortingEnumConstantBodyWithTextBlockSliceViolation {
	INSTANCE {
		int count;
		String docs = """
				contains ; and } fake terminators
				second line
				""";
		String name;
	}
}
// === end ===

// === case: enumconstant_inner ===
class InputFieldSortingEnumConstantInnerSliceViolation {
	enum Misordered {
		FIRST,
		SECOND
	}
}
// === end ===

// === case: enumconstant_multiple ===
enum InputFieldSortingEnumConstantMultipleSliceViolation {
	ALPHA,
	BRAVO,
	CHARLIE
}
// === end ===

// === case: enumconstant_outer_enum ===
enum InputFieldSortingEnumConstantOuterEnumSliceViolation {
	ALPHA,
	BETA;

	enum InnerMisordered {
		XENON,
		YELLOW
	}
}
// === end ===

// === case: enumconstant_single_then_fields_reorder ===
enum InputFieldSortingEnumConstantSingleThenFieldsReorderSliceViolation {
	A;
	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: enumconstant_with_bodies ===
enum InputFieldSortingEnumConstantWithBodiesSliceViolation {
	ADD {
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	},
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	};

	abstract int apply(int a, int b);
}
// === end ===

// === case: enumconstant_with_members ===
enum InputFieldSortingEnumConstantWithMembersSliceViolation {
	APPLE("green"),
	BANANA("yellow"),
	CHERRY("red");

	final String color;

	InputFieldSortingEnumConstantWithMembersSliceViolation(String color) {
		this.color = color;
	}

	String getColor() {
		return color;
	}
}
// === end ===

// === case: enumconstant_wrong_key ===
@SuppressWarnings("unused")
enum InputFieldSortingEnumConstantWrongKeySliceViolation {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings(value = "unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: enumkeyword_in_block_comment ===
/*
 * Doc paragraph mentioning enum constants
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentSliceViolation {
	final int alpha = 2;
	final int beta = 1;
}
// === end ===

// === case: enumkeyword_in_block_comment_with_braces ===
/*
 * Doc paragraph with example: { not real brace } and another { } pair
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentWithBracesSliceViolation {
	final int alpha = 2;
	final int beta = 1;
}
// === end ===

// === case: enumkeyword_in_string_initializer ===
class InputFieldSortingEnumKeywordInStringInitializerSliceViolation {
	final String alpha = "one";
	final String docs = "enum constants list";
}
// === end ===

// === case: enumkeyword_in_text_block ===
class OuterEnumKeywordInTextBlock {
	static final String docs = """
			preamble
			content with enum keyword
			""";
}

enum InputFieldSortingEnumKeywordInTextBlockSliceViolation {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: enumkeyword_in_text_block_above_class ===
class InputFieldSortingEnumKeywordInTextBlockAboveClassSliceViolation {
	static final String docs = """
			line one
			describes enum constants list
			more lines
			""";
	final int alpha = 2;
	final int beta = 1;
}
// === end ===

// === case: enumkeyword_lookback_across_block_comment_47_lines ===
enum InputFieldSortingEnumkeywordLookbackAcrossBlockComment47LinesSliceViolation
/*
 * 1
 * 2
 * 3
 * 4
 * 5
 * 6
 * 7
 * 8
 * 9
 * 10
 * 11
 * 12
 * 13
 * 14
 * 15
 * 16
 * 17
 * 18
 * 19
 * 20
 * 21
 * 22
 * 23
 * 24
 * 25
 * 26
 * 27
 * 28
 * 29
 * 30
 * 31
 * 32
 * 33
 * 34
 * 35
 * 36
 * 37
 * 38
 * 39
 * 40
 * 41
 * 42
 * 43
 * 44
 * 45
 * 46
 * 47
 */
{
	ALPHA,
	BETA
}
// === end ===

// === case: enumkeyword_lookback_across_block_comment_48_lines ===
enum InputFieldSortingEnumkeywordLookbackAcrossBlockComment48LinesSliceViolation
/*
 * 1
 * 2
 * 3
 * 4
 * 5
 * 6
 * 7
 * 8
 * 9
 * 10
 * 11
 * 12
 * 13
 * 14
 * 15
 * 16
 * 17
 * 18
 * 19
 * 20
 * 21
 * 22
 * 23
 * 24
 * 25
 * 26
 * 27
 * 28
 * 29
 * 30
 * 31
 * 32
 * 33
 * 34
 * 35
 * 36
 * 37
 * 38
 * 39
 * 40
 * 41
 * 42
 * 43
 * 44
 * 45
 * 46
 * 47
 * 48
 */
{
	ALPHA,
	BETA
}
// === end ===

// === case: enumkeyword_lookback_stops_at_sibling_close_brace ===
enum InputFieldSortingEnumKeywordLookbackStopsAtSiblingCloseBraceOuter {
	A;

	int someMethod() {
		return 1;
	}
}

class InputFieldSortingEnumKeywordLookbackStopsAtSiblingCloseBraceSliceViolation {
	final int alpha = 2;
	final int beta = 1;
}
// === end ===

// === case: enumkeyword_sibling_enum_lookback_boundary ===
enum InputFieldSortingEnumKeywordSiblingEnumA {
	A
}

class InputFieldSortingEnumKeywordSiblingEnumLookbackBoundarySliceViolation {
	final int alpha = 2;
	final int beta = 1;
}
// === end ===

// === case: enumsameline_inner ===
class InputFieldSortingEnumSameLineInnerSliceViolation {
	enum Inner {
		ALPHA,
		BETA
	}
}
// === end ===

// === case: enumsameline_interior_block_comment ===
enum InputFieldSortingEnumSameLineInteriorBlockCommentSliceViolation {
	ALPHA,
	/* mid */ BETA,
	GAMMA
}
// === end ===

// === case: enumsameline_text_block_arg ===
enum InputFieldSortingEnumSameLineTextBlockArgSliceViolation {
	ALPHA("""x"""),
	BETA
}
// === end ===

// === case: enumsameline_trailing_block_comment_fake_comma ===
enum InputFieldSortingEnumSameLineTrailingBlockCommentFakeCommaSliceViolation {
	ALPHA,
	BETA; /* fake , Z */

	static final int VAL = 1;
}
// === end ===

// === case: enumsameline_whole_enum_one_line ===
enum InputFieldSortingEnumSameLineWholeEnumOneLineSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumsameline_with_body_brace_depth ===
enum InputFieldSortingEnumSameLineWithBodyBraceDepthSliceViolation {
	ALPHA,
	BETA { void foo() {} }
}
// === end ===

// === case: enumspan_annotation_arg_array_value ===
enum InputFieldSortingEnumSpanAnnotationArgArrayValueSliceViolation {
	@SuppressWarnings({"a", "b"})
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	@Deprecated()
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	@SuppressWarnings(value = "unused")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSpanAnnotationArgStructuralCharsSliceViolation {
	@SuppressWarnings("{ } , // /*")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_blank_line_between_constants ===
enum InputFieldSortingEnumSpanBlankLineBetweenConstantsSliceViolation {
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_brace_close_shared ===
enum InputFieldSortingEnumSpanBraceCloseSharedSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_brace_open_shared ===
enum InputFieldSortingEnumSpanBraceOpenSharedSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_brace_shared_with_semi_body ===
enum InputFieldSortingEnumSpanBraceSharedWithSemiBodySliceViolation {
	ALPHA,
	BETA;

	static final int VAL = 1;
}
// === end ===

// === case: enumspan_code_suffix_text_block ===
enum InputFieldSortingEnumSpanCodeSuffixTextBlockSliceViolation {
	ALPHA,
	ZETA;
	String d = """
			x
			""";
}
// === end ===

// === case: enumspan_comment_before_constant_ident ===
enum InputFieldSortingEnumSpanCommentBeforeConstantIdentSliceViolation {
	/* c */ ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_comment_before_separator ===
enum InputFieldSortingEnumSpanCommentBeforeSeparatorSliceViolation {
	ALPHA,
	ZETA /* t */
}
// === end ===

// === case: enumspan_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSpanCommentBetweenAnnotationAndConstantSliceViolation {
	@Deprecated
	// note
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_comment_between_stacked_annotations ===
enum InputFieldSortingEnumSpanCommentBetweenStackedAnnotationsSliceViolation {
	@Deprecated
	/* x */ @SuppressWarnings("y")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_comment_block_trailer ===
enum InputFieldSortingEnumSpanCommentBlockTrailerSliceViolation {
	ALPHA,
	ZETA
	// tail
}
// === end ===

// === case: enumspan_comment_in_argument_list ===
enum InputFieldSortingEnumSpanCommentInArgumentListSliceViolation {
	ALPHA(/* c */ 2),
	ZETA(1);

	private final int value;

	InputFieldSortingEnumSpanCommentInArgumentListSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: enumspan_comment_in_constant_body ===
enum InputFieldSortingEnumSpanCommentInConstantBodySliceViolation {
	ALPHA {
		// a note
	},
	ZETA {
		// z note
	}
}
// === end ===

// === case: enumspan_comment_open_after_comma ===
// skip-reason: cannot reorder enum constants across a comment that does not close on the line it opens
enum InputFieldSortingEnumSpanCommentOpenAfterCommaSliceViolation {
	ZETA, /* ZETA is legacy;
	see #123 */
	ALPHA
}
// === end ===

// === case: enumspan_comment_open_after_terminator ===
// skip-reason: cannot reorder enum constants across a comment that does not close on the line it opens
enum InputFieldSortingEnumSpanCommentOpenAfterTerminatorSliceViolation {
	ZETA,
	ALPHA,
	BETA; /*
	legacy ordinal order is load-bearing
	*/

	int rgb;
}
// === end ===

// === case: enumspan_comment_open_on_header_line ===
// skip-reason: cannot reorder enum constants across a comment that does not close on the line it opens
enum InputFieldSortingEnumSpanCommentOpenOnHeaderLineSliceViolation { /*
ordinal order is load-bearing
*/
	ZETA,
	ALPHA
}
// === end ===

// === case: enumspan_comment_straddles_comma ===
enum InputFieldSortingEnumSpanCommentStraddlesCommaSliceViolation {
	/*b*/ ALPHA,
	ZETA /*a*/
}
// === end ===

// === case: enumspan_escaped_triple_quote ===
enum InputFieldSortingEnumSpanEscapedTripleQuoteSliceViolation {
	@SuppressWarnings("\"\"\"")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_generic_implements_header ===
enum InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation implements Comparable<InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation> {
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_header_line_comment ===
enum InputFieldSortingEnumSpanHeaderLineCommentSliceViolation { // note
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_javadoc_multiline ===
enum InputFieldSortingEnumSpanJavadocMultilineSliceViolation {
	/**
	 * alpha.
	 */
	ALPHA,
	/**
	 * zeta.
	 */
	ZETA
}
// === end ===

// === case: enumspan_lead_both_owners ===
enum InputFieldSortingEnumSpanLeadBothOwnersSliceViolation {
	// note-a
	ALPHA,
	// note-z
	ZETA
}
// === end ===

// === case: enumspan_lead_first_sorts_last ===
enum InputFieldSortingEnumSpanLeadFirstSortsLastSliceViolation {
	ALPHA,
	// note
	ZETA
}
// === end ===

// === case: enumspan_lead_first_stays_first ===
enum InputFieldSortingEnumSpanLeadFirstStaysFirstSliceViolation {
	// note
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_list_tail_block_comment_multiline ===
enum InputFieldSortingEnumSpanListTailBlockCommentMultilineSliceViolation {
	ACTIVE,
	ZOMBIE;
	/* line1

	   line3 */

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSpanListTailCommentNoTrailingCommaSliceViolation {
	ACTIVE,
	ZOMBIE;
	// NOTE: order is serialized to disk

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_own_line ===
enum InputFieldSortingEnumSpanListTailCommentOwnLineSliceViolation {
	ACTIVE,
	ZOMBIE;
	// NOTE: order is serialized to disk

	int x;
}
// === end ===

// === case: enumspan_member_shares_terminator_line ===
class InputFieldSortingEnumSpanMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ALPHA,
		ZETA;
		static final int VAL = 1;
	}
}
// === end ===

// === case: enumspan_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSpanMultilineAnnotationFirstLineSharedSliceViolation {
	@SuppressWarnings(
			"x")
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_nested_type_after_terminator ===
enum InputFieldSortingEnumSpanNestedTypeAfterTerminatorSliceViolation {
	ALPHA,
	ZETA;

	interface Marker {
	}
}
// === end ===

// === case: enumspan_non_canonical_indent ===
enum InputFieldSortingEnumSpanNonCanonicalIndentSliceViolation {
			ALPHA,
			ZETA
}
// === end ===

// === case: enumspan_perm_acb ===
enum InputFieldSortingEnumSpanPermAcbSliceViolation {
	// L-A
	AAA, // T-A
	// L-B
	BBB, // T-B
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_perm_bac ===
enum InputFieldSortingEnumSpanPermBacSliceViolation {
	// L-A
	AAA, // T-A
	// L-B
	BBB, // T-B
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_perm_bca ===
enum InputFieldSortingEnumSpanPermBcaSliceViolation {
	// L-A
	AAA, // T-A
	// L-B
	BBB, // T-B
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_perm_cab ===
enum InputFieldSortingEnumSpanPermCabSliceViolation {
	// L-A
	AAA, // T-A
	// L-B
	BBB, // T-B
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_perm_cba ===
enum InputFieldSortingEnumSpanPermCbaSliceViolation {
	// L-A
	AAA, // T-A
	// L-B
	BBB, // T-B
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSpanStackedAnnotationsAfterPreviousSliceViolation {
	/* mid */ @Deprecated
	@SuppressWarnings("x")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_string_delimiter_before_annotation ===
enum InputFieldSortingEnumSpanStringDelimiterBeforeAnnotationSliceViolation {
	ALPHA,
	@SuppressWarnings(
			"unchecked")
	@Deprecated
	BETA
}
// === end ===

// === case: enumspan_string_with_block_comment_marker ===
enum InputFieldSortingEnumSpanStringWithBlockCommentMarkerSliceViolation {
	@SuppressWarnings("/* not a comment */")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_suffix_unclosed_comment_after_brace ===
enum InputFieldSortingEnumSpanSuffixUnclosedCommentAfterBraceSliceViolation {
	ALPHA,
	BETA,
	ZETA
} /*
still open
*/
// === end ===

// === case: enumspan_supplementary_annotation_arg ===
enum InputFieldSortingEnumSpanSupplementaryAnnotationArgSliceViolation {
	@SuppressWarnings("𝐀")
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_supplementary_constant_name ===
enum InputFieldSortingEnumSpanSupplementaryConstantNameSliceViolation {
	ALPHA𝐀,
	ZETA
}
// === end ===

// === case: enumspan_text_block_arg_multiline ===
enum InputFieldSortingEnumSpanTextBlockArgMultilineSliceViolation {
	ALPHA,
	ZETA("""
			x
			""")
}
// === end ===

// === case: enumspan_text_block_in_constant_body ===
enum InputFieldSortingEnumSpanTextBlockInConstantBodySliceViolation {
	ALPHA {
		String v() {
			return """
					a""";
		}
	},
	ZETA {
		String v() {
			return """
					z""";
		}
	}
}
// === end ===

// === case: enumspan_trailing_comma ===
enum InputFieldSortingEnumSpanTrailingCommaSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_trailing_comma_terminator_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommaTerminatorCommentMultilineSliceViolation {
	ALPHA, // done
	ZETA;
	// more

	int x;
}
// === end ===

// === case: enumspan_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSpanTrailingCommaThenTerminatorCommentSliceViolation {
	ALPHA, // done
	ZETA;

	int x;
}
// === end ===

// === case: enumspan_trailing_comment_multiline ===
// skip-reason: cannot relocate a comment trailing an enum constant across multiple lines
enum InputFieldSortingEnumSpanTrailingCommentMultilineSliceViolation {
	ZETA /* a */
	/* b */, ALPHA
}
// === end ===

// === case: field_annotation_consolidation ===
class InputFieldSortingFieldAnnotationConsolidationSliceViolation {
	@NonNull
	final String currencyCode, equityNumber, source;
	@Nullable
	final String engName, engSymbol, exchange;
}
// === end ===

// === case: field_annotation_consolidation_skips_trailing_comment ===
class InputFieldSortingFieldAnnotationConsolidationSkipsTrailingCommentSliceViolation {
	int height;
	int width; // in pixels
}
// === end ===

// === case: field_annotation_empty_parens_normalization ===
class InputFieldSortingFieldAnnotationEmptyParensNormalizationSliceViolation {
	@Deprecated()
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_block_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInBlockCommentSliceViolation {
	String alpha;
	String /* @Nullable */ beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_initializer ===
class InputFieldSortingFieldAnnotationIgnoresAtInInitializerSliceViolation {
	Object alpha = "hello";
	Object beta = x > 0 ? new @TypeUse Object() : null;
}
// === end ===

// === case: field_annotation_ignores_at_in_line_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInLineCommentSliceViolation {
	String alpha;
	String beta; // @Deprecated docs
}
// === end ===

// === case: field_annotation_ignores_at_in_string ===
class InputFieldSortingFieldAnnotationIgnoresAtInStringSliceViolation {
	String alpha = "hello";
	String beta = "@Zebra";
}
// === end ===

// === case: field_annotation_ignores_at_in_text_block ===
class InputFieldSortingFieldAnnotationIgnoresAtInTextBlockSliceViolation {
	String alpha = "hello";
	String beta = """
		@FakeAnnotation
		""";
}
// === end ===

// === case: field_annotation_ignores_at_in_text_block_escaped_triple_quote ===
class InputFieldSortingFieldAnnotationIgnoresAtInTextBlockEscapedTripleQuoteSliceViolation {
	String alpha = "hello";
	String beta = """
		line with \""" and @FakeAnnotation
		more content
		""";
}
// === end ===

// === case: field_annotation_ignores_at_in_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldAnnotationIgnoresAtInTypeArgSliceViolation {
	List<@Zebra String> typeArgAnnotated;
	@Alpha
	List<String> fieldAnnotated;
}
// === end ===

// === case: field_annotation_ignores_at_in_type_arg_with_initializer ===
// imports: java.util.List
class InputFieldSortingFieldAnnotationIgnoresAtInTypeArgWithInitializerSliceViolation {
	List<String> alpha = List.of();
	List<@Zebra String> beta = List.of();
}
// === end ===

// === case: field_annotation_multi_line ===
class InputFieldSortingFieldAnnotationMultiLineSliceViolation {
	int alpha = 2;
	@SuppressWarnings(
		"unused"
	)
	int beta = 1;
}
// === end ===

// === case: field_annotation_multi_line_block_comment_state_threaded ===
class InputFieldSortingFieldAnnotationMultiLineBlockCommentStateThreadedSliceViolation {
	@AnnV
	@BnnV
	String alpha = "a";
	@AnnV
	/* @FakeAnno1
	   @FakeAnno2 */
	@BnnV
	String zebra = "z";
}
// === end ===

// === case: field_annotation_multi_line_with_text_block_value ===
class InputFieldSortingFieldAnnotationMultiLineWithTextBlockValueSliceViolation {
	@SuppressWarnings(
			"""
			ignored text block with parens ( ) and "quotes"
			"""
	)
	String alpha;
	@SuppressWarnings(
			"""
			ignored text block with parens ( ) and "quotes"
			"""
	)
	String zebra;
}
// === end ===

// === case: field_annotation_order_different_annotations ===
class InputFieldSortingFieldAnnotationOrderDifferentAnnotationsSliceViolation {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_order_multi_annotation ===
class InputFieldSortingFieldAnnotationOrderMultiAnnotationSliceViolation {
	@Ann
	@Bnn
	String abField;
	@Bnn
	String bField;
}
// === end ===

// === case: field_annotation_order_qualified ===
class InputFieldSortingFieldAnnotationOrderQualifiedSliceViolation {
	@java.lang.Deprecated
	String alpha;
	@java.lang.SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_paren_balanced_with_block_comment ===
class InputFieldSortingFieldAnnotationParenBalancedWithBlockCommentSliceViolation {
	@SuppressWarnings(/* fake ) */ "unused")
	String alpha, zebra;
}
// === end ===

// === case: field_annotation_same_annotation_name_order ===
class InputFieldSortingFieldAnnotationSameAnnotationNameOrderSliceViolation {
	@Deprecated
	String alpha, zebra;
}
// === end ===

// === case: field_annotation_with_inline_block_comment_prefix ===
class InputFieldSortingFieldAnnotationWithInlineBlockCommentPrefixSliceViolation {
	@Deprecated
	String alpha;
	/* keep */ @Deprecated
	String zebra;
}
// === end ===

// === case: field_array_type_order ===
class InputFieldSortingFieldArrayTypeOrderSliceViolation {
	int x;
	int[] arr;
}
// === end ===

// === case: field_brace_in_char_literal_between_field_and_class_open ===
class InputFieldSortingFieldBraceInCharLiteralBetweenFieldAndClassOpenSliceViolation {
	final char alpha = 'a';
	final char zebra = '{';
}
// === end ===

// === case: field_braces_in_single_line_string_between_field_and_class_open ===
class InputFieldSortingFieldBracesInSingleLineStringBetweenFieldAndClassOpenSliceViolation {
	final String alpha = "a";
	final String zebra = "{ fake brace }";
}
// === end ===

// === case: field_c_style_array_declarator_refused ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingFieldCStyleArrayDeclaratorRefusedSliceViolation {
	private int y[], x;
}
// === end ===

// === case: field_chunk_keywords_in_block_comment ===
class InputFieldSortingFieldChunkKeywordsInBlockCommentSliceViolation {
	String /* static final = */ alpha;
	String /* static final = */ zebra;
}
// === end ===

// === case: field_chunk_keywords_in_string ===
class InputFieldSortingFieldChunkKeywordsInStringSliceViolation {
	final String alpha = "static final =";
	final String zebra = "static final =";
}
// === end ===

// === case: field_chunk_order ===
class InputFieldSortingFieldChunkOrderSliceViolation {
	final int finalWithValue = 1;

	int nonFinal;
}
// === end ===

// === case: field_chunk_order_all_three_chunks ===
class InputFieldSortingFieldChunkOrderAllThreeChunksSliceViolation {
	final int a = 1;

	final int b;

	int c;
}
// === end ===

// === case: field_circular_dependency ===
// skip-reason: cannot reorder fields with a circular dependency
class InputFieldSortingFieldCircularDependencySliceViolation {
	static final int A = B + 1;
	static final int B = A + 1;
	static final int y = 0;
	static final int x = 0;
}
// === end ===

// === case: field_class_body_end_walks_through_text_block_brace ===
@SuppressWarnings("unused")
class InputFieldSortingFieldClassBodyEndWalksThroughTextBlockBraceSliceViolation {
	static final String docs = """
			line one
			contains } fake brace
			line three
			""";
	final String alpha = "a";
	final String zebra = "z";
}
// === end ===

// === case: field_consolidation_block_comment_contains_double_slash ===
class InputFieldSortingFieldConsolidationBlockCommentContainsDoubleSlashSliceViolation {
	String alpha, zebra;
}
// === end ===

// === case: field_consolidation_skips_candidate_with_block_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithBlockCommentSliceViolation {
	String alpha;
	/* keep me */
	String zebra;
}
// === end ===

// === case: field_consolidation_skips_candidate_with_javadoc ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithJavadocSliceViolation {
	String alpha;
	/** keep me */
	String zebra;
}
// === end ===

// === case: field_consolidation_skips_candidate_with_line_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithLineCommentSliceViolation {
	String alpha;
	// keep me
	String zebra;
}
// === end ===

// === case: field_consolidation_skips_different_transient ===
class InputFieldSortingFieldConsolidationSkipsDifferentTransientSliceViolation {
	int alpha;
	transient int zebra;
}
// === end ===

// === case: field_consolidation_skips_different_visibility ===
class InputFieldSortingFieldConsolidationSkipsDifferentVisibilitySliceViolation {
	private int alpha;
	int zebra;
}
// === end ===

// === case: field_consolidation_wraps_one_name_per_line ===
class InputFieldSortingFieldConsolidationWrapsOneNamePerLineSliceViolation {
	final String nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaA,
			nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaB,
			nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaC;
}
// === end ===

// === case: field_consolidation_wraps_when_merged_too_long ===
class InputFieldSortingFieldConsolidationWrapsWhenMergedTooLongSliceViolation {
	final String veryLongNameAlphaaaaaaaaa, veryLongNameBetaaaaaaaaaa, veryLongNameDeltaaaaaaaaa,
			veryLongNameEpsilonaaaaaa, veryLongNameGammaaaaaaaaa;
}
// === end ===

// === case: field_dependency_initializer_on_continuation_line ===
class InputFieldSortingFieldDependencyInitializerOnContinuationLineSliceViolation {
	int alpha = 10;
	int beta = 1
			+ this.alpha;
}
// === end ===

// === case: field_dependency_order ===
class InputFieldSortingFieldDependencyOrderSliceViolation {
	static final int A = 0;
	static final int B = A + 1;
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_inner_field ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesInnerFieldSliceViolation {
	int alpha = 2;
	int zebra = 1;

	class Inner {
		int beta = 5;
	}
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_nested_local ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesNestedLocalSliceViolation {
	int alpha = 2;
	int beta = 1;

	void m() {
		int charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_duplicate_text_below_does_not_steal_endidx ===
@SuppressWarnings("unused")
class InputFieldSortingFieldDuplicateTextBelowDoesNotStealEndIdxSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method() {
		int alpha = 2;
		System.out.println(alpha);
	}
}
// === end ===

// === case: field_extract_all_names_no_match_fallback ===
class InputFieldSortingFieldExtractAllNamesNoMatchFallbackSliceViolation {
	int alpha = 1;
	int zebra
	;
}
// === end ===

// === case: field_extract_names_initializer_angle_comparison ===
class InputFieldSortingFieldExtractNamesInitializerAngleComparisonSliceViolation {
	int alpha = 7;
	int yankee = 5 < 10 ? 1 : 0, zulu = 5;
}
// === end ===

// === case: field_extract_names_initializer_angle_explicit_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldExtractNamesInitializerAngleExplicitTypeArgSliceViolation {
	int alpha = 7;
	int yankee = List.<Integer>of().size(), zulu = 5;
}
// === end ===

// === case: field_extract_names_initializer_brace_comma ===
class InputFieldSortingFieldExtractNamesInitializerBraceCommaSliceViolation {
	int[] alpha = {6};
	int[] yankee = {1, 2, 3}, zulu = {4, 5};
}
// === end ===

// === case: field_extract_names_initializer_char_comma ===
class InputFieldSortingFieldExtractNamesInitializerCharCommaSliceViolation {
	char alpha = 'a';
	char max = ',';
}
// === end ===

// === case: field_extract_names_initializer_paren_comma ===
@SuppressWarnings("unused")
class InputFieldSortingFieldExtractNamesInitializerParenCommaSliceViolation {
	int alpha = 7;
	int max = Math.max(1, 2);
}
// === end ===

// === case: field_extract_names_initializer_string_comma ===
class InputFieldSortingFieldExtractNamesInitializerStringCommaSliceViolation {
	String alpha = "a";
	String max = "x,y";
}
// === end ===

// === case: field_inner_class_skipped ===
class InputFieldSortingFieldInnerClassSkippedSliceViolation {
	class Inner {
		int first = 1;
	}

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_inner_interface_skipped ===
class InputFieldSortingFieldInnerInterfaceSkippedSliceViolation {
	interface Inner {
		int FIRST = 1;
	}

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_inner_record_skipped ===
class InputFieldSortingFieldInnerRecordSkippedSliceViolation {
	record Inner(int x) {}

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_instance_initializer_block_skipped ===
class InputFieldSortingFieldInstanceInitializerBlockSkippedSliceViolation {
	int alpha = 2;
	int beta = 1;

	{
		Math.random();
	}
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields across an interleaved static or instance field
class InputFieldSortingFieldInterleavedDependencyBailSliceViolation {
	int zebra = this.alpha + 1;
	static int s;
	int alpha;
}
// === end ===

// === case: field_interleaved_opposite_static_bail ===
class InputFieldSortingFieldInterleavedOppositeStaticBailSliceViolation {
	int alpha;
	static int x;
	int zebra;
}
// === end ===

// === case: field_interleaved_opposite_static_bail_static_group ===
class InputFieldSortingFieldInterleavedOppositeStaticBailStaticGroupSliceViolation {
	static int alpha;
	int x;
	static int zebra;
}
// === end ===

// === case: field_interleaved_spans_chunks_bail ===
// skip-reason: cannot reorder fields across an interleaved static or instance field
class InputFieldSortingFieldInterleavedSpansChunksBailSliceViolation {
	int alpha;
	static int s;
	final int zebra = 1;
}
// === end ===

// === case: field_local_in_method_body_not_treated_as_field ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLocalInMethodBodyNotTreatedAsFieldSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method() {
		int charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_lookback_consecutive_javadoc_groups ===
class InputFieldSortingFieldLookbackConsecutiveJavadocGroupsSliceViolation {
	/**
	 * Documentation for alpha.
	 * Multiple lines.
	 */
	int alpha = 2;
	/**
	 * Documentation for beta.
	 * Multiple lines.
	 */
	int beta = 1;
}
// === end ===

// === case: field_lookback_does_not_pull_text_block_content ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLookbackDoesNotPullTextBlockContentSliceViolation {
	static final String docs = """
			content line one
			content line two
			""";
	final int alpha = 2;
	final int zebra = 1;
}
// === end ===

// === case: field_lookback_past_non_annotation_parens ===
class InputFieldSortingFieldLookbackPastNonAnnotationParensSliceViolation {
	final int x = Math.max(
			1,
			2
	);

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_method_in_class_skipped ===
class InputFieldSortingFieldMethodInClassSkippedSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method() {
		Math.random();
	}
}
// === end ===

// === case: field_multiline_generic_type_reorder ===
// imports: java.util.Map
class InputFieldSortingFieldMultilineGenericTypeReorderSliceViolation {
	Map<String, Integer> alpha;
	Map<String,
			Long> beta;
}
// === end ===

// === case: field_multiline_inner_class_skipped ===
class InputFieldSortingFieldMultiLineInnerClassSkippedSliceViolation {
	class Inner {
		int first
				= 1;
	}

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_multiline_inner_record_skipped ===
class InputFieldSortingFieldMultiLineInnerRecordSkippedSliceViolation {
	record Inner(
			int x,
			int y
	) {}

	int alpha = 2;
	int beta = 1;
}
// === end ===

// === case: field_multiline_method_in_class_skipped ===
class InputFieldSortingFieldMultiLineMethodInClassSkippedSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method(
			int x,
			int y
	) {
		System.out.println(x);
		System.out.println(y);
	}
}
// === end ===

// === case: field_multivar_secondary_name_dependency ===
// skip-reason: cannot reorder a field whose dependency is bound in a multi-variable declaration
class InputFieldSortingFieldMultivarSecondaryNameDependencySliceViolation {
	int x, y, z;
	int a = y + 1;
}
// === end ===

// === case: field_multivar_secondary_name_dependency_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyReordersSliceViolation {
	int x, y;
	int a = y + 1;
	int z = 1;
}
// === end ===

// === case: field_name_order ===
class InputFieldSortingFieldNameOrderSliceViolation {
	static final int A = 0;
	static final int Z = 1;
}
// === end ===

// === case: field_name_order_inside_anonymous_class ===
class InputFieldSortingFieldNameOrderInsideAnonymousClassSliceViolation {
	final Runnable task = new Runnable() {
		String alpha = "a";
		String zebra = "z";

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: field_nested_generics_type_reorder ===
// imports: java.util.List
// imports: java.util.Map
class InputFieldSortingFieldNestedGenericsTypeReorderSliceViolation {
	int a, z;
	Map<String, List<Integer>> map;
}
// === end ===

// === case: field_read_field_end_walks_through_char_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughCharSemicolonSliceViolation {
	final char alpha = 'a';
	final char zebra = ';';
}
// === end ===

// === case: field_read_field_end_walks_through_string_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughStringSemicolonSliceViolation {
	final String alpha = "a";
	final String zebra = "contains ; semicolon";
}
// === end ===

// === case: field_startidx_rewind_to_pre_annotation_line ===
@SuppressWarnings("unused")
class InputFieldSortingFieldStartIdxRewindToPreAnnotationLineSliceViolation {
	@Deprecated
	String alpha = "a";
	@Deprecated
	String zebra = "z";
}
// === end ===

// === case: field_static_initializer_block_skipped ===
class InputFieldSortingFieldStaticInitializerBlockSkippedSliceViolation {
	static int alpha = 2;
	static int beta = 1;

	static {
		Math.random();
	}
}
// === end ===

// === case: field_string_with_escaped_backslashes ===
class InputFieldSortingFieldStringWithEscapedBackslashesSliceViolation {
	final String alpha = "one";
	final String zebra = "ends with \\\\";
}
// === end ===

// === case: field_string_with_final_keyword ===
class InputFieldSortingFieldStringWithFinalKeywordSliceViolation {
	String a = "hello";
	String b = "final";
}
// === end ===

// === case: field_text_block_between_fields_in_class_body ===
class InputFieldSortingFieldTextBlockBetweenFieldsInClassBodySliceViolation {
	final String alpha = "a";
	final String zebra = "z";
	@SuppressWarnings("unused")
	final String docs = """
			line one
			contains } and { fake braces
			line three
			""";
}
// === end ===

// === case: field_text_block_in_initializer_ignores_dep_name ===
class InputFieldSortingFieldTextBlockInInitializerIgnoresDepNameSliceViolation {
	final String alpha = """
			mentions docs here
			""";
	final String docs = """
			some docs
			""";
}
// === end ===

// === case: field_text_block_in_initializer_walks_through ===
class InputFieldSortingFieldTextBlockInInitializerWalksThroughSliceViolation {
	final String alpha = "one";
	final String zebra = """
			contains ; and { fake terminator
			second line
			""";
}
// === end ===

// === case: field_type_arg_annotation_consolidation ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationConsolidationSliceViolation {
	List<@Ann String> alpha, beta;
}
// === end ===

// === case: field_type_arg_annotation_order ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderSliceViolation {
	List<@Ann String> aField;
	List<@Bnn String> bField;
}
// === end ===

// === case: field_type_arg_annotation_order_annotated_before_unannotated ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderAnnotatedBeforeUnannotatedSliceViolation {
	List<String> plain;
	List<@Ann String> annotated;
}
// === end ===

// === case: field_type_arg_annotation_order_fewer_before_more ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderFewerBeforeMoreSliceViolation {
	List<@Ann String> oneAnn;
	List<@Ann @Bnn String> twoAnns;
}
// === end ===

// === case: field_type_arg_annotation_order_fqn_generic ===
class InputFieldSortingFieldTypeArgAnnotationOrderFqnGenericSliceViolation {
	java.util.Set<@Ann String> aField;
	java.util.Set<@Bnn String> bField;
}
// === end ===

// === case: field_type_arg_annotation_order_lower_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderLowerBoundSliceViolation {
	List<? super @Ann Number> aField;
	List<? super @Bnn Number> bField;
}
// === end ===

// === case: field_type_arg_annotation_order_parameterized ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderParameterizedSliceViolation {
	List<@Ann(1) String> lower;
	List<@Ann(2) String> higher;
}
// === end ===

// === case: field_type_arg_annotation_order_position_aware ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationOrderPositionAwareSliceViolation {
	Map<String, @Ann String> firstArgUnannotated;
	Map<@Ann String, String> firstArgAnnotated;
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardSliceViolation {
	List<@Ann ? extends Number> aField;
	List<@Bnn ? extends Number> bField;
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardBoundSliceViolation {
	List<? extends @Ann Number> aField;
	List<? extends @Bnn Number> bField;
}
// === end ===

// === case: field_type_arg_annotation_raw_vs_parameterized ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationRawVsParameterizedSliceViolation {
	Map raw;
	Map<@TAnnA String, String> annotated;
}
// === end ===

// === case: field_type_order_byte_float_short ===
class InputFieldSortingFieldTypeOrderByteFloatShortSliceViolation {
	byte flags;
	float scale;
	short code;
}
// === end ===

// === case: field_type_order_qualified_generic_type ===
class InputFieldSortingFieldTypeOrderQualifiedGenericTypeSliceViolation {
	static class Outer {
		static class Inner<T> {
		}
	}

	int count;
	Outer.Inner<String> nested;
}
// === end ===

// === case: inner_enum ===
class InputFieldSortingInnerEnumSliceViolation {
	enum Inner {
		ALPHA,
		BETA
	}
}
// === end ===

// === case: interface_field_name_order ===
interface InputFieldSortingInterfaceFieldNameOrderSliceViolation {
	int FIRST = 1;
	int SECOND = 2;
}
// === end ===

// === case: lexer_string_with_block_comment_marker ===
class InputFieldSortingLexerStringWithBlockCommentMarkerSliceViolation {
	final String alpha = "one";
	final String zebra = "contains /* fake block */ inside";
}
// === end ===

// === case: name_alpha_after_zebra ===
class InputFieldSortingNameAlphaAfterZebraSliceViolation {
	final int alpha = 2;
	final int zebra = 1;
}
// === end ===

// === case: name_order_qualified_generic_type ===
// imports: java.util.Map
class InputFieldSortingNameOrderQualifiedGenericTypeSliceViolation {
	Map.Entry<String, Integer> alpha, zebra;
}
// === end ===

// === case: name_within_decl_continuation_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclContinuationCommentSkipsSliceViolation {
	String zebra, // note
			alpha;
}
// === end ===

// === case: name_within_decl_initialized_skips ===
// skip-reason: cannot reorder initialized names in a multi-variable declaration
class InputFieldSortingNameWithinDeclInitializedSkipsSliceViolation {
	int beta = 1, alpha = 2;
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated private int x, y;
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated private int y[], x;
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInlineAnnotationInteriorCommentSkipsSliceViolation {
	@Deprecated private int zebra, /* keep me */ alpha;
}
// === end ===

// === case: name_within_decl_inline_annotation_wrapped ===
class InputFieldSortingNameWithinDeclInlineAnnotationWrappedSliceViolation {
	@Deprecated private int x, y;
}
// === end ===

// === case: name_within_decl_interior_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInteriorCommentSkipsSliceViolation {
	String zebra, /* keep me */ alpha;
}
// === end ===

// === case: name_within_decl_interior_comment_with_semicolon_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInteriorCommentWithSemicolonSkipsSliceViolation {
	String zebra, alpha /* x; y */;
}
// === end ===

// === case: name_within_decl_multiline_annotation ===
class InputFieldSortingNameWithinDeclMultilineAnnotationSliceViolation {
	@SuppressWarnings({
			"unused"
	})
	int alpha, zebra;
}
// === end ===

// === case: name_within_decl_sorted_names_c_style_prev_declarator ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclSortedNamesCStylePrevDeclaratorSliceViolation {
	int alpha[], beta;
}
// === end ===

// === case: name_within_decl_sorted_names_chunk_violation ===
// skip-reason: cannot reorder initialized names in a multi-variable declaration
class InputFieldSortingNameWithinDeclSortedNamesChunkViolationSliceViolation {
	final int a, b = 1;
}
// === end ===

// === case: name_within_declaration_trailing_comment ===
class InputFieldSortingNameWithinDeclarationTrailingCommentSliceViolation {
	String alpha, zebra; // keep me
}
// === end ===

// === case: name_within_declaration_unsorted ===
class InputFieldSortingNameWithinDeclarationUnsortedSliceViolation {
	String alpha, zebra;
}
// === end ===

// === case: name_within_declaration_wraps ===
class InputFieldSortingNameWithinDeclarationWrapsSliceViolation {
	String wrapAlphaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			wrapZebraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
}
// === end ===

// === case: nested_parens_in_args ===
enum InputFieldSortingNestedParensInArgsSliceViolation {
	A(3),
	B(foo(1, 2))
}
// === end ===

// === case: record_static_field_name_order ===
record InputFieldSortingRecordStaticFieldNameOrderSliceViolation() {
	static int a = 1;
	static int b = 2;
}
// === end ===

// === case: reorder_constant_named_like_token ===
enum InputFieldSortingReorderConstantNamedLikeTokenSliceViolation {
	IDENT,
	NUMBER
}
// === end ===

// === case: reorder_two_constants ===
enum InputFieldSortingReorderTwoConstantsSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: reorder_with_annotation_block_comment_parens ===
enum InputFieldSortingReorderWithAnnotationBlockCommentParensSliceViolation {
	ALPHA,
	@SuppressWarnings(/* ( */ "x")
	BETA
}
// === end ===

// === case: reorder_with_annotation_char_literal_parens ===
enum InputFieldSortingReorderWithAnnotationCharLiteralParensSliceViolation {
	ALPHA,
	@TAnnParam('(')
	BETA
}
// === end ===

// === case: reorder_with_annotation_comment_parens ===
enum InputFieldSortingReorderWithAnnotationCommentParensSliceViolation {
	ALPHA,
	@Deprecated // has ( here
	BETA
}
// === end ===

// === case: reorder_with_annotation_multi_line ===
enum InputFieldSortingReorderWithAnnotationMultiLineSliceViolation {
	ALPHA,
	@SuppressWarnings(
			"unchecked"
	)
	BETA
}
// === end ===

// === case: reorder_with_annotation_multi_line_deep ===
enum InputFieldSortingReorderWithAnnotationMultiLineDeepSliceViolation {
	ALPHA,
	@SuppressWarnings(
			{
					"unchecked",
					"unused"
			}
	)
	BETA
}
// === end ===

// === case: reorder_with_annotation_string_parens ===
enum InputFieldSortingReorderWithAnnotationStringParensSliceViolation {
	ALPHA,
	@SuppressWarnings("(((")
	BETA
}
// === end ===

// === case: reorder_with_annotations ===
enum InputFieldSortingReorderWithAnnotationsSliceViolation {
	ALPHA,
	@Deprecated
	BETA
}
// === end ===

// === case: reorder_with_arguments ===
enum InputFieldSortingReorderWithArgumentsSliceViolation {
	APPLE("green"),
	CHERRY("red")
}
// === end ===

// === case: reorder_with_block_comment ===
enum InputFieldSortingReorderWithBlockCommentSliceViolation {
	ALPHA,
	/*
	 * ZEBRA docs
	 */
	ZEBRA
}
// === end ===

// === case: reorder_with_bodies ===
enum InputFieldSortingReorderWithBodiesSliceViolation {
	ADD {
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	},
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	};
	abstract int apply(int a, int b);
}
// === end ===

// === case: reorder_with_comments ===
enum InputFieldSortingReorderWithCommentsSliceViolation {
	// a
	ALPHA,
	// z
	ZEBRA
}
// === end ===

// === case: reorder_with_javadoc ===
enum InputFieldSortingReorderWithJavadocSliceViolation {
	/** ALPHA constant. */
	ALPHA,
	/** ZEBRA constant. */
	ZEBRA
}
// === end ===

// === case: reorder_with_semicolon_and_trailing_comments ===
enum InputFieldSortingReorderWithSemicolonAndTrailingCommentsSliceViolation {
	ALPHA, // a
	BETA; // b
	int x;
}
// === end ===

// === case: reorder_with_semicolon_on_last ===
enum InputFieldSortingReorderWithSemicolonOnLastSliceViolation {
	ALPHA,
	BETA;
	int x;
}
// === end ===

// === case: reorder_with_trailing_comment_escaped_quote ===
enum InputFieldSortingReorderWithTrailingCommentEscapedQuoteSliceViolation {
	A("x"),
	B("test\\") // note
}
// === end ===

// === case: reorder_with_trailing_comments ===
enum InputFieldSortingReorderWithTrailingCommentsSliceViolation {
	APPLE, // fruit
	BANANA, // fruit
	CHERRY // fruit
}
// === end ===

// === case: reorder_with_url_in_string_arg ===
enum InputFieldSortingReorderWithUrlInStringArgSliceViolation {
	A("y"),
	B("http://example.com")
}
// === end ===

// === case: same_line_and_reorder ===
enum InputFieldSortingSameLineAndReorderSliceViolation {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: same_line_tab_separated ===
enum InputFieldSortingSameLineTabSeparatedSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: same_line_three_constants ===
enum InputFieldSortingSameLineThreeConstantsSliceViolation {
	ALPHA,
	BETA,
	GAMMA
}
// === end ===

// === case: same_line_two_constants ===
enum InputFieldSortingSameLineTwoConstantsSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: same_line_two_with_args ===
enum InputFieldSortingSameLineTwoWithArgsSliceViolation {
	APPLE("red"),
	BANANA("yellow")
}
// === end ===

// === case: same_line_two_with_comma_in_args ===
enum InputFieldSortingSameLineTwoWithCommaInArgsSliceViolation {
	A(3),
	B(1, 2)
}
// === end ===

// === case: same_line_two_with_comma_in_char ===
enum InputFieldSortingSameLineTwoWithCommaInCharSliceViolation {
	A('.'),
	B(',')
}
// === end ===

// === case: same_line_two_with_comma_in_string ===
enum InputFieldSortingSameLineTwoWithCommaInStringSliceViolation {
	A("z"),
	B("x, y")
}
// === end ===

// === case: same_line_with_semicolon ===
enum InputFieldSortingSameLineWithSemicolonSliceViolation {
	ALPHA,
	BETA;
	int x;
}
// === end ===

// === case: type_enum_constant_body ===
enum InputFieldSortingTypeEnumConstantBodySliceViolation {
	INSTANCE {
		int count;
		String name;
	}
}
// === end ===

// === case: type_enum_constant_body_comment_on_opener ===
enum InputFieldSortingTypeEnumConstantBodyCommentOnOpenerSliceViolation {
	INSTANCE { // enum constant body opener
		int count;
		String name;
	}
}
// === end ===

// === case: type_enum_constant_body_single_letter_name ===
enum InputFieldSortingTypeEnumConstantBodySingleLetterNameSliceViolation {
	A {
		int count;
		String name;
	}
}
// === end ===

// === case: type_enum_constant_body_with_args ===
enum InputFieldSortingTypeEnumConstantBodyWithArgsSliceViolation {
	INSTANCE("arg") {
		int count;
		String name;
	};

	final String label;

	InputFieldSortingTypeEnumConstantBodyWithArgsSliceViolation(String label) {
		this.label = label;
	}
}
// === end ===

// === case: type_int_after_string ===
class InputFieldSortingTypeIntAfterStringSliceViolation {
	final int count = 0;
	final String name = "x";
}
// === end ===

// === case: typeargannotation_annotated_before_unannotated ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationAnnotatedBeforeUnannotatedSliceViolation {
	List<String> plain;
	List<@TAnnA String> annotated;
}
// === end ===

// === case: typeargannotation_empty_parens ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationEmptyParensSliceViolation {
	List<@TAnnA() String> aField;
	List<@TAnnB() String> bField;
}
// === end ===

// === case: typeargannotation_explicit_value_keyword ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationExplicitValueKeywordSliceViolation {
	List<@TAnnParam(value = 1) String> lower;
	List<@TAnnParam(value = 2) String> higher;
}
// === end ===

// === case: typeargannotation_lower_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationLowerBoundSliceViolation {
	List<? super @TAnnA Number> aField;
	List<? super @TAnnB Number> bField;
}
// === end ===

// === case: typeargannotation_more_before_fewer ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationMoreBeforeFewerSliceViolation {
	List<@TAnnA String> oneAnn;
	List<@TAnnA @TAnnB String> twoAnns;
}
// === end ===

// === case: typeargannotation_parameterized ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationParameterizedSliceViolation {
	List<@TAnnParam(1) String> lower;
	List<@TAnnParam(2) String> higher;
}
// === end ===

// === case: typeargannotation_position_aware ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationPositionAwareSliceViolation {
	Map<String, @TAnnA String> firstArgUnannotated;
	Map<@TAnnA String, String> firstArgAnnotated;
}
// === end ===

// === case: typeargannotation_qualified ===
@SuppressWarnings("PreferImport")
class InputFieldSortingTypeArgAnnotationQualifiedSliceViolation {
	java.util.Set<String> plain;
	java.util.Set<@TAnnA String> annotated;
}
// === end ===

// === case: typeargannotation_same_annotations_falls_to_name ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationSameAnnotationsFallsToNameSliceViolation {
	@Deprecated
	List<@TAnnA String> alpha = List.of();
	@Deprecated
	List<@TAnnA String> zebra = List.of();
}
// === end ===

// === case: typeargannotation_second_arg_annotated ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationSecondArgAnnotatedSliceViolation {
	Map<String, Integer> plain;
	Map<String, @TAnnA Integer> annotated;
}
// === end ===

// === case: typeargannotation_wildcard ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardSliceViolation {
	List<@TAnnA ? extends Number> aField;
	List<@TAnnB ? extends Number> bField;
}
// === end ===

// === case: typeargannotation_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardBoundSliceViolation {
	List<? extends @TAnnA Number> aField;
	List<? extends @TAnnB Number> bField;
}
// === end ===

// === case: typeargannotation_wrong_order ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWrongOrderSliceViolation {
	List<@TAnnA String> aField;
	List<@TAnnB String> bField;
}
// === end ===