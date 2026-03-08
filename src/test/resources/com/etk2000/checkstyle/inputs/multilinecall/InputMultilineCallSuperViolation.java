package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallSuperViolation {
	static class Base {
		Base(int a, int b, int c) {}
	}

	static class CleanSuperCall extends Base {
		CleanSuperCall() {
			super(
					1,
					2,
					3
			);
		}
	}

	static class LambdaBase {
		LambdaBase(Runnable r) {}
	}

	static class CleanSuperWithLambda extends LambdaBase {
		CleanSuperWithLambda() {
			super(() -> {
				System.out.println("hello");
			});
		}
	}

	static class ClosingViolation extends Base {
		ClosingViolation() {
			super(
					1,
					2,
					3); // violation line 35 — arg on closing paren line
		}
	}

	static class OpeningViolation extends Base {
		OpeningViolation() {
			super(1, // violation line 41 — arg on opening paren line
					2,
					3
			);
		}
	}

	static class SharedLineViolation extends Base {
		SharedLineViolation() {
			super(
					1, 2, // violation line 51 — args sharing line
					3
			);
		}
	}
}