# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.

## TreeWalker checks (FIXERS map)

| Check                                    | Fixer                              | Notes                                                                                     |
|------------------------------------------|------------------------------------|-------------------------------------------------------------------------------------------|
| AnnotationOwnLineCheck                   | AnnotationOwnLineFixer             | Splits stacked annotations to own lines, removes blank lines, sorts alphabetically        |
| AnnotationSameLineCheck                  | AnnotationSameLineFixer            | Joins annotations onto declaration line, sorts inline annotations alphabetically          |
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer      | Removes `super()` call                                                                    |
| ControlFlowBracesCheck                   | ControlFlowBracesFixer             | Do-while only: removes unnecessary braces, fixes one-liners, adds missing braces          |
| ExplicitInitializationCheck              | ExplicitInitializationFixer        | Removes `= 0`/`= null`/`= false` etc.                                                     |
| FinalLocalVariableCheck                  | FinalLocalVariableFixer            | Adds `final` keyword                                                                      |
| LambdaParameterTypeCheck                 | LambdaParameterTypeFixer           | See sub-rules below                                                                       |
| NoArrayTrailingCommaCheck                | NoArrayTrailingCommaFixer          | Removes trailing comma                                                                    |
| NoBlankLineBetweenSingleCasesCheck       | NoBlankLineBetweenSingleCasesFixer | Removes blank line                                                                        |
| NoEnumTrailingCommaCheck                 | NoArrayTrailingCommaFixer          | Same fixer as array trailing comma                                                        |
| NoFinalParametersCheck                   | RedundantModifierFixer             | Removes `final` keyword from parameter                                                    |
| NoUnnecessaryThisCheck                   | NoUnnecessaryThisFixer             | Removes `this.` prefix                                                                    |
| PreferCollectionInterfaceCheck           | PreferCollectionInterfaceFixer     | Replaces concrete collection type with interface (e.g. `ArrayList` to `List`)             |
| PreferMathMethodCheck                    | PreferMathMethodFixer              | See sub-rules below                                                                       |
| PreferPrefixIncrementCheck               | PreferPrefixIncrementFixer         | Moves `++`/`--` to prefix position                                                        |
| PreferSpecificApiCheck                   | PreferSpecificApiFixer             | See sub-rules below                                                                       |
| PreferStandardCharsetsCheck              | PreferStandardCharsetsFixer        | Replaces charset string literal with `StandardCharsets.X` constant (adds import)          |
| PreferStaticImportCheck                  | PreferStaticImportFixer            | Strips `Class.` prefix from a qualified call and adds an `import static <fqcn>.<method>;` |
| PreferVarCheck                           | PreferVarFixer                     | Replaces type with `var`; converts explicit array init to implicit                        |
| RedundantAnnotationSyntaxCheck           | RedundantAnnotationSyntaxFixer     | Removes `()` or `value =`                                                                 |
| RedundantImportCheck                     | DeleteLineFixer                    | Deletes import line                                                                       |
| RedundantModifierCheck                   | RedundantModifierFixer             | Removes redundant modifier keyword                                                        |
| RedundantNumericSuffixCheck              | RedundantNumericSuffixFixer        | Removes redundant `L`/`f`/`d` suffix                                                      |
| UnusedImportsCheck                       | DeleteLineFixer                    | Deletes import line                                                                       |
| UpperEllCheck                            | UpperEllFixer                      | Changes `l` to `L`                                                                        |

## Regex checks (MODULE_ID_FIXERS map)

| Module ID                     | Fixer                            | Notes                                                          |
|-------------------------------|----------------------------------|----------------------------------------------------------------|
| BlankLineAfterBreak           | BlankLineAfterBreakFixer         | Inserts blank line after `break;` before next `case`/`default` |
| NoBlankLineAfterClassBrace    | BlankLineAfterClassBraceFixer    | Removes blank lines after class/interface/enum/record `{`      |
| NoBlankLineBeforeClosingBrace | BlankLineBeforeClosingBraceFixer | Removes blank lines before `}`                                 |
| NoDoubleBlankLines            | DoubleBlankLineFixer             | Removes extra blank line                                       |
| NoTrailingNewline             | TrailingNewlineFixer             | Removes trailing blank lines at EOF                            |
| NoTrailingWhitespace          | TrailingWhitespaceFixer          | Trims trailing whitespace                                      |

## PreferMathMethodCheck sub-rules

The fixer uses regex for ternary patterns and paren-balanced parsing for clamp patterns.

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

| Pattern                          | Replacement             | Auto-fix                                                     |
|----------------------------------|-------------------------|--------------------------------------------------------------|
| `.toArray(new Type[0])`          | `.toArray(Type[]::new)` | Yes (skips multi-dimensional and annotated types)            |
| `.trim().isEmpty()`              | `.isBlank()`            | Yes                                                          |
| `.trim().length() == 0`          | `.isBlank()`            | Yes (including reversed `0 == ...` form)                     |
| `.trim().length() <= 0`          | `.isBlank()`            | Yes                                                          |
| `.trim().length() != 0` / `> 0`  | `!receiver.isBlank()`   | Simple receivers only (identifiers, dotted names)            |
| Reversed forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (positive reversed); simple receivers (negated reversed) |

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

## Regex checks without fixers

| Module ID     | Reason                                                                                            |
|---------------|---------------------------------------------------------------------------------------------------|
| NoSpaceIndent | Converting leading spaces to tabs requires knowing the original indent width (2? 4? 8?), which is |
|               | ambiguous. A wrong guess changes visual indentation. See `docs/regex-fixer-edge-cases.md`.        |

## Checks without fixers

Custom checks without auto-fix support and why.

| Check                                | Reason                                                                                                         |
|--------------------------------------|----------------------------------------------------------------------------------------------------------------|
| ClassStructureOrderCheck             | Reordering class members requires moving multi-line blocks with dependency analysis                            |
| ConstructorAssignmentOrderCheck      | Reordering assignments requires dependency analysis between fields                                             |
| EmptyBodyCheck                       | Ambiguous: removing the statement may discard intentional no-ops; adding a body requires context               |
| EmptySwitchCheck                     | Same as EmptyBodyCheck                                                                                         |
| FieldConsolidationCheck              | Merging field declarations onto one line requires verifying no inline initializers or annotations conflict     |
| FieldSortingCheck                    | Reordering fields/enum constants requires moving multi-line blocks with dependency analysis                    |
| InfiniteEmptyLoopCheck               | Flags bugs (infinite empty loops), not a stylistic issue with a deterministic fix                              |
| InstanceofBeforeCastCheck            | Reordering sub-expressions in compound boolean conditions while preserving short-circuit semantics             |
| MethodAlphabeticalOrderCheck         | Reordering methods requires moving multi-line blocks                                                           |
| MultilineCallFormattingCheck         | Reformatting argument layout across lines with context-dependent indent and grouping rules                     |
| NoCaseBracesCheck                    | Removing braces requires scope analysis to verify no variable declarations leak                                |
| OverloadMethodOrderCheck             | Reordering method overloads requires moving multi-line blocks                                                  |
| PreferImportCheck                    | Replacing FQN with short name and adding import; must verify no name conflicts                                 |
| PreferLambdaCheck                    | Structural transformation: anonymous class to lambda, must handle `this` references and field shadowing        |
| PreferLiteralSuffixCheck             | Replacing widening cast with literal suffix requires expression context analysis                               |
| PreferPatternMatchingInstanceofCheck | Restructuring instanceof + subsequent cast into pattern matching across multiple statements                    |
| PreferRecordCheck                    | Multi-line structural transformation: must rewrite class header, remove fields/constructor, adjust annotations |
| RedundantCastCheck                   | Removing a cast may change method overload resolution or widen the expression type                             |
| SwitchCaseOrderCheck                 | Reordering switch cases requires moving multi-line blocks with fall-through analysis                           |
| ThreadAnnotationCheck                | Cannot determine which thread annotation (`@MainThread`, `@AnyThread`, etc.) to add                            |

## Future fix opportunities

Patterns not currently auto-fixable, with what would be needed to support them.

| Pattern                                                   | Blocker                                                                                                       | Possible approach                                                                       |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `.indexOf(...) != -1`                                     | Need to restructure `expr op literal` into `receiver.contains(arg)` or `!receiver.contains(arg)`              | Extract receiver + arg from the indexOf call text, rebuild as contains, handle negation |
| `.get(size() - 1)` / `.remove(size() - 1)`                | Must verify the `size()` receiver matches the `.get()`/`.remove()` receiver                                   | Parse both receivers and compare, or pass violation metadata to the fixer               |
| `.stream().findFirst().isPresent()` (complex receivers)   | Simple receivers (identifiers, dotted names) are already fixed; method calls, casts, and array access are not | Extend `findReceiverStart()` to handle parenthesized expressions and method calls       |
| `.size() != 0` / `.length() > 0` etc. (complex receivers) | Positive forms (`== 0`) are fully fixed; negated forms need `!` insertion which requires simple receiver scan | Extend `findReceiverStart()` to handle complex receivers                                |