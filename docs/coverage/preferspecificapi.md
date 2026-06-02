# PreferSpecificApiCheck auto-fix coverage

Detection paths grouped by minSdk gate. Patterns that require structural changes are skipped (null).

For the assert rules, a static import for the replacement method is added only when the call is
unqualified (resolves via a static import). Qualified calls (`org.junit.Assert.assertEquals(...)`,
`Assertions.assertEquals(...)`) keep their receiver after the rewrite, so no import is added.

A pattern that appears only inside a string, char literal, or comment is never mistaken for a call
site, and a line that continues a text block (its closing `"""` sits before the real call) is still
rewritten. The one exception is the `.indexOf("x")` ->
`.indexOf('x')`
rewrite, which is anchored to the violation column rather than the first textual match, so an
earlier same-line literal cannot hijack it in the first place.

## No minSdk gate

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `assertEquals(true/false/null, x)` | `assertTrue`/`assertFalse`/`assertNull(x)` | Yes (2-arg and 3-arg forms) |
| `assertNotEquals(true/false/null, x)` | Inverted assertion | Yes (2-arg and 3-arg forms) |
| `assertSame(null, x)` / `assertNotSame(null, x)` | `assertNull`/`assertNotNull(x)` | Yes (2-arg and 3-arg forms) |
| `.collect(Collectors.toList())` | `.toList()` | Yes |
| `.collect(Collectors.toUnmodifiableList())` | `.toList()` | Yes |
| `.equals("")` | `.isEmpty()` | Yes |
| `.indexOf(str) != -1` / `>= 0` etc. | `.contains(str)` / `!.contains(str)` | No |
| `.indexOf("x")` / `.lastIndexOf("x")` (length-1) | `.indexOf('x')` / `.lastIndexOf('x')` | Yes |
| `.keySet().contains(k)` | `.containsKey(k)` | Yes |
| `.replaceAll("literal", x)` | `.replace("literal", x)` | Yes |
| `.size() == 0` / `.length() == 0` etc. | `.isEmpty()` / `!.isEmpty()` | Yes (negated needs simple receiver) |
| `.stream().count()` | `.size()` | Yes |
| `.stream().findFirst().isPresent()` | `!receiver.isEmpty()` | Simple receivers only |
| `.values().contains(v)` | `.containsValue(v)` | Yes |

## API 24+ (MIN_SDK_FOR_EACH)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `Collections.sort(list)` | `list.sort(null)` | Yes |
| `Collections.sort(list, cmp)` | `list.sort(cmp)` | Yes |
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
| `.strip().length() != 0` / `> 0` | `!receiver.isBlank()` | Simple receivers only (identifiers, dotted names) |
| Reversed strip forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (positive reversed); simple receivers (negated reversed) |
| `.trim().isEmpty()` | `.isBlank()` | Yes |
| `.trim().length() == 0` | `.isBlank()` | Yes (including reversed `0 == ...` form) |
| `.trim().length() <= 0` | `.isBlank()` | Yes |
| `.trim().length() != 0` / `> 0` | `!receiver.isBlank()` | Simple receivers only (identifiers, dotted names) |
| Reversed trim forms (`0 != ...` etc.) | `.isBlank()` or negated | Yes (positive reversed); simple receivers (negated reversed) |

## API 34+ (MIN_SDK_FORMATTED)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `String.format("literal", args)` | `"literal".formatted(args)` | Yes |

## API 35+ (MIN_SDK_GET_FIRST_LAST)

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `.get(0)` | `.getFirst()` | Yes |
| `.get(size() - 1)` | `.getLast()` | Yes (receivers must textually match: `r.get(r.size() - 1)`) |
| `.remove(0)` | `.removeFirst()` | Yes |
| `.remove(size() - 1)` | `.removeLast()` | Yes (receivers must textually match: `r.remove(r.size() - 1)`) |

Part of [auto-fix coverage](../auto-fix-coverage.md).