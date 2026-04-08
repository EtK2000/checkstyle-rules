# Adding a New Check

How to add a custom checkstyle rule to the plugin.

## Structure

A check has three parts: the check class, message key, and test resources.

## 1. Check class

In `src/main/java/com/etk2000/checkstyle/`, create a public class extending `AbstractCheck`:

```java
public class MyNewCheck extends AbstractCheck {
    private static final String MSG_KEY = "my.new.violation";

    @Nonnull
    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Nonnull
    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL}; // tokens to visit
    }

    @Nonnull
    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(@Nonnull DetailAST ast) {
        // check logic, call log() to report violations
        if (somethingIsWrong)
            log(ast, MSG_KEY, "arg1");
    }
}
```

All three token methods must return the same array. `visitToken()` is called once per matching
token in the AST.

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

### MinSdk gating

If the suggested API requires a minimum Android SDK version, accept a `minSdk` property:

```java
private int minSdk = Integer.MAX_VALUE;

public void setMinSdk(int minSdk) {
    this.minSdk = minSdk;
}
```

And check it before logging: `if (minSdk >= REQUIRED_API) log(...)`.

Configure the property in `checkstyle.xml`:
```xml
<module name="com.etk2000.checkstyle.MyNewCheck">
    <property name="minSdk" value="${minSdk}" />
</module>
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

### Clean file (`InputMyCheckClean.java`)

Valid code that must produce zero violations. Cover every pattern the check should ALLOW:
- Each branch that exits without logging
- Boundary cases that are just barely acceptable
- Every variant the check explicitly skips (e.g., different token types, null-safe forms)

### Violation file(s) (`InputMyCheckViolation.java`)

Code with known violations. Mark each violation line with a comment:
```java
someCode(); // violation: description of what's wrong
```

Cover:
- Every `log()` call site in the check (each should have at least one triggering line)
- Boundary cases that are just barely violating (boundary pair with the clean file)
- Different contexts (field, local, method arg, return, etc.)

### Multiple violation files

If the check has distinct modes or configurations, use separate files:
`InputMyCheckFooViolation.java`, `InputMyCheckBarViolation.java`.

## 5. Test class

In `src/test/java/com/etk2000/checkstyle/MyNewCheckTest.java`:

```java
public class MyNewCheckTest {
    private static final String DIR = "mycheck/";

    @Test
    public void testClean() throws Exception {
        assertTrue(BaseCheckTest.runCheck(MyNewCheck.class, DIR + "InputMyCheckClean.java").isEmpty());
    }

    @Test
    public void testViolations() throws Exception {
        final var violations = BaseCheckTest.runCheck(MyNewCheck.class, DIR + "InputMyCheckViolation.java");
        assertEquals(3, violations.size());
        assertEquals(10, violations.get(0).getLine());
        assertEquals("Expected message.", violations.get(0).getMessage());
        // ... verify every violation's line and message
    }
}
```

Verify the exact count, line number, and message for every violation. For clean tests, just
assert empty.

### Properties

Pass check properties as alternating key-value pairs:
```java
BaseCheckTest.runCheck(MyNewCheck.class, DIR + "Input.java", "minSdk", "19")
```

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

## Common pitfalls

- **New checks break existing test resources**: when you register a new check in `checkstyle.xml`,
  it immediately fires on ALL source files AND test resources via `checkstyleTestResources`. Before
  running `./gradlew check`, search existing test resources for patterns your check would flag
  (e.g. `grep -r "(String x) ->"` for a lambda type check). Update those files first, or your
  build will fail with violations in files you didn't write.

- **Test resource files are checked too**: the Input*.java files go through
  `checkstyleTestResources` which runs the project's own rules. Methods must be alphabetically
  ordered, blank lines after `break;` before `case`/`default`, no trailing whitespace, etc.

- **AST structure varies by context**: `do-while` body is the first child of `LITERAL_DO`
  (before the condition), unlike `if`/`while`/`for` where the body follows `RPAREN`. Always
  verify AST structure empirically.

- **`AstUtil.typeText()` returns empty string for primitives**: if your check logs type names in
  violation messages (e.g. "use X instead of '{0}'"), handle primitive types explicitly. The TYPE
  node for `int`, `boolean`, etc. contains keyword tokens (`LITERAL_INT`, `LITERAL_BOOLEAN`), not
  `IDENT` tokens, so `AstUtil.typeText()` returns `""`. Use a switch on the child token type to
  map to the primitive name.

- **`DetailAST.getColumnNo()` is tab-expanded**: column numbers account for tab width (default
  8). This matters if your check uses column positions for anything.

- **Cross-check ALL files, not just representative ones**: when your check is related to another
  check (e.g., both handle annotations on parameters), cross-check EVERY test resource file from
  the related check, not just 2-3 "representative" ones. A file that looks unrelated might
  contain a lambda or edge case that triggers your check unexpectedly.

- **Group-state logic needs permutation tests**: when your check sets a flag by iterating a group
  (e.g., "any param has annotations?"), test the flag-triggering item in every position within
  the group (first, last, all, none). The code path is the same, but different iteration orders
  expose different bugs in fixers and in the check's own per-item loop. See the multi-item
  permutation coverage section in `docs/testing.md`.