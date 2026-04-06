# Prefer `Math` Methods Over Manual Comparisons

## Pattern

Ternary expressions and if-else chains that replicate `Math` utility methods should use the method
instead.

## `Math.max` / `Math.min`

Java 1.0+, API 1+.

```java
// bad
a > b ? a : b
a >= b ? a : b
a < b ? b : a
a <= b ? b : a

// good
Math.max(a, b)
```

```java
// bad
a < b ? a : b
a <= b ? a : b
a > b ? b : a
a >= b ? b : a

// good
Math.min(a, b)
```

Also applies to if-else assignment patterns:

```java
// bad
int result;
if (a > b)
    result = a;
else
    result = b;

// good
int result = Math.max(a, b);
```

## `Math.abs`

Java 1.0+, API 1+.

```java
// bad
a < 0 ? -a : a
a >= 0 ? a : -a

// good
Math.abs(a)
```

## `Math.clamp`

Java 21+, API 35+.

```java
// bad
Math.max(min, Math.min(max, value))
Math.min(max, Math.max(min, value))

// good
Math.clamp(value, min, max)
```

## `Math.addExact` / `Math.subtractExact` / `Math.multiplyExact` / `Math.negateExact`

Java 8+, API 24+.

These are overflow-safe arithmetic. Not a ternary replacement, but worth flagging when code does
manual overflow checks:

```java
// bad
if (a > 0 && b > Integer.MAX_VALUE - a) throw new ArithmeticException();
int result = a + b;

// good
int result = Math.addExact(a, b);
```

This is more complex to detect and may not be worth a check. Listing for completeness.

## Scope

A `PreferMathMethodCheck` could cover `max`, `min`, `abs`, and `clamp`. The ternary form is the
most common and easiest to detect via AST (look for `QUESTION` nodes with comparison conditions and
operands matching the branches). The if-else form is harder but still feasible.

Auto-fix is straightforward for ternary expressions. If-else assignment would need multi-line
restructuring.

## Edge cases

- Operands with side effects: `a++ > b ? a : b` is NOT equivalent to `Math.max(a++, b)`. Only flag
  when both operands are pure (identifiers, field accesses, constants, or other pure expressions).
- Floating-point: `Math.max(double, double)` handles NaN differently than a ternary. `NaN > x` is
  false, so `a > b ? a : b` returns `b` when `a` is NaN, but `Math.max(a, b)` returns NaN. This
  only matters for float/double, not int/long.
- `Math.clamp` needs version gating (Java 21+ / API 35+).
- `Math.addExact` etc. need version gating (Java 8+ / API 24+).