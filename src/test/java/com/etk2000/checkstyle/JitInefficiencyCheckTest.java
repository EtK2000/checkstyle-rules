package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import org.junit.jupiter.api.Test;

import java.io.File;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Direct tests for {@link JitInefficiencyCheck#categorizeAt}. Its {@code null} return is
 * unreachable from the fixer's own fixtures: every slice reports a real violation, and every
 * fragment fails to parse before {@code categorizeAt} is called at all.
 */
public class JitInefficiencyCheckTest {
	private static final String CLEAN_FIXTURE = "jitinefficiency/cases.clean.java";
	private static final String VIOLATION_FIXTURE = "jitinefficiency/cases.in.java";

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture(@Nonnull String name) throws Exception {
		final var url = requireNonNull(
				JitInefficiencyCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + name),
				"Test input file not found: " + name
		);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	/**
	 * The positive control for {@link #categorizeAtReturnsNullWhereNoNodeIsDetectable}: without it a
	 * {@code findNodeAt} regression that answered null everywhere would pass that test.
	 */
	@Test
	public void categorizeAtFindsACategorySomewhereInTheViolationFixture() throws Exception {
		final var root = parseFixture(VIOLATION_FIXTURE);
		JitInefficiencyCategory found = null;
		for (var line = 0; line < 200 && found == null; ++line) {
			for (var column = 0; column < 120 && found == null; ++column)
				found = JitInefficiencyCheck.categorizeAt(root, line, column);
		}
		assertNotNull(found);
	}

	/**
	 * A sweep rather than one position: probing {@code (0, 0)} in a clean file lands on the
	 * {@code package} line, which answers null for the same reason it does in a file full of
	 * violations, so it would not distinguish the two fixtures at all.
	 */
	@Test
	public void categorizeAtReturnsNullEverywhereOnACleanFixture() throws Exception {
		final var root = parseFixture(CLEAN_FIXTURE);
		for (var line = 0; line < 200; ++line) {
			for (var column = 0; column < 120; ++column)
				assertNull(JitInefficiencyCheck.categorizeAt(root, line, column));
		}
	}

	@Test
	public void categorizeAtReturnsNullWhereNoNodeIsDetectable() throws Exception {
		assertNull(JitInefficiencyCheck.categorizeAt(parseFixture(VIOLATION_FIXTURE), 0, 0));
	}
}