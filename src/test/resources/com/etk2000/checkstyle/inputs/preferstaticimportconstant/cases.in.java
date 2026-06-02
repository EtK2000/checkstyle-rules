package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

// === case: annotated_alias ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantAnnotatedAliasSliceViolation {
	@Deprecated
	private static final int ANNOTATED_ALIAS = AnchorClass.ANNOTATED_ALIAS; // violation: Replace 'ANNOTATED_ALIAS' alias of 'AnchorClass.ANNOTATED_ALIAS' with a static import.
}
// === end ===

// === case: annotation_arg_containing_equals_before_real_equals_honors_private ===
// imports: foo.Foo
class InputPreferStaticImportConstantAnnotationArgContainingEqualsBeforeRealEqualsHonorsPrivateSliceViolation {
	@SuppressWarnings(value = "rawtypes") private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: annotation_arg_containing_private_string_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantAnnotationArgContainingPrivateStringDoesNotMisidentifyVisibilitySliceViolation {
	@SuppressWarnings("private-key") static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: annotation_arg_unbalanced_paren_inside_string_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantAnnotationArgUnbalancedParenInsideStringDoesNotMisidentifyVisibilitySliceViolation {
	@Description("foo) private bar(") static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: array_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantArrayAliasSliceViolation {
	private static final int[] ARRAY_ALIAS = AnchorClass.ARRAY_ALIAS; // violation: Replace 'ARRAY_ALIAS' alias of 'AnchorClass.ARRAY_ALIAS' with a static import.
}
// === end ===

// === case: block_comment_straddling_into_multi_var_decl_returns_skip ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
class InputPreferStaticImportConstantBlockCommentStraddlingIntoMultiVarDeclSliceViolation {
	/* note
	*/ private static final int A = 0, B = Foo.B; // violation: Replace 'B' alias of 'Foo.B' with a static import.
}
// === end ===

// === case: block_comment_straddling_into_separated_annotation_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantBlockCommentStraddlingIntoSeparatedAnnotationSliceViolation {
	/* note
	*/ @Deprecated

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: c_style_array_suffix_field_name_parses ===
// imports: foo.Foo
class InputPreferStaticImportConstantCStyleArraySuffixFieldNameParsesSliceViolation {
	private static final int X[][] = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: c_style_array_suffix_with_whitespace_parses ===
// imports: foo.Foo
class InputPreferStaticImportConstantCStyleArraySuffixWithWhitespaceParsesSliceViolation {
	private static final int X [] = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: canonical_alias_blank_above_only_deletes_line_only ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantCanonicalAliasBlankAboveOnlyDeletesLineOnlySliceViolation {
	private static final int A_BEFORE = 1;

	private static final int CANONICAL_BLANK_ABOVE = AnchorClass.X1; // violation: Replace 'CANONICAL_BLANK_ABOVE' alias of 'AnchorClass.X1' with a static import.
	private static final int Z_AFTER = 2;
}
// === end ===

// === case: canonical_alias_no_blanks_deletes_line_only ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantCanonicalAliasNoBlanksDeletesLineOnlySliceViolation {
	private static final int A_BEFORE = 1;
	private static final int CANONICAL_NO_BLANKS = AnchorClass.X2; // violation: Replace 'CANONICAL_NO_BLANKS' alias of 'AnchorClass.X2' with a static import.
	private static final int Z_AFTER = 2;
}
// === end ===

// === case: canonical_alias_with_surrounding_blanks_collapses_pair ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantCanonicalAliasWithSurroundingBlanksCollapsesPairSliceViolation {
	private static final int A_BEFORE = 1;

	private static final int CANONICAL_SURROUND = AnchorClass.X3; // violation: Replace 'CANONICAL_SURROUND' alias of 'AnchorClass.X3' with a static import.

	private static final int Z_AFTER = 2;
}
// === end ===

// === case: char_literal_in_method_body_is_preserved_during_rewrite ===
// imports: foo.Foo
class InputPreferStaticImportConstantCharLiteralInMethodBodyIsPreservedDuringRewriteSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		char q = '\''; return Foo.X;
	}
}
// === end ===

// === case: cinit_assignment_sharing_line_with_trailing_statement_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentSharingLineWithTrailingStatementReturnsCinitSkipSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X; int y = 0;
	}
}
// === end ===

// === case: cinit_assignment_spanning_multiple_lines_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitAssignmentSpanningMultipleLinesReturnsCinitSkipSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

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
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		int y = 0; X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_block_comment_closing_on_decl_line_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithBlockCommentClosingOnDeclLineReturnsSkipSliceViolation {
	/* opens
	*/ private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_closer_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticCloserLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	/* close note */ }
}
// === end ===

// === case: cinit_blank_final_with_comment_on_static_opener_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithCommentOnStaticOpenerLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static { // open note
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_decl_after_static_block_on_same_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithDeclAfterStaticBlockOnSameLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	} private static final int Y = 0;
}
// === end ===

// === case: cinit_blank_final_with_decl_before_static_block_on_same_opener_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithDeclBeforeStaticBlockOnSameOpenerLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	private int Z = 7; static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_in_static_block_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentInStaticBlockKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		// important context: do not drop
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_leading_comment_on_cinit_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithLeadingCommentOnCinitLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		/* lead */ X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_qualified_assignment_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithQualifiedAssignmentIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		InputPreferStaticImportConstantCinitBlankFinalWithQualifiedAssignmentIsAutoFixedSliceViolation.X = Foo.X;
	}
}
// === end ===

// === case: cinit_blank_final_with_stray_semicolons_in_static_block_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithStraySemicolonsInStaticBlockIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		;
		X = Foo.X;
		;
	}
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_in_static_block_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentInStaticBlockKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
		// trailing note
	}
}
// === end ===

// === case: cinit_blank_final_with_trailing_comment_on_cinit_line_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitBlankFinalWithTrailingCommentOnCinitLineKeepsBlockSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X; // trailing
	}
}
// === end ===

// === case: cinit_default_package_bare_lhs_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitDefaultPackageBareLhsIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_find_field_def_disambiguates_by_column_on_same_line_nested_classes ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitFindFieldDefDisambiguatesByColumnOnSameLineNestedClassesSliceViolation { static class A { private static final int X; static { X = Foo.X; } } static class B { private static final int X = 5; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_fqn_lhs_assignment_is_auto_fixed ===
// package: x
// imports: foo.Foo
class InputPreferStaticImportConstantCinitFqnLhsAssignmentIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		x.InputPreferStaticImportConstantCinitFqnLhsAssignmentIsAutoFixedSliceViolation.X = Foo.X;
	}
}
// === end ===

// === case: cinit_fqn_lhs_with_leading_non_assign_statement_is_auto_fixed ===
// package: x
// imports: foo.Foo
class InputPreferStaticImportConstantCinitFqnLhsWithLeadingNonAssignStatementIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		System.out.println();
		x.InputPreferStaticImportConstantCinitFqnLhsWithLeadingNonAssignStatementIsAutoFixedSliceViolation.X = Foo.X;
	}
}
// === end ===

// === case: cinit_inside_enum_body_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitInsideEnumBodyIsAutoFixedSliceViolation {
	enum E {
		A;
		private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

		static {
			X = Foo.X;
		}
	}
}
// === end ===

// === case: cinit_inside_record_body_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitInsideRecordBodyIsAutoFixedSliceViolation {
	record R(int x) {
		private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

		static {
			X = Foo.X;
		}
	}
}
// === end ===

// === case: cinit_java_lang_implicit_import_is_auto_fixed ===
class InputPreferStaticImportConstantCinitJavaLangImplicitImportIsAutoFixedSliceViolation {
	private static final double X; // violation: Replace 'X' alias of 'Math.PI' with a static import.

	static {
		X = Math.PI;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_blank_final_with_equals_in_annotation_arg_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedBlankFinalWithEqualsInAnnotationArgIsAutoFixedSliceViolation {
	@SuppressWarnings(value = "unused")
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_block_comment_before_annotation_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithBlockCommentBeforeAnnotationIsAutoFixedSliceViolation {
	/* note */ @SuppressWarnings("u")
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_content_before_annotation_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithContentBeforeAnnotationReturnsCinitSkipSliceViolation {
	int z; @SuppressWarnings("u")
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_intermediate_blank_line_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithIntermediateBlankLineIsAutoFixedSliceViolation {
	@SuppressWarnings("u")

	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_intermediate_comment_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithIntermediateCommentLineReturnsCinitSkipSliceViolation {
	@SuppressWarnings("u")
	/* intermediate */
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_trailing_block_comment_after_field_semi_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithTrailingBlockCommentAfterFieldSemiIsAutoFixedSliceViolation {
	@SuppressWarnings("u")
	private static final int X; /* trailing */ // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_trailing_statement_after_field_semi_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithTrailingStatementAfterFieldSemiReturnsCinitSkipSliceViolation {
	@SuppressWarnings("u")
	private static final int X; private int Y; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_annotated_field_with_zero_indent_annotation_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineAnnotatedFieldWithZeroIndentAnnotationIsAutoFixedSliceViolation {
@SuppressWarnings("unused")
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_multi_line_blank_final_with_trailing_line_comment_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitMultiLineBlankFinalWithTrailingLineCommentIsAutoFixedSliceViolation {
	private static final int X; // legacy alias // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_non_private_blank_final_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantCinitNonPrivateBlankFinalReturnsVisibilitySkipSliceViolation {
	public static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_qualified_lhs_with_internal_whitespace_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitQualifiedLhsWithInternalWhitespaceIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		InputPreferStaticImportConstantCinitQualifiedLhsWithInternalWhitespaceIsAutoFixedSliceViolation . X = Foo.X;
	}
}
// === end ===

// === case: cinit_renamed_alias_shadowed_by_local_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedAliasShadowedByLocalReturnsShadowSkipSliceViolation {
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

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
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
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
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

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
class InputPreferStaticImportConstantCinitRenamedWithBareLocalUsageRewritesToConstantSliceViolation {
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static {
		RENAMED = Foo.X;
	}

	int use() {
		return RENAMED;
	}
}
// === end ===

// === case: cinit_renamed_with_no_body_usage_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedWithNoBodyUsageIsAutoFixedSliceViolation {
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static {
		RENAMED = Foo.X;
	}
}
// === end ===

// === case: cinit_renamed_with_qualified_local_usage_rewrites_to_constant ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitRenamedWithQualifiedLocalUsageRewritesToConstantSliceViolation {
	private static final int RENAMED; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static {
		RENAMED = Foo.X;
	}

	int use() {
		return RENAMED + Foo.X;
	}
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitIsAutoFixedSliceViolation { private static final int X; static { X = Foo.X; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_no_whitespace_around_static_keyword_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitNoWhitespaceAroundStaticKeywordIsAutoFixedSliceViolation { private static final int X;static{X=Foo.X;} } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_target_collides_returns_rename_target_skip ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedTargetCollidesReturnsRenameTargetSkipSliceViolation { private static final int RENAMED; static { RENAMED = Foo.X; } int X() { return 0; } } // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_body_usage_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithBodyUsageIsAutoFixedSliceViolation { private static final int RENAMED; static { RENAMED = Foo.X; } int use() { return RENAMED; } } // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_no_body_usage_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithNoBodyUsageIsAutoFixedSliceViolation { private static final int RENAMED; static { RENAMED = Foo.X; } } // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_substring_in_tail_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitRenamedWithSubstringInTailIsAutoFixedSliceViolation { private static final int RENAMED; static { RENAMED = Foo.X; } int RENAMEDISH() { return 1; } } // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_annotated_field_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithAnnotatedFieldIsAutoFixedSliceViolation { @SuppressWarnings("unused") private static final int X; static { X = Foo.X; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_annotation_on_prior_line_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithAnnotationOnPriorLineReturnsCinitSkipSliceViolation {
	@SuppressWarnings("unused")
	private static final int X; static { X = Foo.X; } // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_comment_inside_static_block_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithCommentInsideStaticBlockKeepsBlockSliceViolation { private static final int X; static { /* note */ X = Foo.X; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_extra_statement_in_static_block_keeps_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithExtraStatementInStaticBlockKeepsBlockSliceViolation { private static final int X; static { X = Foo.X; int y = 0; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_cinit_with_multi_line_static_block_returns_cinit_skip ===
// skip-reason: could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndCinitWithMultiLineStaticBlockReturnsCinitSkipSliceViolation { private static final int X; static { X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
}
// === end ===

// === case: cinit_same_line_decl_and_fqn_cinit_is_auto_fixed ===
// package: x
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndFqnCinitIsAutoFixedSliceViolation { private static final int X; static { x.InputPreferStaticImportConstantCinitSameLineDeclAndFqnCinitIsAutoFixedSliceViolation.X = Foo.X; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_same_line_decl_and_qualified_cinit_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitSameLineDeclAndQualifiedCinitIsAutoFixedSliceViolation { private static final int X; static { InputPreferStaticImportConstantCinitSameLineDeclAndQualifiedCinitIsAutoFixedSliceViolation.X = Foo.X; } } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: cinit_with_leading_non_assign_statement_is_auto_fixed ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitWithLeadingNonAssignStatementIsAutoFixedSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	static {
		System.out.println();
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_with_sibling_fields_between_field_and_cinit_preserves_them ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitWithSiblingFieldsBetweenFieldAndCinitPreservesThemSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
	private int other1;
	private int other2;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_with_text_block_sibling_field_preserves_content ===
// imports: foo.Foo
class InputPreferStaticImportConstantCinitWithTextBlockSiblingFieldPreservesContentSliceViolation {
	private static final int X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
	private static final String DOC = """
			X = Bar.X
			""";

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: class_field_with_stray_semicolon_sibling ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantClassFieldWithStraySemicolonSiblingSliceViolation {
	;
	private static final int CLASS_STRAY_SEMI = AnchorClass.CLASS_STRAY_SEMI; // violation: Replace 'CLASS_STRAY_SEMI' alias of 'AnchorClass.CLASS_STRAY_SEMI' with a static import.
}
// === end ===

// === case: deeply_nested_dot_chain ===
class InputPreferStaticImportConstantDeeplyNestedDotChainSliceViolation {
	private static final int DEEPLY_NESTED_DOT_CHAIN = a0.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.a23.a24.a25.a26.a27.a28.a29.a30.a31.a32.a33.a34.a35.a36.a37.a38.a39.a40.a41.a42.a43.a44.a45.a46.a47.a48.a49; // violation: Replace 'DEEPLY_NESTED_DOT_CHAIN' alias of 'a0.a1.a2.a3.a4.a5.a6.a7.a8.a9.a10.a11.a12.a13.a14.a15.a16.a17.a18.a19.a20.a21.a22.a23.a24.a25.a26.a27.a28.a29.a30.a31.a32.a33.a34.a35.a36.a37.a38.a39.a40.a41.a42.a43.a44.a45.a46.a47.a48.a49' with a static import.
}
// === end ===

// === case: deeply_nested_parens_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantDeeplyNestedParensAliasSliceViolation {
	private static final int DEEPLY_NESTED_PARENS_ALIAS = (((AnchorClass.X23))); // violation: Replace 'DEEPLY_NESTED_PARENS_ALIAS' alias of 'AnchorClass.X23' with a static import.
}
// === end ===

// === case: enum_field ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldSliceViolation {
	enum InnerEnum {
		A;

		private static final int ENUM_FIELD = AnchorClass.ENUM_FIELD; // violation: Replace 'ENUM_FIELD' alias of 'AnchorClass.ENUM_FIELD' with a static import.
	}
}
// === end ===

// === case: enum_field_only_member ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldOnlyMemberSliceViolation {
	enum InnerEnum {
		;
		private static final int ENUM_ONLY_MEMBER = AnchorClass.ENUM_ONLY_MEMBER; // violation: Replace 'ENUM_ONLY_MEMBER' alias of 'AnchorClass.ENUM_ONLY_MEMBER' with a static import.
	}
}
// === end ===

// === case: enum_field_with_content_after_semi ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldWithContentAfterSemiSliceViolation {
	enum InnerEnum {
		A; /* note */
		private static final int ENUM_CONTENT_AFTER_SEMI = AnchorClass.ENUM_CONTENT_AFTER_SEMI; // violation: Replace 'ENUM_CONTENT_AFTER_SEMI' alias of 'AnchorClass.ENUM_CONTENT_AFTER_SEMI' with a static import.
	}
}
// === end ===

// === case: enum_field_with_multi_blank_before_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldWithMultiBlankBeforeSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A


		;
		private static final int ENUM_MULTI_BLANK_SEMI = AnchorClass.ENUM_MULTI_BLANK_SEMI; // violation: Replace 'ENUM_MULTI_BLANK_SEMI' alias of 'AnchorClass.ENUM_MULTI_BLANK_SEMI' with a static import.
	}
}
// === end ===

// === case: enum_field_with_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldWithSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A
		;
		private static final int ENUM_SEMI_OWN_LINE = AnchorClass.ENUM_SEMI_OWN_LINE; // violation: Replace 'ENUM_SEMI_OWN_LINE' alias of 'AnchorClass.ENUM_SEMI_OWN_LINE' with a static import.
	}
}
// === end ===

// === case: enum_field_with_single_blank_before_semi_on_own_line ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantEnumFieldWithSingleBlankBeforeSemiOnOwnLineSliceViolation {
	enum InnerEnum {
		A

		;
		private static final int ENUM_SINGLE_BLANK_SEMI = AnchorClass.ENUM_SINGLE_BLANK_SEMI; // violation: Replace 'ENUM_SINGLE_BLANK_SEMI' alias of 'AnchorClass.ENUM_SINGLE_BLANK_SEMI' with a static import.
	}
}
// === end ===

// === case: equals_inside_comment_before_real_equals_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantEqualsInsideCommentBeforeRealEqualsResolvesSliceViolation {
	private static final int X /* = note */ = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: explicit_import_wins_over_wildcard ===
// imports: other.Foo
// imports: wild.*
class InputPreferStaticImportConstantExplicitImportWinsOverWildcardSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: explicit_java_lang_import_overrides_whitelist ===
// imports: java.lang.Integer
class InputPreferStaticImportConstantExplicitJavaLangImportOverridesWhitelistSliceViolation {
	private static final int X = Integer.MAX_VALUE; // violation: Replace 'X' alias of 'Integer.MAX_VALUE' with a static import.
}
// === end ===

// === case: explicit_non_java_lang_integer_import_wins_over_whitelist ===
// imports: other.Integer
class InputPreferStaticImportConstantExplicitNonJavaLangIntegerImportWinsOverWhitelistSliceViolation {
	private static final int X = Integer.MAX_VALUE; // violation: Replace 'X' alias of 'Integer.MAX_VALUE' with a static import.
}
// === end ===

// === case: find_statement_end_accepts_trailing_block_comment ===
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndAcceptsTrailingBlockCommentSliceViolation {
	private static final int X = Foo.X; /* tail */ // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_handles_escaped_quote_in_string_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndHandlesEscapedQuoteInStringLiteralSliceViolation {
	String A = "a\";b"; private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_skips_block_comment_spanning_lines ===
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsBlockCommentSpanningLinesSliceViolation {
	private static final int X = /* multi-line // violation: Replace 'X' alias of 'Foo.X' with a static import.
		comment ; ignore */ Foo.X;
}
// === end ===

// === case: find_statement_end_skips_escaped_quote_in_char_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsEscapedQuoteInCharLiteralSliceViolation {
	char Q = '\''; private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_skips_line_comment_on_same_line ===
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsLineCommentOnSameLineSliceViolation {
	private static final int X = Foo.X; // a trailing line comment // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_skips_semicolon_in_char_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsSemicolonInCharLiteralSliceViolation {
	char SEMI = ';'; private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_skips_semicolon_in_string_literal ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndSkipsSemicolonInStringLiteralSliceViolation {
	String A = "a;b;c"; private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: find_statement_end_starts_in_block_comment_for_single_var_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantFindStatementEndStartsInBlockCommentForSingleVarReturnsSkipSliceViolation {
	/* opens
	*/ private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: fq_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantFqAliasSliceViolation {
	private static final int FQ_ALIAS = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15; // violation: Replace 'FQ_ALIAS' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15' with a static import.
}
// === end ===

// === case: fq_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantFqCinitSliceViolation {
	private static final int FQ_CINIT; // violation: Replace 'FQ_CINIT' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25' with a static import.

	static {
		FQ_CINIT = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25;
	}
}
// === end ===

// === case: fully_qualified_chain_usage_in_method_body_rewritten ===
class InputPreferStaticImportConstantFullyQualifiedChainUsageInMethodBodyRewrittenSliceViolation {
	private static final int X = pkg.Foo.X; // violation: Replace 'X' alias of 'pkg.Foo.X' with a static import.

	int use() {
		return pkg.Foo.X;
	}
}
// === end ===

// === case: fully_qualified_rhs_resolves_as_is ===
class InputPreferStaticImportConstantFullyQualifiedRhsResolvesAsIsSliceViolation {
	private static final int X = pkg.Foo.X; // violation: Replace 'X' alias of 'pkg.Foo.X' with a static import.
}
// === end ===

// === case: generic_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: java.util.List
class InputPreferStaticImportConstantGenericAliasSliceViolation {
	private static final List<String> GENERIC_ALIAS = AnchorClass.GENERIC_ALIAS; // violation: Replace 'GENERIC_ALIAS' alias of 'AnchorClass.GENERIC_ALIAS' with a static import.
}
// === end ===

// === case: import_inside_text_block_ignored_for_fqcn_resolution ===
// imports: com.fake.Foo
class InputPreferStaticImportConstantImportInsideTextBlockIgnoredForFqcnResolutionSliceViolation {
	String doc = """
		""";
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: import_line_with_leading_block_comment_resolves ===
// imports: /* legacy */ import foo.Foo;
class InputPreferStaticImportConstantImportLineWithLeadingBlockCommentResolvesSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: import_line_with_trailing_comment_resolves ===
// imports: import foo.Foo; // historical note
class InputPreferStaticImportConstantImportLineWithTrailingCommentResolvesSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: import_line_with_url_in_block_comment_resolves ===
// imports: import foo.Foo; /* see https://example.com */
class InputPreferStaticImportConstantImportLineWithUrlInBlockCommentResolvesSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: int_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantIntAliasSliceViolation {
	private static final int INT_ALIAS = AnchorClass.INT_ALIAS; // violation: Replace 'INT_ALIAS' alias of 'AnchorClass.INT_ALIAS' with a static import.
}
// === end ===

// === case: java_lang_implicit_import_resolves_for_simple_var ===
class InputPreferStaticImportConstantJavaLangImplicitImportResolvesForSimpleVarSliceViolation {
	private static final int X = Integer.MAX_VALUE; // violation: Replace 'X' alias of 'Integer.MAX_VALUE' with a static import.
}
// === end ===

// === case: java_lang_whitelist_wins_over_single_wildcard_fallback ===
// imports: other.*
class InputPreferStaticImportConstantJavaLangWhitelistWinsOverSingleWildcardFallbackSliceViolation {
	private static final double X = Math.PI; // violation: Replace 'X' alias of 'Math.PI' with a static import.
}
// === end ===

// === case: leading_annotation_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingAnnotationAboveReturnsSkipSliceViolation {
	@Deprecated
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: leading_javadoc_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingJavadocAboveReturnsSkipSliceViolation {
	/** Important note. */
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: leading_javadoc_continuation_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingJavadocContinuationAboveReturnsSkipSliceViolation {
	/**
	 * doc.
	 */
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: leading_line_comment_above_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingLineCommentAboveReturnsSkipSliceViolation {
	// explains why
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: leading_star_with_equals_does_not_trigger_skip ===
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingStarWithEqualsDoesNotTriggerSkipSliceViolation {
	String a = "x"
		* /* assigned */ "y=";
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: leading_star_with_semicolon_does_not_trigger_skip ===
// imports: foo.Foo
class InputPreferStaticImportConstantLeadingStarWithSemicolonDoesNotTriggerSkipSliceViolation {
	int z = 1
		* 2;
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: lowercase_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantLowercaseAliasSliceViolation {
	private static final int lowercase_alias = AnchorClass.X7; // violation: Replace 'lowercase_alias' alias of 'AnchorClass.X7' with a static import.
}
// === end ===

// === case: lowercase_local_field_name_rewritten_to_uppercase_constant_name ===
// imports: foo.Foo
class InputPreferStaticImportConstantLowercaseLocalFieldNameRewrittenToUppercaseConstantNameSliceViolation {
	private static final int max = Foo.MAX; // violation: Replace 'max' alias of 'Foo.MAX' with a static import.

	int use() {
		return max;
	}
}
// === end ===

// === case: marker_annotation_on_same_line_as_private_fixer_succeeds ===
// imports: foo.Foo
class InputPreferStaticImportConstantMarkerAnnotationOnSameLineAsPrivateFixerSucceedsSliceViolation {
	@Deprecated private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_line_alias_deletes_all_lines ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiLineAliasDeletesAllLinesSliceViolation {
	private static final int MULTILINE_DELETE = // violation: Replace 'MULTILINE_DELETE' alias of 'AnchorClass.X27' with a static import.
			AnchorClass.X27;
}
// === end ===

// === case: multi_line_alias_with_whitespace_around_dot ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiLineAliasWithWhitespaceAroundDotSliceViolation {
	private static final int MULTILINE_WS_DOT = AnchorClass // violation: Replace 'MULTILINE_WS_DOT' alias of 'AnchorClass.X28' with a static import.
			.X28;
}
// === end ===

// === case: multi_statement_on_alias_line_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class InputPreferStaticImportConstantMultiStatementOnAliasLineReturnsSkipSliceViolation {
	private static final int X = Foo.X; int leftover = 7; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_statement_with_mid_line_block_comment_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.Foo
class Holder { private static final int X = Foo.X; /* note */ } // violation: Replace 'X' alias of 'Foo.X' with a static import.
// === end ===

// === case: multi_var_all_aliases_no_usages_with_own_line_annotation ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarAllAliasesNoUsagesWithOwnLineAnnotationSliceViolation {
	@Deprecated
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_all_aliases_with_usages_and_own_line_annotation ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarAllAliasesWithUsagesAndOwnLineAnnotationSliceViolation {
	@Deprecated
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.

	int use() {
		return Foo.X + Bar.Y;
	}
}
// === end ===

// === case: multi_var_annotation_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarAnnotationOnDeclLineIsPreservedInRebuildSliceViolation {
	@Deprecated private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_annotation_on_first_line_multi_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarAnnotationOnFirstLineMultiLineIsPreservedInRebuildSliceViolation {
	@Deprecated private static final int X = Foo.X, // violation: Replace 'X' alias of 'Foo.X' with a static import.
			Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_annotation_with_body_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarAnnotationWithBodyOnDeclLineIsPreservedInRebuildSliceViolation {
	@SuppressWarnings("unused") private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_annotation_with_internal_comma_and_equals_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
@interface Ann { int a(); int b(); }
class InputPreferStaticImportConstantMultiVarAnnotationWithInternalCommaAndEqualsIsPreservedInRebuildSliceViolation {
	@Ann(a = 1, b = 2) private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_array_initializer_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarArrayInitializerInSiblingIsPreservedInRebuildSliceViolation {
	private static final int[] X = Foo.X, Y = {1, 2, 3}; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_array_type_removes_first_variable ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarArrayTypeRemovesFirstVariableSliceViolation {
	private static final int[] X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_block_comment_above_decl_with_final_in_body_does_not_confuse_find_declaration_start ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarBlockCommentAboveDeclWithFinalInBodyDoesNotConfuseFindDeclarationStartSliceViolation {
	private static final int X = Foo.X, /* opens block // violation: Replace 'X' alias of 'Foo.X' with a static import.
		this comment has final keyword
		spanning multiple lines */
		Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_block_comment_in_prefix_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarBlockCommentInPrefixIsPreservedInRebuildSliceViolation {
	private /* note */ static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_block_comment_inside_kept_rhs_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarBlockCommentInsideKeptRhsIsPreservedInRebuildSliceViolation {
	private static final int X = Foo.X, Y = Bar /* note */ . Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_block_comment_with_line_comment_marker_in_content_does_not_bail ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarBlockCommentWithLineCommentMarkerInContentDoesNotBailSliceViolation {
	private static final int X = Foo.X, /* // violation: Replace 'X' alias of 'Foo.X' with a static import.
		x has // marker
		*/ Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_char_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarCharLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = ','; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_char_literal_with_escape_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarCharLiteralWithEscapeInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = '\n'; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_comment_on_decl_line_returns_multi_var_skip ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarCommentOnDeclLineReturnsMultiVarSkipSliceViolation {
	private static final int X = Foo.X, Y = Bar.Y; // important note // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_conflicting_first_alias_is_kept ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static other.Other.X
class InputPreferStaticImportConstantMultiVarConflictingFirstAliasIsKeptSliceViolation {
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_conflicting_sibling_is_kept ===
// imports: foo.Foo
// imports: foo.Bar
// imports: static other.Other.Y
class InputPreferStaticImportConstantMultiVarConflictingSiblingIsKeptSliceViolation {
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_decl_line_char_literal_with_slash ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarDeclLineCharLiteralWithSlashSliceViolation {
	private static final Object S = '/', X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_decl_line_string_literal_contains_slash_slash ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarDeclLineStringLiteralContainsSlashSlashSliceViolation {
	private static final Object S = "//", X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_four_variables_removes_last_variable ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarFourVariablesRemovesLastVariableSliceViolation {
	private static final int A = Foo.A, B = Foo.B, C = Foo.C, D = Foo.D; // violation: Replace 'A' alias of 'Foo.A' with a static import. // violation: Replace 'B' alias of 'Foo.B' with a static import. // violation: Replace 'C' alias of 'Foo.C' with a static import. // violation: Replace 'D' alias of 'Foo.D' with a static import.
}
// === end ===

// === case: multi_var_fqcn_removes_last_variable ===
class InputPreferStaticImportConstantMultiVarFqcnRemovesLastVariableSliceViolation {
	private static final int X = com.foo.Foo.X, Y = com.foo.Bar.Y; // violation: Replace 'X' alias of 'com.foo.Foo.X' with a static import. // violation: Replace 'Y' alias of 'com.foo.Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_generic_type_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
// imports: java.util.Map
class InputPreferStaticImportConstantMultiVarGenericTypeRemovesLastVariableSliceViolation {
	private static final Map<String, Integer> X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_java_lang_implicit_import_on_last_variable ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarJavaLangImplicitImportOnLastVariableSliceViolation {
	private static final int A = Foo.A, B = Integer.MAX_VALUE; // violation: Replace 'A' alias of 'Foo.A' with a static import. // violation: Replace 'B' alias of 'Integer.MAX_VALUE' with a static import.
}
// === end ===

// === case: multi_var_line_comment_followed_by_slash_star_does_not_confuse_block_comment_mask ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarLineCommentFollowedBySlashStarDoesNotConfuseBlockCommentMaskSliceViolation {
	// stray /* in line comment
	private static final int X = Foo.X, // violation: Replace 'X' alias of 'Foo.X' with a static import.
			Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_method_call_rhs_in_non_alias_segment_does_not_confuse_detection ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarMethodCallRhsInNonAliasSegmentDoesNotConfuseDetectionSliceViolation {
	private static final int X = compute(1, 2), Y = Foo.Y; // violation: Replace 'Y' alias of 'Foo.Y' with a static import.
	static int compute(int a, int b) { return a + b; }
}
// === end ===

// === case: multi_var_mixed_alias_removes_alias_variable ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarMixedAliasRemovesAliasVariableSliceViolation {
	private static final int X = 0, Y = Foo.Y; // violation: Replace 'Y' alias of 'Foo.Y' with a static import.
}
// === end ===

// === case: multi_var_mixed_alias_with_qualified_usage_above_decl_rebuilds ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarMixedAliasWithQualifiedUsageAboveDeclRebuildsSliceViolation {
	int use() {
		return Foo.B;
	}

	private static final int A = 0, B = Foo.B; // violation: Replace 'B' alias of 'Foo.B' with a static import.
}
// === end ===

// === case: multi_var_mixed_alias_with_qualified_usage_rebuilds ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarMixedAliasWithQualifiedUsageRebuildsSliceViolation {
	private static final int A = 0, B = Foo.B; // violation: Replace 'B' alias of 'Foo.B' with a static import.

	int use() {
		return A + Foo.B;
	}
}
// === end ===

// === case: multi_var_multi_line_block_comment_above_decl_does_not_confuse_mask ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarMultiLineBlockCommentAboveDeclDoesNotConfuseMaskSliceViolation {
	/* block
	   comment with """ markers */
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_non_private_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarNonPrivateReturnsVisibilitySkipSliceViolation {
	static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_on_continuation_line_removes_continuation_variable ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarOnContinuationLineRemovesContinuationVariableSliceViolation {
	private static final int X = Foo.X, // violation: Replace 'X' alias of 'Foo.X' with a static import.
			Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_partial_keep_with_separated_annotation_above ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarPartialKeepWithSeparatedAnnotationAboveSliceViolation {
	@Deprecated

	private static final int A = 0, B = Foo.B; // violation: Replace 'B' alias of 'Foo.B' with a static import.
}
// === end ===

// === case: multi_var_qualified_annotation_on_decl_line_is_preserved_in_rebuild ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarQualifiedAnnotationOnDeclLineIsPreservedInRebuildSliceViolation {
	@java.lang.SuppressWarnings("unused") private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_renamed_alias_on_last_variable_rewrites_usages ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedAliasOnLastVariableRewritesUsagesSliceViolation {
	private static final int X = Foo.X, RENAMED = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'RENAMED' alias of 'Bar.Y' with a static import.

	int use() {
		return X + RENAMED;
	}
}
// === end ===

// === case: multi_var_renamed_alias_rewrites_usages ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedAliasRewritesUsagesSliceViolation {
	private static final int RENAMED = Foo.X, Y = Bar.Y; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.

	int use() {
		return RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_alias_shadowed_by_local_returns_shadow_skip ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedAliasShadowedByLocalReturnsShadowSkipSliceViolation {
	private static final int RENAMED = Foo.X, Y = Bar.Y; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.

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
	private static final int RENAMED = Foo.X, Y = Bar.Y; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.

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
	private static final int X = Foo.X, RENAMED = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'RENAMED' alias of 'Bar.Y' with a static import.

	int use() {
		final int Y = 1;
		return X + RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_renamed_sibling_rewrites_sibling_usage ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarRenamedSiblingRewritesSiblingUsageSliceViolation {
	private static final int X = Foo.X, RENAMED = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'RENAMED' alias of 'Bar.Y' with a static import.

	int use() {
		return X + RENAMED;
	}
}
// === end ===

// === case: multi_var_sibling_member_name_collides_returns_conflict_skip ===
// skip-reason: cannot add static import: file already imports a different constant with the same name statically
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarSiblingMemberNameCollidesReturnsConflictSkipSliceViolation {
	private static final int A = Foo.MAX, B = Bar.MAX; // violation: Replace 'A' alias of 'Foo.MAX' with a static import. // violation: Replace 'B' alias of 'Bar.MAX' with a static import.
}
// === end ===

// === case: multi_var_single_line_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarSingleLineRemovesLastVariableSliceViolation {
	private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_single_line_triple_quote_sequence_not_treated_as_text_block ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarSingleLineTripleQuoteSequenceNotTreatedAsTextBlockSliceViolation {
	private static final Object X = Foo.X, Y = """oneliner"""; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_string_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarStringLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = "hello"; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_string_literal_with_comma_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarStringLiteralWithCommaInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = "hello, world"; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_string_literal_with_escaped_quote_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarStringLiteralWithEscapedQuoteInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = "a\"b"; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_string_literal_with_semicolon_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarStringLiteralWithSemicolonInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = "a;b"; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_string_literal_with_slash_star_above_decl_does_not_confuse_block_comment_mask ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarStringLiteralWithSlashStarAboveDeclDoesNotConfuseBlockCommentMaskSliceViolation {
	String S = "has /* marker";
	private static final int X = Foo.X, // violation: Replace 'X' alias of 'Foo.X' with a static import.
			Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_text_block_closes_and_continues_on_same_line ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTextBlockClosesAndContinuesOnSameLineSliceViolation {
	private static final String S = """
		hello
		""", Y = Foo.Y; // violation: Replace 'Y' alias of 'Foo.Y' with a static import.
}
// === end ===

// === case: multi_var_text_block_in_first_variable_preserved_when_alias_is_second ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTextBlockInFirstVariablePreservedWhenAliasIsSecondSliceViolation {
	private static final String S = """
		text
		""",
		Y = Foo.Y; // violation: Replace 'Y' alias of 'Foo.Y' with a static import.
}
// === end ===

// === case: multi_var_text_block_in_sibling_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiVarTextBlockInSiblingSliceViolation {
	private static final Object TB_SIBLING_ALIAS = AnchorClass.X1, TB_SIBLING_BLOCK = """
			a;b,c
			"""; // violation@opener: Replace 'TB_SIBLING_ALIAS' alias of 'AnchorClass.X1' with a static import.
}
// === end ===

// === case: multi_var_text_block_with_backslash_escape_in_content_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiVarTextBlockWithBackslashEscapeInContentSliceViolation {
	private static final Object TB_BACKSLASH_ALIAS = AnchorClass.X2, TB_BACKSLASH_BLOCK = """
		a\\nb
		"""; // violation@opener: Replace 'TB_BACKSLASH_ALIAS' alias of 'AnchorClass.X2' with a static import.
}
// === end ===

// === case: multi_var_text_block_with_final_keyword_in_content_does_not_confuse_find_declaration_start ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTextBlockWithFinalKeywordInContentDoesNotConfuseFindDeclarationStartSliceViolation {
	private static final String DOC = """
		public static final int Z = 0;
		""";
	private static final int A = Foo.A, B = Foo.B; // violation: Replace 'A' alias of 'Foo.A' with a static import. // violation: Replace 'B' alias of 'Foo.B' with a static import.
}
// === end ===

// === case: multi_var_text_block_with_internal_quotes_in_sibling_is_preserved_in_rebuild ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiVarTextBlockWithInternalQuotesInSiblingSliceViolation {
	private static final Object TB_QUOTES_ALIAS = AnchorClass.X3, TB_QUOTES_BLOCK = """
			he said "hello".
			"""; // violation@opener: Replace 'TB_QUOTES_ALIAS' alias of 'AnchorClass.X3' with a static import.
}
// === end ===

// === case: multi_var_text_block_with_line_comment_marker_in_content_does_not_bail ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiVarTextBlockWithLineCommentMarkerInContentSliceViolation {
	private static final Object TB_MARKER_ALIAS = AnchorClass.X4, TB_MARKER_BLOCK = """
		code with // marker
		"""; // violation@opener: Replace 'TB_MARKER_ALIAS' alias of 'AnchorClass.X4' with a static import.
}
// === end ===

// === case: multi_var_three_variables_on_last_continuation_line_removes_last_variable ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarThreeVariablesOnLastContinuationLineRemovesLastVariableSliceViolation {
	private static final int A = Foo.A, // violation: Replace 'A' alias of 'Foo.A' with a static import.
			B = Foo.B, // violation: Replace 'B' alias of 'Foo.B' with a static import.
			C = Foo.C; // violation: Replace 'C' alias of 'Foo.C' with a static import.
}
// === end ===

// === case: multi_var_three_variables_removes_last_variable ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarThreeVariablesRemovesLastVariableSliceViolation {
	private static final int A = Foo.A, B = Foo.B, C = Foo.C; // violation: Replace 'A' alias of 'Foo.A' with a static import. // violation: Replace 'B' alias of 'Foo.B' with a static import. // violation: Replace 'C' alias of 'Foo.C' with a static import.
}
// === end ===

// === case: multi_var_two_aliases_different_members_rewrite_each_to_own_member ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTwoAliasesDifferentMembersRewriteEachToOwnMemberSliceViolation {
	private static final int LO = Foo.MIN, HI = Foo.MAX; // violation: Replace 'LO' alias of 'Foo.MIN' with a static import. // violation: Replace 'HI' alias of 'Foo.MAX' with a static import.

	int span() {
		return HI - LO;
	}
}
// === end ===

// === case: multi_var_two_aliases_qualified_usages_rewrite_each_to_own_member ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTwoAliasesQualifiedUsagesRewriteEachToOwnMemberSliceViolation {
	private static final int LO = Foo.MIN, HI = Foo.MAX; // violation: Replace 'LO' alias of 'Foo.MIN' with a static import. // violation: Replace 'HI' alias of 'Foo.MAX' with a static import.

	int span() {
		return Foo.MAX - Foo.MIN;
	}
}
// === end ===

// === case: multi_var_two_kept_siblings_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarTwoKeptSiblingsRebuildSliceViolation {
	private static final int A = 0, B = Foo.B, C = 1; // violation: Replace 'B' alias of 'Foo.B' with a static import.
}
// === end ===

// === case: multi_var_two_line_block_comment_above_decl_start ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarTwoLineBlockCommentAboveDeclStartSliceViolation {
	private static final int X = Foo.X, /* line 1 // violation: Replace 'X' alias of 'Foo.X' with a static import.
		line 2 */
			Y = Bar.Y; // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_url_in_string_literal_in_sibling_is_preserved_in_rebuild ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultiVarUrlInStringLiteralInSiblingIsPreservedInRebuildSliceViolation {
	private static final Object X = Foo.X, Y = "https://example.com"; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multi_var_with_annotation_on_separated_line_above_does_not_misidentify_own_decl_as_shadow ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarWithAnnotationOnSeparatedLineAboveDoesNotMisidentifyOwnDeclAsShadowSliceViolation {
	@SuppressWarnings("unused")

	private static final int RENAMED = Foo.X, Y = Bar.Y; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.

	int use() {
		return RENAMED + Y;
	}
}
// === end ===

// === case: multi_var_with_block_comment_closing_on_decl_line_does_not_confuse_find_declaration_start ===
// skip-reason: multi-variable declaration contains content that can't be safely rebuilt (typically a // line comment within the declaration); remove the comment or split into separate declarations, then re-run
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarWithBlockCommentClosingOnDeclLineDoesNotConfuseFindDeclarationStartSliceViolation {
	/* opens block
	*/ private static final int X = Foo.X, Y = Bar.Y; // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_var_with_parens_removes_last_variable ===
// imports: foo.Foo
// imports: foo.Bar
class InputPreferStaticImportConstantMultiVarWithParensRemovesLastVariableSliceViolation {
	private static final int X = (Foo.X), Y = (Bar.Y); // violation: Replace 'X' alias of 'Foo.X' with a static import. // violation: Replace 'Y' alias of 'Bar.Y' with a static import.
}
// === end ===

// === case: multi_wildcard ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
// imports: java.util.*
class InputPreferStaticImportConstantMultipleWildcardImportsFireFromCheckSliceViolation {
	private static final int MULTI_WILDCARD_X = AnchorClass.MULTI_WILDCARD_X; // violation: Replace 'MULTI_WILDCARD_X' alias of 'AnchorClass.MULTI_WILDCARD_X' with a static import.
}
// === end ===

// === case: multiple_annotations_on_same_line_fixer_succeeds ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultipleAnnotationsOnSameLineFixerSucceedsSliceViolation {
	@Deprecated @SuppressWarnings("unused") private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multiple_qualified_usages_on_same_line_all_rewritten ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultipleQualifiedUsagesOnSameLineAllRewrittenSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		return Foo.X + Foo.X;
	}
}
// === end ===

// === case: multiple_separated_annotations_above_delete_all_annotation_lines ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultipleSeparatedAnnotationsAboveSliceViolation {
	@Deprecated
	@SuppressWarnings("unused")

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multiple_usage_lines_with_intermediate_line_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantMultipleUsageLinesWithIntermediateLinePreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int a() {
		return Foo.X;
	}

	int b() {
		return Foo.X;
	}
}
// === end ===

// === case: multiple_wildcards_returns_skip ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: foo.*
// imports: bar.*
class InputPreferStaticImportConstantMultipleWildcardsReturnsSkipSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: multivar_fq ===
// multi-fix-expected
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantFqSliceViolation {
	private static final int FQ_A = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11, // violation: Replace 'FQ_A' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11' with a static import.
			FQ_B = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12; // violation: Replace 'FQ_B' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12' with a static import.
}
// === end ===

// === case: multivar_map ===
// multi-fix-expected
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
// imports: java.util.Map
class InputPreferStaticImportConstantMapSliceViolation {
	private static final Map<String, Integer> MAP_A = AnchorClass.X13, // violation: Replace 'MAP_A' alias of 'AnchorClass.X13' with a static import.
			MAP_B = AnchorClass.X14; // violation: Replace 'MAP_B' alias of 'AnchorClass.X14' with a static import.
}
// === end ===

// === case: multivar_mixed ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMixedSliceViolation {
	private static final int MIXED_A = 0,
			MIXED_B = AnchorClass.X5; // violation: Replace 'MIXED_B' alias of 'AnchorClass.X5' with a static import.
}
// === end ===

// === case: multivar_multi ===
// multi-fix-expected
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantMultiSliceViolation {
	private static final int MULTI_A = AnchorClass.X3, // violation: Replace 'MULTI_A' alias of 'AnchorClass.X3' with a static import.
			MULTI_B = AnchorClass.X4; // violation: Replace 'MULTI_B' alias of 'AnchorClass.X4' with a static import.
}
// === end ===

// === case: multivar_paren ===
// multi-fix-expected
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantParenSliceViolation {
	private static final int PAREN_A = (AnchorClass.X9), // violation: Replace 'PAREN_A' alias of 'AnchorClass.X9' with a static import.
			PAREN_B = (AnchorClass.X10); // violation: Replace 'PAREN_B' alias of 'AnchorClass.X10' with a static import.
}
// === end ===

// === case: multivar_tri ===
// multi-fix-expected
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantTriSliceViolation {
	private static final int TRI_A = AnchorClass.X6, // violation: Replace 'TRI_A' alias of 'AnchorClass.X6' with a static import.
			TRI_B = AnchorClass.X7, // violation: Replace 'TRI_B' alias of 'AnchorClass.X7' with a static import.
			TRI_C = AnchorClass.X8; // violation: Replace 'TRI_C' alias of 'AnchorClass.X8' with a static import.
}
// === end ===

// === case: nested_annotation_arg_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantNestedAnnotationArgDoesNotMisidentifyVisibilitySliceViolation {
	@MyAnno(@Other("private")) static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: nested_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantNestedCinitSliceViolation {
	private static final int NESTED_CINIT; // violation: Replace 'NESTED_CINIT' alias of 'AnchorClass.Inner.X16' with a static import.

	static {
		NESTED_CINIT = AnchorClass.Inner.X16;
	}
}
// === end ===

// === case: nested_class ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantNestedClassSliceViolation {
	static class NestedClass {
		private static final int NESTED = AnchorClass.NESTED; // violation: Replace 'NESTED' alias of 'AnchorClass.NESTED' with a static import.
	}
}
// === end ===

// === case: nested_class_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantNestedClassAliasSliceViolation {
	private static final int NESTED_CLASS_ALIAS = AnchorClass.Inner.X16; // violation: Replace 'NESTED_CLASS_ALIAS' alias of 'AnchorClass.Inner.X16' with a static import.
}
// === end ===

// === case: nested_class_chain_usage_in_method_body_rewritten ===
// imports: foo.Outer
class InputPreferStaticImportConstantNestedClassChainUsageInMethodBodyRewrittenSliceViolation {
	private static final int X = Outer.Inner.X; // violation: Replace 'X' alias of 'Outer.Inner.X' with a static import.

	int use() {
		return Outer.Inner.X;
	}
}
// === end ===

// === case: nested_class_rhs_resolves_via_simple_class ===
// imports: foo.Outer
class InputPreferStaticImportConstantNestedClassRhsResolvesViaSimpleClassSliceViolation {
	private static final int X = Outer.Inner.X; // violation: Replace 'X' alias of 'Outer.Inner.X' with a static import.
}
// === end ===

// === case: nested_type_import_resolves_to_full_path ===
// imports: foo.Bar.Inner
class InputPreferStaticImportConstantNestedTypeImportResolvesToFullPathSliceViolation {
	private static final int X = Inner.X; // violation: Replace 'X' alias of 'Inner.X' with a static import.
}
// === end ===

// === case: no_method_body_usage_deletes_field_only ===
// imports: foo.Foo
class InputPreferStaticImportConstantNoMethodBodyUsageDeletesFieldOnlySliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		return 0;
	}
}
// === end ===

// === case: non_conflicting_static_import_succeeds ===
// imports: foo.Foo
// imports: static other.Bar.Y
class InputPreferStaticImportConstantNonConflictingStaticImportSucceedsSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: other_suppress_key ===
// skip-reason: initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantOtherSuppressKeySliceViolation {
	@SuppressWarnings("unused")
	private static final int OTHER_SUPPRESS_KEY = AnchorClass.OTHER_SUPPRESS_KEY; // violation: Replace 'OTHER_SUPPRESS_KEY' alias of 'AnchorClass.OTHER_SUPPRESS_KEY' with a static import.
}
// === end ===

// === case: package_private_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantPackagePrivateAliasFiresSliceViolation {
	static final int PACKAGE_PRIVATE_ALIAS = AnchorClass.X1; // violation: Replace 'PACKAGE_PRIVATE_ALIAS' alias of 'AnchorClass.X1' with a static import.
}
// === end ===

// === case: package_private_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPackagePrivateAliasReturnsVisibilitySkipSliceViolation {
	static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: paren_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantParenCinitSliceViolation {
	private static final int PAREN_CINIT; // violation: Replace 'PAREN_CINIT' alias of 'AnchorClass.X23' with a static import.

	static {
		PAREN_CINIT = (AnchorClass.X23);
	}
}
// === end ===

// === case: parenthesized_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantParenthesizedAliasSliceViolation {
	private static final int PARENTHESIZED_ALIAS = (AnchorClass.X11); // violation: Replace 'PARENTHESIZED_ALIAS' alias of 'AnchorClass.X11' with a static import.
}
// === end ===

// === case: private_inside_comment_does_not_misidentify_visibility ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPrivateInsideCommentDoesNotMisidentifyVisibilitySliceViolation {
	public /* private */ static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: protected_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantProtectedAliasFiresSliceViolation {
	protected static final int PROTECTED_ALIAS = AnchorClass.X2; // violation: Replace 'PROTECTED_ALIAS' alias of 'AnchorClass.X2' with a static import.
}
// === end ===

// === case: protected_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantProtectedAliasReturnsVisibilitySkipSliceViolation {
	protected static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: public_alias_fires ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantPublicAliasFiresSliceViolation {
	public static final int PUBLIC_ALIAS = AnchorClass.X3; // violation: Replace 'PUBLIC_ALIAS' alias of 'AnchorClass.X3' with a static import.
}
// === end ===

// === case: public_alias_returns_visibility_skip ===
// skip-reason: cannot auto-fix non-private alias: it may be referenced from outside the class
// imports: foo.Foo
class InputPreferStaticImportConstantPublicAliasReturnsVisibilitySkipSliceViolation {
	public static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: qualified_cinit ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantQualifiedCinitSliceViolation {
	private static final int QUALIFIED_CINIT; // violation: Replace 'QUALIFIED_CINIT' alias of 'AnchorClass.X26' with a static import.

	static {
		InputPreferStaticImportConstantQualifiedCinitSliceViolation.QUALIFIED_CINIT = AnchorClass.X26;
	}
}
// === end ===

// === case: qualified_usage_before_field_decl_is_rewritten ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageBeforeFieldDeclIsRewrittenSliceViolation {
	int use() {
		return Foo.X;
	}

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: qualified_usage_inside_block_comment_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentIsPreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	/** See {@link Foo#X} for details. */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_block_comment_spanning_lines_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideBlockCommentSpanningLinesIsPreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	/*
	 * mentions Foo.X here
	 */
	int use() { return 0; }
}
// === end ===

// === case: qualified_usage_inside_line_comment_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideLineCommentIsPreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		return 0; // see Foo.X above
	}
}
// === end ===

// === case: qualified_usage_inside_string_literal_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideStringLiteralIsPreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	String use() {
		return "Foo.X";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideTextBlockIsPreservedSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	String use() {
		return """
			does Foo.X stuff
			""";
	}
}
// === end ===

// === case: qualified_usage_inside_text_block_with_backslash_escape_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageInsideTextBlockWithBackslashEscapeIsPreservedSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	String use() {
		return """
			RENAMED \"escaped\" and Foo.X stuff
			""";
	}
	int val() { return RENAMED; }
}
// === end ===

// === case: qualified_usage_preceded_by_dot_on_different_object_not_rewritten ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsagePrecededByDotOnDifferentObjectNotRewrittenSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use(Other other) {
		return Foo.X + other.Foo.X;
	}
}
// === end ===

// === case: qualified_usage_with_longer_suffix_is_not_rewritten ===
// imports: foo.Foo
class InputPreferStaticImportConstantQualifiedUsageWithLongerSuffixIsNotRewrittenSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		return Foo.X + Foo.XLong;
	}
}
// === end ===

// === case: record_field ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantRecordFieldSliceViolation {
	record InnerRecord(int x) {
		private static final int RECORD_FIELD = AnchorClass.RECORD_FIELD; // violation: Replace 'RECORD_FIELD' alias of 'AnchorClass.RECORD_FIELD' with a static import.
	}
}
// === end ===

// === case: record_field_with_lcurly_line_content ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantRecordFieldWithLcurlyLineContentSliceViolation {
	record InnerRecord(int x) { /* note */
		private static final int RECORD_LCURLY_CONTENT = AnchorClass.RECORD_LCURLY_CONTENT; // violation: Replace 'RECORD_LCURLY_CONTENT' alias of 'AnchorClass.RECORD_LCURLY_CONTENT' with a static import.
	}
}
// === end ===

// === case: record_field_with_rcurly_line_content ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantRecordFieldWithRcurlyLineContentSliceViolation {
	record InnerRecord(int x) {
		private static final int RECORD_RCURLY_CONTENT = AnchorClass.RECORD_RCURLY_CONTENT; // violation: Replace 'RECORD_RCURLY_CONTENT' alias of 'AnchorClass.RECORD_RCURLY_CONTENT' with a static import.
	/* note */ }
}
// === end ===

// === case: record_field_with_stray_semicolon_sibling ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantRecordFieldWithStraySemicolonSiblingSliceViolation {
	record InnerRecord(int x) {
		;
		private static final int RECORD_STRAY_SEMI = AnchorClass.RECORD_STRAY_SEMI; // violation: Replace 'RECORD_STRAY_SEMI' alias of 'AnchorClass.RECORD_STRAY_SEMI' with a static import.
	}
}
// === end ===

// === case: rename_target_collides_in_multi_var_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantRenameTargetCollidesInMultiVarSkipsFixSliceViolation {
	private static final int A = Foo.A, RENAMED = Foo.X; // violation: Replace 'A' alias of 'Foo.A' with a static import. // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static class X {}
}
// === end ===

// === case: renamed ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantRenamedSliceViolation {
	private static final int RENAMED = AnchorClass.X6; // violation: Replace 'RENAMED' alias of 'AnchorClass.X6' with a static import.
}
// === end ===

// === case: renamed_alias_label_with_same_name_in_method_body_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasLabelWithSameNameInMethodBodyIsPreservedSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use() {
		RENAMED:
		for (int i = 0; i < 5; ++i) {
			if (i > 0)
				return RENAMED;
		}
		return 0;
	}
}
// === end ===

// === case: renamed_alias_own_declaration_is_not_its_own_shadow ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasOwnDeclarationIsNotItsOwnShadowSliceViolation {
	private static final int RENAMED = // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
			Foo.X;

	int use() {
		return RENAMED;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_method_body_rewritten_to_constant_name ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasReferencedInMethodBodyRewrittenToConstantNameSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use() {
		return RENAMED;
	}
}
// === end ===

// === case: renamed_alias_referenced_in_string_literal_is_preserved ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasReferencedInStringLiteralIsPreservedSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	String use() {
		return "RENAMED";
	}
	int val() { return RENAMED; }
}
// === end ===

// === case: renamed_alias_with_annotation_on_separated_line_above_does_not_misidentify_own_decl_as_shadow ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasWithAnnotationOnSeparatedLineAboveDoesNotMisidentifyOwnDeclAsShadowSliceViolation {
	@SuppressWarnings("unused")

	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: renamed_alias_with_both_qualified_and_local_usages_rewritten ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasWithBothQualifiedAndLocalUsagesRewrittenSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use() {
		return RENAMED + Foo.X;
	}
}
// === end ===

// === case: renamed_alias_with_separated_annotation_and_body_usage_rewrites ===
// imports: foo.Foo
class InputPreferStaticImportConstantRenamedAliasWithSeparatedAnnotationAndBodyUsageRewritesSliceViolation {
	@SuppressWarnings("unused")

	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
	int use() {
		return RENAMED;
	}
}
// === end ===

// === case: rhs_comment_between_class_and_dot_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantRhsCommentBetweenClassAndDotResolvesSliceViolation {
	private static final int X = Foo /* mid */ . X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: rhs_fivefold_nested_parens_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantRhsFivefoldNestedParensResolvesSliceViolation {
	private static final int X = (((((Foo.X))))); // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: rhs_nested_parens_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantRhsNestedParensResolvesSliceViolation {
	private static final int X = ((Foo.X)); // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: rhs_parenthesized_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantRhsParenthesizedResolvesSliceViolation {
	private static final int X = (Foo.X); // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: rhs_triple_nested_parens_resolves ===
// imports: foo.Foo
class InputPreferStaticImportConstantRhsTripleNestedParensResolvesSliceViolation {
	private static final int X = (((Foo.X))); // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: same_class_static_import_already_present_does_not_conflict ===
// imports: foo.Foo
// imports: static foo.Foo.X
class InputPreferStaticImportConstantSameClassStaticImportAlreadyPresentDoesNotConflictSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: same_class_used_in_field_and_qualified_method_body_rewrites_usage ===
// imports: foo.Foo
class InputPreferStaticImportConstantSameClassUsedInFieldAndQualifiedMethodBodyRewritesUsageSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.

	int use() {
		return Foo.X;
	}
}
// === end ===

// === case: same_package ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
class InputPreferStaticImportConstantSamePackageResolvableFiresSliceViolation {
	private static final int MAX = InputPreferStaticImportConstantSamePackageHelper.MAX; // violation: Replace 'MAX' alias of 'InputPreferStaticImportConstantSamePackageHelper.MAX' with a static import.
}
// === end ===

// === case: separated_annotation_above_non_renamed_deletes_annotation_too ===
// imports: foo.Foo
class InputPreferStaticImportConstantSeparatedAnnotationAboveNonRenamedSliceViolation {
	@SuppressWarnings("unused")

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: separated_annotation_above_non_renamed_with_qualified_usage_rewrites ===
// imports: foo.Foo
class InputPreferStaticImportConstantSeparatedAnnotationAboveNonRenamedWithUsageSliceViolation {
	@SuppressWarnings("unused")

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
	int use() {
		return Foo.X;
	}
}
// === end ===

// === case: separated_annotation_with_trailing_line_comment_above_deletes_all ===
// imports: foo.Foo
class InputPreferStaticImportConstantSeparatedAnnotationWithTrailingLineCommentAboveSliceViolation {
	@SuppressWarnings("unused") // keep

	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: shadowing_annotation_type_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingAnnotationTypeWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	@interface X {}
}
// === end ===

// === case: shadowing_array_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingArrayTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		int[] RENAMED = new int[5];
	}
}
// === end ===

// === case: shadowing_catch_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a catch parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingCatchParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		try {} catch (Exception RENAMED) {}
	}
}
// === end ===

// === case: shadowing_constructor_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a constructor parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingConstructorParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	InputPreferStaticImportConstantShadowingConstructorParameterSkipsFixSliceViolation(int RENAMED) {}
}
// === end ===

// === case: shadowing_enum_constant_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingEnumConstantWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	enum E { X }
}
// === end ===

// === case: shadowing_for_each_var_skips_fix ===
// skip-reason: renamed alias's local name clashes with a for-each variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingForEachVarSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use(int[] arr) {
		for (var RENAMED : arr) {}
	}
}
// === end ===

// === case: shadowing_for_init_skips_fix ===
// skip-reason: renamed alias's local name clashes with a for-loop variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingForInitSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		for (int RENAMED = 0; RENAMED < 10; ++RENAMED) {}
	}
}
// === end ===

// === case: shadowing_generic_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		Map<String, Integer> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_lambda_multi_param_without_types_skips_fix ===
// skip-reason: renamed alias's local name clashes with a lambda parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLambdaMultiParamWithoutTypesSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		do2((a, RENAMED) -> RENAMED + 1);
	}
}
// === end ===

// === case: shadowing_lambda_single_param_skips_fix ===
// skip-reason: renamed alias's local name clashes with a lambda parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLambdaSingleParamSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		java.util.stream.Stream.of(1).map(RENAMED -> RENAMED + 1);
	}
}
// === end ===

// === case: shadowing_local_declaration_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingLocalDeclarationSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

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
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int X() { return 0; }
}
// === end ===

// === case: shadowing_nested_class_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedClassWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static class X {}
}
// === end ===

// === case: shadowing_nested_enum_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedEnumWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	enum X { A }
}
// === end ===

// === case: shadowing_nested_field_with_same_name_skips_fix ===
// skip-reason: renamed alias's local name clashes with a field with the same name elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedFieldWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	static class Inner {
		int RENAMED;
	}
}
// === end ===

// === case: shadowing_nested_generic_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedGenericReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() {
		Map<String, List<Integer>> RENAMED = null;
	}
}
// === end ===

// === case: shadowing_nested_interface_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedInterfaceWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	interface X {}
}
// === end ===

// === case: shadowing_nested_record_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingNestedRecordWithSameNameSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	record X(int n) {}
}
// === end ===

// === case: shadowing_parameter_skips_fix ===
// skip-reason: renamed alias's local name clashes with a method parameter elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingParameterSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(int RENAMED) {
		return RENAMED;
	}
}
// === end ===

// === case: shadowing_reference_type_local_skips_fix ===
// skip-reason: renamed alias's local name clashes with a local variable elsewhere; rename manually to avoid scope conflicts
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingReferenceTypeLocalSkipsFixSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

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
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	void use() throws Exception {
		try (java.io.Closeable RENAMED = null) {}
	}
}
// === end ===

// === case: shadowing_type_parameter_with_same_name_skips_fix ===
// skip-reason: rename target collides with existing identifier 'X'
// imports: foo.Foo
class InputPreferStaticImportConstantShadowingTypeParameterWithSameNameSkipsFixSliceViolation<X> {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: single_var_with_annotation_arg_containing_comma_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
class InputPreferStaticImportConstantSingleVarWithAnnotationArgContainingCommaIsNotMisidentifiedAsMultiVarSliceViolation {
	@SuppressWarnings({"a", "b"}) private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: single_var_with_comment_containing_comma_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
class InputPreferStaticImportConstantSingleVarWithCommentContainingCommaIsNotMisidentifiedAsMultiVarSliceViolation {
	private static final int /* note, see also Foo */ X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: single_var_with_generic_type_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: java.util.Map
class InputPreferStaticImportConstantSingleVarWithGenericTypeIsNotMisidentifiedAsMultiVarSliceViolation {
	private static final Map<String, Integer> X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: single_var_with_three_arg_generic_type_is_not_misidentified_as_multi_var ===
// imports: foo.Foo
// imports: java.util.function.BiFunction
class InputPreferStaticImportConstantSingleVarWithThreeArgGenericTypeIsNotMisidentifiedAsMultiVarSliceViolation {
	private static final BiFunction<Integer, Integer, Integer> X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: split_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantSplitAliasSliceViolation {
	private static final int SPLIT_ALIAS; // violation: Replace 'SPLIT_ALIAS' alias of 'AnchorClass.X24' with a static import.

	static {
		SPLIT_ALIAS = AnchorClass.X24;
	}
}
// === end ===

// === case: static_import_inside_text_block_ignored_for_conflict_detection ===
// imports: foo.Foo
class InputPreferStaticImportConstantStaticImportInsideTextBlockIgnoredForConflictDetectionSliceViolation {
	String doc = """
		import static other.X;
		""";
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: string_alias ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass
class InputPreferStaticImportConstantStringAliasSliceViolation {
	private static final String STRING_ALIAS = AnchorClass.STRING_ALIAS; // violation: Replace 'STRING_ALIAS' alias of 'AnchorClass.STRING_ALIAS' with a static import.
}
// === end ===

// === case: switch_case_at_column_zero_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantSwitchCaseAtColumnZeroReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(int k) {
		switch (k) {
case RENAMED:
			return 1;
		default:
			return 0;
		}
	}
}
// === end ===

// === case: switch_case_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantSwitchCaseReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(int k) {
		switch (k) {
			case RENAMED:
				return 1;
			default:
				return 0;
		}
	}
}
// === end ===

// === case: ternary_block_comment_between_question_and_ident_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantTernaryBlockCommentBetweenQuestionAndIdentReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(boolean flag) {
		return flag ? /* note */ RENAMED : 0;
	}
}
// === end ===

// === case: ternary_multi_line_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantTernaryMultiLineReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(boolean flag) {
		return flag
				? RENAMED
				: 0;
	}
}
// === end ===

// === case: ternary_no_whitespace_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantTernaryNoWhitespaceReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(boolean flag) {
		return flag?RENAMED:0;
	}
}
// === end ===

// === case: ternary_reference_renamed_field ===
// imports: foo.Foo
class InputPreferStaticImportConstantTernaryReferenceRenamedFieldSliceViolation {
	private static final int RENAMED = Foo.X; // violation: Replace 'RENAMED' alias of 'Foo.X' with a static import.

	int use(boolean flag) {
		return flag ? RENAMED : 0;
	}
}
// === end ===

// === case: wildcard ===
// imports: com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*
class InputPreferStaticImportConstantWildcardImportResolvesAndFiresSliceViolation {
	private static final int WILDCARD_X = AnchorClass.WILDCARD_X; // violation: Replace 'WILDCARD_X' alias of 'AnchorClass.WILDCARD_X' with a static import.
}
// === end ===

// === case: wildcard_before_explicit_still_picks_explicit ===
// imports: wild.*
// imports: other.Foo
class InputPreferStaticImportConstantWildcardBeforeExplicitStillPicksExplicitSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===

// === case: wildcard_import_fallback ===
// imports: foo.*
class InputPreferStaticImportConstantWildcardImportFallbackSliceViolation {
	private static final int X = Foo.X; // violation: Replace 'X' alias of 'Foo.X' with a static import.
}
// === end ===