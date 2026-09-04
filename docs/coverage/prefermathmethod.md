# PreferMathMethodCheck auto-fix coverage

A qualified receiver such as `MyMath.max(` is not taken for a `Math.max(` clamp.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| Multiline ternary (`?` or `:` opening its own line) | `Math.max(a, b)` | Yes, folded onto the line the expression starts on |
| Multiline ternary with a comment inside the span | `Math.max(a, b)` | No (skipped: `cannot rewrite the expression: nothing here is a supported Math shape, or folding it would lose a comment`), since folding would move code onto the comment's line |
| Ternary-shaped text in a comment or string literal on the reported line | n/a | No, never matched |
| Ternary split by a comment between its operands (`a /* n */ > b ? a : b`) | `Math.max(a, b)` | No, refused so the comment survives |
| `a < 0.0 ? -a : a` (floating-point zero) | `Math.abs(a)` | No (skipped: `cannot rewrite the comparison: Math.abs and the branches it replaces differ on -0.0, so a floating-point zero is left alone`). The check still reports it; see [the TODO](../todo/float-nan-math-methods.md) |

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
| Operand with a non-ASCII identifier character | `Math.max(α, b)` | Yes, the operand class is the parser's |
| `c > 'a' ? c : 'a'` (char-literal operand) | `Math.max(c, 'a')` | Yes |
| `a > 1.5e-3 ? a : 1.5e-3` (signed exponent) | `Math.max(a, 1.5e-3)` | Yes |
| `a < 0x0 ? -a : a` (any integral zero spelling: hex, binary, underscores, `0L`) | `Math.abs(a)` | Yes |
| Chained `a > b ? a : b > c ? b : c`, inner ternary reported | `Math.max(b, c)` | Yes, the inner ternary is rewritten in place |

## Clamp (minSdk >= 35)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Math.max(lo, Math.min(hi, val))` | `Math.clamp(val, lo, hi)` | Yes |
| `Math.min(hi, Math.max(lo, val))` | `Math.clamp(val, lo, hi)` | Yes |
| Reversed arg order (inner call first) | `Math.clamp(val, lo, hi)` | Yes |
| Nested calls in args (e.g. `foo(a, b)`) | `Math.clamp(foo(a, b), lo, hi)` | Yes |

The clamp rewrite applies no purity or type test, so three divergences are accepted rather than
gated. All three are tracked in
[the audit backlog](../todo/TODO-prefer-math-method-audit-backlog.md): argument **evaluation order**
changes from `A, B, C` to `C, A, B`; **inverted or NaN bounds** make `Math.clamp` throw
`IllegalArgumentException` where the source form returned a value; and a **`long` value with `int`
bounds** narrows the expression's static type to `int`.

Clamp arguments may be arbitrary expressions (casts and ternaries included). The applied fix
reproduces them verbatim from the source, including a comment written between an argument and its
comma, which travels with the argument to its new position. The violation message re-renders them
from the parsed form, which is faithful for ordinary casts and ternaries but imprecise for exotic
argument syntax (an `instanceof`-pattern ternary, an intersection/annotated cast type, or a call
with explicit type arguments, whose method name the renderer drops). This affects the message text
only, never the applied fix.

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
| `int s = a, r = b; if (a > b) r = a; ...` (multi-declarator above the if) | n/a | No (skipped: `cannot rewrite the if-else: this shape is not supported`), because every declarator of such a statement shares the one type's position, so rewriting any but the first would delete the others. When the target is not the last declarator (`int r = b, s = a;`) the check does not flag it |
| `int r = 7; if (a > b) r = a; else r = b; return r;` (declaration above carries an initializer) | `r = Math.max(a, b);` | Yes, but the declaration stays: only one the if-else assigns in every branch is folded away |
| `if (a > b) r = a; else r = b; return r + 1;` (the following return is not of the target) | `r = Math.max(a, b);` | Yes, without collapsing |
| Field/array assignment target (`this.x`, `arr[i]`) | same as above | Yes |
| `else if (a > b) return a; else return b;` | `return Math.max(a, b);` | Yes, with the `else` kept on its own line |
| `if (a < 0x0) r = -a; else r = a;` (any integral zero spelling: hex, binary, underscores, `0L`) | `r = Math.abs(a);` | Yes |
| `double a; if (a < 0.0) r = -a; else r = a;` (floating-point zero) | `Math.abs(a)` | No (skipped: `cannot rewrite the comparison: Math.abs and the branches it replaces differ on -0.0, so a floating-point zero is left alone`) |
| `double a, b; if (a > b) r = a; else r = b;` (floating-point max/min) | `r = Math.max(a, b);` | Yes, **and divergent**: the if-else yields `-0.0` for `(0.0, -0.0)` and the non-NaN operand for a NaN pair, where `Math.max` yields `0.0` and `NaN`. No type test gates it; see [the TODO](../todo/float-nan-math-methods.md) |

All if-else patterns above accept any single-statement body under any brace layout, the
`{` cuddled or on its own line on either side, and the condition or a branch value may run
across lines: the construct is resolved from the parse rather than from the lines it occupies,
and the replacement is folded onto the line the statement starts on. A blank line anywhere
inside the span (before the `else`, before the trailing `return`, between the declaration and
the `if`) is absorbed. Multi-statement bodies are not flagged by the check.

Every comment the fold would otherwise lose is merged, in source order, into one `//` comment
trailing the emitted statement; a comment after the last branch is pulled in the same way, and
one written *above* the span stays where it is. This includes a comment written between the
condition's own operands, which the expression forms refuse rather than relocate: a ternary's
replacement is an expression that may sit anywhere on its line, while the if-else's is a whole
statement with a line of its own to trail a comment on. The only refusals are these:

| Refusal | Why |
| --- | --- |
| Code before the `if` on its own line (a labelled statement, a nested one-line `if`) | the replacement would be cuddled with a statement this fixer did not write. An `else`, with or without the previous branch's closing `}` cuddled ahead of it, is the one exception, and keeps its line |
| A fold that would exceed the project's line length | nothing downstream wraps it, and the fold is what made it long |
| A multi-declarator declaration the rewrite would have to consume | see the table row above |
| A declaration head or assignment target split across lines by a comment (`var /* n */` then `r = b;`, `r[/* i */` then `i] = a;`) | that text is copied verbatim into the replacement, and joining it onto one line would lose the comment |
| A comment inside the construct when the span's own last line opens a block comment or text block | the relocated comment has nowhere to go: appending it after an open literal would swallow it |

An `if` whose block belongs to an old-style `switch` label is rewritten, but its declaration and
trailing `return` are never folded away: such a block is not a scope of its own, so a later label
can still name a local the `return` makes unreachable there.

Part of [auto-fix coverage](../auto-fix-coverage.md).