package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowBracesFixerTest {
	private final CheckstyleFixer fixer = new ControlFlowBracesFixer();

	@Test
	public void testBoundaryBareMethodTier1VsDottedMethodTier2() {
		// bare method - tier 1 (all one line)
		final var bareLine = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		assertEquals(List.of("\tdo next(); while (x > 0);"), fixer.fix(bareLine, 0, 0).replacement());

		// dotted method - tier 2 (while split)
		final var dottedLine = new ArrayList<>(List.of("\tdo {", "\t\tlist.add(x);", "\t} while (x > 0);"));
		assertEquals(List.of("\tdo list.add(x);", "\twhile (x > 0);"), fixer.fix(dottedLine, 0, 0).replacement());
	}

	@Test
	public void testBoundarySimpleWhileTier1VsCompoundWhileTier2() {
		// simple while - tier 1
		final var simple = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		assertEquals(List.of("\tdo --x; while (x > 0);"), fixer.fix(simple, 0, 0).replacement());

		// compound while - tier 2
		final var compound = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0 && x < 100);"));
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), fixer.fix(compound, 0, 0).replacement());
	}

	@Test
	public void testBracedNoClosingBrace() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo {", "\t\t--x;")), 0, 0));
	}

	@Test
	public void testBracedTier1Assign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx = 5;", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo x = 5; while (x > 0);"), result.replacement());
	}

	@Test
	public void testBracedTier1BareMethodCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tnext();", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo next(); while (x > 0);"), result.replacement());
	}

	@Test
	public void testBracedTier1CompoundAssign() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx += 2;", "\t} while (x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo x += 2; while (x < 100);"), result.replacement());
	}

	@Test
	public void testBracedTier1Decrement() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
	}

	@Test
	public void testBracedTier1Increment() {
		final var lines = new ArrayList<>(List.of("\t\tdo {", "\t\t\t++i;", "\t\t} while (i < len);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\tdo ++i; while (i < len);"), result.replacement());
	}

	@Test
	public void testBracedTier2CompoundWhile() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\t--x;", "\t} while (x > 0 && x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), result.replacement());
	}

	@Test
	public void testBracedTier2DottedMethodCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tlist.add(item);", "\t} while (hasNext());"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo list.add(item);", "\twhile (hasNext());"), result.replacement());
	}

	@Test
	public void testBracedTier2SystemOut() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tSystem.out.println(x);", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
	}

	@Test
	public void testBracedTier3ChainedCall() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tlist.stream().close();", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tlist.stream().close();", "\twhile (x > 0);"),
				result.replacement()
		);
	}

	@Test
	public void testBracedTier3ComplexRhs() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tx += 5 * y;", "\t} while (x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tx += 5 * y;", "\twhile (x < 100);"),
				result.replacement()
		);
	}

	@Test
	public void testBracedTier3NewObject() {
		final var lines = new ArrayList<>(List.of("\tdo {", "\t\tnew Object();", "\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tnew Object();", "\twhile (x > 0);"),
				result.replacement()
		);
	}

	@Test
	public void testDeepIndent() {
		final var lines = new ArrayList<>(List.of("\t\t\tdo {", "\t\t\t\t--x;", "\t\t\t} while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t\tdo --x; while (x > 0);"), result.replacement());
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("do --x; while (x > 0);"), result.replacement());
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo next(x); while (x > 0);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier1JoinCompoundAssign() {
		final var lines = new ArrayList<>(List.of("\tdo x += 5;", "\twhile (x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo x += 5; while (x < 100);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier1JoinWhile() {
		final var lines = new ArrayList<>(List.of("\tdo --x;", "\twhile (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier2CompoundWhileSplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo --x; while (x > 0 && x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo --x;", "\twhile (x > 0 && x < 100);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier2SplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo list.add(x); while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo list.add(x);", "\twhile (x > 0);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier2SystemOutSplitWhile() {
		final var lines = new ArrayList<>(List.of("\tdo System.out.println(x); while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
	}

	@Test
	public void testOnDoLineTier3AsTier1MoveToOwnLine() {
		final var lines = new ArrayList<>(List.of("\tdo list.subList(0, 1).clear(); while (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"),
				result.replacement()
		);
	}

	@Test
	public void testOnDoLineTier3AsTier2MoveToOwnLine() {
		final var lines = new ArrayList<>(List.of("\tdo list.subList(0, 1).clear();", "\twhile (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tlist.subList(0, 1).clear();", "\twhile (x > 0);"),
				result.replacement()
		);
	}

	@Test
	public void testOnDoLineTier3ComplexRhsAsTier2() {
		final var lines = new ArrayList<>(List.of("\tdo x += 5 * y;", "\twhile (x < 100);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(
				List.of("\tdo", "\t\tx += 5 * y;", "\twhile (x < 100);"),
				result.replacement()
		);
	}

	@Test
	public void testOwnLineMultiLineBodyAddsBraces() {
		final var lines = new ArrayList<>(List.of(
				"\tdo",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\twhile (x > 0);"
		));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		final var expected = List.of(
				"\tdo {",
				"\t\tif (x > 0)",
				"\t\t\t--x;",
				"\t} while (x > 0);"
		);
		assertEquals(expected, result.replacement());
	}

	@Test
	public void testOwnLineNoWhile() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tdo", "\t\t--x;")), 0, 0));
	}

	@Test
	public void testOwnLineTier1Join() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\t--x;", "\twhile (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo --x; while (x > 0);"), result.replacement());
	}

	@Test
	public void testOwnLineTier2MoveToDo() {
		final var lines = new ArrayList<>(List.of("\tdo", "\t\tSystem.out.println(x);", "\twhile (x > 0);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\tdo System.out.println(x);", "\twhile (x > 0);"), result.replacement());
	}

	@Test
	public void testSkipForLoop() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tfor (int i = 0; i < 10; ++i) System.out.println(i);")), 0, 0));
	}

	@Test
	public void testSkipIfStatement() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tif (x > 0) --x;")), 0, 0));
	}

	@Test
	public void testSkipNonControlFlow() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tint done = 0;")), 0, 0));
	}

	@Test
	public void testSkipWhileLoop() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\twhile (x > 0) --x;")), 0, 0));
	}
}