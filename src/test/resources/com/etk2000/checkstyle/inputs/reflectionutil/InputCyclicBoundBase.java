package com.etk2000.checkstyle;

public class InputCyclicBoundBase<T extends U, U> {
	public String put(T value) {
		return "base";
	}
}