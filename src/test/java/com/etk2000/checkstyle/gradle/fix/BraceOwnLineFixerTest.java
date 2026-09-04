package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.BraceOwnLineCheck;

import org.junit.jupiter.api.Test;

public class BraceOwnLineFixerTest {
	private static final String TOPIC = "braceownline";

	private final CheckstyleFixer fixer = new BraceOwnLineFixer();

	@Test
	public void testCheckReleasesFixerCachesAfterAFile() throws Exception {
		FixerAst.clearCache();
		BaseCheckTest.runCheck(BraceOwnLineCheck.class, "braceownline/cases.clean.java");
		final var cachedMask = FixerAst.class.getDeclaredField("CACHED_MASK");
		cachedMask.setAccessible(true);
		assertNull(((ThreadLocal<?>) cachedMask.get(null)).get());
	}

	@Test
	public void testStalePositionNoSiteRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "stale_position_no_site_refused", "cannot move the opening brace: an earlier fix in this pass moved it");
	}

	@Test
	public void testStalePositionRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "stale_position_refused", "cannot move the opening brace: an earlier fix in this pass moved it");
	}

	@Test
	public void testUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "unparseable_buffer_refused", "cannot move the opening brace: it cannot be located without a parse");
	}
}