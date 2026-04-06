# Consolidate Same-Type Uninitialized Fields onto Single Line

## CLAUDE.md Rule

> "Multiple fields of the same type on a single line if not setting a value, sorted alphabetically by field name"

## Current State

FieldSortingCheck enforces ordering (type, name, immutability chunks) but does NOT enforce consolidation of same-type uninitialized fields onto a single declaration.

Example that goes unchecked:
```java
// Current: no violation raised
private int height;
private int width;

// Expected: should suggest combining to
private int height, width;
```

## AST Representation

Java AST represents `int a, b, c;` and `int a; int b; int c;` identically - as three separate `VARIABLE_DEF` nodes. The only difference is line number metadata. So the check must use line numbers to detect whether fields are already combined.

## Detection Logic

Find consecutive `VARIABLE_DEF` nodes in the same `OBJBLOCK` that:
1. Have the same type name
2. Have NO initializer (`ASSIGN` node absent)
3. Have the same modifiers (visibility, static, final)
4. Have NO annotations on individual fields (can't safely combine `@Deprecated int a; int b;`)
5. Are on separate lines (if already on the same line, no violation)

When 2+ such fields are found consecutively, flag the second one with a message like:
`Fields 'height' and 'width' (type 'int') should be combined: 'int height, width;'`

## Edge Cases

| Case | Handling |
|------|----------|
| Different annotations | Don't combine: `@Deprecated int a; int b;` |
| Different visibility | Don't combine: `public int a; private int b;` |
| One has initializer | Don't combine: `int a = 1; int b;` |
| Array types | Must match exactly: `int[]` and `int` are different |
| Generic types | Must match exactly: `List<String>` and `List<Integer>` are different |
| Fields with Javadoc | Don't combine if either has a preceding comment/Javadoc |

## Separate Check vs Enhancement

**Should be a separate check** (`FieldConsolidationCheck` or `CombineSameTypeFieldsCheck`):
- FieldSortingCheck focuses on ordering, this focuses on formatting
- Different configuration needs (some teams may not want forced consolidation)
- Different message semantics ("X before Y" vs "X and Y should be combined")
- FieldSortingCheck is already complex

## Implementation Estimate

~150 lines. The main complexity is correctly comparing modifiers and handling the edge cases above.

## Priority

Low-medium - this is a formatting preference, not a correctness issue. Most developers naturally combine fields, so violations would be infrequent.