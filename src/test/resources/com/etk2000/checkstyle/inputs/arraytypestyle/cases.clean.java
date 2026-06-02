package com.etk2000.checkstyle.inputs.arraytypestyle;

import java.io.ByteArrayInputStream;
import java.util.List;

@SuppressWarnings("unused")
class InputArrayTypeStyleClean {
	@interface TypeAnno {}

	// C-style brackets in a class-header extends/implements clause are not a declarator; the check must
	// stay silent (it registers only method/parameter/record-component/variable declarations).
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

	void tryWithResources() throws Exception {
		try (var s = new ByteArrayInputStream(new byte[0])) {
			s.read();
		}
	}
}