# PreferMathMethodCheck auto-fix coverage

A qualified receiver such as `MyMath.max(` is not taken for a `Math.max(` clamp.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| Multiline ternary (`?` or `:` opening its own line) | `Math.max(a, b)` | No (skipped: `cannot rewrite the ternary: it is parenthesized or spans multiple lines`), at every column |
| Ternary-shaped text in a comment or string literal on the reported line | n/a | No, never matched |
| Ternary split by a comment between its operands (`a /* n */ > b ? a : b`) | `Math.max(a, b)` | No, refused so the comment survives |

## Ternary (max/min/abs)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `a > b ? a : b` (4 operator variants) | `Math.max(a, b)` | Yes |
| `a < b ? a : b` (4 operator variants) | `Math.min(a, b)` | Yes |
| `a < 0 ? -a : a` (8 variants) | `Math.abs(a)` | Yes |
| `--a > b ? a : b` (prefix mutation) | `Math.max(--a, b)` | Yes |
| `(a) > (b) ? (a) : (b)` (parenthesized) | n/a | No, the check does not flag parenthesized operands or a parenthesized condition |
| `a > ++b ? a : b` (mutation on either operand) | `Math.max(a, ++b)` | Yes |
| `a > -b ? a : -b` (signed operand) | `Math.max(a, -b)` | Yes |
| `aB_1 > b ? aB_1 : b` (uppercase, underscore) | `Math.max(aB_1, b)` | Yes |
| Operand with a non-ASCII identifier character | `Math.max(α, b)` | No, only ASCII identifier characters are matched |
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
| `int s = a, r = b; if (a > b) r = a; ...` (multi-decl above the if, target declared last) | n/a | No (skipped: `cannot rewrite the if-else: this shape is not supported`). When the target is not the last declarator (`int r = b, s = a;`) the check does not flag it |
| Field/array assignment target (`this.x`, `arr[i]`) | same as above | Yes |

All if-else patterns above accept any single-statement body under all four brace
combinations: unbraced/unbraced, braced-if/unbraced-else, unbraced-if/braced-else,
braced/braced (own-line `}\nelse {` and cuddled `} else {`). `else if` chains remain
unsupported (skip reason: `cannot rewrite the if-else: this shape is not supported`). Multi-statement
bodies are not flagged by the check.

Part of [auto-fix coverage](../auto-fix-coverage.md).