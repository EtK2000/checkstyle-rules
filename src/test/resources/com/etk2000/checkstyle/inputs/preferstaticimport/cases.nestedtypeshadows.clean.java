package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.function.*;

class InputPreferStaticImportNestedTypeShadowsClean {
	@interface Predicate {
	}

	boolean f(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}

class EnumShadowHost {
	enum Predicate {
		X
	}

	boolean f(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}

class InterfaceShadowHost {
	interface Predicate {
	}

	boolean f(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}

class LocalClassShadowHost {
	boolean f(Object a, Object b) {
		class Predicate {
			static boolean not(Object v) {
				return v == null;
			}
		}

		return Predicate.not(a) || Predicate.not(b);
	}
}

class RecordShadowHost {
	record Predicate() {}

	boolean f(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}