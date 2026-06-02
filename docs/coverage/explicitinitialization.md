# ExplicitInitializationCheck auto-fix coverage

Removes the `= <default>` initializer (`= 0` / numeric-zero notations / `= null` / `= false` / `'\0'` or the equivalent `\u` escape) from a declaration. Structural characters (`=`/`;`/`,`) inside a comment, string, or char literal are ignored, and a declaration that continues a multi-line comment is still fixed. Any comment inside the removed `= value` span, and a trailing `//` comment, are preserved.

## Not supported

| Pattern | Reason |
| --- | --- |
| Non-default value (incl. a char/string value containing `;`/`,`/`=`) | Only default-value initializers are removed |
| No `=` or no `;` on the line (multi-line initializer, or an unterminated `/*` in the value) | The initializer can't be located on a single line |

Part of [auto-fix coverage](../auto-fix-coverage.md).