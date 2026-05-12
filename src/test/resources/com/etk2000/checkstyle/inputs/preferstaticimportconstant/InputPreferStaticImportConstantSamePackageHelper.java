package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

/**
 * Same-package sibling for {@code InputPreferStaticImportConstantSamePackageViolation}.
 * Its constants are referenced by the violation file without an explicit
 * import — the check resolves the class via filesystem probe (sibling
 * {@code .java} file in the same directory).
 */
class InputPreferStaticImportConstantSamePackageHelper {
	static final int MAX = 100;
}