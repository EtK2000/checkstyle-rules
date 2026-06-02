package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

class PreferCollectionInterfaceFixerTest {
	private static final PreferCollectionInterfaceFixer FIXER = new PreferCollectionInterfaceFixer();
	private static final String TOPIC = "prefercollectioninterface";

	private static void assertSkipReason(String snippet) throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, snippet);
		final var t = fx.firstTarget();
		final var result = assertInstanceOf(
				SkipResult.class,
				FIXER.fix(new ArrayList<>(fx.inputLines()), t.line(), t.column())
		);
		assertEquals(SkipMessages.COLLECTION_INTERFACE_SKIP, result.reason());
	}

	@Test
	public void testLinkedListSkipped() throws Exception {
		assertSkipReason("linked_list_skipped");
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

	@Test
	public void testNoMatch() throws Exception {
		assertSkipReason("no_match");
	}

	@Test
	public void testPackageAndStaticImportResolved() throws Exception {
		assertSimpleFix(FIXER, TOPIC, "package_and_static_import_resolved", Set.of("java.util.List"));
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
}