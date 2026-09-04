# JitInefficiencyCheck auto-fix coverage

The fixer rewrites the simpler patterns; structural cases (loop-bound and others) are recognized
but not auto-fixed; the reason is shown in the Auto-fix column below.

A rewrite that replaces only a sub-span leaves everything around it, including a line break,
exactly as written.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `"" + x` / `x + ""` | `String.valueOf(x)` | Yes |
| `"" + a + b` (the check reports the inner `+`) | `String.valueOf(a) + b` | Yes |
| `a + b + ""` (the check reports the outer `+`) | `String.valueOf(a + b)` | Yes |
| `"" + expr` with a `//` line comment after the expression on that line | `String.valueOf(expr) // comment`, the comment left where it was written | Yes |
| `"" + expr` with an unterminated `/*` block comment opening on that line | (not reported: nothing after the opener parses) | No |
| `"" +/*c*/x` (a block comment abutting the `+`) | `String.valueOf(/*c*/x)`: the operand's text is copied verbatim, so the comment rides along | Yes |
| `arr[i].name + ""` / `get().name + ""` (a dotted field access on a call or index result) | `String.valueOf(arr[i].name)` | Yes |
| `"" + (a + b)` (parenthesized operand) | `String.valueOf((a + b))`, parens kept | Yes |
| `"" + null` | (skipped: `"" + null` is the text `null`, while `String.valueOf(null)` binds to `valueOf(char[])` and throws a `NullPointerException`) | No |
| any operand whose type resolves to `char[]` | (skipped: `"" + chars` is the array's `toString()` (`[C@1b6d`), while `String.valueOf(char[])` returns its contents. The shapes that resolve are a name, a field, a dotted chain, a cast, an array access, a ternary whose branches agree, a varargs parameter or record component, a `var` taking its type from its initializer, an `instanceof` pattern variable, a record component read either by its accessor or by its simple name inside the record, and a call's return. A field or a method inherited from a same-file supertype resolves at any depth, through `extends` and `implements` alike, in the bare and `this.`-qualified spellings; an owner written qualified, as `Outer.Inner`, resolves too. A field additionally resolves through the `Outer.this` and named-type qualifiers, but a method does not (see the fail-open row below)) | No |
| `"" + t` for a type parameter, `"" + w()` for a call into a class the classpath does not carry, `"" + (flag ? chars : name)` for a ternary whose branches disagree, `"" + new char[]{'h'}`, `"" + rows.get(0)` for a `List<char[]>`, `"" + switch (k) {...}`, `"" + Outer.this.pick()`, `"" + this.pick()` where `pick` comes from a supertype the file does not declare | (rewritten: no type resolves, and the gate fails open by design. Each of these is a genuine `char[]` the rewrite renders differently, so this is the residual of the rule above, not a claim that they are safe) | Yes |
| `"" + (char[][]) o`, `"" + (String[]) o`, `"" + grid[0][0]`, `"" + strs[0]`, `"" + (char) c` | (rewritten: `valueOf` has no overload for any of these, so both forms reach `valueOf(Object)` or the matching scalar overload and render identically) | Yes |
| `("") + value` (the empty literal in grouping parens) | (skipped: the rewrite would emit `String.valueOf((""))` and delete `value`) | No |
| `new String("literal")` | `"literal"` | Yes |
| `new String(stringVar)` | `stringVar` | Yes |
| `new String(/* comment */ arg)` (a comment inside the constructor argument) | `/* comment */ arg`: the argument's text between the parens is copied verbatim | Yes |
| `new java.lang.String("literal")` (fully qualified) | `"literal"` | Yes |
| `new StringBuffer(...)` (local declared with `var`) | `new StringBuilder(...)`. A local whose type is spelled out (`StringBuffer sb = new StringBuffer();`) is skipped, since only the class name inside the `new` is replaced and the declaration would no longer match | Yes |
| `new java.lang.StringBuffer(...)` (fully qualified) | `new StringBuilder(...)` | Yes |
| `new Boolean(true)` / `new Boolean(false)` | `Boolean.TRUE` / `Boolean.FALSE` | Yes |
| `new Boolean(expr)` (non-literal) | `Boolean.valueOf(expr)` | Yes |
| `new Integer/Long/Double/Short/Byte/Character(x)` (also handles `new java.lang.T(x)` FQN form) | `T.valueOf(x)` | Yes |
| `new Integer (42)` (whitespace between the type and its `(`) | `Integer.valueOf(42)` | Yes |
| `new Float(1.5f)` / `new Float(1)` / `new Float("1.5")` | `Float.valueOf(...)` | Yes |
| `new Float(1.5)` / `new Float(1.5d)` / `new Float(someDouble)` / `new Float(someFloat)` | (skipped: `Float` is the one boxed type whose factory is narrower than its constructor. `new Float(double)` exists and `Float.valueOf(double)` does not, so the rewrite would not compile. The literal's suffix decides it; an identifier is refused rather than resolved, which also turns away the `float` variable that would have been safe) | No |
| `new <pkg>.T(x)`, `new <pkg>.String(x)`, `new <pkg>.StringBuffer()` where `<pkg>` is not `java.lang` | (skipped: the check names a class by its simple name, so a user-defined `Foreign.Integer` is reported too, and rewriting it would swap in a different class) | No |
| `.toArray(new T[c.size()])` where `c` is the collection the call is made on | `.toArray(new T[0])`: only the size expression is replaced, so the element type, its annotations and the spacing around the brackets all survive, and the size may sit on its own line | Yes |
| `.toArray(new T[5])` / `new T[n]` / `new T[this.n]` / `new T[arr.length]` / `new T[other.size()]` | (skipped: `Collection.toArray(T[] a)` hands back `a` itself whenever `a.length >= size`, padding the tail with nulls. Measured on a three-element list, `toArray(new String[16])` has length 16 with `[3] == null` while `toArray(new String[0])` has length 3, so any size not provably the collection's own count can change what the call returns. The check still reports these: the performance advice holds, and whether the padding is load-bearing is the developer's to know) | No |
| `.toArray(new T[a][b])` (multi-dimensional) | (not reported: only a one-dimensional array is) | No |
| `.toArray(new T[next()])` / `new T[n++]` / `new T[n + 1]` / `new T[(int) d]` / `new T[c ? n : 1]` / `new T[-1]` | (skipped by the same rule: none of these is the collection's count, and several would additionally drop a side effect or turn a `NegativeArraySizeException` into silent success) | No |
| `sb.append(a + b + ...)` where the innermost pair is already a String concatenation | `sb.append(a).append(b).append(...)` | Yes |
| `sb.append('a' + "x")`, `sb.append(-1 + "x")`, `sb.append(.5 + "x")` (numeric or char first operand, String second) | `sb.append('a').append("x")`: `StringBuilder.append` renders a number and a char the way the concatenation does, so the split preserves the value | Yes |
| `sb.append(a + b)` (both operands `String`-typed, no literal anywhere) | `sb.append(a).append(b)` | Yes |
| `sb.append("a" + ++i)` (unary or increment operand) | `sb.append("a").append(++i)`: the operand is appended whole, so it is still evaluated once | Yes |
| `sb.append(x).append("y" + z)` (chained receiver) | `sb.append(x).append("y").append(z)` | Yes |
| `((StringBuilder) o).append("a" + b)` or `(sb).append("a" + b)` or `sb . append("a" + b)` | `((StringBuilder) o).append("a").append(b)`: the receiver is kept whatever shape it has | Yes |
| `sb.append(1 + 2 + "x")` or `sb.append('a' + 'b' + "x")` (the innermost pair is arithmetic) | (skipped: `1 + 2` is `3` before anything is appended, so the split would write `12x` where the concatenation writes `3x`) | No |
| `sb.append(prefix + chars)` where an operand's type resolves to `char[]`, or is a bare `null` | (skipped: each operand is appended on its own, and `append(char[])` writes the characters where the concatenation writes the array's `toString()`; a bare `null` cannot pick an overload at all. The same two divergences the `String.valueOf` rewrite refuses, resolved through the same shapes) | No |
| `buf().append("a" + b)` or `this.out.append("a" + b)` where the receiver's type is not `StringBuilder`/`StringBuffer` | (not reported: the receiver gate is affirmative. It resolves a name, a dotted chain, `super.f`, `Outer.this.f`, a cast, a `new` of either builder and a chained `append`, and refuses anything it cannot prove is a builder) | No |
| `super.sb.append(a + b)` where the subclass declares its own `sb` of a different type | (not reported: `super.f` is resolved on the supertype rather than here, because a field the subclass redeclares shadows the one `super.` actually reads) | No |
| `this.sb.append(a + b)` inside an anonymous class body that declares its own `sb` | (not reported: `this` names that body, not the class around it, so the field is read off the nearest enclosing body. An enum constant's body does read the enum's fields, since it subclasses the enum. An anonymous body that declares no such field reads it off the type its `new` names, which is what it actually extends, rather than off the class it sits in) | No |
| `"" + pick()` or `"" + this.pick()` inside an anonymous class body or an enum constant body that declares its own `pick()` | (skipped: the frame is the class body, so a method that body declares hides the same-named one on the class around it, exactly as a field does. A body declaring no such method continues outward, and `"" + pick()` there reads the enclosing class's) | No |
| `"" + super.pick()` inside an enum constant body, where the enum declares `char[] pick()` | (skipped: a constant's body subclasses the enum, so `super.` names the enum's own method) | No |
| `sb.append(sb.length() + "x")` or `sb.append("a" + sb.length() + "b")` (an operand reads the receiver) | (skipped: the chain writes each operand into the receiver before evaluating the next, so the read would observe a half-built value) | No |
| `this.sb.append(x + this.sb.length())` (the same field reached through `this` on both sides) | (skipped: `this.f` and `super.f` are compared by the field they name, not by their spelling, so a `this.` stripped from one side earlier in the same pass does not hide the read) | No |
| any of the six single-line rewrites whose own span crosses a line break (`new Integer(\n\t42)`, `"" + foo\n\t\t.trim()`, `sb.append("a"\n\t\t+ x)`) | (skipped: joining the lines would move code the fixer was not asked to touch. Note this is the span the rewrite replaces, not the construct: a wrapped `toArray` call and a `"" + foo\n + bar` chain both still fix, because the part being replaced is on one line) | No |
| a violation whose position no longer resolves, because an earlier fix in the same pass left the buffer unparseable | (skipped, with no reason recorded: without a tree there is no category to name one) | No |
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

The builder is named `sb`, or when that is used, `stringBuilder`, then `sb2`, `sb3`, ... (the first
unused name). A name counts as used when it appears as a whole token anywhere in the file's code,
even in a scope that could not actually collide; occurrences inside strings, char literals, text
blocks, and comments do not count. Two rewritten loops in one method get distinct names, the lower
loop taking the shorter one.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| Canonical `String s = ""; for (...) s += x;` | `final var sb = new StringBuilder(); ...` | Yes |
| Operand containing bracketed arithmetic (`s = s + data[i + 1]`, `sb.append("row " + data[i + 1])`) | the operand is kept whole; an inner `+` is not read as a separator | Yes |
| `String s = "prefix"; ...` (any non-empty initializer expression) | `sb.append(<initExpr>);` after SB construction | Yes |
| Decl with unrelated stmts between decl and loop top (no `s` use) | middle lines pass through unchanged | Yes |
| `for (...) if (cond) s = s + x;` (single-if loop body) | `if (cond) sb.append(x);` | Yes |
| Multi-stmt loop body with the assignment buried (possibly nested if) | sibling stmts pass through; only assign rewrites | Yes |
| Reverse `s = x + s` / mid `s = a + s + b` | `sb.insert(0, ...)` / `sb.insert(0, a).append(b)` | Yes |
| `s += "a" + buf` or `s = a + buf + s` where an operand is `char[]` | `sb.append("a" + buf)` / `sb.insert(0, a + buf)`: a `+=` right-hand side is spliced whole and several prepends are joined into one `insert`, so the argument is a `String` however the parts render. Only an operand emitted on its own is at risk | Yes |
| `this.<field>` LHS | seeded `sb.append(this.f); ... this.f = sb.toString();` | Yes |
| `obj.<field>` LHS (qualified, non-`this`, simple receiver chain) | seeded `sb.append(obj.f); ... obj.f = sb.toString();` | Yes |
| `this.a.b` / deeper field access (simple receiver chain) | seeded `sb.append(this.a.b); ... this.a.b = sb.toString();` | Yes |
| `arr[i]` / `arr[0]` / `this.arr[k]` / `arr[i][j]` LHS, indices loop-stable | seeded `sb.append(arr[i]); ... arr[i] = sb.toString();` | Yes |
| Mid-loop reads of `s.length()` / `s.charAt(0)` / etc. (whitelist) | rewritten to `sb.<method>(...)` | Yes, unless the read sits in an append that is not evaluated first (see the bail table) |
| Pre-test `while`/`for` condition reading the accumulator through a whitelisted method (`while (s.length() < 10)`) | header rewritten to `while (sb.length() < 10)` | Yes |
| Loop header wrapping across lines, with its `{` on a later line (`for (int i = 0;\n\t\ti < n; ++i) {`) | the loop is still recognised as braced and the write-back follows its closing `}` | Yes |
| Trailing comment after the header's `{` (`for (var x : list) { // accumulate`) | the loop is still recognised as braced | Yes |
| First body statement packed onto the header line (`while (it.hasNext()) { var e = it.next();`) | the loop is still recognised as braced and the `sb.toString()` write-back follows its closing `}` instead of landing inside the body | Yes |
| Brace group inside the header (`for (var x : new String[]{"a", "b"}) {`) | the header's own braces are not counted | Yes |
| Tier-2 do-while (`do <body>; while(...);`), incl. array LHS | tier-2 if body becomes single non-chained call, else tier-3 | Yes |
| Do-while terminator cuddled onto the body's closing brace (`do {\n\ts += x;\n} while (c);`, `do\n\tif (c) {\n\t\ts += x;\n\t} while (c);`) | the loop ends on that line, so the `sb.toString()` write-back follows it rather than a later statement's `while` | Yes |
| Multi-line block comment inside the braced loop body (e.g. `/* old code: } */`) | comment-region braces ignored; matching loop close found despite comments/literals | Yes |
| Multi-line block comment inside the for-header (comment closes within the header) | rewritten normally | Yes |
| Block comment closing on the line directly above the loop header (`/* note` then `*/`, with the header on its own line below) | rewritten normally, the comment preserved verbatim | Yes |
| Whitelisted accumulator read packed onto the loop's closing-brace line (`} log(s.length());`) | the call is rewritten against the builder on that line, like any other in-loop read | Yes |
| Unsafe accumulator use packed onto the loop's closing-brace line (`} log(s.equals(t));`) | (skipped: the read is not one the builder can answer) | No |
| Loop that is a controller's unbraced body or follows a statement label (`if (c)` / `outer:` above the loop header), with a field or array-element LHS | (skipped: the construction is spliced above the loop header, where it would become the controller's body or take the label) | No |
| Closed block comment on the loop's last line (`} /* trailing */`) | the line leaves no literal open, so the `sb.toString()` write-back still follows it and the comment is preserved | Yes |

When the rewrite bails on one of the shapes below, the fixer leaves the
code alone (reason: `cannot rewrite the string concatenation in a loop: this shape is not supported`).

| Bail (no fix; check still fires) | Reason |
| --- | --- |
| The accumulator read in the loop under its other spelling: `this.f` with a bare `f`, `holder.value` with `this.holder.value`, `this.arr[k]` with `arr[k]`, or an operand on the assignment's own line (`this.f = this.f + f.length()`) | Only the spelling the assignment used is redirected to the builder, and the write-back lands after the loop, so a read through the other one sees the pre-loop value on every iteration. Both directions are covered, with any trailing chain or index carried along. A local that merely shares a name with a field is refused too, which is a lost rewrite rather than a wrong one |
| `s += buf` / `s = s + buf` / `s = buf + s` where the operand's type resolves to `char[]` | Each of these emits the operand as its own `append(...)` or `insert(0, ...)`, and the array overload writes the characters where the concatenation writes the array's `toString()`. The same gate the `String.valueOf` and `.append()` rewrites use, applied at the emission site rather than to the right-hand side as a whole |
| `s += null` / `s = s + null` / `s = null + s` | A bare `null` picks no `append`/`insert` overload (`append(CharSequence)` and `append(char[])` both match), so the emitted source does not compile |
| `String s = null;` as the accumulator's declaration | The initializer is spliced into its own `sb.append(null)`, with the same ambiguity. A `char[]` initializer cannot reach here, because a `String` variable cannot be initialized from one |
| `arr[i]` where index is the loop iteration variable | Each iteration writes a different slot, so single-StringBuilder accumulation is wrong |
| `arr[i]` where index variable is mutated in the loop body | Index changes during the loop, same aliasing concern as above |
| `arr[k.field]` / `arr[k + 1]` (non-trivial index expression) | Index analysis only handles a single identifier or integer-literal index |
| Method-call array receiver (e.g. `getArr()[i]`) | Not reported at all: the check only accepts an assignment target rooted at a plain name or `this`, since a call in the receiver could have side effects |
| Array variable / chain mutated inside the loop (incl. via method call args) | `arr` reassigned, or passed to a mutator like `Arrays.fill(arr, ...)` |
| Any dotted-receiver prefix mutated in the loop (e.g. `obj = newObj()`, `this.matrix = newMatrix()` for `obj.f[i]` / `this.matrix.cells[i]` LHS) | Reassignment of any prefix (including the leftmost segment) invalidates the post-loop write |
| Index identifier appears as live code on any line of the for-header (for-init / for-each binding, including multi-line headers; a mention only inside a header comment or string literal is ignored and the rewrite still proceeds) | Index would be undefined outside the loop where the post-rewrite reassignment runs |
| Body line packs a second statement after the accumulator assignment | Any top-level `;` in the RHS region bails, whatever the packed statement does and whatever the LHS is: a non-mutating `arr[k] = arr[k] + x; log(k);` and a plain-variable LHS are refused alongside the mutating `arr[k] = arr[k] + x; ++k;` and `this.m.c[i] = this.m.c[i] + x; this.m = newM();` |
| Receiver chain contains a method call (e.g. `getSelf().f`) | Not reported at all, for the same reason as a method-call array receiver |
| `s = s + s` (LHS appears > once in chain) | Pathological / ambiguous |
| An assignment nested inside the outer chain (`a = a + (s = s + x);`), at the inner violation's position | The rewrite would target the outer assignment to `a` rather than the reported one to `s`, so it is refused |
| `String s = "", t = "x";` (multi-variable decl) | Splitting the decl is unsafe |
| Decl with intervening `s` use between decl and loop | Pre-loop read/write not preserved by rewrite |
| Decl in a different brace scope from the loop (e.g. another method) | Cross-scope rewrite would corrupt unrelated code |
| `if/else` around the assign | Else branch handling would require non-trivial flow analysis |
| Mid-loop unsafe-method call on `s` (`equals`, `replace`, `substring`, etc.) | StringBuilder semantics differ |
| Unsafe-method call on `s` in a pre-test `while`/`for` condition (`while (s.equals(t))`) | The condition is evaluated every iteration, so it is validated like any in-loop reference; StringBuilder semantics differ |
| Accumulator written again after the loop, anywhere in the enclosing scope (`s = s.trim();` below the loop), or a text block below the loop that could hide such a write | The rewrite replaces the declaration with `final var s = sb.toString();`, so a later write would target a final variable and no longer compile |
| Operand contains unsafe-method call on `s` (e.g. `s + s.replace(...)`) | Same: would compile-fail or change semantics |
| Text block (`"""..."""`) in the loop on or below the assignment's line, or in the for-header; for an array-element LHS, anywhere in the loop scope | Line-based fixer can't reason about multi-line literals. For any other LHS, a text block in the body above the assignment is not refused and the loop is rewritten around it |
| Block comment (`/* ... */`) in the gap between decl and loop top | Multi-line literal/comment tracking not done at the gap-scan layer |
| `var s = method()`, even when `method` is a same-file helper returning `String` | A `var` accumulator is only trusted when its initializer visibly contains a string literal, so a call initializer is refused whatever it returns |
| Do-while condition spanning more than one line (`while (a\n\t\t&& b);`) | The loop's last line is not the one carrying `while`, so the write-back would land inside the condition |
| Do-while terminator sharing its line with a closing comment or text-block delimiter (`/* note\n*/ while (c);`) | Only a closing brace may precede the `while`: comment or literal text ahead of it would be misread as code |
| Whitelisted read of the accumulator in an append that is not evaluated first (`s = s + "-" + s.length()`; with a prepend present every append counts, as in `s = p + s + s.length()`) | Each emitted op but the first runs against a partially built builder, so the read would observe the accumulator mid-rewrite rather than its pre-statement value: `sb.append("-").append(sb.length())` counts the `-` it just added. A lone append that reads the accumulator (`s = s + s.length()`) is still rewritten, because its argument is evaluated before the call |
| Unary or increment operand (`s = s + ++i`, `s = s + +b`) | The rewrite would emit an argument-less `.append()` |
| Initializer containing a generic type argument with a comma (`String s = new HashMap<String, Integer>().toString();`) | A type-argument comma reads as a declarator separator, so the declaration is refused. Relational operators are unaffected: `String s = a < b ? "x" : "y";` is still rewritten |
| Declaration line carrying a packed second statement (`String s = ""; boolean first = true;`) | The whole declaration line is replaced, so splicing the init region into `sb.append(...)` would emit unparseable Java and drop the second statement's declaration |
| `"" + x` or `sb.append(a + "x")` written inside a block comment or text block carried from an earlier line | Not reported at all: the construct has no node, so the check never sees it |
| `s += "x"` in a loop, written inside a block comment or text block carried from an earlier line | Same again on the loop-rewrite path, and the damage is larger: the commented-out assignment becomes an `sb.append(...)` and a live `StringBuilder` construction plus `toString()` write-back are spliced around the loop |
| Loop header line beginning inside a block comment or text block carried from an earlier line (`/* note\n*/ for (...)`, with the accumulator assignment below it) | For a field accumulator the `StringBuilder` construction is spliced immediately above the loop header, so it would land inside the carried literal and be commented out while the body's `sb.append(...)` and the write-back stay live |
| Loop's last line leaving a block comment or text block open (`} /* trailing`) | The `toString()` write-back is emitted after that line, so the literal swallows it: a local accumulator loses its `final var s = ...` declaration and every later reference fails to compile, while a field accumulator's assignment is silently dropped and the field keeps its pre-loop value |

Part of [auto-fix coverage](../auto-fix-coverage.md).