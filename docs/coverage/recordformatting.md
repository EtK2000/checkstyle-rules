# RecordFormattingCheck auto-fix coverage

The fixer handles brace formatting and component layout for record declarations. Brace
formatting: collapses non-canonical spacing between the anchor token (closing paren or end of
`implements` clause) and the opening brace, joining empty-body braces onto the anchor line, and
splitting non-empty single-line bodies onto multiple lines. Component layout: rebuilds the
record header to single-line form when components fit in 120 columns, or to multi-line form
(each component on its own line) when they don't.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `record R(...){}` (zero space) | `record R(...) {}` | No space before brace |
| `record R(...)  {}` (multiple spaces) | `record R(...) {}` | Extra space before brace |
| `record R(...)\t{}` (tab) | `record R(...) {}` | Tab before brace |
| `record R(...)\n{}` (newline) | `record R(...) {}` | Brace not on the anchor line |
| `record R(...) {\n}` (empty split) | `record R(...) {}` | Empty body split across lines |
| `record R(...) { body; }` | `record R(...) {\n\tbody;\n}` | Non-empty body inline; split onto its own lines |
| `record R(...) implements Foo{}` | `record R(...) implements Foo {}` | No space before brace (implements anchor) |
| `record R(...) implements Foo\n{}` | `record R(...) implements Foo {}` | Brace not on anchor line (implements anchor) |
| `record R(...) implements\n\tFoo\n{}` | `record R(...) implements\n\tFoo {}` | Brace not on anchor line (multi-line implements) |
| `record R(a,\n\tb) {}` (mixed, fits) | `record R(a, b) {}` | Component-layout: collapses to style A if combined line ≤120 chars |
| `record R(<too long for one line>) {}` | `record R(\n\t\ta,\n\t\tb,\n\t\t...\n) {}` | Component-layout: expands to style B if combined would exceed 120 chars |
| `record R(\n\ta, b\n) {}` (multi-per-line, fits) | `record R(a, b) {}` | Component-layout: collapses a multi-per-line header to single line if it fits |

## Not supported

| Pattern | Reason |
| --- | --- |
| `{` on a line whose anchor ends in a `//` comment, unterminated `/*`, or unterminated literal | Joining the line would swallow the `{` into the comment or literal; bail to avoid invalid source |
| `}` on its own line preceded by lines containing non-whitespace, non-`{` content | Cannot safely collapse to empty-body form without losing content |
| Multi-line record header where any line contains a `//` line comment | Collapsing would either swallow code into the comment or silently drop the comment |
| Multi-line record header where a block comment or text block spans lines (opens on one line, closes on a later one) | Cross-line comment/text-block state cannot be tracked when matching the closing paren, so the header cannot be collapsed safely; bail to avoid corrupting it |
| Record header already in canonical single-line or multi-line form (no-op) | The fixer detects when its output would equal the input and returns null instead of looping |

## Known unsound

Unlike the rows above, these are not skips: the check reports and the result may be wrong.

| Pattern | Reason |
| --- | --- |
| Record header line containing a supplementary character before the `{` (an astral char in a trailing comment or an annotation string) | The brace-spacing check reads `lineText.charAt(lcurlyCol - 1)` where `lcurlyCol` is `lcurly.getColumnNo()`, a CODE-POINT column, so on a line with a non-BMP character earlier the index is short by one unit per such character and the wrong character is inspected. The result is a spurious or missed `MSG_OPEN_BRACE_BAD_SPACING`. Reporting only, no splice, so nothing is corrupted, and the bound cannot go out of range because a code-point column never exceeds the char length. Fix is `LineText.charIndexOfColumn` before indexing |

Part of [auto-fix coverage](../auto-fix-coverage.md).