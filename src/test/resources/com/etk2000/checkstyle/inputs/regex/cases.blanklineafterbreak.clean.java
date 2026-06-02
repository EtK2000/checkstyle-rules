class InputBlankLineAfterBreakClean {
	void method(int x) {
		switch (x) {
			case 1:
				break;

			case 2:
				break;

			default:
				break;
		}
	}
}