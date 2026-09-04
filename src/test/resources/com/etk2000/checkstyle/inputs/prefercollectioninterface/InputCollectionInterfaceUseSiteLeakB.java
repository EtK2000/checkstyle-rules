package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.ArrayList;

class InputCollectionInterfaceUseSiteLeakB {
	private final ArrayList<String> pinned = rows();

	private ArrayList<String> rows() {
		return null;
	}

	void use() {
		System.out.println(pinned);
	}
}