# Code Style Conventions

## Threading & Annotations

- All classes must have a thread annotation (`@MainThread`, `@AnyThread`, etc.)
- All non-primitive fields and parameters must be annotated with `@NonNull`/`@Nullable`, except
  final fields initialized inline (at definition) — those don't need `@NonNull`. Final fields set in
  constructors DO need it
- Always add `@CheckResult` on methods that return a value without side effects
- Annotations are always sorted alphabetically, whether stacked (on methods/classes, each on its own
  line) or inline (on parameters, e.g. `@NonNull @NonUiContext Context ctx`)

## Class Structure (top to bottom, separated by blank lines)

1. Inner classes/enums/records (at top of class)
2. Static fields
3. Static initializers
4. Static methods
5. Instance fields
6. Instance initializers/constructors
7. Instance methods

## Prefer

- Record classes over classes with all final fields
- Package-private classes in separate files over inner classes. When the same class is needed by
  multiple sub-packages, place it in the shared parent package with `public` visibility instead of
  duplicating it
- Consolidate similar classes into one (e.g. via overloaded constructors, generics, or extra
  parameters) rather than keeping near-duplicates — same principle as consolidating correlated
  constants across classes. This applies to inner classes too: when multiple classes share the same
  inner class pattern, extract a parameterized standalone class
- `var` whenever possible, especially in for-each and try-with-resource. With `var`, omit `<Object>`
  in generic constructors (e.g. `var x = new LinkedHashSet<>()` not `new LinkedHashSet<Object>()`)
- Pattern matching in `instanceof` when the new type is used
- Pattern matching switches where applicable
- Switches over large if-else chains (especially on enums)
- Prefix increment/decrement (`++i`, `--i`)
- Least accessible visibility (private > package-private > protected > public)
- Specific API methods over generic ones when available (e.g. `.getFirst()` over `.get(0)`), as long
  as there are no compatibility issues (e.g. Android SDK min version)
- Reuse existing code or extract utilities over duplication

## Fields Sorting (within static or instance group)

1. finals with inline values first, then finals without inline values, then non-finals
2. Within each immutability group, sort by type name alphabetically:
   a. Primitives (sorted by type name: boolean, char, double, float, int, long...)
   b. Java built-ins (sorted by type name: Function, List, Map, Set, String...)
   c. Android built-ins (sorted by type name)
   d. Library types (sorted by type name)
   e. App-defined types (sorted by type name)
   Array types sort right after their base type (`int`, then `int[]`, then `int[][]`). This
   applies within each category (primitive arrays stay with primitives, reference arrays stay
   with reference types).
   Visibility (public/private) does NOT affect ordering.
3. Multiple fields of the same type on a single line if not setting a value, sorted alphabetically
   by field name

## Method Sorting

- Methods sorted alphabetically by name within their section (static methods, instance methods)
- Overloads: fewer params first; when param counts are equal, sort by parameter types left-to-right
  following field sorting rules (primitives before reference types, alphabetical within each group).
  When types also match, called-by-others above callers
- Example: `func(int a)` before `func(Object a)`, `func(int a)` before `func(int a, int b)`,
  `func(char a)` before `func(int a)`

## Formatting

- TABs for indentation (not spaces)
- No final comma after last enum/array value
- No blank line at start or end of a class/file
- No blank line immediately before a closing brace
- Never double blank lines
- Blank line separating the groups in class structure and between functions
- No braces around single-line ifs/whiles/fors/do-whiles, but if the body spans multiple lines (even
  if it's a single expression), use braces. Each level is evaluated independently — only the levels
  whose own body is multi-line need braces. Example:
  ```java
  for (...) {
      if (...)
          stmt;
  }
  ```
  The `for` body is two lines (`if` + `stmt`), so it needs braces, but the `if` body is one line
  (`stmt`), so it doesn't
- Never one-line simple ifs/whiles/fors/do-whiles
- In switch statements: blank line after `break;` before the next case; no blank line between
  single-line cases (case label + one statement like `return`/`throw`/`yield`). No blank line
  after a braced case (the closing brace provides visual separation)
- No braces on `case`/`default` blocks unless required (i.e. a variable is defined in the case's
  direct scope)
- Switch cases sorted: alphabetically for names, numerically for literals. Named constants
  sort before numeric literals. For fall-through labels (`case A: case B:`), sort by the first
  label. For comma-separated labels (`case A, B ->`), sort within the list AND sort cases by
  their first label. `default` must always be last
- Prefer enhanced (arrow) switch syntax (`case X ->`) over traditional (`case X:`) when each
  case has a single statement (with break), return, throw, or yield. Fall-through labels
  (no body) are fine, they become comma-separated in enhanced syntax
- `else`/`catch`/`finally` on their own line, not cuddled with the closing brace (i.e. `}\nelse`,
  not `} else`)
- No unused imports
- No trailing whitespace
- Blank lines must be completely empty
- No trailing newline at end of file
- No redundant casts
- Only use `this.XXX` when required (e.g. shadowing) or when assigning an instance field
- Method parameters are never `final`; local variables are `final` wherever possible (except
  for-each
  iteration variables)
- Method/constructor signatures and calls: prefer single line. When too long, break to multi-line
  with no arguments on the opening or closing paren lines. If all arguments fit on a single line
  (just not with the call), they may share that line; otherwise each argument goes on its own line.
  Exception ("ternary"): when a call has exactly one argument that is a ternary expression, the
  condition stays on the opening paren line, `?` on the next line (with the true branch), `:` on
  the line after that (with the false branch), and the closing paren on its own line. Example:
  `method(condition\n\t\t? trueValue\n\t\t: falseValue\n);`
  If the entire ternary fits on one line, keep the closing paren on that line too:
  `method(condition ? trueValue : falseValue);`
  Exception ("inline block"): when a call has exactly one argument that is a lambda, anonymous
  class, constructor call (`new XXX(...)`), or special processing method (`List.of`, `Map.of`,
  `Arrays.asList`, `Context.getString`, `Context.getResources().getQuantityString`), the argument
  stays on the opening paren line and its
  closing brace/paren stays on the closing paren line. For braceless (expression) lambdas that
  extend past the opening line, the closing paren goes on its own line instead.
  Examples: `method(x -> {\n\tbody;\n});`, `method(v ->\n\t\texpr\n);`,
  `list.add(new Foo(\n\targ1,\n\targ2\n));`
  For constructor args with method chaining (`new Foo().bar()`), the constructor starts on the
  opening paren line and the closing paren goes on its own line (like braceless lambdas).
  Both ternary and inline block exceptions also apply when there are exactly two arguments and the
  first is `this` or an Android resource identifier (`R.xxx.yyy` or `android.R.xxx.yyy`).
  Example: `method(this, x -> {\n\tbody;\n});`
  The inline block exception also applies to `Handler.postDelayed` with a braced lambda as the
  first argument and the delay as the second.
  `getString` is only recognized with a known Context receiver (parameter typed as `Context`,
  variable assigned from `requireContext()`/`getContext()`/`requireActivity()`/`getActivity()`, or
  calling directly on one of those)
- No empty switch statements, if bodies, or else bodies. Remove them, but preserve any side effects
  in the condition/expression. Example: `if (++i < 5);` becomes `++i;`,
  `switch (a.mutate()) {}` becomes `a.mutate();`
- Early returns and guard clauses preferred over deep nesting

## Naming & Comments

- Always use understandable variable names (except `i`, `j`, `k` for index iteration)
- No nonsense comments; only TODOs (FIXME for high priority) or explanations of complex logic
- Magic numbers/strings should be `static final` variables, consolidated if correlated across
  classes

## Constructor/Initializer Assignments

- Assign fields alphabetically in 2 chunks: (1) simple one-line assignments, then (2) multi-line
  assignments
- Exception: if assignments have dependencies between them or their calculations, group dependent
  assignments together in their own chunk, still alphabetical within each chunk

## Ordering (when order doesn't matter)

- Alphabetize variable/field access
- Alphabetize function calls: implicit `this` calls first, then by full name
    - Example: `func()`, `a.b()`, `b.a()`, `b.b()`

## Resources

- Icon drawables must be named `icon_XXX.xml`, not `ic_XXX.xml`

## Edge Cases & Testing

- Always think about weird input or edge cases
- Tests should cover all such cases
- Only add messages to assertions when they provide non-obvious context (e.g. guard assertions).
  Don't add messages when the test name already describes the expected behavior

# Writing Style

- No emdashes. Write like a normal developer, not a book author. Use commas, periods, or just
  restructure the sentence