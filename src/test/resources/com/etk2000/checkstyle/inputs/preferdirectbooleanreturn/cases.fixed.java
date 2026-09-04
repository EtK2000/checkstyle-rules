// === case: anonymous_class_in_cond_unbraced_parent ===
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInCondUnbracedParentSliceViolation {
	boolean m(boolean flag, Object o) {
		if (flag) {
			if (new Object() {
				@Override
				public String toString() {
					return "x";
				}
			}.equals(o))
				return true;
			else
				return true;
		}
		return false;
	}
}
// === end ===

// === case: opposite_prefix_length_exactly_max ===
class InputPreferDirectBooleanReturnOppositePrefixLengthExactlyMaxSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			return s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		throw new IllegalStateException();
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_braced_else_braced ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenBracedElseBracedSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_nested_for_in_if ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentNestedForInIfSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			for (var i = 0; i < 2; ++i)
				{
					if (s.isEmpty())
						s.isBlank();
					return true;
				}
		}
		return false;
	}
}
// === end ===

// === case: same_literal_text_block_effect_unbraced_parent ===
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnSameLiteralTextBlockEffectUnbracedParentSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			if ((s = """
					text
					""") != null)
				return true;
			else
				return true;
		}
		return false;
	}
}
// === end ===