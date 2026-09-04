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
	String plain; // violation: Field 'plain' (unannotated) must appear before 'annotated' (annotated @Deprecated) (annotation order, same type).
}
// === end ===

// === case: annotation_array_value_close_brace_own_line ===
class InputFieldSortingAnnotationArrayValueCloseBraceOwnLineSliceViolation {
	@SuppressWarnings({
			"unused",
			"rawtypes"
	})
	int zebra;
	int alpha; // violation: Field 'alpha' (unannotated) must appear before 'zebra' (annotated @SuppressWarnings) (annotation order, same type).
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
	@AnnV // violation: Field 'abField' (annotated @AnnV) must appear before 'bField' (annotated @BnnV) (annotation order, same type).
	@BnnV
	String abField;
}
// === end ===

// === case: annotation_multi_line_close_trailing_constants ===
enum InputFieldSortingAnnotationMultiLineCloseTrailingConstantsSliceViolation {
	@SuppressWarnings(
		"unchecked"
	) BETA, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: annotation_negated_value_orders_by_key ===
class InputFieldSortingAnnotationNegatedValueOrdersByKeySliceViolation {
	@Min(1)
	private int beta;
	@Min(-1) // violation: Field 'alpha' (annotated @Min) must appear before 'beta' (annotated @Min) (annotation order, same type).
	private int alpha;
}
// === end ===

// === case: annotation_qualified ===
class InputFieldSortingAnnotationQualifiedSliceViolation {
	@java.lang.SuppressWarnings("unused")
	int beta;
	@java.lang.Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
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
	@Deprecated BETA, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: annotation_static ===
// skip-reason: cannot reorder fields: a static field carries annotations
class InputFieldSortingAnnotationStaticSliceViolation {
	@SuppressWarnings("unused")
	static String beta;
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
	static String alpha;
}
// === end ===

// === case: annotation_wrong_annotation_order ===
class InputFieldSortingAnnotationWrongAnnotationOrderSliceViolation {
	@SuppressWarnings("unused")
	int beta;
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
	int alpha;
}
// === end ===

// === case: anonclass_annotated_instance ===
class InputFieldSortingAnonClassAnnotatedInstanceSliceViolation {
	@Deprecated // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	final Thread worker = new Thread() {
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_anon_ctor_arg_reads_later_plain ===
class InputFieldSortingAnonClassAnonCtorArgReadsLaterPlainSliceViolation {
	final Thread worker = new Thread(label) { // violation: Field 'worker' must appear after 'label' (dependency order).
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_anon_plain_anon_plain_partial ===
class InputFieldSortingAnonClassAnonPlainAnonPlainPartialSliceViolation {
	final String alpha = "a";
	final Thread first = new Thread() { // violation: Field 'first' with an anonymous class initializer must appear after 'beta'.
	};
	final String beta = "b";
	final Thread second = new Thread() {
	};
}
// === end ===

// === case: anonclass_blank_line_between ===
class InputFieldSortingAnonClassBlankLineBetweenSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};

	final String label = "w";
}
// === end ===

// === case: anonclass_body_forward_reference_between_anons ===
// multi-fix-expected
class InputFieldSortingAnonClassBodyForwardReferenceBetweenAnonsSliceViolation {
	final Runnable alpha = new Runnable() { // violation: Field 'alpha' with an anonymous class initializer must appear after 'count'.
		@Override
		public void run() {
			beta.run();
		}
	};
	final Runnable beta = new Runnable() { // violation: Field 'beta' with an anonymous class initializer must appear after 'count'.
		@Override
		public void run() {
			System.out.println("beta");
		}
	};
	final int count = 0;
}
// === end ===

// === case: anonclass_body_reads_nonfinal_field ===
class InputFieldSortingAnonClassBodyReadsNonfinalFieldSliceViolation {
	final Runnable task = new Runnable() { // violation: Field 'task' with an anonymous class initializer must appear after 'name'.
		@Override
		public void run() {
			++counter;
		}
	};
	final String name = "x";

	int counter;
}
// === end ===

// === case: anonclass_chunk_nonfinal_anon_before_final_plain ===
class InputFieldSortingAnonClassChunkNonfinalAnonBeforeFinalPlainSliceViolation {
	Thread task = new Thread() {
	};
	final String label = "w"; // violation: Field 'label' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: anonclass_chunk_nonfinal_plain_before_anon ===
class InputFieldSortingAnonClassChunkNonfinalPlainBeforeAnonSliceViolation {
	String label;
	final Thread task = new Thread() { // violation: Field 'task' (final with inline value) must appear before non-final fields.
	};
}
// === end ===

// === case: anonclass_construction_time_read_name_order ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingAnonClassConstructionTimeReadNameOrderSliceViolation {
	final Object alpha = new Object() { // violation: Field 'alpha' must appear after 'beta' (dependency order).
		final String snapshot = String.valueOf(beta);
	};
	final Object beta = new Object() {
	};
}
// === end ===

// === case: anonclass_ctor_arg_literal ===
class InputFieldSortingAnonClassCtorArgLiteralSliceViolation {
	final Thread worker = new Thread("w") { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_cycle_through_ctor_arg ===
// skip-reason: cannot reorder fields: they have a circular dependency
// multi-fix-expected
class InputFieldSortingAnonClassCycleThroughCtorArgSliceViolation {
	final Thread worker = new Thread(label) { // violation: Field 'worker' must appear after 'label' (dependency order).
	};
	final String label = worker.getName();

	long count;
	int index; // violation: Field 'index' (type 'int') must appear before 'count' (type 'long').
}
// === end ===

// === case: anonclass_dependency_and_free_plain ===
class InputFieldSortingAnonClassDependencyAndFreePlainSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order). // violation: Field 'holder' with an anonymous class initializer must appear after 'name'.
		final String copy = label;
	};
	final String label = "w";
	final String name = "n";
}
// === end ===

// === case: anonclass_double_violation_same_field ===
// multi-fix-expected
class InputFieldSortingAnonClassDoubleViolationSameFieldSliceViolation {
	final Thread beta = new Thread() { // violation: Field 'beta' with an anonymous class initializer must appear after 'label'.
	};
	final Thread alpha = new Thread() { // violation: Field 'alpha' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_enum_constant_body ===
enum InputFieldSortingAnonClassEnumConstantBodySliceViolation {
	INSTANCE {
		final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
		};
		final String label = "w";
	}
}
// === end ===

// === case: anonclass_enum_fields ===
enum InputFieldSortingAnonClassEnumFieldsSliceViolation {
	ALPHA;

	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_fields_inside_anon_body ===
class InputFieldSortingAnonClassFieldsInsideAnonBodySliceViolation {
	final Runnable outer = new Runnable() {
		final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
		};
		final String label = "w";

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: anonclass_first_of_three ===
class InputFieldSortingAnonClassFirstOfThreeSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'alpha'.
	};
	final String alpha = "a";
	final String beta = "b";
}
// === end ===

// === case: anonclass_free_plain_after_chunk_break ===
// multi-fix-expected
class InputFieldSortingAnonClassFreePlainAfterChunkBreakSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	String note;
	final String label = "w"; // violation: Field 'label' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: anonclass_free_plain_after_reader ===
class InputFieldSortingAnonClassFreePlainAfterReaderSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'name'.
	};
	final String label = worker.getName();
	final String name = "n";
}
// === end ===

// === case: anonclass_in_array_init ===
class InputFieldSortingAnonClassInArrayInitSliceViolation {
	final Thread[] workers = {new Thread() { // violation: Field 'workers' with an anonymous class initializer must appear after 'label'.
	}};
	final String label = "w";
}
// === end ===

// === case: anonclass_in_cast ===
class InputFieldSortingAnonClassInCastSliceViolation {
	final Object worker = (Runnable) new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_in_lambda_body ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassInLambdaBodySliceViolation {
	final Supplier<Thread> factory = () -> new Thread() { // violation: Field 'factory' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_in_list_of ===
// imports: java.util.List
class InputFieldSortingAnonClassInListOfSliceViolation {
	final List<Runnable> tasks = List.of(new Thread() { // violation: Field 'tasks' with an anonymous class initializer must appear after 'label'.
	});
	final String label = "w";
}
// === end ===

// === case: anonclass_in_ternary ===
class InputFieldSortingAnonClassInTernarySliceViolation {
	final Thread worker = Boolean.getBoolean("x") ? null : new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_inert_plain_reorders_without_merge ===
class InputFieldSortingAnonClassInertPlainReordersWithoutMergeSliceViolation {
	Object first = new Object() { // violation: Field 'first' with an anonymous class initializer must appear after 'plain'.
	};
	Object plain;
}
// === end ===

// === case: anonclass_interface_implicit_modifiers ===
interface InputFieldSortingAnonClassInterfaceImplicitModifiersSliceViolation {
	Thread WORKER = new Thread() { // violation: Field 'WORKER' with an anonymous class initializer must appear after 'LABEL'.
	};
	String LABEL = "w";
}
// === end ===

// === case: anonclass_interleaved_cross_group_reference ===
class InputFieldSortingAnonClassInterleavedCrossGroupReferenceSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final int copy = shared;
	};
	static int shared;
	final String label = "w";
}
// === end ===

// === case: anonclass_interleaved_spanning_chunks ===
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingAnonClassInterleavedSpanningChunksSliceViolation {
	Thread worker = new Thread() {
	};
	static int shared;
	final String label = "w"; // violation: Field 'label' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: anonclass_interleaved_static ===
class InputFieldSortingAnonClassInterleavedStaticSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	static int shared;
	final String label = "w";
}
// === end ===

// === case: anonclass_javadoc ===
class InputFieldSortingAnonClassJavadocSliceViolation {
	/** The background worker. */
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_lambda_forward_reference ===
class InputFieldSortingAnonClassLambdaForwardReferenceSliceViolation {
	final Runnable action = () -> System.out.println(name); // violation: Field 'action' must appear after 'name' (dependency order).
	final String name = "test";
}
// === end ===

// === case: anonclass_leading_line_comment ===
class InputFieldSortingAnonClassLeadingLineCommentSliceViolation {
	// runs in the background
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_local_class ===
class InputFieldSortingAnonClassLocalClassSliceViolation {
	void build() {
		class Local {
			final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
			};
			final String label = "w";
		}
	}
}
// === end ===

// === case: anonclass_local_class_in_lambda ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassLocalClassInLambdaSliceViolation {
	final Supplier<Object> factory = () -> { // violation: Field 'factory' must appear after 'label' (dependency order).
		class Local {
			String copy() {
				return label;
			}
		}

		return new Local();
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_middle_of_three ===
class InputFieldSortingAnonClassMiddleOfThreeSliceViolation {
	final String alpha = "a";
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'beta'.
	};
	final String beta = "b";
}
// === end ===

// === case: anonclass_multi_var_all_anon ===
class InputFieldSortingAnonClassMultiVarAllAnonSliceViolation {
	final Thread alpha = new Thread() { // violation: Field 'alpha' with an anonymous class initializer must appear after 'label'. // violation: Field 'beta' with an anonymous class initializer must appear after 'label'.
	}, beta = new Thread() {
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_multi_var_all_anon_secondary_read ===
class InputFieldSortingAnonClassMultiVarAllAnonSecondaryReadSliceViolation {
	final Object a = new Object() { // violation: Field 'a' with an anonymous class initializer must appear after 'name'. // violation: Field 'b' with an anonymous class initializer must appear after 'name'.
	}, b = new Object() {
	};
	final String label = b.toString();
	final String name = "n";
}
// === end ===

// === case: anonclass_multi_var_all_free ===
class InputFieldSortingAnonClassMultiVarAllFreeSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'p'.
	};
	final int p = 1, q = 2;
}
// === end ===

// === case: anonclass_multi_var_all_readers_with_free_sibling ===
class InputFieldSortingAnonClassMultiVarAllReadersWithFreeSiblingSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'name'.
	};
	final int p = anon.hashCode(), q = anon.hashCode();
	final String name = "n";
}
// === end ===

// === case: anonclass_multi_var_anon_first_with_sibling ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes anonymous class and plain initializers
class InputFieldSortingAnonClassMultiVarAnonFirstWithSiblingSliceViolation {
	final Object task = new Thread() { // violation: Field 'task' with an anonymous class initializer must appear after 'name'.
	}, label = "w";
	final String name = "n";
}
// === end ===

// === case: anonclass_multi_var_mixed_other_group ===
class InputFieldSortingAnonClassMultiVarMixedOtherGroupSliceViolation {
	static final Object LABEL = "w", TASK = new Thread() {
	};

	final String zebra = "z";
	final String alpha = "a"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: anonclass_multi_var_reader_mixed ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'p'.
	};
	final int p = 1, q = anon.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_free_last ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedFreeLastSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'b'.
	};
	final int a = anon.hashCode(), b = 1;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_free_middle ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedFreeMiddleSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'b'.
	};
	final int a = anon.hashCode(), b = 1, c = anon.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_reader_middle ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedReaderMiddleSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'a'.
	};
	final int a = 1, b = anon.hashCode(), c = 2;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_static ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedStaticSliceViolation {
	static final Object ANON = new Object() { // violation: Field 'ANON' with an anonymous class initializer must appear after 'P'.
	};
	static final int P = 1, Q = ANON.hashCode();
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_transitive_field ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedTransitiveFieldSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'p'.
	};
	final int base = anon.hashCode();
	final int p = 1, q = base + 1;
}
// === end ===

// === case: anonclass_multi_var_reader_mixed_transitive_sibling ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes names that read an anonymous class field and names that must precede one
class InputFieldSortingAnonClassMultiVarReaderMixedTransitiveSiblingSliceViolation {
	final Object anon = new Object() { // violation: Field 'anon' with an anonymous class initializer must appear after 'c'.
	};
	final int a = anon.hashCode(), b = a + 1, c = 1;
}
// === end ===

// === case: anonclass_multi_var_reader_other_chunk_no_anon ===
class InputFieldSortingAnonClassMultiVarReaderOtherChunkNoAnonSliceViolation {
	final Object anon = new Object() {
	};

	int zeta;
	int p = 1, q = anon.hashCode(); // violation: Field 'p' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: anonclass_multi_var_second_declarator_before_plain ===
// skip-reason: cannot reorder fields: a multi-variable declaration mixes anonymous class and plain initializers
class InputFieldSortingAnonClassMultiVarSecondDeclaratorBeforePlainSliceViolation {
	final Object label = "w", task = new Thread() { // violation: Field 'task' with an anonymous class initializer must appear after 'name'.
	};
	final String name = "n";
}
// === end ===

// === case: anonclass_nested_static_class ===
class InputFieldSortingAnonClassNestedStaticClassSliceViolation {
	static class Inner {
		final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
		};
		final String label = "w";
	}
}
// === end ===

// === case: anonclass_new_object_with_body ===
class InputFieldSortingAnonClassNewObjectWithBodySliceViolation {
	final Object lock = new Object() { // violation: Field 'lock' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_nonfinal_anon_before_nonfinal_plain ===
class InputFieldSortingAnonClassNonfinalAnonBeforeNonfinalPlainSliceViolation {
	Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	String label;
}
// === end ===

// === case: anonclass_order_tie_released_together ===
// multi-fix-expected
class InputFieldSortingAnonClassOrderTieReleasedTogetherSliceViolation {
	final Thread aLPHA = new Thread() { // violation: Field 'aLPHA' with an anonymous class initializer must appear after 'label'.
	};
	final Thread alpha = new Thread() { // violation: Field 'alpha' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_pair_ctor_arg_dependency_over_type ===
// imports: java.util.TimerTask
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingAnonClassPairCtorArgDependencyOverTypeSliceViolation {
	final Thread worker = new Thread(job) { // violation: Field 'worker' must appear after 'job' (dependency order).
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
	final String zebra = "new Runnable() {";
	final String alpha = "hello"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: anonclass_pinned_later_declarator_skips ===
// skip-reason: cannot reorder fields: a field would move past a name bound in a multi-variable declaration
class InputFieldSortingAnonClassPinnedLaterDeclaratorSkipsSliceViolation {
	final Object task = new Object() { // violation: Field 'task' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w", note = String.valueOf(1);
}
// === end ===

// === case: anonclass_pinned_plain_stays_after_anon_reorders ===
class InputFieldSortingAnonClassPinnedPlainStaysAfterAnonReordersSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final Object note = "n";
	final String label = "w";
}
// === end ===

// === case: anonclass_plain_reads_later_anon ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingAnonClassPlainReadsLaterAnonSliceViolation {
	final String label = String.valueOf(task); // violation: Field 'label' must appear after 'task' (dependency order).
	final Thread task = new Thread() {
	};
}
// === end ===

// === case: anonclass_qualified_type ===
class InputFieldSortingAnonClassQualifiedTypeSliceViolation {
	final Thread worker = new java.lang.Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_record_static ===
record InputFieldSortingAnonClassRecordStaticSliceViolation(int x) {
	static final Thread WORKER = new Thread() { // violation: Field 'WORKER' with an anonymous class initializer must appear after 'LABEL'.
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_reference_through_anon_method ===
class InputFieldSortingAnonClassReferenceThroughAnonMethodSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String copy = read();

		String read() {
			return label;
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_self_reference ===
class InputFieldSortingAnonClassSelfReferenceSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final Object self = holder;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_bare_this_member ===
class InputFieldSortingAnonClassShadowBareThisMemberSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner";
		final String copy = this.label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_ctor_arg_unaffected ===
class InputFieldSortingAnonClassShadowCtorArgUnaffectedSliceViolation {
	final Thread worker = new Thread(label) { // violation: Field 'worker' must appear after 'label' (dependency order).
		final String label = "inner";
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_init_block_assigns_member ===
class InputFieldSortingAnonClassShadowInitBlockAssignsMemberSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		String label;

		{
			label = "x";
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_init_block_local_not_a_dependency ===
class InputFieldSortingAnonClassShadowInitBlockLocalNotADependencySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final int seed = 1;

		{
			final var label = "local";
			System.out.println(label);
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_interface_implicit_static ===
interface InputFieldSortingAnonClassShadowInterfaceImplicitStaticSliceViolation {
	Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		static final String LABEL = "inner";
		static final String COPY = LABEL;
	};
	String LABEL = "w";
}
// === end ===

// === case: anonclass_shadow_lambda_param_not_a_dependency ===
// imports: java.util.function.Function
class InputFieldSortingAnonClassShadowLambdaParamNotADependencySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final Function<String, String> copy = label -> label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_method_name_not_a_dependency ===
class InputFieldSortingAnonClassShadowMethodNameNotADependencySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String copy = label();

		String label() {
			return "x";
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_multi_var_member_first ===
class InputFieldSortingAnonClassShadowMultiVarMemberFirstSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner", copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_multi_var_member_last ===
class InputFieldSortingAnonClassShadowMultiVarMemberLastSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String alpha = "x", label = "inner";
		final String copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_multi_var_member_middle ===
class InputFieldSortingAnonClassShadowMultiVarMemberMiddleSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String alpha = "x", label = "inner", omega = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_nested_ctor_arg ===
class InputFieldSortingAnonClassShadowNestedCtorArgSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner";
		final Thread inner = new Thread(label) {
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_nested_member_scoped_to_nested ===
class InputFieldSortingAnonClassShadowNestedMemberScopedToNestedSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String copy = label;
		final Object inner = new Object() {
			final String label = "inner";
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_nested_own_read ===
class InputFieldSortingAnonClassShadowNestedOwnReadSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final Object inner = new Object() {
			final String label = "inner";
			final String copy = label;
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_other_name_still_dependency ===
class InputFieldSortingAnonClassShadowOtherNameStillDependencySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'name' (dependency order). // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner";
		final String copy = label + name;
	};
	final String label = "w";
	final String name = "n";
}
// === end ===

// === case: anonclass_shadow_outer_member_in_nested ===
class InputFieldSortingAnonClassShadowOuterMemberInNestedSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner";
		final Object inner = new Object() {
			final String copy = label;
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_outer_multi_var_secondary ===
class InputFieldSortingAnonClassShadowOuterMultiVarSecondarySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'alpha'.
		final String label = "inner";
		final String copy = label;
	};
	final String alpha = "a", label = "w";
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String label = "inner";
		final String copy = InputFieldSortingAnonClassShadowQualifiedOuterThisSliceViolation.this.label; // violation: Field 'copy' must appear before 'label' (alphabetical order, same type).
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this_init_block ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisInitBlockSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		String label;

		{
			System.out.println(InputFieldSortingAnonClassShadowQualifiedOuterThisInitBlockSliceViolation.this.label);
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_qualified_outer_this_nested ===
class InputFieldSortingAnonClassShadowQualifiedOuterThisNestedSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String label = "inner";
		final Object inner = new Object() {
			final String copy = InputFieldSortingAnonClassShadowQualifiedOuterThisNestedSliceViolation.this.label;
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_shadow_static_member_instance_read ===
class InputFieldSortingAnonClassShadowStaticMemberInstanceReadSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		static final String LABEL = "inner";

		final String copy = LABEL;
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_shadow_static_member_static_init ===
class InputFieldSortingAnonClassShadowStaticMemberStaticInitSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		static final String LABEL = "inner";

		static {
			System.out.println(LABEL);
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_shadowed_member_name ===
class InputFieldSortingAnonClassShadowedMemberNameSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String label = "inner";
		final String copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_skip_method_body ===
class InputFieldSortingAnonClassSkipMethodBodySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		String copy() {
			return label;
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_skip_nested_anon_method_body ===
class InputFieldSortingAnonClassSkipNestedAnonMethodBodySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final Object inner = new Object() {
			String copy() {
				return label;
			}
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_skip_nested_class ===
class InputFieldSortingAnonClassSkipNestedClassSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		class Inner {
			final String copy = LABEL;
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_skip_nested_enum ===
class InputFieldSortingAnonClassSkipNestedEnumSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		enum Inner {
			A;

			static final String COPY = LABEL;
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_skip_nested_interface ===
class InputFieldSortingAnonClassSkipNestedInterfaceSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		interface Inner {
			String COPY = LABEL;
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_skip_nested_record ===
class InputFieldSortingAnonClassSkipNestedRecordSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		record Inner() {
			static final String COPY = LABEL;
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_skip_static_method_body ===
class InputFieldSortingAnonClassSkipStaticMethodBodySliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' with an anonymous class initializer must appear after 'LABEL'.
		static String copy() {
			return LABEL;
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_static_annotated ===
// skip-reason: cannot reorder fields: a static field carries annotations
class InputFieldSortingAnonClassStaticAnnotatedSliceViolation {
	@Deprecated // violation: Field 'WORKER' with an anonymous class initializer must appear after 'LABEL'.
	static final Thread WORKER = new Thread() {
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_static_anon_before_plain ===
class InputFieldSortingAnonClassStaticAnonBeforePlainSliceViolation {
	static final Thread WORKER = new Thread() { // violation: Field 'WORKER' with an anonymous class initializer must appear after 'LABEL'.
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_text_block_in_body ===
class InputFieldSortingAnonClassTextBlockInBodySliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String text = """
				};
				""";
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_trailing_comment_on_close ===
class InputFieldSortingAnonClassTrailingCommentOnCloseSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	}; // background worker
	final String label = "w";
}
// === end ===

// === case: anonclass_transitive_dependency_on_later_plain ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
// multi-fix-expected
class InputFieldSortingAnonClassTransitiveDependencyOnLaterPlainSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'source' (dependency order).
		final String copy = String.valueOf(source);
	};
	final Object source = new Object() { // violation: Field 'source' must appear after 'label' (dependency order).
		final String copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_two_anon_interleaved ===
// multi-fix-expected
class InputFieldSortingAnonClassTwoAnonInterleavedSliceViolation {
	final Thread first = new Thread() { // violation: Field 'first' with an anonymous class initializer must appear after 'alpha'.
	};
	final String alpha = "a";
	final Thread second = new Thread() { // violation: Field 'second' with an anonymous class initializer must appear after 'beta'.
	};
	final String beta = "b";
}
// === end ===

// === case: anonclass_unshadowed_member_name ===
class InputFieldSortingAnonClassUnshadowedMemberNameSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		final String own = "inner";
		final String copy = own;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_volatile_nonfinal ===
class InputFieldSortingAnonClassVolatileNonfinalSliceViolation {
	volatile Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	};
	String label;
}
// === end ===

// === case: anonclass_walk_instance_field_init ===
class InputFieldSortingAnonClassWalkInstanceFieldInitSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_walk_instance_init ===
class InputFieldSortingAnonClassWalkInstanceInitSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final int seed = 1;

		{
			System.out.println(label);
		}
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_walk_lambda_in_field_init ===
// imports: java.util.function.Supplier
class InputFieldSortingAnonClassWalkLambdaInFieldInitSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final Supplier<String> copy = () -> label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_walk_multi_declarator_member ===
class InputFieldSortingAnonClassWalkMultiDeclaratorMemberSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String alpha = "x", copy = label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_walk_nested_anon_field_init ===
class InputFieldSortingAnonClassWalkNestedAnonFieldInitSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final Object inner = new Object() {
			final String copy = label;
		};
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_walk_static_field_init ===
class InputFieldSortingAnonClassWalkStaticFieldInitSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' must appear after 'LABEL' (dependency order).
		static final String COPY = LABEL;
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_walk_static_init ===
class InputFieldSortingAnonClassWalkStaticInitSliceViolation {
	static final Object HOLDER = new Object() { // violation: Field 'HOLDER' must appear after 'LABEL' (dependency order).
		static {
			System.out.println(LABEL);
		}
	};
	static final String LABEL = "w";
}
// === end ===

// === case: anonclass_walk_uninitialized_member ===
class InputFieldSortingAnonClassWalkUninitializedMemberSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' with an anonymous class initializer must appear after 'label'.
		String label;
	};
	final String label = "w";
}
// === end ===

// === case: anonclass_wrapped_initializer ===
class InputFieldSortingAnonClassWrappedInitializerSliceViolation {
	final Thread worker = // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
			new Thread() {
			};
	final String label = "w";
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

// === case: array_dimension_at_the_cap_sorts ===
class InputFieldSortingArrayDimensionAtTheCapSortsSliceViolation {
	static Object registry = "r";
	static int[] sizes = new int[65536]; // violation: Field 'sizes' (type 'int[]') must appear before 'registry' (type 'Object').
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
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
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
	Alpha // violation (warning): Enum constant 'Alpha' should appear before 'beta' (alphabetical order).
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

// === case: constants_annotation_type_name_order ===
@interface InputFieldSortingConstantsAnnotationTypeNameOrderSliceViolation {
	int SECOND = 2;
	int FIRST = 1; // violation: Field 'FIRST' must appear before 'SECOND' (alphabetical order, same type).
}
// === end ===

// === case: constants_enum_name_order ===
enum InputFieldSortingConstantsEnumNameOrderSliceViolation {
	ONE;

	static final int MIN = 1;
	static final int MAX = 10; // violation: Field 'MAX' must appear before 'MIN' (alphabetical order, same type).
}
// === end ===

// === case: constants_record_name_order ===
record InputFieldSortingConstantsRecordNameOrderSliceViolation(int x) {
	static final int B = 2;
	static final int A = 1; // violation: Field 'A' must appear before 'B' (alphabetical order, same type).
}
// === end ===

// === case: constants_sorted_not_merged_with_non_constants ===
// multi-fix-expected
class InputFieldSortingConstantsSortedNotMergedWithNonConstantsSliceViolation {
	static int gamma;
	static final int ZETA = 2; // violation: Field 'ZETA' (final with inline value) must appear before non-final fields.
	static final int ALPHA = 1; // violation: Field 'ALPHA' must appear before 'ZETA' (alphabetical order, same type).
	static int alpha;
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
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingDependencyForwardRefSliceViolation {
	int beta = this.alpha + 1; // violation: Field 'beta' must appear after 'alpha' (dependency order).
	int alpha = 10;
}
// === end ===

// === case: dependency_later_chunk_anon_field_init ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkAnonFieldInitSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'counter' (dependency order).
		final long seed = counter;
	};

	long counter;
}
// === end ===

// === case: dependency_later_chunk_method_body_boundary ===
class InputFieldSortingDependencyLaterChunkMethodBodyBoundarySliceViolation {
	long counter;

	final Object holder = new Object() { // violation: Field 'holder' (final with inline value) must appear before non-final fields.
		long seed() {
			return counter;
		}
	};
}
// === end ===

// === case: dependency_later_chunk_plain ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkPlainSliceViolation {
	final long seed = counter + 1; // violation: Field 'seed' must appear after 'counter' (dependency order).

	long counter;
}
// === end ===

// === case: dependency_later_chunk_plain_backward ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkPlainBackwardSliceViolation {
	long counter = 5;

	final long seed = counter + 1; // violation: Field 'seed' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: dependency_later_chunk_with_chunk_violation ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyLaterChunkWithChunkViolationSliceViolation {
	long counter;

	final Object holder = new Object() { // violation: Field 'holder' (final with inline value) must appear before non-final fields.
		final long seed = counter;
	};
}
// === end ===

// === case: dependency_multi_var_releases_two_dependents ===
// multi-fix-expected
class InputFieldSortingDependencyMultiVarReleasesTwoDependentsSliceViolation {
	int gamma = alpha + beta; // violation: Field 'gamma' must appear after 'alpha' (dependency order).
	int delta = beta + alpha; // violation: Field 'delta' must appear after 'alpha' (dependency order).
	int alpha, beta;
}
// === end ===

// === case: dependency_multi_var_two_dependents_pinned_skips ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyMultiVarTwoDependentsPinnedSkipsSliceViolation {
	int gamma = alpha; // violation: Field 'gamma' must appear after 'alpha' (dependency order).
	int delta = beta; // violation: Field 'delta' must appear after 'beta' (dependency order).
	int alpha = 1, beta = 2;
}
// === end ===

// === case: dependency_non_adjacent_anon_pair ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingDependencyNonAdjacentAnonPairSliceViolation {
	final Object alpha = new Object() { // violation: Field 'alpha' must appear after 'gamma' (dependency order).
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
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String copy = label;
	};
	final String alpha = String.valueOf(holder);
	final String label = "w";
}
// === end ===

// === case: dependency_non_adjacent_plain ===
class InputFieldSortingDependencyNonAdjacentPlainSliceViolation {
	final String alpha = label + "!"; // violation: Field 'alpha' must appear after 'label' (dependency order).
	final String beta = "b";
	final String label = "w";
}
// === end ===

// === case: dependency_non_adjacent_two_between ===
class InputFieldSortingDependencyNonAdjacentTwoBetweenSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String copy = label;
	};
	final String alpha = String.valueOf(holder);
	final String beta = String.valueOf(holder);
	final String label = "w";
}
// === end ===

// === case: dependency_non_adjacent_two_targets ===
class InputFieldSortingDependencyNonAdjacentTwoTargetsSliceViolation {
	final Object holder = new Object() { // violation: Field 'holder' must appear after 'label' (dependency order).
		final String copy = name + label;
	};
	final String alpha = String.valueOf(holder);
	final String label = "w";
	final String name = "n";
}
// === end ===

// === case: dependency_null_order_cross_chunk_anon_wait ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
class InputFieldSortingDependencyNullOrderCrossChunkAnonWaitSliceViolation {
	Object a2 = new Object() {
	};
	final Object a0 = new Object() { // violation: Field 'a0' (final with inline value) must appear before non-final fields.
	};
	final Object p0 = a2;
	Object p2 = a0;
}
// === end ===

// === case: dependency_null_order_cycle_beats_later_chunk ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyNullOrderCycleBeatsLaterChunkSliceViolation {
	final long seed = counter + 1; // violation: Field 'seed' must appear after 'counter' (dependency order).

	long counter = seed;
}
// === end ===

// === case: dependency_null_order_cycle_beats_multi_var ===
// skip-reason: cannot reorder fields: they have a circular dependency
class InputFieldSortingDependencyNullOrderCycleBeatsMultiVarSliceViolation {
	int a = 1, c = d; // violation: Field 'c' must appear after 'd' (dependency order).
	int d = c;
}
// === end ===

// === case: dependency_null_order_earlier_chunk_dep_multi_var ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderEarlierChunkDepMultiVarSliceViolation {
	final int k = 0;

	int a = 1, c = b + k; // violation: Field 'c' must appear after 'b' (dependency order).
	int b = a;
}
// === end ===

// === case: dependency_null_order_later_chunk_beats_multi_var ===
// skip-reason: cannot reorder fields: a field would have to follow a dependency in a later chunk
// multi-fix-expected
class InputFieldSortingDependencyNullOrderLaterChunkBeatsMultiVarSliceViolation {
	final Object a = new Object() { // violation: Field 'c' must appear after 'b' (dependency order).
	}, c = new Object() {
		final Object x = b;
	};
	final Object b = a;
	final Object k = counter; // violation: Field 'k' must appear after 'counter' (dependency order).

	Object counter;
}
// === end ===

// === case: dependency_null_order_multi_var_anon_cycle ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderMultiVarAnonCycleSliceViolation {
	final Object a = new Object() { // violation: Field 'c' must appear after 'b' (dependency order).
	}, c = new Object() {
		final Object x = b;
	};
	final Object b = a;
}
// === end ===

// === case: dependency_null_order_multi_var_plain_cycle ===
// skip-reason: cannot reorder fields: a dependency is bound in a multi-variable declaration
class InputFieldSortingDependencyNullOrderMultiVarPlainCycleSliceViolation {
	int a = 1, c = b; // violation: Field 'c' must appear after 'b' (dependency order).
	int b = a;
}
// === end ===

// === case: dependency_two_names_of_one_multi_var ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingDependencyTwoNamesOfOneMultiVarSliceViolation {
	int sum = alpha + beta; // violation: Field 'sum' must appear after 'alpha' (dependency order).
	int alpha = 1, beta = 2;
}
// === end ===

// === case: detached_directive_above_blank_line_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingDetachedDirectiveAboveBlankLineSkipsSliceViolation {
	//noinspection unused

	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: detached_directive_leading_entry_kept_reorders ===
class InputFieldSortingDetachedDirectiveLeadingEntryKeptReordersSliceViolation {
	//noinspection unused

	int alpha;
	int zeta;
	int beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: detached_directive_trailing_entry_kept_reorders ===
class InputFieldSortingDetachedDirectiveTrailingEntryKeptReordersSliceViolation {
	int alpha; //noinspection unused
	int beta;
	int zeta;
	int gamma; // violation: Field 'gamma' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: detached_directive_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingDetachedDirectiveTrailingLineAboveSkipsSliceViolation {
	final int limit = 1; //noinspection unused
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: division_by_a_nonzero_literal_sorts ===
class InputFieldSortingDivisionByANonzeroLiteralSortsSliceViolation {
	static final Object REGISTRY = System.getProperty("registry");
	static final int HALF = 1 / 2; // violation: Field 'HALF' (type 'int') must appear before 'REGISTRY' (type 'Object').
}
// === end ===

// === case: enum_constant_bare_forward_read_after_runner_warns ===
enum InputFieldSortingEnumConstantBareForwardReadAfterRunnerWarnsSliceViolation {
	MID(null, Integer.parseInt("1")),
	ZED(ALPHA, 0), // violation (warning): Enum constant 'ZED' should appear after 'ALPHA' (dependency order).
	ALPHA(null, 0); // violation (warning): Enum constant 'ALPHA' should appear before 'ZED' (alphabetical order).

	InputFieldSortingEnumConstantBareForwardReadAfterRunnerWarnsSliceViolation(Object parent, int id) {
	}
}
// === end ===

// === case: enum_constant_bare_forward_read_by_a_runner_keeps_its_place_warns ===
enum InputFieldSortingEnumConstantBareForwardReadByARunnerKeepsItsPlaceWarnsSliceViolation {
	ZED(null, 0),
	YEW(null, 0), // violation (warning): Enum constant 'YEW' should appear before 'ZED' (alphabetical order).
	BETA(ALPHA, Ids.next()), // violation (warning): Enum constant 'BETA' should appear after 'ALPHA' (dependency order). // violation (warning): Enum constant 'BETA' should appear before 'YEW' (alphabetical order).
	ALPHA(null, 0); // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).

	InputFieldSortingEnumConstantBareForwardReadByARunnerKeepsItsPlaceWarnsSliceViolation(Object parent, int id) {
	}
}
// === end ===

// === case: enum_constant_bare_forward_read_warns ===
enum InputFieldSortingEnumConstantBareForwardReadWarnsSliceViolation {
	ZED(ALPHA), // violation (warning): Enum constant 'ZED' should appear after 'ALPHA' (dependency order).
	ALPHA(null); // violation (warning): Enum constant 'ALPHA' should appear before 'ZED' (alphabetical order).

	InputFieldSortingEnumConstantBareForwardReadWarnsSliceViolation(Object parent) {
	}
}
// === end ===

// === case: enum_constant_bodies_with_fields_warns ===
enum InputFieldSortingEnumConstantBodiesWithFieldsWarnsSliceViolation {
	ZETA {
		final int depth = 2;
	},
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
		final int width = 1;
	}
}
// === end ===

// === case: enum_constant_body_static_constant_warns ===
enum InputFieldSortingEnumConstantBodyStaticConstantWarnsSliceViolation {
	ZETA {
		static final int DEPTH = 2;
	},
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
		static final int WIDTH = 1;
	}
}
// === end ===

// === case: enum_constant_order_unobserved_by_a_maps_values_warns ===
enum InputFieldSortingEnumConstantOrderUnobservedByAMapsValuesWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object V = java.util.Map.of(1, 2).values();
}
// === end ===

// === case: enum_constant_order_unobserved_by_its_methods_warns ===
enum InputFieldSortingEnumConstantOrderUnobservedByItsMethodsWarnsSliceViolation {
	LOW,
	HIGH; // violation (warning): Enum constant 'HIGH' should appear before 'LOW' (alphabetical order).

	String label() {
		return name();
	}
}
// === end ===

// === case: enum_constant_reads_earlier_constant_qualified_warns ===
enum InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation.ROOT), // violation (warning): Enum constant 'ELECTRONICS' should appear before 'ROOT' (alphabetical order).
	BOOKS(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation.ROOT); // violation (warning): Enum constant 'BOOKS' should appear before 'ELECTRONICS' (alphabetical order).

	private final InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation parent;

	InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation(InputFieldSortingEnumConstantReadsEarlierConstantQualifiedWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_constant_reads_earlier_constant_warns ===
enum InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(ROOT), // violation (warning): Enum constant 'ELECTRONICS' should appear before 'ROOT' (alphabetical order).
	BOOKS(ROOT); // violation (warning): Enum constant 'BOOKS' should appear before 'ELECTRONICS' (alphabetical order).

	private final InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation parent;

	InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation(InputFieldSortingEnumConstantReadsEarlierConstantWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_constant_reads_nothing_warns ===
enum InputFieldSortingEnumConstantReadsNothingWarnsSliceViolation {
	ROOT(null),
	ELECTRONICS(null), // violation (warning): Enum constant 'ELECTRONICS' should appear before 'ROOT' (alphabetical order).
	BOOKS(null); // violation (warning): Enum constant 'BOOKS' should appear before 'ELECTRONICS' (alphabetical order).

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
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
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
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
		static {
			System.out.println("alpha");
		}
	}
}
// === end ===

// === case: enum_order_kept_compare_to_in_method_warns ===
enum InputFieldSortingEnumOrderKeptCompareToInMethodWarnsSliceViolation {
	LOW,
	HIGH; // violation (warning): Enum constant 'HIGH' should appear before 'LOW' (alphabetical order).

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
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZED' (alphabetical order).

	Object next() {
		return null;
	}
}
// === end ===

// === case: enum_order_kept_constant_qualified_forward_read_warns ===
enum InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation {
	ZED(InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation.ALPHA),
	ALPHA(null); // violation (warning): Enum constant 'ALPHA' should appear before 'ZED' (alphabetical order).

	InputFieldSortingEnumOrderKeptConstantQualifiedForwardReadWarnsSliceViolation(Object parent) {
	}
}
// === end ===

// === case: enum_order_kept_constant_reads_earlier_constant_warns ===
enum InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation {
	ROOT(null),
	BOOKS(ROOT); // violation (warning): Enum constant 'BOOKS' should appear before 'ROOT' (alphabetical order).

	private final InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation parent;

	InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation(InputFieldSortingEnumOrderKeptConstantReadsEarlierConstantWarnsSliceViolation parent) {
		this.parent = parent;
	}
}
// === end ===

// === case: enum_order_kept_constructor_registers_warns ===
enum InputFieldSortingEnumOrderKeptConstructorRegistersWarnsSliceViolation {
	TITLE("Title"),
	AUTHOR("Author"); // violation (warning): Enum constant 'AUTHOR' should appear before 'TITLE' (alphabetical order).

	private final int index;

	InputFieldSortingEnumOrderKeptConstructorRegistersWarnsSliceViolation(String header) {
		index = Csv.register(header);
	}
}
// === end ===

// === case: enum_order_kept_enum_map_warns ===
enum InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object LABELS = new java.util.EnumMap<InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation, String>(InputFieldSortingEnumOrderKeptEnumMapWarnsSliceViolation.class);
}
// === end ===

// === case: enum_order_kept_enum_set_complement_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetComplementWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object REST = java.util.EnumSet.complementOf(java.util.EnumSet.of(ZETA));
}
// === end ===

// === case: enum_order_kept_enum_set_copy_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetCopyWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object SOME = java.util.EnumSet.copyOf(java.util.List.of(ZETA, ALPHA));
}
// === end ===

// === case: enum_order_kept_enum_set_of_warns ===
enum InputFieldSortingEnumOrderKeptEnumSetOfWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object SOME = java.util.EnumSet.of(ZETA, ALPHA);
}
// === end ===

// === case: enum_order_kept_get_enum_constants_warns ===
enum InputFieldSortingEnumOrderKeptGetEnumConstantsWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object FIRST = InputFieldSortingEnumOrderKeptGetEnumConstantsWarnsSliceViolation.class.getEnumConstants()[0];
}
// === end ===

// === case: enum_order_kept_jdk_call_in_args_warns ===
enum InputFieldSortingEnumOrderKeptJdkCallInArgsWarnsSliceViolation {
	B(Math.max(1, 2)),
	A(3) // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: enum_order_kept_natural_order_warns ===
enum InputFieldSortingEnumOrderKeptNaturalOrderWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object ORDER = java.util.Comparator.naturalOrder();
}
// === end ===

// === case: enum_order_kept_nested_parens_call_in_args_warns ===
enum InputFieldSortingEnumOrderKeptNestedParensCallInArgsWarnsSliceViolation {
	B(foo(1, 2)),
	A(3) // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: enum_order_kept_own_qualified_values_warns ===
enum InputFieldSortingEnumOrderKeptOwnQualifiedValuesWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object LAST = InputFieldSortingEnumOrderKeptOwnQualifiedValuesWarnsSliceViolation.values()[1];
}
// === end ===

// === case: enum_order_kept_sorted_warns ===
enum InputFieldSortingEnumOrderKeptSortedWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object FIRST = java.util.stream.Stream.of(ZETA, ALPHA).sorted().findFirst();
}
// === end ===

// === case: enum_order_kept_tree_set_warns ===
enum InputFieldSortingEnumOrderKeptTreeSetWarnsSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	static final Object SET = new java.util.TreeSet<>(java.util.List.of(ZETA, ALPHA));
}
// === end ===

// === case: enum_order_kept_values_in_static_field_warns ===
enum InputFieldSortingEnumOrderKeptValuesInStaticFieldWarnsSliceViolation {
	DEBUG,
	INFO,
	WARN,
	ERROR; // violation (warning): Enum constant 'ERROR' should appear before 'WARN' (alphabetical order).

	static final InputFieldSortingEnumOrderKeptValuesInStaticFieldWarnsSliceViolation DEFAULT = values()[1];
}
// === end ===

// === case: enum_pin_constrained_warns ===
enum InputFieldSortingEnumPinConstrainedWarnsSliceViolation {
	ZETA(Integer.parseInt("1")),
	BETA(2), // violation (warning): Enum constant 'BETA' should appear before 'ZETA' (alphabetical order).
	ALPHA(3); // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).

	InputFieldSortingEnumPinConstrainedWarnsSliceViolation(int value) {
	}
}
// === end ===

// === case: enum_pin_runner_kept_last_warns ===
enum InputFieldSortingEnumPinRunnerKeptLastWarnsSliceViolation {
	BETA(2),
	ALPHA(3), // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
	ZETA(Integer.parseInt("1"));

	InputFieldSortingEnumPinRunnerKeptLastWarnsSliceViolation(int value) {
	}
}
// === end ===

// === case: enumconstant_annotated_after_previous ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousSliceViolation {
	ZETA, @Deprecated ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_annotated_after_previous_comment ===
enum InputFieldSortingEnumConstantAnnotatedAfterPreviousCommentSliceViolation {
	ZETA, /* mid */ @Deprecated ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_annotated_same_line ===
enum InputFieldSortingEnumConstantAnnotatedSameLineSliceViolation {
	@Deprecated ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_annotated_same_line_leading_comment ===
enum InputFieldSortingEnumConstantAnnotatedSameLineLeadingCommentSliceViolation {
	/* keep */ @Deprecated ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumconstant_basic ===
enum InputFieldSortingEnumConstantBasicSliceViolation {
	ZEBRA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).

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
		FIRST // violation (warning): Enum constant 'FIRST' should appear before 'SECOND' (alphabetical order).
	}
}
// === end ===

// === case: enumconstant_multiple ===
enum InputFieldSortingEnumConstantMultipleSliceViolation {
	CHARLIE,
	BRAVO, // violation (warning): Enum constant 'BRAVO' should appear before 'CHARLIE' (alphabetical order).
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BRAVO' (alphabetical order).
}
// === end ===

// === case: enumconstant_outer_enum ===
enum InputFieldSortingEnumConstantOuterEnumSliceViolation {
	ALPHA,
	BETA;

	enum InnerMisordered {
		YELLOW,
		XENON // violation (warning): Enum constant 'XENON' should appear before 'YELLOW' (alphabetical order).
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
	ADD { // violation (warning): Enum constant 'ADD' should appear before 'SUBTRACT' (alphabetical order).
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
	BANANA("yellow"), // violation (warning): Enum constant 'BANANA' should appear before 'CHERRY' (alphabetical order).
	APPLE("green"); // violation (warning): Enum constant 'APPLE' should appear before 'BANANA' (alphabetical order).

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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings(value = "unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ZEBRA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
	BETA { void foo() {} }, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_array_value ===
enum InputFieldSortingEnumSpanAnnotationArgArrayValueSliceViolation {
	ZETA,
	@SuppressWarnings({"a", "b"}) ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	ZETA,
	@Deprecated() ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	ZETA,
	@SuppressWarnings(value = "unused") ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSpanAnnotationArgStructuralCharsSliceViolation {
	ZETA,
	@SuppressWarnings("{ } , // /*") ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_blank_line_between_constants ===
enum InputFieldSortingEnumSpanBlankLineBetweenConstantsSliceViolation {
	ZETA,

	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_block_comment_before_terminator ===
enum InputFieldSortingEnumSpanBlockCommentBeforeTerminatorSliceViolation {
	BETA,
	ALPHA, // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
	ZETA /* note */;

	int legacy;
}
// === end ===

// === case: enumspan_block_comment_before_terminator_line ===
enum InputFieldSortingEnumSpanBlockCommentBeforeTerminatorLineSliceViolation {
	GREEN,
	BLUE // violation (warning): Enum constant 'BLUE' should appear before 'GREEN' (alphabetical order).
	/* Shared helper. */
	;

	void helper() {
	}
}
// === end ===

// === case: enumspan_brace_close_shared ===
enum InputFieldSortingEnumSpanBraceCloseSharedSliceViolation {
	ALPHA, BETA } // violation: Enum constant 'BETA' must be on its own line.
// === end ===

// === case: enumspan_brace_open_shared ===
enum InputFieldSortingEnumSpanBraceOpenSharedSliceViolation { BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
			"""; // violation (warning) @opener: Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_before_constant_ident ===
enum InputFieldSortingEnumSpanCommentBeforeConstantIdentSliceViolation {
	ZETA,
	/* c */ ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_before_separator ===
enum InputFieldSortingEnumSpanCommentBeforeSeparatorSliceViolation {
	ZETA /* t */,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSpanCommentBetweenAnnotationAndConstantSliceViolation {
	ZETA,
	@Deprecated // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	// note
	ALPHA
}
// === end ===

// === case: enumspan_comment_between_stacked_annotations ===
enum InputFieldSortingEnumSpanCommentBetweenStackedAnnotationsSliceViolation {
	ZETA,
	@Deprecated /* x */ @SuppressWarnings("y") // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	ALPHA
}
// === end ===

// === case: enumspan_comment_block_trailer ===
enum InputFieldSortingEnumSpanCommentBlockTrailerSliceViolation {
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	// tail
}
// === end ===

// === case: enumspan_comment_escaped_line_terminator_warns ===
enum InputFieldSortingEnumSpanCommentEscapedLineTerminatorWarnsSliceViolation {
	// \u000a
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_in_argument_list ===
enum InputFieldSortingEnumSpanCommentInArgumentListSliceViolation {
	ZETA(1),
	ALPHA(/* c */ 2); // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

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
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
		// a note
	}
}
// === end ===

// === case: enumspan_comment_open_after_comma ===
enum InputFieldSortingEnumSpanCommentOpenAfterCommaSliceViolation {
	ZETA, /* ZETA is legacy;
	see #123 */
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_open_after_terminator ===
enum InputFieldSortingEnumSpanCommentOpenAfterTerminatorSliceViolation {
	ZETA,
	ALPHA, // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_comment_straddles_comma ===
enum InputFieldSortingEnumSpanCommentStraddlesCommaSliceViolation {
	ZETA /*a*/, /*b*/ ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_escaped_triple_quote ===
enum InputFieldSortingEnumSpanEscapedTripleQuoteSliceViolation {
	ZETA,
	@SuppressWarnings("\"\"\"") ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_generic_implements_header ===
enum InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation implements Comparable<InputFieldSortingEnumSpanGenericImplementsHeaderSliceViolation> {
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_header_line_comment ===
enum InputFieldSortingEnumSpanHeaderLineCommentSliceViolation { // note
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_javadoc_after_comma_warns ===
enum InputFieldSortingEnumSpanJavadocAfterCommaWarnsSliceViolation {
	ZETA, /** doc */
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_javadoc_before_terminator_line_warns ===
enum InputFieldSortingEnumSpanJavadocBeforeTerminatorLineWarnsSliceViolation {
	GREEN,
	BLUE // violation (warning): Enum constant 'BLUE' should appear before 'GREEN' (alphabetical order).
	/** Shared helper. */
	;

	void helper() {
	}
}
// === end ===

// === case: enumspan_javadoc_before_terminator_warns ===
enum InputFieldSortingEnumSpanJavadocBeforeTerminatorWarnsSliceViolation {
	BETA,
	ALPHA, // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_lead_both_owners ===
enum InputFieldSortingEnumSpanLeadBothOwnersSliceViolation {
	// note-z
	ZETA,
	// note-a
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_lead_first_not_sorted ===
enum InputFieldSortingEnumSpanLeadFirstNotSortedSliceViolation {
	// note
	ZETA, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
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
	ACTIVE // violation (warning): Enum constant 'ACTIVE' should appear before 'ZOMBIE' (alphabetical order).
	/* line1

	   line3 */
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSpanListTailCommentNoTrailingCommaSliceViolation {
	ZOMBIE,
	ACTIVE // violation (warning): Enum constant 'ACTIVE' should appear before 'ZOMBIE' (alphabetical order).
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_list_tail_comment_own_line ===
enum InputFieldSortingEnumSpanListTailCommentOwnLineSliceViolation {
	ZOMBIE,
	ACTIVE, // violation (warning): Enum constant 'ACTIVE' should appear before 'ZOMBIE' (alphabetical order).
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumspan_member_shares_terminator_line ===
class InputFieldSortingEnumSpanMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ZETA,
		ALPHA; static final int VAL = 1; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	}
}
// === end ===

// === case: enumspan_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSpanMultilineAnnotationFirstLineSharedSliceViolation {
	BETA, @SuppressWarnings( // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
			"x")
	ALPHA
}
// === end ===

// === case: enumspan_nested_type_after_terminator ===
enum InputFieldSortingEnumSpanNestedTypeAfterTerminatorSliceViolation {
	ZETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).

	interface Marker {
	}
}
// === end ===

// === case: enumspan_non_canonical_indent ===
enum InputFieldSortingEnumSpanNonCanonicalIndentSliceViolation {
			ZETA,
			ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_acb ===
enum InputFieldSortingEnumSpanPermAcbSliceViolation {
	// L-A
	AAA, // T-A
	// L-C
	CCC, // T-C
	// L-B
	BBB // T-B // violation (warning): Enum constant 'BBB' should appear before 'CCC' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_bac ===
enum InputFieldSortingEnumSpanPermBacSliceViolation {
	// L-B
	BBB, // T-B
	// L-A
	AAA, // T-A // violation (warning): Enum constant 'AAA' should appear before 'BBB' (alphabetical order).
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
	AAA // T-A // violation (warning): Enum constant 'AAA' should appear before 'CCC' (alphabetical order).
}
// === end ===

// === case: enumspan_perm_cab ===
enum InputFieldSortingEnumSpanPermCabSliceViolation {
	// L-C
	CCC, // T-C
	// L-A
	AAA, // T-A // violation (warning): Enum constant 'AAA' should appear before 'CCC' (alphabetical order).
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
	BBB, // T-B // violation (warning): Enum constant 'BBB' should appear before 'CCC' (alphabetical order).
	// L-A
	AAA // T-A // violation (warning): Enum constant 'AAA' should appear before 'BBB' (alphabetical order).
}
// === end ===

// === case: enumspan_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSpanStackedAnnotationsAfterPreviousSliceViolation {
	ZETA, /* mid */ @Deprecated @SuppressWarnings("x") ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_string_delimiter_before_annotation ===
enum InputFieldSortingEnumSpanStringDelimiterBeforeAnnotationSliceViolation {
	@SuppressWarnings(
			"unchecked") @Deprecated
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_string_with_block_comment_marker ===
enum InputFieldSortingEnumSpanStringWithBlockCommentMarkerSliceViolation {
	ZETA,
	@SuppressWarnings("/* not a comment */") ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_suffix_unclosed_comment_after_brace ===
enum InputFieldSortingEnumSpanSuffixUnclosedCommentAfterBraceSliceViolation {
	ZETA,
	ALPHA, // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	BETA } /*
still open
*/
// === end ===

// === case: enumspan_supplementary_annotation_arg ===
enum InputFieldSortingEnumSpanSupplementaryAnnotationArgSliceViolation {
	ZETA,
	@SuppressWarnings("𝐀") ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_supplementary_constant_name ===
enum InputFieldSortingEnumSpanSupplementaryConstantNameSliceViolation {
	ZETA,
	ALPHA𝐀 // violation (warning): Enum constant 'ALPHA𝐀' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumspan_text_block_arg_multiline ===
enum InputFieldSortingEnumSpanTextBlockArgMultilineSliceViolation {
	ZETA("""
			x
			"""),
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
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
	ALPHA { // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
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
	ALPHA, // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: enumspan_trailing_comma_terminator_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommaTerminatorCommentMultilineSliceViolation {
	ZETA,
	ALPHA, // done // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	// more
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSpanTrailingCommaThenTerminatorCommentSliceViolation {
	ZETA,
	ALPHA, // done // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
	;

	int x;
}
// === end ===

// === case: enumspan_trailing_comment_multiline ===
enum InputFieldSortingEnumSpanTrailingCommentMultilineSliceViolation {
	ZETA /* a */
	/* b */, ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: enumsplit_annotated_same_line ===
enum InputFieldSortingEnumSplitAnnotatedSameLineSliceViolation {
	@Deprecated ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_annotation_arg_structural_chars ===
enum InputFieldSortingEnumSplitAnnotationArgStructuralCharsSliceViolation {
	ALPHA, @SuppressWarnings("{ } , // /*") ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_blank_line_between_constants ===
enum InputFieldSortingEnumSplitBlankLineBetweenConstantsSliceViolation {
	ALPHA,

	BETA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_brace_open_shared ===
enum InputFieldSortingEnumSplitBraceOpenSharedSliceViolation { ALPHA, BETA, // violation: Enum constant 'BETA' must be on its own line.
	ZETA
}
// === end ===

// === case: enumsplit_comment_after_comma ===
enum InputFieldSortingEnumSplitCommentAfterCommaSliceViolation {
	ALPHA, /* mid */ ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_comment_before_separator ===
enum InputFieldSortingEnumSplitCommentBeforeSeparatorSliceViolation {
	ALPHA /* t */, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_comment_between_annotation_and_constant ===
enum InputFieldSortingEnumSplitCommentBetweenAnnotationAndConstantSliceViolation {
	@Deprecated
	// note
	ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_comment_open_after_terminator ===
// skip-reason: cannot split enum constants onto their own lines: a comment between them does not close on the line it opens
enum InputFieldSortingEnumSplitCommentOpenAfterTerminatorSliceViolation {
	ALPHA, BETA, // violation: Enum constant 'BETA' must be on its own line.
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
	ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_non_nls_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineNonNlsSkipsSliceViolation {
	ALPHA("a"), BETA("b", // violation: Enum constant 'BETA' must be on its own line.
		"x"), GAMMA("c"); //$NON-NLS-1$

	InputFieldSortingEnumSplitConstantEndingOnSharedLineNonNlsSkipsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_nosonar_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineNosonarSkipsSliceViolation {
	ALPHA("a"), BETA("b", // violation: Enum constant 'BETA' must be on its own line.
		"x"), GAMMA("c"); // NOSONAR

	InputFieldSortingEnumSplitConstantEndingOnSharedLineNosonarSkipsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_constant_ending_on_shared_line_splits ===
enum InputFieldSortingEnumSplitConstantEndingOnSharedLineSplitsSliceViolation {
	ALPHA("a"), BETA("b", // violation: Enum constant 'BETA' must be on its own line.
		"x"), GAMMA("c");

	InputFieldSortingEnumSplitConstantEndingOnSharedLineSplitsSliceViolation(String... values) {
	}
}
// === end ===

// === case: enumsplit_directive_on_unshared_line_splits ===
enum InputFieldSortingEnumSplitDirectiveOnUnsharedLineSplitsSliceViolation {
	ALPHA, // NOSONAR
	BETA, GAMMA // violation: Enum constant 'GAMMA' must be on its own line.
}
// === end ===

// === case: enumsplit_formatter_off_above_header_splits ===
// @formatter:off
enum InputFieldSortingEnumSplitFormatterOffAboveHeaderSplitsSliceViolation {
	ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumsplit_formatter_off_on_header_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitFormatterOffOnHeaderSkipsSliceViolation { // @formatter:off
	ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumsplit_header_line_comment ===
enum InputFieldSortingEnumSplitHeaderLineCommentSliceViolation { // note
	ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_javadoc_after_comma ===
enum InputFieldSortingEnumSplitJavadocAfterCommaSliceViolation {
	ALPHA, BETA, /** doc */ // violation: Enum constant 'BETA' must be on its own line.
	ZETA
}
// === end ===

// === case: enumsplit_javadoc_before_terminator ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
enum InputFieldSortingEnumSplitJavadocBeforeTerminatorSliceViolation {
	ALPHA, BETA, // violation: Enum constant 'BETA' must be on its own line.
	ZETA /** @deprecated */;

	int legacy;
}
// === end ===

// === case: enumsplit_line_count_window_ending_above_header_splits ===
class InputFieldSortingEnumSplitLineCountWindowEndingAboveHeaderSplitsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	static final int LIMIT = 42;

	enum Inner {
		ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
	}
}
// === end ===

// === case: enumsplit_line_count_window_from_below_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitLineCountWindowFromBelowSkipsSliceViolation {
	ALPHA(42), ZETA(7); // violation: Enum constant 'ZETA' must be on its own line.
	// CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
	static final int OTHER = 1;
}
// === end ===

// === case: enumsplit_line_count_window_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitLineCountWindowSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	ALPHA(42), ZETA(7) // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_list_tail_comment_no_trailing_comma ===
enum InputFieldSortingEnumSplitListTailCommentNoTrailingCommaSliceViolation {
	ACTIVE, ZOMBIE // violation: Enum constant 'ZOMBIE' must be on its own line.
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumsplit_list_tail_comment_own_line ===
enum InputFieldSortingEnumSplitListTailCommentOwnLineSliceViolation {
	ACTIVE, ZOMBIE, // violation: Enum constant 'ZOMBIE' must be on its own line.
	// NOTE: order is serialized to disk
	;

	int x;
}
// === end ===

// === case: enumsplit_member_shares_terminator_line ===
class InputFieldSortingEnumSplitMemberSharesTerminatorLineSliceViolation {
	enum Inner {
		ALPHA, ZETA; static final int VAL = 1; // violation: Enum constant 'ZETA' must be on its own line.
	}
}
// === end ===

// === case: enumsplit_multiline_annotation_first_line_shared ===
enum InputFieldSortingEnumSplitMultilineAnnotationFirstLineSharedSliceViolation {
	ALPHA, @SuppressWarnings(
			"x")
	BETA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_non_canonical_indent ===
enum InputFieldSortingEnumSplitNonCanonicalIndentSliceViolation {
			ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_non_nls_tail_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNonNlsTailSkipsSliceViolation {
	FAIL("fail"), OK("ok") //$NON-NLS-1$ //$NON-NLS-2$ // violation: Enum constant 'OK' must be on its own line.
}
// === end ===

// === case: enumsplit_nosonar_block_comment_closing_on_shared_line_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNosonarBlockCommentClosingOnSharedLineSkipsSliceViolation {
	/* NOSONAR
	 */ ALPHA, BETA // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumsplit_nosonar_tail_skips ===
// skip-reason: cannot split enum constants onto their own lines: a tool directive shares their line and would then act on only one of them
enum InputFieldSortingEnumSplitNosonarTailSkipsSliceViolation {
	ALPHA, ZETA // NOSONAR // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_stacked_annotations_after_previous ===
enum InputFieldSortingEnumSplitStackedAnnotationsAfterPreviousSliceViolation {
	ALPHA, /* mid */ @Deprecated @SuppressWarnings("x") ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_switch_directive_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
enum InputFieldSortingEnumSplitSwitchDirectiveSkipsSliceViolation {
	// @formatter:off
	ALPHA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
	// @formatter:on
}
// === end ===

// === case: enumsplit_text_block_arg_multiline ===
enum InputFieldSortingEnumSplitTextBlockArgMultilineSliceViolation {
	ALPHA("""
			x
			"""), BETA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: enumsplit_trailing_comma ===
enum InputFieldSortingEnumSplitTrailingCommaSliceViolation {
	ALPHA, BETA, // violation: Enum constant 'BETA' must be on its own line.
}
// === end ===

// === case: enumsplit_trailing_comma_then_terminator_comment ===
enum InputFieldSortingEnumSplitTrailingCommaThenTerminatorCommentSliceViolation {
	ALPHA, ZETA, // done // violation: Enum constant 'ZETA' must be on its own line.
	;

	int x;
}
// === end ===

// === case: enumsplit_trailing_comment_multiline ===
// skip-reason: cannot relocate a comment trailing an enum constant: it spans multiple lines
enum InputFieldSortingEnumSplitTrailingCommentMultilineSliceViolation {
	ALPHA /* a */
	/* b */, BETA, ZETA // violation: Enum constant 'ZETA' must be on its own line.
}
// === end ===

// === case: field_annotation_consolidation ===
class InputFieldSortingFieldAnnotationConsolidationSliceViolation {
	@NonNull
	final String currencyCode;
	@Nullable
	final String engName, engSymbol;
	@NonNull // violation: Field 'equityNumber' (annotated @NonNull) must appear before 'engName' (annotated @Nullable) (annotation order, same type).
	final String equityNumber;
	@Nullable
	final String exchange;
	@NonNull // violation: Field 'source' (annotated @NonNull) must appear before 'exchange' (annotated @Nullable) (annotation order, same type).
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
	@Deprecated() // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
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
	Object beta = new @TypeUse Object();
	Object alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
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
	List<@Zebra String> typeArgAnnotated; // violation: Field 'typeArgAnnotated' (unannotated) must appear before 'fieldAnnotated' (annotated @Alpha) (annotation order, same type).
}
// === end ===

// === case: field_annotation_ignores_at_in_type_arg_with_initializer ===
// imports: java.util.List
class InputFieldSortingFieldAnnotationIgnoresAtInTypeArgWithInitializerSliceViolation {
	final List<@Zebra String> beta = null;
	final List<String> alpha = null; // violation: Field 'alpha' (type argument unannotated) must appear before 'beta' (type argument annotated @Zebra) (annotation order, same type).
}
// === end ===

// === case: field_annotation_multi_line ===
class InputFieldSortingFieldAnnotationMultiLineSliceViolation {
	@SuppressWarnings(
		"unused"
	)
	int beta = 1;
	int alpha = 2; // violation: Field 'alpha' (unannotated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
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
	@Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
	String alpha;
}
// === end ===

// === case: field_annotation_order_multi_annotation ===
class InputFieldSortingFieldAnnotationOrderMultiAnnotationSliceViolation {
	@Bnn
	String bField;
	@Ann // violation: Field 'abField' (annotated @Ann) must appear before 'bField' (annotated @Bnn) (annotation order, same type).
	@Bnn
	String abField;
}
// === end ===

// === case: field_annotation_order_qualified ===
class InputFieldSortingFieldAnnotationOrderQualifiedSliceViolation {
	@java.lang.SuppressWarnings("unused")
	String beta;
	@java.lang.Deprecated // violation: Field 'alpha' (annotated @Deprecated) must appear before 'beta' (annotated @SuppressWarnings) (annotation order, same type).
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
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
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
// skip-reason: cannot reorder fields: they have a circular dependency
// multi-fix-expected
class InputFieldSortingFieldCircularDependencySliceViolation {
	static final int A = B + 1; // violation: Field 'A' must appear after 'B' (dependency order).
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

// === case: field_comment_escaped_line_terminator_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldCommentEscapedLineTerminatorSkipsSliceViolation {
	private int zeta = 1; // \u000a { alpha = 9; }
	private int alpha = 3; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_annotation_param_order_merges ===
class InputFieldSortingFieldConsolidationAnnotationParamOrderMergesSliceViolation {
	@Multi(a = 1, b = 2)
	private long zeta;
	@Multi(b = 2, a = 1) // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private long alpha;
}
// === end ===

// === case: field_consolidation_base_annotation_line_comment ===
class InputFieldSortingFieldConsolidationBaseAnnotationLineCommentSliceViolation {
	@Deprecated
	private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
	@Deprecated // set in onBind
	private String body;
}
// === end ===

// === case: field_consolidation_base_carried_close_reorders ===
class InputFieldSortingFieldConsolidationBaseCarriedCloseReordersSliceViolation {
	private int width;
	/* in
	*/ private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_base_comment_before_type ===
class InputFieldSortingFieldConsolidationBaseCommentBeforeTypeSliceViolation {
	int beta;
	/* px */ int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_base_empty_block_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseEmptyBlockCommentReordersSliceViolation {
	private int width;
	/**/
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_base_leading_comment_reorders ===
class InputFieldSortingFieldConsolidationBaseLeadingCommentReordersSliceViolation {
	private int width;
	// in pixels
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_block_comment_contains_double_slash ===
class InputFieldSortingFieldConsolidationBlockCommentContainsDoubleSlashSliceViolation {
	String zebra /* fake // comment */;

	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_candidate_annotation_line_comment ===
class InputFieldSortingFieldConsolidationCandidateAnnotationLineCommentSliceViolation {
	@Deprecated // set in onBind
	private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
	@Deprecated
	private String body;
}
// === end ===

// === case: field_consolidation_candidate_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeSemicolonSliceViolation {
	int beta /* px */;
	int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_candidate_comment_before_type ===
class InputFieldSortingFieldConsolidationCandidateCommentBeforeTypeSliceViolation {
	/* px */ int beta;
	int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_comment_line_splits_three_fields ===
class InputFieldSortingFieldConsolidationCommentLineSplitsThreeFieldsSliceViolation {
	private int beta;
	// note
	private int zeta;
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_hoisted_comments_source_order ===
class InputFieldSortingFieldConsolidationHoistedCommentsSourceOrderSliceViolation {
	@Deprecated // set by native code
	private int zeta;
	@Deprecated // read by tests // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
	private int beta;
	@Deprecated // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
	private int alpha;
	private final int limit = 3; // violation: Field 'limit' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: field_consolidation_inline_base_stacked_commented_candidate ===
class InputFieldSortingFieldConsolidationInlineBaseStackedCommentedCandidateSliceViolation {
	@Deprecated // set in onBind
	private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
	@Deprecated private String body;
}
// === end ===

// === case: field_consolidation_keeps_directive_on_base ===
class InputFieldSortingFieldConsolidationKeepsDirectiveOnBaseSliceViolation {
	@Deprecated
	private int zeta;
	@Deprecated // NOSONAR // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int alpha;
}
// === end ===

// === case: field_consolidation_long_initializer_reorders_without_merge ===
class InputFieldSortingFieldConsolidationLongInitializerReordersWithoutMergeSliceViolation {
	int zeta = 1_000_000 + 2_000_000 + 3_000_000;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_moves_near_miss_directive ===
class InputFieldSortingFieldConsolidationMovesNearMissDirectiveSliceViolation {
	@Deprecated // NOSONA
	private int zeta;
	@Deprecated // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int alpha;
}
// === end ===

// === case: field_consolidation_nested_type_merges ===
class InputFieldSortingFieldConsolidationNestedTypeMergesSliceViolation {
	private ViewGroup.LayoutParams outer;
	private ViewGroup.LayoutParams inner; // violation: Field 'inner' must appear before 'outer' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_repeated_annotation_comment_closes_on_decl_line ===
class InputFieldSortingFieldConsolidationRepeatedAnnotationCommentClosesOnDeclLineSliceViolation {
	@Deprecated /* set in
	 */private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
	@Deprecated /* set in
	 */ private String body;
}
// === end ===

// === case: field_consolidation_repeated_annotation_line_comment_merges ===
class InputFieldSortingFieldConsolidationRepeatedAnnotationLineCommentMergesSliceViolation {
	@Deprecated // set in onBind
	private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
	@Deprecated // set in onBind
	private String body;
}
// === end ===

// === case: field_consolidation_repeated_directive_not_moved ===
class InputFieldSortingFieldConsolidationRepeatedDirectiveNotMovedSliceViolation {
	@Deprecated // NOSONAR
	private int zeta;
	@Deprecated // NOSONAR // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int alpha;
}
// === end ===

// === case: field_consolidation_shared_prefix_comment_merges ===
class InputFieldSortingFieldConsolidationSharedPrefixCommentMergesSliceViolation {
	/* px */ int zebra;
	/* px */ int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_across_block_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossBlockCommentLineSliceViolation {
	private int width;
	/* in pixels */
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_across_line_comment_line ===
class InputFieldSortingFieldConsolidationSkipsAcrossLineCommentLineSliceViolation {
	private int width;
	// in pixels
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_comment_before_semicolon ===
class InputFieldSortingFieldConsolidationSkipsBaseCommentBeforeSemicolonSliceViolation {
	int beta;
	int alpha /* px */; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_javadoc ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocSliceViolation {
	private int width;
	/** Height in pixels. */
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_annotation ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterAnnotationSliceViolation {
	@Deprecated private int width;
	@Deprecated /** d */ private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_close_at_column_zero ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCloseAtColumnZeroSliceViolation {
	private int width;
	/* in
*/ /** d */ private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_close_at_index_one ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCloseAtIndexOneSliceViolation {
	private int width;
	/* in
	*/ /** d */ private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_javadoc_after_carried_comment ===
class InputFieldSortingFieldConsolidationSkipsBaseJavadocAfterCarriedCommentSliceViolation {
	private int width;
	/* in
	 pixels */ /** doc */ private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_markdown_doc_comment ===
class InputFieldSortingFieldConsolidationSkipsBaseMarkdownDocCommentSliceViolation {
	private int width;
	/// Height in pixels.
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_base_starred_javadoc ===
class InputFieldSortingFieldConsolidationSkipsBaseStarredJavadocSliceViolation {
	private int width;
	/***/
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
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

// === case: field_consolidation_skips_directive_ahead_of_base_type ===
class InputFieldSortingFieldConsolidationSkipsDirectiveAheadOfBaseTypeSliceViolation {
	int zeta;
	/* NOSONAR */ int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_directive_on_base_line ===
class InputFieldSortingFieldConsolidationSkipsDirectiveOnBaseLineSliceViolation {
	private int zeta;
	private int alpha; // NOSONAR // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_hoist_below_base_leading_comment_line ===
class InputFieldSortingFieldConsolidationSkipsHoistBelowBaseLeadingCommentLineSliceViolation {
	/* w */ private int width;
	// in pixels
	private int height; // violation: Field 'height' must appear before 'width' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_moved_directive_ahead_of_type ===
class InputFieldSortingFieldConsolidationSkipsMovedDirectiveAheadOfTypeSliceViolation {
	/* NOSONAR */ int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_skips_moved_directive_on_annotation_line ===
class InputFieldSortingFieldConsolidationSkipsMovedDirectiveOnAnnotationLineSliceViolation {
	@Deprecated // NOSONAR
	private int zeta;
	@Deprecated // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int alpha;
}
// === end ===

// === case: field_consolidation_skips_other_run_of_same_key ===
class InputFieldSortingFieldConsolidationSkipsOtherRunOfSameKeySliceViolation {
	final int alpha = 1;
	final int beta;
	final int delta;
	final int gamma = 2; // violation: Field 'gamma' (final with inline value) must appear before final without inline value fields.

	InputFieldSortingFieldConsolidationSkipsOtherRunOfSameKeySliceViolation() {
		beta = 0;
		delta = 0;
	}
}
// === end ===

// === case: field_consolidation_skips_repeated_annotation_order ===
class InputFieldSortingFieldConsolidationSkipsRepeatedAnnotationOrderSliceViolation {
	@Tag("y")
	@Tag("x")
	String zebra;
	@Tag("x") // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	@Tag("y")
	String alpha;
}
// === end ===

// === case: field_consolidation_skips_same_simple_name_annotations ===
class InputFieldSortingFieldConsolidationSkipsSameSimpleNameAnnotationsSliceViolation {
	@javax.annotation.Nullable
	String zebra;
	@org.jetbrains.annotations.Nullable // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String alpha;
}
// === end ===

// === case: field_consolidation_skips_same_simple_nested_type ===
class InputFieldSortingFieldConsolidationSkipsSameSimpleNestedTypeSliceViolation {
	private ViewGroup.LayoutParams outer;
	private LinearLayout.LayoutParams inner; // violation: Field 'inner' (type 'LinearLayout.LayoutParams') must appear before 'outer' (type 'ViewGroup.LayoutParams').
}
// === end ===

// === case: field_consolidation_trailing_block_comment ===
class InputFieldSortingFieldConsolidationTrailingBlockCommentSliceViolation {
	int beta;
	int alpha; /* px */ // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_two_stacked_annotations ===
class InputFieldSortingFieldConsolidationTwoStackedAnnotationsSliceViolation {
	@Deprecated
	@SuppressWarnings("unused")
	private long zeta;
	@Deprecated // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	@SuppressWarnings("unused")
	private long alpha;
}
// === end ===

// === case: field_consolidation_wrapped_base_after_equals ===
class InputFieldSortingFieldConsolidationWrappedBaseAfterEqualsSliceViolation {
	private long timeout;
	private long delay = // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			60;
}
// === end ===

// === case: field_consolidation_wrapped_base_block_comment ===
class InputFieldSortingFieldConsolidationWrappedBaseBlockCommentSliceViolation {
	private long timeout;
	private long delay = 60 // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			/* ms */ * 1000;
}
// === end ===

// === case: field_consolidation_wrapped_base_dot ===
class InputFieldSortingFieldConsolidationWrappedBaseDotSliceViolation {
	private String timeout;
	private String delay = "60" // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			.trim();
}
// === end ===

// === case: field_consolidation_wrapped_base_equals_on_continuation ===
class InputFieldSortingFieldConsolidationWrappedBaseEqualsOnContinuationSliceViolation {
	private long timeout;
	private long delay // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			= 60;
}
// === end ===

// === case: field_consolidation_wrapped_base_minus ===
class InputFieldSortingFieldConsolidationWrappedBaseMinusSliceViolation {
	private long timeout;
	private long delay = 60 // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			- 1;
}
// === end ===

// === case: field_consolidation_wrapped_base_plus ===
class InputFieldSortingFieldConsolidationWrappedBasePlusSliceViolation {
	private long timeout;
	private long delay = 60 // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			+ 1000;
}
// === end ===

// === case: field_consolidation_wrapped_base_star ===
class InputFieldSortingFieldConsolidationWrappedBaseStarSliceViolation {
	private long timeout;
	private long delay = 60 // violation: Field 'delay' must appear before 'timeout' (alphabetical order, same type).
			* 1000;
}
// === end ===

// === case: field_consolidation_wrapped_candidate ===
class InputFieldSortingFieldConsolidationWrappedCandidateSliceViolation {
	private long beta = 60
			* 1000;
	private long alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_consolidation_wrapped_multi_var_base ===
class InputFieldSortingFieldConsolidationWrappedMultiVarBaseSliceViolation {
	private long zeta;
	private long alpha, // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
			beta;
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
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingFieldDependencyInitializerOnContinuationLineSliceViolation {
	int beta = 1 // violation: Field 'beta' must appear after 'alpha' (dependency order).
			+ this.alpha;
	int alpha = 10;
}
// === end ===

// === case: field_dependency_order ===
class InputFieldSortingFieldDependencyOrderSliceViolation {
	static final int B = A + 1; // violation: Field 'B' must appear after 'A' (dependency order).
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
	int alpha = 7; // violation: Field 'alpha' must appear before 'yankee' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_angle_explicit_type_arg ===
// imports: java.util.List
class InputFieldSortingFieldExtractNamesInitializerAngleExplicitTypeArgSliceViolation {
	int yak = List.<Integer>of().size(), zulu = 5;
	int alpha; // violation: Field 'alpha' must appear before 'yak' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_brace_comma ===
class InputFieldSortingFieldExtractNamesInitializerBraceCommaSliceViolation {
	int[] yankee = {1, 2, 3}, zulu = {4, 5};
	int[] alpha = {6}; // violation: Field 'alpha' must appear before 'yankee' (alphabetical order, same type).
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
	int alpha; // violation: Field 'alpha' must appear before 'max' (alphabetical order, same type).
}
// === end ===

// === case: field_extract_names_initializer_string_comma ===
class InputFieldSortingFieldExtractNamesInitializerStringCommaSliceViolation {
	String max = "x,y";
	String alpha = "a"; // violation: Field 'alpha' must appear before 'max' (alphabetical order, same type).
}
// === end ===

// === case: field_first_name_after_multi_name_declaration_reorders ===
class InputFieldSortingFieldFirstNameAfterMultiNameDeclarationReordersSliceViolation {
	long beta, gamma;
	long alpha, zeta; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: field_first_name_after_single_reorders ===
class InputFieldSortingFieldFirstNameAfterSingleReordersSliceViolation {
	long beta;
	long alpha, zeta; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
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

// === case: field_interleaved_across_instance_initializer_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldInterleavedAcrossInstanceInitializerSkipsSliceViolation {
	private int retries = 3;

	{
		retries = Integer.getInteger("app.retries", 5);
	}

	private static int instances;
	private int attempts; // violation: Field 'attempts' must appear before 'retries' (alphabetical order, same type).
}
// === end ===

// === case: field_interleaved_across_method_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldInterleavedAcrossMethodSkipsSliceViolation {
	int zeta;

	void use() {
	}

	static int shared;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
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
	static final String ALPHA = "a"; // violation: Field 'ALPHA' must appear before 'ZETA' (alphabetical order, same type).
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingFieldInterleavedDependencyBailSliceViolation {
	int zebra = this.alpha + 1; // violation: Field 'zebra' must appear after 'alpha' (dependency order).
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
// skip-reason: cannot reorder fields: an interleaved static or instance field is in the way
class InputFieldSortingFieldInterleavedSpansChunksBailSliceViolation {
	int alpha;
	static int s;
	final int zebra = 1; // violation: Field 'zebra' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: field_javadoc_on_class_header_line_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldJavadocOnClassHeaderLineSkipsSliceViolation { /** The zeta. */
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_javadoc_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldJavadocTrailingLineAboveSkipsSliceViolation {
	private int zeta; /**< the zeta */
	private int alpha; /**< the alpha */ // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
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

// === case: field_member_between_fields ===
// skip-reason: cannot reorder fields: code between them belongs to no field
class InputFieldSortingFieldMemberBetweenFieldsSliceViolation {
	int beta = 1;

	void m() {
	}

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

// === case: field_multivar_secondary_name_dependency_pinned_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyPinnedReordersSliceViolation {
	int z = 1;
	int a = y + 1; // violation: Field 'a' must appear after 'y' (dependency order).
	int x, y;
}
// === end ===

// === case: field_multivar_secondary_name_dependency_reorders ===
@SuppressWarnings("unused")
class InputFieldSortingFieldMultiVarSecondaryNameDependencyReordersSliceViolation {
	int z;
	int a = y + 1; // violation: Field 'a' must appear after 'y' (dependency order). // violation: Field 'a' must appear before 'z' (alphabetical order, same type).
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

// === case: field_order_tie_source_order ===
class InputFieldSortingFieldOrderTieSourceOrderSliceViolation {
	int zeta = 3;
	int alpha = 1; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	int aLPHA = 2;
}
// === end ===

// === case: field_order_tie_source_order_reversed ===
class InputFieldSortingFieldOrderTieSourceOrderReversedSliceViolation {
	int zeta = 3;
	int aLPHA = 2; // violation: Field 'aLPHA' must appear before 'zeta' (alphabetical order, same type).
	int alpha = 1;
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

// === case: field_shared_line_anon_close ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineAnonCloseSliceViolation {
	final Thread worker = new Thread() { // violation: Field 'worker' with an anonymous class initializer must appear after 'label'.
	}; final String label = "w";
}
// === end ===

// === case: field_shared_line_class_close_brace ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineClassCloseBraceSliceViolation {
	private int beta;
	private int alpha; } // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
// === end ===

// === case: field_shared_line_comment_above ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentAboveSliceViolation {
	// counters
	private String name; private int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
}
// === end ===

// === case: field_shared_line_comment_blank_line_inside_closes_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentBlankLineInsideClosesBeforeFieldSliceViolation {
	/* note

	 */ int zebra;
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_comment_closes_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentClosesBeforeFieldSliceViolation {
	int zebra; /* note
	   more */ int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_comment_continues_below ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentContinuesBelowSliceViolation {
	private String zebra;
	private String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	private String beta; /* retries,
			capped at 5 */
}
// === end ===

// === case: field_shared_line_comment_opener_on_other_group_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentOpenerOnOtherGroupFieldSliceViolation {
	static int q; /* c
	 */ int zebra;
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_comment_opens_before_other_group_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineCommentOpensBeforeOtherGroupFieldSliceViolation {
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	int beta; /* c
	 */ static int q;
}
// === end ===

// === case: field_shared_line_different_type ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineDifferentTypeSliceViolation {
	private String name; private int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
}
// === end ===

// === case: field_shared_line_elsewhere_in_group ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineElsewhereInGroupSliceViolation {
	private String name; private String title;
	private int count; // violation: Field 'count' (type 'int') must appear before 'title' (type 'String').
}
// === end ===

// === case: field_shared_line_enum_terminator ===
// skip-reason: cannot reorder fields: a field shares a line with other code
enum InputFieldSortingFieldSharedLineEnumTerminatorSliceViolation {
	ALPHA; static final int ZETA = 1;
	static final int BETA = 2; // violation: Field 'BETA' must appear before 'ZETA' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_multi_var_entry_in_group ===
class InputFieldSortingFieldSharedLineMultiVarEntryInGroupSliceViolation {
	private int zeta, alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private long beta;
}
// === end ===

// === case: field_shared_line_other_group ===
class InputFieldSortingFieldSharedLineOtherGroupSliceViolation {
	static int alpha; static int beta;

	private long zeta;
	private int count; // violation: Field 'count' (type 'int') must appear before 'zeta' (type 'long').
}
// === end ===

// === case: field_shared_line_precedes_static_annotation_skip ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLinePrecedesStaticAnnotationSkipSliceViolation {
	@Deprecated
	static int zeta; static int alpha; // violation: Field 'alpha' (unannotated) must appear before 'zeta' (annotated @Deprecated) (annotation order, same type).
}
// === end ===

// === case: field_shared_line_prefix_closes_earlier_comment ===
class InputFieldSortingFieldSharedLinePrefixClosesEarlierCommentSliceViolation {
	/* note
	 */ String zebra;
	String alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_same_type ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSameTypeSliceViolation {
	private int zeta; private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_single_entry_other_group ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSingleEntryOtherGroupSliceViolation {
	static int count; private int zeta, alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_static_and_instance ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineStaticAndInstanceSliceViolation {
	private long zeta;
	private static int shared; private int alpha; // violation: Field 'alpha' (type 'int') must appear before 'zeta' (type 'long').
}
// === end ===

// === case: field_shared_line_supplementary_char_before_field ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineSupplementaryCharBeforeFieldSliceViolation {
	static String s = """
			🎉🎉🎉🎉🎉"""; int zebra;
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_supplementary_char_in_comment ===
// multi-fix-expected
class InputFieldSortingFieldSharedLineSupplementaryCharInCommentSliceViolation {
	static final String PARTY = "x" /* 🎉 */;
	static final int BETA = 2; // violation: Field 'BETA' (type 'int') must appear before 'PARTY' (type 'String').
	static final int ALPHA = 1; // violation: Field 'ALPHA' must appear before 'BETA' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_supplementary_char_in_literal ===
// multi-fix-expected
class InputFieldSortingFieldSharedLineSupplementaryCharInLiteralSliceViolation {
	static final String PARTY = "🎉";
	static final int BETA = 2; // violation: Field 'BETA' (type 'int') must appear before 'PARTY' (type 'String').
	static final int ALPHA = 1; // violation: Field 'ALPHA' must appear before 'BETA' (alphabetical order, same type).
}
// === end ===

// === case: field_shared_line_wrapped_field_end ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingFieldSharedLineWrappedFieldEndSliceViolation {
	private String name = """
			x"""; private int count; // violation: Field 'count' (type 'int') must appear before 'name' (type 'String').
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

// === case: field_string_escaped_quote_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingFieldStringEscapedQuoteSkipsSliceViolation {
	private String zeta = "a\u0022 + \u0022b";
	private String alpha = "c"; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
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
	List<@Ann String> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_annotated_before_unannotated ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderAnnotatedBeforeUnannotatedSliceViolation {
	List<@Ann String> annotated;
	List<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @Ann) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_fewer_before_more ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderFewerBeforeMoreSliceViolation {
	List<@Ann @Bnn String> twoAnns;
	List<@Ann String> oneAnn; // violation: Field 'oneAnn' (type argument annotated @Ann) must appear before 'twoAnns' (type argument annotated @Ann) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_fqn_generic ===
class InputFieldSortingFieldTypeArgAnnotationOrderFqnGenericSliceViolation {
	java.util.Set<@Bnn String> bField;
	java.util.Set<@Ann String> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_lower_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderLowerBoundSliceViolation {
	List<? super @Bnn Number> bField;
	List<? super @Ann Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_parameterized ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderParameterizedSliceViolation {
	List<@Ann(2) String> higher;
	List<@Ann(1) String> lower; // violation: Field 'lower' (type argument annotated @Ann) must appear before 'higher' (type argument annotated @Ann) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_position_aware ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationOrderPositionAwareSliceViolation {
	Map<@Ann String, String> firstArgAnnotated;
	Map<String, @Ann String> firstArgUnannotated; // violation: Field 'firstArgUnannotated' (type argument unannotated) must appear before 'firstArgAnnotated' (type argument annotated @Ann) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardSliceViolation {
	List<@Bnn ? extends Number> bField;
	List<@Ann ? extends Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_order_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingFieldTypeArgAnnotationOrderWildcardBoundSliceViolation {
	List<? extends @Bnn Number> bField;
	List<? extends @Ann Number> aField; // violation: Field 'aField' (type argument annotated @Ann) must appear before 'bField' (type argument annotated @Bnn) (annotation order, same type).
}
// === end ===

// === case: field_type_arg_annotation_raw_vs_parameterized ===
// imports: java.util.Map
class InputFieldSortingFieldTypeArgAnnotationRawVsParameterizedSliceViolation {
	Map<@TAnnA String, String> annotated;
	Map raw; // violation: Field 'raw' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA) (annotation order, same type).
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

// === case: init_backward_qualified_read_reorders ===
class InputFieldSortingInitBackwardQualifiedReadReordersSliceViolation {
	private long count = 3;
	long total = this.count * 2L;
	int alpha; // violation: Field 'alpha' (type 'int') must appear before 'total' (type 'long').
}
// === end ===

// === case: init_bare_forward_read_of_inert_reorders ===
class InputFieldSortingInitBareForwardReadOfInertReordersSliceViolation {
	int total = count + Integer.parseInt("1"); // violation: Field 'total' must appear after 'count' (dependency order).
	int count;
}
// === end ===

// === case: init_bare_forward_read_pinned_skips ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingInitBareForwardReadPinnedSkipsSliceViolation {
	int total = count + Integer.parseInt("1"); // violation: Field 'total' must appear after 'count' (dependency order).
	int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_field_initializer_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousFieldInitializerSkipsSliceViolation {
	private Object total = new Object() { // violation: Field 'total' must appear after 'count' (dependency order).
		final int seen = InputFieldSortingInitForwardReadAnonymousFieldInitializerSkipsSliceViolation.this.count;
	};
	private int count = 3;
}
// === end ===

// === case: init_forward_read_anonymous_init_block_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadAnonymousInitBlockSkipsSliceViolation {
	private Object total = new Object() { // violation: Field 'total' must appear after 'count' (dependency order).
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
	private Object total = new Object() { // violation: Field 'total' must appear after 'count' (dependency order).
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
	private static Object total = new Object() { // violation: Field 'total' must appear after 'label' (dependency order).
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
	private final int total = this.label.length(); // violation: Field 'total' must appear after 'label' (dependency order).
	private final String label = "a" + int.class;
}
// === end ===

// === case: init_forward_read_computed_final_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadComputedFinalTargetSkipsSliceViolation {
	private final int total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private final int count = Integer.parseInt("3");
}
// === end ===

// === case: init_forward_read_constant_target_reorders ===
class InputFieldSortingInitForwardReadConstantTargetReordersSliceViolation {
	private final int total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private final int count = 3;
}
// === end ===

// === case: init_forward_read_created_array_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadCreatedArrayConcatTargetSkipsSliceViolation {
	private final int total = this.label.length(); // violation: Field 'total' must appear after 'label' (dependency order).
	private final String label = "a" + new int[0];
}
// === end ===

// === case: init_forward_read_division_by_zero_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadDivisionByZeroTargetSkipsSliceViolation {
	private final long total = this.ratio * 2L; // violation: Field 'total' must appear after 'ratio' (dependency order).
	private final int ratio = 1 / 0;
}
// === end ===

// === case: init_forward_read_final_array_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadFinalArrayTargetSkipsSliceViolation {
	private final int total = this.counts.length; // violation: Field 'total' must appear after 'counts' (dependency order).
	private final int[] counts = {1, 2};
}
// === end ===

// === case: init_forward_read_in_lambda_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadInLambdaSkipsSliceViolation {
	private Runnable total = () -> System.out.println(this.count); // violation: Field 'total' must appear after 'count' (dependency order).
	private int count = 3;
}
// === end ===

// === case: init_forward_read_null_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadNullConcatTargetSkipsSliceViolation {
	private final int total = this.label.length(); // violation: Field 'total' must appear after 'label' (dependency order).
	private final String label = "a" + null;
}
// === end ===

// === case: init_forward_read_of_later_static_reorders ===
class InputFieldSortingInitForwardReadOfLaterStaticReordersSliceViolation {
	static class Q {
		private int total = Q.count * 2;
		private int alpha; // violation: Field 'alpha' must appear before 'total' (alphabetical order, same type).
		private static int count = 3;
	}
}
// === end ===

// === case: init_forward_read_qualified_string_constant_reorders ===
class InputFieldSortingInitForwardReadQualifiedStringConstantReordersSliceViolation {
	private final int total = this.label.length(); // violation: Field 'total' must appear after 'label' (dependency order).
	private final java.lang.String label = "w";
}
// === end ===

// === case: init_forward_read_qualified_type_name_skips ===
// package: com.etk2000.checkstyle.inputs.fieldsorting
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadQualifiedTypeNameSkipsSliceViolation {
	private static int total = com.etk2000.checkstyle.inputs.fieldsorting.InputFieldSortingInitForwardReadQualifiedTypeNameSkipsSliceViolation.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private static int count = 3;
}
// === end ===

// === case: init_forward_read_same_declaration_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSameDeclarationSkipsSliceViolation {
	private int total = this.count * 2, count = 3; // violation: Field 'total' must appear after 'count' (dependency order).
}
// === end ===

// === case: init_forward_read_secondary_name_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSecondaryNameSkipsSliceViolation {
	private int alpha, total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private int count = 3;
	private int beta; // violation: Field 'beta' must appear before 'count' (alphabetical order, same type).
}
// === end ===

// === case: init_forward_read_static_qualified_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadStaticQualifiedSkipsSliceViolation {
	private static int total = InputFieldSortingInitForwardReadStaticQualifiedSkipsSliceViolation.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private static int count = 3;
}
// === end ===

// === case: init_forward_read_switch_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadSwitchTargetSkipsSliceViolation {
	private final int total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private final int count = switch (1) {
		case 1 -> 3;
		default -> 4;
	};
}
// === end ===

// === case: init_forward_read_this_concat_target_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadThisConcatTargetSkipsSliceViolation {
	private final int total = this.label.length(); // violation: Field 'total' must appear after 'label' (dependency order).
	private final String label = "a" + this;
}
// === end ===

// === case: init_forward_read_this_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardReadThisSkipsSliceViolation {
	private int total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private int count = 3;
}
// === end ===

// === case: init_forward_read_uninitialized_target_reorders ===
class InputFieldSortingInitForwardReadUninitializedTargetReordersSliceViolation {
	private int total = this.count * 2; // violation: Field 'total' must appear after 'count' (dependency order).
	private int count;
}
// === end ===

// === case: init_forward_write_skips ===
// skip-reason: cannot reorder fields: a field would move to or above an earlier field whose initializer reads it, which would then see the default value
class InputFieldSortingInitForwardWriteSkipsSliceViolation {
	int alpha = (beta = 5) + 1; // violation: Field 'alpha' must appear after 'beta' (dependency order).
	int beta = 2;
}
// === end ===

// === case: init_local_class_in_lambda_reads_later_field_skips ===
// imports: java.util.function.IntSupplier
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingInitLocalClassInLambdaReadsLaterFieldSkipsSliceViolation {
	static int alpha = compute(() -> { // violation: Field 'alpha' must appear after 'zeta' (dependency order).
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
	static Function<Object, String> describe = o -> { // violation: Field 'describe' must appear after 'handler' (dependency order).
		if (o instanceof InputFieldSortingInitPatternVariableOfEarlierStatementReadsLaterFieldReordersSliceViolation handler)
			return handler.toString();
		return handler.toString();
	};
	static InputFieldSortingInitPatternVariableOfEarlierStatementReadsLaterFieldReordersSliceViolation handler;
}
// === end ===

// === case: init_self_qualified_read_reorders ===
class InputFieldSortingInitSelfQualifiedReadReordersSliceViolation {
	private String label;
	private int count = this.count + 1; // violation: Field 'count' (type 'int') must appear before 'label' (type 'String').
}
// === end ===

// === case: init_written_lambda_local_named_like_later_field_reorders ===
// imports: java.util.function.Supplier
class InputFieldSortingInitWrittenLambdaLocalNamedLikeLaterFieldReordersSliceViolation {
	Supplier<Object> supplier = () -> {
		int rate;
		rate = 2;
		return rate;
	};
	int rate = Integer.parseInt("1");
	int alpha; // violation: Field 'alpha' must appear before 'rate' (alphabetical order, same type).
}
// === end ===

// === case: inner_enum ===
class InputFieldSortingInnerEnumSliceViolation {
	enum Inner {
		BETA,
		ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
	}
}
// === end ===

// === case: interface_field_name_order ===
interface InputFieldSortingInterfaceFieldNameOrderSliceViolation {
	int SECOND = 2;
	int FIRST = 1; // violation: Field 'FIRST' must appear before 'SECOND' (alphabetical order, same type).
}
// === end ===

// === case: lambda_parameter_named_like_later_field_not_a_dependency ===
// imports: java.util.function.DoubleUnaryOperator
class InputFieldSortingLambdaParameterNamedLikeLaterFieldNotADependencySliceViolation {
	private final DoubleUnaryOperator discount = rate -> rate * 0.9;
	private final double rate = 1.5; // violation: Field 'rate' (type 'double') must appear before 'discount' (type 'DoubleUnaryOperator').
}
// === end ===

// === case: leading_block_comment_no_star_continuation ===
class InputFieldSortingLeadingBlockCommentNoStarSliceViolation {
	/* block
	continuing line without a star
	*/
	String zebra;
	int alpha; // violation: Field 'alpha' (type 'int') must appear before 'zebra' (type 'String').
}
// === end ===

// === case: leading_block_comment_opener_on_code_line ===
// skip-reason: cannot reorder fields: a field shares a line with other code
class InputFieldSortingLeadingBlockCommentOpenerOnCodeLineSliceViolation {
	int zebra; /* start
	// middle
	*/
	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: leading_comment_separated_by_blank ===
// skip-reason: cannot reorder fields: a comment between them belongs to no field
class InputFieldSortingLeadingCommentSeparatedByBlankSliceViolation {
	int zebra;
	// a note about nothing in particular

	int alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: lexer_string_with_block_comment_marker ===
class InputFieldSortingLexerStringWithBlockCommentMarkerSliceViolation {
	final String zebra = "a /* fake block */ b";
	final String alpha = "one"; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: line_count_directive_above_first_field_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveAboveFirstFieldSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	private int zeta = 42;
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).

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
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: line_count_directive_one_line_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveOneLineSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zeta = 42;
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: line_count_directive_trailing_line_above_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveTrailingLineAboveSkipsSliceViolation {
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	int zeta = 42;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: line_count_directive_window_ends_above_span_reorders ===
class InputFieldSortingLineCountDirectiveWindowEndsAboveSpanReordersSliceViolation {
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES

	int zeta = 42;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: line_count_directive_window_from_below_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveWindowFromBelowSkipsSliceViolation {
	int zeta = 42;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	static int other; // CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
}
// === end ===

// === case: line_count_directive_zero_count_on_span_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingLineCountDirectiveZeroCountOnSpanSkipsSliceViolation {
	int zeta; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 0 LINES
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
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

// === case: name_within_decl_annotated_line_count_window_reorders ===
class InputFieldSortingNameWithinDeclAnnotatedLineCountWindowReordersSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	@Deprecated // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int zeta = 42, alpha = 7;
}
// === end ===

// === case: name_within_decl_comment_escaped_line_terminator_skips ===
// skip-reason: cannot reorder fields: the reorder would change a field, a comment or the code around them
class InputFieldSortingNameWithinDeclCommentEscapedLineTerminatorSkipsSliceViolation {
	// \u000a
	String zebra, alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_continuation_comment_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclContinuationCommentSkipsSliceViolation {
	String zebra, // note // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
			alpha;
}
// === end ===

// === case: name_within_decl_directive_on_annotation_line_reorders ===
class InputFieldSortingNameWithinDeclDirectiveOnAnnotationLineReordersSliceViolation {
	@Deprecated //$NON-NLS-1$ // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	String zebra, alpha;
}
// === end ===

// === case: name_within_decl_directive_on_line_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration's line holds a tool directive, which may count names by position
class InputFieldSortingNameWithinDeclDirectiveOnLineSkipsSliceViolation {
	private String title = "Title", key = "k"; //$NON-NLS-2$ // violation: Field 'key' must appear before 'title' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_initialized_reorders ===
class InputFieldSortingNameWithinDeclInitializedReordersSliceViolation {
	int beta = 1, alpha = 2; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated private int y, x; // violation: Field 'x' must appear before 'y' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated private int y[], x; // violation: Field 'x' (type 'int') must appear before 'y' (type 'int[]').
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
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
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclInteriorCommentSkipsSliceViolation {
	String zebra, /* keep me */ alpha; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_interior_comment_with_semicolon_skips ===
// skip-reason: cannot reorder fields: a multi-variable declaration contains a comment
class InputFieldSortingNameWithinDeclInteriorCommentWithSemicolonSkipsSliceViolation {
	String zebra, alpha /* x; y */; // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_line_count_window_from_below_wrapped_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowFromBelowWrappedSkipsSliceViolation {
	private int zeta = 42, // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
			alpha = 7;
	// CHECKSTYLE IGNORE MagicNumber FOR -2 LINES
}
// === end ===

// === case: name_within_decl_line_count_window_one_line_reorders ===
class InputFieldSortingNameWithinDeclLineCountWindowOneLineReordersSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zeta = 42, alpha = 7; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_line_count_window_rewrap_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowRewrapSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zetaWithALongNameToForceAWrap = 42, alphaWithALongNameToForceAWrap = 7, betaWithALongNameToForceAWrap = 9; // violation: Field 'alphaWithALongNameToForceAWrap' must appear before 'zetaWithALongNameToForceAWrap' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_line_count_window_wrapped_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclLineCountWindowWrappedSkipsSliceViolation {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int zeta = 42, // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
			alpha = 7;
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
// skip-reason: cannot reorder fields: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclSortedNamesCStylePrevDeclaratorSliceViolation {
	int alpha[], beta; // violation: Field 'beta' (type 'int') must appear before 'alpha' (type 'int[]').
}
// === end ===

// === case: name_within_decl_sorted_names_chunk_violation ===
class InputFieldSortingNameWithinDeclSortedNamesChunkViolationSliceViolation {
	final int a, b = 1; // violation: Field 'b' (final with inline value) must appear before final without inline value fields.
}
// === end ===

// === case: name_within_decl_supplementary_char_value_reorders ===
class InputFieldSortingNameWithinDeclSupplementaryCharValueReordersSliceViolation {
	int zeta = "𝑥".length(), alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: name_within_decl_switch_directive_on_annotation_line_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingNameWithinDeclSwitchDirectiveOnAnnotationLineSkipsSliceViolation {
	@Deprecated // @formatter:off // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
	private int zeta = 42,
			alpha = 7;
	// @formatter:on
}
// === end ===

// === case: name_within_decl_unsorted_reported_when_placed ===
class InputFieldSortingNameWithinDeclUnsortedReportedWhenPlacedSliceViolation {
	long alpha, zeta, beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
	long gamma;
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
	B((new int[] {1, 2})),
	A(new int[] {3}) // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: nested_type_constant_initializer_sorts ===
// imports: java.util.ArrayList
// imports: java.util.List
final class InputFieldSortingNestedTypeConstantInitializerSortsSliceViolation {
	static final class Registry {
		static final List<String> NAMES = new ArrayList<>();
		static final int RATE = 10; // violation: Field 'RATE' (type 'int') must appear before 'NAMES' (type 'List').
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
	int zeta;
	int beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: own_line_directive_near_miss_capitalised_merges ===
class InputFieldSortingOwnLineDirectiveNearMissCapitalisedMergesSliceViolation {
	// Language=SQL
	int alpha;
	int zeta;
	int beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: parenthesized_literal_initializer_sorts ===
class InputFieldSortingParenthesizedLiteralInitializerSortsSliceViolation {
	private static Object widgets = "none";
	private static int clockWidth = ((120)); // violation: Field 'clockWidth' (type 'int') must appear before 'widgets' (type 'Object').
}
// === end ===

// === case: pin_chunk_order_inert_reorders ===
class InputFieldSortingPinChunkOrderInertReordersSliceViolation {
	int zeta = Integer.parseInt("1");
	final String label = "w"; // violation: Field 'label' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: pin_chunk_order_skips ===
// skip-reason: cannot reorder fields: a field would move past another field whose initializer runs code or reads it, which could change the value read or written
class InputFieldSortingPinChunkOrderSkipsSliceViolation {
	int zeta = Integer.parseInt("1");
	final String label = String.valueOf(2); // violation: Field 'label' (final with inline value) must appear before non-final fields.
}
// === end ===

// === case: pin_free_field_moves_past_pinned_pair_reorders ===
class InputFieldSortingPinFreeFieldMovesPastPinnedPairReordersSliceViolation {
	int zeta = Integer.parseInt("1");
	int beta = 2;
	int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: pin_inert_runs_reorders ===
class InputFieldSortingPinInertRunsReordersSliceViolation {
	int zeta;
	int alpha = Integer.parseInt("1"); // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: pin_literal_runs_kept_reorders ===
class InputFieldSortingPinLiteralRunsKeptReordersSliceViolation {
	int zeta = 2;
	int beta = Integer.parseInt("1");
	int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: pin_runs_inert_reorders ===
class InputFieldSortingPinRunsInertReordersSliceViolation {
	int zeta = Integer.parseInt("1");
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: pin_runs_runs_reorders ===
class InputFieldSortingPinRunsRunsReordersSliceViolation {
	int zeta = Integer.parseInt("1");
	int beta = Integer.parseInt("2");
	int alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: pin_within_declaration_among_fields_reorders ===
class InputFieldSortingPinWithinDeclarationAmongFieldsReordersSliceViolation {
	int zeta = Integer.parseInt("1"), beta = 2;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: pin_within_single_declaration_reorders ===
class InputFieldSortingPinWithinSingleDeclarationReordersSliceViolation {
	int zeta = Integer.parseInt("1"), beta = 2, alpha; // violation: Field 'alpha' must appear before 'beta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_above_enum_warns ===
// @formatter:off
enum InputFieldSortingRangeDirectiveAboveEnumWarnsSliceViolation {
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: range_directive_before_span_skips ===
// skip-reason: cannot reorder fields: a tool directive set apart from the field it leads would then lead another field
class InputFieldSortingRangeDirectiveBeforeSpanSkipsSliceViolation {
	// @formatter:off

	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_block_comment_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveBlockCommentSkipsSliceViolation {
	/* @formatter:off */
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_checkstyle_colon_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleColonOnAloneSkipsSliceViolation {
	int zeta;
	// CHECKSTYLE:ON
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_checkstyle_colon_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleColonSkipsSliceViolation {
	// CHECKSTYLE:OFF
	int zeta;
	// CHECKSTYLE:ON
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_checkstyle_dot_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleDotOnAloneSkipsSliceViolation {
	int zeta;
	// CHECKSTYLE.ON: MagicNumber
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_checkstyle_dot_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveCheckstyleDotSkipsSliceViolation {
	// CHECKSTYLE.OFF: MagicNumber
	int zeta;
	// CHECKSTYLE.ON: MagicNumber
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_enum_header_line_warns ===
enum InputFieldSortingRangeDirectiveEnumHeaderLineWarnsSliceViolation { // @formatter:off
	ZETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: range_directive_enum_warns ===
enum InputFieldSortingRangeDirectiveEnumWarnsSliceViolation {
	// @formatter:off
	ZETA,
	// @formatter:on
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZETA' (alphabetical order).
}
// === end ===

// === case: range_directive_formatter_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveFormatterOnAloneSkipsSliceViolation {
	int zeta;
	// @formatter:on
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_formatter_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveFormatterSkipsSliceViolation {
	// @formatter:off
	int zeta;
	// @formatter:on
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_near_miss_lowercase_reorders ===
class InputFieldSortingRangeDirectiveNearMissLowercaseReordersSliceViolation {
	// checkstyle:off
	int zeta;
	// checkstyle:on
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_nosonar_rides_reorders ===
class InputFieldSortingRangeDirectiveNosonarRidesReordersSliceViolation {
	// NOSONAR
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_off_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveOffAloneSkipsSliceViolation {
	// @formatter:off
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_other_group_interleaved_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveOtherGroupInterleavedSkipsSliceViolation {
	int zeta;
	static int beta; // @formatter:off
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_outside_span_reorders ===
class InputFieldSortingRangeDirectiveOutsideSpanReordersSliceViolation {
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).

	// @formatter:off
	void m() {
	}
	// @formatter:on
}
// === end ===

// === case: range_directive_single_declaration_reorders ===
class InputFieldSortingRangeDirectiveSingleDeclarationReordersSliceViolation {
	// @formatter:off
	int zeta, alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_spotless_on_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveSpotlessOnAloneSkipsSliceViolation {
	int zeta;
	// spotless:on
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_spotless_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveSpotlessSkipsSliceViolation {
	// spotless:off
	int zeta;
	// spotless:on
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: range_directive_trailing_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRangeDirectiveTrailingSkipsSliceViolation {
	int zeta; // @formatter:off
	int alpha; // @formatter:on // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: record_static_field_name_order ===
record InputFieldSortingRecordStaticFieldNameOrderSliceViolation() {
	static int b = 2;
	static int a = 1; // violation: Field 'a' must appear before 'b' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_blank_line_layout_skips ===
// skip-reason: cannot reorder fields: a comment between them belongs to no field
class InputFieldSortingRegionMarkerBlankLineLayoutSkipsSliceViolation {
	//region Tuning
	private int zeta;
	//endregion

	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_block_comment_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerBlockCommentSkipsSliceViolation {
	/* region Tuning */
	private int zeta;
	/* endregion */
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_compact_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerCompactSkipsSliceViolation {
	//region Tuning
	private int zeta;
	//endregion
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_editor_fold_close_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEditorFoldCloseAloneSkipsSliceViolation {
	private int zeta;
	// </editor-fold>
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_editor_fold_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEditorFoldSkipsSliceViolation {
	// <editor-fold desc="Tuning">
	private int zeta;
	// </editor-fold>
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_end_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerEndAloneSkipsSliceViolation {
	private int zeta;
	// endregion
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_hash_end_alone_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerHashEndAloneSkipsSliceViolation {
	private int zeta;
	// #endregion
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_hash_near_miss_reorders ===
class InputFieldSortingRegionMarkerHashNearMissReordersSliceViolation {
	// #regional default
	private int zeta;
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_hash_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerHashSkipsSliceViolation {
	//#region Tuning
	private int zeta;
	//#endregion
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_near_miss_prose_reorders ===
class InputFieldSortingRegionMarkerNearMissProseReordersSliceViolation {
	// regional default
	private int zeta;
	// the region it ends
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: region_marker_spaced_skips ===
// skip-reason: cannot reorder fields: a comment around them switches a tool off or on for the lines after it or counts lines
class InputFieldSortingRegionMarkerSpacedSkipsSliceViolation {
	// region
	private int zeta;
	// endregion
	private int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: reorder_constant_named_like_token ===
enum InputFieldSortingReorderConstantNamedLikeTokenSliceViolation {
	NUMBER,
	IDENT // violation (warning): Enum constant 'IDENT' should appear before 'NUMBER' (alphabetical order).
}
// === end ===

// === case: reorder_two_constants ===
enum InputFieldSortingReorderTwoConstantsSliceViolation {
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_block_comment_parens ===
enum InputFieldSortingReorderWithAnnotationBlockCommentParensSliceViolation {
	@SuppressWarnings(/* ( */ "x")
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_char_literal_parens ===
enum InputFieldSortingReorderWithAnnotationCharLiteralParensSliceViolation {
	@TAnnParam('(')
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_comment_parens ===
enum InputFieldSortingReorderWithAnnotationCommentParensSliceViolation {
	@Deprecated // has ( here
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_multi_line ===
enum InputFieldSortingReorderWithAnnotationMultiLineSliceViolation {
	@SuppressWarnings(
			"unchecked"
	)
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotation_string_parens ===
enum InputFieldSortingReorderWithAnnotationStringParensSliceViolation {
	@SuppressWarnings("(((")
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_annotations ===
enum InputFieldSortingReorderWithAnnotationsSliceViolation {
	@Deprecated
	BETA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
}
// === end ===

// === case: reorder_with_arguments ===
enum InputFieldSortingReorderWithArgumentsSliceViolation {
	CHERRY("red"),
	APPLE("green") // violation (warning): Enum constant 'APPLE' should appear before 'CHERRY' (alphabetical order).
}
// === end ===

// === case: reorder_with_block_comment ===
enum InputFieldSortingReorderWithBlockCommentSliceViolation {
	/*
	 * ZEBRA docs
	 */
	ZEBRA,
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
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
	ADD { // violation (warning): Enum constant 'ADD' should appear before 'SUBTRACT' (alphabetical order).
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
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: reorder_with_javadoc ===
enum InputFieldSortingReorderWithJavadocSliceViolation {
	/** ZEBRA constant. */
	ZEBRA,
	/** ALPHA constant. */
	ALPHA // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: reorder_with_semicolon_and_trailing_comments ===
enum InputFieldSortingReorderWithSemicolonAndTrailingCommentsSliceViolation {
	BETA, // b
	ALPHA; // a // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
	int x;
}
// === end ===

// === case: reorder_with_semicolon_on_last ===
enum InputFieldSortingReorderWithSemicolonOnLastSliceViolation {
	BETA,
	ALPHA; // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
	int x;
}
// === end ===

// === case: reorder_with_trailing_comment_escaped_quote ===
enum InputFieldSortingReorderWithTrailingCommentEscapedQuoteSliceViolation {
	B("test\\"), // note
	A("x") // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: reorder_with_trailing_comments ===
enum InputFieldSortingReorderWithTrailingCommentsSliceViolation {
	CHERRY, // fruit
	APPLE, // fruit // violation (warning): Enum constant 'APPLE' should appear before 'CHERRY' (alphabetical order).
	BANANA // fruit
}
// === end ===

// === case: reorder_with_url_in_string_arg ===
enum InputFieldSortingReorderWithUrlInStringArgSliceViolation {
	B("http://example.com"),
	A("y") // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: same_line_and_reorder ===
enum InputFieldSortingSameLineAndReorderSliceViolation {
	ZEBRA, ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'ZEBRA' (alphabetical order).
}
// === end ===

// === case: same_line_tab_separated ===
enum InputFieldSortingSameLineTabSeparatedSliceViolation {
	BETA,	ALPHA // violation: Enum constant 'ALPHA' must be on its own line. // violation (warning): Enum constant 'ALPHA' should appear before 'BETA' (alphabetical order).
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
	B(1, 2), A(3) // violation: Enum constant 'A' must be on its own line. // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: same_line_two_with_comma_in_char ===
enum InputFieldSortingSameLineTwoWithCommaInCharSliceViolation {
	B(','), A('.') // violation: Enum constant 'A' must be on its own line. // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: same_line_two_with_comma_in_string ===
enum InputFieldSortingSameLineTwoWithCommaInStringSliceViolation {
	B("x, y"), A("z") // violation: Enum constant 'A' must be on its own line. // violation (warning): Enum constant 'A' should appear before 'B' (alphabetical order).
}
// === end ===

// === case: same_line_with_semicolon ===
enum InputFieldSortingSameLineWithSemicolonSliceViolation {
	ALPHA, BETA; // violation: Enum constant 'BETA' must be on its own line.
	int x;
}
// === end ===

// === case: statement_directive_led_base_reorders_without_merge ===
class InputFieldSortingStatementDirectiveLedBaseReordersWithoutMergeSliceViolation {
	//noinspection unused
	int alpha;
	int zeta;
	int beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: statement_directive_led_later_reorders_without_merge ===
class InputFieldSortingStatementDirectiveLedLaterReordersWithoutMergeSliceViolation {
	//noinspection unused
	int zeta;
	int alpha; // violation: Field 'alpha' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: statement_directive_near_miss_capitalised_merges ===
class InputFieldSortingStatementDirectiveNearMissCapitalisedMergesSliceViolation {
	// NoInspection unused
	int alpha;
	int zeta;
	int beta; // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
}
// === end ===

// === case: statement_directive_on_annotation_line_reorders_without_merge ===
class InputFieldSortingStatementDirectiveOnAnnotationLineReordersWithoutMergeSliceViolation {
	@Deprecated //noinspection unused
	int alpha;
	@Deprecated
	int zeta;
	@Deprecated // violation: Field 'beta' must appear before 'zeta' (alphabetical order, same type).
	int beta;
}
// === end ===

// === case: string_constant_initializer_sorts ===
final class InputFieldSortingStringConstantInitializerSortsSliceViolation {
	private final String user = System.getProperty("user.name");
	private final String tag = "session:" + "x"; // violation: Field 'tag' must appear before 'user' (alphabetical order, same type).

	@Override
	public String toString() {
		return user;
	}
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
	List<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_empty_parens ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationEmptyParensSliceViolation {
	List<@TAnnB() String> bField;
	List<@TAnnA() String> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_explicit_value_keyword ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationExplicitValueKeywordSliceViolation {
	List<@TAnnParam(value = 2) String> higher;
	List<@TAnnParam(value = 1) String> lower; // violation: Field 'lower' (type argument annotated @TAnnParam) must appear before 'higher' (type argument annotated @TAnnParam) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_lower_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationLowerBoundSliceViolation {
	List<? super @TAnnB Number> bField;
	List<? super @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_more_before_fewer ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationMoreBeforeFewerSliceViolation {
	List<@TAnnA @TAnnB String> twoAnns;
	List<@TAnnA String> oneAnn; // violation: Field 'oneAnn' (type argument annotated @TAnnA) must appear before 'twoAnns' (type argument annotated @TAnnA) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_negated_value_orders_by_key ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationNegatedValueOrdersByKeySliceViolation {
	List<@TAnnParam(1) String> beta;
	List<@TAnnParam(-1) String> alpha; // violation: Field 'alpha' (type argument annotated @TAnnParam) must appear before 'beta' (type argument annotated @TAnnParam) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_parameterized ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationParameterizedSliceViolation {
	List<@TAnnParam(2) String> higher;
	List<@TAnnParam(1) String> lower; // violation: Field 'lower' (type argument annotated @TAnnParam) must appear before 'higher' (type argument annotated @TAnnParam) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_position_aware ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationPositionAwareSliceViolation {
	Map<@TAnnA String, String> firstArgAnnotated;
	Map<String, @TAnnA String> firstArgUnannotated; // violation: Field 'firstArgUnannotated' (type argument unannotated) must appear before 'firstArgAnnotated' (type argument annotated @TAnnA) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_qualified ===
@SuppressWarnings("PreferImport")
class InputFieldSortingTypeArgAnnotationQualifiedSliceViolation {
	java.util.Set<@TAnnA String> annotated;
	java.util.Set<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_same_annotations_falls_to_name ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationSameAnnotationsFallsToNameSliceViolation {
	@Deprecated
	final List<@TAnnA String> zebra = null;
	@Deprecated // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
	final List<@TAnnA String> alpha = null;
}
// === end ===

// === case: typeargannotation_second_arg_annotated ===
// imports: java.util.Map
class InputFieldSortingTypeArgAnnotationSecondArgAnnotatedSliceViolation {
	Map<String, @TAnnA Integer> annotated;
	Map<String, Integer> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_wildcard ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardSliceViolation {
	List<@TAnnB ? extends Number> bField;
	List<@TAnnA ? extends Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_wildcard_bound ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWildcardBoundSliceViolation {
	List<? extends @TAnnB Number> bField;
	List<? extends @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB) (annotation order, same type).
}
// === end ===

// === case: typeargannotation_wrong_order ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationWrongOrderSliceViolation {
	List<@TAnnB String> bField;
	List<@TAnnA String> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB) (annotation order, same type).
}
// === end ===