package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

// === case: annotated_alias ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantAnnotatedAliasSliceViolation {
	@Deprecated
	private static final int ANNOTATED_ALIAS = AnchorClass.ANNOTATED_ALIAS;
}
// === end ===

// === case: annotation_arg_containing_equals_before_real_equals_honors_private ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantAnnotationArgContainingEqualsBeforeRealEqualsHonorsPrivateSliceViolation {
}
// === end ===

// === case: annotation_arg_containing_private_string_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantAnnotationArgContainingPrivateStringDoesNotMisidentifyVisibilitySliceViolation {
	@SuppressWarnings("private-key") static final int X = Foo.X;
}
// === end ===

// === case: annotation_arg_unbalanced_paren_inside_string_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantAnnotationArgUnbalancedParenInsideStringDoesNotMisidentifyVisibilitySliceViolation {
	@Description("foo) private bar(") static final int X = Foo.X;
}
// === end ===

// === case: array_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ARRAY_ALIAS
class InputPreferStaticImportConstantArrayAliasSliceViolation {
}
// === end ===

// === case: block_comment_straddling_into_multi_var_decl_returns_skip ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
class InputPreferStaticImportConstantBlockCommentStraddlingIntoMultiVarDeclSliceViolation {
	/* note
	*/ private static final int A = 0, B = Foo.B;
}
// === end ===

// === case: block_comment_straddling_into_separated_annotation_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantBlockCommentStraddlingIntoSeparatedAnnotationSliceViolation {
	/* note
	*/ @Deprecated

	private static final int X = Foo.X;
}
// === end ===

// === case: c_style_array_suffix_field_name_parses ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCStyleArraySuffixFieldNameParsesSliceViolation {
}
// === end ===

// === case: c_style_array_suffix_with_whitespace_parses ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCStyleArraySuffixWithWhitespaceParsesSliceViolation {
}
// === end ===

// === case: canonical_alias_blank_above_only_deletes_line_only ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X1
class InputPreferStaticImportConstantCanonicalAliasBlankAboveOnlyDeletesLineOnlySliceViolation {
	private static final int A_BEFORE = 1;

	private static final int Z_AFTER = 2;
}
// === end ===

// === case: canonical_alias_no_blanks_deletes_line_only ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X2
class InputPreferStaticImportConstantCanonicalAliasNoBlanksDeletesLineOnlySliceViolation {
	private static final int A_BEFORE = 1;
	private static final int Z_AFTER = 2;
}
// === end ===

// === case: canonical_alias_with_surrounding_blanks_collapses_pair ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X3
class InputPreferStaticImportConstantCanonicalAliasWithSurroundingBlanksCollapsesPairSliceViolation {
	private static final int A_BEFORE = 1;

	private static final int Z_AFTER = 2;
}
// === end ===

// === case: char_literal_in_method_body_is_preserved_during_rewrite ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCharLiteralInMethodBodyIsPreservedDuringRewriteSliceViolation {

	int use() {
		char q = '\''; return X;
	}
}
// === end ===

// === case: cinit_assignment_sharing_line_with_trailing_statement_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentSharingLineWithTrailingStatementReturnsCinitSkipSliceViolation {
	private static final int X;

	static {
		X = Foo.X; int y = 0;
	}
}
// === end ===

// === case: cinit_assignment_spanning_multiple_lines_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentSpanningMultipleLinesReturnsCinitSkipSliceViolation {
	private static final int X;

	static {
		X =
				Foo.X;
	}
}
// === end ===

// === case: cinit_assignment_with_leading_assignment_statement_on_same_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentWithLeadingAssignmentStatementOnSameLineReturnsCinitSkipSliceViolation {
	private static final int X;

	static {
		int y = 0; X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_blank_final_with_block_comment_closing_on_decl_line_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithBlockCommentClosingOnDeclLineReturnsSkipSliceViolation {
	/* opens
	*/ private static final int X;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_closer_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticCloserLineKeepsBlockSliceViolation {

	static {
	/* close note */ }
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_opener_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticOpenerLineKeepsBlockSliceViolation {

	static { // open note
	}
}
// === end ===

// === case: cinit_blank_final_with_decl_after_static_block_on_same_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithDeclAfterStaticBlockOnSameLineKeepsBlockSliceViolation {

	static {
	} private static final int Y = 0;
}
// === end ===

// === case: cinit_blank_final_with_decl_before_static_block_on_same_opener_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithDeclBeforeStaticBlockOnSameOpenerLineKeepsBlockSliceViolation {

	private int Z = 7; static {
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_in_static_block_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentInStaticBlockKeepsBlockSliceViolation {

	static {
		// important context: do not drop
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_on_cinit_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentOnCinitLineKeepsBlockSliceViolation {

	static {
	}
}
// === end ===

// === case: cinit_blank_final_with_qualified_assignment_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithQualifiedAssignmentIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_blank_final_with_stray_semicolons_in_static_block_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithStraySemicolonsInStaticBlockIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_in_static_block_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentInStaticBlockKeepsBlockSliceViolation {

	static {
		// trailing note
	}
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_on_cinit_line_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentOnCinitLineKeepsBlockSliceViolation {

	static {
	}
}
// === end ===

// === case: cinit_default_package_bare_lhs_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitDefaultPackageBareLhsIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_find_field_def_disambiguates_by_column_on_same_line_nested_classes ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitFindFieldDefDisambiguatesByColumnOnSameLineNestedClassesSliceViolation { static class A {   } static class B { private static final int X = 5; } }
// === end ===

// === case: cinit_fqn_lhs_assignment_is_auto_fixed ===
// package: x
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitFqnLhsAssignmentIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_fqn_lhs_with_leading_non_assign_statement_is_auto_fixed ===
// package: x
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitFqnLhsWithLeadingNonAssignStatementIsAutoFixedSliceViolation {

	static {
		System.out.println();
	}
}
// === end ===

// === case: cinit_inside_enum_body_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitInsideEnumBodyIsAutoFixedSliceViolation {
	enum E {
		A
	}
}
// === end ===

// === case: cinit_inside_record_body_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitInsideRecordBodyIsAutoFixedSliceViolation {
	record R(int x) {}
}
// === end ===

// === case: cinit_java_lang_implicit_import_is_auto_fixed ===
// imports: static java.lang.Math.PI
class InputPreferStaticImportConstantCinitJavaLangImplicitImportIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_annotated_blank_final_with_equals_in_annotation_arg_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineAnnotatedBlankFinalWithEqualsInAnnotationArgIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_block_comment_before_annotation_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithBlockCommentBeforeAnnotationIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_content_before_annotation_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithContentBeforeAnnotationReturnsCinitSkipSliceViolation {
	int z; @SuppressWarnings("u")
	private static final int X;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_intermediate_blank_line_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithIntermediateBlankLineIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_intermediate_comment_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithIntermediateCommentLineReturnsCinitSkipSliceViolation {
	@SuppressWarnings("u")
	/* intermediate */
	private static final int X;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_trailing_block_comment_after_field_semi_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithTrailingBlockCommentAfterFieldSemiIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_trailing_statement_after_field_semi_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithTrailingStatementAfterFieldSemiReturnsCinitSkipSliceViolation {
	@SuppressWarnings("u")
	private static final int X; private int Y;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_zero_indent_annotation_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithZeroIndentAnnotationIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_multi_line_blank_final_with_trailing_line_comment_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitMultiLineBlankFinalWithTrailingLineCommentIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_non_private_blank_final_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantCinitNonPrivateBlankFinalReturnsVisibilitySkipSliceViolation {
	public static final int X;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_qualified_lhs_with_internal_whitespace_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitQualifiedLhsWithInternalWhitespaceIsAutoFixedSliceViolation {
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
		int RENAMED = 5;
		return RENAMED;
	}
}
// === end ===

// === case: cinit_renamed_alias_shadowed_by_nested_class_field_between_decl_and_cinit_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a field with the same name elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedAliasShadowedByNestedClassFieldBetweenDeclAndCinitReturnsShadowSkipSliceViolation {
	private static final int RENAMED;
	static class Inner {
		private static final int RENAMED = 0;
	}

	static {
		RENAMED = Foo.X;
	}
}
// === end ===

// === case: cinit_renamed_alias_target_collides_returns_rename_target_skip ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedAliasTargetCollidesReturnsRenameTargetSkipSliceViolation {
	private static final int RENAMED;

	static {
		RENAMED = Foo.X;
	}

	int X() {
		return 0;
	}
}
// === end ===

// === case: cinit_renamed_with_bare_local_usage_rewrites_to_constant ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitRenamedWithBareLocalUsageRewritesToConstantSliceViolation {


	int use() {
		return X;
	}
}
// === end ===

// === case: cinit_renamed_with_no_body_usage_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitRenamedWithNoBodyUsageIsAutoFixedSliceViolation {
}
// === end ===

// === case: cinit_renamed_with_qualified_local_usage_rewrites_to_constant ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitRenamedWithQualifiedLocalUsageRewritesToConstantSliceViolation {


	int use() {
		return X + X;
	}
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitIsAutoFixedSliceViolation {   }
// === end ===

// === case: cinit_same_line_decl_and_cinit_no_whitespace_around_static_keyword_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitNoWhitespaceAroundStaticKeywordIsAutoFixedSliceViolation {  }
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_target_collides_returns_rename_target_skip ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedTargetCollidesReturnsRenameTargetSkipSliceViolation { private static final int RENAMED; static { RENAMED = Foo.X; } int X() { return 0; } }
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_body_usage_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithBodyUsageIsAutoFixedSliceViolation {   int use() { return X; } }
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_no_body_usage_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithNoBodyUsageIsAutoFixedSliceViolation {   }
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_substring_in_tail_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithSubstringInTailIsAutoFixedSliceViolation {   int RENAMEDISH() { return 1; } }
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_annotated_field_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithAnnotatedFieldIsAutoFixedSliceViolation {   }
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_annotation_on_prior_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithAnnotationOnPriorLineReturnsCinitSkipSliceViolation {
	@SuppressWarnings("unused")
	private static final int X; static { X = Foo.X; }
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_comment_inside_static_block_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithCommentInsideStaticBlockKeepsBlockSliceViolation {  static { /* note */  } }
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_extra_statement_in_static_block_keeps_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithExtraStatementInStaticBlockKeepsBlockSliceViolation {  static {  int y = 0; } }
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_multi_line_static_block_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithMultiLineStaticBlockReturnsCinitSkipSliceViolation { private static final int X; static { X = Foo.X;
}
}
// === end ===

// === case: cinit_same_line_decl_and_fqn_cinit_is_auto_fixed ===
// package: x
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndFqnCinitIsAutoFixedSliceViolation {   }
// === end ===

// === case: cinit_same_line_decl_and_qualified_cinit_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitSameLineDeclAndQualifiedCinitIsAutoFixedSliceViolation {   }
// === end ===

// === case: cinit_with_leading_non_assign_statement_is_auto_fixed ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitWithLeadingNonAssignStatementIsAutoFixedSliceViolation {

	static {
		System.out.println();
	}
}
// === end ===

// === case: cinit_with_sibling_fields_between_field_and_cinit_preserves_them ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitWithSiblingFieldsBetweenFieldAndCinitPreservesThemSliceViolation {
	private int other1;
	private int other2;

}
// === end ===

// === case: cinit_with_text_block_sibling_field_preserves_content ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantCinitWithTextBlockSiblingFieldPreservesContentSliceViolation {
	private static final String DOC = """
			X = Bar.X
			""";

}
// === end ===

// === case: class_field_with_stray_semicolon_sibling ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.CLASS_STRAY_SEMI
class InputPreferStaticImportConstantClassFieldWithStraySemicolonSiblingSliceViolation {
	;
}
// === end ===

// === case: deeply_nested_dot_chain ===
// imports: static a0.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.a23.a24.a25.a26.a27.a28.a29.a30.a31.a32.a33.a34.a35.a36.a37.a38.a39.a40.a41.a42.a43.a44.a45.a46.a47.a48.a49
class InputPreferStaticImportConstantDeeplyNestedDotChainSliceViolation {
}
// === end ===

// === case: deeply_nested_parens_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X23
class InputPreferStaticImportConstantDeeplyNestedParensAliasSliceViolation {
}
// === end ===

// === case: enum_field ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_FIELD
class InputPreferStaticImportConstantEnumFieldSliceViolation {
	enum InnerEnum {
		A
	}
}
// === end ===

// === case: enum_field_only_member ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_ONLY_MEMBER
class InputPreferStaticImportConstantEnumFieldOnlyMemberSliceViolation {
	enum InnerEnum {
		;
	}
}
// === end ===

// === case: enum_field_with_content_after_semi ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_CONTENT_AFTER_SEMI
class InputPreferStaticImportConstantEnumFieldWithContentAfterSemiSliceViolation {
	enum InnerEnum {
		A; /* note */
	}
}
// === end ===

// === case: enum_field_with_multi_blank_before_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_MULTI_BLANK_SEMI
class InputPreferStaticImportConstantEnumFieldWithMultiBlankBeforeSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A
	}
}
// === end ===

// === case: enum_field_with_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_SEMI_OWN_LINE
class InputPreferStaticImportConstantEnumFieldWithSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A
	}
}
// === end ===

// === case: enum_field_with_single_blank_before_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.ENUM_SINGLE_BLANK_SEMI
class InputPreferStaticImportConstantEnumFieldWithSingleBlankBeforeSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A
	}
}
// === end ===

// === case: equals_inside_comment_before_real_equals_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantEqualsInsideCommentBeforeRealEqualsResolvesSliceViolation {
}
// === end ===

// === case: explicit_import_wins_over_wildcard ===
// imports: other.Foo
// imports: wild.*
// imports: static other.Foo.X
class InputPreferStaticImportConstantExplicitImportWinsOverWildcardSliceViolation {
}
// === end ===

// === case: explicit_java_lang_import_overrides_whitelist ===
// imports: java.lang.Integer
// imports: static java.lang.Integer.MAX_VALUE
class InputPreferStaticImportConstantExplicitJavaLangImportOverridesWhitelistSliceViolation {
}
// === end ===

// === case: explicit_non_java_lang_integer_import_wins_over_whitelist ===
// imports: other.Integer
// imports: static other.Integer.MAX_VALUE
class InputPreferStaticImportConstantExplicitNonJavaLangIntegerImportWinsOverWhitelistSliceViolation {
}
// === end ===

// === case: find_statement_end_accepts_trailing_block_comment ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantFindStatementEndAcceptsTrailingBlockCommentSliceViolation {
}
// === end ===

// === case: find_statement_end_handles_escaped_quote_in_string_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndHandlesEscapedQuoteInStringLiteralSliceViolation {
	String A = "a\";b"; private static final int X = Foo.X;
}
// === end ===

// === case: find_statement_end_skips_block_comment_spanning_lines ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantFindStatementEndSkipsBlockCommentSpanningLinesSliceViolation {
}
// === end ===

// === case: find_statement_end_skips_escaped_quote_in_char_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsEscapedQuoteInCharLiteralSliceViolation {
	char Q = '\''; private static final int X = Foo.X;
}
// === end ===

// === case: find_statement_end_skips_line_comment_on_same_line ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantFindStatementEndSkipsLineCommentOnSameLineSliceViolation {
}
// === end ===

// === case: find_statement_end_skips_semicolon_in_char_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsSemicolonInCharLiteralSliceViolation {
	char SEMI = ';'; private static final int X = Foo.X;
}
// === end ===

// === case: find_statement_end_skips_semicolon_in_string_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsSemicolonInStringLiteralSliceViolation {
	String A = "a;b;c"; private static final int X = Foo.X;
}
// === end ===

// === case: find_statement_end_starts_in_block_comment_for_single_var_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndStartsInBlockCommentForSingleVarReturnsSkipSliceViolation {
	/* opens
	*/ private static final int X = Foo.X;
}
// === end ===

// === case: fq_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15
class InputPreferStaticImportConstantFqAliasSliceViolation {
}
// === end ===

// === case: fq_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25
class InputPreferStaticImportConstantFqCinitSliceViolation {
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

// === case: fully_qualified_rhs_resolves_as_is ===
// imports: static pkg.Foo.X
class InputPreferStaticImportConstantFullyQualifiedRhsResolvesAsIsSliceViolation {
}
// === end ===

// === case: generic_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: java.util.List
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.GENERIC_ALIAS
class InputPreferStaticImportConstantGenericAliasSliceViolation {
}
// === end ===

// === case: import_inside_text_block_ignored_for_fqcn_resolution ===
// imports: com.fake.Foo
// imports: static com.fake.Foo.X
class InputPreferStaticImportConstantImportInsideTextBlockIgnoredForFqcnResolutionSliceViolation {
	String doc = """
		""";
}
// === end ===

// === case: import_line_with_leading_block_comment_resolves ===
// imports: /* legacy */ import foo.Foo;
// imports: static foo.Foo.X
class InputPreferStaticImportConstantImportLineWithLeadingBlockCommentResolvesSliceViolation {
}
// === end ===

// === case: import_line_with_trailing_comment_resolves ===
// imports: import foo.Foo; // historical note
// imports: static foo.Foo.X
class InputPreferStaticImportConstantImportLineWithTrailingCommentResolvesSliceViolation {
}
// === end ===

// === case: import_line_with_url_in_block_comment_resolves ===
// imports: import foo.Foo; /* see https://example.com */
// imports: static foo.Foo.X
class InputPreferStaticImportConstantImportLineWithUrlInBlockCommentResolvesSliceViolation {
}
// === end ===

// === case: int_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.INT_ALIAS
class InputPreferStaticImportConstantIntAliasSliceViolation {
}
// === end ===

// === case: java_lang_implicit_import_resolves_for_simple_var ===
// imports: static java.lang.Integer.MAX_VALUE
class InputPreferStaticImportConstantJavaLangImplicitImportResolvesForSimpleVarSliceViolation {
}
// === end ===

// === case: java_lang_whitelist_wins_over_single_wildcard_fallback ===
// imports: other.*
// imports: static java.lang.Math.PI
class InputPreferStaticImportConstantJavaLangWhitelistWinsOverSingleWildcardFallbackSliceViolation {
}
// === end ===

// === case: leading_annotation_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingAnnotationAboveReturnsSkipSliceViolation {
	@Deprecated
	private static final int X = Foo.X;
}
// === end ===

// === case: leading_javadoc_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingJavadocAboveReturnsSkipSliceViolation {
	/** Important note. */
	private static final int X = Foo.X;
}
// === end ===

// === case: leading_javadoc_continuation_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingJavadocContinuationAboveReturnsSkipSliceViolation {
	/**
	 * doc.
	 */
	private static final int X = Foo.X;
}
// === end ===

// === case: leading_line_comment_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingLineCommentAboveReturnsSkipSliceViolation {
	// explains why
	private static final int X = Foo.X;
}
// === end ===

// === case: leading_star_with_equals_does_not_trigger_skip ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantLeadingStarWithEqualsDoesNotTriggerSkipSliceViolation {
	String a = "x"
		* /* assigned */ "y=";
}
// === end ===

// === case: leading_star_with_semicolon_does_not_trigger_skip ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantLeadingStarWithSemicolonDoesNotTriggerSkipSliceViolation {
	int z = 1
		* 2;
}
// === end ===

// === case: lowercase_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X7
class InputPreferStaticImportConstantLowercaseAliasSliceViolation {
}
// === end ===

// === case: lowercase_local_field_name_rewritten_to_uppercase_constant_name ===
// imports: foo.Foo
// imports: static foo.Foo.MAX
class InputPreferStaticImportConstantLowercaseLocalFieldNameRewrittenToUppercaseConstantNameSliceViolation {

	int use() {
		return MAX;
	}
}
// === end ===

// === case: marker_annotation_on_same_line_as_private_fixer_succeeds ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMarkerAnnotationOnSameLineAsPrivateFixerSucceedsSliceViolation {
}
// === end ===

// === case: multi_line_alias_deletes_all_lines ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X27
class InputPreferStaticImportConstantMultiLineAliasDeletesAllLinesSliceViolation {
}
// === end ===

// === case: multi_line_alias_with_whitespace_around_dot ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X28
class InputPreferStaticImportConstantMultiLineAliasWithWhitespaceAroundDotSliceViolation {
}
// === end ===

// === case: multi_statement_on_alias_line_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantMultiStatementOnAliasLineReturnsSkipSliceViolation {
	private static final int X = Foo.X; int leftover = 7;
}
// === end ===

// === case: multi_statement_with_mid_line_block_comment_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class Holder { private static final int X = Foo.X; /* note */ }
// === end ===

// === case: multi_var_all_aliases_no_usages_with_own_line_annotation ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAllAliasesNoUsagesWithOwnLineAnnotationSliceViolation {
}
// === end ===

// === case: multi_var_all_aliases_with_usages_and_own_line_annotation ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAllAliasesWithUsagesAndOwnLineAnnotationSliceViolation {

	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_annotation_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAnnotationOnDeclLineIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_annotation_on_first_line_multi_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAnnotationOnFirstLineMultiLineIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_annotation_with_body_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarAnnotationWithBodyOnDeclLineIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_annotation_with_internal_comma_and_equals_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
@interface Ann { int a(); int b(); }
class InputPreferStaticImportConstantMultiVarAnnotationWithInternalCommaAndEqualsIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_array_initializer_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarArrayInitializerInSiblingIsPreservedInRebuildSliceViolation {
	private static final int[] Y = {1, 2, 3};
}
// === end ===

// === case: multi_var_array_type_removes_first_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarArrayTypeRemovesFirstVariableSliceViolation {
}
// === end ===

// === case: multi_var_block_comment_above_decl_with_final_in_body_does_not_confuse_find_declaration_start ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarBlockCommentAboveDeclWithFinalInBodyDoesNotConfuseFindDeclarationStartSliceViolation {
}
// === end ===

// === case: multi_var_block_comment_in_prefix_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarBlockCommentInPrefixIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_block_comment_inside_kept_rhs_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarBlockCommentInsideKeptRhsIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_block_comment_with_line_comment_marker_in_content_does_not_bail ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarBlockCommentWithLineCommentMarkerInContentDoesNotBailSliceViolation {
}
// === end ===

// === case: multi_var_char_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarCharLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = ',';
}
// === end ===

// === case: multi_var_char_literal_with_escape_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarCharLiteralWithEscapeInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = '\n';
}
// === end ===

// === case: multi_var_comment_on_decl_line_returns_multi_var_skip ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarCommentOnDeclLineReturnsMultiVarSkipSliceViolation {
	private static final int X = Foo.X, Y = Bar.Y; // important note
}
// === end ===

// === case: multi_var_conflicting_first_alias_is_kept ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static other.Other.X
// imports: static foo.Bar.Y
class InputPreferStaticImportConstantMultiVarConflictingFirstAliasIsKeptSliceViolation {
	private static final int X = Foo.X;
}
// === end ===

// === case: multi_var_conflicting_sibling_is_kept ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static other.Other.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarConflictingSiblingIsKeptSliceViolation {
	private static final int Y = Bar.Y;
}
// === end ===

// === case: multi_var_decl_line_char_literal_with_slash ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarDeclLineCharLiteralWithSlashSliceViolation {
	private static final Object S = '/';
}
// === end ===

// === case: multi_var_decl_line_string_literal_contains_slash_slash ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarDeclLineStringLiteralContainsSlashSlashSliceViolation {
	private static final Object S = "//";
}
// === end ===

// === case: multi_var_four_variables_removes_last_variable ===
// imports: foo.Foo
// imports: static foo.Foo.A
// imports: static foo.Foo.B
// imports: static foo.Foo.C
// imports: static foo.Foo.D
class InputPreferStaticImportConstantMultiVarFourVariablesRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_fqcn_removes_last_variable ===
// imports: static com.foo.Bar.Y
// imports: static com.foo.Foo.X
class InputPreferStaticImportConstantMultiVarFqcnRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_generic_type_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: java.util.Map
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarGenericTypeRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_java_lang_implicit_import_on_last_variable ===
// imports: foo.Foo
// imports: static foo.Foo.A
// imports: static java.lang.Integer.MAX_VALUE
class InputPreferStaticImportConstantMultiVarJavaLangImplicitImportOnLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_line_comment_followed_by_slash_star_does_not_confuse_block_comment_mask ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarLineCommentFollowedBySlashStarDoesNotConfuseBlockCommentMaskSliceViolation {
	// stray /* in line comment
}
// === end ===

// === case: multi_var_method_call_rhs_in_non_alias_segment_does_not_confuse_detection ===
// imports: foo.Foo
// imports: static foo.Foo.Y
class InputPreferStaticImportConstantMultiVarMethodCallRhsInNonAliasSegmentDoesNotConfuseDetectionSliceViolation {
	private static final int X = compute(1, 2);
	static int compute(int a, int b) { return a + b; }
}
// === end ===

// === case: multi_var_mixed_alias_removes_alias_variable ===
// imports: foo.Foo
// imports: static foo.Foo.Y
class InputPreferStaticImportConstantMultiVarMixedAliasRemovesAliasVariableSliceViolation {
	private static final int X = 0;
}
// === end ===

// === case: multi_var_mixed_alias_with_qualified_usage_above_decl_rebuilds ===
// imports: foo.Foo
// imports: static foo.Foo.B
class InputPreferStaticImportConstantMultiVarMixedAliasWithQualifiedUsageAboveDeclRebuildsSliceViolation {
	int use() {
		return B;
	}

	private static final int A = 0;
}
// === end ===

// === case: multi_var_mixed_alias_with_qualified_usage_rebuilds ===
// imports: foo.Foo
// imports: static foo.Foo.B
class InputPreferStaticImportConstantMultiVarMixedAliasWithQualifiedUsageRebuildsSliceViolation {
	private static final int A = 0;

	int use() {
		return A + B;
	}
}
// === end ===

// === case: multi_var_multi_line_block_comment_above_decl_does_not_confuse_mask ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarMultiLineBlockCommentAboveDeclDoesNotConfuseMaskSliceViolation {
	/* block
	   comment with """ markers */
}
// === end ===

// === case: multi_var_non_private_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarNonPrivateReturnsVisibilitySkipSliceViolation {
	static final int X = Foo.X, Y = Bar.Y;
}
// === end ===

// === case: multi_var_on_continuation_line_removes_continuation_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarOnContinuationLineRemovesContinuationVariableSliceViolation {
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

// === case: multi_var_qualified_annotation_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarQualifiedAnnotationOnDeclLineIsPreservedInRebuildSliceViolation {
}
// === end ===

// === case: multi_var_renamed_alias_on_last_variable_rewrites_usages ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarRenamedAliasOnLastVariableRewritesUsagesSliceViolation {

	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_renamed_alias_rewrites_usages ===
// imports: foo.Foo
// imports: foo.Bar
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
		int RENAMED = 5;
		return RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_alias_shadowed_by_nested_class_field_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a field with the same name elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedAliasShadowedByNestedClassFieldReturnsShadowSkipSliceViolation {
	private static final int RENAMED = Foo.X, Y = Bar.Y;

	static class Inner {
		int RENAMED;
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
		final int Y = 1;
		return X + RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_sibling_rewrites_sibling_usage ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarRenamedSiblingRewritesSiblingUsageSliceViolation {

	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_sibling_member_name_collides_returns_conflict_skip ===
// skip-reason: cannot add static import: file already imports a different constant with the same name statically
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarSiblingMemberNameCollidesReturnsConflictSkipSliceViolation {
	private static final int A = Foo.MAX, B = Bar.MAX;
}
// === end ===

// === case: multi_var_single_line_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarSingleLineRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_single_line_triple_quote_sequence_not_treated_as_text_block ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarSingleLineTripleQuoteSequenceNotTreatedAsTextBlockSliceViolation {
	private static final Object Y = """oneliner""";
}
// === end ===

// === case: multi_var_string_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarStringLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = "hello";
}
// === end ===

// === case: multi_var_string_literal_with_comma_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarStringLiteralWithCommaInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = "hello, world";
}
// === end ===

// === case: multi_var_string_literal_with_escaped_quote_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarStringLiteralWithEscapedQuoteInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = "a\"b";
}
// === end ===

// === case: multi_var_string_literal_with_semicolon_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarStringLiteralWithSemicolonInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = "a;b";
}
// === end ===

// === case: multi_var_string_literal_with_slash_star_above_decl_does_not_confuse_block_comment_mask ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarStringLiteralWithSlashStarAboveDeclDoesNotConfuseBlockCommentMaskSliceViolation {
	String S = "has /* marker";
}
// === end ===

// === case: multi_var_text_block_closes_and_continues_on_same_line ===
// imports: foo.Foo
// imports: static foo.Foo.Y
class InputPreferStaticImportConstantMultiVarTextBlockClosesAndContinuesOnSameLineSliceViolation {
	private static final String S = """
		hello
		""";
}
// === end ===

// === case: multi_var_text_block_in_first_variable_preserved_when_alias_is_second ===
// imports: foo.Foo
// imports: static foo.Foo.Y
class InputPreferStaticImportConstantMultiVarTextBlockInFirstVariablePreservedWhenAliasIsSecondSliceViolation {
	private static final String S = """
		text
		""";
}
// === end ===

// === case: multi_var_text_block_in_sibling_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X1
class InputPreferStaticImportConstantMultiVarTextBlockInSiblingSliceViolation {
	private static final Object TB_SIBLING_BLOCK = """
			a;b,c
			""";
}
// === end ===

// === case: multi_var_text_block_with_backslash_escape_in_content_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X2
class InputPreferStaticImportConstantMultiVarTextBlockWithBackslashEscapeInContentSliceViolation {
	private static final Object TB_BACKSLASH_BLOCK = """
		a\\nb
		""";
}
// === end ===

// === case: multi_var_text_block_with_final_keyword_in_content_does_not_confuse_find_declaration_start ===
// imports: foo.Foo
// imports: static foo.Foo.A
// imports: static foo.Foo.B
class InputPreferStaticImportConstantMultiVarTextBlockWithFinalKeywordInContentDoesNotConfuseFindDeclarationStartSliceViolation {
	private static final String DOC = """
		public static final int Z = 0;
		""";
}
// === end ===

// === case: multi_var_text_block_with_internal_quotes_in_sibling_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X3
class InputPreferStaticImportConstantMultiVarTextBlockWithInternalQuotesInSiblingSliceViolation {
	private static final Object TB_QUOTES_BLOCK = """
			he said "hello".
			""";
}
// === end ===

// === case: multi_var_text_block_with_line_comment_marker_in_content_does_not_bail ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X4
class InputPreferStaticImportConstantMultiVarTextBlockWithLineCommentMarkerInContentSliceViolation {
	private static final Object TB_MARKER_BLOCK = """
		code with // marker
		""";
}
// === end ===

// === case: multi_var_three_variables_on_last_continuation_line_removes_last_variable ===
// imports: foo.Foo
// imports: static foo.Foo.A
// imports: static foo.Foo.B
// imports: static foo.Foo.C
class InputPreferStaticImportConstantMultiVarThreeVariablesOnLastContinuationLineRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_three_variables_removes_last_variable ===
// imports: foo.Foo
// imports: static foo.Foo.A
// imports: static foo.Foo.B
// imports: static foo.Foo.C
class InputPreferStaticImportConstantMultiVarThreeVariablesRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_var_two_aliases_different_members_rewrite_each_to_own_member ===
// imports: foo.Foo
// imports: static foo.Foo.MAX
// imports: static foo.Foo.MIN
class InputPreferStaticImportConstantMultiVarTwoAliasesDifferentMembersRewriteEachToOwnMemberSliceViolation {

	int span() {
		return MAX - MIN;
	}
}
// === end ===

// === case: multi_var_two_aliases_qualified_usages_rewrite_each_to_own_member ===
// imports: foo.Foo
// imports: static foo.Foo.MAX
// imports: static foo.Foo.MIN
class InputPreferStaticImportConstantMultiVarTwoAliasesQualifiedUsagesRewriteEachToOwnMemberSliceViolation {

	int span() {
		return MAX - MIN;
	}
}
// === end ===

// === case: multi_var_two_kept_siblings_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.B
class InputPreferStaticImportConstantMultiVarTwoKeptSiblingsRebuildSliceViolation {
	private static final int A = 0, C = 1;
}
// === end ===

// === case: multi_var_two_line_block_comment_above_decl_start ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarTwoLineBlockCommentAboveDeclStartSliceViolation {
}
// === end ===

// === case: multi_var_url_in_string_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarUrlInStringLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object Y = "https://example.com";
}
// === end ===

// === case: multi_var_with_annotation_on_separated_line_above_does_not_misidentify_own_decl_as_shadow ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarWithAnnotationOnSeparatedLineAboveDoesNotMisidentifyOwnDeclAsShadowSliceViolation {

	int use() {
		return X + Y;
	}
}
// === end ===

// === case: multi_var_with_block_comment_closing_on_decl_line_does_not_confuse_find_declaration_start ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarWithBlockCommentClosingOnDeclLineDoesNotConfuseFindDeclarationStartSliceViolation {
	/* opens block
	*/ private static final int X = Foo.X, Y = Bar.Y;
}
// === end ===

// === case: multi_var_with_parens_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static foo.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultiVarWithParensRemovesLastVariableSliceViolation {
}
// === end ===

// === case: multi_wildcard ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
// imports: java.util.*
class InputPreferStaticImportConstantMultipleWildcardImportsFireFromCheckSliceViolation {
	private static final int MULTI_WILDCARD_X = AnchorClass.MULTI_WILDCARD_X;
}
// === end ===

// === case: multiple_annotations_on_same_line_fixer_succeeds ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultipleAnnotationsOnSameLineFixerSucceedsSliceViolation {
}
// === end ===

// === case: multiple_qualified_usages_on_same_line_all_rewritten ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultipleQualifiedUsagesOnSameLineAllRewrittenSliceViolation {

	int use() {
		return X + X;
	}
}
// === end ===

// === case: multiple_separated_annotations_above_delete_all_annotation_lines ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantMultipleSeparatedAnnotationsAboveSliceViolation {
}
// === end ===

// === case: multiple_usage_lines_with_intermediate_line_preserved ===
// imports: foo.Foo
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

// === case: multiple_wildcards_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.*
// imports: bar.*
class InputPreferStaticImportConstantMultipleWildcardsReturnsSkipSliceViolation {
	private static final int X = Foo.X;
}
// === end ===

// === case: multivar_fq ===
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantFqSliceViolation {
}
// === end ===

// === case: multivar_map ===
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X13
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X14
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: java.util.Map
class InputPreferStaticImportConstantMapSliceViolation {
}
// === end ===

// === case: multivar_mixed ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X5
class InputPreferStaticImportConstantMixedSliceViolation {
	private static final int MIXED_A = 0;
}
// === end ===

// === case: multivar_multi ===
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X3
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X4
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiSliceViolation {
}
// === end ===

// === case: multivar_paren ===
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X10
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X9
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantParenSliceViolation {
}
// === end ===

// === case: multivar_tri ===
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X6
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X7
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X8
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantTriSliceViolation {
}
// === end ===

// === case: nested_annotation_arg_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantNestedAnnotationArgDoesNotMisidentifyVisibilitySliceViolation {
	@MyAnno(@Other("private")) static final int X = Foo.X;
}
// === end ===

// === case: nested_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.Inner.X16
class InputPreferStaticImportConstantNestedCinitSliceViolation {
}
// === end ===

// === case: nested_class ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.NESTED
class InputPreferStaticImportConstantNestedClassSliceViolation {
	static class NestedClass {
	}
}
// === end ===

// === case: nested_class_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.Inner.X16
class InputPreferStaticImportConstantNestedClassAliasSliceViolation {
}
// === end ===

// === case: nested_class_chain_usage_in_method_body_rewritten ===
// imports: foo.Outer
// imports: static foo.Outer.Inner.X
class InputPreferStaticImportConstantNestedClassChainUsageInMethodBodyRewrittenSliceViolation {

	int use() {
		return X;
	}
}
// === end ===

// === case: nested_class_rhs_resolves_via_simple_class ===
// imports: foo.Outer
// imports: static foo.Outer.Inner.X
class InputPreferStaticImportConstantNestedClassRhsResolvesViaSimpleClassSliceViolation {
}
// === end ===

// === case: nested_type_import_resolves_to_full_path ===
// imports: foo.Bar.Inner
// imports: static foo.Bar.Inner.X
class InputPreferStaticImportConstantNestedTypeImportResolvesToFullPathSliceViolation {
}
// === end ===

// === case: no_method_body_usage_deletes_field_only ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantNoMethodBodyUsageDeletesFieldOnlySliceViolation {

	int use() {
		return 0;
	}
}
// === end ===

// === case: non_conflicting_static_import_succeeds ===
// imports: foo.Foo
// imports: static other.Bar.Y
// imports: static foo.Foo.X
class InputPreferStaticImportConstantNonConflictingStaticImportSucceedsSliceViolation {
}
// === end ===

// === case: other_suppress_key ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantOtherSuppressKeySliceViolation {
	@SuppressWarnings("unused")
	private static final int OTHER_SUPPRESS_KEY = AnchorClass.OTHER_SUPPRESS_KEY;
}
// === end ===

// === case: package_private_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X1
class InputPreferStaticImportConstantPackagePrivateAliasFiresSliceViolation {
	static final int PACKAGE_PRIVATE_ALIAS = AnchorClass.X1;
}
// === end ===

// === case: package_private_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPackagePrivateAliasReturnsVisibilitySkipSliceViolation {
	static final int X = Foo.X;
}
// === end ===

// === case: paren_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X23
class InputPreferStaticImportConstantParenCinitSliceViolation {
}
// === end ===

// === case: parenthesized_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11
class InputPreferStaticImportConstantParenthesizedAliasSliceViolation {
}
// === end ===

// === case: private_inside_comment_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPrivateInsideCommentDoesNotMisidentifyVisibilitySliceViolation {
	public /* private */ static final int X = Foo.X;
}
// === end ===

// === case: protected_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X2
class InputPreferStaticImportConstantProtectedAliasFiresSliceViolation {
	protected static final int PROTECTED_ALIAS = AnchorClass.X2;
}
// === end ===

// === case: protected_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantProtectedAliasReturnsVisibilitySkipSliceViolation {
	protected static final int X = Foo.X;
}
// === end ===

// === case: public_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X3
class InputPreferStaticImportConstantPublicAliasFiresSliceViolation {
	public static final int PUBLIC_ALIAS = AnchorClass.X3;
}
// === end ===

// === case: public_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPublicAliasReturnsVisibilitySkipSliceViolation {
	public static final int X = Foo.X;
}
// === end ===

// === case: qualified_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X26
class InputPreferStaticImportConstantQualifiedCinitSliceViolation {
}
// === end ===

// === case: qualified_usage_before_field_decl_is_rewritten ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageBeforeFieldDeclIsRewrittenSliceViolation {
	int use() {
		return X;
	}

}
// === end ===

// === case: qualified_usage_inside_block_comment_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentIsPreservedSliceViolation {

	/** See {@link Foo#X} for details. */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_block_comment_spanning_lines_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentSpanningLinesIsPreservedSliceViolation {

	/*
	 * mentions Foo.X here
	 */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_line_comment_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideLineCommentIsPreservedSliceViolation {

	int use() {
		return 0; // see Foo.X above
	}
}
// === end ===

// === case: qualified_usage_inside_string_literal_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideStringLiteralIsPreservedSliceViolation {

	String use() {
		return "Foo.X";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageInsideTextBlockIsPreservedSliceViolation {

	String use() {
		return """
			does Foo.X stuff
			""";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_with_backslash_escape_is_preserved ===
// imports: foo.Foo
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
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsagePrecededByDotOnDifferentObjectNotRewrittenSliceViolation {

	int use(Other other) {
		return X + other.Foo.X;
	}
}
// === end ===

// === case: qualified_usage_with_longer_suffix_is_not_rewritten ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantQualifiedUsageWithLongerSuffixIsNotRewrittenSliceViolation {

	int use() {
		return X + Foo.XLong;
	}
}
// === end ===

// === case: record_field ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.RECORD_FIELD
class InputPreferStaticImportConstantRecordFieldSliceViolation {
	record InnerRecord(int x) {}
}
// === end ===

// === case: record_field_with_lcurly_line_content ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.RECORD_LCURLY_CONTENT
class InputPreferStaticImportConstantRecordFieldWithLcurlyLineContentSliceViolation {
	record InnerRecord(int x) { /* note */
	}
}
// === end ===

// === case: record_field_with_rcurly_line_content ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.RECORD_RCURLY_CONTENT
class InputPreferStaticImportConstantRecordFieldWithRcurlyLineContentSliceViolation {
	record InnerRecord(int x) {
	/* note */ }
}
// === end ===

// === case: record_field_with_stray_semicolon_sibling ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.RECORD_STRAY_SEMI
class InputPreferStaticImportConstantRecordFieldWithStraySemicolonSiblingSliceViolation {
	record InnerRecord(int x) {
		;
	}
}
// === end ===

// === case: rename_target_collides_in_multi_var_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantRenameTargetCollidesInMultiVarSkipsFixSliceViolation {
	private static final int A = Foo.A, RENAMED = Foo.X;

	static class X {}
}
// === end ===

// === case: renamed ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X6
class InputPreferStaticImportConstantRenamedSliceViolation {
}
// === end ===

// === case: renamed_alias_label_with_same_name_in_method_body_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasLabelWithSameNameInMethodBodyIsPreservedSliceViolation {

	int use() {
		RENAMED:
		for (int i = 0; i < 5; ++i) {
			if (i > 0)
				return X;
		}
		return 0;
	}
}
// === end ===

// === case: renamed_alias_own_declaration_is_not_its_own_shadow ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasOwnDeclarationIsNotItsOwnShadowSliceViolation {

	int use() {
		return X;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_method_body_rewritten_to_constant_name ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasReferencedInMethodBodyRewrittenToConstantNameSliceViolation {

	int use() {
		return X;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_string_literal_is_preserved ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasReferencedInStringLiteralIsPreservedSliceViolation {

	String use() {
		return "RENAMED";
	}
	int val() { return X; }
}
// === end ===

// === case: renamed_alias_with_annotation_on_separated_line_above_does_not_misidentify_own_decl_as_shadow ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasWithAnnotationOnSeparatedLineAboveDoesNotMisidentifyOwnDeclAsShadowSliceViolation {
}
// === end ===

// === case: renamed_alias_with_both_qualified_and_local_usages_rewritten ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasWithBothQualifiedAndLocalUsagesRewrittenSliceViolation {

	int use() {
		return X + X;
	}
}
// === end ===

// === case: renamed_alias_with_separated_annotation_and_body_usage_rewrites ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRenamedAliasWithSeparatedAnnotationAndBodyUsageRewritesSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: rhs_comment_between_class_and_dot_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRhsCommentBetweenClassAndDotResolvesSliceViolation {
}
// === end ===

// === case: rhs_fivefold_nested_parens_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRhsFivefoldNestedParensResolvesSliceViolation {
}
// === end ===

// === case: rhs_nested_parens_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRhsNestedParensResolvesSliceViolation {
}
// === end ===

// === case: rhs_parenthesized_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRhsParenthesizedResolvesSliceViolation {
}
// === end ===

// === case: rhs_triple_nested_parens_resolves ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantRhsTripleNestedParensResolvesSliceViolation {
}
// === end ===

// === case: same_class_static_import_already_present_does_not_conflict ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSameClassStaticImportAlreadyPresentDoesNotConflictSliceViolation {
}
// === end ===

// === case: same_class_used_in_field_and_qualified_method_body_rewrites_usage ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSameClassUsedInFieldAndQualifiedMethodBodyRewritesUsageSliceViolation {

	int use() {
		return X;
	}
}
// === end ===

// === case: same_package ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.InputPreferStaticImportConstantSamePackageHelper.MAX
class InputPreferStaticImportConstantSamePackageResolvableFiresSliceViolation {
}
// === end ===

// === case: separated_annotation_above_non_renamed_deletes_annotation_too ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSeparatedAnnotationAboveNonRenamedSliceViolation {
}
// === end ===

// === case: separated_annotation_above_non_renamed_with_qualified_usage_rewrites ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSeparatedAnnotationAboveNonRenamedWithUsageSliceViolation {
	int use() {
		return X;
	}
}
// === end ===

// === case: separated_annotation_with_trailing_line_comment_above_deletes_all ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSeparatedAnnotationWithTrailingLineCommentAboveSliceViolation {
}
// === end ===

// === case: shadowing_annotation_type_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingAnnotationTypeWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	@interface X {}
}
// === end ===

// === case: shadowing_array_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingArrayTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		int[] RENAMED = new int[5];
	}
}
// === end ===

// === case: shadowing_catch_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a catch parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingCatchParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		try {} catch (Exception RENAMED) {}
	}
}
// === end ===

// === case: shadowing_constructor_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a constructor parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingConstructorParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	InputPreferStaticImportConstantShadowingConstructorParameterSkipsFixSliceViolation(int RENAMED) {}
}
// === end ===

// === case: shadowing_enum_constant_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingEnumConstantWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	enum E { X }
}
// === end ===

// === case: shadowing_for_each_var_skips_fix ===
// skip-reason: renamed alias's local name clashes with a for-each variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingForEachVarSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use(int[] arr) {
		for (var RENAMED : arr) {}
	}
}
// === end ===

// === case: shadowing_for_init_skips_fix ===
// skip-reason: renamed alias's local name clashes with a for-loop variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingForInitSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		for (int RENAMED = 0; RENAMED < 10; ++RENAMED) {}
	}
}
// === end ===

// === case: shadowing_generic_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		Map<String, Integer> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_lambda_multi_param_without_types_skips_fix ===
// skip-reason: renamed alias's local name clashes with a lambda parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLambdaMultiParamWithoutTypesSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		do2((a, RENAMED) -> RENAMED + 1);
	}
}
// === end ===

// === case: shadowing_lambda_single_param_skips_fix ===
// skip-reason: renamed alias's local name clashes with a lambda parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLambdaSingleParamSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		java.util.stream.Stream.of(1).map(RENAMED -> RENAMED + 1);
	}
}
// === end ===

// === case: shadowing_local_declaration_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLocalDeclarationSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int use() {
		int RENAMED = 5;
		return RENAMED;
	}
}
// === end ===

// === case: shadowing_method_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingMethodWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int X() { return 0; }
}
// === end ===

// === case: shadowing_nested_class_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedClassWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	static class X {}
}
// === end ===

// === case: shadowing_nested_enum_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedEnumWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	enum X { A }
}
// === end ===

// === case: shadowing_nested_field_with_same_name_skips_fix ===
// skip-reason: renamed alias's local name clashes with a field with the same name elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedFieldWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	static class Inner {
		int RENAMED;
	}
}
// === end ===

// === case: shadowing_nested_generic_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() {
		Map<String, List<Integer>> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_nested_interface_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedInterfaceWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	interface X {}
}
// === end ===

// === case: shadowing_nested_record_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedRecordWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	record X(int n) {}
}
// === end ===

// === case: shadowing_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a method parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int use(int RENAMED) {
		return RENAMED;
	}
}
// === end ===

// === case: shadowing_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	int use() {
		Foo RENAMED = null;
		return 0;
	}
}
// === end ===

// === case: shadowing_try_with_resources_variable_skips_fix ===
// skip-reason: renamed alias's local name clashes with a try-with-resources variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingTryWithResourcesVariableSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X;

	void use() throws Exception {
		try (java.io.Closeable RENAMED = null) {}
	}
}
// === end ===

// === case: shadowing_type_parameter_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingTypeParameterWithSameNameSkipsFixSliceViolation<X> {
	private static final int RENAMED = Foo.X;
}
// === end ===

// === case: single_var_with_annotation_arg_containing_comma_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSingleVarWithAnnotationArgContainingCommaIsNotMisidentifiedAsMultiVarSliceViolation {
}
// === end ===

// === case: single_var_with_comment_containing_comma_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSingleVarWithCommentContainingCommaIsNotMisidentifiedAsMultiVarSliceViolation {
}
// === end ===

// === case: single_var_with_generic_type_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: java.util.Map
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSingleVarWithGenericTypeIsNotMisidentifiedAsMultiVarSliceViolation {
}
// === end ===

// === case: single_var_with_three_arg_generic_type_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: java.util.function.BiFunction
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSingleVarWithThreeArgGenericTypeIsNotMisidentifiedAsMultiVarSliceViolation {
}
// === end ===

// === case: split_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X24
class InputPreferStaticImportConstantSplitAliasSliceViolation {
}
// === end ===

// === case: static_import_inside_text_block_ignored_for_conflict_detection ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantStaticImportInsideTextBlockIgnoredForConflictDetectionSliceViolation {
	String doc = """
		import static other.X;
		""";
}
// === end ===

// === case: string_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.STRING_ALIAS
class InputPreferStaticImportConstantStringAliasSliceViolation {
}
// === end ===

// === case: switch_case_at_column_zero_reference_renamed_field ===
// imports: foo.Foo
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
// imports: foo.Foo
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
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryBlockCommentBetweenQuestionAndIdentReferenceRenamedFieldSliceViolation {

	int use(boolean flag) {
		return flag ? /* note */ X : 0;
	}
}
// === end ===

// === case: ternary_multi_line_reference_renamed_field ===
// imports: foo.Foo
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
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryNoWhitespaceReferenceRenamedFieldSliceViolation {

	int use(boolean flag) {
		return flag?X:0;
	}
}
// === end ===

// === case: ternary_reference_renamed_field ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantTernaryReferenceRenamedFieldSliceViolation {

	int use(boolean flag) {
		return flag ? X : 0;
	}
}
// === end ===

// === case: wildcard ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
// imports: static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.WILDCARD_X
class InputPreferStaticImportConstantWildcardImportResolvesAndFiresSliceViolation {
}
// === end ===

// === case: wildcard_before_explicit_still_picks_explicit ===
// imports: wild.*
// imports: other.Foo
// imports: static other.Foo.X
class InputPreferStaticImportConstantWildcardBeforeExplicitStillPicksExplicitSliceViolation {
}
// === end ===

// === case: wildcard_import_fallback ===
// imports: foo.*
// imports: static foo.Foo.X
class InputPreferStaticImportConstantWildcardImportFallbackSliceViolation {
}
// === end ===