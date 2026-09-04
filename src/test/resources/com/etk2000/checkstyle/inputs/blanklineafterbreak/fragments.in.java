// === case: already_has_blank_line ===
// target: col=0
		break;

		case 2:
// === end ===

// === case: insert_blank_before_case ===
// target: col=0
		break;
		case 2:
// === end ===

// === case: insert_blank_before_default ===
// target: col=0
		break;
		default:
// === end ===

// === case: insert_blank_below_text_block ===
// target: line=7 col=0
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

// === case: no_next_line ===
// target: col=0
		break;
// === end ===

// === case: skip_break_inside_text_block ===
// target: line=2 col=0
class T {
	String code = """
			break;
			case 2:
		""";
}
// === end ===