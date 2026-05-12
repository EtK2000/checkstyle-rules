package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

import java.util.Map;

class InputPreferStaticImportConstantMultiVarViolation {
	private static final int FQ_A = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11, // violation: Replace 'FQ_A' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11' with a static import.
			FQ_B = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12; // violation: Replace 'FQ_B' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12' with a static import.
	private static final int MIXED_A = 0,
			MIXED_B = AnchorClass.X5; // violation: Replace 'MIXED_B' alias of 'AnchorClass.X5' with a static import.
	private static final int MULTI_A = AnchorClass.X3, // violation: Replace 'MULTI_A' alias of 'AnchorClass.X3' with a static import.
			MULTI_B = AnchorClass.X4; // violation: Replace 'MULTI_B' alias of 'AnchorClass.X4' with a static import.
	private static final int PAREN_A = (AnchorClass.X9), // violation: Replace 'PAREN_A' alias of 'AnchorClass.X9' with a static import.
			PAREN_B = (AnchorClass.X10); // violation: Replace 'PAREN_B' alias of 'AnchorClass.X10' with a static import.
	private static final int SINGLE_A = AnchorClass.X1, SINGLE_B = AnchorClass.X2; // violation: Replace 'SINGLE_A' alias of 'AnchorClass.X1' with a static import. // violation: Replace 'SINGLE_B' alias of 'AnchorClass.X2' with a static import.
	private static final int TRI_A = AnchorClass.X6, // violation: Replace 'TRI_A' alias of 'AnchorClass.X6' with a static import.
			TRI_B = AnchorClass.X7, // violation: Replace 'TRI_B' alias of 'AnchorClass.X7' with a static import.
			TRI_C = AnchorClass.X8; // violation: Replace 'TRI_C' alias of 'AnchorClass.X8' with a static import.
	private static final Map<String, Integer> MAP_A = AnchorClass.X13, // violation: Replace 'MAP_A' alias of 'AnchorClass.X13' with a static import.
			MAP_B = AnchorClass.X14; // violation: Replace 'MAP_B' alias of 'AnchorClass.X14' with a static import.
}