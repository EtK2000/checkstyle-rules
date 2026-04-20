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

## Fixer behavior by style combination

| Prev field style         | Curr field style        | Fixer action                         | Example                |
|--------------------------|-------------------------|--------------------------------------|------------------------|
| Java (`int[] alpha;`)    | Java (`int[] beta;`)    | Merge, strip type from curr          | `int[] alpha, beta;`   |
| Java (`int[] alpha;`)    | C-style (`int beta[];`) | Merge, strip brackets from curr name | `int[] alpha, beta;`   |
| C-style (`int alpha[];`) | C-style (`int beta[];`) | Merge, keep brackets on curr name    | `int alpha[], beta[];` |
| C-style (`int alpha[];`) | Java (`int[] beta;`)    | Bail out (return null)               | N/A                    |

The last row bails out because merging would produce `int alpha[], beta;` which changes beta's
type from `int[]` to `int`. Safely fixing this would require adding `[]` after the name, but the
fixer doesn't know the array dimensions from the text alone (the check knows via AST, but the
fixer only sees raw lines).

## Compound arrays

The same rules apply to multidimensional arrays:

- `String[][] alpha; String[] beta[];` merges to `String[][] alpha, beta[];` (both Java+C-style,
  brackets kept on curr)
- `String[] alpha[]; String[] beta[];` merges to `String[] alpha[], beta[];` (both C-style)
- `String[] alpha[]; String[][] beta;` bails out (prev C-style, curr Java-style)

## Why not convert C-style to Java-style?

The project does not enforce `ArrayTypeStyle` (no such check is configured). Converting C-style to
Java-style during consolidation would be a style change beyond the fixer's scope, which is limited
to merging declarations. If a future `ArrayTypeStyle` check is added, its fixer would handle the
conversion independently.