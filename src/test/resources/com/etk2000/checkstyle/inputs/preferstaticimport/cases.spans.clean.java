package com.etk2000.checkstyle.inputs.preferstaticimport;

import static com.foo.Bar.*;

import java.util.Objects;

/**
 * A static wildcard import from a class that is not a candidate can bring in any name, so every
 * candidate method is treated as conflicted and nothing here is reported. The file is on its own
 * because that block is file-wide: put it beside another conflict fixture and it would silence that
 * fixture's rows too, for the wrong reason.
 */
class InputPreferStaticImportSpansClean {
	Object twoRequireNonNullCallsBlockedByAStaticWildcard(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		return x.toString() + y.toString();
	}
}