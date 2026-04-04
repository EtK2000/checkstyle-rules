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
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class InputStandardCharsetsClean {
	static final Charset CHARSET_FIELD = StandardCharsets.UTF_8;

	void charsetField(byte[] data, File file) throws Exception {
		var a = "hello".getBytes(CHARSET_FIELD);
		var b = new String(data, CHARSET_FIELD);
		var c = new String(data, 0, 1, CHARSET_FIELD);
		var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_FIELD);
		var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_FIELD);
		var f = URLEncoder.encode("hello", CHARSET_FIELD);
		var g = URLDecoder.decode("hello", CHARSET_FIELD);
		var h = new ByteArrayOutputStream().toString(CHARSET_FIELD);
		var i = new PrintStream(file, CHARSET_FIELD);
		var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_FIELD);
		var k = new PrintWriter(file, CHARSET_FIELD);
		var l = new Scanner(new ByteArrayInputStream(data), CHARSET_FIELD);
	}

	void charsetLocal(byte[] data, File file) throws Exception {
		Charset charset = StandardCharsets.UTF_8;
		var a = "hello".getBytes(charset);
		var b = new String(data, charset);
		var c = new String(data, 0, 1, charset);
		var d = new InputStreamReader(new ByteArrayInputStream(data), charset);
		var e = new OutputStreamWriter(new ByteArrayOutputStream(), charset);
		var f = URLEncoder.encode("hello", charset);
		var g = URLDecoder.decode("hello", charset);
		var h = new ByteArrayOutputStream().toString(charset);
		var i = new PrintStream(file, charset);
		var j = new PrintStream(new ByteArrayOutputStream(), true, charset);
		var k = new PrintWriter(file, charset);
		var l = new Scanner(new ByteArrayInputStream(data), charset);
	}

	void charsetParameter(byte[] data, Charset charset, File file) throws Exception {
		var a = "hello".getBytes(charset);
		var b = new String(data, charset);
		var c = new String(data, 0, 1, charset);
		var d = new InputStreamReader(new ByteArrayInputStream(data), charset);
		var e = new OutputStreamWriter(new ByteArrayOutputStream(), charset);
		var f = URLEncoder.encode("hello", charset);
		var g = URLDecoder.decode("hello", charset);
		var h = new ByteArrayOutputStream().toString(charset);
		var i = new PrintStream(file, charset);
		var j = new PrintStream(new ByteArrayOutputStream(), true, charset);
		var k = new PrintWriter(file, charset);
		var l = new Scanner(new ByteArrayInputStream(data), charset);
	}

	void exactMatchNotAnArgument() {
		var a = "UTF-8";
		var b = "ISO-8859-1";
		var c = "US-ASCII";
	}

	void nonCharsetStringArguments() throws Exception {
		var a = "hello".getBytes("not-a-charset-name");
		var b = URLEncoder.encode("hello", "not-a-charset-name");
		var c = URLDecoder.decode("%20", "not-a-charset-name");
	}

	void standardCharsetsConstants(byte[] data, File file) throws Exception {
		var a = "hello".getBytes(StandardCharsets.UTF_8);
		var b = new String(data, StandardCharsets.UTF_8);
		var c = new String(data, 0, 1, StandardCharsets.UTF_8);
		var d = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		var e = new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
		var f = URLEncoder.encode("hello", StandardCharsets.UTF_8);
		var g = URLDecoder.decode("hello", StandardCharsets.UTF_8);
		var h = new ByteArrayOutputStream().toString(StandardCharsets.UTF_8);
		var i = new PrintStream(file, StandardCharsets.UTF_8);
		var j = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
		var k = new PrintWriter(file, StandardCharsets.UTF_8);
		var l = new Scanner(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
	}

	void standardCharsetsOther(byte[] data) throws Exception {
		var a = "test".getBytes(StandardCharsets.ISO_8859_1);
		var b = "test".getBytes(StandardCharsets.US_ASCII);
		var c = "test".getBytes(StandardCharsets.UTF_16);
		var d = "test".getBytes(StandardCharsets.UTF_16BE);
		var e = "test".getBytes(StandardCharsets.UTF_16LE);
		var f = new String(data, StandardCharsets.ISO_8859_1);
		var g = new String(data, StandardCharsets.US_ASCII);
	}

	void stringsContainingCharsetNames() {
		var a = "encoding is UTF-8";
		var b = "charset=UTF-8";
		var c = "Content-Type: text/html; charset=ISO-8859-1";
		var d = "UTF-8 is the default encoding";
		var e = "converted from US-ASCII to UTF-16";
		var f = "uses latin1";
		var g = "ASCII art";
		var h = "UTF-16BE big-endian";
		var i = "UTF-16LE little-endian";
		var j = "UTF8 without hyphen";
	}
}