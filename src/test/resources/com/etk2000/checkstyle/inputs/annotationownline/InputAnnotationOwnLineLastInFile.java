package com.etk2000.checkstyle.inputs.annotationownline;

@interface V {
	String[] value();
}

class InputAnnotationOwnLineLastInFile {
	@V({
		"a"
	}) int field;}