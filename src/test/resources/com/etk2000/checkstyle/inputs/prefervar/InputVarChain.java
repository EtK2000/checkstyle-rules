package com.etk2000.checkstyle.inputs.prefervar;

class InputVarChain {
	static class Node {
	}

	void m() {
		final var a0 = new Node();
		final var a1 = a0;
		final var chained = a1;
		System.out.println(chained);
	}
}