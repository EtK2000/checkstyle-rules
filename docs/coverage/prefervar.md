# PreferVarCheck auto-fix coverage

Replaces an explicit local, for-each or try-with-resources type with `var`, moving the declared type
arguments onto every diamond initializer on the line so nothing is inferred away
(`List<String> l = new ArrayList<>()` -> `var l = new ArrayList<String>()`; each arm of a ternary or
single-line switch initializer receives them, since any arm may supply the value). Also converts an
explicit array initializer to the implicit form and replaces `<Object>` with `<>` on a `var`
declaration. The implicit form creates an array of the declared type (JLS 10.6), so it is used only
when the `new` creates exactly that type, written the same way or resolving to the same one
(`final java.lang.String[] a = new String[]{"a"}` and `final String a[] = new String[]{"a"}` convert),
or under `var` (`final var rows = new int[][]{{1}}` becomes `final int[][] rows = {{1}}`), a single
`for`-init declarator included (`for (int[] counter = new int[]{0}; ...)` becomes
`for (int[] counter = {0}; ...)`); any other creation is refused below. An array creation used as a
receiver or indexed (`final int[] copy = new int[]{1, 2}.clone()`, `new int[][]{{1}}[0]`, on one line or with the `}` and
the `.clone()` on later lines) has no `{...}` shorthand, so a proven one converts the declared type
instead (`final var copy = new int[]{1, 2}.clone()`).

A primitive declaration whose initializer the check cannot type is reported as a warning
(`should use 'var'`) and never fixed, since `var` may bind a narrower type (`long total = count()`
with an `int` method). One shape is typed and converted: a `boolean` initialized by a comparison,
`!`, `&&`, `||` or `instanceof` (`boolean b = x == y`), whose result is always a `boolean`.

A primitive array is a reference type, and needs a proven initializer like any other ("Proving the
declared type" below). `int[][] m = new int[3][3]`, a cast (`(int[]) source`), an `int[]` name, an
element of an `int[][]` and a proven call (`byte[] all = in.readAllBytes()`, `values.clone()`)
convert. A same-file generic call (`final int[] ids = cache.get("ids")` with `<T> T get(String)`,
which binds `Object` under `var`, on a variable, a type-variable receiver or without arguments), a
generic call such as `Objects.requireNonNull(values)`, a field read off a receiver nothing resolves
(`holder.values`), and a conditional or switch with an arm that proves nothing (an unproven call, a
`null` arm, a `throw` arm: `switch (kind) { case 1 -> null; default -> throw ...; }` gives `var`
nothing but `null` to infer) are warnings.

A reference declaration is converted only when the syntax tree and the classpath prove that `var`
binds exactly the declared type ("Proving the declared type" below). One that is not proven, or is
proven narrower than the declared type, is reported as a warning (`should use 'var'`) and never
fixed: under `var` the variable takes the narrower type, and every use of it rebinds, which nothing
here can see.

The check refuses wherever `var` would bind a type other than the one written. Those refusals are
grouped below by what drives them.

## The initializer binds a different type

| Pattern | Reason |
| --- | --- |
| Boxed declaration whose initializer is not itself the box (`Byte b = 0`, `Integer i = Integer.parseInt(s)`) | `var` binds the primitive. `Integer.valueOf(...)` and `new Integer(...)` still convert |
| Widening literal (`long x = 1`, `double d = 5f`, `float f = 5.0`) | `var` binds the literal's own type, and no fixer rewrites the literal to carry the suffix |
| Declared supertype (`Object o = "x"`, `Object o = compute()`) | `var` binds the value's own type. An array of a supertype is its own type rather than a widening, so `Object[] a = new String[0]` stays as written while `Object[] a = list.toArray()` converts |
| Explicit array creation of another array type (`Object[] a = new String[]{"a"}`, `Number[] n = new Integer[]{1}`, `Object[] rows = new int[][]{{1}}`, `Object[] rows = new Object[][]{{1}}`, `Object o = new int[]{1}`, `Cloneable c = new int[]{1}`, `List<String>[] l = new List[]{list}`) | The `{...}` shorthand would create the declared type instead: `Object[] a = {"a"}` is an `Object[]`, so a later `(String[]) a` throws and storing a non-`String` no longer does, while `Object[] rows = {{1}}` and `List<String>[] l = {list}` do not compile. The declaration takes the same path as any other initializer: silent where the rows above already refuse it (a supertype element, a widening declared type), and otherwise a warning, since `var` would bind the narrower created type (`Object[] rows = new int[][]{{1}}`, `Cloneable c = new int[]{1}`, `Object[] rows = new Object[][]{{1}}`, `List<String>[] l = new List[]{list}`) |
| Explicit array creation of a wildcard element type (`List<?>[] l = new List<?>[]{list}`) | A wildcard has no proven type, so the creation is not shown to be the declared type and the shorthand is not reported, although `List<?>[] l = {list}` would compile. The wildcard refusal below then leaves the declaration silent. Under `var` the creation names the type itself, so `final var l = new List<?>[]{list}` still becomes `final List<?>[] l = {list}`, and a raw array (`final List[] raws = new List[]{list}`) takes the shorthand |
| Widened type argument (`Map<String, Object> m = Map.of(...)`) | The arguments are inferred from the call and the deliberate widening is lost. A `new` is exempt, since its diamond receives them |
| Wildcard (`List<?> w = ...`) | `var` binds the inferred arguments, losing the wildcard |
| Multi-member anonymous class body | `var` cannot name the type. The single-method form is deferred to `PreferLambdaCheck` |
| Qualifier carrying its own arguments (`Outer<String>.Inner`, at any depth) | Those bind the enclosing instance rather than the declared type |
| `null` initializer of a try-with-resources variable (`try (Closeable c = null)`) | `var` cannot take `null`, the same as a local |

A qualified declared type is treated identically to its imported form, so `java.util.List<?>` and
`Api.Cache<K, V>` get the same refusals as `List<?>` and `Cache<K, V>`. A qualified type *argument*,
by contrast, only counts as widening when its full name does (`java.lang.Object`, `java.lang.Number`,
`java.lang.CharSequence`, `java.lang.Comparable`, `java.io.Serializable`), so an application type
sharing the simple name (`Map<String, Types.Number>`) converts while `Map<String, Number>` does not.

## Conditional initializers

A conditional or switch-expression initializer is converted only when every arm proves the declared
type, since under `var` the arms no longer share the declaration as a target type. With type
arguments, every arm has to be a `new` of the declared class, whose diamond receives them
(`ArrayList<String> v = flag ? new ArrayList<>() : new ArrayList<>(16)` converts, each arm getting
`<String>`); without them any arm that proves the type will do (`Derived item = flag ? makeDerived()
: new Derived()` converts).

| Pattern | Reason |
| --- | --- |
| Any arm that is not a `new`, on a declaration with type arguments (`flag ? new ArrayList<>() : Collections.emptyList()`) | That arm infers on its own, so the variable binds the arms' least upper bound. Not reported |
| Colon-form switch | It yields through statements the arm walk cannot follow. Not reported |
| Braced rule body (`case 1 -> { yield new ArrayList<>(); }`) | The value is hidden behind a block. Not reported |
| Parenthesised condition (`(flag) ? ... : ...`) | The parens shift the arms out of the positions the walk reads. Not reported |
| Arms of different classes, or an arm narrower than the declared type (`List<String> v = flag ? new ArrayList<>() : new LinkedList<>()`, `Base item = flag ? makeDerived() : new Derived()`) | They bind the arms' least upper bound, narrower than the declared type. A warning, never fixed |

## For-each element types

A for-each variable is converted only when its element type is proven to be the declared one: an
array's element type, or the one argument of the `java.util.Iterator` the iterable's `iterator()`
returns, read through the iterable's proven type (`for (String s : names)` with `List<String>
names`). Anything else is reported as a warning (`For-each variable should use 'var'`) and never
fixed: a declared supertype of the element (`for (Closeable c : streams)` over a
`List<InputStream>`, `for (Collection<String> c : lists)` over a `List<List<String>>`), a wildcard
element (`List<? extends InputStream>`), and an iterable whose type is not proven (`List.of("a")`, a
`var` initialized by a generic call), since under `var` the variable takes the element type and
every use rebinds. The rows below are not reported at all.

| Pattern | Reason |
| --- | --- |
| Widening supertype of the element type (`for (Object o : strings)`), including an array of one (`for (Object[] row : listOfStringArrays)`) | `var` binds the element type, which the declaration deliberately is not |
| Primitive or boxed element type, unless the iterable is an array of exactly that type | Boxing and unboxing both retype the variable: `for (int n : listOfIntegers)` rebinds to `Integer`, where `==` compares references and arithmetic runs in int space, and `for (Integer n : intArray)` rebinds to `int`, where a null check stops compiling. `for (int n : intArray)` still converts |

## Proving the declared type

A reference declaration that none of the rules above or below refuses is converted when its
initializer is proven to have exactly the declared type, type arguments included, and is otherwise
reported as a warning (`should use 'var'`) and never fixed. A primitive array takes this proof too,
while a primitive declaration keeps the literal rules at the top.

| Initializer | Proven when |
| --- | --- |
| `new` (`ArrayList<String> l = new ArrayList<>()`) | It constructs the declared class, written the same way or resolving to the same one (`java.util.ArrayList<String> l = new ArrayList<>()` converts), with the same type arguments, or with a diamond that receives the declared ones. A raw declaration's diamond infers arguments the declaration does not have, so `List raw = new ArrayList<>()` warns |
| Array creation (`String[] a = new String[1]`) | The element type and dimensions are the declared ones |
| String literal or text block | The declared type is `String` |
| Cast (`(String) o`, `(List<String>) o`) | The cast names the declared type, written the same way or resolving to the same one. A cast with more than one bound (`(Comparator<String> & Serializable) (a, b) -> ...`) proves nothing, since its type is the intersection, and neither does a `var` local it initializes |
| Conditional or switch expression | Every arm proves it (see "Conditional initializers") |
| A name: a parameter, a local, a field, `this.f`, `Outer.this.f`, a record component, an `instanceof` pattern, an enum constant (`TimeUnit.SECONDS`) | Its written type is the declared one, read where it is written. A `var` local takes its initializer's proven type, and an array `var` for-each variable its array's element type. A name proves nothing where something the file does not show may bind it instead: an `instanceof` or record pattern of that name in the enclosing method, constructor, initializer or field initializer, unless the read sits in an `if`'s then-branch or an `&&`'s right operand that pattern governs (after `if (!(cause instanceof IOException error)) return;`, in that `if`'s `else`, a `case`, a conditional's arm or an `\|\|`'s right operand, `error` may be the pattern rather than an `Exception error` field), and a non-private field of that name on a classpath supertype of a class the read sits in, or any supertype nothing resolves (`in` inside a `FilterInputStream` subclass is the inherited `InputStream in`, not an outer `Closeable in`) |
| Array element (`lists[0]`) | The array's proven element type is the declared one |
| Call | Its receiver's own type is proven (a written type, a class name, `this`, `super`, `Outer.this`, a `new`, a cast or a proven call), and every method the call may bind to returns exactly the declared type. A classpath method's class type variable is read through the receiver's type arguments (`Iterator<String> it = list.iterator()` on a `List<String>` converts) and a method's own type variable is never proven (`List<Integer> l = List.of(1, 2)`, `List<String> l = wrap("a")` warn). A bare call binds in the innermost enclosing class with a method of that name (JLS 15.12.1), its same-file and classpath supertypes read alike, and only failing one through the static imports; `super.m()` reaches `java.lang.Object` when the class extends nothing. A nearer method with the same exact parameter types overrides a farther one, so a covariant override is read at its own return type; every other candidate has to agree. A call with a lambda, a method reference, `null`, a diamond `new`, an unproven call, or a conditional or switch whose arms do not all prove one type among its arguments is never proven, since a poly expression can steer the overload or the inference (`List<Runnable> t = List.of(() -> run())` and `String s = String.valueOf(flag ? x : make())` warn, while `String.valueOf(flag ? x : y)` with `Object x, y` converts) |
| Anything else: `this`, string concatenation, a name nothing resolves | Never proven, except `this` inside an enum constant's body, which is proven to be the constant's own class, a subclass of the enum, so `Mode current = this` there warns |

| Pattern | Reason |
| --- | --- |
| Declared type a proper supertype of the proven type (`List<String> l = new ArrayList<>()`, `Closeable c = Files.newInputStream(path)`, `Base b = (Derived) o`, `Base b = makeDerived()`, `Map<Object, Object> p = new Properties()`, `Function<String, Integer> f = new MyFunc<>()`, `Api.Cache<String, Integer> c = new Impl.Cache<>()`) | Under `var` the variable takes the narrower type, and every use rebinds: the overload a call it is passed to selects, a member reached through it, a later reassignment. A warning whatever the uses are |
| Initializer not proven (`List<String> l = raw` with `var raw = registry.load()`, `List<String> names = registry.names` with `registry` of a class nothing resolves, `Base self = this`, `Base b = make(1)` beside `Base make(int)` and `Derived make(String)`) | `var` may bind a narrower type. A warning |

A final declared type takes the same proof as any other. A final class, a record or an enum has no
subclass a variable of it could hold, but that says nothing about the initializer's own type, which
`var` takes: a same-file generic method called on a variable, a `new` or a cast
(`String name = registry.get("name")` with `<T> T get(String)`) binds `Object`, and so does a
conditional argument (`Objects.requireNonNull(flag ? get("a") : get("b"))`); `this` in an enum
constant's body (`Mode m = flag ? this : null`, `Objects.requireNonNull(this)`) binds the constant's
own class; and `flag ? null : null` gives `var` nothing to infer. So `final String text = holder.text`,
`final Label label = holder.label` and `final Duration total = holder.total` warn when `holder` is of
a class nothing resolves, whatever the declared name resolves to, and so do `final Node self = this`
in a final class and a type variable bounded by a final class (`<T extends Label> void m(T t) {
final Label l = t; }`). A string literal, a `new`, a cast, a name of the written type and a proven
call still convert (`final String s = "x"`, `new String(chars)`, `(String) source`, a `String`
parameter, `typed.substring(1)`).

An inner class of a generic class (a member class that is not static, enclosed by a generic class
directly or through further inner classes) takes the type arguments of its enclosing instance, which
no proof carries. It is proven only written as a simple name inside the body of the class declaring
it, where no class in between inherits that class other than as its own type (`new Outer<T>() { ... }`
still does), so `Inner` names that body's own instance type (`Inner` inside `Outer<T>` is
`Outer<T>.Inner`), and read through an instance typed with the class's own type parameters
(`inner()`, `new Inner()`, a field of the class, `other.inner()` on an `Outer<T> other`), so those
convert. Each of these is a warning instead:

- written raw (`Outer.Inner raw = strings.inner()` with `Outer<String> strings`, where `var` binds
  `Outer<String>.Inner` and a later `raw = new Outer<Integer>().inner()` stops compiling), at any depth
  (`Outer.Inner.Deeper`), and for a generic record's inner class too;
- written as a simple name inside a class or anonymous class that extends the enclosing class raw
  (`new Outer() { Inner copy = given; }`, or a class nested inside that one), where it names the raw
  type, or with other type arguments
  (`final Inner made = new Inner()` inside a `new Outer<String>() { ... }`, although both sides bind
  `Outer<String>.Inner` there); likewise inside one that reaches it through another class
  (`Typed extends Middle`, `Middle extends Outer<T>`), whose type arguments are not followed, or that
  has a supertype nothing resolves;
- reached through a supertype (`Inner` inside a class extending `Outer<String>`), and a classpath
  inner class, whether written raw or inherited by a same-file class;
- read through an instance of the enclosing class typed with other type arguments (`raw.inner()`,
  `raw.field` or `raw.new Inner()` on a raw `Outer`), and created on any qualifying instance
  (`other.new Inner()` on an `Outer<T> other` too);
- a raw cast (`(Outer.Inner) source`), although raw to raw would bind the same.

A static member class, an enum, a record or an interface member of a generic class, an inner class of
a non-generic class and a local class all still convert (`final Outer.Nested n = strings.nested()`).

A proof that would read more than 256 nested expressions (a chain of that many calls, or of `var`
locals each initialized by the last) or spell a type longer than 4096 characters (a type argument
that doubles at every supertype, `C1<T> extends C0<Map<T, T>>`) is no proof, so such a declaration is
a warning.

Simple type names are resolved the way Java scopes them: a type parameter, a type declared in an
enclosing block, body or the file, or a member type an enclosing class inherits, from this file or
from a classpath supertype (`Entry` inside a class implementing `Map`, including a package-private
one when the supertype is in this file's package), ahead of the imports. A
supertype nothing resolves on the way makes every name below it unknown, since it may declare a
member type of that name.

## Unresolvable call targets

A declaration whose initializer is a call is not reported at all when the class declaring that method
cannot be determined: the method may be target-typed, and the conversion would drop the declared
arguments it infers from, so `List<String> l = emptyList()` would bind `List<Object>`.

The declaring class is looked for on every enclosing class in the file, on a same-file supertype, on
a static import's owner, on a qualified receiver (`java.util.Collections.emptyList()`), on a receiver
that names its own type (a string or text block, a `new`, a cast, `this`/`super`, and through a chain
to its base), and on a receiver naming a variable whose type the file declares or the classpath holds,
including an array element's array, a primitive array, a type variable's erasure, and the class a
`var` initializer spells (`var r = new Registry()`). A variable of a class the classpath does not hold
does not resolve, as a local, field, parameter, array element, `var` or type variable bounded by it,
since `registry.lookup("k")` may be a `<T> T lookup(String)` that `var` would bind to `Object`. Neither
does a variable whose type is never spelled: an untyped lambda parameter, a `var` for-each variable,
or a `var` initialized by a call (`var list = List.of("a")`). A call none of those resolve stays as
written.

A same-file declaration answers only a call made on an enclosing body (bare, or qualified by `this`
or `super`), and a static import only a bare call: a foreign receiver's same-named method is an
unrelated one. So `Registry.lookup("k")`, with `Registry` nowhere to be found, stays as written beside
a same-file `lookup`, and so does `Registry.valueOf(1)` beside `import static java.lang.String.valueOf`.
A class imported by name that the classpath does not hold (`import com.example.Registry;`) does not
resolve its static calls either, the same as a qualified name nothing loads, and neither does a
same-file type that does not enclose the call (`Util.make()`). A call's return type is proven under
the same rule ("Proving the declared type"), so a homonym does not stand in for the return type
of `other.get("k")` on a classpath receiver.

## Target-typed calls

| Pattern | Reason |
| --- | --- |
| A same-file method whose return type mentions, anywhere, a type variable it declares that no parameter mentions (`<T> T make()`, `<T> List<T> empty()`, `<T> Optional<T> find()`, `<K, V> Map<K, V> newMap()`, `<K, V> Map<K, V> keyed(K)`, `<T> List<? extends T> items()`, `<T> List<T>[] buckets()`, `<T> java.util.List<T> empty()`) | Only the declaration infers the type variable, so `var` would bind what the call infers alone (`Object`, `List<Object>`). The explicit declaration is not reported and a `var` one gets the generic-type-info warning; with explicit type arguments (`Outer.<String>make()`) it gets the type-arguments warning. A type variable a parameter mentions is inferred from the argument (`<T> List<T> wrap(T)`, `<K, V> Map<K, V> pair(K, V)`), and a return type that does not mention it (`<T> List<String> labels()`) binds the same, so both still convert |
| Such a method reached by a call qualified by `this`/`super`, `Outer.this`/`Iface.super` or an enclosing type's name (`Outer.make()`, `Top.Outer.make()`), or from an anonymous class or enum constant body that inherits it | Each form invokes the same-file declaration a bare call would. A variable that shares the enclosing type's name obscures it (JLS 6.4.2), so `Box.trim()` on a `String Box` parameter reads the variable and converts |

## Try-with-resources

Refused on the same initializer grounds as a local (the first two sections), and held to the same
proof ("Proving the declared type"): `try (StringReader r = new StringReader(t))` converts, while a
declared supertype of what it constructs or returns (`try (InputStream in = new
ByteArrayInputStream(bytes))`, `try (Closeable c = openReader())`) is reported as a warning
(`Try-with-resources variable should use 'var'`) and never fixed, since the body is written against
the declared type, and rebinding to the concrete one can reselect an overload or make an enclosing
`catch` unreachable when the subtype's `close()` throws less. A `null` initializer is not reported,
since `var` cannot take one. A resource inside a lambda in another declaration's initializer (an
explicit array creation, or a primitive local's call argument) is read as its own declaration and
converts like any other.

The unresolvable-call and target-typed-call rules apply too, so `try (BufferedReader r = acquire())`
with a generic `acquire()` is not reported, and with explicit type arguments
(`Util.<BufferedReader>acquire()`) it gets the type-arguments warning instead.

## Fixer-side skips

| Pattern | Reason |
| --- | --- |
| Column out of range, or resolving to a field, a bare assignment, or a position inside an initializer (`int[] b = new int[]{2}; Object[] c = new String[]{"x"};` with the column shifted onto `b`'s `new int`) | A sibling fixer shifted the line earlier in the pass, so the reported column no longer names a convertible declaration: `var` is illegal on a field or an assignment, and a position inside an initializer names no declared type, so reading on from it would take a later declaration's `=` and `new` for this one's |
| Column landing mid-identifier, on a non-identifier, inside a string or char literal, or on a name followed by `==` | Replacing from that offset would leave the token's head behind, splice `var` into a literal, or rewrite a comparison |
| C-style array declarator (`String x[] = new String[0];`) | `var x[]` is not legal Java. In the full pipeline `ArrayTypeStyleFixer` normalises the declaration first, after which it converts |
| Explicit array initializer split across lines: the name and `=` (`final String[]` / `names = new String[]{"a"};`), the `new` (`final String[] names =` / `new String[]{"a"};`), the created type (`= new` / `int[]{1};`), its dimensions or type arguments (`new int` / `[]{1};`, `new List<` / `?>[]{list};`) or the `{` (`new int[]` / `{1, 2, 3};`) on a later line than the reported type (`does not resolve on the reported line`) | The text rewrite works on the reported line only. Skipped rather than falling through to the type-to-`var` rewrite, which would leave the violation standing |
| Explicit array initializer whose text the rewrite would drop or misread (`a comment, parenthesis, annotation or spacing`): a comment or a parenthesis between the `=` and the `new` (`= /* keep */ new int[]{1}`, `= (new int[]{1})`), a type-use annotation on the creation (`new @Ann String[]{"a"}`, `new String @Ann []{"b"}`), spacing inside the creation (`new String [] {"a"}`, a tab after `new`), a comment between the `[]` and the `{` (`new String[] /* {"z"} */ {"a"}`), or a comment inside the declared brackets (`String[/* c */] names`) | Splicing from the `=` to the `{` would delete the comment or leave the parenthesis unbalanced, and the text scan reads `Type[]` only when it is written as one run. An annotation on the declared dimensions is kept (`final String @Ann [] declared = new String[]{"d"}` becomes `final String @Ann [] declared = {"d"}`) |
| Explicit array initializer the current text no longer proves (`no longer creates the declared array type`): one that no longer creates the declared array type (`final Object[] values = new String[]{"a"}` reached with the event of a `String[]` declaration), one a sibling rewrote (`new int[]{1}.clone()`), one whose reported column a sibling shifted off the type, or a buffer that no longer parses | The check's own proof is re-read on the current text with the file's own imports: the text rewrite reads only the spelling, and would make `values` an `Object[]`. The type-to-`var` paths answer a different violation, so the event is skipped rather than handed to them |
| `<Object>` type arguments wrapped onto a later line (`final var map = new HashMap<Object,` / `Object>();`) | The list does not close on the reported line, and reading to the line end would take `Object` for the whole list and splice away the comma. The declaration already uses `var`, so it is skipped as such |
| Declared type arguments that wrap onto a later line (`Map<String,` / `Integer> m = ...`) | The `<...>` never closes on the reported line, so consuming to end-of-line would delete the rest of the declaration |
| Declared type arguments whose diamond sits on a continuation line, or whose only diamond is nested in a call argument | Only a diamond on the reported line can receive them; dropping them binds the constructor to `Object`, and moving them into a nested call would retype that call |
| Declared type arguments on a declaration whose buffer no longer parses | The target cannot be verified: splicing into a chain receiver's diamond retypes that receiver, and dropping them rebinds the variable |
| Multi-line block comment between the type and the name, or between the name and its `=` | The comment does not close on the reported line, so the declaration shape cannot be confirmed |
| Multi-variable declaration (`int a = 1, b = 2;`, which the check reports only as a warning) | One `var` cannot serve several declarators |
| A required conversion of a primitive declaration whose initializer no longer pins its type (`final long y = x;` with `int x`, once a sibling fix removed the `(long)` the check saw) | `var` would make `y` an `int`, so the explicit type stays. On the next run the declaration is reported as a suggestion instead |
| A required conversion of a reference declaration, a primitive array included, whose initializer no longer proves the declared type (`final List raw = typed;` with `List<String> typed`, once a sibling fix removed the `(List)` the check saw) | `var` would bind `List<String>` rather than the raw `List`, so the explicit type stays. The proof is re-read on the current text with the file's own imports, and for this row and the one above, a re-read that throws or overflows, or a buffer that no longer parses, counts as no proof, so the type stays then too and the conversion is retried on the next pass. In the full pipeline the cast removal already holds the conversion back to the next pass, where it is a suggestion, so this guards a fixer call made on the stale event |
| Declaration already uses `var` | Nothing to convert: the `<Object>` diamond or explicit-array rewrite did not apply (`var x = factory<Object>()` has no `new`), and the advisory generic-type-info warning never reaches the fixer |
| Non-`Object` generic type arguments (`<String>`) | The diamond rewrite only collapses `<Object>` |
| Open-paren join: trailing content on the `(` line, a `(` that is only a comment's own trailing character, a `(` line that starts with an annotation, or non-annotation content between the `(` and the declaration | Joining would swallow the clause into a comment or move the declaration inside one |

Part of [auto-fix coverage](../auto-fix-coverage.md).
