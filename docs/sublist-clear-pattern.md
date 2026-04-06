# Prefer `subList().clear()` Over Loop-Based Removal

Java 1.2+, API 1+.

## Pattern

```java
// reverse loop
for (var i = end; i >= start; --i)
    list.remove(i);

// forward loop (always removes at start, since elements shift down)
for (var i = 0; i < count; ++i)
    list.remove(start);

// size-based forward loop
while (list.size() > targetSize)
    list.remove(start);

// preferred
list.subList(start, end + 1).clear();
```

## Why Not a Check

The pattern is too varied and structurally complex for a checkstyle rule:

1. Multiple loop forms express the same operation (reverse index, forward with fixed index,
   size-based while loop)
2. Each requires matching loop structure, bounds, body, AND verifying semantic equivalence
3. Must confirm the remove index and loop bounds are consistent, which varies per variant
4. IDEs (IntelliJ) already detect this and suggest the fix
5. The pattern is rare in practice

`PreferSpecificApiCheck` can't handle this because it matches individual method calls, not
surrounding loop structure. A new check visiting loop tokens would be needed, which isn't
justified for the low frequency of occurrence.