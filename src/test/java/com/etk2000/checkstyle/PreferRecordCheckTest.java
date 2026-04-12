package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferRecordCheckTest {
	private static final String DIR = "preferrecord/";
	private static final String MSG = "should be a record (all instance fields are final with no inline initializers).";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferRecordCheck.class, DIR + "InputPreferRecordClean.java").isEmpty());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferRecordCheck.class, DIR + "InputPreferRecordViolation.java");
		assertEquals(26, violations.size());

		assertEquals(10, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Class 'InputPreferRecordViolation' " + MSG, violations.get(0).getMessage());

		assertEquals(20, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Class 'ArrayField' " + MSG, violations.get(1).getMessage());

		assertEquals(28, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Class 'AnnotatedArrayField' " + MSG, violations.get(2).getMessage());

		assertEquals(37, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Class 'AnnotatedFields' " + MSG, violations.get(3).getMessage());

		assertEquals(48, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Class 'AnnotatedFieldOnly' " + MSG, violations.get(4).getMessage());

		assertEquals(57, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Class 'AnnotatedParamOnly' " + MSG, violations.get(5).getMessage());

		assertEquals(65, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Class 'ConstructorLocalAssignment' " + MSG, violations.get(6).getMessage());

		assertEquals(74, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals("Class 'ConstructorBareAssignment' " + MSG, violations.get(7).getMessage());

		assertEquals(83, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals("Class 'EmptyConstructor' " + MSG, violations.get(8).getMessage());

		assertEquals(91, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Class 'GenericAnnotatedTypeArg' " + MSG, violations.get(9).getMessage());

		assertEquals(99, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Class 'GenericClassAnnotated' " + MSG, violations.get(10).getMessage());

		assertEquals(108, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals("Class 'GenericClassConcrete' " + MSG, violations.get(11).getMessage());

		assertEquals(116, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals("Class 'GenericClass' " + MSG, violations.get(12).getMessage());

		assertEquals(124, violations.get(13).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(13).getSeverityLevel());
		assertEquals("Class 'HasStaticAndInstanceFields' " + MSG, violations.get(13).getMessage());

		assertEquals(133, violations.get(14).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(14).getSeverityLevel());
		assertEquals("Class 'HasSideEffects' " + MSG, violations.get(14).getMessage());

		assertEquals(142, violations.get(15).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(15).getSeverityLevel());
		assertEquals("Class 'HasValidation' " + MSG, violations.get(15).getMessage());

		assertEquals(152, violations.get(16).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(16).getSeverityLevel());
		assertEquals("Class 'MethodsPresent' " + MSG, violations.get(16).getMessage());

		assertEquals(164, violations.get(17).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(17).getSeverityLevel());
		assertEquals("Class 'MultipleConstructors' " + MSG, violations.get(17).getMessage());

		assertEquals(177, violations.get(18).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(18).getSeverityLevel());
		assertEquals("Class 'MultipleFieldTypes' " + MSG, violations.get(18).getMessage());

		assertEquals(187, violations.get(19).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(19).getSeverityLevel());
		assertEquals("Class 'NonOverrideEquals' " + MSG, violations.get(19).getMessage());

		assertEquals(199, violations.get(20).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(20).getSeverityLevel());
		assertEquals("Class 'OverrideNonRecordMethod' " + MSG, violations.get(20).getMessage());

		assertEquals(212, violations.get(21).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(21).getSeverityLevel());
		assertEquals("Class 'SameLineFields' " + MSG, violations.get(21).getMessage());

		assertEquals(221, violations.get(22).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(22).getSeverityLevel());
		assertEquals("Class 'SingleField' " + MSG, violations.get(22).getMessage());

		assertEquals(229, violations.get(23).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(23).getSeverityLevel());
		assertEquals("Class 'WithGenericImplements' " + MSG, violations.get(23).getMessage());

		assertEquals(242, violations.get(24).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(24).getSeverityLevel());
		assertEquals("Class 'WithImplements' " + MSG, violations.get(24).getMessage());

		assertEquals(250, violations.get(25).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(25).getSeverityLevel());
		assertEquals("Class 'WithMultipleImplements' " + MSG, violations.get(25).getMessage());
	}
}