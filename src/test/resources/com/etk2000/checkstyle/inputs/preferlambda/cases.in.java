package com.etk2000.checkstyle.inputs.preferlambda;

// === case: main ===
// imports: java.util.function.Supplier
class InputPreferLambdaViolation {
	void constructorTypeArgs() {
		final Runnable r = new <String>Runnable() { // violation: Use a lambda expression instead of anonymous 'Runnable'.
			@Override
			public void run() {
				System.out.println("constructor type args");
			}
		};
	}

	void constructorTypeArgsBothLevels() {
		final Supplier<String> s = new <String>Supplier<String>() { // violation: Use a lambda expression instead of anonymous 'Supplier'.
			@Override
			public String get() {
				return "both levels";
			}
		};
	}

	void lambdaCandidates() {
		final Runnable r = new Runnable() { // violation: Use a lambda expression instead of anonymous 'Runnable'.
			@Override
			public void run() {
				System.out.println("hello");
			}
		};
		final Supplier<String> s = new Supplier<>() { // violation: Use a lambda expression instead of anonymous 'Supplier'.
			@Override
			public String get() {
				return "world";
			}
		};
		final var t = new Thread(new Runnable() { // violation: Use a lambda expression instead of anonymous 'Runnable'.
			@Override
			public void run() {
				System.out.println("argument");
			}
		});
	}

	void qualifiedAnonymousClass() {
		final Runnable r = new java.lang.Runnable() { // violation: Use a lambda expression instead of anonymous 'java.lang.Runnable'.
			@Override
			public void run() {
				System.out.println("qualified");
			}
		};
	}

	void qualifiedThis() {
		final Runnable r = new Runnable() { // violation: Use a lambda expression instead of anonymous 'Runnable'.
			@Override
			public void run() {
				System.out.println(InputPreferLambdaViolation.this.toString());
			}
		};
	}
}
// === end ===