package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.PreferCollectionInterfaceCheck;
import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

class PreferCollectionInterfaceFixerTest {
	private static final PreferCollectionInterfaceFixer FIXER = new PreferCollectionInterfaceFixer();
	private static final String TOPIC = "prefercollectioninterface";

	@Test
	public void testBlockCommentImportIgnored() throws Exception {
		assertSkipResult(FIXER, TOPIC, "block_comment_import_ignored", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}

	@Test
	public void testBlockCommentPackageIgnored() throws Exception {
		assertSkipResult(FIXER, TOPIC, "block_comment_package_ignored", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}

	/**
	 * A cast's {@code TYPE} hangs off {@code TYPECAST}, so the position names a type the check
	 * never reports and rewriting it would widen an expression rather than a signature.
	 */
	@Test
	public void testCastTypeTargetIsStale() throws Exception {
		assertSkipResult(FIXER, TOPIC, "cast_type_target_is_stale", SkipMessages.COLLECTION_INTERFACE_STALE);
	}

	@Test
	public void testLinkedListSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "linked_list_skipped", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}

	/**
	 * A local's {@code TYPE} hangs off {@code VARIABLE_DEF}, which the tightened type-identifier
	 * guard rejects; without it the line-local scan would happily widen the local's declaration.
	 */
	@Test
	public void testLocalVariableTypeTargetIsStale() throws Exception {
		assertSkipResult(FIXER, TOPIC, "local_variable_type_target_is_stale", SkipMessages.COLLECTION_INTERFACE_STALE);
	}

	/**
	 * The buffer does not parse, so only the line-local identifier-start guard stands between a
	 * column that drifted into the middle of {@code ArrayList} and a rewrite of the whole name.
	 */
	@Test
	public void testMidIdentifierColumnIsStale() throws Exception {
		assertSkipResult(FIXER, TOPIC, "mid_identifier_column_is_stale", SkipMessages.COLLECTION_INTERFACE_STALE);
	}

	@Test
	public void testMultipleParamsFirst() throws Exception {
		// can't migrate: snippet has two violations on one line; assertCaseFix expects zero residual after one fix, but fixing only the first leaves the second
		assertSimpleFix(FIXER, TOPIC, "multiple_params_first", Set.of("java.util.List"));
	}

	@Test
	public void testMultipleParamsSecond() throws Exception {
		// can't migrate: same shape as multiple_params_first, with second-position target
		assertSimpleFix(FIXER, TOPIC, "multiple_params_second", Set.of("java.util.Map"));
	}

	/**
	 * {@code new ArrayList<>()} puts the name under {@code LITERAL_NEW}, not a {@code TYPE}, so
	 * rewriting it would turn the instantiation into {@code new List<>()}.
	 */
	@Test
	public void testNewExpressionTypeTargetIsStale() throws Exception {
		assertSkipResult(FIXER, TOPIC, "new_expression_type_target_is_stale", SkipMessages.COLLECTION_INTERFACE_STALE);
	}

	@Test
	public void testNoMatch() throws Exception {
		assertSkipResult(FIXER, TOPIC, "no_match", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}

	@Test
	public void testNonIdentifierColumnOnAParseableBufferIsStale() throws Exception {
		assertSkipResult(
				FIXER,
				TOPIC,
				"non_identifier_column_on_a_parseable_buffer_is_stale",
				SkipMessages.COLLECTION_INTERFACE_STALE
		);
	}

	@Test
	public void testPackageAndStaticImportResolved() throws Exception {
		assertSimpleFix(FIXER, TOPIC, "package_and_static_import_resolved", Set.of("java.util.List"));
	}

	/**
	 * {@code recordTypePairAt} ascends one {@code DOT} and demands the type's own name, so a
	 * qualifier segment of an accessor's qualified return type yields no pair while the position is
	 * still half of one.
	 */
	@Test
	public void testRecordAccessorQualifierSegmentSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "record_accessor_qualifier_segment_skipped", SkipMessages.RECORD_PAIR_HALF);
	}

	/** The component-side counterpart of {@link #testRecordAccessorQualifierSegmentSkipped}. */
	@Test
	public void testRecordComponentQualifierSegmentSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "record_component_qualifier_segment_skipped", SkipMessages.RECORD_PAIR_HALF);
	}

	/**
	 * {@code assertCaseSkip} only ever drives {@code violations.getFirst()}, which on a record pair
	 * is the component half, so the accessor half's deferral has to be driven by hand.
	 */
	@Test
	public void testRecordPairAccessorHalfDefers() throws Exception {
		final var slice = TestResources.loadCaseSlice(TOPIC, "record_component_and_its_explicit_accessor_are_flagged_together");
		final var source = FullPipelineRunner.stripViolationComments(String.join("\n", slice.inputLines()));
		final var violations = BaseCheckTest.runCheckInline(PreferCollectionInterfaceCheck.class, source);
		assertEquals(2, violations.size());
		final var accessor = violations.stream().max(Comparator.comparingInt(AuditEvent::getLine)).orElseThrow();
		final var lines = new ArrayList<>(List.of(source.split("\n", -1)));
		final var lineIndex = accessor.getLine() - 1;
		final var column = CheckstyleFixAction.tabColumnToCharIndex(lines.get(lineIndex), accessor.getColumn() - 1);
		final var result = assertInstanceOf(SkipResult.class, FIXER.fix(lines, lineIndex, column));
		assertEquals(SkipMessages.RECORD_PAIR_DEFERRED, result.reason());
	}

	@Test
	public void testRecordWordInCommentDoesNotRefuseFix() throws Exception {
		assertSimpleFix(FIXER, TOPIC, "record_word_in_comment_not_a_pair", Set.of("java.util.List"));
	}

	@Test
	public void testRecordWordInStringDoesNotRefuseFix() throws Exception {
		assertSimpleFix(FIXER, TOPIC, "record_word_in_string_not_a_pair", Set.of("java.util.List"));
	}

	@Test
	public void testReplacementImportAlreadyPresent() throws Exception {
		// the fixer reports the interface even when the file already imports it; production's
		// insertMissingImports drops the duplicate, and the slice harness compares net-new only
		assertSimpleFix(FIXER, TOPIC, "replacement_import_already_present", Set.of("java.util.List"));
	}

	@Test
	public void testReturnAndParamParam() throws Exception {
		// can't migrate: snippet has two violations on one line; fixing only the param leaves the return-type violation
		assertSimpleFix(FIXER, TOPIC, "return_and_param_param", Set.of("java.util.Set"));
	}

	@Test
	public void testReturnAndParamReturn() throws Exception {
		// can't migrate: snippet has two violations on one line; fixing only the return leaves the param violation
		assertSimpleFix(FIXER, TOPIC, "return_and_param_return", Set.of("java.util.List"));
	}

	@Test
	public void testUnparseableRecordDeclarationSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "unparseable_record_declaration_skipped", SkipMessages.RECORD_PAIR_HALF);
	}

	@Test
	public void testUnresolvableQualifiedNameSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "unresolvable_qualified_name_skipped", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}

	@Test
	public void testUnresolvableSimpleNameSkipped() throws Exception {
		assertSkipResult(FIXER, TOPIC, "unresolvable_simple_name_skipped", SkipMessages.COLLECTION_INTERFACE_SKIP);
	}
}