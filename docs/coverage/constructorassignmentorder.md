# ConstructorAssignmentOrderCheck auto-fix coverage

The fixer parses constructor and instance initializer bodies to find `this.xxx = ...` assignment
statements. It groups them (simple single-line, multi-line, variable-dependent) and sorts within
each group. Local variable declarations are placed before the assignments that reference them.

| Violation type | Pattern | Auto-fix | Notes |
| --- | --- | --- | --- |
| Alphabetical order | `this.beta = b; this.alpha = a;` | Yes | Swaps/sorts by field name |
| Simple before multi-line | Multi-line before `this.alpha = a;` | Yes | Moves simple group before multi group |
| Non-var before var-dependent | Var-dependent before `this.beta = x;` | Yes | Moves non-var groups before var group |
| Var sub-group order | `this.beta = second; this.alpha = first;` | Yes | Sorts by variable declaration order |
| Field-to-field dependency | `this.beta = this.alpha + 1;` before `this.alpha = a;` | Yes | Respects dependency: A before B if B uses A. Both `this.field` and bare `field` references count |
| Shadowed bare reference | ctor param/local `value`, `this.tracks = value + 1;` | Yes | A bare name shadowed by a parameter or local isn't a field dependency; alphabetical order applies |
| Member/method sharing a name | `this.beta = other.value;` / `this.beta = value();` | Yes | `other.value` and `value()` aren't bare field references, so no dependency is inferred |
| Multi-line alphabetical | Two multi-line anonymous class assignments out of order | Yes | Tracks brace/paren depth for boundaries |
| Text blocks in assignments | `this.x = """\n...\n""";` | Yes | Reordered as a multi-line assignment; string/char/comment/text-block content is ignored so braces, quotes, and semicolons inside a block are never counted as structure and an interior line is never read as a field reference or a local-var declaration |
| Circular dependencies | `this.a = this.b + 1; this.b = this.c + 1; this.c = this.a + 1;` | No | SkipResult: "cannot reorder: a field dependency cycle has no valid order" (a topological sort orders the assignments so each field is assigned before it is read; a cycle has no such order). A two-field cycle is not flagged at all, since each assignment already reads the other before assigning it |
| Multiple statements on one line | `this.b = 2; this.a = 1;` (two assignments, or an assignment and another statement, on one physical line) | No | SkipResult: "cannot reorder: multiple statements share a line" (a whole-line move would duplicate or drop the shared line) |
| Field assigned more than once | `this.x = 1; this.y = x; this.x = 2;` | No | SkipResult: "cannot reorder: a field is assigned more than once" (sorting by field name would change which value a later read sees) |
| Interleaved comment | standalone `//` or `/* */` comment between the reordered assignments | No | SkipResult: "cannot reorder: an interleaved comment would be lost" (the parse drops comments, so a reorder can't preserve one) |
| Interleaved statement | `System.out.println()` or another non-assignment statement between the reordered assignments | No | SkipResult: "cannot reorder: an interleaved statement would change execution order" |
| Multi-line local var decl, referenced by an assignment | `final var x =\n\t\tcompute(a);\n\tthis.f = x;` | Yes | The full multi-line declaration span is carried and emitted just before the assignment that reads it |
| Unreferenced local var with a side-effecting initializer | `final var x =\n\t\tcompute(a);` (no assignment reads `x`) | No | SkipResult: "cannot reorder: relocating an unused local would move its initializer's side effects" (an unread var is emitted after every assignment, which would move a lock acquisition / timestamp / log call past the field writes; an unread var whose initializer is side-effect-free, e.g. `x + 1`, is still moved to the tail) |
| Local var used by another local var | `final var a = f(); final var b = a + 1; this.x = b;` | Yes | A var whose initializer reads another local var is emitted after the var it depends on, so relocation never creates a forward reference |
| Nested generics in local var | `Map<String, List<Integer>> m = ...` | Yes | AST classification parses any local var type, so the declaration is carried verbatim |

Part of [auto-fix coverage](../auto-fix-coverage.md).