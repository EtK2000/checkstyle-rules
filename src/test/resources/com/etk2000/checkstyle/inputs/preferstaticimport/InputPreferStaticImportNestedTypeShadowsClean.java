package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.function.*;

// Exercises every token type accepted by walkForLocalShadows as a nested
// shadow: ENUM_DEF, INTERFACE_DEF, RECORD_DEF, ANNOTATION_DEF.
// (CLASS_DEF is covered by InputPreferStaticImportSameFileShadowClean.)
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

class RecordShadowHost {
	record Predicate() {
	}

	boolean f(Object a, Object b) {
		return Predicate.not(a) || Predicate.not(b);
	}
}