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
	int alpha = 2, beta = 1;
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
	@SuppressWarnings(
		"unchecked"
	)
	BETA,
	ALPHA
}
// === end ===

// === case: annotation_negated_value_orders_by_key ===
class InputFieldSortingAnnotationNegatedValueOrdersByKeySliceViolation {
	@Min(-1)
	private int alpha;
	@Min(1)
	private int beta;
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
	@Deprecated
	BETA,
	ALPHA
}
// === end ===

// === case: annotation_static ===
// skip-reason: cannot reorder fields: a static field carries annotations
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

// === case: anonclass_annotated_instance ===
class InputFieldSortingAnonClassAnnotatedInstanceSliceViolation {
	final String label = "w";
	@Deprecated
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_anon_ctor_arg_reads_later_plain ===
class InputFieldSortingAnonClassAnonCtorArgReadsLaterPlainSliceViolation {
	final String label = "w";
	final Thread worker = new Thread(label) {
	};
}
// === end ===

// === case: anonclass_anon_plain_anon_plain_partial ===
class InputFieldSortingAnonClassAnonPlainAnonPlainPartialSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread first = new Thread() {
	};
	final Thread second = new Thread() {
	};
}
// === end ===

// === case: anonclass_blank_line_between ===
class InputFieldSortingAnonClassBlankLineBetweenSliceViolation {
	final String label = "w";
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_body_forward_reference_between_anons ===
class InputFieldSortingAnonClassBodyForwardReferenceBetweenAnonsSliceViolation {
	final int count = 0;
	final Runnable alpha = new Runnable() {
		@Override
		public void run() {
			beta.run();
		}
	};
	final Runnable beta = new Runnable() {
		@Override
		public void run() {
			System.out.println("beta");
		}
	};
}
// === end ===

// === case: anonclass_body_reads_nonfinal_field ===
class InputFieldSortingAnonClassBodyReadsNonfinalFieldSliceViolation {
	final String name = "x";
	final Runnable task = new Runnable() {
		@Override
		public void run() {
			++counter;
		}
	};

	int counter;
}
// === end ===

// === case: anonclass_chunk_nonfinal_anon_before_final_plain ===
class InputFieldSortingAnonClassChunkNonfinalAnonBeforeFinalPlainSliceViolation {
	final String label = "w";

	Thread task = new Thread() {
	};
}
// === end ===

// === case: anonclass_chunk_nonfinal_plain_before_anon ===
class InputFieldSortingAnonClassChunkNonfinalPlainBeforeAnonSliceViolation {
	final Thread task = new Thread() {
	};

	String label;
}
// === end ===

// === case: anonclass_construction_time_read_name_order ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingAnonClassConstructionTimeReadNameOrderSliceViolation {
	final Object alpha = new Object() {
		final String snapshot = String.valueOf(beta);
	};
	final Object beta = new Object() {
	};
}
// === end ===

// === case: anonclass_ctor_arg_literal ===
class InputFieldSortingAnonClassCtorArgLiteralSliceViolation {
	final String label = "w";
	final Thread worker = new Thread("w") {
	};
}
// === end ===

// === case: anonclass_cycle_through_ctor_arg ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingAnonClassCycleThroughCtorArgSliceViolation {
	final Thread worker = new Thread(label) {
	};
	final String label = worker.getName();

	long count;
	int index;
}
// === end ===

// === case: anonclass_dependency_and_free_plain ===
class InputFieldSortingAnonClassDependencyAndFreePlainSliceViolation {
	final String label = "w", name = "n";
	final Object holder = new Object() {
		final String copy = label;
	};
}
// === end ===

// === case: anonclass_double_violation_same_field ===
class InputFieldSortingAnonClassDoubleViolationSameFieldSliceViolation {
	final String label = "w";
	final Thread beta = new Thread() {
	};
	final Thread alpha = new Thread() {
	};
}
// === end ===

// === case: anonclass_enum_constant_body ===
enum InputFieldSortingAnonClassEnumConstantBodySliceViolation {
	INSTANCE {
		final String label = "w";
		final Thread worker = new Thread() {
		};
	}
}
// === end ===

// === case: anonclass_enum_fields ===
enum InputFieldSortingAnonClassEnumFieldsSliceViolation {
	ALPHA;

	final String label = "w";
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_fields_inside_anon_body ===
class InputFieldSortingAnonClassFieldsInsideAnonBodySliceViolation {
	final Runnable outer = new Runnable() {
		final String label = "w";
		final Thread worker = new Thread() {
		};

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: anonclass_first_of_three ===
class InputFieldSortingAnonClassFirstOfThreeSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_free_plain_after_chunk_break ===
class InputFieldSortingAnonClassFreePlainAfterChunkBreakSliceViolation {
	final String label = "w";
	final Thread worker = new Thread() {
	};

	String note;
}
// === end ===

// === case: anonclass_free_plain_after_reader ===
class InputFieldSortingAnonClassFreePlainAfterReaderSliceViolation {
	final String name = "n";
	final Thread worker = new Thread() {
	};
	final String label = worker.getName();
}
// === end ===

// === case: anonclass_in_array_init ===
class InputFieldSortingAnonClassInArrayInitSliceViolation {
	final String label = "w";
	final Thread[] workers = {new Thread() {
	}};
}
// === end ===

// === case: anonclass_in_cast ===
class InputFieldSortingAnonClassInCastSliceViolation {
	final String label = "w";
	final Object worker = (Runnable) new Thread() {
	};
}
// === end ===

// === case: anonclass_in_lambda_body ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassInLambdaBodySliceViolation {
	final String label = "w";
	final Supplier<Thread> factory = () -> new Thread() {
	};
}
// === end ===

// === case: anonclass_in_list_of ===
// imports: java.util.List
class InputFieldSortingAnonClassInListOfSliceViolation {
	final String label = "w";
	final List<Runnable> tasks = List.of(new Thread() {
	});
}
// === end ===

// === case: anonclass_in_ternary ===
class InputFieldSortingAnonClassInTernarySliceViolation {
	final String label = "w";
	final Thread worker = Boolean.getBoolean("x") ? null : new Thread() {
	};
}
// === end ===

// === case: anonclass_inert_plain_reorders_without_merge ===
class InputFieldSortingAnonClassInertPlainReordersWithoutMergeSliceViolation {
	Object plain;
	Object first = new Object() {
	};
}
// === end ===

// === case: anonclass_interface_implicit_modifiers ===
interface InputFieldSortingAnonClassInterfaceImplicitModifiersSliceViolation {
	String LABEL = "w";
	Thread WORKER = new Thread() {
	};
}
// === end ===

// === case: anonclass_interleaved_cross_group_reference ===
class InputFieldSortingAnonClassInterleavedCrossGroupReferenceSliceViolation {
	final String label = "w";
	static int shared;
	final Object holder = new Object() {
		final int copy = shared;
	};
}
// === end ===

// === case: anonclass_interleaved_spanning_chunks ===
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingAnonClassInterleavedSpanningChunksSliceViolation {
	Thread worker = new Thread() {
	};
	static int shared;
	final String label = "w";
}
// === end ===

// === case: anonclass_interleaved_static ===
class InputFieldSortingAnonClassInterleavedStaticSliceViolation {
	final String label = "w";
	static int shared;
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_javadoc ===
class InputFieldSortingAnonClassJavadocSliceViolation {
	final String label = "w";
	/** The background worker. */
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_lambda_forward_reference ===
class InputFieldSortingAnonClassLambdaForwardReferenceSliceViolation {
	final String name = "test";
	final Runnable action = () -> System.out.println(name);
}
// === end ===

// === case: anonclass_leading_line_comment ===
class InputFieldSortingAnonClassLeadingLineCommentSliceViolation {
	final String label = "w";
	// runs in the background
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_local_class ===
class InputFieldSortingAnonClassLocalClassSliceViolation {
	void build() {
		class Local {
			final String label = "w";
			final Thread worker = new Thread() {
			};
		}
	}
}
// === end ===

// === case: anonclass_local_class_in_lambda ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassLocalClassInLambdaSliceViolation {
	final String label = "w";
	final Supplier<Object> factory = () -> {
		class Local {
			String copy() {
				return label;
			}
		}

		return new Local();
	};
}
// === end ===

// === case: anonclass_middle_of_three ===
class InputFieldSortingAnonClassMiddleOfThreeSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_multi_var_all_anon ===
class InputFieldSortingAnonClassMultiVarAllAnonSliceViolation {
	final String label = "w";
	final Thread alpha = new Thread() {
	}, beta = new Thread() {
	};
}
// === end ===

// === case: anonclass_multi_var_all_anon_secondary_read ===
class InputFieldSortingAnonClassMultiVarAllAnonSecondaryReadSliceViolation {
	final String name = "n";
	final Object a = new Object() {
	}, b = new Object() {
	};
	final String label = b.toString();
}
// === end ===

// === case: anonclass_multi_var_all_free ===
class InputFieldSortingAnonClassMultiVarAllFreeSliceViolation {
	final int p = 1, q = 2;
	final Object anon = new Object() {
	};
}
// === end ===

// === case: anonclass_multi_var_all_readers_with_free_sibling ===
class InputFieldSortingAnonClassMultiVarAllReadersWithFreeSiblingSliceViolation {
	final String name = "n";
	final Object anon = new Object() {
	};
	final int p = anon.hashCode(), q = anon.hashCode();
}
// === end ===

// === case: anonclass_multi_var_anon_first_with_sibling ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes anonymous class and plain initializers
class InputFieldSortingAnonClassMultiVarAnonFirstWithSiblingSliceViolation {
	final Object task = new Thread() {
	}, label = "w";
	final String name = "n";
}
// === end ===

// === case: anonclass_multi_var_mixed_other_group ===
class InputFieldSortingAnonClassMultiVarMixedOtherGroupSliceViolation {
	static final Object LABEL = "w", TASK = new Thread() {
	};

	final String alpha = "a", zebra = "z";
}
// === end ===

// === case: anonclass_multi_var_reader_mixed ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedSliceViolation {
	final Object anon = new Object() {
	};
	final int p = 1, q = anon.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_free_last ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedFreeLastSliceViolation {
	final Object anon = new Object() {
	};
	final int a = anon.hashCode(), b = 1;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_free_middle ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedFreeMiddleSliceViolation {
	final Object anon = new Object() {
	};
	final int a = anon.hashCode(), b = 1, c = anon.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_reader_middle ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedReaderMiddleSliceViolation {
	final Object anon = new Object() {
	};
	final int a = 1, b = anon.hashCode(), c = 2;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_static ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedStaticSliceViolation {
	static final Object ANON = new Object() {
	};
	static final int P = 1, Q = ANON.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_transitive_field ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedTransitiveFieldSliceViolation {
	final Object anon = new Object() {
	};
	final int base = anon.hashCode();
	final int p = 1, q = base + 1;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_transitive_sibling ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedTransitiveSiblingSliceViolation {
	final Object anon = new Object() {
	};
	final int a = anon.hashCode(), b = a + 1, c = 1;
}
// === end ===

// === case: anonclass_multi_var_reader_other_chunk_no_anon ===
class InputFieldSortingAnonClassMultiVarReaderOtherChunkNoAnonSliceViolation {
	final Object anon = new Object() {
	};

	int p = 1, q = anon.hashCode();
	int zeta;
}
// === end ===

// === case: anonclass_multi_var_second_declarator_before_plain ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes anonymous class and plain initializers
class InputFieldSortingAnonClassMultiVarSecondDeclaratorBeforePlainSliceViolation {
	final Object label = "w", task = new Thread() {
	};
	final String name = "n";
}
// === end ===

// === case: anonclass_nested_static_class ===
class InputFieldSortingAnonClassNestedStaticClassSliceViolation {
	static class Inner {
		final String label = "w";
		final Thread worker = new Thread() {
		};
	}
}
// === end ===

// === case: anonclass_new_object_with_body ===
class InputFieldSortingAnonClassNewObjectWithBodySliceViolation {
	final String label = "w";
	final Object lock = new Object() {
	};
}
// === end ===

// === case: anonclass_nonfinal_anon_before_nonfinal_plain ===
class InputFieldSortingAnonClassNonfinalAnonBeforeNonfinalPlainSliceViolation {
	String label;
	Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_order_tie_released_together ===
class InputFieldSortingAnonClassOrderTieReleasedTogetherSliceViolation {
	final String label = "w";
	final Thread aLPHA = new Thread() {
	};
	final Thread alpha = new Thread() {
	};
}
// === end ===

// === case: anonclass_pair_ctor_arg_dependency_over_type ===
// imports: java.util.TimerTask
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingAnonClassPairCtorArgDependencyOverTypeSliceViolation {
	final Thread worker = new Thread(job) {
	};
	final TimerTask job = new TimerTask() {
		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: anonclass_pattern_in_string_initializer ===
class InputFieldSortingAnonClassPatternInStringInitializerSliceViolation {
	final String alpha = "hello", zebra = "new Runnable() {";
}
// === end ===

// === case: anonclass_pinned_later_declarator_skips ===
// skip-reason: cannot reorder fields: a field would move past a name bound in a multi-variable declaration
class InputFieldSortingAnonClassPinnedLaterDeclaratorSkipsSliceViolation {
	final Object task = new Object() {
	};
	final String label = "w", note = String.valueOf(1);
}
// === end ===

// === case: anonclass_pinned_plain_stays_after_anon_reorders ===
class InputFieldSortingAnonClassPinnedPlainStaysAfterAnonReordersSliceViolation {
	final String label = "w";
	final Thread worker = new Thread() {
	};
	final Object note = "n";
}
// === end ===

// === case: anonclass_plain_reads_later_anon ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingAnonClassPlainReadsLaterAnonSliceViolation {
	final String label = String.valueOf(task);
	final Thread task = new Thread() {
	};
}
// === end ===

// === case: anonclass_qualified_type ===
class InputFieldSortingAnonClassQualifiedTypeSliceViolation {
	final String label = "w";
	final Thread worker = new java.lang.Thread() {
	};
}
// === end ===

// === case: anonclass_record_static ===
record InputFieldSortingAnonClassRecordStaticSliceViolation(int x) {
	static final String LABEL = "w";
	static final Thread WORKER = new Thread() {
	};
}
// === end ===

// === case: anonclass_reference_through_anon_method ===
class InputFieldSortingAnonClassReferenceThroughAnonMethodSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = read();

		String read() {
			return label;
		}
	};
}
// === end ===

// === case: anonclass_self_reference ===
class InputFieldSortingAnonClassSelfReferenceSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Object self = holder;
	};
}
// === end ===

// === case: anonclass_shadow_bare_this_member ===
class InputFieldSortingAnonClassShadowBareThisMemberSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final String copy = this.label;
	};
}
// === end ===

// === case: anonclass_shadow_ctor_arg_unaffected ===
class InputFieldSortingAnonClassShadowCtorArgUnaffectedSliceViolation {
	final String label = "w";
	final Thread worker = new Thread(label) {
		final String label = "inner";
	};
}
// === end ===

// === case: anonclass_shadow_init_block_assigns_member ===
class InputFieldSortingAnonClassShadowInitBlockAssignsMemberSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		String label;

		{
			label = "x";
		}
	};
}
// === end ===

// === case: anonclass_shadow_init_block_local_not_a_dependency ===
class InputFieldSortingAnonClassShadowInitBlockLocalNotADependencySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final int seed = 1;

		{
			final var label = "local";
			System.out.println(label);
		}
	};
}
// === end ===

// === case: anonclass_shadow_interface_implicit_static ===
interface InputFieldSortingAnonClassShadowInterfaceImplicitStaticSliceViolation {
	String LABEL = "w";
	Object HOLDER = new Object() {
		static final String LABEL = "inner";
		static final String COPY = LABEL;
	};
}
// === end ===

// === case: anonclass_shadow_lambda_param_not_a_dependency ===
// imports: java.util.function.Function
class InputFieldSortingAnonClassShadowLambdaParamNotADependencySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Function<String, String> copy = label -> label;
	};
}
// === end ===

// === case: anonclass_shadow_method_name_not_a_dependency ===
class InputFieldSortingAnonClassShadowMethodNameNotADependencySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label();

		String label() {
			return "x";
		}
	};
}
// === end ===

// === case: anonclass_shadow_multi_var_member_first ===
class InputFieldSortingAnonClassShadowMultiVarMemberFirstSliceViolation {
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
		final String alpha = "x", label = "inner";
		final String copy = label;
	};
}
// === end ===

// === case: anonclass_shadow_multi_var_member_middle ===
class InputFieldSortingAnonClassShadowMultiVarMemberMiddleSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String alpha = "x", label = "inner", omega = label;
	};
}
// === end ===

// === case: anonclass_shadow_nested_ctor_arg ===
class InputFieldSortingAnonClassShadowNestedCtorArgSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final Thread inner = new Thread(label) {
		};
	};
}
// === end ===

// === case: anonclass_shadow_nested_member_scoped_to_nested ===
class InputFieldSortingAnonClassShadowNestedMemberScopedToNestedSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label;
		final Object inner = new Object() {
			final String label = "inner";
		};
	};
}
// === end ===

// === case: anonclass_shadow_nested_own_read ===
class InputFieldSortingAnonClassShadowNestedOwnReadSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Object inner = new Object() {
			final String label = "inner";
			final String copy = label;
		};
	};
}
// === end ===

// === case: anonclass_shadow_other_name_still_dependency ===
class InputFieldSortingAnonClassShadowOtherNameStillDependencySliceViolation {
	final String label = "w", name = "n";
	final Object holder = new Object() {
		final String label = "inner";
		final String copy = label + name;
	};
}
// === end ===

// === case: anonclass_shadow_outer_member_in_nested ===
class InputFieldSortingAnonClassShadowOuterMemberInNestedSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final Object inner = new Object() {
			final String copy = label;
		};
	};
}
// === end ===

// === case: anonclass_shadow_outer_multi_var_secondary ===
class InputFieldSortingAnonClassShadowOuterMultiVarSecondarySliceViolation {
	final String alpha = "a", label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final String copy = label;
	};
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = InputFieldSortingAnonClassShadowQualifiedOuterThisSliceViolation.this.label;
		final String label = "inner";
	};
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this_init_block ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisInitBlockSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		String label;

		{
			System.out.println(InputFieldSortingAnonClassShadowQualifiedOuterThisInitBlockSliceViolation.this.label);
		}
	};
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this_nested ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisNestedSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final Object inner = new Object() {
			final String copy = InputFieldSortingAnonClassShadowQualifiedOuterThisNestedSliceViolation.this.label;
		};
	};
}
// === end ===

// === case: anonclass_shadow_static_member_instance_read ===
class InputFieldSortingAnonClassShadowStaticMemberInstanceReadSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		static final String LABEL = "inner";

		final String copy = LABEL;
	};
}
// === end ===

// === case: anonclass_shadow_static_member_static_init ===
class InputFieldSortingAnonClassShadowStaticMemberStaticInitSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		static final String LABEL = "inner";

		static {
			System.out.println(LABEL);
		}
	};
}
// === end ===

// === case: anonclass_shadowed_member_name ===
class InputFieldSortingAnonClassShadowedMemberNameSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String label = "inner";
		final String copy = label;
	};
}
// === end ===

// === case: anonclass_skip_method_body ===
class InputFieldSortingAnonClassSkipMethodBodySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		String copy() {
			return label;
		}
	};
}
// === end ===

// === case: anonclass_skip_nested_anon_method_body ===
class InputFieldSortingAnonClassSkipNestedAnonMethodBodySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Object inner = new Object() {
			String copy() {
				return label;
			}
		};
	};
}
// === end ===

// === case: anonclass_skip_nested_class ===
class InputFieldSortingAnonClassSkipNestedClassSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		class Inner {
			final String copy = LABEL;
		}
	};
}
// === end ===

// === case: anonclass_skip_nested_enum ===
class InputFieldSortingAnonClassSkipNestedEnumSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		enum Inner {
			A;

			static final String COPY = LABEL;
		}
	};
}
// === end ===

// === case: anonclass_skip_nested_interface ===
class InputFieldSortingAnonClassSkipNestedInterfaceSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		interface Inner {
			String COPY = LABEL;
		}
	};
}
// === end ===

// === case: anonclass_skip_nested_record ===
class InputFieldSortingAnonClassSkipNestedRecordSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		record Inner() {
			static final String COPY = LABEL;
		}
	};
}
// === end ===

// === case: anonclass_skip_static_method_body ===
class InputFieldSortingAnonClassSkipStaticMethodBodySliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		static String copy() {
			return LABEL;
		}
	};
}
// === end ===

// === case: anonclass_static_annotated ===
// skip-reason: cannot reorder fields: a static field carries annotations
class InputFieldSortingAnonClassStaticAnnotatedSliceViolation {
	@Deprecated
	static final Thread WORKER = new Thread() {
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_static_anon_before_plain ===
class InputFieldSortingAnonClassStaticAnonBeforePlainSliceViolation {
	static final String LABEL = "w";
	static final Thread WORKER = new Thread() {
	};
}
// === end ===

// === case: anonclass_text_block_in_body ===
class InputFieldSortingAnonClassTextBlockInBodySliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String text = """
				};
				""";
	};
}
// === end ===

// === case: anonclass_trailing_comment_on_close ===
class InputFieldSortingAnonClassTrailingCommentOnCloseSliceViolation {
	final String label = "w";
	final Thread worker = new Thread() {
	}; // background worker
}
// === end ===

// === case: anonclass_transitive_dependency_on_later_plain ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingAnonClassTransitiveDependencyOnLaterPlainSliceViolation {
	final Object holder = new Object() {
		final String copy = String.valueOf(source);
	};
	final Object source = new Object() {
		final String copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_two_anon_interleaved ===
class InputFieldSortingAnonClassTwoAnonInterleavedSliceViolation {
	final String alpha = "a", beta = "b";
	final Thread first = new Thread() {
	};
	final Thread second = new Thread() {
	};
}
// === end ===

// === case: anonclass_unshadowed_member_name ===
class InputFieldSortingAnonClassUnshadowedMemberNameSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String own = "inner";
		final String copy = own;
	};
}
// === end ===

// === case: anonclass_volatile_nonfinal ===
class InputFieldSortingAnonClassVolatileNonfinalSliceViolation {
	String label;
	volatile Thread worker = new Thread() {
	};
}
// === end ===

// === case: anonclass_walk_instance_field_init ===
class InputFieldSortingAnonClassWalkInstanceFieldInitSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label;
	};
}
// === end ===

// === case: anonclass_walk_instance_init ===
class InputFieldSortingAnonClassWalkInstanceInitSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final int seed = 1;

		{
			System.out.println(label);
		}
	};
}
// === end ===

// === case: anonclass_walk_lambda_in_field_init ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassWalkLambdaInFieldInitSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Supplier<String> copy = () -> label;
	};
}
// === end ===

// === case: anonclass_walk_multi_declarator_member ===
class InputFieldSortingAnonClassWalkMultiDeclaratorMemberSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String alpha = "x", copy = label;
	};
}
// === end ===

// === case: anonclass_walk_nested_anon_field_init ===
class InputFieldSortingAnonClassWalkNestedAnonFieldInitSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final Object inner = new Object() {
			final String copy = label;
		};
	};
}
// === end ===

// === case: anonclass_walk_static_field_init ===
class InputFieldSortingAnonClassWalkStaticFieldInitSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		static final String COPY = LABEL;
	};
}
// === end ===

// === case: anonclass_walk_static_init ===
class InputFieldSortingAnonClassWalkStaticInitSliceViolation {
	static final String LABEL = "w";
	static final Object HOLDER = new Object() {
		static {
			System.out.println(LABEL);
		}
	};
}
// === end ===

// === case: anonclass_walk_uninitialized_member ===
class InputFieldSortingAnonClassWalkUninitializedMemberSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		String label;
	};
}
// === end ===

// === case: anonclass_wrapped_initializer ===
class InputFieldSortingAnonClassWrappedInitializerSliceViolation {
	final String label = "w";
	final Thread worker =
			new Thread() {
			};
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

// === case: array_dimension_at_the_cap_sorts ===
class InputFieldSortingArrayDimensionAtTheCapSortsSliceViolation {
	static int[] sizes = new int[65536];
	static Object registry = "r";
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
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
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
	beta,
	Alpha
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

// === case: constants_annotation_type_name_order ===
@interface InputFieldSortingConstantsAnnotationTypeNameOrderSliceViolation {
	int FIRST = 1;
	int SECOND = 2;
}
// === end ===

// === case: constants_enum_name_order ===
enum InputFieldSortingConstantsEnumNameOrderSliceViolation {
	ONE;

	static final int MAX = 10;
	static final int MIN = 1;
}
// === end ===

// === case: constants_record_name_order ===
record InputFieldSortingConstantsRecordNameOrderSliceViolation(int x) {
	static final int A = 1;
	static final int B = 2;
}
// === end ===

// === case: constants_sorted_not_merged_with_non_constants ===
class InputFieldSortingConstantsSortedNotMergedWithNonConstantsSliceViolation {
	static final int ALPHA = 1;
	static final int ZETA = 2;

	static int alpha, gamma;
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
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingDependencyForwardRefSliceViolation {
	int beta = this.alpha + 1;
	int alpha = 10;
}
// === end ===

// === case: dependency_later_chunk_anon_field_init ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkAnonFieldInitSliceViolation {
	final Object holder = new Object() {
		final long seed = counter;
	};

	long counter;
}
// === end ===

// === case: dependency_later_chunk_method_body_boundary ===
class InputFieldSortingDependencyLaterChunkMethodBodyBoundarySliceViolation {
	final Object holder = new Object() {
		long seed() {
			return counter;
		}
	};

	long counter;
}
// === end ===

// === case: dependency_later_chunk_plain ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkPlainSliceViolation {
	final long seed = counter + 1;

	long counter;
}
// === end ===

// === case: dependency_later_chunk_plain_backward ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkPlainBackwardSliceViolation {
	long counter = 5;

	final long seed = counter + 1;
}
// === end ===

// === case: dependency_later_chunk_with_chunk_violation ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkWithChunkViolationSliceViolation {
	long counter;

	final Object holder = new Object() {
		final long seed = counter;
	};
}
// === end ===

// === case: dependency_multi_var_releases_two_dependents ===
class InputFieldSortingDependencyMultiVarReleasesTwoDependentsSliceViolation {
	int alpha, beta;
	int gamma = alpha + beta;
	int delta = beta + alpha;
}
// === end ===

// === case: dependency_multi_var_two_dependents_pinned_skips ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyMultiVarTwoDependentsPinnedSkipsSliceViolation {
	int gamma = alpha;
	int delta = beta;
	int alpha = 1, beta = 2;
}
// === end ===

// === case: dependency_non_adjacent_anon_pair ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingDependencyNonAdjacentAnonPairSliceViolation {
	final Object alpha = new Object() {
		final String snapshot = String.valueOf(gamma);
	};
	final Object beta = new Object() {
	};
	final Object gamma = new Object() {
	};
}
// === end ===

// === case: dependency_non_adjacent_one_between ===
class InputFieldSortingDependencyNonAdjacentOneBetweenSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label;
	};
	final String alpha = String.valueOf(holder);
}
// === end ===

// === case: dependency_non_adjacent_plain ===
class InputFieldSortingDependencyNonAdjacentPlainSliceViolation {
	final String beta = "b", label = "w";
	final String alpha = label + "!";
}
// === end ===

// === case: dependency_non_adjacent_two_between ===
class InputFieldSortingDependencyNonAdjacentTwoBetweenSliceViolation {
	final String label = "w";
	final Object holder = new Object() {
		final String copy = label;
	};
	final String alpha = String.valueOf(holder);
	final String beta = String.valueOf(holder);
}
// === end ===

// === case: dependency_non_adjacent_two_targets ===
class InputFieldSortingDependencyNonAdjacentTwoTargetsSliceViolation {
	final String label = "w", name = "n";
	final Object holder = new Object() {
		final String copy = name + label;
	};
	final String alpha = String.valueOf(holder);
}
// === end ===

// === case: dependency_null_order_cross_chunk_anon_wait ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyNullOrderCrossChunkAnonWaitSliceViolation {
	Object a2 = new Object() {
	};
	final Object a0 = new Object() {
	};
	final Object p0 = a2;
	Object p2 = a0;
}
// === end ===

// === case: dependency_null_order_cycle_beats_later_chunk ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyNullOrderCycleBeatsLaterChunkSliceViolation {
	final long seed = counter + 1;

	long counter = seed;
}
// === end ===

// === case: dependency_null_order_cycle_beats_multi_var ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyNullOrderCycleBeatsMultiVarSliceViolation {
	int a = 1, c = d;
	int d = c;
}
// === end ===

// === case: dependency_null_order_earlier_chunk_dep_multi_var ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderEarlierChunkDepMultiVarSliceViolation {
	final int k = 0;

	int a = 1, c = b + k;
	int b = a;
}
// === end ===

// === case: dependency_null_order_later_chunk_beats_multi_var ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
// multi-fix-expected
class InputFieldSortingDependencyNullOrderLaterChunkBeatsMultiVarSliceViolation {
	final Object a = new Object() {
	}, c = new Object() {
		final Object x = b;
	};
	final Object b = a;
	final Object k = counter;

	Object counter;
}
// === end ===

// === case: dependency_null_order_multi_var_anon_cycle ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderMultiVarAnonCycleSliceViolation {
	final Object a = new Object() {
	}, c = new Object() {
		final Object x = b;
	};
	final Object b = a;
}
// === end ===

// === case: dependency_null_order_multi_var_plain_cycle ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderMultiVarPlainCycleSliceViolation {
	int a = 1, c = b;
	int b = a;
}
// === end ===

// === case: dependency_two_names_of_one_multi_var ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingDependencyTwoNamesOfOneMultiVarSliceViolation {
	int sum = alpha + beta;
	int alpha = 1, beta = 2;
}
// === end ===

// === case: detached_directive_above_blank_line_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingDetachedDirectiveAboveBlankLineSkipsSliceViolation {
	//noinspection unused

	int zeta;
	int alpha;
}
// === end ===

// === case: detached_directive_leading_entry_kept_reorders ===
class InputFieldSortingDetachedDirectiveLeadingEntryKeptReordersSliceViolation {
	//noinspection unused

	int alpha;
	int beta, zeta;
}
// === end ===

// === case: detached_directive_trailing_entry_kept_reorders ===
class InputFieldSortingDetachedDirectiveTrailingEntryKeptReordersSliceViolation {
	int alpha; //noinspection unused
	int beta;
	int gamma, zeta;
}
// === end ===

// === case: detached_directive_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingDetachedDirectiveTrailingLineAboveSkipsSliceViolation {
	final int limit = 1; //noinspection unused
	int zeta;
	int alpha;
}
// === end ===

// === case: division_by_a_nonzero_literal_sorts ===
class InputFieldSortingDivisionByANonzeroLiteralSortsSliceViolation {
	static final int HALF = 1 / 2;
	static final Object REGISTRY = System.getProperty("registry");
}
// === end ===

// === case: enum_constant_bare_forward_read_after_runner_warns ===
enum InputFieldSortingEnumConstantBareForwardReadAfterRunnerWarnsSliceViolation {
	MID(null, Integer.parseInt("1")),
	ZED(ALPHA, 0),
	ALPHA(null, 0);

	InputFieldSortingEnumConstantBareForwardReadAfterRunnerWarnsSliceViolation(Object parent, int id) {
	}
}
// === end ===

// === case: enum_constant_bare_forward_read_by_a_runner_keeps_its_place_warns ===
enum InputFieldSortingEnumConstantBareForwardReadByARunnerKeepsItsPlaceWarnsSliceViolation {
	ZED(null, 0),
	YEW(null, 0),
	BETA(ALPHA, Ids.next()),
	ALPHA(null, 0);

	InputFieldSortingEnumConstantBareForwardReadByARunnerKeepsItsPlaceWarnsSliceViolation(Object parent, int id) {
	}
}
// === end ===

// === case: enum_constant_bare_forward_read_warns ===
enum InputFieldSortingEnumConstantBareForwardReadWarnsSliceViolation {
	ZED(ALPHA),
	ALPHA(null);

	InputFieldSortingEnumConstantBareForwardReadWarnsSliceViolation(Object parent) {
	}
}
// === end ===

// === case: enum_constant_bodies_with_fields_warns ===
enum InputFieldSortingEnumConstantBodiesWithFieldsWarnsSliceViolation {
	ZETA {
		final int depth = 2;
	},
	ALPHA {
		final int width = 1;
	}
}
// === end ===

// === case: enum_constant_body_static_constant_warns ===
enum InputFieldSortingEnumConstantBodyStaticConstantWarnsSliceViolation {
	ZETA {
		static final int DEPTH = 2;
	},
	ALPHA {
		static final int WIDTH = 1;
	}
}
// === end ===

// === case: enum_constant_order_unobserved_by_a_maps_values_warns ===
enum InputFieldSortingEnumConstantOrderUnobservedByAMapsValuesWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object V = java.util.Map.of(1, 2).values();
}
// === end ===

// === case: enum_constant_order_unobserved_by_its_methods_warns ===
enum InputFieldSortingEnumConstantOrderUnobservedByItsMethodsWarnsSliceViolation {
	LOW,
	HIGH;

	String label() {
		return name();
	}
}
// === end ===

// === case: enum_constant_reads_earlier_constant_qualified_warns ===
enum InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation.ROOT),
	BOOKS(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation.ROOT);

	private final InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation parent;

	InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_constant_reads_earlier_constant_warns ===
enum InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(ROOT),
	BOOKS(ROOT);

	private final InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation parent;

	InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation(InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_constant_reads_nothing_warns ===
enum InputFieldSortingEnumConstantReadsNothingWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(null),
	BOOKS(null);

	private final InputFieldSortingEnumConstantReadsNothingWarnsSliceViolation parent;

	InputFieldSortingEnumConstantReadsNothingWarnsSliceViolation(InputFieldSortingEnumConstantReadsNothingWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_order_kept_body_static_field_running_code_warns ===
enum InputFieldSortingEnumOrderKeptBodyStaticFieldRunningCodeWarnsSliceViolation {
	ZETA {
		static final Object TAG = System.getProperty("zeta");
	},
	ALPHA {
		static final Object TAG = System.getProperty("alpha");
	}
}
// === end ===

// === case: enum_order_kept_body_static_initializer_warns ===
enum InputFieldSortingEnumOrderKeptBodyStaticInitializerWarnsSliceViolation {
	ZETA {
		static {
			System.out.println("zeta");
		}
	},
	ALPHA {
		static {
			System.out.println("alpha");
		}
	}
}
// === end ===

// === case: enum_order_kept_compare_to_in_method_warns ===
enum InputFieldSortingEnumOrderKeptCompareToInMethodWarnsSliceViolation {
	LOW,
	HIGH;

	boolean atLeast(InputFieldSortingEnumOrderKeptCompareToInMethodWarnsSliceViolation other) {
		return compareTo(other) >= 0;
	}
}
// === end ===

// === case: enum_order_kept_constant_body_method_reads_later_constant_warns ===
enum InputFieldSortingEnumOrderKeptConstantBodyMethodReadsLaterConstantWarnsSliceViolation {
	ZED {
		@Override
		Object next() {
			return ALPHA;
		}
	},
	ALPHA;

	Object next() {
		return null;
	}
}
// === end ===

// === case: enum_order_kept_constant_qualified_forward_read_warns ===
enum InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation {
	ZED(InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation.ALPHA),
	ALPHA(null);

	InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation(Object parent) {
	}
}
// === end ===

// === case: enum_order_kept_constant_reads_earlier_constant_warns ===
enum InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation {
	ROOT(null),
	BOOKS(ROOT);

	private final InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation parent;

	InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation(InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_order_kept_constructor_registers_warns ===
enum InputFieldSortingEnumOrderKeptConstructorRegistersWarnsSliceViolation {
	TITLE("Title"),
	AUTHOR("Author");

	private final int index;

	InputFieldSortingEnumOrderKeptConstructorRegistersWarnsSliceViolation(String header) {
		index = Csv.register(header);
	}
}
// === end ===

// === case: enum_order_kept_enum_map_warns ===
enum InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object LABELS = new java.util.EnumMap<InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation, String>(InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation.class);
}
// === end ===

// === case: enum_order_kept_enum_set_complement_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetComplementWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object REST = java.util.EnumSet.complementOf(java.util.EnumSet.of(ZETA));
}
// === end ===

// === case: enum_order_kept_enum_set_copy_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetCopyWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object SOME = java.util.EnumSet.copyOf(java.util.List.of(ZETA, ALPHA));
}
// === end ===

// === case: enum_order_kept_enum_set_of_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetOfWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object SOME = java.util.EnumSet.of(ZETA, ALPHA);
}
// === end ===

// === case: enum_order_kept_get_enum_constants_warns ===
enum InputFieldSortingEnumOrderKeptGetEnumConstantsWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object FIRST = InputFieldSortingEnumOrderKeptGetEnumConstantsWarnsSliceViolation.class.getEnumConstants()[0];
}
// === end ===

// === case: enum_order_kept_jdk_call_in_args_warns ===
enum InputFieldSortingEnumOrderKeptJdkCallInArgsWarnsSliceViolation {
	B(Math.max(1, 2)),
	A(3)
}
// === end ===

// === case: enum_order_kept_natural_order_warns ===
enum InputFieldSortingEnumOrderKeptNaturalOrderWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object ORDER = java.util.Comparator.naturalOrder();
}
// === end ===

// === case: enum_order_kept_nested_parens_call_in_args_warns ===
enum InputFieldSortingEnumOrderKeptNestedParensCallInArgsWarnsSliceViolation {
	B(foo(1, 2)),
	A(3)
}
// === end ===

// === case: enum_order_kept_own_qualified_values_warns ===
enum InputFieldSortingEnumOrderKeptOwnQualifiedValuesWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object LAST = InputFieldSortingEnumOrderKeptOwnQualifiedValuesWarnsSliceViolation.values()[1];
}
// === end ===

// === case: enum_order_kept_sorted_warns ===
enum InputFieldSortingEnumOrderKeptSortedWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object FIRST = java.util.stream.Stream.of(ZETA, ALPHA).sorted().findFirst();
}
// === end ===

// === case: enum_order_kept_tree_set_warns ===
enum InputFieldSortingEnumOrderKeptTreeSetWarnsSliceViolation {
	ZETA,
	ALPHA;

	static final Object SET = new java.util.TreeSet<>(java.util.List.of(ZETA, ALPHA));
}
// === end ===

// === case: enum_order_kept_values_in_static_field_warns ===
enum InputFieldSortingEnumOrderKeptValuesInStaticFieldWarnsSliceViolation {
	DEBUG,
	INFO,
	WARN,
	ERROR;

	static final InputFieldSortingEnumOrderKeptValuesInStaticFieldWarnsSliceViolation DEFAULT = values()[1];
}
// === end ===

// === case: enum_pin_constrained_warns ===
enum InputFieldSortingEnumPinConstrainedWarnsSliceViolation {
	ZETA(Integer.parseInt("1")),
	BETA(2),
	ALPHA(3);

	InputFieldSortingEnumPinConstrainedWarnsSliceViolation(int value) {
	}
}
// === end ===

// === case: enum_pin_runner_kept_last_warns ===
enum InputFieldSortingEnumPinRunnerKeptLastWarnsSliceViolation {
	BETA(2),
	ALPHA(3),
	ZETA(Integer.parseInt("1"));

	InputFieldSortingEnumPinRunnerKeptLastWarnsSliceViolation(int value) {
	}
}
// === end ===

// === case: enumconstant_annotated_after_previous ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousSliceViolation {
	ZETA,
	@Deprecated
	ALPHA
}
// === end ===

// === case: enumconstant_annotated_after_previous_comment ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousCommentSliceViolation {
	ZETA,
	/* mid */ @Deprecated
	ALPHA
}
// === end ===

// === case: enumconstant_annotated_same_line ===
enum InputFieldSortingEnumConstantAnnotatedSameLineSliceViolation {
	@Deprecated ZETA,
	ALPHA
}
// === end ===

// === case: enumconstant_annotated_same_line_leading_comment ===
enum InputFieldSortingEnumConstantAnnotatedSameLineLeadingCommentSliceViolation {
	/* keep */ @Deprecated ZETA,
	ALPHA
}
// === end ===

// === case: enumconstant_basic ===
enum InputFieldSortingEnumConstantBasicSliceViolation {
	ZEBRA,
	ALPHA;

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
		SECOND,
		FIRST
	}
}
// === end ===

// === case: enumconstant_multiple ===
enum InputFieldSortingEnumConstantMultipleSliceViolation {
	CHARLIE,
	BRAVO,
	ALPHA
}
// === end ===

// === case: enumconstant_outer_enum ===
enum InputFieldSortingEnumConstantOuterEnumSliceViolation {
	ALPHA,
	BETA;

	enum InnerMisordered {
		YELLOW,
		XENON
	}
}
// === end ===

// === case: enumconstant_single_then_fields_reorder ===
enum InputFieldSortingEnumConstantSingleThenFieldsReorderSliceViolation {
	A;
	int alpha = 2, beta = 1;
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
	ADD {
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
	BANANA("yellow"),
	APPLE("green");

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
	ALPHA
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings(value = "unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ZEBRA,
	ALPHA
}
// === end ===

// === case: enumkeyword_in_block_comment ===
/*
 * Doc paragraph mentioning enum constants
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentSliceViolation {
	final int alpha = 2, beta = 1;
}
// === end ===

// === case: enumkeyword_in_block_comment_with_braces ===
/*
 * Doc paragraph with example: { not real brace } and another { } pair
 * spanning multiple lines.
 */
class InputFieldSortingEnumKeywordInBlockCommentWithBracesSliceViolation {
	final int alpha = 2, beta = 1;
}
// === end ===

// === case: enumkeyword_in_string_initializer ===
class InputFieldSortingEnumKeywordInStringInitializerSliceViolation {
	final String alpha = "one", docs = "enum constants list";
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
	ALPHA
}
// === end ===

// === case: enumkeyword_in_text_block_above_class ===
class InputFieldSortingEnumKeywordInTextBlockAboveClassSliceViolation {
	static final String docs = """
			line one
			describes enum constants list
			more lines
			""";
	final int alpha = 2, beta = 1;
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
	ALPHA
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
	ALPHA
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
	final int alpha = 2, beta = 1;
}
// === end ===

// === case: enumkeyword_sibling_enum_lookback_boundary ===
enum InputFieldSortingEnumKeywordSiblingEnumA {
	A
}

class InputFieldSortingEnumKeywordSiblingEnumLookbackBoundarySliceViolation {
	final int alpha = 2, beta = 1;
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
	BETA { void foo() {} },
	ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_array_value ===
enum InputFieldSortingEnumSpanAnnotationArgArrayValueSliceViolation {
	ZETA,
	@SuppressWarnings({"a", "b"}) ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	ZETA,
	@Deprecated() ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	ZETA,
	@SuppressWarnings(value = "unused") ALPHA
}
// === end ===

// === case: enumspan_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSpanAnnotationArgStructuralCharsSliceViolation {
	ZETA,
	@SuppressWarnings("{ } , // /*") ALPHA
}
// === end ===

// === case: enumspan_blank_line_between_constants ===
enum InputFieldSortingEnumSpanBlankLineBetweenConstantsSliceViolation {
	ZETA,

	ALPHA
}
// === end ===

// === case: enumspan_block_comment_before_terminator ===
enum InputFieldSortingEnumSpanBlockCommentBeforeTerminatorSliceViolation {
	BETA,
	ALPHA,
	ZETA /* note */;

	int legacy;
}
// === end ===

// === case: enumspan_block_comment_before_terminator_line ===
enum InputFieldSortingEnumSpanBlockCommentBeforeTerminatorLineSliceViolation {
	GREEN,
	BLUE
	/* Shared helper. */
	;

	void helper() {
	}
}
// === end ===

// === case: enumspan_brace_close_shared ===
enum InputFieldSortingEnumSpanBraceCloseSharedSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumspan_brace_open_shared ===
enum InputFieldSortingEnumSpanBraceOpenSharedSliceViolation { BETA,
	ALPHA
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
	ZETA,
	ALPHA; String d = """
			x
			""";
}
// === end ===

// === case: enumspan_comment_before_constant_ident ===
enum InputFieldSortingEnumSpanCommentBeforeConstantIdentSliceViolation {
	ZETA,
	/* c */ ALPHA
}
// === end ===

// === case: enumspan_comment_before_separator ===
enum InputFieldSortingEnumSpanCommentBeforeSeparatorSliceViolation {
	ZETA /* t */,
	ALPHA
}
// === end ===

// === case: enumspan_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSpanCommentBetweenAnnotationAndConstantSliceViolation {
	ZETA,
	@Deprecated
	// note
	ALPHA
}
// === end ===

// === case: enumspan_comment_between_stacked_annotations ===
enum InputFieldSortingEnumSpanCommentBetweenStackedAnnotationsSliceViolation {
	ZETA,
	@Deprecated /* x */ @SuppressWarnings("y")
	ALPHA
}
// === end ===

// === case: enumspan_comment_block_trailer ===
enum InputFieldSortingEnumSpanCommentBlockTrailerSliceViolation {
	ZETA,
	ALPHA
	// tail
}
// === end ===

// === case: enumspan_comment_escaped_line_terminator_warns ===
enum InputFieldSortingEnumSpanCommentEscapedLineTerminatorWarnsSliceViolation {
	// \u000a
	ZETA,
	ALPHA
}
// === end ===

// === case: enumspan_comment_in_argument_list ===
enum InputFieldSortingEnumSpanCommentInArgumentListSliceViolation {
	ZETA(1),
	ALPHA(/* c */ 2);

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
	ALPHA {
		// a note
	}
}
// === end ===

// === case: enumspan_comment_open_after_comma ===
enum InputFieldSortingEnumSpanCommentOpenAfterCommaSliceViolation {
	ZETA, /* ZETA is legacy;
	see #123 */
	ALPHA
}
// === end ===

// === case: enumspan_comment_open_after_terminator ===
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
enum InputFieldSortingEnumSpanCommentOpenOnHeaderLineSliceViolation { /*
ordinal order is load-bearing
*/
	ZETA,
	ALPHA
}
// === end ===

// === case: enumspan_comment_straddles_comma ===
enum InputFieldSortingEnumSpanCommentStraddlesCommaSliceViolation {
	ZETA, /*a*/
	/*b*/ ALPHA
}
// === end ===

// === case: enumspan_escaped_triple_quote ===
enum InputFieldSortingEnumSpanEscapedTripleQuoteSliceViolation {
	ZETA,
	@SuppressWarnings("\"\"\"") ALPHA
}
// === end ===

// === case: enumspan_generic_implements_header ===
enum InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation implements Comparable<InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation> {
	ZETA,
	ALPHA
}
// === end ===

// === case: enumspan_header_line_comment ===
enum InputFieldSortingEnumSpanHeaderLineCommentSliceViolation { // note
	ZETA,
	ALPHA
}
// === end ===

// === case: enumspan_javadoc_after_comma_warns ===
enum InputFieldSortingEnumSpanJavadocAfterCommaWarnsSliceViolation {
	ZETA, /** doc */
	ALPHA
}
// === end ===

// === case: enumspan_javadoc_before_terminator_line_warns ===
enum InputFieldSortingEnumSpanJavadocBeforeTerminatorLineWarnsSliceViolation {
	GREEN,
	BLUE
	/** Shared helper. */
	;

	void helper() {
	}
}
// === end ===

// === case: enumspan_javadoc_before_terminator_warns ===
enum InputFieldSortingEnumSpanJavadocBeforeTerminatorWarnsSliceViolation {
	BETA,
	ALPHA,
	ZETA /** @deprecated */;

	int legacy;
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
	ALPHA
}
// === end ===

// === case: enumspan_lead_both_owners ===
enum InputFieldSortingEnumSpanLeadBothOwnersSliceViolation {
	// note-z
	ZETA,
	// note-a
	ALPHA
}
// === end ===

// === case: enumspan_lead_first_not_sorted ===
enum InputFieldSortingEnumSpanLeadFirstNotSortedSliceViolation {
	// note
	ZETA,
	ALPHA
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
	ZOMBIE,
	ACTIVE
	/* line1

	   line3 */
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSpanListTailCommentNoTrailingCommaSliceViolation {
	ZOMBIE,
	ACTIVE
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_own_line ===
enum InputFieldSortingEnumSpanListTailCommentOwnLineSliceViolation {
	ZOMBIE,
	ACTIVE,
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_member_shares_terminator_line ===
class InputFieldSortingEnumSpanMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ZETA,
		ALPHA; static final int VAL = 1;
	}
}
// === end ===

// === case: enumspan_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSpanMultilineAnnotationFirstLineSharedSliceViolation {
	BETA, @SuppressWarnings(
			"x")
	ALPHA
}
// === end ===

// === case: enumspan_nested_type_after_terminator ===
enum InputFieldSortingEnumSpanNestedTypeAfterTerminatorSliceViolation {
	ZETA,
	ALPHA;

	interface Marker {
	}
}
// === end ===

// === case: enumspan_non_canonical_indent ===
enum InputFieldSortingEnumSpanNonCanonicalIndentSliceViolation {
			ZETA,
			ALPHA
}
// === end ===

// === case: enumspan_perm_acb ===
enum InputFieldSortingEnumSpanPermAcbSliceViolation {
	// L-A
	AAA, // T-A
	// L-C
	CCC, // T-C
	// L-B
	BBB // T-B
}
// === end ===

// === case: enumspan_perm_bac ===
enum InputFieldSortingEnumSpanPermBacSliceViolation {
	// L-B
	BBB, // T-B
	// L-A
	AAA, // T-A
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
	AAA // T-A
}
// === end ===

// === case: enumspan_perm_cab ===
enum InputFieldSortingEnumSpanPermCabSliceViolation {
	// L-C
	CCC, // T-C
	// L-A
	AAA, // T-A
	// L-B
	BBB // T-B
}
// === end ===

// === case: enumspan_perm_cba ===
enum InputFieldSortingEnumSpanPermCbaSliceViolation {
	// L-C
	CCC, // T-C
	// L-B
	BBB, // T-B
	// L-A
	AAA // T-A
}
// === end ===

// === case: enumspan_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSpanStackedAnnotationsAfterPreviousSliceViolation {
	ZETA,
	/* mid */ @Deprecated
	@SuppressWarnings("x")
	ALPHA
}
// === end ===

// === case: enumspan_string_delimiter_before_annotation ===
enum InputFieldSortingEnumSpanStringDelimiterBeforeAnnotationSliceViolation {
	@SuppressWarnings(
			"unchecked") @Deprecated
	BETA,
	ALPHA
}
// === end ===

// === case: enumspan_string_with_block_comment_marker ===
enum InputFieldSortingEnumSpanStringWithBlockCommentMarkerSliceViolation {
	ZETA,
	@SuppressWarnings("/* not a comment */") ALPHA
}
// === end ===

// === case: enumspan_suffix_unclosed_comment_after_brace ===
enum InputFieldSortingEnumSpanSuffixUnclosedCommentAfterBraceSliceViolation {
	ZETA,
	ALPHA,
	BETA } /*
still open
*/
// === end ===

// === case: enumspan_supplementary_annotation_arg ===
enum InputFieldSortingEnumSpanSupplementaryAnnotationArgSliceViolation {
	ZETA,
	@SuppressWarnings("𝐀") ALPHA
}
// === end ===

// === case: enumspan_supplementary_constant_name ===
enum InputFieldSortingEnumSpanSupplementaryConstantNameSliceViolation {
	ZETA,
	ALPHA𝐀
}
// === end ===

// === case: enumspan_text_block_arg_multiline ===
enum InputFieldSortingEnumSpanTextBlockArgMultilineSliceViolation {
	ZETA("""
			x
			"""),
	ALPHA
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
	ALPHA {
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
	ALPHA,
}
// === end ===

// === case: enumspan_trailing_comma_terminator_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommaTerminatorCommentMultilineSliceViolation {
	ZETA,
	ALPHA, // done
	// more
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSpanTrailingCommaThenTerminatorCommentSliceViolation {
	ZETA,
	ALPHA, // done
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommentMultilineSliceViolation {
	ZETA /* a */
	/* b */, ALPHA
}
// === end ===

// === case: enumsplit_annotated_same_line ===
enum InputFieldSortingEnumSplitAnnotatedSameLineSliceViolation {
	@Deprecated
	ALPHA,
	ZETA
}
// === end ===

// === case: enumsplit_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSplitAnnotationArgStructuralCharsSliceViolation {
	ALPHA,
	@SuppressWarnings("{ } , // /*")
	ZETA
}
// === end ===

// === case: enumsplit_blank_line_between_constants ===
enum InputFieldSortingEnumSplitBlankLineBetweenConstantsSliceViolation {
	ALPHA,
	BETA,
	ZETA
}
// === end ===

// === case: enumsplit_brace_open_shared ===
enum InputFieldSortingEnumSplitBraceOpenSharedSliceViolation {
	ALPHA,
	BETA,
	ZETA
}
// === end ===

// === case: enumsplit_comment_after_comma ===
enum InputFieldSortingEnumSplitCommentAfterCommaSliceViolation {
	ALPHA,
	/* mid */ ZETA
}
// === end ===

// === case: enumsplit_comment_before_separator ===
enum InputFieldSortingEnumSplitCommentBeforeSeparatorSliceViolation {
	ALPHA, /* t */
	ZETA
}
// === end ===

// === case: enumsplit_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSplitCommentBetweenAnnotationAndConstantSliceViolation {
	@Deprecated
	// note
	ALPHA,
	ZETA
}
// === end ===

// === case: enumsplit_comment_open_after_terminator ===
// skip-reason: cannot split enum constants onto their own lines: a comment between them does not close on the line it opens
enum InputFieldSortingEnumSplitCommentOpenAfterTerminatorSliceViolation {
	ALPHA, BETA,
	ZETA; /*
	legacy ordinal order is load-bearing
	*/

	int rgb;
}
// === end ===

// === case: enumsplit_comment_open_on_header_line ===
// skip-reason: cannot split enum constants onto their own lines: a comment between them does not close on the line it opens
enum InputFieldSortingEnumSplitCommentOpenOnHeaderLineSliceViolation { /*
ordinal order is load-bearing
*/
	ALPHA, ZETA
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_non_nls_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineNonNlsSkipsSliceViolation {
	ALPHA("a"), BETA("b",
		"x"), GAMMA("c"); //$NON-NLS-1$

	InputFieldSortingEnumSplitConstantEndingOnSharedLineNonNlsSkipsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_nosonar_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineNosonarSkipsSliceViolation {
	ALPHA("a"), BETA("b",
		"x"), GAMMA("c"); // NOSONAR

	InputFieldSortingEnumSplitConstantEndingOnSharedLineNosonarSkipsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_splits ===
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineSplitsSliceViolation {
	ALPHA("a"),
	BETA("b",
		"x"),
	GAMMA("c");

	InputFieldSortingEnumSplitConstantEndingOnSharedLineSplitsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_directive_on_unshared_line_splits ===
enum InputFieldSortingEnumSplitDirectiveOnUnsharedLineSplitsSliceViolation {
	ALPHA, // NOSONAR
	BETA,
	GAMMA
}
// === end ===

// === case: enumsplit_formatter_off_above_header_splits ===
// @formatter:off
enum InputFieldSortingEnumSplitFormatterOffAboveHeaderSplitsSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumsplit_formatter_off_on_header_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitFormatterOffOnHeaderSkipsSliceViolation { // @formatter:off
	ALPHA, BETA
}
// === end ===

// === case: enumsplit_header_line_comment ===
enum InputFieldSortingEnumSplitHeaderLineCommentSliceViolation { // note
	ALPHA,
	ZETA
}
// === end ===

// === case: enumsplit_javadoc_after_comma ===
enum InputFieldSortingEnumSplitJavadocAfterCommaSliceViolation {
	ALPHA,
	BETA, /** doc */
	ZETA
}
// === end ===

// === case: enumsplit_javadoc_before_terminator ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
enum InputFieldSortingEnumSplitJavadocBeforeTerminatorSliceViolation {
	ALPHA, BETA,
	ZETA /** @deprecated */;

	int legacy;
}
// === end ===

// === case: enumsplit_line_count_window_ending_above_header_splits ===
class InputFieldSortingEnumSplitLineCountWindowEndingAboveHeaderSplitsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	static final int LIMIT = 42;

	enum Inner {
		ALPHA,
		BETA
	}
}
// === end ===

// === case: enumsplit_line_count_window_from_below_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitLineCountWindowFromBelowSkipsSliceViolation {
	ALPHA(42), ZETA(7);
	// CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
	static final int OTHER = 1;
}
// === end ===

// === case: enumsplit_line_count_window_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitLineCountWindowSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	ALPHA(42), ZETA(7)
}
// === end ===

// === case: enumsplit_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSplitListTailCommentNoTrailingCommaSliceViolation {
	ACTIVE,
	ZOMBIE;
	// NOTE: order is serialized to disk

	int x;
}
// === end ===

// === case: enumsplit_list_tail_comment_own_line ===
enum InputFieldSortingEnumSplitListTailCommentOwnLineSliceViolation {
	ACTIVE,
	ZOMBIE;
	// NOTE: order is serialized to disk

	int x;
}
// === end ===

// === case: enumsplit_member_shares_terminator_line ===
class InputFieldSortingEnumSplitMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ALPHA,
		ZETA;
		static final int VAL = 1;
	}
}
// === end ===

// === case: enumsplit_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSplitMultilineAnnotationFirstLineSharedSliceViolation {
	ALPHA,
	@SuppressWarnings(
			"x")
	BETA,
	ZETA
}
// === end ===

// === case: enumsplit_non_canonical_indent ===
enum InputFieldSortingEnumSplitNonCanonicalIndentSliceViolation {
			ALPHA,
			ZETA
}
// === end ===

// === case: enumsplit_non_nls_tail_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNonNlsTailSkipsSliceViolation {
	FAIL("fail"), OK("ok") //$NON-NLS-1$ //$NON-NLS-2$
}
// === end ===

// === case: enumsplit_nosonar_block_comment_closing_on_shared_line_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNosonarBlockCommentClosingOnSharedLineSkipsSliceViolation {
	/* NOSONAR
	 */ ALPHA, BETA
}
// === end ===

// === case: enumsplit_nosonar_tail_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNosonarTailSkipsSliceViolation {
	ALPHA, ZETA // NOSONAR
}
// === end ===

// === case: enumsplit_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSplitStackedAnnotationsAfterPreviousSliceViolation {
	ALPHA,
	/* mid */ @Deprecated
	@SuppressWarnings("x")
	ZETA
}
// === end ===

// === case: enumsplit_switch_directive_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitSwitchDirectiveSkipsSliceViolation {
	// @formatter:off
	ALPHA, ZETA
	// @formatter:on
}
// === end ===

// === case: enumsplit_text_block_arg_multiline ===
enum InputFieldSortingEnumSplitTextBlockArgMultilineSliceViolation {
	ALPHA("""
			x
			"""),
	BETA,
	ZETA
}
// === end ===

// === case: enumsplit_trailing_comma ===
enum InputFieldSortingEnumSplitTrailingCommaSliceViolation {
	ALPHA,
	BETA
}
// === end ===

// === case: enumsplit_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSplitTrailingCommaThenTerminatorCommentSliceViolation {
	ALPHA,
	ZETA; // done

	int x;
}
// === end ===

// === case: enumsplit_trailing_comment_multiline ===
// skip-reason: cannot relocate a comment trailing an enum constant: it spans multiple lines
enum InputFieldSortingEnumSplitTrailingCommentMultilineSliceViolation {
	ALPHA /* a */
	/* b */, BETA, ZETA
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
	// @Nullable
	String alpha, beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_initializer ===
class InputFieldSortingFieldAnnotationIgnoresAtInInitializerSliceViolation {
	Object alpha, beta = new @TypeUse Object();
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
	String alpha = "hello", beta = "@Zebra";
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
	final List<String> alpha = null;
	final List<@Zebra String> beta = null;
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
	final char alpha = 'a', zebra = '{';
}
// === end ===

// === case: field_braces_in_single_line_string_between_field_and_class_open ===
class InputFieldSortingFieldBracesInSingleLineStringBetweenFieldAndClassOpenSliceViolation {
	final String alpha = "a", zebra = "{ fake brace }";
}
// === end ===

// === case: field_c_style_array_declarator_refused ===
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingFieldCStyleArrayDeclaratorRefusedSliceViolation {
	private int y[], x;
}
// === end ===

// === case: field_chunk_keywords_in_block_comment ===
class InputFieldSortingFieldChunkKeywordsInBlockCommentSliceViolation {
	String /* static final = */ alpha, zebra;
}
// === end ===

// === case: field_chunk_keywords_in_string ===
class InputFieldSortingFieldChunkKeywordsInStringSliceViolation {
	final String alpha = "static final =", zebra = "static final =";
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
// skip-reason: cannot reorder fields: they have a circular dependency
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
	final String alpha = "a", zebra = "z";
}
// === end ===

// === case: field_comment_escaped_line_terminator_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldCommentEscapedLineTerminatorSkipsSliceViolation {
	private int zeta = 1; // \u000a { alpha = 9; }
	private int alpha = 3;
}
// === end ===

// === case: field_consolidation_annotation_param_order_merges ===
class InputFieldSortingFieldConsolidationAnnotationParamOrderMergesSliceViolation {
	@Multi(b = 2, a = 1)
	private long alpha, zeta;
}
// === end ===

// === case: field_consolidation_base_annotation_line_comment ===
class InputFieldSortingFieldConsolidationBaseAnnotationLineCommentSliceViolation {
	private int count;
	@Deprecated // set in onBind
	private String body, title;
}
// === end ===

// === case: field_consolidation_base_carried_close_reorders ===
class InputFieldSortingFieldConsolidationBaseCarriedCloseReordersSliceViolation {
	/* in
	*/ private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_base_comment_before_type ===
class InputFieldSortingFieldConsolidationBaseCommentBeforeTypeSliceViolation {
	/* px */ int alpha, beta;
}
// === end ===

// === case: field_consolidation_base_empty_block_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseEmptyBlockCommentReordersSliceViolation {
	/**/
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_base_leading_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseLeadingCommentReordersSliceViolation {
	// in pixels
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_block_comment_contains_double_slash ===
class InputFieldSortingFieldConsolidationBlockCommentContainsDoubleSlashSliceViolation {
	String alpha;
	String zebra /* fake // comment */;
}
// === end ===

// === case: field_consolidation_candidate_annotation_line_comment ===
class InputFieldSortingFieldConsolidationCandidateAnnotationLineCommentSliceViolation {
	private int count;
	// set in onBind
	@Deprecated
	private String body, title;
}
// === end ===

// === case: field_consolidation_candidate_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeSemicolonSliceViolation {
	int alpha;
	int beta /* px */;
}
// === end ===

// === case: field_consolidation_candidate_comment_before_type ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeTypeSliceViolation {
	// px
	int alpha, beta;
}
// === end ===

// === case: field_consolidation_comment_line_splits_three_fields ===
class InputFieldSortingFieldConsolidationCommentLineSplitsThreeFieldsSliceViolation {
	private int alpha;
	private int beta;
	// note
	private int zeta;
}
// === end ===

// === case: field_consolidation_hoisted_comments_source_order ===
class InputFieldSortingFieldConsolidationHoistedCommentsSourceOrderSliceViolation {
	private final int limit = 3;

	// set by native code read by tests
	@Deprecated
	private int alpha, beta, zeta;
}
// === end ===

// === case: field_consolidation_inline_base_stacked_commented_candidate ===
class InputFieldSortingFieldConsolidationInlineBaseStackedCommentedCandidateSliceViolation {
	private int count;
	// set in onBind
	@Deprecated private String body, title;
}
// === end ===

// === case: field_consolidation_keeps_directive_on_base ===
class InputFieldSortingFieldConsolidationKeepsDirectiveOnBaseSliceViolation {
	@Deprecated // NOSONAR
	private int alpha, zeta;
}
// === end ===

// === case: field_consolidation_long_initializer_reorders_without_merge ===
class InputFieldSortingFieldConsolidationLongInitializerReordersWithoutMergeSliceViolation {
	int alpha;
	int zeta = 1_000_000 + 2_000_000 + 3_000_000;
}
// === end ===

// === case: field_consolidation_moves_near_miss_directive ===
class InputFieldSortingFieldConsolidationMovesNearMissDirectiveSliceViolation {
	// NOSONA
	@Deprecated
	private int alpha, zeta;
}
// === end ===

// === case: field_consolidation_nested_type_merges ===
class InputFieldSortingFieldConsolidationNestedTypeMergesSliceViolation {
	private ViewGroup.LayoutParams inner, outer;
}
// === end ===

// === case: field_consolidation_repeated_annotation_comment_closes_on_decl_line ===
class InputFieldSortingFieldConsolidationRepeatedAnnotationCommentClosesOnDeclLineSliceViolation {
	private int count;
	@Deprecated /* set in
	 */ private String body;
	@Deprecated /* set in
	 */private String title;
}
// === end ===

// === case: field_consolidation_repeated_annotation_line_comment_merges ===
class InputFieldSortingFieldConsolidationRepeatedAnnotationLineCommentMergesSliceViolation {
	private int count;
	@Deprecated // set in onBind
	private String body, title;
}
// === end ===

// === case: field_consolidation_repeated_directive_not_moved ===
class InputFieldSortingFieldConsolidationRepeatedDirectiveNotMovedSliceViolation {
	@Deprecated // NOSONAR
	private int alpha, zeta;
}
// === end ===

// === case: field_consolidation_shared_prefix_comment_merges ===
class InputFieldSortingFieldConsolidationSharedPrefixCommentMergesSliceViolation {
	/* px */ int alpha, zebra;
}
// === end ===

// === case: field_consolidation_skips_across_block_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossBlockCommentLineSliceViolation {
	/* in pixels */
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_across_line_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossLineCommentLineSliceViolation {
	// in pixels
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationSkipsBaseCommentBeforeSemicolonSliceViolation {
	int alpha /* px */;
	int beta;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocSliceViolation {
	/** Height in pixels. */
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_annotation ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterAnnotationSliceViolation {
	@Deprecated /** d */ private int height;
	@Deprecated private int width;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_close_at_column_zero ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCloseAtColumnZeroSliceViolation {
	/* in
*/ /** d */ private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_close_at_index_one ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCloseAtIndexOneSliceViolation {
	/* in
	*/ /** d */ private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_comment ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCommentSliceViolation {
	/* in
	 pixels */ /** doc */ private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_markdown_doc_comment ===
class InputFieldSortingFieldConsolidationSkipsBaseMarkdownDocCommentSliceViolation {
	/// Height in pixels.
	private int height;
	private int width;
}
// === end ===

// === case: field_consolidation_skips_base_starred_javadoc ===
class InputFieldSortingFieldConsolidationSkipsBaseStarredJavadocSliceViolation {
	/***/
	private int height;
	private int width;
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

// === case: field_consolidation_skips_directive_ahead_of_base_type ===
class InputFieldSortingFieldConsolidationSkipsDirectiveAheadOfBaseTypeSliceViolation {
	/* NOSONAR */ int alpha;
	int zeta;
}
// === end ===

// === case: field_consolidation_skips_directive_on_base_line ===
class InputFieldSortingFieldConsolidationSkipsDirectiveOnBaseLineSliceViolation {
	private int alpha; // NOSONAR
	private int zeta;
}
// === end ===

// === case: field_consolidation_skips_hoist_below_base_leading_comment_line ===
class InputFieldSortingFieldConsolidationSkipsHoistBelowBaseLeadingCommentLineSliceViolation {
	// in pixels
	private int height;
	/* w */ private int width;
}
// === end ===

// === case: field_consolidation_skips_moved_directive_ahead_of_type ===
class InputFieldSortingFieldConsolidationSkipsMovedDirectiveAheadOfTypeSliceViolation {
	int alpha;
	/* NOSONAR */ int zeta;
}
// === end ===

// === case: field_consolidation_skips_moved_directive_on_annotation_line ===
class InputFieldSortingFieldConsolidationSkipsMovedDirectiveOnAnnotationLineSliceViolation {
	@Deprecated
	private int alpha;
	@Deprecated // NOSONAR
	private int zeta;
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

// === case: field_consolidation_skips_repeated_annotation_order ===
class InputFieldSortingFieldConsolidationSkipsRepeatedAnnotationOrderSliceViolation {
	@Tag("x")
	@Tag("y")
	String alpha;
	@Tag("y")
	@Tag("x")
	String zebra;
}
// === end ===

// === case: field_consolidation_skips_same_simple_name_annotations ===
class InputFieldSortingFieldConsolidationSkipsSameSimpleNameAnnotationsSliceViolation {
	@org.jetbrains.annotations.Nullable
	String alpha;
	@javax.annotation.Nullable
	String zebra;
}
// === end ===

// === case: field_consolidation_skips_same_simple_nested_type ===
class InputFieldSortingFieldConsolidationSkipsSameSimpleNestedTypeSliceViolation {
	private LinearLayout.LayoutParams inner;
	private ViewGroup.LayoutParams outer;
}
// === end ===

// === case: field_consolidation_trailing_block_comment ===
class InputFieldSortingFieldConsolidationTrailingBlockCommentSliceViolation {
	int alpha; /* px */
	int beta;
}
// === end ===

// === case: field_consolidation_two_stacked_annotations ===
class InputFieldSortingFieldConsolidationTwoStackedAnnotationsSliceViolation {
	@Deprecated
	@SuppressWarnings("unused")
	private long alpha;
	@Deprecated
	@SuppressWarnings("unused")
	private long zeta;
}
// === end ===

// === case: field_consolidation_wrapped_base_after_equals ===
class InputFieldSortingFieldConsolidationWrappedBaseAfterEqualsSliceViolation {
	private long delay =
			60;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_block_comment ===
class InputFieldSortingFieldConsolidationWrappedBaseBlockCommentSliceViolation {
	private long delay = 60
			/* ms */ * 1000;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_dot ===
class InputFieldSortingFieldConsolidationWrappedBaseDotSliceViolation {
	private String delay = "60"
			.trim();
	private String timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_equals_on_continuation ===
class InputFieldSortingFieldConsolidationWrappedBaseEqualsOnContinuationSliceViolation {
	private long delay
			= 60;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_minus ===
class InputFieldSortingFieldConsolidationWrappedBaseMinusSliceViolation {
	private long delay = 60
			- 1;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_plus ===
class InputFieldSortingFieldConsolidationWrappedBasePlusSliceViolation {
	private long delay = 60
			+ 1000;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_base_star ===
class InputFieldSortingFieldConsolidationWrappedBaseStarSliceViolation {
	private long delay = 60
			* 1000;
	private long timeout;
}
// === end ===

// === case: field_consolidation_wrapped_candidate ===
class InputFieldSortingFieldConsolidationWrappedCandidateSliceViolation {
	private long alpha;
	private long beta = 60
			* 1000;
}
// === end ===

// === case: field_consolidation_wrapped_multi_var_base ===
class InputFieldSortingFieldConsolidationWrappedMultiVarBaseSliceViolation {
	private long alpha,
			beta;
	private long zeta;
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
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingFieldDependencyInitializerOnContinuationLineSliceViolation {
	int beta = 1
			+ this.alpha;
	int alpha = 10;
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
	int alpha = 2, zebra = 1;

	class Inner {
		int beta = 5;
	}
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_nested_local ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesNestedLocalSliceViolation {
	int alpha = 2, beta = 1;

	void m() {
		int charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_duplicate_text_below_does_not_steal_endidx ===
@SuppressWarnings("unused")
class InputFieldSortingFieldDuplicateTextBelowDoesNotStealEndIdxSliceViolation {
	int alpha = 2, beta = 1;

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
	int alpha = 7, yankee = 5 < 10 ? 1 : 0, zulu = 5;
}
// === end ===

// === case: field_extract_names_initializer_angle_explicit_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldExtractNamesInitializerAngleExplicitTypeArgSliceViolation {
	int alpha, yak = List.<Integer>of().size(), zulu = 5;
}
// === end ===

// === case: field_extract_names_initializer_brace_comma ===
class InputFieldSortingFieldExtractNamesInitializerBraceCommaSliceViolation {
	int[] alpha = {6}, yankee = {1, 2, 3}, zulu = {4, 5};
}
// === end ===

// === case: field_extract_names_initializer_char_comma ===
class InputFieldSortingFieldExtractNamesInitializerCharCommaSliceViolation {
	char alpha = 'a', max = ',';
}
// === end ===

// === case: field_extract_names_initializer_paren_comma ===
@SuppressWarnings("unused")
class InputFieldSortingFieldExtractNamesInitializerParenCommaSliceViolation {
	int alpha, max = Math.max(1, 2);
}
// === end ===

// === case: field_extract_names_initializer_string_comma ===
class InputFieldSortingFieldExtractNamesInitializerStringCommaSliceViolation {
	String alpha = "a", max = "x,y";
}
// === end ===

// === case: field_first_name_after_multi_name_declaration_reorders ===
class InputFieldSortingFieldFirstNameAfterMultiNameDeclarationReordersSliceViolation {
	long alpha, beta, gamma, zeta;
}
// === end ===

// === case: field_first_name_after_single_reorders ===
class InputFieldSortingFieldFirstNameAfterSingleReordersSliceViolation {
	long alpha, beta, zeta;
}
// === end ===

// === case: field_inner_class_skipped ===
class InputFieldSortingFieldInnerClassSkippedSliceViolation {
	class Inner {
		int first = 1;
	}

	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_inner_interface_skipped ===
class InputFieldSortingFieldInnerInterfaceSkippedSliceViolation {
	interface Inner {
		int FIRST = 1;
	}

	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_inner_record_skipped ===
class InputFieldSortingFieldInnerRecordSkippedSliceViolation {
	record Inner(int x) {}

	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_instance_initializer_block_skipped ===
class InputFieldSortingFieldInstanceInitializerBlockSkippedSliceViolation {
	int alpha = 2, beta = 1;

	{
		Math.random();
	}
}
// === end ===

// === case: field_interleaved_across_instance_initializer_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldInterleavedAcrossInstanceInitializerSkipsSliceViolation {
	private int retries = 3;

	{
		retries = Integer.getInteger("app.retries", 5);
	}

	private static int instances;
	private int attempts;
}
// === end ===

// === case: field_interleaved_across_method_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldInterleavedAcrossMethodSkipsSliceViolation {
	int zeta;

	void use() {
	}

	static int shared;
	int alpha;
}
// === end ===

// === case: field_interleaved_across_static_initializer_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldInterleavedAcrossStaticInitializerSkipsSliceViolation {
	static final String ZETA = "z";

	static {
		System.out.println(ZETA);
	}

	int count;
	static final String ALPHA = "a";
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
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
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingFieldInterleavedSpansChunksBailSliceViolation {
	int alpha;
	static int s;
	final int zebra = 1;
}
// === end ===

// === case: field_javadoc_on_class_header_line_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldJavadocOnClassHeaderLineSkipsSliceViolation { /** The zeta. */
	int zeta;
	int alpha;
}
// === end ===

// === case: field_javadoc_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldJavadocTrailingLineAboveSkipsSliceViolation {
	private int zeta; /**< the zeta */
	private int alpha; /**< the alpha */
}
// === end ===

// === case: field_local_in_method_body_not_treated_as_field ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLocalInMethodBodyNotTreatedAsFieldSliceViolation {
	int alpha = 2, beta = 1;

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
	final int alpha = 2, zebra = 1;
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

// === case: field_member_between_fields ===
// skip-reason: cannot reorder fields: code between them belongs to no field
class InputFieldSortingFieldMemberBetweenFieldsSliceViolation {
	int beta = 1;

	void m() {
	}

	int alpha = 2;
}
// === end ===

// === case: field_method_in_class_skipped ===
class InputFieldSortingFieldMethodInClassSkippedSliceViolation {
	int alpha = 2, beta = 1;

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

	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_multiline_inner_record_skipped ===
class InputFieldSortingFieldMultiLineInnerRecordSkippedSliceViolation {
	record Inner(
			int x,
			int y
	) {}

	int alpha = 2, beta = 1;
}
// === end ===

// === case: field_multiline_method_in_class_skipped ===
class InputFieldSortingFieldMultiLineMethodInClassSkippedSliceViolation {
	int alpha = 2, beta = 1;

	void method(
			int x,
			int y
	) {
		System.out.println(x);
		System.out.println(y);
	}
}
// === end ===

// === case: field_multivar_secondary_name_dependency_pinned_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyPinnedReordersSliceViolation {
	int x, y, z = 1;
	int a = y + 1;
}
// === end ===

// === case: field_multivar_secondary_name_dependency_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyReordersSliceViolation {
	int x, y;
	int a = y + 1;
	int z;
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
		String alpha = "a", zebra = "z";

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

// === case: field_order_tie_source_order ===
class InputFieldSortingFieldOrderTieSourceOrderSliceViolation {
	int alpha = 1, aLPHA = 2, zeta = 3;
}
// === end ===

// === case: field_order_tie_source_order_reversed ===
class InputFieldSortingFieldOrderTieSourceOrderReversedSliceViolation {
	int aLPHA = 2, alpha = 1, zeta = 3;
}
// === end ===

// === case: field_read_field_end_walks_through_char_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughCharSemicolonSliceViolation {
	final char alpha = 'a', zebra = ';';
}
// === end ===

// === case: field_read_field_end_walks_through_string_semicolon ===
class InputFieldSortingFieldReadFieldEndWalksThroughStringSemicolonSliceViolation {
	final String alpha = "a", zebra = "contains ; semicolon";
}
// === end ===

// === case: field_shared_line_anon_close ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineAnonCloseSliceViolation {
	final Thread worker = new Thread() {
	}; final String label = "w";
}
// === end ===

// === case: field_shared_line_class_close_brace ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineClassCloseBraceSliceViolation {
	private int beta;
	private int alpha; }
// === end ===

// === case: field_shared_line_comment_above ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentAboveSliceViolation {
	// counters
	private String name; private int count;
}
// === end ===

// === case: field_shared_line_comment_blank_line_inside_closes_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentBlankLineInsideClosesBeforeFieldSliceViolation {
	/* note

	 */ int zebra;
	int alpha;
}
// === end ===

// === case: field_shared_line_comment_closes_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentClosesBeforeFieldSliceViolation {
	int zebra; /* note
	   more */ int alpha;
}
// === end ===

// === case: field_shared_line_comment_continues_below ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentContinuesBelowSliceViolation {
	private String zebra;
	private String alpha;
	private String beta; /* retries,
			capped at 5 */
}
// === end ===

// === case: field_shared_line_comment_opener_on_other_group_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentOpenerOnOtherGroupFieldSliceViolation {
	static int q; /* c
	 */ int zebra;
	int alpha;
}
// === end ===

// === case: field_shared_line_comment_opens_before_other_group_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentOpensBeforeOtherGroupFieldSliceViolation {
	int zeta;
	int alpha;
	int beta; /* c
	 */ static int q;
}
// === end ===

// === case: field_shared_line_different_type ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineDifferentTypeSliceViolation {
	private String name; private int count;
}
// === end ===

// === case: field_shared_line_elsewhere_in_group ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineElsewhereInGroupSliceViolation {
	private String name; private String title;
	private int count;
}
// === end ===

// === case: field_shared_line_enum_terminator ===
// skip-reason: cannot reorder fields: a field shares a line with other code
enum InputFieldSortingFieldSharedLineEnumTerminatorSliceViolation {
	ALPHA; static final int ZETA = 1;
	static final int BETA = 2;
}
// === end ===

// === case: field_shared_line_multi_var_entry_in_group ===
class InputFieldSortingFieldSharedLineMultiVarEntryInGroupSliceViolation {
	private int alpha, zeta;
	private long beta;
}
// === end ===

// === case: field_shared_line_other_group ===
class InputFieldSortingFieldSharedLineOtherGroupSliceViolation {
	static int alpha; static int beta;

	private int count;
	private long zeta;
}
// === end ===

// === case: field_shared_line_precedes_static_annotation_skip ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLinePrecedesStaticAnnotationSkipSliceViolation {
	@Deprecated
	static int zeta; static int alpha;
}
// === end ===

// === case: field_shared_line_prefix_closes_earlier_comment ===
class InputFieldSortingFieldSharedLinePrefixClosesEarlierCommentSliceViolation {
	String alpha;
	/* note
	 */ String zebra;
}
// === end ===

// === case: field_shared_line_same_type ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSameTypeSliceViolation {
	private int zeta; private int alpha;
}
// === end ===

// === case: field_shared_line_single_entry_other_group ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSingleEntryOtherGroupSliceViolation {
	static int count; private int zeta, alpha;
}
// === end ===

// === case: field_shared_line_static_and_instance ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineStaticAndInstanceSliceViolation {
	private long zeta;
	private static int shared; private int alpha;
}
// === end ===

// === case: field_shared_line_supplementary_char_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSupplementaryCharBeforeFieldSliceViolation {
	static String s = """
			🎉🎉🎉🎉🎉"""; int zebra;
	int alpha;
}
// === end ===

// === case: field_shared_line_supplementary_char_in_comment ===
class InputFieldSortingFieldSharedLineSupplementaryCharInCommentSliceViolation {
	static final int ALPHA = 1;
	static final int BETA = 2;
	static final String PARTY = "x" /* 🎉 */;
}
// === end ===

// === case: field_shared_line_supplementary_char_in_literal ===
class InputFieldSortingFieldSharedLineSupplementaryCharInLiteralSliceViolation {
	static final int ALPHA = 1;
	static final int BETA = 2;
	static final String PARTY = "🎉";
}
// === end ===

// === case: field_shared_line_wrapped_field_end ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineWrappedFieldEndSliceViolation {
	private String name = """
			x"""; private int count;
}
// === end ===

// === case: field_startidx_rewind_to_pre_annotation_line ===
@SuppressWarnings("unused")
class InputFieldSortingFieldStartIdxRewindToPreAnnotationLineSliceViolation {
	@Deprecated
	String alpha = "a", zebra = "z";
}
// === end ===

// === case: field_static_initializer_block_skipped ===
class InputFieldSortingFieldStaticInitializerBlockSkippedSliceViolation {
	static int alpha = 2, beta = 1;

	static {
		Math.random();
	}
}
// === end ===

// === case: field_string_escaped_quote_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldStringEscapedQuoteSkipsSliceViolation {
	private String zeta = "a\u0022 + \u0022b";
	private String alpha = "c";
}
// === end ===

// === case: field_string_with_escaped_backslashes ===
class InputFieldSortingFieldStringWithEscapedBackslashesSliceViolation {
	final String alpha = "one", zebra = "ends with \\\\";
}
// === end ===

// === case: field_string_with_final_keyword ===
class InputFieldSortingFieldStringWithFinalKeywordSliceViolation {
	String a = "hello", b = "final";
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

// === case: init_backward_qualified_read_reorders ===
class InputFieldSortingInitBackwardQualifiedReadReordersSliceViolation {
	int alpha;
	private long count = 3;
	long total = this.count * 2L;
}
// === end ===

// === case: init_bare_forward_read_of_inert_reorders ===
class InputFieldSortingInitBareForwardReadOfInertReordersSliceViolation {
	int count;
	int total = count + Integer.parseInt("1");
}
// === end ===

// === case: init_bare_forward_read_pinned_skips ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingInitBareForwardReadPinnedSkipsSliceViolation {
	int total = count + Integer.parseInt("1");
	int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_field_initializer_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousFieldInitializerSkipsSliceViolation {
	private Object total = new Object() {
		final int seen = InputFieldSortingInitForwardReadAnonymousFieldInitializerSkipsSliceViolation.this.count;
	};
	private int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_init_block_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousInitBlockSkipsSliceViolation {
	private Object total = new Object() {
		{
			System.out.println(InputFieldSortingInitForwardReadAnonymousInitBlockSkipsSliceViolation.this.count);
		}
	};
	private int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_nested_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousNestedSkipsSliceViolation {
	private Object total = new Object() {
		final Object inner = new Object() {
			final int seen = InputFieldSortingInitForwardReadAnonymousNestedSkipsSliceViolation.this.count;
		};
	};
	private int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_static_init_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousStaticInitSkipsSliceViolation {
	private static Object total = new Object() {
		static {
			System.out.println(InputFieldSortingInitForwardReadAnonymousStaticInitSkipsSliceViolation.label);
		}
	};
	private static String label = String.valueOf("w");
}
// === end ===

// === case: init_forward_read_class_literal_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadClassLiteralConcatTargetSkipsSliceViolation {
	private final int total = this.label.length();
	private final String label = "a" + int.class;
}
// === end ===

// === case: init_forward_read_computed_final_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadComputedFinalTargetSkipsSliceViolation {
	private final int total = this.count * 2;
	private final int count = Integer.parseInt("3");
}
// === end ===

// === case: init_forward_read_constant_target_reorders ===
class InputFieldSortingInitForwardReadConstantTargetReordersSliceViolation {
	private final int count = 3;
	private final int total = this.count * 2;
}
// === end ===

// === case: init_forward_read_created_array_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadCreatedArrayConcatTargetSkipsSliceViolation {
	private final int total = this.label.length();
	private final String label = "a" + new int[0];
}
// === end ===

// === case: init_forward_read_division_by_zero_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadDivisionByZeroTargetSkipsSliceViolation {
	private final long total = this.ratio * 2L;
	private final int ratio = 1 / 0;
}
// === end ===

// === case: init_forward_read_final_array_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadFinalArrayTargetSkipsSliceViolation {
	private final int total = this.counts.length;
	private final int[] counts = {1, 2};
}
// === end ===

// === case: init_forward_read_in_lambda_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadInLambdaSkipsSliceViolation {
	private Runnable total = () -> System.out.println(this.count);
	private int count = 3;
}
// === end ===

// === case: init_forward_read_null_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadNullConcatTargetSkipsSliceViolation {
	private final int total = this.label.length();
	private final String label = "a" + null;
}
// === end ===

// === case: init_forward_read_of_later_static_reorders ===
class InputFieldSortingInitForwardReadOfLaterStaticReordersSliceViolation {
	static class Q {
		private int alpha, total = Q.count * 2;
		private static int count = 3;
	}
}
// === end ===

// === case: init_forward_read_qualified_string_constant_reorders ===
class InputFieldSortingInitForwardReadQualifiedStringConstantReordersSliceViolation {
	private final java.lang.String label = "w";
	private final int total = this.label.length();
}
// === end ===

// === case: init_forward_read_qualified_type_name_skips ===
// package: com.etk2000.checkstyle.inputs.fieldsorting
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadQualifiedTypeNameSkipsSliceViolation {
	private static int total = com.etk2000.checkstyle.inputs.fieldsorting.InputFieldSortingInitForwardReadQualifiedTypeNameSkipsSliceViolation.count * 2;
	private static int count = 3;
}
// === end ===

// === case: init_forward_read_same_declaration_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSameDeclarationSkipsSliceViolation {
	private int total = this.count * 2, count = 3;
}
// === end ===

// === case: init_forward_read_secondary_name_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSecondaryNameSkipsSliceViolation {
	private int alpha, total = this.count * 2;
	private int count = 3;
	private int beta;
}
// === end ===

// === case: init_forward_read_static_qualified_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadStaticQualifiedSkipsSliceViolation {
	private static int total = InputFieldSortingInitForwardReadStaticQualifiedSkipsSliceViolation.count * 2;
	private static int count = 3;
}
// === end ===

// === case: init_forward_read_switch_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSwitchTargetSkipsSliceViolation {
	private final int total = this.count * 2;
	private final int count = switch (1) {
		case 1 -> 3;
		default -> 4;
	};
}
// === end ===

// === case: init_forward_read_this_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadThisConcatTargetSkipsSliceViolation {
	private final int total = this.label.length();
	private final String label = "a" + this;
}
// === end ===

// === case: init_forward_read_this_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadThisSkipsSliceViolation {
	private int total = this.count * 2;
	private int count = 3;
}
// === end ===

// === case: init_forward_read_uninitialized_target_reorders ===
class InputFieldSortingInitForwardReadUninitializedTargetReordersSliceViolation {
	private int count;
	private int total = this.count * 2;
}
// === end ===

// === case: init_forward_write_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardWriteSkipsSliceViolation {
	int alpha = (beta = 5) + 1;
	int beta = 2;
}
// === end ===

// === case: init_local_class_in_lambda_reads_later_field_skips ===
// imports: java.util.function.IntSupplier
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingInitLocalClassInLambdaReadsLaterFieldSkipsSliceViolation {
	static int alpha = compute(() -> {
		class Reader {
			int read() {
				return zeta;
			}
		}

		return new Reader().read();
	});
	static int zeta = 5;

	static int compute(IntSupplier supplier) {
		return supplier.getAsInt();
	}
}
// === end ===

// === case: init_pattern_variable_of_earlier_statement_reads_later_field_reorders ===
// imports: java.util.function.Function
class InputFieldSortingInitPatternVariableOfEarlierStatementReadsLaterFieldReordersSliceViolation {
	static InputFieldSortingInitPatternVariableOfEarlierStatementReadsLaterFieldReordersSliceViolation handler;
	static Function<Object, String> describe = o -> {
		if (o instanceof InputFieldSortingInitPatternVariableOfEarlierStatementReadsLaterFieldReordersSliceViolation handler)
			return handler.toString();
		return handler.toString();
	};
}
// === end ===

// === case: init_self_qualified_read_reorders ===
class InputFieldSortingInitSelfQualifiedReadReordersSliceViolation {
	private int count = this.count + 1;
	private String label;
}
// === end ===

// === case: init_written_lambda_local_named_like_later_field_reorders ===
// imports: java.util.function.Supplier
class InputFieldSortingInitWrittenLambdaLocalNamedLikeLaterFieldReordersSliceViolation {
	int alpha;
	Supplier<Object> supplier = () -> {
		int rate;
		rate = 2;
		return rate;
	};
	int rate = Integer.parseInt("1");
}
// === end ===

// === case: inner_enum ===
class InputFieldSortingInnerEnumSliceViolation {
	enum Inner {
		BETA,
		ALPHA
	}
}
// === end ===

// === case: interface_field_name_order ===
interface InputFieldSortingInterfaceFieldNameOrderSliceViolation {
	int FIRST = 1;
	int SECOND = 2;
}
// === end ===

// === case: lambda_parameter_named_like_later_field_not_a_dependency ===
// imports: java.util.function.DoubleUnaryOperator
class InputFieldSortingLambdaParameterNamedLikeLaterFieldNotADependencySliceViolation {
	private final double rate = 1.5;
	private final DoubleUnaryOperator discount = rate -> rate * 0.9;
}
// === end ===

// === case: leading_block_comment_no_star_continuation ===
class InputFieldSortingLeadingBlockCommentNoStarSliceViolation {
	int alpha;
	/* block
	continuing line without a star
	*/
	String zebra;
}
// === end ===

// === case: leading_block_comment_opener_on_code_line ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingLeadingBlockCommentOpenerOnCodeLineSliceViolation {
	int zebra; /* start
	// middle
	*/
	int alpha;
}
// === end ===

// === case: leading_comment_separated_by_blank ===
// skip-reason: cannot reorder fields: a comment between them belongs to no field
class InputFieldSortingLeadingCommentSeparatedByBlankSliceViolation {
	int zebra;
	// a note about nothing in particular

	int alpha;
}
// === end ===

// === case: lexer_string_with_block_comment_marker ===
class InputFieldSortingLexerStringWithBlockCommentMarkerSliceViolation {
	final String alpha = "one", zebra = "a /* fake block */ b";
}
// === end ===

// === case: line_count_directive_above_first_field_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveAboveFirstFieldSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	private int zeta = 42;
	private int alpha;

	int days() {
		return 365;
	}
}
// === end ===

// === case: line_count_directive_multiline_block_on_name_line_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveMultilineBlockOnNameLineSkipsSliceViolation {
	/* CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	 */ private int zeta = 7;
	private int alpha;
}
// === end ===

// === case: line_count_directive_one_line_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveOneLineSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zeta = 42;
	private int alpha;
}
// === end ===

// === case: line_count_directive_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveTrailingLineAboveSkipsSliceViolation {
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	int zeta = 42;
	int alpha;
}
// === end ===

// === case: line_count_directive_window_ends_above_span_reorders ===
class InputFieldSortingLineCountDirectiveWindowEndsAboveSpanReordersSliceViolation {
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES

	int alpha, zeta = 42;
}
// === end ===

// === case: line_count_directive_window_from_below_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveWindowFromBelowSkipsSliceViolation {
	int zeta = 42;
	int alpha;
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
}
// === end ===

// === case: line_count_directive_zero_count_on_span_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveZeroCountOnSpanSkipsSliceViolation {
	int zeta; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 0 LINES
	int alpha;
}
// === end ===

// === case: name_alpha_after_zebra ===
class InputFieldSortingNameAlphaAfterZebraSliceViolation {
	final int alpha = 2, zebra = 1;
}
// === end ===

// === case: name_order_qualified_generic_type ===
// imports: java.util.Map
class InputFieldSortingNameOrderQualifiedGenericTypeSliceViolation {
	Map.Entry<String, Integer> alpha, zebra;
}
// === end ===

// === case: name_within_decl_annotated_line_count_window_reorders ===
class InputFieldSortingNameWithinDeclAnnotatedLineCountWindowReordersSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	@Deprecated
	private int alpha = 7, zeta = 42;
}
// === end ===

// === case: name_within_decl_comment_escaped_line_terminator_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingNameWithinDeclCommentEscapedLineTerminatorSkipsSliceViolation {
	// \u000a
	String zebra, alpha;
}
// === end ===

// === case: name_within_decl_continuation_comment_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclContinuationCommentSkipsSliceViolation {
	String zebra, // note
			alpha;
}
// === end ===

// === case: name_within_decl_directive_on_annotation_line_reorders ===
class InputFieldSortingNameWithinDeclDirectiveOnAnnotationLineReordersSliceViolation {
	@Deprecated //$NON-NLS-1$
	String alpha, zebra;
}
// === end ===

// === case: name_within_decl_directive_on_line_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration's line holds a tool directive, which may count names by position
class InputFieldSortingNameWithinDeclDirectiveOnLineSkipsSliceViolation {
	private String title = "Title", key = "k"; //$NON-NLS-2$
}
// === end ===

// === case: name_within_decl_initialized_reorders ===
class InputFieldSortingNameWithinDeclInitializedReordersSliceViolation {
	int alpha = 2, beta = 1;
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated private int x, y;
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated private int y[], x;
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
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
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclInteriorCommentSkipsSliceViolation {
	String zebra, /* keep me */ alpha;
}
// === end ===

// === case: name_within_decl_interior_comment_with_semicolon_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclInteriorCommentWithSemicolonSkipsSliceViolation {
	String zebra, alpha /* x; y */;
}
// === end ===

// === case: name_within_decl_line_count_window_from_below_wrapped_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowFromBelowWrappedSkipsSliceViolation {
	private int zeta = 42,
			alpha = 7;
	// CHECKSTYLE IGNORE MagicNumber FOR -2 LINES
}
// === end ===

// === case: name_within_decl_line_count_window_one_line_reorders ===
class InputFieldSortingNameWithinDeclLineCountWindowOneLineReordersSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int alpha = 7, zeta = 42;
}
// === end ===

// === case: name_within_decl_line_count_window_rewrap_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowRewrapSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zetaWithALongNameToForceAWrap = 42, alphaWithALongNameToForceAWrap = 7, betaWithALongNameToForceAWrap = 9;
}
// === end ===

// === case: name_within_decl_line_count_window_wrapped_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowWrappedSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zeta = 42,
			alpha = 7;
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
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclSortedNamesCStylePrevDeclaratorSliceViolation {
	int alpha[], beta;
}
// === end ===

// === case: name_within_decl_sorted_names_chunk_violation ===
class InputFieldSortingNameWithinDeclSortedNamesChunkViolationSliceViolation {
	final int b = 1, a;
}
// === end ===

// === case: name_within_decl_supplementary_char_value_reorders ===
class InputFieldSortingNameWithinDeclSupplementaryCharValueReordersSliceViolation {
	int alpha, zeta = "𝑥".length();
}
// === end ===

// === case: name_within_decl_switch_directive_on_annotation_line_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclSwitchDirectiveOnAnnotationLineSkipsSliceViolation {
	@Deprecated // @formatter:off
	private int zeta = 42,
			alpha = 7;
	// @formatter:on
}
// === end ===

// === case: name_within_decl_unsorted_reported_when_placed ===
class InputFieldSortingNameWithinDeclUnsortedReportedWhenPlacedSliceViolation {
	long alpha, beta, gamma, zeta;
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
	B((new int[] {1, 2})),
	A(new int[] {3})
}
// === end ===

// === case: nested_type_constant_initializer_sorts ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputFieldSortingNestedTypeConstantInitializerSortsSliceViolation {
	static final class Registry {
		static final int RATE = 10;
		static final List<String> NAMES = new ArrayList<>();
	}

	static int sampleRate = 10;

	static {
		Registry.NAMES.add("default");
	}
}
// === end ===

// === case: own_line_directive_led_base_reorders_without_merge ===
class InputFieldSortingOwnLineDirectiveLedBaseReordersWithoutMergeSliceViolation {
	// language=SQL
	int alpha;
	int beta, zeta;
}
// === end ===

// === case: own_line_directive_near_miss_capitalised_merges ===
class InputFieldSortingOwnLineDirectiveNearMissCapitalisedMergesSliceViolation {
	// Language=SQL
	int alpha, beta, zeta;
}
// === end ===

// === case: parenthesized_literal_initializer_sorts ===
class InputFieldSortingParenthesizedLiteralInitializerSortsSliceViolation {
	private static int clockWidth = ((120));
	private static Object widgets = "none";
}
// === end ===

// === case: pin_chunk_order_inert_reorders ===
class InputFieldSortingPinChunkOrderInertReordersSliceViolation {
	final String label = "w";

	int zeta = Integer.parseInt("1");
}
// === end ===

// === case: pin_chunk_order_skips ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingPinChunkOrderSkipsSliceViolation {
	int zeta = Integer.parseInt("1");
	final String label = String.valueOf(2);
}
// === end ===

// === case: pin_free_field_moves_past_pinned_pair_reorders ===
class InputFieldSortingPinFreeFieldMovesPastPinnedPairReordersSliceViolation {
	int alpha, zeta = Integer.parseInt("1"), beta = 2;
}
// === end ===

// === case: pin_inert_runs_reorders ===
class InputFieldSortingPinInertRunsReordersSliceViolation {
	int alpha = Integer.parseInt("1"), zeta;
}
// === end ===

// === case: pin_literal_runs_kept_reorders ===
class InputFieldSortingPinLiteralRunsKeptReordersSliceViolation {
	int alpha, zeta = 2, beta = Integer.parseInt("1");
}
// === end ===

// === case: pin_runs_inert_reorders ===
class InputFieldSortingPinRunsInertReordersSliceViolation {
	int alpha, zeta = Integer.parseInt("1");
}
// === end ===

// === case: pin_runs_runs_reorders ===
class InputFieldSortingPinRunsRunsReordersSliceViolation {
	int alpha, zeta = Integer.parseInt("1"), beta = Integer.parseInt("2");
}
// === end ===

// === case: pin_within_declaration_among_fields_reorders ===
class InputFieldSortingPinWithinDeclarationAmongFieldsReordersSliceViolation {
	int alpha, zeta = Integer.parseInt("1"), beta = 2;
}
// === end ===

// === case: pin_within_single_declaration_reorders ===
class InputFieldSortingPinWithinSingleDeclarationReordersSliceViolation {
	int alpha, zeta = Integer.parseInt("1"), beta = 2;
}
// === end ===

// === case: range_directive_above_enum_warns ===
// @formatter:off
enum InputFieldSortingRangeDirectiveAboveEnumWarnsSliceViolation {
	ZETA,
	ALPHA
}
// === end ===

// === case: range_directive_before_span_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingRangeDirectiveBeforeSpanSkipsSliceViolation {
	// @formatter:off

	int zeta;
	int alpha;
}
// === end ===

// === case: range_directive_block_comment_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveBlockCommentSkipsSliceViolation {
	/* @formatter:off */
	int zeta;
	int alpha;
}
// === end ===

// === case: range_directive_checkstyle_colon_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleColonOnAloneSkipsSliceViolation {
	int zeta;
	// CHECKSTYLE:ON
	int alpha;
}
// === end ===

// === case: range_directive_checkstyle_colon_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleColonSkipsSliceViolation {
	// CHECKSTYLE:OFF
	int zeta;
	// CHECKSTYLE:ON
	int alpha;
}
// === end ===

// === case: range_directive_checkstyle_dot_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleDotOnAloneSkipsSliceViolation {
	int zeta;
	// CHECKSTYLE.ON: MagicNumber
	int alpha;
}
// === end ===

// === case: range_directive_checkstyle_dot_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleDotSkipsSliceViolation {
	// CHECKSTYLE.OFF: MagicNumber
	int zeta;
	// CHECKSTYLE.ON: MagicNumber
	int alpha;
}
// === end ===

// === case: range_directive_enum_header_line_warns ===
enum InputFieldSortingRangeDirectiveEnumHeaderLineWarnsSliceViolation { // @formatter:off
	ZETA,
	ALPHA
}
// === end ===

// === case: range_directive_enum_warns ===
enum InputFieldSortingRangeDirectiveEnumWarnsSliceViolation {
	// @formatter:off
	ZETA,
	// @formatter:on
	ALPHA
}
// === end ===

// === case: range_directive_formatter_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveFormatterOnAloneSkipsSliceViolation {
	int zeta;
	// @formatter:on
	int alpha;
}
// === end ===

// === case: range_directive_formatter_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveFormatterSkipsSliceViolation {
	// @formatter:off
	int zeta;
	// @formatter:on
	int alpha;
}
// === end ===

// === case: range_directive_near_miss_lowercase_reorders ===
class InputFieldSortingRangeDirectiveNearMissLowercaseReordersSliceViolation {
	// checkstyle:on
	int alpha;
	// checkstyle:off
	int zeta;
}
// === end ===

// === case: range_directive_nosonar_rides_reorders ===
class InputFieldSortingRangeDirectiveNosonarRidesReordersSliceViolation {
	int alpha;
	// NOSONAR
	int zeta;
}
// === end ===

// === case: range_directive_off_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveOffAloneSkipsSliceViolation {
	// @formatter:off
	int zeta;
	int alpha;
}
// === end ===

// === case: range_directive_other_group_interleaved_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveOtherGroupInterleavedSkipsSliceViolation {
	int zeta;
	static int beta; // @formatter:off
	int alpha;
}
// === end ===

// === case: range_directive_outside_span_reorders ===
class InputFieldSortingRangeDirectiveOutsideSpanReordersSliceViolation {
	int alpha, zeta;

	// @formatter:off
	void m() {
	}
	// @formatter:on
}
// === end ===

// === case: range_directive_single_declaration_reorders ===
class InputFieldSortingRangeDirectiveSingleDeclarationReordersSliceViolation {
	// @formatter:off
	int alpha, zeta;
}
// === end ===

// === case: range_directive_spotless_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveSpotlessOnAloneSkipsSliceViolation {
	int zeta;
	// spotless:on
	int alpha;
}
// === end ===

// === case: range_directive_spotless_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveSpotlessSkipsSliceViolation {
	// spotless:off
	int zeta;
	// spotless:on
	int alpha;
}
// === end ===

// === case: range_directive_trailing_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveTrailingSkipsSliceViolation {
	int zeta; // @formatter:off
	int alpha; // @formatter:on
}
// === end ===

// === case: record_static_field_name_order ===
record InputFieldSortingRecordStaticFieldNameOrderSliceViolation() {
	static int a = 1, b = 2;
}
// === end ===

// === case: region_marker_blank_line_layout_skips ===
// skip-reason: cannot reorder fields: a comment between them belongs to no field
class InputFieldSortingRegionMarkerBlankLineLayoutSkipsSliceViolation {
	//region Tuning
	private int zeta;
	//endregion

	private int alpha;
}
// === end ===

// === case: region_marker_block_comment_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerBlockCommentSkipsSliceViolation {
	/* region Tuning */
	private int zeta;
	/* endregion */
	private int alpha;
}
// === end ===

// === case: region_marker_compact_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerCompactSkipsSliceViolation {
	//region Tuning
	private int zeta;
	//endregion
	private int alpha;
}
// === end ===

// === case: region_marker_editor_fold_close_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEditorFoldCloseAloneSkipsSliceViolation {
	private int zeta;
	// </editor-fold>
	private int alpha;
}
// === end ===

// === case: region_marker_editor_fold_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEditorFoldSkipsSliceViolation {
	// <editor-fold desc="Tuning">
	private int zeta;
	// </editor-fold>
	private int alpha;
}
// === end ===

// === case: region_marker_end_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEndAloneSkipsSliceViolation {
	private int zeta;
	// endregion
	private int alpha;
}
// === end ===

// === case: region_marker_hash_end_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerHashEndAloneSkipsSliceViolation {
	private int zeta;
	// #endregion
	private int alpha;
}
// === end ===

// === case: region_marker_hash_near_miss_reorders ===
class InputFieldSortingRegionMarkerHashNearMissReordersSliceViolation {
	private int alpha;
	// #regional default
	private int zeta;
}
// === end ===

// === case: region_marker_hash_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerHashSkipsSliceViolation {
	//#region Tuning
	private int zeta;
	//#endregion
	private int alpha;
}
// === end ===

// === case: region_marker_near_miss_prose_reorders ===
class InputFieldSortingRegionMarkerNearMissProseReordersSliceViolation {
	// the region it ends
	private int alpha;
	// regional default
	private int zeta;
}
// === end ===

// === case: region_marker_spaced_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerSpacedSkipsSliceViolation {
	// region
	private int zeta;
	// endregion
	private int alpha;
}
// === end ===

// === case: reorder_constant_named_like_token ===
enum InputFieldSortingReorderConstantNamedLikeTokenSliceViolation {
	NUMBER,
	IDENT
}
// === end ===

// === case: reorder_two_constants ===
enum InputFieldSortingReorderTwoConstantsSliceViolation {
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_annotation_block_comment_parens ===
enum InputFieldSortingReorderWithAnnotationBlockCommentParensSliceViolation {
	@SuppressWarnings(/* ( */ "x")
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_annotation_char_literal_parens ===
enum InputFieldSortingReorderWithAnnotationCharLiteralParensSliceViolation {
	@TAnnParam('(')
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_annotation_comment_parens ===
enum InputFieldSortingReorderWithAnnotationCommentParensSliceViolation {
	@Deprecated // has ( here
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_annotation_multi_line ===
enum InputFieldSortingReorderWithAnnotationMultiLineSliceViolation {
	@SuppressWarnings(
			"unchecked"
	)
	BETA,
	ALPHA
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
	ALPHA
}
// === end ===

// === case: reorder_with_annotation_string_parens ===
enum InputFieldSortingReorderWithAnnotationStringParensSliceViolation {
	@SuppressWarnings("(((")
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_annotations ===
enum InputFieldSortingReorderWithAnnotationsSliceViolation {
	@Deprecated
	BETA,
	ALPHA
}
// === end ===

// === case: reorder_with_arguments ===
enum InputFieldSortingReorderWithArgumentsSliceViolation {
	CHERRY("red"),
	APPLE("green")
}
// === end ===

// === case: reorder_with_block_comment ===
enum InputFieldSortingReorderWithBlockCommentSliceViolation {
	/*
	 * ZEBRA docs
	 */
	ZEBRA,
	ALPHA
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
	ADD {
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
	ALPHA
}
// === end ===

// === case: reorder_with_javadoc ===
enum InputFieldSortingReorderWithJavadocSliceViolation {
	/** ZEBRA constant. */
	ZEBRA,
	/** ALPHA constant. */
	ALPHA
}
// === end ===

// === case: reorder_with_semicolon_and_trailing_comments ===
enum InputFieldSortingReorderWithSemicolonAndTrailingCommentsSliceViolation {
	BETA, // b
	ALPHA; // a
	int x;
}
// === end ===

// === case: reorder_with_semicolon_on_last ===
enum InputFieldSortingReorderWithSemicolonOnLastSliceViolation {
	BETA,
	ALPHA;
	int x;
}
// === end ===

// === case: reorder_with_trailing_comment_escaped_quote ===
enum InputFieldSortingReorderWithTrailingCommentEscapedQuoteSliceViolation {
	B("test\\"), // note
	A("x")
}
// === end ===

// === case: reorder_with_trailing_comments ===
enum InputFieldSortingReorderWithTrailingCommentsSliceViolation {
	CHERRY, // fruit
	APPLE, // fruit
	BANANA // fruit
}
// === end ===

// === case: reorder_with_url_in_string_arg ===
enum InputFieldSortingReorderWithUrlInStringArgSliceViolation {
	B("http://example.com"),
	A("y")
}
// === end ===

// === case: same_line_and_reorder ===
enum InputFieldSortingSameLineAndReorderSliceViolation {
	ZEBRA,
	ALPHA
}
// === end ===

// === case: same_line_tab_separated ===
enum InputFieldSortingSameLineTabSeparatedSliceViolation {
	BETA,
	ALPHA
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
	B(1, 2),
	A(3)
}
// === end ===

// === case: same_line_two_with_comma_in_char ===
enum InputFieldSortingSameLineTwoWithCommaInCharSliceViolation {
	B(','),
	A('.')
}
// === end ===

// === case: same_line_two_with_comma_in_string ===
enum InputFieldSortingSameLineTwoWithCommaInStringSliceViolation {
	B("x, y"),
	A("z")
}
// === end ===

// === case: same_line_with_semicolon ===
enum InputFieldSortingSameLineWithSemicolonSliceViolation {
	ALPHA,
	BETA;
	int x;
}
// === end ===

// === case: statement_directive_led_base_reorders_without_merge ===
class InputFieldSortingStatementDirectiveLedBaseReordersWithoutMergeSliceViolation {
	//noinspection unused
	int alpha;
	int beta, zeta;
}
// === end ===

// === case: statement_directive_led_later_reorders_without_merge ===
class InputFieldSortingStatementDirectiveLedLaterReordersWithoutMergeSliceViolation {
	int alpha;
	//noinspection unused
	int zeta;
}
// === end ===

// === case: statement_directive_near_miss_capitalised_merges ===
class InputFieldSortingStatementDirectiveNearMissCapitalisedMergesSliceViolation {
	// NoInspection unused
	int alpha, beta, zeta;
}
// === end ===

// === case: statement_directive_on_annotation_line_reorders_without_merge ===
class InputFieldSortingStatementDirectiveOnAnnotationLineReordersWithoutMergeSliceViolation {
	@Deprecated //noinspection unused
	int alpha;
	@Deprecated
	int beta, zeta;
}
// === end ===

// === case: string_constant_initializer_sorts ===
final class InputFieldSortingStringConstantInitializerSortsSliceViolation {
	private final String tag = "session:" + "x";
	private final String user = System.getProperty("user.name");

	@Override
	public String toString() {
		return user;
	}
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

// === case: typeargannotation_negated_value_orders_by_key ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationNegatedValueOrdersByKeySliceViolation {
	List<@TAnnParam(-1) String> alpha;
	List<@TAnnParam(1) String> beta;
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
	final List<@TAnnA String> alpha = null, zebra = null;
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