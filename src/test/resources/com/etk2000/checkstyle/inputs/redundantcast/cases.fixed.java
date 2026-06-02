// === case: same_type_cast_field ===
class InputRedundantCastSameTypeFieldRefSliceViolation {
	int sameField;

	void m() {
		final var x = sameField;
	}
}
// === end ===

// === case: same_type_cast_foreach ===
// imports: java.util.List
class InputRedundantCastSameTypeForeachSliceViolation {
	void m(List<String> list) {
		for (var s : list)
			System.out.println(s);
	}
}
// === end ===

// === case: same_type_cast_forinit ===
class InputRedundantCastSameTypeForInitSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: same_type_cast_parameter ===
class InputRedundantCastSameTypeParameterSliceViolation {
	void m(String s) {
		final var x = s;
	}
}
// === end ===

// === case: same_type_cast_variable_int ===
class InputRedundantCastSameTypeVarIntSliceViolation {
	void m() {
		final var x = 5;
		final var y = x;
	}
}
// === end ===

// === case: same_type_cast_variable_string ===
class InputRedundantCastSameTypeVarStringSliceViolation {
	void m() {
		final var s = "hi";
		final var t = s;
	}
}
// === end ===

// === case: same_type_field_long ===
class InputRedundantCastSameTypeLongFieldSliceViolation {
	long x = 5;
}
// === end ===

// === case: same_type_nested_cast ===
class InputRedundantCastSameTypeNestedSliceViolation {
	void m(Object obj) {
		final var s = (String) obj;
	}
}
// === end ===

// === case: sibling_double_literal ===
class InputRedundantCastSiblingDoubleSliceViolation {
	void m() {
		final var x = 5;
		final double d = x * 1.5;
	}
}
// === end ===

// === case: sibling_float_literal ===
class InputRedundantCastSiblingFloatSliceViolation {
	void m() {
		final var x = 5;
		final float d = x * 1.5f;
	}
}
// === end ===

// === case: sibling_long_literal ===
class InputRedundantCastSiblingLongLiteralSliceViolation {
	void m() {
		final var x = 5;
		final long y = x * 100L;
	}
}
// === end ===

// === case: sibling_long_variable ===
class InputRedundantCastSiblingLongVarSliceViolation {
	void m() {
		final var x = 5;
		final long y = 10;
		final long z = x * y;
	}
}
// === end ===

// === case: sibling_wider_comparison ===
class InputRedundantCastSiblingComparisonSliceViolation {
	void m() {
		final var x = 5;
		final long y = 10;
		final boolean b = x == y;
	}
}
// === end ===

// === case: ternary_false_branch ===
class InputRedundantCastTernaryFalseBranchSliceViolation {
	void m(boolean flag) {
		final var x = 5;
		final long y = flag ? 0 : x;
	}
}
// === end ===

// === case: ternary_return ===
class InputRedundantCastTernaryReturnSliceViolation {
	long m(boolean flag) {
		final var x = 5;
		return flag ? x : 0;
	}
}
// === end ===

// === case: ternary_true_branch ===
class InputRedundantCastTernaryTrueBranchSliceViolation {
	void m(boolean flag) {
		final var x = 5;
		final long y = flag ? x : 0;
	}
}
// === end ===

// === case: ternary_with_long_reassignment ===
class InputRedundantCastTernaryLongReassignSliceViolation {
	void m(boolean flag) {
		final var x = 5;
		final var z = 10;
		long y = 0;
		y = flag ? x : z;
	}
}
// === end ===

// === case: ternary_with_long_target ===
class InputRedundantCastTernaryLongTargetSliceViolation {
	void m(boolean flag) {
		final var x = 5;
		final var z = 10;
		final long y = flag ? x : z;
	}
}
// === end ===

// === case: widening_byte_to_int ===
class InputRedundantCastWideningByteToIntSliceViolation {
	void m() {
		final byte b = 5;
		final var x = b;
	}
}
// === end ===

// === case: widening_byte_to_short ===
class InputRedundantCastWideningByteToShortSliceViolation {
	void m() {
		final byte b = 5;
		final var x = b;
	}
}
// === end ===

// === case: widening_char_to_int ===
class InputRedundantCastWideningCharToIntSliceViolation {
	void m() {
		final var c = 'a';
		final var x = c;
	}
}
// === end ===

// === case: widening_float_to_double ===
class InputRedundantCastWideningFloatToDoubleSliceViolation {
	void m() {
		final var f = 5.0f;
		final var x = f;
	}
}
// === end ===

// === case: widening_in_return ===
class InputRedundantCastWideningInReturnSliceViolation {
	long m() {
		final var x = 5;
		return x;
	}
}
// === end ===

// === case: widening_in_return_wider ===
class InputRedundantCastWideningInReturnWiderSliceViolation {
	double m() {
		final var x = 5;
		return x;
	}
}
// === end ===

// === case: widening_in_standalone_assign ===
class InputRedundantCastWideningStandaloneAssignSliceViolation {
	void m() {
		final var x = 5;
		long y = 0;
		y = x;
	}
}
// === end ===

// === case: widening_int_to_double ===
class InputRedundantCastWideningIntToDoubleSliceViolation {
	void m() {
		final var x = 5;
		final var x = x;
	}
}
// === end ===

// === case: widening_int_to_float ===
class InputRedundantCastWideningIntToFloatSliceViolation {
	void m() {
		final var x = 5;
		final var x = x;
	}
}
// === end ===

// === case: widening_int_to_long ===
class InputRedundantCastWideningIntToLongSliceViolation {
	void m() {
		final var x = 5;
		final var x = x;
	}
}
// === end ===

// === case: widening_long_to_double ===
class InputRedundantCastWideningLongToDoubleSliceViolation {
	void m() {
		final var x = 5L;
		final var d = x;
	}
}
// === end ===

// === case: widening_long_to_float ===
class InputRedundantCastWideningLongToFloatSliceViolation {
	void m() {
		final var x = 5L;
		final var f = x;
	}
}
// === end ===

// === case: widening_on_parameter ===
class InputRedundantCastWideningOnParameterSliceViolation {
	void m(int x) {
		final var y = x;
	}
}
// === end ===

// === case: widening_short_to_int ===
class InputRedundantCastWideningShortToIntSliceViolation {
	void m() {
		final short s = 5;
		final var x = s;
	}
}
// === end ===

// === case: widening_short_to_long ===
class InputRedundantCastWideningShortToLongSliceViolation {
	void m() {
		final short s = 5;
		final var x = s;
	}
}
// === end ===