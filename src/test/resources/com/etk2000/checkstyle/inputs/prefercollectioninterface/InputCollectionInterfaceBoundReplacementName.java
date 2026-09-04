package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.awt.List;
import java.util.ArrayList;

class InputCollectionInterfaceBoundReplacementName {
	void dump(ArrayList<String> values) {
		System.out.println(values);
		System.out.println(new List());
	}
}