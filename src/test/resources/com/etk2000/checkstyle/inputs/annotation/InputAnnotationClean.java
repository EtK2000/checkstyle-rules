package com.etk2000.checkstyle.inputs.annotation;

import javax.annotation.Nonnull;

@interface AnyThread {}
@interface CheckResult {}
@interface NonNull {}

class InputAnnotationClean {
	@javax.annotation.CheckResult
	@javax.annotation.Nonnull
	void fullyQualifiedMethod() {}

	@CheckResult
	@Nonnull
	void importedMethod() {}

	@AnyThread
	@CheckResult
	@NonNull
	void method() {}

	@AnyThread
	@Nonnull
	void mixedMethod() {}
}