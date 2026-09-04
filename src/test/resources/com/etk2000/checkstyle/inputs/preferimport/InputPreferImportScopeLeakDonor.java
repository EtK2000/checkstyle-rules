package com.etk2000.checkstyle.inputs.preferimport;

import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*;

class InputPreferImportScopeLeakDonor {
	Boolean value() {
		return java.lang.Boolean.TRUE;
	}
}