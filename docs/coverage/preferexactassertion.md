# PreferExactAssertionCheck auto-fix coverage

The check fires on `assertTrue`/`assertFalse` whose argument is a comparison operator, an
`instanceof` expression, or a top-level negation (`!x`). The `instanceof` form is
framework-gated: JUnit 4's `Assert` has no `assertInstanceOf`/`assertNotInstanceOf`, so the
check suppresses the violation when the call resolves to JUnit 4. Resolution rule: a qualified
call decides on the receiver's simple name (`Assert` -> suppress, `Assertions` -> fire); an
unqualified call requires a static import of `Assertions` with no static import of `Assert`.
The comparison and negation forms are not framework-gated (`assertEquals` /
`assertTrue` / `assertFalse` exist under both frameworks). Only static imports count;
non-static type imports don't enable unqualified method resolution and are ignored.

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
| `Assert\n.assertTrue(!(o instanceof Y))` (multi-line qualifier) | `Assert\n.assertFalse(o instanceof Y)` (negation fallback) | Cross-line walk-back with `//` and `/* */` comment stripping; identifier-whitelist gate (`Assert` or `Assertions`) still applies |
| `stmt(); assertTrue(o instanceof Y);` (leading code/comment shares the line) | `stmt();` then `assertInstanceOf(Y.class, o);` on its own line | A leading statement or comment sharing the assert's physical line is moved onto its own line above the rewritten assert, keeping the indentation |

## Not supported

| Pattern | Reason |
| --- | --- |
| `assertTrue(o instanceof Y y)` (pattern binding, polarity TRUE) | Binding is reachable at runtime; stripping would silently drop a referenced variable |
| `assertFalse(!(o instanceof Y y))` (instanceof side only) | Same: effective polarity TRUE keeps the binding. Negation rule still fires and simplifies the `!` |
| `assertTrue(o instanceof List<X>)` (generic type) | Generics can't appear in a `.class` literal |
| `assertTrue(o instanceof Y, "msg", "extra")` (3+ args) | Beyond the JUnit 4/5 1- and 2-arg shapes |
| Unqualified instanceof call, no static import of `Assertions` | Rewrite to unqualified `assertInstanceOf` wouldn't resolve |
| Unqualified instanceof call, static import of both `Assert` and `Assertions` | Original `assertTrue` may resolve through JUnit 4; rewrite would silently swap frameworks |
| Unqualified negation call, no static import of `Assert` or `Assertions` | Can't infer the framework class for the opposite-method import |
| Unqualified negation call, static import of both `Assert` and `Assertions` | Swap would change which framework resolves the unqualified call |
| Args containing a structural `//` line comment (between `(` and `)`) | The rewrite flattens lines and `//` would consume the rewritten `);` |
| Comment (`/* */` or `//`) between the closing `)` and the `;` | The rewrite requires the `;` be whitespace-adjacent to `)`; an intervening comment would be silently dropped |
| Args containing an explicit type argument (`Foo.<T>method()`) | `<...>` brackets contain commas that confuse the top-level-comma argument splitter |
| Multi-line negation argument (`!(...)` spanning lines) | The shared paren-matcher mis-handles `//` line comments in multi-line text; bail to be safe |
| Args containing a source-level Unicode escape (`\uXXXX` outside literals) | The Java compiler preprocesses these before tokenization (JLS 3.3) but our text-based scanners don't; the rewrite could emit code whose meaning the compiler reinterprets |
| Multi-call line where the violation column doesn't pin the call (slow-path with `name<ws>(` + `name<newline>(` shapes mixed) | Without a usable column the resolver can't tell which call the violation refers to; bail rather than guess |

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

## Comparison form (not fixable)

`assertTrue(a == b)`, `assertTrue(a > b)`, etc. The exact expected value depends on domain
knowledge (`assertEquals` needs the expected literal; `assertTrue(a > b)` could mean any of
`assertEquals(b + 1, a)`, `assertEquals(specificValue, a)`, etc.). The check fires under any
framework (JUnit 4 also has `assertEquals`/`assertSame`/etc.) but no auto-fix is provided.

Comparison form fires under every receiver shape the check accepts: unqualified (with a
`Assert`/`Assertions` static import in scope), qualified `Assert.<call>`, qualified
`Assertions.<call>`, and fully-qualified `org.junit.[jupiter.api.]<class>.<call>`. The
qualified shapes are not framework-gated, because both `Assert` and `Assertions` have
`assertEquals`/`assertNotEquals`.

Part of [auto-fix coverage](../auto-fix-coverage.md).