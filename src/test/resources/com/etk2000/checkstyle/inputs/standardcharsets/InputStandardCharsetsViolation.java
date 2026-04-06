package com.etk2000.checkstyle.inputs.standardcharsets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Scanner;

class InputStandardCharsetsViolation {
	static final String CHARSET_NAME = "UTF-8";

	void builtinFunctions(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes("UTF-8"); // violation: use StandardCharsets.UTF_8
		final var b = new String(data, "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var c = new String(data, 0, 1, "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var d = new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var f = Charset.forName("UTF-8"); // violation: use StandardCharsets.UTF_8
		final var g = URLEncoder.encode("hello", "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var h = URLDecoder.decode("hello", "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var i = new ByteArrayOutputStream().toString("UTF-8"); // violation: use StandardCharsets.UTF_8
		final var j = new PrintStream(file, "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var k = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var l = new PrintWriter(file, "UTF-8"); // violation: use StandardCharsets.UTF_8
		final var m = new Scanner(new ByteArrayInputStream(data), "UTF-8"); // violation: use StandardCharsets.UTF_8
	}

	void otherCharsets() throws Exception {
		final var a = "test".getBytes("ISO-8859-1"); // violation: use StandardCharsets.ISO_8859_1
		final var b = "test".getBytes("latin1"); // violation: use StandardCharsets.ISO_8859_1
		final var c = "test".getBytes("US-ASCII"); // violation: use StandardCharsets.US_ASCII
		final var d = "test".getBytes("ASCII"); // violation: use StandardCharsets.US_ASCII
		final var e = "test".getBytes("UTF-16"); // violation: use StandardCharsets.UTF_16
		final var f = "test".getBytes("UTF-16BE"); // violation: use StandardCharsets.UTF_16BE
		final var g = "test".getBytes("UTF-16LE"); // violation: use StandardCharsets.UTF_16LE
	}

	void stringField(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(CHARSET_NAME); // violation: use StandardCharsets constant
		final var b = new String(data, CHARSET_NAME); // violation: use StandardCharsets constant
		final var c = new String(data, 0, 1, CHARSET_NAME); // violation: use StandardCharsets constant
		final var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_NAME); // violation: use StandardCharsets constant
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_NAME); // violation: use StandardCharsets constant
		final var f = URLEncoder.encode("hello", CHARSET_NAME); // violation: use StandardCharsets constant
		final var g = URLDecoder.decode("hello", CHARSET_NAME); // violation: use StandardCharsets constant
		final var h = new ByteArrayOutputStream().toString(CHARSET_NAME); // violation: use StandardCharsets constant
		final var i = new PrintStream(file, CHARSET_NAME); // violation: use StandardCharsets constant
		final var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_NAME); // violation: use StandardCharsets constant
		final var k = new PrintWriter(file, CHARSET_NAME); // violation: use StandardCharsets constant
		final var l = new Scanner(new ByteArrayInputStream(data), CHARSET_NAME); // violation: use StandardCharsets constant
	}

	void stringLocal(byte[] data, File file) throws Exception {
		final String encoding = "UTF-8";
		final var a = "hello".getBytes(encoding); // violation: use StandardCharsets constant
		final var b = new String(data, encoding); // violation: use StandardCharsets constant
		final var c = new String(data, 0, 1, encoding); // violation: use StandardCharsets constant
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding); // violation: use StandardCharsets constant
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding); // violation: use StandardCharsets constant
		final var f = URLEncoder.encode("hello", encoding); // violation: use StandardCharsets constant
		final var g = URLDecoder.decode("hello", encoding); // violation: use StandardCharsets constant
		final var h = new ByteArrayOutputStream().toString(encoding); // violation: use StandardCharsets constant
		final var i = new PrintStream(file, encoding); // violation: use StandardCharsets constant
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding); // violation: use StandardCharsets constant
		final var k = new PrintWriter(file, encoding); // violation: use StandardCharsets constant
		final var l = new Scanner(new ByteArrayInputStream(data), encoding); // violation: use StandardCharsets constant
	}

	void stringParameter(byte[] data, String encoding, File file) throws Exception {
		final var a = "hello".getBytes(encoding); // violation: use StandardCharsets constant
		final var b = new String(data, encoding); // violation: use StandardCharsets constant
		final var c = new String(data, 0, 1, encoding); // violation: use StandardCharsets constant
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding); // violation: use StandardCharsets constant
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding); // violation: use StandardCharsets constant
		final var f = URLEncoder.encode("hello", encoding); // violation: use StandardCharsets constant
		final var g = URLDecoder.decode("hello", encoding); // violation: use StandardCharsets constant
		final var h = new ByteArrayOutputStream().toString(encoding); // violation: use StandardCharsets constant
		final var i = new PrintStream(file, encoding); // violation: use StandardCharsets constant
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding); // violation: use StandardCharsets constant
		final var k = new PrintWriter(file, encoding); // violation: use StandardCharsets constant
		final var l = new Scanner(new ByteArrayInputStream(data), encoding); // violation: use StandardCharsets constant
	}

	void utf8Aliases(byte[] data) throws Exception {
		final var a = "hello".getBytes("UTF-8"); // violation: use StandardCharsets.UTF_8
		final var b = new String(data, "utf-8"); // violation: use StandardCharsets.UTF_8
		final var c = "test".getBytes("UTF8"); // violation: use StandardCharsets.UTF_8
		final var d = new String(data, "utf8"); // violation: use StandardCharsets.UTF_8
		final var e = new String(data, "Utf-8"); // violation: use StandardCharsets.UTF_8
	}
}