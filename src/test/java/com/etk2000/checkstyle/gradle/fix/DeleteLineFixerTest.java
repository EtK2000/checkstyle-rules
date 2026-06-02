package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;

import org.junit.jupiter.api.Test;

public class DeleteLineFixerTest {
	private static final String TOPIC = "deleteline";

	private final CheckstyleFixer fixer = new DeleteLineFixer();

	@Test
	public void testDeleteFirstLine() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input; DeleteLineFixer is shared by RegexpMultiline/RegexpSingleline (AbstractFileSetCheck) and UnusedImports/RedundantImport, no single AbstractCheck class drives this case
		assertSimpleFix(fixer, TOPIC, "delete_first_line");
	}

	@Test
	public void testDeleteFirstLineBlankBelow() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input (`import A;` is not a resolvable import)
		assertSimpleFix(fixer, TOPIC, "delete_first_line_blank_below");
	}

	@Test
	public void testDeleteImportBlankAboveOnly() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input (`import A;` is not a resolvable import)
		assertSimpleFix(fixer, TOPIC, "delete_import_blank_above_only");
	}

	@Test
	public void testDeleteImportBlankBelowOnly() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input (`import A;` is not a resolvable import)
		assertSimpleFix(fixer, TOPIC, "delete_import_blank_below_only");
	}

	@Test
	public void testDeleteLastLine() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with invalid Java (import after class declaration)
		assertSimpleFix(fixer, TOPIC, "delete_last_line");
	}

	@Test
	public void testDeleteLastLineBlankAbove() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input (`import A;` is not a resolvable import)
		assertSimpleFix(fixer, TOPIC, "delete_last_line_blank_above");
	}

	@Test
	public void testDeleteMiddleLine() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with non-Java content (`line1\nline2\nline3`)
		assertSimpleFix(fixer, TOPIC, "delete_middle_line");
	}

	@Test
	public void testDeleteOrphanedImportBlankAboveAndBelow() throws Exception {
		// can't migrate: fixer-internal line-deletion probe with synthetic non-parseable input (`import A;` is not a resolvable import)
		assertSimpleFix(fixer, TOPIC, "delete_orphaned_import_blank_above_and_below");
	}
}