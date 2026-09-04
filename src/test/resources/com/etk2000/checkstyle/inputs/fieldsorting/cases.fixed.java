package com.etk2000.checkstyle.inputs.fieldsorting;

// === case: annotation_explicit_value_keyword_normalization ===
class InputFieldSortingAnnotationExplicitValueKeywordNormalizationSliceViolation {
	@SuppressWarnings("unused")
	int alpha = 2, beta = 1;
}
// === end ===

// === case: annotation_qualified ===
class InputFieldSortingAnnotationQualifiedSliceViolation {
	@Deprecated
	int alpha;
	@SuppressWarnings("unused")
	int beta;
}
// === end ===

// === case: anonclass_anon_plain_anon_plain_partial ===
class InputFieldSortingAnonClassAnonPlainAnonPlainPartialSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread first = new Thread() {}, second = new Thread() {};
}
// === end ===

// === case: anonclass_double_violation_same_field ===
class InputFieldSortingAnonClassDoubleViolationSameFieldSliceViolation {
	final String label = "w";
	final Thread beta = new Thread() {}, alpha = new Thread() {};
}
// === end ===

// === case: anonclass_order_tie_released_together ===
class InputFieldSortingAnonClassOrderTieReleasedTogetherSliceViolation {
	final String label = "w";
	final Thread aLPHA = new Thread() {}, alpha = new Thread() {};
}
// === end ===

// === case: anonclass_qualified_type ===
class InputFieldSortingAnonClassQualifiedTypeSliceViolation {
	final String label = "w";
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_shadow_bare_this_member ===
class InputFieldSortingAnonClassShadowBareThisMemberSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner", copy = label;
	};
}
// === end ===

// === case: anonclass_shadow_multi_var_member_last ===
class InputFieldSortingAnonClassShadowMultiVarMemberLastSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String alpha = "x", label = "inner", copy = label;
	};
}
// === end ===

// === case: anonclass_shadow_nested_own_read ===
class InputFieldSortingAnonClassShadowNestedOwnReadSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Object inner = new Object() {
			final String label = "inner", copy = label;
		};
	};
}
// === end ===

// === case: anonclass_shadow_other_name_still_dependency ===
class InputFieldSortingAnonClassShadowOtherNameStillDependencySliceViolation {
	final String label = "w", name = "n";
	final Object holder = new Object() {
		final String label = "inner", copy = label + name;
	};
}
// === end ===

// === case: anonclass_shadow_outer_multi_var_secondary ===
class InputFieldSortingAnonClassShadowOuterMultiVarSecondarySliceViolation {
	final String alpha = "a", label = "w";
	final Object holder = new Object() {
		final String label = "inner", copy = label;
	};
}
// === end ===

// === case: anonclass_shadowed_member_name ===
class InputFieldSortingAnonClassShadowedMemberNameSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner", copy = label;
	};
}
// === end ===

// === case: anonclass_two_anon_interleaved ===
class InputFieldSortingAnonClassTwoAnonInterleavedSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread first = new Thread() {}, second = new Thread() {};
}
// === end ===

// === case: anonclass_unshadowed_member_name ===
class InputFieldSortingAnonClassUnshadowedMemberNameSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String own = "inner", copy = own;
	};
}
// === end ===

// === case: array_c_style_mixed_style_reorders ===
class InputFieldSortingArrayCStyleMixedStyleReordersSliceViolation {
	int[] alpha, zebra;
}
// === end ===

// === case: array_c_style_plain_sibling_reorders ===
class InputFieldSortingArrayCStylePlainSiblingReordersSliceViolation {
	int codes;
	int[] elements;
}
// === end ===

// === case: array_c_style_reorders ===
class InputFieldSortingArrayCStyleReordersSliceViolation {
	int[] alpha, zebra;
}
// === end ===

// === case: array_c_style_two_dimensional_reorders ===
class InputFieldSortingArrayCStyleTwoDimensionalReordersSliceViolation {
	int[][] alpha, zebra;
}
// === end ===

// === case: array_c_style_whitespace_inside_brackets ===
class InputFieldSortingArrayCStyleWhitespaceInsideBracketsSliceViolation {
	int alpha;
	int[ ] zebra;
}
// === end ===

// === case: array_c_style_with_string_group_consolidation ===
class InputFieldSortingArrayCStyleWithStringGroupConsolidationSliceViolation {
	int[] elements;
	String alpha, zebra;
}
// === end ===

// === case: array_mixed_bracket_style_single_declarator ===
class InputFieldSortingArrayMixedBracketStyleSingleDeclaratorSliceViolation {
	String[][] alpha, zebra;
}
// === end ===

// === case: dependency_multi_var_releases_two_dependents ===
class InputFieldSortingDependencyMultiVarReleasesTwoDependentsSliceViolation {
	int alpha, beta, gamma = alpha + beta, delta = beta + alpha;
}
// === end ===

// === case: dependency_multi_var_two_dependents_pinned_skips ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyMultiVarTwoDependentsPinnedSkipsSliceViolation {
	int gamma = alpha, delta = beta, alpha = 1, beta = 2;
}
// === end ===

// === case: dependency_non_adjacent_anon_pair ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingDependencyNonAdjacentAnonPairSliceViolation {
	final Object alpha = new Object() {
		final String snapshot = String.valueOf(gamma);
	};
	final Object beta = new Object() {}, gamma = new Object() {};
}
// === end ===

// === case: dependency_non_adjacent_plain ===
class InputFieldSortingDependencyNonAdjacentPlainSliceViolation {
	final String beta = "b", label = "w", alpha = label + "!";
}
// === end ===

// === case: dependency_non_adjacent_two_between ===
class InputFieldSortingDependencyNonAdjacentTwoBetweenSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label;
	};
	final String alpha = String.valueOf(holder), beta = String.valueOf(holder);
}
// === end ===

// === case: dependency_null_order_cross_chunk_anon_wait ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyNullOrderCrossChunkAnonWaitSliceViolation {
	Object a2 = new Object() {
	};
	final Object a0 = new Object() {}, p0 = a2;
	Object p2 = a0;
}
// === end ===

// === case: dependency_null_order_cycle_beats_multi_var ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyNullOrderCycleBeatsMultiVarSliceViolation {
	int a = 1, c = d, d = c;
}
// === end ===

// === case: dependency_null_order_earlier_chunk_dep_multi_var ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderEarlierChunkDepMultiVarSliceViolation {
	final int k = 0;

	int a = 1, c = b + k, b = a;
}
// === end ===

// === case: dependency_null_order_multi_var_plain_cycle ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderMultiVarPlainCycleSliceViolation {
	int a = 1, c = b, b = a;
}
// === end ===

// === case: dependency_two_names_of_one_multi_var ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingDependencyTwoNamesOfOneMultiVarSliceViolation {
	int sum = alpha + beta, alpha = 1, beta = 2;
}
// === end ===

// === case: enumconstant_annotated_same_line ===
enum InputFieldSortingEnumConstantAnnotatedSameLineSliceViolation {
	@Deprecated
	ZETA,
	ALPHA
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings("unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ZEBRA,
	ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_array_value ===
enum InputFieldSortingEnumSpanAnnotationArgArrayValueSliceViolation {
	ZETA,
	@SuppressWarnings({"a", "b"})
	ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	ZETA,
	@Deprecated
	ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	ZETA,
	@SuppressWarnings("unused")
	ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSpanAnnotationArgStructuralCharsSliceViolation {
	ZETA,
	@SuppressWarnings("{ } , // /*")
	ALPHA
}
// === end ===

// === case: enumspan_escaped_triple_quote ===
enum InputFieldSortingEnumSpanEscapedTripleQuoteSliceViolation {
	ZETA,
	@SuppressWarnings("\"\"\"")
	ALPHA
}
// === end ===

// === case: enumspan_list_tail_comment_own_line ===
enum InputFieldSortingEnumSpanListTailCommentOwnLineSliceViolation {
	ZOMBIE,
	ACTIVE
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_string_with_block_comment_marker ===
enum InputFieldSortingEnumSpanStringWithBlockCommentMarkerSliceViolation {
	ZETA,
	@SuppressWarnings("/* not a comment */")
	ALPHA
}
// === end ===

// === case: enumspan_supplementary_annotation_arg ===
enum InputFieldSortingEnumSpanSupplementaryAnnotationArgSliceViolation {
	ZETA,
	@SuppressWarnings("𝐀")
	ALPHA
}
// === end ===

// === case: enumspan_trailing_comma ===
enum InputFieldSortingEnumSpanTrailingCommaSliceViolation {
	BETA,
	ALPHA
}
// === end ===

// === case: enumspan_trailing_comma_terminator_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommaTerminatorCommentMultilineSliceViolation {
	ZETA,
	ALPHA // done
	// more
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSpanTrailingCommaThenTerminatorCommentSliceViolation {
	ZETA,
	ALPHA // done
	;

	int x;
}
// === end ===

// === case: field_annotation_consolidation_skips_trailing_comment ===
class InputFieldSortingFieldAnnotationConsolidationSkipsTrailingCommentSliceViolation {
	int height, width; // in pixels
}
// === end ===

// === case: field_annotation_empty_parens_normalization ===
class InputFieldSortingFieldAnnotationEmptyParensNormalizationSliceViolation {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_block_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInBlockCommentSliceViolation {
	String /* @Nullable */ alpha, beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_line_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInLineCommentSliceViolation {
	String alpha, beta; // @Deprecated docs
}
// === end ===

// === case: field_annotation_multi_line_block_comment_state_threaded ===
class InputFieldSortingFieldAnnotationMultiLineBlockCommentStateThreadedSliceViolation {
	@AnnV
	/* @FakeAnno1
	   @FakeAnno2 */
	@BnnV
	String alpha = "a", zebra = "z";
}
// === end ===

// === case: field_annotation_multi_line_with_text_block_value ===
class InputFieldSortingFieldAnnotationMultiLineWithTextBlockValueSliceViolation {
	@SuppressWarnings(
			"""
			ignored text block with parens ( ) and "quotes"
			"""
	)
	String alpha, zebra;
}
// === end ===

// === case: field_annotation_order_qualified ===
class InputFieldSortingFieldAnnotationOrderQualifiedSliceViolation {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_with_inline_block_comment_prefix ===
class InputFieldSortingFieldAnnotationWithInlineBlockCommentPrefixSliceViolation {
	/* keep */ @Deprecated
	String alpha, zebra;
}
// === end ===

// === case: field_consolidation_annotation_param_order_merges ===
class InputFieldSortingFieldConsolidationAnnotationParamOrderMergesSliceViolation {
	@Multi(a = 1, b = 2)
	private long alpha, zeta;
}
// === end ===

// === case: field_consolidation_base_annotation_line_comment ===
class InputFieldSortingFieldConsolidationBaseAnnotationLineCommentSliceViolation {
	private int count;
	// set in onBind
	@Deprecated
	private String body, title;
}
// === end ===

// === case: field_consolidation_base_carried_close_reorders ===
class InputFieldSortingFieldConsolidationBaseCarriedCloseReordersSliceViolation {
	/* in
	*/ private int height, width;
}
// === end ===

// === case: field_consolidation_base_comment_before_type ===
class InputFieldSortingFieldConsolidationBaseCommentBeforeTypeSliceViolation {
	// px
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_base_empty_block_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseEmptyBlockCommentReordersSliceViolation {
	/**/
	private int height, width;
}
// === end ===

// === case: field_consolidation_base_leading_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseLeadingCommentReordersSliceViolation {
	// in pixels
	private int height, width;
}
// === end ===

// === case: field_consolidation_block_comment_contains_double_slash ===
class InputFieldSortingFieldConsolidationBlockCommentContainsDoubleSlashSliceViolation {
	// fake // comment
	String alpha, zebra;
}
// === end ===

// === case: field_consolidation_candidate_annotation_line_comment ===
class InputFieldSortingFieldConsolidationCandidateAnnotationLineCommentSliceViolation {
	private int count;
	@Deprecated // set in onBind
	private String body, title;
}
// === end ===

// === case: field_consolidation_candidate_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeSemicolonSliceViolation {
	// px
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_candidate_comment_before_type ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeTypeSliceViolation {
	/* px */ int alpha, beta;
}
// === end ===

// === case: field_consolidation_comment_line_splits_three_fields ===
class InputFieldSortingFieldConsolidationCommentLineSplitsThreeFieldsSliceViolation {
	// note
	private int alpha, beta, zeta;
}
// === end ===

// === case: field_consolidation_inline_base_stacked_commented_candidate ===
class InputFieldSortingFieldConsolidationInlineBaseStackedCommentedCandidateSliceViolation {
	private int count;
	@Deprecated // set in onBind
	private String body, title;
}
// === end ===

// === case: field_consolidation_moves_near_miss_directive ===
class InputFieldSortingFieldConsolidationMovesNearMissDirectiveSliceViolation {
	@Deprecated // NOSONA
	private int alpha, zeta;
}
// === end ===

// === case: field_consolidation_skips_across_block_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossBlockCommentLineSliceViolation {
	/* in pixels */
	private int height, width;
}
// === end ===

// === case: field_consolidation_skips_across_line_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossLineCommentLineSliceViolation {
	// in pixels
	private int height, width;
}
// === end ===

// === case: field_consolidation_skips_base_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationSkipsBaseCommentBeforeSemicolonSliceViolation {
	// px
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_annotation ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterAnnotationSliceViolation {
	// d
	@Deprecated
	private int height, width;
}
// === end ===

// === case: field_consolidation_skips_candidate_with_block_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithBlockCommentSliceViolation {
	/* keep me */
	String alpha, zebra;
}
// === end ===

// === case: field_consolidation_skips_candidate_with_line_comment ===
class InputFieldSortingFieldConsolidationSkipsCandidateWithLineCommentSliceViolation {
	// keep me
	String alpha, zebra;
}
// === end ===

// === case: field_consolidation_skips_hoist_below_base_leading_comment_line ===
class InputFieldSortingFieldConsolidationSkipsHoistBelowBaseLeadingCommentLineSliceViolation {
	// in pixels
	// w
	private int height, width;
}
// === end ===

// === case: field_consolidation_skips_moved_directive_on_annotation_line ===
class InputFieldSortingFieldConsolidationSkipsMovedDirectiveOnAnnotationLineSliceViolation {
	@Deprecated // NOSONAR
	private int alpha, zeta;
}
// === end ===

// === case: field_consolidation_skips_other_run_of_same_key ===
class InputFieldSortingFieldConsolidationSkipsOtherRunOfSameKeySliceViolation {
	final int alpha = 1, gamma = 2;
	final int beta, delta;

	InputFieldSortingFieldConsolidationSkipsOtherRunOfSameKeySliceViolation() {
		beta = 0;
		delta = 0;
	}
}
// === end ===

// === case: field_consolidation_trailing_block_comment ===
class InputFieldSortingFieldConsolidationTrailingBlockCommentSliceViolation {
	// px
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_two_stacked_annotations ===
class InputFieldSortingFieldConsolidationTwoStackedAnnotationsSliceViolation {
	@Deprecated
	@SuppressWarnings("unused")
	private long alpha, zeta;
}
// === end ===

// === case: field_consolidation_wrapped_base_after_equals ===
class InputFieldSortingFieldConsolidationWrappedBaseAfterEqualsSliceViolation {
	private long delay = 60, timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_dot ===
class InputFieldSortingFieldConsolidationWrappedBaseDotSliceViolation {
	private String delay = "60".trim(), timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_equals_on_continuation ===
class InputFieldSortingFieldConsolidationWrappedBaseEqualsOnContinuationSliceViolation {
	private long delay = 60, timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_minus ===
class InputFieldSortingFieldConsolidationWrappedBaseMinusSliceViolation {
	private long delay = 60 - 1, timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_plus ===
class InputFieldSortingFieldConsolidationWrappedBasePlusSliceViolation {
	private long delay = 60 + 1000, timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_star ===
class InputFieldSortingFieldConsolidationWrappedBaseStarSliceViolation {
	private long delay = 60 * 1000, timeout;
}
// === end ===

// === case: field_consolidation_wrapped_candidate ===
class InputFieldSortingFieldConsolidationWrappedCandidateSliceViolation {
	private long alpha, beta = 60 * 1000;
}
// === end ===

// === case: field_consolidation_wrapped_multi_var_base ===
class InputFieldSortingFieldConsolidationWrappedMultiVarBaseSliceViolation {
	private long alpha, beta, zeta;
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_nested_local ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesNestedLocalSliceViolation {
	int alpha = 2, beta = 1;

	void m() {
		final var charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_duplicate_text_below_does_not_steal_endidx ===
@SuppressWarnings("unused")
class InputFieldSortingFieldDuplicateTextBelowDoesNotStealEndIdxSliceViolation {
	int alpha = 2, beta = 1;

	void method() {
		final var alpha = 2;
		System.out.println(alpha);
	}
}
// === end ===

// === case: field_extract_all_names_no_match_fallback ===
class InputFieldSortingFieldExtractAllNamesNoMatchFallbackSliceViolation {
	int alpha = 1, zebra;
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingFieldInterleavedDependencyBailSliceViolation {
	int alpha, zebra = alpha + 1;
	static int s;
}
// === end ===

// === case: field_interleaved_opposite_static_bail ===
class InputFieldSortingFieldInterleavedOppositeStaticBailSliceViolation {
	int alpha, zebra;
	static int x;
}
// === end ===

// === case: field_interleaved_opposite_static_bail_static_group ===
class InputFieldSortingFieldInterleavedOppositeStaticBailStaticGroupSliceViolation {
	static int alpha, zebra;
	int x;
}
// === end ===

// === case: field_local_in_method_body_not_treated_as_field ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLocalInMethodBodyNotTreatedAsFieldSliceViolation {
	int alpha = 2, beta = 1;

	void method() {
		final var charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_lookback_past_non_annotation_parens ===
class InputFieldSortingFieldLookbackPastNonAnnotationParensSliceViolation {
	final int x = Math.max(
			1,
			2
	);
	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_multivar_secondary_name_dependency_pinned_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyPinnedReordersSliceViolation {
	int x, y, z = 1, a = y + 1;
}
// === end ===

// === case: field_multivar_secondary_name_dependency_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyReordersSliceViolation {
	int x, y, a = y + 1, z;
}
// === end ===

// === case: field_shared_line_comment_blank_line_inside_closes_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentBlankLineInsideClosesBeforeFieldSliceViolation {
	/* note

	 */ int alpha, zebra;
}
// === end ===

// === case: field_shared_line_comment_opener_on_other_group_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentOpenerOnOtherGroupFieldSliceViolation {
	static int q; /* c
	 */ int alpha, zebra;
}
// === end ===

// === case: field_shared_line_prefix_closes_earlier_comment ===
class InputFieldSortingFieldSharedLinePrefixClosesEarlierCommentSliceViolation {
	/* note
	 */ String alpha, zebra;
}
// === end ===

// === case: field_text_block_between_fields_in_class_body ===
class InputFieldSortingFieldTextBlockBetweenFieldsInClassBodySliceViolation {
	final String alpha = "a", zebra = "z";

	@SuppressWarnings("unused")
	final String docs = """
			line one
			contains } and { fake braces
			line three
			""";
}
// === end ===

// === case: init_backward_qualified_read_reorders ===
class InputFieldSortingInitBackwardQualifiedReadReordersSliceViolation {
	int alpha;
	private long count = 3;
	long total = count * 2L;
}
// === end ===

// === case: init_forward_read_constant_target_reorders ===
class InputFieldSortingInitForwardReadConstantTargetReordersSliceViolation {
	private final int count = 3, total = count * 2;
}
// === end ===

// === case: init_forward_read_qualified_string_constant_reorders ===
class InputFieldSortingInitForwardReadQualifiedStringConstantReordersSliceViolation {
	private final String label = "w";
	private final int total = label.length();
}
// === end ===

// === case: init_forward_read_secondary_name_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSecondaryNameSkipsSliceViolation {
	private int alpha, total = this.count * 2;
	private int beta, count = 3;
}
// === end ===

// === case: init_forward_read_uninitialized_target_reorders ===
class InputFieldSortingInitForwardReadUninitializedTargetReordersSliceViolation {
	private int count, total = count * 2;
}
// === end ===

// === case: init_written_lambda_local_named_like_later_field_reorders ===
// imports: java.util.function.Supplier
class InputFieldSortingInitWrittenLambdaLocalNamedLikeLaterFieldReordersSliceViolation {
	Supplier<Object> supplier = () -> {
		final int rate;
		rate = 2;
		return rate;
	};
	int alpha, rate = Integer.parseInt("1");
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated
	private int x, y;
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated
	private int y[], x;
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclInlineAnnotationInteriorCommentSkipsSliceViolation {
	@Deprecated
	private int zebra, /* keep me */ alpha;
}
// === end ===

// === case: name_within_decl_inline_annotation_wrapped ===
class InputFieldSortingNameWithinDeclInlineAnnotationWrappedSliceViolation {
	@Deprecated
	private int x, y;
}
// === end ===

// === case: region_marker_hash_near_miss_reorders ===
class InputFieldSortingRegionMarkerHashNearMissReordersSliceViolation {
	// #regional default
	private int alpha, zeta;
}
// === end ===

// === case: typeargannotation_empty_parens ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationEmptyParensSliceViolation {
	List<@TAnnA String> aField;
	List<@TAnnB String> bField;
}
// === end ===

// === case: typeargannotation_explicit_value_keyword ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationExplicitValueKeywordSliceViolation {
	List<@TAnnParam(1) String> lower;
	List<@TAnnParam(2) String> higher;
}
// === end ===