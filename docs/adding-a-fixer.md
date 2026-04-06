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
- **Checkstyle auto-created tasks**: the plugin has a task name conflict with the `java` plugin
  (see `checkstyle-task-name-conflict.md`). This doesn't affect fixer development but matters
  for integration testing in consumer projects.