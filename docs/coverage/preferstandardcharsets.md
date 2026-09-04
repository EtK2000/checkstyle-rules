# PreferStandardCharsetsCheck auto-fix coverage

Replaces a charset name string literal with the matching `StandardCharsets` constant, adding the
import, and replaces a whole `Charset.forName("name")` call with that constant.

## Supported (auto-fixable)

| Pattern | Fixed |
| --- | --- |
| Charset name literal passed to a JDK `String` charset parameter whose `Charset` overload exists at `minSdk` and declares no checked exception the `String` overload does not (`s.getBytes("UTF-8")` becomes `s.getBytes(StandardCharsets.UTF_8)`) | Yes |
| `Charset.forName("name")` with one argument, the receiver resolving to `java.nio.charset.Charset` (simple or fully qualified), or a bare `forName("name")` through `import static java.nio.charset.Charset.forName` | Yes, the whole call becomes `StandardCharsets.X`, including when spaces sit inside the call or a call chain follows it (`Charset.forName("UTF-8").name()` becomes `StandardCharsets.UTF_8.name()`) |
| Any name or alias the `StandardCharsets` constants answer to (`"latin1"`, `"utf8"`, any case) | Yes, as the constant's own name |

## Which overloads count at which `minSdk`

Android added several `Charset` overloads after `StandardCharsets` (API 19). Below its level, a
call is not reported at all, neither its literal nor a `String` variable passed there, because the
overload the advice points at does not exist yet.

| `minSdk` | Overloads |
| --- | --- |
| 19 | `String.getBytes`, `new String(byte[], ...)`, `InputStreamReader`, `OutputStreamWriter` |
| 33 | `ByteArrayOutputStream.toString`, `PrintStream(File or String, ...)`, `PrintStream(OutputStream, boolean, ...)`, `PrintWriter(File or String, ...)`, `URLEncoder.encode`, `URLDecoder.decode`, `Channels.newReader`, `Channels.newWriter` |
| 34 | `Formatter(File, OutputStream or String, ..., Locale)`, `Scanner(File, InputStream, ReadableByteChannel or Path, ...)` |
| 35 | `Properties.storeToXML(OutputStream, String, ...)`, also through a JDK subclass such as `java.security.Provider` |

## Not supported

| Pattern | Reason |
| --- | --- |
| Charset passed as a `String` variable, field, or parameter (the `prefer.standard.charsets.string` violation) | The fixer only rewrites a string *literal* charset name; a variable's value is unknown at fix time, so no `StandardCharsets` constant can be substituted |
| Charset name literal passed where nothing proves a `Charset` overload accepts it: a parameter with no `Charset` sibling (`Charset.isSupported("UTF-8")`, `new String("UTF-8")`, `String.format`, a user method), a receiver the check cannot resolve (a cast or ternary receiver, a fully qualified static call such as `java.net.URLEncoder.encode`, a statically imported method, an unknown type), or a user subclass of a JDK class | Reported as a warning (`prefer.standard.charsets.literal`), never fixed: a `Charset` passed to a `String` parameter does not compile |
| `Charset.forName` with two arguments, called on an instance, written with a type witness (`Charset.<T>forName`), qualified by a class other than `java.nio.charset.Charset` (`Outer.Charset.forName`), or a bare `forName` reached through `import static java.nio.charset.Charset.*` or inherited from a supertype | Reported as the same warning: none of these is provably the one-argument static `Charset.forName` call the fixer replaces |
| `Charset.forName` where a declaration in this file takes the name `Charset` at the call: a type of that name enclosing the call, a type parameter of an enclosing type or method, a member type of any enclosing class body (anonymous class and enum constant bodies included) or of a supertype of one declared in the same file (`extends` and `implements` chains followed through the file), a local type declared earlier in the same or an enclosing block, a top-level type of the file, or a variable named `Charset` that may be in scope: a field, parameter or local variable (a `var` local included, whatever its initializer), a local of an earlier group of a `case:` switch, an enum constant of an enclosing enum, a pattern variable anywhere in the enclosing method, constructor, initializer or field initializer or in an enum constant's arguments, a name a single static import brings in, a static field a static on-demand import brings in from a class that loads (public, or declared in this file's package) or any field or enum constant an owner declared in this file has or its supertypes pass down, or a field a supertype declared outside the file passes down when that supertype loads (public, protected, or package-private in this file's package), the supertype's simple name read nearest scope first as in [PreferImport](preferimport.md): a member type a nearer enclosing body declares or inherits outranks one an outer class or the file declares. A supertype or static on-demand import owner the check cannot read (its name does not resolve, it is not on the checkstyle classpath, or its field types are missing) may declare such a field, so it counts as taking the name too. Likewise a bare `forName` where a `forName` method of an enclosing class body, or of a same-file supertype of one, shadows the static import | Reported as the same warning: the name does not refer to `java.nio.charset.Charset.forName`. Member types and methods of a supertype declared in another file are not seen, so a `Charset` type or `forName` method inherited from one still reports the error and is fixed |
| `Charset.forName` through a simple `Charset` that only an import-on-demand (`java.nio.charset.*`) provides, in a file with a package, or in the default package with a `Charset.java` source file next to it | Reported as the same warning: a same-package class named `Charset` would take precedence over the wildcard, and one in a sibling source file is a class the check cannot load. In the default package without such a file the call is fixed, unless the file itself declares a top-level `Charset` (row above). A single-type import of `java.nio.charset.Charset` outranks a same-package class, so that call is fixed either way |
| Any charset report where `StandardCharsets` would not mean `java.nio.charset.StandardCharsets`: a declaration of that name the row above lists for `Charset` (an enclosing type, a type parameter, a member type of an enclosing or same-file supertype body, an earlier local type, a top-level type, or any of the variables listed there, classpath supertype and static-import fields included), an import of a different class by that name, or, with no single-type import of `java.nio.charset.StandardCharsets`, a `StandardCharsets.java` source file next to this one, with a package or in the default package (a same-package class the check cannot load, which outranks an import-on-demand and which the inserted import would shadow file-wide) | Reported as the same warning: the inserted `StandardCharsets.X` would bind to that name. As above, a member type inherited from a supertype in another file is not seen |
| Charset name literal where the `Charset` overload declares a checked exception the `String` overload does not: `PrintStream(File or String, ...)`, `PrintWriter(File or String, ...)`, `Formatter(File or String, ..., Locale)` and `Scanner(File, ...)`, whose `Charset` forms throw `IOException` where the `String` forms throw only `FileNotFoundException` and `UnsupportedEncodingException` | Reported as the same warning: the swap would leave `IOException` unreported where the method only declares or catches the narrower exceptions. Where the `Charset` overload throws nothing new (`Scanner(Path, ...)`, both forms throwing `IOException`; `InputStreamReader`; `String.getBytes`) the literal is fixed |
| `Charset.forName` standing alone as a statement (an expression statement anywhere, a `for` init or update), or as an expression lambda's whole body | The fixer skips it: a bare constant is not a statement, and a lambda's target may be `void`. A `return`, an initializer, an argument, or a call chained onto it (`Charset.forName("UTF-8").name()`) is fixed |
| Charset name literal inside the body or resources of a `try` whose catch names `UnsupportedEncodingException` or `IOException` (simple or qualified, any alternative of a multi-catch, any enclosing `try`) | The fixer skips it: the `Charset` overload no longer throws, so the catch could stop compiling. A broader catch (`Exception`) is fixed, as is a literal in the catching `try`'s own catch or finally body. A literal in an inner `try`'s catch or finally body still sits in the outer `try`'s body, so it is skipped |
| `Charset.forName` call spanning more than one line | The one-line replacement would have to join the lines |
| `Charset.forName` call with a comment inside it (before the receiver's last part, between the parens, around the dot) | The replacement would drop the comment |

## Which frames the String-variable report reaches

| Frame | Reports |
| --- | --- |
| A field inherited from a same-file superclass, read inside an anonymous class body | Yes |
| A local `var` initialised from a method inherited the same way | Yes |
| A field of the enclosing enum, read inside an enum constant body | Yes |
| `super.field` or `super.method()` in any frame | No: only a bare name argument is resolved |

Part of [auto-fix coverage](../auto-fix-coverage.md).