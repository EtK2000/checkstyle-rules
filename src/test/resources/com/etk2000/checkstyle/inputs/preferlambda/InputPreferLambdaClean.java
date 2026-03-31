package com.etk2000.checkstyle.inputs.preferlambda;

import java.util.Iterator;
import java.util.function.Supplier;

class InputPreferLambdaClean {
	void abstractClass() {
		var obj = new AbstractClass() {
			@Override
			public void doWork() {
				System.out.println("work");
			}
		};
	}

	void complexAnonymous() {
		var r = new Runnable() {
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void lambdas() {
		Runnable r = () -> System.out.println("hello");
		Supplier<String> s = () -> "world";
	}

	void methodReferenceToThis() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				func(this);
			}
		};
	}

	void methodUsingSuper() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				super.toString();
			}
		};
	}

	void nonFunctionalInterface() {
		var it = new Iterator<String>() {
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