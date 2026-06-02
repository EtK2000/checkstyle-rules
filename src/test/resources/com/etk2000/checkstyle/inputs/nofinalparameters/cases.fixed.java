// === case: main ===
// imports: java.util.List
class InputNoFinalParametersViolation {
	void bothFinal(int x, String y) {}

	void lambdaWithFinal(List<String> list) {
		list.sort((a, b) -> a.compareTo(b));
	}
}
// === end ===