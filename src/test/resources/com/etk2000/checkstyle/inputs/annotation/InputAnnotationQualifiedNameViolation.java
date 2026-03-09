package com.etk2000.checkstyle.inputs.annotation;

class InputAnnotationQualifiedNameViolation {
	@javax.annotation.Nonnull
	@javax.annotation.CheckResult // violation: annotations not in alphabetical order
	void method() {}
}