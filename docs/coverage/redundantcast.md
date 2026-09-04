# RedundantCastCheck auto-fix coverage

Replaces the text from the cast's `(` up to the expression it applies to, which deletes the cast along with any whitespace between the two and re-emits any block comment there ahead of the expression. Grouping parentheses around the cast go in the same edit (`((Foo) x).y` -> `x.y`, `return ((Foo) x);` -> `return x;`), nested pairs included, and a block comment on either side of the expression comes back ahead of it, while one written inside the expression stays where it is. A space is inserted at whichever edge would otherwise fuse two tokens, so `return((Foo) x).y` becomes `return x.y` and `((Foo) x)instanceof Bar` becomes `x instanceof Bar`. A parenthesis belonging to a call, a `switch` selector, a `synchronized` lock or an `if` condition is not a grouping one and stays.

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| Intersection cast (`(Runnable & Serializable) r`) | The cast has one type per conjunct and no single type name. Removing it narrows the expression to one conjunct, which does not compile where another conjunct is what the context needs |
| A cast to one alternative of a multi-catch parameter (`catch (A \| B e) { throw (A) e; }`) | The parameter's type is `A \| B`, which has no single name. Removing the cast widens what the statement throws, and a caller that handles only `A` stops compiling |
| A compound assignment whose target is not a primitive (`s += (int) c` on a `String`) | `+=` there selects string concatenation rather than a numeric conversion, so the cast changes the appended text rather than being redundant |
| A compound assignment whose cast is wider than its target (`f += (double) i`) | The arithmetic happens in the cast's type and is narrowed back, which reaches a different value above 2^24 |
| A compound assignment whose target is not a plain resolvable name (`arr[0] += (long) y`, an inherited field) | The target's type is unknown, so whether the cast changes the conversion cannot be decided |
| An operand whose name has more than one visible declaration (a local, pattern variable, resource or lambda parameter shadowing a field, or two locals of one name in a method) | Which declaration applies depends on scoping the check does not model: a pattern variable's scope follows the flow of its condition, a resource's covers only the try block, a local's starts at its own declarator. Reading the wrong one types the operand as something it is not, and the fix then deletes a cast the code needs |
| An operand bound by a declaration whose type is not written (an inferred lambda parameter) | The operand's type is unknown |
| A cast whose type arguments differ from the expression's (`(List<String>) raw` over a raw `List`, `(List) l` over a `List<String>`) | The types differ, so the cast is not redundant. Removing it changes the expression's static type, and a consumer that needs the arguments stops compiling |
| Two spellings of one type (`(java.util.List) o` over a `List`) | Compared by name, so two spellings compare unequal and the check stays quiet |
| A `float` cast under a `double` result (`(float) i * 2.0`, `double v = (float) i`, `d += (float) i`) | The cast rounds to 24 bits of mantissa before the widening to `double`, which converting directly does not. At `i == 16777217` the two reach different values |
| A `float` cast under a `double` result whose operand is a `byte`, `char` or `short` | Exact either way, so removing it is safe, but it is refused with the rest of the family rather than special-cased |
| A cast whose type is annotated ahead of the name (`(@Ann String[]) o`) | The annotation takes the place of the type token, so no name is read. The same annotation written on the declarator (`(String @Ann []) o`) is flagged |
| Operand the syntax tree does not type (parenthesised, a negation, a postfix increment, a qualified name such as `Double.NaN` or `super.field`, a pattern variable, a record component, a multi-catch parameter, a text block) | The expression's type is unproven, so the comparison cannot be made |

## Not supported

| Pattern | Reason |
| --- | --- |
| A `//` comment between the cast and the expression | The replaced text runs onto a later line, and folding it onto one would put the code after the comment inside it |
| A grouping parenthesis around a cast whose own subject is a cast (`((String) (String) s).length()`) | A cast binds looser than the `.` the parenthesis stands to the left of, so only the inner cast goes and the parentheses stay. The inner cast is reported too, and removing it first leaves the shape the parentheses do come out of |
| A grouping parenthesis whose text cannot be folded onto one line (a `//` comment or a text block between the parenthesis and the expression) | The cast still goes; the parentheses stay |
| A grouping parenthesis around an expression that spans lines (an anonymous class body, a wrapped argument list) | The expression is written back into the replaced region, so taking the parentheses out would fold it onto one line and destroy its layout. The cast still goes; the parentheses stay |

Part of [auto-fix coverage](../auto-fix-coverage.md).