package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.ArrayList;
import java.util.List;

class InputCollectionInterfaceTypeGraphLeakB {
	static class Base {
		void dump(List<String> values) {
			System.out.println(values);
		}
	}

	static class Sub extends Base {
		void dump(ArrayList<String> values) {
			System.out.println(values);
		}
	}
}