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
// imports: java.nio.charset.StandardCharsets
// imports: java.util.Scanner
class InputStandardCharsetsBuiltinFunctionsSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(StandardCharsets.UTF_8);
		final var b = new String(data, StandardCharsets.UTF_8);
		final var c = new String(data, 0, 1, StandardCharsets.UTF_8);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
		final var f = Charset.forName(StandardCharsets.UTF_8);
		final var g = URLEncoder.encode("hello", StandardCharsets.UTF_8);
		final var h = URLDecoder.decode("hello", StandardCharsets.UTF_8);
		final var i = new ByteArrayOutputStream().toString(StandardCharsets.UTF_8);
		final var j = new PrintStream(file, StandardCharsets.UTF_8);
		final var k = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
		final var l = new PrintWriter(file, StandardCharsets.UTF_8);
		final var m = new Scanner(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		final var n = new <String>String(data, StandardCharsets.UTF_8);
		final var o = new <String>ByteArrayOutputStream().toString(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: fq_string_local ===
// imports: java.net.URLEncoder
class InputStandardCharsetsFqStringLocalSliceViolation {
	void m(byte[] data) throws Exception {
		final var encoding = "UTF-8";
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLEncoder.encode("hello", encoding);
	}
}
// === end ===

// === case: fq_string_parameter ===
// imports: java.net.URLDecoder
class InputStandardCharsetsFqStringParameterSliceViolation {
	void m(byte[] data, java.lang.String encoding) throws Exception {
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLDecoder.decode("hello", encoding);
	}
}
// === end ===

// === case: iso_8859_1 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsIso88591SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
	}
}
// === end ===

// === case: iso_8859_1_latin1 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsIso88591Latin1SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
	}
}
// === end ===

// === case: other_charsets ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsOtherCharsetsSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes(StandardCharsets.ISO_8859_1);
		final var b = "test".getBytes(StandardCharsets.ISO_8859_1);
		final var c = "test".getBytes(StandardCharsets.US_ASCII);
		final var d = "test".getBytes(StandardCharsets.US_ASCII);
		final var e = "test".getBytes(StandardCharsets.UTF_16);
		final var f = "test".getBytes(StandardCharsets.UTF_16BE);
		final var g = "test".getBytes(StandardCharsets.UTF_16LE);
	}
}
// === end ===

// === case: string_field ===
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
		final var a = "hello".getBytes(CHARSET_NAME);
		final var b = new String(data, CHARSET_NAME);
		final var c = new String(data, 0, 1, CHARSET_NAME);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_NAME);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_NAME);
		final var f = URLEncoder.encode("hello", CHARSET_NAME);
		final var g = URLDecoder.decode("hello", CHARSET_NAME);
		final var h = new ByteArrayOutputStream().toString(CHARSET_NAME);
		final var i = new PrintStream(file, CHARSET_NAME);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_NAME);
		final var k = new PrintWriter(file, CHARSET_NAME);
		final var l = new Scanner(new ByteArrayInputStream(data), CHARSET_NAME);
	}
}
// === end ===

// === case: string_local ===
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
		final var encoding = "UTF-8";
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = new String(data, 0, 1, encoding);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding);
		final var f = URLEncoder.encode("hello", encoding);
		final var g = URLDecoder.decode("hello", encoding);
		final var h = new ByteArrayOutputStream().toString(encoding);
		final var i = new PrintStream(file, encoding);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding);
		final var k = new PrintWriter(file, encoding);
		final var l = new Scanner(new ByteArrayInputStream(data), encoding);
	}
}
// === end ===

// === case: string_parameter ===
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
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = new String(data, 0, 1, encoding);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding);
		final var f = URLEncoder.encode("hello", encoding);
		final var g = URLDecoder.decode("hello", encoding);
		final var h = new ByteArrayOutputStream().toString(encoding);
		final var i = new PrintStream(file, encoding);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding);
		final var k = new PrintWriter(file, encoding);
		final var l = new Scanner(new ByteArrayInputStream(data), encoding);
	}
}
// === end ===

// === case: supplementary_char_before_literal ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSupplementaryCharBeforeLiteralSliceViolation {
	void m(String s) throws Exception {
		final var bytes = ("𠀀" + s).getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: us_ascii ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUsAsciiSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.US_ASCII);
	}
}
// === end ===

// === case: us_ascii_short ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUsAsciiShortSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.US_ASCII);
	}
}
// === end ===

// === case: utf8_alias_canonical ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CanonicalSliceViolation {
	void m() throws Exception {
		final var a = "hello".getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_capitalized ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CapitalizedSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_lowercase_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LowercaseHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_lowercase_no_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LowercaseNoHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_uppercase_no_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8UppercaseNoHyphenSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_16 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16);
	}
}
// === end ===

// === case: utf_16_be ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16BeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16BE);
	}
}
// === end ===

// === case: utf_16_le ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16LeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16LE);
	}
}
// === end ===

// === case: utf_8 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_alt ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8AltSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_alt_lc ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8AltLcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_cap ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CapSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_lc ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===