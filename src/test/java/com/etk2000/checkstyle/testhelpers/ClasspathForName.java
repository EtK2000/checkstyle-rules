package com.etk2000.checkstyle.testhelpers;

/** Test helper whose {@code forName} is not {@code Charset.forName}, for a variable named {@code Charset} to hold. */
public class ClasspathForName {
	public Object forName(String name) {
		return name;
	}
}