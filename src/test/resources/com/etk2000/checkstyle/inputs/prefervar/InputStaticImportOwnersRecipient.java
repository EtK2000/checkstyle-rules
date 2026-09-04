package com.etk2000.checkstyle.inputs.prefervar;

class InputStaticImportOwnersRecipient {
	void m() {
		final String text = valueOf(1);
		System.out.println(text);
	}
}