package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.TreeSet;

public class DocCommentsTest {
	@Test
	public void testDeclarationStarts() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated int alpha;\n\tvoid m() {\n\t\tint local = 1;\n\t}\n}");
		assertEquals(
				Set.of(DocComments.position(1, 0), DocComments.position(2, 1), DocComments.position(3, 1), DocComments.position(4, 2)),
				DocComments.declarationStarts(ast)
		);
	}

	@Test
	public void testDocumentedTokenPastTheLastToken() throws Exception {
		final var tokens = DocComments.tokenPositions(parseSource("class T {\n}"));
		final var docs = new DocComments(tokens, new TreeSet<>(Set.of(DocComments.position(3, 0))));
		assertNull(docs.documentedToken(DocComments.position(3, 0)));
		assertEquals(DocComments.position(1, 0), docs.documentedToken(DocComments.position(0, 0)));
	}

	@CsvSource(delimiter = '|', value = {
			"/** d */|true",
			"/***/|true",
			"/**|true",
			"/// d|true",
			"///|true",
			"//// d|true",
			"/**/|false",
			"/* d */|false",
			"// d|false",
			"/|false"
	})
	@ParameterizedTest
	void testIsDocComment(String text, boolean expected) {
		assertEquals(expected, DocComments.isDocComment(text));
	}

	@Test
	public void testIsDocumentedFirstDeclarationOfTheFile() throws Exception {
		final var documented = parseSource("/** d */ class T {\n}");
		final var docs = new DocComments(DocComments.tokenPositions(documented), new TreeSet<>(Set.of(DocComments.position(1, 0))));
		assertTrue(docs.isDocumented(findFirst(documented, TokenTypes.CLASS_DEF)));
		final var plain = parseSource("class T {\n}");
		assertFalse(new DocComments(DocComments.tokenPositions(plain), new TreeSet<>()).isDocumented(findFirst(plain, TokenTypes.CLASS_DEF)));
	}

	@Test
	public void testIsDocumentedOnlyByTheCommentAheadOfTheFirstToken() throws Exception {
		final var ast = parseSource("class T {\n\tprivate int alpha;\n\tprivate int beta;\n}");
		final var alpha = findFirst(ast, TokenTypes.VARIABLE_DEF);
		final var beta = alpha.getNextSibling();
		final var tokens = DocComments.tokenPositions(ast);
		final var afterFirstToken = new DocComments(tokens, new TreeSet<>(Set.of(DocComments.position(2, 8))));
		assertFalse(afterFirstToken.isDocumented(alpha));
		final var aheadOfFirstToken = new DocComments(tokens, new TreeSet<>(Set.of(DocComments.position(1, 10))));
		assertTrue(aheadOfFirstToken.isDocumented(alpha));
		assertFalse(aheadOfFirstToken.isDocumented(beta));
	}

	@Test
	public void testPositionSortsBySourceOrder() {
		final var positions = new TreeSet<>(Set.of(DocComments.position(2, 1), DocComments.position(2, 0), DocComments.position(1, Integer.MAX_VALUE)));
		assertEquals(DocComments.position(1, Integer.MAX_VALUE), positions.first());
		assertEquals(DocComments.position(2, 1), positions.last());
	}

	@Test
	public void testTokenPositionsSkipImaginaryNodes() throws Exception {
		assertEquals(
				Set.of(DocComments.position(1, 0), DocComments.position(1, 6), DocComments.position(1, 8), DocComments.position(1, 9)),
				DocComments.tokenPositions(parseSource("class T {}"))
		);
	}
}