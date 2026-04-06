package com.etk2000.checkstyle.inputs.preferlambda;

import java.util.function.Supplier;

class InputPreferLambdaViolation {
	void lambdaCandidates() {
		final Runnable r = new Runnable() { // violation: prefer lambda
			@Override
			public void run() {
				System.out.println("hello");
			}
		};
		final Supplier<String> s = new Supplier<>() { // violation: prefer lambda
			@Override
			public String get() {
				return "world";
			}
		};
		final var t = new Thread(new Runnable() { // violation: prefer lambda (method argument)
			@Override
			public void run() {
				System.out.println("argument");
			}
		});
	}

	// qualified this (Outer.this) is accessible from a lambda, so this is still a candidate
	void qualifiedThis() {
		final Runnable r = new Runnable() { // violation: prefer lambda
			@Override
			public void run() {
				System.out.println(InputPreferLambdaViolation.this.toString());
			}
		};
	}
}