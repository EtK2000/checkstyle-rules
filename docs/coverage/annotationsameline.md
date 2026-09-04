# AnnotationSameLineCheck auto-fix coverage

Puts a declaration's annotations on its own line, in alphabetical order, wherever the check fires:
method, constructor, catch, lambda and receiver parameters, record components, and for-each and
for-init variables. Both boundaries come from node positions, the group's first modifier and the
declaration's first token, so nothing is matched against the line text and the annotations may be
spelled any way the language allows: with whitespace or a comment after the `@`, with a package
qualifier, with that qualifier split across lines, with an argument list written across lines, or
with characters outside ASCII.

Each annotation is sliced where it stands and re-emitted as written, so an argument holding a
string, a char literal, a nested annotation, a comma at any depth, or text that looks like another
annotation (`@Val("@X @Y")`) travels unchanged. An annotation written across lines is folded onto
the joined line (`@B(` / `n = 1)` becomes `@B(n = 1)`). A non-annotation modifier written among the
annotations is kept, after them, because sorting leaves no other placement defined.

Comments inside the replaced span are carried rather than dropped. Where one lands is decided by the
stretch of text between two of the group's tokens that it sits in, not by the comment itself: a
stretch written on one line can hold only block comments, so its comments stay inline, emitted after
the sorted run, while a stretch running across lines keeps each of its lines above the joined
declaration. That is the only placement a `//` comment has, or a block comment spanning lines, and it
is why `@B` on one line with `/* c */ @A String p` below puts the comment above rather than inline.
When the span's own first line begins inside a block comment or a text block opened further up, the
carried lines go above the line that opened it, and the lines between are re-emitted as they stand,
so nothing is carried into the open construct. Inside a block comment that would close it early and
the file would stop parsing; inside a text block it would silently change the string's value. A
comment ahead of the first annotation, or trailing the declaration, is outside the span and is left
exactly where it was.

A blank line inside the group is removed with the rest of the whitespace. The joined line is emitted
even when it runs past the project's 120-column budget: joining is the only way to clear the
violation, so refusing would keep it standing for good.

Decoy `@Name` sequences inside a string, comment or text block are never matched, because nothing is
matched: they are not annotations in the parse. Annotations the check does not own are never
collected either, and that includes a type-use annotation (`List<@A String>`, `String @A []`), a
type-parameter annotation, an annotation on an enum constant, a try-with-resources resource, a
field, a method, a class or a local variable, and an annotation nested inside another annotation's
argument.

## Not supported

| Pattern | Reason |
| --- | --- |
| A group holding an annotation that cannot be written on one line, such as one whose argument is a text block | Every annotation has to end up on the declaration's line, and a text block's opening delimiter has to be the last thing on its own line, so no layout satisfies the check. A named `SkipResult` says so |
| A group holding an annotation written across lines with a comment inside its argument list | Folding it onto one line is refused while a comment sits inside the span, so the annotation cannot reach the declaration's line. Same named `SkipResult` |
| A position whose text has moved under an earlier fix in the same pass | Nothing the check recognises sits at the reported position any more, so the fix is left for the next pass |
| A buffer that does not parse | The classifier needs the parse. The fix is retried once the buffer parses |

Part of [auto-fix coverage](../auto-fix-coverage.md).