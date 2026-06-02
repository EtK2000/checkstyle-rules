# UnusedImportsCheck auto-fix coverage

Deletes the unused import line (the check has already decided the import is removable). Wildcard imports (`*` / `static ...*`) and `java.lang.<SimpleName>` imports are deleted unconditionally; other imports are re-verified against the file body before deletion (guarding against a same-pass interaction with another fixer), and a residual blank line is swept.

## Not supported

| Pattern | Reason |
| --- | --- |
| The body still uses the simple name | Deleting the import would break compilation |
| Line doesn't parse as `import [static] X.Y;` | Malformed import, not safely removable (single-line `//` and `/* */` comments on the import line are stripped first, so a commented-but-otherwise-valid import is still removed) |
| A trailing block comment opens on the import line but closes on a later line | Deleting only the import line would orphan the comment continuation |

Part of [auto-fix coverage](../auto-fix-coverage.md).