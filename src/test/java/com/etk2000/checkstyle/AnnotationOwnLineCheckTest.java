package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnnotationOwnLineCheckTest {
	private static final String DIR = "annotationownline/";

	@Nullable
	private static DetailAST findFirst(@Nonnull DetailAST node, int tokenType) {
		if (node.getType() == tokenType)
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findFirst(child, tokenType);
			if (found != null)
				return found;
		}
		return null;
	}

	@Test
	public void testBetweenScanClampPreventsAioobeWhenDeclLineExceedsFileLength() throws Exception {
		final var source = "package com.example;\n@interface V { String[] value(); }\nclass T {\n\t@V({\n\t\t\"a\"\n\t})\n\tint field;\n}";
		final var tempFile = File.createTempFile("test", ".java");
		try {
			Files.writeString(tempFile.toPath(), source);
			final var fileContents = new FileContents(new FileText(tempFile, StandardCharsets.UTF_8.name()));
			final var variableDef = findFirst(JavaParser.parse(fileContents), TokenTypes.VARIABLE_DEF);
			assertNotNull(variableDef);
			final var modifiers = variableDef.findFirstToken(TokenTypes.MODIFIERS);
			assertNotNull(modifiers);
			final var type = variableDef.findFirstToken(TokenTypes.TYPE);
			assertNotNull(type);

			assertEquals(8, fileContents.getLines().length);
			((DetailAstImpl) type).setLineNo(fileContents.getLines().length + 5);

			final var check = new AnnotationOwnLineCheck();
			check.setFileContents(fileContents);
			assertDoesNotThrow(() -> check.visitToken(modifiers));
		}
		finally {
			tempFile.delete();
		}
	}

	@Test
	public void testBlankLineInsideMultiLineAnnotationViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputAnnotationOwnLineBlankInsideAnnotation.java"
		);
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("No blank line inside annotation 'V'.", violations.getFirst().getMessage());
	}

	@Test
	public void testInternalScanClampPreventsAioobeWhenAstLineExceedsFileLength() throws Exception {
		final var source = "package com.example;\n@interface V { String[] value(); }\nclass T {\n\t@V({\n\t\t\"a\"\n\t})\n\tint field;\n}";
		final var tempFile = File.createTempFile("test", ".java");
		try {
			Files.writeString(tempFile.toPath(), source);
			final var fileContents = new FileContents(new FileText(tempFile, StandardCharsets.UTF_8.name()));
			final var variableDef = findFirst(JavaParser.parse(fileContents), TokenTypes.VARIABLE_DEF);
			assertNotNull(variableDef);
			final var modifiers = variableDef.findFirstToken(TokenTypes.MODIFIERS);
			assertNotNull(modifiers);
			final var annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);
			assertNotNull(annotation);
			final var rparen = annotation.findFirstToken(TokenTypes.RPAREN);
			assertNotNull(rparen);

			assertEquals(8, fileContents.getLines().length);
			final var bogusLine = fileContents.getLines().length + 5;
			((DetailAstImpl) rparen).setLineNo(bogusLine);
			assertEquals(bogusLine, AstUtil.lastLine(annotation));

			final var check = new AnnotationOwnLineCheck();
			check.setFileContents(fileContents);
			assertDoesNotThrow(() -> check.visitToken(modifiers));
		}
		finally {
			tempFile.delete();
		}
	}

	@Test
	public void testMultiLineAnnotationAtEofBoundary() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputAnnotationOwnLineLastInFile.java"
		).isEmpty());
	}
}