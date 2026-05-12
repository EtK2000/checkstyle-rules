package com.etk2000.checkstyle.inputs.preferlambda;

import java.util.Iterator;
import java.util.function.Supplier;

class InputPreferLambdaClean {
	void abstractClass() {
		final var obj = new AbstractClass() {
			@Override
			public void doWork() {
				System.out.println("work");
			}
		};
	}

	void complexAnonymous() {
		final var r = new Runnable() {
			int count;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void lambdas() {
		final Runnable r = () -> System.out.println("hello");
		final Supplier<String> s = () -> "world";
	}

	void methodReferenceToThis() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				func(this);
			}
		};
	}

	void methodUsingSuper() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				super.toString();
			}
		};
	}

	void nonFunctionalInterface() {
		final var it = new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public String next() {
				return null;
			}
		};
	}
}

abstract class AbstractClass {
	abstract void doWork();
}