# C-Style Array Declarations in FieldConsolidationFixer

Java supports two syntaxes for array field declarations:

- **Java-style:** `int[] alpha;` (brackets on the type)
- **C-style:** `int alpha[];` (brackets on the name)

Both are semantically identical. `FieldConsolidationCheck` normalizes both to the same type string
(via `ARRAY_DECLARATOR` tokens in the AST) and fires when same-type fields of one stretch of
fields can be merged regardless of bracket style.

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

| Prev field style         | Curr field style        | Fixer action                                                  |
|--------------------------|-------------------------|---------------------------------------------------------------|
| Java (`int[] alpha;`)    | Java (`int[] beta;`)    | Merge, strip type from curr                                   |
| Java (`int[] alpha;`)    | C-style (`int beta[];`) | Merge, strip brackets from curr name                          |
| C-style (`int alpha[];`) | C-style (`int beta[];`) | Merge, rewrite prev prefix to Java-style, strip curr brackets |
| C-style (`int alpha[];`) | Java (`int[] beta;`)    | Merge, rewrite prev prefix to Java-style                      |

The prev-prefix rewrite covers a multi-declarator previous field too
(`int alpha[], beta[];` -> `int[] alpha, beta`), and a declaration that wraps so that its names
start the line (`int` / `alpha[];` -> `int[]` / `alpha, beta;`).

All four combinations converge in a single fix pass.

## Compound arrays

The same rules apply to multidimensional arrays:

- `String[][] alpha; String[] beta[];` merges to `String[][] alpha, beta;`
- `String[] alpha[]; String[] beta[];` merges to `String[][] alpha, beta;`
- `String[] alpha[]; String[][] beta;` merges to `String[][] alpha, beta;`

## Stale columns

`ArrayTypeStyleFixer` can rewrite `int beta[]` to `int[] beta` mid-pass and shift the column the
`FieldConsolidationCheck` event reported. The fixer takes the field named on the event's line
nearest the column, so the shift does not matter.