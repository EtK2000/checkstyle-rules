package com.etk2000.checkstyle.inputs.annotation;

@interface AnyThread {}
@interface CheckResult {}
@interface NonNull {}

class InputAnnotationViolation {
	@NonNull
	@CheckResult
	@AnyThread
	void method() {}
}