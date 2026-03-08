package com.etk2000.checkstyle.inputs.annotation;

@interface NonNull {}

class InputAnnotationSingleAnnotation {
	@NonNull
	void method() {}
}