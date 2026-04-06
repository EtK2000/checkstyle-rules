# Consumer Test Project

## Current State

The plugin's Gradle integration (task registration, XML report parsing, hint messages, minSdk
resolution) is only tested via `ProjectBuilder` in unit tests. `ProjectBuilder` creates a
lightweight project that can verify task existence and basic configuration, but cannot execute
tasks, produce real Checkstyle reports, or test the full Gradle lifecycle.

The plugin's own project applies the raw `checkstyle` Gradle plugin directly (not
`com.etk2000.checkstyle`), so features like `checkstyleFixHint`, `checkstyleFixAll`, and the
auto-configured minSdk detection are never exercised in a real build.

## Gap

These features have no real-build test coverage:

1. **`checkstyleFixHint`**: the hint message after checkstyle failures (parses XML reports, counts
   fixable violations, prints the right task name). Entirely untested end-to-end.
2. **`checkstyleFixAll`**: the combined fix task. Only verified to exist, never executed.
3. **`checkstyleFix` / `checkstyleFixTest`**: the fix tasks run a real `Checker` and write files.
   Only the fix logic is unit-tested, not the Gradle task wiring (source directory property, file
   walking, file writing).
4. **minSdk resolution**: the `resolveMinSdk` path through the Android plugin's `getDefaultConfig`
   reflection is only tested with `ProjectBuilder` (no real Android plugin). The manifest fallback
   is tested but the reflection path is not exercised against a real Android project.
5. **Dependency injection**: `addDependencies` adds the plugin JAR and checkstyle to the classpath.
   Never verified that Checkstyle can actually load the custom check classes in a consumer.
6. **`extractCheckstyleConfig`**: the extracted XML is verified to contain `<module name="Checker">`
   but never used by a real Checkstyle execution through the plugin.

## Proposal

Add a minimal consumer project under `test-project/` (or `integration-test/`) that applies the
plugin and has a few Java files with known violations. Use Gradle's `TestKit`
(`GradleRunner.create()`) to run real Gradle builds and verify:

- `checkstyleMain` detects expected violations and the hint message appears in output
- `checkstyleFix` fixes the expected violations (compare file content before/after)
- `checkstyleFixAll` runs both fix tasks
- The fix hint shows the correct count and task name
- Clean files produce no violations

### Structure

```
test-project/
    build.gradle        # applies com.etk2000.checkstyle, minimal config
    src/main/java/
        Clean.java      # no violations
        Fixable.java    # only fixable violations (trailing comma, redundant L, etc.)
        Mixed.java      # mix of fixable and non-fixable violations
```

### TestKit test

```java
@Test
void checkstyleFixHintShowsCount() {
    var result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .withArguments("checkstyleMain")
        .buildAndFail();
    assertThat(result.getOutput()).contains("Run ./gradlew checkstyleFix to auto-fix");
}

@Test
void checkstyleFixAppliesFixes() {
    GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .withArguments("checkstyleFix")
        .build();
    // verify Fixable.java no longer has violations
    var result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .withArguments("checkstyleMain")
        .build(); // should pass now
}
```

### Trade-offs

**Pros**: catches integration issues that unit tests can't (classpath problems, task ordering,
report paths, Gradle version compatibility). The hint message is currently completely untested.

**Cons**: slower (real Gradle builds), more maintenance (consumer project files need to stay in
sync with check changes), test-project files need to be excluded from the main project's own
checkstyle runs.

### Recommendation

Worth adding. The hint feature and fix task wiring are user-facing and have zero real-build
coverage. A single TestKit test class with 3-4 tests would cover the critical paths without
much maintenance overhead.