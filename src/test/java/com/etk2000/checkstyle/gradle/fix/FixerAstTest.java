package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.etk2000.checkstyle.gradle.fix.FixerAst.AstFunction;
import com.etk2000.checkstyle.gradle.fix.FixerAst.ThrowingParser;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

/**
 * The firewall exists so every fixer degrades to "no AST" instead of aborting
 * the fix pass. Only the {@code CheckstyleException} arm is reachable from a
 * real buffer, so the rest are driven through the injected-parser overload.
 */
public class FixerAstTest {
	private static final List<String> UNPARSEABLE = List.of("+");
	private static final List<String> VALID = List.of("class T {", "\tint x;", "}");

	/** Name of the single field declared by {@link #VALID}. */
	@Nonnull
	private static String fieldName(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.VARIABLE_DEF)
				return node.findFirstToken(TokenTypes.IDENT).getText();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no field in the parsed buffer");
	}

	/**
	 * The cache is per-thread and outlives a test, so every case starts cold and
	 * leaves none behind: the injected-parser overload writes its result into the
	 * same cache the production one-arg path reads.
	 */
	@AfterEach
	@BeforeEach
	public void clearCache() {
		FixerAst.clearCache();
	}

	@Test
	public void testAssertionErrorYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new AssertionError("grammar assertion");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	/**
	 * The cache is keyed on buffer content, so an in-place edit (which is how the
	 * pipeline applies a fix) must not be served the pre-edit AST.
	 */
	@Test
	public void testCachedRootNotReusedAfterInPlaceEdit() {
		final var lines = new ArrayList<>(VALID);
		final var first = FixerAst.parseOrNull(lines);
		assertNotNull(first);
		lines.set(1, "\tint y;");
		final var second = FixerAst.parseOrNull(lines);
		assertNotNull(second);
		assertNotSame(first, second);
		assertEquals("y", fieldName(second));
		assertEquals("x", fieldName(first));
	}

	@Test
	public void testCachedRootNotReusedForDifferentParser() {
		final var lines = new ArrayList<>(VALID);
		assertNotNull(FixerAst.parseOrNull(lines));
		final ThrowingParser failing = ignored -> {
			throw new CheckstyleException("injected");
		};
		assertNull(FixerAst.parseOrNull(lines, failing), "an injected parser must not be served the default parser's AST");
		assertNotNull(FixerAst.parseOrNull(lines), "the injected parser's failure must not poison the production path");
	}

	@Test
	public void testCachedRootReusedForUnchangedBuffer() {
		final var lines = new ArrayList<>(VALID);
		final var first = FixerAst.parseOrNull(lines);
		assertNotNull(first);
		assertSame(first, FixerAst.parseOrNull(lines));
	}

	@Test
	public void testCacheIsThreadConfined() throws Exception {
		final var mine = FixerAst.parseOrNull(VALID);
		assertNotNull(mine);
		final var theirs = new AtomicReference<>();
		final var thread = new Thread(() -> theirs.set(FixerAst.parseOrNull(VALID)));
		thread.start();
		thread.join();
		assertNotNull(theirs.get());
		assertNotSame(mine, theirs.get());
	}

	@Test
	public void testCheckstyleExceptionYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new CheckstyleException("unparseable");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	/**
	 * A re-parse the pipeline pays for on every retried violation is exactly what
	 * the cache exists to avoid, so a failure has to be cached like a success.
	 */
	@Test
	public void testClearCacheForcesReparse() {
		final var first = FixerAst.parseOrNull(VALID);
		assertNotNull(first);
		FixerAst.clearCache();
		assertNotSame(first, FixerAst.parseOrNull(VALID));
	}

	@Test
	public void testFailedParseIsCached() {
		final var calls = new AtomicInteger();
		final ThrowingParser counting = lines -> {
			calls.incrementAndGet();
			throw new CheckstyleException("unparseable");
		};
		assertNull(FixerAst.parseOrNull(List.copyOf(UNPARSEABLE), counting));
		assertNull(FixerAst.parseOrNull(List.copyOf(UNPARSEABLE), counting));
		assertEquals(1, calls.get());
	}

	@Test
	public void testRuntimeExceptionYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new IllegalStateException("recognition failure");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testStackOverflowErrorYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new StackOverflowError();
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testUnparseableBufferYieldsNull() {
		assertNull(FixerAst.parseOrNull(UNPARSEABLE));
	}

	@Test
	public void testWithAstCatchesActionAssertionError() {
		final AstFunction<String> action = root -> {
			throw new AssertionError("classifier assertion");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionCheckstyleException() {
		final AstFunction<String> action = root -> {
			throw new CheckstyleException("classifier rejected the tree");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionFailure() {
		final AstFunction<String> action = root -> {
			throw new IllegalStateException("classifier blew up");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionStackOverflowError() {
		final AstFunction<String> action = root -> {
			throw new StackOverflowError();
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	/**
	 * The dominant production outcome: the buffer parsed and the classifier simply
	 * found nothing at the reported position.
	 */
	@Test
	public void testWithAstNullActionResult() {
		final var ran = new AtomicInteger();
		final AstFunction<String> action = root -> {
			ran.incrementAndGet();
			return null;
		};
		assertNull(FixerAst.withAst(VALID, action));
		assertEquals(1, ran.get());
	}

	@Test
	public void testWithAstReturnsActionResult() {
		assertEquals("ok", FixerAst.withAst(VALID, root -> "ok"));
	}

	@Test
	public void testWithAstSkipsActionWhenUnparseable() {
		assertNull(FixerAst.withAst(UNPARSEABLE, root -> "should not run"));
	}
}