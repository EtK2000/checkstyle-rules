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

An expression-position qualifier (static call receiver, static field access, method reference,
annotation argument value) is not flagged where a variable named like its simple name is in scope.
Java reads the shortened name as that variable (JLS 6.4.2 obscuring), so `Charset.defaultCharset()`
with a local `Charset` would not compile, and `Integer.MAX_VALUE` with an `Integer Integer` local
would quietly read the static through the variable. The qualified name is what the code needs there,
and no import can change that. These count as variables:

- locals, including one whose own initializer or annotation holds the qualifier, and one declared in
  an earlier group of a `case:` switch
- method, constructor, lambda and catch parameters, seen from the method's and the parameter's own
  annotations too, as javac does
- `for`, for-each and try-with-resources variables
- fields of an enclosing type or of a supertype declared in the same file, interface constants,
  record components and the enum constants of an enclosing enum
- a pattern variable in an enum constant's arguments
- a field passed down by a supertype declared outside the file (an `extends`/`implements` type, or
  the type an anonymous class extends) when that supertype loads on the checkstyle classpath: a public
  one, a protected one, or a package-private one declared in this file's package; a private one
  never. A supertype's simple name is read the way javac reads it, nearest scope first: a local
  class declared earlier in an enclosing block, then each enclosing class body from the inside out,
  where a member type the body declares or inherits from a supertype in this file or on the
  classpath counts (`static class Mid extends ClasspathScopeOuter { static class Sub extends Inner {} }`
  names `ClasspathScopeOuter.Inner` even when an outer class or the file's top level declares an
  `Inner`), then a single-type import, a top-level type of the file, a static member type a single
  static import brings in, from a classpath class or one declared in this file
  (`import static a.Outer.Inner;` + `extends Inner`, and `import static java.awt.geom.Point2D.Double;`
  + `extends Double` names `Point2D.Double`, not `java.lang.Double`), then the package, and last the
  on-demand imports, `java.lang` and the static member types a static on-demand import brings in,
  together. An anonymous class's type is read from the scope of its `new`. A single static import of
  a field or method of the name names no type, so the next one is read. An enclosing body whose
  supertype cannot be read, a static-import class that cannot be read, or two different answers at
  the on-demand level leave the supertype unknown. A dotted name (`ClasspathOuter.Inner`,
  `Enclosing.Holder.Inner`) is read through its head
- a name a single static import brings in
- a static field a static on-demand import (`import static a.B.*;`) brings in, when `B` loads: a
  public one, or a protected or package-private one declared in this file's package. When `B` is
  declared in this file, any field or enum constant of the name it declares counts, and so does a
  field its supertypes pass down, read the same way

When the answer depends on a class the check cannot read, the qualifier is reported as the warning
`Consider an import instead of fully qualified name '...'; a class this file cannot read may give the
short name another meaning.`, which the fixer leaves alone. That happens when nothing above already
binds the name and a supertype of an enclosing body does not resolve, does not load (any class of the
project's own sources, which are not on the checkstyle classpath), or loads with field types missing,
or when a static on-demand import's owner cannot be read the same way. A proven binder still wins: a
parameter of the name, or a readable supertype passing the field down, leaves the site unreported
even inside such a class.

Class literals (`java.nio.charset.Charset.class`) and constructor references
(`java.lang.StringBuilder::new`) name a type outright, so they are still flagged next to such a
variable, as is every type position (`a.b.C x`, `new a.b.C()`, casts, `instanceof`, generics,
annotation types, `extends`/`implements`/`throws`). The other usages in the file are unaffected:
each site is reported and shortened on its own, through an import the file already has, so a site
left qualified still compiles beside a shortened one.

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
| Expression qualifier inside a type whose supertype, or a static on-demand import's owner, cannot be read (the name does not resolve, the class is not on the checkstyle classpath, or its field types are missing) | Reported as the `prefer.import.warning` warning and not fixed: the unread class may declare a field of that name, which would take over the short name |
| Simple name is a member type a supertype declared outside the file passes down to an enclosing body (`java.lang.Thread.State` inside a class implementing `java.util.concurrent.Future`, which passes down `Future.State`; one that is public, protected, or package-private and declared in this file's package, so `java.lang.Byte` inside a subclass of a same-package class with a package-private `Byte` too), or two different member types of that name reach the body, at one depth or at two (`java.lang.Thread.State` inside a subclass of a class that extends `Thread` and implements `Future`) | Inside that body the short name would read as the inherited member type, or be ambiguous. Skipped only when the inherited type is a different class: `java.lang.Thread.State` inside a subclass of `Thread` is still stripped, as is `java.util.concurrent.Future.State` that reaches the body along two paths (`RunnableScheduledFuture`), and a member type that a supertype's own member type of the name hides does not count, whatever that declaration's access (`java.security.KeyStore.Entry` inside a `java.util.LinkedHashMap` subclass in another package is stripped: `LinkedHashMap`'s package-private `Entry` hides `Map.Entry`). A private member type, or a package-private one of another package, is not passed down. Only the nearest enclosing body that has a member type of that name counts: `java.lang.Thread.State` in `static class Worker extends Thread` nested in a `Future` implementor is stripped |
| Simple name inside a type whose nearest body with a member type of that name has a supertype that cannot be read (as above), even when a class further out passes down a readable one | That supertype may pass down a member type of the same name |
| `java.lang` name a static import brings in as a member type (`import static java.awt.geom.Point2D.*;` or `import static java.awt.geom.Point2D.Double;` + `java.lang.Double`), or a same-package or dependency type's name a single static import brings in as one | A single static import outranks the package and `java.lang`, so the short name would read as the imported member type; a static on-demand one makes a `java.lang` name ambiguous. A same-package type outranks a static on-demand import and a single-type import outranks both, so those are still stripped, as is a name whose static member type is not imported: a non-static one, one hidden by a non-static member type of the same name, a private one, or a protected or package-private one of a class in another package. A protected or package-private one declared in this file's package is imported (`import static p.Holder.*;` + `java.lang.Byte` inside package `p` is skipped) |
| `java.lang` name next to any static import, or a same-package or dependency type's name next to a single static import of it, whose class cannot be read (does not load, or is declared in this file with a supertype that cannot be read) | The unread class may bring in a member type of the same name, so the site is skipped even when it brings in none |

## Not flagged, though shortening would compile

The scope rules above lean towards leaving a qualified name alone. These sites are not reported
even though the short form would compile:

| Case | Why |
| --- | --- |
| A pattern variable of that name anywhere in the enclosing method, constructor, initializer or field initializer, even where it is out of scope (the `else` of a positive `instanceof`, another `case`, the right of a non-short-circuit `&`, the `then` of a negated `instanceof`) | Pattern scoping is followed only into an `if`'s `then` branch and an `&&`; anywhere else the pattern may be the variable in scope |
| The iterable expression of a for-each loop whose variable has that name | The loop variable is treated as in scope for the whole loop header |
| A single static import of a method or member type of that name (`import static java.util.Map.Entry;`) | The file cannot tell an imported method or member type from an imported field |
| A field of that name a same-file static on-demand owner declares as an instance field | Any field the same-file owner declares counts, static or not |
| A class nested in one whose supertype cannot be read, extending a type by its simple name (`class A extends Unreadable { static class B extends ClasspathType {} }`), reported as the warning rather than the error, even when a class further out passes down a readable member type of that name | The unread supertype may pass down a member type of that name, so `B`'s own supertype is not known either |
| A field of that name a supertype declared in this file declares privately | Any field a same-file supertype declares counts, private or not |
| A supertype named after a `java.lang` or on-demand imported type (`extends Thread`) in a file with a static on-demand import whose class cannot be read, reported as the warning rather than the error | The unread class may bring in a static member type of that name, which would make the name ambiguous |

Part of [auto-fix coverage](../auto-fix-coverage.md).