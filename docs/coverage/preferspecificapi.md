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
so `s.replaceAll(/*c*/"x", y)` keeps it), but a rewrite that would discard one refuses: collapsing
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
| `.equals("")` | `.isEmpty()` | Yes. A `CharSequence` receiver that is not a `String` is only reported at API 35+ |
| `.indexOf(str) != -1` / `>= 0` etc. | `.contains(str)` / `!.contains(str)` | No (see "Reported but not fixed") |
| `.indexOf("x")` / `.lastIndexOf("x")` (length-1) | `.indexOf('x')` / `.lastIndexOf('x')` | Yes, when the receiver declares the `char` overload (see "Not flagged by check"), and the one character has a char spelling |
| `.keySet().contains(k)` | `.containsKey(k)` | Yes |
| `.replaceAll("literal", x)` | `.replace("literal", x)` | Yes |
| `.size() == 0` / `.length() == 0` etc. | `.isEmpty()` / `!.isEmpty()` | Yes (any receiver shape outside grouping parens). A `CharSequence` receiver that is not a `String` is only reported at API 35+ |
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
| `.strip().isEmpty()` | `.isBlank()` | Yes |
| `.strip().length() == 0` | `.isBlank()` | Yes (including reversed `0 == ...` form) |
| `.strip().length() <= 0` | `.isBlank()` | Yes |
| `.strip().length() != 0` / `> 0` | `!receiver.isBlank()` | Yes (any receiver shape outside grouping parens) |
| Reversed strip forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (any receiver shape outside grouping parens) |
| `.trim().isEmpty()` | `.isBlank()` | Yes |
| `.trim().length() == 0` | `.isBlank()` | Yes (including reversed `0 == ...` form) |
| `.trim().length() <= 0` | `.isBlank()` | Yes |
| `.trim().length() != 0` / `> 0` | `!receiver.isBlank()` | Yes (any receiver shape outside grouping parens) |
| Reversed trim forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (any receiver shape outside grouping parens) |

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
| A receiver written inside grouping parens (`(a + b).trim().length() == 0`, `((List<?>) o).stream().findFirst().isPresent()`) | the receiver cannot be re-emitted with its grouping intact. Whitespace between the receiver and its `.` is fine (`list .size() == 0` is rewritten) |
| A rewrite that would discard a comment written inside the span it replaces | the comment has nowhere to go once the span collapses |
| A buffer that does not parse, or a position no node starts at | there is no tree to locate a rule in |

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

Part of [auto-fix coverage](../auto-fix-coverage.md).