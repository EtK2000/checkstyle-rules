# EmptyBodyCheck auto-fix coverage

`EmptyBodyFixer` deletes the reported construct. An `if` condition is evaluated exactly once, so
anything in it that changes program state is left behind as a statement where the `if` was; a loop
header is evaluated once per iteration, so nothing can be left behind for it and a mutating header
is refused instead.

Empty loop bodies are reported as warnings, and `checkstyleFix` only acts on errors, so the rows
below describe what the fixer does when it is driven directly. In a real fix run only the `if`,
`else`, initializer and stray-semicolon rows apply.

## Supported (auto-fixable)

| Pattern | Result | Notes |
| --- | --- | --- |
| `if (cond);` / `if (cond)\n\t;` / `if (cond) {}` / `if (cond) {\n}` | statement removed | All four spellings collapse to nothing |
| `else;` / `else\n\t;` / `else {}` / `else {\n}` | `else` clause removed | The `if` it belongs to stays |
| `else if (cond);` / `else if (cond) {\n}` | whole `else if` clause removed | Removing only the inner `if` would leave a dangling `else`. Only when the condition changes no state; a condition that does is refused, see the `else if (++x < 0)` row below |
| `if (cond) {\n} // note` | statement removed, `// note` kept on its own line | A comment after the construct is not part of it |
| `if (++x > 0);` | `++x;` | The condition's state change is kept, in evaluation order |
| `if (first() > second());` | `first();`<br>`second();` | Several state changes are kept, left to right |
| `if ((x = compute()) > 0);` | `x = compute();` | |
| `static {}` / `static {\n}` | initializer removed | |
| `{}` / `{\n}` instance initializer | initializer removed | |
| `while (cond);` / `while (cond) {\n}` | loop removed | Only when the condition changes no state |
| `for (var i = 0; i < x; ++i);` | loop removed | Only when the header changes nothing that outlives the loop |
| `for (var s : list);` | loop removed | Only when the iterable expression changes no state |
| `do;\nwhile (cond);` / `do {\n} while (cond);` | loop removed | Only when the condition changes no state |
| a stray `;` on its own line | line removed | |
| `; // note` | `// note` kept at the same indent | The comment does not belong to the semicolon, so it outlives it |
| `{ ; }` | `{ }` | Whatever shared the semicolon's line stays |

## Not supported

Each of these reports a skip reason rather than editing the file.

| Pattern | Reason |
| --- | --- |
| `if (cond) { // note\n}`, `if (cond) {\n\t/* note */\n}` | Removing the construct would destroy a comment inside it, and there is nowhere to move a comment that explains a branch once the branch is gone. `;` plus a comment is the spelling that survives; a braced body is not |
| `if (cond);\nelse\n\tstmt;` | The `else` branch would have to be re-attached to a negated condition. That rewrites the statement rather than removing it, so it is left for the author |
| `if (a)\n\tif (b)\n\t\tstmt;\n\telse {\n\t}\nelse\n\tstmt;` | Java binds an `else` to the nearest preceding `if` that lacks one, so removing this one would hand the enclosing `else` to the inner `if`, which then runs on `a && !b` instead of `!a`. The result still compiles, so nothing downstream would catch it |
| `if (x > 0 && next());` | The state change sits under a short-circuit operator, so it may not run at all. Hoisting it out would make it run unconditionally |
| `if (cond)\n\tstmt;\nelse if (++x < 0) {\n}` | The span to delete starts at the `else`, so the hoisted `++x;` would land outside the chain and run on every path instead of only the one the `else` guarded |
| `if (self()\n\t\t.count() > 0);` | The state change is spread over more than one line, so its text cannot be lifted out as a single statement |
| `while (next());`, `do;\nwhile (next());`, `for (var s : items());`, `for (var i = 0; i < 3; ++x);` | A loop header runs once per iteration. Lifting its state changes out would run them exactly once, which is a different program, and removing the loop outright would drop them |
| `if (ready)\n\tif (busy);`, `while (x > 0)\n\tif (busy);`, `for (var i = 0; i < 3; ++i)\n\tif (busy);`, `do\n\tif (busy);\nwhile (x > 0);`, `label:\nif (busy);`, `if (ready)\n\tstmt;\nelse\n\twhile (x > 0);` | The construct is its enclosing clause's whole body, so deleting it leaves the clause with nothing at all, which does not parse. Braces on the enclosing clause remove the problem: an empty `if` among other statements in a block is removed normally |
| `final Runnable r = () -> { if (x > 0); };`, `private int z = 7; static {\n}` | The construct shares its opening line with other code, which would go with it |
| the reported position no longer holds an empty construct | An earlier fix in the same pass moved or resolved it |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| `if (cond)\n\t; // handled by the caller`, and the same for `else`, `while`, `for` and `do` | A semicolon that is a clause's body cannot be deleted without deleting the clause, and a comment on its line is what marks the no-op deliberate. A block comment (`; /* reason */`) counts the same, and so does the one-line form `if (cond); // reason` |
| `// note` on the line **above** the clause | A comment above annotates the clause, not the semicolon; the body is still reported |
| `label: ;` | The semicolon is load-bearing (`label:` alone does not parse), but a labelled statement is not one of the clause forms this rule covers |

## No escape hatch

A comment only excuses a clause body written as `;`. A static initializer, an instance initializer
and a `switch` have no `;` spelling at all, so `static { /* reason */ }`, `{ /* reason */ }` and
`switch (x) { /* reason */ }` stay violations with no in-code way to keep them. A stray `;` keeps
no comment escape either: it is not a clause body, so deleting it costs nothing and the comment
stays where it was.

Part of [auto-fix coverage](../auto-fix-coverage.md).
