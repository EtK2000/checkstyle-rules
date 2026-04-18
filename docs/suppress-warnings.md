# Per-Type Suppression via @SuppressWarnings

Some checks support `@SuppressWarnings` annotations to suppress violations on a per-type basis.
When a type (class, enum, etc.) carries a recognized suppression key, the check skips that type
entirely, and the auto-fix task never sees the violation.

## Supported keys

| Key            | Check             | Effect                                                   |
|----------------|-------------------|----------------------------------------------------------|
| `FieldSorting` | FieldSortingCheck | Suppresses all field ordering and enum constant ordering |
| `PreferRecord` | PreferRecordCheck | Suppresses the "should be a record" suggestion           |

## Annotation forms

All standard `@SuppressWarnings` forms are supported:

```java
@SuppressWarnings("FieldSorting")                     // single value
@SuppressWarnings({"FieldSorting"})                   // array with one value
@SuppressWarnings({"FieldSorting", "unused"})         // array with multiple values
@SuppressWarnings({"unused", "FieldSorting"})         // key at any position
```

## Placement

Place the annotation on the type declaration:

```java
@SuppressWarnings("FieldSorting")
enum MyEnum {
	ZEBRA,  // no violation, entire enum is suppressed
	ALPHA
}

@SuppressWarnings("PreferRecord")
class MyClass {
	final int value;

	MyClass(int value) {
		this.value = value;
	}
	// no "should be a record" suggestion
}
```

For `FieldSortingCheck`, the annotation on an individual enum constant only suppresses field
sorting within that constant's body, not the enum constant ordering:

```java
enum Example {
	@SuppressWarnings("FieldSorting")
	ZEBRA,
	ALPHA  // still fires: constant ordering checks the enum, not individual constants

	@SuppressWarnings("FieldSorting")
	INSTANCE {
		String name;
		int count;  // suppressed: field sorting within this constant's body
	}
}
```

To suppress enum constant ordering, annotate the enum itself.

## Fixer behavior

Suppressed violations are never reported by the check, so the `checkstyleFix` task automatically
skips them. No separate fixer configuration is needed.

## Adding suppression to a new check

To make a check suppressible:

1. In `visitToken()`, read the type's MODIFIERS and call `AstUtil.hasSuppressWarnings`:
   ```java
   final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
   if (modifiers != null && AstUtil.hasSuppressWarnings(modifiers, "MyCheckKey"))
       return;
   ```

2. If the check visits `OBJBLOCK` (not the type directly), read the parent's modifiers:
   ```java
   final var parent = ast.getParent();
   if (parent != null) {
       final var parentModifiers = parent.findFirstToken(TokenTypes.MODIFIERS);
       if (parentModifiers != null && AstUtil.hasSuppressWarnings(parentModifiers, "MyCheckKey"))
           return;
   }
   ```

3. Add test coverage in both the clean file (suppressed types produce 0 violations) and the
   violation file (wrong-key types still produce violations).

4. Update the supported keys table above and the check's description in README.md.