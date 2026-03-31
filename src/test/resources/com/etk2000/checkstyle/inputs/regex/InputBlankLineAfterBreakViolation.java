class InputBlankLineAfterBreakViolation {
	void missingBeforeCase(int x) {
		switch (x) {
			case 1:
				break;
			case 2: // violation: no blank line after break
				break;

			default:
				break;
		}
	}

	void missingBeforeDefault(int x) {
		switch (x) {
			case 1:
				break;
			default: // violation: no blank line after break
				break;
		}
	}
}