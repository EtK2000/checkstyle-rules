package com.etk2000.checkstyle.inputs.braceownline;

import java.io.IOException;

class InputLocatorPositions {
	record R(int a) {}

	int field;

	{
		field = 1;
	}

	void lambda() {
		final Runnable r = () ->
		{
			System.out.println("a");
			System.out.println("b");
		};
		r.run();
	}

	void multiLineHeader(boolean a, boolean b) {
		if (a
				&& b)
		{
			System.out.println(a);
			System.out.println(b);
		}
	}

	void throwsNextLine()
			throws IOException
	{
		throw new IOException();
	}
}