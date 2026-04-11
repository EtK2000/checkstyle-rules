package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class LambdaParameterTypeCheckTest {
	private static final String ANNOTATION_OWN_DIR = "annotationownline/";
	private static final String ANNOTATION_SAME_DIR = "annotationsameline/";
	private static final String DIR = "lambdaparam/";
	private static final String PREFER_VAR_DIR = "prefervar/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				DIR + "InputLambdaParamClean.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckAnnotationOwnLineOnLambdaParamFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputLambdaParamClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputLambdaParamViolationImplicit.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputLambdaParamViolationVar.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationOwnLineCheck.class,
				DIR + "InputLambdaParamViolationParens.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckAnnotationSameLineOnLambdaParamFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				AnnotationSameLineCheck.class,
				DIR + "InputLambdaParamClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationSameLineCheck.class,
				DIR + "InputLambdaParamViolationImplicit.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationSameLineCheck.class,
				DIR + "InputLambdaParamViolationVar.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				AnnotationSameLineCheck.class,
				DIR + "InputLambdaParamViolationParens.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckLambdaParamOnAnnotationOwnLineFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLineClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLineBlankViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLineOrderViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLinePackageClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLinePackageViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_OWN_DIR + "InputAnnotationOwnLineViolation.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckLambdaParamOnAnnotationSameLineFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_SAME_DIR + "InputAnnotationSameLineClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_SAME_DIR + "InputAnnotationSameLineOrderViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				ANNOTATION_SAME_DIR + "InputAnnotationSameLineViolation.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckLambdaParamOnPreferVarFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarAllowedMethodClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarAllowedMethodViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarChainClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarChainViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarGenericReturnClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarGenericReturnViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarLiteralMismatchViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarMultiVarViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarReflectionClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarReflectionViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				PREFER_VAR_DIR + "InputPreferVarViolation.java"
		).isEmpty());
	}

	@Test
	public void testCrossCheckPreferVarOnLambdaParamFiles() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputLambdaParamClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputLambdaParamViolationImplicit.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputLambdaParamViolationVar.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputLambdaParamViolationParens.java"
		).isEmpty());
	}

	@Test
	public void testViolationImplicit() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				DIR + "InputLambdaParamViolationImplicit.java"
		);
		assertEquals(8, violations.size());
		var i = 0;

		// explicitArrayType: (String[] x) ->
		assertEquals(8, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'String'.", violations.get(i++).getMessage());

		// explicitMultipleTypes: (String x, String y) -> (2 violations)
		assertEquals(12, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'String'.", violations.get(i++).getMessage());

		assertEquals(12, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'String'.", violations.get(i++).getMessage());

		// explicitPrimitiveType: (int x) ->
		assertEquals(16, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'int'.", violations.get(i++).getMessage());

		// explicitSingleType: (String x) ->
		assertEquals(20, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'String'.", violations.get(i++).getMessage());

		// varMultipleParams: (var x, var y) -> (2 violations)
		assertEquals(24, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'var'.", violations.get(i++).getMessage());

		assertEquals(24, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'var'.", violations.get(i++).getMessage());

		// varSingleParam: (var x) ->
		assertEquals(28, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter should use implicit type instead of 'var'.", violations.get(i++).getMessage());
	}

	@Test
	public void testViolationParens() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				DIR + "InputLambdaParamViolationParens.java"
		);
		assertEquals(2, violations.size());
		var i = 0;

		// bracedBody: (x) -> { ... }
		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove unnecessary parentheses around single lambda parameter.", violations.get(i++).getMessage());

		// expressionBody: (x) -> ...
		assertEquals(13, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove unnecessary parentheses around single lambda parameter.", violations.get(i++).getMessage());
	}

	@Test
	public void testViolationVar() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				LambdaParameterTypeCheck.class,
				DIR + "InputLambdaParamViolationVar.java"
		);
		assertEquals(8, violations.size());
		var i = 0;

		// annotatedBothParams: (@C String x, @D String y) -> (2 violations)
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		// annotatedExplicitMultiParam: (@C String x, String y) -> (2 violations)
		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		// annotatedExplicitSingle: (@C String x) ->
		assertEquals(18, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		// annotatedMultiAnnotation: (@C @D String x) ->
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		// annotatedSecondParam: (String x, @C String y) -> (2 violations)
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());

		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Lambda parameter with annotation should use 'var' instead of 'String'.", violations.get(i++).getMessage());
	}
}