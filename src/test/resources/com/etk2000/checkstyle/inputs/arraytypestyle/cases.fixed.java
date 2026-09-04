// === case: annotated_base_dimension_lambda_param ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
// imports: java.util.function.BiConsumer
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionLambdaParamSliceViolation {
	@interface A {}

	BiConsumer<int[][], Integer> b = (a, y) -> {};
}
// === end ===

// === case: for_loop_single_var_fixed ===
@SuppressWarnings("unused")
class InputArrayTypeStyleForLoopSingleVarFixedSliceViolation {
	void m(int[] a, int step) {
		for (var x = a; x.length < 10; step += 1)
			break;
	}
}
// === end ===

// === case: generic_method_multi_param_lambda ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaSliceViolation {
	BiConsumer<X, Y> b = (a, y) -> {};
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_last ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnLastSliceViolation {
	BiConsumer<X, Y> b = (a, y) -> {};
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_middle ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnMiddleSliceViolation {
	TriConsumer<X, Y, Z> t = (a, b, c) -> {};
}
// === end ===