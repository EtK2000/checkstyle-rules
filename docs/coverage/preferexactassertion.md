# PreferExactAssertionCheck auto-fix coverage

The check fires on `assertTrue`/`assertFalse` whose argument is a comparison operator, an
`instanceof` expression, or a top-level negation (`!x`). The `instanceof` form is
framework-gated: JUnit 4's `Assert` has no `assertInstanceOf`/`assertNotInstanceOf`, so the
check suppresses the violation when the call resolves to JUnit 4. Resolution rule: a receiver
written with its package is matched against the holder's full name (`org.junit.Assert` ->
suppress, `org.junit.jupiter.api.Assertions` -> fire); a receiver written as a bare name carries
no package, so its simple name is all there is to go on (`Assert` -> suppress, `Assertions` ->
fire); an unqualified call requires a static import whose class is
`org.junit.jupiter.api.Assertions` with none whose class is `org.junit.Assert`.
The comparison and negation forms are not framework-gated (`assertEquals` /
`assertTrue` / `assertFalse` exist under both frameworks). Only static imports count;
non-static type imports don't enable unqualified method resolution and are ignored.

The holder is matched by full name, never by a suffix, because a class called `Assertions` or
`Assert` in some other package is a different class that need not declare the same methods.
`com.example.Assertions.assertTrue(...)`, and an unqualified call under
`import static com.example.Assertions.assertTrue;`, are therefore invisible to both the check and
the fix: nothing proves the call is a JUnit assertion, so nothing is reported. The one spelling
this cannot see through is a bare receiver brought in by a type import
(`import com.example.Assertions;` then `Assertions.assertTrue(...)`), which would need the type
imports resolved to tell apart from JUnit's.

Which imports are in scope is read from the parse, not from the import lines' text, so how an import
is written does not change what it means: a comment before `import`, after the `;`
or between the name's tokens, JLS-legal whitespace inside the qualified name (`import static a . b . C . m ;`), and
a line break anywhere in the declaration all read the same, and the import a fix adds always names
the collapsed, comment-free FQN. An `import` written *inside* a block comment or a text block is
content rather than an import: it neither supplies the class an added static import is drawn from
nor counts toward the both-frameworks ambiguity.

The check also measures the replacement it is asking for: `assertInstanceOf(Y.class, x)` is longer
than the `assertTrue(x instanceof Y)` it replaces, and a multi-line call folds onto one line, so an
`instanceof` or negation violation whose rewrite would exceed the line-length budget is left
unreported rather than reported and then refused. Comparison violations are exempt, since no rewrite
is offered for them under any width.

The budget does not change which rule applies. An over-budget `instanceof` rewrite drops the report
rather than falling through to the negation rule, so `assertTrue(!(longChain() instanceof Y))` is
reported once as an `instanceof` violation or not at all, never as "a negated argument" at the same
site.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `assertTrue(x instanceof Y)` | `assertInstanceOf(Y.class, x)` | Unqualified, JUnit 5 static import in scope |
| `assertFalse(x instanceof Y)` | `assertNotInstanceOf(Y.class, x)` | Polarity flip |
| `assertTrue(!(x instanceof Y))` | `assertNotInstanceOf(Y.class, x)` | Single negation cancels with `assertTrue` |
| `assertFalse(!(x instanceof Y))` | `assertInstanceOf(Y.class, x)` | Single negation cancels with `assertFalse` |
| `assertTrue(!!(x instanceof Y))` | `assertInstanceOf(Y.class, x)` | Double-negation parity |
| `assertTrue(o instanceof java.lang.String)` | `assertInstanceOf(java.lang.String.class, o)` | Fully-qualified type preserved |
| `assertTrue(o instanceof java.util.Map.Entry)` | `assertInstanceOf(java.util.Map.Entry.class, o)` | Nested type |
| `assertTrue(ex.getCause() instanceof RuntimeException)` | `assertInstanceOf(RuntimeException.class, ex.getCause())` | Complex LHS preserved |
| `assertTrue("msg", o instanceof Y)` | `assertInstanceOf(Y.class, o, "msg")` | JUnit 4 message-first form |
| `assertTrue(o instanceof Y, "msg")` | `assertInstanceOf(Y.class, o, "msg")` | JUnit 5 message-last form |
| `Assertions.assertTrue(o instanceof Y)` | `Assertions.assertInstanceOf(Y.class, o)` | Qualifier preserved, no import added |
| `org.junit.jupiter.api.Assertions.assertTrue(o instanceof Y)` | `org.junit.jupiter.api.Assertions.assertInstanceOf(Y.class, o)` | Fully-qualified qualifier preserved |
| `assertTrue((o instanceof Y))` | `assertInstanceOf(Y.class, o)` | Outer parens stripped before classification |
| `assertTrue((!(o instanceof Y)))` | `assertNotInstanceOf(Y.class, o)` | Parens around negation |
| `assertTrue(\n  o instanceof Y\n);` (multi-line call) | `assertInstanceOf(Y.class, o);` | Collapses multi-line shape. Supports `(` and/or `;` on their own lines |
| `assertTrue("""text""", o instanceof Y)` (text-block message) | `assertInstanceOf(Y.class, o, """text""")` | Text block preserved verbatim |
| `assertFalse(o instanceof Y y)` (pattern binding, polarity FALSE) | `assertNotInstanceOf(Y.class, o)` | Binding never binds at runtime when polarity is FALSE; safe to drop |
| `assertTrue(!(o instanceof Y y))` (pattern binding, polarity FALSE) | `assertNotInstanceOf(Y.class, o)` | Same: effective polarity FALSE drops the unreachable binding |
| `assertFalse(!!(o instanceof Y y))` (polarity FALSE via parity) | `assertNotInstanceOf(Y.class, o)` | Even-count `!`s preserve the assertFalse polarity; binding still unreachable |
| `assertTrue(!flag)` | `assertFalse(flag)` | Negation rewrite. Adds `static <SameClass>.assertFalse` where `<SameClass>` matches the existing static-imported assertion class. No import added when a wildcard or explicit `assertFalse` import already covers the call |
| `assertFalse(!flag)` | `assertTrue(flag)` | Mirror polarity |
| `assertTrue(!list.isEmpty())` | `assertFalse(list.isEmpty())` | Method-call inner preserved |
| `assertTrue(!(a && b))` | `assertFalse(a && b)` | Inner outer parens stripped on rewrite |
| `assertTrue((!flag))` | `assertFalse(flag)` | Outer parens around the negation also stripped |
| `assertTrue(!flag, "msg")` / `assertTrue("msg", !flag)` | `assertFalse(flag, "msg")` / `assertFalse("msg", flag)` | JUnit 5 message-last and JUnit 4 message-first preserved |
| `assertFalse(!(o instanceof Y y))` (polarity TRUE binding) | `assertTrue(o instanceof Y y)` | Instanceof rule keeps the binding (polarity TRUE); negation rule simplifies the `!` |
| `assertTrue(!(o instanceof Y))` under JUnit-4-only static imports | `assertFalse(o instanceof Y)` (negation fallback) | Instanceof rewrite blocked because JUnit 4 has no `assertInstanceOf`; falls back to the negation rewrite |
| `assertFalse(!(o instanceof Y))` under JUnit-4-only static imports | `assertTrue(o instanceof Y)` (negation fallback) | Mirror: same fallback applies for the assertFalse polarity |
| `Assert.assertTrue(!(o instanceof Y))` (qualified JUnit 4) | `Assert.assertFalse(o instanceof Y)` (negation fallback) | Qualifier `Assert` means `assertInstanceOf` doesn't exist on the call's class; falls back to negation, preserving the qualifier |
| `Assert.assertFalse(!(o instanceof Y))` (qualified JUnit 4) | `Assert.assertTrue(o instanceof Y)` (negation fallback) | Mirror: same fallback applies for the assertFalse polarity |
| `Assert. assertTrue(!(o instanceof Y))` (whitespace between dot and method) | `Assert.assertFalse(o instanceof Y)` (negation fallback) | Whitespace-tolerant qualified detection; the whitespace between the qualifier dot and the method is normalized away (`Qualifier. method` -> `Qualifier.method`) |
| `Assert\n.assertTrue(!(o instanceof Y))` (multi-line qualifier) | `Assert\n.assertFalse(o instanceof Y)` (negation fallback) | A receiver written on an earlier line is left where the author put it |
| `stmt(); assertTrue(o instanceof Y);` (leading code/comment shares the line) | `stmt();` then `assertInstanceOf(Y.class, o);` on its own line | A leading statement or comment sharing the assert's physical line is moved onto its own line above the rewritten assert, keeping the indentation |
| `assertTrue(x instanceof Y)` with `import static com.example.Assertions.assertEquals;` above `import static org.junit.jupiter.api.Assertions.assertTrue;` | `assertInstanceOf(Y.class, x)` + `import static org.junit.jupiter.api.Assertions.assertInstanceOf;` | The class the replacement is drawn from is matched by full name, so an assert-named class from another package never donates, wherever it sits in the import list |
| `assertTrue(!flag)` with `import static com.example.Assertions.*;` beside a real JUnit 5 import | `assertFalse(flag)` + `import static org.junit.jupiter.api.Assertions.assertFalse;` | A look-alike wildcard no longer reads as "already in scope" |
| `assertTrue(x instanceof Y)` / `assertTrue(!flag)` with `import static com.foo.Assert.*;` beside a real JUnit 5 import | `assertInstanceOf(Y.class, x)` / `assertFalse(flag)` | A look-alike `Assert` does not count as JUnit 4, so neither the instanceof suppression nor the both-frameworks ambiguity applies |
| `assertTrue(!flag)` with a look-alike `Assertions` beside `import static org.junit.Assert.assertTrue;` | `assertFalse(flag)` + `import static org.junit.Assert.assertFalse;` | Mirror for the JUnit 4 side |
| `assertTrue(!flag)` with `import static com.foo.Assert.assertNotNull;` beside a real JUnit import | `assertFalse(flag)` + the replacement drawn from the real holder | The exact-member spelling of a look-alike `Assert` behaves like its wildcard: it neither counts as JUnit 4 nor donates the replacement |
| `assertTrue(x instanceof Y)` with `import static com.example.Assertions.assertThrowsExactly;` beside a real JUnit 5 import | `assertInstanceOf(Y.class, x)` + `import static org.junit.jupiter.api.Assertions.assertInstanceOf;` | A look-alike import is skipped on its class before its member is considered, so an unknown member there does not stop the real import donating |
| `new org.junit.Assert().assertTrue(!flag)` (instance-expression receiver) | `new org.junit.Assert().assertFalse(flag)` | The receiver resolves to the holder's full name, so it is matched like any other qualified receiver; `new Helper()` and `helpers[0]` resolve to neither holder and stay untouched |
| `assertTrue(o instanceof Y); stmt();` (code shares the line after the assert) | `assertInstanceOf(Y.class, o);` then `stmt();` on its own line | The mirror of the row above. The trailing statement is excluded from the line-length budget, since moving it to the shared indent can only make it narrower than it already was |
| `if (flag) assertTrue(o instanceof Y);` (the assert IS a control-flow or lambda body) | `if (flag) assertInstanceOf(Y.class, o);` | Neither split applies: the text sharing the line is a header the assert belongs to, not a sibling statement, and hoisting it would leave the body at the header's own indent |
| `assertTrue(o/* c */instanceof Y)`, `assertTrue(o instanceof/*c*/Y)` | `assertInstanceOf(Y.class, o/* c */)`, `assertInstanceOf(/*c*/Y.class, o)` | A comment abutting the keyword stays with whichever side it was written on |
| `assertTrue(o instanceof Y) /* note */ ;` | `assertInstanceOf(Y.class, o); // note` | A comment the rewrite cannot leave in place is relocated to one trailing `//` comment, except as the next row says |
| `runLater(() -> assertTrue(/* why */ !flag)); /* tail` (text left on the line opens a block comment or text block) | `runLater(() -> assertFalse(flag)); /* tail` | The relocated comment is **dropped** rather than appended: an open block comment would swallow it, and a terminator inside it would close that comment early and turn the prose after it back into code. Only whitespace may follow a text block's opening delimiter, so appending there would not compile |
| `assertTrue(!flag // note\n);` (structural line comment in the args) | `assertFalse(flag); // note` | A `//` comment inside the argument list is relocated rather than refused; left in place it would comment out the rewritten `);` |
| `assertTrue(Map.<String, Integer>of() instanceof Map)` | `assertInstanceOf(Map.class, Map.<String, Integer>of())` | Explicit type arguments stay with the operand rather than splitting the arguments |
| `assertTrue(!(\n  o instanceof Y\n));` (multi-line negated argument) | `assertNotInstanceOf(Y.class, o);` | The `!` chain is structure the rewrite drops, so its layout does not matter |
| `assertTrue\n(o instanceof Y);` (name and `(` on separate lines) | `assertInstanceOf(Y.class, o);` | |
| `runLater(() -> assertTrue(o instanceof Y))` (lambda body) | `runLater(() -> assertInstanceOf(Y.class, o))` | An assertion that is an expression rather than a statement has no `;`, and none is emitted |

## Not supported

| Pattern | Skip reason | Why |
| --- | --- | --- |
| `assertTrue(a == b)`, `assertTrue(a > b)`, ... | `cannot rewrite the assertion: a comparison needs the operand types to rewrite` | The operand types decide whether the rewrite is even equivalent; see the comparison section below |
| `assertTrue(o instanceof List<X>)` | `cannot rewrite the assertion: the instanceof type is generic` | Generics can't appear in a `.class` literal |
| `assertTrue(o instanceof @A Y)` | `cannot rewrite the assertion: the instanceof type is annotated` | The annotation has no place on the `.class` literal |
| `assertTrue(o instanceof Pt(int x, int y))` (record pattern) | `cannot rewrite the assertion: record pattern with bindings` | The check's binding gate does not see a record pattern's bindings, so the check fires; dropping the pattern would drop bindings the code may still use |
| `assertTrue(o instanceof Y, "msg", "extra")` (3+ args) | `cannot rewrite the assertion: it has more arguments than a condition and a message` | Beyond the JUnit 4/5 1- and 2-arg shapes, so no replacement overload is known to exist |
| Two violations reported at one position (`Assertions.assertTrue(!flag, a == b)` on a user-defined holder) | `cannot rewrite the assertion: more than one assertion violation at this position` | The fixer cannot tell which of the two violations it is fixing |
| Unqualified negation call, static import of both `Assert` and `Assertions` | `cannot rewrite the assertion: both JUnit 4 and JUnit 5 assertions are statically imported` | Swap would change which framework resolves the unqualified call, and there is no telling which import to add |
| Unqualified call whose only JUnit static import names a member the fixer does not know (`import static …Assertions.assertThrowsExactly;`, the call itself inherited) | `cannot rewrite the assertion: no static import says which class the replacement comes from` | The check's gate is the class, the fix's is the member, so no import resolves the class to draw the replacement from. Emitting the rewrite would call a method nothing imports |
| A call whose source span does not resolve against the buffer | `cannot rewrite the assertion: its call span does not resolve` | A span the rewrite cannot slice, e.g. after an earlier fix in the same pass moved the text |
| A buffer that does not parse as a compilation unit | (no skip, no fix) | The check never reports inside a file that does not parse, so a position there is not a recognized violation and has no reason to name |
| A reported position no assertion starts at any more | (no skip, no fix) | An earlier fix in the same pass moved or rewrote the text. Same reasoning: the position is no longer a recognized violation, so there is nothing to name a refusal about |

### Also rewritten

A source-level Unicode escape outside a literal
(`assertTrue(o instanceof String /* \uXXXX */)`) is rewritten.

### Not fixable, but not refused either

| Pattern | Reason |
| --- | --- |
| `assertTrue(o instanceof Y y)` (pattern binding, polarity TRUE) | The check does not report it: the binding is reachable at runtime, so stripping it would silently drop a referenced variable |
| `assertFalse(!(o instanceof Y y))` (instanceof side only) | Same: effective polarity TRUE keeps the binding. The negation rule still fires and simplifies the `!` |
| Unqualified instanceof call, no static import of `Assertions`, or static imports of both `Assert` and `Assertions` | The check does not report it: the rewrite to unqualified `assertInstanceOf` would resolve through JUnit 4 or not at all |
| Unqualified negation call, no static import of `Assert` or `Assertions` | The check does not report it: nothing proves the call is a JUnit assertion |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| `Assert.assertTrue(o instanceof Y)` (qualifier `Assert`) | JUnit 4 has no `assertInstanceOf`, so the instanceof rule is suppressed (negation rule fires only when there's a top-level `!`) |
| Unqualified `assertTrue(o instanceof Y)` under JUnit 4 static only | Same: instanceof rule resolves through JUnit 4 |
| Chained-receiver `helper().assertTrue(o instanceof Y)` | Receiver's runtime type is unknown; suppress conservatively |
| `helper.assertTrue(!(o instanceof Y))` (qualifier not `Assert`/`Assertions`) | No evidence the receiver has `assertFalse`/`assertNotInstanceOf`; skipping avoids breaking the build |
| `helper.assertTrue(!flag)` (qualifier not `Assert`/`Assertions`) | Same: no evidence the receiver has `assertFalse` |
| `helper.assertTrue(a == b)` (qualifier not `Assert`/`Assertions`) | Same: no evidence the receiver has `assertEquals`/`assertNotEquals` |
| `(helper).assertTrue(...)` / `getHelper().assertTrue(...)` (chained receiver) | Receiver can't be resolved to a known class; same gate as `helper.` above |
| Unqualified `assertTrue(!flag)` / `assertTrue(a == b)` with no JUnit static import | No JUnit `Assert`/`Assertions` in scope; the rewrite has no class to add an import for |
| `assertTrue(o instanceof Y y && y.length() > 0)` (pattern binding inside compound) | Top-level is `&&`, not a comparison/instanceof/negation |
| `assertTrue(a > 0 && b > 0)` (compound boolean) | Top-level is `&&`/`\|\|`, not a comparison/instanceof/negation |
| `assertTrue(flag)` (plain identifier, no `!`) | Already specific: no simpler form |
| Unqualified call whose only static import is of an assert-named class from another package (`com.example.Assertions`, `com.foo.Assert`, exact or wildcard) | Nothing proves the call is a JUnit assertion: the import's class is neither `org.junit.Assert` nor `org.junit.jupiter.api.Assertions` |
| Unqualified call under a static import that names no package (`import static Assertions.assertTrue;`) | Same: a class with no package is not either holder |
| `com.example.Assertions.assertTrue(o instanceof Y)` (qualified with a look-alike's package) | The receiver names its package, so it is matched in full and resolves to neither holder |

## Comparison form (not fixable)

`assertTrue(a == b)`, `assertTrue(a > b)`, etc. The exact expected value depends on domain
knowledge (`assertEquals` needs the expected literal; `assertTrue(a > b)` could mean any of
`assertEquals(b + 1, a)`, `assertEquals(specificValue, a)`, etc.). The check fires under any
framework (JUnit 4 also has `assertEquals`/`assertSame`/etc.) but no auto-fix is provided.

`assertTrue(Double.NaN == x)` fails
while `assertEquals(Double.NaN, x)` passes, and `assertTrue(0.0d == -0.0d)` passes while
`assertEquals(0.0d, -0.0d)` fails, so `==`/`!=` cannot be rewritten without type information;
`>`/`>=`/`<`/`<=` have no JUnit counterpart at all.

Comparison form fires under every receiver shape the check accepts: unqualified (with a
`Assert`/`Assertions` static import in scope), qualified `Assert.<call>`, qualified
`Assertions.<call>`, and fully-qualified `org.junit.[jupiter.api.]<class>.<call>`. The
qualified shapes are not framework-gated, because both `Assert` and `Assertions` have
`assertEquals`/`assertNotEquals`.

Part of [auto-fix coverage](../auto-fix-coverage.md).