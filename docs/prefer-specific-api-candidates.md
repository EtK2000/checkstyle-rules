# Prefer Specific API Candidates

Additional "prefer X over Y" patterns not yet covered by existing checks. Grouped by complexity.

## Simple method replacements (could extend PreferSpecificApiCheck)

### `String.isBlank()` over `trim().isEmpty()`

Java 11+, API 33+.

```java
// bad
s.trim().isEmpty()
s.trim().length() == 0

// good
s.isBlank()
```

`isBlank()` also handles Unicode whitespace, while `trim()` only handles ASCII.

### `String.formatted()` over `String.format()`

Java 15+, API 34+.

```java
// bad
String.format("Hello %s, you are %d", name, age)

// good
"Hello %s, you are %d".formatted(name, age)
```

Only when the format string is a literal (can't rewrite `String.format(variable, args)`).

### `String.repeat()` over manual loop/StringBuilder

Java 11+, API 33+.

```java
// bad
final var sb = new StringBuilder();
for (var i = 0; i < n; ++i)
    sb.append("x");
// or
String result = "";
for (var i = 0; i < n; ++i)
    result += "x";

// good
"x".repeat(n)
```

Detection is harder (multi-statement pattern), may not be worth it as an AST check.

### `Collection.toArray(Type[]::new)` over `toArray(new Type[0])`

Java 11+, API 33+.

```java
// bad
list.toArray(new String[0])

// good
list.toArray(String[]::new)
```

The method reference form is clearer about intent.

### `Predicate.not()` over negated lambda

Java 11+, API 33+.

```java
// bad
stream.filter(s -> !s.isEmpty())

// good (with static import of Predicate.not)
stream.filter(not(String::isEmpty))
```

Only when the lambda body is a single negated method reference candidate.

### `List.of()` / `Set.of()` over `Arrays.asList()` when unmodifiable is fine

Java 9+, API 30+.

```java
// bad
Arrays.asList("a", "b", "c")  // returns mutable-ish fixed-size list

// good
List.of("a", "b", "c")  // returns unmodifiable list
```

Only safe when the code doesn't mutate the list. Detecting mutation requires data flow analysis, so
this may need to be a warning rather than an error. Note: `Collections.singletonList` and
`Collections.emptyList` are already covered by PreferSpecificApiCheck.

## Map idiom replacements (need multi-statement pattern detection)

### `Map.getOrDefault()` over containsKey + get

Java 8+, API 24+.

```java
// bad
map.containsKey(key) ? map.get(key) : defaultValue
// or
if (map.containsKey(key))
    return map.get(key);
return defaultValue;

// good
map.getOrDefault(key, defaultValue)
```

The ternary form is detectable via AST. The if-else form is harder.

### `Map.computeIfAbsent()` over containsKey + put + get

Java 8+, API 24+.

```java
// bad
if (!map.containsKey(key))
    map.put(key, new ArrayList<>());
map.get(key).add(value);
// or
var list = map.get(key);
if (list == null) {
    list = new ArrayList<>();
    map.put(key, list);
}

// good
map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
```

Complex multi-statement detection. Possibly too hard for an AST check.

### `Map.putIfAbsent()` over containsKey + put

Java 8+, API 24+.

```java
// bad
if (!map.containsKey(key))
    map.put(key, value);

// good
map.putIfAbsent(key, value);
```

Detectable when the if-body is a single `put` call on the same map with the same key.

### `Map.merge()` over get + check + put

Java 8+, API 24+.

```java
// bad
var current = map.get(key);
map.put(key, current == null ? value : current + value);

// good
map.merge(key, value, Integer::sum);
```

Very hard to detect generically. Probably not worth an AST check.

## Null-safety patterns

### `Objects.requireNonNull()` over manual null check

Java 7+, API 19+.

```java
// bad
if (param == null)
    throw new NullPointerException();
// or
if (param == null)
    throw new NullPointerException("param");

// good
Objects.requireNonNull(param);
Objects.requireNonNull(param, "param");
```

Only in constructors/method entry points (guard clauses). Don't flag null checks that do something
other than throw NPE.

### `Objects.equals()` over null-safe manual comparison

Java 7+, API 19+.

```java
// bad
(a == b) || (a != null && a.equals(b))
a != null ? a.equals(b) : b == null

// good
Objects.equals(a, b)
```

### `Objects.hash()` over manual hash computation

Java 7+, API 19+.

```java
// bad
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + field1.hashCode();
    result = 31 * result + field2.hashCode();
    return result;
}

// good
@Override
public int hashCode() {
    return Objects.hash(field1, field2);
}
```

Only in `hashCode()` overrides. Records don't need this (auto-generated).

## Optional anti-patterns

All `Optional` APIs: Java 8+, API 24+.

### `Optional.orElse()` / `orElseGet()` over isPresent + get

```java
// bad
optional.isPresent() ? optional.get() : defaultValue

// good
optional.orElse(defaultValue)
// or if defaultValue is expensive:
optional.orElseGet(() -> expensiveComputation())
```

### `Optional.ifPresent()` over isPresent + get

```java
// bad
if (optional.isPresent())
    doSomething(optional.get());

// good
optional.ifPresent(this::doSomething);
```

### `Optional.map()` over isPresent + get + transform

```java
// bad
if (optional.isPresent())
    return Optional.of(optional.get().transform());
return Optional.empty();

// good
optional.map(Value::transform)
```

## Priority

Rough implementation priority based on frequency and detection difficulty:

1. `String.isBlank()` over `trim().isEmpty()` (Java 11+, API 33+) - very common, easy detection
2. `Map.getOrDefault()` ternary form (Java 8+, API 24+) - common, detectable
3. `Optional.orElse()` ternary form (Java 8+, API 24+) - common, detectable
4. `Map.putIfAbsent()` (Java 8+, API 24+) - common, detectable
5. `Objects.requireNonNull()` (Java 7+, API 19+) - common in constructors, detectable
6. `String.formatted()` (Java 15+, API 34+) - common, easy detection (receiver is String literal)
7. `Objects.equals()` (Java 7+, API 19+) - moderately common, ternary/or-chain detection
8. Everything else - less common or harder to detect