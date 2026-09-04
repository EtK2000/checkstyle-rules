# Test-matrix dimensions

Part of the [testing guide](../testing.md). Read this when building or auditing a test matrix (the
BLOCKING "Before writing any code" step): it lists the input dimensions a matrix must enumerate.

## Permutation matrix (for checks with tiers, categories, or modes)

When a check classifies inputs into categories (e.g., tiers, severity levels, format modes) and
enforces a specific format per category, build an NxN permutation matrix: actual category vs.
formatted-as category. Every cell must be tested.

Example: `ControlFlowBracesCheck` has 2 do-while tiers (tier 2 = body on do line + while on next
line, tier 3 = body on own line). The matrix is:

| Actual \ Written as | Tier 2    | Tier 3    |
|---------------------|-----------|-----------|
| **Tier 2**          | Clean     | Violation |
| **Tier 3**          | Violation | Clean     |

**Every cell needs three layers of coverage:**

1. **Check test**: clean file for diagonal, violation file for off-diagonal, with exact
   line/severity/message assertions
2. **Fixer unit test**: for every off-diagonal cell, a unit test that inputs the wrong format and
   asserts the fixer produces the correct format
3. **Integration test**: for every off-diagonal cell, an end-to-end test running the full pipeline

Do not declare coverage complete until every cell has all three layers. If a check has N categories,
that is N^2 cells to fill (N clean + N^2 - N violation/fixer). Trace each cell to specific test
method names.

## Multi-item permutation coverage

When a check processes multiple items in a group (e.g., multiple lambda params, multiple fields in
a declaration, multiple annotations) and the check's behavior depends on the GROUP state (like
"does any item have an annotation?"), test every permutation of per-item states. This is not the
same as branch coverage: the code paths are identical, but the data flows through different
iterations.

Example: `LambdaParameterTypeCheck` has `anyAnnotated` logic that inspects ALL params to decide
whether to suggest `var` vs implicit. For a 2-param lambda, these cases are distinct:

| First param   | Second param  | Expected          |
|---------------|---------------|-------------------|
| `@A String x` | `String y`    | Both MSG_VAR      |
| `String x`    | `@A String y` | Both MSG_VAR      |
| `@A String x` | `@B String y` | Both MSG_VAR      |
| `String x`    | `String y`    | Both MSG_IMPLICIT |

The first three share the same code path (anyAnnotated=true), but a fixer that iterates params
left-to-right can still handle the non-annotated param differently depending on its position.

**Rule**: when code loops over a group and sets a flag from any member, test the flag-setting member
in every position (first, middle, last, all).

## Boundary pairing

For every value a function accepts, write a rejection test with a "nearby" invalid value. This
catches over-matching.

Examples from this project:

- `ExplicitInitializationFixer` accepts `0.0f` (zero) → must reject `1.0f` (non-zero)
- `ExplicitInitializationFixer` accepts `'\u0000'` → must reject `'\u0001'`
- `DoubleBlankLineFixer` accepts 2 consecutive blanks → must reject 1 blank (the boundary)
- `OverloadMethodOrderCheck` clean file shows `secondParam(int a, char b)` before
  `secondParam(int a, int b)` → violation file shows the reverse

## Axis coverage

When code handles multiple independent dimensions (prefixes, suffixes, formats, label types),
test each axis. Cross-axis tests are only needed when axes interact.

Example: `ExplicitInitializationFixer.isNumericZero()` has axes for prefix (`0x`, `0b`, none),
suffix (`L`, `f`, `d`, none), and format (decimal, exponent, underscore). Each is tested
independently. The only cross-axis test is `0x0.0p0f` because hex prefix changes which exponent
letter (`p` vs `e`) is valid.

## Context coverage (for checks that handle multiple AST contexts)

When a check registers for multiple token types or handles multiple parent contexts (e.g. CLASS_DEF,
METHOD_DEF, VARIABLE_DEF), every context must appear in all three test categories:

1. **Clean file**: a correctly formatted example that produces zero violations
2. **Violation file**: an incorrectly formatted example that produces the expected violation
3. **Fixer test**: at least one integration test per fix type (not per context, since fixers are
   text-based and context-agnostic)

If a check has multiple violation types (e.g. same-line placement, blank lines, alphabetical order),
each violation type needs its own set of clean + violation examples. Use separate violation files
per violation type to keep them manageable.

**Boundary contexts**: the clean file must also include examples of contexts the check should NOT
fire on. For example, if a check handles VARIABLE_DEF but skips for-each variables, the clean file
needs an annotated for-each variable to prove it is not flagged.

## Expression form coverage

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

## Annotation placement coverage

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

## Numeric literal permutations

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
is zero, `0x1` is non-zero; `0.0e1` is zero (a zero mantissa stays zero for any exponent), `1.0e1`
is non-zero).

## Comparison operator coverage

When a check detects comparisons (e.g. `.size() == 0`, `.trim().length() != 0`), there are 6
comparison operators (EQUAL, GE, GT, LE, LT, NOT_EQUAL) and each has 2 operand orders
(normal and reversed). That's 12 branches total. Test ALL of them, not just the common ones.

The operator determines the semantics (positive vs negated), and the operand order determines
which side has the method call vs the literal. Missing even one branch means that specific
comparison form goes untested. Use a violation file that covers every branch with a dedicated
method.

For fixers handling these comparisons: group them into positive forms (simple replacement) and
negated forms (needs `!` insertion). Test each group separately. For reversed forms (`0 != expr`
vs `expr != 0`), the fixer logic differs (reversed forms extract the receiver from between the
literal and the method suffix, while normal forms scan backwards from the method).

## Single-arg vs multi-arg dispatch

When a check/fixer handles method calls that can have different argument counts (e.g.
`String.format(x)` vs `String.format("fmt", args)`), the behavior may differ by arg count:

- Single-arg: strip the call entirely (`String.format(x)` -> `x`)
- Multi-arg with literal first arg: rewrite (`String.format("fmt", a)` -> `"fmt".formatted(a)`)
- Multi-arg with non-literal first arg: skip (can't safely rewrite)

Test ALL combinations of arg count and arg type. For single-arg, test with literals, variables,
method calls (`obj.toString()`), cast expressions (`(String) obj`), and other complex expressions
to verify the paren-balanced parser handles nested structures correctly.