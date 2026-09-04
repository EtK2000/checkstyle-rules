package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.ArrayList;

class InputCollectionInterfaceTypeGraphLeakA {
	void dump(ArrayList<String> values) {
		System.out.println(values);
	}
}