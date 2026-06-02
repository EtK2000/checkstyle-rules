package com.etk2000.checkstyle.inputs.preferstaticimport.siblingshadow;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class InputPreferStaticImportSiblingShadowClean {
	Object usesShadowedCollectors() {
		final var a = Collectors.toSet();
		final var b = Collectors.toSet();
		return a == null ? b : a;
	}

	boolean usesShadowedObjects(Object a, Object b) {
		return Objects.isNull(a) || Objects.isNull(b);
	}

	List<Object> usesShadowedPredicate(List<Object> list) {
		final var a = Predicate.not(list.getFirst());
		final var b = Predicate.not(list.getLast());
		return a || b ? list : List.of();
	}
}