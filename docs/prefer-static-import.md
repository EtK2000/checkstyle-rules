# Prefer Static Import for Utility Methods

## Pattern

Some static methods read better without the class qualifier when used frequently. These should use
static imports.

## Candidates

### `Predicate.not()`

Java 11+, API 33+.

```java
// bad
stream.filter(Predicate.not(String::isEmpty))

// good (with `import static java.util.function.Predicate.not;`)
stream.filter(not(String::isEmpty))
```

`Predicate.isEqual()` is the only other static method on `Predicate`, but it's rarely used in
practice. `and()`, `or()`, and `negate()` are instance methods and don't apply.

### `Objects.requireNonNull()`

Java 7+, API 19+.

```java
// bad
this.name = Objects.requireNonNull(name);
this.value = Objects.requireNonNull(value);

// good (with `import static java.util.Objects.requireNonNull;`)
this.name = requireNonNull(name);
this.value = requireNonNull(value);
```

Especially in constructors with multiple fields, the static import removes a lot of noise.

### `Map.of()` / `List.of()` / `Set.of()` / `Map.entry()`

Java 9+, API 30+.

```java
// bad
Map.of("a", 1, "b", 2)
List.of("x", "y", "z")
Map.ofEntries(Map.entry("key", "value"))

// good (with static imports)
of("a", 1, "b", 2)        // ambiguous if both Map.of and List.of are imported
List.of("x", "y", "z")    // keep qualifier when ambiguous
ofEntries(entry("key", "value"))
```

This one is tricky. `of()` is ambiguous when multiple collection types are used in the same file.
Probably NOT worth enforcing as a check, only `entry()` is unambiguous enough to flag.

### `Collectors.toList()` / `Collectors.toSet()` / `Collectors.toMap()`

Java 8+, API 24+.

```java
// bad
stream.collect(Collectors.toList())
stream.collect(Collectors.groupingBy(Foo::type))

// good
stream.collect(toList())
stream.collect(groupingBy(Foo::type))
```

Note: `stream.toList()` (Java 16+, API 33+) is preferred over both forms and is already covered
by `PreferSpecificApiCheck`.

### `Math.max()` / `Math.min()` / `Math.abs()`

Java 1.0+, API 1+.

```java
// bad
int clamped = Math.max(0, Math.min(100, value));

// good
int clamped = max(0, min(100, value));
```

Only when the file uses multiple `Math` calls. A single `Math.max` doesn't benefit much from a
static import.

## Scope

A check for this would need to:
1. Find qualified static method calls (`Predicate.not(...)`, `Objects.requireNonNull(...)`)
2. Verify the method has a static import candidate (the method is `static` on the class)
3. Suggest adding a static import and using the short form

This overlaps with `PreferImportCheck` (which flags fully qualified type names). A similar approach
could work: flag qualified static calls when a static import would be cleaner.

## What NOT to flag

- Methods where the class name adds clarity: `Integer.parseInt()`, `String.valueOf()`,
  `Arrays.sort()`. These read worse without the qualifier.
- Methods where static import causes ambiguity: `of()` from multiple classes, `of()` shadowing a
  local method.
- Single-use calls: if `Math.max` appears once in a file, the import doesn't help much.

## Decision

This is more of a style preference than a correctness issue. A check could flag the most egregious
cases (`Predicate.not` and `Objects.requireNonNull` are the strongest candidates since they're
always cleaner with a static import), but a blanket "prefer static import" rule would be too noisy.

Consider starting with just `Predicate.not` as part of the `Predicate.not()` check from
`prefer-specific-api-candidates.md`, since both the lambda-to-not conversion and the static import
would happen together.