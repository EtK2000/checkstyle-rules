package com.etk2000.checkstyle.inputs.annotation;

import javax.annotation.Nonnull;

@interface AnyThread {}

class InputAnnotationQualifiedViolation {
	@Nonnull
	@AnyThread // violation line 9
	void method() {}
}