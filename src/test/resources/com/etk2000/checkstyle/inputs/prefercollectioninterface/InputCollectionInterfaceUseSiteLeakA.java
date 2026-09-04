package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.ArrayList;

class InputCollectionInterfaceUseSiteLeakA {
	private ArrayList<String> rows() {
		return null;
	}

	void use() {
		System.out.println(rows());
	}
}