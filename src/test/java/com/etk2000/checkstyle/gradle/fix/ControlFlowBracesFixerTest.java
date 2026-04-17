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
	public void testBoundaryBareMethodTier1VsDottedMethodTier2() {
		// bare method - tier 1 (all one line)
		final var bareLine = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		final var bareResult = assertInstanceOf(FixResult.class, fixer.fix(bareLine, 0, 0));
		assertEquals(List.of("\tdo next(); while (x > 0);"), bareResult.replacement());
		assertEquals(0, bareResult.startLine());
		assertEquals(2, bareResult.endLine());
		assertTrue(bareResult.importsToAdd().isEmpty());

		// dotted method - tier 2 (while split)
		final var dottedLine = new ArrayList<>(List.of("\tdo {", "\t\tlist.add(x);", "\t} while (x > 0);"));
		final var dottedResult = assertInstanceOf(FixResult.class, fixer.fix(dottedLine, 0, 0));
		assertEquals(List.of("\tdo list.add(x);", "\twhile (x > 0);"), dottedResult.replacement());
		assertEquals(0, dottedResult.startLine());
		assertEquals(2, dottedResult.endLine());
		assertTrue(dottedResult.importsToAdd().isEmpty());
	}

	@Test
	public void testBoundarySimpleWhileTier1VsCompoundWhileTier2() {
		// simple while - tier 1
		final var simple = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		final var simpleResult = assertInstanceOf(FixResult.class, fixer.fix(simple, 0, 0));
		assertEquals(List.of("\tdo --x; while (x > 0);"), simpleResult.replacement());
		assertEquals(0, simpleResult.startLine());
		assertEquals(2, simpleResult.endLine());
		assertTrue(simpleResult.importsToAdd().isEmpty());

		// compound while - tier 2
		final var compound = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0 && x < 100);"));
		final var compoundResult = assertInstanceOf(FixResult.class, fixer.fix(compound, 0, 0));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), compoundResult.replacement());
		assertEquals(0, compoundResult.startLine());
		assertEquals(2, compoundResult.endLine());
		assertTrue(compoundResult.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedNoClosingBrace() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo {", "\t\t--x;")), 0, 0));
	}

	@Test
	public void testBracedTier1Assign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx = 5;", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x = 5; while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier1BareMethodCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo next(); while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier1CompoundAssign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx += 2;", "\t} while (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x += 2; while (x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier1Decrement() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBracedTier1Increment() {
		final var lines = new ArrayList<>(List.of("\t\tdo {", "\t\t\t++i;", "\t\t} while (i < len);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\tdo ++i; while (i < len);"), result.replacement());
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
	public void testDeepIndent() {
		final var lines = new ArrayList<>(List.of("\t\t\tdo {", "\t\t\t\t--x;", "\t\t\t} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t\tdo --x; while (x > 0);"), result.replacement());
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
	public void testNoIndent() {
		final var lines = new ArrayList<>(List.of("do {", "\t--x;", "} while (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("do --x; while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineEmptyBody() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo ")), 0, 0));
	}

	@Test
	public void testOnDoLineNoWhileLine() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo --x;")), 0, 0));
	}

	@Test
	public void testOnDoLineTier1JoinBareMethod() {
		final var lines = new ArrayList<>(List.of("\tdo next(x);", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo next(x); while (x > 0);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier1JoinCompoundAssign() {
		final var lines = new ArrayList<>(List.of("\tdo x += 5;", "\twhile (x < 100);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo x += 5; while (x < 100);"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOnDoLineTier1JoinWhile() {
		final var lines = new ArrayList<>(List.of("\tdo --x;", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
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
	public void testOnDoLineTier3AsTier1MoveToOwnLine() {
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
	public void testOwnLineTier1Join() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t--x;", "\twhile (x > 0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
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
}