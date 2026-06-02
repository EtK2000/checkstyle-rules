class InputNoFinalParametersViolation {
	InputNoFinalParametersViolation(final int x) {} // violation

	void bothFinal(final int x, final String y) {} // violation (2x)

	void secondParamFinal(int x, final String y) {} // violation

	void singleFinal(final int x) {} // violation
}