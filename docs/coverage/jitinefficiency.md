# JitInefficiencyCheck auto-fix coverage

The fixer rewrites the simpler patterns; structural cases (loop-bound and others) are recognized
but not auto-fixed; the reason is shown in the Auto-fix column below.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `"" + x` / `x + ""` | `String.valueOf(x)` | Yes |
| `"" + a + b` or `a + b + ""` (chain: more than one non-empty operand, either orientation) | (skipped: the single-line fixer only rewrites a two-operand `"" + x` / `x + ""`; a longer chain has no single operand to wrap) | No |
| `"" + expr` with a `//` line comment before the expression's end on that line, or the concat continuing on the next line | (skipped: the comment masks to end-of-line, so the expression's end can't be located on one line and the concat may continue on the next line) | No |
| `"" + expr` with an unterminated `/*` block comment opening on that line | (skipped: the line ends inside an open block comment, so the expression's true boundary is undeterminable on one line) | No |
| `new String("literal")` | `"literal"` | Yes |
| `new String(stringVar)` | `stringVar` | Yes |
| `new String(/* comment */ arg)` (a comment inside the constructor argument) | (skipped: with a comment present the raw argument text is neither a bare identifier nor a single string literal, so the unwrapped value can't be extracted) | No |
| `new java.lang.String("literal")` (fully qualified) | (skipped: the check resolves the class through the `DOT` chain and reports, but the fixer anchors on `new String(` at the reported column and finds the qualifier instead) | No |
| `new StringBuffer(...)` (local) | `new StringBuilder(...)` | Yes |
| `new java.lang.StringBuffer(...)` (fully qualified) | (skipped: same anchor mismatch as the `new String` FQN form; reported as `local StringBuffer`) | No |
| `new Boolean(true)` / `new Boolean(false)` | `Boolean.TRUE` / `Boolean.FALSE` | Yes |
| `new Boolean(expr)` (non-literal) | `Boolean.valueOf(expr)` | Yes |
| `new Integer/Long/Double/Float/Short/Byte/Character(x)` (also handles `new java.lang.T(x)` FQN form) | `T.valueOf(x)` | Yes |
| `new <pkg>.T(x)` where `<pkg>` is not `java.lang.` (foreign qualifier) | (skipped: not necessarily JDK boxed type; rewriting to unqualified `T.valueOf` would change which class is invoked) | No |
| `new Integer (42)` (whitespace between the type and its `(`) | (skipped: the rewrite anchors on `T(` immediately after the type, so a separated paren is not matched. Shares the `qualified boxed constructor` reason, which does not describe this input) | No |
| `.toArray(new T[size])` (size != 0, single-dim) | `.toArray(new T[0])` | Yes |
| `.toArray(new @A T[size])` (annotated element type) | (skipped: rewriting to `new T[0]` would drop the type-use annotation) | No |
| `.toArray(new T[a][b])` (multi-dimensional) | (skipped: only single-dimension arrays are rewritten) | No |
| `.toArray(new T[expr])` where `expr` has side effects (e.g. `new T[next()]`) | (skipped: dropping the size expression would lose its side effect) | No |
| `.toArray(new T[\n\t\tn])` (call wrapped across lines, or a spaced close such as `new T[5] )`) | (skipped: the check reports on the `METHOD_CALL` paren line, but the rewrite is single-line: it needs the `[`, the matching `]` and the call's `)` all on the reported line) | No |
| `sb.append(a + b + ...)` (with a String literal anywhere in the chain; leading operands must be identifiers or method calls, not numeric/char literals; receiver name must not appear in any operand) | `sb.append(a).append(b).append(...)` | Yes |
| `sb.append(x).append("y" + z)` (chained receiver; the chain's root identifier is what the self-reference check uses) | `sb.append(x).append("y").append(z)` | Yes |
| `buf().append("len=" + buf().length())` (call receiver whose root appears in an operand) | (skipped) | No |
| `((StringBuilder) o).append("a" + b)` or `(sb).append("a" + b)` (receiver whose root cannot be identified) | (skipped: without a root the self-reference check cannot run, and splitting blind could duplicate a side effect) | No |
| `sb.append(1 + 2 + "x")` (numeric/char leading operand) or `sb.append("a" + sb.length() + "b")` (receiver referenced in an operand) | (skipped: the leading operands aren't promotable to String, or the receiver appears in the chain, so splitting into chained `.append()` calls could change evaluation) | No |
| `sb.append(a + b)` (both operands `String`-typed, no literal anywhere) | (skipped: the split is anchored on the first operand containing a `"`, so a concat the check flagged purely from resolved operand types has no anchor) | No |
| String `+=` / `s = s + ...` inside a loop | multi-line `StringBuilder` rewrite | Yes / skip (see sub-rules) |
| `.matches(...)` / `.replaceAll(...)` / `.split(...)` in loop | (manual: hoist `Pattern.compile`) | No |
| `Map.keySet()` for-each + `map.get(key)` body | (manual: iterate `.entrySet()`) | No |
| `Enum.values()` in loop | (manual: cache to static final) | No |
| Double-brace initialization | (manual: use `List.of(...)`/constructor) | No |
| `Pattern.compile / DateTimeFormatter.ofPattern / new SimpleDateFormat / DecimalFormat` with constant arg in method body | (manual: hoist to static final) | No |
| `new Gson()` / `new ObjectMapper()` | (not reported) | The detector requires a constructor whose first argument is a string literal, and neither library offers one, so these two set members only fire for a same-named user class taking a literal |
| Boxed numeric accumulator modified in loop | (manual: change type to primitive) | No |
| Explicit iterator `while (it.hasNext())` | (manual: convert to enhanced `for`) | No |

## String-concat-in-loop fixer detail

The multi-line `StringBuilder` rewrite handles all of the following shapes
beyond the canonical `String s = ""; for (...) s += x; return s;`. Output
uses `final var <name> = sb.toString();` (or `<this.f|obj.f> = sb.toString();`
for field LHS) to satisfy `PreferVarCheck` and `FinalLocalVariableCheck`.

The builder is named `sb` unless that name already appears as a token anywhere
in the file, in which case it becomes `stringBuilder`, then `sb2`, `sb3` and so
on. Reusing a bound name is either a duplicate-local compile error or, when the
name belongs to a field or a nested type, a silent rebinding of every later
reference to the new local. The test is deliberately coarse (any whole-token
occurrence in code, anywhere), so a name in a scope that could not actually
collide still forces the longer one; occurrences inside strings, char literals,
text blocks, and comments do not count. Two rewritten loops in one method get
distinct names, with the lower loop taking the shorter one, because the pipeline
applies fixes bottom-to-top.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| Canonical `String s = ""; for (...) s += x;` | `final var sb = new StringBuilder(); ...` | Yes |
| Operand containing bracketed arithmetic (`s = s + data[i + 1]`, `sb.append("row " + data[i + 1])`) | the operand is kept whole; the concat split tracks bracket and brace depth as well as parens, so an inner `+` is not read as a separator | Yes |
| The name `sb` is already used in the file | the builder is named `stringBuilder`, then `sb2`, `sb3`, ... (the first unused name) | Yes |
| `String s = "prefix"; ...` (any non-empty initializer expression) | `sb.append(<initExpr>);` after SB construction | Yes |
| Decl with unrelated stmts between decl and loop top (no `s` use) | middle lines pass through unchanged | Yes |
| `for (...) if (cond) s = s + x;` (single-if loop body) | `if (cond) sb.append(x);` | Yes |
| Multi-stmt loop body with the assignment buried (possibly nested if) | sibling stmts pass through; only assign rewrites | Yes |
| Reverse `s = x + s` / mid `s = a + s + b` | `sb.insert(0, ...)` / `sb.insert(0, a).append(b)` | Yes |
| `this.<field>` LHS | seeded `sb.append(this.f); ... this.f = sb.toString();` | Yes |
| `obj.<field>` LHS (qualified, non-`this`, simple receiver chain) | seeded `sb.append(obj.f); ... obj.f = sb.toString();` | Yes |
| `this.a.b` / deeper field access (simple receiver chain) | seeded `sb.append(this.a.b); ... this.a.b = sb.toString();` | Yes |
| `arr[i]` / `arr[0]` / `this.arr[k]` / `arr[i][j]` LHS, indices loop-stable | seeded `sb.append(arr[i]); ... arr[i] = sb.toString();` | Yes |
| Mid-loop reads of `s.length()` / `s.charAt(0)` / etc. (whitelist) | rewritten to `sb.<method>(...)` | Yes, unless the read sits in an append that is not evaluated first (see the bail table) |
| Pre-test `while`/`for` condition reading the accumulator through a whitelisted method (`while (s.length() < 10)`) | header rewritten to `while (sb.length() < 10)` | Yes |
| Loop header wrapping across lines, with its `{` on a later line (`for (int i = 0;\n\t\ti < n; ++i) {`) | header end located by parenthesis depth, so the loop is still recognised as braced and the write-back follows its closing `}` | Yes |
| Trailing comment after the header's `{` (`for (var x : list) { // accumulate`) | the header is classified off the masked line, so the loop is still recognised as braced | Yes |
| First body statement packed onto the header line (`while (it.hasNext()) { var e = it.next();`) | bracedness is read from the first code character after the header's `)`, not from the line ending in `{`, so the loop is still recognised as braced and the `sb.toString()` write-back follows its closing `}` instead of landing inside the body | Yes |
| Brace group inside the header (`for (var x : new String[]{"a", "b"}) {`) | the body's matching `}` is found by seeding the brace walk past the header's `)`, so the header's own braces are not counted | Yes |
| Tier-2 do-while (`do <body>; while(...);`), incl. array LHS | tier-2 if body becomes single non-chained call, else tier-3 | Yes |
| Do-while terminator cuddled onto the body's closing brace (`do {\n\ts += x;\n} while (c);`, `do\n\tif (c) {\n\t\ts += x;\n\t} while (c);`) | the loop ends on that line, so the `sb.toString()` write-back follows it rather than a later statement's `while` | Yes |
| Multi-line block comment inside the braced loop body (e.g. `/* old code: } */`) | comment-region braces ignored; matching loop close found despite comments/literals | Yes |
| Multi-line block comment inside the for-header (comment closes within the header) | header masked across lines; matching `)` found, then the header index-check runs normally | Yes |
| Block comment closing on the line directly above the loop header (`/* note` then `*/`, with the header on its own line below) | the header line's own entry state is clean, so the construction anchors normally and the comment is preserved verbatim | Yes |
| Whitelisted accumulator read packed onto the loop's closing-brace line (`} log(s.length());`) | the call is rewritten against the builder on that line, like any other in-loop read | Yes |
| Unsafe accumulator use packed onto the loop's closing-brace line (`} log(s.equals(t));`) | (skipped: the read is not one the builder can answer, and the line is inside the scanned span) | No |
| Loop that is a controller's unbraced body or follows a statement label (`if (c)` / `outer:` above the loop header), with a field or array-element LHS | (skipped: the construction is spliced above the loop header, where it would become the controller's body or take the label) | No |
| Closed block comment on the loop's last line (`} /* trailing */`) | the line leaves no literal open, so the `sb.toString()` write-back still follows it and the comment is preserved | Yes |

When the rewrite bails on one of the shapes below, the fixer leaves the
code alone (reason: string concatenation in a loop; the check still
fires; the rewrite is just left to the developer).

| Bail (no fix; check still fires) | Reason |
| --- | --- |
| `arr[i]` where index is the loop iteration variable | Each iteration writes a different slot, so single-StringBuilder accumulation is wrong |
| `arr[i]` where index variable is mutated in the loop body | Index changes during the loop, same aliasing concern as above |
| `arr[k.field]` / `arr[k + 1]` (non-trivial index expression) | Index analysis only handles a single identifier or integer-literal index |
| Method-call array receiver (e.g. `getArr()[i]`) | Receiver could have side effects |
| Array variable / chain mutated inside the loop (incl. via method call args) | `arr` reassigned, or passed to a mutator like `Arrays.fill(arr, ...)` |
| Any dotted-receiver prefix mutated in the loop (e.g. `obj = newObj()`, `this.matrix = newMatrix()` for `obj.f[i]` / `this.matrix.cells[i]` LHS) | Reassignment of any prefix (including the leftmost segment) invalidates the post-loop write |
| Index identifier appears as live code on any line of the for-header (for-init / for-each binding, including multi-line headers; a mention only inside a header comment or string literal is ignored and the rewrite still proceeds) | Index would be undefined outside the loop where the post-rewrite reassignment runs |
| Body line packs a second statement after the accumulator assignment | Any top-level `;` in the RHS region bails, whatever the packed statement does and whatever the LHS is: a non-mutating `arr[k] += x; log(k);` and a plain-variable LHS are refused alongside the mutating `arr[k] += x; ++k;` and `this.m.c[i] += x; this.m = newM();` |
| Receiver chain contains a method call (e.g. `getSelf().f`) | Receiver could have side effects |
| `s = s + s` (LHS appears > once in chain) | Pathological / ambiguous |
| `String s = "", t = "x";` (multi-variable decl) | Splitting the decl is unsafe |
| Decl with intervening `s` use between decl and loop | Pre-loop read/write not preserved by rewrite |
| Decl in a different brace scope from the loop (e.g. another method) | Cross-scope rewrite would corrupt unrelated code |
| `if/else` around the assign | Else branch handling would require non-trivial flow analysis |
| Mid-loop unsafe-method call on `s` (`equals`, `replace`, `substring`, etc.) | StringBuilder semantics differ |
| Unsafe-method call on `s` in a pre-test `while`/`for` condition (`while (s.equals(t))`) | The condition is evaluated every iteration, so it is validated like any in-loop reference; StringBuilder semantics differ |
| Accumulator written again after the loop, anywhere in the enclosing scope (`s = s.trim();` below the loop), or a text block below the loop that could hide such a write | The rewrite replaces the declaration with `final var s = sb.toString();`, so a later write would target a final variable and no longer compile |
| Operand contains unsafe-method call on `s` (e.g. `s + s.replace(...)`) | Same: would compile-fail or change semantics |
| Text block (`"""..."""`) anywhere in the loop scope (for-header or body) | Line-based fixer can't reason about multi-line literals |
| Block comment (`/* ... */`) in the gap between decl and loop top | Multi-line literal/comment tracking not done at the gap-scan layer |
| `var s = method()` returning non-String | Same-file method return-type inference handles `String`-returning helpers; rest bail |
| Do-while condition spanning more than one line (`while (a\n\t\t&& b);`) | The loop's last line is not the one carrying `while`, so the write-back would land inside the condition |
| Do-while terminator sharing its line with a closing comment or text-block delimiter (`/* note\n*/ while (c);`) | Only a closing brace may precede the `while`: the in-loop reference scan reads that line from a cold lexer state, so comment or literal text ahead of it would be misread as code |
| Whitelisted read of the accumulator in an append that is not evaluated first (`s = s + "-" + s.length()`; with a prepend present every append counts, as in `s = p + s + s.length()`) | Each emitted op but the first runs against a partially built builder, so the read would observe the accumulator mid-rewrite rather than its pre-statement value: `sb.append("-").append(sb.length())` counts the `-` it just added. A lone append that reads the accumulator (`s = s + s.length()`) is still rewritten, because its argument is evaluated before the call |
| Unary or increment operand (`s = s + ++i`, `s = s + +b`) | The concat split is by top-level `+` with no unary/binary distinction, so the operand list gains a blank entry and the rewrite would emit an argument-less `.append()` |
| Initializer containing a generic type argument with a comma (`String s = new HashMap<String, Integer>().toString();`) | The declarator scan does not treat `<`/`>` as a bracket pair, because a relational `<` would otherwise open a group that hides every later comma while a relational `>`, a lambda `->` or a shift `>>` closes it again, leaving the scan balanced and a real declarator separator undetected. Without that pairing a type-argument comma reads as a separator, so the declaration is refused. Relational operators themselves are unaffected: `String s = a < b ? "x" : "y";` is still rewritten |
| Declaration line carrying a packed second statement (`String s = ""; boolean first = true;`) | The whole declaration line is replaced, so splicing the init region into `sb.append(...)` would emit unparseable Java and drop the second statement's declaration |
| `"" + x` whose operand is a generic constructor (`"" + new ArrayList<>()`) | The operand scan reads the type-argument `<` as a comparison and stops there, truncating the operand mid-expression; a `<` or `>` cannot legally end a String-concat operand, so landing on one is treated as a mis-parse and refused |
| `expr + ""` whose left operand does not end in an identifier or dotted name (`f() + ""`, `arr[0] + ""`) | The left-operand scan walks back over identifier characters and `.` only, so it finds no operand to wrap |
| `"" + x` written inside a block comment or text block carried from an earlier line | Every scan on this path masks from a cold lexer state, so carried content reads as live code and the rewrite would splice the literal's terminator into a call argument; the fixer refuses when the line begins inside a multi-line literal |
| `sb.append(a + "x")` written inside a block comment or text block carried from an earlier line | Same cold-state problem on the append-splitting path: the in-line `"""` test only catches a literal that opens on this line, so a commented-out append would be split into a live chain. Refused when the line begins inside a multi-line literal |
| `s += "x"` in a loop, written inside a block comment or text block carried from an earlier line | Same again on the loop-rewrite path, and the damage is larger: the commented-out assignment becomes an `sb.append(...)` and a live `StringBuilder` construction plus `toString()` write-back are spliced around the loop. The in-line `/*` test only catches a comment that opens on this line, so the carried state is checked too |
| Loop header line beginning inside a block comment or text block carried from an earlier line (`/* note\n*/ for (...)`, with the accumulator assignment below it) | For a field accumulator the `StringBuilder` construction is spliced immediately above the loop header, so it would land inside the carried literal and be commented out while the body's `sb.append(...)` and the write-back stay live. The body line's own entry state is clean in this shape, so the body-line check above does not reach it |
| Loop's last line leaving a block comment or text block open (`} /* trailing`) | The `toString()` write-back is emitted after that line, so the literal swallows it: a local accumulator loses its `final var s = ...` declaration and every later reference fails to compile, while a field accumulator's assignment is silently dropped and the field keeps its pre-loop value |

Part of [auto-fix coverage](../auto-fix-coverage.md).