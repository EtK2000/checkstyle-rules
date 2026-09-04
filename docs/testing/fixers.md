# Fixer test coverage

Part of the [testing guide](../testing.md). Read this when writing, changing, or auditing a fixer
(`*Fixer.java`) or its tests.

## Three-layer fixer coverage

Every fixable pattern needs three layers of tests:

1. **Fixer unit test** (`PreferSpecificApiFixerTest`): test the `fix()` method directly with
   crafted input lines. Cover success cases, return-null guards, boundary rejections, and edge
   cases (nested parens, escaped quotes, complex receivers).
2. **Integration test** (`CheckstyleFixIntegrationTest`): run the full pipeline (Checkstyle
   detection -> column conversion -> fixer application -> output verification -> re-check).
   Assert exact full output, `fixCount`, and `needsAnotherPass`. Every fix-producing test
   automatically re-checks the output via `verifyFixedOutputClean` (enforced by `@AfterEach`).
   If the fix makes an import unused, add a separate `runFixMultiPass` test to verify the
   unused import is cleaned up on the second pass.
3. **Violation/clean test resources**: the check must have violation test cases for every pattern
   the fixer handles, and clean cases for every pattern it should NOT handle.

When adding a new fixer, also check existing integration tests. A new fixer changes the behavior
of any test that relied on the pattern being unfixable, including tests whose input keeps an
import alive only through the now-fixable call.

## Integration coverage

Every entry in `FIXERS` and `MODULE_ID_FIXERS` must have an end-to-end integration test that runs
the full pipeline (Checkstyle detection, column conversion, fixer application, output verification).
Assert the exact full output string, not fragments via `contains()`. This catches unintended
modifications to lines the test wasn't looking at.

When a fixer handles mutually exclusive conventions (e.g. JUnit 4 message-first vs JUnit 5
message-last), each convention needs its own integration test combining multiple patterns in a
single file. This verifies that fixers for one convention don't silently mangle lines belonging to
the other. A single-pattern test per convention is not enough because interference only surfaces
when multiple fixer code paths run against the same file.

## Fixer regex robustness

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

## Negation insertion in fixers

When a fixer needs to negate a result (e.g. `.stream().findFirst().isPresent()` -> `!receiver
.isEmpty()`), use `AstSplice.spliceNegated`, which replaces the call's AST span and puts `!`
before it.

**Critical edge case**: if the expression is ALREADY negated (`!list.stream().findFirst()
.isPresent()`), the existing `!` right before the span must be REMOVED instead of adding another.
Otherwise the output is `!!list.isEmpty()` (double negation). `spliceNegated` handles this, so
test the already-negated form for every negating rewrite.

This applies to any fixer that inserts `!`: trim length comparisons (`!= 0`, `> 0`), reversed
negated forms (`0 != ...`), and `stream().findFirst().isPresent()`.