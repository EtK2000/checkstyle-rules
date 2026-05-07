package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

public class JitInefficiencyFixerTest {
	@Test
	public void appendConcatLiteralAndIdent() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"key=\" + value);";
		final var attempt = fixer.fix(List.of(line), 0, line.indexOf('+'));
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tsb.append(\"key=\").append(value);", result.replacement().getFirst());
	}

	@Test
	public void appendConcatRefusesNonStringConcat() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(a + b);";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatRefusesNumericLeadingChain() {
		// `1 + 2 + "x"` evaluates as `"3x"`; naive `.append(1).append(2).append("x")` splitting yields `"12x"`
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(1 + 2 + \"x\");";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatTextBlockBail() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"\"\"prefix\"\"\" + value);";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatThreeOperandsStringLeading() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"a\" + b + c);";
		final var attempt = fixer.fix(List.of(line), 0, line.indexOf('+'));
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tsb.append(\"a\").append(b).append(c);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanFalseLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(false);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.FALSE;", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanTrueLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(true);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.TRUE;", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanVariable() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(flag);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.valueOf(flag);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorInteger() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Integer(42);";
		final var col = line.indexOf("new Integer");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var x = Integer.valueOf(42);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorRefusesNonBoxedType() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Foo(42);";
		final var col = line.indexOf("new Foo");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void boxedConstructorRefusesUnclosedParen() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Integer(42";
		final var col = line.indexOf("new Integer");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void emptyStringConcatCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + 'x';";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf('x');", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatEscapedQuoteCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + f('\\'');";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(f('\\''));", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatLeft() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + x;";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(x);", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatPlusInsideCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + f('+');";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(f('+'));", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatRefusesChain() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + a + b;";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void emptyStringConcatRefusesChainReversed() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = a + b + \"\";";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void emptyStringConcatRight() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = name + \"\";";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(name);", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatTextBlockBail() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\"\"text\"\"\" + \"\" + x;";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf("\"\" +")));
	}

	@Test
	public void newStringLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String(\"hello\");";
		final var col = line.indexOf("new String");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = \"hello\";", result.replacement().getFirst());
	}

	@Test
	public void newStringRefusesComplexArg() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String(getValue());";
		final var col = line.indexOf("new String");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void newStringRefusesEmptyArg() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String();";
		final var col = line.indexOf("new String");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void newStringVariable() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var copy = new String(existing);";
		final var col = line.indexOf("new String");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var copy = existing;", result.replacement().getFirst());
	}

	@Test
	public void returnsNullWhenNoPattern() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = 42;";
		assertNull(fixer.fix(List.of(line), 0, 0));
	}

	@Test
	public void stringBufferRefusesInputStreamSubstring() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new StringBufferInputStream(bytes);";
		final var col = line.indexOf("new StringBuffer");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void stringBufferToBuilder() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var sb = new StringBuffer(\"hi\");";
		final var col = line.indexOf("new StringBuffer");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var sb = new StringBuilder(\"hi\");", result.replacement().getFirst());
	}

	@Test
	public void stringConcatArrayLhsArrayMentionedInBlockCommentPasses() {
		// `lineHasUnsafeArrayReference` must skip block comments. A comment
		// containing the array name on a non-body loop line is not a real
		// reference; the rewrite should still apply.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\t/* arr is debug-only */",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsArrayMentionedInLineCommentPasses() {
		// Same as above for `//` comments.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\t// arr is dead",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsArrayMentionedInStringLiteralPasses() {
		// Receiver text inside a string literal must not be flagged as a
		// real reference.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tlog(\"debug arr\");",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsArrayMutatedByMethodCallBails() {
		// `Arrays.fill(arr, "")` mutates `arr`'s contents; `mutatesIdentifier`
		// can't see through the call. The fixer must still bail because `arr`
		// is mentioned (as an arg) on a non-body line in the loop scope.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString[] arr = new String[3];",
				"\tarr[0] = \"\";",
				"\tfor (var x : list) {",
				"\t\tArrays.fill(arr, \"\");",
				"\t\tarr[0] = arr[0] + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 4, 2));
	}

	@Test
	public void stringConcatArrayLhsArrayVarReassignedBails() {
		// Array variable `arr` is reassigned inside the loop; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString[] arr = new String[3];",
				"\tarr[0] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr = newArr();",
				"\t\tarr[0] = arr[0] + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 4, 2));
	}

	@Test
	public void stringConcatArrayLhsBodyLineIndexMutationBails() {
		// Body line itself increments the index via side-effect on the RHS;
		// rewrite would lose per-iteration semantics. Currently the validator
		// excludes the body line from mutation scanning, but the loop-top-line
		// check catches the for-init declaration of `i`, so this still bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[0] = \"\";",
				"\tfor (var i = 0; i < 5; ++i)",
				"\t\tarr[i] = arr[i] + (\"\" + ++i);"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsBodyLineMutatesIndexExternalBails() {
		// Body line itself mutates the external index via RHS side-effect
		// (`++k`). The validator scans the body line for index-mutation
		// patterns and bails. (The for-each iter-var `x` is unrelated to k.)
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list)",
				"\t\tarr[k] = arr[k] + (\"\" + ++k);"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsBodyLinePacksChainPrefixCompoundAssignBails() {
		// Body line packs an `op=` mutation of the leftmost prefix; bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tobj.f[k] = \"\";",
				"\tfor (var x : list)",
				"\t\tobj.f[k] = obj.f[k] + x; obj += newObjFlag();"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsBodyLinePacksChainPrefixCousinNameNotMatchPasses() {
		// Boundary pair: `myObj` and `newObj` (both containing `obj` as
		// a suffix) on the body line must not be conflated with the chain
		// prefix `obj`. Confirms `containsChainAssignment`'s identifier
		// boundary handling.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tobj.f[k] = \"\";",
				"\tfor (var x : list)",
				"\t\tobj.f[k] = obj.f[k] + myObj.length();"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(obj.f[k]);",
				"\tfor (var x : list)",
				"\t\tsb.append(myObj.length());",
				"\tobj.f[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsBodyLinePacksChainPrefixSimpleAssignBails() {
		// Body line packs an LHS `+=` followed by an `=` mutation of the
		// leftmost chain prefix `obj`. `containsChainAssignment` catches
		// this on the body line where the chain-substring scan does not.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tobj.f[k] = \"\";",
				"\tfor (var x : list)",
				"\t\tobj.f[k] = obj.f[k] + x; obj = newObj();"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsBodyLinePacksChainPrefixStringLiteralPasses() {
		// `containsChainAssignment` must skip string literals; a chain
		// assignment-shaped substring inside `"..."` is not a real mutation.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tobj.f[k] = \"\";",
				"\tfor (var x : list)",
				"\t\tobj.f[k] = obj.f[k] + \"obj = newObj()\";"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(obj.f[k]);",
				"\tfor (var x : list)",
				"\t\tsb.append(\"obj = newObj()\");",
				"\tobj.f[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsBodyLineRebindsArrayBails() {
		// Body line rebinds `arr` via RHS side-effect. The validator scans the
		// body line for `arr = ...` patterns and bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString[] arr = new String[3];",
				"\tarr[0] = \"\";",
				"\tfor (var x : list)",
				"\t\tarr[0] = (arr = newArr())[0] + x;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsChainedIndex() {
		// Chained `arr[i][j]` LHS where both indexes are external parameters;
		// fixer rewrites to a single SB seeded from `arr[i][j]`.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tmatrix[k][j] = matrix[k][j] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(matrix[k][j]);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tmatrix[k][j] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsChainedIndexInnerVarBails() {
		// Chained `arr[k][i]` where `i` is the inner loop iter variable; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var matrix = new String[3][3];",
				"\tfor (var i = 0; i < 3; ++i)",
				"\t\tmatrix[k][i] = matrix[k][i] + \"!\";"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsChainedIndexThis() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tthis.grid[k][j] = this.grid[k][j] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(this.grid[k][j]);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tthis.grid[k][j] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsClassicForUnparseableHeaderBails() {
		// Classic `for` header with an unclosed block comment makes
		// `findForHeaderEnd` return -1; the validator must fail closed
		// because we can't verify the index-binding invariant.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (/* unclosed",
				"\t\tj = 0; j < n; ++j)",
				"\t\tarr[k] = arr[k] + j;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsCompoundAssignIndexBails() {
		// Index `k` is mutated by `k += 1`; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk += 1;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsExternalIndex() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tarr[k] = arr[k] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(arr[k]);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tarr[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsForEachIterVarBails() {
		// Index `x` is the for-each iter variable, so each iteration writes a
		// different slot; the StringBuilder rewrite would be wrong.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[3];",
				"\tfor (var x : indices)",
				"\t\tarr[x] = arr[x] + \"!\";"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderArrayLengthPasses() {
		// For-header reads `arr.length` to bound the loop; the for-header line
		// is exempt from `lineHasUnsafeArrayReference` (otherwise this very
		// common shape would be unfixable).
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var j = 0; j < arr.length; ++j)",
				"\t\tarr[k] = arr[k] + j;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(arr[k]);",
				"\tfor (var j = 0; j < arr.length; ++j)",
				"\t\tsb.append(j);",
				"\tarr[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsForHeaderArrayReassignedBails() {
		// Multi-statement for-init clause that reassigns the array variable.
		// `lineHasUnsafeArrayReference` is skipped for the for-header line,
		// but `mutatesIdentifier(arrayName)` must still bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (arr = newArr(), j = 0; j < arr.length; ++j)",
				"\t\tarr[CONSTANT] = arr[CONSTANT] + j;"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderArrayReassignedCompoundBails() {
		// Boundary pair to stringConcatArrayLhsForHeaderArrayReassignedBails:
		// uses a compound-assign (`+=`) form rather than simple `=` to ensure
		// `mutatesIdentifier`'s compound branch fires on the for-header path.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (arr += newArr(), j = 0; j < arr.length; ++j)",
				"\t\tarr[CONSTANT] = arr[CONSTANT] + j;"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderBlockCommentBindingBails() {
		// `findForHeaderEnd` must skip `/* ... */` while tracking depth.
		// The for-each variable `x` is the LHS index, mentioned on the
		// header line; the binding scan must still detect it.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (/* note */ var x : list)",
				"\t\tarr[x] = arr[x] + \"!\";"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderChainPrefixMentionedBails() {
		// For-header line that mentions an intermediate chain prefix; the
		// `containsReceiverChain` scan on the for-header line must catch it.
		// (`this.matrix.cells` LHS, intermediate prefix `this.matrix` is
		// referenced as the iterable on the for-each header.)
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var entry : this.matrix.entrySet())",
				"\t\tthis.matrix.cells[k][j] = this.matrix.cells[k][j] + entry;"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderChainPrefixMutatedBails() {
		// For-header init clause REASSIGNS an intermediate chain prefix
		// (`this.matrix = pickNew()`) for an LHS whose index identifiers
		// (`a`, `b`) are external (not bound by the header), so the
		// binding-scan does not pre-bail. The for-header line scan then
		// reaches `containsReceiverChain(line, "this")` (the leftmost
		// intermediate prefix) and bails on the chain-prefix mention.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (this.matrix = pickNew(), j = 0; j < n; ++j)",
				"\t\tthis.matrix.cells[a][b] = this.matrix.cells[a][b] + j;"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderLineCommentSkipPasses() {
		// `findForHeaderEnd` `break`s out of the inner loop on `//` so paren
		// depth is preserved across lines. Multi-line for-each with a
		// trailing line-comment on line 0; the binding `int i` is on line 1,
		// caught by the for-header binding scan.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor ( // trailing",
				"\t\t\tint i : indices)",
				"\t\tarr[i] = arr[i] + \"x\";"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsForHeaderStringLiteralWithParenPasses() {
		// `findForHeaderEnd` must skip strings when tracking paren depth.
		// `"("` inside the for-each iterable contains a `(` that must NOT be
		// counted as a real header paren.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : List.of(\"(\"))",
				"\t\tarr[k] = arr[k] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(arr[k]);",
				"\tfor (var x : List.of(\"(\"))",
				"\t\tsb.append(x);",
				"\tarr[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsIntegerLiteralIndex() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tarr[0] = arr[0] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(arr[0]);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tarr[0] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsLongerNameNotSubstringMatchPasses() {
		// A line referencing `myArr` (which contains `arr` as a suffix) must
		// not be conflated with a real reference to `arr`.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tvar myArr = compute();",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsLoopIterVarBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[n];",
				"\tfor (var i = 0; i < n; ++i)",
				"\t\tarr[i] = arr[i] + \"!\";"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsMemberKDoesNotCountAsMutation() {
		// `obj.k = 5;` writes to `obj.k`, not the loop-stable `k`. The
		// mutation scanner's `prev=='.'` skip means this is correctly
		// excluded; the rewrite should still apply.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list) {",
				"\t\tobj.k = 5;",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsMultiLineForEachBails() {
		// Multi-line for-each header: the `int i` declaration is on the loop
		// top line; `mentionsIdentifier(topLine, "i")` returns true so we bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[3];",
				"\tfor (int i :",
				"\t\t\tindices)",
				"\t\tarr[i] = arr[i] + \"x\";"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsMultiLineForEachContinuationLineBails() {
		// For-each header's binding `int i :` lives on a continuation line.
		// The for-header scan must walk paren depth across lines and bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[3];",
				"\tfor (",
				"\t\t\tint i : indices)",
				"\t\tarr[i] = arr[i] + \"x\";"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsMutatedIndexBails() {
		// Index `k` is mutated by `++k` in the loop body; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\t++k;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsNegativeLiteralIndexBails() {
		// Index `-1` is not a positive integer literal; bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[3];",
				"\tarr[2] = \"\";",
				"\tfor (var x : list)",
				"\t\tarr[-1] = arr[-1] + x;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsNestedLoopInnerVarBails() {
		// Inner loop's iter var `i` used as index. The inner loop is the
		// enclosing loop, so `i` is on the inner-for-top line so we bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var k = 0; k < m; ++k) {",
				"\t\tfor (var i = 0; i < n; ++i)",
				"\t\t\tarr[i] = arr[i] + k;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 2, 3));
	}

	@Test
	public void stringConcatArrayLhsNestedLoopOuterIndexFix() {
		// Outer loop's iter var `k` used as index inside the inner loop. The
		// inner loop is the enclosing loop; `k` is NOT on the inner-for-top
		// line and is NOT mutated inside the inner loop. Rewrite is safe and
		// per-outer-iteration correct (each outer iteration gets a fresh SB).
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var k = 0; k < m; ++k) {",
				"\t\tlocal[k] = \"\";",
				"\t\tfor (var i = 0; i < n; ++i)",
				"\t\t\tlocal[k] = local[k] + i;",
				"\t}"
		);
		final var expected = List.of(
				"\t\tfinal var sb = new StringBuilder();",
				"\t\tsb.append(local[k]);",
				"\t\tfor (var i = 0; i < n; ++i)",
				"\t\t\tsb.append(i);",
				"\t\tlocal[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 3);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsNonSimpleIndexBails() {
		// Index `k.field` is not a simple IDENT/literal; analysis bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k.field] = \"\";",
				"\tfor (var x : list)",
				"\t\tarr[k.field] = arr[k.field] + x;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsPostDecrementIndexBails() {
		// Index `k` is mutated by `k--` post-decrement; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk--;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsPostIncrementIndexBails() {
		// Index `k` is mutated by `k++` post-increment; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk++;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsPrefixNameNotSubstringMatchPasses() {
		// A line referencing `arrayList` (which contains `arr` as a prefix)
		// must not be conflated with a real reference to `arr`.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tvar arrayList = newList();",
				"\t\tarr[k] = arr[k] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsRightShiftCompoundIndexBails() {
		// Index `k` is mutated by `k >>>= 1`.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk >>>= 1;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsRightShiftRegularCompoundIndexBails() {
		// Boundary pair to stringConcatArrayLhsRightShiftCompoundIndexBails:
		// `>>=` (regular right-shift compound) takes the FALSE inner branch
		// in `mutatesIdentifier` where `++k` is not bumped, distinct from
		// the `>>>=` path.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk >>= 1;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsRootIdentifierMutatedBails() {
		// Receiver `obj.f`: mutation of the leftmost segment `obj` (which
		// is a normal local/parameter, not `this`) must be detected.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tobj.f[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tobj = newObj();",
				"\t\tobj.f[k] = obj.f[k] + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsShiftCompoundIndexBails() {
		// Index `k` is mutated by `k <<= 1`; mutatesIdentifier must detect
		// the shift compound assignment.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk <<= 1;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatArrayLhsSimpleAssignIndexBails() {
		// Index `k` is mutated by `k = ...`; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfinal var arr = new String[10];",
				"\tarr[k] = \"\";",
				"\tfor (var x : list) {",
				"\t\tarr[k] = arr[k] + x;",
				"\t\tk = otherK();",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsThisArray() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tthis.arr[k] = this.arr[k] + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(this.arr[k]);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tthis.arr[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatArrayLhsThisChainInCommentPasses() {
		// `containsReceiverChain` must skip block / line comments. A chain
		// mention inside a comment is not a real reference; rewrite proceeds.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tthis.matrix.cells[i][j] = \"\";",
				"\tfor (var x : list) {",
				"\t\t// this.matrix is fine",
				"\t\tthis.matrix.cells[i][j] = this.matrix.cells[i][j] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsThisChainInStringLiteralPasses() {
		// `containsReceiverChain` must skip string literals.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tthis.matrix.cells[i][j] = \"\";",
				"\tfor (var x : list) {",
				"\t\tlog(\"this.matrix is fine\");",
				"\t\tthis.matrix.cells[i][j] = this.matrix.cells[i][j] + x;",
				"\t}"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 2);
		assertNotNull(result);
	}

	@Test
	public void stringConcatArrayLhsThisChainMutatedBails() {
		// Receiver chain `this.a.b` is reassigned inside the loop. The bare
		// `b` mutation check skips `this.a.b = ...` because of the `prev=='.'`
		// guard, but the chain-substring scan catches it.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tthis.a.b[0] = \"\";",
				"\tfor (var x : list) {",
				"\t\tthis.a.b = newArr();",
				"\t\tthis.a.b[0] = this.a.b[0] + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatArrayLhsThisChainPrefixMutatedBails() {
		// For LHS `this.matrix.cells[i][j]`, mutation of the *intermediate*
		// chain prefix `this.matrix` (not the bare `cells` and not the full
		// `this.matrix.cells`) would silently invalidate the pre/post-loop
		// snapshot. Validator must bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tthis.matrix.cells[i][j] = \"\";",
				"\tfor (var x : list) {",
				"\t\tthis.matrix = pickNew();",
				"\t\tthis.matrix.cells[i][j] = this.matrix.cells[i][j] + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatBlockCommentContainingBraceFindsRealClose() {
		// A `}` inside a multi-line block comment at the loop-top's indent must
		// not be treated as the loop's closing brace. Pre-fix, the fixer would
		// truncate at the comment's `}` and emit `final var s = sb.toString();`
		// inside the still-open comment, silently discarding the rewrite.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\ts = s + x;",
				"\t\t/* old code:",
				"\t}",
				"\t*/",
				"\t}"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list) {",
				"\t\tsb.append(x);",
				"\t\t/* old code:",
				"\t}",
				"\t*/",
				"\t}",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatBlockCommentInGapBails() {
		// Block comment in the gap between decl and loop: scanners can't track
		// multi-line literal/comment state, so we bail conservatively.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\t/* note: } unrelated brace */",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatBlockCommentOnBodyLineBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + /* a + b */ x;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatBracedDoWhile() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo {",
				"\t\ts = s + \"x\";",
				"\t}",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo {",
				"\t\tsb.append(\"x\");",
				"\t}",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatBracedDoWhileSiblingUnsafeBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"a\";",
				"\tdo {",
				"\t\tlog(s.equals(target));",
				"\t\ts = s + \"x\";",
				"\t}",
				"\twhile (s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatBracedSingleIfBody() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\tif (x != null)",
				"\t\t\ts = s + x;",
				"\t}"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list) {",
				"\t\tif (x != null)",
				"\t\t\tsb.append(x);",
				"\t}",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 3);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatBuriedAssignBails() {
		// Multi-stmt body where another stmt also writes to s; bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\ts = s.trim();",
				"\t\ts = s + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatBuriedInIfWithBracedLoop() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\tlog(x);",
				"\t\tif (x != null)",
				"\t\t\ts = s + x;",
				"\t\tother();",
				"\t}"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list) {",
				"\t\tlog(x);",
				"\t\tif (x != null)",
				"\t\t\tsb.append(x);",
				"\t\tother();",
				"\t}",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 4, 3);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatCrossMethodDeclBails() {
		// Decl in a different method than the loop must not be picked up by findDeclarationAbove.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tvoid g() {",
				"\t\tString s = \"in g\";",
				"\t\tSystem.out.println(s);",
				"\t}",
				"\tvoid f() {",
				"\t\tfor (var x : list)",
				"\t\t\ts = s + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 6, 3));
	}

	@Test
	public void stringConcatDeclWithGap() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tint x = compute();",
				"\tlog(\"start\");",
				"\tfor (var v : list)",
				"\t\ts = s + v;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tint x = compute();",
				"\tlog(\"start\");",
				"\tfor (var v : list)",
				"\t\tsb.append(v);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 4, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatDeclWithGapMentionsVarBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tlog(s);",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		assertNull(fixer.fix(lines, 3, 2));
	}

	@Test
	public void stringConcatElseBranchBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\tif (x != null)",
				"\t\t\ts = s + x;",
				"\t\telse",
				"\t\t\tlog(\"skip\");"
		);
		assertNull(fixer.fix(lines, 3, 3));
	}

	@Test
	public void stringConcatExplicitAssign() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + x;",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitBracedBody() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\ts = s + x;",
				"\t}",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list) {",
				"\t\tsb.append(x);",
				"\t}",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitChained() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + \", \" + x;",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(\", \").append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitClassicFor() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var i = 0; i < 10; ++i)",
				"\t\ts = s + i;",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var i = 0; i < 10; ++i)",
				"\t\tsb.append(i);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitDeepChain() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + x + \",\" + \" \" + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x).append(\",\").append(\" \").append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitDoubleLhsBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"a\";",
				"\tfor (var x : list)",
				"\t\ts = s + s;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatExplicitFqnType() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tjava.lang.String s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitInForIteratorBails() {
		// Check fires on `for (..; ..; s = s + i)`, but the for-line doesn't end in
		// `;`, so parseConcatAssignment bails.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var i = 0; i < n; s = s + i)",
				"\t\tSystem.out.println(i);"
		);
		assertNull(fixer.fix(lines, 1, 24));
	}

	@Test
	public void stringConcatExplicitLhsNotInRhsBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = a + x;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatExplicitMid() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = \">\" + s + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.insert(0, \">\").append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitReverse() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = x + s;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.insert(0, x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitReverseMultiPrepend() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = \"<\" + x + s;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.insert(0, \"<\" + x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatExplicitWhile() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\twhile (cond)",
				"\t\ts = s + getNext();",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\twhile (cond)",
				"\t\tsb.append(getNext());",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatFieldOnObj() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tobj.f = obj.f + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(obj.f);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tobj.f = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatFieldThis() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tthis.f = this.f + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(this.f);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tthis.f = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatFieldThisNested() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tthis.a.b = this.a.b + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(this.a.b);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tthis.a.b = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatIfElseAboveBodyBails() {
		// `else` precedes the body line. matchesIfTop catches it via !contains("else").
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\tif (x == null)",
				"\t\t\tlog(\"skip\");",
				"\t\telse",
				"\t\t\ts = s + x;"
		);
		assertNull(fixer.fix(lines, 5, 3));
	}

	@Test
	public void stringConcatIndexOfCharBails() {
		// indexOf/lastIndexOf are NOT in the safe-method allowlist because
		// StringBuilder lacks the (char) overload that String has.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"a\";",
				"\tfor (var x : list) {",
				"\t\tif (s.indexOf('x') < 0)",
				"\t\t\ts = s + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 3));
	}

	@Test
	public void stringConcatLastIndexOfCharBails() {
		// Boundary pair to stringConcatIndexOfCharBails: lastIndexOf was also
		// removed from SAFE_STRING_METHODS_ON_BUILDER (StringBuilder lacks the
		// (char) overload).
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"a\";",
				"\tfor (var x : list) {",
				"\t\tif (s.lastIndexOf('x') < 0)",
				"\t\t\ts = s + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 3));
	}

	@Test
	public void stringConcatLhsHasMethodCallBails() {
		// LHS receiver is a method call (`getObj().f`); must bail because rewriting
		// would lose the side-effecting receiver.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tfor (var x : list)",
				"\t\tgetObj().f = getObj().f + x;"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatLhsMalformedBails() {
		// LHS starts with `.`; malformed, parser must reject.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\t.bad = s + x;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatMidLoopReadCharAt() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"z\";",
				"\tfor (var x : list)",
				"\t\ts = s + s.charAt(0) + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(\"z\");",
				"\tfor (var x : list)",
				"\t\tsb.append(sb.charAt(0)).append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatMidLoopReadLengthInIfCond() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list) {",
				"\t\tif (s.length() < 100)",
				"\t\t\ts = s + x;",
				"\t}"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list) {",
				"\t\tif (sb.length() < 100)",
				"\t\t\tsb.append(x);",
				"\t}",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 3, 3);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatMidLoopUnsafeMethodBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"abc\";",
				"\tfor (var x : list) {",
				"\t\tif (s.equals(target))",
				"\t\t\ts = s + x;",
				"\t}"
		);
		assertNull(fixer.fix(lines, 3, 3));
	}

	@Test
	public void stringConcatMultiVarDeclBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\", t = \"x\";",
				"\tfor (var v : list)",
				"\t\ts = s + v;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatNestedIfBody() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\tif (x != null)",
				"\t\t\tif (!x.isEmpty())",
				"\t\t\t\ts = s + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tif (x != null)",
				"\t\t\tif (!x.isEmpty())",
				"\t\t\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 4, 4);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatNonEmptyInitLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"prefix\";",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(\"prefix\");",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatNonEmptyInitMethodCall() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = compute();",
				"\tfor (var x : list)",
				"\t\ts += x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(compute());",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatNonEmptyInitVar() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = otherVar;",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(otherVar);",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatNonStringDeclBails() {
		// Defensive: even if the check fires, the fixer must reject non-String declarations.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tInteger s = 0;",
				"\tfor (var x : list)",
				"\t\ts = s + x;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatOperandUnsafeMethodBails() {
		// Operand `s.replace('x', 'y')` would rewrite to `sb.replace('x', 'y')`, which fails
		// to compile (StringBuilder.replace has signature (int, int, String), not (char, char)).
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts = s + s.replace('x', 'y');"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatPlusAssignAcceptsTabBetweenLhsAndOp() {
		// Tab between the variable name and `+=` (or `=`) is legal Java; the
		// whitespace skip must accept any whitespace, not just spaces.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts\t+=\tx;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatPlusAssignClassicFor() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var i = 0; i < 10; ++i)",
				"\t\ts += i;",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var i = 0; i < 10; ++i)",
				"\t\tsb.append(i);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatPlusAssignDoWhileTier3() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo",
				"\t\ts += \"x\";",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo",
				"\t\tsb.append(\"x\");",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatPlusAssignForEach() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tfor (var x : list)",
				"\t\ts += x;",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatPlusAssignUnsafeMethodRhsBails() {
		// `+=` form runs referencesAreAllSafeMethodCalls on the rhs.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"abc\";",
				"\tfor (var x : list)",
				"\t\ts += s.replace('x', 'y');"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatPlusAssignWhile() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\twhile (cond)",
				"\t\ts += getNext();",
				"\tSystem.out.println(s);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\twhile (cond)",
				"\t\tsb.append(getNext());",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatPrependOperandUnsafeMethodBails() {
		// Mirror of stringConcatOperandUnsafeMethodBails for the prepends path.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"a\";",
				"\tfor (var x : list)",
				"\t\ts = s.replace('x', 'y') + s;"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatTier2DoWhileAcceptsTabSeparator() {
		// Tab between `do` and the body must be accepted (legal Java).
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo\ts = s + \"y\";",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo sb.append(\"y\");",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileArrayLhsExternalIndex() {
		// Tier-2 do-while with array LHS, external index; fix applies. The
		// seeded `sb.append(arr[k])` and trailing `arr[k] = sb.toString()`
		// flank the do-while.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tdo arr[k] = arr[k] + \"y\";",
				"\twhile (arr[k].length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(arr[k]);",
				"\tdo sb.append(\"y\");",
				"\twhile (sb.length() < 5);",
				"\tarr[k] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileArrayLhsMutatedIndexBails() {
		// Tier-2 do-while where the do-body itself mutates the index `k`;
		// validator must bail.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tdo arr[k] = arr[k] + (\"\" + ++k);",
				"\twhile (arr[k] != null);"
		);
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void stringConcatTier2DoWhileBlockCommentInDoLineBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo /* note */ s = s + \"y\";",
				"\twhile (s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileBlockCommentInWhileLineBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";",
				"\twhile (/* note */ s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileChainedExpandsToTier3() {
		// Rewritten body has 2 `(`s (chained), so the tier-3 fallback emits
		// `do` / body / while across three lines.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \", \" + x;",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo",
				"\t\tsb.append(\", \").append(x);",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileChainedIndex() {
		// Tier-2 do-while with chained-index array LHS.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tdo matrix[k][j] = matrix[k][j] + \"y\";",
				"\twhile (matrix[k][j].length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(matrix[k][j]);",
				"\tdo sb.append(\"y\");",
				"\twhile (sb.length() < 5);",
				"\tmatrix[k][j] = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileCharLiteralParenStaysTier2() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s += '(';",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo sb.append('(');",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileFieldThis() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tdo this.f = this.f + \"y\";",
				"\twhile (this.f.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tsb.append(this.f);",
				"\tdo sb.append(\"y\");",
				"\twhile (sb.length() < 5);",
				"\tthis.f = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileGapMentionsVarBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tlog(s);",
				"\tdo s = s + \"y\";",
				"\twhile (s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 2, 2));
	}

	@Test
	public void stringConcatTier2DoWhileLastLineBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileMismatchedWhileIndentBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";",
				"\t\twhile (s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileMissingSemicolonBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";",
				"\twhile (s.length() < 5) {"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileNoDeclAboveBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tdo s = s + \"y\";",
				"\twhile (s.length() < 5);"
		);
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void stringConcatTier2DoWhileNoMatchingWhileBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";",
				"\tnotAWhile();"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatTier2DoWhileParenInLiteralStaysTier2() {
		// String literal contains `(`; the paren count must ignore literals so
		// the rewrite stays tier-2 rather than expanding to tier-3.
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s += \"(\";",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo sb.append(\"(\");",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhilePlusAssign() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s += \"y\";",
				"\twhile (s.length() < 5);"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tdo sb.append(\"y\");",
				"\twhile (sb.length() < 5);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 1, 2);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(expected, result.replacement());
	}

	@Test
	public void stringConcatTier2DoWhileUnsafeMethodInWhileBails() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tString s = \"\";",
				"\tdo s = s + \"y\";",
				"\twhile (s.equals(target));"
		);
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void stringConcatVarDecl() {
		final var fixer = new JitInefficiencyFixer();
		final var lines = List.of(
				"\tvar s = \"\";",
				"\tfor (var x : list)",
				"\t\ts += x;"
		);
		final var expected = List.of(
				"\tfinal var sb = new StringBuilder();",
				"\tfor (var x : list)",
				"\t\tsb.append(x);",
				"\tfinal var s = sb.toString();"
		);
		final var result = (FixResult) fixer.fix(lines, 2, 2);
		assertNotNull(result);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void toArraySizedRefusesAnnotatedType() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new @Nullable String[5]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedRefusesMultiDim() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new String[5][3]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedRefusesSideEffectingSize() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new String[mutate(x)]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedToZero() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var arr = list.toArray(new String[5]);";
		final var col = line.indexOf("list");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var arr = list.toArray(new String[0]);", result.replacement().getFirst());
	}

	@Test
	public void toArraySizedWithSizeExpression() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var arr = list.toArray(new String[list.size()]);";
		final var col = line.indexOf("list");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var arr = list.toArray(new String[0]);", result.replacement().getFirst());
	}
}