# PreferLiteralSuffixCheck auto-fix coverage

## Supported (auto-fixable)

| Pattern | Fix |
| --- | --- |
| `(long)`/`(float)`/`(double)` cast LHS of binary op with int-literal RHS | Remove cast, append `L`/`f`/`d` to the RHS literal |
| `(long)`/`(float)`/`(double)` cast RHS of binary op with int-literal LHS | Remove cast, append suffix to the LHS literal |
| Ternary `cond ? (Type) X : LIT` (cast in true branch) | Remove cast, append suffix to false-branch literal |
| Ternary `cond ? LIT : (Type) X` (cast in false branch) | Remove cast, append suffix to true-branch literal |
| Signed literals (`-100`, `+100`) as the unary-wrapped int-literal sibling | Preserved verbatim; suffix appended after digits |
| Hex literals where the int value is non-negative (e.g. `0xFF`, `0x7FFFFFFF`) | Suffix appended after digits |
| Binary literals where the int value is non-negative (e.g. `0b1010`) | Suffix appended after digits |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason check does not fire |
| --- | --- |
| Shift operators (`<<`, `>>`, `>>>`) | JLS 15.19: shift result type/masking is decided by the LHS operand only. Swapping cast for suffix would change behavior |
| Hex/binary int literal with bit 31 set (`0x80000000`, `0xFFFFFFFF`, `0b10000000_00000000_00000000_00000000`) | Int value sign-extends to a different long than the same digits as a long literal |
| Non-widening casts (`(int)`, `(byte)`, `(short)`) | No suffix exists for these types |
| Other operand is already typed (long, double, or float literal, expression, or identifier) | No int literal to attach a suffix to |
| Cast wraps the whole expression (`(long) (x * 100)`) instead of a single operand | The literal is not a direct binary-op sibling of the cast |

## Not supported

| Pattern | Why |
| --- | --- |
| Cast spans multiple lines (e.g. `(\n long\n) x * 100`) | The cast spans more than one line |
| Cast with nothing on the violation line after `)` (e.g. `(long)` at end of line) | No expression after the cast on the violation line |
| Cast subject is not an identifier (e.g. `(long) (x) * 100`) | The cast subject is not a plain identifier |
| Identifier-prefixed compound subject (e.g. `(long) obj.field * 100`, method call) | The cast subject is a compound expression, not a plain identifier |

Part of [auto-fix coverage](../auto-fix-coverage.md).