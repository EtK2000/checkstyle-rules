package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

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