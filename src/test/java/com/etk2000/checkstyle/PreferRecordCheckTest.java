package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertEquals(18, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals("Class 'InputPreferRecordViolation' " + MSG, violations.get(0).getMessage());
		assertEquals(15, violations.get(1).getLine());
		assertEquals("Class 'AnnotatedFields' " + MSG, violations.get(1).getMessage());
		assertEquals(27, violations.get(2).getLine());
		assertEquals("Class 'ConstructorLocalAssignment' " + MSG, violations.get(2).getMessage());
		assertEquals(37, violations.get(3).getLine());
		assertEquals("Class 'ConstructorBareAssignment' " + MSG, violations.get(3).getMessage());
		assertEquals(47, violations.get(4).getLine());
		assertEquals("Class 'ConstructorOtherAssignment' " + MSG, violations.get(4).getMessage());
		assertEquals(56, violations.get(5).getLine());
		assertEquals("Class 'EmptyConstructor' " + MSG, violations.get(5).getMessage());
		assertEquals(64, violations.get(6).getLine());
		assertEquals("Class 'GenericClass' " + MSG, violations.get(6).getMessage());
		assertEquals(72, violations.get(7).getLine());
		assertEquals("Class 'HasStaticAndInstanceFields' " + MSG, violations.get(7).getMessage());
		assertEquals(82, violations.get(8).getLine());
		assertEquals("Class 'HasSideEffects' " + MSG, violations.get(8).getMessage());
		assertEquals(92, violations.get(9).getLine());
		assertEquals("Class 'HasValidation' " + MSG, violations.get(9).getMessage());
		assertEquals(102, violations.get(10).getLine());
		assertEquals("Class 'MethodsPresent' " + MSG, violations.get(10).getMessage());
		assertEquals(115, violations.get(11).getLine());
		assertEquals("Class 'MultipleConstructors' " + MSG, violations.get(11).getMessage());
		assertEquals(128, violations.get(12).getLine());
		assertEquals("Class 'MultipleFieldTypes' " + MSG, violations.get(12).getMessage());
		assertEquals(141, violations.get(13).getLine());
		assertEquals("Class 'NonOverrideEquals' " + MSG, violations.get(13).getMessage());
		assertEquals(156, violations.get(14).getLine());
		assertEquals("Class 'OverrideNonRecordMethod' " + MSG, violations.get(14).getMessage());
		assertEquals(169, violations.get(15).getLine());
		assertEquals("Class 'SameLineFields' " + MSG, violations.get(15).getMessage());
		assertEquals(178, violations.get(16).getLine());
		assertEquals("Class 'SingleField' " + MSG, violations.get(16).getMessage());
		assertEquals(186, violations.get(17).getLine());
		assertEquals("Class 'WithImplements' " + MSG, violations.get(17).getMessage());
	}
}