package com.etk2000.checkstyle.inputs.redundantcast;

import java.util.function.LongSupplier;

class InputRedundantCastClean {
	// downcast: needed at runtime (cast type matches variable type but expression is Object)
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

	// null cast in method arguments (may be needed for overload resolution)
	void nullCastInArgs() {
		takesString((String) null);
	}

	// null cast in ternary (no context target type)
	void nullCastInTernary(boolean flag) {
		final Object o = flag ? (String) null : "hello";
	}

	// null cast in var declaration (needed for type inference)
	void nullCastInVar() {
		final var s = (String) null;
	}

	// qualified new: expressionType returns "java.lang.Object" but cast type is "Object"
	void qualifiedNewCast() {
		final Object x = (Object) new java.lang.Object();
	}

	void takesLong(long x) {}
	void takesString(String s) {}

	// cast of this in anonymous class (can't determine anonymous type)
	void thisInAnonymousClass() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final Object o = (Runnable) this;
			}
		};
	}

	// cast where variable uses var (type unknown from AST)
	void varDeclaration() {
		final var x = getNumber();
		final int y = (int) x;
	}

	// widening in arithmetic: changes result type
	void wideningInArithmetic() {
		final int x = 5;
		final long y = (long) x * 100;
	}

	void wideningInArrayInit() {
		final int x = 5;
		final long[] arr = new long[]{(long) x};
	}

	// widening in bitwise ops: changes result type
	void wideningInBitwiseOps() {
		final int x = 5;
		final long y = (long) x & 0xFF;
	}

	// widening in expression lambda (can't verify functional interface return type)
	void wideningInLambdaReturn() {
		final int x = 5;
		final LongSupplier sup = () -> (long) x;
	}

	// widening in method arguments: may affect overload resolution
	void wideningInMethodArgs() {
		final int x = 5;
		takesLong((long) x);
	}

	Object wideningInNonPrimitiveReturn() {
		final int x = 5;
		return (long) x;
	}

	// widening in shift operations: changes result type
	void wideningInShift() {
		final int x = 5;
		final long y = (long) x << 32;
	}

	// widening in ternary: may affect result type (var target)
	void wideningInTernary(boolean flag) {
		final int x = 5;
		final int y = 10;
		final var z = flag ? (long) x : y;
	}

	Object wideningInTernaryNonPrimitiveReturn(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0;
	}

	// widening in unary context: changes result type
	void wideningInUnary() {
		final int x = 5;
		final long y = -(long) x;
	}

	// widening to wrapper variable (affects boxing)
	void wideningToWrapper() {
		final int x = 5;
		final Long y = (long) x;
	}
}