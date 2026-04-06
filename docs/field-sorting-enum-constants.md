# FieldSortingCheck: Enum Constants Not Checked

## Current State

FieldSortingCheck visits `OBJBLOCK` tokens and iterates children looking for `VARIABLE_DEF` nodes only. It completely ignores `ENUM_CONSTANT_DEF` tokens.

## Gap

When an enum has both enum constants and static/instance fields, the enum constants are silently skipped. No ordering validation is performed on them.

Example that goes unchecked:
```java
enum Priority {
    ZEBRA,  // should be after ALPHA alphabetically
    ALPHA;

    static final int MAX = 10;
    static final int MIN = 1;  // fields ARE checked, constants are NOT
}
```

## Impact

- **False negatives only.** Misordered enum constants are never flagged.
- This affects both top-level and inner enums equally.
- No test cases exist for enum constant ordering.

## What Would Need to Change

1. Add `ENUM_CONSTANT_DEF` handling to the field iteration loop
2. Enum constants should be treated as a group that sorts alphabetically (they're always at the top of the enum body, before any fields/methods)
3. Decide whether this belongs in FieldSortingCheck or SwitchCaseOrderCheck (since CLAUDE.md groups enum ordering with switch case ordering conceptually)

## Considerations

- Enum constants are conceptually different from fields. They don't have types, visibility, or the immutability chunking that FieldSortingCheck uses.
- A simple alphabetical sort of enum constant names might be sufficient.
- This could also be a separate `EnumConstantOrderCheck` to keep FieldSortingCheck focused on field ordering.

## Priority

Medium - enum constant ordering is explicitly part of the code style but has no enforcement.