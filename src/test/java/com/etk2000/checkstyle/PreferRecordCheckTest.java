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
		assertEquals(24, violations.size());

		assertEquals(9, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Class 'InputPreferRecordViolation' " + MSG, violations.get(0).getMessage());

		assertEquals(19, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Class 'ArrayField' " + MSG, violations.get(1).getMessage());

		assertEquals(27, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Class 'AnnotatedArrayField' " + MSG, violations.get(2).getMessage());

		assertEquals(36, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Class 'AnnotatedFields' " + MSG, violations.get(3).getMessage());

		assertEquals(47, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Class 'AnnotatedFieldOnly' " + MSG, violations.get(4).getMessage());

		assertEquals(56, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Class 'AnnotatedParamOnly' " + MSG, violations.get(5).getMessage());

		assertEquals(64, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Class 'ConstructorLocalAssignment' " + MSG, violations.get(6).getMessage());

		assertEquals(73, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals("Class 'ConstructorBareAssignment' " + MSG, violations.get(7).getMessage());

		assertEquals(82, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals("Class 'EmptyConstructor' " + MSG, violations.get(8).getMessage());

		assertEquals(90, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Class 'GenericAnnotatedTypeArg' " + MSG, violations.get(9).getMessage());

		assertEquals(98, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Class 'GenericClassAnnotated' " + MSG, violations.get(10).getMessage());

		assertEquals(107, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals("Class 'GenericClassConcrete' " + MSG, violations.get(11).getMessage());

		assertEquals(115, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals("Class 'GenericClass' " + MSG, violations.get(12).getMessage());

		assertEquals(123, violations.get(13).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(13).getSeverityLevel());
		assertEquals("Class 'HasStaticAndInstanceFields' " + MSG, violations.get(13).getMessage());

		assertEquals(132, violations.get(14).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(14).getSeverityLevel());
		assertEquals("Class 'HasSideEffects' " + MSG, violations.get(14).getMessage());

		assertEquals(141, violations.get(15).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(15).getSeverityLevel());
		assertEquals("Class 'HasValidation' " + MSG, violations.get(15).getMessage());

		assertEquals(151, violations.get(16).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(16).getSeverityLevel());
		assertEquals("Class 'MethodsPresent' " + MSG, violations.get(16).getMessage());

		assertEquals(163, violations.get(17).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(17).getSeverityLevel());
		assertEquals("Class 'MultipleConstructors' " + MSG, violations.get(17).getMessage());

		assertEquals(176, violations.get(18).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(18).getSeverityLevel());
		assertEquals("Class 'MultipleFieldTypes' " + MSG, violations.get(18).getMessage());

		assertEquals(186, violations.get(19).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(19).getSeverityLevel());
		assertEquals("Class 'NonOverrideEquals' " + MSG, violations.get(19).getMessage());

		assertEquals(198, violations.get(20).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(20).getSeverityLevel());
		assertEquals("Class 'OverrideNonRecordMethod' " + MSG, violations.get(20).getMessage());

		assertEquals(211, violations.get(21).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(21).getSeverityLevel());
		assertEquals("Class 'SameLineFields' " + MSG, violations.get(21).getMessage());

		assertEquals(220, violations.get(22).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(22).getSeverityLevel());
		assertEquals("Class 'SingleField' " + MSG, violations.get(22).getMessage());

		assertEquals(228, violations.get(23).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(23).getSeverityLevel());
		assertEquals("Class 'WithImplements' " + MSG, violations.get(23).getMessage());
	}
}