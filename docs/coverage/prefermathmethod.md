# PreferMathMethodCheck auto-fix coverage

Every rewrite is anchored to the reported violation. The if-else form is reported under its own
message key (`prefer.math.method.if`) at the `if`, which is not a call position, so the fixer goes
straight to the if-else rewrite rather than trying the expression rewrites there. A clamp is
reported at the call's `(`, so `Math.max(` / `Math.min(` must end exactly at the reported column
and the character before the match must not continue a name: a qualified receiver such as
`MyMath.max(` carries `Math.max(` as a suffix and is not taken for one.

The ternary search is anchored at the reported column, column 0 included, and runs over a comment-
and literal-masked copy of the buffer threaded from its top, so comment and literal content can
neither supply a match nor be rewritten. A match that covers masked content is refused rather than
rewritten: the replacement splices the original line, so it would delete the comment the mask stood
in for.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| Multiline ternary (`?` or `:` opening its own line) | `Math.max(a, b)` | No (skipped: `parenthesized or multiline ternary`), at every column |
| Ternary-shaped text in a comment or string literal on the reported line | n/a | No, never matched |
| Ternary split by a comment between its operands (`a /* n */ > b ? a : b`) | `Math.max(a, b)` | No, refused so the comment survives |

## Ternary (max/min/abs)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `a > b ? a : b` (4 operator variants) | `Math.max(a, b)` | Yes |
| `a < b ? a : b` (4 operator variants) | `Math.min(a, b)` | Yes |
| `a < 0 ? -a : a` (8 variants) | `Math.abs(a)` | Yes |
| `--a > b ? a : b` (prefix mutation) | `Math.max(--a, b)` | Yes |
| `(a) > (b) ? (a) : (b)` (parenthesized) | `Math.max(a, b)` | No, the operand scan admits no parenthesis |
| `a > ++b ? a : b` (mutation on either operand) | `Math.max(a, ++b)` | Yes |
| `a > -b ? a : -b` (signed operand) | `Math.max(a, -b)` | Yes |
| `aB_1 > b ? aB_1 : b` (uppercase, underscore) | `Math.max(aB_1, b)` | Yes |
| Operand with a non-ASCII identifier character | `Math.max(α, b)` | No, the operand scan is ASCII, matching the old `\w` |
| Chained `a > b ? a : b > c ? b : c`, inner ternary reported | `Math.max(b, c)` | No, the inner span overlaps the outer one |

## Clamp (minSdk >= 35)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Math.max(lo, Math.min(hi, val))` | `Math.clamp(val, lo, hi)` | Yes |
| `Math.min(hi, Math.max(lo, val))` | `Math.clamp(val, lo, hi)` | Yes |
| Reversed arg order (inner call first) | `Math.clamp(val, lo, hi)` | Yes |
| Nested calls in args (e.g. `foo(a, b)`) | `Math.clamp(foo(a, b), lo, hi)` | Yes |

Clamp arguments may be arbitrary expressions (casts and ternaries included). The applied fix
reproduces them verbatim from the source; the violation message re-renders them from the parsed
form, which is faithful for ordinary casts and ternaries but imprecise for exotic argument syntax
(an `instanceof`-pattern ternary, or an intersection/annotated cast type). This affects the
message text only, never the applied fix.

## If-else (max/min/abs)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `if (a > b) r += a; else r += b;` (compound assign: `+=`, `-=`, `*=`, `/=`, `%=`, `&=`, `\|=`, `^=`, `<<=`, `>>=`, `>>>=`) | `r += Math.max(a, b);` | Yes |
| `var r = b; if (a > b) r = a; return r;` (init-overwrite + trailing return) | `return Math.max(a, b);` | Yes |
| `var r = b; if (a > b) r = a;` (init-overwrite, no trailing return) | `var r = Math.max(a, b);` | Yes |
| `int r; if (a > b) r = a; else r = b; return r;` (decl + assign + return) | `return Math.max(a, b);` | Yes |
| `if (a > b) r = a; else r = b;` (bare assign, no decl/return) | `r = Math.max(a, b);` | Yes |
| `if (a > b) return a; else return b;` (if-else return) | `return Math.max(a, b);` | Yes |
| `if (a > b) return a; return b;` (trailing return, no else) | `return Math.max(a, b);` | Yes |
| `int r = a, s = b; if (a > b) r = a; ...` (multi-decl above the if) | n/a | No (skipped: pattern rejects multi-decls for safety) |
| Field/array assignment target (`this.x`, `arr[i]`) | same as above | Yes |

All if-else patterns above accept any single-statement body under all four brace
combinations: unbraced/unbraced, braced-if/unbraced-else, unbraced-if/braced-else,
braced/braced (own-line `}\nelse {` and cuddled `} else {`). Multi-statement
bodies and `else if` chains remain unsupported (skip reason: `if-else not auto-fixable`).

Part of [auto-fix coverage](../auto-fix-coverage.md).