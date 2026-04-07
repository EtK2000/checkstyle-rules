# Testing

How tests work in this project, common pitfalls, and how to ensure coverage.

## Architecture

### Check tests

Check tests use `BaseCheckTest.runCheck()` to run a single check against a test resource file and
collect violations. Each check has:

- **Clean file** (`InputXxxClean.java`): valid code that must produce zero violations
- **Violation file(s)** (`InputXxxViolation.java`): code with known violations, each marked with
  `// violation: <description>` comments

Tests verify the exact count, line numbers, and messages of violations. Clean tests use
`assertTrue(...isEmpty())`.

```java
final var violations = BaseCheckTest.runCheck(SomeCheck.class, DIR + "InputViolation.java");
assertEquals(3, violations.size());
assertEquals(10, violations.get(0).getLine());
assertEquals("Expected message.", violations.get(0).getMessage());
```

### Regex tests

Regex rules (RegexpSingleline/RegexpMultiline) use `BaseCheckTest.runRegexCheck()` or
`runRegexCheckInline()`. These test the regex pattern in isolation, not through the full
checkstyle.xml config. The format string must match the one in checkstyle.xml exactly.

### Fixer tests

Fixers are unit-tested by calling `fix()` directly with crafted input lines. Integration tests
create real Java files, run Checkstyle to detect violations, apply fixes, and assert the exact
full output.

## Tab-expanded columns

Checkstyle reports column numbers with tabs expanded to width 8 (the default `tabWidth`). A tab
at position 0 makes the next character report as column 8 instead of 1.

`CheckstyleFixTask.tabColumnToCharIndex()` converts tab-expanded columns to character indices.
Any code that uses `event.getColumn()` to index into a line string must convert first. This
applies to all fixers since `applyFixes` does the conversion before calling `fix()`.

**Diagnosis**: if a fixer works in unit tests (no tabs) but fails on real files (tabs), the column
conversion is the first thing to check. Write integration tests with tab-indented files to catch
this.

## Cross-check interference

The fix task runs ALL fixable checks simultaneously. Multiple checks can fire on the same code:

- `UpperEll` and `RedundantNumericSuffix` both fire on `100l` (lowercase L that's also redundant)
- `RedundantImport` and `UnusedImports` both fire on `import java.lang.String;`
- `FinalLocalVariable` fires twice on `int x, y;` (once per variable)

When two violations target the same line and one deletes/modifies it, the second violation operates
on stale line indices. Bottom-to-top processing (sorting by line descending) handles most cases,
but same-line violations can still interfere.

**Integration tests must account for this.** Use inputs that isolate a single check. For example,
test UpperEll with `3000000000l` (exceeds int range, so RedundantNumericSuffix doesn't also fire)
rather than `100l`.

## RegexpMultiline violation reporting

`RegexpMultiline` violations report the line number where the match starts, which is the line
BEFORE the problematic content. For `NoDoubleBlankLines` with format `\n\s*\n\s*\n`, the violation
line is the last content line before the blank line group, not the blank lines themselves.

The `DoubleBlankLineFixer` accounts for this by scanning forward from `lineIndex + 1`.

## Coverage checklist

Use this as a **driving process while writing code**, not a post-hoc audit. Do not write a check,
then write a fixer, then write tests. Instead: write one branch of the check, write its tests, then
the next branch and its tests, then the fixer and its tests.

### Before writing any code

1. List every AST context / token type the check will handle
2. For each context, decide: clean example, violation example, boundary example (should NOT fire)
3. For checks with multiple violation types (e.g. placement + ordering + blank lines), repeat step 2
   per violation type
4. If the check has a related check that partitions contexts, list the cross-check file pairs
5. Write this matrix down (on paper, in a comment, in the plan). Every cell must be filled before
   declaring done

### While writing code

6. After writing each `if`/`switch`/`return` branch, immediately write the test for both paths
7. After writing a fixer `fix()` method, immediately write unit tests for every return path (both
   `null` returns and each `FixResult` shape)
8. After registering the fixer in `FIXERS`, immediately write the integration test(s) - one per fix
   type, asserting exact full output

### After all code is written

9. Re-read this entire checklist top to bottom
10. Verify the matrix from step 2 - trace every cell to a specific test method name
11. Verify every branch in the source has a test exercising both true and false
12. Verify every violation test asserts exact count, line numbers, and messages
13. Run `./gradlew check`

### Context coverage (for checks that handle multiple AST contexts)

When a check registers for multiple token types or handles multiple parent contexts (e.g. CLASS_DEF,
METHOD_DEF, VARIABLE_DEF), every context must appear in all three test categories:

1. **Clean file**: a correctly formatted example that produces zero violations
2. **Violation file**: an incorrectly formatted example that produces the expected violation
3. **Fixer test**: at least one integration test per fix type (not per context, since fixers are
   text-based and context-agnostic)

If a check has multiple violation types (e.g. same-line placement, blank lines, alphabetical order),
each violation type needs its own set of clean + violation examples. Use separate violation files per
violation type to keep them manageable.

**Boundary contexts**: the clean file must also include examples of contexts the check should NOT
fire on. For example, if a check handles VARIABLE_DEF but skips for-each variables, the clean file
needs an annotated for-each variable to prove it is not flagged.

### Cross-check testing (for related checks that partition contexts)

When two or more checks divide responsibility over the same token types (e.g. one handles stacked
annotations on declarations, another handles inline annotations on parameters), run every test
resource file from each check against ALL related checks. Every file must produce zero violations
from the other check. This catches context-detection bugs where one check fires in the other's
territory.

```java
// AnnotationOwnLineCheck must not fire on any SameLine test files
assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "Clean.java").isEmpty());
assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "Violation.java").isEmpty());
// ... and vice versa for every file
```

### Branch coverage

Trace every `if`, `switch`, `while` condition, and early `return` in the code. Each branch must
have a dedicated test. After writing code, re-read it and confirm every conditional has a test that
exercises both the true and false paths.

### Boundary pairing

For every value a function accepts, write a rejection test with a "nearby" invalid value. This
catches over-matching.

Examples from this project:
- `ExplicitInitializationFixer` accepts `0.0f` (zero) → must reject `1.0f` (non-zero)
- `ExplicitInitializationFixer` accepts `'\u0000'` → must reject `'\u0001'`
- `DoubleBlankLineFixer` accepts 2 consecutive blanks → must reject 1 blank (the boundary)
- `OverloadMethodOrderCheck` clean file shows `secondParam(int a, char b)` before
  `secondParam(int a, int b)` → violation file shows the reverse

### Expression form coverage

When a check inspects initializers, arguments, or right-hand-side expressions, test every
syntactic form that can appear in that position. Common forms:

- Literal (numeric with all notations: decimal, hex, octal, binary, underscore-separated; also
  char, boolean, string, null)
- Method call (`Integer.parseInt(...)`, `list.size()`)
- Constructor call (`new Foo(...)`)
- Cast expression (`(Type) expr`), including cast-to-same-type vs cast-to-different-type
- Binary/unary expression (`a + b`, `-x`, `a << b`)
- Ternary (`flag ? a : b`)
- Field/array access (`obj.field`, `arr[i]`)

If the check behaves differently based on the expression form (e.g. literal vs non-literal), each
form needs a test in the appropriate category (clean, violation, or warning).

### Severity coverage

When a check emits different severity levels on different code paths (e.g. error for safe cases,
warning for uncertain cases), test each severity explicitly with `assertEquals(SeverityLevel.X,
violation.getSeverityLevel())`. A test that only checks violation count and message will miss a
path that fires at the wrong severity.

### Guard regression

When adding a guard or filter that exempts some inputs from a check, add explicit tests proving
that non-exempted inputs still fire. For example, if a new guard skips `byte x = 5;` (unfixable),
add a test for `int x = 5;` (same structure, should still fire) to prove the guard doesn't
over-suppress. Cover every type/variant that should pass through the guard unchanged.

### Axis coverage

When code handles multiple independent dimensions (prefixes, suffixes, formats, label types),
test each axis. Cross-axis tests are only needed when axes interact.

Example: `ExplicitInitializationFixer.isNumericZero()` has axes for prefix (`0x`, `0b`, none),
suffix (`L`, `f`, `d`, none), and format (decimal, exponent, underscore). Each is tested
independently. The only cross-axis test is `0x0.0p0f` because hex prefix changes which exponent
letter (`p` vs `e`) is valid.

### Integration coverage

Every entry in `FIXERS` and `MODULE_ID_FIXERS` must have an end-to-end integration test that runs
the full pipeline (Checkstyle detection, column conversion, fixer application, output verification).
Assert the exact full output string, not fragments via `contains()`. This catches unintended
modifications to lines the test wasn't looking at.

When a fixer handles mutually exclusive conventions (e.g. JUnit 4 message-first vs JUnit 5
message-last), each convention needs its own integration test combining multiple patterns in a single
file. This verifies that fixers for one convention don't silently mangle lines belonging to the other.
A single-pattern test per convention is not enough because interference only surfaces when multiple
fixer code paths run against the same file.

### Test resource consolidation

Never create a violation file with only one violation. Group related violations into shared files by
rule category (e.g. all stream simplifications in one file, all Collections factory replacements in
one file). This keeps the test suite manageable and makes it obvious when a rule fires on the wrong
input.

When violations in the same file have different minSdk gates, the minSdk boundary tests must account
for the ungated violations still firing. For example, a file with both `stream().count()` (no gate)
and `stream().forEach()` (API 24+) should expect 2 violations at minSdk 23 (only forEach suppressed)
and 3 at minSdk 24 (all fire).

### Clean file coverage

For check tests: the clean file is just as important as the violation file. It must cover every
pattern that should NOT trigger the check. If a check has special handling for `default:` labels
(not just `case:`), the clean file needs `default:` examples.

### AST structure awareness

Checkstyle's AST doesn't always match intuition. `do-while` bodies are the first child of
`LITERAL_DO` (before the condition), while `if`/`while`/`for` bodies follow `RPAREN`. This caused
`PreferPrefixIncrementCheck` to miss braceless `do` loops (documented in
`docs/prefer-prefix-increment-braceless-do.md`).

When a test unexpectedly produces zero violations, check the AST structure with Checkstyle's
`-t` flag or by adding debug prints in the check's `visitToken()`.

## Common mistakes

### Hardcoded value sets instead of parsing

The original `ExplicitInitializationFixer` used `Set.of("0", "0L", "0.0f", ...)` to match default
values. This missed `0.000`, `0x0`, `0_0`, `0.0e0`, and many other valid zero representations. The
fix was to parse the value (strip suffix, underscores, prefix, then verify all remaining chars are
zero).

When matching against a set of known values, ask: "is this set exhaustive, or should I parse the
structure instead?"

### Missing lowercase `l` suffix

`RedundantNumericSuffixFixer` originally checked `D/F/L/d/f` but not `l`. The boundary pairing
audit caught it: adding a test for `100l` revealed the bug. This is why boundary pairing matters,
it catches omissions that look correct at a glance.

### Method ordering in test resource files

Test resource files (.java) are checked by `checkstyleTestResources` which runs the project's own
checkstyle rules on them. Methods in test resources must be alphabetically ordered (per
`MethodAlphabeticalOrderCheck`), blank lines after `break;` are required before `case`/`default`
(per the `BlankLineAfterBreak` regex rule), and no blank lines after braced cases (per
`NoBlankLineBetweenSingleCasesCheck`).

Run `./gradlew check` (not just `test`) after any change to catch these.

## Running tests

Always use `./gradlew check`, never a subset like `test` or `checkstyleMain checkstyleTest`. The
full `check` task includes `checkstyleTestResources` and `validatePlugins` which catch issues in
test resource files and plugin metadata that other tasks miss.