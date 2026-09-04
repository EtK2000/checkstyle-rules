package com.etk2000.checkstyle;

import java.io.Reader;
import java.io.StringReader;

public class InputLinkageProbeProtectedOverloadApi {
	void hidden(InputLinkageProbeMissing missing) {
	}

	public Reader pick(Object value) {
		return new StringReader("");
	}

	protected StringReader pick(String value) {
		return new StringReader(value);
	}
}