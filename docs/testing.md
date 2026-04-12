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

assertEquals(3,violations.size());

assertEquals(10,violations.get(0).

getLine());

assertEquals("Expected message.",violations.get(0).

getMessage());
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

## Coverage philosophy

**If it's not tested, it's not supported.** Never assume code works correctly without a test proving
it. "The code doesn't reference that token type so it can't fire" is not a valid argument. The only
proof is a test that runs the check against that input and asserts the expected result. If a check
interacts with a concept (init blocks, enums, records, lambdas, anonymous classes, etc.), there must
be a test for it, even if you believe the code path is unreachable. Beliefs are not tests.

## Coverage checklist

Use this as a **driving process while writing code**, not a post-hoc audit. Do not write a check,
then write a fixer, then write tests. Instead: write one branch of the check, write its tests, then
the next branch and its tests, then the fixer and its tests.

### Before writing any code (BLOCKING - do not skip or defer)

These steps produce the test matrix. No code is written until the matrix is complete and reviewed.

1. List every AST token type the check will visit and every expression/body type it will classify
2. If the check has categories (tiers, modes, severity levels), build the full NxN permutation
   matrix
   (see "Permutation matrix" below). Write out every cell explicitly
3. For each category/token type, list: clean example, violation example, boundary example (nearby
   value that should NOT fire). Be specific - write actual code snippets, not descriptions
4. For each violation type (message key), repeat step 3
5. List every boundary dimension (what single change flips one category to another). For each
   dimension, write the clean-side snippet and the violation-side snippet side by side
6. If a token type cannot appear in test resource files (blocked by another check), note it as
   requiring a direct AST unit test
7. If the check has a related check that partitions contexts, list the cross-check file pairs
8. Write all of the above into the plan. Do not proceed to code until every cell, boundary, and
   token type has a planned test. This is the most important step. Gaps in the matrix become gaps in
   coverage. Filling the matrix after writing code is post-hoc rationalization, not test-driven
   development

### While writing code (interleaved - never batch)

9. Write ONE branch of the check. Immediately write the clean and violation test resource lines for
   that branch. Immediately write the check test assertions. Run `./gradlew check`. Only then write
   the next branch
10. After completing ALL check branches: write the fixer. For each `return null`, immediately write
    the fixer unit test. For each `return new FixResult(...)`, immediately write the fixer unit
    test.
    For each fix type, immediately write the integration test. Run `./gradlew check` after each test
11. Write direct AST unit tests for any token types identified in step 6

### After all code is written (exhaustive audit - do not skip)

12. Run the 6-step exhaustive audit (see "Exhaustive audit process" below). This is not optional. Do
    not declare done, do not say "coverage is complete", do not move on until every step produces
    zero gaps
13. Trace every cell of the matrix from step 2 to a specific test method name. If any cell maps to
    "tested implicitly" or "same code path as X", that cell is NOT covered - add an explicit test
14. Verify every violation test asserts all three: exact line number, severity level, AND message
15. Run `./gradlew check` one final time

### Permutation matrix (for checks with tiers, categories, or modes)

When a check classifies inputs into categories (e.g., tiers, severity levels, format modes) and
enforces a specific format per category, build an NxN permutation matrix: actual category vs.
formatted-as category. Every cell must be tested.

Example: `ControlFlowBracesCheck` has 3 do-while tiers (tier 1 = all one line, tier 2 = body on do
line + while split, tier 3 = body on own line). The matrix is:

| Actual \ Written as | Tier 1    | Tier 2    | Tier 3    |
|---------------------|-----------|-----------|-----------|
| **Tier 1**          | Clean     | Violation | Violation |
| **Tier 2**          | Violation | Clean     | Violation |
| **Tier 3**          | Violation | Violation | Clean     |

The diagonal is clean (correct format). Every off-diagonal cell is a violation. **Every cell needs
three layers of coverage:**

1. **Check test**: clean file for diagonal, violation file for off-diagonal, with exact
   line/severity/
   message assertions
2. **Fixer unit test**: for every off-diagonal cell, a unit test that inputs the wrong format and
   asserts the fixer produces the correct format
3. **Integration test**: for every off-diagonal cell, an end-to-end test running the full pipeline

Do not declare coverage complete until every cell has all three layers. If a check has N categories,
that is N^2 cells to fill (N clean + N^2 - N violation/fixer). Trace each cell to specific test
method names.

### Direct AST unit tests (for token types blocked by other checks)

When a token type (e.g., `POST_INC`/`POST_DEC`) cannot appear in test resource files because
another check (e.g., `PreferPrefixIncrementCheck`) would flag it, write direct AST unit tests
instead. Parse a temp file with `JavaParser.parse()`, walk the AST to find the relevant node, and
call the check's package-private methods directly.

```java
// example: test POST_INC directly via AST
var source = "class T { void f(int x) { do x++; while (x > 0); } }";
var tmp = File.createTempFile("test", ".java");
Files.writeString(tmp.toPath(), source);
var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
var doBody = findFirst(ast, TokenTypes.LITERAL_DO).getFirstChild();
assertTrue(SomeCheck.isSimpleExpression(doBody));
```

This is the only way to achieve full token-type coverage when test resource checkstyle rules
conflict. Use this for every token in a `switch` or `if` chain that shares a code path with other
tokens but has a distinct AST type. Same code path does not mean same coverage: a future edit could
accidentally drop one token from the case list.

### Exhaustive audit process

After all code and tests are written, run this audit before declaring done. Do not skip it.

**Step 1: Branch trace.** For every `if`, `switch case`, `else`, and early `return` in the source,
write down:

- The condition
- The test exercising the TRUE path (file:line or test method name)
- The test exercising the FALSE path
  Mark MISSING if either path lacks a test. Fix before proceeding.

**Step 2: Token type trace.** For every token type in a `switch` or `if` chain, verify there is a
dedicated test (either in test resources or direct AST unit test). Do not rely on shared case arms
as proof of coverage.

**Step 3: Permutation matrix trace.** If the check has categories (step 5 above), verify every cell
has check test + fixer unit test + integration test.

**Step 4: Boundary pair trace.** For every dimension that causes a category transition (e.g., "has
DOT" flips tier 1 to tier 2), verify both sides of the boundary are tested: a clean case just
inside the boundary and a violation/clean case just outside.

**Step 5: Fixer return path trace.** For every `return null` and every `return new FixResult(...)`
in the fixer, name the unit test that exercises it. Skip only paths that are provably unreachable
(called from a context that already validated the precondition).

**Step 6: Edge case trace.** For the specific AST token the check handles, list structural edge
cases (empty body, nested constructs, construct inside other constructs, braced vs. unbraced
variants). Verify each is in the clean or violation file.

If any step reveals a gap, fix it immediately. Then re-run the full audit from step 1 (gaps often
cascade). Only after a full clean pass through all 6 steps is coverage complete.

### Context coverage (for checks that handle multiple AST contexts)

When a check registers for multiple token types or handles multiple parent contexts (e.g. CLASS_DEF,
METHOD_DEF, VARIABLE_DEF), every context must appear in all three test categories:

1. **Clean file**: a correctly formatted example that produces zero violations
2. **Violation file**: an incorrectly formatted example that produces the expected violation
3. **Fixer test**: at least one integration test per fix type (not per context, since fixers are
   text-based and context-agnostic)

If a check has multiple violation types (e.g. same-line placement, blank lines, alphabetical order),
each violation type needs its own set of clean + violation examples. Use separate violation files
per
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
assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR +"Clean.java").

isEmpty());

assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR +"Violation.java").

isEmpty());
// ... and vice versa for every file
```

### Branch coverage

Trace every `if`, `switch`, `while` condition, and early `return` in the code. Each branch must
have a dedicated test. After writing code, re-read it and confirm every conditional has a test that
exercises both the true and false paths.

### Multi-item permutation coverage

When a check processes multiple items in a group (e.g., multiple lambda params, multiple fields in
a declaration, multiple annotations) and the check's behavior depends on the GROUP state (like
"does any item have an annotation?"), test every permutation of per-item states. This is not the
same as branch coverage -- the code paths are identical, but the data flows through different
iterations.

Example: `LambdaParameterTypeCheck` has `anyAnnotated` logic that inspects ALL params to decide
whether to suggest `var` vs implicit. For a 2-param lambda, these cases are distinct:

| First param   | Second param  | Expected          |
|---------------|---------------|-------------------|
| `@A String x` | `String y`    | Both MSG_VAR      |
| `String x`    | `@A String y` | Both MSG_VAR      |
| `@A String x` | `@B String y` | Both MSG_VAR      |
| `String x`    | `String y`    | Both MSG_IMPLICIT |

The first three share the same code path (anyAnnotated=true), but testing only case 1 missed a
fixer bug where non-annotated params in an annotated context were stripped to implicit instead of
getting `var`. Case 2 caught it because the fixer iterated params left-to-right and handled the
non-annotated first param differently.

**Rule**: when code loops over a group and sets a flag from any member, test the flag-setting member
in every position (first, middle, last, all).

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

### Annotation placement coverage

When a check inspects types, fields, parameters, or declarations, test every position where
annotations can appear in the AST. Annotations are syntactically valid in many positions but land
in different AST nodes depending on placement:

- **Declaration annotations** (`@Nonnull String field`): on MODIFIERS, not TYPE. The TYPE node is
  unchanged. Test that the check produces the same result with and without the annotation.
- **Type-use annotations in generics** (`List<@Ann String>`): inside TYPE_ARGUMENTS > TYPE_ARGUMENT
  > ANNOTATIONS. Test with annotations on some/all type arguments.
- **Annotation asymmetry**: when comparing field types to parameter types, annotations on one side
  but not the other should not affect type matching (annotations are on MODIFIERS, not TYPE).

For any check that reads TYPE nodes, the annotation permutation matrix is:

| Field annotation | Param annotation | Generic type-arg annotation |
|------------------|------------------|-----------------------------|
| None             | None             | None                        |
| Yes              | Yes              | -                           |
| Yes              | None             | -                           |
| None             | Yes              | -                           |
| -                | -                | Yes (on some args)          |
| -                | -                | Yes (on all args)           |

For declaration annotations (fields, parameters), use `@Deprecated` or `@SuppressWarnings("unused")`
which are built-in and need no imports. For type-use positions (`List<@Ann String>`), these don't
work since they lack `@Target(TYPE_USE)`. Define a `@interface Ann {}` only when you need type-use
annotations. A plain `@interface` with no `@Target` can be used in any position.

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
message-last), each convention needs its own integration test combining multiple patterns in a
single
file. This verifies that fixers for one convention don't silently mangle lines belonging to the
other.
A single-pattern test per convention is not enough because interference only surfaces when multiple
fixer code paths run against the same file.

### Fixer regex robustness

When a fixer uses regex to match code patterns, test for these attack vectors:

**False positives from partial matches.** `Matcher.find()` matches anywhere in the line. A regex
for `a > b ? a : b` will also match inside `++a > b ? a : b`, producing `++Math.max(a, b)`.
Test with prefix and suffix characters around the pattern. Use negative lookbehind to reject
unwanted prefixes.

**Nested expressions.** Regex character classes like `[^,]+` and `[^)]+` split at the FIRST
delimiter, not at the correct nesting depth. `Math.max(a, Math.min(b, foo(c, d)))` breaks
because `[^)]+` captures `foo(c, d` (stops at the inner `)` instead of the outer). Test with
nested method calls, casts, and parenthesized expressions in every argument position. Use
paren-balanced parsing instead of regex for nested structures.

**ReDoS (catastrophic backtracking).** Verify that no regex has overlapping alternatives with
nested quantifiers (e.g. `(a+)+`, `(a|a)+`). Test with long inputs (100+ character operands)
to confirm linear-time matching. Character classes like `[\w.\[\]]+` are safe (single class,
no alternation). Alternations like `>=?|<=?` are safe (no overlap, fail fast).

**Multiline.** If the check can fire on expressions that span multiple lines, test that the
fixer either handles it or returns null gracefully. Never produce a partial fix that corrupts
the file.

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

See `docs/ast-structure.md` for the full reference on Checkstyle's AST quirks (ternary children
order, INDEX_OP's hidden RBRACK, comparison operators as parent nodes, pre vs post increment
semantics, `exprText` vs `displayText`, etc.).

When a test unexpectedly produces zero violations, check the AST structure with Checkstyle's
`-t` flag or by adding debug prints in the check's `visitToken()`.

### Numeric literal permutations

When a check or fixer handles numeric literals (zero detection, suffix removal, etc.), test ALL
Java literal forms. Missing even one form is a bug. The full set:

**Integer forms**: `0`, `0x0` (hex), `0X0` (hex uppercase prefix), `0b0` (binary), `0B0` (binary
uppercase prefix), `0_0` (underscore separator)

**Long forms**: every integer form with `L` suffix (e.g. `0L`, `0x0L`, `0b0L`)

**Float forms**: `0.0f`, `0f` (no decimal), `0.f` (trailing dot)

**Double forms**: `0.0`, `0.0d` (explicit suffix), `0.` (trailing dot), `.0` (leading dot),
`0.0e0` (exponent), `0.0e+0` (exponent with plus), `0.0e-0` (exponent with minus)

**Negative forms**: every form above prefixed with `-`. In the AST, `-0` is `UNARY_MINUS{NUM_INT}`
(two nodes), not a single negative literal. Tests must verify both that `isZeroLiteral(UNARY_MINUS)`
returns false and that `isZeroLiteral(child NUM token)` returns true.

**Boundary pairs**: for every zero form, test a non-zero value with the same notation (e.g. `0x0`
is zero, `0x1` is non-zero; `0.0e0` is zero, `0.0e1` is non-zero).

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

### Violation comments

Violation comments in test resource files MUST use exact message text:
`// violation: <exact AuditEvent.getMessage() text>` for errors,
`// violation (warning): <exact message>` for warnings.
Never use shorthand, paraphrases, or descriptions. The comment IS the expected message.

- Clean files must have ZERO violation comments
- Violation files must have a comment on EVERY violation line
- No clean cases in violation files, no violation cases in clean files

### Assertion completeness

Every test assertion on a violation MUST check all three: line number, severity level, AND message.
Never assert just line + count. Never assert line without severity. Never skip message validation
on "obvious" cases. Every single violation gets all three checks, no exceptions.

## Running tests

Always use `./gradlew check`, never a subset like `test` or `checkstyleMain checkstyleTest`. The
full `check` task includes `checkstyleTestResources` and `validatePlugins` which catch issues in
test resource files and plugin metadata that other tasks miss.