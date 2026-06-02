# PreferVarCheck auto-fix coverage

Replaces an explicit local, for-each or try-with-resources type with `var`, moving the declared type
arguments onto every diamond initializer on the line so nothing is inferred away
(`List<String> l = new ArrayList<>()` -> `var l = new ArrayList<String>()`; each arm of a ternary or
single-line switch initializer receives them, since any arm may supply the value). Also converts an
explicit array initializer to the implicit form and replaces `<Object>` with `<>` on a `var`
declaration.

The check refuses wherever `var` would bind a type other than the one written. Those refusals are
grouped below by what drives them.

## The initializer binds a different type

| Pattern | Reason |
| --- | --- |
| Boxed declaration whose initializer is not itself the box (`Byte b = 0`, `Integer i = Integer.parseInt(s)`) | `var` binds the primitive. `Integer.valueOf(...)` and `new Integer(...)` still convert |
| Widening literal (`long x = 1`, `double d = 5f`, `float f = 5.0`) | `var` binds the literal's own type, and no fixer rewrites the literal to carry the suffix |
| Declared supertype (`Object o = "x"`, `Object o = compute()`) | `var` binds the value's own type. An array of a supertype is its own type rather than a widening, so `Object[] a = new String[0]` stays as written while `Object[] a = list.toArray()` converts |
| Widened type argument (`Map<String, Object> m = Map.of(...)`) | The arguments are inferred from the call and the deliberate widening is lost. A `new` is exempt, since its diamond receives them |
| Wildcard (`List<?> w = ...`) | `var` binds the inferred arguments, losing the wildcard |
| Multi-member anonymous class body | `var` cannot name the type. The single-method form is deferred to `PreferLambdaCheck` |
| Qualifier carrying its own arguments (`Outer<String>.Inner`, at any depth) | Those bind the enclosing instance rather than the declared type |
| Constructed class that cannot take the declared arguments (`Function<String, Integer> f = new MyFunc<>()` where `MyFunc<T> implements Function<T, Integer>`) | Moving the arguments onto the diamond would not compile. Off-classpath classes are compared by name, fully qualified when the `new` names one, falling back to the arity declared in the same file |

A qualified declared type is treated identically to its imported form, so `java.util.List<?>` and
`Api.Cache<K, V>` get the same refusals as `List<?>` and `Cache<K, V>`. A qualified type *argument*,
by contrast, only counts as widening when its full name does (`java.lang.Object`, `java.lang.Number`,
`java.lang.CharSequence`, `java.lang.Comparable`, `java.io.Serializable`), so an application type
sharing the simple name (`Map<String, Types.Number>`) converts while `Map<String, Number>` does not.

## Conditional initializers

Converted only when every arm is a `new` that can take the declared arguments, since under `var` the
arms no longer share the declaration as a target type.

| Pattern | Reason |
| --- | --- |
| Any arm that is not a `new` (`flag ? new ArrayList<>() : Collections.emptyList()`) | That arm infers on its own, so the variable binds the arms' least upper bound |
| Colon-form switch | It yields through statements the arm walk cannot follow |
| Braced rule body (`case 1 -> { yield new ArrayList<>(); }`) | The value is hidden behind a block |
| Parenthesised condition (`(flag) ? ... : ...`) | The parens shift the arms out of the positions the walk reads |
| Arms constructing different classes, where the variable is later reassigned | They bind an intersection type the declaration cannot name and that is narrower than the declared supertype, so assigning that supertype afterwards stops compiling. Without a reassignment it still converts |

## For-each element types

| Pattern | Reason |
| --- | --- |
| Widening supertype of the element type (`for (Object o : strings)`), including an array of one (`for (Object[] row : listOfStringArrays)`) | `var` binds the element type, which the declaration deliberately is not |
| Primitive or boxed element type, unless the iterable is an array of exactly that type | Boxing and unboxing both retype the variable: `for (int n : listOfIntegers)` rebinds to `Integer`, where `==` compares references and arithmetic runs in int space, and `for (Integer n : intArray)` rebinds to `int`, where a null check stops compiling. `for (int n : intArray)` still converts |

## Narrowing and later use

A non-`final` local whose declared type names something other than the class its initializer binds is
reported as a warning (`should use 'var'`) and never fixed, whenever converting it would change what
later code means. The bound class is read from the initializer rather than from a `new` keyword, so a
same-file factory (`List<String> l = makeArrayList()`) and a conditional whose arms all construct one
class are covered too. The classpath is deliberately not consulted for a method's return type, since
reflection reports the erasure and `List<String>.get` would read as `Object`.

| Pattern | Reason |
| --- | --- |
| Reassigned to anything but a `new` of the same class | Under `var` the variable takes the bound class, so `List<String> l = new ArrayList<>()` followed by `l = new LinkedList<>()` stops compiling. Reassigning the very same class still converts, as does `l = null` |
| Reassigned where the two spellings resolve differently (`new api.Cache<>()` then `new impl.Cache<>()`) | Compared fully qualified, so a matching simple name is not mistaken for the same class. When neither name resolves the declaration stays a warning |
| Passed to an overload set that discriminates on the narrowing (both `take(List)` and `take(ArrayList)` exist) | The call binds to the most specific applicable parameter, so under `var` it would silently select the other overload. Constructor overloads are checked the same way, against the constructors of the class the `new` names |

The reassignment scan starts at the declarator, so a same-named field assigned above it is not
mistaken for a reassignment, and it skips nested and anonymous class bodies, whose same-named members
shadow the local rather than using it. The overload test treats a parameter as discriminating only
when it accepts the constructed class but not the declared one, so an overload set that cannot tell
them apart (`take(List)` beside `take(String)`, or `println(Object)`) still converts, as does a
same-named overload of a different arity. Only a bare or `this`/`super`-qualified call is matched,
since a call on any other receiver resolves to a class this check does not read.

## Unresolvable call targets

A declaration whose initializer is a call is not reported at all when the class declaring that method
cannot be determined: the method may be target-typed, and the conversion would drop the declared
arguments it infers from, so `List<String> l = emptyList()` would bind `List<Object>`.

The declaring class is looked for on every enclosing class in the file, on a same-file supertype, on
a static import's owner, on a qualified receiver (`java.util.Collections.emptyList()`), on a receiver
that names its own type (a string or text block, a `new`, a cast, `this`/`super`, and through a chain
to its base), and on any receiver naming a variable declared in the file, including an array
element's array. A call none of those resolve stays as written.

## Try-with-resources

Refused on all the same grounds as a local, including a declared supertype of what it constructs
(`try (InputStream in = new FileInputStream(f))` stays as written): the body is written against the
declared type, and rebinding to the concrete one can reselect an overload or make an enclosing
`catch` unreachable when the subtype's `close()` throws less.

## Fixer-side skips

| Pattern | Reason |
| --- | --- |
| Column out of range, or resolving to a field or a bare assignment | A sibling fixer shifted the line earlier in the pass, so the reported column no longer names a convertible declaration, and `var` is illegal in both shapes |
| Column landing mid-identifier, on a non-identifier, inside a string or char literal, or on a name followed by `==` | Replacing from that offset would leave the token's head behind, splice `var` into a literal, or rewrite a comparison |
| C-style array declarator (`String x[] = new String[0];`) | `var x[]` is not legal Java. In the full pipeline `ArrayTypeStyleFixer` normalises the declaration first, after which it converts |
| Explicit array initializer the array path cannot resolve on the reported line (brace on a later line, or a comment between the `=` and the `new`) | Skipped rather than falling through to the type-to-`var` rewrite, which would leave the violation standing |
| Explicit array initializer whose declared type cannot be read at the reported column | The `{...}` shorthand is legal only where the declared type is itself an array, so the `new Type[]` is kept rather than emitting `var x = {1}` |
| Declared type arguments that wrap onto a later line (`Map<String,` / `Integer> m = ...`) | The `<...>` never closes on the reported line, so consuming to end-of-line would delete the rest of the declaration |
| Declared type arguments whose diamond sits on a continuation line, or whose only diamond is nested in a call argument | Only a diamond on the reported line can receive them; dropping them binds the constructor to `Object`, and moving them into a nested call would retype that call |
| Declared type arguments on a declaration whose buffer no longer parses | The target cannot be verified: splicing into a chain receiver's diamond retypes that receiver, and dropping them rebinds the variable |
| Multi-line block comment between the type and the name, or between the name and its `=` | The comment does not close on the reported line, so the declaration shape cannot be confirmed |
| Multi-variable declaration (`int a, b;`) | One `var` cannot serve several declarators |
| Declaration already uses `var` | Nothing to convert; the advisory generic-type-info warning has no mechanical fix |
| Non-`Object` generic type arguments (`<String>`) | The diamond rewrite only collapses `<Object>` |
| Open-paren join: trailing content on the `(` line, a `(` that is only a comment's own trailing character, or non-annotation content between the `(` and the declaration | Joining would swallow the clause into a comment or move the declaration inside one |

Part of [auto-fix coverage](../auto-fix-coverage.md).
