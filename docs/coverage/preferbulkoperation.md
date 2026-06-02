# PreferBulkOperationCheck auto-fix coverage

The fixer reuses the check's AST classifier and replaces the flagged span in place, so it preserves
any leading or trailing text on the span's lines (a guarding `if`, an enclosing block or call, a
following statement) and rewrites the call even when it is nested inside another expression (e.g.
`if (flag) source.forEach(...)` becomes `if (flag) target.putAll(source);`). A comment inside the
replaced span is dropped. The receiver, target, source, and value text are sliced verbatim from the
source, so any shape (qualified name, generics, cast, ternary, method-ref qualifier) is preserved
exactly.

When a sliced operand (the value, receiver, or source) itself spans more than one line, the lines
are rejoined without a stray space; but if a comment falls inside the operand's own span, the check
does not fire at all (no auto-fix), since collapsing it to one line would comment out the trailing
tokens. A comment that precedes the operand's first token (e.g. `for (var x : /* c */ src` on the
opening line) is outside the operand span and does not block the fix.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `for (var x : source) target.add(x)` | `target.addAll(source)` | Yes |
| `for (var i = 0; i < source.size(); ++i) target.add(source.get(i))` | `target.addAll(source)` | Yes |
| `for (var e : source.entrySet()) target.put(e.getKey(), e.getValue())` | `target.putAll(source)` | Yes |
| `source.forEach((k, v) -> target.put(k, v))` | `target.putAll(source)` | Yes |
| `source.forEach(target::put)` | `target.putAll(source)` | Yes |
| `list.forEach(item -> other.add(item))` | `other.addAll(list)` | Yes |
| `list.forEach(other::add)` | `other.addAll(list)` | Yes |
| `for (var i = 0; i < src.length; ++i) dst[i] = src[i]` | `System.arraycopy(src, 0, dst, 0, src.length)` | Yes |
| `for (var i = 0; i < arr.length; ++i) arr[i] = value` | `Arrays.fill(arr, value)` | Yes |
| Single-line block-body lambda (e.g. `-> { target.put(k, v); }`) | `target.putAll(source)` | Yes |
| Multi-line block-body lambda (`-> {` line + body + `});` line) | `target.putAll(source)` | Yes |
| `source.forEach((k, v) -> (cond ? a : b).put(k, v))` (parenthesized / ternary / cast target) | `(cond ? a : b).putAll(source)` | Yes |
| `map.forEach((k, v) -> v.forEach(item -> target.add(item)))` (nested `forEach`; the inner call is replaced) | `map.forEach((k, v) -> target.addAll(v))` | Yes |
| `synchronized (lock) { source.forEach(target::put); }` (embedded in an inline block) | `synchronized (lock) { target.putAll(source); }` | Yes |
| `consume(source.forEach(target::put))` (embedded as a call argument) | `consume(target.putAll(source))` | Yes |
| `source/* c */.forEach(target::put)` (comment inside the replaced call) | `target.putAll(source)` (comment dropped) | Yes |
| Multi-line receiver (`source` alone, `.forEach(...)` on the next line) | `target.putAll(source)` (collapsed to one line) | Yes |

Part of [auto-fix coverage](../auto-fix-coverage.md).