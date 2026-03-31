package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AstUtilTest {
	private static DetailAST root;

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

	@Nullable
	private static DetailAST findMethod(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() == TokenTypes.METHOD_DEF) {
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
	private static DetailAST parse(@Nonnull String inputPath) throws Exception {
		final var url = AstUtilTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		Objects.requireNonNull(url, "Test input file not found: " + inputPath);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITH_COMMENTS);
	}

	@BeforeClass
	public static void setUp() throws Exception {
		root = parse("astutil/InputAstUtil.java");
	}

	@Test
	public void testAnnotationNameQualified() {
		// @javax.annotation.CheckReturnValue on the class
		final var classAnnotation = findFirst(root, TokenTypes.ANNOTATION);
		// first annotation is the qualified one on the class
		assertEquals("CheckReturnValue", AstUtil.annotationName(classAnnotation));
	}

	@Test
	public void testAnnotationNameSimple() {
		// @Nonnull on the field — find it inside the class body
		final var objBlock = findFirst(root, TokenTypes.OBJBLOCK);
		final var fieldAnnotation = findFirst(objBlock, TokenTypes.ANNOTATION);
		assertEquals("Nonnull", AstUtil.annotationName(fieldAnnotation));
	}

	@Test
	public void testContainsCastToFalse() {
		final var method = findMethod(root, "emptyBlock");
		assertFalse(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToTrue() {
		final var method = findMethod(root, "castAndResolve");
		assertTrue(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testExprText() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		// first statement is the variable def: String s = (String) obj;
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		final var expr = assign.getFirstChild();
		// the expression is the typecast (String) obj
		assertEquals("String)obj", AstUtil.exprText(expr));
	}

	@Test
	public void testExprTextSimpleIdent() {
		final var method = findMethod(root, "castAndResolve");
		final var params = method.findFirstToken(TokenTypes.PARAMETERS);
		final var param = params.findFirstToken(TokenTypes.PARAMETER_DEF);
		final var ident = param.findFirstToken(TokenTypes.IDENT);
		assertEquals("obj", AstUtil.exprText(ident));
	}

	@Test
	public void testIsEmptyBodyBlock() {
		final var method = findMethod(root, "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertTrue(AstUtil.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyNonEmpty() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertFalse(AstUtil.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyStatement() {
		final var method = findMethod(root, "emptyStatement");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var ifNode = slist.findFirstToken(TokenTypes.LITERAL_IF);
		// the body of if (flag); is an EMPTY_STAT
		final var rparen = ifNode.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen.getNextSibling();
		assertTrue(AstUtil.isEmptyBody(body));
	}

	@Test
	public void testLastLineMultiLine() {
		final var method = findMethod(root, "multiLine");
		// method spans multiple lines (signature + body)
		assertTrue(AstUtil.lastLine(method) > method.getLineNo());
	}

	@Test
	public void testLastLineSingleLine() {
		final var method = findMethod(root, "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		// the RCURLY is on the next line, so lastLine > slist line
		final var rcurly = slist.findFirstToken(TokenTypes.RCURLY);
		assertEquals(rcurly.getLineNo(), AstUtil.lastLine(slist));
	}

	@Test
	public void testResolveVariableTypeLocalVariable() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		// find the method call System.out.println(s) — use `s` from within it
		final var exprNode = slist.getFirstChild().getNextSibling();
		assertEquals("String", AstUtil.resolveVariableType(exprNode, "s"));
	}

	@Test
	public void testResolveVariableTypeParameter() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Object", AstUtil.resolveVariableType(slist, "obj"));
	}

	@Test
	public void testResolveVariableTypeUnknown() {
		final var method = findMethod(root, "castAndResolve");
		assertNull(AstUtil.resolveVariableType(method, "nonexistent"));
	}

	@Test
	public void testResolveVariableTypeVar() {
		final var method = findMethod(root, "varLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		// var x = "hello" — should return null since var is not a real type
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testTypeTextQualified() {
		// java.util.List qualifiedField; — has a DOT in the type
		final var objBlock = findFirst(root, TokenTypes.OBJBLOCK);
		// find the second VARIABLE_DEF (qualifiedField)
		var varDef = objBlock.findFirstToken(TokenTypes.VARIABLE_DEF);
		varDef = varDef.getNextSibling();
		while (varDef != null && varDef.getType() != TokenTypes.VARIABLE_DEF)
			varDef = varDef.getNextSibling();
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("javautilList", AstUtil.typeText(type));
	}

	@Test
	public void testTypeTextSimple() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("String", AstUtil.typeText(type));
	}
}