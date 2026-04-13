package com.etk2000.checkstyle.inputs.preferstaticimport;

import com.etk2000.checkstyle.inputs.preferstaticimport.shadowpkg.Collectors;
import com.etk2000.checkstyle.inputs.preferstaticimport.shadowpkg.Objects;
import com.etk2000.checkstyle.inputs.preferstaticimport.shadowpkg.Predicate;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class InputPreferStaticImportExplicitShadowClean {
	Object usesShadowedCollectors() {
		return Collectors.toSet() == null ? Collectors.toSet() : null;
	}

	boolean usesShadowedObjects(Object a, Object b) {
		return Objects.isNull(a) || Objects.isNull(b);
	}

	boolean usesShadowedPredicate(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}