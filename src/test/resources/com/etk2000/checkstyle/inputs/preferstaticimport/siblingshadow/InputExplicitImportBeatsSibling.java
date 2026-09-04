package com.etk2000.checkstyle.inputs.preferstaticimport.siblingshadow;

import java.util.Objects;

/**
 * The sibling {@code Objects.java} in this directory shadows the simple name for the wildcard
 * fixture beside it, but an explicit single-type import names the class outright and the shadow
 * never comes into it. The check must still report here, which is what keeps the shadow probe from
 * quietly swallowing the ordinary case.
 */
class InputExplicitImportBeatsSibling {
	boolean usesExplicitlyImportedObjects(Object a, Object b) {
		return Objects.isNull(a) || Objects.isNull(b);
	}
}