package com.etk2000.checkstyle.inputs.redundantcast;

class InputRedundantCastClean {
	// downcast: needed at runtime (cast type matches variable type but expression is Object)
	void downcast(Object obj) {
		String s = (String) obj;
	}

	Object getNumber() {
		return 0;
	}

	// cast of method return to different type (downcast/unbox)
	void methodReturnCast() {
		int x = (int) getNumber();
	}

	// narrowing primitive: needed
	void narrowingCast() {
		long x = 5;
		int y = (int) x;
	}

	// null cast in method arguments (may be needed for overload resolution)
	void nullCastInArgs() {
		takesString((String) null);
	}

	// null cast in ternary (no context target type)
	void nullCastInTernary(boolean flag) {
		Object o = flag ? (String) null : "hello";
	}

	// null cast in var declaration (needed for type inference)
	void nullCastInVar() {
		var s = (String) null;
	}

	void takesLong(long x) {}
	void takesString(String s) {}

	// cast of this in anonymous class (can't determine anonymous type)
	void thisInAnonymousClass() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Object o = (Runnable) this;
			}
		};
	}

	// cast where variable uses var (type unknown from AST)
	void varDeclaration() {
		var x = getNumber();
		int y = (int) x;
	}

	// widening in arithmetic: changes result type
	void wideningInArithmetic() {
		int x = 5;
		long y = (long) x * 100;
	}

	// widening in array initializer
	void wideningInArrayInit() {
		int x = 5;
		long[] arr = new long[]{(long) x};
	}

	// widening in bitwise ops: changes result type
	void wideningInBitwiseOps() {
		int x = 5;
		long y = (long) x & 0xFF;
	}

	// widening in expression lambda (can't verify functional interface return type)
	void wideningInLambdaReturn() {
		int x = 5;
		java.util.function.LongSupplier sup = () -> (long) x;
	}

	// widening in method arguments: may affect overload resolution
	void wideningInMethodArgs() {
		int x = 5;
		takesLong((long) x);
	}

	// widening in return from non-primitive method
	Object wideningInNonPrimitiveReturn() {
		int x = 5;
		return (long) x;
	}

	// widening in shift operations: changes result type
	void wideningInShift() {
		int x = 5;
		long y = (long) x << 32;
	}

	// widening in standalone reassignment (not variable declaration)
	void wideningInStandaloneAssign() {
		int x = 5;
		long y = 0;
		y = (long) x;
	}

	// widening in ternary: may affect result type
	void wideningInTernary(boolean flag) {
		int x = 5;
		int y = 10;
		var z = flag ? (long) x : y;
	}

	// widening in unary context: changes result type
	void wideningInUnary() {
		int x = 5;
		long y = -(long) x;
	}

	// widening to wrapper variable (affects boxing)
	void wideningToWrapper() {
		int x = 5;
		Long y = (long) x;
	}
}