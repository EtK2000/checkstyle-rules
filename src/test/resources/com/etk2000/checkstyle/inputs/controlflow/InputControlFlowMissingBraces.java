package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowMissingBraces {
	void elseMethod(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}

	void method(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);

		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;

		for (int i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 0)
				System.out.println(i);

		final var list = List.of("a");
		for (var item : list) // violation: Braceless control flow has multi-line body, add braces.
			if (item != null)
				System.out.println(item);

		do // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0)
				--x;
		while (x > 0);
	}
}