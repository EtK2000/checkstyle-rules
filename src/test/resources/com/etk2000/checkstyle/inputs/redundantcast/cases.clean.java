package com.etk2000.checkstyle.inputs.redundantcast;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;
import java.util.function.LongSupplier;

class InputRedundantCastClean {
	private final Number shadowed = 0;

	void compoundAssignToArrayElement(long[] arr, int y) {
		arr[0] += (long) y;
	}

	void compoundAssignToStringTarget(char c) {
		String s = "";
		s += (int) c;
	}

	void compoundAssignWiderCast(int x) {
		float f = 1;
		f += (double) x;
	}

	void downcast(Object obj) {
		final String s = (String) obj;
	}

	void floatCastInCompoundDouble(int x) {
		double d = 1;
		d += (float) x;
	}

	void floatCastInTernaryUnderDouble(boolean flag, int x) {
		takesDouble(flag ? (float) x : 1.5);
	}

	void floatCastOfByteUnderDouble(byte b) {
		final double d = (float) b * 1.5;
	}

	void floatCastToDoubleLocal(int x) {
		final double d = (float) x;
	}

	void floatCastToDoubleReassign(int x) {
		double d = 1;
		d = (float) x;
	}

	double floatCastToDoubleReturn(int x) {
		return (float) x;
	}

	void floatCastUnderDoubleSibling(int x) {
		final double d = (float) x * 1.5;
	}

	void genericCastRawSource(List raw) {
		final Object o = (List<String>) raw;
	}

	void genericCastRawTarget(List<String> l) {
		final Object o = (List) l;
	}

	void genericCastWildcardBounds(List<? super Number> l) {
		final Object o = (List<? extends Number>) l;
	}

	Object getNumber() {
		return 0;
	}

	void intersectionCast(Runnable r) {
		final Object o = (Runnable & Serializable) r;
	}

	void lambdaParamShadowsField(List<Object> items) {
		items.forEach(shadowed -> {
			final Number n = (Number) shadowed;
		});
	}

	void methodReturnCast() {
		final int x = (int) getNumber();
	}

	void multiCatchRethrow() throws Exception {
		try {
			throwsTwo();
		}
		catch (IllegalStateException | IllegalArgumentException e) {
			throw (IllegalStateException) e;
		}
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

	void patternVariableShadowsField(Object o) {
		if (o instanceof Comparable shadowed) {
			final Number n = (Number) shadowed;
		}
	}

	void qualifiedNewCast() {
		final Object x = (Object) new java.lang.Object();
	}

	void recordComponentShadowsField() {
		record Holder(Object shadowed) {
			Object unwrap() {
				return (Number) shadowed;
			}
		}
	}

	void takesDouble(double d) {}

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

	void throwsTwo() {
		throw new IllegalStateException();
	}

	void tryResourceShadowsField(Closeable c) throws Exception {
		try (Closeable shadowed = c) {
			final Number n = (Number) shadowed;
		}
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

	void wideningInParenthesisedProduct() {
		final int x = 5;
		final int y = 7;
		final long r = (long) (x * y);
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

	void wideningInTruncatingDivision() {
		final int i = 5;
		final int j = 7;
		final double r = (double) i / j;
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