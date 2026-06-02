package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class InputPreferStaticImportSameFileShadowClean {
	static class Collectors {
		static Object toSet() {
			return null;
		}
	}

	static class Objects {
		static boolean isNull(Object x) {
			return x == null;
		}
	}

	static class Predicate {
		static boolean not(Object x) {
			return x == null;
		}
	}

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