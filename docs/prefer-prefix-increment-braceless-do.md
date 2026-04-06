# PreferPrefixIncrementCheck: Braceless `do` Loop Not Detected

## Current State

PreferPrefixIncrementCheck flags postfix `i++`/`i--` when the value is discarded (standalone
statement, for-loop update, braceless control flow body). It correctly detects braceless `if`,
`else`, `while`, and `for` bodies. It does NOT detect braceless `do` bodies.

## Gap

```java
// detected (braceless while)
while (flag)
    i++;  // violation

// NOT detected (braceless do)
do
    i++;  // no violation, but should be
while (flag);
```

## Root Cause

The check uses `isAfterRparen()` to detect braceless control flow bodies. This works because `if`,
`while`, and `for` all have the structure:

```
LITERAL_IF / LITERAL_WHILE / LITERAL_FOR
    LPAREN
    EXPR (condition)
    RPAREN
    EXPR (body)       <-- previous sibling is RPAREN
```

But `do-while` has the body FIRST, before the condition:

```
LITERAL_DO
    EXPR (body)       <-- first child, previous sibling is null
    LPAREN
    EXPR (condition)
    RPAREN
    SEMI
```

Since the body `EXPR` has no previous sibling (it's the first child), `isAfterRparen()` returns
false. The `LITERAL_DO` case in the switch is never reached.

## Fix

Add a separate check in `isValueDiscarded()` for `do` loops. After the `isAfterRparen` check, add:

```java
// braceless do-while body: EXPR is first child of LITERAL_DO
if (grandparent.getType() == TokenTypes.LITERAL_DO
        && grandparent.getFirstChild() == parent)
    return true;
```

This checks if the EXPR is the first child of `LITERAL_DO`, which is how Checkstyle represents the
do-while body in the AST.

## Testing

Add to `InputPrefixViolation.java`:
```java
void bracelessDo(boolean flag) {
    int i = 0;
    do
        i++;  // violation
    while (flag);
}
```