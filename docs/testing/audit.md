# Exhaustive audit

Part of the [testing guide](../testing.md). Read this before declaring any check or fixer work done
(the "After all code is written" step), and when auditing coverage: it holds the 6-step audit and
the per-branch rules it enforces.

## Exhaustive audit process

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

**Step 3: Permutation matrix trace.** If the check has categories (step 2 of the coverage checklist
in [testing.md](../testing.md)), verify every cell has check test + fixer unit test + integration
test.

**Step 4: Boundary pair trace.** For every dimension that causes a category transition (e.g., "has
chained call" flips tier 2 to tier 3), verify both sides of the boundary are tested: a clean case
just inside the boundary and a violation/clean case just outside.

**Step 5: Fixer return path trace.** For every `return null` and every `return new FixResult(...)`
in the fixer, name the unit test that exercises it. Skip only paths that are provably unreachable
(called from a context that already validated the precondition).

**Step 6: Edge case trace.** For the specific AST token the check handles, list structural edge
cases (empty body, nested constructs, construct inside other constructs, braced vs. unbraced
variants). Verify each is in the clean or violation file.

If any step reveals a gap, fix it immediately. Then re-run the full audit from step 1 (gaps often
cascade). Only after a full clean pass through all 6 steps is coverage complete.

## Branch coverage

Trace every `if`, `switch`, `while` condition, and early `return` in the code. Each branch must
have a dedicated test. After writing code, re-read it and confirm every conditional has a test that
exercises both the true and false paths.

## Severity coverage

When a check emits different severity levels on different code paths (e.g. error for safe cases,
warning for uncertain cases), test each severity explicitly with `assertEquals(SeverityLevel.X,
violation.getSeverityLevel())`. A test that only checks violation count and message will miss a
path that fires at the wrong severity.

## Guard regression

When adding a guard or filter that exempts some inputs from a check, add explicit tests proving
that non-exempted inputs still fire. For example, if a new guard skips `byte x = 5;` (unfixable),
add a test for `int x = 5;` (same structure, should still fire) to prove the guard doesn't
over-suppress. Cover every type/variant that should pass through the guard unchanged.

## Clean file coverage

For check tests: the clean file is just as important as the violation file. It must cover every
pattern that should NOT trigger the check. If a check has special handling for `default:` labels
(not just `case:`), the clean file needs `default:` examples.

## Direct AST unit tests (for token types blocked by other checks)

When a token type (e.g., `POST_INC`/`POST_DEC`) cannot appear in test resource files because
another check (e.g., `PreferPrefixIncrementCheck`) would flag it, write direct AST unit tests
instead. Parse a temp file with `JavaParser.parse()`, walk the AST to find the relevant node, and
call the check's package-private methods directly.

```java
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

## AST structure awareness

See `docs/ast-structure.md` for the full reference on Checkstyle's AST quirks (ternary children
order, INDEX_OP's hidden RBRACK, comparison operators as parent nodes, pre vs post increment
semantics, `exprText` vs `displayText`, etc.).

When a test unexpectedly produces zero violations, check the AST structure with Checkstyle's
`-t` flag or by adding debug prints in the check's `visitToken()`.