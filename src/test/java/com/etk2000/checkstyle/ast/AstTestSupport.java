package com.etk2000.checkstyle.ast;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.JavaParser.Options;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Parsing and node-lookup helpers shared by the {@code Ast*Test} classes. */
final class AstTestSupport {
	private static final String FIXTURE = "astutil/InputAstUtil.java";
	private static final DetailAST SHARED_ROOT;
	private static final Throwable SHARED_ROOT_FAILURE;

	// The failure is stored rather than thrown, so class init always succeeds. Throwing here
	// would give the first class to touch this one the real cause and the other three a
	// causeless NoClassDefFoundError, with which one depends on execution order. It also keeps
	// AstDisplayTest, which never reads the fixture, from failing when the fixture is missing.
	static {
		DetailAST parsed = null;
		Throwable failure = null;
		try {
			parsed = parse(FIXTURE);
		}
		catch (Throwable e) {
			failure = e;
		}
		SHARED_ROOT = parsed;
		SHARED_ROOT_FAILURE = failure;
	}

	/**
	 * Asserts that {@code extract} answers {@code expected} for {@code source} parsed both with and
	 * without comment nodes. The {@code WITHOUT_COMMENTS} half pins the expectation to the
	 * behaviour that ships today, so a wrong literal cannot quietly encode the very drift the
	 * {@code WITH_COMMENTS} half exists to catch.
	 */
	static <T> void assertSameWithAndWithoutComments(@Nonnull String source, @Nullable T expected, @Nonnull Function<DetailAST, T> extract) throws Exception {
		assertEquals(expected, extract.apply(parseSource(source)), "WITHOUT_COMMENTS");
		assertEquals(expected, extract.apply(parseSourceWithComments(source)), "WITH_COMMENTS");
	}

	@Nullable
	static DetailAST findFirst(@Nonnull DetailAST node, int tokenType) {
		if (node.getType() == tokenType)
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findFirst(child, tokenType);
			if (found != null)
				return found;
		}
		return null;
	}

	@Nullable
	static DetailAST findMethod(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() == TokenTypes.METHOD_DEF) {
			// the null check is defensive only: probed interface, enum, annotation, ctor and
			// record shapes all give every METHOD_DEF an IDENT
			final var ident = node.findFirstToken(TokenTypes.IDENT);
			if (ident != null && name.equals(ident.getText()))
				return node;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findMethod(child, name);
			if (found != null)
				return found;
		}
		return null;
	}

	@Nonnull
	static DetailAST parse(@Nonnull String inputPath) throws Exception {
		final var url = AstTestSupport.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		requireNonNull(url, "Test input file not found: " + inputPath);
		return JavaParser.parseFile(new File(url.toURI()), Options.WITH_COMMENTS);
	}

	@Nonnull
	static DetailAST parseExprFirstChild(@Nonnull String source) throws Exception {
		final var ast = parseSource(source);
		final var assign = findFirst(ast, TokenTypes.ASSIGN);
		requireNonNull(assign, "No ASSIGN found");
		final var expr = assign.getFirstChild();
		requireNonNull(expr, "No child of ASSIGN");
		return expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
	}

	@Nonnull
	static DetailAST parseSource(@Nonnull String source) throws Exception {
		return parseSource(source, Options.WITHOUT_COMMENTS);
	}

	@Nonnull
	private static DetailAST parseSource(@Nonnull String source, @Nonnull Options options) throws Exception {
		final var sourceFile = File.createTempFile("test", ".java");
		try {
			Files.writeString(sourceFile.toPath(), source);
			return JavaParser.parseFileText(new FileText(sourceFile, StandardCharsets.UTF_8.name()), options);
		}
		finally {
			sourceFile.delete();
		}
	}

	/**
	 * The same parse as {@link #parseSource(String)} but with comment nodes woven in, which is what
	 * the shipped code does <em>not</em> do today. The comment-skip hardening in {@code AstText},
	 * {@code AstQuery} and {@code AstDisplay} is a no-op under {@code WITHOUT_COMMENTS} by
	 * construction, so only a tree parsed this way can prove it works.
	 */
	@Nonnull
	static DetailAST parseSourceWithComments(@Nonnull String source) throws Exception {
		return parseSource(source, Options.WITH_COMMENTS);
	}

	/**
	 * The parsed {@code astutil/InputAstUtil.java} fixture. Parsed once at class load rather than
	 * in a {@code @BeforeAll}, so the four test classes share one parse without inheriting a base.
	 * Callers treat the AST as read-only; nothing here copies it.
	 */
	@Nonnull
	static DetailAST root() {
		if (SHARED_ROOT_FAILURE != null)
			throw new IllegalStateException("could not parse the shared fixture " + FIXTURE, SHARED_ROOT_FAILURE);
		return SHARED_ROOT;
	}
}