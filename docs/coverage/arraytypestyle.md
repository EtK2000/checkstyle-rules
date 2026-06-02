# ArrayTypeStyleCheck auto-fix coverage

The fixer moves C-style brackets from after the variable name to the type position, preserving the
original comments and surrounding whitespace.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `int x[];` | `int[] x;` | Field, local, parameter, record component |
| `int x[][];` | `int[][] x;` | Compound C-style |
| `int[] x[];` | `int[][] x;` | Mixed Java + C-style |
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
| `record R<T>(int x[], String s) {}` | `record R<T>(int[] x, String s) {}` | Generic record component |
| `record R<T extends List<String>>(int x[], int y) {}` | (corresponding fix) | Nested type-param bounds |
| `int m()[] { return null; }` | `int[] m() { return null; }` | Method return type |
| `int m()[][]` | `int[][] m()` | Compound method return |
| `int[] m()[]` | `int[][] m()` | Java + C-style method return |
| `int m()[] throws X { ... }` | `int[] m() throws X { ... }` | Method return + throws |
| `int m()[];` | `int[] m();` | Abstract method |
| `<T> T m()[]` | `<T> T[] m()` | Generic method |
| `List<String> m()[]` | `List<String>[] m()` | Generic return type |
| `int x\n[];` (multi-line) | `int[] x\n;` | Bracket on next line |
| `int m()\n[]\n{...}` (multi-line return) | `int[] m()\n{...}` | Method return, bracket-only line removed |
| `int x[] /* note */ = a;` | `int[] x /* note */ = a;` | Comments after `]` preserved |
| `int x[] = a; // hello, world` | `int[] x = a; // hello, world` | Comma in trailing comment ignored |
| `/* doc */ final int x[];` | `/* doc */ final int[] x;` | Leading block comment preserved |

## Not supported

| Pattern | Reason |
| --- | --- |
| Multi-variable single-line (`int a[], b;` / `int a, b[];`) | Moving the bracket would retype the sibling variable |
| Multi-var with initializer (`int x[] = {1}, y = 0;`) | Multiple variables share the declaration; moving the bracket would retype a sibling |
| Multi-var across lines (`int x\n[], y;` / `int x\n[]\n, y;`) | Multiple variables share the declaration across lines; moving the bracket would retype a sibling |
| Multi-var spanning paren initializer (`int x[] = foo(a,\nb), y;`) | Multiple variables share the declaration; moving the bracket would retype a sibling |
| C-style with type-use annotation on bracket (`int x @Anno []`) | Preserving the type-use annotation through the move is ambiguous |
| Comment between the name and `[` (`int x /* note */ [];`) | Dropping or preserving the gap comment is ambiguous |
| For-loop multi-var init (`for (int x[] = a, y = 1; ...)`) | Multiple variables share the for-loop init; moving the bracket would retype a sibling |
| Method-return as expression-context bracket (`x = bar()[]`) | Not a type declaration (the brackets are an array-access expression, not a C-style type) |
| Method-return type-use annotation (`int m() @A []`) | Preserving the type-use annotation through the move is unsafe |
| Declaration line containing a `"""` text-block delimiter in code | `possible text block prevents scanning the declaration`: a multi-line text block cannot be reliably scanned for the declaration's extent, so the fixer refuses conservatively. A `"""` inside a comment or string literal does not block the fix |
| Multi-line declaration first line (no prev line) | No preceding declaration line to attach the moved bracket to |
| Multi-line with empty/whitespace-only prev line | No identifier on the previous line to attach the moved bracket to |
| Multi-line with method-call argument list on prev line | Can't locate the type on the previous line to attach the moved bracket to |

Part of [auto-fix coverage](../auto-fix-coverage.md).