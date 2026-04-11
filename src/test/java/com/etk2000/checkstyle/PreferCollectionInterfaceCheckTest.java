package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferCollectionInterfaceCheckTest {
	private static final String DIR = "collectioninterface/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferCollectionInterfaceCheck.class, DIR + "InputCollectionInterfaceClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferCollectionInterfaceCheck.class, DIR + "InputCollectionInterfaceViolation.java");
		assertEquals(26, violations.size());

		// ArrayList return
		assertEquals(17, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(0).getMessage());

		// HashMap param
		assertEquals(23, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use 'Map' instead of 'HashMap'.", violations.get(1).getMessage());

		// HashSet param
		assertEquals(27, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use 'Set' instead of 'HashSet'.", violations.get(2).getMessage());

		// TreeMap return
		assertEquals(31, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use 'Map' instead of 'TreeMap'.", violations.get(3).getMessage());

		// LinkedHashSet param
		assertEquals(37, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Use 'Set' instead of 'LinkedHashSet'.", violations.get(4).getMessage());

		// LinkedHashMap param
		assertEquals(41, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Use 'Map' instead of 'LinkedHashMap'.", violations.get(5).getMessage());

		// TreeSet param
		assertEquals(45, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Use 'Set' instead of 'TreeSet'.", violations.get(6).getMessage());

		// return + param on same line
		assertEquals(49, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(7).getMessage());

		assertEquals(49, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals("Use 'Set' instead of 'HashSet'.", violations.get(8).getMessage());

		// two params on same line
		assertEquals(55, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(9).getMessage());

		assertEquals(55, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Use 'Map' instead of 'HashMap'.", violations.get(10).getMessage());

		// raw type
		assertEquals(61, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(11).getMessage());

		// nested generic return
		assertEquals(67, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(12).getMessage());

		// nested generic param
		assertEquals(73, violations.get(13).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(13).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(13).getMessage());

		// constructor param
		assertEquals(80, violations.get(14).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(14).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(14).getMessage());

		// fully qualified name
		assertEquals(86, violations.get(15).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(15).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(15).getMessage());

		// annotated type arg
		assertEquals(92, violations.get(16).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(16).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(16).getMessage());

		// annotated generic arg on concrete type
		assertEquals(96, violations.get(17).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(17).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(17).getMessage());

		// wildcard extends
		assertEquals(100, violations.get(18).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(18).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(18).getMessage());

		// wildcard super
		assertEquals(102, violations.get(19).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(19).getSeverityLevel());
		assertEquals("Use 'Set' instead of 'HashSet'.", violations.get(19).getMessage());

		// multi-level nesting
		assertEquals(106, violations.get(20).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(20).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(20).getMessage());

		// ArrayDeque -> Deque
		assertEquals(112, violations.get(21).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(21).getSeverityLevel());
		assertEquals("Use 'Deque' instead of 'ArrayDeque'.", violations.get(21).getMessage());

		// PriorityQueue -> Queue
		assertEquals(116, violations.get(22).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(22).getSeverityLevel());
		assertEquals("Use 'Queue' instead of 'PriorityQueue'.", violations.get(22).getMessage());

		// bounded type param: return fires, bound does not
		assertEquals(120, violations.get(23).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(23).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(23).getMessage());

		// intersection bound: return fires, bound does not
		assertEquals(126, violations.get(24).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(24).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(24).getMessage());

		// concrete in bound: return fires, bound does not
		assertEquals(132, violations.get(25).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(25).getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violations.get(25).getMessage());
	}
}