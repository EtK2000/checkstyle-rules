package com.etk2000.checkstyle.inputs.preferstaticimport;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

class InputPreferStaticImportConflictsClean {
	/**
	 * A record rather than an interface, because an interface would have to declare {@code isNull}
	 * as a method and the check conflicts every candidate method name declared anywhere in the file.
	 * That would silence the two rows below for a reason other than the one they are about, leaving
	 * them green whatever the receiver gate does. A record component generates no method declaration.
	 */
	record Holder(boolean isNull) {}

	@SuppressWarnings("unused")
	boolean isNullCallsHaveShadowingLocalVar(Holder Objects) {
		return Objects.isNull() || Objects.isNull();
	}

	/**
	 * A pattern binds the name as completely as a declarator does, but only a governing {@code if}
	 * and the left operand of an {@code &&} put it on the call's own ancestor chain. These three put
	 * it elsewhere, and each was measured rewriting the call onto {@code java.util.Objects} before
	 * the loose probe was added.
	 */
	@SuppressWarnings("unused")
	boolean isNullCallsHaveShadowingPatternVariable(Object o, Object p, Object q) {
		if (!(o instanceof Holder Objects))
			return false;
		final var early = Objects.isNull() || Objects.isNull();

		while (p instanceof Holder Objects)
			p = Objects.isNull() || Objects.isNull() ? null : p;

		return early || (q instanceof Holder Objects
				? Objects.isNull() || Objects.isNull()
				: false);
	}

	/**
	 * A {@code var} declarator spells no type, so a gate that asks what the name's type is cannot
	 * tell this from a name nothing binds. Reporting here would rewrite a call on the local into one
	 * on {@code java.util.Objects}, which compiles and runs a different method.
	 */
	@SuppressWarnings("unused")
	boolean isNullCallsHaveShadowingVarLocal(Holder source) {
		final var Objects = source;
		return Objects.isNull() || Objects.isNull();
	}

	boolean not(Object x) {
		return x == null;
	}

	Object requireNonNull(Object value) {
		return value;
	}

	void usesObjectsRequireNonNullButLocalShadows(Object a, Object b) {
		final var x = Objects.requireNonNull(a);
		final var y = Objects.requireNonNull(b);
		System.out.println(x + ":" + y);
	}

	void usesPredicateNotButLocalShadows(Stream<String> stream) {
		final var s1 = stream.filter(Predicate.not(String::isEmpty));
		final var s2 = stream.filter(Predicate.not(String::isBlank));
		System.out.println(s1 + ":" + s2);
	}
}