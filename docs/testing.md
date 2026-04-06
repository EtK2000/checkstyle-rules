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

When writing or reviewing tests, verify each of these:

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