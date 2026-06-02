# PreferStaticImportConstantCheck auto-fix coverage

The fixer deletes the field declaration line(s) and adds `import static <FQCN>.<CONST>;`.
The FQCN is resolved by trying, in priority order: (1) an explicit `import` of the simple
class, (2) a sibling `.java` file in the same directory combined with the file's `package`
declaration, (3) a single wildcard `import pkg.*;` (multiple wildcards are ambiguous and
not fixed).

## Supported (auto-fixable)

| Pattern | Fix | Notes |
| --- | --- | --- |
| Single-line alias surrounded by blanks | Delete the line + collapse one of the adjacent blanks | Same shared helper as `DeleteLineFixer` |
| Single-line alias with blank above only | Delete the line | No double-blank to collapse |
| Single-line alias with blank below only | Delete the line | No double-blank to collapse |
| Single-line alias with no surrounding blanks | Delete the line | Field is part of a tightly-packed chunk |
| Single-line alias with trailing `// comment` or `/* */` | Delete the whole line, including the comment | The comment is part of the deleted line |
| Multi-line alias (wrapped initializer) | Delete through the line with the terminating `;` at paren depth 0 | Forward scan respects strings/comments/parens |
| Multi-line `Bar\n\t\t.X` form (whitespace around the `.`) | Same as multi-line; whitespace between the class name, `.`, and member is skipped | Allows declarations broken across lines at the `.` |
| Whitespace-only blank lines (tabs/spaces) around the deletion | Adjacent blank lines are detected and collapsed | Tab-only or space-only lines still collapse |
| Class resolved via explicit `import com.foo.Foo;` | `import static com.foo.Foo.X;` | Explicit import wins over sibling and wildcard |
| Class resolved via same-package sibling `Foo.java` | `import static <package>.Foo.X;` | Probes the file's parent directory |
| Class resolved via nested-type import `import foo.Bar.Inner;` | `import static foo.Bar.Inner.X;` | Inner's simple name (`Inner`) matches the alias's class name |
| Class resolved via single wildcard `import com.foo.*;` | `import static com.foo.Foo.X;` | Used only when neither explicit import nor sibling resolves |
| Parenthesized RHS `= (Foo.X);` (any depth of parens) | Parens stripped, FQCN resolved as usual | Inner whitespace and nested parens accepted |
| Fully-qualified RHS `= com.foo.Bar.X;` | `import static com.foo.Bar.X;` | Detected by leading lowercase segment; no class resolution needed. Any chain depth is supported. |
| Nested-class RHS `= Outer.Inner.X;` | Leftmost `Outer` resolved via imports/siblings; FQCN = `<pkg>.Outer.Inner.X` | Leading uppercase segment routes through the simple-class resolver |
| Locally-renamed alias `= Foo.X;` where field name ≠ `X` | `import static <fqcn>.X;` | Local field is removed; static import brings `X` into scope |
| Pre-existing same-class static import already present | Field deleted; the existing import dedups | The local alias is just redundant |
| Qualified usage of the same constant in method bodies (`Class.X`) | Rewritten to bare `X` alongside the field deletion | Usages inside strings, char/text-block literals, and comments are left alone; only whole-word `Class.X` occurrences are rewritten. |
| Renamed local alias referenced elsewhere (`RENAMED = Foo.X`) | References to `RENAMED` rewritten to `X` alongside field deletion | References to `RENAMED` are rewritten to `X`. Not fixed when the local name is also declared as a local variable, parameter, lambda / for-each / for-loop / try-with-resources variable, or sibling field (see Not supported below). |
| Qualified or local usage BEFORE the field decl | Rewritten in place; the edit range extends backward to cover the usage | Pass-through lines between the usage and field declaration are preserved verbatim. |
| Alias field with one or more annotations on their own line(s) above, separated by a blank line (e.g. `@Deprecated` / `@SuppressWarnings("unused")`, blank, then the field) | Deletion extends up to the declaration's AST start, removing the annotation line(s) and the intervening blank along with the field; works for renamed and non-renamed aliases, with or without a body usage | Deleting the field line alone would strand a dangling annotation (invalid Java). Matches the multi-var path. An annotation on the immediately-preceding line (no blank) still skips (see Not supported). |
| Package declaration with internal whitespace (`package foo . bar;`) | Whitespace stripped before composing the FQCN | Both `package` and `import` lines have their internal whitespace stripped |
| Tab-indented alias | Deletion uses a character index after tab expansion | Tab/space agnostic |
| Split assignment to a cinit block (`private static final int X;` with `X = Foo.X;` in a sibling `static { ... }` block) | Removes both the blank-final field declaration and the cinit assignment in one edit, extending the range backward to cover leading annotations on the field. When the static block contains only the cinit assignment (and no comments), the whole `static { ... }` block is removed; otherwise only the assignment line is removed. Handles the field decl and cinit sharing a line via substring splicing. | Matches bare `X = ...`, self-qualified `EnclosingClass.X = ...`, and fully-qualified `package.EnclosingClass.X = ...` LHS forms. Multiple assignments to the same field, mismatched qualifier prefixes, and conditional assignments are not fixed (see the cinit row under "Not supported" below). |
| Multi-variable declaration (`int X = Foo.X, Y = Bar.Y;`) | Deletes EVERY convertible alias in the declaration and adds a static import for each, in one edit; each alias is rewritten to its own member name. Non-alias variables are kept in a canonical single-line declaration. When no variable is left to keep, the whole declaration goes, including any annotation or modifier lines above it. | Converges in a single pass. Converting only the reported variable rewrote the whole declaration, which invalidated the coordinates the sibling violations were reported at: their fixes were dropped and their imports lost. Multi-line declarations are consolidated to a single line, preserving the modifiers/type prefix (including annotations and in-prefix block comments) and each kept variable's RHS verbatim. |
| Multi-variable declaration whose siblings alias the same member name on different classes (`int A = Foo.MAX, B = Bar.MAX;`) | Not fixed (skips): both aliases would need a static import of the same simple name |
| Multi-variable declaration sibling whose class cannot be resolved, or whose static import would conflict with an existing one | That sibling is kept in the rebuilt declaration and contributes no static import; the resolvable, non-conflicting aliases are still converted |

## Not supported

| Pattern | Reason |
| --- | --- |
| Previous line is an annotation (`@...`), Javadoc opener (`/**`), or `//` line comment | A line-only delete would orphan the comment or annotation |
| Previous line is a Javadoc continuation (`*` with no `;` or `=` punctuation) | The `*` form is treated as Javadoc only when the line has no statement-like punctuation; lines like `* 2;` (a binary-multiply continuation) or text-block tails are not skipped |
| Single-var alias whose `private`/other modifiers are split onto their own line above the field name (e.g. `private` ⏎ `static final int X = Foo.X;`, or `private static final` ⏎ `int X = Foo.X;`) | The visibility/alias classification reads only the field name's own stitched line, not the split-off modifier line, so the field is skipped before the deletion runs (reported as non-private, or routed to the multi-var path and skipped there). Split modifiers are vanishingly rare in practice; only annotations, not bare modifiers, are stitched from the AST declaration start. |
| Alias field's declaration begins inside a straddling block comment (a `/*` opened above closes on the annotation/modifier line the deletion would start from) | Deleting from there would strand the unterminated `/*` opener on a prior line; the fixer skips rather than corrupt the source (both single-var and multi-var paths) |
| Declaration's terminating `;` not found before EOF | No terminating `;` found |
| RHS doesn't start with a Java identifier (literal, operator) | Not a `Class.IDENT` shape |
| RHS first identifier not followed by `.` (bare identifier) | Not a `Class.IDENT` shape |
| RHS continues with `+`/`-`/etc. instead of `;` after the member name | Another fixer introduced an operator |
| FQCN cannot be resolved (no import, no sibling, no usable wildcard) | The FQCN can't be determined and must not be guessed |
| Multiple wildcard imports present (ambiguous fallback) | Picking one could produce a non-compiling FQCN |
| `import static ...` lines | Ignored during class resolution (they don't bring the class's simple name into scope) |
| Multi-statement line: alias `;` followed by another statement on the same line | Deleting the line would silently destroy the trailing statement |
| File path the platform rejects as invalid | Sibling resolution is skipped; falls through to wildcard resolution, else not fixed |
| Non-private alias (`public`, `protected`, or package-private) | Deleting an externally-visible alias may break callers in other compilation units |
| Split assignment to a cinit block (fixer cannot safely produce a fix) | The fixer can't safely locate and remove both halves. It bails when: the violation column isn't on an identifier; the file won't re-parse; there's no matching field declaration at the violation line; the field isn't a class/enum member; there's no cinit assignment to the field in a sibling static block, or more than one; the LHS qualifier doesn't match the enclosing type (or uses a wrong package prefix); or the cinit assignment spans multiple lines, shares a line with other statements, or isn't the expected `X` / `EnclosingType.X` / `package.EnclosingType.X` alias. The check only fires on a single direct top-level assignment with a dot-chain RHS, so most of these guard against mid-pass corruption by another fixer. |
| Split assignment to a cinit block where the field is not `private` | Same as the single-line case: deleting an externally-visible alias may break callers in other compilation units |
| Split assignment to a cinit block where the FQCN cannot be resolved | Same as the single-line case: the FQCN can't be determined and must not be guessed |
| Multi-variable declaration where any decl line contains a `//` line comment | Line comments break the stitching (newlines are collapsed to spaces, so `//` would extend through the rest of the declaration). The match is a substring check, so `//` inside a string literal (e.g. a URL like `"https://example.com"`) also triggers the bail. Remove the line comment (or split the multi-var) and re-run. |
| Renamed alias whose local name shadows another declaration | Detection distinguishes a local variable, method/constructor/lambda/catch parameter, for-each/for-loop/try-with-resources variable, or a sibling field with the same name. If the file fails to parse mid-fix, it bails conservatively. |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| Non-`static` or non-`final` field | Not a constant: semantics differ from the original |
| RHS with manipulation (`Foo.X + 1`, `(int) Foo.X`, `-Foo.X`, etc.) | Not a pure alias: value diverges from the source constant |
| RHS method call (`Foo.getX()`) or array access (`Foo.ARR[0]`) | Not a `Class.member` reference |
| RHS bare identifier (`X = X;`) referring to a same-class constant | Not a cross-class alias; static import wouldn't apply |
| Alias whose static-import replacement would conflict with an existing `import static <other>.<member>;` (a different class, same member) — including the split-cinit form | Converting is impossible (a duplicate-member static import would not compile), so flagging it would suggest an impossible fix. A same-class existing static import, a static wildcard (`import static P.*;`), or an indeterminate FQCN (two or more wildcard imports) is not a conflict and still fires. In a multi-variable declaration only the conflicting variable is left unflagged; a non-conflicting sibling still fires and converts, keeping the conflicting one. |
| Simple class is unresolvable (not imported, no sibling) | Can't construct a valid FQCN; user must manually add the import or fix the receiver |
| Field has `@SuppressWarnings("PreferStaticImportConstant")` | Explicit opt-out |
| Enclosing type has `@SuppressWarnings("PreferStaticImportConstant")` | Explicit opt-out at type level |
| Field is in an interface or annotation type body without explicit `static final` modifiers | The check requires explicit `static` and `final` tokens; implicit modifiers don't trigger |
| Field is a local variable, for-init variable, or parameter | Not a class/enum member; the field-context guard skips them |
| Conditional assignment in static initializer (`if (foo) X = A.X;`) | The cinit-detection only recognizes a single direct assignment at the top of one static block; conditional or split-across-blocks assignments are deliberately skipped |
| Same field assigned twice (in one or across static blocks) | More than one matching top-level assignment makes which value "wins" non-obvious; the check skips both |
| Cinit RHS is a literal, method call, or non-dotted expression | RHS shape is checked the same way as inline init; non-dotted shapes never fire |

Part of [auto-fix coverage](../auto-fix-coverage.md).