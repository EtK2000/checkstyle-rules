# AnnotationOwnLineCheck auto-fix coverage

Splits stacked or embedded annotations onto their own lines, removes blank lines between them, and
sorts them alphabetically.

## Not supported

| Pattern | Reason |
| --- | --- |
| Annotation already on its own line, just needing sorting | No fix when the annotations are already in the correct order |
| Violation line is a comment-only line (`//`, `/*`, `*`) | The delete-blank-below step is skipped, guarding against a stale line index after a prior fixer's deletion |

Part of [auto-fix coverage](../auto-fix-coverage.md).