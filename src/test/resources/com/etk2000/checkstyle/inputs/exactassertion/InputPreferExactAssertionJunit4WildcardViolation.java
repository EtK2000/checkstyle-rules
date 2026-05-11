package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionJunit4WildcardViolation {
	void unqualifiedComparisonFiresUnderWildcardJ4() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}
}