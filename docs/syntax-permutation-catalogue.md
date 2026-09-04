# Syntax permutation catalogue

Java syntactic variants that share an AST skeleton but differ in ways a check or fixer must
handle. These are the categories test coverage most consistently misses.

Every subsection opens with an `Apply if` line. Read the ones whose trigger matches the constructs
the check actually touches, which you can work out from `getRequiredTokens` / `getDefaultTokens`
plus any traversal the check does. `test-coverage-auditor` and `test-matrix-author` both read this
doc, and each states in its own prompt how to report which subsections applied and which did not.

## Imports
Apply if the check inspects `IMPORT`, `STATIC_IMPORT`, or resolves simple names to types.
- Single-type: `import a.b.C;`
- Star: `import a.b.*;`
- Static: `import static a.b.C.method;`
- Static star: `import static a.b.C.*;`
- Nested-type import: `import a.b.C.D;`
- FQN usage with no import at all
- Same simple name from different packages (one must be FQN)
- Implicit `java.lang`: an unqualified `Integer` needs no import at all
- Same-package sibling type: also resolves with no import
- Default package: the file has no `PACKAGE_DEF` node
- `package-info.java`: a compilation unit with a package declaration and no type

## Annotations
Apply if the check inspects `ANNOTATION`, `MODIFIERS`, or any declaration that can be annotated.
- Marker: `@A`
- Empty parens: `@A()`
- Single-value shorthand vs explicit: `@A(x)` and `@A(value = x)`
- Multi-param (order matters for alphabetical checks): `@A(b = 1, c = 2)`
- Array-valued: `@A({1, 2})`
- Nested annotation value: `@A(@B)`
- Stacked (multiple annotations): `@A @B @C SomeDecl`
- Repeatable duplicates: `@A @A SomeDecl`
- **Every annotatable target kind:** class, interface, enum, record, annotation type, method,
  constructor, field, local var, enum constant, annotation element, parameter, catch param, lambda
  param, for-each var, type parameter, package, import
- **Type-use positions** (often forgotten — these annotations land on `TYPE`, not `MODIFIERS`):
  - `@A String field`
  - `List<@A String>` (inside `TYPE_ARGUMENTS`)
  - `String @A []` (on the array level)
  - `Foo.@A Inner` (qualified nested type)
  - `(@A String) cast`
  - `new @A Foo()`
  - `x instanceof @A Foo`
  - `throws @A E`
- Receiver param annotations: `void m(@A Outer.this)`
- Annotations asymmetrically on field vs parameter vs return — check that type-matching isn't
  affected

## Generics
Apply if the check inspects `TYPE`, `TYPE_ARGUMENTS`, `TYPE_PARAMETERS`, or any type at a call site.
- Raw type: `List` (no generics at all)
- Simple: `List<String>`
- Nested: `Map<String, List<Integer>>`
- Deeply nested: `Map<K, Map<K2, Map<K3, List<V>>>>`
- Wildcards: `List<?>`, `List<? extends X>`, `List<? super X>`
- Multiple wildcards in one type: `Map<? extends K, ? super V>`
- Type parameter bounds: `<T extends X>`, `<T extends X & Y>`, `<T extends X & Y & Z>`
- Recursive bounds: `<T extends Comparable<T>>`
- Type parameter on class, method, AND constructor
- Diamond: `new ArrayList<>()`
- Generic constructor invocation: `new <String> ArrayList<>()`
- Type witness on method call: `Collections.<String>emptyList()`
- Type-use annotations on type args: `List<@Nullable String>`
- Generic array workarounds (`List<String>[]`)
- Empty type args (invalid but parseable in some positions)

## Method / constructor chains
Apply if the check inspects `METHOD_CALL`, `DOT`, `CTOR_CALL`, or walks expressions.
- No chain: `foo()`
- Short chain: `a.b()`
- Long chain across multiple lines
- Chain with cast: `((Foo) x).bar()`
- Chain with parenthesized expr as receiver: `(a + b).toString()`
- Chain with generics: `Collections.<String>emptyList().size()`
- Chain with ternary receiver: `(cond ? a : b).method()`
- Chain starting with constructor: `new Foo().bar().baz()`
- Chain with array access: `arr[0].method()`
- Chain with method arg that is itself a chain
- Chain ending at various depths (for "last call in chain" logic)

## Lambdas and method references
Apply if the check inspects `LAMBDA`, `METHOD_REF`, or functional interface usage.
- Expression body: `x -> x + 1`
- Block body with return: `x -> { return x + 1; }`
- Block body with multiple statements
- Zero params: `() -> 42`
- Single naked param: `x -> ...`
- Single parenthesized: `(x) -> ...` (should be flagged by `LambdaParameterTypeCheck`)
- Multi-param: `(x, y) -> ...`
- Typed params: `(String x) -> ...`
- `var` params: `(var x, var y) -> ...`
- Annotated params: `(@A String x) -> ...` (requires `var`)
- Mixed annotated + non-annotated — `anyAnnotated` logic
- Nested lambdas: `x -> y -> x + y`
- Method references: `X::method`, `X::new`, `this::method`, `obj::method`, `X::<T>method`
- Bound vs unbound method references

## Anonymous classes
Apply if the check inspects `OBJBLOCK` in `LITERAL_NEW` context, or class-like bodies.
- Empty body: `new Foo() {}`
- With methods only: `new Foo() { void bar() {} }`
- With fields
- With inner types (triggers different handling in `PreferLambdaCheck`)
- Generic supertype: `new ArrayList<String>() {}`
- Implementing interface vs extending class
- With constructor args: `new Foo(1, 2) {}`
- As argument to a method call
- As right-hand-side of an assignment / field initializer
- Anonymous vs lambda — both can implement functional interfaces but produce different ASTs

## Loops and control flow
Apply if the check inspects `LITERAL_FOR`, `LITERAL_WHILE`, `LITERAL_DO`, `DO_WHILE`,
`FOR_EACH_CLAUSE`, `LITERAL_BREAK`, `LITERAL_CONTINUE`, or any statement body.
- `DO_WHILE` (175) and `LITERAL_WHILE` (84) are DISTINCT tokens: a do-while's trailing `while` is
  not a while loop
- The do-while body is the FIRST child of `LITERAL_DO`, ahead of the condition
- Traditional `for` with empty init, empty condition, or empty update, up to `for (;;)`
- Comma-separated init and update: `for (int i = 0, j = n; i < j; ++i, --j)`
- For-each over an array vs over an `Iterable`; the `FOR_EACH_CLAUSE` iterable is an `EXPR`
- Empty-statement body: `while (c);` is `EMPTY_STAT`, not an empty block
- Braced vs braceless body, and a body that is itself a declaration (a declaration is a block
  statement, so it cannot stand as a braceless body)
- `break` / `continue`, bare vs labeled
- Infinite forms: `for (;;)`, `while (true)`, `do ... while (true)`

## Conditional expressions (ternary)
Apply if the check inspects `QUESTION` or walks expression trees.
- The condition is the bare operator node, NOT wrapped in `EXPR`, and `COLON` is a real child
  between the branches (see `docs/ast-structure.md`)
- Nested ternary in the condition, in the true branch, in the false branch
- Position: argument, initializer, return value, assignment RHS
- Parenthesized condition `(flag) ? a : b` vs bare
- Spanning multiple lines (the `CLAUDE.md` call-formatting "ternary" exception)
- Both branches literals, one branch a literal, neither

## Assignment, increment, and decrement
Apply if the check inspects `ASSIGN`, any `*_ASSIGN`, `INC`, `DEC`, `POST_INC`, or `POST_DEC`.
- `INC` / `DEC` (prefix) and `POST_INC` / `POST_DEC` (postfix) are structurally identical but
  semantically different tokens
- All eleven compound forms: `+=`, `-=`, `*=`, `/=`, `%=`, `&=`, `|=`, `^=`, `<<=`, `>>=`, `>>>=`
- Assignment as a statement vs as an expression: `a = b = c`, `while ((n = read()) != -1)`
- Simple RHS vs chained-call RHS vs `new` RHS, the distinction `ControlFlowBracesCheck`'s do-while
  tiers are defined on
- Target forms: local, field, `this.field`, array element, qualified field

## Switches
Apply if the check inspects `LITERAL_SWITCH`, `SWITCH_RULE`, or `CASE_GROUP`.
- Traditional (colon) syntax: `case X:`
- Enhanced (arrow) syntax: `case X ->`
- Statement switch vs expression switch (`var y = switch (x) {...}`)
- Fall-through cases (colon form with shared body)
- Comma-separated labels: `case A, B ->`
- `yield`
- `break` with value (traditional form)
- Default position (last vs middle — grammar allows middle)
- Empty case body (should be caught but verify interaction)
- Pattern cases: `case Integer i ->`, `case null, default ->`
- Guarded patterns: `case String s when s.isEmpty() ->`
- Record patterns: `case Point(int x, int y) ->`
- Nested patterns
- Braced case blocks (when a variable is declared — `NoCaseBracesCheck`)

## Pattern matching
Apply if the check inspects `LITERAL_INSTANCEOF`, `PATTERN_VARIABLE_DEF`, or switch patterns.
- `x instanceof Foo` (no binding)
- `x instanceof Foo f` (binding)
- `x instanceof Foo(int a, int b)` (record pattern)
- Negated: `!(x instanceof Foo f)`
- In compound conditions: `if (x instanceof Foo f && f.bar())`
- In switch (as above)
- Pattern variable scope — leaks into else-branch or after the `if`

## Try statements
Apply if the check inspects `LITERAL_TRY`, `LITERAL_CATCH`, or `RESOURCES`.
- try / catch
- try / finally
- try / catch / finally
- Multi-catch: `catch (A | B e)`
- Try-with-resources with 1 resource
- Try-with-resources with multiple resources
- TWR with existing variable (Java 9+): `try (x)` with no assignment, requires effectively-final
- Nested try
- Annotations on catch param
- Final catch param (should be flagged by `NoFinalParametersCheck`)

## Records and sealed types
Apply if the check inspects `RECORD_DEF`, `RECORD_COMPONENTS`, or class hierarchy /
`PERMITS_CLAUSE`.
- Record: zero / one / many components
- Generic record: `record Pair<A, B>(A a, B b) {}`
- Record with methods, static fields, compact constructor, canonical constructor
- Accessor overrides with `@Override`
- Sealed class: `sealed class X permits Y, Z {}`
- Sealed interface
- `non-sealed` subtype
- `final` subtype of sealed
- Implicit permits (permitted subtypes in same file, no `permits` clause)

## Casts
Apply if the check inspects `TYPECAST`.
- Primitive vs reference cast
- Widening vs narrowing vs cast to the same type
- Generic cast `(List<String>) x`, intersection cast `(Runnable & Serializable) x`
- Double cast `(int) (long) x`
- Cast of a lambda or method reference
- Cast as a chain receiver `((Foo) x).bar()`, where the outer parens belong to the chain and not
  to the cast
- Cast inside vs outside the enclosing parens: `((Foo) x)` vs `(Foo) (x)`
- Type-use annotation on the cast type: `(@A String) x`

## this and super
Apply if the check inspects `LITERAL_THIS`, `LITERAL_SUPER`, `SUPER_CTOR_CALL`, or `CTOR_CALL`.
- Bare `this.field` vs qualified `Outer.this.field`
- `super.method()` vs `Outer.super.method()`
- Explicit constructor invocation `this(...)` and `super(...)`, plus the implicit no-arg `super()`
  that produces no node at all
- `this` passed as an argument
- Inner-class creation `new Inner()` vs `outer.new Inner()`

## Enums
Apply if the check inspects `ENUM_DEF`, `ENUM_CONSTANT_DEF`.
- Simple enum
- Enum with fields + constructor + methods
- **Enum constants with body** (each constant overriding — easy to forget)
- Enum implementing interface
- Enum with annotations on constants
- Enum with Javadoc on constants
- Static imports of enum values

## Inner / nested types
Apply if the check inspects class structure or type declarations.
- Static nested class
- Non-static inner class
- Local class (inside method)
- Anonymous class (as above)
- Nested enums, records, interfaces, annotation types
- Double-nested (class inside class inside class)
- Inner enum with bodies per constant

## Variable declarations
Apply if the check inspects `VARIABLE_DEF`, or reports one violation per declarator.
- Multi-declarator: `int a, b;` produces TWO `VARIABLE_DEF` nodes from one statement, so a
  per-node `log()` fires twice on one line. See the cross-check interference section of
  `docs/testing.md`
- Two declarators vs three or more
- Mixed initialized and uninitialized: `int a = 1, b;`
- C-style brackets on the name: `int x[]`, `int x[][]`, `int x [] `, and the mixed form
  `int alpha[], beta;` where the two declarators end up with different types
- Declaration split across lines, type on one line and name on the next
- Field vs local vs for-init vs catch parameter vs record component: each has a different parent

## Modifiers
Apply if the check inspects `MODIFIERS`.
- All 4 visibilities on each declaration kind
- Package-private (absent modifier)
- All combinations relevant to the check: `public static final`, `private static`, etc.
- JLS modifier order violations
- **Implicit modifiers** (often forgotten):
  - Interface methods: implicitly `public abstract` (unless `default`/`static`/`private`)
  - Interface fields: implicitly `public static final`
  - Record components: implicitly `private final`
  - Nested types in interfaces: implicitly `public static`
- `default` methods on interfaces
- `private` methods on interfaces (Java 9+)
- `sealed` / `non-sealed` / `final`

## Strings and text blocks
Apply if the check inspects `STRING_LITERAL` or `TEXT_BLOCK_LITERAL_BEGIN`.
- Empty: `""`
- Single char: `"x"`
- With escape sequences: `\n`, `\t`, `\u0041`
- With unicode escapes in source
- Text block with various indentation
- String + `+` concatenation (becomes parse tree with multiple `STRING_LITERAL` siblings)
- Concatenation across multiple lines
- `String.format` / `.formatted` (relevant to `PreferSpecificApiCheck`)

## Char literals
Apply if the check inspects `CHAR_LITERAL`, or scans line text for quotes.
- Plain `'a'`, escapes `'\n'` / `'\''` / `'\\'` / `'\0'`, unicode `'\u0041'`
- A char literal holding a quote or comment-looking text (`'/'`, `'"'`, `'*'`) that a text-scanning
  fixer has to mask before it scans
- Char used where a number is expected (`char` sorts among the primitives, and arithmetic on it is
  int arithmetic)

## Numeric literals
Apply if the check inspects `NUM_INT`, `NUM_LONG`, `NUM_FLOAT`, `NUM_DOUBLE`. `docs/testing.md`
("Numeric literal permutations") owns the full enumeration and the boundary-pair rule; this is the
shape list.
- Integer forms: decimal, octal (`010` is 8, `077`), hex (`0x`), binary (`0b`), with underscores,
  uppercase vs lowercase prefix
- Hex digit case: `0xFF` vs `0xff`, and the prefix case independently (`0X`, `0B`). The project
  style rule is uppercase digits with a lowercase prefix, so both halves need their own test
- Long: every integer form plus `L`, and the lowercase `l` that is easy to miss
- Float: `0.0f`, `0f`, `0.f`, `.5f`, and every one of them with a capital `F`
- Double: `0.0`, `0.0d`, `0.`, `.0`, and each with a capital `D`
- Exponents: `1e10`, `1E10`, `1e+10`, `1e-10`, and an exponent with no fractional part at all
- Hex float: `0x0.0p0`, plus the capital `P` form
- Underscores in every legal position (`1_000`, `0xFF_FF`, `1_0.0_1`) and the illegal ones a regex
  might accept anyway (leading, trailing, adjacent to the dot or the prefix)
- Signed forms: `-0` is `UNARY_MINUS{NUM_INT}` and `+5` is `UNARY_PLUS{NUM_INT}`; neither is a
  single literal node, and whitespace is allowed after the sign (`+ 5`, `- 0`)
- NaN and the infinities have NO literal form. `Float.NaN` / `Double.POSITIVE_INFINITY` are field
  accesses, and `0.0f / 0.0f` or `1.0 / 0.0` are expressions. A check looking for them by literal
  text will never fire; see the numeric semantics subsection below for their behavior
- Boundary pairs: for every zero-detection test, add a non-zero counterpart with the same notation

## Numeric semantics edge values (IEEE 754 + integer overflow)
Apply if the check inspects numeric comparisons, arithmetic expressions, numeric casts, `Math.*`
calls, or any rewrite that must preserve numeric semantics. Claude almost always forgets these
without an explicit prompt — they are *not* covered by "the happy path with `int x = 5`."

For each value below that's relevant to the check's behavior, name a test exercising it (or mark
MISSING if no test exists). Boundary pairs still apply: a test with `NaN` needs a counterpart test
with a normal value under the same code path.

**Float / double edge values**
- `NaN` — expressed as `Double.NaN` / `Float.NaN` or the expression `0.0 / 0.0`
- `+Infinity` — `Double.POSITIVE_INFINITY`, `Float.POSITIVE_INFINITY`, `1.0 / 0.0`
- `-Infinity` — `Double.NEGATIVE_INFINITY`, `Float.NEGATIVE_INFINITY`, `-1.0 / 0.0`
- `-0.0` paired with `+0.0` (they `==` each other but behave differently)
- `Float.MIN_VALUE` / `Double.MIN_VALUE` (smallest positive denormal, NOT most negative)
- `-Double.MAX_VALUE` / `-Float.MAX_VALUE` (actual most-negative finite values)
- `Double.MAX_VALUE` / `Float.MAX_VALUE`
- Subnormals (denormals) — any value smaller than `Double.MIN_NORMAL`
- Precision-loss boundary: `1e16 + 1.0` rounds to `1e16` in double

**Integer edge values**
- `Integer.MIN_VALUE`, `Long.MIN_VALUE` — `Math.abs` of these returns the same negative value
  (overflow); negation of these overflows too
- `Integer.MAX_VALUE`, `Long.MAX_VALUE` — `+1` overflows
- Boundaries of narrower integer types if the check distinguishes them: `Byte.MIN_VALUE` /
  `Byte.MAX_VALUE`, `Short.*`, `Character.MIN_VALUE` / `MAX_VALUE`
- Zero (`0`, `0L`) paired with just-above-zero (`1`, `1L`) and just-below-zero (`-1`, `-1L`)

**IEEE 754 comparison semantics** (only relevant when the check inspects relational operators on
floats/doubles)
- `NaN < x`, `NaN > x`, `NaN == x`, `NaN <= x`, `NaN >= x` are all false; `NaN != x` is true
- `-0.0 == 0.0` is true, but `1.0 / -0.0 == Double.NEGATIVE_INFINITY` while `1.0 / 0.0 ==
  Double.POSITIVE_INFINITY`
- For ternary-to-`Math` rewrites: `>` vs `>=` gives different results for `-0.0` (`x > 0 ? x : -x`
  vs `x >= 0 ? x : -x`)
- `Math.min(-0.0, 0.0)` returns `-0.0`; `Math.max` returns `0.0`. The ternary `a < b ? a : b` on
  `(+0.0, -0.0)` returns `+0.0` (since neither is less than the other) — divergent from `Math.min`
- `Math.min(x, NaN)` and `Math.max(x, NaN)` return `NaN` for any `x`. The ternary `a < b ? a : b` on
  `(x, NaN)` returns `x` (since `x < NaN` is false). Divergent

**Integer arithmetic semantics** (only relevant when the check inspects arithmetic or casts)
- `Integer.MIN_VALUE` negated still equals `Integer.MIN_VALUE` — `Math.abs` does NOT diverge from
  the ternary here (both preserve the overflow), but the test is still worth having
- Widening-cast order changes overflow behavior: `(long) (x * y)` computes `x * y` as int first
  (overflows), then widens; `(long) x * y` widens first, then multiplies (no overflow). A fixer that
  rewrites between these forms changes semantics for values near `Integer.MAX_VALUE`
- Integer division by zero throws `ArithmeticException`; float division by zero produces `Infinity`.
  If the check rewrites between integer and float arithmetic, this matters
- Integer division truncates toward zero; `Math.floorDiv` rounds toward negative infinity —
  different for negative operands

**When in doubt, ask** "does my check/fixer ever touch a value where `NaN == NaN` being false
matters? Where `-0.0` behaves differently from `+0.0`? Where `Integer.MIN_VALUE` overflow on
negation matters?" If yes, tests are required.

## Comments and Javadoc
Apply if the check reads raw text or inspects comment positions (`COMMENT_CONTENT`,
`SINGLE_LINE_COMMENT`, `BLOCK_COMMENT_BEGIN`).
- Line, block, Javadoc
- Comment between tokens: `int /* */ x;`
- Comment inside expression: `a + /* */ b`
- Javadoc on various declaration kinds
- Trailing line comment after code on same line
- Comment immediately before a declaration (can inhibit `FieldConsolidationCheck`)

## Varargs and arrays
Apply if the check inspects method params, types, or `ARRAY_DECLARATOR`.
- `String...`
- `String[]`
- Multi-dim: `String[][]`
- Array annotations in each position: `String @A [] arr` vs `@A String[] arr` vs `String[] @A arr`
- Varargs with generic: `List<String>...`
- Varargs position (must be last parameter)
- Array initializer: `new int[]{1, 2}` vs `new int[]{}` vs implicit `{1, 2}`
- Trailing comma in array initializer (`NoArrayTrailingCommaCheck`)

## Line wrapping and multi-line constructs
Apply if the check or fixer works from a line and a column rather than purely from the AST.
- Where the break falls: after `(`, after `,`, before `.`, either side of an operator
- Arguments sharing the opening-paren line vs each on its own line
- A `//` comment between wrapped lines, which a naive join would swallow
- A violation whose reported column lands on a continuation line rather than the first line
- A block comment or text block that opens on one line and closes on a later one, so a
  single-line scan sees unbalanced state
- The same construct in both collapsed and expanded form, since the fixer must be a no-op on the
  one that is already correct

## Whitespace, encoding, line endings
Apply if the check is regex-based or inspects columns / text directly.

- Tabs at various positions (project uses tabWidth=4, see `LineLength.TAB_WIDTH`)
- Mixed tabs/spaces on one line
- CRLF vs LF vs CR
- BOM at file start
- Trailing whitespace on lines
- Multi-byte UTF-8 characters in strings / comments / identifiers
- Very long lines
- Empty file vs file with only a newline vs file with only BOM
- File ending with and without trailing newline (project rule: no trailing newline)

## Receiver parameters and unusual declaration forms
Apply if the check inspects method signatures or declaration bodies.
- Receiver parameter: `void foo(@A MyClass this)` — often forgotten, lives in `PARAMETERS`
- Generic method: `<T> T foo()`
- Static vs instance initializer blocks: `static {}` vs `{}`
- Empty blocks vs blocks with statements (`EmptyBodyCheck`)
- Labeled statements: `label: for (...) { break label; }`
