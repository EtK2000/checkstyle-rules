package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;
import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.OtherAnchorClass;

import static com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.OtherAnchorClass.X23;

class InputPreferStaticImportConstantConflictViolation {
	private static final int X23_FROM_ANCHOR = AnchorClass.X23; // violation: Replace 'X23_FROM_ANCHOR' alias of 'AnchorClass.X23' with a static import.

	int useOther() {
		return X23 + OtherAnchorClass.X23;
	}
}