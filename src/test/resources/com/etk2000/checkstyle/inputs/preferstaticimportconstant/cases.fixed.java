package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

// === case: annotation_arg_containing_private_string_does_not_misidentify_visibility ===
// imports: foo.Foo
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
class InputPreferStaticImportConstantAnnotationArgContainingPrivateStringDoesNotMisidentifyVisibilitySliceViolation {
	@SuppressWarnings("private-key")
	static final int X = Foo.X;
}
// === end ===

// === case: annotation_arg_unbalanced_paren_inside_string_does_not_misidentify_visibility ===
// imports: foo.Foo
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
class InputPreferStaticImportConstantAnnotationArgUnbalancedParenInsideStringDoesNotMisidentifyVisibilitySliceViolation {
	@Description("foo) private bar(")
	static final int X = Foo.X;
}
// === end ===

// === case: char_literal_in_method_body_is_preserved_during_rewrite ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCharLiteralInMethodBodyIsPreservedDuringRewriteSliceViolation {
	int use() {
		final var q = '\''; return X;
	}
}
// === end ===

// === case: cinit_assignment_sharing_line_with_trailing_statement_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentSharingLineWithTrailingStatementReturnsCinitSkipSliceViolation {
	private static final int X;

	static {
		X = Foo.X; final var y = 0;
	}
}
// === end ===

// === case: cinit_assignment_with_leading_assignment_statement_on_same_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentWithLeadingAssignmentStatementOnSameLineReturnsCinitSkipSliceViolation {
	private static final int X;

	static {
		final var y = 0; X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_closer_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticCloserLineKeepsBlockSliceViolation {
	static {
	/* close note */ }
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_opener_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticOpenerLineKeepsBlockSliceViolation {
	static { // open note
	}
}
// === end ===

// === case: cinit_blank_final_with_decl_after_static_block_on_same_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithDeclAfterStaticBlockOnSameLineKeepsBlockSliceViolation {
	static {
	} private static final int Y = 0;
}
// === end ===

// === case: cinit_blank_final_with_decl_before_static_block_on_same_opener_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithDeclBeforeStaticBlockOnSameOpenerLineKeepsBlockSliceViolation {
	private int Z = 7; static {
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_in_static_block_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentInStaticBlockKeepsBlockSliceViolation {
	static {
		// important context: do not drop
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_on_cinit_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentOnCinitLineKeepsBlockSliceViolation {
	static {
	}
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_in_static_block_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentInStaticBlockKeepsBlockSliceViolation {
	static {
		// trailing note
	}
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_on_cinit_line_keeps_block ===
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentOnCinitLineKeepsBlockSliceViolation {
	static {
	}
}
// === end ===

// === case: cinit_fqn_lhs_with_leading_non_assign_statement_is_auto_fixed ===
// package: x
class InputPreferStaticImportConstantCinitFqnLhsWithLeadingNonAssignStatementIsAutoFixedSliceViolation {
	static {
		System.out.println();
	}
}
// === end ===

// === case: cinit_renamed_alias_shadowed_by_local_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedAliasShadowedByLocalReturnsShadowSkipSliceViolation {
	private static final int RENAMED;

	static {
		RENAMED = Foo.X;
	}

	int use() {
		final var RENAMED = 5;
		return RENAMED;
	}
}
// === end ===

// === case: cinit_renamed_with_bare_local_usage_rewrites_to_constant ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitRenamedWithBareLocalUsageRewritesToConstantSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: cinit_renamed_with_qualified_local_usage_rewrites_to_constant ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitRenamedWithQualifiedLocalUsageRewritesToConstantSliceViolation {
	int use() {
		return X + X;
	}
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_extra_statement_in_static_block_keeps_block ===
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithExtraStatementInStaticBlockKeepsBlockSliceViolation {  static {  final var y = 0; } }
// === end ===

// === case: cinit_with_leading_non_assign_statement_is_auto_fixed ===
class InputPreferStaticImportConstantCinitWithLeadingNonAssignStatementIsAutoFixedSliceViolation {
	static {
		System.out.println();
	}
}
// === end ===

// === case: cinit_with_sibling_fields_between_field_and_cinit_preserves_them ===
class InputPreferStaticImportConstantCinitWithSiblingFieldsBetweenFieldAndCinitPreservesThemSliceViolation {
	private int other1, other2;
}
// === end ===

// === case: cinit_with_text_block_sibling_field_preserves_content ===
class InputPreferStaticImportConstantCinitWithTextBlockSiblingFieldPreservesContentSliceViolation {
	private static final String DOC = """
			X = Bar.X
			""";
}
// === end ===

// === case: enum_field_only_member ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_ONLY_MEMBER
class InputPreferStaticImportConstantEnumFieldOnlyMemberSliceViolation {
	enum InnerEnum {
	}
}
// === end ===

// === case: enum_field_with_content_after_semi ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_CONTENT_AFTER_SEMI
class InputPreferStaticImportConstantEnumFieldWithContentAfterSemiSliceViolation {
	enum InnerEnum {
		A /* note */
	}
}
// === end ===

// === case: fully_qualified_chain_usage_in_method_body_rewritten ===
// imports: static pkg.Foo.X
class InputPreferStaticImportConstantFullyQualifiedChainUsageInMethodBodyRewrittenSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: lowercase_local_field_name_rewritten_to_uppercase_constant_name ===
// imports: static foo.Foo.MAX
class InputPreferStaticImportConstantLowercaseLocalFieldNameRewrittenToUppercaseConstantNameSliceViolation {
	int use() {
		return MAX;
	}
}
// === end ===

// === case: multi_var_all_aliases_with_usages_and_own_line_annotation ===
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAllAliasesWithUsagesAndOwnLineAnnotationSliceViolation {
	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_conflicting_first_alias_is_kept ===
// imports: static other.Other.X
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarConflictingFirstAliasIsKeptSliceViolation {
	private static final int X = Foo.X;
}
// === end ===

// === case: multi_var_conflicting_sibling_is_kept ===
// imports: static other.Other.Y
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarConflictingSiblingIsKeptSliceViolation {
	private static final int Y = Bar.Y;
}
// === end ===

// === case: multi_var_partial_keep_with_separated_annotation_above ===
// imports: foo.Foo
// imports: static foo.Foo.B
class InputPreferStaticImportConstantMultiVarPartialKeepWithSeparatedAnnotationAboveSliceViolation {
	@Deprecated
	private static final int A = 0;
}
// === end ===

// === case: multi_var_renamed_alias_on_last_variable_rewrites_usages ===
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarRenamedAliasOnLastVariableRewritesUsagesSliceViolation {
	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_renamed_alias_rewrites_usages ===
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarRenamedAliasRewritesUsagesSliceViolation {
	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_renamed_alias_shadowed_by_local_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedAliasShadowedByLocalReturnsShadowSkipSliceViolation {
	private static final int RENAMED = Foo.X, Y = Bar.Y;

	int use() {
		final var RENAMED = 5;
		return RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_sibling_rename_target_collides_returns_skip ===
// skip-reason: rename target collides with existing identifier 'Y'
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedSiblingRenameTargetCollidesReturnsSkipSliceViolation {
	private static final int X = Foo.X, RENAMED = Bar.Y;

	int use() {
		final var Y = 1;
		return X + RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_sibling_rewrites_sibling_usage ===
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarRenamedSiblingRewritesSiblingUsageSliceViolation {
	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_two_aliases_different_members_rewrite_each_to_own_member ===
// imports: static foo.Foo.MAX
// imports: static foo.Foo.MIN
class InputPreferStaticImportConstantMultiVarTwoAliasesDifferentMembersRewriteEachToOwnMemberSliceViolation {
	int span() {
		return MAX - MIN;
	}
}
// === end ===

// === case: multi_var_two_aliases_qualified_usages_rewrite_each_to_own_member ===
// imports: static foo.Foo.MAX
// imports: static foo.Foo.MIN
class InputPreferStaticImportConstantMultiVarTwoAliasesQualifiedUsagesRewriteEachToOwnMemberSliceViolation {
	int span() {
		return MAX - MIN;
	}
}
// === end ===

// === case: multi_var_with_annotation_on_separated_line_above_does_not_misidentify_own_decl_as_shadow ===
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarWithAnnotationOnSeparatedLineAboveDoesNotMisidentifyOwnDeclAsShadowSliceViolation {
	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multiple_qualified_usages_on_same_line_all_rewritten ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultipleQualifiedUsagesOnSameLineAllRewrittenSliceViolation {
	int use() {
		return X + X;
	}
}
// === end ===

// === case: multiple_usage_lines_with_intermediate_line_preserved ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultipleUsageLinesWithIntermediateLinePreservedSliceViolation {
	int a() {
		return X;
	}

	int b() {
		return X;
	}
}
// === end ===

// === case: multivar_tri ===
class InputPreferStaticImportConstantTriSliceViolation {
}
// === end ===

// === case: nested_annotation_arg_does_not_misidentify_visibility ===
// imports: foo.Foo
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
class InputPreferStaticImportConstantNestedAnnotationArgDoesNotMisidentifyVisibilitySliceViolation {
	@MyAnno(@Other("private"))
	static final int X = Foo.X;
}
// === end ===

// === case: nested_class_chain_usage_in_method_body_rewritten ===
// imports: static foo.Outer.Inner.X
class InputPreferStaticImportConstantNestedClassChainUsageInMethodBodyRewrittenSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: no_method_body_usage_deletes_field_only ===
class InputPreferStaticImportConstantNoMethodBodyUsageDeletesFieldOnlySliceViolation {
	int use() {
		return 0;
	}
}
// === end ===

// === case: qualified_usage_before_field_decl_is_rewritten ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageBeforeFieldDeclIsRewrittenSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: qualified_usage_inside_block_comment_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentIsPreservedSliceViolation {
	/** See {@link Foo#X} for details. */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_block_comment_spanning_lines_is_preserved ===
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentSpanningLinesIsPreservedSliceViolation {
	/*
	 * mentions Foo.X here
	 */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_line_comment_is_preserved ===
class InputPreferStaticImportConstantQualifiedUsageInsideLineCommentIsPreservedSliceViolation {
	int use() {
		return 0; // see Foo.X above
	}
}
// === end ===

// === case: qualified_usage_inside_string_literal_is_preserved ===
class InputPreferStaticImportConstantQualifiedUsageInsideStringLiteralIsPreservedSliceViolation {
	String use() {
		return "Foo.X";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_is_preserved ===
class InputPreferStaticImportConstantQualifiedUsageInsideTextBlockIsPreservedSliceViolation {
	String use() {
		return """
			does Foo.X stuff
			""";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_with_backslash_escape_is_preserved ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideTextBlockWithBackslashEscapeIsPreservedSliceViolation {
	String use() {
		return """
			RENAMED \"escaped\" and Foo.X stuff
			""";
	}
	int val() { return X; }
}
// === end ===

// === case: qualified_usage_preceded_by_dot_on_different_object_not_rewritten ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsagePrecededByDotOnDifferentObjectNotRewrittenSliceViolation {
	int use(Other other) {
		return X + other.Foo.X;
	}
}
// === end ===

// === case: qualified_usage_with_longer_suffix_is_not_rewritten ===
// imports: static foo.Foo.X
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageWithLongerSuffixIsNotRewrittenSliceViolation {
	int use() {
		return X + Foo.XLong;
	}
}
// === end ===

// === case: renamed_alias_label_with_same_name_in_method_body_is_preserved ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasLabelWithSameNameInMethodBodyIsPreservedSliceViolation {
	int use() {
		RENAMED:
		for (var i = 0; i < 5; ++i) {
			if (i > 0)
				return X;
		}
		return 0;
	}
}
// === end ===

// === case: renamed_alias_own_declaration_is_not_its_own_shadow ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasOwnDeclarationIsNotItsOwnShadowSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_method_body_rewritten_to_constant_name ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasReferencedInMethodBodyRewrittenToConstantNameSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_string_literal_is_preserved ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasReferencedInStringLiteralIsPreservedSliceViolation {
	String use() {
		return "RENAMED";
	}
	int val() { return X; }
}
// === end ===

// === case: renamed_alias_with_both_qualified_and_local_usages_rewritten ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasWithBothQualifiedAndLocalUsagesRewrittenSliceViolation {
	int use() {
		return X + X;
	}
}
// === end ===

// === case: same_class_used_in_field_and_qualified_method_body_rewrites_usage ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSameClassUsedInFieldAndQualifiedMethodBodyRewritesUsageSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: shadowing_array_type_local_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingArrayTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		final int[] RENAMED = new int[5];
	}
}
// === end ===

// === case: shadowing_for_init_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a for-loop variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingForInitSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		for (var RENAMED = 0; RENAMED < 10; ++RENAMED) {}
	}
}
// === end ===

// === case: shadowing_generic_reference_type_local_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		final Map<String, Integer> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_local_declaration_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingLocalDeclarationSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int use() {
		final var RENAMED = 5;
		return RENAMED;
	}
}
// === end ===

// === case: shadowing_nested_generic_reference_type_local_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingNestedGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		final Map<String, List<Integer>> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_reference_type_local_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int use() {
		final Foo RENAMED = null;
		return 0;
	}
}
// === end ===

// === case: shadowing_try_with_resources_variable_skips_fix ===
// imports: foo.Foo
// skip-reason: renamed alias's local name clashes with a try-with-resources variable elsewhere; rename manually to avoid scope conflicts
class InputPreferStaticImportConstantShadowingTryWithResourcesVariableSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() throws Exception {
		try (var RENAMED = null) {}
	}
}
// === end ===

// === case: switch_case_at_column_zero_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSwitchCaseAtColumnZeroReferenceRenamedFieldSliceViolation {
	int use(int k) {
		switch (k) {
case X:
			return 1;
		default:
			return 0;
		}
	}
}
// === end ===

// === case: switch_case_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSwitchCaseReferenceRenamedFieldSliceViolation {
	int use(int k) {
		switch (k) {
			case X:
				return 1;
			default:
				return 0;
		}
	}
}
// === end ===

// === case: ternary_block_comment_between_question_and_ident_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryBlockCommentBetweenQuestionAndIdentReferenceRenamedFieldSliceViolation {
	int use(boolean flag) {
		return flag ? /* note */ X : 0;
	}
}
// === end ===

// === case: ternary_multi_line_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryMultiLineReferenceRenamedFieldSliceViolation {
	int use(boolean flag) {
		return flag
				? X
				: 0;
	}
}
// === end ===

// === case: ternary_no_whitespace_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryNoWhitespaceReferenceRenamedFieldSliceViolation {
	int use(boolean flag) {
		return flag?X:0;
	}
}
// === end ===

// === case: ternary_reference_renamed_field ===
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryReferenceRenamedFieldSliceViolation {
	int use(boolean flag) {
		return flag ? X : 0;
	}
}
// === end ===