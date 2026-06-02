# PreferMathMethodCheck auto-fix coverage

## Ternary (max/min/abs)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `a > b ? a : b` (4 operator variants) | `Math.max(a, b)` | Yes |
| `a < b ? a : b` (4 operator variants) | `Math.min(a, b)` | Yes |
| `a < 0 ? -a : a` (8 variants) | `Math.abs(a)` | Yes |
| `--a > b ? a : b` (prefix mutation) | `Math.max(--a, b)` | Yes |
| `(a) > (b) ? (a) : (b)` (parenthesized) | `Math.max(a, b)` | No (regex limitation) |
| Multiline ternary | `Math.max(a, b)` | No (single-line fixer) |

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