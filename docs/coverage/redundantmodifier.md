# RedundantModifierFixer auto-fix coverage

Shared fixer for `NoFinalParametersCheck` and `RedundantModifierCheck`; it removes a single
modifier keyword. It acts only on the 12 JLS modifier keywords (`abstract`, `default`, `final`,
`native`, `private`, `protected`, `public`, `static`, `strictfp`, `synchronized`, `transient`,
`volatile`); any other letter run is left alone, guarding against a stale violation column
shifted by a prior same-line fixer.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| Modifier keyword + single space (`abstract void m();`) | Remove keyword and the trailing space | Standard case |
| Modifier keyword + tab separator (`final\tint x;`) | Remove keyword and the trailing tab | Tab-indented codebases produce tab separators after column-aligned modifiers |
| Modifier at end-of-line with no trailing whitespace (`int abstract`) | Remove keyword; line keeps any prefix text | No trailing whitespace to remove |
| Resulting line is blank after the splice (`public` alone on the line) | Empty replacement (whole line removed) | The line is deleted entirely |
| Keyword in the middle of multi-modifier declaration (`public static final int X`) | Removes only the flagged keyword | Other modifiers on the line are untouched; `RedundantModifierCheck` may fire on each separately and process them in subsequent iterations |

## Not supported

| Pattern | Reason |
| --- | --- |
| Letter run is not a JLS modifier keyword (e.g. `compareTo`, `return`, `sealed`) | A prior same-line fixer likely rewrote text and shifted columns |

## Known limitations

- Stale column landing on a *different* valid modifier than the one the upstream check intended. The
  whitelist confirms "*a* modifier" but cannot confirm "*the* modifier flagged."
- Non-ASCII Unicode letters at the violation column flow through `Character.isLetter` and form
  letter runs; the whitelist closes the corruption path (since the run never matches a JLS keyword),
  but the precondition is undocumented.

Part of [auto-fix coverage](../auto-fix-coverage.md).