package com.etk2000.checkstyle.inputs.prefervar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;

class InputPreferVarDiamondViolation {
	void anonymousClassWithObjectTypeArg() {
		final var cmp = new Comparator<Object>() { // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
			@Override
			public int compare(Object a, Object b) {
				return 0;
			}
		};
	}

	void constructorArgsWithObjectTypeArg() {
		final var list = new ArrayList<Object>(16); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var map = new HashMap<Object, Object>(8, 0.5f); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}

	void mixedQualifiedAndBareObjectTypeArgs() {
		final var map = new HashMap<Object, java.lang.Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}

	void multipleObjectTypeArgs() {
		final var map = new HashMap<Object, Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}

	void qualifiedConstructorName() {
		final var list = new java.util.ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}

	void qualifiedObjectTypeArg() {
		final var list = new ArrayList<java.lang.Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}

	void singleObjectTypeArg() {
		final var list = new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var set = new LinkedHashSet<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}