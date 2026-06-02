package com.etk2000.checkstyle.inputs.redundantcast;

import java.util.function.LongSupplier;

class InputRedundantCastClean {
	void downcast(Object obj) {
		final String s = (String) obj;
	}

	Object getNumber() {
		return 0;
	}

	void methodReturnCast() {
		final int x = (int) getNumber();
	}

	void narrowingCast() {
		final long x = 5;
		final int y = (int) x;
	}

	void nullCastInArgs() {
		takesString((String) null);
	}

	void nullCastInTernary(boolean flag) {
		final Object o = flag ? (String) null : "hello";
	}

	void nullCastInVar() {
		final var s = (String) null;
	}

	void qualifiedNewCast() {
		final Object x = (Object) new java.lang.Object();
	}

	void takesLong(long x) {}

	void takesString(String s) {}

	void thisInAnonymousClass() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final Object o = (Runnable) this;
			}
		};
	}

	void varDeclaration() {
		final var x = getNumber();
		final int y = (int) x;
	}

	void wideningInArithmetic() {
		final int x = 5;
		final long y = (long) x * 100;
	}

	void wideningInArrayInit() {
		final int x = 5;
		final long[] arr = new long[]{(long) x};
	}

	void wideningInBitwiseOps() {
		final int x = 5;
		final long y = (long) x & 0xFF;
	}

	void wideningInLambdaReturn() {
		final int x = 5;
		final LongSupplier sup = () -> (long) x;
	}

	void wideningInMethodArgs() {
		final int x = 5;
		takesLong((long) x);
	}

	Object wideningInNonPrimitiveReturn() {
		final int x = 5;
		return (long) x;
	}

	void wideningInShift() {
		final int x = 5;
		final long y = (long) x << 32;
	}

	void wideningInTernary(boolean flag) {
		final int x = 5;
		final int y = 10;
		final var z = flag ? (long) x : y;
	}

	Object wideningInTernaryNonPrimitiveReturn(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0;
	}

	void wideningInUnary() {
		final int x = 5;
		final long y = -(long) x;
	}

	void wideningToWrapper() {
		final int x = 5;
		final Long y = (long) x;
	}
}