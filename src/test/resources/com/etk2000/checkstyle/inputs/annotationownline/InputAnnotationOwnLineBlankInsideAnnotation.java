package com.etk2000.checkstyle.inputs.annotationownline;

@interface V {
	String[] value();
}

class InputAnnotationOwnLineBlankInsideAnnotation {
	@V({

		"a"
	})
	void multiLineBlankInsideAnnotation() {}
}