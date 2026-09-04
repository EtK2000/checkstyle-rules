# Test resources and cross-check rules

Part of the [testing guide](../testing.md). Read this when adding or moving fixture cases
(`cases.*.java`, `fragments.*.java`), registering a new check in `checkstyle.xml`, or testing checks
that share token types.

## Cross-check testing (for related checks that partition contexts)

When two or more checks divide responsibility over the same token types (e.g. one handles stacked
annotations on declarations, another handles inline annotations on parameters), run every test
resource file from each check against ALL related checks. Every file must produce zero violations
from the other check. This catches context-detection bugs where one check fires in the other's
territory.

```java
// AnnotationOwnLineCheck must not fire on any SameLine test files
assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "cases.clean.java").isEmpty());
assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "cases.in.java").isEmpty());
// ... and vice versa for every file
```

## Cross-check impact of new checks

When registering a new check or extending an existing one in `checkstyle.xml`, it immediately
fires on ALL source files AND test resources via `checkstyleTestResources`. Before running
`./gradlew check`:

1. Search existing test resources for patterns your check would flag (e.g. `grep -r
   "Arrays.asList"` for a new Arrays.asList check)
2. Add cross-check suppressions in `suppressions-test-resources.xml` for test directories where
   the pattern appears legitimately
3. Check if your new detection overlaps with an existing detection on the same code pattern
   (e.g. `Arrays.asList` inside `Collections.unmodifiableList` is caught by both the copyOf check
   and the Arrays.asList check, need parent-chain exclusion to prevent double-firing)

## Test resource consolidation

Never create a violation file with only one violation. Group related violations into shared files by
rule category (e.g. all stream simplifications in one file, all Collections factory replacements in
one file). This keeps the test suite manageable and makes it obvious when a rule fires on the wrong
input.

When violations in the same file have different minSdk gates, the minSdk boundary tests must account
for the ungated violations still firing. For example, a file with both `stream().count()` (no gate)
and `stream().forEach()` (API 24+) should expect 2 violations at minSdk 23 (only forEach suppressed)
and 3 at minSdk 24 (all fire).

## Fragment-migration guard

`fragments.in.java` snippets are the fallback fixture form: line fragments fed to a fixer's
`fix()` at an explicit `// target:` position, used only when a case genuinely cannot be a
`cases.{in,out}.java` slice. `FragmentMigrationGuardTest` (via `FragmentMigrationDetector`) keeps
the corpus honest: it wraps each fragment body in a minimal compilation unit (a bare
`class {...}`, a method body, a constructor body, or a class with an appended `int __x;` so a body
ending in a bare annotation parses), runs the topic's check, and flags any case that behaves like a
slice: the check fires and the fixer either reproduces `.out` as a fixed point (a fix-slice) or
returns a `SkipResult` at the reported site (a `// skip-reason:` skip-slice). It also flags a
`// target:` that is *redundant* (points at a position the check already reports): the directive is
only justified for a synthetic position the check never reports.

The guard fails on any flagged case: migrate it to a slice, or delete it if an existing slice
already covers the behavior.

A case is legitimately fragment-only (and correctly NOT flagged) when it is:

- **non-compilable**: a body no wrapper can parse (`@B(` at EOF, a bare `value = x;`);
- **synthetic-target**: the `// target:` is an out-of-bounds/negative/past-end column or line the
  check never reports, exercising the fixer's positional robustness at a site a slice (which
  derives the position from the check) cannot reproduce;
- **null-returning**: the fixer returns bare `null` rather than a `SkipResult`; `assertCaseSkip`
  requires a `SkipResult` and `assertCaseFix` would leave the violation unfixed, so neither slice
  form can express it;
- **style-invariant**: the body trips a non-suppressible formatting invariant (`NoSpaceIndent`,
  `NoTrailingWhitespace`) that a linted slice cannot carry and the migration policy refuses to
  suppress.

To migrate a flagged case: author the `cases.in.java`/`cases.out.java` slice (a `// skip-reason:`
slice for a skip case, `.out == .in` minus markers), add a per-topic cross-check suppression if a
non-topic check fires on it, then delete the fragment (both `.in` and `.out` entries) and its
dedicated fixer-test method. If the flagged case is already covered by an existing slice (the
fragment is a leftover duplicate), delete it outright instead of creating a second copy.

## File formatting in test resources

New test resource files must comply with the project's formatting rules:

- No trailing newline at end of file (use `perl -pi -e 'chomp if eof'` after creating)
- Methods alphabetically ordered
- TAB indentation, no spaces
- LF line endings
- No unused imports
- Inner classes at top of class body (per ClassStructureOrderCheck)