package com.etk2000.checkstyle.inputs.preferspecificapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shapes for PreferSpecificApiLocatorTest. Each method holds exactly one statement so a test can
 * name the method instead of hard-coding a line.
 */
public class InputLocatorPositions {
	ArrayList<String> al;
	List<String> fieldInitializer = Collections.emptyList();
	List<String> list, other;
	Map<String, String> map;
	Object[] used;
	String s;

	public long commentInsideChain() {
		return list.stream()/*c*/.count();
	}

	public void elistBorrowsOperatorColumn() {
		use(list.size() == 0, other);
	}

	public void exprBorrowsLparenColumn() {
		use(list.get(0));
	}

	public boolean exprBorrowsOperatorColumn() {
		return list.size() == 0;
	}

	public String getSizeMinusOne() {
		return al.get(al.size() - 1);
	}

	public String getZero() {
		return al.get(0);
	}

	public boolean indexOfSingleCharComparison() {
		return s.indexOf("x") != -1;
	}

	public void otherIndicesSuppressed() {
		use(al.get(0), al.get(2));
	}

	public boolean plainLengthComparison() {
		return s.length() == 0;
	}

	public void removeSizeMinusOne() {
		al.remove(al.size() - 1);
	}

	public void removeZero() {
		al.remove(0);
	}

	public String stringFormatOneArgument() {
		return String.format(s);
	}

	public String stringFormatTwoArguments() {
		return String.format("%s", s);
	}

	public String supplementaryBeforeGetZero() {
		return s.concat("𝐀") + al.get(0);
	}

	public boolean trimLengthComparison() {
		return s.trim().length() == 0;
	}

	public List<String> unmodifiableOfAsList() {
		return Collections.unmodifiableList(Arrays.asList(s));
	}

	public List<String> unmodifiableOfVariable() {
		return Collections.unmodifiableList(list);
	}

	public void use(Object... values) {
		used = values;
	}
}