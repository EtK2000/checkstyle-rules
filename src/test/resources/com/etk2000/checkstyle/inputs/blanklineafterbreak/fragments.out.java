// === case: insert_blank_before_case ===
		break;

		case 2:
// === end ===

// === case: insert_blank_before_default ===
		break;

		default:
// === end ===

// === case: insert_blank_below_text_block ===
class T {
	String s = """
		x
		""";
	void f(int i) {
		switch (i) {
			case 1:
				break;

			case 2:
				break;
		}
	}
}
// === end ===