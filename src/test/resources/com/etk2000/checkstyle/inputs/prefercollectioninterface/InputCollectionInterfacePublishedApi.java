package com.etk2000.checkstyle.inputs.prefercollectioninterface;

import java.util.ArrayList;
import java.util.HashMap;

public class InputCollectionInterfacePublishedApi {
	public static class Nested {
		public ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
			return null;
		}
	}

	public interface Holder {
		class Member {
			public ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
				return null;
			}
		}
	}

	public interface Rows {
		ArrayList<String> all(); // violation (warning): Use 'List' instead of 'ArrayList'.
	}

	protected void consume(HashMap<String, Integer> lookup) { // violation (warning): Use 'Map' instead of 'HashMap'.
		System.out.println(lookup);
	}

	public ArrayList<String> rows() { // violation (warning): Use 'List' instead of 'ArrayList'.
		return null;
	}

	public void store(ArrayList<String> values) { // violation (warning): Use 'List' instead of 'ArrayList'.
		System.out.println(values);
	}
}