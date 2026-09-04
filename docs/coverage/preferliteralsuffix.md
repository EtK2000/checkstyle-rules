# PreferLiteralSuffixCheck auto-fix coverage

Deletes a widening cast and appends the matching suffix to the integer literal the check paired it
with.

The rewritten region is the operator or ternary the cast is an operand of, emitted as a single line:
a wrap written inside it does not survive. The region stops there rather than widening to the
enclosing statement, so a wrap elsewhere in the same statement is left alone. The cast's text is
deleted up to the subject's first token, not up to the cast's own closing parenthesis, so
parentheses around the subject survive.

## Supported (auto-fixable)

| Pattern | Fix |
| --- | --- |
| `(long)`/`(float)`/`(double)` cast LHS of binary op with int-literal RHS | Remove cast, append `L`/`f`/`d` to the RHS literal |
| `(long)`/`(float)`/`(double)` cast RHS of binary op with int-literal LHS | Remove cast, append suffix to the LHS literal |
| Ternary `cond ? (Type) X : LIT` (cast in true branch) | Remove cast, append suffix to false-branch literal |
| Ternary `cond ? LIT : (Type) X` (cast in false branch) | Remove cast, append suffix to true-branch literal |
| Signed literals (`-100`, `+100`, `- 100`) as the unary-wrapped int-literal sibling | Preserved verbatim; suffix appended after the digits |
| Hex literals where the int value is non-negative (e.g. `0xFF`, `0x7FFFFFFF`), under an `L` suffix | Suffix appended after the digits |
| Binary literals where the int value is non-negative (e.g. `0b1010`, `0b1010_1010`), under an `L` suffix | Suffix appended after the digits |
| Octal, underscore-separated, and boundary decimal literals (`010`, `1_000`, `2147483647`, `-2147483648`) | Suffix appended after the digits |
| Cast subject other than a plain identifier, where the file proves its type: parentheses, field access, array access, nested array access, a same-file method call, nested cast, unary `-`/`~`, prefix increment, an operator expression over proven operands | Fixed; the subject's own text is re-emitted unchanged |
| Cast spanning multiple lines, or ending a line with its subject below | Fixed; the region folds onto one line |
| Operator or literal on a later line than the cast | Fixed; the region folds onto one line |
| Cast inside a lambda body, an anonymous class, a static initializer, a `for` condition, a braceless `if` body, a switch rule, a ternary condition, or a multi-declarator | Fixed |
| Comment ahead of the region, or between the subject and the rest of the expression on one line | Survives in place |
| Comment inside the cast's parentheses, between the cast and a subject on the next line, or anywhere the fold consumes | Fixed; the displaced comments merge into one comment trailing the rewritten line |
| Two casts on one line | Both fixed, one per pass |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason check does not fire |
| --- | --- |
| Cast that is not an operand at all (`takesLong((long) x)`, a `return`, an assignment RHS, an array index) | There is no other operand to carry a suffix |
| Shift operators (`<<`, `>>`, `>>>`) | JLS 15.19: shift result type/masking is decided by the LHS operand only. Swapping cast for suffix would change behavior |
| Compound assignment (`v *= (long) x`, `v <<= (long) x`) | The parent is an assignment, not a binary-numeric-promotion operator |
| Hex/binary int literal with bit 31 set (`0x80000000`, `0xFFFFFFFF`, `0b10000000_00000000_00000000_00000000`) | Int value sign-extends to a different long than the same digits as a long literal |
| Non-widening casts (`(int)`, `(byte)`, `(short)`, `(char)`), in either operand position or either ternary branch | No suffix exists for these types |
| Reference, generic and array cast types (`(Integer) o`, `(List<Integer>) raw`, `(long[]) raw`) | The cast type is not a primitive, so no suffix applies |
| Type-use-annotated cast type (`(@Ann long) x * 100`) | The annotation stands where the type name is read, so the cast type is never recognized |
| Other operand is already typed (long, double, or float literal, expression, or identifier) | No int literal to attach a suffix to |
| Doubly-signed literal (`(long) x * - -100`) | Behind two signs, the operand is not read as an int literal |
| Cast wraps the whole expression (`(long) (x * 100)`) instead of a single operand | The literal is not a direct binary-op sibling of the cast |
| Cast whose subject this file cannot prove widens to the cast target | The check's premise is that the cast only widens, and the cast type alone does not say so: `(long) aDouble * 100` narrows, and `aDouble * 100L` is a `double` expression with a different value that does not even assign to a `long`. Operator expressions are typed by Java's own promotion rules, so `(long) -aDouble * 100` and `(long) (x * 1.5) * 100` are refused too. **An unproven subject is refused as well**: "this file could not type it" is no evidence the cast widens. That is deliberate, and it is why the check stays silent on a subject reached through a type absent from checkstyle's classpath (`(long) sb.append("a").length() * 100`, `(long) new StringBuilder("ab").length() * 100`), on a call taking a lambda, and on an identifier with no visible declaration. A missed report costs a style nit; a wrong rewrite costs a broken build |
| Hex, binary or octal literal under a `d`/`f` suffix (`(double) x * 0xFF`, `* 0b1010`, `* 010`) | `d` and `f` are hex digits, so the suffix joins the literal's own digits: `0xFF` is 255 but `0xFFd` is the int 4093, `0b1010d` does not compile, and `010` is octal 8 while `010d` is 10.0. `L` is unaffected |
| `-0` under a `d`/`f` suffix (`(double) x * -0`) | `-0` is the int zero, which the cast widens to `+0.0`, while `-0d` is a negative zero: `x * 0.0` and `x * -0.0` differ in the sign of the product |
| Grouping parentheses around the cast in first-operand position (`((long) x) * 100`) or around the literal (`(long) x * (100)`) | The parentheses stand where the check expects the cast or the literal. Measured: this is position-dependent, and `100 * ((long) x)` **does** fire, because there the literal is still the operator's first operand. The rewrite would be valid in all three, so this is a false negative rather than a guard |
| Nested ternary or parenthesized branch as the other branch (`flag ? (long) x : flag2 ? 0 : 1`) | The other branch is a conditional or a parenthesized expression, not an int literal |

## Not supported

| Pattern | Reason |
| --- | --- |
| Buffer does not parse, or no reportable cast at the position | Nothing to classify, so no fix is attempted |
| Text block or comment inside the part of the region that folds | Folding would strip the line break a text block's opening delimiter needs, or comment out the folded code (`cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite`) |
| Displaced comment with nowhere to go, because the line's tail opens a literal | Dropping it is the one way the rewrite could lose text silently (same message) |
| Rewritten line longer than the project's line budget | Nothing downstream wraps it, and the fold is what made it long (`cannot replace the cast with a suffix: the rewritten expression would exceed the line length limit`) |

## Interaction with other fixers

A nested same-type cast (`(long) (long) x * 100`) leaves `(long) x * 100L` behind, which
`RedundantCastCheck` reports and `RedundantCastFixer` then deletes, so the pipeline settles on
`x * 100L`.

A fix inside a `for`-init whose declarator `PreferVarCheck` can prove
(`for (int i = 1; (long) i < 100; ++i)`) also has `int i` rewritten to `var i` in the same run.

`RedundantNumericSuffixCheck` reads through a conditional to its arms, so it reports the suffix this
fixer just added to a ternary arm as redundant and strips it. The net effect of a ternary fix in the
full pipeline is "delete the cast", not "move the suffix". Binary-operator fixes keep their suffix,
because that check does not read through an arithmetic operator.

Part of [auto-fix coverage](../auto-fix-coverage.md).
