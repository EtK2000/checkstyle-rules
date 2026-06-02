# PreferImportCheck auto-fix coverage

`PreferImportFixer` strips a flagged fully-qualified type name down to its simple name, but only
when that simple name provably re-binds to the same FQN from the current file. It strips only to a
single simple segment (never a still-qualified `Outer.Inner`, which the check would re-flag), and
never inserts a new import.

The check flags FQNs in type positions (fields, parameters, returns, locals, generics, casts,
`extends`/`implements`/`throws`, `new`, annotation types) and in these expression positions: static
method-call receivers (`java.util.List.of(...)`, including a `<T>` type witness), method references
(`java.lang.Thread::yield`), static field/constant access (`java.lang.Integer.MAX_VALUE`), class
literals (`java.util.List.class`), and FQNs in annotation argument values
(`@Cap(java.lang.Integer.MAX_VALUE)`). A bare simple-name receiver (`SimpleClass.method()`) is not
flagged here; that is `PreferStaticImportCheck`'s domain.

Generic type arguments are flagged in every context they appear in — declared types, `new`
expressions, method-call and method-reference type witnesses, and casts — including wildcard bounds
(`? extends java.util.Map`). An off-classpath type argument (`new ArrayList<com.foo.Unknown>()`) is
flagged but not auto-fixable (see below).

A qualified name whose leading segment names an enclosing type (`Enclosing.Nested` used inside
`Enclosing`) is not flagged, since the simple name already denotes that type.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| Simple name has an existing single-type import (`import java.util.Map;` + `java.util.Map<...>`) | Strip to `Map` | The matching import is kept |
| Nested type with an exact nested-type import (`import java.util.Map.Entry;` + `java.util.Map.Entry`) | Strip to `Entry` | The exact nested import only; an outer-only import is not enough (see skips) |
| Same-package type with a sibling source in the same package (`Foo.java` alongside) | Strip to `Foo` | No import needed |
| Same-package type contributed by a dependency (`pkg.Foo` where the file is in `pkg`) | Strip to `Foo` | No import needed; a dependency class in the same package counts too |
| `java.lang` type with no wildcard import (`@java.lang.SuppressWarnings`, `java.lang.Runnable`) | Strip to simple name | `java.lang` is implicitly imported; no import added |
| Multiple FQNs on one line (`java.util.List<java.util.Map<...>>`) | Each stripped | One violation per site |

## Not supported

| Case | Why |
| --- | --- |
| No single-type import / same-package / `java.lang` binds the simple name (e.g. `java.util.Map.Entry` with only `import java.util.Map;`) | Stripping would need a new import, or leave a still-qualified `Map.Entry` the check re-flags, which is the general fixer's job |
| Off-classpath FQN in a type-argument position (`new ArrayList<com.foo.Unknown>()`) | Flagged by the check, but the simple name is not importable from this file, so it is not auto-fixed |
| Simple name resolves to a different type than the flagged FQN (`import java.util.List;` + `com.foo.List`) | Stripping would silently rebind to the imported / same-package / dependency type |
| Simple name shadowed by an in-file type declaration or type parameter | The bare name would bind to the local declaration |
| `java.lang` type with a wildcard import in scope | A second on-demand import could bind the same simple name |
| Resolution depends only on a wildcard import (`import java.util.*;`) | The wildcard's contents cannot be confirmed from this file |
| Qualified name not contiguous at the violation column (split by comments/whitespace) | Cannot safely locate the dotted run |
| File cannot be parsed to verify shadowing | Cannot confirm the simple name is not shadowed |

Part of [auto-fix coverage](../auto-fix-coverage.md).