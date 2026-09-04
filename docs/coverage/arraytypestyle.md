# ArrayTypeStyleCheck auto-fix coverage

The fixer moves C-style brackets from after the variable name to the type position, preserving the
original comments and surrounding whitespace.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `int x[];` | `int[] x;` | Field, local, parameter, record component |
| `int x[][];` | `int[][] x;` | Compound C-style |
| `int[] x[];` | `int[][] x;` | Mixed Java + C-style, when no type-use annotation sits on the base type's brackets |
| `List<@A String>[] x[];` / `Box.@A Item x[];` / `record R(@A int x[]) {}` | `List<@A String>[][] x;` / `Box.@A Item[] x;` / `record R(@A int[] x) {}` | A type-use annotation inside the type arguments or on a qualified name, or a declaration annotation, is not on a dimension and stays where it is |
| `int x[] = {1};` | `int[] x = {1};` | With initializer |
| `int x [];` | `int[] x;` | Whitespace between name and `[` |
| `int x[ ];` | `int[ ] x;` | Whitespace inside brackets preserved |
| `final int x[];` | `final int[] x;` | With modifiers |
| `public static final int x[];` | `public static final int[] x;` | Multiple modifiers |
| `@Deprecated int x[];` | `@Deprecated int[] x;` | Declaration annotations preserved |
| `int @TypeAnno [] x;` | (clean, no violation) | Type-use annotation in Java-style position |
| `List<String> x[];` | `List<String>[] x;` | Generic type |
| `List<? extends Number> x[];` | `List<? extends Number>[] x;` | Wildcard generic |
| `Map<K, V> x[];` | `Map<K, V>[] x;` | Multiple type args |
| `Map<String, List<Integer>> x[];` | `Map<String, List<Integer>>[] x;` | Nested generics |
| `void m(int x[])` | `void m(int[] x)` | Method parameter |
| `void m(int a[], int b)` | `void m(int[] a, int b)` | Multi-param, C-style on first |
| `void m(int x, int y[])` | `void m(int x, int[] y)` | Multi-param, C-style on last |
| `void m(int a, int b[], int c)` | `void m(int a, int[] b, int c)` | Multi-param, C-style in middle |
| `(int a[], int b) -> {}` (lambda) | `(int[] a, int b) -> {}` | Multi-param lambda |
| `record R(int x[]) {}` | `record R(int[] x) {}` | Record component |
| `int m()[] { return null; }` | `int[] m() { return null; }` | Method return type |
| `int m()[][]` | `int[][] m()` | Compound method return |
| `int[] m()[]` | `int[][] m()` | Java + C-style method return |
| `int m()[] throws X { ... }` | `int[] m() throws X { ... }` | Method return + throws |
| `int m()[];` | `int[] m();` | Abstract method |
| `<T> T m()[]` | `<T> T[] m()` | Generic method |
| `List<String> m()[]` | `List<String>[] m()` | Generic return type |
| `int x\n[];` (multi-line) | `int[] x\n;` | Bracket on next line |
| `int m()\n[]\n{...}` (multi-line return) | `int[] m() {...}` | Method return, bracket-only line removed; the body's `{`, which the brackets kept off the signature line, joins it |
| `int m()\n[] {...}` (body on the bracket line) | `int[] m() {...}` | The body's `{` joins the signature line too |
| `int m() // c\n[]\n{...}` (comment on the signature line) | `int[] m() {...} // c` | The `{` goes ahead of the signature line's comment |
| `int m()\n[]\nthrows E {...}` | `int[] m()\nthrows E {...}` | Only a `{` joins the signature line; a `throws` clause stays where it is |
| `int x[] /* note */ = a;` | `int[] x /* note */ = a;` | Comments after `]` preserved |
| `int x[] = a; // hello, world` | `int[] x = a; // hello, world` | Comma in trailing comment ignored |
| `/* doc */ final int x[];` | `/* doc */ final int[] x;` | Leading block comment preserved |
| `int a[], b[];` | `int[] a, b;` | Multi-variable declaration whose declarators all carry the same C-style depth and whose base type has none: field, local, for-init (`for (int a[] = x, b[] = y; ...)`), constant, in any class, interface, enum, record or anonymous class body. Fixed from any declarator's event in one rewrite |
| `int a[][], b[][];` | `int[][] a, b;` | Same depth above one |
| `int a[] = {1, 2}, b[] = f(3, 4);` | `int[] a = {1, 2}, b = f(3, 4);` | Initializers are kept as written, commas, strings, chars, casts, array access and text blocks included |
| `int a[ ], b[];` | `int[ ] a, b;` | The first declarator's brackets are moved as written; every declarator's brackets, and any whitespace between its name and them, are removed from after the name |
| `int a[] /* c */, /* d */ b[];` / `int /* c */ a[], b[];` | `int[] a /* c */, /* d */ b;` / `int[] /* c */ a, b;` | Comments outside a name's brackets are kept |
| `List<int[]> a[], b[];` / `Box.Item<String> a[], b[];` | `List<int[]>[] a, b;` / `Box.Item<String>[] a, b;` | Brackets inside type arguments do not count toward the base type |
| `int a[],\n\tb[];` / `final int\n\ta[], b[];` | `int[] a,\n\tb;` / `final int[]\n\ta, b;` | Declarators or the type on separate lines, as long as each name shares a line with its brackets |
| Same-depth multi-variable declaration with a name spelled as a unicode escape | The brackets move onto the type and the escape is kept as written | The single-declarator form is refused, below |

## Not supported

| Pattern | Reason |
| --- | --- |
| Multi-variable declaration whose declarators carry different C-style depths: single-line (`int a[], b;` / `int a, b[];` / `int a[], b[][];` / `int a[], b, c[];`), with initializer (`int x[] = {1}, y = 0;`), across lines (`int x\n[], y;` / `int x\n[]\n, y;`), spanning a paren initializer (`int x[] = foo(a,\nb), y;`), in a for-loop init (`for (int x[] = a, y = 1; ...)`), or in a lambda body passed on the same line as the call's open paren (`run(() -> { int x[], y; });`) | Moving the bracket would retype a sibling variable |
| Multi-variable declaration whose base type already has brackets (`int[] a[], b[];` / `int @A [] a[], b[];`) | `cannot move the brackets: multi-variable declaration`. Kept as a refusal rather than proven safe |
| Same-depth multi-variable declaration with anything but whitespace between a name and its last `]`: a comment (`int a /* c */ [], b[];`, `int a[/* c */], b[];`) or a type-use annotation (`int a[], b @A [];`, `int a[][], b[] @A [];`) | The move would have to place the comment or annotation. Refused as `multi-variable declaration`, except at the event of a declarator whose own brackets follow the annotation, which gets the type-use annotation reason |
| Same-depth multi-variable declaration with a name and its brackets on different lines (`int a\n[], b[];` / `int a[], b\n[];` / `int a[]\n[], b[][];`) | `cannot move the brackets: multi-variable declaration` |
| Any declaration in a buffer an earlier fix in the same pass left unparseable | `cannot move the brackets: the file could not be parsed to confirm the declaration`. Without a tree the text alone cannot tell a parameter list from the declarators of one statement |
| C-style component of a generic record (`record R<T>(int x[], String s) {}`, `record R<T extends List<String>>(int x[], int y) {}`) | Not reported: Checkstyle cannot parse C-style brackets in a generic record's components, so the check never sees the file |
| Type-use annotation on a base-type dimension of a C-style declaration (`int @A [] x[];`, `int[] @A [] x[];`, `int @A [] m()[]`, `record R(int @A [] x[])`, single or multi-line, any declaration kind) | `cannot move the brackets: a type-use annotation precedes them`. The name's brackets are the outer dimension (JLS 10.2), so moving them after the annotated pair would put the annotation on the other dimension |
| Single declarator whose name is spelled as a unicode escape | `cannot move the brackets: the name is spelled with a unicode escape`. Checkstyle reads the escape without its backslash, so the brackets would land between the backslash and the rest of the name |
| C-style with type-use annotation on bracket (`int x @Anno []`) | Preserving the type-use annotation through the move is ambiguous |
| Comment between the name and `[` (`int x /* note */ [];`) | Dropping or preserving the gap comment is ambiguous |
| Method-return as expression-context bracket (`x = bar()[]`) | Not a type declaration |
| Method-return type-use annotation (`int m() @A []`) | Preserving the type-use annotation through the move is unsafe |
| Declaration line containing a `"""` text-block delimiter in code | `cannot move the brackets: a possible text block prevents scanning the declaration`. A `"""` inside a comment or string literal does not block the fix |
| Multi-line declaration first line (no prev line) | No preceding declaration line to attach the moved bracket to |
| Multi-line with empty/whitespace-only prev line | No identifier on the previous line to attach the moved bracket to |
| Multi-line with method-call argument list on prev line | Can't locate the type on the previous line to attach the moved bracket to |

Part of [auto-fix coverage](../auto-fix-coverage.md).