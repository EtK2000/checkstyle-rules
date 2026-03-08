package com.etk2000.checkstyle.inputs.annotation;

class InputAnnotationQualifiedNameViolation {
	@javax.annotation.Nonnull
	@javax.annotation.CheckResult // violation line 5
	void method() {}
}