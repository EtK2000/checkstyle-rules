# PreferSpecificApiCheck auto-fix coverage

Detection paths grouped by minSdk gate. Patterns that require structural changes are skipped.

For the assert rules, a static import for the replacement method is added only when the call is
unqualified (resolves via a static import). Qualified calls (`org.junit.Assert.assertEquals(...)`,
`Assertions.assertEquals(...)`) keep their receiver after the rewrite, so no import is added.

The class the replacement is taken from is the one a static import of `assertEquals`,
`assertNotEquals`, `assertSame` or `assertNotSame` names. An unrelated assertion import
(`org.assertj.core.api.Assertions.assertThat`) is not treated as the donor, and neither an
unrelated static wildcard nor a trailing comment on the donor's own import line hides it. A
wildcard that does cover the donor class makes the addition a no-op.

A pattern that appears only inside a string, char literal, or comment is never mistaken for a call
site.

Interior whitespace is irrelevant: `list.stream() .count()`, `list.get( 0 )` and
`Collections . emptyList()` are all rewritten. An argument
list broken across lines survives for the rules that stop the rewrite at the call's `(`; a rule
that replaces the whole call refuses instead.

A comment is never deleted. A rewrite may carry one along (the argument text it copies is verbatim,
so `s.replaceAll(/*c*/"x", "y")` keeps it), but a rewrite that would discard one refuses: collapsing
`list.stream()/*c*/.count()` to `list.size()` has nowhere to put the comment, so the violation is
left unfixed.

A line carrying several candidates rewrites only the one the check accepted: a `File.length() == 0`
beside a `List.size() == 0`, or a `Map.get(0)` beside a `List.get(0)`.

## No minSdk gate

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `assertEquals(true/false/null, x)` | `assertTrue`/`assertFalse`/`assertNull(x)` | Yes (2-arg and 3-arg forms) |
| `assertNotEquals(true/false/null, x)` | Inverted assertion | Yes (2-arg and 3-arg forms) |
| `assertSame(null, x)` / `assertNotSame(null, x)` | `assertNull`/`assertNotNull(x)` | Yes (2-arg and 3-arg forms) |
| `.collect(Collectors.toList())` | `.toList()` | Yes |
| `.collect(Collectors.toUnmodifiableList())` | `.toList()` | Yes |
| `.equals("")` | `.isEmpty()` | Yes, when the receiver is proven to be a `String` (see "Proving a `String` receiver" below) |
| `.indexOf(str) != -1` / `>= 0` etc. | `.contains(str)` / `!.contains(str)` | No (see "Reported but not fixed") |
| `.indexOf("x")` / `.lastIndexOf("x")` (length-1) | `.indexOf('x')` / `.lastIndexOf('x')` | Yes, when the receiver declares the `char` overload (see "Not flagged by check"), and the one character has a char spelling |
| `.keySet().contains(k)` | `.containsKey(k)` | Yes |
| `.replaceAll("literal", "replacement")` | `.replace("literal", "replacement")` | Yes, when the receiver is proven to be a `String` (see "Proving a `String` receiver" below). A `\` escaping a character is dropped and `$0` (with any `0`s after it) becomes the pattern, since that is what `replaceAll` inserts: `replaceAll("x", "a\\\\b")` becomes `replace("x", "a\\b")` and `replaceAll("foo", "[$0]")` becomes `replace("foo", "[foo]")`. Every other spelling is kept as written. A text block or a concatenation of literals is rewritten only when it spells no `\` or `$` (see "Not flagged by check (`replaceAll` replacement)") |
| `.size() == 0` / `.length() == 0` etc. | `.isEmpty()` / `!.isEmpty()` | Yes (any receiver shape outside grouping parens), including a `trim()` receiver (`s.trim().length() == 0` becomes `s.trim().isEmpty()`). A `CharSequence` receiver that is not a `String` is only reported at API 35+ |
| `.stream().count()` | `.size()` | Yes |
| `.stream().findFirst().isPresent()` | `!receiver.isEmpty()` | Yes (any receiver shape outside grouping parens) |
| `.values().contains(v)` | `.containsValue(v)` | Yes |

## API 24+ (MIN_SDK_FOR_EACH)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Collections.sort(list)` | `list.sort(null)` | Yes; the list argument is parenthesized when it is not a primary expression, because the rewrite moves it into receiver position (`Collections.sort((List<String>) o)` -> `((List<String>) o).sort(null)`) |
| `Collections.sort(list, cmp)` | `list.sort(cmp)` | Yes, same parenthesization |
| `.stream().forEach(...)` | `.forEach(...)` | Yes |

## API 30+ (MIN_SDK_COLLECTION_FACTORY)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Arrays.asList(...)` | `List.of(...)` | Yes |
| `Collections.emptyList()` | `List.of()` | Yes |
| `Collections.emptyMap()` | `Map.of()` | Yes |
| `Collections.emptySet()` | `Set.of()` | Yes |
| `Collections.singleton(x)` | `Set.of(x)` | Yes |
| `Collections.singletonList(x)` | `List.of(x)` | Yes |
| `Collections.singletonMap(k, v)` | `Map.of(k, v)` | Yes |

## API 31+ (MIN_SDK_COPY_OF)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Collections.unmodifiableList(x)` | `List.copyOf(x)` | Yes |
| `Collections.unmodifiableList(Arrays.asList(...))` | `List.of(...)` | Yes |
| `Collections.unmodifiableMap(x)` | `Map.copyOf(x)` | Yes |
| `Collections.unmodifiableSet(x)` | `Set.copyOf(x)` | Yes |

## API 33+ (MIN_SDK_IS_BLANK / MIN_SDK_TO_ARRAY_GENERATOR)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `.toArray(new Type[0])` | `.toArray(Type[]::new)` | Yes (skips multi-dimensional and annotated types) |
| `.strip().isEmpty()` | `.isBlank()` | Yes, when the receiver is proven to be a `String` (see "Proving a `String` receiver" below) |
| `.strip().length() == 0` | `.isBlank()` | Yes (including reversed `0 == ...` form), same receiver gate |
| `.strip().length() <= 0` | `.isBlank()` | Yes, same receiver gate |
| `.strip().length() != 0` / `> 0` | `!receiver.isBlank()` | Yes, same receiver gate |
| Reversed strip forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes, same receiver gate |

## API 34+ (MIN_SDK_FORMATTED)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `String.format("literal", args)` | `"literal".formatted(args)` | Yes |
| `String.format("literal")` (single argument, a string literal whose decoded characters hold no `%`) | the literal directly | Yes |

## API 35+ (MIN_SDK_GET_FIRST_LAST)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `.get(0)` | `.getFirst()` | Yes |
| `.get(size() - 1)` | `.getLast()` | Yes (receivers must textually match, and the receiver must be side-effect free) |
| `.remove(0)` | `.removeFirst()` | Yes, when the receiver is a `java.util.List` (see "Not flagged by check") |
| `.remove(size() - 1)` | `.removeLast()` | Yes, same gate (receivers must textually match, and the receiver must be side-effect free) |

## Reported but not fixed

| Pattern | Reason |
| --- | --- |
| `.indexOf(str) != -1` and its five other comparison forms | `contains` takes the needle, not the comparison, so the edit restructures the expression rather than substituting a span |
| A call or comparison whose rewritten span crosses a line break (`list.size()\n== 0`, `Collections.sort(\n list\n)`) | the replacement would have to collapse lines, which can push the result past the line limit and contend with the formatting fixers; the reported-but-unfixed shapes include `.stream()` chains, zero-comparisons, `Collections.sort`, `String.format` and `findFirst().isPresent()` broken across lines |
| `r.get(r.size() - 1)` / `r.remove(r.size() - 1)` where `r` has side effects (`getList().get(getList().size() - 1)`) | the source evaluates `r` twice and `getLast()` evaluates it once, so the rewrite would change how many times the receiver runs |
| A receiver written inside grouping parens (`((List<?>) o).stream().findFirst().isPresent()`) | the receiver cannot be re-emitted with its grouping intact. Whitespace between the receiver and its `.` is fine (`list .size() == 0` is rewritten) |
| A rewrite that would discard a comment written inside the span it replaces | the comment has nowhere to go once the span collapses |
| A buffer that does not parse, or a position no node starts at | there is no tree to locate a rule in |

## Proving a `String` receiver

The `replaceAll`, strip and `.equals("")` rewrites bind to `String`'s own methods, so they fire only
on a receiver the syntax tree and the classpath prove to be a `String`; one whose type is unknown is
refused. A receiver is proven when it is:

- a string literal or text block;
- a name with a written `String` type: a parameter, a local, a field (`this.label`, `holder.name`
  for a same-file `Holder`), an `instanceof` pattern (`o instanceof String s`), an explicit
  for-each variable (`for (String n : names)`) or an explicitly typed lambda parameter;
- a `var` local whose initializer is proven (`var t = "  "`, `var t = String.valueOf(value)`);
- a cast to `String` (one bound only), `new String(...)`, or an element of a `String[]`;
- a call whose receiver's own type is proven and whose every candidate returns `String`: a classpath
  method read by reflection with the receiver's type arguments substituted (`map.get(k)` on a
  `Map<String, String>`, `getName()` in a `Thread` subclass, `super.toString()` through
  `java.lang.Object` when the class extends nothing), a same-file method, or both, a covariant
  override read at its own return type (`String source(T)` over `CharSequence source(T)` in
  `Derived<T> extends Base<T>`, `String subSequence(int, int)` in a `CharSequence` implementation).
  A bare call binds in the innermost enclosing class that has a method of that name (JLS 15.12.1),
  a private method or an interface's static method of a supertype not counting, and only failing one
  through the static imports.

Refused: an implicitly typed lambda parameter (`names.removeIf(n -> n.strip().isEmpty())`), a `var`
local whose initializer is not proven (`var t = attr.getValue()`), a `var` for-each variable over a
collection, and a call into a supertype or a static-import owner that reflection cannot load
(`name()` in `extends UnresolvableBase`, `label()` with `import static com.nonexistent.Texts.label`,
single-member or on demand), since the unread class may declare it with another return type. A name
is refused where something the file does not show may bind it instead of the declaration it shows:
an `instanceof` or record pattern of the enclosing method declaring that name anywhere outside the
two scopes read (an `if`'s then-branch and an `&&`'s right operand), and a non-private field of that
name on a classpath supertype of a class the name is read inside, or any supertype nothing resolves
(see the table below). So is a receiver whose proof would read more than 256 nested expressions
(a chain of that many calls, or of `var` locals each initialized by the last), or whose type spells
out to more than 4096 characters (a type argument that doubles at every supertype,
`C1<T> extends C0<Map<T, T>>`). A simple type name is resolved the way Java scopes it, a member
type an enclosing class inherits from a classpath supertype included (`Entry` inside a class implementing `Map`, `EnumDesc` inside an enum,
and a package-private one when the supertype is in this file's package, so `String` inside a
subclass of a same-package class declaring a package-private `String` is not `java.lang.String`),
so a parameter type spelled like an import is not mistaken for it.

## Not flagged by check (receiver resolution)

The collection-shape rules (`contains`, `getFirst`, `getLast`, `removeFirst`, `removeLast`) only
fire when reflection can confirm the receiver's type actually declares the target method. Guessing
would rewrite to a method that does not exist.

`.remove(0)` and `.remove(size() - 1)` go further and require the receiver to be a
`java.util.List`. The method name alone is not enough: `Deque` declares `remove(Object)`, so
`pending.remove(0)` removes the *value* `0` while `pending.removeFirst()` removes the first
element, which compiles and silently differs. The erased signature alone is not enough either:
`java.awt.Container` and `javax.swing.DefaultListModel` both declare a positional `remove(int)`
without declaring `removeFirst()`, so gating on `remove(int)` emits a call that does not compile.
`List` is the only JDK interface that answers both halves, since `remove(int)` is positional there
by contract and `removeFirst()`/`removeLast()` are guaranteed at the API level this section
asserts. Asking the receiver rather than asking reflection for `removeFirst()` also keeps the rule
independent of the JDK Gradle runs on, where `List.removeFirst()` only exists from 21.

`.indexOf("x")` confirms the erased *signature* rather than the method name, because it retypes its
argument and so changes which overload the call selects: it requires `indexOf(int)`
(`indexOf(int, int)` for the 2-argument form). `List.indexOf(Object)` takes a `char` as a boxed
`Character` and answers `-1`, and `StringBuilder.indexOf(String)` stops compiling.

Both rules therefore also refuse a receiver they cannot resolve, where `getFirst`/`getLast` fall
open: a wrong `getFirst()` has no overload to land on and fails to compile, so it is loud rather
than silent.

| Pattern | Reason |
| --- | --- |
| Receiver whose type resolves to a type this file declares (or is otherwise off the checkstyle classpath) | Check does not fire: reflection cannot load the type, so the target method cannot be confirmed |
| Receiver whose type is not written down and cannot be inferred (chained call, dotted factory) | `getFirst`/`getLast`/`contains` fire anyway: the type is unknown rather than known-unsuitable, so the rule falls open. `remove`/`indexOf` do not fire: an unknown type cannot show the overload they would bind to exists |
| Receiver that resolves to anything but a `java.util.List` (`Deque`, `Set`, `Collection`, and the positional-but-not-a-`List` `java.awt.Container` and `javax.swing.DefaultListModel`) | `remove` does not fire: the rewrite would either select a different overload or name a method the receiver does not declare |
| Receiver that resolves but declares no `indexOf(int)` (`List`, `StringBuilder`) | `indexOf` does not fire: the rewrite would select a different overload |
| `.indexOf("x")` with three or more arguments | Check does not fire: no `char` overload of that arity is confirmed |
| `.indexOf` of a one-character literal spelled with a unicode escape and more, such as a backslash written as a unicode escape before an `n` | Check does not fire: javac reads the char literal copied from it as one character, but checkstyle's lexer does not, so the rewritten file would no longer parse |
| `.equals("")` on a receiver that is not a `String` (`StringBuilder`, `StringBuffer`, `CharBuffer`, `CharSequence`, `List`, `Map`, `Optional`), or is not proven to be one ("Proving a `String` receiver") | Check does not fire: `equals("")` is only true for a `String`, so on an empty `StringBuilder` it is `false` where `isEmpty()` is `true`. A field read through a same-file variable (`other.name.equals("")`) and a same-file call returning `String` (`getName().equals("")`) are proven and reported |
| `.replaceAll`, a strip form or `.equals("")` on a name an `instanceof` or record pattern of the enclosing method declares outside the two scopes read, an `if`'s then-branch and an `&&`'s right operand: after a negated `if` that returns, in that `if`'s `else`, in a `case`, in a conditional's arm, or in an `\|\|`'s right operand (`if (!(value instanceof Doc text)) return;` then `text.strip().isEmpty()`, beside a `String text` field) | Check does not fire: there the name may be the pattern rather than the field, and `Doc` has no `isBlank`. Any pattern of that name in the enclosing method, constructor, initializer or field initializer is enough, so a field read after an unrelated `if (o instanceof Doc text) { ... }` is refused too. The two scopes read are reported (`value instanceof String text && text.strip().isEmpty()`) |
| `.replaceAll`, a strip form or `.equals("")` on an outer variable read inside a class whose classpath supertype has a non-private field of that name, or whose supertype nothing resolves (`text.strip().isEmpty()` in `Inner extends DocField`, with `protected Doc text` on the classpath class `DocField`, or in `Inner extends UnresolvableBase`) | Check does not fire: the inherited field shadows the outer variable (JLS 6.4.1), and nothing in the file shows it. An outer name the classpath supertypes do not declare is reported (`label.strip().isEmpty()` in the same `Inner`) |
| `.replaceAll` or a strip form on an intersection cast (`((String & Comparable<String>) value).strip()`) | Check does not fire: a cast with more than one bound is not read as any one of them |
| `.replaceAll` on a written type that is not `String`, such as a type this file declares with a `replaceAll` of its own, or on a bare or `this`-qualified call to a method this file declares returning one (`text().replaceAll(...)`) | Check does not fire: only `String`'s `replaceAll` is known to mean what `replace` does. A bare or `this`-qualified same-file call is read through its declared return type, so one returning `String` is still reported |
| `.strip().isEmpty()` and the other strip forms on a receiver that is not a `String` or cannot be shown to be one: a type this file declares (`value.strip()` for a `Text value`, a `Text` constant), a bare or `this`-qualified call to a method this file declares returning one (its own, a record component's accessor or one inherited from a same-file supertype), a parenthesized expression (`(a + b).strip()`) | Check does not fire: only `String`'s `strip` and `isEmpty` are known to mean what `isBlank` does, and a receiver whose type is written somewhere the check does not read could be anything. A field read through a same-file variable (`holder.name.strip()`), an array element, a cast and `new String(chars)` are proven and reported, and so is a `var` local whose initializer is proven; an implicitly typed lambda parameter is not |
| `.replaceAll` or a strip form on a call that is not shown to return `String`: one made on a receiver whose type nothing resolves (`attribute.getValue().replaceAll(...)`, `holder.source().strip()`, `h.source().strip()` on a lambda parameter), a same-file call reached at an arity only its varargs or receiver parameter admits (`template()`, `template("a")` or `template("a", "b")` for `Template template(String... parts)`, `label()` for `Text label(Row this)`), or a `super`-qualified call to a same-file supertype's method (`super.source()` for `Text source()`) | Check does not fire: the rewrite binds to the returned type's own method, which need not exist, so `h.source().isBlank()` would not compile. A varargs parameter takes any number of arguments from none up and a receiver parameter takes none, so a same-file method is matched at every arity it admits, and a `super` call is read through every same-file supertype of the enclosing type. A same-file method returning `String` at any of those arities, and a `super` call no supertype declares (`super.toString()`), are still reported |
| `.replaceAll` or a strip form on a bare, `this`-qualified or `super`-qualified call to a method inherited from a supertype this file does not declare that returns anything but `String` (`text().replaceAll(...)` in `Leaf extends DocSource`, with `Doc text()` on the classpath type `DocSource`): the class's own supertype or one reached through a same-file supertype, an enclosing class, an anonymous class's `new` type or an implemented interface's default method, protected and static methods included | Check does not fire: reflection reads the inherited method's return type, and `Doc` need not have `replace` or `isBlank`. A bare call binds in the innermost enclosing class with a method of that name, of any arity (JLS 15.12.1), where those methods are read beside the same-file ones, so a same-file `String text(String)` does not make `text(5)` reportable beside an inherited `Doc text(int)`, and a nested class's inherited `text()` hides an enclosing class's `String text()`. A same-file override narrowing such a method's return type to `String` overrides it, the same parameter types, and is reported. An inherited method of another arity is not read, so `text("a", "b")` with a same-file `String text(String, String)` is reported |
| `.replaceAll` or a strip form on a bare, `this`-qualified or `super`-qualified call to a method inherited from a supertype this file does not declare that returns a type variable not shown to be `String`: one the supertype binds to another type (`value()` in `Docs extends Source<Doc>`, `get(0)` in `extends ArrayList<Template>` with `Template` a same-file class, `get()` in `ThreadLocal<Doc>`, `AtomicReference<Doc>` or `WeakReference<Doc>`, `get("k")` in `HashMap<String, Doc>`), to a type parameter nothing binds (`get(0)` in `Bounded<T extends Doc> extends ArrayList<T>`), through a same-file supertype (`Substituted extends Base<Doc>`), or one no argument binds: a diamond anonymous class (`new ArrayList<>() { ... }`) or a method's own type variable (`invokeAny(tasks)` in an `AbstractExecutorService` subclass) | Check does not fire: the call returns what the variable is bound to, and `Doc.replace` or `Template.isBlank` need not exist. A type argument naming a same-file type, a type parameter or nothing at all is never read as `String`. The inherited method also hides an enclosing class's method of that name, so a `String get(int)` around `Inner extends ArrayList<Doc>` does not make `get(0)` reportable |
| `.replaceAll` or a strip form on a bare call no enclosing class declares that a static import brings in returning anything but `String`: a method's own type variable (`requireNonNull(doc)` with `java.util.Objects.requireNonNull` imported), or another type (`create()` with `DocSource.*` imported and `static Doc create()` on `DocSource`) | Check does not fire: the call binds to the imported method (JLS 15.12.1), whose type variable is inferred from the argument or target, which is not read, so even `requireNonNull(text)` on a `String` is not reported. A class's own method of that name hides the import and is read instead, so `requireNonNull(value)` beside a same-file `String requireNonNull(Object)` is reported, while a call imported from a class reflection cannot load is not, since that class may declare it with another return type |
| `.replaceAll` or a strip form on a bare call to a method a same-file supertype declares but a subtype does not inherit, a private method or an interface's static method (`make()` in `Inner extends Base` with `private String make()` on `Base`, or in `Inner implements Maker` with `static String make()` or `private String make()` on `Maker`), beside an enclosing class's `Doc make()` | Check does not fire: neither is a member of the subtype (JLS 8.4.8), so the call binds to the enclosing class's method, whose `Doc` has no `replace` or `isBlank`. An enclosing `String make()` there is reported, and so are a superclass's static method, which is inherited, and a class's or interface's own private or static method |
| `.replaceAll` or a strip form on a bare, `this`-qualified or `super`-qualified call whose nearest same-file class returns `String` at the call's arity while a supertype further up declares an overload of that arity that does not (`text("k").replaceAll(...)` in `Leaf extends Derived`, with `String text(int)` on `Derived` and `Doc text(String)` on `Base`) | Check does not fire: the call may bind to the overload further up, since every level's overloads at that arity are read, and `Doc.replace` would not exist. A supertype's method is dropped only when a nearer level declares the same parameter types naming the same types from both declarations, which overrides it, so a covariant override (`String text(String)` over `CharSequence text(String)`, also typed by a same-file class or an enclosing class's type variable both levels see) is read at its most derived return type and still reported, as is a call whose every level returns `String`. A record component's accessor is read beside an overload whose varargs parameter takes no argument too, so `text()` on `record Labelled(Doc text)` with `String text(String... parts)` is not reported, while on `record Named(String text)` it is |
| `.replaceAll` or a strip form on a bare call whose nearest same-file class declares parameter types spelled like a supertype's method's but naming different types: the class's own type variable (`String text(T)` in `Derived<T extends CharSequence> extends Base<Integer>` beside `Doc text(T)` on `Base<T>`), or a member type each level nests under one simple name (`String text(Key)` beside `Doc text(Key)`, each `Key` its own class's) | Check does not fire: the spelling alone does not show an override, so both methods are read and the call may bind to the supertype's (`text(5)` selects `Base.text(Integer)`), whose `Doc` has no `replace` or `isBlank`. Each parameter type has to name the same type from both declarations: the same type parameter, the same same-file class or the same classpath class; a name nothing resolves is never taken to be the same. A real override typed by a type variable the hierarchy passes through is read as one and reported (`String source(T)` in `Derived<T> extends Base<T>` over `CharSequence source(T)`), while a generic method's own type variable is never proven, so `<U> String label(U)` over `<U> CharSequence label(U)` is not |
| `.replaceAll` or a strip form on a bare call whose nearest same-file class declares parameter types spelled like a supertype's method's while either declaration sees a supertype this file does not declare, through its own class, a same-file supertype of it, or a class enclosing either (`String text(Entry)` in `Derived extends Base implements Map<String, String>` beside `Doc text(Entry)` on `Base`, with `KeyStore.Entry` imported), an enum included | Check does not fire: a member type that supertype declares (`Map.Entry`, and `Enum.EnumDesc` in an enum) is in scope there under its simple name and shadows an import, a same-file type or a type variable of that name further out, which nothing in this file can see, so the two methods may be overloads and the call may bind to `Base.text`, whose `Doc` has no `replace` or `isBlank`. The member types a classpath supertype brings in are read by reflection, so a real covariant override is reported when no such member type shadows its parameter types (`String source(String)` over `CharSequence source(String)` in `Derived extends Base implements Runnable`) |
| `.replaceAll` or a strip form on a bare call whose same-file overloads at the call's arity disagree on the return type (`text(1).strip()` with `Text text(int)` and `String text(String)`) | Check does not fire: the overload the call selects is not resolved, so unless every overload of that arity returns `String` the receiver may be another type, and `text(1).isBlank()` would not compile. Overloads that all return `String`, or one of another arity, are still reported |
| `.trim().isEmpty()` | Check does not fire: `trim` strips every char up to U+0020 while `isBlank` tests for whitespace, so a string of only U+0001 or only an em space (U+2003) answers differently, and `isEmpty` is already the specific call |
| `.trim().length() == 0` and the other comparisons on a receiver whose type cannot be resolved (`getText().trim()`, `obj.name.trim()`, `(a + b).trim()`) | Check does not fire: the `isEmpty` rule confirms the receiver declares it, as for any `length()` comparison |
| `.indexOf("\u0027")`, and the same shape for `\u005C` | Check does not fire: JLS 3.3 translates a unicode escape before tokenization, while checkstyle's lexer leaves it as written, so copying the escape into `'...'` hands javac `'''` or `'\'` and neither closes. Only these two characters are affected, and only in the unicode spelling: an octal escape is decoded with the token rather than ahead of it, so `.indexOf("\47")` still becomes `.indexOf('\47')`, and `.indexOf("\u0041")` still becomes `.indexOf('\u0041')` |

## Not flagged by check (single-argument `String.format`)

`String.format(value)` is not the identity: with nothing to substitute the format string is still
processed, so `String.format("%n")` yields a line separator and `String.format("50%% off")` yields
`50% off`. Only an argument the check can read and show to carry no conversion is reported.

The literal is decoded before the `%` is looked for, because a `%` can be spelled as an escape that
a scan of the source text does not see: `"\045"` is an octal `%`, and `"\u0025"` is a unicode one.
Escapes that decode to anything else are no reason to refuse, so `String.format("a\n")` is the
identity and is reported.

| Pattern | Reason |
| --- | --- |
| `String.format("%n")`, `String.format("50%% off")`, `String.format("%")` | the literal spells a conversion, so handing back the literal changes the result (or stops it throwing) |
| `String.format("50\045 off")`, `String.format("\u0025")` | the escape decodes to a `%`, which is a conversion however it is spelled |
| `String.format("\z")` | the escapes do not decode, so the characters cannot be read; no parseable file can carry one, since the lexer rejects it first |
| `String.format(fmt)`, `String.format(obj.toString())`, `String.format((String) obj)`, `String.format(a + b)` | a non-literal argument cannot be read at all, so it can never be shown to carry no conversion |
| `String.format("""...""")` | a text block is a different token, and is refused with the rest |
| `String.format` of a literal whose `%` is an octal escape begun by a backslash written as a unicode escape | javac translates the unicode escape first, so the backslash still begins the octal escape and the literal holds a `%` |

## Not flagged by check (`replaceAll` replacement)

`replaceAll` reads its replacement as a template, where `replace` inserts it as written: a `\`
escapes the next character and `$n` inserts group `n`. Only a replacement whose characters can be
read and whose template can be spelled for `replace` is reported.

| Pattern | Reason |
| --- | --- |
| A replacement that is not one literal: a variable, a constant, a call, a cast, a ternary, `null`, a parenthesized literal, or a concatenation with anything but literals | its characters cannot be read, so a `\` or `$` in it cannot be ruled out |
| A text block or a concatenation of literals that spells a `\` or `$` (`"a" + "\\$"`) | an escape can span the operands, which no single operand's rewrite can see |
| A replacement `replaceAll` throws on: `$` last or before anything but a digit (`"a$"`, `"$a"`, `"$$"`), `${name}`, a group above 0 (`"$1"`, `"$10"`), or a `\` last (`"a\\"`), however each is spelled | `replace` would return a value where `replaceAll` throws |

Part of [auto-fix coverage](../auto-fix-coverage.md).