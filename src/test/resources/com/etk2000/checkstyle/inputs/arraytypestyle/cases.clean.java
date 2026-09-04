package com.etk2000.checkstyle.inputs.arraytypestyle;

import java.io.ByteArrayInputStream;
import java.util.List;

@SuppressWarnings("unused")
class InputArrayTypeStyleClean {
	@interface TypeAnno {}

	static class ExtendsArrayType extends Number[] {}

	static class ImplementsArrayType implements Cloneable[] {}

	record InnerRecord(int[] component, String[] names) {}

	static int[] staticArr = {1};

	int @TypeAnno [] taField;

	@Deprecated
	int[] annotatedJava;

	int[] ia;
	int[][] iaa;
	List<String>[] lsf;
	String[] sf;
	byte[][] eta, theta;
	char[] alpha, beta;
	List<int[]> iota, kappa;

	InputArrayTypeStyleClean(int[] param) {}

	void arrayCreationExpression() {
		final int[] x = new int[5];
		x[0] = 1;
	}

	void compoundJavaStyle() {
		final int[][] x = {{1, 2}};
		x[0][0] = 1;
	}

	void forEachVariable() {
		for (var item : new int[]{1, 2, 3})
			System.out.println(item);
	}

	int[] methodReturnJavaStyle() {
		return null;
	}

	void methodWithArrayParam(@Deprecated int[] a) {
		a[0] = 1;
	}

	void multiVarJavaStyle(int[] first, int[] second, boolean cond) {
		final int[] delta = {1}, gamma = {2};
		delta[0] = gamma[0];
		for (int[] epsilon = first, zeta = second; cond; epsilon = zeta)
			break;
	}

	void tryWithResources() throws Exception {
		try (var s = new ByteArrayInputStream(new byte[0])) {
			s.read();
		}
	}
}