package com.etk2000.checkstyle.inputs.preferstandardcharsets;

// === case: builtin_functions ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.nio.charset.Charset
// imports: java.util.Scanner
class InputStandardCharsetsBuiltinFunctionsSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var b = new String(data, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var c = new String(data, 0, 1, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var d = new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var f = Charset.forName("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var g = URLEncoder.encode("hello", "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var h = URLDecoder.decode("hello", "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var i = new ByteArrayOutputStream().toString("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var j = new PrintStream(file, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var k = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var l = new PrintWriter(file, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var m = new Scanner(new ByteArrayInputStream(data), "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var n = new <String>String(data, "UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
		final var o = new <String>ByteArrayOutputStream().toString("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
	}
}
// === end ===

// === case: fq_string_local ===
// multi-fix-expected
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.net.URLEncoder
class InputStandardCharsetsFqStringLocalSliceViolation {
	void m(byte[] data) throws Exception {
		final java.lang.String encoding = "UTF-8";
		final var a = "hello".getBytes(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var b = new String(data, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var c = URLEncoder.encode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
	}
}
// === end ===

// === case: fq_string_parameter ===
// multi-fix-expected
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.net.URLDecoder
class InputStandardCharsetsFqStringParameterSliceViolation {
	void m(byte[] data, java.lang.String encoding) throws Exception {
		final var a = "hello".getBytes(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var b = new String(data, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var c = URLDecoder.decode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
	}
}
// === end ===

// === case: iso_8859_1 ===
class InputStandardCharsetsIso88591SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("ISO-8859-1"); // violation [minSdk>=19]: Use 'StandardCharsets.ISO_8859_1' instead of "ISO-8859-1".
	}
}
// === end ===

// === case: iso_8859_1_latin1 ===
class InputStandardCharsetsIso88591Latin1SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("latin1"); // violation [minSdk>=19]: Use 'StandardCharsets.ISO_8859_1' instead of "latin1".
	}
}
// === end ===

// === case: other_charsets ===
class InputStandardCharsetsOtherCharsetsSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes("ISO-8859-1"); // violation [minSdk>=19]: Use 'StandardCharsets.ISO_8859_1' instead of "ISO-8859-1".
		final var b = "test".getBytes("latin1"); // violation [minSdk>=19]: Use 'StandardCharsets.ISO_8859_1' instead of "latin1".
		final var c = "test".getBytes("US-ASCII"); // violation [minSdk>=19]: Use 'StandardCharsets.US_ASCII' instead of "US-ASCII".
		final var d = "test".getBytes("ASCII"); // violation [minSdk>=19]: Use 'StandardCharsets.US_ASCII' instead of "ASCII".
		final var e = "test".getBytes("UTF-16"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16' instead of "UTF-16".
		final var f = "test".getBytes("UTF-16BE"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16BE' instead of "UTF-16BE".
		final var g = "test".getBytes("UTF-16LE"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16LE' instead of "UTF-16LE".
	}
}
// === end ===

// === case: string_field ===
// multi-fix-expected
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringFieldSliceViolation {
	static final String CHARSET_NAME = "UTF-8";

	void m(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var b = new String(data, CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var c = new String(data, 0, 1, CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var f = URLEncoder.encode("hello", CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var g = URLDecoder.decode("hello", CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var h = new ByteArrayOutputStream().toString(CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var i = new PrintStream(file, CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var k = new PrintWriter(file, CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
		final var l = new Scanner(new ByteArrayInputStream(data), CHARSET_NAME); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'CHARSET_NAME'.
	}
}
// === end ===

// === case: string_local ===
// multi-fix-expected
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringLocalSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final String encoding = "UTF-8";
		final var a = "hello".getBytes(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var b = new String(data, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var c = new String(data, 0, 1, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var f = URLEncoder.encode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var g = URLDecoder.decode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var h = new ByteArrayOutputStream().toString(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var i = new PrintStream(file, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var k = new PrintWriter(file, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var l = new Scanner(new ByteArrayInputStream(data), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
	}
}
// === end ===

// === case: string_parameter ===
// multi-fix-expected
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringParameterSliceViolation {
	void m(byte[] data, String encoding, File file) throws Exception {
		final var a = "hello".getBytes(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var b = new String(data, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var c = new String(data, 0, 1, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var f = URLEncoder.encode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var g = URLDecoder.decode("hello", encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var h = new ByteArrayOutputStream().toString(encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var i = new PrintStream(file, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var k = new PrintWriter(file, encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
		final var l = new Scanner(new ByteArrayInputStream(data), encoding); // violation [minSdk>=19]: Use a 'StandardCharsets' constant instead of String variable 'encoding'.
	}
}
// === end ===

// === case: supplementary_char_before_literal ===
class InputStandardCharsetsSupplementaryCharBeforeLiteralSliceViolation {
	void m(String s) throws Exception {
		final var bytes = ("𠀀" + s).getBytes("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
	}
}
// === end ===

// === case: us_ascii ===
class InputStandardCharsetsUsAsciiSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("US-ASCII"); // violation [minSdk>=19]: Use 'StandardCharsets.US_ASCII' instead of "US-ASCII".
	}
}
// === end ===

// === case: us_ascii_short ===
class InputStandardCharsetsUsAsciiShortSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("ASCII"); // violation [minSdk>=19]: Use 'StandardCharsets.US_ASCII' instead of "ASCII".
	}
}
// === end ===

// === case: utf8_alias_canonical ===
class InputStandardCharsetsUtf8CanonicalSliceViolation {
	void m() throws Exception {
		final var a = "hello".getBytes("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
	}
}
// === end ===

// === case: utf8_alias_capitalized ===
class InputStandardCharsetsUtf8CapitalizedSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, "Utf-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "Utf-8".
	}
}
// === end ===

// === case: utf8_alias_lowercase_hyphen ===
class InputStandardCharsetsUtf8LowercaseHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, "utf-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "utf-8".
	}
}
// === end ===

// === case: utf8_alias_lowercase_no_hyphen ===
class InputStandardCharsetsUtf8LowercaseNoHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, "utf8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "utf8".
	}
}
// === end ===

// === case: utf8_alias_uppercase_no_hyphen ===
class InputStandardCharsetsUtf8UppercaseNoHyphenSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes("UTF8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF8".
	}
}
// === end ===

// === case: utf_16 ===
class InputStandardCharsetsUtf16SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("UTF-16"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16' instead of "UTF-16".
	}
}
// === end ===

// === case: utf_16_be ===
class InputStandardCharsetsUtf16BeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("UTF-16BE"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16BE' instead of "UTF-16BE".
	}
}
// === end ===

// === case: utf_16_le ===
class InputStandardCharsetsUtf16LeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("UTF-16LE"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_16LE' instead of "UTF-16LE".
	}
}
// === end ===

// === case: utf_8 ===
class InputStandardCharsetsUtf8SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("UTF-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF-8".
	}
}
// === end ===

// === case: utf_8_alt ===
class InputStandardCharsetsUtf8AltSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("UTF8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "UTF8".
	}
}
// === end ===

// === case: utf_8_alt_lc ===
class InputStandardCharsetsUtf8AltLcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("utf8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "utf8".
	}
}
// === end ===

// === case: utf_8_cap ===
class InputStandardCharsetsUtf8CapSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("Utf-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "Utf-8".
	}
}
// === end ===

// === case: utf_8_lc ===
class InputStandardCharsetsUtf8LcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes("utf-8"); // violation [minSdk>=19]: Use 'StandardCharsets.UTF_8' instead of "utf-8".
	}
}
// === end ===