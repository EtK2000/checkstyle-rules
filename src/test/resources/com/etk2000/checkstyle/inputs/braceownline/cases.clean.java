package com.etk2000.checkstyle.inputs.braceownline;

import java.util.List;
import java.util.function.Function;

class InputBraceOwnLineClean {
	abstract static class AbstractMethodNoBody {
		abstract void allman(int x);
	}

	enum InstanceInitializerAfterEnumConstants {
		A,
		B;

		int field;

		{
			field = 1;
		}
	}

	static class InstanceInitializerAfterComment {
		int field;

		// set before any constructor runs
		{
			field = 1;
		}
	}

	static class InstanceInitializerAfterField {
		int field;

		{
			field = 1;
		}
	}

	interface InterfaceMethodNoBody {
		void allman(int x);
	}

	@SuppressWarnings(
			{
					"unchecked",
					"unused"
			}
	)
	void annotationArrayValueBraceOwnLine() {
		System.out.println("annotated");
	}

	int[] arrayCreationInitializerBraceOwnLine() {
		return new int[]
		{
			1,
			2
		};
	}

	void arrayInitializerBraceOwnLine() {
		final int[] values =
		{
			1,
			2
		};
		System.out.println(values.length);
	}

	void bareBlockAfterBracelessIfBody(int x) {
		if (x > 0)
			--x;
		{
			final var y = x;
			System.out.println(y);
		}
	}

	void bareBlockAfterStatement(int x) {
		--x;
		{
			final var y = x;
			System.out.println(y);
		}
	}

	void caseGroupBraceOwnLine(int x) {
		switch (x) {
			case 1:
			{
				final var y = x;
				System.out.println(y);
			}
			case 2:
				--x;
				break;

			default:
				System.out.println(x);
		}
	}

	void commentContainingAllman(int x) {
		// if (x > 0)
		// {
		System.out.println(x);
	}

	void headerLineBraceControl(int x, int y, List<String> list) throws InterruptedException {
		if (x > 0) {
			--x;
			++y;
		}
		else if (x < 0) {
			++x;
			--y;
		}
		else {
			--y;
			++y;
		}
		for (var i = 0; i < x; ++i) {
			System.out.println(i);
			System.out.println(y);
		}
		for (var item : list) {
			System.out.println(item);
			System.out.println(x);
		}
		while (x > 0) {
			--x;
			++y;
		}
		do {
			--x;
			++y;
		}
		while (x > 0);
		try {
			Thread.sleep(1);
		}
		catch (IllegalStateException e) {
			System.out.println(e);
		}
		finally {
			System.out.println(x);
		}
		switch (x) {
			case 1 -> {
				--x;
				++y;
			}
			default -> ++x;
		}
		synchronized (this) {
			--x;
		}
		check: {
			if (x > 0)
				break check;
			--x;
		}
	}

	void headerLineBraceDeclarations() {
		class Local {
			int a;
		}

		final Runnable r = () -> {
			System.out.println("a");
			System.out.println(new Local().a);
		};
		final Object o = new Object() {
			@Override
			public String toString() {
				return "";
			}
		};
		System.out.println(r);
		System.out.println(o);
	}

	void instanceInitializerInAnonymousClass() {
		final var o = new Object() {
			final int field;

			{
				field = 1;
			}
		};
		System.out.println(o);
	}

	void lambdaExpressionBodyNextLine() {
		final Function<Integer, Integer> f = v ->
				v + 1;
		System.out.println(f.apply(1));
	}

	void singleStatementCommentBeforeBraceUnwrapWins(int x) {
		if (x > 0)
			// only when positive
		{
			--x;
		}
	}

	void singleStatementDoUnwrapWins(int x, int y) {
		do
		{
			--x;
		}
		while (x > 0);
		do
		{
			x = x + y;
		}
		while (x < 100);
		do
		{
			--x;
		} while (x > 0);
		do
			// note
		{
			--x;
		}
		while (x > 0);
	}

	void singleStatementUnwrapWins(int x, List<String> list) {
		if (x > 0)
		{
			--x;
		}
		else
		{
			++x;
		}
		if (x > 0) {
			--x;
			++x;
		} else
		{
			++x;
		}
		for (var i = 0; i < x; ++i)
		{
			System.out.println(i);
		}
		for (var item : list)
		{
			System.out.println(item);
		}
		while (x > 0)
		{
			--x;
		}
	}

	void textBlockContainingAllmanAndElseIf() {
		final var s = """
				if (x > 0)
				{
				}
				else
					if (y)
				""";
		System.out.println(s);
	}
}