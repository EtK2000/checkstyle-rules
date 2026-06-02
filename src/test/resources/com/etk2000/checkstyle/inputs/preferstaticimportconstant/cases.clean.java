package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

class InputPreferStaticImportConstantClean {
	interface InnerInterface {
		int X28 = AnchorClass.X28;
	}

	@interface InnerAnnotationType {
		int X29 = AnchorClass.X29;
	}

	private static final int X10 = (int) AnchorClass.X10;
	private static final int X12 = AnchorClass.getX12();
	private static final int X13 = AnchorClass.ARR[0];
	private static final int X18 = 42;
	private static final int X30 = NotImportedClass.X30;
	private static final int X8 = AnchorClass.X8 + 1;
	private static final int X9 = -AnchorClass.X9;
	@SuppressWarnings("PreferStaticImportConstant")
	private static final int X19 = AnchorClass.X19;
	@SuppressWarnings(value = "PreferStaticImportConstant")
	private static final int X22 = AnchorClass.X22;
	@SuppressWarnings({"PreferStaticImportConstant", "unused"})
	private static final int X21 = AnchorClass.X21;
	@SuppressWarnings({"PreferStaticImportConstant"})
	private static final int X20 = AnchorClass.X20;
	@SuppressWarnings("unused")
	private static final int X17_SOURCE = 1;
	@SuppressWarnings("unused")
	private static final int X17 = X17_SOURCE;
	private static final String X14 = AnchorClass.X14 + "";

	private static final int CONDITIONAL_CINIT;
	private static final int DUP_SAME_BLOCK;
	private static final int DUP_TWO_BLOCKS;
	private static final int LITERAL_CINIT;
	private static final int UNINITIALIZED;

	private static int X5 = AnchorClass.X5;

	static {
		UNINITIALIZED = 0;
		LITERAL_CINIT = 42;
		if (System.currentTimeMillis() > 0)
			CONDITIONAL_CINIT = AnchorClass.X1;
		else
			CONDITIONAL_CINIT = AnchorClass.X2;
		DUP_SAME_BLOCK = AnchorClass.X1;
		DUP_SAME_BLOCK = AnchorClass.X2;
		DUP_TWO_BLOCKS = AnchorClass.X3;
	}

	static {
		DUP_TWO_BLOCKS = AnchorClass.X4;
	}

	private final int X4 = AnchorClass.X4;

	void forInitAliasShape() {
		for (var X27 = AnchorClass.X27; X27 < 10; ++X27)
			System.out.println(X27);
	}

	void localVariableAliasShape() {
		final var X26 = AnchorClass.X26;
		System.out.println(X26);
	}
}

@SuppressWarnings("PreferStaticImportConstant")
class InputPreferStaticImportConstantTypeSuppressedClean {
	@SuppressWarnings("PreferStaticImportConstant")
	enum SuppressedEnum {
		A;

		private static final int X = AnchorClass.X;
	}

	@SuppressWarnings("PreferStaticImportConstant")
	record SuppressedRecord(int n) {
		private static final int X = AnchorClass.X;
	}

	static class NestedNotDirectlySuppressed {
		private static final int X = AnchorClass.X;
	}

	static class Outer {
		static class Middle {
			static class Inner {
				private static final int X = AnchorClass.X1;
			}
		}
	}

	static class HostingLocalClass {
		Object m() {
			class LocalInsideMethod {
				private static final int X = AnchorClass.X2;
			}

			return new LocalInsideMethod();
		}
	}

	@SuppressWarnings("PreferStaticImportConstant")
	interface NestedSuppressedInterface {
		class HasFieldInsideInterface {
			private static final int X = AnchorClass.X1;
		}
	}

	@SuppressWarnings("PreferStaticImportConstant")
	@interface NestedSuppressedAnnotationType {
		class HasFieldInsideAnnotationType {
			private static final int X = AnchorClass.X2;
		}
	}

	private static final int X1 = AnchorClass.X1;
	private static final int X2 = AnchorClass.X2;
}

class InputPreferStaticImportConstantCinitMethodCallDotChainLhsClean {
	private static final int X;

	static {
		getOther().X = Foo.X;
	}
}

class InputPreferStaticImportConstantCinitMismatchedQualifierClean {
	private static final int X;

	static {
		Other.X = Foo.X;
	}
}

class InputPreferStaticImportConstantCinitNoMatchingAssignmentClean {
	private static final int X;
	private static int Y;

	static {
		Y = Foo.X;
	}
}

class InputPreferStaticImportConstantCinitUnresolvableClassClean {
	private static final int X;

	static {
		X = NotImported.X;
	}
}

class InputPreferStaticImportConstantCinitFqnLhsWrongPackageClean {
	private static final int X;

	static {
		y.T.X = Foo.X;
	}
}

class InputPreferStaticImportConstantNestedClassRhsUnresolvedOuterClean {
	private static final int X = Outer.Inner.X;
}

class InputPreferStaticImportConstantRhsParenWrappedBareIdentClean {
	private static final int X = (Foo);
}