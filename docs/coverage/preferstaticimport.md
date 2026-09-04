# PreferStaticImportCheck auto-fix coverage

The fixer replaces the text from the receiver naming the class up to the method name it qualifies,
and adds `import static <fqcn>.<method>;`. Everything written between the two names goes with the
class name: whitespace, a block comment, or a line break. The fixer locates the call the same way
the check does, so it supports exactly the calls the check reports and no others.

Candidate classes and their minimum API levels:

| Class | Methods | minSdk |
| --- | --- | --- |
| `java.util.Objects` | `isNull`, `nonNull`, `requireNonNull` | 19 |
| `java.util.Objects` | `requireNonNullElse`, `requireNonNullElseGet` | 30 |
| `java.util.stream.Collectors` | every public static method except `toList` and `toUnmodifiableList` | 24 |
| `java.util.function.Predicate` | `not` | 33 |

`Collectors.toList` and `Collectors.toUnmodifiableList` are excluded here because
`PreferSpecificApiCheck` rewrites them to `stream.toList()` instead.

## Supported (auto-fixable)

| Pattern | Fix | Notes |
| --- | --- | --- |
| `Objects.isNull(a)` and the other `Objects` methods above | `isNull(a)` + `import static java.util.Objects.isNull` | One static import per distinct method |
| `Collectors.joining()` and the other `Collectors` methods | `joining()` + `import static java.util.stream.Collectors.joining` | |
| `Predicate.not(x)` | `not(x)` + `import static java.util.function.Predicate.not` | |
| A call reached through a wildcard `import java.util.*;` | Same rewrite | The check decides reachability; the fixer only rewrites what it reports |
| A chained call whose receiver is the candidate class (`Collectors.joining(", ")` inside a longer expression) | Only the `Class.` prefix is removed; the rest of the line is untouched | |
| A violation line carrying a supplementary character before the receiver | Same rewrite | The reported column counts code points, so it is converted to a character index before the line is indexed |
| Whitespace around the `.` (`Objects . isNull(a)`, and either side alone) | `isNull(a)` | The separator is part of the span, so it goes with the class name |
| A block comment between the two names on one line (`Objects/* c */.isNull(a)`, `Objects./* c */isNull(a)`, or both) | `/* c */ isNull(a)` | The comment was written against the text being removed, so it moves to the front of the method name, in source order and spaced away from whatever it lands against. A comment on a span that crosses lines is a skip instead, below |
| A call split across lines with no comment in the span, with the break on either side of the `.` | Folded onto one line, when the joined line stays inside the line length limit | The span covers the break |
| Text after the call on the same line, including a trailing comment | Left byte for byte | Only the span is replaced |
| Two calls on one line, or one nested in the other's arguments | Both rewritten in one pass | Violations are applied right to left, so an earlier fix cannot move a later one's column |

## Not supported

| Pattern | Behavior | Reason |
| --- | --- | --- |
| Any comment on a line of a span that crosses lines, whether `//` or a block comment, and whether or not it closes | Skips with a reason | The fold puts the two names on one line, and a `//` would take the rest of that line into the comment |
| A fold whose joined line would pass the line length limit | Skips with a reason | Two lines each inside the budget can join into one that is not, and nothing downstream breaks a line again, so the fix would trade this violation for one that never clears |
| The reported position does not name the start of a candidate receiver | No fix, no reason | Covers a column that is negative, inside the receiver, on the `.`, on the method name, or past the end of the line, and a line other than the one reported. All of them mean a sibling fix rewrote the line earlier in the same pass, and nothing at that position is a call the check saw |
| The buffer does not parse | No fix, no reason | There is no tree to locate the call in |

The check itself declines in several more cases, so no violation reaches the fixer at all: fewer
than `minOccurrences` uses of the same `Class.method` in the file (default 2), a target API level
below the method's minimum, a call written fully qualified (`java.util.Objects.isNull(x)`, which
`PreferImportCheck` covers instead), a candidate class that is shadowed by a same-file type, a
sibling `.java` file, or another explicit import, a local variable, field or parameter that shares
the class's simple name, and a local method or conflicting static import that would capture the
bare name. A static wildcard import from a non-candidate class (`import static com.foo.Bar.*;`)
blocks every candidate method, not just a same-named one. The shadow tests only guard the wildcard
path: a class named outright by a single-type import is reported even when a sibling file or
same-file type takes its simple name.

A call carrying an explicit type witness (`Collectors.<K, V>toMap(...)`) is never reported, and that
is deliberate rather than an oversight. An unqualified method invocation has no syntactic slot for
explicit type arguments (JLS 15.12), so there is no static-import form of such a call: reporting it
would ask for a rewrite that does not compile.

Part of [auto-fix coverage](../auto-fix-coverage.md).
