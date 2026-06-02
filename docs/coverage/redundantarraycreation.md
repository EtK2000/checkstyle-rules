# RedundantArrayCreationCheck auto-fix coverage

The fixer removes `new Type[]{...}` and replaces it with the elements directly. For empty arrays,
it also removes the preceding comma if one exists. A trailing comma inside the initializer
(`{"a", "b",}`) is dropped so the spliced argument list stays valid.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `method(new Type[]{"a", "b"})` | `method("a", "b")` | Only argument |
| `method(arg, new Type[]{"a"})` | `method(arg, "a")` | Last argument with preceding args |
| `method(new Type[]{})` | `method()` | Empty array, only argument |
| `method(arg, new Type[]{})` | `method(arg)` | Empty array, removes comma |
| `method(new Type[]{"a", "b",})` | `method("a", "b")` | Trailing comma in initializer is dropped |
| `new Ctor(new Type[]{"a"})` | `new Ctor("a")` | Constructor varargs |
| `IntStream.of(new int[]{1, 2})` | `IntStream.of(1, 2)` | Primitive array to a primitive-typed varargs param (guard does not fire) |
| `addAll(list, new T[][]{arr1, arr2})` (multi-dim, plain-reference elements) | `addAll(list, arr1, arr2)` | Elements are array references, not nested `{...}` initializers, so splicing them is valid (contrast the not-supported nested-initializer row) |
| Elements with nested parens (`foo(1, 2)` inside array) | Correctly extracted | Paren-balanced parsing |
| String literals with braces (`"a{b}"` inside array) | Correctly extracted | String/char literal awareness |
| Comment with braces (`/* } */` between elements) | Correctly extracted | Comment + text-block aware |
| Comment before an empty array (`method(arg, /* c */ new T[]{})`) | `method(arg)` | Preceding comma located on the masked line |

## Not supported

| Pattern | Reason |
| --- | --- |
| Multiline array creation (`new Type[]{\n...\n}`) | Closing `}` not on the same line as opening `{` |
| Opening `{` on a different line than `new` | No `{` found on the violation line |
| Multi-dimensional array with nested brace-initializer elements (`new T[][]{{"a"}, {"b"}}`) | Each element is a bare `{...}` initializer, valid only inside a `new T[]`; splicing them as arguments would not compile, so the fixer bails ("nested array initializer") |
| Text-block element (`"""..."""`) containing `}` | Text block opens with `"""`+newline, so the closing `}` lands on a later line (multiline skip) |
| Statically imported varargs (`asList(new T[]{})`) | Check does not fire (no receiver to resolve via reflection) |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| `List.of(new Object[]{"a"})` (non-varargs overload) | `List.of(E)` is non-varargs with `Object` last param, blocks |
| `List.of(new int[]{1})` (non-varargs overload) | Blocked by the same non-varargs `List.of(E)` overload as the `Object[]` row, before the primitive guard is consulted |
| `Arrays.asList(new int[]{1})` (primitive array to reference varargs) | Genuine varargs, so the primitive-array guard fires: removing the wrapper would change autoboxing (`List<int[]>` vs `List<Integer>`) |
| `new String(new char[]{'a'})` (non-varargs constructor) | `String(char[])` is not varargs, so there is no redundant wrapper |
| `(Object) new String[]{"a"}` (cast wrapping) | Last arg is a cast, not a `new` array |
| `(CharSequence[]) new String[]{"a"}` (cast wrapping) | Last arg is a cast, not a `new` array |
| `method(existingArrayVar)` (variable, not `new`) | Not an explicit array creation |
| `method(new Type[5])` (explicit size, no init) | Allocates without a `{...}` initializer |

Part of [auto-fix coverage](../auto-fix-coverage.md).