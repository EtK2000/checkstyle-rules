# checkstyle-rules

[![Release](https://jitpack.io/v/com.etk2000/checkstyle-rules.svg)](https://jitpack.io/#com.etk2000/checkstyle-rules)

> **Warning:** This project is vibecoded slop. Proceed with caution.

A Gradle plugin that bundles custom checkstyle checks and configuration. Apply one plugin and get
all rules, no per-module boilerplate.

## Usage

```gradle
plugins {
    id 'com.etk2000.checkstyle' version 'VERSION'
}
```

This automatically:

- Applies the built-in `checkstyle` plugin
- Configures checkstyle with the bundled rules
- Registers `checkstyleMain` and `checkstyleTest` tasks
- Registers `checkstyleFix` and `checkstyleFixTest` auto-fix tasks
- Hooks both check tasks into `check`

## Auto-fix

The plugin includes tasks that auto-fix simple, mechanical violations:

```bash
./gradlew checkstyleFix       # fix main sources
./gradlew checkstyleFixTest   # fix test sources
./gradlew checkstyleFixAll    # fix both
```

When checkstyle finds violations, a hint shows how many are auto-fixable:

```
Run ./gradlew checkstyleFixAll to auto-fix 5 of 8 violations.
```

The fix task reports what it fixed and what it skipped with reasons:

```
Fixed 5 violations in 2 files (2 skipped)
Skipped violations:
  PreferMathMethodCheck: parenthesized or multiline ternary (x2)
```

See [docs/auto-fix-coverage.md](docs/auto-fix-coverage.md) for the full list of fixable checks
and sub-rules. `@SuppressWarnings` suppressions are honored by the fix task (suppressed violations
are not reported, so the fixer never sees them). Review changes before committing.

## Suppressions

To suppress specific rules per project, create `config/checkstyle/suppressions.xml` in your project
root. It will be picked up automatically.

Some checks also support per-type `@SuppressWarnings` suppression. See
[docs/suppress-warnings.md](docs/suppress-warnings.md) for details and supported keys.

## Included checks

### Custom checks (AST-based)

| Check | Description |
| --- | --- |
| AnnotationOwnLineCheck | Stacked annotation formatting for declarations (classes, interfaces, enums, records, annotation types, methods, constructors, fields, local variables, enum constants, annotation fields, package declarations): each annotation on its own line, no blank lines between annotations, alphabetically sorted. Replaces built-in AnnotationLocation and AnnotationAlphabeticalOrderCheck |
| AnnotationSameLineCheck | Inline annotation formatting for parameters (method, constructor, catch, lambda), record components, and for-each iteration variables: all annotations on the same line as the declaration, alphabetically sorted |
| ArrayTypeStyleCheck | Flag C-style array declarations (`int x[]`) and suggest Java-style (`int[] x`). Applies to fields, local variables, method/constructor parameters, and method return types. [Auto-fixable](docs/coverage/arraytypestyle.md) |
| ClassStructureOrderCheck | Class members must follow section ordering (inner types, static fields, static methods, instance fields, constructors, instance methods) |
| ConstructorAssignmentOrderCheck | Constructor field assignments in three groups: (1) simple one-liners without local variables, (2) multi-line without local variables, (3) assignments using local variables (sub-grouped by variable declaration order). Alphabetical within each group, with field-to-field dependency exceptions |
| ControlFlowBracesCheck | No one-liners, no unnecessary braces on single-line bodies, braces required on multi-line bodies. A body that is a variable or local-type declaration keeps its braces (a declaration is a block statement, so it cannot stand as a braceless body). Do-while: simple bodies (increment, assignment with simple RHS, single non-chained method call) on the `do` line with `while` on the next line; non-simple bodies (chained calls, `new`, or an assignment whose RHS is a chained call/`new`/binary expression) on their own line. Either form requires the closing `while` on its own line. [Auto-fixable](docs/coverage/controlflowbraces.md) |
| EmptyBodyCheck | No empty if/else bodies (error), no empty while/for/do-while bodies (warning), no empty static/instance initializer blocks (error) |
| EmptySwitchCheck | No empty switch statements |
| FieldConsolidationCheck | Consecutive same-type uninitialized fields with matching modifiers and annotations must be combined onto one line (`int a; int b;` becomes `int a, b;`). Skips fields with initializers, different visibility/static/final, different annotations or annotation params, or preceding comments/Javadoc |
| FieldSortingCheck | Fields sorted: finals with values, finals without, non-finals; anonymous class initializers before non-anonymous within each chunk; primitives before references; within same type, unannotated before annotated, then by first annotation name alphabetically; within same field annotations, type argument annotations follow the same rules (unannotated before annotated, alphabetical); then alphabetical by field name. Enum constants sorted alphabetically, each on its own line. References inside anonymous class bodies are not treated as init-time dependencies. Suppressible with `@SuppressWarnings("FieldSorting")` |
| InfiniteEmptyLoopCheck | Empty infinite loops (`for(;;);`, `while(true);`, `do;while(true);`) are errors |
| InstanceofBeforeCastCheck | In `&&` conditions, `instanceof` must come before casting to the same type. Also flags casts in branches where `instanceof` is false (ternary false branch, `else` after `instanceof`, `then` after `!(instanceof)`) since these always throw `ClassCastException` |
| JitInefficiencyCheck | Flags JIT-unfriendly patterns and unnecessary allocations: `"" + x` (use `String.valueOf`); `new String(literal)` / `new String(stringVar)` (use directly); local `new StringBuffer()` (use `StringBuilder`); boxed primitive constructors (use `valueOf` / `Boolean.TRUE`); `.toArray(new T[size])` size!=0 (use `new T[0]`); string concat inside `.append(...)` (use chained `.append()`); String `+=` / `s = s + ...` in a loop (use `StringBuilder`); `.matches/.replaceAll/.split` in a loop (hoist `Pattern.compile`); `Map.keySet()` + `.get(key)` for-each (iterate `.entrySet()`); `Enum.values()` in a loop (cache to static final); double-brace initialization (use `List.of(...)`/constructor); repeated `Pattern.compile` / `DateTimeFormatter.ofPattern` / `new SimpleDateFormat`/`Gson`/`ObjectMapper`/`DecimalFormat` in method bodies (hoist to static final); boxed numeric accumulator modified in a loop (use primitive); explicit iterator `while (it.hasNext())` (use enhanced `for`) |
| LambdaParameterTypeCheck | Lambda parameters should use implicit types (`x ->` not `(String x) ->`). When annotations are present, use `var` instead of explicit types (`@A var x` not `@A String x`). Single implicit parameters should be naked (`x ->` not `(x) ->`) |
| MethodAlphabeticalOrderCheck | Methods sorted alphabetically within their section (static/instance) |
| MultilineCallFormattingCheck | Multiline call/signature formatting with ternary, inline block, and method call arg exceptions. Special inline methods: `Arrays.asList`, `List/Map/Set.of`, `List/Map/Set.copyOf`. Also flags a `new JSONObject().put(k, v)` with a simple value that is split across lines but fits on one. [Auto-fixable](docs/coverage/multilinecallformatting.md) |
| NoArrayTrailingCommaCheck | No trailing comma in array initializers |
| NoBlankLineBetweenSingleCasesCheck | No blank lines between consecutive single-line switch cases |
| NoCaseBracesCheck | No unnecessary braces in case blocks, braces required when a variable is defined |
| NoEnumTrailingSemicolonCheck | No trailing semicolon in enum without body declarations. Flags `enum Foo { A; }` but allows `enum Foo { A; void m() {} }` |
| NoFinalParametersCheck | Parameters (method, constructor, catch, lambda) and for-each iteration variables must not be `final` (error). For-loop init variables must not be `final` (warning, conflicts with `FinalLocalVariable`). Replaces the regex-based NoFinalParameters rule with AST-based detection |
| NoUnnecessaryThisCheck | No `this.` unless shadowing or in field assignment |
| OverloadMethodOrderCheck | Overloaded methods ordered by ascending parameter count |
| PreferBulkOperationCheck | Prefer `addAll`/`putAll`/`System.arraycopy`/`Arrays.fill` over element-by-element loops. Detects for-each add, indexed add+get, entrySet put, forEach lambda put, indexed array copy, and indexed constant fill patterns. Only fires on single-statement bodies with no transformation, no conditionals, and matching source/target identifiers |
| PreferCollectionInterfaceCheck | Method and constructor signatures must use collection interface types (List, Set, Map) instead of concrete implementations (ArrayList, HashSet, HashMap, etc.). Checks return types, parameter types, and nested generic type arguments. A signature is left alone when replacing its concrete collections would make it identical to another overload's (`dump(List<String>)` alongside `dump(ArrayList<String>)`), since that would stop compiling |
| PreferDirectBooleanReturnCheck | Flag an `if` whose body and paired return collapse to one `return`: opposite literals -> `return cond;` / `return !cond;`; both the same literal -> `return LIT;` (a side-effect-free condition is dropped, even if it could throw; otherwise a hoistable side effect is extracted ahead of the return, and a leading cast is transparent so `(boolean) box()` drops the cast and hoists `box();`); one non-literal branch -> short-circuit `C && X` / `!C \|\| X` / `C \|\| X` / `!C && X`. Collapses that span multiple lines, mix `&&`/`\|\|`, use 3+ boolean operators, or exceed the line-length budget are left alone. Array creation counts as side-effect-free. [Auto-fixable](docs/coverage/preferdirectbooleanreturn.md) |
| PreferDoWhileCheck | Prefer `do-while` when a statement is duplicated immediately before a `while` loop whose body is structurally identical. Only fires for single-statement bodies (with or without braces); multi-statement bodies are excluded because the transformation can change execution order |
| PreferExactAssertionCheck | Flag `assertTrue`/`assertFalse` with comparison operators (`>`, `>=`, `<`, `<=`, `==`, `!=`), `instanceof`, or a top-level negation (`!x`). Use a dedicated assertion method (`assertEquals`, `assertSame`, `assertInstanceOf`, `assertNotInstanceOf`) or the opposite-polarity assertion (`assertTrue(!x)` -> `assertFalse(x)`) instead. The `instanceof` rule is polarity-aware for pattern bindings (`instanceof Y y`): cases that effectively assert FALSE drop the unreachable binding and rewrite to `assertNotInstanceOf`; cases that assert TRUE keep the binding and skip. Handles bare and qualified calls, JUnit 4 and JUnit 5 argument orders. The `instanceof` form is framework-gated (JUnit 4 has no `assertInstanceOf`); comparison and negation forms fire under either framework |
| PreferImportCheck | Prefer imports over fully qualified names (error); a nested type qualified through an in-scope type (e.g. `Map.Entry` with `Map` imported) is a warning to import the nested type directly |
| PreferLambdaCheck | Prefer lambda expression over anonymous class for functional interfaces with a single method and no extra members. Uses reflection to verify the type is a functional interface |
| PreferLiteralSuffixCheck | Prefer `100L` over `(long) x * 100` when a literal suffix can replace a widening cast. [Auto-fixable](docs/coverage/preferliteralsuffix.md) |
| PreferMathMethodCheck | Prefer `Math.max`/`Math.min`/`Math.abs` over ternary comparisons or if-else assignment/return chains, `Math.clamp` over nested `Math.max`/`Math.min` (API 35+). Only flags pure operands (no side effects) |
| PreferPatternMatchingInstanceofCheck | Prefer `instanceof Foo f` over `instanceof` followed by cast |
| PreferPrefixIncrementCheck | Use `++i`/`--i` instead of `i++`/`i--` |
| PreferRecordCheck | Flag classes that can be converted to records: all instance fields final without inline initializers, no extends clause, no instance initializers, no `@Override` equals/hashCode/toString, constructors only do simple `this.field = param` assignments. Suppressible with `@SuppressWarnings("PreferRecord")` |
| PreferSpecificApiCheck | Prefer `.getFirst()` over `.get(0)`, `.getLast()` over `.get(size()-1)`, `.removeFirst()` over `.remove(0)`, `.removeLast()` over `.remove(size()-1)`, `.isEmpty()` over `.size()`/`.length() == 0`, `.isEmpty()` over `.equals("")`, `.contains(...)` over `.indexOf(...) != -1`/`>= 0`, `.toList()` over `.collect(Collectors.toList())` and `.collect(Collectors.toUnmodifiableList())`, `.forEach(...)` over `.stream().forEach(...)` (API 24+), `List.of()` over `Arrays.asList()` (API 30+), `List.of()`/`Set.of()`/`Map.of()` over `Collections.emptyList()`/`emptySet()`/`emptyMap()`/`singletonList()`/`singleton()`/`singletonMap()` (API 30+), `List.copyOf()`/`Set.copyOf()`/`Map.copyOf()` over `Collections.unmodifiableList()`/`unmodifiableSet()`/`unmodifiableMap()` (API 31+), `.isBlank()` over `.trim().isEmpty()`/`.strip().isEmpty()` (API 33+), `.toArray(Type[]::new)` over `.toArray(new Type[0])` (API 33+), `.formatted(...)` over `String.format(...)` (API 34+), `assertTrue`/`assertFalse` over `assertEquals`/`assertNotEquals` with `true`/`false`, `assertNull`/`assertNotNull` over `assertEquals`/`assertNotEquals`/`assertSame`/`assertNotSame` with `null`. Uses reflection to verify the receiver type has the suggested method. Suppresses when the same receiver uses other indices (sequential access pattern) |
| PreferStandardCharsetsCheck | Prefer `StandardCharsets.UTF_8` etc. over charset name strings (`"UTF-8"`). Covers all `StandardCharsets` constants. Respects `minSdk` (requires API 19+) |
| RecordFormattingCheck | Record declarations must put all components on the `record` line (style A) or each on its own line with no component sharing the opening- or closing-paren line (style B). Opening brace must be on the closing-paren (or implements clause) line, with exactly one space before it. Empty body keeps both braces together as `{}`; non-empty body puts `}` on its own line. [Auto-fixable](docs/coverage/recordformatting.md) |
| PreferStaticImportCheck | Prefer static imports for well-known utility methods: `Predicate.not` (API 33+), all `Objects` null helpers (`requireNonNull`/`isNull`/`nonNull` API 19+, `requireNonNullElse`/`requireNonNullElseGet` API 30+), and all static `Collectors` methods except `toList`/`toUnmodifiableList` (API 24+; the carve-out avoids overlap with `PreferSpecificApiCheck` rewriting to `stream.toList()`). Conservative: only flags when an explicit `import` exists, no local method shadows, no conflicting static import. Configurable `minOccurrences` (default `2`) avoids flagging single-use calls |
| PreferStaticImportConstantCheck | Flag `static final` fields whose initializer is a redundant alias of another class's static constant (e.g. `private static final int MAX_LINE_LENGTH = LineLength.MAX_LINE_LENGTH;`). Fires on any DOT-chain ending in an `IDENT` that uses the constant as-is: simple `Foo.X`, parenthesized `(Foo.X)`, fully-qualified `com.foo.Foo.X`, nested-class `Outer.Inner.X`. RHS with mutation (`Foo.X + 1`, `(int) Foo.X`, `Foo.getX()`, `Foo.ARR[0]`) is correctly skipped. Local field name doesn't need to match the constant name. Also detects the split form where the field is blank-final and assigned in a sibling `static { ... }` block. The fixer auto-fixes private fields; non-private fields fire but skip (deletion may break callers in other compilation units); cinit-split assignments cause a cinit-skip. An alias whose static-import replacement would collide with an existing different-class same-member `import static` is not flagged at all (converting it is impossible); a same-class import, a static wildcard, or an indeterminate FQCN still fires. Suppressible with `@SuppressWarnings("PreferStaticImportConstant")` on the field or enclosing type |
| PreferVarCheck | For-each loops, try-with-resources, and local variables must use `var`. Skips implicit array initializers (`Type[] x = {...}`) and lambdas/method references since `var` can't be used. Flags explicit array constructors (`new Type[]{...}`) preferring implicit form. Flags redundant `<Object>` type arguments in constructor calls with `var` (use diamond `<>` instead). Never flags a declaration where `var` would bind a different type than the declaration states: a boxed type whose initializer is not itself the box (`Byte b = 0`, `Integer i = Integer.parseInt(s)`; `Integer.valueOf(...)` and `new Integer(...)` still convert), a widening literal (`long x = 1`, `double d = 5f`), a declared supertype (`Object o = "x"`, `Object o = compute()`; an array of a supertype is exempt, since `Object[]` is its own type rather than a widening), a widened type argument (`Map<String, Object> m = Map.of(...)`), a wildcard (`List<?> w = ...`), a type whose qualifier carries its own type arguments (`Outer<String>.Inner`, since those bind the enclosing instance rather than the declared type), an anonymous class body `var` cannot name, or a constructed class that cannot take the declared type arguments (`Function<String, Integer> f = new MyFunc<>()` where `MyFunc<T> implements Function<T, Integer>`; off-classpath classes are compared by name, fully qualified when the `new` names one, falling back to the arity declared in the same file when the declared name cannot be resolved at all). A non-`final` local that is later reassigned to anything but a `new` of the class its initializer binds is reported as a warning rather than converted, since `var` would bind that class and the reassignment would stop compiling; the class is read from the initializer rather than from a `new` keyword, so a factory call and a conditional whose arms all construct one class count too, and the two spellings are compared fully qualified so `impl.Cache` is not mistaken for `api.Cache`. Reassigning the very same class still converts, and an already-`var` declaration is exempt. A for-each variable declared as a widening supertype of the element type (`for (Object o : strings)`, and equally `for (Object[] row : listOfStringArrays)`) is left alone for the same reason, as is a primitive or boxed element type unless the iterable is an array of exactly that type (`for (int n : listOfIntegers)` would rebind to `Integer`, while `for (int n : intArray)` converts). A call whose declaring class cannot be determined is left alone as well, since it may be target-typed and the conversion would drop the arguments it infers from. A declaration whose variable is passed to an overload set that discriminates on the narrowing (both `take(List)` and `take(ArrayList)` exist) is a warning rather than a conversion, since `var` would silently reselect the overload; an overload set that cannot tell the two types apart still converts. These apply to a try-with-resources declaration just as they do to a local, including a declared supertype of what it constructs (`try (InputStream in = new FileInputStream(f))` stays as written). A conditional or switch initializer is only converted when every arm is a `new` that can take those arguments, since under `var` the arms no longer share the declaration as a target type and each keeps its own diamond; a colon-form switch, a braced rule body, and a parenthesised condition are refused, since the arm walk cannot reach their values. Qualified declared types (`java.util.List<?>`) get the same refusals as their imported form. A declaration whose type arguments cannot be inferred from the call's arguments (`List<String> l = List.of()`) is also left alone, since the only legal witness form is `List.<String>of()`. A diamond initializer keeps its arguments instead: `List<String> l = new ArrayList<>()` becomes `var l = new ArrayList<String>()`, so the variable takes the concrete class; a diamond belonging to a chain's receiver (`new Foo<>().names()`) has them dropped instead, since the value's type comes from the chain. Simple anonymous classes are handled by PreferLambdaCheck. Auto-detects generic return types via reflection and same-file AST analysis, resolving a bare call through every enclosing class, a same-file supertype, or an `import static`'s owner; configurable `allowedMethods` fallback (e.g. `findViewById`) |
| RedundantAnnotationSyntaxCheck | Flag redundant annotation syntax: `@A()` should be `@A` (empty parens), `@A(value = x)` should be `@A(x)` (explicit `value` key) |
| RedundantArrayCreationCheck | Flag redundant explicit array creation for varargs calls: `Arrays.asList(new Object[]{"a"})` should be `Arrays.asList("a")`. Uses reflection to confirm the method is varargs. Skips primitive arrays to reference-type varargs (autoboxing change). Skips when non-varargs overloads exist with matching param count |
| RedundantCastCheck | Flag redundant casts: same-type casts and implicit widening primitive casts in assignments/returns. [Auto-fixable](docs/coverage/redundantcast.md) |
| RedundantEqualityBranchCheck | Flag redundant if-else where the condition is `X == Y` or `X != Y` and the branches each assign or return one of the two operands. The two operands are interchangeable in the matching branch, so the if-else can collapse to a single statement |
| RedundantNumericSuffixCheck | Flag redundant `L`/`f`/`d` suffixes when the target type is known from context. A suffix on a single-variable local or `for`-init declaration's whole initializer is left alone, including behind a leading `-`/`+` or parentheses (`long n = -1L` counts): the local must become `var` (see PreferVarCheck) and `var` binds the literal's own type, so dropping it would retype the variable (`long l = 5L` would become an `int`). Only `d` on a literal that already has a decimal point or exponent stays flagged there, since such a literal is a `double` without it. A multi-variable declaration (`long a = 0L, b = 1L;`) never becomes `var`, so its suffixes stay flagged just as a field's are |
| SwitchCaseOrderCheck | Switch cases sorted: named constants first, then numeric/digit content, then alphabetic content. `default` last |
| ThreadAnnotationCheck | Top-level classes must have a thread annotation |

### Regex rules

- No space indentation (use tabs)
- No blank line after class opening brace
- No blank line before closing brace
- No double blank lines
- Blank line after `break;` before next case
- No trailing whitespace
- No trailing newline at end of file

### Built-in checkstyle checks

AvoidNoArgumentSuperConstructorCall, CovariantEquals, EmptyLineSeparator, ExplicitInitialization,
FinalLocalVariable, HexLiteralCase,
IllegalSymbol, LineEnding (LF), MissingOverride, MissingOverrideOnRecordAccessor,
ModifierOrder, NoEnumTrailingComma, NumericalPrefixesInfixesSuffixesCharacterCase,
OneStatementPerLine, PatternVariableAssignment,
RedundantImport, RedundantModifier, RightCurly (else/catch/finally on own line),
SimplifyBooleanExpression, StringLiteralEquality, UnnecessaryNullCheckWithInstanceOf,
UnusedImports, UnusedLocalVariable, UpperEll, UseEnhancedSwitch

### TODO: Enable when upgrading to Java 22+

- `UnusedCatchParameterShouldBeUnnamed` -- flags unused catch params that should be `_`
- `UnusedLambdaParameterShouldBeUnnamed` -- flags unused lambda params that should be `_`

## Documentation

| Doc | Description |
| --- | --- |
| [Auto-fix coverage](docs/auto-fix-coverage.md) | Index of which checks have auto-fix support; per-check detail in [docs/coverage/](docs/coverage/) |
| [Adding a check](docs/adding-a-check.md) | How to add a new custom checkstyle rule |
| [Adding a fixer](docs/adding-a-fixer.md) | How to add auto-fix support for a check |
| [AST structure](docs/ast-structure.md) | Reference for checkstyle AST token types |
| [Testing](docs/testing.md) | Test architecture, conventions, and how to run tests |