package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowBracesFixerTest {
	private final CheckstyleFixer fixer = new ControlFlowBracesFixer();

	@Test
	public void testBoundaryBareMethodVsDottedMethod() {
		final var bareLine = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		final var bareResult = assertInstanceOf(FixResult.class, fixer.fix(bareLine, 0, 0));
		assertEquals(List.of("\tdo next();", "\twhile (x > 0);"), bareResult.replacement());
		assertEquals(0, bareResult.startLine());
		assertEquals(2, bareResult.endLine());
		assertTrue(bareResult.importsToAdd().isEmpty());

		final var dottedLine = new ArrayList<>(List.of("\tdo {", "\t\tlist.add(x);", "\t} while (x > 0);"));
		final var dottedResult = assertInstanceOf(FixResult.class, fixer.fix(dottedLine, 0, 0));
		assertEquals(List.of("\tdo list.add(x);", "\twhile (x > 0);"), dottedResult.replacement());
		assertEquals(0, dottedResult.startLine());
		assertEquals(2, dottedResult.endLine());
		assertTrue(dottedResult.importsToAdd().isEmpty());
	}

	@Test
	public void testBoundarySimpleWhileVsCompoundWhile() {
		final var simple = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		final var simpleResult = assertInstanceOf(FixResult.class, fixer.fix(simple, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), simpleResult.replacement());
		assertEquals(0, simpleResult.startLine());
		assertEquals(2, simpleResult.endLine());
		assertTrue(simpleResult.importsToAdd().isEmpty());

		final var compound = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0 && x < 100);"));
		final var compoundResult = assertInstanceOf(FixResult.class, fixer.fix(compound, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), compoundResult.replacement());
		assertEquals(0, compoundResult.startLine());
		assertEquals(2, compoundResult.endLine());
		assertTrue(compoundResult.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedBlankBodyLine() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t   ", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo", "\t\t;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedBlockCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t/* pending */", "\t} while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBlockCommentBodyWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t/* pending */",
				"\t}",
				"\twhile (x > 0);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBlockCommentWithEmptyStatement() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t/* placeholder */;", "\t} while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBlockCommentWithEmptyStatementWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t/* placeholder */;",
				"\t}",
				"\twhile (x > 0);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBodyContainingCharWithCommentDelimiters() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tarr[i] = '/';", "\t} while (cond);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		// tier-3 emit because SIMPLE_BODY_PATTERN doesn't match `arr[i] = '/'`
		assertEquals(
				List.of("\tdo", "\t\tarr[i] = '/';", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedBodyContainingStringWithCommentDelimiters() {
		// var-decl body needs braces for scope; string-aware stripBlockComments still
		// correctly preserves `"/*"` as string content, but the variable-declaration
		// guard refuses to remove the braces
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tString s = \"/*\";", "\t} while (cond);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBodyContainingStringWithLineCommentDelimiters() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\ts = \"// not a comment\";", "\t} while (cond);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		// tier-3 because the `/` in the string content disqualifies SIMPLE_BODY_PATTERN's
		// negated character class; the `//` inside the string is correctly preserved
		assertEquals(
				List.of("\tdo", "\t\ts = \"// not a comment\";", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedBodyVarDeclWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\tint x = 5;",
				"\t}",
				"\twhile (cond);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBodyWithLineCommentInsideBlock() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t/* contains // tricky */ x = 5;", "\t} while (cond);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t/* contains // tricky */ x = 5;", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedBodyWithLineCommentNoSemicolonRefused() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x // pending", "\t} while (cond);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBodyWithoutSemicolonRefused() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x", "\t} while (cond);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedBodyWithoutSemicolonWhileOnSeparateLineRefused() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x", "\t}", "\twhile (cond);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedCommentBracketedCode() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t/* pre */ x = 5; /* post */", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t/* pre */ x = 5; /* post */", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedCommentBracketedCodeWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t/* pre */ x = 5; /* post */",
				"\t}",
				"\twhile (x > 0);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t/* pre */ x = 5; /* post */", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedCommentOnlyBody() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t// pending", "\t} while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedCommentOnlyBodyWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t// pending",
				"\t}",
				"\twhile (x > 0);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedEmptyBody() {
		// empty body has no statement to keep; emitting `do\n\twhile(...)` would be invalid Java
		final var lines = new ArrayList<>(List.of("\tdo {", "\t} while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedEmptyBodyAcrossLines() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t}", "\twhile (cond);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedMixedCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t/* note */ // pending", "\t} while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedMixedCommentBodyWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t/* note */ // pending",
				"\t}",
				"\twhile (x > 0);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testBracedMultiLineBody() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\t} while (x > 0);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		final var expected = List.of(
				"\tdo",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\twhile (x > 0);"
		);
		assertEquals(expected, result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedMultiLineBodyWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\t}",
				"\twhile (x > 0);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		final var expected = List.of(
				"\tdo",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\twhile (x > 0);"
		);
		assertEquals(expected, result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedMultiLineBodyWithoutSemicolonOnLastLine() {
		// multi-line braced bodies bypass buildTierResult, so the no-`;` guard does not
		// apply. The last line lacks `;` here but the fixer emits it as-is. Pinning
		// current behavior; see docs/fixer-control-flow-edge-cases.md for known gaps.
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t--x;",
				"\t\t--y",
				"\t} while (cond);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x;", "\t\t--y", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedNoClosingBrace() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo {", "\t\t--x;")), 0, 0));
	}

	@Test
	public void testBracedSimpleAssign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx = 5;", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x = 5;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedSimpleBareMethodCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo next();", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedSimpleCompoundAssign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx += 2;", "\t} while (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x += 2;", "\twhile (x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedSimpleDecrement() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedSimpleIncrement() {
		final var lines = new ArrayList<>(List.of("\t\tdo {", "\t\t\t++i;", "\t\t} while (i < len);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\tdo ++i;", "\t\twhile (i < len);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedStatementWithLineCommentContainingBlockOpener() {
		// `// contains /* note` - // line comment with a /* inside; previously
		// stripBlockComments treated the inner /* as a real block-comment opener and
		// returned null, causing the fixer to refuse a valid body
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x; // contains /* note", "\t} while (cond);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x; // contains /* note", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedStatementWithTrailingComment() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x; // pending", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x; // pending", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedStatementWithTrailingCommentWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t--x; // pending",
				"\t}",
				"\twhile (x > 0);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x; // pending", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTabBeforeBrace() {
		final var lines = new ArrayList<>(List.of("\tdo\t{", "\t\t--x;", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier2CompoundWhile() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0 && x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier2DottedMethodCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tlist.add(item);", "\t} while (hasNext());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo list.add(item);", "\twhile (hasNext());"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier2SystemOut() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tSystem.out.println(x);", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier3ChainedCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tlist.stream().close();", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tlist.stream().close();", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier3ComplexRhs() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx += 5 * y;", "\t} while (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tx += 5 * y;", "\twhile (x < 100);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier3NewObject() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tnew Object();", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tnew Object();", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier3ThisChainedCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tthis.helper().chain();", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tthis.helper().chain();", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedWhileOnSeparateLine() {
		final var lines = new ArrayList<>(List.of(
				"\tdo {",
				"\t\t++x;",
				"\t}",
				"\twhile (x < 10);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo ++x;", "\twhile (x < 10);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBraceOnOwnLineElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\telse",
				"\t\t{",
				"\t\t\t++x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\telse", "\t\t\t++x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBraceOnOwnLineForCompactSkip() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tfor(int i = 0; i < 10; ++i)", "\t{")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testBraceOnOwnLineForSkip() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tfor (int i = 0; i < 10; ++i)", "\t{")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testBraceOnOwnLineIf() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0)",
				"\t\t{",
				"\t\t\t--x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tif (x > 0)", "\t\t\t--x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBraceOnOwnLineIfNoBody() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tif (x > 0)", "\t\t{")), 0, 0));
	}

	@Test
	public void testBraceOnOwnLineIfNoCloseBrace() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tif (x > 0)", "\t\t{", "\t\t\t--x;", "\t\t++y;")), 0, 0));
	}

	@Test
	public void testBraceOnOwnLineIfWithElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0)",
				"\t\t{",
				"\t\t\t--x;",
				"\t\t} else {"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tif (x > 0)", "\t\t\t--x;", "\t\telse {"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBraceOnOwnLineIfWithVarDecl() {
		final var finalVar = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\tfinal var x = 5;", "\t\t}"));
		assertNull(fixer.fix(finalVar, 0, 0));

		final var plainVar = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\tvar x = 5;", "\t\t}"));
		assertNull(fixer.fix(plainVar, 0, 0));

		final var typedDecl = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\tint x = 5;", "\t\t}"));
		assertNull(fixer.fix(typedDecl, 0, 0));

		final var qualifiedType = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\tMap.Entry<String, Integer> e = null;", "\t\t}"));
		assertNull(fixer.fix(qualifiedType, 0, 0));

		final var annotated = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\t@Nullable String s = null;", "\t\t}"));
		assertNull(fixer.fix(annotated, 0, 0));

		final var annotatedWithArgs = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\t@SuppressWarnings(\"unused\") int y = 0;", "\t\t}"));
		assertNull(fixer.fix(annotatedWithArgs, 0, 0));

		final var arrayDecl = new ArrayList<>(List.of("\t\tif (true)", "\t\t{", "\t\t\tint[] arr = new int[5];", "\t\t}"));
		assertNull(fixer.fix(arrayDecl, 0, 0));
	}

	@Test
	public void testBraceOnOwnLineWhile() {
		final var lines = new ArrayList<>(List.of(
				"\t\twhile (x > 0)",
				"\t\t{",
				"\t\t\t--x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\twhile (x > 0)", "\t\t\t--x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBraceOnOwnLineWithCommentOnBrace() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0)",
				"\t\t{ // important",
				"\t\t\t--x;",
				"\t\t}"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testDeepIndent() {
		final var lines = new ArrayList<>(List.of("\t\t\tdo {", "\t\t\t\t--x;", "\t\t\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t\tdo --x;", "\t\t\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixOwnLineDoAsLastLine() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo")), 0, 0));
	}

	@Test
	public void testLineIndexNegative() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo --x; while (x > 0);")), -1, 0));
	}

	@Test
	public void testLineIndexOutOfBounds() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo --x; while (x > 0);")), 5, 0));
	}

	@Test
	public void testMissingBracesElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\telse",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\telse {",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesFor() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\tif (i > 0)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\tfor (int i = 0; i < x; ++i) {",
				"\t\t\tif (i > 0)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesForEach() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfor (var item : list)",
				"\t\t\tif (item != null)",
				"\t\t\t\tSystem.out.println(item);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\tfor (var item : list) {",
				"\t\t\tif (item != null)",
				"\t\t\t\tSystem.out.println(item);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesIf() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif(x > 0)",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var compactResult = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, compactResult.startLine());
		assertEquals(2, compactResult.endLine());
		final var compactExpected = List.of(
				"\t\tif(x > 0) {",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(compactExpected, compactResult.replacement());
		assertTrue(compactResult.importsToAdd().isEmpty());

		final var spacedLines = new ArrayList<>(List.of(
				"\t\tif (x > 0)",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(spacedLines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\tif (x > 0) {",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesIfNoBodyEnd() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0)",
				"\t\t\tfor (int i = 0; i < x; ++i)"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testMissingBracesIfNoBodyLines() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tif (x > 0)")), 0, 0));
	}

	@Test
	public void testMissingBracesIfSingleLineBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\telse",
				"\t\t\t--x;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testMissingBracesIfWithBlockCommentContainingSlashSlash() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) /* contains // tricky */",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		// the // inside /* */ is correctly NOT treated as a trailing comment
		final var expected = List.of(
				"\t\tif (x > 0) /* contains // tricky */ {",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesIfWithTrailingBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) /* guard */",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		// no `//` anywhere, so `{` appended at end
		final var expected = List.of(
				"\t\tif (x > 0) /* guard */ {",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesIfWithTrailingBlockThenLineComment() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) /* note */ // guard",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		// findTrailingComment correctly skips past /* */ to find the real //
		final var expected = List.of(
				"\t\tif (x > 0) /* note */ { // guard",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesIfWithTrailingComment() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) // guard",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\tif (x > 0) { // guard",
				"\t\t\tfor (int i = 0; i < x; ++i)",
				"\t\t\t\tSystem.out.println(i);",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMissingBracesWhile() {
		final var lines = new ArrayList<>(List.of(
				"\t\twhile (x > 0)",
				"\t\t\tif (x > 5)",
				"\t\t\t\t--x;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		final var expected = List.of(
				"\t\twhile (x > 0) {",
				"\t\t\tif (x > 5)",
				"\t\t\t\t--x;",
				"\t\t}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoIndent() {
		final var lines = new ArrayList<>(List.of("do {", "\t--x;", "} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("do --x;", "while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineEmptyBody() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo ")), 0, 0));
	}

	@Test
	public void testOnDoLineEmptyBodyWithWhileBelow() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo ", "\twhile (x > 0);")), 0, 0));
	}

	@Test
	public void testOnDoLineFalseWhileInBody() {
		final var lines = new ArrayList<>(List.of("\tdo whileVar++; while (whileTimer > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo whileVar++;", "\twhile (whileTimer > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineMidLineUnterminatedBlockComment() {
		final var lines = new ArrayList<>(List.of("\tdo --x; /* TODO", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineNoWhileLine() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo --x;")), 0, 0));
	}

	@Test
	public void testOnDoLineSimpleBareMethodIdempotent() {
		final var lines = new ArrayList<>(List.of("\tdo next(x);", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo next(x);", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineSimpleCompoundAssignIdempotent() {
		final var lines = new ArrayList<>(List.of("\tdo x += 5;", "\twhile (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x += 5;", "\twhile (x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineSimpleDecrementIdempotent() {
		final var lines = new ArrayList<>(List.of("\tdo --x;", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier2CompoundWhileSplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo --x; while (x > 0 && x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier2SplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo list.add(x); while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo list.add(x);", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier2SystemOutSplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo System.out.println(x); while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier3AsTier2MoveToOwnLine() {
		final var lines = new ArrayList<>(List.of("\tdo list.subList(0, 1).clear();", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier3ComplexRhsAsTier2() {
		final var lines = new ArrayList<>(List.of("\tdo x += 5 * y;", "\twhile (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tx += 5 * y;", "\twhile (x < 100);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier3OneLinerMoveToOwnLine() {
		final var lines = new ArrayList<>(List.of("\tdo list.subList(0, 1).clear(); while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineUnterminatedBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\tdo /* multi",
				"\t       line */",
				"\t\tx = 5;",
				"\twhile (x > 0);"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileNextLineBlockCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo /* pending */", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileNextLineBlockCommentWithEmptyStatement() {
		final var lines = new ArrayList<>(List.of("\tdo /* placeholder */;", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileNextLineCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo // pending", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileNextLineCommentBracketedCode() {
		final var lines = new ArrayList<>(List.of("\tdo /* pre */ x = 5; /* post */", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t/* pre */ x = 5; /* post */", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineWhileNextLineMixedCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo /* note */ // pending", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileNextLineStatementWithTrailingBlockComment() {
		final var lines = new ArrayList<>(List.of("\tdo x++; /* note */", "\twhile (cond);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\tx++; /* note */", "\twhile (cond);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineWhileNextLineStatementWithTrailingComment() {
		final var lines = new ArrayList<>(List.of("\tdo --x; // pending", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x; // pending", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineWhileNextLineWithoutSemicolonRefused() {
		final var lines = new ArrayList<>(List.of("\tdo --x", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWhileSameLineBlockCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo /* pending */; while (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOnDoLineWithTab() {
		final var lines = new ArrayList<>(List.of("\tdo\t--x;", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineBlockCommentOnlyBody() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t/* pending */", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOwnLineBlockCommentWithEmptyStatement() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t/* placeholder */;", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOwnLineCommentBracketedCode() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t/* pre */ x = 5; /* post */", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t/* pre */ x = 5; /* post */", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineCommentOnlyBody() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t// pending", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOwnLineEmptyBody() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo", "", "\twhile (x > 0);")), 0, 0));
	}

	@Test
	public void testOwnLineMixedCommentBody() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t/* note */ // pending", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOwnLineMultiLineBodyAddsBraces() {
		final var lines = new ArrayList<>(List.of(
				"\tdo",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\twhile (x > 0);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		final var expected = List.of(
				"\tdo {",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\t} while (x > 0);"
		);
		assertEquals(expected, result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineNoWhile() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo", "\t\t--x;")), 0, 0));
	}

	@Test
	public void testOwnLineSimpleBodyMoveToDo() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t--x;", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineStatementWithTrailingComment() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t--x; // pending", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(
				List.of("\tdo", "\t\t--x; // pending", "\twhile (x > 0);"),
				result.replacement()
		);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineTier2MoveToDo() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\tSystem.out.println(x);", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineTier3BodyIdempotent() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOwnLineWhitespaceOnlyBody() {
		// asymmetric with testBracedBlankBodyLine: fixOwnLine refuses whitespace-only,
		// fixBracedBody synthesizes `;`. Both behaviors are intentional and pinned.
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo", "\t   ", "\twhile (x > 0);")), 0, 0));
	}

	@Test
	public void testOwnLineWithoutSemicolonRefused() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t--x", "\twhile (x > 0);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSkipBracedForLoop() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tfor (int i = 0; i < 10; ++i) {")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testSkipBracedForLoopCompact() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tfor(int i = 0; i < 10; ++i) {")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testSkipForLoop() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tfor (int i = 0; i < 10; ++i) System.out.println(i);")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testSkipIfStatement() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tif (x > 0) --x;")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testSkipNonControlFlow() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\tint done = 0;")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testSkipWhileLoop() {
		final var attempt = fixer.fix(new ArrayList<>(List.of("\twhile (x > 0) --x;")), 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testUnnecessaryBracesElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\telse {",
				"\t\t\t--x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\telse", "\t\t\t--x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testUnnecessaryBracesIf() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) {",
				"\t\t\t--x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tif (x > 0)", "\t\t\t--x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testUnnecessaryBracesIfWithElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) {",
				"\t\t\t--x;",
				"\t\t} else {",
				"\t\t\t++x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tif (x > 0)", "\t\t\t--x;", "\t\telse {"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testUnnecessaryBracesWhile() {
		final var lines = new ArrayList<>(List.of(
				"\t\twhile (x > 0) {",
				"\t\t\t--x;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\twhile (x > 0)", "\t\t\t--x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testUnnecessaryBracesWithNoBody() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tif (true) {")), 0, 0));
	}

	@Test
	public void testUnnecessaryBracesWithNoCloseBrace() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tif (true) {", "\t\t\t--x;")), 0, 0));
	}

	@Test
	public void testUnnecessaryBracesWithVarDeclReturnsNull() {
		final var finalVar = new ArrayList<>(List.of("\t\tif (true) {", "\t\t\tfinal var x = 5;", "\t\t}"));
		assertNull(fixer.fix(finalVar, 0, 0));

		final var typedDecl = new ArrayList<>(List.of("\t\tif (true) {", "\t\t\tint x = 5;", "\t\t}"));
		assertNull(fixer.fix(typedDecl, 0, 0));

		final var qualifiedType = new ArrayList<>(List.of("\t\tif (true) {", "\t\t\tMap.Entry<String, Integer> e = null;", "\t\t}"));
		assertNull(fixer.fix(qualifiedType, 0, 0));

		final var annotated = new ArrayList<>(List.of("\t\tif (true) {", "\t\t\t@Nullable String s = null;", "\t\t}"));
		assertNull(fixer.fix(annotated, 0, 0));

		final var arrayDecl = new ArrayList<>(List.of("\t\tif (true) {", "\t\t\tint[] arr = new int[5];", "\t\t}"));
		assertNull(fixer.fix(arrayDecl, 0, 0));
	}
}