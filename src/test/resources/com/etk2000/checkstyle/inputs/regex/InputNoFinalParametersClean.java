class InputNoFinalParametersClean {
	void finalize(int x) {}

	void forEachWithFinal(java.util.List<String> list) {
		for (final var item : list)
			System.out.println(item);
	}

	void normalParams(int x, String y) {}

	void stringContainingFinal() {
		System.out.println("method(final int x)");
	}
}