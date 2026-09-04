# AnnotationOwnLineCheck auto-fix coverage

Puts a declaration's annotations one to a line, in alphabetical order, above the declaration, and
deletes the blank lines the check reports. Applies wherever the check fires: classes, interfaces,
enums, records, annotation types, methods, constructors, compact constructors, fields, local
variables, annotation fields, enum constants and package declarations.

Both boundaries come from node positions, the group's first modifier and the token that follows the
group, so nothing is matched against the line text and the annotations may be spelled any way the
language allows: with whitespace or a comment after the `@`, with a package qualifier, with that
qualifier split across lines, with an argument list written across lines, or with characters outside
ASCII. A declaration whose own type is split across lines is read the same way, so an annotation
sharing the declaration's first line is reported there rather than overlooked.

Each annotation is sliced where it stands and re-emitted as written, so an argument holding a
string, a char literal, a nested annotation, a comma at any depth, or text that looks like another
annotation travels unchanged. An annotation written across lines is folded onto one line where that
is safe, and kept as written where a comment or a text block inside it makes folding lose the
comment or the line break the literal depends on. A non-annotation modifier written among the
annotations is kept, after them, because sorting leaves no other placement defined, and the
modifiers keep their own order. Code written ahead of the group on its line keeps that line, and the
group starts on the next one, which is what putting an annotation on its own line means when
something else shares it.

Comments travel with what they are written against. A comment on a token's own line stays on that
token's line, so a trailing `// why` on an annotation moves with that annotation when the group is
sorted. A whole line of comment between two tokens goes above the one below it and is copied exactly
as written, so a block comment's interior, including a blank line inside it, is not rewritten. A
comment written immediately before the group moves down with the group rather than staying on the
line the group is lifted off.

A comment that cannot be carried in pieces is not carried in pieces. When the text between two of
the group's tokens opens a block comment that closes further down, the whole stretch keeps its own
lines, in the order it was written, and travels with the token that follows it: sorting moves the
token it would otherwise have ridden on, which would separate the opener from its closing delimiter
and leave whatever sorting put between them inside the construct. For the same reason, when the
group's own first line begins inside a block comment or a text block opened above it, the text ahead
of the group on that line keeps the line, since it holds that construct's terminator. And a line
comment written after a modifier moves to the line above the declaration rather than staying beside
it, because the modifiers end up on the declaration's own line and a line comment joined onto it
would comment the declaration out. One written after an annotation stays where it is, since each
annotation keeps a line to itself.

A blank line inside a text block inside an annotation is left alone, because deleting it would
change the string's value. One inside a block comment is removed with the rest.

Decoy `@Name` sequences inside a string, comment or text block are never matched, because nothing is
matched: they are not annotations in the parse. Annotations the check does not own are never
collected either, and that includes a parameter, a catch, lambda or receiver parameter, a for-each
or for-init variable, a record component, a try-with-resources resource, a pattern variable in any
of its four positions, every type-use position (`List<@A String>`, `String @A []`, `java.util.@A
List`, a cast, a `new`, an `instanceof`, a `throws`), a type-parameter annotation, and an annotation
nested inside another annotation's argument.

## Not supported

| Pattern | Reason |
| --- | --- |
| A position whose text has moved under an earlier fix in the same pass | Nothing the check recognises sits at the reported position any more, so the fix is left for the next pass |
| A buffer that does not parse | The classifier needs the parse. The fix is retried once the buffer parses |

Part of [auto-fix coverage](../auto-fix-coverage.md).
