package com.etk2000.checkstyle.inputs.noblanklinebetweensinglecases;

class InputBracedCaseBlankLineClean {
	int method(int x) {
		switch (x) {
			case 1: {
				final var y = x + 1;
				return y;
			}
			case 2: {
				final var z = x + 2;
				return z;
			}
			default:
				return 0;
		}
	}

	// mix of braced and unbraced — blank line only after unbraced multi-line case
	int mixed(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				return 1;

			case 2: {
				final var z = x + 2;
				return z;
			}
			default:
				return 0;
		}
	}
}

class InputSingleCaseClean {
	int method(int x) {
		switch (x) {
			case 1:
				return 1;
			case 2:
				return 2;
			default:
				return 0;
		}
	}

	int multiLineCaseBreaksChain(int x) {
		switch (x) {
			case 1:
				return 1;

			case 2:
				System.out.println("multi-line");
				return 2;

			case 3:
				return 3;
			default:
				return 0;
		}
	}

	void throwCases(int x) {
		switch (x) {
			case 1:
				throw new RuntimeException("one");
			case 2:
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("default");
		}
	}

	int yieldCases(int x) {
		return switch (x) {
			case 1:
				yield 1;
			case 2:
				yield 2;
			default:
				yield 0;
		};
	}
}