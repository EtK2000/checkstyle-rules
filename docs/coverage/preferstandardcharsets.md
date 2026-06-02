# PreferStandardCharsetsCheck auto-fix coverage

Replaces a charset name string literal with the matching `StandardCharsets` constant, adding the
import.

## Not supported

| Pattern | Reason |
| --- | --- |
| Charset passed as a `String` variable, field, or parameter (the `prefer.standard.charsets.string` violation) | The fixer only rewrites a string *literal* charset name; a variable's value is unknown at fix time, so no `StandardCharsets` constant can be substituted |

Part of [auto-fix coverage](../auto-fix-coverage.md).