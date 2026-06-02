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
	@Deprecated
	String annotated;
	String plain; // violation: Field 'plain' (unannotated) must appear before 'annotated' (annotated @Deprecated), same type.
}
// === end ===

// === case: annotation_array_value_close_brace_own_line ===
class InputFieldSortingAnnotationArrayValueCloseBraceOwnLineSliceViolation {
	@SuppressWarnings({
			"unused",
			"rawtypes"
	})
	int zebra;
	int alpha; // violation: Field 'alpha' (unannotated) must appear before 'zebra' (annotated @SuppressWarnings), same type.
}
// === end ===

// === case: annotation_explicit_value_keyword_normalization ===
class InputFieldSortingAnnotationExplicitValueKeywordNormalizationSliceViolation {
	@SuppressWarnings(value = "unused")
	int beta = 1;
	@SuppressWarnings(value = "unused") // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
	int alpha = 2;
}
// === end ===

// === case: annotation_multi_annotation ===
class InputFieldSortingAnnotationMultiAnnotationSliceViolation {
	@BnnV
	String bField;
	@AnnV // violation: Field 'abField' (annotated @AnnV) must appear before 'bField' (annotated @BnnV), same type.
	@BnnV
	String abField;
}
// === end ===

// === case: annotation_multi_line_close_trailing_constants ===
enum InputFieldSortingAnnotationMultiLineCloseTrailingConstantsSliceViolation {
	@SuppressWarnings(
		"unchecked"
	) BETA, ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: annotation_qualified ===
class InputFieldSortingAnnotationQualifiedSliceViolation {
	@java.lang.SuppressWarnings("unused")
	int beta;
	@java.lang.Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	int alpha;
}
// === end ===

// === case: annotation_same_annotation_name ===
class InputFieldSortingAnnotationSameAnnotationNameSliceViolation {
	@Deprecated
	String zebra;

	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha;
}
// === end ===

// === case: annotation_same_line_trailing_constants ===
enum InputFieldSortingAnnotationSameLineTrailingConstantsSliceViolation {
	@Deprecated BETA, ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: annotation_static ===
// skip-reason: cannot safely reorder static fields with annotations
class InputFieldSortingAnnotationStaticSliceViolation {
	@SuppressWarnings("unused")
	static String beta;
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	static String alpha;
}
// === end ===

// === case: annotation_wrong_annotation_order ===
class InputFieldSortingAnnotationWrongAnnotationOrderSliceViolation {
	@SuppressWarnings("unused")
	int beta;
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	int alpha;
}
// === end ===

// === case: anonclass_anon_before_field_by_type ===
class InputFieldSortingAnonClassAnonBeforeFieldByTypeSliceViolation {
	final Object data = "x";
	final Runnable action = new Runnable() { // violation: Field 'action' with anonymous class initializer must appear before 'data'.
		@Override
		public void run() {
			System.out.println("hi");
		}
	};
}
// === end ===

// === case: anonclass_anon_before_field_by_type_redundant ===
class InputFieldSortingAnonClassAnonBeforeFieldByTypeRedundantSliceViolation {
	final Runnable data = () -> System.out.println("x");
	final Comparable<Integer> action = new Comparable<>() { // violation: Field 'action' with anonymous class initializer must appear before 'data'.
		@Override
		public int compareTo(Integer o) {
			return 0;
		}
	};
}
// === end ===

// === case: anonclass_anon_initializer_after_field ===
// skip-reason: cannot reorder anonymous-class initializer field across a field it references
// imports: java.util.HashMap
// imports: java.util.Map
class InputFieldSortingAnonClassAnonInitializerAfterFieldSliceViolation {
	final Map<String, Object> data = new HashMap<>();
	final Runnable action = new Runnable() { // violation: Field 'action' with anonymous class initializer must appear before 'data'.
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
	final Object action = new HashMap<String, List<Integer>>() { // violation: Field 'action' with anonymous class initializer must appear before 'data'.
		@Override
		public void clear() {
			data.clear();
		}
	};
}
// === end ===

// === case: anonclass_lambda_forward_reference ===
class InputFieldSortingAnonClassLambdaForwardReferenceSliceViolation {
	final Runnable action = () -> System.out.println(name); // violation: Field 'action' references 'name' which should be declared before it.
	final String name = "test";
}
// === end ===

// === case: anonclass_pattern_in_string_initializer ===
class InputFieldSortingAnonClassPatternInStringInitializerSliceViolation {
	final String zebra = "new Runnable() { run(); }";
	final String alpha = "hello"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_c_style_mixed_style_reorders ===
class InputFieldSortingArrayCStyleMixedStyleReordersSliceViolation {
	int[] zebra;
	int alpha[]; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_c_style_plain_sibling_reorders ===
class InputFieldSortingArrayCStylePlainSiblingReordersSliceViolation {
	int elements[];
	int codes; // violation: Field 'codes' (type 'int') must appear before 'elements' (type 'int[]').
}
// === end ===

// === case: array_c_style_reorders ===
class InputFieldSortingArrayCStyleReordersSliceViolation {
	int zebra[];
	int alpha[]; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_c_style_secondary_bracket_self_rebuild ===
class InputFieldSortingArrayCStyleSecondaryBracketSelfRebuildSliceViolation {
	String beta;
	int alpha, zebra[]; // violation: Field 'alpha' (type 'int') must appear before 'beta' (type 'String').
}
// === end ===

// === case: array_c_style_secondary_name_skips ===
// skip-reason: cannot reorder a field past a name bound in a multi-variable declaration
class InputFieldSortingArrayCStyleSecondaryNameSkipsSliceViolation {
	int alpha, zebra[];
	int beta; // violation: Field 'beta' (type 'int') must appear before 'zebra' (type 'int[]').
}
// === end ===

// === case: array_c_style_two_dimensional_reorders ===
class InputFieldSortingArrayCStyleTwoDimensionalReordersSliceViolation {
	int zebra[][];
	int alpha[][]; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_c_style_whitespace_inside_brackets ===
class InputFieldSortingArrayCStyleWhitespaceInsideBracketsSliceViolation {
	int zebra[ ];
	int alpha; // violation: Field 'alpha' (type 'int') must appear before 'zebra' (type 'int[]').
}
// === end ===

// === case: array_c_style_with_string_group_consolidation ===
class InputFieldSortingArrayCStyleWithStringGroupConsolidationSliceViolation {
	int elements[];
	String zebra;
	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_distinct_element_types ===
class InputFieldSortingArrayDistinctElementTypesSliceViolation {
	String[] names;
	int[] codes; // violation: Field 'codes' (type 'int[]') must appear before 'names' (type 'String[]').
}
// === end ===

// === case: array_int_after_string ===
class InputFieldSortingArrayIntAfterStringSliceViolation {
	int[] arr;
	String name;
	int plain; // violation: Field 'plain' (type 'int') must appear before 'name' (type 'String').
}
// === end ===

// === case: array_mixed_bracket_style_multi_var_skips ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingArrayMixedBracketStyleMultiVarSkipsSliceViolation {
	private String[] y[], x; // violation: Field 'x' (type 'String[]') must appear before 'y' (type 'String[][]').
}
// === end ===

// === case: array_mixed_bracket_style_single_declarator ===
class InputFieldSortingArrayMixedBracketStyleSingleDeclaratorSliceViolation {
	String[] zebra[];
	String[] alpha[]; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_multi_var_reorders ===
class InputFieldSortingArrayMultiVarReordersSliceViolation {
	int[] zebra, alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: array_multidim_and_scalar ===
class InputFieldSortingArrayMultidimAndScalarSliceViolation {
	int[][] matrix;
	int[] vector; // violation: Field 'vector' (type 'int[]') must appear before 'matrix' (type 'int[][]').
	int scalar;   // violation: Field 'scalar' (type 'int') must appear before 'vector' (type 'int[]').
}
// === end ===

// === case: array_primitive_after_array ===
class InputFieldSortingArrayPrimitiveAfterArraySliceViolation {
	double[] values;
	char letter; // violation: Field 'letter' (type 'char') must appear before 'values' (type 'double[]').
}
// === end ===

// === case: case_insensitive_sort ===
enum InputFieldSortingCaseInsensitiveSortSliceViolation {
	beta,
	Alpha // violation: Enum constant 'Alpha' must appear before 'beta' (alphabetical order).
}
// === end ===

// === case: chunk_mixed_chunks ===
class InputFieldSortingChunkMixedChunksSliceViolation {
	int nonFinal;
	final int finalNoValue; // violation: Field 'finalNoValue' (final without inline value) must appear before non-final fields.
	final int finalWithValue = 1; // violation: Field 'finalWithValue' (final with inline value) must appear before final without inline value fields.

	InputFieldSortingChunkMixedChunksSliceViolation(int value) {
		this.finalNoValue = value;
	}
}
// === end ===

// === case: dependency_alpha_after_dep_chain ===
class InputFieldSortingDependencyAlphaAfterDepChainSliceViolation {
	static final int BASE = 10;
	static final int DERIVED = BASE + 1;
	static final int ALPHA = 5; // violation: Field 'ALPHA' must appear before 'DERIVED' (alphabetical order, same type).
}
// === end ===

// === case: dependency_forward_ref ===
class InputFieldSortingDependencyForwardRefSliceViolation {
	int beta = this.alpha + 1; // violation: Field 'beta' references 'alpha' which should be declared before it.
	int alpha = 10;
}
// === end ===

// === case: enumconstant_annotated_after_previous ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousSliceViolation {
	ZETA, @Deprecated ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumconstant_annotated_after_previous_comment ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousCommentSliceViolation {
	ZETA, /* mid */ @Deprecated ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumconstant_annotated_same_line ===
enum InputFieldSortingEnumConstantAnnotatedSameLineSliceViolation {
	@Deprecated ZETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_annotated_same_line_leading_comment ===
enum InputFieldSortingEnumConstantAnnotatedSameLineLeadingCommentSliceViolation {
	/* keep */ @Deprecated ZETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_basic ===
enum InputFieldSortingEnumConstantBasicSliceViolation {
	ZEBRA,
	ALPHA; // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).

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
		String docs = """
				contains ; and } fake terminators
				second line
				""";
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	}
}
// === end ===

// === case: enumconstant_inner ===
class InputFieldSortingEnumConstantInnerSliceViolation {
	enum Misordered {
		SECOND,
		FIRST // violation: Enum constant 'FIRST' must appear before 'SECOND' (alphabetical order).
	}
}
// === end ===

// === case: enumconstant_multiple ===
enum InputFieldSortingEnumConstantMultipleSliceViolation {
	CHARLIE,
	BRAVO, // violation: Enum constant 'BRAVO' must appear before 'CHARLIE' (alphabetical order).
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BRAVO' (alphabetical order).
}
// === end ===

// === case: enumconstant_outer_enum ===
enum InputFieldSortingEnumConstantOuterEnumSliceViolation {
	ALPHA,
	BETA;

	enum InnerMisordered {
		YELLOW,
		XENON // violation: Enum constant 'XENON' must appear before 'YELLOW' (alphabetical order).
	}
}
// === end ===

// === case: enumconstant_single_then_fields_reorder ===
enum InputFieldSortingEnumConstantSingleThenFieldsReorderSliceViolation {
	A;
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: enumconstant_with_bodies ===
enum InputFieldSortingEnumConstantWithBodiesSliceViolation {
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	},
	ADD { // violation: Enum constant 'ADD' must appear before 'SUBTRACT' (alphabetical order).
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	};

	abstract int apply(int a, int b);
}
// === end ===

// === case: enumconstant_with_members ===
enum InputFieldSortingEnumConstantWithMembersSliceViolation {
	CHERRY("red"),
	BANANA("yellow"), // violation: Enum constant 'BANANA' must appear before 'CHERRY' (alphabetical order).
	APPLE("green"); // violation: Enum constant 'APPLE' must appear before 'BANANA' (alphabetical order).

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
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings(value = "unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: enumkeyword_in_block_comment ===
/*
 * Doc paragraph mentioning enum constants
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentSliceViolation {
	final int beta = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: enumkeyword_in_block_comment_with_braces ===
/*
 * Doc paragraph with example: { not real brace } and another { } pair
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentWithBracesSliceViolation {
	final int beta = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: enumkeyword_in_string_initializer ===
class InputFieldSortingEnumKeywordInStringInitializerSliceViolation {
	final String docs = "enum constants list";
	final String alpha = "one"; // violation: Field 'alpha' must appear before 'docs' (alphabetical order, same type).
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
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: enumkeyword_in_text_block_above_class ===
class InputFieldSortingEnumKeywordInTextBlockAboveClassSliceViolation {
	static final String docs = """
			line one
			describes enum constants list
			more lines
			""";
	final int beta = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
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
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
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
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
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
	final int beta = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: enumkeyword_sibling_enum_lookback_boundary ===
enum InputFieldSortingEnumKeywordSiblingEnumA {
	A
}

class InputFieldSortingEnumKeywordSiblingEnumLookbackBoundarySliceViolation {
	final int beta = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: enumsameline_inner ===
class InputFieldSortingEnumSameLineInnerSliceViolation {
	enum Inner {
		ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
	}
}
// === end ===

// === case: enumsameline_interior_block_comment ===
enum InputFieldSortingEnumSameLineInteriorBlockCommentSliceViolation {
	ALPHA, /* mid */ BETA, GAMMA // violation: Enum constant 'BETA' must be on its own line. // violation: Enum constant 'GAMMA' must be on its own line.
}
// === end ===

// === case: enumsameline_text_block_arg ===
enum InputFieldSortingEnumSameLineTextBlockArgSliceViolation {
	ALPHA("""x"""), BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumsameline_trailing_block_comment_fake_comma ===
enum InputFieldSortingEnumSameLineTrailingBlockCommentFakeCommaSliceViolation {
	ALPHA, BETA; /* fake , Z */ // violation: Enum constant 'BETA' must be on its own line.

	static final int VAL = 1;
}
// === end ===

// === case: enumsameline_whole_enum_one_line ===
enum InputFieldSortingEnumSameLineWholeEnumOneLineSliceViolation { ALPHA, BETA } // violation: Enum constant 'BETA' must be on its own line.
// === end ===

// === case: enumsameline_with_body_brace_depth ===
enum InputFieldSortingEnumSameLineWithBodyBraceDepthSliceViolation {
	BETA { void foo() {} }, ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumspan_annotation_arg_array_value ===
enum InputFieldSortingEnumSpanAnnotationArgArrayValueSliceViolation {
	ZETA,
	@SuppressWarnings({"a", "b"}) ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	ZETA,
	@Deprecated() ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	ZETA,
	@SuppressWarnings(value = "unused") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSpanAnnotationArgStructuralCharsSliceViolation {
	ZETA,
	@SuppressWarnings("{ } , // /*") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_blank_line_between_constants ===
enum InputFieldSortingEnumSpanBlankLineBetweenConstantsSliceViolation {
	ZETA,

	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_brace_close_shared ===
enum InputFieldSortingEnumSpanBraceCloseSharedSliceViolation {
	ALPHA, BETA } // violation: Enum constant 'BETA' must be on its own line.
// === end ===

// === case: enumspan_brace_open_shared ===
enum InputFieldSortingEnumSpanBraceOpenSharedSliceViolation { BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_brace_shared_with_semi_body ===
enum InputFieldSortingEnumSpanBraceSharedWithSemiBodySliceViolation { ALPHA, BETA; // violation: Enum constant 'BETA' must be on its own line.

	static final int VAL = 1;
}
// === end ===

// === case: enumspan_code_suffix_text_block ===
enum InputFieldSortingEnumSpanCodeSuffixTextBlockSliceViolation {
	ZETA,
	ALPHA; String d = """
			x
			"""; // violation@opener: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_before_constant_ident ===
enum InputFieldSortingEnumSpanCommentBeforeConstantIdentSliceViolation {
	ZETA,
	/* c */ ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_before_separator ===
enum InputFieldSortingEnumSpanCommentBeforeSeparatorSliceViolation {
	ZETA /* t */,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSpanCommentBetweenAnnotationAndConstantSliceViolation {
	ZETA,
	@Deprecated // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	// note
	ALPHA
}
// === end ===

// === case: enumspan_comment_between_stacked_annotations ===
enum InputFieldSortingEnumSpanCommentBetweenStackedAnnotationsSliceViolation {
	ZETA,
	@Deprecated /* x */ @SuppressWarnings("y") // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	ALPHA
}
// === end ===

// === case: enumspan_comment_block_trailer ===
enum InputFieldSortingEnumSpanCommentBlockTrailerSliceViolation {
	ZETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	// tail
}
// === end ===

// === case: enumspan_comment_in_argument_list ===
enum InputFieldSortingEnumSpanCommentInArgumentListSliceViolation {
	ZETA(1),
	ALPHA(/* c */ 2); // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).

	private final int value;

	InputFieldSortingEnumSpanCommentInArgumentListSliceViolation(int value) {
		this.value = value;
	}
}
// === end ===

// === case: enumspan_comment_in_constant_body ===
enum InputFieldSortingEnumSpanCommentInConstantBodySliceViolation {
	ZETA {
		// z note
	},
	ALPHA { // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
		// a note
	}
}
// === end ===

// === case: enumspan_comment_open_after_comma ===
// skip-reason: cannot reorder enum constants across a comment that does not close on the line it opens
enum InputFieldSortingEnumSpanCommentOpenAfterCommaSliceViolation {
	ZETA, /* ZETA is legacy;
	see #123 */
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_open_after_terminator ===
// skip-reason: cannot reorder enum constants across a comment that does not close on the line it opens
enum InputFieldSortingEnumSpanCommentOpenAfterTerminatorSliceViolation {
	ZETA,
	ALPHA, // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
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
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_straddles_comma ===
enum InputFieldSortingEnumSpanCommentStraddlesCommaSliceViolation {
	ZETA /*a*/, /*b*/ ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumspan_escaped_triple_quote ===
enum InputFieldSortingEnumSpanEscapedTripleQuoteSliceViolation {
	ZETA,
	@SuppressWarnings("\"\"\"") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_generic_implements_header ===
enum InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation implements Comparable<InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation> {
	ZETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_header_line_comment ===
enum InputFieldSortingEnumSpanHeaderLineCommentSliceViolation { // note
	ZETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_javadoc_multiline ===
enum InputFieldSortingEnumSpanJavadocMultilineSliceViolation {
	/**
	 * zeta.
	 */
	ZETA,
	/**
	 * alpha.
	 */
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_lead_both_owners ===
enum InputFieldSortingEnumSpanLeadBothOwnersSliceViolation {
	// note-z
	ZETA,
	// note-a
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_lead_first_sorts_last ===
enum InputFieldSortingEnumSpanLeadFirstSortsLastSliceViolation {
	// note
	ZETA, ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumspan_lead_first_stays_first ===
enum InputFieldSortingEnumSpanLeadFirstStaysFirstSliceViolation {
	// note
	ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumspan_list_tail_block_comment_multiline ===
enum InputFieldSortingEnumSpanListTailBlockCommentMultilineSliceViolation {
	ZOMBIE,
	ACTIVE // violation: Enum constant 'ACTIVE' must appear before 'ZOMBIE' (alphabetical order).
	/* line1

	   line3 */
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSpanListTailCommentNoTrailingCommaSliceViolation {
	ZOMBIE,
	ACTIVE // violation: Enum constant 'ACTIVE' must appear before 'ZOMBIE' (alphabetical order).
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_own_line ===
enum InputFieldSortingEnumSpanListTailCommentOwnLineSliceViolation {
	ZOMBIE,
	ACTIVE, // violation: Enum constant 'ACTIVE' must appear before 'ZOMBIE' (alphabetical order).
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_member_shares_terminator_line ===
class InputFieldSortingEnumSpanMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ZETA,
		ALPHA; static final int VAL = 1; // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	}
}
// === end ===

// === case: enumspan_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSpanMultilineAnnotationFirstLineSharedSliceViolation {
	BETA, @SuppressWarnings( // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
			"x")
	ALPHA
}
// === end ===

// === case: enumspan_nested_type_after_terminator ===
enum InputFieldSortingEnumSpanNestedTypeAfterTerminatorSliceViolation {
	ZETA,
	ALPHA; // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).

	interface Marker {
	}
}
// === end ===

// === case: enumspan_non_canonical_indent ===
enum InputFieldSortingEnumSpanNonCanonicalIndentSliceViolation {
			ZETA,
			ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_acb ===
enum InputFieldSortingEnumSpanPermAcbSliceViolation {
	// L-A
	AAA, // T-A
	// L-C
	CCC, // T-C
	// L-B
	BBB // T-B // violation: Enum constant 'BBB' must appear before 'CCC' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_bac ===
enum InputFieldSortingEnumSpanPermBacSliceViolation {
	// L-B
	BBB, // T-B
	// L-A
	AAA, // T-A // violation: Enum constant 'AAA' must appear before 'BBB' (alphabetical order).
	// L-C
	CCC // T-C
}
// === end ===

// === case: enumspan_perm_bca ===
enum InputFieldSortingEnumSpanPermBcaSliceViolation {
	// L-B
	BBB, // T-B
	// L-C
	CCC, // T-C
	// L-A
	AAA // T-A // violation: Enum constant 'AAA' must appear before 'CCC' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_cab ===
enum InputFieldSortingEnumSpanPermCabSliceViolation {
	// L-C
	CCC, // T-C
	// L-A
	AAA, // T-A // violation: Enum constant 'AAA' must appear before 'CCC' (alphabetical order).
	// L-B
	BBB // T-B
}
// === end ===

// === case: enumspan_perm_cba ===
// multi-fix-expected
enum InputFieldSortingEnumSpanPermCbaSliceViolation {
	// L-C
	CCC, // T-C
	// L-B
	BBB, // T-B // violation: Enum constant 'BBB' must appear before 'CCC' (alphabetical order).
	// L-A
	AAA // T-A // violation: Enum constant 'AAA' must appear before 'BBB' (alphabetical order).
}
// === end ===

// === case: enumspan_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSpanStackedAnnotationsAfterPreviousSliceViolation {
	ZETA, /* mid */ @Deprecated @SuppressWarnings("x") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: enumspan_string_delimiter_before_annotation ===
enum InputFieldSortingEnumSpanStringDelimiterBeforeAnnotationSliceViolation {
	@SuppressWarnings(
			"unchecked") @Deprecated
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_string_with_block_comment_marker ===
enum InputFieldSortingEnumSpanStringWithBlockCommentMarkerSliceViolation {
	ZETA,
	@SuppressWarnings("/* not a comment */") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_suffix_unclosed_comment_after_brace ===
enum InputFieldSortingEnumSpanSuffixUnclosedCommentAfterBraceSliceViolation {
	ZETA,
	ALPHA, // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	BETA } /*
still open
*/
// === end ===

// === case: enumspan_supplementary_annotation_arg ===
enum InputFieldSortingEnumSpanSupplementaryAnnotationArgSliceViolation {
	ZETA,
	@SuppressWarnings("𝐀") ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_supplementary_constant_name ===
enum InputFieldSortingEnumSpanSupplementaryConstantNameSliceViolation {
	ZETA,
	ALPHA𝐀 // violation: Enum constant 'ALPHA𝐀' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_text_block_arg_multiline ===
enum InputFieldSortingEnumSpanTextBlockArgMultilineSliceViolation {
	ZETA("""
			x
			"""),
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_text_block_in_constant_body ===
enum InputFieldSortingEnumSpanTextBlockInConstantBodySliceViolation {
	ZETA {
		String v() {
			return """
					z""";
		}
	},
	ALPHA { // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
		String v() {
			return """
					a""";
		}
	}
}
// === end ===

// === case: enumspan_trailing_comma ===
enum InputFieldSortingEnumSpanTrailingCommaSliceViolation {
	BETA,
	ALPHA, // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_trailing_comma_terminator_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommaTerminatorCommentMultilineSliceViolation {
	ZETA,
	ALPHA, // done // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	// more
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSpanTrailingCommaThenTerminatorCommentSliceViolation {
	ZETA,
	ALPHA, // done // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comment_multiline ===
// skip-reason: cannot relocate a comment trailing an enum constant across multiple lines
enum InputFieldSortingEnumSpanTrailingCommentMultilineSliceViolation {
	ZETA /* a */
	/* b */, ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: field_annotation_consolidation ===
class InputFieldSortingFieldAnnotationConsolidationSliceViolation {
	@NonNull
	final String currencyCode;
	@Nullable
	final String engName, engSymbol;
	@NonNull // violation: Field 'equityNumber' (annotated @NonNull) must appear before 'engSymbol' (annotated @Nullable), same type.
	final String equityNumber;
	@Nullable
	final String exchange;
	@NonNull // violation: Field 'source' (annotated @NonNull) must appear before 'exchange' (annotated @Nullable), same type.
	final String source;
}
// === end ===

// === case: field_annotation_consolidation_skips_trailing_comment ===
class InputFieldSortingFieldAnnotationConsolidationSkipsTrailingCommentSliceViolation {
	int width; // in pixels
	int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_empty_parens_normalization ===
class InputFieldSortingFieldAnnotationEmptyParensNormalizationSliceViolation {
	@SuppressWarnings("unused")
	String beta;
	@Deprecated() // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	String alpha;
}
// === end ===

// === case: field_annotation_ignores_at_in_block_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInBlockCommentSliceViolation {
	String /* @Nullable */ beta;
	String alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_initializer ===
class InputFieldSortingFieldAnnotationIgnoresAtInInitializerSliceViolation {
	Object beta = x > 0 ? new @TypeUse Object() : null;
	Object alpha = "hello"; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_line_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInLineCommentSliceViolation {
	String beta; // @Deprecated docs
	String alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_string ===
class InputFieldSortingFieldAnnotationIgnoresAtInStringSliceViolation {
	String beta = "@Zebra";
	String alpha = "hello"; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_text_block ===
class InputFieldSortingFieldAnnotationIgnoresAtInTextBlockSliceViolation {
	String beta = """
		@FakeAnnotation
		""";
	String alpha = "hello"; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_text_block_escaped_triple_quote ===
class InputFieldSortingFieldAnnotationIgnoresAtInTextBlockEscapedTripleQuoteSliceViolation {
	String beta = """
		line with \""" and @FakeAnnotation
		more content
		""";
	String alpha = "hello"; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldAnnotationIgnoresAtInTypeArgSliceViolation {
	@Alpha
	List<String> fieldAnnotated;
	List<@Zebra String> typeArgAnnotated; // violation: Field 'typeArgAnnotated' (unannotated) must appear before 'fieldAnnotated' (annotated @Alpha), same type.
}
// === end ===

// === case: field_annotation_ignores_at_in_type_arg_with_initializer ===
// imports: java.util.List
class InputFieldSortingFieldAnnotationIgnoresAtInTypeArgWithInitializerSliceViolation {
	List<@Zebra String> beta = List.of();
	List<String> alpha = List.of(); // violation: Field 'alpha' (type argument unannotated) must appear before 'beta' (type argument annotated @Zebra), same type.
}
// === end ===

// === case: field_annotation_multi_line ===
class InputFieldSortingFieldAnnotationMultiLineSliceViolation {
	@SuppressWarnings(
		"unused"
	)
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' (unannotated) must appear before 'beta' (annotated @SuppressWarnings), same type.
}
// === end ===

// === case: field_annotation_multi_line_block_comment_state_threaded ===
class InputFieldSortingFieldAnnotationMultiLineBlockCommentStateThreadedSliceViolation {
	@AnnV
	/* @FakeAnno1
	   @FakeAnno2 */
	@BnnV
	String zebra = "z";
	@AnnV // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	@BnnV
	String alpha = "a";
}
// === end ===

// === case: field_annotation_multi_line_with_text_block_value ===
class InputFieldSortingFieldAnnotationMultiLineWithTextBlockValueSliceViolation {
	@SuppressWarnings(
			"""
			ignored text block with parens ( ) and "quotes"
			"""
	)
	String zebra;

	@SuppressWarnings( // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
			"""
			ignored text block with parens ( ) and "quotes"
			"""
	)
	String alpha;
}
// === end ===

// === case: field_annotation_order_different_annotations ===
class InputFieldSortingFieldAnnotationOrderDifferentAnnotationsSliceViolation {
	@SuppressWarnings("unused")
	String beta;
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	String alpha;
}
// === end ===

// === case: field_annotation_order_multi_annotation ===
class InputFieldSortingFieldAnnotationOrderMultiAnnotationSliceViolation {
	@Bnn
	String bField;
	@Ann // violation: Field 'abField' (annotated @Ann) must appear before 'bField' (annotated @Bnn), same type.
	@Bnn
	String abField;
}
// === end ===

// === case: field_annotation_order_qualified ===
class InputFieldSortingFieldAnnotationOrderQualifiedSliceViolation {
	@java.lang.SuppressWarnings("unused")
	String beta;
	@java.lang.Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings), same type.
	String alpha;
}
// === end ===

// === case: field_annotation_paren_balanced_with_block_comment ===
class InputFieldSortingFieldAnnotationParenBalancedWithBlockCommentSliceViolation {
	@SuppressWarnings(/* fake ) */ "unused")
	String zebra;

	@SuppressWarnings(/* fake ) */ "unused") // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha;
}
// === end ===

// === case: field_annotation_same_annotation_name_order ===
class InputFieldSortingFieldAnnotationSameAnnotationNameOrderSliceViolation {
	@Deprecated
	String zebra;
	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha;
}
// === end ===

// === case: field_annotation_with_inline_block_comment_prefix ===
class InputFieldSortingFieldAnnotationWithInlineBlockCommentPrefixSliceViolation {
	/* keep */ @Deprecated
	String zebra;

	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha;
}
// === end ===

// === case: field_array_type_order ===
class InputFieldSortingFieldArrayTypeOrderSliceViolation {
	int[] arr;
	int x; // violation: Field 'x' (type 'int') must appear before 'arr' (type 'int[]').
}
// === end ===

// === case: field_brace_in_char_literal_between_field_and_class_open ===
class InputFieldSortingFieldBraceInCharLiteralBetweenFieldAndClassOpenSliceViolation {
	final char zebra = '{';
	final char alpha = 'a'; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_braces_in_single_line_string_between_field_and_class_open ===
class InputFieldSortingFieldBracesInSingleLineStringBetweenFieldAndClassOpenSliceViolation {
	final String zebra = "{ fake brace }";
	final String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_c_style_array_declarator_refused ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingFieldCStyleArrayDeclaratorRefusedSliceViolation {
	private int y[], x; // violation: Field 'x' (type 'int') must appear before 'y' (type 'int[]').
}
// === end ===

// === case: field_chunk_keywords_in_block_comment ===
class InputFieldSortingFieldChunkKeywordsInBlockCommentSliceViolation {
	String /* static final = */ zebra;

	String /* static final = */ alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_chunk_keywords_in_string ===
class InputFieldSortingFieldChunkKeywordsInStringSliceViolation {
	final String zebra = "static final =";
	final String alpha = "static final ="; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_chunk_order ===
class InputFieldSortingFieldChunkOrderSliceViolation {
	int nonFinal;
	final int finalWithValue = 1; // violation: Field 'finalWithValue' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: field_chunk_order_all_three_chunks ===
class InputFieldSortingFieldChunkOrderAllThreeChunksSliceViolation {
	int c;
	final int b; // violation: Field 'b' (final without inline value) must appear before non-final fields.
	final int a = 1; // violation: Field 'a' (final with inline value) must appear before final without inline value fields.
}
// === end ===

// === case: field_circular_dependency ===
// skip-reason: cannot reorder fields with a circular dependency
class InputFieldSortingFieldCircularDependencySliceViolation {
	static final int A = B + 1;
	static final int B = A + 1;
	static final int y = 0;
	static final int x = 0; // violation: Field 'x' must appear before 'y' (alphabetical order, same type).
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
	final String zebra = "z";
	final String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_block_comment_contains_double_slash ===
class InputFieldSortingFieldConsolidationBlockCommentContainsDoubleSlashSliceViolation {
	String zebra /* fake // comment */;

	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_candidate_with_block_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithBlockCommentSliceViolation {
	/* keep me */
	String zebra;

	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_candidate_with_javadoc ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithJavadocSliceViolation {
	/** keep me */
	String zebra;

	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_candidate_with_line_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithLineCommentSliceViolation {
	// keep me
	String zebra;

	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_different_transient ===
class InputFieldSortingFieldConsolidationSkipsDifferentTransientSliceViolation {
	transient int zebra;
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_different_visibility ===
class InputFieldSortingFieldConsolidationSkipsDifferentVisibilitySliceViolation {
	int zebra;
	private int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_wraps_one_name_per_line ===
class InputFieldSortingFieldConsolidationWrapsOneNamePerLineSliceViolation {
	final String nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaC;
	final String nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaB; // violation: Field 'nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaB' must appear before 'nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaC' (alphabetical order, same type).
	final String nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaA; // violation: Field 'nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaA' must appear before 'nameAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaB' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_wraps_when_merged_too_long ===
class InputFieldSortingFieldConsolidationWrapsWhenMergedTooLongSliceViolation {
	final String veryLongNameGammaaaaaaaaa;
	final String veryLongNameEpsilonaaaaaa; // violation: Field 'veryLongNameEpsilonaaaaaa' must appear before 'veryLongNameGammaaaaaaaaa' (alphabetical order, same type).
	final String veryLongNameDeltaaaaaaaaa; // violation: Field 'veryLongNameDeltaaaaaaaaa' must appear before 'veryLongNameEpsilonaaaaaa' (alphabetical order, same type).
	final String veryLongNameBetaaaaaaaaaa; // violation: Field 'veryLongNameBetaaaaaaaaaa' must appear before 'veryLongNameDeltaaaaaaaaa' (alphabetical order, same type).
	final String veryLongNameAlphaaaaaaaaa; // violation: Field 'veryLongNameAlphaaaaaaaaa' must appear before 'veryLongNameBetaaaaaaaaaa' (alphabetical order, same type).
}
// === end ===

// === case: field_dependency_initializer_on_continuation_line ===
class InputFieldSortingFieldDependencyInitializerOnContinuationLineSliceViolation {
	int beta = 1 // violation: Field 'beta' references 'alpha' which should be declared before it.
			+ this.alpha;
	int alpha = 10;
}
// === end ===

// === case: field_dependency_order ===
class InputFieldSortingFieldDependencyOrderSliceViolation {
	static final int B = A + 1; // violation: Field 'B' references 'A' which should be declared before it.
	static final int A = 0;
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_inner_field ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesInnerFieldSliceViolation {
	int zebra = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).

	class Inner {
		int beta = 5;
	}
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_nested_local ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesNestedLocalSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	void m() {
		int charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_duplicate_text_below_does_not_steal_endidx ===
@SuppressWarnings("unused")
class InputFieldSortingFieldDuplicateTextBelowDoesNotStealEndIdxSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	void method() {
		int alpha = 2;
		System.out.println(alpha);
	}
}
// === end ===

// === case: field_extract_all_names_no_match_fallback ===
class InputFieldSortingFieldExtractAllNamesNoMatchFallbackSliceViolation {
	int zebra
	;
	int alpha = 1; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_angle_comparison ===
class InputFieldSortingFieldExtractNamesInitializerAngleComparisonSliceViolation {
	int yankee = 5 < 10 ? 1 : 0, zulu = 5;
	int alpha = 7; // violation: Field 'alpha' must appear before 'zulu' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_angle_explicit_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldExtractNamesInitializerAngleExplicitTypeArgSliceViolation {
	int yankee = List.<Integer>of().size(), zulu = 5;
	int alpha = 7; // violation: Field 'alpha' must appear before 'zulu' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_brace_comma ===
class InputFieldSortingFieldExtractNamesInitializerBraceCommaSliceViolation {
	int[] yankee = {1, 2, 3}, zulu = {4, 5};
	int[] alpha = {6}; // violation: Field 'alpha' must appear before 'zulu' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_char_comma ===
class InputFieldSortingFieldExtractNamesInitializerCharCommaSliceViolation {
	char max = ',';
	char alpha = 'a'; // violation: Field 'alpha' must appear before 'max' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_paren_comma ===
@SuppressWarnings("unused")
class InputFieldSortingFieldExtractNamesInitializerParenCommaSliceViolation {
	int max = Math.max(1, 2);
	int alpha = 7; // violation: Field 'alpha' must appear before 'max' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_string_comma ===
class InputFieldSortingFieldExtractNamesInitializerStringCommaSliceViolation {
	String max = "x,y";
	String alpha = "a"; // violation: Field 'alpha' must appear before 'max' (alphabetical order, same type).
}
// === end ===

// === case: field_inner_class_skipped ===
class InputFieldSortingFieldInnerClassSkippedSliceViolation {
	class Inner {
		int first = 1;
	}

	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_inner_interface_skipped ===
class InputFieldSortingFieldInnerInterfaceSkippedSliceViolation {
	interface Inner {
		int FIRST = 1;
	}

	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_inner_record_skipped ===
class InputFieldSortingFieldInnerRecordSkippedSliceViolation {
	record Inner(int x) {}

	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_instance_initializer_block_skipped ===
class InputFieldSortingFieldInstanceInitializerBlockSkippedSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	{
		Math.random();
	}
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields across an interleaved static or instance field
class InputFieldSortingFieldInterleavedDependencyBailSliceViolation {
	int zebra = this.alpha + 1; // violation: Field 'zebra' references 'alpha' which should be declared before it.
	static int s;
	int alpha;
}
// === end ===

// === case: field_interleaved_opposite_static_bail ===
class InputFieldSortingFieldInterleavedOppositeStaticBailSliceViolation {
	int zebra;
	static int x;
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_interleaved_opposite_static_bail_static_group ===
class InputFieldSortingFieldInterleavedOppositeStaticBailStaticGroupSliceViolation {
	static int zebra;
	int x;
	static int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_interleaved_spans_chunks_bail ===
// skip-reason: cannot reorder fields across an interleaved static or instance field
class InputFieldSortingFieldInterleavedSpansChunksBailSliceViolation {
	int alpha;
	static int s;
	final int zebra = 1; // violation: Field 'zebra' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: field_local_in_method_body_not_treated_as_field ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLocalInMethodBodyNotTreatedAsFieldSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	void method() {
		int charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_lookback_consecutive_javadoc_groups ===
class InputFieldSortingFieldLookbackConsecutiveJavadocGroupsSliceViolation {
	/**
	 * Documentation for beta.
	 * Multiple lines.
	 */
	int beta = 1;
	/**
	 * Documentation for alpha.
	 * Multiple lines.
	 */
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_lookback_does_not_pull_text_block_content ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLookbackDoesNotPullTextBlockContentSliceViolation {
	static final String docs = """
			content line one
			content line two
			""";
	final int zebra = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_lookback_past_non_annotation_parens ===
class InputFieldSortingFieldLookbackPastNonAnnotationParensSliceViolation {
	final int x = Math.max(
			1,
			2
	);
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_method_in_class_skipped ===
class InputFieldSortingFieldMethodInClassSkippedSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	void method() {
		Math.random();
	}
}
// === end ===

// === case: field_multiline_generic_type_reorder ===
// imports: java.util.Map
class InputFieldSortingFieldMultilineGenericTypeReorderSliceViolation {
	Map<String,
			Long> beta;
	Map<String, Integer> alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_multiline_inner_class_skipped ===
class InputFieldSortingFieldMultiLineInnerClassSkippedSliceViolation {
	class Inner {
		int first
				= 1;
	}

	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_multiline_inner_record_skipped ===
class InputFieldSortingFieldMultiLineInnerRecordSkippedSliceViolation {
	record Inner(
			int x,
			int y
	) {}

	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_multiline_method_in_class_skipped ===
class InputFieldSortingFieldMultiLineMethodInClassSkippedSliceViolation {
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

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
	int a = y + 1; // violation: Field 'a' must appear before 'z' (alphabetical order, same type).
}
// === end ===

// === case: field_multivar_secondary_name_dependency_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyReordersSliceViolation {
	int z = 1;
	int a = y + 1; // violation: Field 'a' must appear before 'z' (alphabetical order, same type).
	int x, y;
}
// === end ===

// === case: field_name_order ===
class InputFieldSortingFieldNameOrderSliceViolation {
	static final int Z = 1;
	static final int A = 0; // violation: Field 'A' must appear before 'Z' (alphabetical order, same type).
}
// === end ===

// === case: field_name_order_inside_anonymous_class ===
class InputFieldSortingFieldNameOrderInsideAnonymousClassSliceViolation {
	final Runnable task = new Runnable() {
		String zebra = "z";
		String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).

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
	int z;
	Map<String, List<Integer>> map;
	int a; // violation: Field 'a' (type 'int') must appear before 'map' (type 'Map').
}
// === end ===

// === case: field_read_field_end_walks_through_char_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughCharSemicolonSliceViolation {
	final char zebra = ';';
	final char alpha = 'a'; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_read_field_end_walks_through_string_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughStringSemicolonSliceViolation {
	final String zebra = "contains ; semicolon";
	final String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_startidx_rewind_to_pre_annotation_line ===
@SuppressWarnings("unused")
class InputFieldSortingFieldStartIdxRewindToPreAnnotationLineSliceViolation {
	@Deprecated
	String zebra = "z";
	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha = "a";
}
// === end ===

// === case: field_static_initializer_block_skipped ===
class InputFieldSortingFieldStaticInitializerBlockSkippedSliceViolation {
	static int beta = 1;
	static int alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).

	static {
		Math.random();
	}
}
// === end ===

// === case: field_string_with_escaped_backslashes ===
class InputFieldSortingFieldStringWithEscapedBackslashesSliceViolation {
	final String zebra = "ends with \\\\";
	final String alpha = "one"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_string_with_final_keyword ===
class InputFieldSortingFieldStringWithFinalKeywordSliceViolation {
	String b = "final";
	String a = "hello"; // violation: Field 'a' must appear before 'b' (alphabetical order, same type).
}
// === end ===

// === case: field_text_block_between_fields_in_class_body ===
class InputFieldSortingFieldTextBlockBetweenFieldsInClassBodySliceViolation {
	final String zebra = "z";
	final String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).

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
	final String docs = """
			some docs
			""";
	final String alpha = """ // violation: Field 'alpha' must appear before 'docs' (alphabetical order, same type).
			mentions docs here
			""";
}
// === end ===

// === case: field_text_block_in_initializer_walks_through ===
class InputFieldSortingFieldTextBlockInInitializerWalksThroughSliceViolation {
	final String zebra = """
			contains ; and { fake terminator
			second line
			""";
	final String alpha = "one"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_type_arg_annotation_consolidation ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationConsolidationSliceViolation {
	List<@Ann String> beta;
	List<@Ann String> alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderSliceViolation {
	List<@Bnn String> bField;
	List<@Ann String> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_annotated_before_unannotated ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderAnnotatedBeforeUnannotatedSliceViolation {
	List<@Ann String> annotated;
	List<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @Ann), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_fewer_before_more ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderFewerBeforeMoreSliceViolation {
	List<@Ann @Bnn String> twoAnns;
	List<@Ann String> oneAnn; // violation: Field 'oneAnn' (type argument annotated @Ann) must appear before 'twoAnns' (type argument annotated @Ann), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_fqn_generic ===
class InputFieldSortingFieldTypeArgAnnotationOrderFqnGenericSliceViolation {
	java.util.Set<@Bnn String> bField;
	java.util.Set<@Ann String> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_lower_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderLowerBoundSliceViolation {
	List<? super @Bnn Number> bField;
	List<? super @Ann Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_parameterized ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderParameterizedSliceViolation {
	List<@Ann(2) String> higher;
	List<@Ann(1) String> lower; // violation: Field 'lower' (type argument annotated @Ann) must appear before 'higher' (type argument annotated @Ann), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_position_aware ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationOrderPositionAwareSliceViolation {
	Map<@Ann String, String> firstArgAnnotated;
	Map<String, @Ann String> firstArgUnannotated; // violation: Field 'firstArgUnannotated' (type argument unannotated) must appear before 'firstArgAnnotated' (type argument annotated @Ann), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardSliceViolation {
	List<@Bnn ? extends Number> bField;
	List<@Ann ? extends Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn), same type.
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardBoundSliceViolation {
	List<? extends @Bnn Number> bField;
	List<? extends @Ann Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn), same type.
}
// === end ===

// === case: field_type_arg_annotation_raw_vs_parameterized ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationRawVsParameterizedSliceViolation {
	Map<@TAnnA String, String> annotated;
	Map raw; // violation: Field 'raw' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: field_type_order_byte_float_short ===
class InputFieldSortingFieldTypeOrderByteFloatShortSliceViolation {
	short code;
	float scale; // violation: Field 'scale' (type 'float') must appear before 'code' (type 'short').
	byte flags; // violation: Field 'flags' (type 'byte') must appear before 'scale' (type 'float').
}
// === end ===

// === case: field_type_order_qualified_generic_type ===
class InputFieldSortingFieldTypeOrderQualifiedGenericTypeSliceViolation {
	static class Outer {
		static class Inner<T> {
		}
	}

	Outer.Inner<String> nested;
	int count; // violation: Field 'count' (type 'int') must appear before 'nested' (type 'Outer.Inner').
}
// === end ===

// === case: inner_enum ===
class InputFieldSortingInnerEnumSliceViolation {
	enum Inner {
		BETA,
		ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
	}
}
// === end ===

// === case: interface_field_name_order ===
interface InputFieldSortingInterfaceFieldNameOrderSliceViolation {
	int SECOND = 2;
	int FIRST = 1; // violation: Field 'FIRST' must appear before 'SECOND' (alphabetical order, same type).
}
// === end ===

// === case: lexer_string_with_block_comment_marker ===
class InputFieldSortingLexerStringWithBlockCommentMarkerSliceViolation {
	final String zebra = "contains /* fake block */ inside";
	final String alpha = "one"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_alpha_after_zebra ===
class InputFieldSortingNameAlphaAfterZebraSliceViolation {
	final int zebra = 1;
	final int alpha = 2; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_order_qualified_generic_type ===
// imports: java.util.Map
class InputFieldSortingNameOrderQualifiedGenericTypeSliceViolation {
	Map.Entry<String, Integer> zebra;
	Map.Entry<String, Integer> alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_continuation_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclContinuationCommentSkipsSliceViolation {
	String zebra, // note // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
			alpha;
}
// === end ===

// === case: name_within_decl_initialized_skips ===
// skip-reason: cannot reorder initialized names in a multi-variable declaration
class InputFieldSortingNameWithinDeclInitializedSkipsSliceViolation {
	int beta = 1, alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated private int y, x; // violation: Field 'x' must appear before 'y' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated private int y[], x; // violation: Field 'x' (type 'int') must appear before 'y' (type 'int[]').
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInlineAnnotationInteriorCommentSkipsSliceViolation {
	@Deprecated private int zebra, /* keep me */ alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_inline_annotation_wrapped ===
class InputFieldSortingNameWithinDeclInlineAnnotationWrappedSliceViolation {
	@Deprecated private int y, // violation: Field 'x' must appear before 'y' (alphabetical order, same type).
			x;
}
// === end ===

// === case: name_within_decl_interior_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInteriorCommentSkipsSliceViolation {
	String zebra, /* keep me */ alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_interior_comment_with_semicolon_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInteriorCommentWithSemicolonSkipsSliceViolation {
	String zebra, alpha /* x; y */; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_multiline_annotation ===
class InputFieldSortingNameWithinDeclMultilineAnnotationSliceViolation {
	@SuppressWarnings({ // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
			"unused"
	})
	int zebra, alpha;
}
// === end ===

// === case: name_within_decl_sorted_names_c_style_prev_declarator ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclSortedNamesCStylePrevDeclaratorSliceViolation {
	int alpha[], beta; // violation: Field 'beta' (type 'int') must appear before 'alpha' (type 'int[]').
}
// === end ===

// === case: name_within_decl_sorted_names_chunk_violation ===
// skip-reason: cannot reorder initialized names in a multi-variable declaration
class InputFieldSortingNameWithinDeclSortedNamesChunkViolationSliceViolation {
	final int a, b = 1; // violation: Field 'b' (final with inline value) must appear before final without inline value fields.
}
// === end ===

// === case: name_within_declaration_trailing_comment ===
class InputFieldSortingNameWithinDeclarationTrailingCommentSliceViolation {
	String zebra, alpha; // keep me // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_declaration_unsorted ===
class InputFieldSortingNameWithinDeclarationUnsortedSliceViolation {
	String zebra, alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_declaration_wraps ===
class InputFieldSortingNameWithinDeclarationWrapsSliceViolation {
	String wrapZebraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, wrapAlphaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa; // violation: Field 'wrapAlphaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' must appear before 'wrapZebraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (alphabetical order, same type).
}
// === end ===

// === case: nested_parens_in_args ===
enum InputFieldSortingNestedParensInArgsSliceViolation {
	B(foo(1, 2)),
	A(3) // violation: Enum constant 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: record_static_field_name_order ===
record InputFieldSortingRecordStaticFieldNameOrderSliceViolation() {
	static int b = 2;
	static int a = 1; // violation: Field 'a' must appear before 'b' (alphabetical order, same type).
}
// === end ===

// === case: reorder_constant_named_like_token ===
enum InputFieldSortingReorderConstantNamedLikeTokenSliceViolation {
	NUMBER,
	IDENT // violation: Enum constant 'IDENT' must appear before 'NUMBER' (alphabetical order).
}
// === end ===

// === case: reorder_two_constants ===
enum InputFieldSortingReorderTwoConstantsSliceViolation {
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_block_comment_parens ===
enum InputFieldSortingReorderWithAnnotationBlockCommentParensSliceViolation {
	@SuppressWarnings(/* ( */ "x")
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_char_literal_parens ===
enum InputFieldSortingReorderWithAnnotationCharLiteralParensSliceViolation {
	@TAnnParam('(')
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_comment_parens ===
enum InputFieldSortingReorderWithAnnotationCommentParensSliceViolation {
	@Deprecated // has ( here
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_multi_line ===
enum InputFieldSortingReorderWithAnnotationMultiLineSliceViolation {
	@SuppressWarnings(
			"unchecked"
	)
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_multi_line_deep ===
enum InputFieldSortingReorderWithAnnotationMultiLineDeepSliceViolation {
	@SuppressWarnings(
			{
					"unchecked",
					"unused"
			}
	)
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_string_parens ===
enum InputFieldSortingReorderWithAnnotationStringParensSliceViolation {
	@SuppressWarnings("(((")
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotations ===
enum InputFieldSortingReorderWithAnnotationsSliceViolation {
	@Deprecated
	BETA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_arguments ===
enum InputFieldSortingReorderWithArgumentsSliceViolation {
	CHERRY("red"),
	APPLE("green") // violation: Enum constant 'APPLE' must appear before 'CHERRY' (alphabetical order).
}
// === end ===

// === case: reorder_with_block_comment ===
enum InputFieldSortingReorderWithBlockCommentSliceViolation {
	/*
	 * ZEBRA docs
	 */
	ZEBRA,
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: reorder_with_bodies ===
enum InputFieldSortingReorderWithBodiesSliceViolation {
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	},
	ADD { // violation: Enum constant 'ADD' must appear before 'SUBTRACT' (alphabetical order).
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	};
	abstract int apply(int a, int b);
}
// === end ===

// === case: reorder_with_comments ===
enum InputFieldSortingReorderWithCommentsSliceViolation {
	// z
	ZEBRA,
	// a
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: reorder_with_javadoc ===
enum InputFieldSortingReorderWithJavadocSliceViolation {
	/** ZEBRA constant. */
	ZEBRA,
	/** ALPHA constant. */
	ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: reorder_with_semicolon_and_trailing_comments ===
enum InputFieldSortingReorderWithSemicolonAndTrailingCommentsSliceViolation {
	BETA, // b
	ALPHA; // a // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
	int x;
}
// === end ===

// === case: reorder_with_semicolon_on_last ===
enum InputFieldSortingReorderWithSemicolonOnLastSliceViolation {
	BETA,
	ALPHA; // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order).
	int x;
}
// === end ===

// === case: reorder_with_trailing_comment_escaped_quote ===
enum InputFieldSortingReorderWithTrailingCommentEscapedQuoteSliceViolation {
	B("test\\"), // note
	A("x") // violation: Enum constant 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: reorder_with_trailing_comments ===
enum InputFieldSortingReorderWithTrailingCommentsSliceViolation {
	CHERRY, // fruit
	APPLE, // fruit // violation: Enum constant 'APPLE' must appear before 'CHERRY' (alphabetical order).
	BANANA // fruit
}
// === end ===

// === case: reorder_with_url_in_string_arg ===
enum InputFieldSortingReorderWithUrlInStringArgSliceViolation {
	B("http://example.com"),
	A("y") // violation: Enum constant 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: same_line_and_reorder ===
enum InputFieldSortingSameLineAndReorderSliceViolation {
	ZEBRA, ALPHA // violation: Enum constant 'ALPHA' must appear before 'ZEBRA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: same_line_tab_separated ===
enum InputFieldSortingSameLineTabSeparatedSliceViolation {
	BETA,	ALPHA // violation: Enum constant 'ALPHA' must appear before 'BETA' (alphabetical order). // violation: Enum constant 'ALPHA' must be on its own line.
}
// === end ===

// === case: same_line_three_constants ===
enum InputFieldSortingSameLineThreeConstantsSliceViolation {
	ALPHA, BETA, GAMMA // violation: Enum constant 'BETA' must be on its own line. // violation: Enum constant 'GAMMA' must be on its own line.
}
// === end ===

// === case: same_line_two_constants ===
enum InputFieldSortingSameLineTwoConstantsSliceViolation {
	ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: same_line_two_with_args ===
enum InputFieldSortingSameLineTwoWithArgsSliceViolation {
	APPLE("red"), BANANA("yellow") // violation: Enum constant 'BANANA' must be on its own line.
}
// === end ===

// === case: same_line_two_with_comma_in_args ===
enum InputFieldSortingSameLineTwoWithCommaInArgsSliceViolation {
	B(1, 2), A(3) // violation: Enum constant 'A' must appear before 'B' (alphabetical order). // violation: Enum constant 'A' must be on its own line.
}
// === end ===

// === case: same_line_two_with_comma_in_char ===
enum InputFieldSortingSameLineTwoWithCommaInCharSliceViolation {
	B(','), A('.') // violation: Enum constant 'A' must appear before 'B' (alphabetical order). // violation: Enum constant 'A' must be on its own line.
}
// === end ===

// === case: same_line_two_with_comma_in_string ===
enum InputFieldSortingSameLineTwoWithCommaInStringSliceViolation {
	B("x, y"), A("z") // violation: Enum constant 'A' must appear before 'B' (alphabetical order). // violation: Enum constant 'A' must be on its own line.
}
// === end ===

// === case: same_line_with_semicolon ===
enum InputFieldSortingSameLineWithSemicolonSliceViolation {
	ALPHA, BETA; // violation: Enum constant 'BETA' must be on its own line.
	int x;
}
// === end ===

// === case: type_enum_constant_body ===
enum InputFieldSortingTypeEnumConstantBodySliceViolation {
	INSTANCE {
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	}
}
// === end ===

// === case: type_enum_constant_body_comment_on_opener ===
enum InputFieldSortingTypeEnumConstantBodyCommentOnOpenerSliceViolation {
	INSTANCE { // enum constant body opener
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	}
}
// === end ===

// === case: type_enum_constant_body_single_letter_name ===
enum InputFieldSortingTypeEnumConstantBodySingleLetterNameSliceViolation {
	A {
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	}
}
// === end ===

// === case: type_enum_constant_body_with_args ===
enum InputFieldSortingTypeEnumConstantBodyWithArgsSliceViolation {
	INSTANCE("arg") {
		String name;
		int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
	};

	final String label;

	InputFieldSortingTypeEnumConstantBodyWithArgsSliceViolation(String label) {
		this.label = label;
	}
}
// === end ===

// === case: type_int_after_string ===
class InputFieldSortingTypeIntAfterStringSliceViolation {
	final String name = "x";
	final int count = 0; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
}
// === end ===

// === case: typeargannotation_annotated_before_unannotated ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationAnnotatedBeforeUnannotatedSliceViolation {
	List<@TAnnA String> annotated;
	List<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: typeargannotation_empty_parens ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationEmptyParensSliceViolation {
	List<@TAnnB() String> bField;
	List<@TAnnA() String> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}
// === end ===

// === case: typeargannotation_explicit_value_keyword ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationExplicitValueKeywordSliceViolation {
	List<@TAnnParam(value = 2) String> higher;
	List<@TAnnParam(value = 1) String> lower; // violation: Field 'lower' (type argument annotated @TAnnParam) must appear before 'higher' (type argument annotated @TAnnParam), same type.
}
// === end ===

// === case: typeargannotation_lower_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationLowerBoundSliceViolation {
	List<? super @TAnnB Number> bField;
	List<? super @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}
// === end ===

// === case: typeargannotation_more_before_fewer ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationMoreBeforeFewerSliceViolation {
	List<@TAnnA @TAnnB String> twoAnns;
	List<@TAnnA String> oneAnn; // violation: Field 'oneAnn' (type argument annotated @TAnnA) must appear before 'twoAnns' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: typeargannotation_parameterized ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationParameterizedSliceViolation {
	List<@TAnnParam(2) String> higher;
	List<@TAnnParam(1) String> lower; // violation: Field 'lower' (type argument annotated @TAnnParam) must appear before 'higher' (type argument annotated @TAnnParam), same type.
}
// === end ===

// === case: typeargannotation_position_aware ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationPositionAwareSliceViolation {
	Map<@TAnnA String, String> firstArgAnnotated;
	Map<String, @TAnnA String> firstArgUnannotated; // violation: Field 'firstArgUnannotated' (type argument unannotated) must appear before 'firstArgAnnotated' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: typeargannotation_qualified ===
@SuppressWarnings("PreferImport")
class InputFieldSortingTypeArgAnnotationQualifiedSliceViolation {
	java.util.Set<@TAnnA String> annotated;
	java.util.Set<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: typeargannotation_same_annotations_falls_to_name ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationSameAnnotationsFallsToNameSliceViolation {
	@Deprecated
	List<@TAnnA String> zebra = List.of();
	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	List<@TAnnA String> alpha = List.of();
}
// === end ===

// === case: typeargannotation_second_arg_annotated ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationSecondArgAnnotatedSliceViolation {
	Map<String, @TAnnA Integer> annotated;
	Map<String, Integer> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}
// === end ===

// === case: typeargannotation_wildcard ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardSliceViolation {
	List<@TAnnB ? extends Number> bField;
	List<@TAnnA ? extends Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}
// === end ===

// === case: typeargannotation_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardBoundSliceViolation {
	List<? extends @TAnnB Number> bField;
	List<? extends @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}
// === end ===

// === case: typeargannotation_wrong_order ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWrongOrderSliceViolation {
	List<@TAnnB String> bField;
	List<@TAnnA String> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}
// === end ===