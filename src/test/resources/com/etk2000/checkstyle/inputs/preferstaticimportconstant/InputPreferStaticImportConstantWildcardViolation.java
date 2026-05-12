package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*;

class InputPreferStaticImportConstantWildcardViolation {
	private static final int WILDCARD_X = AnchorClass.WILDCARD_X; // violation: Replace 'WILDCARD_X' alias of 'AnchorClass.WILDCARD_X' with a static import.
}