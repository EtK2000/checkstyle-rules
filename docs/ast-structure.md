# Checkstyle AST Structure

Checkstyle's AST doesn't always match intuition. This doc covers the non-obvious structures that
have caused bugs or confusion.

## General rules

- Parent nodes (operators, keywords) have children but their `getText()` returns only the operator
  symbol, not the full expression. `GT.getText()` returns `">"`, not `"a > b"`.
- `exprText(node)` concatenates all leaf text under a node without operators or punctuation. Use for
  structural equality comparison, not for display.
- `displayText(node)` recursively renders operators, dots, brackets. Use for violation messages.

## Ternary (QUESTION)

```
a > b ? a : b
```

```
QUESTION
  GT              (condition - operator node, NOT wrapped in EXPR)
    IDENT[a]      (left operand)
    IDENT[b]      (right operand)
  IDENT[a]        (true branch)
  COLON
  IDENT[b]        (false branch)
```

Children order: condition, trueBranch, COLON, falseBranch. The condition is the operator node
directly (GT, GE, LT, LE, EQUAL, etc.), not wrapped in EXPR.

## Comparison operators

`GT`, `GE`, `LT`, `LE`, `EQUAL`, `NOT_EQUAL` are all parent nodes with two children (left and
right operands). They are NOT leaf nodes. Any recursive walk (like `isPureExpression`) must handle
the children, not the operator itself.

## INDEX_OP (array access)

```
arr[0]
```

```
INDEX_OP
  IDENT[arr]
  EXPR
    NUM_INT[0]
  RBRACK["]"]     (hidden third child)
```

INDEX_OP has THREE children, not two. The RBRACK is a leaf token that must be handled in recursive
walks (e.g. `isPureExpression` must accept RBRACK as pure).

Nested access `arr[0][1]` produces nested INDEX_OPs:

```
INDEX_OP
  INDEX_OP
    IDENT[arr]
    EXPR{NUM_INT[0]}
    RBRACK
  EXPR{NUM_INT[1]}
  RBRACK
```

## DOT (member access)

```
this.x
```

```
DOT
  LITERAL_THIS[this]
  IDENT[x]
```

DOT is a parent node with two children. `exprText(DOT)` returns `"thisx"` (no dot).
`displayText(DOT)` returns `"this.x"`.

Chained dots `a.b.c` nest left-to-right:

```
DOT
  DOT
    IDENT[a]
    IDENT[b]
  IDENT[c]
```

## Unary operators

**Prefix** (`++x`, `--x`, `-x`, `+x`, `!x`, `~x`): operator node with one child (the operand).

```
DEC             (--x)
  IDENT[x]

UNARY_MINUS     (-x)
  IDENT[x]
```

**Postfix** (`x++`, `x--`): also operator node with one child, but different token type.

```
POST_INC        (x++)
  IDENT[x]

POST_DEC        (x--)
  IDENT[x]
```

`INC`/`DEC` are prefix. `POST_INC`/`POST_DEC` are postfix. The AST structure is identical (one
child), but the evaluation semantics differ: prefix mutates before use, postfix mutates after.

## do-while body

```
do stmt; while (cond);
```

The body is the FIRST child of `LITERAL_DO` (before the condition), unlike `if`/`while`/`for`
where the body follows `RPAREN`.

## Numeric literals

Negative numbers are NOT single tokens. `-42` is `UNARY_MINUS{NUM_INT[42]}` (two nodes).
The NUM_INT token text is always unsigned.

Literal suffixes are part of the token text: `0L` is `NUM_LONG` with `getText()` returning `"0L"`.

## METHOD_CALL

```
obj.method(arg)
```

```
METHOD_CALL
  DOT
    IDENT[obj]
    IDENT[method]
  LPAREN
  ELIST
    EXPR
      IDENT[arg]
  RPAREN
```

Static calls have the same structure: `Math.max(a, b)` has `DOT{IDENT[Math], IDENT[max]}`.

Bare calls (no receiver): `foo()` has `IDENT[foo]` as the first child instead of `DOT`.

## FOR_EACH_CLAUSE

```
for (var item : source) body;
```

```
LITERAL_FOR
  LPAREN
  FOR_EACH_CLAUSE
    VARIABLE_DEF
      MODIFIERS
      TYPE{IDENT[var]}
      IDENT[item]
    EXPR              (the iterable -- NOT a second VARIABLE_DEF)
      IDENT[source]
  RPAREN
  body (EXPR or SLIST)
```

The iterable is an EXPR child of `FOR_EACH_CLAUSE`, not of `LITERAL_FOR`. To get it, iterate
`FOR_EACH_CLAUSE` children and find the `EXPR` (skip `VARIABLE_DEF`).

## LAMBDA

```
(k, v) -> target.put(k, v)
```

```
LAMBDA
  LPAREN
  PARAMETERS
    PARAMETER_DEF{MODIFIERS, TYPE, IDENT[k]}
    COMMA
    PARAMETER_DEF{MODIFIERS, TYPE, IDENT[v]}
  RPAREN
  EXPR              (expression body)
    METHOD_CALL{...}
```

Even inferred-type parameters `(k, v)` have `PARAMETERS` with `PARAMETER_DEF` children (each with
empty `MODIFIERS` and `TYPE`). The body is either `EXPR` (expression lambda) or `SLIST` (block
lambda `{ ... }`).

**Important:** When a lambda appears as an argument in a METHOD_CALL's ELIST, it may be a direct
child of ELIST (not wrapped in EXPR). Always check for both `LAMBDA` and `EXPR{LAMBDA}` children.

## SLIST semicolons

Expression statements inside an SLIST (`{ stmt; }`) may or may not have a SEMI token between the
EXPR and the RCURLY:

```
SLIST
  EXPR{METHOD_CALL{...}}
  SEMI              (optional -- present in some AST configurations)
  RCURLY
```

When checking for a single-statement SLIST, skip an optional SEMI between the first EXPR and RCURLY
rather than relying on `getChildCount() == 2`.

## VARIABLE_DEF

```
int a, b;
```

Produces TWO `VARIABLE_DEF` nodes (one per variable), not one with multiple names. Multi-variable
declarations are split at the AST level.

## Annotation values

Single value `@Anno("x")`: value wrapped in `EXPR`.
Array value `@Anno({"x", "y"})`: uses `ANNOTATION_ARRAY_INIT` (no `EXPR` wrapper).
Named param `@Anno(key = value)`: uses `ANNOTATION_MEMBER_VALUE_PAIR`.

## Wildcard type arguments

```
List<? extends Number> x;
```

```
TYPE_ARGUMENTS
  GENERIC_START[<]
  TYPE_ARGUMENT
    WILDCARD_TYPE[?]
    TYPE_UPPER_BOUNDS[extends]
      IDENT[Number]
  GENERIC_END[>]
```

```
List<? super Integer> y;
```

```
TYPE_ARGUMENTS
  GENERIC_START[<]
  TYPE_ARGUMENT
    WILDCARD_TYPE[?]
    TYPE_LOWER_BOUNDS[super]
      IDENT[Integer]
  GENERIC_END[>]
```

`WILDCARD_TYPE` and `TYPE_UPPER_BOUNDS`/`TYPE_LOWER_BOUNDS` are **siblings** under `TYPE_ARGUMENT`,
not parent-child. `WILDCARD_TYPE` has no children. The bound node's (`TYPE_UPPER_BOUNDS` or
`TYPE_LOWER_BOUNDS`) first child is the bound type `IDENT` directly (no `TYPE` wrapper).

Plain `?` (unbounded) has just `WILDCARD_TYPE` with no bounds sibling.

## Array type declarations

Both `int[] x` (Java-style) and `int x[]` (C-style) produce `ARRAY_DECLARATOR` siblings under
`TYPE`. `int[] x[]` produces two `ARRAY_DECLARATOR` siblings, identical to `int[][] x`.