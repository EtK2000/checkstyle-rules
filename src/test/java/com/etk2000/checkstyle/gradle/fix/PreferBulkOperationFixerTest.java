package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferBulkOperationFixerTest {
	private final CheckstyleFixer fixer = new PreferBulkOperationFixer();

	@Test
	public void testArrayCopy() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < src.length; ++i)",
				"\t\t\tdst[i] = src[i];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tSystem.arraycopy(src, 0, dst, 0, src.length);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayCopyBraced() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < src.length; ++i) {",
				"\t\t\tdst[i] = src[i];",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tSystem.arraycopy(src, 0, dst, 0, src.length);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayCopyBracketSuffixedRhsRejected() {
		// RHS `arr[i][0]` has prefix `arr[i]` but continues with `[0]` (2D access).
		// The after-match char check (next is `[`) must reject this as NOT-an-arraycopy.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = arr[i][0];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, arr[i][0]);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayCopyDotSuffixedRhsRejected() {
		// RHS `arr[i].clone()` has prefix `arr[i]` but continues with `.clone()`. The
		// after-match char check (next is `.`) must reject this as NOT-an-arraycopy,
		// falling through to fixArrayFill. Uses matching `arr` names because the check
		// requires LHS array == bound array for fill path.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = arr[i].clone();"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, arr[i].clone());"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayCopyIdentSuffixedRhsRejected() {
		// RHS `arr[i]extra` has prefix `arr[i]` but continues with an identifier char.
		// The after-match char check must reject this as NOT-an-arraycopy.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = arr[i]extra;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, arr[i]extra);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayCopyNoLessThanBoundReturnsNull() {
		// Dispatch fires on `.length` + `] = ` + `[` in after-assign, but `fixArrayCopy`
		// requires `< ` before `.length` to extract the source. A reverse-iteration
		// loop with no `< ` must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = arr.length; i > 0; --i) {",
				"\t\t\tdst[i] = src[i];",
				"\t\t}"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testArrayFill() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = 0;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, 0);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillBoundLessThanInsideStringLiteralSkipped() {
		// `"< "` in the condition is inside a string literal. The structural
		// scanner skips it and finds the real `< ` before `arr.length`.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; \"< \".length() < arr.length; ++i)",
				"\t\t\tarr[i] = 0;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, 0);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillBoundWithoutStructuralLengthReturnsNull() {
		// The loop condition uses `.lengthArray` (no structural `.length`). The
		// identifier-boundary check must reject every `.length` prefix and the helper
		// falls through to return null; fixer returns null.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < obj.lengthArray; ++i)",
				"\t\t\tobj.lengthArray[i] = 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testArrayFillBraced() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i) {",
				"\t\t\tarr[i] = 0;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, 0);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillCharLiteralCloseBrace() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i) {",
				"\t\t\tarr[i] = '}';",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, '}');"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillEmptyValueReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = ;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testArrayFillNoLessThanBoundReturnsNull() {
		// Dispatch fires on `.length` + `] = ` + no `[` in after-assign, but
		// `fixArrayFill` requires `< ` before `.length` to extract the array name.
		// A reverse-iteration fill must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = arr.length; i > 0; --i)",
				"\t\t\tarr[i] = 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testArrayFillNonZeroValue() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = -1;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, -1);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillSameArrayDifferentIndexNotArraycopy() {
		// `arr[i] = arr[0]` is a FILL (every element gets `arr[0]`), NOT a copy.
		// The dispatch tries arraycopy first; its RHS validation must reject this
		// (LHS index `i` doesn't match RHS index `0`) and fall back to fill.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = arr[0];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, arr[0]);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillSourceHasFieldNameStartingWithLength() {
		// Source like `obj.lengthArray.length` has `.length` as a prefix of `.lengthArray`.
		// The identifier-boundary check must skip the false match and land on the real
		// `.length` at the end.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < obj.lengthArray.length; ++i)",
				"\t\t\tobj.lengthArray[i] = 0;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(obj.lengthArray, 0);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillSourceNameHasLengthPrefix() {
		// The source array is named `lengthValues` (starts with `length`). The substring
		// matcher must NOT match `.length` as part of `.lengthValues` — identifier
		// boundary check in `substringBetweenIdentBoundary` ensures this.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < lengthValues.length; ++i)",
				"\t\t\tlengthValues[i] = 0;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(lengthValues, 0);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillStringLiteralCloseBrace() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i) {",
				"\t\t\tarr[i] = \"}\";",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, \"}\");"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillStringLiteralOpenBrace() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i) {",
				"\t\t\tarr[i] = \"{\";",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, \"{\");"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillStringValueWithEscapedQuote() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = \"a\\\"b\";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, \"a\\\"b\");"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillStringValueWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = \"a;b\";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, \"a;b\");"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillStringValueWithSlashes() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = \"http://x\"; // comment"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, \"http://x\");"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillTextBlockValue() {
		// Fill value is a multi-line text block. `extractValueUpToSemicolon` must skip
		// the `;` inside the text block content and find the structural `;` after the
		// closing `"""`. The joined value preserves the text-block delimiters and the
		// content (note: the flattened output is not syntactically valid Java, but the
		// test's purpose is to validate the scanner's text-block handling).
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = \"\"\"",
				"\t\t\tSELECT * FROM t;",
				"\t\t\tWHERE x = 1",
				"\t\t\t\"\"\";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\t\tArrays.fill(arr, \"\"\" \t\t\tSELECT * FROM t; \t\t\tWHERE x = 1 \t\t\t\"\"\");"),
				result.replacement()
		);
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillValueContainsBracketNotArraycopy() {
		// Fill value has `[` in it (e.g. `-a[b[0]]`). The dispatch must NOT route this
		// to `fixArrayCopy` (which would produce a self-copy). Must route to fill.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = -a[b[0]];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, -a[b[0]]);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testArrayFillValueContainsUnaryPlusAndBracket() {
		// Similar to above but with UNARY_PLUS. Still a fill, not a copy.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < arr.length; ++i)",
				"\t\t\tarr[i] = +otherArr[0];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\tArrays.fill(arr, +otherArr[0]);"), result.replacement());
		assertEquals(Set.of("java.util.Arrays"), result.importsToAdd());
	}

	@Test
	public void testForEachAddAllBraced() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source) {",
				"\t\t\ttarget.add(item);",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachAddAllBraceless() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source)",
				"\t\t\ttarget.add(item);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachAddAllBracelessMissingCloseParenReturnsNull() {
		// Dispatch fires on `: ` + `.add(`, but `extractForEachSource` needs a
		// matching `)` to delimit the source expression. A malformed for-each with
		// no closing paren must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source",
				"\t\t\ttarget.add(item);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachAddAllBracelessMissingTargetReturnsNull() {
		// Dispatch fires but `extractTargetName` finds no identifier before `.add(`
		// (the `)` ending the for-each clause is immediately followed by ` .add(`),
		// so the fixer must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source) .add(item);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachAddAllBracelessWithTrailingComment() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source) // violation: some message",
				"\t\t\ttarget.add(item);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachAddAllMethodCallSource() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : map.values())",
				"\t\t\ttarget.add(item);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(map.values());"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachAddAllNestedParenSource() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : getList(a, b))",
				"\t\t\ttarget.add(item);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(getList(a, b));"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaAddAll() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(item -> other.add(item));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\tother.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaAddAllBlockBodyMultiLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(item -> {",
				"\t\t\tother.add(item);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tother.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaAddAllDottedTarget() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(item -> this.other.add(item));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\tthis.other.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBailsOnBlockCommentBeforeForEach() {
		// An inline `/* */` between receiver and `.forEach(` breaks the backward
		// receiver scan. The fixer bails safely instead of producing garbled output.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource/* comment */.forEach(target::put);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnComplexAddTargetExpression() {
		// `.add(` target expression contains parens (nested expression), should bail.
		// Symmetric with the `.put(` variant (`testForEachLambdaBailsOnComplexTargetExpression`).
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(item -> (cond ? a : b).add(item));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnComplexTargetExpression() {
		// target expression contains parens (nested expression), should bail
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> (cond ? a : b).put(k, v));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnEmptyBlockBodyBeforePut() {
		// `(k, v) -> {.put(k, v)` yields `{` after the arrow; after stripping the brace
		// the target becomes empty. `extractLambdaTarget` must reject the empty target.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {.put(k, v));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnLeadingArrowWithColumn() {
		// When `column` points to the INNER `.forEach(`, leading content contains the
		// outer lambda's `->`. The `leading.contains("->")` guard must bail.
		final var lines = new ArrayList<>(List.of(
				"\t\tmap.forEach((k, v) -> other.forEach(item -> target.add(item)));"
		));
		final var line = lines.getFirst();
		final var outerForEachIdx = line.indexOf(".forEach(");
		final var innerForEachIdx = line.indexOf(".forEach(", outerForEachIdx + 1);
		final var innerColumn = innerForEachIdx + ".forEach".length();
		assertNull(fixer.fix(lines, 0, innerColumn));
	}

	@Test
	public void testForEachLambdaBailsOnNestedCall() {
		// Nested forEach: leading content contains `->`, the fixer bails to avoid
		// extracting a garbled target from the outer lambda body.
		final var lines = new ArrayList<>(List.of(
				"\t\tmap.forEach((k, v) -> other.forEach(item -> target.add(item)));"
		));
		// With column=0 fallback, the OUTER forEach is picked and its target extraction
		// produces a multi-expression string that fails the simple-identifier validation.
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnUnclosedBraceInLeading() {
		// `synchronized (lock) { source.forEach(...); }` has an unclosed `{` in the prefix;
		// the fixer bails to avoid dropping the closing `}`.
		final var lines = new ArrayList<>(List.of(
				"\t\tsynchronized (lock) { source.forEach(target::put); }"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBailsOnUnclosedParenInLeading() {
		// `method(source.forEach(...))` has an unclosed `(` in the prefix;
		// the fixer bails to avoid dropping the closing `)`.
		final var lines = new ArrayList<>(List.of(
				"\t\tmethod(source.forEach(target::put));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaBlockBodyMultiLinePutAll() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyMultipleCommentLines() {
		// Several consecutive `//` comment lines are each stripped independently,
		// and the fixer still locates the single real body statement.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\t// first comment",
				"\t\t\t// second comment",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithBlockCommentContainingPut() {
		// A multi-line block comment whose continuation contains `target.put(k, v);`
		// text. The shared-state comment stripper must treat the entire comment as
		// comment; the fixer must use the REAL body (`real.put`) not the text inside
		// the comment. This guards against silent wrong-target corruption.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\t/* future cleanup:",
				"\t\t\t   target.put(k, v);",
				"\t\t\t*/",
				"\t\t\treal.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\treal.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithBlockCommentNoStarPrefix() {
		// A multi-line `/* ... */` block comment inside the body where continuation
		// lines do NOT start with `*`. The shared-state comment stripper removes all
		// the comment's content, leaving only the real body statement visible.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\t/* this is a",
				"\t\t\t   multi-line comment */",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithBlockCommentStarPrefix() {
		// A javadoc-style `/*` ... `*/` with `*` prefix lines inside the body. The
		// shared-state comment stripper handles cross-line state, so the fix succeeds.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\t/*",
				"\t\t\t * multi-line",
				"\t\t\t */",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithBraceInCharLiteral() {
		// `}` inside a char literal inside the block body must not prematurely close it.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\ttarget.put(k, '}');",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithLineComment() {
		// A `//` line comment inside the body is stripped by `stripComment`, so the
		// target extraction sees only the real body statement.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"\t\t\t// this key/value mapping is noteworthy",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaBlockBodyWithWhitespaceLines() {
		// Blank and whitespace-only lines between `{` and the body statement are
		// stripped via `.strip()` and do not contribute to the joined inner text.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> {",
				"",
				"\t\t\t   ",
				"\t\t\ttarget.put(k, v);",
				"\t\t});"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaEmptySourceReturnsNull() {
		// `.forEach(` with no preceding receiver yields an empty source extraction.
		final var lines = new ArrayList<>(List.of(
				"\t\t.forEach(target::put);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaPreservesColumnExactHit() {
		// Column points directly at the `(` of `.forEach(` (typical real-invocation case).
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> target.put(k, v));"
		));
		final var openParenCol = lines.getFirst().indexOf(".forEach(") + ".forEach".length();
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, openParenCol));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaPreservesLeadingIfStatement() {
		// `if (flag) source.forEach(...)` is a valid one-line if-statement; the fixer
		// should preserve the `if (flag) ` prefix and rewrite just the forEach call.
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) source.forEach((k, v) -> target.put(k, v));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\tif (flag) target.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaPutAll() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> target.put(k, v));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaPutAllDottedTarget() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> this.target.put(k, v));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\tthis.target.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaPutAllPreservesTrailingContent() {
		// Trailing content after `);` on the same line should be preserved.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> target.put(k, v)); done();"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source); done();"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaPutAllSemicolonInStringBody() {
		// A string literal in the body containing `;` must not confuse the braceless
		// end-of-loop scan or the body extraction.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> target.put(k, \"a;b\"));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaReturnNullMultiLineUnclosed() {
		// Multi-line `.forEach(` whose closing `)` is missing across all lines.
		// `findClosingParen` returns null; the fixer must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) ->",
				"\t\t\ttarget.put(k, v)"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaReturnNullNoPut() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> System.out.println(k));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaSingleLineBlockBodyPutAll() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach((k, v) -> { target.put(k, v); });"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachLambdaSourceEndsWithDotReturnsNull() {
		// A source extracted as `source.` (trailing dot, e.g. `source..forEach(`) is rejected.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource..forEach((k, v) -> target.put(k, v));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachLambdaSourceStartsWithDotReturnsNull() {
		// A source extracted as `.something` (leading dot) is rejected. The backward
		// identifier scan includes dots, so a line starting with `.name.forEach(` yields
		// a source with a leading dot, which the guard must reject.
		final var lines = new ArrayList<>(List.of(
				"\t\t.something.forEach(target::put);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachMethodRefAdd() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(other::add);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\tother.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachMethodRefEmptyTargetReturnsNull() {
		// `::put` with no receiver before the `::` yields an empty ref target.
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach(::put);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachMethodRefMultiLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(",
				"\t\t\tother::add",
				"\t\t);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tother.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachMethodRefNoCloseParenReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(other::add"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachMethodRefOtherReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach(target::remove);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testForEachMethodRefPut() {
		final var lines = new ArrayList<>(List.of(
				"\t\tsource.forEach(target::put);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachNoArrowNoDoubleColonReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.forEach(consumer);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testGuardBracedUnclosed() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source) {",
				"\t\t\ttarget.add(item);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testGuardBracelessNoSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : source)",
				"\t\t\ttarget.add(item)"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testGuardNotForLoop() {
		final var lines = new ArrayList<>(List.of("\t\tSystem.out.println(\"hello\");"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testGuardUnmatchedForLoop() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < 10; ++i)",
				"\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_BULK_SKIP, result.reason());
	}

	@Test
	public void testIndexedAddAll() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < source.size(); ++i)",
				"\t\t\ttarget.add(source.get(i));"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIndexedAddAllBoundWithoutLessThanReturnsNull() {
		// Dispatch fires on `.size()` + `.add(` + `.get(`, but `fixIndexedAddAll`
		// needs `< ` before `.size()` to extract the source. A loop starting from
		// `source.size()` has no `< ` in that position; must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = source.size() - 1; i >= 0; --i)",
				"\t\t\ttarget.add(source.get(i));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testIndexedAddAllBraced() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < source.size(); ++i) {",
				"\t\t\ttarget.add(source.get(i));",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\ttarget.addAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIndexedAddAllMissingTargetReturnsNull() {
		// Dispatch fires but `extractTargetName` finds no identifier before `.add(`.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var i = 0; i < source.size(); ++i) .add(source.get(i));"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testPutAllEntrySet() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var entry : source.entrySet())",
				"\t\t\ttarget.put(entry.getKey(), entry.getValue());"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPutAllEntrySetBlockCommentWithFakeBrace() {
		// A multi-line block comment containing `}` inside a braced for-each body must
		// not confuse `findLoopEndLine`; the real matching brace is on line 4.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var entry : source.entrySet()) {",
				"\t\t\t/* fake } brace",
				"\t\t\t   inside comment */",
				"\t\t\ttarget.put(entry.getKey(), entry.getValue());",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPutAllEntrySetBraced() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var entry : source.entrySet()) {",
				"\t\t\ttarget.put(entry.getKey(), entry.getValue());",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\ttarget.putAll(source);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPutAllEntrySetMissingColonSpaceReturnsNull() {
		// Dispatch fires on `.entrySet()` + `.put(` but `fixEntrySetPutAll` needs
		// `: ` before `.entrySet()` to extract the map. An indexed for-loop that
		// references `.entrySet()` elsewhere must bail safely.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (int i = 0; i < map.entrySet().size(); ++i)",
				"\t\t\ttarget.put(keys[i], vals[i]);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testPutAllEntrySetMissingTargetReturnsNull() {
		// Dispatch fires but `extractTargetName` finds no identifier before `.put(`.
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var entry : source.entrySet()) {",
				"\t\t\t.put(entry.getKey(), entry.getValue());",
				"\t\t}"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}
}