# PreferBulkOperationCheck auto-fix coverage

The fixer reuses the check's AST classifier and replaces the flagged span in place, so it preserves
any leading or trailing text on the span's lines (a guarding `if`, an enclosing block or call, a
following statement) and rewrites the call even when it is nested inside another expression (e.g.
`if (flag) source.forEach(...)` becomes `if (flag) target.putAll(source);`). A comment inside the
replaced span is dropped. The receiver, target, source, and value text are sliced verbatim from the
source, so any shape (qualified name, generics, cast, ternary, method-ref qualifier) is preserved
exactly.

When a sliced operand (the value, receiver, or source) itself spans more than one line, the lines
are rejoined without a stray space; but if a comment or a text block falls inside the operand's own
span, the check does not fire at all (no auto-fix), since collapsing it to one line would comment out
the trailing tokens, or strip the line terminator a text block's opening delimiter requires. A
comment that precedes the operand's first token (e.g. `for (var x : /* c */ src` on the opening line)
is outside the operand span and does not block the fix.

The `addAll`/`putAll` rewrites additionally require both operands' types to resolve through the
file's imports and package: the source assignable to `Collection` (or `Map`), and the target
declaring the bulk method. A shape match alone is not enough, because the same loop over an array,
an `Iterable`, a `Stream`, or a type that merely has `add` would produce a call that does not
compile. The `System.arraycopy` and `Arrays.fill` rewrites are ungated, on the reasoning
that `arr.length` and `arr[i]` only parse against an array. That establishes array-ness but not
component-type compatibility, and it is wrong in two known cases tracked in
[TODO-prefer-bulk-array-kinds-and-columns.md](../todo/TODO-prefer-bulk-array-kinds-and-columns.md):
a `byte[]`/`short[]`/`char[]` fill with an int-typed value does not compile, and an `arraycopy`
between differing component types throws `ArrayStoreException` at runtime.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `for (var x : source) target.add(x)` | `target.addAll(source)` | Yes |
| `for (var x : objectArray) target.add(x)` | `Collections.addAll(target, objectArray)` (import added) | Yes |
| `for (var x : varargsParam) target.add(x)` | `Collections.addAll(target, varargsParam)` (import added) | Yes |
| `for (var x : objectArray2d) target.add(x)` | `Collections.addAll(target, objectArray2d)` (import added) | Yes |
| `for (var x : call()) target.add(x)` where `call()` returns an object array | `Collections.addAll(target, call())` (import added) | Yes |
| `for (var i = 0; i < source.size(); ++i) target.add(source.get(i))` | `target.addAll(source)` | Yes |
| `for (var e : source.entrySet()) target.put(e.getKey(), e.getValue())` | `target.putAll(source)` | Yes |
| `source.forEach((k, v) -> target.put(k, v))` | `target.putAll(source)` | Yes |
| `source.forEach(target::put)` | `target.putAll(source)` | Yes |
| `list.forEach(item -> other.add(item))` | `other.addAll(list)` | Yes |
| `list.forEach(other::add)` | `other.addAll(list)` | Yes |
| `for (var i = 0; i < src.length; ++i) dst[i] = src[i]` | `System.arraycopy(src, 0, dst, 0, src.length)` | Yes |
| `for (var i = 0; i < arr.length; ++i) arr[i] = value` | `Arrays.fill(arr, value)` (import added) | Yes |
| Single-line block-body lambda (e.g. `-> { target.put(k, v); }`) | `target.putAll(source)` | Yes |
| Multi-line block-body lambda (`-> {` line + body + `});` line) | `target.putAll(source)` | Yes |
| `source.forEach((k, v) -> (cond ? a : b).put(k, v))` (parenthesized / ternary / cast target) | `(cond ? a : b).putAll(source)` | Yes |
| `map.forEach((k, v) -> v.forEach(item -> target.add(item)))` (nested `forEach`; the inner call is replaced) | `map.forEach((k, v) -> target.addAll(v))` | Yes |
| `synchronized (lock) { source.forEach(target::put); }` (embedded in an inline block) | `synchronized (lock) { target.putAll(source); }` | Yes |
| `consume(source.forEach(target::put))` (embedded as a call argument) | `consume(target.putAll(source))` | Yes |
| `source/* c */.forEach(target::put)` (comment inside the replaced call) | `target.putAll(source)` (comment dropped) | Yes |
| Multi-line receiver (`source` alone, `.forEach(...)` on the next line) | `target.putAll(source)` (collapsed to one line) | Yes |

## Patterns deliberately not flagged

Each row is a loop the check matches structurally but refuses, because the bulk call it would
suggest does not exist or does not compile.

| Pattern | Why not flagged |
| --- | --- |
| Source is an `Iterable`, `Stream` or any other non-`Collection` | `Collection.addAll` takes a `Collection` |
| Source is a `Map`-shaped type that is not a `Map` (`SparseArray`) on a `putAll` path | `Map.putAll` takes a `Map` |
| Target does not declare the bulk method (`SparseArray`, `ContentValues`) | the method does not exist on that type |
| Source is a primitive array (`int[]`) or primitive varargs (`int...`) | no boxing-free bulk API exists; an *object* array becomes `Collections.addAll` instead |
| Either operand's type is declared in the same file, even when it declares the bulk method | the classpath cannot confirm a same-file type's members, so the rewrite is refused rather than guessed |
| Either operand's type does not resolve through this file's imports and package | same |
| Source is a same-file `enum`'s `values()` | resolves to a same-file type, and is an array besides |
| Source is a type variable (`T`) | a type parameter's name resolves to nothing on the classpath, which is the same refusal as any unknown name |
| Source is a fully qualified static call (`java.time.DayOfWeek.values()`) | a multi-segment qualifier is not resolved to a receiver type |
| The `forEach` receiver is itself a method call | the chain's element type is not read |
| A `forEach` lambda parameter whose receiver is raw, is itself a call, or whose type argument is a wildcard | there is nothing to infer the parameter's type from |
| A lambda parameter whose enclosing call is not `forEach` (`computeIfAbsent`, `merge`) | only `forEach` passes the receiver's type arguments to the lambda in declaration order |
| Ternary operand whose branches resolve to different types | neither branch describes every value the operand can take |
| A multi-line operand containing a text block | collapsing it to one line strips the line terminator the opening `"""` requires |

Part of [auto-fix coverage](../auto-fix-coverage.md).