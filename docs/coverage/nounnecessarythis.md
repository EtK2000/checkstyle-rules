# NoUnnecessaryThisCheck auto-fix coverage

Replaces the text from the `this` keyword up to the member name it qualifies, which deletes the
qualifier along with whatever was written between the two: whitespace, a comment, or a line break.
`return this.field;` becomes `return field;`, and so do `this .field`, `this. field` and
`this . field`. A block comment written inside that text comes back ahead of the member, so
`this/* c */.field` and `this./* c */field` both become `/* c */ field`. A space is inserted ahead of
a relocated comment when the text before the qualifier ends in `/`, so `divisor/this/* c */.field`
becomes `divisor/ /* c */ field` rather than a `/` and a `/*` that read as a line comment and
swallow the rest of the line. The member itself is untouched, so a call, a nested access and an
array access keep everything to the right of the qualifier.

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| A field assignment target (`this.field = 1`) | The qualifier is what makes the assignment reach the field rather than a same-named binding. A compound assignment (`this.field += 1`) is flagged, since its target is read as well as written and the qualifier is redundant there |
| A name bound anywhere in the same member (a parameter, local, for or for-each variable, catch parameter, lambda parameter, pattern variable or try resource **declaration**) | The binding shadows the field, so the qualifier is what reaches it. Bindings are collected by subtree rather than by scope, so a name bound in a sibling branch counts too: a deliberate over-approximation, since a wrong answer here deletes a qualifier the code needs |
| A try resource written as a bare variable access (`try (reader)`) | The name is read as a binding although nothing is declared, so a `this.reader` inside the block is left alone. Another case of the over-approximation above, and the opposite of the qualified form `try (this.reader)`, which binds nothing and is flagged |
| A name bound inside an anonymous class body in the same member | Anonymous bodies are deliberately not excluded from the collection, which only ever keeps a `this.` that could have gone |
| A field or instance initializer reading a field declared by the same or a later declarator | JLS 8.3.3 rejects that read as a simple name, so the qualifier is what keeps it legal. A read through a method call (`this.beta()`) is not such a read and is flagged |
| A qualifier that is not `this` (`super.field`, `Outer.this.field`, `(this).field`, `((Object) this).hashCode()`) | The qualifier is not the bare `this` keyword, so nothing here is a redundant `this.` |
| A member that is not a plain name (`this.new Inner()`, `this.<String>m()`) | A qualified class instance creation and a type witness put something other than the member name after the `.`. The type witness is a measured false negative rather than a decision: removing the qualifier there would be safe, but the check reads only the name |
| `this` on its own (`register(this)`, `this::method`, `this(1)`, `super()`) | There is no `.` to report |
| A name bound inside a nested class, record, interface, enum or annotation declared in the member | Those bindings are never in scope outside the nested type, so they do not shadow and the qualifier is redundant |

## Not supported

| Pattern | Reason |
| --- | --- |
| A `//` comment between `this` and the member | The replaced text runs onto a later line, and folding it onto one would put the code after the comment inside it |
| A block comment anywhere inside a replaced region that spans lines | The region cannot be folded onto one line without losing the comment, so the qualifier stays. A block comment inside a single-line region is carried |
| A position whose text has moved under an earlier fix in the same pass | Nothing the check recognises sits at the reported position any more, so the fix is left for the next pass |
| A buffer that does not parse | The classifier needs the parse. The fix is retried once the buffer parses |

Part of [auto-fix coverage](../auto-fix-coverage.md).