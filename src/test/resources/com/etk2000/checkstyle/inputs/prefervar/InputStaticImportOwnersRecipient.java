package com.etk2000.checkstyle.inputs.prefervar;

import java.util.List;

class InputStaticImportOwnersRecipient {
	void m() {
		final List<String> names = asList("a", "b");
		System.out.println(names);
	}
}