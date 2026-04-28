package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

class InputJitInefficiencyStructuralViolation {
	private final Pattern instancePattern;

	InputJitInefficiencyStructuralViolation(String s) {
		final var p = Pattern.compile("\\d+"); // violation: 'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.
		this.instancePattern = p;
		System.out.println(s);
	}

	void doubleBraceList() {
		final var list = new ArrayList<String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			add("a");
			add("b");
		}};
		System.out.println(list);
	}

	void doubleBraceMap() {
		final var map = new HashMap<String, String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			put("k", "v");
		}};
		System.out.println(map);
	}

	void reusableDateTimeFormatter(Date d) {
		final var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // violation: 'DateTimeFormatter.ofPattern(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt + " " + d);
	}

	void reusablePatternCompile(String s) {
		final var matched = Pattern.compile("\\d+").matcher(s).matches(); // violation: 'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(matched);
	}

	void reusableSimpleDateFormat(Date d) {
		final var fmt = new SimpleDateFormat("yyyy-MM-dd"); // violation: 'new SimpleDateFormat(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt.format(d));
	}

	void reusableDecimalFormat(double n) {
		final var fmt = new DecimalFormat("#,##0.00"); // violation: 'new DecimalFormat(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt.format(n));
	}

	void doubleBraceFqn() {
		final var list = new java.util.ArrayList<String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			add("a");
		}};
		System.out.println(list);
	}
}