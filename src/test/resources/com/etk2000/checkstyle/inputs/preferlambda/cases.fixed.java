// === case: main ===
// imports: java.util.function.Supplier
class InputPreferLambdaViolation {
	void constructorTypeArgs() {
		final Runnable r = new <String>Runnable() {
			@Override
			public void run() {
				System.out.println("constructor type args");
			}
		};
	}

	void constructorTypeArgsBothLevels() {
		final Supplier<String> s = new <String>Supplier<String>() {
			@Override
			public String get() {
				return "both levels";
			}
		};
	}

	void lambdaCandidates() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				System.out.println("hello");
			}
		};
		final Supplier<String> s = new Supplier<>() {
			@Override
			public String get() {
				return "world";
			}
		};
		final var t = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("argument");
			}
		});
	}

	void qualifiedAnonymousClass() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				System.out.println("qualified");
			}
		};
	}

	void qualifiedThis() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				System.out.println(InputPreferLambdaViolation.this.toString());
			}
		};
	}
}
// === end ===