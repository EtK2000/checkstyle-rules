package com.etk2000.checkstyle.inputs.astutil;

import java.util.ArrayList;

import javax.annotation.Nonnull;

@javax.annotation.CheckReturnValue // intentional FQN: tested by AstUtilTest.testTypeTextQualified
class InputAstUtil {
	@interface A {}
	@interface B {}

	int noAnnotationField, primitiveField;
	@Nonnull
	int field;
	java.util.List qualifiedField; // intentional FQN: tested by AstUtilTest.testTypeTextQualified

	InputAstUtil(String ctorParam) {}

	void castAndResolve(Object obj) {
		final String s = (String) obj;
		System.out.println(s);
	}

	void castWrongExpr(Object obj) {
		final String s = (String) this;
	}

	void castWrongType(Object obj) {
		final Integer n = (Integer) obj;
	}

	void emptyBlock() {
	}

	void emptyStatement(boolean flag) {
		if (flag);
	}

	void multiLine(
			int a,
			int b
	) {}

	void primitiveLocal() {
		final int x = 1;
	}

	void varAnonymousClassLocal() {
		// Thread has multiple methods, so this is not convertible to a lambda.
		final var x = new Thread() {
			@Override
			public void run() {}
		};
	}

	void varGenericAnonymousClassLocal() {
		final var x = new ArrayList<String>() {};
	}

	void varLambdaParameterLocal() {
		final java.util.function.Consumer<String> c = (var s) -> {
			s.length();
		};
	}

	void varLocal() {
		final var x = "hello";
	}

	Object varMethodCallInitHelper() {
		return null;
	}

	void varMethodCallInitLocal() {
		final var x = varMethodCallInitHelper();
	}

	void varNewArrayInitializerLocal() {
		final var x = new int[]{1, 2, 3};
	}

	void varNewArrayInitializerRefLocal() {
		final var x = new String[]{"a", "b"};
	}

	void varNewArrayLocal() {
		final var x = new String[10];
	}

	void varNewBothAnnotatedArrayLocal() {
		final var x = new @A String @B [10];
	}

	void varNewDeeplyQualifiedLocal() {
		final var x = new java.util.concurrent.atomic.AtomicInteger();
	}

	void varNewDimAnnotatedArrayLocal() {
		final var x = new String @A [10];
	}

	void varNewDimAnnotatedMultiDimArrayLocal() {
		final var x = new String @A [2] @B [3];
	}

	void varNewGenericLocal() {
		final var x = new java.util.HashMap<String, Integer>();
	}

	void varNewLocal() {
		final var x = new StringBuilder();
	}

	void varNewMultiDimArrayLocal() {
		final var x = new String[2][3];
	}

	void varNewPrimitiveBothAnnotatedArrayLocal() {
		final var x = new @A int @B [10];
	}

	void varNewPrimitiveDimAnnotatedArrayLocal() {
		final var x = new int @A [10];
	}

	void varNewPrimitiveDimAnnotatedMultiDimArrayLocal() {
		final var x = new int @A [2] @B [3];
	}

	void varNewPrimitiveMultiDimArrayInitializerLocal() {
		final var x = new int[][]{{1, 2}, {3, 4}};
	}

	void varNewPrimitiveMultiDimArrayLocal() {
		final var x = new int[2][3];
	}

	void varNewPrimitiveSizedArrayLocal() {
		final var x = new int[10];
	}

	void varNewPrimitiveTypeAnnotatedArrayLocal() {
		final var x = new @A int[10];
	}

	void varNewQualifiedAnonymousClassLocal() {
		final var x = new java.lang.Thread() {
			@Override
			public void run() {}
		};
	}

	void varNewQualifiedArrayInitializerLocal() {
		final var x = new java.lang.String[]{"a", "b"};
	}

	void varNewQualifiedArrayLocal() {
		final var x = new java.lang.String[10];
	}

	void varNewQualifiedDiamondLocal() {
		final var x = new java.util.HashMap<>();
	}

	void varNewQualifiedDimAnnotatedArrayLocal() {
		final var x = new java.lang.String @A [10];
	}

	void varNewQualifiedDimAnnotatedMultiDimArrayLocal() {
		final var x = new java.lang.String @A [2] @B [3];
	}

	void varNewQualifiedGenericAnonymousClassLocal() {
		final var x = new java.util.ArrayList<String>() {};
	}

	void varNewQualifiedInnerClassLocal() {
		final var x = new java.util.AbstractMap.SimpleEntry<>("a", "b");
	}

	void varNewQualifiedLocal() {
		final var x = new java.lang.Object();
	}

	void varNewQualifiedMultiDimArrayLocal() {
		final var x = new java.lang.String[2][3];
	}

	void varNewQualifiedTypeAnnotatedArrayLocal() {
		final var x = new @A java.lang.String[10];
	}

	void varNewTypeAnnotatedArrayLocal() {
		final var x = new @A String[10];
	}
}