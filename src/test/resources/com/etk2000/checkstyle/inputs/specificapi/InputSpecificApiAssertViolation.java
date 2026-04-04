package com.etk2000.checkstyle.inputs.specificapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

class InputSpecificApiAssertViolation {
	void assertEqualsFalse() {
		assertEquals(false, 1 == 2); // violation: use assertFalse
	}

	void assertEqualsFalseReversed() {
		assertEquals(1 == 2, false); // violation: use assertFalse
	}

	void assertEqualsNull() {
		assertEquals(null, new Object()); // violation: use assertNull
	}

	void assertEqualsNullReversed() {
		assertEquals(new Object(), null); // violation: use assertNull
	}

	void assertEqualsTrue() {
		assertEquals(true, 1 == 1); // violation: use assertTrue
	}

	void assertEqualsTrueReversed() {
		assertEquals(1 == 1, true); // violation: use assertTrue
	}

	void assertEqualsWithMessageFalse() {
		assertEquals("msg", false, 1 == 2); // violation: use assertFalse
	}

	void assertEqualsWithMessageFalseReversed() {
		assertEquals("msg", 1 == 2, false); // violation: use assertFalse
	}

	void assertEqualsWithMessageNull() {
		assertEquals("msg", null, new Object()); // violation: use assertNull
	}

	void assertEqualsWithMessageNullReversed() {
		assertEquals("msg", new Object(), null); // violation: use assertNull
	}

	void assertEqualsWithMessageTrue() {
		assertEquals("msg", true, 1 == 1); // violation: use assertTrue
	}

	void assertEqualsWithMessageTrueReversed() {
		assertEquals("msg", 1 == 1, true); // violation: use assertTrue
	}

	void assertNotEqualsFalse() {
		assertNotEquals(false, 1 == 1); // violation: use assertTrue
	}

	void assertNotEqualsFalseReversed() {
		assertNotEquals(1 == 1, false); // violation: use assertTrue
	}

	void assertNotEqualsNull() {
		assertNotEquals(null, new Object()); // violation: use assertNotNull
	}

	void assertNotEqualsNullReversed() {
		assertNotEquals(new Object(), null); // violation: use assertNotNull
	}

	void assertNotEqualsTrue() {
		assertNotEquals(true, 1 == 2); // violation: use assertFalse
	}

	void assertNotEqualsTrueReversed() {
		assertNotEquals(1 == 2, true); // violation: use assertFalse
	}

	void assertNotEqualsWithMessageFalse() {
		assertNotEquals("msg", false, 1 == 1); // violation: use assertTrue
	}

	void assertNotEqualsWithMessageFalseReversed() {
		assertNotEquals("msg", 1 == 1, false); // violation: use assertTrue
	}

	void assertNotEqualsWithMessageNull() {
		assertNotEquals("msg", null, new Object()); // violation: use assertNotNull
	}

	void assertNotEqualsWithMessageNullReversed() {
		assertNotEquals("msg", new Object(), null); // violation: use assertNotNull
	}

	void assertNotEqualsWithMessageTrue() {
		assertNotEquals("msg", true, 1 == 2); // violation: use assertFalse
	}

	void assertNotEqualsWithMessageTrueReversed() {
		assertNotEquals("msg", 1 == 2, true); // violation: use assertFalse
	}

	void assertNotSameNull() {
		assertNotSame(null, new Object()); // violation: use assertNotNull
	}

	void assertNotSameNullReversed() {
		assertNotSame(new Object(), null); // violation: use assertNotNull
	}

	void assertNotSameWithMessage() {
		assertNotSame("msg", null, new Object()); // violation: use assertNotNull
	}

	void assertNotSameWithMessageReversed() {
		assertNotSame("msg", new Object(), null); // violation: use assertNotNull
	}

	void assertSameNull() {
		assertSame(null, new Object()); // violation: use assertNull
	}

	void assertSameNullReversed() {
		assertSame(new Object(), null); // violation: use assertNull
	}

	void assertSameWithMessage() {
		assertSame("msg", null, new Object()); // violation: use assertNull
	}

	void assertSameWithMessageReversed() {
		assertSame("msg", new Object(), null); // violation: use assertNull
	}

	void qualifiedAssertEquals() {
		org.junit.Assert.assertEquals(true, 1 == 1); // violation: use assertTrue
	}
}