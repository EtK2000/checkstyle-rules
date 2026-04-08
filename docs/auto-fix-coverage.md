# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.

## TreeWalker checks (FIXERS map)

| Check                                    | Fixer                              | Notes                                                              |
|------------------------------------------|------------------------------------|--------------------------------------------------------------------|
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer      | Removes `super()` call                                             |
| ExplicitInitializationCheck              | ExplicitInitializationFixer        | Removes `= 0`/`= null`/`= false` etc.                              |
| FinalLocalVariableCheck                  | FinalLocalVariableFixer            | Adds `final` keyword                                               |
| LambdaParameterTypeCheck                 | LambdaParameterTypeFixer           | See sub-rules below                                                |
| NoArrayTrailingCommaCheck                | NoArrayTrailingCommaFixer          | Removes trailing comma                                             |
| NoBlankLineBetweenSingleCasesCheck       | NoBlankLineBetweenSingleCasesFixer | Removes blank line                                                 |
| NoEnumTrailingCommaCheck                 | NoArrayTrailingCommaFixer          | Same fixer as array trailing comma                                 |
| NoFinalParametersCheck                   | RedundantModifierFixer             | Removes `final` keyword from parameter                             |
| NoUnnecessaryThisCheck                   | NoUnnecessaryThisFixer             | Removes `this.` prefix                                             |
| PreferPrefixIncrementCheck               | PreferPrefixIncrementFixer         | Moves `++`/`--` to prefix position                                 |
| PreferSpecificApiCheck                   | PreferSpecificApiFixer             | See sub-rules below                                                |
| PreferVarCheck                           | PreferVarFixer                     | Replaces type with `var`; converts explicit array init to implicit |
| RedundantImportCheck                     | DeleteLineFixer                    | Deletes import line                                                |
| RedundantModifierCheck                   | RedundantModifierFixer             | Removes redundant modifier keyword                                 |
| RedundantNumericSuffixCheck              | RedundantNumericSuffixFixer        | Removes redundant `L`/`f`/`d` suffix                               |
| UnusedImportsCheck                       | DeleteLineFixer                    | Deletes import line                                                |
| UpperEllCheck                            | UpperEllFixer                      | Changes `l` to `L`                                                 |

## Regex checks (MODULE_ID_FIXERS map)

| Module ID            | Fixer                   | Notes                     |
|----------------------|-------------------------|---------------------------|
| NoDoubleBlankLines   | DoubleBlankLineFixer    | Removes extra blank line  |
| NoTrailingWhitespace | TrailingWhitespaceFixer | Trims trailing whitespace |

## PreferSpecificApiCheck sub-rules

This check has 17 detection paths grouped by minSdk gate. The fixer handles them via line-text
pattern matching, returning null (skipping) for patterns that require structural changes.

### No minSdk gate

| Pattern                                          | Replacement                                | Auto-fix   |
|--------------------------------------------------|--------------------------------------------|------------|
| `assertEquals(true/false/null, x)`               | `assertTrue`/`assertFalse`/`assertNull(x)` | 2-arg only |
| `assertNotEquals(true/false/null, x)`            | Inverted assertion                         | 2-arg only |
| `assertSame(null, x)` / `assertNotSame(null, x)` | `assertNull`/`assertNotNull(x)`            | 2-arg only |
| `.collect(Collectors.toList())`                  | `.toList()`                                | Yes        |
| `.collect(Collectors.toUnmodifiableList())`      | `.toList()`                                | Yes        |
| `.equals("")`                                    | `.isEmpty()`                               | Yes        |
| `.get(0)`                                        | `.getFirst()` (API 35+)                    | No         |
| `.get(size() - 1)`                               | `.getLast()` (API 35+)                     | No         |
| `.indexOf(str) != -1` / `>= 0` etc.              | `.contains(str)`                           | No         |
| `.keySet().contains(k)`                          | `.containsKey(k)`                          | Yes        |
| `.remove(0)`                                     | `.removeFirst()` (API 35+)                 | No         |
| `.remove(size() - 1)`                            | `.removeLast()` (API 35+)                  | No         |
| `.replaceAll("literal", x)`                      | `.replace("literal", x)`                   | Yes        |
| `.size() == 0` / `.length() == 0` etc.           | `.isEmpty()`                               | No         |
| `.stream().count()`                              | `.size()`                                  | Yes        |
| `.stream().findFirst().isPresent()`              | `!.isEmpty()`                              | No         |
| `.values().contains(v)`                          | `.containsValue(v)`                        | Yes        |

### API 24+ (MIN_SDK_FOR_EACH)

| Pattern                       | Replacement       | Auto-fix |
|-------------------------------|-------------------|----------|
| `Collections.sort(list)`      | `list.sort(null)` | No       |
| `Collections.sort(list, cmp)` | `list.sort(cmp)`  | No       |
| `.stream().forEach(...)`      | `.forEach(...)`   | Yes      |

### API 30+ (MIN_SDK_COLLECTION_FACTORY)

| Pattern                          | Replacement    | Auto-fix |
|----------------------------------|----------------|----------|
| `Collections.emptyList()`        | `List.of()`    | Yes      |
| `Collections.emptySet()`         | `Set.of()`     | Yes      |
| `Collections.emptyMap()`         | `Map.of()`     | Yes      |
| `Collections.singletonList(x)`   | `List.of(x)`   | Yes      |
| `Collections.singleton(x)`       | `Set.of(x)`    | Yes      |
| `Collections.singletonMap(k, v)` | `Map.of(k, v)` | Yes      |

### API 31+ (MIN_SDK_COPY_OF)

| Pattern                                            | Replacement      | Auto-fix                      |
|----------------------------------------------------|------------------|-------------------------------|
| `Collections.unmodifiableList(x)`                  | `List.copyOf(x)` | Yes                           |
| `Collections.unmodifiableSet(x)`                   | `Set.copyOf(x)`  | Yes                           |
| `Collections.unmodifiableMap(x)`                   | `Map.copyOf(x)`  | Yes                           |
| `Collections.unmodifiableList(Arrays.asList(...))` | `List.of(...)`   | Partial (gives `List.copyOf`) |

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

## Checks without fixers

Checks intentionally left without auto-fix support because the transformation is too complex.

| Check             | Reason                                                                                                         |
|-------------------|----------------------------------------------------------------------------------------------------------------|
| FieldSortingCheck | Reordering fields/enum constants requires moving multi-line blocks with dependency analysis                    |
| PreferRecordCheck | Multi-line structural transformation: must rewrite class header, remove fields/constructor, adjust annotations |

## Future fix opportunities

Patterns not currently auto-fixable, with what would be needed to support them.

| Pattern                             | Blocker                                                                                          | Possible approach                                                                                                                              |
|-------------------------------------|--------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| 3-arg assertions                    | Comma inside string args makes boundary detection unreliable                                     | Pass the violation message to the fixer (contains the literal), or use a regex to find the literal token rather than splitting on commas       |
| `.indexOf(...) != -1`               | Need to restructure `expr op literal` into `receiver.contains(arg)` or `!receiver.contains(arg)` | Extract receiver + arg from the indexOf call text, rebuild as contains, handle negation                                                        |
| `.stream().findFirst().isPresent()` | `!` must go before the receiver, not at the `.`                                                  | Find the start of the chain (scan backwards for the receiver), insert `!` there, replace `.stream().findFirst().isPresent()` with `.isEmpty()` |
| `Collections.sort(list)`            | First arg becomes the receiver                                                                   | Find arg text between `(` and `)` or `,`, reconstruct as `arg.sort(null)` or `arg.sort(cmpArg)`                                                |
| `.get(0)` / `.remove(0)`            | Simple case is trivial but shares detection with `get(size()-1)`                                 | Split: fix `.get(0)` → `.getFirst()` and `.remove(0)` → `.removeFirst()` via text match, skip `size()-1` variants                              |
| `.size() == 0` etc.                 | Need to restructure comparison into method call                                                  | Extract receiver from size/length call, rebuild as `receiver.isEmpty()` or `!receiver.isEmpty()`, remove comparison operator and literal       |