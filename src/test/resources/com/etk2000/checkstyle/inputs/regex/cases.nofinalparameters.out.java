class InputNoFinalParametersViolation {
	InputNoFinalParametersViolation(int x) {} // violation

	void bothFinal(int x, String y) {} // violation (2x)

	void secondParamFinal(int x, String y) {} // violation

	void singleFinal(int x) {} // violation
}