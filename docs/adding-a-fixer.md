# Adding a New Fixable Check

How to add auto-fix support for a checkstyle rule to the `checkstyleFix` task.

## Steps

### 1. Create the fixer class

In `src/main/java/com/etk2000/checkstyle/gradle/fix/`, create a package-private class
implementing `CheckstyleFixer`:

```java
class MyFixer implements CheckstyleFixer {
    @Nonnull
    @Override
    public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
        // lineIndex: 0-based line in the file
        // column: 0-based CHARACTER index (already converted from tab-expanded)
        // return a SkipResult with a reason if the fix can't be applied, never null
        // return FixResult(startLine, endLine, replacement) to replace lines[start..end]
    }
}
```

`applyFixes()` converts the column before calling the fixer, so don't do your own tab conversion.

**Adding imports**: if the fix introduces a new type reference (e.g., replacing
`Collections.emptyList()` with `List.of()`), use the 4-arg constructor to request the import:

```java
return new FixResult(lineIndex, lineIndex, List.of(fixed), Set.of("java.util.List"));
```

The `importsToAdd` set contains fully qualified class names. `applyFixes()` inserts them in
sorted position within the correct package group after all line fixes are applied. If the
import already exists, it is skipped. When imports are added, `applyFixes()` signals
`needsAnotherPass = true` so the fix task runs another pass to clean up cascading violations
(e.g., the old import becoming unused).

If the fix doesn't need imports, use the 3-arg constructor (defaults to empty set):

```java
return new FixResult(lineIndex, lineIndex, List.of(fixed));
```

**Second-pass cascade triggers**: after the per-violation fix loop, `applyFixes()` may signal `needsAnotherPass = true`:

| Trigger | Behavior | Limitations |
| --- | --- | --- |
| Fixer adds an import (`importsToAdd` non-empty, insertion occurred) | Insert missing imports; flip `needsAnotherPass` | None |
| Fixer removes the last usage of an imported short name | Flip `needsAnotherPass` so `UnusedImportsCheck` runs in pass 2 and `DeleteLineFixer` removes the dead line | Heuristic word-boundary scan. Skips wildcard imports (`*` and `static ...*`) and default-package imports (no `.` in FQN). Pre-existing unused imports (no fixer involvement) are handled by a direct `UnusedImportsCheck` run instead. Identifiers used only inside Javadoc `@link`/`@see` may be flagged as unused (a harmless extra pass 2). Text blocks are skipped like comments; surrogate-pair identifiers are not supported. |
| An event falls inside a span an earlier fix in the pass rewrote, or on a line a fix in the pass changed in any way | Skip the event (`FIX_SUPPRESSED`), hold back `UnusedImportsFixer` deletes for the rest of the pass, flip `needsAnotherPass` so the next pass re-checks the new text | Once a fix changes line L, every other check's later event on L waits for the next pass, whatever the fix left intact, since an event may hang on text past its column (PreferVar's event on a declaration whose cast another fix removed). The one exception is the same check after a single-line in-place edit of L (one replacement line, start and end on L): its later events stay open while they point ahead of the first char the edit changed, so six `this.` reads on one line still go in one pass. A multi-line fix covering L, or one that lands another declaration on it, holds back every later event on L, the same check's too. The state is per line and builds up: a second fix on L moves the point events must stay ahead of, and a multi-line one closes L for the rest of the pass. The next pass runs even when the rewrite already resolved the skipped event, which costs one clean re-check |
| An event on the line a fix just deleted, which now holds whatever shifted up into it | Skip the event (`FIX_SUPPRESSED`), with the same hold-back and `needsAnotherPass` as above | An empty line that shifted up is let through once to a line-deleting fixer (RedundantImport and UnusedImports both firing on one import: the first deletes it, the second the leftover blank) |

`execute()` runs another pass whenever the last one fixed anything, over just the files that pass
rewrote, up to `MAX_FIX_PASSES` (6) in total. `needsAnotherPass` alone is not enough: a fix can
leave a violation that only a fresh check run reports (a FieldSorting move that brings two fields
of one key together for FieldConsolidation). Every integration case and every topic's
`cases.in.java` settles within 5 passes, the last of which finds nothing to fix (measured; the
same-line hold-back above costs the busiest files a pass), so the spare pass stops a file that
never settles without cutting a slow one short; when it is hit, the task names the files still
changing on stderr (`Stopped after 6 fix passes with files still changing, run the task again to
finish them: [...]`). A dry run makes one pass, so its hint counts only the events no earlier fix
on their line held back.

Each rewrite goes to a temporary file beside the source (through any symlink), takes over its
POSIX permissions and is moved over it in one step, so a failed or cancelled write leaves the
original intact; a file its owner cannot write is reported (`Could not write ...`) and left out of
the next pass. The fix Checker reads sources as UTF-8, the encoding the fixers read and write, so
the columns it reports match the text they edit.

### 2. Register the fixer

**For TreeWalker checks** (most checks): add to `FIXERS` in `CheckstyleFixAction`:

```java
Map.entry(MyCheck.class.getName(), new MyFixer()),
```

The check class must be importable (it's a `compileOnly` dependency). The key is the fully
qualified class name, which matches `AuditEvent.getSourceName()`.

**For Checker-level modules** (RegexpSingleline, RegexpMultiline): add to `MODULE_ID_FIXERS`
with a string ID, then configure the module in `createCheckerConfig()`:

```java
// in MODULE_ID_FIXERS
Map.entry("MyModuleId", new MyFixer())

// in createCheckerConfig()
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
`validateEnhancedForLoopVariable=false`), add them in `createCheckerConfig()`, where the check
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
when the check is in `ENTRIES`. That duplicates what the auto-pipeline
already runs, and drifts silently as you add new slices.

When the fixer adds imports, list them as `// imports: <fqcn>` directives in
the Fixed slice (but not the Violation slice); `assertCaseFix` derives the
expected `importsToAdd` from the slice diff. A `// imports:` value that is a
full import line (it begins with `import ` or with a leading block comment
`/*`) is emitted verbatim rather than wrapped, so a slice can carry an import
line bearing a comment (`// imports: import foo.Foo; // note`,
`// imports: /* legacy */ import foo.Foo;`) that a real import line in the
linted `cases.*.java` file could not. That full-import form is the *only* way
to attach a comment: a bare FQCN carrying one
(`// imports: java.util.List // note`) is rejected, because the wrapping
appends a `;` that the comment would swallow. When a slice needs its own package
context (a check that only fires on a package-qualified reference, e.g. a
`pkg.Type.FIELD` cinit LHS), declare it with a `// package: <name>` directive
at the top of the slice: it stays a comment in the physical file (which keeps
its single file-level package and still parses as one unit) and expands to a
real `package <name>;` line in the slice's input, replacing the file-level
package for that slice. When the check is `minSdk`-gated,
register every variant (gated-on AND gated-off) in `ENTRIES` and add the
matching predicate (`// violation [minSdk>=N]: ...`) to every marker in
`cases.in.java`. The gated-off variant's `cases.out.<variant>.java`
must NOT carry `// imports:` directives the input doesn't already have;
the auto-emitted `imports-unchanged` dynamic test enforces this.

Both directives are expanded twice, by two implementations with deliberately
different rules, so a case that works one way may not work the other. Loading a
slice (`TestResources.loadCaseSlice`, which every `assertCaseFix` and auto-
pipeline test goes through) substitutes each directive line in place, keeping
line numbers stable, and classifies purely by text. Running a check over the
whole physical `cases.*.java` file (`BaseCheckTest.runCheck` and the path form
of `assertCheckMatchesMarkers`) instead packs the imports onto the `package`
line, because a real `import` between two class declarations would not parse;
it consults a lexer, so a directive-shaped line that is only text-block or
block-comment content is not treated as a directive there; and it rejects any
comment-carrying value outright. So a slice that needs the full-import form can
only be asserted per-slice, never through a whole-file run.

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

Use `assertFullFix(caseName, fixCount, needsAnotherPass)` for most tests. The helper loads
the fixture from a slice, runs the pipeline, asserts (content, fixCount, needsAnotherPass),
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

Use inputs that only trigger the check
being tested (avoid cross-check interference). Don't include imports that would become unused
after fixing (Checkstyle is AST-based, so the check fires even without the import).

If the fixer changes a class qualifier (e.g. `Collections.sort` to `list.sort`), also add an
`integration` case with the import present in its input and a row to the `testFixUntilSettled`
`@CsvSource` in `CheckstyleFixIntegrationTest` giving the pass count, which runs the task's own
pass loop and verifies the unused import is cleaned up on a later pass.

If your new fixer collapses scaffolding that other fixers' `cases.in.java` files use
(e.g. you simplify a syntactic form they rely on for context), `FullPipelineRegressionTest`
will start producing output that differs from those checks' `cases.out.java`. The fix is
NOT to weaken the test. Drop a per-case override slice into the sibling topic's
`cases.*.fixed.java` documenting the post-pipeline result.

No-fix tests (clean files, skipped violations, warning-only) go in `CheckstyleFixNoFixTest`.
Pure utility tests (hint messages, tab-column math) go in `CheckstyleFixUtilTest`.

Your topic's `cases.clean.java` already gets a free no-op assertion once the entry is
`hasFixer=true`: `StandardCheckTests`'s `clean` dynamic runs your fixer over it to a fixed point
and fails if it changes anything (the file is clean for your check/fixer, so your fixer must do
nothing). Prefer adding a parsable, check-silent edge case there over a `fragments.in.java`
entry, so this no-op guard covers it.

### 6. Update README

If you added a doc under `docs/`, add a hook line for it to [docs/README.md](README.md) in the
same commit. That index is the only cheap way to find prior art across 150+ docs.

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

- **Tab columns**: the fixer receives the column as an index into the line's chars, not
  tab-expanded: `CheckstyleFixAction.applyFixes` converts the reported column once
  (`eventCharIndex`) for every fixer. Unit tests without tabs will pass even if column handling is
  wrong. Always write an integration test with tab-indented code.

- **Char index, not code points**: index the line with the column directly; never pass it through
  `LineText.charIndexOfColumn` again. A supplementary character ahead of it is two chars but one
  code point, and an AST node's `getColumnNo()` counts code points, so a fixer matching the column
  against an AST position (a check's `...At(root, line, column)` locator) converts it back once at
  the top of `fix()` with `LineText.columnOfCharIndex(line, column)`, saying why;
  `SpanReformat.charColumn(lines, node)` gives a node's char index. The `fix-supplementary` sub-test `StandardFixerCases` adds beside each `fix` case
  whose violation is reported past its line's first non-blank char re-runs it with the text ahead
  of the column lengthened by supplementary characters, and expects the same output. So a fixer scanning
  identifiers char by char must accept the two halves of a supplementary letter:
  `LineText.isIdentifierPart(char)` and `isIdentifierStart(char)` do.

- **Unparseable output**: a pass whose fixes would leave a file that parsed unparseable is not
  written; the task names the file and the first parse error on stderr and leaves it as it was.
  That only stops the damage, so a fixer must still never emit text that does not parse.

- **Cross-check interference**: if the test input triggers multiple checks, the fixes can
  interact. Use inputs that isolate the check being tested.

- **The fixer doesn't know which message was logged**: the `fix()` method only receives `lines`,
  `lineIndex`, and `column`. If the check has multiple violation types (e.g., "remove parens" vs
  "remove type" vs "use var"), the fixer must infer the fix type from the line content at the
  violation position. Design the fixer to work from context, not from the message. A common
  approach: examine the text at the column to determine what construct is present, then choose
  the appropriate transformation. When the fix rests on the check's decision about text another
  fix may have changed, confirm it on the current text: `FixContext.getViolationMessage()` and
  `getViolationSeverity()` expose the event, and a public static on the check can re-run its
  decision (ExplicitInitialization confirms the name at the column is the one the message quotes;
  PreferVar re-reads whether the initializer still pins a primitive type).

- **One-pass convergence**: if fixing violation A produces code that triggers violation B from
  the SAME check, collapse both fixes into one fixer pass. The fix task runs Checkstyle and
  applies all detected violations in a single pass. If a fixer adds imports (via `importsToAdd`),
  or an event is held back (see the second-pass triggers above), or any fix was applied, the
  task runs another pass, up to `MAX_FIX_PASSES`. Those passes exist for cross-check cascading
  and held-back events, and each costs a full check run over the rewritten files, so don't lean
  on them for same-check convergence. If your fixer for `(String x) ->` produces `(x) ->` (still
  a violation), the parens are only removed a pass later. Instead, go directly to the final form:
  `x ->`. Always ask:
  "would the check fire on my fixer's output?" If yes, fix further.

- **Group-consistent output**: when a fixer handles multi-item violations on the same line (e.g.,
  multiple lambda params), the fix must be group-consistent. If the fixer fixes the line on the
  first violation, it modifies ALL items at once (since they share a line). Subsequent violations
  on the same line that point ahead of the first changed char may call the fixer again on the
  already-fixed line in the same pass, and the rest call it on the next pass, so the fixer must be
  idempotent. Example: `(@A String x, String y) ->` fires two violations. The first fixer call
  fixes both to `(@A var x, var y) ->`. The second call sees `(@A var x, var y)` and must
  produce the same output. If the fixer treated each param independently (only fixing the
  violation's specific param), the first call would produce `(@A var x, String y)` and the
  second `(@A var x, var y)`, which is correct by accident but fragile. Fix the whole group at once.

- **Invalid intermediate states**: when a check requires uniform form across a group (e.g., all
  lambda params must use the same type form), the fixer must maintain that invariant. If some
  items need `var` (due to annotations) and others would normally get type removal (implicit),
  ALL items must get `var`. Mixing forms produces invalid Java.

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