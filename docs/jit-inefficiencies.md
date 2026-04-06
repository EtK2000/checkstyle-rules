# JIT-Unfriendly Patterns

Code patterns that prevent JIT optimization or cause unnecessary allocations. These are detectable
via AST and could become checks.

## String concatenation in loops

```java
// bad - creates a new String object each iteration
String result = "";
for (var item : list)
    result += item + ",";

// good
final var sb = new StringBuilder();
for (var item : list)
    sb.append(item).append(',');
final var result = sb.toString();

// also good (when joining with delimiter)
final var result = String.join(",", list);
```

Java 9+ JIT can optimize simple concatenation chains (`a + b + c`) into `StringBuilder` calls via
`invokedynamic`, but NOT across loop iterations. Each `+=` in a loop allocates a new `String`.

Detection: find `+=` on a `String` variable inside any loop (`for`, `while`, `do-while`). Also flag
`result = result + expr` inside loops.

## Autoboxing in loops

```java
// bad - autoboxes every iteration
long sum = 0;
for (var item : list)
    sum += item.getValue();  // fine if getValue() returns long

// bad - boxing for no reason
List<Integer> values = ...;
int total = 0;
for (var v : values)
    total += v;  // unboxes Integer to int each iteration, but this is unavoidable with List<Integer>

// actually bad - unnecessary boxing
Long sum = 0L;  // boxed Long
for (var item : list)
    sum += item.getValue();  // unbox Long, add, rebox to Long each iteration

// good
long sum = 0L;  // primitive
for (var item : list)
    sum += item.getValue();
```

Detection: find accumulator variables with boxed types (`Long`, `Integer`, `Double`, etc.) that are
modified inside loops. The fix is to use the primitive type instead. Note: this overlaps with a
general "prefer primitives over boxed types for local variables" rule.

## `String.matches()` for repeated regex

```java
// bad - compiles the regex every call
for (var line : lines)
    if (line.matches("\\d+"))
        process(line);

// good - compile once
final var pattern = Pattern.compile("\\d+");
for (var line : lines)
    if (pattern.matcher(line).matches())
        process(line);
```

Java 1.4+, API 1+.

`String.matches()` calls `Pattern.compile()` internally every time. In a loop, this recompiles the
same regex on every iteration.

Detection: find `String.matches()` calls inside loops. Also flag `String.replaceAll()` and
`String.split()` inside loops (both compile a regex internally), though `replaceAll` is already
flagged by `PreferSpecificApiCheck` for the non-regex case.

## `Collections.sort()` vs `List.sort()`

```java
// bad - wraps in an unnecessary utility call
Collections.sort(list);
Collections.sort(list, comparator);

// good - direct method on List
list.sort(null);  // natural ordering
list.sort(comparator);
```

Java 8+, API 24+.

`Collections.sort()` delegates to `List.sort()` anyway. Using `List.sort()` directly avoids the
indirection and is clearer.

Detection: find `Collections.sort()` calls. Could go in `PreferSpecificApiCheck`.

## `Iterator` loop vs enhanced for

```java
// bad - verbose, error-prone
final var it = list.iterator();
while (it.hasNext()) {
    final var item = it.next();
    process(item);
}

// good
for (var item : list)
    process(item);
```

Java 5+, API 1+.

The enhanced for loop compiles to the same iterator code but is shorter and prevents bugs (calling
`next()` twice, forgetting `hasNext()`). Exception: when you need `Iterator.remove()` or the
iterator itself, the explicit form is necessary.

Detection: find `while (it.hasNext())` patterns where the iterator is only used for `next()` inside
the loop body (no `remove()` or other iterator methods).

## `new String(literal)` / `new String(existingString)`

```java
// bad - creates an unnecessary copy
final var s = new String("hello");
final var copy = new String(existingString);

// good
final var s = "hello";
final var copy = existingString;  // strings are immutable, no need to copy
```

Detection: find `new String(STRING_LITERAL)` and `new String(stringVariable)`. The only valid use
of `new String()` is to force a new object for identity comparison (extremely rare) or to trim the
backing char array of a substring (irrelevant since Java 7u6).

## `toArray()` with pre-sized array

```java
// bad - pre-sizing is slower than empty array on modern JVMs
list.toArray(new String[list.size()])

// good - empty array lets the JVM optimize
list.toArray(new String[0])

// also good (Java 11+, API 33+)
list.toArray(String[]::new)
```

Counter-intuitive: `new String[0]` is faster than `new String[list.size()]` on modern JVMs because
the JVM can skip zeroing the array when it knows the exact size from `toArray()`. The pre-sized
version forces a zero-fill that gets immediately overwritten.

Detection: find `toArray(new Type[expr])` where `expr` is not `0`. The method reference form is
covered in `prefer-specific-api-candidates.md`.

## `synchronized` on method vs block

```java
// bad - locks on `this`, wider scope than needed
public synchronized void process() {
    // only part of this needs synchronization
    updateSharedState();
    doExpensiveComputation();  // doesn't need the lock
}

// good - narrow lock scope
public void process() {
    synchronized (lock) {
        updateSharedState();
    }
    doExpensiveComputation();
}
```

This is more of a correctness/performance review item than a mechanical check. Hard to detect when
the entire method body needs the lock vs only part of it. Probably not worth an AST check.

## `Enum.values()` in loops / hot paths

Java 5+, API 1+.

```java
// bad - clones the backing array on every call
for (var i = 0; i < 100; ++i)
    for (var color : Color.values())
        process(color);

// good - cache the array
private static final Color[] VALUES = Color.values();
// ...
for (var i = 0; i < 100; ++i)
    for (var color : VALUES)
        process(color);
```

`Enum.values()` allocates a new array every call. In a loop this adds up. Detection: find
`.values()` calls on enum types inside loops.

## `Map.keySet()` iteration with `map.get()` (double lookup)

Java 1.2+, API 1+.

```java
// bad - two hash lookups per iteration
for (var key : map.keySet()) {
    final var value = map.get(key);
    process(key, value);
}

// good - single lookup per iteration
for (var entry : map.entrySet()) {
    process(entry.getKey(), entry.getValue());
}
```

Detection: for-each over `.keySet()` where `.get()` is called on the same map inside the loop body.

## `FileInputStream` / `FileOutputStream`

Java 7+ for replacement, API 26+ for replacement.

```java
// old API
try (var in = new FileInputStream(file)) { ... }

// preferred - consistent with java.nio.file
try (var in = Files.newInputStream(file.toPath())) { ... }
```

Note: on Java 18+, the finalizer in `FileInputStream`/`FileOutputStream` was replaced with a
`Cleaner`-based approach, so the original GC overhead argument no longer applies on modern JVMs.
The main reason to prefer `Files.newInputStream()` is API consistency with `java.nio.file` and
better option support (e.g., `StandardOpenOption`). Lower priority than the other patterns here.

Detection: any `new FileInputStream(` or `new FileOutputStream(` constructor call.

## `String.indexOf("x")` with single-character string

Java 1.0+, API 1+.

```java
// bad - String comparison overhead
s.indexOf("x")
s.lastIndexOf("/")

// good - char comparison
s.indexOf('x')
s.lastIndexOf('/')
```

Detection: `indexOf` or `lastIndexOf` with a single-character string literal argument. Could go in
`PreferSpecificApiCheck`.

## `"" + value` to convert to String

Java 1.0+, API 1+.

```java
// bad - creates intermediate StringBuilder
final var s = "" + someInt;

// good
final var s = String.valueOf(someInt);
// or
final var s = Integer.toString(someInt);
```

Detection: string concatenation where one operand is the empty string literal `""`.

## Boxed primitive constructors

Removed in Java 16, but still compiles with warnings. API 1+.

```java
// bad - allocates a new object every time
new Integer(42)
new Long(100L)
new Boolean(true)

// good - uses cached instances
Integer.valueOf(42)
Long.valueOf(100L)
Boolean.TRUE
```

Detection: any `new Integer(`, `new Long(`, `new Boolean(`, `new Double(`, `new Float(`,
`new Short(`, `new Byte(`, `new Character(` constructor call.

## String concatenation inside `StringBuilder.append()`

Java 1.0+, API 1+.

```java
// bad - creates intermediate String for the concatenation
sb.append("key=" + value)

// good - chains directly
sb.append("key=").append(value)
```

Detection: `+` expression (string concatenation) as argument to `StringBuilder.append()` or
`StringBuffer.append()`.

## `StringBuffer` instead of `StringBuilder`

Java 5+, API 1+.

```java
// bad - synchronized, unnecessary overhead for local variables
final var sb = new StringBuffer();

// good - unsynchronized
final var sb = new StringBuilder();
```

Detection: `new StringBuffer(` where the variable is local (never escapes the method).

## Double-brace initialization

Java 1.1+, API 1+.

```java
// bad
final var list = new ArrayList<>() {{ add("a"); add("b"); }};

// what it actually means:
final var list = new ArrayList<>() {  // anonymous subclass of ArrayList
    {                                  // instance initializer block
        add("a");
        add("b");
    }
};

// good
final var list = new ArrayList<>(List.of("a", "b"));
// or if unmodifiable is fine (Java 9+, API 30+):
final var list = List.of("a", "b");
```

Problems with double-brace:
- Every usage generates a separate `.class` file (classloader bloat)
- The anonymous class holds an implicit reference to the enclosing instance (memory leak if the
  collection outlives the outer object, e.g., stored in a static field or passed elsewhere)
- The result is a subclass of ArrayList, not an ArrayList, which can break `equals()`,
  serialization, and type checks

Detection: instance initializer block inside an anonymous class extending a Collection or Map type.

## Repeated creation of immutable/reusable objects in methods

Java 1.0+, API 1+.

```java
// bad - recompiled/reconstructed every call
void format(Date date) {
    final var fmt = new SimpleDateFormat("yyyy-MM-dd");
    return fmt.format(date);
}

// good - hoisted to static final (thread-safe for DateTimeFormatter, not SimpleDateFormat)
private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

void format(LocalDate date) {
    return FMT.format(date);
}
```

Applies to: `DateTimeFormatter.ofPattern()`, `Pattern.compile()`, `new DecimalFormat()`,
`new ObjectMapper()`, `new Gson()`. Detection: these constructors/factory calls inside method bodies
where the argument is a constant. Hard to detect when thread-safety matters (SimpleDateFormat is NOT
thread-safe, DateTimeFormatter IS).

## Priority

Based on frequency and detection ease:

1. String `+=` in loop - very common, easy detection, clear fix
2. `String.matches()` in loop - common, easy detection
3. `String.indexOf("x")` single-char string - very common, trivial detection
4. `Map.keySet()` + `get()` in loop - common, detectable
5. `Collections.sort()` - common, trivial detection, could go in PreferSpecificApiCheck
6. String concat inside `append()` - common, easy detection
7. `"" + value` string conversion - common, easy detection
8. `StringBuffer` instead of `StringBuilder` (local) - common, easy detection
9. `new String(literal)` - uncommon but easy to detect
10. Boxed primitive constructors - easy detection, mostly obsolete code
11. `toArray(new Type[size])` - moderately common, easy detection
12. `Enum.values()` in loop - moderately common, needs type resolution
13. Double-brace initialization - uncommon, detectable (anonymous class with instance initializer)
14. Reusable object creation in methods - common but hard to detect correctly
15. `FileInputStream`/`FileOutputStream` - easy detection, lower priority (no longer a JIT issue on Java 18+)
16. Boxed accumulator in loop - moderately common, harder to detect (need type resolution)
17. Explicit iterator loop - less common in modern code, complex pattern matching
18. `synchronized` method - too subjective for a check