package com.etk2000.checkstyle.inputs.preferbulkoperation;

// === case: for_each_lambda_add_all_dotted_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllDottedTargetSliceViolation {
	List<String> other;

	void m(List<String> list) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_preserves_leading_if_statement ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPreservesLeadingIfStatementSliceViolation {
	void m(boolean flag, Map<String, String> source, Map<String, String> target) {
		if (flag)
			target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_put_all_dotted_target ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllDottedTargetSliceViolation {
	Map<String, String> target;

	void m(Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_method_ref_add_dotted_receiver ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddDottedReceiverSliceViolation {
	List<String> other;

	void m(List<String> list) {
		other.addAll(list);
	}
}
// === end ===