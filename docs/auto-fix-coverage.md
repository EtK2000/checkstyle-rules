# Auto-Fix Coverage

Which checks and sub-rules have auto-fix support via `checkstyleFix`/`checkstyleFixTest`.
Per-check detail lives in [`docs/coverage/`](coverage/), one file per check.

## TreeWalker checks

| Check | Fixer | Notes |
| --- | --- | --- |
| AnnotationOwnLineCheck | AnnotationOwnLineFixer | Splits stacked/embedded annotations to own lines, removes blank lines, sorts alphabetically. See [sub-rules](coverage/annotationownline.md) |
| AnnotationSameLineCheck | AnnotationSameLineFixer | Joins annotations onto the declaration line, sorts inline annotations alphabetically, folding an annotation written across lines and carrying any comment in between. See [sub-rules](coverage/annotationsameline.md) |
| ArrayTypeStyleCheck | ArrayTypeStyleFixer | Moves C-style brackets to the type position, for a multi-variable declaration only when every declarator has the same depth. See [sub-rules](coverage/arraytypestyle.md) |
| AvoidNoArgumentSuperConstructorCallCheck | AvoidNoArgumentSuperCallFixer | Removes the bare `super()` call from anywhere on the line (whitespace-tolerant, e.g. `super( )`; a `super()` inside a comment or string is never matched), preserving surrounding code and comments (a compact constructor `X() { super(); }` becomes `X() { }`) and deleting the whole line when nothing else remains; skips only when the `super()` call spans multiple lines |
| BraceOwnLineCheck | BraceOwnLineFixer | Moves an opening brace that sits on its own line up onto its header (control statements and declarations), carrying comments and re-indenting a GNU-style block. See [coverage](coverage/braceownline.md) |
| ConstructorAssignmentOrderCheck | ConstructorAssignmentOrderFixer | Sorts `this.xxx = ...` assignments by group (simple, multi-line, var-dependent) then alphabetically; handles dependencies. See [sub-rules](coverage/constructorassignmentorder.md) |
| ControlFlowBracesCheck | ControlFlowBracesFixer | Do-while: removes unnecessary braces, fixes one-liners, adds missing braces. Non-do-while: adds braces to multi-line braceless bodies, removes unnecessary braces, moves one-liner bodies to their own line, and merges an `else` / `if` split across lines into `else if`. See [non-do-while sub-rules](coverage/controlflowbraces.md#non-do-while-sub-rules) and [do-while sub-rules](coverage/controlflowbraces.md#do-while-sub-rules) |
| EmptyBodyCheck | EmptyBodyFixer | Deletes the empty construct, hoisting an `if` condition's side effects out as statements in its place; a stray `;` is cut out of its line, leaving anything that shared it. See [sub-rules](coverage/emptybody.md) |
| ExplicitInitializationCheck | ExplicitInitializationFixer | Removes the `= <default>` initializer (`= 0`/`= null`/`= false`/`'\0'`/etc.) from a declaration. See [sub-rules](coverage/explicitinitialization.md) |
| FieldConsolidationCheck | FieldConsolidationFixer | Merges each run of same-key fields in a stretch of fields into its first declaration, names in FieldSorting order, moving the other declarations' comments above it, and joining a declarator that spans lines onto one; wraps after a comma if >120 chars. Fields with a long initializer are left on their own, and constants are left as written. A run with a comment switching a tool off or on, or counting lines, among or over its lines is left as written. See [C-style arrays](c-style-array-fixer.md) and [limitations](coverage/fieldconsolidation.md) |
| FieldSortingCheck | FieldSortingFixer | Enum constants: splits same-line, in the order written; their order is reported as a warning and never auto-fixed. Fields: sorts by chunk, type (primitives first), annotations, type-arg annotations, name; handles dependencies. See [sub-rules](coverage/fieldsorting.md) |
| FinalLocalVariableCheck | FinalLocalVariableFixer | Adds `final` keyword (split declarations: inserts on the type line). See [sub-rules](coverage/finallocalvariable.md) |
| JitInefficiencyCheck | JitInefficiencyFixer | See [sub-rules](coverage/jitinefficiency.md) |
| LambdaParameterTypeCheck | LambdaParameterTypeFixer | See [sub-rules](coverage/lambdaparametertype.md) |
| MultilineCallFormattingCheck | MultilineCallFormattingFixer | Collapses a split `put(simpleValue)` call, and moves a call's closing paren and arguments (push/pull/re-stack, collapsing to one line when it fits) when it's the call's sole violation, and re-emits the whole argument list when the call has both an opening-paren and a closing-paren violation. See [sub-rules](coverage/multilinecallformatting.md) |
| NoArrayTrailingCommaCheck | NoArrayTrailingCommaFixer | Removes trailing comma |
| NoBlankLineBetweenSingleCasesCheck | NoBlankLineBetweenSingleCasesFixer | Removes the blank lines separating the reported case from the one above it. Comments in that gap are kept; only blank lines go |
| NoEnumTrailingCommaCheck | NoArrayTrailingCommaFixer | Same fixer as array trailing comma |
| NoEnumTrailingSemicolonCheck | NoEnumTrailingSemicolonFixer | Removes trailing semicolon; deletes line if semicolon was the only content |
| NoFinalParametersCheck | RedundantModifierFixer | Removes `final` keyword from parameter. See [shared sub-rules](coverage/redundantmodifier.md) |
| NoUnnecessaryThisCheck | NoUnnecessaryThisFixer | Removes the `this.` qualifier, including whitespace or a comment written between `this` and the member, and a qualifier whose dot falls on a later line. See [sub-rules](coverage/nounnecessarythis.md) |
| PreferBulkOperationCheck | PreferBulkOperationFixer | See [sub-rules](coverage/preferbulkoperation.md) |
| PreferCollectionInterfaceCheck | PreferCollectionInterfaceFixer | Replaces concrete collection type with interface (e.g. `ArrayList` to `List`). See [sub-rules](coverage/prefercollectioninterface.md) |
| PreferDirectBooleanReturnCheck | PreferDirectBooleanReturnFixer | Collapses an `if` whose body and paired return are each a single boolean return (opposite literals, same literal, or one non-literal branch via short-circuit). See [sub-rules](coverage/preferdirectbooleanreturn.md) |
| PreferDoWhileCheck | PreferDoWhileFixer | Collapses pre-loop statement + `while` into a `do-while`, in whichever of the two forms the body calls for. See [sub-rules](coverage/preferdowhile.md) |
| PreferExactAssertionCheck | PreferExactAssertionFixer | Converts `assertTrue/assertFalse(x instanceof Y)` to `assertInstanceOf/assertNotInstanceOf(Y.class, x)`, and `assertTrue/assertFalse(!x)` to `assertFalse/assertTrue(x)`. See [sub-rules](coverage/preferexactassertion.md) |
| PreferImportCheck | PreferImportFixer | Strips a fully-qualified type name to its simple name, only when that name provably re-binds to the same FQN from this file; never inserts an import. A qualifier a same-named variable would take over (`java.nio.charset.Charset.defaultCharset()` with a local `Charset`) is not reported. See [sub-rules](coverage/preferimport.md) |
| PreferLiteralSuffixCheck | PreferLiteralSuffixFixer | Removes the `(long)`/`(float)`/`(double)` cast and appends the corresponding `L`/`f`/`d` suffix to the paired integer literal (binary ops and ternary branches). See [sub-rules](coverage/preferliteralsuffix.md) |
| PreferMathMethodCheck | PreferMathMethodFixer | See [sub-rules](coverage/prefermathmethod.md) |
| PreferPrefixIncrementCheck | PreferPrefixIncrementFixer | Moves `++`/`--` to prefix position, including qualified (`h.count++`), chained (`a.b.c++`), `this`-qualified, array (`arr[i]++`), cast (`((T) o).x++`) and call-qualified (`holder().count++`) operands. Not fixed when the operand begins on an earlier line than its `++`/`--` (e.g. `h` on one line and `.count++` on the next): the operator would have to move across the line break |
| PreferSpecificApiCheck | PreferSpecificApiFixer | See [sub-rules](coverage/preferspecificapi.md) |
| PreferStandardCharsetsCheck | PreferStandardCharsetsFixer | Replaces a charset string literal passed to a `Charset`-overloaded parameter with the `StandardCharsets.X` constant, and a one-argument `Charset.forName("name")` call with the constant (adds import). Skips when the violation names a `String` variable, field, or parameter instead of a literal: there is no literal on the line to rewrite, and replacing the reference would mean proving every assignment to that variable is the same charset. Skips a `Charset.forName` call spanning lines, holding a comment, or standing alone as a statement or lambda body, and a literal inside a `try` whose catch names `UnsupportedEncodingException` or `IOException`. A literal nothing proves a `Charset` overload accepts, whose `Charset` overload throws a checked exception the `String` one does not, or where `Charset`/`StandardCharsets` would bind to another declaration, is only a warning, never fixed. See [sub-rules](coverage/preferstandardcharsets.md) |
| PreferStaticImportCheck | PreferStaticImportFixer | Replaces the class name and whatever separates it from the method name, and adds an `import static <fqcn>.<method>;`. See [sub-rules](coverage/preferstaticimport.md) |
| PreferStaticImportConstantCheck | PreferStaticImportConstantFixer | Deletes the alias field (including any trailing same-line comment) and adds `import static <FQCN>.<CONST>;`. Surrounding blank lines collapse. See [sub-rules](coverage/preferstaticimportconstant.md) |
| PreferVarCheck | PreferVarFixer | Replaces type with `var`, moving the declared type arguments onto every diamond initializer on the line so nothing is inferred away (`List<String> l = new ArrayList<>()` -> `var l = new ArrayList<String>()`; each arm of a ternary or single-line switch initializer receives them, since any arm may supply the value). Declared arguments that are all `Object` are dropped rather than moved when every diamond is unconstrained (`List<Object> l = new ArrayList<>()` -> `var l = new ArrayList<>()`), since `var` already infers `Object` there; with a constructor argument they are moved as usual, because the diamond would otherwise infer from that argument instead. An initializer that already carries explicit type arguments (`new HashMap<String, Integer>()`) converts unchanged; converts explicit array init to implicit; replaces `<Object>` with `<>` on a constructor taking no arguments (with arguments the diamond would infer from them rather than `Object`, so the explicit arguments stay). A diamond that belongs to a chain's receiver (`new Foo<>().names()`) has the declared arguments dropped rather than moved, since the value's type comes from the chain and the receiver's type parameter is unrelated to the declared type. Replaces the whole type for qualified types (`java.util.List<String> l = x;` -> `var l = x;`), and pulls a multi-line `for (...)`/`try (...)` clause onto the `(` line when the rewrite lands inside it. See [sub-rules](coverage/prefervar.md) |
| RecordFormattingCheck | RecordFormattingFixer | Brace formatting: collapses non-canonical spacing/newlines between `)` and `{`, joins `{`/`}` for empty bodies, splits `{...}` onto multiple lines for non-empty bodies. See [sub-rules](coverage/recordformatting.md) |
| RedundantAnnotationSyntaxCheck | RedundantAnnotationSyntaxFixer | Removes empty annotation parens (`@A()` -> `@A`) and a redundant `value =` key (`@A(value = x)` -> `@A(x)`), single-line and multiline, for a name spelled any way the language allows (qualified, split across lines, or with whitespace or a comment after the `@`). See [sub-rules](coverage/redundantannotationsyntax.md) |
| RedundantArrayCreationCheck | RedundantArrayCreationFixer | Removes `new Type[]{...}` wrapper, extracts elements directly; removes empty array with preceding comma; folds an initializer written across lines. See [sub-rules](coverage/redundantarraycreation.md) |
| RedundantCastCheck | RedundantCastFixer | Deletes a redundant `(Type)` cast at the violation column, along with the grouping parentheses the check pairs with it. See [sub-rules](coverage/redundantcast.md) |
| RedundantEqualityBranchCheck | RedundantEqualityBranchFixer | Collapses redundant if-else with `==`/`!=` condition. See [sub-rules](coverage/redundantequalitybranch.md) |
| RedundantImportCheck | DeleteLineFixer | Deletes import line |
| RedundantModifierCheck | RedundantModifierFixer | Removes redundant modifier keyword. See [sub-rules](coverage/redundantmodifier.md) |
| RedundantNumericSuffixCheck | RedundantNumericSuffixFixer | Removes a redundant `L`/`l`/`f`/`F`/`d`/`D` suffix, including after a signed exponent (`1e+2d` -> `1e+2`, `0x1p-3d` -> `0x1p-3`). Reported only where removal keeps both type and value: `d` on a decimal or exponent literal anywhere, and otherwise an `int`-valued literal whose target is a proven primitive (primitive declaration, assignment, wide-enough compound assignment, primitive cast or return, primitive array element, annotation element). Not reported for a boxed or reference target (`Long`, `Object`, `Number`, `Comparable<Long>`, qualified or not), an argument, a lambda body, a comparison or arithmetic operand, a target the file cannot prove, a compound assignment narrower than the literal (`int += 1L`, `int += 1f`), an `f` arm of a conditional or switch for a non-`float` target, `f`/`d` past `int` range, with an octal-looking mantissa, on a negative zero, or `f` on a value `float` cannot hold exactly. Not reported on a single-variable local or `for`-init declaration whose whole initializer is the literal, optionally behind a leading `-`/`+`/`~` or parentheses (`long n = -1L` counts): that declaration becomes `var`, which binds the literal's own type, so the suffix is load-bearing there (`long l = 5L` -> `var l = 5L`, not `var l = 5`). A multi-variable declaration (`long a = 0L, b = 1L;`, which never becomes `var`) is still reported |
| UnusedImportsCheck | UnusedImportsFixer | Deletes the unused import line (wildcard / `java.lang` unconditionally; others only once the file body is confirmed not to use them). See [sub-rules](coverage/unusedimports.md) |
| UpperEllCheck | UpperEllFixer | Changes `l` to `L` |

## Regex checks

| Module ID | Fixer | Notes |
| --- | --- | --- |
| BlankLineAfterBreak | BlankLineAfterBreakFixer | Inserts blank line after `break;` before next `case`/`default`. Refuses when the insertion point is text block content |
| NoBlankLineAfterClassBrace | BlankLineAfterClassBraceFixer | Removes blank lines after class/interface/enum/record `{`. Refuses when the brace or the blank run is text block content |
| NoBlankLineBeforeClosingBrace | BlankLineBeforeClosingBraceFixer | Removes blank lines before `}`. Refuses when the blank run is text block content |
| NoDoubleBlankLines | DoubleBlankLineFixer | Removes extra blank line. Refuses when the blank run is text block content |
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
| EmptyBodyCheck (a few shapes) | Most empty constructs are fixed; the shapes that are not (comment inside the construct, an `else` branch to re-attach, a loop header or short-circuited condition whose side effects cannot be hoisted, a construct that is its enclosing clause's whole body, an `else if` whose condition changes state, an `else` whose removal would let a later `else` bind to a nearer `if`) are each skipped with a documented reason. See [EmptyBodyCheck sub-rules](coverage/emptybody.md) |
| EmptySwitchCheck | Replacing `switch (expr) {}` with `expr;` is only equivalent when `expr` is side-effect-free, and when it is, the whole statement should go; deciding between the two needs the author's intent |
| FieldSortingCheck (enum constant order) | Reported as a warning and never auto-fixed: code anywhere, in this file or another, may depend on the declared order (a persisted or compared ordinal, a natural-order sort, an `EnumSet` range), so only a person can decide to change it |
| FieldSortingCheck (some dependency / anon.class / multi-var / shared-line shapes) | A few unresolvable shapes (dependency cycle, dependency on a field in a later chunk, multi-var comment/initialized/C-style-array/secondary-name-dependency/secondary-name-interleaved, multi-var mixing anonymous-class and plain initializers, multi-var mixing anonymous-class readers and free fields, multi-var names on a line holding a tool directive, a field sharing a line with other code, same-group fields separated by another member, a tool directive set apart from the field it leads, a comment switching a tool off or on or counting lines among the fields, a rewrite whose re-parse does not reproduce the members, comments and surrounding code) are skipped with a documented reason rather than fixed; see the FieldSorting sub-rules table |
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