# BraceOwnLineCheck auto-fix coverage

An opening brace must end the line its header ends on, for control statements and declarations
alike. The fixer pulls the `{` up onto the header's last line: the `)` line of a multi-line
condition or parameter list, the `throws` / `implements` / `permits` line of a declaration, the
`->` line of a lambda or switch rule.

A single-statement brace-optional body (`if`, `else`, `for`, for-each, `while`, `do`) whose braces
[ControlFlowBraces](controlflowbraces.md) reports as unnecessary is left to that check, which removes
the braces and the own-line brace with them. Only when its fixer refuses the unwrap is the brace
reported here too, so the two checks report the same keyword and this one pulls the brace up.

## Supported (auto-fixable)

| Violation type | Pattern | Auto-fix | Notes |
| --- | --- | --- | --- |
| Control header, multi-statement / declaration / empty / one-line body | `if (c)\n{\n\ta;\n\tb;\n}`, `if (c)\n{\n}`, `if (c)\n{ a; b; }` | Yes | `{` joins the header; a one-line block stays one line (`if (c) { a; b; }`). Covers `if`, `else`, `else if`, cuddled `} else`, `for`, for-each, `for (;;)`, `while`, `do` (with the `while` on its own line or cuddled) |
| Brace-required headers | `try\n{`, `try (var r = open())\n{`, `catch (E e)\n{`, `finally\n{`, `switch (x)\n{`, `synchronized (o)\n{` | Yes | Even a one-statement body, whose braces are load-bearing. Multi-catch, an annotated catch parameter, an existing-variable resource and a resource list spanning lines included |
| Switch expression / arrow-rule block | `= switch (x)\n{`, `case 1 ->\n{`, `default ->\n{` | Yes | Reported at the `switch`, `case` or `default` |
| Labeled bare block | `check:\n{` | Yes | The label is the header; reported at the label. A label ahead of a loop (`outer:\nfor (...)\n{`) leaves the loop as the header |
| Method / constructor / compact constructor | `void m()\n{`, `T(int a)\n{`, `R\n{` | Yes | Reported at the name. Including annotated, generic, receiver-parameter, interface default and private methods, a `throws` clause on its own line and a multi-line parameter list (`)\n{` becomes `) {`) |
| Class / interface / enum / annotation type / static initializer | `class C\n{`, `static\n{` | Yes | Top-level, nested, inner, local and annotated local types, multi-line `extends` / `implements` / `permits` clauses, and members of a record. A record's own brace is excluded: [RecordFormatting](recordformatting.md) owns it |
| Lambda / anonymous class | `() ->\n{`, `x ->\n{`, `new X()\n{`, `new X()\n{}` | Yes | Reported at the `->` or `new`, also as a call argument and nested. An anonymous class's instance initializer is not reported |
| Fallback when ControlFlowBraces refuses to unwrap | `if (c)\n{ /* note */\n\ta;\n}`, `if (a\n\t&& b)\n{\n\tx;\n}`, `do\n{ x;\n} while (c);` | Yes | One per refusal reason: content on the brace line, a comment ahead of the brace or ahead of the `}`, a `}` past a text block, an `else` that would re-bind, a multi-line header, and the `do` forms. A `{ // note` fallback converges further: once the brace is up, ControlFlowBraces unwraps it to `if (c) // note` |
| Header line comment | `if (c) // why\n{`, `if (c) /* why */\n{` | Yes | The `{` goes ahead of the comment: `if (c) { // why` |
| Comment after the brace | `if (c)\n{ // why` | Yes | Rides up with the brace: `if (c) { // why`. When the header already has a comment, or a comment between the header and the brace moved into the block, it follows those as the block's next line instead, which keeps source order and keeps a block comment it opens from swallowing them |
| Comment between header and brace | `if (c)\n\t// why\n{`, `if (c)\n/* a\n   b */\n{`, `void m()\n/** why */\n{`, `if (c)\n/* why */ {` | Yes | Becomes the block's first line at the body's indentation; a multi-line comment shifts as a whole (a continuation line not carrying the comment's own indent stays as written), and a Javadoc is treated the same. Code after the brace then follows the comment rather than riding up ahead of it |
| Header opens a multi-line block comment | `if (c) /* a\n   b */\n{`, `if (c) /* a\n   b */ {` | Yes | The `{` goes right after the `)`, and the comment's continuation lines stay where they are, including one the brace shared |
| Code after the brace when the header has a comment | `if (c) // a\n{ x;\n\ty;\n}`, `if (c) // a\n{ x; y; }`, `void m() // a\n{}` | Yes | The code opens the block on its own line; a block that closed on the brace line is split so its `}` keeps the statement's indentation (`void m() { // a\n}`). A one-line block with a mis-indented brace and no comment stays one line (`if (c) { x; y; }`) |
| Blank line between header and brace | `if (c)\n\n{` | Yes | Dropped |
| Multi-line header | `if (a\n\t\t&& b)\n{`, a header ending in a text block's closing `"""))` | Yes | `{` on the header's last line |
| GNU or mis-indented brace | `if (c)\n\t{\n\t\tx;\n\t}`, `if (c)\n{` one level shallower, a tab-and-space brace indent | Yes | The body and `}` are re-indented to the statement's indentation by the same amount the brace moved. A text block in the body shifts with it, which keeps its value. Blank lines stay blank, and a body line not carrying the brace's indent (a comment at column 0) stays as written. A brace line that begins inside a block comment's continuation is not taken as a re-indent |
| Line past 120 columns after the pull-up | | Yes | No length limit applies |

## Not supported

| Pattern | Reason |
| --- | --- |
| GNU or mis-indented brace whose body holds a text block content line that does not carry the brace's indent (`"""\ntext\n\t\t\t""";` with `text` at column 0) | `cannot re-indent the block: a text block's value would change`. Shifting that line by a different amount than the rest changes the string's incidental indentation |
| A buffer that does not parse | `cannot move the opening brace: it cannot be located without a parse`. Only reachable after an earlier fix in the same pass |
| The brace already sits on its header's line at the reported position | `cannot move the opening brace: an earlier fix in this pass moved it` |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| Single-statement brace-optional body ControlFlowBraces unwraps | `if (c)\n{\n\ta;\n}`, `do\n{\n\ta;\n}\nwhile (c);`, a comment between the header and the brace included: removing the braces fixes it |
| No header | an instance initializer (after a field, after a comment, after enum constants, in an anonymous class), a bare block, an array initializer (`= \n{`, `new int[]\n{`), an annotation array value, a colon `case` block, an enum constant body |
| Record declaration | `record R(int a)\n{}`: [RecordFormatting](recordformatting.md) |
| Text inside a comment or text block | `// if (x)\n// {`, a text block holding Allman code |

Part of [auto-fix coverage](../auto-fix-coverage.md).