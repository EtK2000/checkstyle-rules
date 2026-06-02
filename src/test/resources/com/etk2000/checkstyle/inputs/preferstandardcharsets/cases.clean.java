package com.etk2000.checkstyle.inputs.preferstandardcharsets;

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
		final var a = "hello".getBytes(CHARSET_FIELD);
		final var b = new String(data, CHARSET_FIELD);
		final var c = new String(data, 0, 1, CHARSET_FIELD);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_FIELD);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_FIELD);
		final var f = URLEncoder.encode("hello", CHARSET_FIELD);
		final var g = URLDecoder.decode("hello", CHARSET_FIELD);
		final var h = new ByteArrayOutputStream().toString(CHARSET_FIELD);
		final var i = new PrintStream(file, CHARSET_FIELD);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_FIELD);
		final var k = new PrintWriter(file, CHARSET_FIELD);
		final var l = new Scanner(new ByteArrayInputStream(data), CHARSET_FIELD);
	}

	void charsetLocal(byte[] data, File file) throws Exception {
		final Charset charset = StandardCharsets.UTF_8;
		final var a = "hello".getBytes(charset);
		final var b = new String(data, charset);
		final var c = new String(data, 0, 1, charset);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), charset);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), charset);
		final var f = URLEncoder.encode("hello", charset);
		final var g = URLDecoder.decode("hello", charset);
		final var h = new ByteArrayOutputStream().toString(charset);
		final var i = new PrintStream(file, charset);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, charset);
		final var k = new PrintWriter(file, charset);
		final var l = new Scanner(new ByteArrayInputStream(data), charset);
	}

	void charsetParameter(byte[] data, Charset charset, File file) throws Exception {
		final var a = "hello".getBytes(charset);
		final var b = new String(data, charset);
		final var c = new String(data, 0, 1, charset);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), charset);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), charset);
		final var f = URLEncoder.encode("hello", charset);
		final var g = URLDecoder.decode("hello", charset);
		final var h = new ByteArrayOutputStream().toString(charset);
		final var i = new PrintStream(file, charset);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, charset);
		final var k = new PrintWriter(file, charset);
		final var l = new Scanner(new ByteArrayInputStream(data), charset);
	}

	void exactMatchNotAnArgument() {
		final var a = "UTF-8";
		final var b = "ISO-8859-1";
		final var c = "US-ASCII";
	}

	void nonCharsetStringArguments() throws Exception {
		final var a = "hello".getBytes("not-a-charset-name");
		final var b = URLEncoder.encode("hello", "not-a-charset-name");
		final var c = URLDecoder.decode("%20", "not-a-charset-name");
	}

	void nonCharsetStringArrayReceiver() {
		final String[] arr = {"UTF-8"};
		final java.lang.String[] qArr = {"UTF-8"};
		final var a = arr.toString();
		final var b = qArr.toString();
	}

	void nonStringReceivers(Object obj, java.lang.Object qObj) {
		obj.toString();
		qObj.toString();
	}

	void standardCharsetsConstants(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(StandardCharsets.UTF_8);
		final var b = new String(data, StandardCharsets.UTF_8);
		final var c = new String(data, 0, 1, StandardCharsets.UTF_8);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
		final var f = URLEncoder.encode("hello", StandardCharsets.UTF_8);
		final var g = URLDecoder.decode("hello", StandardCharsets.UTF_8);
		final var h = new ByteArrayOutputStream().toString(StandardCharsets.UTF_8);
		final var i = new PrintStream(file, StandardCharsets.UTF_8);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
		final var k = new PrintWriter(file, StandardCharsets.UTF_8);
		final var l = new Scanner(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		final var m = new <String>String(data, StandardCharsets.UTF_8);
		final var n = new <String>ByteArrayOutputStream().toString(StandardCharsets.UTF_8);
	}

	void standardCharsetsOther(byte[] data) throws Exception {
		final var a = "test".getBytes(StandardCharsets.ISO_8859_1);
		final var b = "test".getBytes(StandardCharsets.US_ASCII);
		final var c = "test".getBytes(StandardCharsets.UTF_16);
		final var d = "test".getBytes(StandardCharsets.UTF_16BE);
		final var e = "test".getBytes(StandardCharsets.UTF_16LE);
		final var f = new String(data, StandardCharsets.ISO_8859_1);
		final var g = new String(data, StandardCharsets.US_ASCII);
	}

	void stringsContainingCharsetNames() {
		final var a = "encoding is UTF-8";
		final var b = "charset=UTF-8";
		final var c = "Content-Type: text/html; charset=ISO-8859-1";
		final var d = "UTF-8 is the default encoding";
		final var e = "converted from US-ASCII to UTF-16";
		final var f = "uses latin1";
		final var g = "ASCII art";
		final var h = "UTF-16BE big-endian";
		final var i = "UTF-16LE little-endian";
		final var j = "UTF8 without hyphen";
	}
}