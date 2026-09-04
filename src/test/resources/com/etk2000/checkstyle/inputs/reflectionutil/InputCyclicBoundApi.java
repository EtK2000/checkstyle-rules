package com.etk2000.checkstyle;

public class InputCyclicBoundApi<T extends U, U> {
	public String put(T value) {
		return "api";
	}
}