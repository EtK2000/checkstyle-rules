package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

class InputPreferStaticImportConstantCinitViolation {
	private static final int FQ_CINIT; // violation: Replace 'FQ_CINIT' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25' with a static import.
	private static final int FQ_LHS_CINIT; // violation: Replace 'FQ_LHS_CINIT' alias of 'AnchorClass.X27' with a static import.
	private static final int NESTED_CINIT; // violation: Replace 'NESTED_CINIT' alias of 'AnchorClass.Inner.X16' with a static import.
	private static final int PAREN_CINIT; // violation: Replace 'PAREN_CINIT' alias of 'AnchorClass.X23' with a static import.
	private static final int QUALIFIED_CINIT; // violation: Replace 'QUALIFIED_CINIT' alias of 'AnchorClass.X26' with a static import.
	private static final int SPLIT_ALIAS; // violation: Replace 'SPLIT_ALIAS' alias of 'AnchorClass.X24' with a static import.

	static {
		FQ_CINIT = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25;
		com.etk2000.checkstyle.inputs.preferstaticimportconstant.InputPreferStaticImportConstantCinitViolation.FQ_LHS_CINIT = AnchorClass.X27;
		NESTED_CINIT = AnchorClass.Inner.X16;
		PAREN_CINIT = (AnchorClass.X23);
		InputPreferStaticImportConstantCinitViolation.QUALIFIED_CINIT = AnchorClass.X26;
		SPLIT_ALIAS = AnchorClass.X24;
	}
}