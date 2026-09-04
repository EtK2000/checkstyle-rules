# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.
Per-check detail lives in [`docs/coverage/`](coverage/), one file per check.

## TreeWalker checks (FIXERS map)

| Check | Fixer | Notes |
| --- | --- | --- |
| AnnotationOwnLineCheck | AnnotationOwnLineFixer | Splits stacked/embedded annotations to own lines, removes blank lines, sorts alphabetically. See [sub-rules](coverage/annotationownline.md) |
| AnnotationSameLineCheck | AnnotationSameLineFixer | Joins annotations onto declaration line, sorts inline annotations alphabetically |
| ArrayTypeStyleCheck | ArrayTypeStyleFixer | Moves C-style brackets to the type position. See [sub-rules](coverage/arraytypestyle.md) |
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer | Removes the bare `super()` call from anywhere on the line (whitespace-tolerant, e.g. `super( )`; comments/strings are masked first, so a `super()` inside a comment is never matched), preserving surrounding code and comments (a compact constructor `X() { super(); }` becomes `X() { }`) and deleting the whole line when nothing else remains; skips (SkipResult) only when the `super()` call spans multiple lines |
| ConstructorAssignmentOrderCheck | ConstructorAssignmentOrderFixer | Sorts `this.xxx = ...` assignments by group (simple, multi-line, var-dependent) then alphabetically; handles dependencies. See [sub-rules](coverage/constructorassignmentorder.md) |
| ControlFlowBracesCheck | ControlFlowBracesFixer | Do-while: removes unnecessary braces, fixes one-liners, adds missing braces. Non-do-while: adds braces to multi-line braceless bodies, removes unnecessary braces, and moves one-liner bodies to their own line. See [non-do-while sub-rules](coverage/controlflowbraces.md#non-do-while-sub-rules) and [do-while sub-rules](coverage/controlflowbraces.md#do-while-sub-rules) |
| ExplicitInitializationCheck | ExplicitInitializationFixer | Removes the `= <default>` initializer (`= 0`/`= null`/`= false`/`'\0'`/etc.) from a declaration. See [sub-rules](coverage/explicitinitialization.md) |
| FieldConsolidationCheck | FieldConsolidationFixer | Merges consecutive same-type fields; wraps across lines if >120 chars. See [C-style arrays](c-style-array-fixer.md) and [limitations](coverage/fieldconsolidation.md) |
| FieldSortingCheck | FieldSortingFixer | Enum constants: sorts alphabetically, splits same-line. Fields: sorts by chunk, type (primitives first), annotations, type-arg annotations, name; handles dependencies. See [sub-rules](coverage/fieldsorting.md) |
| FinalLocalVariableCheck | FinalLocalVariableFixer | Adds `final` keyword (split declarations: inserts on the type line). See [sub-rules](coverage/finallocalvariable.md) |
| JitInefficiencyCheck | JitInefficiencyFixer | See [sub-rules](coverage/jitinefficiency.md) |
| LambdaParameterTypeCheck | LambdaParameterTypeFixer | See [sub-rules](coverage/lambdaparametertype.md) |
| MultilineCallFormattingCheck | MultilineCallFormattingFixer | Collapses a split `put(simpleValue)` call, and moves a call's closing paren and arguments (push/pull/re-stack, collapsing to one line when it fits) when it's the call's sole violation. See [sub-rules](coverage/multilinecallformatting.md) |
| NoArrayTrailingCommaCheck | NoArrayTrailingCommaFixer | Removes trailing comma |
| NoBlankLineBetweenSingleCasesCheck | NoBlankLineBetweenSingleCasesFixer | Removes blank line |
| NoEnumTrailingCommaCheck | NoArrayTrailingCommaFixer | Same fixer as array trailing comma |
| NoEnumTrailingSemicolonCheck | NoEnumTrailingSemicolonFixer | Removes trailing semicolon; deletes line if semicolon was the only content |
| NoFinalParametersCheck | RedundantModifierFixer | Removes `final` keyword from parameter. See [shared sub-rules](coverage/redundantmodifier.md) |
| NoUnnecessaryThisCheck | NoUnnecessaryThisFixer | Removes `this.` prefix |
| PreferBulkOperationCheck | PreferBulkOperationFixer | See [sub-rules](coverage/preferbulkoperation.md) |
| PreferCollectionInterfaceCheck | PreferCollectionInterfaceFixer | Replaces concrete collection type with interface (e.g. `ArrayList` to `List`). See [sub-rules](coverage/prefercollectioninterface.md) |
| PreferDirectBooleanReturnCheck | PreferDirectBooleanReturnFixer | Collapses an `if` whose body and paired return are each a single boolean return (opposite literals, same literal, or one non-literal branch via short-circuit). See [sub-rules](coverage/preferdirectbooleanreturn.md) |
| PreferDoWhileCheck | PreferDoWhileFixer | Collapses pre-loop statement + `while` into a tier-2 `do-while`. See [sub-rules](coverage/preferdowhile.md) |
| PreferExactAssertionCheck | PreferExactAssertionFixer | Converts `assertTrue/assertFalse(x instanceof Y)` to `assertInstanceOf/assertNotInstanceOf(Y.class, x)`, and `assertTrue/assertFalse(!x)` to `assertFalse/assertTrue(x)`. See [sub-rules](coverage/preferexactassertion.md) |
| PreferImportCheck | PreferImportFixer | Strips a fully-qualified type name to its simple name, only when that name provably re-binds to the same FQN from this file; never inserts an import. See [sub-rules](coverage/preferimport.md) |
| PreferLiteralSuffixCheck | PreferLiteralSuffixFixer | Removes the `(long)`/`(float)`/`(double)` cast and appends the corresponding `L`/`f`/`d` suffix to the paired integer literal (binary ops and ternary branches). See [sub-rules](coverage/preferliteralsuffix.md) |
| PreferMathMethodCheck | PreferMathMethodFixer | See [sub-rules](coverage/prefermathmethod.md) |
| PreferPrefixIncrementCheck | PreferPrefixIncrementFixer | Moves `++`/`--` to prefix position, including qualified (`h.count++`), chained (`a.b.c++`), `this`-qualified, array (`arr[i]++`), cast (`((T) o).x++`) and call-qualified (`holder().count++`) operands. Not fixed when the operand begins on an earlier line than its `++`/`--` (e.g. `h` on one line and `.count++` on the next): the operator would have to move across the line break |
| PreferSpecificApiCheck | PreferSpecificApiFixer | See [sub-rules](coverage/preferspecificapi.md) |
| PreferStandardCharsetsCheck | PreferStandardCharsetsFixer | Replaces charset string literal with `StandardCharsets.X` constant (adds import). Skips when the violation names a `String` variable, field, or parameter instead of a literal: there is no literal on the line to rewrite, and replacing the reference would mean proving every assignment to that variable is the same charset. See [sub-rules](coverage/preferstandardcharsets.md) |
| PreferStaticImportCheck | PreferStaticImportFixer | Strips `Class.` prefix from a qualified call and adds an `import static <fqcn>.<method>;` |
| PreferStaticImportConstantCheck | PreferStaticImportConstantFixer | Deletes the alias field (including any trailing same-line comment) and adds `import static <FQCN>.<CONST>;`. Surrounding blank lines collapse. See [sub-rules](coverage/preferstaticimportconstant.md) |
| PreferVarCheck | PreferVarFixer | Replaces type with `var`, moving the declared type arguments onto every diamond initializer on the line so nothing is inferred away (`List<String> l = new ArrayList<>()` -> `var l = new ArrayList<String>()`; each arm of a ternary or single-line switch initializer receives them, since any arm may supply the value). Declared arguments that are all `Object` are dropped rather than moved when every diamond is unconstrained (`List<Object> l = new ArrayList<>()` -> `var l = new ArrayList<>()`), since `var` already infers `Object` there; with a constructor argument they are moved as usual, because the diamond would otherwise infer from that argument instead. An initializer that already carries explicit type arguments (`new HashMap<String, Integer>()`) converts unchanged, since the comma between them is not a declarator separator; converts explicit array init to implicit; replaces `<Object>` with `<>` on a constructor taking no arguments (with arguments the diamond would infer from them rather than `Object`, so the explicit arguments stay). A diamond that belongs to a chain's receiver (`new Foo<>().names()`) has the declared arguments dropped rather than moved, since the value's type comes from the chain and the receiver's type parameter is unrelated to the declared type. Replaces the whole type for qualified types (`java.util.List<String> l = x;` -> `var l = x;`), and pulls a multi-line `for (...)`/`try (...)` clause onto the `(` line when the rewrite lands inside it. See [sub-rules](coverage/prefervar.md) |
| RecordFormattingCheck | RecordFormattingFixer | Brace formatting: collapses non-canonical spacing/newlines between `)` and `{`, joins `{`/`}` for empty bodies, splits `{...}` onto multiple lines for non-empty bodies. See [sub-rules](coverage/recordformatting.md) |
| RedundantAnnotationSyntaxCheck | RedundantAnnotationSyntaxFixer | Removes empty annotation parens (`@A()` -> `@A`) and a redundant `value =` key (`@A(value = x)` -> `@A(x)`), single-line and multiline. See [sub-rules](coverage/redundantannotationsyntax.md) |
| RedundantArrayCreationCheck | RedundantArrayCreationFixer | Removes `new Type[]{...}` wrapper, extracts elements directly; removes empty array with preceding comma. See [sub-rules](coverage/redundantarraycreation.md) |
| RedundantCastCheck | RedundantCastFixer | Deletes a redundant `(Type)` cast at the violation column, stripping surrounding parens for receiver/bare-cast wraps. See [sub-rules](coverage/redundantcast.md) |
| RedundantEqualityBranchCheck | RedundantEqualityBranchFixer | Collapses redundant if-else with `==`/`!=` condition. See [sub-rules](coverage/redundantequalitybranch.md) |
| RedundantImportCheck | DeleteLineFixer | Deletes import line |
| RedundantModifierCheck | RedundantModifierFixer | Removes redundant modifier keyword. See [sub-rules](coverage/redundantmodifier.md) |
| RedundantNumericSuffixCheck | RedundantNumericSuffixFixer | Removes redundant `L`/`f`/`d` suffix. Not reported on a single-variable local or `for`-init declaration whose whole initializer is the literal, optionally behind a leading `-`/`+` or parentheses (`long n = -1L` counts): that declaration becomes `var`, which binds the literal's own type, so the suffix is load-bearing there (`long l = 5L` -> `var l = 5L`, not `var l = 5`). A literal nested in an expression, a field, and a multi-variable declaration (`long a = 0L, b = 1L;`, which never becomes `var`) are all still reported |
| UnusedImportsCheck | UnusedImportsFixer | Deletes the unused import line (wildcard / `java.lang` unconditionally; others re-verified against the file body first). See [sub-rules](coverage/unusedimports.md) |
| UpperEllCheck | UpperEllFixer | Changes `l` to `L` |

## Regex checks (MODULE_ID_FIXERS map)

| Module ID | Fixer | Notes |
| --- | --- | --- |
| BlankLineAfterBreak | BlankLineAfterBreakFixer | Inserts blank line after `break;` before next `case`/`default` |
| NoBlankLineAfterClassBrace | BlankLineAfterClassBraceFixer | Removes blank lines after class/interface/enum/record `{` |
| NoBlankLineBeforeClosingBrace | BlankLineBeforeClosingBraceFixer | Removes blank lines before `}` |
| NoDoubleBlankLines | DoubleBlankLineFixer | Removes extra blank line |
| NoTrailingNewline | TrailingNewlineFixer | Removes the end-of-file newline, including a single terminator |
| NoTrailingWhitespace | TrailingWhitespaceFixer | Trims trailing whitespace |

## Regex checks without fixers

| Module ID | Reason |
| --- | --- |
| NoSpaceIndent | Converting leading spaces to tabs requires knowing the original indent width (2? 4? 8?), which is ambiguous. A wrong guess changes visual indentation. |

## Checks without fixers

Custom checks without auto-fix support and why.

| Check | Reason |
| --- | --- |
| ClassStructureOrderCheck | Reordering class members requires moving multi-line blocks with dependency analysis |
| EmptyBodyCheck | Ambiguous: removing the statement may discard intentional no-ops; adding a body requires context |
| EmptySwitchCheck | Same as EmptyBodyCheck |
| FieldSortingCheck (some dependency / anon.class / multi-var shapes) | A few unresolvable shapes (dependency cycle, anon-class field referencing a field it must precede, multi-var comment/initialized/secondary-name-dependency) return a documented `SkipResult` rather than a fix; see the FieldSorting sub-rules table |
| InfiniteEmptyLoopCheck | Flags bugs (infinite empty loops), not a stylistic issue with a deterministic fix |
| InstanceofBeforeCastCheck | Reordering sub-expressions in compound boolean conditions while preserving short-circuit semantics |
| MethodAlphabeticalOrderCheck | Reordering methods requires moving multi-line blocks |
| NoCaseBracesCheck | Removing braces requires scope analysis to verify no variable declarations leak |
| OverloadMethodOrderCheck | Reordering method overloads requires moving multi-line blocks |
| PreferExactAssertionCheck (comparison form) | No deterministic fix for `assertTrue(a > b)`-style: the exact expected value depends on domain knowledge |
| PreferImportCheck (FQN needing a new import) | `PreferImportFixer` strips an FQN only when its simple name already resolves (existing import / same-package / `java.lang`); inserting and sorting a brand-new import is unsupported. See [PreferImportCheck sub-rules](coverage/preferimport.md) |
| PreferLambdaCheck | Structural transformation: anonymous class to lambda, must handle `this` references and field shadowing |
| PreferPatternMatchingInstanceofCheck | Restructuring instanceof + subsequent cast into pattern matching across multiple statements |
| PreferRecordCheck | Multi-line structural transformation: must rewrite class header, remove fields/constructor, adjust annotations |
| SwitchCaseOrderCheck | Reordering switch cases requires moving multi-line blocks with fall-through analysis |
| ThreadAnnotationCheck | Cannot determine which thread annotation (`@MainThread`, `@AnyThread`, etc.) to add |