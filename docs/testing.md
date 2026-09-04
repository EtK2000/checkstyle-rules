# Testing

## Architecture

### Check tests

Check tests are driven by `StandardCheckTests` / `StandardFixerCases`,
which expand each `ENTRIES` row into per-case dynamic tests. Every new
check should land in `ENTRIES`. Each check has:

- **Clean file** (`cases.clean.java`): valid code that must produce zero violations. For a
  fixer-backed check it doubles as a no-op assertion: `StandardCheckTests` runs the entry's own
  fixer over it to a fixed point and asserts it is left unchanged (clean for that check/fixer, not
  the whole pipeline). Put parsable, check-silent edge cases here rather than in
  `fragments.in.java` so this no-op check covers them.
- **Violation file(s)** (`cases.in.java`, plus `cases.<variant>.in.java` for sub-scenarios): code
  with known violations, each marked with `// violation: <description>` comments

`BaseCheckTest.runCheck()` is the underlying primitive that loads a
resource file, runs the check, and returns the raw violation list.
`StandardCheckTests` calls it internally via `assertCheckMatchesMarkers`,
which parses the file's `// violation:` markers (honouring `[minSdk>=N]`
and other predicates) and asserts an exact 1:1 match against the check's
output (count + line + severity + message). Do not call `runCheck()`
directly in a new test to assert violation absence under a different
property combination. Register that combination as another `ENTRIES`
row instead, so the marker pipeline runs against it too.

### Regex tests

Regex rules (RegexpSingleline/RegexpMultiline) use `BaseCheckTest.runRegexCheck()` or
`runRegexCheckInline()`. These test the regex pattern in isolation, not through the full
checkstyle.xml config. The format string must match the one in checkstyle.xml exactly.

### Fixer tests

Fixers are unit-tested by calling `fix()` directly with crafted input lines. Integration tests
create real Java files, run Checkstyle to detect violations, apply fixes, and assert the exact
full output. Integration tests are split across three classes:

- **`CheckstyleFixIntegrationTest`**: fix-producing tests. Every test must call
  `verifyFixedOutputClean` exactly once (enforced by `@AfterEach`). This re-checks the fixed
  output through the full pipeline to ensure no fixer produces output that still has fixable
  violations
- **`CheckstyleFixNoFixTest`**: pipeline tests where fixCount=0 (clean files, skipped violations,
  dry-run execution, warning-only violations)
- **`CheckstyleFixUtilTest`**: pure utility tests (hint message formatting, tab-column conversion,
  skip reason tracking) with no file I/O

## Reading fixtures

Fixture files are large (`jitinefficiency/cases.in.java` holds 465 cases in 6,180 lines), but
every case is delimited:

```java
// === case: <name> ===
...
// === end ===
```

`tools/slice` reads one case instead of the whole file. Reach for it before any Read of a fixture.

| Command | What it gives |
| ------- | ------------- |
| `tools/slice list` | every topic and its case count |
| `tools/slice list <topic>` | case names + line numbers in the topic's canonical input |
| `tools/slice list <topic> --all` | same, for every sliced file in the topic |
| `tools/slice show <topic> <name>` | the case from each sliced file, with line ranges |
| `tools/slice find <pattern>` | case names matching a regex, across all topics |

`show` is the one to use when changing a case: it prints the `in`, `out` and `fixed` versions
together, so a `.out` vs `.fixed` divergence is visible in one call rather than three file reads.
Both a topic name and a direct file path work as the first argument.

`find` answers "does a case for this exist already" without reading any fixture. Case names are
descriptive, so a pattern search over names usually locates prior coverage on its own.

`cases.clean.java` files carry no case markers, so `list` and `show` skip them; clean files are
whole-file fixtures by design.

## Tab-expanded columns

Checkstyle reports column numbers with tabs expanded to the configured `tabWidth`. This project
sets `tabWidth=4` (see `LineLength.TAB_WIDTH`), so a tab at position 0 makes the next character
report as column 4 instead of 1.

`CheckstyleFixAction.eventCharIndex()` converts a tab-expanded column to an index into the line's
chars, which is what `fix()` receives, in the pipeline and in the harness alike. A fixer matching
it against an AST position, which counts code points, converts it back with
`LineText.columnOfCharIndex`, which matters on any line containing a supplementary character. See
the `getColumnNo()` note in `adding-a-check.md` for the full rule.

**Diagnosis**: if a fixer works in unit tests (no tabs) but fails on real files (tabs), the column
conversion is the first thing to check. Write integration tests with tab-indented files to catch
this.

## Cross-check interference

The fix task runs ALL fixable checks simultaneously. Multiple checks can fire on the same code:

- `UpperEll` and `RedundantNumericSuffix` both fire on `100l` (lowercase L that's also redundant)
- `RedundantImport` and `UnusedImports` both fire on `import java.lang.String;`
- `FinalLocalVariable` fires twice on `int x, y;` (once per variable)

When two violations target the same line and one deletes/modifies it, the second violation would
operate on stale text. Bottom-to-top processing (sorting by line, then column, descending) handles
most cases, and an event on a line an earlier fix in the pass changed is mostly held back to the
next pass (see the second-pass triggers in `adding-a-fixer.md`).

**Integration tests must account for this.** Use inputs that isolate a single check. For example,
test UpperEll with `3000000000l` (exceeds int range, so RedundantNumericSuffix doesn't also fire)
rather than `100l`.

## RegexpMultiline violation reporting

`RegexpMultiline` violations report the line number where the match starts, which is the line
BEFORE the problematic content. For `NoDoubleBlankLines` with format `\n\s*\n\s*\n`, the violation
line is the last content line before the blank line group, not the blank lines themselves.

The `DoubleBlankLineFixer` accounts for this by scanning forward from `lineIndex + 1`.

## Coverage philosophy

**If it's not tested, it's not supported.** Never assume code works correctly without a test proving
it. "The code doesn't reference that token type so it can't fire" is not a valid argument. The only
proof is a test that runs the check against that input and asserts the expected result. If a check
interacts with a concept (init blocks, enums, records, lambdas, anonymous classes, etc.), there must
be a test for it, even if you believe the code path is unreachable. Beliefs are not tests.

## Coverage checklist

Use this as a **driving process while writing code**, not a post-hoc audit. Do not write a check,
then write a fixer, then write tests. Instead: write one branch of the check, write its tests, then
the next branch and its tests, then the fixer and its tests.

The detailed coverage rules live in `docs/testing/`, one file per concern, so each reader loads only
what its task needs. The steps below say which file to read at each point. **Reading the file a step
names is part of that step, not optional.**

| File | Read when | Sections |
| ---- | --------- | -------- |
| [testing/matrix.md](testing/matrix.md) | building or auditing a test matrix (steps 1-8) | Permutation matrix, Multi-item permutation coverage, Boundary pairing, Axis coverage, Context coverage, Expression form coverage, Annotation placement coverage, Numeric literal permutations, Comparison operator coverage, Single-arg vs multi-arg dispatch |
| [testing/fixers.md](testing/fixers.md) | writing, changing, or auditing a fixer or its tests (step 10) | Three-layer fixer coverage, Integration coverage, Fixer regex robustness, Negation insertion in fixers |
| [testing/resources.md](testing/resources.md) | adding or moving fixture cases, registering a check in `checkstyle.xml`, or testing checks that share token types | Cross-check testing, Cross-check impact of new checks, Test resource consolidation, Fragment-migration guard, File formatting in test resources |
| [testing/audit.md](testing/audit.md) | before declaring check/fixer work done, and when auditing coverage (steps 11-14) | Exhaustive audit process (the 6-step audit), Branch coverage, Severity coverage, Guard regression, Clean file coverage, Direct AST unit tests, AST structure awareness |

### Before writing any code (BLOCKING - do not skip or defer)

These steps produce the test matrix. No code is written until the matrix is complete and reviewed.
Read [testing/matrix.md](testing/matrix.md) first: it lists the dimensions the matrix must cover.

1. List every AST token type the check will visit and every expression/body type it will classify
2. If the check has categories (tiers, modes, severity levels), build the full NxN permutation
   matrix (see "Permutation matrix" in [testing/matrix.md](testing/matrix.md)). Write out every
   cell explicitly
3. For each category/token type, list: clean example, violation example, boundary example (nearby
   value that should NOT fire). Be specific - write actual code snippets, not descriptions
4. For each violation type (message key), repeat step 3
5. List every boundary dimension (what single change flips one category to another). For each
   dimension, write the clean-side snippet and the violation-side snippet side by side
6. If a token type cannot appear in test resource files (blocked by another check), note it as
   requiring a direct AST unit test
7. If the check has a related check that partitions contexts, list the cross-check file pairs
8. Write all of the above into the plan. Do not proceed to code until every cell, boundary, and
   token type has a planned test. This is the most important step. Gaps in the matrix become gaps in
   coverage. Filling the matrix after writing code is post-hoc rationalization, not test-driven
   development

### While writing code (interleaved - never batch)

9. Write ONE branch of the check. Immediately write the clean and violation test resource lines for
   that branch. Immediately write the check test assertions. Run `./gradlew check`. Only then write
   the next branch
10. After completing ALL check branches: write the fixer (read
    [testing/fixers.md](testing/fixers.md) first). For each `return null`, immediately write the
    fixer unit test. For each `return new FixResult(...)`, immediately write the fixer unit test.
    For each fix type, immediately write the integration test. Run `./gradlew check` after each test
11. Write direct AST unit tests for any token types identified in step 6 (see "Direct AST unit
    tests" in [testing/audit.md](testing/audit.md))

### After all code is written (exhaustive audit - do not skip)

12. Run the 6-step exhaustive audit (see "Exhaustive audit process" in
    [testing/audit.md](testing/audit.md)). This is not optional. Do not declare done, do not say
    "coverage is complete", do not move on until every step produces zero gaps
13. Trace every cell of the matrix from step 2 to a specific test method name. If any cell maps to
    "tested implicitly" or "same code path as X", that cell is NOT covered - add an explicit test
14. Verify every violation test asserts all three: exact line number, severity level, AND message
15. Run `./gradlew check` one final time

## Common mistakes

### Hardcoded value sets instead of parsing

A `Set.of("0", "0L", "0.0f", ...)` of default values misses `0.000`, `0x0`, `0_0`, `0.0e0`, and
many other valid zero representations. Parse the value instead: strip suffix, underscores and
prefix, then verify every remaining char is zero.

When matching against a set of known values, ask: "is this set exhaustive, or should I parse the
structure instead?"

### Missing lowercase `l` suffix

A suffix check covering `D/F/L/d/f` but not `l` looks correct at a glance. Boundary pairing is what
catches that class of omission: a test for `100l` beside the `100L` one.

### Method ordering in test resource files

Test resource files (.java) are checked by `checkstyleTestResources` which runs the project's own
checkstyle rules on them. Methods in test resources must be alphabetically ordered (per
`MethodAlphabeticalOrderCheck`), blank lines after `break;` are required before `case`/`default`
(per the `BlankLineAfterBreak` regex rule), and no blank lines after braced cases (per
`NoBlankLineBetweenSingleCasesCheck`).

Run `./gradlew check` (not just `test`) after any change to catch these.

### Violation comments

Violation comments in test resource files MUST use exact message text:
`// violation: <exact AuditEvent.getMessage() text>` for errors,
`// violation (warning): <exact message>` for warnings.
Never use shorthand, paraphrases, or descriptions. The comment IS the expected message.

- Clean files must have ZERO violation comments
- Violation files must have a comment on EVERY violation line
- No clean cases in violation files, no violation cases in clean files
- The slice `violations` sub-test (and the fixer sub-tests) strip `// violation:`
  markers, along with the whitespace before them, before running the check, so a
  marker never alters the check's own input. That lets a marker sit on a line the
  check actually reads: an otherwise-blank line for an internal-blank violation
  (`\n// violation: ...\n` strips back to a blank line), or a line already
  carrying a comment (`@B( // note // violation: ...` strips back to `@B( // note`)

### Assertion completeness

Every test assertion on a violation MUST check all three: line number, severity level, AND message.
Never assert just line + count. Never assert line without severity. Never skip message validation
on "obvious" cases. Every single violation gets all three checks, no exceptions.

## Parallel execution

Test classes run concurrently in one JVM (`build.gradle` turns on JUnit's parallel mode); the methods
of one class run in sequence, except the `@TestFactory` harnesses (`StandardCheckTests`,
`FullPipelineRegressionTest`, `CheckstyleFixIntegrationCasesTest`), whose dynamic tests also run
concurrently. A new test must not share mutable state across threads:

- **Timing**: a class that measures its own code (`assertScalesLinearly`, `assertTimeoutPreemptively`
  on real work) is `@Isolated`, so no other class competes for the CPU while it measures
- **JVM-wide state**: a class that swaps `Locale`, `System.out`/`System.err`, or resets
  `ReflectionUtil.classForNameCallCount` or its cache is `@Isolated`
- **Gradle `ProjectBuilder`**: hold `@ResourceLock(CheckstylePluginTest.PROJECT_BUILDER)`
- **Files**: write under `@TempDir` or `Files.createTempFile`, never a fixed path that another thread
  could read while it is being written
- **Caches**: per-thread (`ThreadLocal`, as `CheckerCache` and `FixerAst` do) or concurrent
  (`ConcurrentHashMap`)

### Scaling tests

`ScalingAssertions.assertScalesLinearly` builds each size's input once, outside the timing, and
repeats the timed run as often as the smallest size needs to take a measurable 20 ms, so every
doubling is asserted (under 3x, a quadratic takes 4x). Keep sizes small: checkstyle's own parse grows
quadratically in a long sibling list (`DetailAstImpl.addChild` walks to the last child), so a large
input spends seconds in setup without making the ratio any more telling. Any cache the code under
measure fills must be dropped at the start of each timed run, or later runs time the cache hit.

## Running tests

Always use `./gradlew check`, never a subset like `test` or `checkstyleMain checkstyleTest`. The
full `check` task includes `checkstyleTestResources` and `validatePlugins` which catch issues in
test resource files and plugin metadata that other tasks miss.