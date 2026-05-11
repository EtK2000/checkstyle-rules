package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class PreferExactAssertionCheckTest {
	private record Expected(int line, String message) {
	}

	private static final String DIR = "exactassertion/";
	private static final String MSG_PREFIX = "Use a dedicated assertion (e.g. 'assertEquals') instead of '";

	private static void assertClean(@Nonnull String fixture) throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferExactAssertionCheck.class, DIR + fixture).isEmpty());
	}

	private static void assertViolations(List<AuditEvent> got, Expected... expected) {
		assertEquals(expected.length, got.size());
		for (var i = 0; i < expected.length; ++i) {
			assertEquals(expected[i].line, got.get(i).getLine(), "line " + i);
			assertEquals(SeverityLevel.ERROR, got.get(i).getSeverityLevel(), "severity " + i);
			assertEquals(expected[i].message, got.get(i).getMessage(), "message " + i);
		}
	}

	private static Expected comparison(int line, String method, String op) {
		return new Expected(line, MSG_PREFIX + method + "' with '" + op + "'.");
	}

	private static Expected instanceofMsg(int line, String original, String replacement) {
		return new Expected(line, "Use '" + replacement + "' instead of '" + original + "' with 'instanceof'.");
	}

	@Nonnull
	private static List<AuditEvent> runCheckOnFiles(@Nonnull String... resourceSuffixes) throws Exception {
		final var checkConfig = new DefaultConfiguration(PreferExactAssertionCheck.class.getName());
		final var treeWalker = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalker.addChild(checkConfig);
		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalker);

		final var checker = new Checker();
		final var violations = new ArrayList<AuditEvent>();
		try {
			checker.setModuleClassLoader(PreferExactAssertionCheck.class.getClassLoader());
			checker.configure(checkerConfig);
			checker.addListener(new AuditListener() {
				@Override
				public void addError(@Nonnull AuditEvent event) {
					violations.add(event);
				}

				@Override
				public void addException(@Nonnull AuditEvent event, @Nonnull Throwable throwable) {
				}

				@Override
				public void auditFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void auditStarted(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileStarted(@Nonnull AuditEvent event) {
				}
			});

			final var files = new ArrayList<File>();
			for (var suffix : resourceSuffixes) {
				final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + suffix);
				files.add(new File(url.toURI()));
			}
			checker.process(files);
		}
		finally {
			checker.destroy();
		}
		return violations;
	}

	@Test
	public void testClean() throws Exception {
		assertClean("InputPreferExactAssertionClean.java");
	}

	@Test
	public void testJunit4Violations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionJunit4Violation.java"
		);
		assertViolations(
				violations,
				comparison(15, "assertFalse", "=="),
				comparison(19, "assertFalse", ">"),
				comparison(23, "assertTrue", "=="),
				comparison(27, "assertTrue", ">"),
				comparison(31, "assertTrue", "<"),
				comparison(35, "assertTrue", "!="),
				comparison(39, "assertFalse", ">="),
				comparison(43, "assertTrue", ">"),
				instanceofMsg(48, "assertFalse", "assertNotInstanceOf"),
				instanceofMsg(53, "assertTrue", "assertInstanceOf"),
				instanceofMsg(58, "assertTrue", "assertInstanceOf")
		);
	}

	@Test
	public void testJunit4WildcardClean() throws Exception {
		assertClean("InputPreferExactAssertionJunit4WildcardClean.java");
	}

	@Test
	public void testJunit4WildcardViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionJunit4WildcardViolation.java"
		);
		assertViolations(violations, comparison(8, "assertTrue", ">"));
	}

	@Test
	public void testJunit5Clean() throws Exception {
		assertClean("InputPreferExactAssertionJunit5Clean.java");
	}

	@Test
	public void testJunit5WildcardViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionJunit5WildcardViolation.java"
		);
		assertViolations(
				violations,
				comparison(12, "assertTrue", ">"),
				instanceofMsg(17, "assertTrue", "assertInstanceOf")
		);
	}

	@Test
	public void testMixedImportsClean() throws Exception {
		assertClean("InputPreferExactAssertionMixedImportsClean.java");
	}

	@Test
	public void testMixedImportsViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionMixedImportsViolation.java"
		);
		assertViolations(
				violations,
				comparison(15, "assertFalse", "=="),
				comparison(19, "assertTrue", ">"),
				instanceofMsg(24, "assertTrue", "assertInstanceOf"),
				instanceofMsg(29, "assertFalse", "assertNotInstanceOf"),
				instanceofMsg(34, "assertTrue", "assertInstanceOf")
		);
	}

	@Test
	public void testNonStaticJ4WithJ5StaticClean() throws Exception {
		assertClean("InputPreferExactAssertionNonStaticJ4WithJ5StaticClean.java");
	}

	@Test
	public void testNonStaticJ4WithJ5StaticViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionNonStaticJ4WithJ5StaticViolation.java"
		);
		assertViolations(violations, instanceofMsg(15, "assertTrue", "assertInstanceOf"));
	}

	@Test
	public void testNoStaticImportClean() throws Exception {
		assertClean("InputPreferExactAssertionNoStaticImportClean.java");
	}

	@Test
	public void testSpecificApiCleanCrossCheck() throws Exception {
		assertViolations(
				BaseCheckTest.runCheck(PreferExactAssertionCheck.class, "specificapi/InputSpecificApiClean.java"),
				comparison(33, "assertFalse", "=="),
				comparison(77, "assertTrue", "==")
		);
	}

	@Test
	public void testSpecificApiViolationCrossCheck() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				"specificapi/InputSpecificApiAssertViolation.java"
		).isEmpty());
	}

	@Test
	public void testStateResetBetweenFiles() throws Exception {
		final var violations = runCheckOnFiles(
				DIR + "InputPreferExactAssertionJunit4WildcardViolation.java",
				DIR + "InputPreferExactAssertionJunit5WildcardViolation.java"
		);
		assertViolations(
				violations,
				comparison(8, "assertTrue", ">"),
				comparison(12, "assertTrue", ">"),
				instanceofMsg(17, "assertTrue", "assertInstanceOf")
		);
	}

	@Test
	public void testTypeWildcardClean() throws Exception {
		assertClean("InputPreferExactAssertionTypeWildcardClean.java");
	}

	@Test
	public void testTypeWildcardViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				DIR + "InputPreferExactAssertionTypeWildcardViolation.java"
		);
		assertViolations(
				violations,
				comparison(14, "assertTrue", ">"),
				comparison(18, "assertTrue", ">"),
				instanceofMsg(23, "assertTrue", "assertInstanceOf")
		);
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferExactAssertionCheck.class, DIR + "InputPreferExactAssertionViolation.java");
		assertViolations(
				violations,
				comparison(17, "assertFalse", "=="),
				comparison(21, "assertFalse", ">="),
				comparison(25, "assertFalse", ">"),
				instanceofMsg(30, "assertFalse", "assertNotInstanceOf"),
				instanceofMsg(35, "assertFalse", "assertNotInstanceOf"),
				instanceofMsg(40, "assertFalse", "assertInstanceOf"),
				instanceofMsg(45, "assertFalse", "assertNotInstanceOf"),
				instanceofMsg(50, "assertFalse", "assertNotInstanceOf"),
				comparison(54, "assertFalse", "<="),
				comparison(58, "assertFalse", "<"),
				comparison(62, "assertFalse", "!="),
				instanceofMsg(67, "assertTrue", "assertInstanceOf"),
				comparison(71, "assertTrue", "=="),
				comparison(75, "assertTrue", ">="),
				comparison(79, "assertTrue", ">"),
				instanceofMsg(84, "assertTrue", "assertInstanceOf"),
				instanceofMsg(89, "assertTrue", "assertInstanceOf"),
				instanceofMsg(94, "assertTrue", "assertNotInstanceOf"),
				instanceofMsg(99, "assertTrue", "assertInstanceOf"),
				instanceofMsg(104, "assertTrue", "assertInstanceOf"),
				comparison(108, "assertTrue", "<="),
				comparison(112, "assertTrue", "<"),
				comparison(116, "assertTrue", "!="),
				comparison(120, "assertTrue", ">"),
				instanceofMsg(125, "assertTrue", "assertInstanceOf"),
				comparison(129, "assertFalse", ">"),
				comparison(133, "assertTrue", ">"),
				comparison(137, "assertFalse", ">"),
				comparison(141, "assertTrue", ">"),
				instanceofMsg(146, "assertTrue", "assertInstanceOf"),
				comparison(150, "assertFalse", ">="),
				comparison(154, "assertTrue", ">"),
				instanceofMsg(159, "assertTrue", "assertInstanceOf")
		);
	}
}