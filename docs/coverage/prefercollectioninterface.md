# PreferCollectionInterfaceCheck auto-fix coverage

Replaces a concrete collection type in a method or constructor signature with its interface
(e.g. `ArrayList` to `List`).

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| Signature whose rewrite would duplicate another overload (`dump(List<String>)` alongside `dump(ArrayList<String>)`) | Replacing the concrete type would make the two signatures identical and the file would stop compiling. Overloads are compared on their erased parameter types, positionally, which is the identity the compiler uses |

## Not supported

| Pattern | Reason |
| --- | --- |
| Column out of range (negative, or at/past end of line) | The violation column is outside the line's content |
| No identifier at the violation column | The column does not point at a Java identifier character |
| Class not resolvable | The class can't be resolved (missing from the classpath, or an unresolvable name) |
| Class is not a standard collection | The resolved class is an interface, abstract, ambiguous, or not in the supported set |

Part of [auto-fix coverage](../auto-fix-coverage.md).