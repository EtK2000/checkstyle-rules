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

In `src/test/java/com/etk2000/checkstyle/gradle/fix/MyFixerTest.java`:

- Test every `return null` guard with a dedicated test
- Test every successful fix path
- For every accepted value, write a rejection test with a nearby invalid value (boundary
  pairing)
- If the fixer handles multiple dimensions (e.g., different suffixes, prefixes), test each axis

### 5. Write integration tests

Fix-producing integration tests go in `CheckstyleFixIntegrationTest`. This class has an
`@AfterEach` that asserts `verifyFixedOutputClean` was called exactly once per test, so
every fix test automatically re-checks its output for remaining fixable violations.

Use `runFixAndGetResult()` for most tests (it calls `verifyFixedOutputClean` internally):

```java
@Test
public void testMyFix() throws Exception {
    final var file = tempDir.resolve("My.java").toFile();
    Files.writeString(file.toPath(), "class T {\n\t...\n}");

    final var output = runFixAndGetResult(file);
    assertEquals("class T {\n\t...(fixed)...\n}", output.content());
    assertEquals(1, output.result().fixCount());
    assertFalse(output.result().needsSecondPass());
}
```

Assert the **exact full output**, not fragments. Assert `fixCount` to catch accidental extra
or missing fixes. Assert `needsSecondPass` to verify the fixer correctly signals whether
imports were added. Use inputs that only trigger the check being tested (avoid cross-check
interference). Don't include imports that would become unused after fixing (Checkstyle is
AST-based, so the check fires even without the import).

If the fixer changes a class qualifier (e.g. `Collections.sort` to `list.sort`), also write
a multi-pass test using `runFixMultiPass()` with the import present to verify the unused
import gets cleaned up on the second pass.

If the check is a Checker-level module, also configure it in `runChecks()`.

No-fix tests (clean files, skipped violations, warning-only) go in `CheckstyleFixNoFixTest`.
Pure utility tests (hint messages, tab-column math) go in `CheckstyleFixUtilTest`.

### 6. Update README

Add the check to the fixable checks table in `README.md`.

### 7. Verify

Run `./gradlew check`. This runs all tests, checkstyle on main code, test code, AND test
resources.

## Common pitfalls

- **Column-awareness**: if the check can fire multiple times on the same line (e.g., two
  `.get(0)` calls), the fixer MUST use the `column` parameter to target the correct occurrence.
  Use `line.indexOf(pattern, column)` or validate that the pattern exists at the expected column,
  not `line.indexOf(pattern)` from position 0. If the check structurally can't fire twice on one
  line (e.g., line-range operations, whole-statement matches like `super()`), the column can
  safely be ignored. See `docs/targeted-fixing.md` for the full audit of existing fixers.

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

- **Checkstyle auto-created tasks**: the plugin has a task name conflict with the `java` plugin
  (see `checkstyle-task-name-conflict.md`). This doesn't affect fixer development but matters
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
  `PreferMathMethodFixer.findAtDepthZero()` for an example. See also
  `docs/fixer-architecture-redesign.md` for the longer-term solution of giving fixers AST
  access.

- **Prefix increment/decrement in condition operands**: `--a > b ? a : b` is semantically
  equivalent to `Math.max(--a, b)` because the mutation happens before the ternary evaluates.
  If the fixer's regex captures `--a` as the operand, it must use the full captured text
  (including `--`) in the replacement but strip it when comparing against branch operands
  (since the branches use the post-mutation variable name `a`, not `--a`).

- **Update auto-fix-coverage.md**: when adding a fixer, add it to `docs/auto-fix-coverage.md`
  in the appropriate table (TreeWalker checks or regex checks).

- **@SuppressWarnings and fixers**: if the check supports `@SuppressWarnings` suppression (see
  [docs/suppress-warnings.md](suppress-warnings.md)), the fixer automatically skips suppressed
  types because the check never reports violations for them. No separate handling is needed in
  the fixer.