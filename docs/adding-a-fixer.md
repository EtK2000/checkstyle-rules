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

### 5. Write integration test

In `CheckstyleFixIntegrationTest`, add a test that runs the full pipeline:

```java
@Test
public void testMyFix() throws Exception {
    final var file = tempDir.newFile("My.java");
    Files.writeString(file.toPath(), "class T {\n\t...\n}");

    assertEquals("class T {\n\t...(fixed)...\n}", runFixAndGetResult(file));
}
```

Assert the **exact full output**, not fragments. Use inputs that only trigger the check being
tested (avoid cross-check interference).

If the check is a Checker-level module, also configure it in `runChecks()`.

### 6. Update README

Add the check to the fixable checks table in `README.md`.

### 7. Verify

Run `./gradlew check`. This runs all tests, checkstyle on main code, test code, AND test
resources.

## Common pitfalls

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
  the SAME check, collapse both fixes into one fixer pass. The fix task runs Checkstyle once and
  applies all detected violations. It does NOT re-run Checkstyle after fixing. So if your fixer
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