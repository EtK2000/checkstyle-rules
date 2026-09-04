# Adding a New Check

How to add a custom checkstyle rule to the plugin.

## Structure

A check has three parts: the check class, message key, and test resources.

## 1. Check class

In `src/main/java/com/etk2000/checkstyle/`, create a public class extending one of three bases.
Pick the narrowest that fits:

| Base | Use when | Gives you |
| ---- | -------- | --------- |
| `AbstractAstCheck` | the default | `getAcceptableTokens`/`getRequiredTokens` delegating to `getDefaultTokens`, plus `logWarning` |
| `AbstractMinSdkCheck` | the check suggests an API that needs a minimum Android API level | the reflective `minSdk` property and `minSdkAtLeast(int)` |
| `AbstractResolvingCheck` | the check resolves simple type names against the file's imports or package | a per-file import/package scope, `resolve(String)` and `receiverTypeName(DetailAST)` |

```java
public class MyNewCheck extends AbstractAstCheck {
    private static final String MSG_KEY = "my.new.violation";

    @Nonnull
    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL}; // tokens to visit
    }

    @Override
    public void visitToken(@Nonnull DetailAST ast) {
        // check logic, call log() to report violations
        if (somethingIsWrong)
            log(ast, MSG_KEY, "arg1");
    }
}
```

`visitToken()` is called once per matching token in the AST.

`AbstractResolvingCheck` differs in two ways that are easy to get wrong:

- Its `beginTree` and `visitToken` are `final`. Override `beginFile(rootAST)` for per-file setup
  and `visitScopedToken(ast)` instead of `visitToken`.
- It consumes `IMPORT` and `PACKAGE_DEF` itself, but you must still list both in
  `getDefaultTokens()` or the scope stays empty and every simple name silently resolves to
  nothing. `AbstractResolvingCheckTest` fails the build if you forget.

### Choosing tokens

Pick the most specific token type for what you're checking. Common choices:
- `METHOD_CALL` - method invocations
- `VARIABLE_DEF` - variable declarations
- `METHOD_DEF` - method declarations
- `LITERAL_IF` / `LITERAL_FOR` / etc. - control flow statements
- `OBJBLOCK` - class body (iterate children manually for ordering checks)
- `DOT` - member access (`a.b`)

Use Checkstyle's `-t` flag or print the AST in a test to explore the tree structure.

### Helper methods

- Mark all private helpers with `@CheckReturnValue` if they're pure functions
- Use `@Nonnull`/`@Nullable` on all non-primitive parameters
- Static methods before instance methods, sorted alphabetically
- For complex receiver type resolution, use `ReflectionUtil`
- For AST traversal utilities, use `AstUtil`

### AstUtil reference

Common utilities in `AstUtil` (check before writing your own):

- `displayText(DetailAST)`: human-readable text for messages. Handles operators, dots, brackets,
  increment/decrement. Use for violation message construction
- `exprText(DetailAST)`: structural text for equality comparison. Concatenates leaf text without
  operators. Use for comparing whether two AST expressions refer to the same thing
- `isPureExpression(DetailAST)`: checks if an expression has no side effects (identifiers, field
  accesses, literals, array accesses, unary operators). Use for gating transformations that
  require operand purity
- `isZeroLiteral(DetailAST)`: checks if a numeric literal is zero. Handles all Java literal forms
  (hex, binary, underscore, exponent, suffixes). Use instead of comparing `getText()` against `"0"`
- `resolveVariableType(DetailAST, String)`: finds the declared type of a variable by walking up
  scopes. Returns null for primitives and `var`
- `getReceiverTypeName(DetailAST)`: finds the type of a method call's receiver
- `hasSuppressWarnings(DetailAST, String)`: checks if a MODIFIERS node contains
  `@SuppressWarnings` with a specific key. Use for per-type suppression support (see
  `docs/suppress-warnings.md`)

When adding general-purpose utilities, add them to `AstUtil` with tests in `AstUtilTest` rather
than keeping them private in the check class. This prevents duplication when another check needs
the same logic.

### MinSdk gating

If the suggested API requires a minimum Android SDK version, accept a `minSdk` property:

```java
private int minSdk = Integer.MAX_VALUE;

/**
 * Sets the minimum SDK version for the target platform.
 * <p>Called by Checkstyle via reflection when {@code minSdk} is set in the config.</p>
 */
@SuppressWarnings("unused")
public void setMinSdk(int minSdk) {
    this.minSdk = minSdk;
}
```

The `@SuppressWarnings("unused")` documents that Checkstyle calls the setter via reflection. Add
Javadoc explaining which APIs the gate affects.

And check it before logging: `if (minSdk >= REQUIRED_API) log(...)`.

Configure the property in **both** XML files:

`src/main/resources/com/etk2000/checkstyle/checkstyle.xml`:
```xml
<module name="com.etk2000.checkstyle.MyNewCheck">
    <property name="minSdk" value="${minSdk}" />
</module>
```

`config/checkstyle/checkstyle-test-resources.xml` (same entry, same location alphabetically):
```xml
<module name="com.etk2000.checkstyle.MyNewCheck">
    <property name="minSdk" value="${minSdk}" />
</module>
```

Forgetting the test-resources config means the check runs on test resource files with
`minSdk = Integer.MAX_VALUE` (all gates open), which may not match the intended behavior.
```

## 2. Message key

Add the message to `src/main/resources/com/etk2000/checkstyle/messages.properties`:

```properties
my.new.violation=Use ''{0}'' instead of ''{1}''.
```

Messages are sorted alphabetically. Arguments use `{0}`, `{1}`, etc. (MessageFormat syntax).
Single quotes must be doubled (`''`).

## 3. Register in checkstyle.xml

Add the module to `src/main/resources/com/etk2000/checkstyle/checkstyle.xml` inside the
`<module name="TreeWalker">` block:

```xml
<!-- Custom: description of what it checks -->
<module name="com.etk2000.checkstyle.MyNewCheck" />
```

Modules are grouped: custom checks first (alphabetical), then built-in checks (alphabetical).

## 4. Test resources

Create a directory under `src/test/resources/com/etk2000/checkstyle/inputs/mycheck/` with:

### Clean file (`cases.clean.java`)

Valid code that must produce zero violations. Cover every pattern the check should ALLOW:
- Each branch that exits without logging
- Boundary cases that are just barely acceptable
- Every variant the check explicitly skips (e.g., different token types, null-safe forms)

### Violation file(s) (`cases.in.java`)

Code with known violations. Mark each violation line with a comment:
```java
someCode(); // violation: description of what's wrong
```

Cover:
- Every `log()` call site in the check (each should have at least one triggering line)
- Boundary cases that are just barely violating (boundary pair with the clean file)
- Different contexts (field, local, method arg, return, etc.)

### Multiple violation files

If the check has distinct modes or configurations, use separate files with a variant suffix:
`cases.foo.in.java`, `cases.bar.in.java` (each paired with `cases.foo.out.java`,
`cases.bar.out.java` if there is fixer output to verify).

## 5. Wire the check into `StandardCheckTests`

**Always** add the check to `StandardCheckTests.ENTRIES`. This is the
project's standard test pipeline; one-off `XxxCheckTest` classes that
duplicate clean / violations / per-slice fix logic are a bug, not a
shortcut (they bypass `assertCheckMatchesMarkers`, so drifted markers
and missing minSdk predicates go undetected).

```java
new Entry(MyNewCheck .class),                       // check-only
new

Entry(MyNewCheck .class, /*hasFixer*/ true)     // check + per-slice fixer tests
```

Entries must stay alphabetically sorted (a dedicated test in the same class
enforces this). The auto-pipeline expands each entry to:

- `<CheckClass> > clean` — runs the check against `cases.clean.java`,
  asserts zero violations. When `hasFixer=true` it also runs the entry's own
  fixer over the clean file to a fixed point and asserts it is unchanged (the
  file is clean for that check/fixer, so its fixer must be a no-op).
- `<CheckClass> > violations` — runs the check against `cases.in.java`,
  matches every `// violation:` marker 1:1 against the check's output via
  `BaseCheckTest.assertCheckMatchesMarkers`.
- `<CheckClass> > <slice_name> > violations` — same matcher run against
  each `// === case: NAME ===` slice (with the file prefix prepended) so
  type-resolving checks see the right import context.
- `<CheckClass> > <slice_name> > fix` — applies the fixer to each
  single-violation slice and asserts the post-fix output equals the
  matching Fixed slice. Emitted only when `hasFixer=true`.
- `<CheckClass> > <slice_name> > imports-unchanged` — emitted only when
  `hasFixer=true` AND the check is gated off under the entry's
  properties. Asserts the Fixed slice's imports equal the Violation
  slice's (no fix runs, so no diff allowed).

See `docs/testing.md` for the full pipeline.

A dedicated `MyNewCheckTest` class is only justified for things the
auto-pipeline can't express (direct AST unit tests against tokens that
no `cases.in.java` line can produce, cross-check sanity scans, fragment
or skip-result assertions against `fragments.in.java`). Do not put a
clean / violations / per-slice fix test there.

### Properties and minSdk variants

Each `ENTRIES` row can carry a `Map<String,String>` of check properties.
When a check uses `minSdk` gating, register **every** variant (both
gated-on and gated-off):

```java
new Entry(MyNewCheck .class, true,Map.of("minSdk", "18")),  // gated off
        new

Entry(MyNewCheck .class, true,Map.of("minSdk", "19")),  // gated on
```

Marker predicates are mandatory under variant registration: every
`// violation:` marker in `cases.in.java` must carry a predicate matching
the gate (`// violation [minSdk>=19]: ...`) so the marker is inactive
under the gated-off variant. Without the predicate the
`violations` dynamic test for the gated-off variant fails with
"expected N, got 0". For each variant, add a sibling
`cases.out.<variant>.java` holding the expected post-fix output (for the
gated-off variant the Fixed slices are byte-identical to the Violation
slices). See "MinSdk-gated checks: variants and marker predicates" in
`docs/testing.md`.

Underneath, `BaseCheckTest.runCheck(MyNewCheck.class, "input.java", "minSdk", "19")`
is the raw primitive; `StandardCheckTests` calls it for you with the
entry's properties. Don't call `runCheck` directly in a new test.

## 6. Verify

Run `./gradlew check`. This checks:
- The check compiles
- `checkstyleMain` runs the check on the plugin's own source
- `checkstyleTest` runs it on test source
- `checkstyleTestResources` runs it on test resource files (the Input*.java files)
- All tests pass
- `messages.properties` stays sorted (`MessagesFileSortedTest`)

## 7. Update README

Add the check to the appropriate table in `README.md` (custom checks, regex rules, or
built-in checks).

If you also added a doc under `docs/`, add a hook line for it to [docs/README.md](README.md) in the
same commit. That index is the only cheap way to find prior art across 150+ docs.

## 8. Register suppression for test resources

Add a suppression in `config/checkstyle/suppressions-test-resources.xml` so the check doesn't fire
on its own violation test files:

```xml
<suppress checks="MyNewCheck" files="inputs[\\/]mynewcheck[\\/]" />
```

This goes in the first section (alphabetically by check name) where each check suppresses itself in
its own test directory.

If your check also fires on test resources for OTHER checks (e.g., a ternary check fires on
ternaries in the `controlflow/` test directory), add cross-check suppressions in the second section.

## 9. (Optional) Add @SuppressWarnings support

If users may need to exempt specific types from the check (e.g., intentionally non-alphabetical
enum constants), add `@SuppressWarnings` support. See
[docs/suppress-warnings.md](suppress-warnings.md) for implementation details and the full list of
existing suppression keys.

## Common pitfalls

- **New checks break existing test resources**: when you register a new check in `checkstyle.xml`,
  it immediately fires on ALL source files AND test resources via `checkstyleTestResources`. Before
  running `./gradlew check`, search existing test resources for patterns your check would flag
  (e.g. `grep -r "(String x) ->"` for a lambda type check). Update those files first, or your
  build will fail with violations in files you didn't write.

- **Test resource files are checked too**: the Input*.java files go through
  `checkstyleTestResources` which runs the project's own rules. Methods must be alphabetically
  ordered, blank lines after `break;` before `case`/`default`, no trailing whitespace, etc.

- **AST structure varies by context**: see `docs/ast-structure.md` for the full reference.
  Always verify AST structure empirically.

- **`AstUtil.typeText()` returns empty string for primitives**: if your check logs type names in
  violation messages (e.g. "use X instead of '{0}'"), handle primitive types explicitly. The TYPE
  node for `int`, `boolean`, etc. contains keyword tokens (`LITERAL_INT`, `LITERAL_BOOLEAN`), not
  `IDENT` tokens, so `AstUtil.typeText()` returns `""`. Use a switch on the child token type to
  map to the primitive name.

- **`DetailAST.getColumnNo()` is a raw char index, NOT tab-expanded**: it is the 0-based character
  offset on the line (each tab counts as one char). Tab expansion applies only to the *reported*
  violation column (`AuditEvent.getColumn()`), which the fixer harness converts back to a char index
  via `CheckstyleFixAction.tabColumnToCharIndex` before matching it against `getColumnNo()`. So a
  fixer can slice a raw line at `getColumnNo()` directly (see `JavaTernaryReformatter`).

- **Cross-check ALL files, not just representative ones**: when your check is related to another
  check (e.g., both handle annotations on parameters), cross-check EVERY test resource file from
  the related check, not just 2-3 "representative" ones. A file that looks unrelated might
  contain a lambda or edge case that triggers your check unexpectedly.

- **Group-state logic needs permutation tests**: when your check sets a flag by iterating a group
  (e.g., "any param has annotations?"), test the flag-triggering item in every position within
  the group (first, last, all, none). The code path is the same, but different iteration orders
  expose different bugs in fixers and in the check's own per-item loop. See the multi-item
  permutation coverage section in `docs/testing.md`.