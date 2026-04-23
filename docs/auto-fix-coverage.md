# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.

## TreeWalker checks (FIXERS map)

| Check                                    | Fixer                              | Notes                                                                                                                                                                       |
|------------------------------------------|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AnnotationOwnLineCheck                   | AnnotationOwnLineFixer             | Splits stacked/embedded annotations to own lines, removes blank lines, sorts alphabetically                                                                                 |
| AnnotationSameLineCheck                  | AnnotationSameLineFixer            | Joins annotations onto declaration line, sorts inline annotations alphabetically                                                                                            |
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer      | Removes `super()` call                                                                                                                                                      |
| ConstructorAssignmentOrderCheck          | ConstructorAssignmentOrderFixer    | Sorts `this.xxx = ...` assignments by group (simple, multi-line, var-dependent) then alphabetically; handles dependencies                                                   |
| ControlFlowBracesCheck                   | ControlFlowBracesFixer             | Do-while: removes unnecessary braces, fixes one-liners, adds missing braces. Non-do-while: adds braces to multi-line braceless bodies                                       |
| ExplicitInitializationCheck              | ExplicitInitializationFixer        | Removes `= 0`/`= null`/`= false` etc.                                                                                                                                       |
| FieldConsolidationCheck                  | FieldConsolidationFixer            | Merges consecutive same-type fields; wraps across lines if >120 chars. See [C-style arrays](c-style-array-fixer.md) and [limitations](#fieldconsolidationfixer-limitations) |
| FieldSortingCheck                        | FieldSortingFixer                  | Enum constants: sorts alphabetically, splits same-line. Fields: sorts by chunk, type (primitives first), name; handles dependencies                                         |
| FinalLocalVariableCheck                  | FinalLocalVariableFixer            | Adds `final` keyword                                                                                                                                                        |
| LambdaParameterTypeCheck                 | LambdaParameterTypeFixer           | See sub-rules below                                                                                                                                                         |
| NoArrayTrailingCommaCheck                | NoArrayTrailingCommaFixer          | Removes trailing comma                                                                                                                                                      |
| NoBlankLineBetweenSingleCasesCheck       | NoBlankLineBetweenSingleCasesFixer | Removes blank line                                                                                                                                                          |
| NoEnumTrailingCommaCheck                 | NoArrayTrailingCommaFixer          | Same fixer as array trailing comma                                                                                                                                          |
| NoEnumTrailingSemicolonCheck             | NoEnumTrailingSemicolonFixer       | Removes trailing semicolon; deletes line if semicolon was the only content                                                                                                  |
| NoFinalParametersCheck                   | RedundantModifierFixer             | Removes `final` keyword from parameter                                                                                                                                      |
| NoUnnecessaryThisCheck                   | NoUnnecessaryThisFixer             | Removes `this.` prefix                                                                                                                                                      |
| PreferBulkOperationCheck                 | PreferBulkOperationFixer           | See sub-rules below                                                                                                                                                         |
| PreferCollectionInterfaceCheck           | PreferCollectionInterfaceFixer     | Replaces concrete collection type with interface (e.g. `ArrayList` to `List`)                                                                                               |
| PreferMathMethodCheck                    | PreferMathMethodFixer              | See sub-rules below                                                                                                                                                         |
| PreferPrefixIncrementCheck               | PreferPrefixIncrementFixer         | Moves `++`/`--` to prefix position                                                                                                                                          |
| PreferSpecificApiCheck                   | PreferSpecificApiFixer             | See sub-rules below                                                                                                                                                         |
| PreferStandardCharsetsCheck              | PreferStandardCharsetsFixer        | Replaces charset string literal with `StandardCharsets.X` constant (adds import)                                                                                            |
| PreferStaticImportCheck                  | PreferStaticImportFixer            | Strips `Class.` prefix from a qualified call and adds an `import static <fqcn>.<method>;`                                                                                   |
| PreferVarCheck                           | PreferVarFixer                     | Replaces type with `var`; converts explicit array init to implicit; replaces `<Object>` with `<>`                                                                           |
| RedundantAnnotationSyntaxCheck           | RedundantAnnotationSyntaxFixer     | Removes `()` or `value =`                                                                                                                                                   |
| RedundantImportCheck                     | DeleteLineFixer                    | Deletes import line                                                                                                                                                         |
| RedundantModifierCheck                   | RedundantModifierFixer             | Removes redundant modifier keyword                                                                                                                                          |
| RedundantNumericSuffixCheck              | RedundantNumericSuffixFixer        | Removes redundant `L`/`f`/`d` suffix                                                                                                                                        |
| UnusedImportsCheck                       | DeleteLineFixer                    | Deletes import line                                                                                                                                                         |
| UpperEllCheck                            | UpperEllFixer                      | Changes `l` to `L`                                                                                                                                                          |

## Regex checks (MODULE_ID_FIXERS map)

| Module ID                     | Fixer                            | Notes                                                          |
|-------------------------------|----------------------------------|----------------------------------------------------------------|
| BlankLineAfterBreak           | BlankLineAfterBreakFixer         | Inserts blank line after `break;` before next `case`/`default` |
| NoBlankLineAfterClassBrace    | BlankLineAfterClassBraceFixer    | Removes blank lines after class/interface/enum/record `{`      |
| NoBlankLineBeforeClosingBrace | BlankLineBeforeClosingBraceFixer | Removes blank lines before `}`                                 |
| NoDoubleBlankLines            | DoubleBlankLineFixer             | Removes extra blank line                                       |
| NoTrailingNewline             | TrailingNewlineFixer             | Removes trailing blank lines at EOF                            |
| NoTrailingWhitespace          | TrailingWhitespaceFixer          | Trims trailing whitespace                                      |

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

| Violation type              | Input pattern                                      | Auto-fix | Notes                                                   |
|-----------------------------|----------------------------------------------------|----------|---------------------------------------------------------|
| Chunk order                 | Non-final before final-with-value                  | Yes      | Adds blank lines between chunks                         |
| Type order (prim vs ref)    | `String name` before `int count`                   | Yes      | Primitives sort before reference types                  |
| Type order (alphabetical)   | `String` before `int` (same chunk)                 | Yes      | Alphabetical by base type name                          |
| Array depth                 | `int[]` before `int`                               | Yes      | Base type first, then arrays                            |
| Annotation order            | `@Nullable String` before `@NonNull String`        | Yes      | Unannotated first, then by canonical annotation key     |
| Annotation consolidation    | Same-type same-annotation fields on separate lines | Yes      | Merges into single declaration after sorting            |
| Name order                  | `int z` before `int a` (same type)                 | Yes      | Case-insensitive alphabetical                           |
| Field dependencies          | `B = A + 1` before `A = 0`                         | Yes      | Respects dependency: A before B if B uses A             |
| Multi-line initializers     | Fields with anonymous class or lambda init         | Yes      | Tracks brace/paren depth for field end                  |
| Annotated fields            | Fields with `@Deprecated` etc. above               | Yes      | Annotation lines move with their field                  |
| Circular dependencies       | `A = B + 1; B = A + 1`                             | No       | Max-iteration guard stops the loop; best-effort order   |
| Unparseable field pattern   | Complex generics, multi-variable declarations      | No       | Returns null if FIELD_PATTERN doesn't match             |
| Anonymous class initializer | anon.class field must come before non-anon         | No       | Not implemented in fixer sorting                        |
| Text blocks in initializers | Field with `"""` initializer containing `{}`       | No       | String parser doesn't handle text blocks                |
| Nested generics in type     | `Map<String, List<Integer>>` field                 | No       | `[^>]*` in FIELD_PATTERN stops at first `>`             |
| Inline annotation with `()` | `@SuppressWarnings(")")` in field value            | No       | Annotation parser doesn't track string literals in args |

## Regex checks without fixers

| Module ID     | Reason                                                                                            |
|---------------|---------------------------------------------------------------------------------------------------|
| NoSpaceIndent | Converting leading spaces to tabs requires knowing the original indent width (2? 4? 8?), which is |
|               | ambiguous. A wrong guess changes visual indentation. See `docs/regex-fixer-edge-cases.md`.        |

## Checks without fixers

Custom checks without auto-fix support and why.

| Check                                      | Reason                                                                                                         |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| ClassStructureOrderCheck                   | Reordering class members requires moving multi-line blocks with dependency analysis                            |
| ControlFlowBracesCheck (one-liner)         | Body on same line as keyword; moving to own line requires re-indentation context                               |
| EmptyBodyCheck                             | Ambiguous: removing the statement may discard intentional no-ops; adding a body requires context               |
| EmptySwitchCheck                           | Same as EmptyBodyCheck                                                                                         |
| FieldSortingCheck (dependency, anon.class) | Dependency and anonymous class ordering violations may return null if the fixer can't parse the pattern        |
| InfiniteEmptyLoopCheck                     | Flags bugs (infinite empty loops), not a stylistic issue with a deterministic fix                              |
| InstanceofBeforeCastCheck                  | Reordering sub-expressions in compound boolean conditions while preserving short-circuit semantics             |
| MethodAlphabeticalOrderCheck               | Reordering methods requires moving multi-line blocks                                                           |
| MultilineCallFormattingCheck               | Reformatting argument layout across lines with context-dependent indent and grouping rules                     |
| NoCaseBracesCheck                          | Removing braces requires scope analysis to verify no variable declarations leak                                |
| OverloadMethodOrderCheck                   | Reordering method overloads requires moving multi-line blocks                                                  |
| PreferExactAssertionCheck                  | No deterministic fix: the exact expected value depends on domain knowledge the tool cannot infer               |
| PreferImportCheck                          | Replacing FQN with short name and adding import; must verify no name conflicts                                 |
| PreferLambdaCheck                          | Structural transformation: anonymous class to lambda, must handle `this` references and field shadowing        |
| PreferLiteralSuffixCheck                   | Replacing widening cast with literal suffix requires expression context analysis                               |
| PreferPatternMatchingInstanceofCheck       | Restructuring instanceof + subsequent cast into pattern matching across multiple statements                    |
| PreferRecordCheck                          | Multi-line structural transformation: must rewrite class header, remove fields/constructor, adjust annotations |
| RedundantCastCheck                         | Removing a cast may change method overload resolution or widen the expression type                             |
| SwitchCaseOrderCheck                       | Reordering switch cases requires moving multi-line blocks with fall-through analysis                           |
| ThreadAnnotationCheck                      | Cannot determine which thread annotation (`@MainThread`, `@AnyThread`, etc.) to add                            |

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

| Fixer                          | Skipped case                                       | Reason                                                                                 |
|--------------------------------|----------------------------------------------------|----------------------------------------------------------------------------------------|
| RedundantAnnotationSyntaxFixer | Multiline annotation with `()` or `value =`        | Skip: regex can't reliably detect annotation boundary across lines                     |
| PreferVarFixer                 | Multi-variable declaration (`int a, b;`)           | Skip: can't replace type with `var` when multiple variables share the declaration      |
| PreferVarFixer                 | No `new` after `=` in explicit array init          | null: pattern requires `= new Type[]{}` structure                                      |
| PreferVarFixer                 | Non-Object generic type args (`<String>`)          | null: diamond `<>` only replaces `<Object>`, not other explicit types                  |
| PreferCollectionInterfaceFixer | Class not resolvable or not a standard collection  | Skip: concrete-to-interface mapping requires class resolution at runtime               |
| LambdaParameterTypeFixer       | Arrow `->` not found from violation column         | Skip: fixer operates on text from the column; if arrow is on a different line, skipped |
| LambdaParameterTypeFixer       | Opening paren not found for lambda params          | Skip: single naked param without parens in unusual positions                           |
| FieldConsolidationFixer        | Block comment between field names                  | null: comment would be lost or misplaced during merge                                  |
| FieldConsolidationFixer        | C-style array type mismatch between fields         | null: `int[] a` and `int b` can't merge to one declaration safely                      |
| AnnotationOwnLineFixer         | Annotation already on own line, just needs sorting | null when already in correct order                                                     |
| AnnotationSameLineFixer        | Annotation block reaches end of file               | null: no declaration found to join annotations onto                                    |

## Future fix opportunities

Patterns not currently auto-fixable, with what would be needed to support them.

| Pattern                                                   | Blocker                                                                                                       | Possible approach                                                                       |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `.indexOf(...) != -1`                                     | Need to restructure `expr op literal` into `receiver.contains(arg)` or `!receiver.contains(arg)`              | Extract receiver + arg from the indexOf call text, rebuild as contains, handle negation |
| `.get(size() - 1)` / `.remove(size() - 1)`                | Must verify the `size()` receiver matches the `.get()`/`.remove()` receiver                                   | Parse both receivers and compare, or pass violation metadata to the fixer               |
| `.stream().findFirst().isPresent()` (complex receivers)   | Simple receivers (identifiers, dotted names) are already fixed; method calls, casts, and array access are not | Extend `findReceiverStart()` to handle parenthesized expressions and method calls       |
| `.size() != 0` / `.length() > 0` etc. (complex receivers) | Positive forms (`== 0`) are fully fixed; negated forms need `!` insertion which requires simple receiver scan | Extend `findReceiverStart()` to handle complex receivers                                |