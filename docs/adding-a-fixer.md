# Adding a New Fixable Check

How to add auto-fix support for a checkstyle rule to the `checkstyleFix` task.

## Steps

### 1. Create the fixer class

In `src/main/java/com/etk2000/checkstyle/gradle/fix/`, create a package-private class
implementing `CheckstyleFixer`:

```java
class MyFixer implements CheckstyleFixer {
    @Nullable
    @Override
    public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
        // lineIndex: 0-based line in the file
        // column: 0-based CHARACTER index (already converted from tab-expanded)
        // return null if the fix can't be applied (safety guard)
        // return FixResult(startLine, endLine, replacement) to replace lines[start..end]
    }
}
```

The `column` parameter is a character index, not a tab-expanded column. The conversion happens
in `applyFixes()` before calling the fixer. Don't do your own tab conversion.

**Adding imports**: if the fix introduces a new type reference (e.g., replacing
`Collections.emptyList()` with `List.of()`), use the 4-arg constructor to request the import:

```java
return new FixResult(lineIndex, lineIndex, List.of(fixed), Set.of("java.util.List"));
```

The `importsToAdd` set contains fully qualified class names. `applyFixes()` inserts them in
sorted position within the correct package group after all line fixes are applied. If the
import already exists, it is skipped. When imports are added, `applyFixes()` signals
`needsSecondPass = true` so the fix task runs a second pass to clean up cascading violations
(e.g., the old import becoming unused).

If the fix doesn't need imports, use the 3-arg constructor (defaults to empty set):

```java
return new FixResult(lineIndex, lineIndex, List.of(fixed));
```

**Second-pass cascade triggers**: after the per-violation fix loop, `applyFixes()` may signal `needsSecondPass = true`:

| Trigger | Behavior | Limitations |
| --- | --- | --- |
| Fixer adds an import (`importsToAdd` non-empty, insertion occurred) | Insert missing imports; flip `needsSecondPass` | None |
| Fixer removes the last usage of an imported short name | Flip `needsSecondPass` so `UnusedImportsCheck` runs in pass 2 and `DeleteLineFixer` removes the dead line | Heuristic word-boundary scan. Skips wildcard imports (`*` and `static ...*`) and default-package imports (no `.` in FQN). Pre-existing unused imports (no fixer involvement) are handled by a direct `UnusedImportsCheck` run instead. Identifiers used only inside Javadoc `@link`/`@see` may be flagged as unused (a harmless extra pass 2). Text blocks are skipped like comments; surrogate-pair identifiers are not supported. |

### 2. Register the fixer

**For TreeWalker checks** (most checks): add to `FIXERS` in `CheckstyleFixTask`:

```java
Map.entry(MyCheck.class.getName(), new MyFixer()),
```

The check class must be importable (it's a `compileOnly` dependency). The key is the fully
qualified class name, which matches `AuditEvent.getSourceName()`.

**For Checker-level modules** (RegexpSingleline, RegexpMultiline): add to `MODULE_ID_FIXERS`
with a string ID, then configure the module in the `fix()` method:

```java
// in MODULE_ID_FIXERS
Map.entry("MyModuleId", new MyFixer())

// in fix() task action
final var config = new DefaultConfiguration("RegexpSingleline");
config.addProperty("id", "MyModuleId");
config.addProperty("format", "...");
config.addProperty("message", "...");
checkerConfig.addChild(config);
```

The key is the module ID (set via the `id` property), which matches
`AuditEvent.getModuleId()`.

### 3. Configure check properties

If the check needs properties (like `FinalLocalVariable` needs
`validateEnhancedForLoopVariable=false`), add them in the `fix()` method where the check
config is built:

```java
if (checkName.equals(MyCheck.class.getName()))
    checkConfig.addProperty("propertyName", "value");
```

### 4. Write unit tests

There are two layers of fixer unit tests, with different homes:

**Per-slice fix tests** (one `fix()` call produces a fully fixed slice): add
`// === case: NAME ===` slices to `cases.in.java` and `cases.out.java` under
your topic directory. **Always** flip the check's `StandardCheckTests.ENTRIES`
row to `hasFixer=true`; the auto-pipeline picks every slice up as
`<CheckClass> > NAME > fix` (and `> imports-unchanged` for slices the check
is gated off on under a registered variant). No `@Test` methods to write.
Do not author per-slice `assertCaseFix` calls in a dedicated `XxxFixerTest`
when the check is in `ENTRIES` — that duplicates what the auto-pipeline
already runs, and drifts silently as you add new slices.

When the fixer adds imports, list them as `// imports: <fqcn>` directives in
the Fixed slice (but not the Violation slice); `assertCaseFix` derives the
expected `importsToAdd` from the slice diff. A `// imports:` value that is a
full import line (it begins with `import ` or with a leading block comment
`/*`) is emitted verbatim rather than wrapped, so a slice can carry an import
line bearing a comment (`// imports: import foo.Foo; // note`,
`// imports: /* legacy */ import foo.Foo;`) that a real import line in the
linted `cases.*.java` file could not. When a slice needs its own package
context (a check that only fires on a package-qualified reference, e.g. a
`pkg.Type.FIELD` cinit LHS), declare it with a `// package: <name>` directive
at the top of the slice: it stays a comment in the physical file (which keeps
its single file-level package and still parses as one unit) and expands to a
real `package <name>;` line in the slice's input, replacing the file-level
package for that slice. When the check is `minSdk`-gated,
register every variant (gated-on AND gated-off) in `ENTRIES` and add the
matching predicate (`// violation [minSdk>=N]: ...`) to every marker in
`cases.in.java` — see "MinSdk-gated checks: variants and marker predicates"
in `docs/testing.md`. The gated-off variant's `cases.out.<variant>.java`
must NOT carry `// imports:` directives the input doesn't already have;
the auto-emitted `imports-unchanged` dynamic test enforces this.

**Fragment / skip / boundary tests** (no compiled Java context, deliberately
out-of-bounds inputs, or `SkipResult` reason assertions): add them to
`fragments.in.java` / `fragments.out.java` and write explicit `@Test`s in
`MyFixerTest.java` using `assertSimpleFix`, `assertSkip`, or
`assertSkipResult`. These tests stay in the dedicated `MyFixerTest` class
even when the check is in `ENTRIES`, since the auto-pipeline can't express
them.

Cover, across both layers:

- Every `return null` guard with a dedicated test
- Every successful fix path
- For every accepted value, write a rejection test with a nearby invalid value
  (boundary pairing)
- If the fixer handles multiple dimensions (e.g., different suffixes,
  prefixes), test each axis

### 5. Write integration tests

Fix-producing integration tests go in `CheckstyleFixIntegrationTest`. This class has an
`@AfterEach` that asserts `verifyFixedOutputClean` was called exactly once per test, so
every fix test automatically re-checks its output for remaining fixable violations.

Use `assertFullFix(caseName, fixCount, needsSecondPass)` for most tests. The helper loads
the fixture from a slice, runs the pipeline, asserts (content, fixCount, needsSecondPass),
and calls `verifyFixedOutputClean` internally:

```java
@Test
public void testMyFix() throws Exception {
  assertFullFix("my_fix", 1, false);
}
```

The case `my_fix` must be present in BOTH
`src/test/resources/com/etk2000/checkstyle/inputs/integration/fragments.in.java` (input)
AND `fragments.out.java` (expected post-pipeline output), inside `// === case: my_fix ===`
/ `// === end ===` markers. Inline `Files.writeString(file, "class T { ... }")` is blocked
by `NoInlineJavaSourceTest`.

Assert the **exact full output** via the slice's `Fixed` lines (the helper does this). Assert
`fixCount` to catch accidental extra or missing fixes. Assert `needsSecondPass` to verify the
fixer correctly signals whether imports were added. Use inputs that only trigger the check
being tested (avoid cross-check interference). Don't include imports that would become unused
after fixing (Checkstyle is AST-based, so the check fires even without the import).

If the fixer changes a class qualifier (e.g. `Collections.sort` to `list.sort`), also write
a multi-pass test using `assertFullFixMultiPass(caseName, pass1FixCount, pass1NeedsSecondPass,
pass2FixCount, pass2NeedsSecondPass)` with the import present in the input slice to verify
the unused import gets cleaned up on the second pass.

If your new fixer collapses scaffolding that other fixers' `cases.in.java` files use
(e.g. you simplify a syntactic form they rely on for context), `FullPipelineRegressionTest`
will start producing output that differs from those checks' `cases.out.java`. The fix is
NOT to weaken the test. Drop a per-case override slice into the sibling topic's
`cases.*.fixed.java` documenting the post-pipeline result. See "Per-case pipeline-output
overrides" in `docs/testing.md`.

If the check is a Checker-level module, also configure it in `runChecks()`.

No-fix tests (clean files, skipped violations, warning-only) go in `CheckstyleFixNoFixTest`.
Pure utility tests (hint messages, tab-column math) go in `CheckstyleFixUtilTest`.

Your topic's `cases.clean.java` already gets a free no-op assertion once the entry is
`hasFixer=true`: `StandardCheckTests`'s `clean` dynamic runs your fixer over it to a fixed point
and fails if it changes anything (the file is clean for your check/fixer, so your fixer must do
nothing). Prefer adding a parsable, check-silent edge case there over a `fragments.in.java`
entry, so this no-op guard covers it.

### 6. Update README

Add the check to the fixable checks table in `README.md`.

### 7. Verify

Run `./gradlew check`. This runs all tests, checkstyle on main code, test code, AND test
resources.

## Reusable text/span utilities

Text-based fixers should build on these shared helpers (in `com.etk2000.checkstyle` and
`com.etk2000.checkstyle.format`) rather than re-parsing lines from scratch:

- **`JavaLineScanner`**: literal/comment-aware lexer for a physical line (tracks string, char,
  block-comment, and text-block state), so a structural char inside a literal or comment is never
  mistaken for source. Use `matchingCloseParen` and its siblings to mask a line before indexing.
- **`LexerState`** (record on `JavaLineScanner`): the block-comment / text-block state carried into
  a line, letting a multi-line fixer fold each line's real entry state from the preceding lines.
- **`LineText`**: stateless single-line helpers (e.g. indentation extraction) shared across fixers.
- **`SpanReformat`**: shared vocabulary and text primitives for the two span reformatters below
  (slice a contiguous span at AST token boundaries, collapse a segment onto one line, tight-join).
- **`JavaArgListReformatter`**: re-lays-out a multi-line call/definition argument list (collapse
  onto one line when it fits the max width, else one argument per line at the continuation indent).
- **`JavaTernaryReformatter`**: re-lays-out a multi-line ternary call argument into the canonical
  shape (condition on the `(` line, `?`/`:` each on their own line, `)` on its own line).
- **`JavaSpanReindenter`**: re-indents an already-line-broken span to the project's canonical
  indentation (the caller decides the line breaks; this fixes only leading indentation).

Grep the class for its exact API before use; several fixers (e.g. `MultilineCallFormattingFixer`,
`RedundantAnnotationSyntaxFixer`) already compose them.

## Messages and skip reasons

Check violation messages live in `src/main/resources/com/etk2000/checkstyle/messages.properties`.
Fixer skip-reason strings (the `SkipResult` reasons) are the same file's `*.skip*` keys, referenced
through the constants in `src/main/java/com/etk2000/checkstyle/gradle/fix/SkipMessages.java`.
`docs/coverage/<check>.md` describes in plain terms *why* a pattern is skipped or not fixed; look
here for the exact message key behind a given reason.

## Common pitfalls

- **Column-awareness**: if the check can fire multiple times on the same line (e.g., two
  `.get(0)` calls), the fixer MUST use the `column` parameter to target the correct occurrence.
  Use `line.indexOf(pattern, column)` or validate that the pattern exists at the expected column,
  not `line.indexOf(pattern)` from position 0. If the check structurally can't fire twice on one
  line (e.g., line-range operations, whole-statement matches like `super()`), the column can
  safely be ignored.

- **Tab columns**: the fixer receives character indices, not tab-expanded columns. Unit tests
  without tabs will pass even if column handling is wrong. Always write an integration test with
  tab-indented code.

- **Cross-check interference**: if the test input triggers multiple checks, the fixes can
  interact. Use inputs that isolate the check being tested.

- **The fixer doesn't know which message was logged**: the `fix()` method only receives `lines`,
  `lineIndex`, and `column`. If the check has multiple violation types (e.g., "remove parens" vs
  "remove type" vs "use var"), the fixer must infer the fix type from the line content at the
  violation position. Design the fixer to work from context, not from the message. A common
  approach: examine the text at the column to determine what construct is present, then choose
  the appropriate transformation.

- **One-pass convergence**: if fixing violation A produces code that triggers violation B from
  the SAME check, collapse both fixes into one fixer pass. The fix task runs Checkstyle and
  applies all detected violations in a single pass. If a fixer adds imports (via `importsToAdd`),
  the task runs a second pass to clean up cascading violations (e.g., unused imports). But the
  second pass is only for cross-check cascading, not for same-check convergence. So if your fixer
  for `(String x) ->` produces `(x) ->` (still a violation), the parens won't be removed until
  the NEXT `checkstyleFix` run. Instead, go directly to the final form: `x ->`. Always ask:
  "would the check fire on my fixer's output?" If yes, fix further.

- **Group-consistent output**: when a fixer handles multi-item violations on the same line (e.g.,
  multiple lambda params), the fix must be group-consistent. If the fixer fixes the line on the
  first violation, it modifies ALL items at once (since they share a line). Subsequent violations
  on the same line will call the fixer again on the already-fixed line, so the fixer must be
  idempotent. Example: `(@A String x, String y) ->` fires two violations. The first fixer call
  fixes both to `(@A var x, var y) ->`. The second call sees `(@A var x, var y)` and must
  produce the same output. If the fixer treated each param independently (only fixing the
  violation's specific param), the first call would produce `(@A var x, String y)` and the
  second `(@A var x, var y)` -- correct by accident but fragile. Fix the whole group at once.

- **Invalid intermediate states**: when a check requires uniform form across a group (e.g., all
  lambda params must use the same type form), the fixer must maintain that invariant. If some
  items need `var` (due to annotations) and others would normally get type removal (implicit),
  ALL items must get `var`. Mixing forms produces invalid Java.

- **Checkstyle auto-created tasks**: the plugin has a task name conflict with the `java` plugin.
  This doesn't affect fixer development but matters
  for integration testing in consumer projects.

- **Regex partial matches**: when using regex to match code patterns, `Matcher.find()` matches
  anywhere in the line. If the pattern can match a substring of a larger construct, the fixer
  will corrupt the line. For example, a ternary regex matching `a > b ? a : b` will also match
  inside `++a > b ? a : b`, producing `++Math.max(a, b)`. Use negative lookbehind (`(?<![+-])`)
  or anchor the match appropriately. Always write fixer tests with prefix/suffix characters
  around the matched pattern. See "Fixer regex robustness" in `docs/testing.md` for the full
  checklist (partial matches, nested expressions, ReDoS, multiline).

- **Paren-balanced parsing over regex for nested expressions**: regex character classes like
  `[^,]+` and `[^)]+` break on nested calls (they split at the first delimiter, not the
  correct nesting depth). Use a paren-balancing scanner that tracks `(`/`)` depth. See
  `PreferMathMethodFixer.findAtDepthZero()` for an example.

- **Prefix increment/decrement in condition operands**: `--a > b ? a : b` is semantically
  equivalent to `Math.max(--a, b)` because the mutation happens before the ternary evaluates.
  If the fixer's regex captures `--a` as the operand, it must use the full captured text
  (including `--`) in the replacement but strip it when comparing against branch operands
  (since the branches use the post-mutation variable name `a`, not `--a`).

- **Update the coverage docs**: when adding a fixer, add a row to the appropriate table in
  `docs/auto-fix-coverage.md` (TreeWalker checks or regex checks), and put the per-pattern
  supported/not-supported detail in `docs/coverage/<check>.md` (one file per check, named after
  the test-resource topic dir). Link the row to that file.

- **@SuppressWarnings and fixers**: if the check supports `@SuppressWarnings` suppression (see
  [docs/suppress-warnings.md](suppress-warnings.md)), the fixer automatically skips suppressed
  types because the check never reports violations for them. No separate handling is needed in
  the fixer.