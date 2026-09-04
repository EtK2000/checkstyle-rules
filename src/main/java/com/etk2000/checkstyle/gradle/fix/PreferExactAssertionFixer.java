package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.PreferExactAssertionCheck.ASSERT_FQCN;
import static com.etk2000.checkstyle.PreferExactAssertionCheck.ASSERTIONS_FQCN;
import static com.etk2000.checkstyle.PreferExactAssertionCheck.isJunitAssertFqcn;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferExactAssertionCheck;
import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionRefusal;
import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionRule;
import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionTarget;
import com.etk2000.checkstyle.PreferExactAssertionCheck.CannotRewrite;
import com.etk2000.checkstyle.PreferExactAssertionCheck.Rewritten;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Rewrites a reported {@code assertTrue}/{@code assertFalse} call to the exact assertion
 * {@link PreferExactAssertionCheck} named for it. The call's shape, the span to replace and the
 * replacement text all come from the check, so neither side re-derives the decision; what is left here
 * is the part the check has no business knowing about, which is the file's static imports: whether the
 * replacement method is already in scope, and which class to import it from.
 */
class PreferExactAssertionFixer implements CheckstyleFixer {
	/** What a scan of the file's static imports concluded. */
	private enum ImportOutcome {
		ADDED,
		ALREADY_IN_SCOPE,
		UNRESOLVED
	}

	/**
	 * What one parse of the buffer yields: the assertions reported at the position, and the static
	 * imports in scope. Both come from the same root so they cannot describe different buffers.
	 */
	private record Located(
			@Nonnull List<AssertionTarget> targets,
			@Nonnull List<String> staticImportFqns
	) {}

	/**
	 * Canonical JUnit assertion method names. Excluding AssertJ ({@code assertThat}) and Truth
	 * ({@code assertWithMessage}) which would otherwise match a generic {@code startsWith("assert")}
	 * test.
	 */
	private static final Set<String> JUNIT_ASSERT_METHODS = Set.of(
			"assertAll", "assertArrayEquals", "assertDoesNotThrow", "assertEquals",
			"assertFalse", "assertInstanceOf", "assertIterableEquals", "assertLinesMatch",
			"assertNotEquals", "assertNotInstanceOf", "assertNotNull", "assertNotSame",
			"assertNull", "assertSame", "assertThrows", "assertTimeout",
			"assertTimeoutPreemptively", "assertTrue", "fail"
	);

	/**
	 * Scans existing static imports for one from {@code org.junit.jupiter.api.Assertions} and adds
	 * the replacement method's static import to {@code imports}. JUnit 4's {@code Assert} doesn't
	 * have {@code assertInstanceOf}, so adding such an import there would produce a
	 * non-compiling result.
	 */
	private static ImportOutcome addAssertImport(
			@Nonnull List<String> staticImportFqns,
			@Nonnull String replacementMethod,
			@Nonnull Set<String> imports
	) {
		for (var fqn : staticImportFqns) {
			final var classFqn = PreferExactAssertionCheck.staticImportClassFqn(fqn);
			if (!ASSERTIONS_FQCN.equals(classFqn))
				continue;

			final var methodName = PreferExactAssertionCheck.staticImportMember(fqn);
			if (methodName == null)
				return ImportOutcome.ALREADY_IN_SCOPE;
			if (!JUNIT_ASSERT_METHODS.contains(methodName))
				continue;
			imports.add("static " + classFqn + "." + replacementMethod);
			return ImportOutcome.ADDED;
		}
		return ImportOutcome.UNRESOLVED;
	}

	/**
	 * Companion to {@link #addAssertImport} for the negation rewrite. Adds a static import
	 * for {@code oppositeMethod} (either {@code assertTrue} or {@code assertFalse}) drawn
	 * from the same class as the existing {@code assertTrue}/{@code assertFalse} static
	 * import. Unlike {@link #addAssertImport}, this accepts both {@code org.junit.Assert}
	 * (JUnit 4) and {@code org.junit.jupiter.api.Assertions} (JUnit 5) because both classes
	 * have both methods.
	 */
	private static ImportOutcome addOppositeAssertImport(
			@Nonnull List<String> staticImportFqns,
			@Nonnull String oppositeMethod,
			@Nonnull Set<String> imports
	) {
		String inferredClassFqn = null;
		for (var fqn : staticImportFqns) {
			final var classFqn = PreferExactAssertionCheck.staticImportClassFqn(fqn);
			if (!isJunitAssertFqcn(classFqn))
				continue;

			final var methodName = PreferExactAssertionCheck.staticImportMember(fqn);
			if (methodName == null || oppositeMethod.equals(methodName))
				return ImportOutcome.ALREADY_IN_SCOPE;
			if (!JUNIT_ASSERT_METHODS.contains(methodName))
				continue;
			if (inferredClassFqn == null)
				inferredClassFqn = classFqn;
		}
		if (inferredClassFqn == null)
			return ImportOutcome.UNRESOLVED;
		imports.add("static " + inferredClassFqn + "." + oppositeMethod);
		return ImportOutcome.ADDED;
	}

	@CheckReturnValue
	private static boolean hasAssertionsImport(@Nonnull List<String> staticImportFqns) {
		return hasStaticImportOfClass(staticImportFqns, ASSERTIONS_FQCN::equals);
	}

	@CheckReturnValue
	private static boolean hasJunit4AssertImport(@Nonnull List<String> staticImportFqns) {
		return hasStaticImportOfClass(staticImportFqns, ASSERT_FQCN::equals);
	}

	@CheckReturnValue
	private static boolean hasStaticImportOfClass(
			@Nonnull List<String> staticImportFqns,
			@Nonnull Predicate<String> classMatcher
	) {
		for (var fqn : staticImportFqns) {
			final var classFqn = PreferExactAssertionCheck.staticImportClassFqn(fqn);
			if (classFqn == null)
				continue;
			if (classMatcher.test(classFqn))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String skipMessageFor(@Nonnull AssertionRefusal refusal) {
		return switch (refusal) {
			case ANNOTATED_TYPE -> SkipMessages.PREFER_ASSERT_SKIP_ANNOTATED_TYPE;
			case ARITY -> SkipMessages.PREFER_ASSERT_SKIP_ARITY;
			case COMPARISON -> SkipMessages.PREFER_ASSERT_SKIP_COMPARISON;
			case GENERIC_TYPE -> SkipMessages.PREFER_ASSERT_SKIP_GENERIC_TYPE;
			case RECORD_PATTERN -> SkipMessages.PREFER_ASSERT_SKIP_RECORD_PATTERN;
			case UNSLICEABLE -> SkipMessages.PREFER_ASSERT_SKIP_UNSLICEABLE;
		};
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		// the pipeline guards this centrally, but a position straight from a synthetic fragment target
		// does not go through it, and a throw here reads as a fixer bug rather than an out-of-range line
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		final var located = FixerAst.withAst(
				lines,
				root -> new Located(
						PreferExactAssertionCheck.locateAt(root, lines, lineIndex, column),
						PreferExactAssertionCheck.staticImportFqns(root)
				)
		);
		// a buffer an earlier fix in the same pass left unparseable, or a position no reported assertion
		// starts at any more. Neither is a recognized violation, so there is no skip reason to name
		if (located == null || located.targets().isEmpty())
			return null;
		final var targets = located.targets();
		// the pipeline calls the fixer once per event and passes nothing saying which of them this is,
		// so a position carrying two rules cannot be acted on without guessing which was reported
		if (targets.size() > 1)
			return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP_MULTI_TARGET);

		final var target = targets.getFirst();
		final var span = PreferExactAssertionCheck.spanOf(lines, target);
		final var rewrite = PreferExactAssertionCheck.rewriteOf(lines, target);
		if (rewrite instanceof CannotRewrite refused)
			return new SkipResult(skipMessageFor(refused.reason()));
		if (span == null || !(rewrite instanceof Rewritten rewritten))
			return new SkipResult(skipMessageFor(AssertionRefusal.UNSLICEABLE));

		final var imports = new TreeSet<String>();
		// a qualified call names its class on the rewritten line, so nothing has to come into scope
		if (target.qualifierSimpleName() == null) {
			final var staticImports = located.staticImportFqns();
			final ImportOutcome outcome;
			if (target.rule() == AssertionRule.NEGATION) {
				// both Assert and Assertions have assertTrue/assertFalse, so either framework resolves the
				// swap; with both statically imported there is no telling which one does, and the import
				// this would add could be the one that loses
				if (hasAssertionsImport(staticImports) && hasJunit4AssertImport(staticImports))
					return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP_IMPORT_AMBIGUOUS);
				outcome = addOppositeAssertImport(staticImports, target.replacementMethod(), imports);
			}
			else
				outcome = addAssertImport(staticImports, target.replacementMethod(), imports);
			// emitting the rewrite anyway would call a method nothing imports, so the file would stop
			// compiling; leaving the violation reported costs only that it stays reported
			if (outcome == ImportOutcome.UNRESOLVED)
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP_IMPORT_UNRESOLVED);
		}

		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var prefix = edges.prefix();
		final var indent = edges.indent();
		final var suffix = edges.suffix();

		final var emitted = rewritten.call().split("\n", -1);
		final var replacement = new ArrayList<String>(emitted.length + 2);
		final var hoists = PreferExactAssertionCheck.hoistsLeadingText(target);
		final var leading = hoists ? prefix.substring(indent.length()).stripTrailing() : "";
		if (!leading.isEmpty())
			replacement.add(indent + leading);
		// `spanOf` already absorbed a suffix that is only a comment, so whatever is left here is code,
		// and it is a sibling statement in exactly the cases where `leading` is one
		final var trailing = hoists ? suffix.strip() : "";
		final var tail = trailing.isEmpty() ? suffix : "";
		final var comment = SpanEdges.appendableAfter(rewritten.comment(), tail);
		for (var i = 0; i < emitted.length; ++i) {
			// a continuation line is an argument's own content carried over verbatim, indentation included
			final var head = i == 0 ? (hoists ? indent : prefix) : "";
			replacement.add(i == emitted.length - 1
					? head + emitted[i] + tail + (comment.isEmpty() ? "" : " " + comment)
					: head + emitted[i]
			);
		}
		if (!trailing.isEmpty())
			replacement.add(indent + trailing);
		return imports.isEmpty()
				? new FixResult(span.start().line(), span.end().line(), replacement)
				: new FixResult(span.start().line(), span.end().line(), replacement, imports);
	}
}