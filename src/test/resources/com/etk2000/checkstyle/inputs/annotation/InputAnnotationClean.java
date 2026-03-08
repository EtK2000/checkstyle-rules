package com.etk2000.checkstyle.inputs.annotation;

import javax.annotation.Nonnull;

@interface AnyThread {}
@interface CheckResult {}
@interface NonNull {}

class InputAnnotationClean {
	@AnyThread
	@CheckResult
	@NonNull
	void method() {}

	@CheckResult
	@Nonnull
	void importedMethod() {}

	@AnyThread
	@Nonnull
	void mixedMethod() {}

	@javax.annotation.CheckResult
	@javax.annotation.Nonnull
	void fullyQualifiedMethod() {}
}