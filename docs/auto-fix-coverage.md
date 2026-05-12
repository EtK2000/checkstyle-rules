# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.

## TreeWalker checks (FIXERS map)

| Check                                    | Fixer                              | Notes                                                                                                                                                                                                                            |
|------------------------------------------|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AnnotationOwnLineCheck                   | AnnotationOwnLineFixer             | Splits stacked/embedded annotations to own lines, removes blank lines, sorts alphabetically                                                                                                                                      |
| AnnotationSameLineCheck                  | AnnotationSameLineFixer            | Joins annotations onto declaration line, sorts inline annotations alphabetically                                                                                                                                                 |
| ArrayTypeStyleCheck                      | ArrayTypeStyleFixer                | Moves C-style brackets to the type position. See [sub-rules below](#arraytypestylecheck-sub-rules)                                                                                                                               |
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer      | Removes `super()` call                                                                                                                                                                                                           |
| ConstructorAssignmentOrderCheck          | ConstructorAssignmentOrderFixer    | Sorts `this.xxx = ...` assignments by group (simple, multi-line, var-dependent) then alphabetically; handles dependencies                                                                                                        |
| ControlFlowBracesCheck                   | ControlFlowBracesFixer             | Do-while: removes unnecessary braces, fixes one-liners, adds missing braces. Non-do-while: adds braces to multi-line braceless bodies                                                                                            |
| ExplicitInitializationCheck              | ExplicitInitializationFixer        | Removes `= 0`/`= null`/`= false` etc.                                                                                                                                                                                            |
| FieldConsolidationCheck                  | FieldConsolidationFixer            | Merges consecutive same-type fields; wraps across lines if >120 chars. See [C-style arrays](c-style-array-fixer.md) and [limitations](#fieldconsolidationfixer-limitations)                                                      |
| FieldSortingCheck                        | FieldSortingFixer                  | Enum constants: sorts alphabetically, splits same-line. Fields: sorts by chunk, type (primitives first), annotations, type-arg annotations, name; handles dependencies                                                           |
| FinalLocalVariableCheck                  | FinalLocalVariableFixer            | Adds `final` keyword                                                                                                                                                                                                             |
| JitInefficiencyCheck                     | JitInefficiencyFixer               | See sub-rules below                                                                                                                                                                                                              |
| LambdaParameterTypeCheck                 | LambdaParameterTypeFixer           | See sub-rules below                                                                                                                                                                                                              |
| NoArrayTrailingCommaCheck                | NoArrayTrailingCommaFixer          | Removes trailing comma                                                                                                                                                                                                           |
| NoBlankLineBetweenSingleCasesCheck       | NoBlankLineBetweenSingleCasesFixer | Removes blank line                                                                                                                                                                                                               |
| NoEnumTrailingCommaCheck                 | NoArrayTrailingCommaFixer          | Same fixer as array trailing comma                                                                                                                                                                                               |
| NoEnumTrailingSemicolonCheck             | NoEnumTrailingSemicolonFixer       | Removes trailing semicolon; deletes line if semicolon was the only content                                                                                                                                                       |
| NoFinalParametersCheck                   | RedundantModifierFixer             | Removes `final` keyword from parameter                                                                                                                                                                                           |
| NoUnnecessaryThisCheck                   | NoUnnecessaryThisFixer             | Removes `this.` prefix                                                                                                                                                                                                           |
| PreferBulkOperationCheck                 | PreferBulkOperationFixer           | See sub-rules below                                                                                                                                                                                                              |
| PreferCollectionInterfaceCheck           | PreferCollectionInterfaceFixer     | Replaces concrete collection type with interface (e.g. `ArrayList` to `List`)                                                                                                                                                    |
| PreferDirectBooleanReturnCheck           | PreferDirectBooleanReturnFixer     | Collapses `if (cond) return BOOL_LIT;` paired with opposite-literal return into `return cond;` / `return !cond;`. Applies `!!X` -> `X` simplification                                                                            |
| PreferDoWhileCheck                       | PreferDoWhileFixer                 | Collapses pre-loop statement + `while` into a tier-2 `do-while`                                                                                                                                                                  |
| PreferExactAssertionCheck                | PreferExactAssertionFixer          | Converts `assertTrue/assertFalse(x instanceof Y)` to `assertInstanceOf/assertNotInstanceOf(Y.class, x)`. See [sub-rules below](#preferexactassertioncheck-sub-rules)                                                             |
| PreferMathMethodCheck                    | PreferMathMethodFixer              | See sub-rules below                                                                                                                                                                                                              |
| PreferPrefixIncrementCheck               | PreferPrefixIncrementFixer         | Moves `++`/`--` to prefix position                                                                                                                                                                                               |
| PreferSpecificApiCheck                   | PreferSpecificApiFixer             | See sub-rules below                                                                                                                                                                                                              |
| PreferStandardCharsetsCheck              | PreferStandardCharsetsFixer        | Replaces charset string literal with `StandardCharsets.X` constant (adds import)                                                                                                                                                 |
| PreferStaticImportCheck                  | PreferStaticImportFixer            | Strips `Class.` prefix from a qualified call and adds an `import static <fqcn>.<method>;`                                                                                                                                        |
| PreferVarCheck                           | PreferVarFixer                     | Replaces type with `var`; converts explicit array init to implicit; replaces `<Object>` with `<>`                                                                                                                                |
| RecordFormattingCheck                    | RecordFormattingFixer              | Brace formatting: collapses non-canonical spacing/newlines between `)` and `{`, joins `{`/`}` for empty bodies, splits `{...}` onto multiple lines for non-empty bodies. See [sub-rules below](#recordformattingcheck-sub-rules) |
| RedundantAnnotationSyntaxCheck           | RedundantAnnotationSyntaxFixer     | Removes `()` or `value =`                                                                                                                                                                                                        |
| RedundantArrayCreationCheck              | RedundantArrayCreationFixer        | Removes `new Type[]{...}` wrapper, extracts elements directly; removes empty array with preceding comma                                                                                                                          |
| RedundantEqualityBranchCheck             | RedundantEqualityBranchFixer       | Collapses redundant if-else with `==`/`!=` condition. See [sub-rules below](#redundantequalitybranchcheck-sub-rules)                                                                                                             |
| RedundantImportCheck                     | DeleteLineFixer                    | Deletes import line                                                                                                                                                                                                              |
| RedundantModifierCheck                   | RedundantModifierFixer             | Removes redundant modifier keyword                                                                                                                                                                                               |
| RedundantNumericSuffixCheck              | RedundantNumericSuffixFixer        | Removes redundant `L`/`f`/`d` suffix                                                                                                                                                                                             |
| UnusedImportsCheck                       | DeleteLineFixer                    | Deletes import line                                                                                                                                                                                                              |
| UpperEllCheck                            | UpperEllFixer                      | Changes `l` to `L`                                                                                                                                                                                                               |

## Regex checks (MODULE_ID_FIXERS map)

| Module ID                     | Fixer                            | Notes                                                          |
|-------------------------------|----------------------------------|----------------------------------------------------------------|
| BlankLineAfterBreak           | BlankLineAfterBreakFixer         | Inserts blank line after `break;` before next `case`/`default` |
| NoBlankLineAfterClassBrace    | BlankLineAfterClassBraceFixer    | Removes blank lines after class/interface/enum/record `{`      |
| NoBlankLineBeforeClosingBrace | BlankLineBeforeClosingBraceFixer | Removes blank lines before `}`                                 |
| NoDoubleBlankLines            | DoubleBlankLineFixer             | Removes extra blank line                                       |
| NoTrailingNewline             | TrailingNewlineFixer             | Removes trailing blank lines at EOF                            |
| NoTrailingWhitespace          | TrailingWhitespaceFixer          | Trims trailing whitespace                                      |

## ArrayTypeStyleCheck sub-rules

The fixer moves C-style brackets from after the variable name to the type position. It blanks
comments before scanning (so punctuation inside `/* */` and `//` is ignored) and walks across lines
when the bracket sits on a different line from the rest of the declaration. The output preserves
the original comments and surrounding whitespace.

### Supported (auto-fixable)

| Pattern                                               | Fix                                 | Notes                                      |
|-------------------------------------------------------|-------------------------------------|--------------------------------------------|
| `int x[];`                                            | `int[] x;`                          | Field, local, parameter, record component  |
| `int x[][];`                                          | `int[][] x;`                        | Compound C-style                           |
| `int[] x[];`                                          | `int[][] x;`                        | Mixed Java + C-style                       |
| `int x[] = {1};`                                      | `int[] x = {1};`                    | With initializer                           |
| `int x [];`                                           | `int[] x;`                          | Whitespace between name and `[`            |
| `int x[ ];`                                           | `int[ ] x;`                         | Whitespace inside brackets preserved       |
| `final int x[];`                                      | `final int[] x;`                    | With modifiers                             |
| `public static final int x[];`                        | `public static final int[] x;`      | Multiple modifiers                         |
| `@Deprecated int x[];`                                | `@Deprecated int[] x;`              | Declaration annotations preserved          |
| `int @TypeAnno [] x;`                                 | (clean — no violation)              | Type-use annotation in Java-style position |
| `List<String> x[];`                                   | `List<String>[] x;`                 | Generic type                               |
| `List<? extends Number> x[];`                         | `List<? extends Number>[] x;`       | Wildcard generic                           |
| `Map<K, V> x[];`                                      | `Map<K, V>[] x;`                    | Multiple type args                         |
| `Map<String, List<Integer>> x[];`                     | `Map<String, List<Integer>>[] x;`   | Nested generics                            |
| `void m(int x[])`                                     | `void m(int[] x)`                   | Method parameter                           |
| `void m(int a[], int b)`                              | `void m(int[] a, int b)`            | Multi-param, C-style on first              |
| `void m(int x, int y[])`                              | `void m(int x, int[] y)`            | Multi-param, C-style on last               |
| `void m(int a, int b[], int c)`                       | `void m(int a, int[] b, int c)`     | Multi-param, C-style in middle             |
| `(int a[], int b) -> {}` (lambda)                     | `(int[] a, int b) -> {}`            | Multi-param lambda                         |
| `record R(int x[]) {}`                                | `record R(int[] x) {}`              | Record component                           |
| `record R<T>(int x[], String s) {}`                   | `record R<T>(int[] x, String s) {}` | Generic record component                   |
| `record R<T extends List<String>>(int x[], int y) {}` | (corresponding fix)                 | Nested type-param bounds                   |
| `int m()[] { return null; }`                          | `int[] m() { return null; }`        | Method return type                         |
| `int m()[][]`                                         | `int[][] m()`                       | Compound method return                     |
| `int[] m()[]`                                         | `int[][] m()`                       | Java + C-style method return               |
| `int m()[] throws X { ... }`                          | `int[] m() throws X { ... }`        | Method return + throws                     |
| `int m()[];`                                          | `int[] m();`                        | Abstract method                            |
| `<T> T m()[]`                                         | `<T> T[] m()`                       | Generic method                             |
| `List<String> m()[]`                                  | `List<String>[] m()`                | Generic return type                        |
| `int x\n[];` (multi-line)                             | `int[] x\n;`                        | Bracket on next line                       |
| `int m()\n[]\n{...}` (multi-line return)              | `int[] m()\n{...}`                  | Method return, bracket-only line removed   |
| `int x[] /* note */ = a;`                             | `int[] x /* note */ = a;`           | Comments after `]` preserved               |
| `int x[] = a; // hello, world`                        | `int[] x = a; // hello, world`      | Comma in trailing comment ignored          |
| `/* doc */ final int x[];`                            | `/* doc */ final int[] x;`          | Leading block comment preserved            |

### Not supported (check fires, fixer returns null)

| Pattern                                                           | Reason                                                                      |
|-------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Multi-variable single-line (`int a[], b;` / `int a, b[];`)        | Moving brackets would retype the sibling; depth-aware comma scan bails      |
| Multi-var with initializer (`int x[] = {1}, y = 0;`)              | Comma after `]` (depth 0, before `;`) detected by multi-line scan           |
| Multi-var across lines (`int x\n[], y;` / `int x\n[]\n, y;`)      | Multi-line forward scan tracks depth + literals across lines                |
| Multi-var spanning paren initializer (`int x[] = foo(a,\nb), y;`) | Multi-line scan continues past unclosed paren on bracket line               |
| C-style with type-use annotation on bracket (`int x @Anno []`)    | Walk-back from `[` lands on `@`; preserving the annotation is ambiguous     |
| Comment between IDENT and `[` (`int x /* note */ [];`)            | Dropping or preserving the gap comment is ambiguous                         |
| For-loop multi-var init (`for (int x[] = a, y = 1; ...)`)         | Keyword denylist (`for`/`if`/`while`/...) prevents treating as param list   |
| Method-return as expression-context bracket (`x = bar()[]`)       | Char before method ident is `=`/operator, not a type-end char               |
| Method-return type-use annotation (`int m() @A []`)               | Conservative bail; preserving annotation through the move is unsafe         |
| Junk after method-return brackets (`int m()[] foo`)               | Tightened next-char check requires `{`, `;`, or exact `throws` keyword      |
| Multi-line declaration first line (no prev line)                  | Walk-back has no preceding declaration to insert brackets into              |
| Multi-line with empty/whitespace-only prev line                   | No identifier or `)` to anchor the type-end                                 |
| Multi-line with method-call argument list on prev line            | Returns null when the prev line ends in non-ident/`)` (e.g. `}`, `;`)       |
| Unclosed bracket (`int x[abc];` or `int x[`)                      | `findBracketsEnd` requires `[` immediately followed by optional ws then `]` |

## JitInefficiencyCheck sub-rules

The fixer handles textual rewrites for the simpler patterns; check-only patterns
(loop-bound and structural cases) are detected but not auto-fixed.

| Pattern                                                                                                                                       | Replacement                              | Auto-fix            |
|-----------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|---------------------|
| `"" + x` / `x + ""`                                                                                                                           | `String.valueOf(x)`                      | Yes                 |
| `new String("literal")`                                                                                                                       | `"literal"`                              | Yes                 |
| `new String(stringVar)`                                                                                                                       | `stringVar`                              | Yes                 |
| `new StringBuffer(...)` (local)                                                                                                               | `new StringBuilder(...)`                 | Yes                 |
| `new Boolean(true)` / `new Boolean(false)`                                                                                                    | `Boolean.TRUE` / `Boolean.FALSE`         | Yes                 |
| `new Boolean(expr)` (non-literal)                                                                                                             | `Boolean.valueOf(expr)`                  | Yes                 |
| `new Integer/Long/Double/Float/Short/Byte/Character(x)`                                                                                       | `T.valueOf(x)`                           | Yes                 |
| `.toArray(new T[size])` (size != 0, single-dim)                                                                                               | `.toArray(new T[0])`                     | Yes                 |
| `sb.append(a + b + ...)` (with String operand)                                                                                                | `sb.append(a).append(b).append(...)`     | Yes                 |
| String `+=` / `s = s + ...` inside a loop                                                                                                     | multi-line `StringBuilder` rewrite       | Yes (see sub-rules) |
| `.matches(...)` / `.replaceAll(...)` / `.split(...)` in loop                                                                                  | (manual: hoist `Pattern.compile`)        | No                  |
| `Map.keySet()` for-each + `map.get(key)` body                                                                                                 | (manual: iterate `.entrySet()`)          | No                  |
| `Enum.values()` in loop                                                                                                                       | (manual: cache to static final)          | No                  |
| Double-brace initialization                                                                                                                   | (manual: use `List.of(...)`/constructor) | No                  |
| `Pattern.compile / DateTimeFormatter.ofPattern / new SimpleDateFormat / Gson / ObjectMapper / DecimalFormat` with constant arg in method body | (manual: hoist to static final)          | No                  |
| Boxed numeric accumulator modified in loop                                                                                                    | (manual: change type to primitive)       | No                  |
| Explicit iterator `while (it.hasNext())`                                                                                                      | (manual: convert to enhanced `for`)      | No                  |

### String-concat-in-loop fixer detail

The multi-line `StringBuilder` rewrite handles all of the following shapes
beyond the canonical `String s = ""; for (...) s += x; return s;`. Output
uses `final var <name> = sb.toString();` (or `<this.f|obj.f> = sb.toString();`
for field LHS) to satisfy `PreferVarCheck` and `FinalLocalVariableCheck`.

| Shape                                                                      | Output                                                      | Auto-fix |
|----------------------------------------------------------------------------|-------------------------------------------------------------|----------|
| Canonical `String s = ""; for (...) s += x;`                               | `final var sb = new StringBuilder(); ...`                   | Yes      |
| `String s = "prefix"; ...` (any non-empty initializer expression)          | `sb.append(<initExpr>);` after SB construction              | Yes      |
| Decl with unrelated stmts between decl and loop top (no `s` use)           | middle lines pass through unchanged                         | Yes      |
| `for (...) if (cond) s = s + x;` (single-if loop body)                     | `if (cond) sb.append(x);`                                   | Yes      |
| Multi-stmt loop body with the assignment buried (possibly nested if)       | sibling stmts pass through; only assign rewrites            | Yes      |
| Reverse `s = x + s` / mid `s = a + s + b`                                  | `sb.insert(0, ...)` / `sb.insert(0, a).append(b)`           | Yes      |
| `this.<field>` LHS                                                         | seeded `sb.append(this.f); ... this.f = sb.toString();`     | Yes      |
| `obj.<field>` LHS (qualified, non-`this`, simple receiver chain)           | seeded `sb.append(obj.f); ... obj.f = sb.toString();`       | Yes      |
| `this.a.b` / deeper field access (simple receiver chain)                   | seeded `sb.append(this.a.b); ... this.a.b = sb.toString();` | Yes      |
| `arr[i]` / `arr[0]` / `this.arr[k]` / `arr[i][j]` LHS, indices loop-stable | seeded `sb.append(arr[i]); ... arr[i] = sb.toString();`     | Yes      |
| Mid-loop reads of `s.length()` / `s.charAt(0)` / etc. (allowlist)          | rewritten to `sb.<method>(...)`                             | Yes      |
| Tier-2 do-while (`do <body>; while(...);`), incl. array LHS                | tier-2 if body becomes single non-chained call, else tier-3 | Yes      |

| Bail (no fix; check still fires)                                                                                                                | Reason                                                                                                                |
|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `arr[i]` where index is the loop iteration variable                                                                                             | Each iteration writes a different slot — single-StringBuilder accumulation is wrong                                   |
| `arr[i]` where index variable is mutated in the loop body                                                                                       | Index changes during the loop, same aliasing concern as above                                                         |
| `arr[k.field]` / `arr[k + 1]` (non-trivial index expression)                                                                                    | Index analysis only handles single IDENT or integer-literal indices                                                   |
| Method-call array receiver (e.g. `getArr()[i]`)                                                                                                 | Receiver could have side effects                                                                                      |
| Array variable / chain mutated inside the loop (incl. via method call args)                                                                     | `arr` reassigned, or passed to a mutator like `Arrays.fill(arr, ...)`                                                 |
| Any dotted-receiver prefix mutated in the loop (e.g. `obj = newObj()`, `this.matrix = newMatrix()` for `obj.f[i]` / `this.matrix.cells[i]` LHS) | Reassignment of any prefix — including the leftmost segment — invalidates the post-loop write                         |
| Index identifier appears on any line of the for-header (for-init / for-each binding, including multi-line headers)                              | Index would be undefined outside the loop where the post-rewrite reassignment runs                                    |
| Body line packs multiple statements that mutate the array, an index variable, or any dotted receiver prefix                                     | Mutation on the same line as the LHS still bails; covers `arr[k] += x; ++k;` and `this.m.c[i] += x; this.m = newM();` |
| Unparseable for-header (e.g. unclosed block comment in header) for a classic-for loop                                                           | Validator fails closed since binding semantics can't be verified                                                      |
| Receiver chain contains a method call (e.g. `getSelf().f`)                                                                                      | Receiver could have side effects                                                                                      |
| `s = s + s` (LHS appears > once in chain)                                                                                                       | Pathological / ambiguous                                                                                              |
| `String s = "", t = "x";` (multi-variable decl)                                                                                                 | Splitting the decl is unsafe                                                                                          |
| Decl with intervening `s` use between decl and loop                                                                                             | Pre-loop read/write not preserved by rewrite                                                                          |
| Decl in a different brace scope from the loop (e.g. another method)                                                                             | Cross-scope rewrite would corrupt unrelated code                                                                      |
| `if/else` around the assign                                                                                                                     | Else branch handling would require non-trivial flow analysis                                                          |
| Mid-loop unsafe-method call on `s` (`equals`, `replace`, `substring`, etc.)                                                                     | StringBuilder semantics differ                                                                                        |
| Operand contains unsafe-method call on `s` (e.g. `s + s.replace(...)`)                                                                          | Same — would compile-fail or change semantics                                                                         |
| Text block (`"""..."""`) in loop body                                                                                                           | Line-based fixer can't reason about multi-line literals                                                               |
| Block comment (`/* ... */`) in the gap between decl and loop top                                                                                | Multi-line literal/comment tracking not done at the gap-scan layer                                                    |
| `var s = method()` returning non-String                                                                                                         | Same-file method return-type inference handles `String`-returning helpers; rest bail                                  |
| `String[] s` (and other non-String-typed `s`)                                                                                                   | Defensive bail in fixer; check shouldn't fire                                                                         |

## PreferBulkOperationCheck sub-rules

The fixer delegates multi-line paren balancing, comment stripping, and receiver extraction to
`LambdaCallParser` (shared across fixers). It preserves any non-nested prefix on the line (e.g.
`if (flag) source.forEach(...)` becomes `if (flag) target.putAll(source);`), and bails on truly
nested cases (unclosed parens or a `->` in the prefix).

| Pattern                                                                | Replacement                                    | Auto-fix |
|------------------------------------------------------------------------|------------------------------------------------|----------|
| `for (var x : source) target.add(x)`                                   | `target.addAll(source)`                        | Yes      |
| `for (var i = 0; i < source.size(); ++i) target.add(source.get(i))`    | `target.addAll(source)`                        | Yes      |
| `for (var e : source.entrySet()) target.put(e.getKey(), e.getValue())` | `target.putAll(source)`                        | Yes      |
| `source.forEach((k, v) -> target.put(k, v))`                           | `target.putAll(source)`                        | Yes      |
| `source.forEach(target::put)`                                          | `target.putAll(source)`                        | Yes      |
| `list.forEach(item -> other.add(item))`                                | `other.addAll(list)`                           | Yes      |
| `list.forEach(other::add)`                                             | `other.addAll(list)`                           | Yes      |
| `for (var i = 0; i < src.length; ++i) dst[i] = src[i]`                 | `System.arraycopy(src, 0, dst, 0, src.length)` | Yes      |
| `for (var i = 0; i < arr.length; ++i) arr[i] = value`                  | `Arrays.fill(arr, value)`                      | Yes      |
| Single-line block-body lambda (e.g. `-> { target.put(k, v); }`)        | `target.putAll(source)`                        | Yes      |
| Multi-line block-body lambda (`-> {` line + body + `});` line)         | `target.putAll(source)`                        | Yes      |

## PreferExactAssertionCheck sub-rules

The check fires on `assertTrue`/`assertFalse` whose argument is a comparison operator or
`instanceof` expression. The `instanceof` form is framework-gated: JUnit 4's `Assert` has no
`assertInstanceOf`/`assertNotInstanceOf`, so the check suppresses the violation when the call
resolves to JUnit 4. Resolution rule: a qualified call decides on the receiver's simple name
(`Assert` -> suppress, `Assertions` -> fire); an unqualified call requires a static import of
`Assertions` with no static import of `Assert`. Only static imports count; non-static type
imports don't enable unqualified method resolution and are ignored.

### Supported (auto-fixable)

| Pattern                                                       | Fix                                                             | Notes                                                                                                                               |
|---------------------------------------------------------------|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `assertTrue(x instanceof Y)`                                  | `assertInstanceOf(Y.class, x)`                                  | Unqualified, JUnit 5 static import in scope                                                                                         |
| `assertFalse(x instanceof Y)`                                 | `assertNotInstanceOf(Y.class, x)`                               | Polarity flip                                                                                                                       |
| `assertTrue(!(x instanceof Y))`                               | `assertNotInstanceOf(Y.class, x)`                               | Single negation cancels with `assertTrue`                                                                                           |
| `assertFalse(!(x instanceof Y))`                              | `assertInstanceOf(Y.class, x)`                                  | Single negation cancels with `assertFalse`                                                                                          |
| `assertTrue(!!(x instanceof Y))`                              | `assertInstanceOf(Y.class, x)`                                  | Double-negation parity                                                                                                              |
| `assertTrue(o instanceof java.lang.String)`                   | `assertInstanceOf(java.lang.String.class, o)`                   | Fully-qualified type preserved                                                                                                      |
| `assertTrue(o instanceof java.util.Map.Entry)`                | `assertInstanceOf(java.util.Map.Entry.class, o)`                | Nested type                                                                                                                         |
| `assertTrue(ex.getCause() instanceof RuntimeException)`       | `assertInstanceOf(RuntimeException.class, ex.getCause())`       | Complex LHS preserved                                                                                                               |
| `assertTrue("msg", o instanceof Y)`                           | `assertInstanceOf(Y.class, o, "msg")`                           | JUnit 4 message-first form                                                                                                          |
| `assertTrue(o instanceof Y, "msg")`                           | `assertInstanceOf(Y.class, o, "msg")`                           | JUnit 5 message-last form                                                                                                           |
| `Assertions.assertTrue(o instanceof Y)`                       | `Assertions.assertInstanceOf(Y.class, o)`                       | Qualifier preserved, no import added                                                                                                |
| `org.junit.jupiter.api.Assertions.assertTrue(o instanceof Y)` | `org.junit.jupiter.api.Assertions.assertInstanceOf(Y.class, o)` | Fully-qualified qualifier preserved                                                                                                 |
| `assertTrue((o instanceof Y))`                                | `assertInstanceOf(Y.class, o)`                                  | Outer parens stripped before classification                                                                                         |
| `assertTrue((!(o instanceof Y)))`                             | `assertNotInstanceOf(Y.class, o)`                               | Parens around negation                                                                                                              |
| `assertTrue(\n  o instanceof Y\n);` (multi-line call)         | `assertInstanceOf(Y.class, o);`                                 | Collapses multi-line shape. Supports `(` and/or `;` on their own lines                                                              |
| `assertTrue("""text""", o instanceof Y)` (text-block message) | `assertInstanceOf(Y.class, o, """text""")`                      | Text block preserved verbatim. Final layout TBD — see [text-block formatting TODO](prefer-exact-assertion-text-block-formatting.md) |

### Not supported (check fires, fixer returns null)

| Pattern                                                           | Reason                                                                                    |
|-------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `assertTrue(o instanceof Y y)` (pattern binding)                  | Binding semantics can't be preserved through `.class`-literal rewrite                     |
| `assertTrue(o instanceof List<X>)` (generic type)                 | Generics can't appear in a `.class` literal                                               |
| `assertTrue(o instanceof Y, "msg", "extra")` (3+ args)            | Beyond the JUnit 4/5 1- and 2-arg shapes                                                  |
| Unqualified call, no static import of `Assertions`                | Rewrite to unqualified `assertInstanceOf` wouldn't resolve                                |
| Unqualified call, static import of both `Assert` and `Assertions` | Original `assertTrue` may resolve through JUnit 4; rewrite would silently swap frameworks |

### Not flagged by check (correct behavior, not a limitation)

| Pattern                                                            | Reason                                                                         |
|--------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `Assert.assertTrue(o instanceof Y)` (qualifier `Assert`)           | JUnit 4 has no `assertInstanceOf` — suppress instead of emit an unfixable hint |
| Unqualified `assertTrue(o instanceof Y)` under JUnit 4 static only | Same — resolves through JUnit 4                                                |
| Chained-receiver `helper().assertTrue(o instanceof Y)`             | Receiver's runtime type is unknown; suppress conservatively                    |
| `assertTrue(o instanceof Y y && y.length() > 0)` (pattern binding) | Pattern binding skip                                                           |
| `assertTrue(a > 0 && b > 0)` (compound boolean)                    | Top-level is `&&`/`\|\|`, not a comparison or `instanceof`                     |

### Comparison form (not fixable)

`assertTrue(a == b)`, `assertTrue(a > b)`, etc. — the exact expected value depends on domain
knowledge (`assertEquals` needs the expected literal; `assertTrue(a > b)` could mean any of
`assertEquals(b + 1, a)`, `assertEquals(specificValue, a)`, etc.). The check fires under any
framework (JUnit 4 also has `assertEquals`/`assertSame`/etc.) but no auto-fix is provided.

## PreferMathMethodCheck sub-rules

The fixer uses regex for ternary patterns, paren-balanced parsing for clamp patterns, and
multi-line line-text restructuring for if-shape patterns.

### Ternary (max/min/abs)

| Pattern                                 | Replacement        | Auto-fix               |
|-----------------------------------------|--------------------|------------------------|
| `a > b ? a : b` (4 operator variants)   | `Math.max(a, b)`   | Yes                    |
| `a < b ? a : b` (4 operator variants)   | `Math.min(a, b)`   | Yes                    |
| `a < 0 ? -a : a` (8 variants)           | `Math.abs(a)`      | Yes                    |
| `--a > b ? a : b` (prefix mutation)     | `Math.max(--a, b)` | Yes                    |
| `(a) > (b) ? (a) : (b)` (parenthesized) | `Math.max(a, b)`   | No (regex limitation)  |
| Multiline ternary                       | `Math.max(a, b)`   | No (single-line fixer) |

### Clamp (minSdk >= 35)

| Pattern                                 | Replacement                     | Auto-fix             |
|-----------------------------------------|---------------------------------|----------------------|
| `Math.max(lo, Math.min(hi, val))`       | `Math.clamp(val, lo, hi)`       | Yes                  |
| `Math.min(hi, Math.max(lo, val))`       | `Math.clamp(val, lo, hi)`       | Yes                  |
| Reversed arg order (inner call first)   | `Math.clamp(val, lo, hi)`       | Yes                  |
| Nested calls in args (e.g. `foo(a, b)`) | `Math.clamp(foo(a, b), lo, hi)` | Yes (paren-balanced) |

### If-else (max/min/abs)

| Pattern                                                                                                                    | Replacement               | Auto-fix                                               |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------|--------------------------------------------------------|
| `if (a > b) r += a; else r += b;` (compound assign: `+=`, `-=`, `*=`, `/=`, `%=`, `&=`, `\|=`, `^=`, `<<=`, `>>=`, `>>>=`) | `r += Math.max(a, b);`    | Yes                                                    |
| `var r = b; if (a > b) r = a; return r;` (init-overwrite + trailing return)                                                | `return Math.max(a, b);`  | Yes                                                    |
| `var r = b; if (a > b) r = a;` (init-overwrite, no trailing return)                                                        | `var r = Math.max(a, b);` | Yes                                                    |
| `int r; if (a > b) r = a; else r = b; return r;` (decl + assign + return)                                                  | `return Math.max(a, b);`  | Yes                                                    |
| `if (a > b) r = a; else r = b;` (bare assign, no decl/return)                                                              | `r = Math.max(a, b);`     | Yes                                                    |
| `if (a > b) return a; else return b;` (if-else return)                                                                     | `return Math.max(a, b);`  | Yes                                                    |
| `if (a > b) return a; return b;` (trailing return, no else)                                                                | `return Math.max(a, b);`  | Yes                                                    |
| `int r = a, s = b; if (a > b) r = a; ...` (multi-decl above the if)                                                        | n/a                       | No (skipped: pattern rejects multi-decls for safety)   |
| Field/array assignment target (`this.x`, `arr[i]`)                                                                         | same as above             | No (target-shape not yet supported by line-text fixer) |

## PreferSpecificApiCheck sub-rules

Detection paths grouped by minSdk gate. The fixer handles them via line-text pattern matching,
returning null (skipping) for patterns that require structural changes.

### No minSdk gate

| Pattern                                          | Replacement                                | Auto-fix                            |
|--------------------------------------------------|--------------------------------------------|-------------------------------------|
| `assertEquals(true/false/null, x)`               | `assertTrue`/`assertFalse`/`assertNull(x)` | Yes (2-arg and 3-arg forms)         |
| `assertNotEquals(true/false/null, x)`            | Inverted assertion                         | Yes (2-arg and 3-arg forms)         |
| `assertSame(null, x)` / `assertNotSame(null, x)` | `assertNull`/`assertNotNull(x)`            | Yes (2-arg and 3-arg forms)         |
| `.collect(Collectors.toList())`                  | `.toList()`                                | Yes                                 |
| `.collect(Collectors.toUnmodifiableList())`      | `.toList()`                                | Yes                                 |
| `.equals("")`                                    | `.isEmpty()`                               | Yes                                 |
| `.indexOf(str) != -1` / `>= 0` etc.              | `.contains(str)` / `!.contains(str)`       | No                                  |
| `.indexOf("x")` / `.lastIndexOf("x")` (length-1) | `.indexOf('x')` / `.lastIndexOf('x')`      | Yes (escape-safe rewrite)           |
| `.keySet().contains(k)`                          | `.containsKey(k)`                          | Yes                                 |
| `.replaceAll("literal", x)`                      | `.replace("literal", x)`                   | Yes                                 |
| `.size() == 0` / `.length() == 0` etc.           | `.isEmpty()` / `!.isEmpty()`               | Yes (negated needs simple receiver) |
| `.stream().count()`                              | `.size()`                                  | Yes                                 |
| `.stream().findFirst().isPresent()`              | `!receiver.isEmpty()`                      | Simple receivers only               |
| `.values().contains(v)`                          | `.containsValue(v)`                        | Yes                                 |

### API 24+ (MIN_SDK_FOR_EACH)

| Pattern                       | Replacement       | Auto-fix                            |
|-------------------------------|-------------------|-------------------------------------|
| `Collections.sort(list)`      | `list.sort(null)` | Yes (paren-balanced arg extraction) |
| `Collections.sort(list, cmp)` | `list.sort(cmp)`  | Yes (paren-balanced arg extraction) |
| `.stream().forEach(...)`      | `.forEach(...)`   | Yes                                 |

### API 30+ (MIN_SDK_COLLECTION_FACTORY)

| Pattern                          | Replacement    | Auto-fix          |
|----------------------------------|----------------|-------------------|
| `Arrays.asList(...)`             | `List.of(...)` | Yes (adds import) |
| `Collections.emptyList()`        | `List.of()`    | Yes (adds import) |
| `Collections.emptyMap()`         | `Map.of()`     | Yes (adds import) |
| `Collections.emptySet()`         | `Set.of()`     | Yes (adds import) |
| `Collections.singleton(x)`       | `Set.of(x)`    | Yes (adds import) |
| `Collections.singletonList(x)`   | `List.of(x)`   | Yes (adds import) |
| `Collections.singletonMap(k, v)` | `Map.of(k, v)` | Yes (adds import) |

### API 31+ (MIN_SDK_COPY_OF)

| Pattern                                            | Replacement      | Auto-fix                      |
|----------------------------------------------------|------------------|-------------------------------|
| `Collections.unmodifiableList(x)`                  | `List.copyOf(x)` | Yes (adds import)             |
| `Collections.unmodifiableList(Arrays.asList(...))` | `List.of(...)`   | Partial (gives `List.copyOf`) |
| `Collections.unmodifiableMap(x)`                   | `Map.copyOf(x)`  | Yes (adds import)             |
| `Collections.unmodifiableSet(x)`                   | `Set.copyOf(x)`  | Yes (adds import)             |

### API 33+ (MIN_SDK_IS_BLANK / MIN_SDK_TO_ARRAY_GENERATOR)

| Pattern                                | Replacement             | Auto-fix                                                     |
|----------------------------------------|-------------------------|--------------------------------------------------------------|
| `.toArray(new Type[0])`                | `.toArray(Type[]::new)` | Yes (skips multi-dimensional and annotated types)            |
| `.strip().isEmpty()`                   | `.isBlank()`            | Yes                                                          |
| `.strip().length() == 0`               | `.isBlank()`            | Yes (including reversed `0 == ...` form)                     |
| `.strip().length() <= 0`               | `.isBlank()`            | Yes                                                          |
| `.strip().length() != 0` / `> 0`       | `!receiver.isBlank()`   | Simple receivers only (identifiers, dotted names)            |
| Reversed strip forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (positive reversed); simple receivers (negated reversed) |
| `.trim().isEmpty()`                    | `.isBlank()`            | Yes                                                          |
| `.trim().length() == 0`                | `.isBlank()`            | Yes (including reversed `0 == ...` form)                     |
| `.trim().length() <= 0`                | `.isBlank()`            | Yes                                                          |
| `.trim().length() != 0` / `> 0`        | `!receiver.isBlank()`   | Simple receivers only (identifiers, dotted names)            |
| Reversed trim forms (`0 != ...` etc.)  | `.isBlank()` or negated | Yes (positive reversed); simple receivers (negated reversed) |

### API 34+ (MIN_SDK_FORMATTED)

| Pattern                          | Replacement                 | Auto-fix                            |
|----------------------------------|-----------------------------|-------------------------------------|
| `String.format("literal", args)` | `"literal".formatted(args)` | Yes (paren-balanced arg extraction) |

### API 35+ (MIN_SDK_GET_FIRST_LAST)

| Pattern               | Replacement      | Auto-fix                            |
|-----------------------|------------------|-------------------------------------|
| `.get(0)`             | `.getFirst()`    | Yes                                 |
| `.get(size() - 1)`    | `.getLast()`     | No (requires receiver match verify) |
| `.remove(0)`          | `.removeFirst()` | Yes                                 |
| `.remove(size() - 1)` | `.removeLast()`  | No (requires receiver match verify) |

## RecordFormattingCheck sub-rules

The fixer handles brace formatting and component layout for record declarations. Brace
formatting: collapses non-canonical spacing between the anchor token (closing paren or end of
`implements` clause) and the opening brace, joining empty-body braces onto the anchor line, and
splitting non-empty single-line bodies onto multiple lines. Component layout: rebuilds the
record header to single-line form when components fit in 120 columns, or to multi-line form
(each component on its own line) when they don't.

### Supported (auto-fixable)

| Pattern                                          | Replacement                                | Notes                                                                                     |
|--------------------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------------------|
| `record R(...){}` (zero space)                   | `record R(...) {}`                         | BAD_SPACING                                                                               |
| `record R(...)  {}` (multiple spaces)            | `record R(...) {}`                         | BAD_SPACING                                                                               |
| `record R(...)\t{}` (tab)                        | `record R(...) {}`                         | BAD_SPACING                                                                               |
| `record R(...)\n{}` (newline)                    | `record R(...) {}`                         | OPEN_BRACE_NOT_ON_ANCHOR_LINE                                                             |
| `record R(...) {\n}` (empty split)               | `record R(...) {}`                         | EMPTY_BODY_BRACES_SPLIT                                                                   |
| `record R(...) { body; }`                        | `record R(...) {\n\tbody;\n}`              | NON_EMPTY_BODY_BRACES_SAME_LINE; finds matching `{` via literal/comment-aware brace count |
| `record R(...) implements Foo{}`                 | `record R(...) implements Foo {}`          | BAD_SPACING with implements anchor                                                        |
| `record R(...) implements Foo\n{}`               | `record R(...) implements Foo {}`          | OPEN_BRACE_NOT_ON_ANCHOR_LINE with implements anchor                                      |
| `record R(...) implements\n\tFoo\n{}`            | `record R(...) implements\n\tFoo {}`       | OPEN_BRACE_NOT_ON_ANCHOR_LINE with multi-line implements                                  |
| `record R(a,\n\tb) {}` (mixed, fits)             | `record R(a, b) {}`                        | Component-layout: collapses to style A if combined line ≤120 chars                        |
| `record R(<too long for one line>) {}`           | `record R(\n\t\ta,\n\t\tb,\n\t\t...\n) {}` | Component-layout: expands to style B if combined would exceed 120 chars                   |
| `record R(\n\ta, b\n) {}` (multi-per-line, fits) | `record R(a, b) {}`                        | Component-layout: collapses MULTI_PER_LINE to single line if it fits                      |

### Not supported (check fires, fixer returns null)

| Pattern                                                                                       | Reason                                                                                           |
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `{` on a line whose anchor ends in a `//` comment, unterminated `/*`, or unterminated literal | Joining the line would swallow the `{` into the comment or literal; bail to avoid invalid source |
| `}` on its own line preceded by lines containing non-whitespace, non-`{` content              | Cannot safely collapse to empty-body form without losing content                                 |
| Multi-line record header where any line contains a `//` line comment                          | Collapsing would either swallow code into the comment or silently drop the comment               |
| Unterminated string, char, text block, or block comment anywhere in the header                | Cross-line literal/comment state cannot be safely tracked; bail                                  |
| Record header already in canonical single-line or multi-line form (no-op)                     | The fixer detects when its output would equal the input and returns null instead of looping      |
| Record header with a trailing comma (`record R(a, b,)`)                                       | Malformed input; bail rather than silently "fix" the comma away                                  |
| Record header with a leading or empty middle component (`record R(, a)`, `record R(a, , b)`)  | Malformed input; bail rather than emit corrupt output                                            |
| Record header with unbalanced angle brackets (`record R(List<String x, int y)`)               | Malformed input; cannot tell where the generic ends, so bail instead of mis-splitting components |

## RedundantArrayCreationCheck sub-rules

The fixer removes `new Type[]{...}` and replaces it with the elements directly. For empty arrays,
it also removes the preceding comma if one exists.

### Supported (auto-fixable)

| Pattern                                                | Fix                 | Notes                             |
|--------------------------------------------------------|---------------------|-----------------------------------|
| `method(new Type[]{"a", "b"})`                         | `method("a", "b")`  | Only argument                     |
| `method(arg, new Type[]{"a"})`                         | `method(arg, "a")`  | Last argument with preceding args |
| `method(new Type[]{})`                                 | `method()`          | Empty array, only argument        |
| `method(arg, new Type[]{})`                            | `method(arg)`       | Empty array, removes comma        |
| `new Ctor(new Type[]{"a"})`                            | `new Ctor("a")`     | Constructor varargs               |
| Elements with nested parens (`foo(1, 2)` inside array) | Correctly extracted | Paren-balanced parsing            |
| String literals with braces (`"a{b}"` inside array)    | Correctly extracted | String/char literal awareness     |

### Not supported (check fires, fixer returns null)

| Pattern                                           | Reason                                                      |
|---------------------------------------------------|-------------------------------------------------------------|
| Multiline array creation (`new Type[]{\n...\n}`)  | Closing `}` not on the same line as opening `{`             |
| Opening `{` on a different line than `new`        | `line.indexOf('{', column)` returns -1                      |
| Statically imported varargs (`asList(new T[]{})`) | Check does not fire (no receiver to resolve via reflection) |

### Not flagged by check (correct behavior, not a limitation)

| Pattern                                              | Reason                                                       |
|------------------------------------------------------|--------------------------------------------------------------|
| `List.of(new Object[]{"a"})` (non-varargs overload)  | `List.of(E)` is non-varargs with `Object` last param, blocks |
| `List.of(new int[]{1})` (primitive to reference)     | Removing wrapper would change autoboxing behavior            |
| `(Object) new String[]{"a"}` (cast wrapping)         | Last arg is TYPECAST, not LITERAL_NEW                        |
| `(CharSequence[]) new String[]{"a"}` (cast wrapping) | Last arg is TYPECAST, not LITERAL_NEW                        |
| `method(existingArrayVar)` (variable, not `new`)     | Not an explicit array creation                               |
| `method(new Type[5])` (explicit size, no init)       | No ARRAY_INIT child on LITERAL_NEW                           |

## RedundantEqualityBranchCheck sub-rules

The fixer detects four shapes via line-text regex parsing of the if-line and adjacent
lines. For `==` the surviving value is the else-branch's; for `!=` it's the then-branch's.

| Pattern                                                          | Replacement     | Auto-fix                |
|------------------------------------------------------------------|-----------------|-------------------------|
| `final int r; if (a == b) r = a; else r = b; return r;`          | `return b;`     | Yes                     |
| `if (a == b) r = a; else r = b;` (no decl/return: bare collapse) | `r = b;`        | Yes                     |
| `if (a == b) return a; else return b;`                           | `return b;`     | Yes                     |
| `if (a == b) return a; return b;` (trailing return)              | `return b;`     | Yes                     |
| `if (a != b) ...` (`!=` instead of `==`)                         | uses then-value | Yes                     |
| Branches use a third operand (e.g. `r = c;`)                     | n/a             | No (check doesn't fire) |
| Operands or branch values impure (method calls, increments)      | n/a             | No (check doesn't fire) |

## LambdaParameterTypeCheck sub-rules

The fixer handles all three violation types in a single pass. For single non-annotated params, the
fixer goes straight to naked form (removing both type and parens).

| Violation             | Input                           | Fix output                |
|-----------------------|---------------------------------|---------------------------|
| Unnecessary parens    | `(x) ->`                        | `x ->`                    |
| Use implicit (single) | `(String x) ->` / `(var x) ->`  | `x ->`                    |
| Use implicit (multi)  | `(String x, int y) ->`          | `(x, y) ->`               |
| Use var (single)      | `(@A String x) ->`              | `(@A var x) ->`           |
| Use var (multi mixed) | `(@A String x, String y) ->`    | `(@A var x, var y) ->`    |
| Use var (multi both)  | `(@A String x, @B String y) ->` | `(@A var x, @B var y) ->` |

## ConstructorAssignmentOrderCheck sub-rules

The fixer parses constructor and instance initializer bodies to find `this.xxx = ...` assignment
statements. It groups them (simple single-line, multi-line, variable-dependent) and sorts within
each group. Local variable declarations are placed before the assignments that reference them.

| Violation type               | Input pattern                                           | Auto-fix | Notes                                                   |
|------------------------------|---------------------------------------------------------|----------|---------------------------------------------------------|
| Alphabetical order           | `this.beta = b; this.alpha = a;`                        | Yes      | Swaps/sorts by field name                               |
| Simple before multi-line     | Multi-line before `this.alpha = a;`                     | Yes      | Moves simple group before multi group                   |
| Non-var before var-dependent | Var-dependent before `this.beta = x;`                   | Yes      | Moves non-var groups before var group                   |
| Var sub-group order          | `this.beta = second; this.alpha = first;`               | Yes      | Sorts by variable declaration order                     |
| Field-to-field dependency    | `this.beta = this.alpha + 1;` before `this.alpha = a;`  | Yes      | Respects dependency: A before B if B uses A             |
| Multi-line alphabetical      | Two multi-line anonymous class assignments out of order | Yes      | Tracks brace/paren depth for boundaries                 |
| Circular dependencies        | `this.a = this.b + 1; this.b = this.a + 1;`             | No       | Max-iteration guard stops the loop; best-effort order   |
| Non-assignment statements    | `System.out.println()` between assignments              | No       | Returns null if non-assignment lines exist in the range |
| Multi-line local var decl    | `final var x =\n\tnew Foo();`                           | No       | Only single-line local var decls parsed                 |
| Nested generics in local var | `Map<String, List<Integer>> m = ...`                    | No       | `[^>]*` in regex stops at first `>`; var not tracked    |
| Text blocks in assignments   | `this.x = """\n...\n""";`                               | No       | String parser doesn't handle `"""`; may misparse body   |

## ControlFlowBracesCheck sub-rules (non-do-while)

The fixer adds braces to braceless multi-line bodies, removes unnecessary braces from single-line
bodies, and handles brace-on-own-line formatting. Do-while violations are handled separately (see
main table).

| Violation type                      | Input pattern                             | Auto-fix | Notes                                                     |
|-------------------------------------|-------------------------------------------|----------|-----------------------------------------------------------|
| Missing braces (if)                 | `if (cond)\n\tfor (...)\n\t\tstmt;`       | Yes      | Wraps in `{ }`, preserves body indentation                |
| Missing braces (else)               | `else\n\tfor (...)\n\t\tstmt;`            | Yes      | Same wrapping logic                                       |
| Missing braces (for)                | `for (...)\n\tif (...)\n\t\tstmt;`        | Yes      | Same wrapping logic                                       |
| Missing braces (for-each)           | `for (var x : list)\n\tif...\n\t...`      | Yes      | Same wrapping logic                                       |
| Missing braces (while)              | `while (cond)\n\tif (...)\n\t...`         | Yes      | Same wrapping logic                                       |
| Missing braces + trailing comment   | `if (cond) // note\n\tfor...\n\t\tstmt;`  | Yes      | Inserts `{` before the `//` comment                       |
| Unnecessary braces (if)             | `if (cond) { singleStmt; }`               | Yes      | Removes `{` and `}`, preserves `else` on own line         |
| Unnecessary braces (else)           | `else { singleStmt; }`                    | Yes      | Same removal logic                                        |
| Unnecessary braces (while)          | `while (cond) { singleStmt; }`            | Yes      | Same removal logic                                        |
| Unnecessary braces (for)            | `for (...) { singleStmt; }`               | No       | PreferBulkOperation may also fire; returns SkipResult     |
| Brace on own line (if)              | `if (cond)\n{\n\tstmt;\n}`                | Yes      | Removes `{` and `}` lines                                 |
| Brace on own line (else)            | `else\n{\n\tstmt;\n}`                     | Yes      | Same removal logic                                        |
| Brace on own line (while)           | `while (cond)\n{\n\tstmt;\n}`             | Yes      | Same removal logic                                        |
| Brace on own line (for)             | `for (...)\n{\n\tstmt;\n}`                | No       | PreferBulkOperation may also fire; returns SkipResult     |
| Brace on own line + comment on `{`  | `if (cond)\n{ // note\n\tstmt;\n}`        | No       | Returns null to avoid losing the comment                  |
| Variable declaration body           | `if (cond) { int x = 5; }`                | No       | Returns null; braces required for variable scope          |
| Annotated variable declaration body | `if (cond) { @Nullable String s = ...; }` | No       | Returns null; annotation-aware variable detection         |
| One-liner                           | `if (cond) stmt;`                         | No       | Returns SkipResult; body on same line as keyword          |
| No semicolon found                  | Body without reachable `;`                | No       | Returns null from `findStatementEnd`                      |
| Text blocks in body                 | Body containing `"""`                     | No       | String parser doesn't handle text blocks; may return null |
| Qualified annotation in body        | `@java.lang.Deprecated int x`             | No       | Annotation parser doesn't handle dot-separated names      |

## FieldSortingCheck sub-rules (field ordering)

The fixer parses field declarations in a class body and sorts them by the check's rules.
Enum constant sorting is handled separately (see main table).

| Violation type              | Input pattern                                                  | Auto-fix | Notes                                                                                                                                                               |
|-----------------------------|----------------------------------------------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Chunk order                 | Non-final before final-with-value                              | Yes      | Adds blank lines between chunks                                                                                                                                     |
| Type order (prim vs ref)    | `String name` before `int count`                               | Yes      | Primitives sort before reference types                                                                                                                              |
| Type order (alphabetical)   | `String` before `int` (same chunk)                             | Yes      | Alphabetical by base type name                                                                                                                                      |
| Array depth                 | `int[]` before `int`                                           | Yes      | Base type first, then arrays                                                                                                                                        |
| Annotation order            | `@Nullable String` before `@NonNull String`                    | Yes      | Unannotated first, then by canonical annotation key                                                                                                                 |
| Type-arg annotation order   | `List<@B String>` before `List<@A String>`                     | Yes      | Position-aware: compares type arguments left to right, unannotated before annotated, then alphabetical. Extracts annotations from `<...>` in the captured type name |
| Annotation consolidation    | Same-type same-annotation fields on separate lines             | Yes      | Merges into single declaration after sorting                                                                                                                        |
| Name order                  | `int z` before `int a` (same type)                             | Yes      | Case-insensitive alphabetical                                                                                                                                       |
| Field dependencies          | `B = A + 1` before `A = 0`                                     | Yes      | Respects dependency: A before B if B uses A                                                                                                                         |
| Multi-line initializers     | Fields with anonymous class or lambda init                     | Yes      | Tracks brace/paren depth for field end                                                                                                                              |
| Annotated fields            | Fields with `@Deprecated` etc. above                           | Yes      | Annotation lines move with their field                                                                                                                              |
| Circular dependencies       | `A = B + 1; B = A + 1`                                         | No       | Max-iteration guard stops the loop; best-effort order                                                                                                               |
| Unparseable field pattern   | Complex generics, multi-variable declarations                  | No       | Returns null if FIELD_PATTERN doesn't match                                                                                                                         |
| Anonymous class initializer | anon.class field must come before non-anon                     | No       | Not implemented in fixer sorting                                                                                                                                    |
| Text blocks in initializers | Field with `"""` initializer containing `{}`                   | No       | String parser doesn't handle text blocks                                                                                                                            |
| Nested generics in type     | `Map<String, List<Integer>>` field                             | No       | `[^>]*` in FIELD_PATTERN stops at first `>`                                                                                                                         |
| Wildcard bound annotations  | `List<? extends @B Number>` before `List<? extends @A Number>` | Yes      | Collects annotations from TYPE_UPPER_BOUNDS/TYPE_LOWER_BOUNDS; fixer scans full type arg text                                                                       |
| Inline annotation with `()` | `@SuppressWarnings(")")` in field value                        | No       | Annotation parser doesn't track string literals in args                                                                                                             |

## Regex checks without fixers

| Module ID     | Reason                                                                                            |
|---------------|---------------------------------------------------------------------------------------------------|
| NoSpaceIndent | Converting leading spaces to tabs requires knowing the original indent width (2? 4? 8?), which is |
|               | ambiguous. A wrong guess changes visual indentation. See `docs/regex-fixer-edge-cases.md`.        |

## Checks without fixers

Custom checks without auto-fix support and why.

| Check                                       | Reason                                                                                                         |
|---------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| ClassStructureOrderCheck                    | Reordering class members requires moving multi-line blocks with dependency analysis                            |
| ControlFlowBracesCheck (one-liner)          | Body on same line as keyword; moving to own line requires re-indentation context                               |
| EmptyBodyCheck                              | Ambiguous: removing the statement may discard intentional no-ops; adding a body requires context               |
| EmptySwitchCheck                            | Same as EmptyBodyCheck                                                                                         |
| FieldSortingCheck (dependency, anon.class)  | Dependency and anonymous class ordering violations may return null if the fixer can't parse the pattern        |
| InfiniteEmptyLoopCheck                      | Flags bugs (infinite empty loops), not a stylistic issue with a deterministic fix                              |
| InstanceofBeforeCastCheck                   | Reordering sub-expressions in compound boolean conditions while preserving short-circuit semantics             |
| MethodAlphabeticalOrderCheck                | Reordering methods requires moving multi-line blocks                                                           |
| MultilineCallFormattingCheck                | Reformatting argument layout across lines with context-dependent indent and grouping rules                     |
| NoCaseBracesCheck                           | Removing braces requires scope analysis to verify no variable declarations leak                                |
| OverloadMethodOrderCheck                    | Reordering method overloads requires moving multi-line blocks                                                  |
| PreferExactAssertionCheck (comparison form) | No deterministic fix for `assertTrue(a > b)`-style: the exact expected value depends on domain knowledge       |
| PreferImportCheck                           | Replacing FQN with short name and adding import; must verify no name conflicts                                 |
| PreferLambdaCheck                           | Structural transformation: anonymous class to lambda, must handle `this` references and field shadowing        |
| PreferLiteralSuffixCheck                    | Replacing widening cast with literal suffix requires expression context analysis                               |
| PreferPatternMatchingInstanceofCheck        | Restructuring instanceof + subsequent cast into pattern matching across multiple statements                    |
| PreferRecordCheck                           | Multi-line structural transformation: must rewrite class header, remove fields/constructor, adjust annotations |
| RedundantCastCheck                          | Removing a cast may change method overload resolution or widen the expression type                             |
| SwitchCaseOrderCheck                        | Reordering switch cases requires moving multi-line blocks with fall-through analysis                           |
| ThreadAnnotationCheck                       | Cannot determine which thread annotation (`@MainThread`, `@AnyThread`, etc.) to add                            |

## FieldConsolidationFixer limitations

Line-length wrapping measures tab-expanded length at tab-width 4, with a 120-character threshold.
Continuation lines use base indent + 2 tabs.

| Scenario                                                   | Behavior                       | Reason                                                                                                                    |
|------------------------------------------------------------|--------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| C-style array fields that were wrapped by a prior fix pass | Second merge skipped           | The wrapped first line has no `;`, so C-style bracket detection fails and the `prevCStyle && !currCStyle` guard bails out |
| Line length at a different tab width                       | May wrap too early or too late | Wrapping uses a fixed tab-width of 4; projects displaying tabs as 8 will see wider lines than the fixer expects           |

## Other fixer limitations

Known cases where fixers return null or SkipResult. These are not bugs; each represents a
pattern the fixer intentionally skips because it cannot safely transform the code.

| Fixer                          | Skipped case                                                                    | Reason                                                                                 |
|--------------------------------|---------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| RedundantAnnotationSyntaxFixer | Multiline annotation with `()` or `value =`                                     | Skip: regex can't reliably detect annotation boundary across lines                     |
| PreferVarFixer                 | Multi-variable declaration (`int a, b;`)                                        | Skip: can't replace type with `var` when multiple variables share the declaration      |
| PreferVarFixer                 | No `new` after `=` in explicit array init                                       | null: pattern requires `= new Type[]{}` structure                                      |
| PreferVarFixer                 | Non-Object generic type args (`<String>`)                                       | null: diamond `<>` only replaces `<Object>`, not other explicit types                  |
| PreferCollectionInterfaceFixer | Class not resolvable or not a standard collection                               | Skip: concrete-to-interface mapping requires class resolution at runtime               |
| LambdaParameterTypeFixer       | Arrow `->` not found from violation column                                      | Skip: fixer operates on text from the column; if arrow is on a different line, skipped |
| LambdaParameterTypeFixer       | Opening paren not found for lambda params                                       | Skip: single naked param without parens in unusual positions                           |
| FieldConsolidationFixer        | Block comment between field names                                               | null: comment would be lost or misplaced during merge                                  |
| FieldConsolidationFixer        | C-style array type mismatch between fields                                      | null: `int[] a` and `int b` can't merge to one declaration safely                      |
| AnnotationOwnLineFixer         | Annotation already on own line, just needs sorting                              | null when already in correct order                                                     |
| AnnotationSameLineFixer        | Annotation block reaches end of file                                            | null: no declaration found to join annotations onto                                    |
| RedundantArrayCreationFixer    | Multiline array creation                                                        | null: closing `}` not on the same line as opening `{`                                  |
| RedundantArrayCreationFixer    | No `{` found on the violation line                                              | null: opening brace on a different line than `new`                                     |
| ArrayTypeStyleFixer            | See [ArrayTypeStyleCheck sub-rules](#arraytypestylecheck-sub-rules)             | Skipped patterns are listed there alongside supported patterns                         |
| PreferExactAssertionFixer      | See [PreferExactAssertionCheck sub-rules](#preferexactassertioncheck-sub-rules) | Skipped patterns are listed there alongside supported patterns                         |
| PreferDoWhileFixer             | Comment on pre-statement or body line                                           | Skip: comment preservation in the collapsed do-while is non-trivial                    |
| PreferDoWhileFixer             | Pre-statement / body indent mismatch                                            | Skip: defensive — happy path requires same indent                                      |
| PreferDoWhileFixer             | Braced body has multi-statement or unusual closing                              | Skip: only single-statement braced bodies are collapsed                                |
| PreferDoWhileFixer             | While line not in expected single-line format                                   | Skip: fixer regex requires `while (cond)` (or `{`) on one line                         |
| PreferDoWhileFixer             | Pre-statement and body not textually equal after stripping                      | Skip: defensive — check fired but text differs (e.g. whitespace artifacts)             |

## Future fix opportunities

Patterns not currently auto-fixable, with what would be needed to support them.

| Pattern                                                   | Blocker                                                                                                       | Possible approach                                                                       |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `.indexOf(...) != -1`                                     | Need to restructure `expr op literal` into `receiver.contains(arg)` or `!receiver.contains(arg)`              | Extract receiver + arg from the indexOf call text, rebuild as contains, handle negation |
| `.get(size() - 1)` / `.remove(size() - 1)`                | Must verify the `size()` receiver matches the `.get()`/`.remove()` receiver                                   | Parse both receivers and compare, or pass violation metadata to the fixer               |
| `.stream().findFirst().isPresent()` (complex receivers)   | Simple receivers (identifiers, dotted names) are already fixed; method calls, casts, and array access are not | Extend `findReceiverStart()` to handle parenthesized expressions and method calls       |
| `.size() != 0` / `.length() > 0` etc. (complex receivers) | Positive forms (`== 0`) are fully fixed; negated forms need `!` insertion which requires simple receiver scan | Extend `findReceiverStart()` to handle complex receivers                                |