# Version Gating

Some checks suggest APIs that aren't available on all Java versions or Android API levels. This doc
covers the current `minSdk` gating mechanism and how to extend it with Java version gating.

## Current state: Android `minSdk` only

The plugin resolves `minSdk` from the target project (Android plugin's `defaultConfig.minSdk`, or
`AndroidManifest.xml`, or `Integer.MAX_VALUE` for non-Android projects). This value is substituted
into `checkstyle.xml` via `${minSdk}` and passed to checks as a property.

Checks that use it:
- `PreferStandardCharsetsCheck` (API 19+)
- `PreferSpecificApiCheck` (various: API 24+ for `forEach`, API 30+ for `List.of`, API 31+ for
  `List.copyOf`)

### How it works in a check

```java
private int minSdk = Integer.MAX_VALUE;

public void setMinSdk(int minSdk) {
    this.minSdk = minSdk;
}

// in visitToken:
if (minSdk >= REQUIRED_API_LEVEL)
    log(ast, MSG_KEY, ...);
```

Checkstyle calls the setter via reflection when the property is set in `checkstyle.xml`:

```xml
<module name="com.etk2000.checkstyle.SomeCheck">
    <property name="minSdk" value="${minSdk}" />
</module>
```

## Adding Java version gating

If the plugin needs to support projects targeting below Java 21, a `javaTarget` property can be
added alongside `minSdk`. This would gate checks that suggest Java version-specific features.

### APIs that would need Java gating

| Java version | Features |
|-------------|----------|
| 7+ | `Objects.requireNonNull()`, `Objects.equals()`, `Objects.hash()` |
| 8+ | `Map.getOrDefault()`, `Map.computeIfAbsent()`, `Map.putIfAbsent()`, `Optional`, `Math.addExact()` |
| 9+ | `List.of()`, `Set.of()`, `Map.of()` (already gated by minSdk 30 for Android) |
| 10+ | `var` (already enforced by `PreferVarCheck`) |
| 11+ | `String.isBlank()`, `String.repeat()`, `Predicate.not()`, `Collection.toArray(IntFunction)` |
| 14+ | Pattern matching `instanceof` (already enforced by `PreferPatternMatchingInstanceofCheck`) |
| 15+ | `String.formatted()` |
| 16+ | Records (already enforced by `PreferRecordCheck`) |
| 21+ | `Math.clamp()`, pattern matching switch |

### Checks that currently assume Java 21

These checks don't gate on Java version because the plugin currently requires Java 21 to build.
If supporting lower targets, they'd need gating:

- `PreferVarCheck` (Java 10+)
- `PreferPatternMatchingInstanceofCheck` (Java 14+)
- `PreferRecordCheck` (Java 16+)
- `UseEnhancedSwitch` (Java 14+, pattern matching in 21+)

### Implementation plan

1. **Resolve `javaTarget`** in `CheckstylePlugin`:
   - Read from `java.toolchain.languageVersion` if set
   - Fall back to `sourceCompatibility` / `targetCompatibility`
   - Fall back to `JavaVersion.current()` (the running JVM)
   - Non-Java projects (unlikely) get `Integer.MAX_VALUE`

2. **Add property to `checkstyle.xml`**:
   ```xml
   <property name="javaTarget" value="${javaTarget}" />
   ```

3. **Add setter to gated checks**:
   ```java
   private int javaTarget = Integer.MAX_VALUE;

   public void setJavaTarget(int javaTarget) {
       this.javaTarget = javaTarget;
   }
   ```

4. **Gate suggestions** the same way as `minSdk`:
   ```java
   if (javaTarget >= 11 && minSdk >= 33)
       // suggest String.isBlank()
   ```

5. **Dual gating**: some APIs need BOTH Java version AND Android API level. For example,
   `String.isBlank()` is Java 11 but only available on Android API 33. On a non-Android Java 11
   project it's fine; on an Android project targeting API 33+ with Java 11+ it's also fine. The
   check must verify both conditions.

### When to implement

Don't implement until needed. The current plugin requires Java 21 to build, and the target
project's Java version isn't relevant to Checkstyle (which parses source text, not bytecode).
The only scenario where this matters is if someone wants to use this plugin on a project that
targets a lower Java version and gets suggestions for APIs they can't use.

Signs it's time:
- A consumer project reports false positives from `PreferVarCheck` on a Java 8 project
- A new check suggests Java 11+ APIs and needs to run on Java 8 projects
- The plugin is published for general use (not just internal projects that all target Java 21)