package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.*;

class InputPreferStaticImportConstantSamePackageViolation {
	private static final int MAX = InputPreferStaticImportConstantSamePackageHelper.MAX; // violation: Replace 'MAX' alias of 'InputPreferStaticImportConstantSamePackageHelper.MAX' with a static import.
}