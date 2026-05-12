package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

class InputPreferStaticImportConstantVisibilityViolation {
	static final int PACKAGE_PRIVATE_ALIAS = AnchorClass.X1; // violation: Replace 'PACKAGE_PRIVATE_ALIAS' alias of 'AnchorClass.X1' with a static import.
	protected static final int PROTECTED_ALIAS = AnchorClass.X2; // violation: Replace 'PROTECTED_ALIAS' alias of 'AnchorClass.X2' with a static import.
	public static final int PUBLIC_ALIAS = AnchorClass.X3; // violation: Replace 'PUBLIC_ALIAS' alias of 'AnchorClass.X3' with a static import.
}