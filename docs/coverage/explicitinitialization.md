# ExplicitInitializationCheck auto-fix coverage

Removes the `= <default>` initializer (`= 0` / numeric-zero notations / `= null` / `= false` / `'\0'` or the equivalent `\u` escape) from a declaration. Structural characters (`=`/`;`/`,`) inside a comment, string, or char literal are ignored, and a declaration that continues a multi-line comment is still fixed. Any comment inside the removed `= value` span, and a trailing `//` comment, are preserved.

## Not supported

| Pattern | Reason |
| --- | --- |
| Non-default value (incl. a char/string value containing `;`/`,`/`=`) | Only default-value initializers are removed |
| No `=`, or neither `;` nor `,` after it on the line (multi-line initializer, or an unterminated `/*` in the value) | The initializer can't be located on a single line. It ends at the first `,` or `;` after the `=`, so the first declarator of a declaration wrapped after its comma (`int x = 0,` / `y;`) is still fixed |
| The name at the reported column is not the variable the violation quotes (`retries` where the event reported `timeout`, after a merge put another declarator first; also a longer or shorter name, or no name) | A sibling fix rebuilt the line under the event, so removing the initializer there would drop another variable's value |

Part of [auto-fix coverage](../auto-fix-coverage.md).