# C-Style Array Declarations in FieldConsolidationFixer

Java supports two syntaxes for array field declarations:

- **Java-style:** `int[] alpha;` (brackets on the type)
- **C-style:** `int alpha[];` (brackets on the name)

Both are semantically identical. `FieldConsolidationCheck` normalizes both to the same type string
(via `ARRAY_DECLARATOR` tokens in the AST) and fires when consecutive same-type fields can be
merged regardless of bracket style.

## Multi-variable declaration semantics

When multiple variables share a declaration, brackets on individual names apply only to that
variable:

| Declaration            | alpha type | beta type |
|------------------------|------------|-----------|
| `int[] alpha, beta;`   | `int[]`    | `int[]`   |
| `int alpha[], beta[];` | `int[]`    | `int[]`   |
| `int alpha[], beta;`   | `int[]`    | `int`     |
| `int[] alpha, beta[];` | `int[]`    | `int[][]` |

The last two rows show why mixing styles in a single declaration is dangerous.
The fixer avoids producing them by always emitting Java-style output.

Because the merged output is a single base type followed by bare names, every declarator on
both sides has to end up with that one type. `FieldConsolidationCheck` therefore compares the
whole declarations, not only the adjacent pair it names: it reports a pair only when **every**
declarator of both declarations has the same type. So `int alpha;` above `int zebra, beta[];`
is not reported (merging would retype `beta` from `int[]` to `int`), while
`int alpha[], beta[];` above `int gamma[];` is (all three are `int[]`, and the merge hoists
every declarator's brackets onto the base type: `int[] alpha, beta, gamma;`).

Whitespace between a name and its brackets (`int alpha [];`) is part of the declarator suffix
and is normalized away by the merge.

## Fixer behavior by style combination

The fixer always normalizes the merged output to Java-style brackets (`int[] alpha, beta;`),
regardless of how the input fields placed their brackets.

| Prev field style         | Curr field style        | Fixer action                                                  |
|--------------------------|-------------------------|---------------------------------------------------------------|
| Java (`int[] alpha;`)    | Java (`int[] beta;`)    | Merge, strip type from curr                                   |
| Java (`int[] alpha;`)    | C-style (`int beta[];`) | Merge, strip brackets from curr name                          |
| C-style (`int alpha[];`) | C-style (`int beta[];`) | Merge, rewrite prev prefix to Java-style, strip curr brackets |
| C-style (`int alpha[];`) | Java (`int[] beta;`)    | Merge, rewrite prev prefix to Java-style                      |

The prev-prefix rewrite covers a multi-declarator previous field too
(`int alpha[], beta[];` -> `int[] alpha, beta`); it refuses (`SkipResult`) when the
declarators do not all carry the same brackets, or when the declaration wraps so that its
names start the line and there is no base type on it to hoist onto.

All four combinations converge in a single fix pass; no follow-up pass is required.

## Compound arrays

The same rules apply to multidimensional arrays:

- `String[][] alpha; String[] beta[];` merges to `String[][] alpha, beta;`
- `String[] alpha[]; String[] beta[];` merges to `String[][] alpha, beta;`
- `String[] alpha[]; String[][] beta;` merges to `String[][] alpha, beta;`

## Normalization to Java-style

The fixer rewrites C-style brackets to Java-style as part of the merge. The motivations:

1. The merge produces a single declaration covering all fields, so leaving brackets on the
   first field's name (`int alpha[], beta;`) would change `beta`'s type from `int[]` to `int`.
   The fixer must either move brackets onto every name or move them to the base type. The
   latter is shorter and matches the project's other type conventions.

2. Without this normalization, the fixer could not handle the C-style-prev / Java-style-curr
   combination (`int alpha[]; int[] beta;`) — there is no way to express both fields in a
   single C-style declaration without rewriting at least one bracket position.

3. Normalization removes a class of cross-fixer interleaving bugs where `ArrayTypeStyleFixer`
   rewrites `int beta[]` to `int[] beta` mid-pass and shifts the column the
   `FieldConsolidationCheck` event reported. The fixer now relocates the identifier when
   `column` does not point at a Java identifier start.