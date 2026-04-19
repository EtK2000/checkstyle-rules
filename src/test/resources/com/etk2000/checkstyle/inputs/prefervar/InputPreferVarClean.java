package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

class InputPreferVarClean {
	@interface Ann {
	}

	String field = "not flagged";

	void annotatedLocalVariable() {
		@Nonnull
		final var s = "hello";
	}

	void forEach() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
		for (final var item : list)
			System.out.println(item);
	}

	void forEachAnnotated() {
		final var list = List.of("a", "b");
		for (@Nonnull var item : list)
			System.out.println(item);
		for (@Nonnull final var item : list)
			System.out.println(item);
	}

	void forEachGenericType() {
		final var map = Map.of("a", 1);
		for (var entry : map.entrySet())
			System.out.println(entry);
	}

	void forLoopInit() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void forLoopInitReferenceType() {
		final var list = List.of("a", "b");
		for (var it = list.iterator(); it.hasNext(); )
			System.out.println(it.next());
	}

	void knownParseMethodWithVar() {
		final var bp = Boolean.parseBoolean("true");
		final var byp = Byte.parseByte("5");
		final var dp = Double.parseDouble("5.0");
		final var fp = Float.parseFloat("5.0");
		final var ip = Integer.parseInt("5");
		final var lp = Long.parseLong("5");
		final var sp = Short.parseShort("5");
	}

	void literalTypeMismatchUnfixable() {
		// byte/short have no literal suffix, var would change type to int
		final byte b = 5;
		final byte bBin = 0b101;
		final byte bHex = 0xF;
		final byte bNeg = -5;
		final byte bOct = 07;
		final byte bPlus = +5;
		final byte bSep = 1_2;
		final short s = 5;
		final short sBin = 0b101;
		final short sHex = 0xFF;
		final short sNeg = -5;
		final short sOct = 077;
		final short sPlus = +5;
		final short sSep = 1_000;
		// char from int literal, var would change type to int
		final char ci = 65;
		final char ciHex = 0x41;
		// int from char literal, var would change type to char
		final int ic = 'a';
		final int icEscape = '\n';
		final int icNeg = -'a';
		final int icUnicode = '\u0041';
		// long/float/double from char literal, var would change type to char
		final double dc = 'a';
		final float fc = 'a';
		final long lc = 'a';
		// float from long literal, var would change type to long
		final float fl = 5L;
	}

	void localVariableNoInit() {
		final int x;
		final String s;
	}

	void localVariableNoInitMultiVar() {
		final int x, y;
	}

	void localVariables() {
		final var x = 42;
		final var s = "hello";
		final var list = List.of(1, 2, 3);
		final String nullStr = null;
		final int uninitialized;
		final int[] numbers = {1, 2, 3};
		final int[][] matrix = {{1, 2}, {3, 4}};
		final String[] names = {"a", "b"};
		final var sized = new String[5];
		final Runnable r = () -> System.out.println("hello");
		final Supplier<String> s2 = () -> "world";
		final Function<String, Integer> f = String::length;
		final Runnable anon = new Runnable() {
			@Override
			public void run() {
				System.out.println("anonymous");
			}
		};
		final var complexAnon = new Runnable() {
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void newAnonymousClassWithNonObjectTypeArg() {
		final var cmp = new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return 0;
			}
		};
	}

	void newWithAnnotatedObjectTypeArg() {
		final var list = new ArrayList<@Ann Object>();
	}

	void newWithConstructorArgs() {
		final var list = new ArrayList<String>(16);
		final var map = new HashMap<String, Integer>(8, 0.5f);
	}

	void newWithDiamondAlreadyUsed() {
		final var list = new ArrayList<>();
		final var map = new HashMap<>();
	}

	void newWithFirstArgObjectSecondNot() {
		final var map = new HashMap<Object, String>();
	}

	void newWithMixedTypeArgs() {
		final var map = new HashMap<String, Object>();
	}

	void newWithNonObjectQualifiedTypeArg() {
		final var list = new ArrayList<java.lang.String>();
	}

	void newWithNonObjectTypeArg() {
		final var list = new ArrayList<String>();
		final var map = new HashMap<String, Integer>();
	}

	void newWithoutTypeArgs() {
		final var obj = new Object();
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}

	void tryWithResourcesAnnotated() throws Exception {
		try (@Nonnull var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}