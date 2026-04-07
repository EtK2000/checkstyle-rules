package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.HashMap;
import java.util.Map;

class InputFieldSortingAnonClassViolation {
	final Map<String, Object> data = new HashMap<>();
	final Runnable action = new Runnable() { // violation: Field 'action' with anonymous class initializer must appear before 'data'.
		@Override
		public void run() {
			data.clear();
		}
	};
}

class InputFieldSortingAnonClassLambdaNotAnon {
	// lambda syntax should NOT count as anonymous class
	final Runnable action = () -> System.out.println("hello");
	final String name = "test";
}

class InputFieldSortingAnonClassLambdaDependency {
	// lambda referencing another field IS a dependency (unlike anonymous class methods)
	final Runnable action = () -> System.out.println(name); // violation: Field 'action' references 'name' which should be declared before it.
	final String name = "test";
}