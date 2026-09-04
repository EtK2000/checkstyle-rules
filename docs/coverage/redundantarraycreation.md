# RedundantArrayCreationCheck auto-fix coverage

Removes the `new Type[]{...}` wrapper from a varargs argument and leaves the initializer's elements
as the arguments. Every boundary comes from a node position: the `new`, both braces, the
initializer's trailing comma, and the argument-list comma in front of an empty initializer. The
elements themselves are copied as written, so an element keeps its spacing, its comments, and
whatever punctuation it contains.

An initializer that spans lines is folded onto one, which removes the wrapper from a call written
across several lines. A fold whose result would be longer than the line-length budget is refused
instead, so the fixer never hands back a file carrying a violation it created.

A comment written between the `new` and the opening brace is carried to the front of the elements,
because that whole region is deleted. A comment after the initializer's trailing comma is carried
to the end of them, and a comment that is the only thing inside the braces is carried to where the
argument stood. A comment written after the opening brace is not carried: it sits inside the
elements and travels with them.

A supplementary character written ahead of the call does not shift the fix.

## Supported (auto-fixable)

| Pattern | Replacement | Notes |
| --- | --- | --- |
| `method(new Type[]{"a", "b"})` | `method("a", "b")` | Only argument |
| `method(arg, new Type[]{"a"})` | `method(arg, "a")` | Last argument with preceding args |
| `method(new Type[]{})` | `method()` | Empty array, only argument |
| `method(arg, new Type[]{})` | `method(arg)` | Empty array, removes the preceding comma |
| `method(arg, foo(1, 2), new Type[]{})` | `method(arg, foo(1, 2))` | The comma removed is the one before the array, not the one inside the earlier call |
| `method(new Type[]{ })` | `method()` | Whitespace between the braces is still an empty initializer |
| `method(new Type[]{"a", "b",})` | `method("a", "b")` | Trailing comma in the initializer is dropped |
| `method(new Type[]{"a",})` | `method("a")` | The dropped comma is the only one |
| `method(new Type[]{"a", "b", /* c */})` | `method("a", "b" /* c */)` | A comment after the trailing comma is kept, and the comma still goes |
| `method(arg, new Type[]{/* c */})` | `method(arg /* c */)` | An initializer holding only a comment is still empty |
| `method(new Type[]{/* c */})` | `method(/* c */)` | The same with no preceding argument to sit against |
| `method(new /* c */ Type[]{"a"})`, `method(new Type[/* c */]{"a"})`, `method(new Type[] /* c */ {"a"})` | `method(/* c */ "a")` | A comment anywhere in the deleted head is carried in front of the elements |
| `method(new /* a */ Type[] /* b */ {"x"})` | `method(/* a */ /* b */ "x")` | Several comments are carried in source order |
| `method(new Type[]{"a", "b" /* c */})` | `method("a", "b" /* c */)` | A comment inside the elements travels with them |
| `method(new Type[]{` newline `"a", "b"` newline `})` | `method("a", "b")` | A multi-line initializer is folded onto one line |
| `method(new Type[]` newline `{"a", "b"})` | `method("a", "b")` | The break falls inside the deleted head |
| `method(` newline `arg,` newline `new Type[]{"a"}` newline `)` | `method(` newline `arg,` newline `"a"` newline `)` | The call's `(` is on a line above the creation |
| `method(` newline `arg,` newline `new Type[]{}` newline `)` | `method(` newline `arg` newline `)` | The deleted span reaches back over the argument comma on the line above |
| `new Ctor(new Type[]{"a"})` | `new Ctor("a")` | Constructor varargs |
| `IntStream.of(new int[]{1, 2})` | `IntStream.of(1, 2)` | Primitive array to a primitive-typed varargs param |
| `Collections.<String>addAll(list, new String[]{"a"})` | `Collections.<String>addAll(list, "a")` | A type witness on the owner call does not get in the way |
| `addAll(list, new T[][]{arr1, arr2})` (multi-dim, plain-reference elements) | `addAll(list, arr1, arr2)` | Elements are array references, not nested `{...}` initializers, so splicing them is valid (contrast the not-supported nested-initializer row) |
| Elements with nested parens (`foo(1, 2)`), a ternary, a cast, an array access, a lambda, or a nested call that is itself a violation | Copied verbatim | The elements are source text, not re-emitted expressions |
| String literals with braces (`"a{b}"`), a `}` char literal, or a `//` inside a string | Correctly extracted | The elements come from the parse, so nothing has to be masked |

## Not supported

| Pattern | Reason |
| --- | --- |
| A comment anywhere inside a creation that spans lines | Folding the span onto one line would put whatever followed a `//` comment inside it, and a block comment fares no better once the lines are joined, so the creation is left alone. This covers a comment between the elements, one in the deleted head between `new` and `{`, and one that is the whole body of an empty initializer. A comment inside a creation written on one line is kept |
| A fold whose result would exceed the line-length budget | The wrapper would come off at the cost of a line the project's own `LineLength` rule rejects |
| A text block inside an initializer that spans lines | A text block's opening delimiter has to be the last thing on its line, so folding would delete the line break the block depends on |
| Multi-dimensional array with nested brace-initializer elements (`new T[][]{{"a"}, {"b"}}`) | Each element is a bare `{...}` initializer, valid only inside a `new T[]`; splicing them as arguments would not compile |
| A position whose text has moved under an earlier fix in the same pass | Nothing the check recognises sits at the reported position any more, so the fix is left for the next pass |
| A buffer that does not parse | The classifier needs the parse. The fix is retried once the buffer parses |

## Not flagged by check (correct behavior, not a limitation)

| Pattern | Reason |
| --- | --- |
| An initializer holding exactly one element whose type cannot be read off its shape (`f(new T[]{null})`, `f(new T[]{row})`, `f(new T[]{ident})`, `f(new T[]{call()})`, `f(new T[]{arr[0]})`, `f(new T[]{flag ? a : b})`, `f(new T[]{(Object[]) x})`, `f(new T[]{new T[]{...}})`) | For a lone element the wrapper is what makes the call mean what it means. `f(new T[]{e})` always passes an array of one, but `f(e)` is resolved by strict invocation first, so when `e` is assignable to `T[]` it becomes the whole varargs array instead of its first element. Measured: `Arrays.asList(new Object[]{row}).size()` is 1 while `Arrays.asList(row).size()` is 2, and `Arrays.asList(new Object[]{null})` is a one-element list while `Arrays.asList(null)` throws. A lone element is reported only when its form is one the language cannot give an array type: a char, boolean, numeric, string or text-block literal, `this`, the result of an arithmetic, shift, bitwise, comparison, logical or `instanceof` operator, a method reference, a `new Foo(...)` that is not an array creation, or a cast to a type that is not an array. Every other form keeps its wrapper. That is load-bearing for `null`, an identifier, a call, an array access and a ternary, whose value really may be an array. An element that is not an expression node at all (a lambda, a nested `{...}` initializer) is not a lone-element case and is still reported. Two or more elements cannot bind the fixed-arity form and an empty initializer passes an empty array either way, so both are still reported |
| A varargs target declared in the compilation unit being checked | Whether the target is varargs is answered by reflecting on a compiled class, loaded through the check's own class loader. That loader carries the JDK and the rule jar's dependencies, never the sources being analysed, so a method or constructor declared in the file under check never resolves. This is one limitation with several faces: it is also why `this(...)`, `super(...)` and an enum constant's argument list are never reported, since each names a constructor in its own compilation unit |
| A receiver that is not a simple name (`new StringBuilder().toString().formatted(...)`, `"x".formatted(...)`) | Reflection resolves a simple type name against the file's imports; a chain or a literal has no such name to resolve |
| A fully qualified receiver (`java.util.Arrays.asList(...)`) | Same: the receiver is a `.`-chain rather than an imported simple name |
| Statically imported varargs (`asList(new T[]{})`) and an unqualified call to a method in the same class | There is no receiver to resolve |
| `List.of(new Object[]{"a"})` and `List.of(new int[]{1})` (non-varargs overload) | `List.of(E)` is non-varargs with `Object` as its last param, and wins overload resolution |
| `Arrays.asList(new int[]{1})` (primitive array to reference varargs) | Genuine varargs, but removing the wrapper would change autoboxing (`List<int[]>` vs `List<Integer>`) |
| `new String(new char[]{'a'})` (non-varargs constructor) | `String(char[])` is not varargs, so there is no redundant wrapper |
| `(Object) new String[]{"a"}` and `(CharSequence[]) new String[]{"a"}` (cast wrapping) | The last argument is a cast, not a `new` array |
| `method(new Object[]{}, "x")` | The array is not the last argument, so it is not the varargs one |
| `method(existingArrayVar)` (variable, not `new`) | Not an explicit array creation |
| `method(new Type[5])` (explicit size, no init) | Allocates without a `{...}` initializer |
| An array creation that is not an argument at all (a return, a field or local initializer) | Nothing is being passed to a varargs parameter |
| An annotation's array value (`@SuppressWarnings({"a", "b"})`) | An annotation's array value carries no `new`, so there is no wrapper to remove |

Part of [auto-fix coverage](../auto-fix-coverage.md).