# PreferStandardCharsetsCheck auto-fix coverage

Replaces a charset name string literal with the matching `StandardCharsets` constant, adding the
import.

## Not supported

| Pattern | Reason |
| --- | --- |
| Charset passed as a `String` variable, field, or parameter (the `prefer.standard.charsets.string` violation) | The fixer only rewrites a string *literal* charset name; a variable's value is unknown at fix time, so no `StandardCharsets` constant can be substituted |

## Which frames the String-variable report reaches

| Frame | Reports |
| --- | --- |
| A field inherited from a same-file superclass, read inside an anonymous class body | Yes |
| A local `var` initialised from a method inherited the same way | Yes |
| A field of the enclosing enum, read inside an enum constant body | Yes |
| `super.field` or `super.method()` in any frame | No: only a bare name argument is resolved |

Part of [auto-fix coverage](../auto-fix-coverage.md).