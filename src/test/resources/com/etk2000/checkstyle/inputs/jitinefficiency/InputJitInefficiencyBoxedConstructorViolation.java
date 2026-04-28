package com.etk2000.checkstyle.inputs.jitinefficiency;

class InputJitInefficiencyBoxedConstructorViolation {
	void allBoxedTypes(boolean flag) {
		final var a = new Integer(42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		final var b = new Long(100L); // violation: Use 'Long.valueOf(...)' instead of 'new Long(...)'.
		final var c = new Boolean(true); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		final var d = new Boolean(flag); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		final var e = new Double(3.14); // violation: Use 'Double.valueOf(...)' instead of 'new Double(...)'.
		final var f = new Float(1.5f); // violation: Use 'Float.valueOf(...)' instead of 'new Float(...)'.
		final var g = new Short((short) 1); // violation: Use 'Short.valueOf(...)' instead of 'new Short(...)'.
		final var h = new Byte((byte) 1); // violation: Use 'Byte.valueOf(...)' instead of 'new Byte(...)'.
		final var i = new Character('c'); // violation: Use 'Character.valueOf(...)' instead of 'new Character(...)'.
		System.out.println(a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + g + " " + h + " " + i);
	}
}